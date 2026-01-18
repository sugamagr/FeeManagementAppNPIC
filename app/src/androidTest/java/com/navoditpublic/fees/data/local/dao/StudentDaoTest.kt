package com.navoditpublic.fees.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.navoditpublic.fees.data.local.FeesDatabase
import com.navoditpublic.fees.data.local.entity.AcademicSessionEntity
import com.navoditpublic.fees.data.local.entity.StudentEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for StudentDao.
 * Uses an in-memory database for fast, isolated tests.
 */
@RunWith(AndroidJUnit4::class)
class StudentDaoTest {

    private lateinit var database: FeesDatabase
    private lateinit var studentDao: StudentDao
    private lateinit var academicSessionDao: AcademicSessionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            FeesDatabase::class.java
        ).allowMainThreadQueries().build()
        
        studentDao = database.studentDao()
        academicSessionDao = database.academicSessionDao()
        
        // Insert required session for foreign key
        runTest {
            academicSessionDao.insert(createTestSession())
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createTestSession(id: Long = 1L) = AcademicSessionEntity(
        id = id,
        sessionName = "2025-26",
        startDate = System.currentTimeMillis(),
        endDate = System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L,
        isCurrent = true,
        isActive = true
    )

    private fun createStudentEntity(
        id: Long = 0L,
        srNumber: String = "SR001",
        accountNumber: String = "ACC001",
        name: String = "Test Student",
        fatherName: String = "Test Father",
        currentClass: String = "5th",
        section: String = "A",
        isActive: Boolean = true
    ) = StudentEntity(
        id = id,
        srNumber = srNumber,
        accountNumber = accountNumber,
        name = name,
        fatherName = fatherName,
        motherName = "",
        phonePrimary = "9876543210",
        phoneSecondary = "",
        addressLine1 = "",
        addressLine2 = "",
        district = "",
        state = "Uttar Pradesh",
        pincode = "",
        currentClass = currentClass,
        section = section,
        admissionDate = System.currentTimeMillis(),
        admissionSessionId = 1L,
        hasTransport = false,
        transportRouteId = null,
        openingBalance = 0.0,
        openingBalanceRemarks = "",
        openingBalanceDate = null,
        admissionFeePaid = false,
        isActive = isActive,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    // ===== Insert Tests =====

    @Test
    fun insertStudent_returnsGeneratedId() = runTest {
        val student = createStudentEntity()
        
        val id = studentDao.insert(student)
        
        assertThat(id).isGreaterThan(0L)
    }

    @Test
    fun insertStudent_canBeRetrieved() = runTest {
        val student = createStudentEntity(name = "John Doe")
        val id = studentDao.insert(student)
        
        val retrieved = studentDao.getById(id)
        
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name).isEqualTo("John Doe")
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun insertStudent_duplicateSrNumber_throwsException() = runTest {
        val student1 = createStudentEntity(srNumber = "SR001")
        val student2 = createStudentEntity(srNumber = "SR001", accountNumber = "ACC002")
        
        studentDao.insert(student1)
        studentDao.insert(student2) // Should throw
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun insertStudent_duplicateAccountNumber_throwsException() = runTest {
        val student1 = createStudentEntity(accountNumber = "ACC001")
        val student2 = createStudentEntity(srNumber = "SR002", accountNumber = "ACC001")
        
        studentDao.insert(student1)
        studentDao.insert(student2) // Should throw
    }

    // ===== Update Tests =====

    @Test
    fun updateStudent_updatesData() = runTest {
        val student = createStudentEntity(name = "Original Name")
        val id = studentDao.insert(student)
        
        val updatedStudent = student.copy(id = id, name = "Updated Name")
        studentDao.update(updatedStudent)
        
        val retrieved = studentDao.getById(id)
        assertThat(retrieved?.name).isEqualTo("Updated Name")
    }

    // ===== Delete Tests =====

    @Test
    fun deleteStudent_removesFromDatabase() = runTest {
        val student = createStudentEntity()
        val id = studentDao.insert(student)
        
        val insertedStudent = studentDao.getById(id)!!
        studentDao.delete(insertedStudent)
        
        val retrieved = studentDao.getById(id)
        assertThat(retrieved).isNull()
    }

    // ===== GetById Tests =====

    @Test
    fun getById_returnsNullWhenNotFound() = runTest {
        val result = studentDao.getById(999L)
        
        assertThat(result).isNull()
    }

    // ===== GetByIdFlow Tests =====

    @Test
    fun getByIdFlow_emitsStudent() = runTest {
        val student = createStudentEntity(name = "Flow Test")
        val id = studentDao.insert(student)
        
        studentDao.getByIdFlow(id).test {
            val result = awaitItem()
            assertThat(result?.name).isEqualTo("Flow Test")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getByIdFlow_emitsUpdates() = runTest {
        val student = createStudentEntity(name = "Original")
        val id = studentDao.insert(student)
        
        studentDao.getByIdFlow(id).test {
            assertThat(awaitItem()?.name).isEqualTo("Original")
            
            // Update the student
            studentDao.update(student.copy(id = id, name = "Updated"))
            
            assertThat(awaitItem()?.name).isEqualTo("Updated")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetBySrNumber Tests =====

    @Test
    fun getBySrNumber_returnsStudent() = runTest {
        val student = createStudentEntity(srNumber = "SR123")
        studentDao.insert(student)
        
        val result = studentDao.getBySrNumber("SR123")
        
        assertThat(result).isNotNull()
        assertThat(result?.srNumber).isEqualTo("SR123")
    }

    @Test
    fun getBySrNumber_returnsNullWhenNotFound() = runTest {
        val result = studentDao.getBySrNumber("NOTFOUND")
        
        assertThat(result).isNull()
    }

    // ===== GetByAccountNumber Tests =====

    @Test
    fun getByAccountNumber_returnsStudent() = runTest {
        val student = createStudentEntity(accountNumber = "ACC123")
        studentDao.insert(student)
        
        val result = studentDao.getByAccountNumber("ACC123")
        
        assertThat(result).isNotNull()
        assertThat(result?.accountNumber).isEqualTo("ACC123")
    }

    // ===== GetAllActiveStudents Tests =====

    @Test
    fun getAllActiveStudents_returnsOnlyActive() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR001", accountNumber = "ACC001", name = "Active 1", isActive = true))
        studentDao.insert(createStudentEntity(srNumber = "SR002", accountNumber = "ACC002", name = "Inactive", isActive = false))
        studentDao.insert(createStudentEntity(srNumber = "SR003", accountNumber = "ACC003", name = "Active 2", isActive = true))
        
        studentDao.getAllActiveStudents().test {
            val students = awaitItem()
            assertThat(students).hasSize(2)
            assertThat(students.all { it.isActive }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllActiveStudents_orderedByName() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR001", accountNumber = "ACC001", name = "Zara"))
        studentDao.insert(createStudentEntity(srNumber = "SR002", accountNumber = "ACC002", name = "Alice"))
        studentDao.insert(createStudentEntity(srNumber = "SR003", accountNumber = "ACC003", name = "Mike"))
        
        studentDao.getAllActiveStudents().test {
            val students = awaitItem()
            assertThat(students.map { it.name }).isEqualTo(listOf("Alice", "Mike", "Zara"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetStudentsByClass Tests =====

    @Test
    fun getStudentsByClass_filtersCorrectly() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR001", accountNumber = "ACC001", currentClass = "5th"))
        studentDao.insert(createStudentEntity(srNumber = "SR002", accountNumber = "ACC002", currentClass = "6th"))
        studentDao.insert(createStudentEntity(srNumber = "SR003", accountNumber = "ACC003", currentClass = "5th"))
        
        studentDao.getStudentsByClass("5th").test {
            val students = awaitItem()
            assertThat(students).hasSize(2)
            assertThat(students.all { it.currentClass == "5th" }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetStudentsByClassAndSection Tests =====

    @Test
    fun getStudentsByClassAndSection_filtersCorrectly() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR001", accountNumber = "ACC001", currentClass = "5th", section = "A"))
        studentDao.insert(createStudentEntity(srNumber = "SR002", accountNumber = "ACC002", currentClass = "5th", section = "B"))
        studentDao.insert(createStudentEntity(srNumber = "SR003", accountNumber = "ACC003", currentClass = "5th", section = "A"))
        
        studentDao.getStudentsByClassAndSection("5th", "A").test {
            val students = awaitItem()
            assertThat(students).hasSize(2)
            assertThat(students.all { it.currentClass == "5th" && it.section == "A" }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== SearchStudents Tests =====

    @Test
    fun searchStudents_matchesByName() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR001", accountNumber = "ACC001", name = "John Doe"))
        studentDao.insert(createStudentEntity(srNumber = "SR002", accountNumber = "ACC002", name = "Jane Smith"))
        
        studentDao.searchStudents("John").test {
            val students = awaitItem()
            assertThat(students).hasSize(1)
            assertThat(students[0].name).isEqualTo("John Doe")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchStudents_matchesBySrNumber() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR123", accountNumber = "ACC001", name = "Student 1"))
        studentDao.insert(createStudentEntity(srNumber = "SR456", accountNumber = "ACC002", name = "Student 2"))
        
        studentDao.searchStudents("123").test {
            val students = awaitItem()
            assertThat(students).hasSize(1)
            assertThat(students[0].srNumber).isEqualTo("SR123")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchStudents_matchesByFatherName() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR001", accountNumber = "ACC001", name = "Child", fatherName = "Rajesh Kumar"))
        studentDao.insert(createStudentEntity(srNumber = "SR002", accountNumber = "ACC002", name = "Other", fatherName = "Suresh Singh"))
        
        studentDao.searchStudents("Rajesh").test {
            val students = awaitItem()
            assertThat(students).hasSize(1)
            assertThat(students[0].fatherName).isEqualTo("Rajesh Kumar")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchStudents_excludesInactive() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR001", accountNumber = "ACC001", name = "Active John", isActive = true))
        studentDao.insert(createStudentEntity(srNumber = "SR002", accountNumber = "ACC002", name = "Inactive John", isActive = false))
        
        studentDao.searchStudents("John").test {
            val students = awaitItem()
            assertThat(students).hasSize(1)
            assertThat(students[0].name).isEqualTo("Active John")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetActiveStudentCount Tests =====

    @Test
    fun getActiveStudentCount_countsOnlyActive() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR001", accountNumber = "ACC001", isActive = true))
        studentDao.insert(createStudentEntity(srNumber = "SR002", accountNumber = "ACC002", isActive = true))
        studentDao.insert(createStudentEntity(srNumber = "SR003", accountNumber = "ACC003", isActive = false))
        
        studentDao.getActiveStudentCount().test {
            assertThat(awaitItem()).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetStudentCountByClass Tests =====

    @Test
    fun getStudentCountByClass_countsCorrectly() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR001", accountNumber = "ACC001", currentClass = "5th"))
        studentDao.insert(createStudentEntity(srNumber = "SR002", accountNumber = "ACC002", currentClass = "5th"))
        studentDao.insert(createStudentEntity(srNumber = "SR003", accountNumber = "ACC003", currentClass = "6th"))
        
        studentDao.getStudentCountByClass("5th").test {
            assertThat(awaitItem()).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== SrNumberExists Tests =====

    @Test
    fun srNumberExists_returnsTrueWhenExists() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR001"))
        
        val result = studentDao.srNumberExists("SR001")
        
        assertThat(result).isTrue()
    }

    @Test
    fun srNumberExists_returnsFalseWhenNotExists() = runTest {
        val result = studentDao.srNumberExists("NOTFOUND")
        
        assertThat(result).isFalse()
    }

    // ===== SrNumberExistsExcluding Tests =====

    @Test
    fun srNumberExistsExcluding_excludesSpecifiedId() = runTest {
        val id = studentDao.insert(createStudentEntity(srNumber = "SR001"))
        
        val result = studentDao.srNumberExistsExcluding("SR001", id)
        
        assertThat(result).isFalse()
    }

    @Test
    fun srNumberExistsExcluding_returnsTrueForOtherStudent() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR001", accountNumber = "ACC001"))
        val id2 = studentDao.insert(createStudentEntity(srNumber = "SR002", accountNumber = "ACC002"))
        
        val result = studentDao.srNumberExistsExcluding("SR001", id2)
        
        assertThat(result).isTrue()
    }

    // ===== AccountNumberExists Tests =====

    @Test
    fun accountNumberExists_returnsTrueWhenExists() = runTest {
        studentDao.insert(createStudentEntity(accountNumber = "ACC001"))
        
        val result = studentDao.accountNumberExists("ACC001")
        
        assertThat(result).isTrue()
    }

    // ===== GetAllActiveStudentsList Tests =====

    @Test
    fun getAllActiveStudentsList_returnsListNotFlow() = runTest {
        studentDao.insert(createStudentEntity(srNumber = "SR001", accountNumber = "ACC001"))
        studentDao.insert(createStudentEntity(srNumber = "SR002", accountNumber = "ACC002"))
        
        val students = studentDao.getAllActiveStudentsList()
        
        assertThat(students).hasSize(2)
    }
}
