package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.navoditpublic.fees.domain.model.TransportFeeHelper

@Entity(
    tableName = "transport_routes",
    indices = [
        Index(value = ["route_name"], unique = true)
    ]
)
data class TransportRouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "route_name")
    val routeName: String,
    
    // Legacy field - kept for backward compatibility, use class-wise fees
    @ColumnInfo(name = "monthly_fee")
    val monthlyFee: Double = 0.0,
    
    // Class-wise fees: NC to 5th
    @ColumnInfo(name = "fee_nc_to_5")
    val feeNcTo5: Double = 0.0,
    
    // Class-wise fees: 6th to 8th
    @ColumnInfo(name = "fee_6_to_8")
    val fee6To8: Double = 0.0,
    
    // Class-wise fees: 9th to 12th
    @ColumnInfo(name = "fee_9_to_12")
    val fee9To12: Double = 0.0,
    
    val description: String = "",
    
    @ColumnInfo(name = "stops")
    val stops: String = "", // Comma-separated stops
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "is_closed")
    val isClosed: Boolean = false, // Route closed but history preserved
    
    @ColumnInfo(name = "closed_date")
    val closedDate: Long? = null, // When the route was closed
    
    @ColumnInfo(name = "close_reason")
    val closeReason: String? = null, // Reason for closing
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Get the appropriate fee based on student's class
     * Delegates to shared TransportFeeHelper to avoid code duplication
     */
    fun getFeeForClass(className: String): Double {
        return TransportFeeHelper.getFeeForClass(className, feeNcTo5, fee6To8, fee9To12)
    }
}

