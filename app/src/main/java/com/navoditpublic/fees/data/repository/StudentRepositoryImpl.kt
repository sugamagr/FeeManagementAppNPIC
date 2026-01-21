package com.navoditpublic.fees.data.repository

import com.navoditpublic.fees.data.local.dao.LedgerDao
import com.navoditpublic.fees.data.local.dao.StudentDao
import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.model.StudentWithBalance
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.util.ClassUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao,
    private val ledgerDao: LedgerDao
) : StudentRepository {
    
    override suspend fun insert(student: Student): Result<Long> = runCatching {
        studentDao.insert(student.toEntity())
    }
    
    override suspend fun update(student: Student): Result<Unit> = runCatching {
        studentDao.update(student.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }
    
    override suspend fun delete(student: Student): Result<Unit> = runCatching {
        // Soft delete - mark as inactive
        studentDao.update(student.toEntity().copy(isActive = false, updatedAt = System.currentTimeMillis()))
    }
    
    override suspend fun getById(id: Long): Student? {
        return studentDao.getById(id)?.let { Student.fromEntity(it) }
    }
    
    override fun getByIdFlow(id: Long): Flow<Student?> {
        return studentDao.getByIdFlow(id).map { it?.let { entity -> Student.fromEntity(entity) } }
    }
    
    override suspend fun getBySrNumber(srNumber: String): Student? {
        return studentDao.getBySrNumber(srNumber)?.let { Student.fromEntity(it) }
    }
    
    override suspend fun getByAccountNumber(accountNumber: String): Student? {
        return studentDao.getByAccountNumber(accountNumber)?.let { Student.fromEntity(it) }
    }
    
    override fun getAllActiveStudents(): Flow<List<Student>> {
        return studentDao.getAllActiveStudents().map { entities ->
            entities.map { Student.fromEntity(it) }
        }
    }
    
    override fun getAllStudents(): Flow<List<Student>> {
        return studentDao.getAllStudents().map { entities ->
            entities.map { Student.fromEntity(it) }
        }
    }
    
    override fun getStudentsByClass(className: String): Flow<List<Student>> {
        return studentDao.getStudentsByClass(className).map { entities ->
            entities.map { Student.fromEntity(it) }
        }
    }
    
    override fun getStudentsByClassAndSection(className: String, section: String): Flow<List<Student>> {
        return studentDao.getStudentsByClassAndSection(className, section).map { entities ->
            entities.map { Student.fromEntity(it) }
        }
    }
    
    override fun searchStudents(query: String): Flow<List<Student>> {
        return studentDao.searchStudents(query).map { entities ->
            entities.map { Student.fromEntity(it) }
        }
    }
    
    override fun getActiveStudentCount(): Flow<Int> {
        return studentDao.getActiveStudentCount()
    }
    
    override fun getStudentCountByClass(className: String): Flow<Int> {
        return studentDao.getStudentCountByClass(className)
    }
    
    override suspend fun srNumberExists(srNumber: String): Boolean {
        return studentDao.srNumberExists(srNumber)
    }
    
    override suspend fun accountNumberExists(accountNumber: String): Boolean {
        return studentDao.accountNumberExists(accountNumber)
    }
    
    override suspend fun srNumberExistsExcluding(srNumber: String, excludeId: Long): Boolean {
        return studentDao.srNumberExistsExcluding(srNumber, excludeId)
    }
    
    override suspend fun accountNumberExistsExcluding(accountNumber: String, excludeId: Long): Boolean {
        return studentDao.accountNumberExistsExcluding(accountNumber, excludeId)
    }
    
    // ========== Class-Scoped Account Number Methods ==========
    
    override suspend fun accountNumberExistsInClass(accountNumber: String, className: String): Boolean {
        return studentDao.accountNumberExistsInClass(accountNumber, className)
    }
    
    override suspend fun accountNumberExistsInClassExcluding(accountNumber: String, className: String, excludeId: Long): Boolean {
        return studentDao.accountNumberExistsInClassExcluding(accountNumber, className, excludeId)
    }
    
    override fun getStudentsWithBalance(): Flow<List<StudentWithBalance>> {
        return studentDao.getAllActiveStudents().map { entities ->
            entities.map { entity ->
                val student = Student.fromEntity(entity)
                val balance = ledgerDao.getCurrentBalance(entity.id)
                StudentWithBalance(student, balance)
            }
        }
    }
    
    override fun getAllStudentsWithBalance(): Flow<List<StudentWithBalance>> {
        return studentDao.getAllStudents().map { entities ->
            entities.map { entity ->
                val student = Student.fromEntity(entity)
                val balance = ledgerDao.getCurrentBalance(entity.id)
                StudentWithBalance(student, balance)
            }
        }
    }
    
    override fun getStudentsWithDues(): Flow<List<StudentWithBalance>> {
        return combine(
            studentDao.getAllActiveStudents(),
            ledgerDao.getStudentIdsWithDues()
        ) { students, studentIdsWithDues ->
            students
                .filter { it.id in studentIdsWithDues }
                .map { entity ->
                    val student = Student.fromEntity(entity)
                    val balance = ledgerDao.getCurrentBalance(entity.id)
                    StudentWithBalance(student, balance)
                }
        }
    }
    
    // ========== Session Promotion Methods ==========
    
    override suspend fun promoteClass(currentClass: String, newClass: String): Int {
        return studentDao.promoteClass(currentClass, newClass)
    }
    
    override suspend fun demoteClass(currentClass: String, previousClass: String): Int {
        return studentDao.demoteClass(currentClass, previousClass)
    }
    
    override suspend fun deactivatePassedOutStudents(sessionName: String): Int {
        val prefix = ClassUtils.getPassedOutPrefix(sessionName)
        return studentDao.deactivatePassedOutStudents(prefix)
    }
    
    override suspend fun reactivateStudentsByClass(className: String): Int {
        return studentDao.reactivateStudentsByClass(className)
    }
    
    override suspend fun reactivatePassedOutStudentsAndRestoreAccountNumbers(sessionName: String): Int {
        val prefix = ClassUtils.getPassedOutPrefix(sessionName)
        return studentDao.reactivatePassedOutStudentsAndRestoreAccountNumbers(prefix)
    }
    
    override suspend fun getPassedOutStudentsWithConflictsCount(sessionName: String): Int {
        val prefix = ClassUtils.getPassedOutPrefix(sessionName)
        return studentDao.getPassedOutStudentsWithAccountNumberConflicts(prefix, "12th").size
    }
    
    override suspend fun getClassWiseStudentCounts(): Map<String, Int> {
        return studentDao.getClassWiseStudentCounts().associate { it.className to it.count }
    }
    
    override suspend fun get12thClassStudentCount(): Int {
        return studentDao.get12thClassStudentCount()
    }
    
    override suspend fun getStudentsAddedAfter(timestamp: Long): List<Student> {
        return studentDao.getStudentsAddedAfter(timestamp).map { Student.fromEntity(it) }
    }
    
    override suspend fun deleteStudentsAddedAfter(timestamp: Long): Int {
        return studentDao.deleteStudentsAddedAfter(timestamp)
    }
    
    override suspend fun getActiveStudentCountSync(): Int {
        return studentDao.getActiveStudentCountSync()
    }
    
    // ========== Individual Student Status Management ==========
    
    override suspend fun markInactive(studentId: Long): Result<Unit> = runCatching {
        studentDao.setActiveStatus(studentId, false)
    }
    
    override suspend fun reactivate(studentId: Long): Result<Unit> = runCatching {
        studentDao.setActiveStatus(studentId, true)
    }
    
    override suspend fun hardDelete(student: Student): Result<Unit> = runCatching {
        studentDao.delete(student.toEntity())
    }
    
    override suspend fun getInactiveStudentCount(): Int {
        return studentDao.getInactiveStudentCount()
    }
}


