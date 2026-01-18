package com.navoditpublic.fees.data.repository

import com.navoditpublic.fees.data.local.dao.TransportEnrollmentDao
import com.navoditpublic.fees.data.local.dao.TransportRouteDao
import com.navoditpublic.fees.domain.model.TransportEnrollment
import com.navoditpublic.fees.domain.model.TransportEnrollmentWithRoute
import com.navoditpublic.fees.domain.repository.TransportEnrollmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransportEnrollmentRepositoryImpl @Inject constructor(
    private val enrollmentDao: TransportEnrollmentDao,
    private val routeDao: TransportRouteDao
) : TransportEnrollmentRepository {
    
    override suspend fun insert(enrollment: TransportEnrollment): Result<Long> = runCatching {
        enrollmentDao.insert(enrollment.toEntity())
    }
    
    override suspend fun update(enrollment: TransportEnrollment): Result<Unit> = runCatching {
        enrollmentDao.update(enrollment.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }
    
    override suspend fun delete(enrollment: TransportEnrollment): Result<Unit> = runCatching {
        enrollmentDao.delete(enrollment.toEntity())
    }
    
    override suspend fun getById(id: Long): TransportEnrollment? {
        return enrollmentDao.getById(id)?.let { TransportEnrollment.fromEntity(it) }
    }
    
    override fun getByStudentId(studentId: Long): Flow<List<TransportEnrollment>> {
        return enrollmentDao.getByStudentId(studentId).map { entities ->
            entities.map { TransportEnrollment.fromEntity(it) }
        }
    }
    
    override suspend fun getByStudentIdSync(studentId: Long): List<TransportEnrollment> {
        return enrollmentDao.getByStudentIdSync(studentId).map { TransportEnrollment.fromEntity(it) }
    }
    
    override suspend fun getActiveEnrollment(studentId: Long): TransportEnrollment? {
        return enrollmentDao.getActiveEnrollment(studentId)?.let { TransportEnrollment.fromEntity(it) }
    }
    
    override suspend fun getEnrollmentsInDateRange(
        studentId: Long,
        startDate: Long,
        endDate: Long
    ): List<TransportEnrollment> {
        return enrollmentDao.getEnrollmentsInDateRange(studentId, startDate, endDate)
            .map { TransportEnrollment.fromEntity(it) }
    }
    
    override suspend fun calculateTransportFeeForDateRange(
        studentId: Long,
        startDate: Long,
        endDate: Long
    ): Pair<Int, Double> {
        val enrollments = getEnrollmentsInDateRange(studentId, startDate, endDate)
        if (enrollments.isEmpty()) return Pair(0, 0.0)
        
        // Get all months in the range
        val allMonths = getMonthsInRange(startDate, endDate)
        var totalMonths = 0
        var totalFee = 0.0
        
        // For each month, check if there's an active enrollment
        for ((year, month) in allMonths) {
            val monthStart = getMonthStart(year, month)
            val monthEnd = getMonthEnd(year, month)
            
            // Find enrollment active during this month
            // Transport applies for entire month even if started/stopped mid-month
            val activeEnrollment = enrollments.find { enrollment ->
                val enrollmentStart = enrollment.startDate
                val enrollmentEnd = enrollment.endDate ?: Long.MAX_VALUE
                
                // Enrollment overlaps with this month
                enrollmentStart <= monthEnd && enrollmentEnd >= monthStart
            }
            
            if (activeEnrollment != null) {
                totalMonths++
                totalFee += activeEnrollment.monthlyFeeAtEnrollment
            }
        }
        
        return Pair(totalMonths, totalFee)
    }
    
    override suspend fun endEnrollment(enrollmentId: Long, endDate: Long): Result<Unit> = runCatching {
        enrollmentDao.endEnrollment(enrollmentId, endDate)
    }
    
    override suspend fun getEnrollmentsWithRoute(studentId: Long, studentClass: String): List<TransportEnrollmentWithRoute> {
        val enrollments = getByStudentIdSync(studentId)
        return enrollments.map { enrollment ->
            val route = routeDao.getById(enrollment.routeId)
            TransportEnrollmentWithRoute(
                enrollment = enrollment,
                routeName = route?.routeName ?: "Unknown Route",
                // Use class-based fee instead of legacy monthlyFee
                currentRouteFee = route?.getFeeForClass(studentClass) ?: 0.0
            )
        }
    }
    
    override suspend fun deleteAllForStudent(studentId: Long) {
        enrollmentDao.deleteAllForStudent(studentId)
    }
    
    // Helper functions for month calculations
    private fun getMonthsInRange(startDate: Long, endDate: Long): List<Pair<Int, Int>> {
        val months = mutableListOf<Pair<Int, Int>>()
        val calendar = Calendar.getInstance()
        
        calendar.timeInMillis = startDate
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        val endCalendar = Calendar.getInstance()
        endCalendar.timeInMillis = endDate
        
        while (calendar.timeInMillis <= endDate) {
            months.add(Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)))
            calendar.add(Calendar.MONTH, 1)
        }
        
        return months
    }
    
    private fun getMonthStart(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getMonthEnd(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        return calendar.timeInMillis
    }
}

