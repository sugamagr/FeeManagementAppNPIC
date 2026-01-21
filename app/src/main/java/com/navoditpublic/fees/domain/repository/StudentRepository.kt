package com.navoditpublic.fees.domain.repository

import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.model.StudentWithBalance
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Student operations.
 * Implementation can be switched between Room and Firebase.
 */
interface StudentRepository {
    
    suspend fun insert(student: Student): Result<Long>
    
    suspend fun update(student: Student): Result<Unit>
    
    suspend fun delete(student: Student): Result<Unit>
    
    suspend fun getById(id: Long): Student?
    
    fun getByIdFlow(id: Long): Flow<Student?>
    
    suspend fun getBySrNumber(srNumber: String): Student?
    
    suspend fun getByAccountNumber(accountNumber: String): Student?
    
    fun getAllActiveStudents(): Flow<List<Student>>
    
    fun getAllStudents(): Flow<List<Student>>
    
    fun getStudentsByClass(className: String): Flow<List<Student>>
    
    fun getStudentsByClassAndSection(className: String, section: String): Flow<List<Student>>
    
    fun searchStudents(query: String): Flow<List<Student>>
    
    fun getActiveStudentCount(): Flow<Int>
    
    fun getStudentCountByClass(className: String): Flow<Int>
    
    suspend fun srNumberExists(srNumber: String): Boolean
    
    suspend fun accountNumberExists(accountNumber: String): Boolean
    
    suspend fun srNumberExistsExcluding(srNumber: String, excludeId: Long): Boolean
    
    suspend fun accountNumberExistsExcluding(accountNumber: String, excludeId: Long): Boolean
    
    // Class-scoped account number methods
    suspend fun accountNumberExistsInClass(accountNumber: String, className: String): Boolean
    
    suspend fun accountNumberExistsInClassExcluding(accountNumber: String, className: String, excludeId: Long): Boolean
    
    // With balance
    fun getStudentsWithBalance(): Flow<List<StudentWithBalance>>
    
    /**
     * Get ALL students (including inactive) with their current balance.
     * Used in the Students screen where inactive students should be visible.
     */
    fun getAllStudentsWithBalance(): Flow<List<StudentWithBalance>>
    
    fun getStudentsWithDues(): Flow<List<StudentWithBalance>>
    
    // ========== Session Promotion Methods ==========
    
    /**
     * Promote all students from one class to next
     * @return Number of students promoted
     */
    suspend fun promoteClass(currentClass: String, newClass: String): Int
    
    /**
     * Demote all students from one class to previous (for revert)
     * @return Number of students demoted
     */
    suspend fun demoteClass(currentClass: String, previousClass: String): Int
    
    /**
     * Deactivate all 12th class students (passed out) and prefix their account numbers.
     * Account numbers are prefixed with "PASS<sessionCode>-" (e.g., "5" becomes "PASS2425-5")
     * to free up the original number for new students.
     * 
     * @param sessionName The session name (e.g., "2024-25") used to generate prefix
     * @return Number of students deactivated
     */
    suspend fun deactivatePassedOutStudents(sessionName: String): Int
    
    /**
     * Reactivate students by class (for revert)
     * @return Number of students reactivated
     */
    suspend fun reactivateStudentsByClass(className: String): Int
    
    /**
     * Reactivate passed-out students and restore their original account numbers.
     * Removes the session prefix (e.g., "PASS2425-5" becomes "5").
     * 
     * @param sessionName The session name to identify which prefix to remove
     * @return Number of students reactivated
     */
    suspend fun reactivatePassedOutStudentsAndRestoreAccountNumbers(sessionName: String): Int
    
    /**
     * Get count of passed-out students whose original account numbers conflict with existing active students.
     * Used to determine if safe reactivation is possible during promotion revert.
     * 
     * @param sessionName The session name to identify which prefix to check
     * @return Number of students with conflicts
     */
    suspend fun getPassedOutStudentsWithConflictsCount(sessionName: String): Int
    
    /**
     * Get class-wise student counts
     */
    suspend fun getClassWiseStudentCounts(): Map<String, Int>
    
    /**
     * Get count of 12th class students
     */
    suspend fun get12thClassStudentCount(): Int
    
    /**
     * Get students added after a timestamp (for revert safety check)
     */
    suspend fun getStudentsAddedAfter(timestamp: Long): List<Student>
    
    /**
     * Delete students added after a timestamp (for forced revert)
     */
    suspend fun deleteStudentsAddedAfter(timestamp: Long): Int
    
    /**
     * Get total count of active students (non-flow)
     */
    suspend fun getActiveStudentCountSync(): Int
    
    // ========== Individual Student Status Management ==========
    
    /**
     * Mark a student as inactive (soft delete).
     * Account number is NOT modified - student can be reactivated later.
     * 
     * @param studentId The student to mark inactive
     * @return Result indicating success or failure
     */
    suspend fun markInactive(studentId: Long): Result<Unit>
    
    /**
     * Reactivate an inactive student.
     * 
     * @param studentId The student to reactivate
     * @return Result indicating success or failure
     */
    suspend fun reactivate(studentId: Long): Result<Unit>
    
    /**
     * Permanently delete a student from the database (hard delete).
     * This will also delete related transport enrollments (CASCADE).
     * 
     * WARNING: This action cannot be undone!
     * Should only be called after verifying the student has no financial records.
     * 
     * @param student The student to delete
     * @return Result indicating success or failure
     */
    suspend fun hardDelete(student: Student): Result<Unit>
    
    /**
     * Get the count of inactive students
     */
    suspend fun getInactiveStudentCount(): Int
}


