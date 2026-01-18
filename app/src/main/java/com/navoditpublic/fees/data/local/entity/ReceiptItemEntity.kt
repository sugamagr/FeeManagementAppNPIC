package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipt_items",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptEntity::class,
            parentColumns = ["id"],
            childColumns = ["receipt_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["receipt_id"])
    ]
)
data class ReceiptItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "receipt_id")
    val receiptId: Long,
    
    @ColumnInfo(name = "fee_type")
    val feeType: String, // MONTHLY, ANNUAL, ADMISSION, REGISTRATION, TRANSPORT
    
    val description: String, // "Monthly Fee - April 2025", "Transport - May 2025"
    
    @ColumnInfo(name = "month_year")
    val monthYear: String? = null, // "04-2025" for monthly fees
    
    val amount: Double,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)


