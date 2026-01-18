package com.navoditpublic.fees.domain.model

import com.navoditpublic.fees.data.local.entity.AcademicSessionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AcademicSession(
    val id: Long = 0,
    val sessionName: String,
    val startDate: Long,
    val endDate: Long,
    val isCurrent: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val startDateFormatted: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(startDate))
    
    val endDateFormatted: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(endDate))
    
    val displayText: String
        get() = "$sessionName ($startDateFormatted - $endDateFormatted)"
    
    fun toEntity(): AcademicSessionEntity = AcademicSessionEntity(
        id = id,
        sessionName = sessionName,
        startDate = startDate,
        endDate = endDate,
        isCurrent = isCurrent,
        isActive = isActive,
        createdAt = createdAt
    )
    
    companion object {
        fun fromEntity(entity: AcademicSessionEntity): AcademicSession = AcademicSession(
            id = entity.id,
            sessionName = entity.sessionName,
            startDate = entity.startDate,
            endDate = entity.endDate,
            isCurrent = entity.isCurrent,
            isActive = entity.isActive,
            createdAt = entity.createdAt
        )
    }
}


