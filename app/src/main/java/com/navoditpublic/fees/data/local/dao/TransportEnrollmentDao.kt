package com.navoditpublic.fees.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.TransportEnrollmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransportEnrollmentDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(enrollment: TransportEnrollmentEntity): Long
    
    @Update
    suspend fun update(enrollment: TransportEnrollmentEntity)
    
    @Delete
    suspend fun delete(enrollment: TransportEnrollmentEntity)
    
    @Query("SELECT * FROM transport_enrollments WHERE id = :id")
    suspend fun getById(id: Long): TransportEnrollmentEntity?
    
    @Query("SELECT * FROM transport_enrollments WHERE student_id = :studentId ORDER BY start_date DESC")
    fun getByStudentId(studentId: Long): Flow<List<TransportEnrollmentEntity>>
    
    @Query("SELECT * FROM transport_enrollments WHERE student_id = :studentId ORDER BY start_date DESC")
    suspend fun getByStudentIdSync(studentId: Long): List<TransportEnrollmentEntity>
    
    // Get currently active enrollment (end_date is null)
    @Query("SELECT * FROM transport_enrollments WHERE student_id = :studentId AND end_date IS NULL LIMIT 1")
    suspend fun getActiveEnrollment(studentId: Long): TransportEnrollmentEntity?
    
    // Get all enrollments for a student within a date range (for fee calculation)
    @Query("""
        SELECT * FROM transport_enrollments 
        WHERE student_id = :studentId 
        AND (
            (start_date <= :endDate AND (end_date IS NULL OR end_date >= :startDate))
        )
        ORDER BY start_date ASC
    """)
    suspend fun getEnrollmentsInDateRange(
        studentId: Long, 
        startDate: Long, 
        endDate: Long
    ): List<TransportEnrollmentEntity>
    
    // Count total transport months in a date range for a student
    // This is a helper - actual calculation should be done in repository
    @Query("""
        SELECT COUNT(*) FROM transport_enrollments 
        WHERE student_id = :studentId 
        AND start_date <= :endDate 
        AND (end_date IS NULL OR end_date >= :startDate)
    """)
    suspend fun hasTransportInDateRange(studentId: Long, startDate: Long, endDate: Long): Int
    
    // End an active enrollment
    @Query("UPDATE transport_enrollments SET end_date = :endDate, updated_at = :updatedAt WHERE id = :enrollmentId")
    suspend fun endEnrollment(enrollmentId: Long, endDate: Long, updatedAt: Long = System.currentTimeMillis())
    
    // Get all enrollments for a route
    @Query("SELECT * FROM transport_enrollments WHERE route_id = :routeId ORDER BY start_date DESC")
    suspend fun getByRouteId(routeId: Long): List<TransportEnrollmentEntity>
    
    // Delete all enrollments for a student (used when deleting student)
    @Query("DELETE FROM transport_enrollments WHERE student_id = :studentId")
    suspend fun deleteAllForStudent(studentId: Long)
}

