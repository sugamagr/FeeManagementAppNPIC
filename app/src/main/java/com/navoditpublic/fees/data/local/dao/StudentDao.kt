package com.navoditpublic.fees.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(student: StudentEntity): Long
    
    @Update
    suspend fun update(student: StudentEntity)
    
    @Delete
    suspend fun delete(student: StudentEntity)
    
    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getById(id: Long): StudentEntity?
    
    @Query("SELECT * FROM students WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<StudentEntity?>
    
    @Query("SELECT * FROM students WHERE sr_number = :srNumber")
    suspend fun getBySrNumber(srNumber: String): StudentEntity?
    
    @Query("SELECT * FROM students WHERE account_number = :accountNumber")
    suspend fun getByAccountNumber(accountNumber: String): StudentEntity?
    
    @Query("SELECT * FROM students WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveStudents(): Flow<List<StudentEntity>>
    
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>
    
    @Query("""
        SELECT * FROM students 
        WHERE is_active = 1 
        AND current_class = :className 
        ORDER BY name ASC
    """)
    fun getStudentsByClass(className: String): Flow<List<StudentEntity>>
    
    @Query("""
        SELECT * FROM students 
        WHERE is_active = 1 
        AND current_class = :className 
        AND section = :section 
        ORDER BY name ASC
    """)
    fun getStudentsByClassAndSection(className: String, section: String): Flow<List<StudentEntity>>
    
    @Query("""
        SELECT * FROM students 
        WHERE is_active = 1 
        AND (
            name LIKE '%' || :query || '%' 
            OR sr_number LIKE '%' || :query || '%'
            OR account_number LIKE '%' || :query || '%'
            OR father_name LIKE '%' || :query || '%'
            OR phone_primary LIKE '%' || :query || '%'
        )
        ORDER BY name ASC
    """)
    fun searchStudents(query: String): Flow<List<StudentEntity>>
    
    @Query("SELECT COUNT(*) FROM students WHERE is_active = 1")
    fun getActiveStudentCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM students WHERE is_active = 1 AND current_class = :className")
    fun getStudentCountByClass(className: String): Flow<Int>
    
    @Query("SELECT EXISTS(SELECT 1 FROM students WHERE sr_number = :srNumber)")
    suspend fun srNumberExists(srNumber: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM students WHERE account_number = :accountNumber)")
    suspend fun accountNumberExists(accountNumber: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM students WHERE sr_number = :srNumber AND id != :excludeId)")
    suspend fun srNumberExistsExcluding(srNumber: String, excludeId: Long): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM students WHERE account_number = :accountNumber AND id != :excludeId)")
    suspend fun accountNumberExistsExcluding(accountNumber: String, excludeId: Long): Boolean
    
    // Get all active students as list (not Flow)
    @Query("SELECT * FROM students WHERE is_active = 1 ORDER BY name ASC")
    suspend fun getAllActiveStudentsList(): List<StudentEntity>
    
    // Demo data support
    @Query("SELECT COUNT(*) FROM students WHERE sr_number LIKE :prefix || '%'")
    suspend fun countBySrNumberPrefix(prefix: String): Int
    
    @Query("SELECT id FROM students WHERE sr_number LIKE :prefix || '%'")
    suspend fun getStudentIdsBySrNumberPrefix(prefix: String): List<Long>
    
    @Query("DELETE FROM students WHERE sr_number LIKE :prefix || '%'")
    suspend fun deleteBySrNumberPrefix(prefix: String)
}


