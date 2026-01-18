package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PaymentMode {
    CASH,
    ONLINE
}

@Entity(
    tableName = "receipts",
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
        Index(value = ["receipt_number"], unique = true),
        Index(value = ["student_id"]),
        Index(value = ["session_id"]),
        Index(value = ["receipt_date"]),
        Index(value = ["is_cancelled"])
    ]
)
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "receipt_number")
    val receiptNumber: Int, // Manual entry: 1, 2, 3...
    
    @ColumnInfo(name = "student_id")
    val studentId: Long,
    
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    
    @ColumnInfo(name = "receipt_date")
    val receiptDate: Long, // Manual entry date
    
    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,
    
    @ColumnInfo(name = "discount_amount")
    val discountAmount: Double = 0.0,
    
    @ColumnInfo(name = "net_amount")
    val netAmount: Double, // totalAmount - discountAmount
    
    @ColumnInfo(name = "payment_mode")
    val paymentMode: PaymentMode,
    
    @ColumnInfo(name = "online_reference")
    val onlineReference: String? = null,
    
    val remarks: String = "",
    
    @ColumnInfo(name = "is_cancelled")
    val isCancelled: Boolean = false,
    
    @ColumnInfo(name = "cancelled_at")
    val cancelledAt: Long? = null,
    
    @ColumnInfo(name = "cancellation_reason")
    val cancellationReason: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)


