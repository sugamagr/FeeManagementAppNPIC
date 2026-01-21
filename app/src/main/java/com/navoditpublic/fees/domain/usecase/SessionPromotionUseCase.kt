package com.navoditpublic.fees.domain.usecase

import com.navoditpublic.fees.data.local.entity.PromotionStatus
import com.navoditpublic.fees.domain.model.PromotionOptions
import com.navoditpublic.fees.domain.model.PromotionPreview
import com.navoditpublic.fees.domain.model.PromotionProgress
import com.navoditpublic.fees.domain.model.PromotionResult
import com.navoditpublic.fees.domain.model.SessionPromotion
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.util.ClassUtils
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for session promotion operations.
 * Handles the complete promotion flow including:
 * - Copying fee structures
 * - Carrying forward dues
 * - Promoting classes
 * - Deactivating 12th class students
 * - Adding session fees
 * - Setting current session
 */
@Singleton
class SessionPromotionUseCase @Inject constructor(
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository
) {
    // Chunk size for batch operations (Firebase-ready: keeps under 500 ops limit)
    private val CHUNK_SIZE = 50
    
    /**
     * Get preview data before promotion
     */
    suspend fun getPromotionPreview(sourceSessionId: Long): PromotionPreview {
        val classWiseCounts = studentRepository.getClassWiseStudentCounts()
        val totalStudents = studentRepository.getActiveStudentCountSync()
        val studentsIn12th = studentRepository.get12thClassStudentCount()
        val studentsWithDues = feeRepository.getStudentsWithDuesCount()
        val totalDuesAmount = feeRepository.getTotalPendingDuesSync()
        val feeStructuresCount = feeRepository.getFeeStructureCountForSession(sourceSessionId)
        
        // Count students with transport
        val studentsWithTransport = studentRepository.getAllActiveStudents().first()
            .count { it.hasTransport }
        
        return PromotionPreview(
            classWiseCounts = classWiseCounts,
            totalStudents = totalStudents,
            studentsIn12th = studentsIn12th,
            studentsWithDues = studentsWithDues,
            totalDuesAmount = totalDuesAmount,
            studentsWithTransport = studentsWithTransport,
            feeStructuresCount = feeStructuresCount
        )
    }
    
    /**
     * Execute the complete promotion process
     */
    suspend fun execute(
        sourceSessionId: Long,
        targetSessionId: Long,
        options: PromotionOptions,
        onProgress: suspend (PromotionProgress) -> Unit
    ): Result<PromotionResult> {
        return try {
            var result = PromotionResult()
            
            // Check if target session was already promoted (idempotency check)
            if (settingsRepository.wasSessionPromoted(targetSessionId)) {
                return Result.failure(
                    IllegalStateException("This session was already created via promotion. Cannot promote again.")
                )
            }
            
            // Get source and target sessions
            val sourceSession = settingsRepository.getSessionById(sourceSessionId)
                ?: return Result.failure(IllegalArgumentException("Source session not found"))
            val targetSession = settingsRepository.getSessionById(targetSessionId)
                ?: return Result.failure(IllegalArgumentException("Target session not found"))
            
            // Step 1: Copy fee structures (5%)
            if (options.copyFeeStructures) {
                onProgress(PromotionProgress("Copying fee structures...", 5))
                val copyResult = feeRepository.copyFeeStructures(sourceSessionId, targetSessionId)
                copyResult.onSuccess { count ->
                    result = result.copy(feeStructuresCopied = count)
                }.onFailure { e ->
                    return Result.failure(e)
                }
            }
            
            // Step 2: Carry forward dues (15-35%)
            if (options.carryForwardDues) {
                onProgress(PromotionProgress("Carrying forward dues...", 15))
                val carryResult = feeRepository.carryForwardDuesForAllStudents(
                    newSessionId = targetSessionId,
                    sessionStartDate = targetSession.startDate
                )
                carryResult.onSuccess { (count, amount) ->
                    result = result.copy(
                        studentsWithDuesCarried = count,
                        duesCarriedForward = amount
                    )
                }.onFailure { e ->
                    return Result.failure(e)
                }
                onProgress(PromotionProgress("Dues carried forward: ${result.studentsWithDuesCarried} students", 35))
            }
            
            // Step 3: Deactivate 12th students (40%)
            // Their account numbers are prefixed with PASS<sessionCode>- (e.g., "5" → "PASS2425-5")
            // to free up the original number for new students
            if (options.deactivate12thStudents) {
                onProgress(PromotionProgress("Deactivating passed-out students...", 40))
                val deactivatedCount = studentRepository.deactivatePassedOutStudents(sourceSession.sessionName)
                result = result.copy(studentsDeactivated = deactivatedCount)
            }
            
            // Step 4: Promote classes (45-60%)
            if (options.promoteClasses) {
                onProgress(PromotionProgress("Promoting classes...", 45))
                var totalPromoted = 0
                
                // Promote in order from highest to lowest to avoid conflicts
                // e.g., promote 11th to 12th first, then 10th to 11th, etc.
                val classesInOrder = ClassUtils.PROMOTABLE_CLASSES.reversed()
                
                for ((index, currentClass) in classesInOrder.withIndex()) {
                    val nextClass = ClassUtils.getNextClass(currentClass)
                    if (nextClass != null) {
                        val promoted = studentRepository.promoteClass(currentClass, nextClass)
                        totalPromoted += promoted
                        
                        val progress = 45 + ((index + 1) * 15 / classesInOrder.size)
                        onProgress(PromotionProgress("Promoted $currentClass → $nextClass ($promoted students)", progress))
                    }
                }
                
                result = result.copy(studentsPromoted = totalPromoted)
            }
            
            // Step 5: Add session fees (65-90%)
            if (options.addTuitionFees || options.addTransportFees) {
                onProgress(PromotionProgress("Adding session fees...", 65))
                
                val feesResult = feeRepository.addSessionFeesForAllStudents(
                    sessionId = targetSessionId,
                    addTuition = options.addTuitionFees,
                    addTransport = options.addTransportFees
                )
                
                feesResult.onSuccess { (count, amount) ->
                    result = result.copy(
                        studentsWithFeesAdded = count,
                        totalFeesAdded = amount
                    )
                }.onFailure { e ->
                    return Result.failure(e)
                }
                
                onProgress(PromotionProgress("Fees added for ${result.studentsWithFeesAdded} students", 90))
            }
            
            // Step 6: Set as current session (95%)
            if (options.setAsCurrent) {
                onProgress(PromotionProgress("Setting current session...", 95))
                settingsRepository.setCurrentSession(targetSessionId).onFailure { e ->
                    return Result.failure(e)
                }
            }
            
            // Save promotion record
            val promotion = SessionPromotion(
                sourceSessionId = sourceSessionId,
                targetSessionId = targetSessionId,
                status = PromotionStatus.COMPLETED,
                copiedFeeStructures = options.copyFeeStructures,
                carriedForwardDues = options.carryForwardDues,
                promotedClasses = options.promoteClasses,
                deactivated12thStudents = options.deactivate12thStudents,
                addedTuitionFees = options.addTuitionFees,
                addedTransportFees = options.addTransportFees,
                setAsCurrent = options.setAsCurrent,
                feeStructuresCopiedCount = result.feeStructuresCopied,
                studentsWithDuesCarried = result.studentsWithDuesCarried,
                totalDuesCarriedForward = result.duesCarriedForward,
                studentsPromoted = result.studentsPromoted,
                studentsDeactivated = result.studentsDeactivated,
                studentsWithFeesAdded = result.studentsWithFeesAdded,
                totalFeesAdded = result.totalFeesAdded,
                promotedAt = System.currentTimeMillis()
            )
            
            settingsRepository.saveSessionPromotion(promotion).onFailure { e ->
                // Non-critical: promotion succeeded but record wasn't saved
                // Log and add warning to result
                android.util.Log.e("SessionPromotion", "Failed to save promotion record: ${e.message}")
                result = result.copy(
                    warnings = result.warnings + "Promotion record could not be saved. Re-promoting this session may cause issues."
                )
            }
            
            onProgress(PromotionProgress("Promotion complete!", 100, isComplete = true))
            
            Result.success(result)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
