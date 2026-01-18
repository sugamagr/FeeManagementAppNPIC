package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class LedgerEntryType {
    DEBIT,  // Fee charged
    CREDIT  // Payment received
}

enum class LedgerReferenceType {
    FEE_CHARGE,       // Monthly/Annual fee charged
    RECEIPT,          // Payment via receipt
    ADJUSTMENT,       // Manual adjustment
    REVERSAL,         // Cancelled receipt reversal
    OPENING_BALANCE,  // Previous dues carried forward
    DISCOUNT          // Full year discount
}

@Entity(
    tableName = "ledger_entries",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AcademicSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["student_id"]),
        Index(value = ["session_id"]),
        Index(value = ["entry_date"]),
        Index(value = ["reference_type", "reference_id"])
    ]
)
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "student_id")
    val studentId: Long,
    
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    
    @ColumnInfo(name = "entry_date")
    val entryDate: Long,
    
    val particulars: String, // Description like "Monthly Fee - April 2025"
    
    @ColumnInfo(name = "entry_type")
    val entryType: LedgerEntryType,
    
    @ColumnInfo(name = "debit_amount")
    val debitAmount: Double = 0.0, // Amount if DEBIT
    
    @ColumnInfo(name = "credit_amount")
    val creditAmount: Double = 0.0, // Amount if CREDIT
    
    val balance: Double, // Running balance after this entry
    
    @ColumnInfo(name = "reference_type")
    val referenceType: LedgerReferenceType,
    
    @ColumnInfo(name = "reference_id")
    val referenceId: Long? = null, // Link to receipt or charge
    
    @ColumnInfo(name = "folio_number")
    val folioNumber: String? = null, // C.B. Folio (optional)
    
    @ColumnInfo(name = "is_reversed")
    val isReversed: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)


