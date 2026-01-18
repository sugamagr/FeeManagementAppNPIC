package com.navoditpublic.fees.domain.model

import com.navoditpublic.fees.data.local.entity.TransportEnrollmentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Domain model for transport enrollment.
 * Represents a period during which a student used transport.
 */
data class TransportEnrollment(
    val id: Long = 0,
    val studentId: Long,
    val routeId: Long,
    val startDate: Long,
    val endDate: Long? = null,
    val monthlyFeeAtEnrollment: Double,
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isActive: Boolean
        get() = endDate == null
    
    val formattedStartDate: String
        get() = formatDate(startDate)
    
    val formattedEndDate: String
        get() = endDate?.let { formatDate(it) } ?: "Active"
    
    val displayDateRange: String
        get() = "$formattedStartDate - $formattedEndDate"
    
    private fun formatDate(millis: Long): String {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return format.format(Date(millis))
    }
    
    fun toEntity(): TransportEnrollmentEntity = TransportEnrollmentEntity(
        id = id,
        studentId = studentId,
        routeId = routeId,
        startDate = startDate,
        endDate = endDate,
        monthlyFeeAtEnrollment = monthlyFeeAtEnrollment,
        remarks = remarks,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    companion object {
        fun fromEntity(entity: TransportEnrollmentEntity): TransportEnrollment = TransportEnrollment(
            id = entity.id,
            studentId = entity.studentId,
            routeId = entity.routeId,
            startDate = entity.startDate,
            endDate = entity.endDate,
            monthlyFeeAtEnrollment = entity.monthlyFeeAtEnrollment,
            remarks = entity.remarks,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}

/**
 * Transport enrollment with route details for display
 */
data class TransportEnrollmentWithRoute(
    val enrollment: TransportEnrollment,
    val routeName: String,
    val currentRouteFee: Double
)

