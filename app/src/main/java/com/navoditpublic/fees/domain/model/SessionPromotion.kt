package com.navoditpublic.fees.domain.model

import com.navoditpublic.fees.data.local.entity.PromotionStatus
import com.navoditpublic.fees.data.local.entity.SessionPromotionEntity

/**
 * Domain model for session promotion.
 */
data class SessionPromotion(
    val id: Long = 0,
    val sourceSessionId: Long,
    val targetSessionId: Long,
    val status: PromotionStatus = PromotionStatus.COMPLETED,
    
    // Options
    val copiedFeeStructures: Boolean = false,
    val carriedForwardDues: Boolean = false,
    val promotedClasses: Boolean = false,
    val deactivated12thStudents: Boolean = false,
    val addedTuitionFees: Boolean = false,
    val addedTransportFees: Boolean = false,
    val setAsCurrent: Boolean = false,
    
    // Results
    val feeStructuresCopiedCount: Int = 0,
    val studentsWithDuesCarried: Int = 0,
    val totalDuesCarriedForward: Double = 0.0,
    val studentsPromoted: Int = 0,
    val studentsDeactivated: Int = 0,
    val studentsWithFeesAdded: Int = 0,
    val totalFeesAdded: Double = 0.0,
    
    // Timestamps
    val promotedAt: Long = System.currentTimeMillis(),
    val revertedAt: Long? = null,
    val revertReason: String? = null
) {
    val isReverted: Boolean
        get() = status == PromotionStatus.REVERTED
    
    fun toEntity(): SessionPromotionEntity = SessionPromotionEntity(
        id = id,
        sourceSessionId = sourceSessionId,
        targetSessionId = targetSessionId,
        status = status,
        copiedFeeStructures = copiedFeeStructures,
        carriedForwardDues = carriedForwardDues,
        promotedClasses = promotedClasses,
        deactivated12thStudents = deactivated12thStudents,
        addedTuitionFees = addedTuitionFees,
        addedTransportFees = addedTransportFees,
        setAsCurrent = setAsCurrent,
        feeStructuresCopiedCount = feeStructuresCopiedCount,
        studentsWithDuesCarried = studentsWithDuesCarried,
        totalDuesCarriedForward = totalDuesCarriedForward,
        studentsPromoted = studentsPromoted,
        studentsDeactivated = studentsDeactivated,
        studentsWithFeesAdded = studentsWithFeesAdded,
        totalFeesAdded = totalFeesAdded,
        promotedAt = promotedAt,
        revertedAt = revertedAt,
        revertReason = revertReason
    )
    
    companion object {
        fun fromEntity(entity: SessionPromotionEntity): SessionPromotion = SessionPromotion(
            id = entity.id,
            sourceSessionId = entity.sourceSessionId,
            targetSessionId = entity.targetSessionId,
            status = entity.status,
            copiedFeeStructures = entity.copiedFeeStructures,
            carriedForwardDues = entity.carriedForwardDues,
            promotedClasses = entity.promotedClasses,
            deactivated12thStudents = entity.deactivated12thStudents,
            addedTuitionFees = entity.addedTuitionFees,
            addedTransportFees = entity.addedTransportFees,
            setAsCurrent = entity.setAsCurrent,
            feeStructuresCopiedCount = entity.feeStructuresCopiedCount,
            studentsWithDuesCarried = entity.studentsWithDuesCarried,
            totalDuesCarriedForward = entity.totalDuesCarriedForward,
            studentsPromoted = entity.studentsPromoted,
            studentsDeactivated = entity.studentsDeactivated,
            studentsWithFeesAdded = entity.studentsWithFeesAdded,
            totalFeesAdded = entity.totalFeesAdded,
            promotedAt = entity.promotedAt,
            revertedAt = entity.revertedAt,
            revertReason = entity.revertReason
        )
    }
}

/**
 * Options for session promotion
 */
data class PromotionOptions(
    val copyFeeStructures: Boolean = true,
    val carryForwardDues: Boolean = true,
    val promoteClasses: Boolean = true,
    val deactivate12thStudents: Boolean = false,
    val addTuitionFees: Boolean = true,
    val addTransportFees: Boolean = true,
    val setAsCurrent: Boolean = true
)

/**
 * Progress updates during promotion
 */
data class PromotionProgress(
    val currentStep: String,
    val percentComplete: Int,
    val isComplete: Boolean = false,
    val currentBatch: Int = 0,
    val totalBatches: Int = 0,
    val error: String? = null
)

/**
 * Result of session promotion
 */
data class PromotionResult(
    val success: Boolean = true,
    val feeStructuresCopied: Int = 0,
    val duesCarriedForward: Double = 0.0,
    val studentsWithDuesCarried: Int = 0,
    val studentsDeactivated: Int = 0,
    val studentsPromoted: Int = 0,
    val totalFeesAdded: Double = 0.0,
    val studentsWithFeesAdded: Int = 0,
    val errorMessage: String? = null
)

/**
 * Preview data before promotion
 */
data class PromotionPreview(
    val classWiseCounts: Map<String, Int> = emptyMap(),
    val totalStudents: Int = 0,
    val studentsIn12th: Int = 0,
    val studentsWithDues: Int = 0,
    val totalDuesAmount: Double = 0.0,
    val studentsWithTransport: Int = 0,
    val feeStructuresCount: Int = 0
)

/**
 * Safety check result before reverting
 */
data class RevertSafetyCheck(
    val canRevertSafely: Boolean,
    val receiptsInNewSession: Int = 0,
    val receiptsAmount: Double = 0.0,
    val studentsAddedAfterPromotion: Int = 0,
    val warnings: List<String> = emptyList()
)

/**
 * Result of revert operation
 */
data class RevertResult(
    val success: Boolean = true,
    val classesReverted: Int = 0,
    val studentsReactivated: Int = 0,
    val feeEntriesDeleted: Int = 0,
    val openingBalanceEntriesDeleted: Int = 0,
    val feeStructuresDeleted: Int = 0,
    val receiptsDeleted: Int = 0,
    val studentsDeleted: Int = 0,
    val errorMessage: String? = null
)
