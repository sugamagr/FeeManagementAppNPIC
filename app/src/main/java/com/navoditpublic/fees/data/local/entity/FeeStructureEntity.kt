package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Fee types:
 * - MONTHLY: For NC to 8th class (monthly fee with full year discount)
 * - ANNUAL: For 9th to 12th class (lump sum annual fee)
 * - ADMISSION: One-time admission fee for all classes
 * - REGISTRATION: One-time registration fee (for 9th-12th)
 */
enum class FeeType {
    MONTHLY,
    ANNUAL,
    ADMISSION,
    REGISTRATION
}

@Entity(
    tableName = "fee_structure",
    foreignKeys = [
        ForeignKey(
            entity = AcademicSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["class_name", "fee_type", "session_id"], unique = true)
    ]
)
data class FeeStructureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    
    @ColumnInfo(name = "class_name")
    val className: String, // NC, LKG, 1st, 9th, etc. or "ALL" for admission fee
    
    @ColumnInfo(name = "fee_type")
    val feeType: FeeType,
    
    val amount: Double,
    
    @ColumnInfo(name = "full_year_discount_months")
    val fullYearDiscountMonths: Int = 1, // For monthly fees: pay 12, get 1 month free
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)


