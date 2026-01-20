package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Status of a session promotion
 */
enum class PromotionStatus {
    COMPLETED,  // Promotion was successful
    REVERTED    // Promotion was undone
}

/**
 * Entity to track session promotions for auditing and revert functionality.
 * Each promotion from one session to another is recorded here.
 */
@Entity(
    tableName = "session_promotions",
    foreignKeys = [
        ForeignKey(
            entity = AcademicSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["source_session_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AcademicSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["target_session_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["source_session_id"]),
        Index(value = ["target_session_id"]),
        Index(value = ["status"])
    ]
)
data class SessionPromotionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "source_session_id")
    val sourceSessionId: Long,  // From which session (e.g., 2024-25)
    
    @ColumnInfo(name = "target_session_id")
    val targetSessionId: Long,  // To which session (e.g., 2025-26)
    
    @ColumnInfo(name = "status")
    val status: PromotionStatus = PromotionStatus.COMPLETED,
    
    // Options that were selected during promotion
    @ColumnInfo(name = "copied_fee_structures")
    val copiedFeeStructures: Boolean = false,
    
    @ColumnInfo(name = "carried_forward_dues")
    val carriedForwardDues: Boolean = false,
    
    @ColumnInfo(name = "promoted_classes")
    val promotedClasses: Boolean = false,
    
    @ColumnInfo(name = "deactivated_12th_students")
    val deactivated12thStudents: Boolean = false,
    
    @ColumnInfo(name = "added_tuition_fees")
    val addedTuitionFees: Boolean = false,
    
    @ColumnInfo(name = "added_transport_fees")
    val addedTransportFees: Boolean = false,
    
    @ColumnInfo(name = "set_as_current")
    val setAsCurrent: Boolean = false,
    
    // Results/Statistics
    @ColumnInfo(name = "fee_structures_copied_count")
    val feeStructuresCopiedCount: Int = 0,
    
    @ColumnInfo(name = "students_with_dues_carried")
    val studentsWithDuesCarried: Int = 0,
    
    @ColumnInfo(name = "total_dues_carried_forward")
    val totalDuesCarriedForward: Double = 0.0,
    
    @ColumnInfo(name = "students_promoted")
    val studentsPromoted: Int = 0,
    
    @ColumnInfo(name = "students_deactivated")
    val studentsDeactivated: Int = 0,
    
    @ColumnInfo(name = "students_with_fees_added")
    val studentsWithFeesAdded: Int = 0,
    
    @ColumnInfo(name = "total_fees_added")
    val totalFeesAdded: Double = 0.0,
    
    // Timestamps
    @ColumnInfo(name = "promoted_at")
    val promotedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "reverted_at")
    val revertedAt: Long? = null,
    
    @ColumnInfo(name = "revert_reason")
    val revertReason: String? = null
)
