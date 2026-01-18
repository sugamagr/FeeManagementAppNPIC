package com.navoditpublic.fees.domain.model

import com.navoditpublic.fees.data.local.entity.TransportRouteEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shared utility for transport fee calculation.
 * Used by both domain model and entity to avoid duplication (DRY principle).
 */
object TransportFeeHelper {
    private val CLASSES_NC_TO_5 = listOf("NC", "LKG", "UKG", "1st", "2nd", "3rd", "4th", "5th")
    private val CLASSES_6_TO_8 = listOf("6th", "7th", "8th")
    private val CLASSES_9_TO_12 = listOf("9th", "10th", "11th", "12th")
    
    /**
     * Get the appropriate fee based on student's class
     */
    fun getFeeForClass(className: String, feeNcTo5: Double, fee6To8: Double, fee9To12: Double): Double {
        return when {
            className in CLASSES_NC_TO_5 -> feeNcTo5
            className in CLASSES_6_TO_8 -> fee6To8
            className in CLASSES_9_TO_12 -> fee9To12
            else -> feeNcTo5 // Default to lowest
        }
    }
}

data class TransportRoute(
    val id: Long = 0,
    val routeName: String,
    val monthlyFee: Double = 0.0, // Legacy, use class-wise fees
    val feeNcTo5: Double = 0.0,   // NC, LKG, UKG, 1st-5th
    val fee6To8: Double = 0.0,    // 6th-8th
    val fee9To12: Double = 0.0,   // 9th-12th
    val description: String = "",
    val stops: String = "",
    val isActive: Boolean = true,
    val isClosed: Boolean = false,
    val closedDate: Long? = null,
    val closeReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val stopsList: List<String>
        get() = stops.split(",").map { it.trim() }.filter { it.isNotBlank() }
    
    val displayText: String
        get() = if (isClosed) {
            "$routeName (Closed)"
        } else {
            "$routeName (₹${feeNcTo5.toInt()}-${fee9To12.toInt()})"
        }
    
    val closedDateFormatted: String?
        get() = closedDate?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
        }
    
    val statusText: String
        get() = when {
            isClosed -> "Closed"
            !isActive -> "Inactive"
            else -> "Active"
        }
    
    /**
     * Get the appropriate fee based on student's class
     */
    fun getFeeForClass(className: String): Double {
        return TransportFeeHelper.getFeeForClass(className, feeNcTo5, fee6To8, fee9To12)
    }
    
    fun toEntity(): TransportRouteEntity = TransportRouteEntity(
        id = id,
        routeName = routeName,
        monthlyFee = feeNcTo5, // Legacy compatibility
        feeNcTo5 = feeNcTo5,
        fee6To8 = fee6To8,
        fee9To12 = fee9To12,
        description = description,
        stops = stops,
        isActive = isActive,
        isClosed = isClosed,
        closedDate = closedDate,
        closeReason = closeReason,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    companion object {
        fun fromEntity(entity: TransportRouteEntity): TransportRoute = TransportRoute(
            id = entity.id,
            routeName = entity.routeName,
            monthlyFee = entity.monthlyFee,
            feeNcTo5 = entity.feeNcTo5,
            fee6To8 = entity.fee6To8,
            fee9To12 = entity.fee9To12,
            description = entity.description,
            stops = entity.stops,
            isActive = entity.isActive,
            isClosed = entity.isClosed,
            closedDate = entity.closedDate,
            closeReason = entity.closeReason,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}

