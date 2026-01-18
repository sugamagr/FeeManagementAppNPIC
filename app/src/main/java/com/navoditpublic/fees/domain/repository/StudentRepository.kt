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
    
    // With balance
    fun getStudentsWithBalance(): Flow<List<StudentWithBalance>>
    
    fun getStudentsWithDues(): Flow<List<StudentWithBalance>>
}


