package com.navoditpublic.fees.domain.usecase

import com.navoditpublic.fees.domain.model.PromotionProgress
import com.navoditpublic.fees.domain.model.RevertResult
import com.navoditpublic.fees.domain.model.RevertSafetyCheck
import com.navoditpublic.fees.domain.model.SessionPromotion
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.util.ClassUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for reverting a session promotion.
 * Handles safety checks and the complete revert process.
 */
@Singleton
class RevertSessionPromotionUseCase @Inject constructor(
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository
) {
    
    /**
     * Check if it's safe to revert the promotion.
     * Returns information about any changes made after promotion.
     */
    suspend fun checkRevertSafety(promotion: SessionPromotion): RevertSafetyCheck {
        val warnings = mutableListOf<String>()
        
        // Check for receipts in the new session
        val receiptsCount = feeRepository.getReceiptCountForSession(promotion.targetSessionId)
        val receiptsAmount = feeRepository.getTotalCollectionForSession(promotion.targetSessionId)
        
        if (receiptsCount > 0) {
            warnings.add("$receiptsCount receipts (₹${String.format("%.0f", receiptsAmount)}) will be permanently deleted")
        }
        
        // Check for students added after promotion
        val studentsAdded = studentRepository.getStudentsAddedAfter(promotion.promotedAt)
        
        if (studentsAdded.isNotEmpty()) {
            warnings.add("${studentsAdded.size} students added after promotion will be permanently deleted")
        }
        
        // Check for account number conflicts if 12th students were deactivated
        var accountNumberConflicts = 0
        if (promotion.deactivated12thStudents) {
            val sourceSession = settingsRepository.getSessionById(promotion.sourceSessionId)
            if (sourceSession != null) {
                accountNumberConflicts = studentRepository.getPassedOutStudentsWithConflictsCount(sourceSession.sessionName)
                if (accountNumberConflicts > 0) {
                    warnings.add("$accountNumberConflicts passed-out students have account number conflicts with new students")
                }
            }
        }
        
        // Can revert safely only if no changes were made AND no account number conflicts exist
        // Account number conflicts would cause database constraint violations during reactivation
        val canRevertSafely = receiptsCount == 0 && studentsAdded.isEmpty() && accountNumberConflicts == 0
        
        if (!canRevertSafely) {
            warnings.add("This action cannot be undone!")
        }
        
        return RevertSafetyCheck(
            canRevertSafely = canRevertSafely,
            receiptsInNewSession = receiptsCount,
            receiptsAmount = receiptsAmount,
            studentsAddedAfterPromotion = studentsAdded.size,
            accountNumberConflicts = accountNumberConflicts,
            warnings = warnings
        )
    }
    
    /**
     * Execute the revert process.
     * 
     * @param promotion The promotion record to revert
     * @param forceDelete If true, deletes post-promotion data (receipts, students)
     * @param reason Optional reason for the revert
     * @param onProgress Progress callback
     */
    suspend fun execute(
        promotion: SessionPromotion,
        forceDelete: Boolean,
        reason: String? = null,
        onProgress: suspend (PromotionProgress) -> Unit
    ): Result<RevertResult> {
        return try {
            var result = RevertResult()
            
            // Safety check
            val safetyCheck = checkRevertSafety(promotion)
            if (!safetyCheck.canRevertSafely && !forceDelete) {
                return Result.failure(
                    IllegalStateException("Cannot revert safely. Use forceDelete=true to proceed.")
                )
            }
            
            // Step 1: Delete post-promotion receipts and their ledger entries (if forced) - 10%
            if (forceDelete && safetyCheck.receiptsInNewSession > 0) {
                onProgress(PromotionProgress("Deleting post-promotion receipts...", 10))
                // Delete receipt ledger entries (CREDIT entries) first
                feeRepository.deleteReceiptLedgerEntriesForSession(promotion.targetSessionId)
                // Then delete receipts and their items
                feeRepository.deleteReceiptsForSession(promotion.targetSessionId).onSuccess { count ->
                    result = result.copy(receiptsDeleted = count)
                }
            }
            
            // Step 2: Delete post-promotion students (if forced) - 20%
            if (forceDelete && safetyCheck.studentsAddedAfterPromotion > 0) {
                onProgress(PromotionProgress("Deleting post-promotion students...", 20))
                val deleted = studentRepository.deleteStudentsAddedAfter(promotion.promotedAt)
                result = result.copy(studentsDeleted = deleted)
            }
            
            // Step 3: Delete session fee entries - 30%
            if (promotion.addedTuitionFees || promotion.addedTransportFees) {
                onProgress(PromotionProgress("Removing session fees...", 30))
                feeRepository.deleteFeeChargeEntriesForSession(promotion.targetSessionId).onSuccess { count ->
                    result = result.copy(feeEntriesDeleted = count)
                }
            }
            
            // Step 4: Delete carried forward dues - 40%
            if (promotion.carriedForwardDues) {
                onProgress(PromotionProgress("Removing carried forward dues...", 40))
                feeRepository.deleteOpeningBalanceEntriesForSession(promotion.targetSessionId).onSuccess { count ->
                    result = result.copy(openingBalanceEntriesDeleted = count)
                }
            }
            
            // Step 5: Demote classes FIRST (reverse promotion) - 50-70%
            // IMPORTANT: Must demote BEFORE reactivating 12th students
            // Otherwise we'd also demote the reactivated students
            if (promotion.promotedClasses) {
                onProgress(PromotionProgress("Reverting class promotions...", 50))
                var totalDemoted = 0
                
                // Demote in order from lowest to highest
                // e.g., demote LKG to NC first, then UKG to LKG, etc.
                val classesInOrder = ClassUtils.DEMOTABLE_CLASSES
                
                for ((index, currentClass) in classesInOrder.withIndex()) {
                    val previousClass = ClassUtils.getPreviousClass(currentClass)
                    if (previousClass != null) {
                        val demoted = studentRepository.demoteClass(currentClass, previousClass)
                        totalDemoted += demoted
                        
                        val progress = 50 + ((index + 1) * 20 / classesInOrder.size)
                        onProgress(PromotionProgress("Demoted $currentClass → $previousClass ($demoted students)", progress))
                    }
                }
                
                result = result.copy(classesReverted = totalDemoted)
            }
            
            // Step 6: Reactivate 12th students AFTER demotion - 75%
            // After demotion, only the originally deactivated graduates remain in 12th
            // Restore their original account numbers by removing the PASS prefix
            if (promotion.deactivated12thStudents && promotion.studentsDeactivated > 0) {
                onProgress(PromotionProgress("Reactivating passed-out students...", 75))
                
                // Get source session name to know which prefix to remove
                val sourceSession = settingsRepository.getSessionById(promotion.sourceSessionId)
                val reactivated = if (sourceSession != null) {
                    // Use new method that restores original account numbers
                    studentRepository.reactivatePassedOutStudentsAndRestoreAccountNumbers(sourceSession.sessionName)
                } else {
                    // Fallback: just reactivate without restoring A/C (shouldn't happen)
                    studentRepository.reactivateStudentsByClass("12th")
                }
                result = result.copy(studentsReactivated = reactivated)
            }
            
            // Step 7: Delete fee structures - 80%
            if (promotion.copiedFeeStructures) {
                onProgress(PromotionProgress("Deleting fee structures...", 80))
                feeRepository.deleteFeeStructuresForSession(promotion.targetSessionId).onSuccess { count ->
                    result = result.copy(feeStructuresDeleted = count)
                }
            }
            
            // Step 8: Reset current session - 90%
            if (promotion.setAsCurrent) {
                onProgress(PromotionProgress("Resetting current session...", 90))
                settingsRepository.setCurrentSession(promotion.sourceSessionId)
            }
            
            // Step 9: Mark promotion as reverted - 95%
            onProgress(PromotionProgress("Marking promotion as reverted...", 95))
            settingsRepository.markPromotionAsReverted(promotion.id, reason)
            
            onProgress(PromotionProgress("Revert complete!", 100, isComplete = true))
            
            Result.success(result.copy(success = true))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
