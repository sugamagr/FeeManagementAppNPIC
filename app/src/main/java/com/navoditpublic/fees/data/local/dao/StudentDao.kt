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
    
    // ========== Class-Scoped Account Number Methods ==========
    
    /**
     * Check if account number exists within a specific class for ACTIVE students.
     * Account numbers are unique per class for active students only.
     * Inactive/passed-out students have prefixed A/C numbers so they don't conflict.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM students WHERE account_number = :accountNumber AND current_class = :className AND is_active = 1)")
    suspend fun accountNumberExistsInClass(accountNumber: String, className: String): Boolean
    
    /**
     * Check if account number exists within a specific class, excluding a specific student.
     * Used when editing a student to allow keeping their own account number.
     * Checks only ACTIVE students.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM students WHERE account_number = :accountNumber AND current_class = :className AND is_active = 1 AND id != :excludeId)")
    suspend fun accountNumberExistsInClassExcluding(accountNumber: String, className: String, excludeId: Long): Boolean
    
    /**
     * Get active student by account number in a specific class.
     */
    @Query("SELECT * FROM students WHERE account_number = :accountNumber AND current_class = :className AND is_active = 1 LIMIT 1")
    suspend fun getByAccountNumberInClass(accountNumber: String, className: String): StudentEntity?
    
    /**
     * Get all active students with a specific account number (across all classes).
     * Useful for search functionality - only returns active students for display.
     */
    @Query("SELECT * FROM students WHERE account_number = :accountNumber AND is_active = 1")
    suspend fun getAllByAccountNumber(accountNumber: String): List<StudentEntity>
    
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
    
    @Query("UPDATE students SET admission_fee_paid = :paid, updated_at = :updatedAt WHERE id = :studentId")
    suspend fun updateAdmissionFeePaid(studentId: Long, paid: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    // ========== Session Promotion Methods ==========
    
    /**
     * Promote all active students from one class to another
     * @return Number of students updated
     */
    @Query("""
        UPDATE students 
        SET current_class = :newClass, updated_at = :updatedAt 
        WHERE current_class = :currentClass AND is_active = 1
    """)
    suspend fun promoteClass(
        currentClass: String, 
        newClass: String, 
        updatedAt: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Demote all active students from one class to another (for revert)
     * @return Number of students updated
     */
    @Query("""
        UPDATE students 
        SET current_class = :previousClass, updated_at = :updatedAt 
        WHERE current_class = :currentClass AND is_active = 1
    """)
    suspend fun demoteClass(
        currentClass: String, 
        previousClass: String, 
        updatedAt: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Deactivate all students in 12th class (passed out) and prefix their account numbers.
     * Account numbers are prefixed with "PASS<sessionCode>-" (e.g., "5" becomes "PASS2425-5")
     * to free up the original number for new students.
     * 
     * @param sessionPrefix The prefix to add (e.g., "PASS2425-")
     * @return Number of students deactivated
     */
    @Query("""
        UPDATE students 
        SET is_active = 0, 
            account_number = :sessionPrefix || account_number,
            updated_at = :updatedAt 
        WHERE current_class = '12th' AND is_active = 1
    """)
    suspend fun deactivatePassedOutStudents(
        sessionPrefix: String,
        updatedAt: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Get all inactive 12th class students (passed out) with prefixed account numbers.
     * Used for reverting promotion.
     */
    @Query("SELECT * FROM students WHERE current_class = '12th' AND is_active = 0 AND account_number LIKE :prefix || '%'")
    suspend fun getPassedOutStudentsWithPrefix(prefix: String): List<StudentEntity>
    
    /**
     * Reactivate students who were deactivated (for revert)
     * This reactivates all inactive students - use carefully!
     * For precise revert, use reactivateStudentsByIds
     */
    @Query("""
        UPDATE students 
        SET is_active = 1, updated_at = :updatedAt 
        WHERE current_class = :className AND is_active = 0
    """)
    suspend fun reactivateStudentsByClass(
        className: String,
        updatedAt: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Reactivate passed-out students and restore their original account numbers.
     * Removes the session prefix (e.g., "PASS2425-5" becomes "5").
     * 
     * @param sessionPrefix The prefix to remove (e.g., "PASS2425-")
     * @return Number of students reactivated
     */
    @Query("""
        UPDATE students 
        SET is_active = 1, 
            account_number = SUBSTR(account_number, LENGTH(:sessionPrefix) + 1),
            updated_at = :updatedAt 
        WHERE current_class = '12th' AND is_active = 0 AND account_number LIKE :sessionPrefix || '%'
    """)
    suspend fun reactivatePassedOutStudentsAndRestoreAccountNumbers(
        sessionPrefix: String,
        updatedAt: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Get passed-out students whose restored account numbers would conflict with existing active students.
     * Used during promotion revert to identify collision cases.
     * 
     * @param sessionPrefix The PASS prefix (e.g., "PASS2425-")
     * @param className The class to check conflicts in (usually "12th")
     */
    @Query("""
        SELECT deactivated.* FROM students deactivated 
        WHERE deactivated.is_active = 0 
        AND deactivated.current_class = :className
        AND deactivated.account_number LIKE :sessionPrefix || '%'
        AND EXISTS (
            SELECT 1 FROM students active 
            WHERE active.is_active = 1 
            AND active.current_class = :className
            AND active.account_number = SUBSTR(deactivated.account_number, LENGTH(:sessionPrefix) + 1)
        )
    """)
    suspend fun getPassedOutStudentsWithAccountNumberConflicts(
        sessionPrefix: String,
        className: String = "12th"
    ): List<StudentEntity>
    
    /**
     * Get count of active students per class
     */
    @Query("""
        SELECT current_class as className, COUNT(*) as count 
        FROM students 
        WHERE is_active = 1 
        GROUP BY current_class
    """)
    suspend fun getClassWiseStudentCounts(): List<ClassStudentCount>
    
    /**
     * Get count of students in 12th class (for preview before deactivation)
     */
    @Query("SELECT COUNT(*) FROM students WHERE current_class = '12th' AND is_active = 1")
    suspend fun get12thClassStudentCount(): Int
    
    /**
     * Get students added after a certain timestamp (for revert safety check)
     */
    @Query("""
        SELECT * FROM students 
        WHERE created_at > :timestamp 
        AND is_active = 1
        ORDER BY created_at DESC
    """)
    suspend fun getStudentsAddedAfter(timestamp: Long): List<StudentEntity>
    
    /**
     * Delete students added after a certain timestamp (for forced revert)
     */
    @Query("DELETE FROM students WHERE created_at > :timestamp")
    suspend fun deleteStudentsAddedAfter(timestamp: Long): Int
    
    /**
     * Get total count of active students
     */
    @Query("SELECT COUNT(*) FROM students WHERE is_active = 1")
    suspend fun getActiveStudentCountSync(): Int
    
    // ========== Individual Student Status Management ==========
    
    /**
     * Set the active status of a student.
     * Used for marking inactive or reactivating individual students.
     * 
     * @param studentId The student to update
     * @param isActive The new active status
     */
    @Query("UPDATE students SET is_active = :isActive, updated_at = :updatedAt WHERE id = :studentId")
    suspend fun setActiveStatus(studentId: Long, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Get count of inactive students
     */
    @Query("SELECT COUNT(*) FROM students WHERE is_active = 0")
    suspend fun getInactiveStudentCount(): Int
}

/**
 * Data class for class-wise student count
 */
data class ClassStudentCount(
    val className: String,
    val count: Int
)


