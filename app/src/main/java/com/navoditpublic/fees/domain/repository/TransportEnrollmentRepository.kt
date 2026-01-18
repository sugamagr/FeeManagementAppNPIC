package com.navoditpublic.fees.domain.repository

import com.navoditpublic.fees.domain.model.TransportEnrollment
import com.navoditpublic.fees.domain.model.TransportEnrollmentWithRoute
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Transport Enrollment operations.
 * Tracks when students start/stop using transport.
 */
interface TransportEnrollmentRepository {
    
    suspend fun insert(enrollment: TransportEnrollment): Result<Long>
    
    suspend fun update(enrollment: TransportEnrollment): Result<Unit>
    
    suspend fun delete(enrollment: TransportEnrollment): Result<Unit>
    
    suspend fun getById(id: Long): TransportEnrollment?
    
    fun getByStudentId(studentId: Long): Flow<List<TransportEnrollment>>
    
    suspend fun getByStudentIdSync(studentId: Long): List<TransportEnrollment>
    
    suspend fun getActiveEnrollment(studentId: Long): TransportEnrollment?
    
    /**
     * Get all enrollments for a student within a date range.
     * Used for fee calculation.
     */
    suspend fun getEnrollmentsInDateRange(
        studentId: Long,
        startDate: Long,
        endDate: Long
    ): List<TransportEnrollment>
    
    /**
     * Calculate transport fee months for a student in a date range.
     * Considers multiple enrollment periods.
     * Transport fees apply for entire month even if started/stopped mid-month.
     * 
     * @return Pair of (total months, total fee amount)
     */
    suspend fun calculateTransportFeeForDateRange(
        studentId: Long,
        startDate: Long,
        endDate: Long
    ): Pair<Int, Double>
    
    /**
     * End an active enrollment by setting end date.
     */
    suspend fun endEnrollment(enrollmentId: Long, endDate: Long): Result<Unit>
    
    /**
     * Get enrollments with route details for display.
     * @param studentClass The student's current class for calculating class-based transport fee
     */
    suspend fun getEnrollmentsWithRoute(studentId: Long, studentClass: String): List<TransportEnrollmentWithRoute>
    
    /**
     * Delete all enrollments for a student.
     */
    suspend fun deleteAllForStudent(studentId: Long)
}

