package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity to track transport fee changes over time.
 * Each route can have multiple fee entries with different effective dates.
 * When fees change, a new record is created with the effective date.
 * The most recent entry before or on a given date is used for calculations.
 * 
 * Stores all 3 fee tiers for accurate historical tracking:
 * - NC to 5th class
 * - 6th to 8th class
 * - 9th to 12th class
 */
@Entity(
    tableName = "transport_fee_history",
    foreignKeys = [
        ForeignKey(
            entity = TransportRouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["route_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["route_id"]),
        Index(value = ["route_id", "effective_from"], unique = true)
    ]
)
data class TransportFeeHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "route_id")
    val routeId: Long,
    
    // Legacy field - kept for backward compatibility, contains NC-5 fee
    @ColumnInfo(name = "monthly_fee")
    val monthlyFee: Double,
    
    // Class-wise fees for accurate historical tracking
    @ColumnInfo(name = "fee_nc_to_5", defaultValue = "0.0")
    val feeNcTo5: Double = 0.0,
    
    @ColumnInfo(name = "fee_6_to_8", defaultValue = "0.0")
    val fee6To8: Double = 0.0,
    
    @ColumnInfo(name = "fee_9_to_12", defaultValue = "0.0")
    val fee9To12: Double = 0.0,
    
    @ColumnInfo(name = "effective_from")
    val effectiveFrom: Long, // Date from when this fee applies
    
    @ColumnInfo(name = "notes")
    val notes: String = "", // e.g., "Session 2025-26 fee update"
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)


