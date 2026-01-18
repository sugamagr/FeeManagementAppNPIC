package com.navoditpublic.fees.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.navoditpublic.fees.data.local.dao.LedgerDao
import com.navoditpublic.fees.data.local.dao.StudentDao
import com.navoditpublic.fees.data.local.entity.StudentEntity
import com.navoditpublic.fees.domain.model.Student
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for StudentRepositoryImpl.
 * Uses MockK to mock DAO dependencies.
 */
class StudentRepositoryImplTest {

    private lateinit var studentDao: StudentDao
    private lateinit var ledgerDao: LedgerDao
    private lateinit var repository: StudentRepositoryImpl

    private fun createStudentEntity(
        id: Long = 1L,
        srNumber: String = "SR001",
        accountNumber: String = "ACC001",
        name: String = "Test Student",
        fatherName: String = "Test Father",
        currentClass: String = "5th",
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
        section = "A",
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

    private fun createStudent(
        id: Long = 1L,
        srNumber: String = "SR001",
        accountNumber: String = "ACC001",
        name: String = "Test Student",
        currentClass: String = "5th"
    ) = Student(
        id = id,
        srNumber = srNumber,
        accountNumber = accountNumber,
        name = name,
        fatherName = "Test Father",
        phonePrimary = "9876543210",
        currentClass = currentClass,
        admissionDate = System.currentTimeMillis(),
        admissionSessionId = 1L
    )

    @Before
    fun setup() {
        studentDao = mockk(relaxed = true)
        ledgerDao = mockk(relaxed = true)
        repository = StudentRepositoryImpl(studentDao, ledgerDao)
    }

    // ===== Insert Tests =====

    @Test
    fun `insert calls studentDao insert with correct entity`() = runTest {
        val student = createStudent()
        coEvery { studentDao.insert(any()) } returns 1L

        val result = repository.insert(student)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(1L)
        coVerify { studentDao.insert(any()) }
    }

    @Test
    fun `insert returns failure when dao throws exception`() = runTest {
        val student = createStudent()
        coEvery { studentDao.insert(any()) } throws Exception("Database error")

        val result = repository.insert(student)

        assertThat(result.isFailure).isTrue()
    }

    // ===== Update Tests =====

    @Test
    fun `update calls studentDao update`() = runTest {
        val student = createStudent()
        coEvery { studentDao.update(any()) } returns Unit

        val result = repository.update(student)

        assertThat(result.isSuccess).isTrue()
        coVerify { studentDao.update(any()) }
    }

    @Test
    fun `update sets updatedAt to current time`() = runTest {
        val student = createStudent()
        val entitySlot = slot<StudentEntity>()
        coEvery { studentDao.update(capture(entitySlot)) } returns Unit

        repository.update(student)

        val capturedEntity = entitySlot.captured
        // updatedAt should be very recent (within 1 second)
        assertThat(System.currentTimeMillis() - capturedEntity.updatedAt).isLessThan(1000L)
    }

    // ===== Delete Tests (Soft Delete) =====

    @Test
    fun `delete performs soft delete by setting isActive to false`() = runTest {
        val student = createStudent()
        val entitySlot = slot<StudentEntity>()
        coEvery { studentDao.update(capture(entitySlot)) } returns Unit

        val result = repository.delete(student)

        assertThat(result.isSuccess).isTrue()
        assertThat(entitySlot.captured.isActive).isFalse()
    }

    @Test
    fun `delete updates the updatedAt timestamp`() = runTest {
        val student = createStudent()
        val entitySlot = slot<StudentEntity>()
        coEvery { studentDao.update(capture(entitySlot)) } returns Unit

        repository.delete(student)

        assertThat(System.currentTimeMillis() - entitySlot.captured.updatedAt).isLessThan(1000L)
    }

    // ===== GetById Tests =====

    @Test
    fun `getById returns student when found`() = runTest {
        val entity = createStudentEntity()
        coEvery { studentDao.getById(1L) } returns entity

        val result = repository.getById(1L)

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(1L)
        assertThat(result?.srNumber).isEqualTo("SR001")
    }

    @Test
    fun `getById returns null when not found`() = runTest {
        coEvery { studentDao.getById(999L) } returns null

        val result = repository.getById(999L)

        assertThat(result).isNull()
    }

    // ===== GetByIdFlow Tests =====

    @Test
    fun `getByIdFlow emits student when found`() = runTest {
        val entity = createStudentEntity()
        every { studentDao.getByIdFlow(1L) } returns flowOf(entity)

        repository.getByIdFlow(1L).test {
            val student = awaitItem()
            assertThat(student).isNotNull()
            assertThat(student?.id).isEqualTo(1L)
            awaitComplete()
        }
    }

    @Test
    fun `getByIdFlow emits null when not found`() = runTest {
        every { studentDao.getByIdFlow(999L) } returns flowOf(null)

        repository.getByIdFlow(999L).test {
            val student = awaitItem()
            assertThat(student).isNull()
            awaitComplete()
        }
    }

    // ===== GetBySrNumber Tests =====

    @Test
    fun `getBySrNumber returns student when found`() = runTest {
        val entity = createStudentEntity(srNumber = "SR001")
        coEvery { studentDao.getBySrNumber("SR001") } returns entity

        val result = repository.getBySrNumber("SR001")

        assertThat(result).isNotNull()
        assertThat(result?.srNumber).isEqualTo("SR001")
    }

    @Test
    fun `getBySrNumber returns null when not found`() = runTest {
        coEvery { studentDao.getBySrNumber("NOTFOUND") } returns null

        val result = repository.getBySrNumber("NOTFOUND")

        assertThat(result).isNull()
    }

    // ===== GetByAccountNumber Tests =====

    @Test
    fun `getByAccountNumber returns student when found`() = runTest {
        val entity = createStudentEntity(accountNumber = "ACC001")
        coEvery { studentDao.getByAccountNumber("ACC001") } returns entity

        val result = repository.getByAccountNumber("ACC001")

        assertThat(result).isNotNull()
        assertThat(result?.accountNumber).isEqualTo("ACC001")
    }

    // ===== GetAllActiveStudents Tests =====

    @Test
    fun `getAllActiveStudents returns flow of students`() = runTest {
        val entities = listOf(
            createStudentEntity(id = 1, name = "Student 1"),
            createStudentEntity(id = 2, name = "Student 2")
        )
        every { studentDao.getAllActiveStudents() } returns flowOf(entities)

        repository.getAllActiveStudents().test {
            val students = awaitItem()
            assertThat(students).hasSize(2)
            assertThat(students[0].name).isEqualTo("Student 1")
            assertThat(students[1].name).isEqualTo("Student 2")
            awaitComplete()
        }
    }

    @Test
    fun `getAllActiveStudents returns empty list when no students`() = runTest {
        every { studentDao.getAllActiveStudents() } returns flowOf(emptyList())

        repository.getAllActiveStudents().test {
            val students = awaitItem()
            assertThat(students).isEmpty()
            awaitComplete()
        }
    }

    // ===== GetStudentsByClass Tests =====

    @Test
    fun `getStudentsByClass returns filtered students`() = runTest {
        val entities = listOf(
            createStudentEntity(id = 1, currentClass = "5th"),
            createStudentEntity(id = 2, currentClass = "5th")
        )
        every { studentDao.getStudentsByClass("5th") } returns flowOf(entities)

        repository.getStudentsByClass("5th").test {
            val students = awaitItem()
            assertThat(students).hasSize(2)
            assertThat(students.all { it.currentClass == "5th" }).isTrue()
            awaitComplete()
        }
    }

    // ===== GetStudentsByClassAndSection Tests =====

    @Test
    fun `getStudentsByClassAndSection returns filtered students`() = runTest {
        val entity = createStudentEntity(currentClass = "5th")
        every { studentDao.getStudentsByClassAndSection("5th", "A") } returns flowOf(listOf(entity))

        repository.getStudentsByClassAndSection("5th", "A").test {
            val students = awaitItem()
            assertThat(students).hasSize(1)
            awaitComplete()
        }
    }

    // ===== SearchStudents Tests =====

    @Test
    fun `searchStudents returns matching students`() = runTest {
        val entity = createStudentEntity(name = "John Doe")
        every { studentDao.searchStudents("John") } returns flowOf(listOf(entity))

        repository.searchStudents("John").test {
            val students = awaitItem()
            assertThat(students).hasSize(1)
            assertThat(students[0].name).isEqualTo("John Doe")
            awaitComplete()
        }
    }

    // ===== GetActiveStudentCount Tests =====

    @Test
    fun `getActiveStudentCount returns count from dao`() = runTest {
        every { studentDao.getActiveStudentCount() } returns flowOf(25)

        repository.getActiveStudentCount().test {
            assertThat(awaitItem()).isEqualTo(25)
            awaitComplete()
        }
    }

    // ===== srNumberExists Tests =====

    @Test
    fun `srNumberExists returns true when exists`() = runTest {
        coEvery { studentDao.srNumberExists("SR001") } returns true

        val result = repository.srNumberExists("SR001")

        assertThat(result).isTrue()
    }

    @Test
    fun `srNumberExists returns false when not exists`() = runTest {
        coEvery { studentDao.srNumberExists("SR999") } returns false

        val result = repository.srNumberExists("SR999")

        assertThat(result).isFalse()
    }

    // ===== srNumberExistsExcluding Tests =====

    @Test
    fun `srNumberExistsExcluding excludes specified id`() = runTest {
        coEvery { studentDao.srNumberExistsExcluding("SR001", 1L) } returns false

        val result = repository.srNumberExistsExcluding("SR001", 1L)

        assertThat(result).isFalse()
        coVerify { studentDao.srNumberExistsExcluding("SR001", 1L) }
    }

    // ===== GetStudentsWithBalance Tests =====

    @Test
    fun `getStudentsWithBalance combines student and balance data`() = runTest {
        val entity = createStudentEntity(id = 1)
        every { studentDao.getAllActiveStudents() } returns flowOf(listOf(entity))
        coEvery { ledgerDao.getCurrentBalance(1L) } returns 5000.0

        repository.getStudentsWithBalance().test {
            val studentsWithBalance = awaitItem()
            assertThat(studentsWithBalance).hasSize(1)
            assertThat(studentsWithBalance[0].student.id).isEqualTo(1L)
            assertThat(studentsWithBalance[0].currentBalance).isEqualTo(5000.0)
            awaitComplete()
        }
    }

    @Test
    fun `getStudentsWithBalance handles zero balance`() = runTest {
        val entity = createStudentEntity(id = 1)
        every { studentDao.getAllActiveStudents() } returns flowOf(listOf(entity))
        coEvery { ledgerDao.getCurrentBalance(1L) } returns 0.0

        repository.getStudentsWithBalance().test {
            val studentsWithBalance = awaitItem()
            assertThat(studentsWithBalance[0].currentBalance).isEqualTo(0.0)
            assertThat(studentsWithBalance[0].isCleared).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `getStudentsWithBalance handles negative balance (advance)`() = runTest {
        val entity = createStudentEntity(id = 1)
        every { studentDao.getAllActiveStudents() } returns flowOf(listOf(entity))
        coEvery { ledgerDao.getCurrentBalance(1L) } returns -1000.0

        repository.getStudentsWithBalance().test {
            val studentsWithBalance = awaitItem()
            assertThat(studentsWithBalance[0].currentBalance).isEqualTo(-1000.0)
            assertThat(studentsWithBalance[0].hasAdvance).isTrue()
            awaitComplete()
        }
    }

    // ===== GetStudentsWithDues Tests =====

    @Test
    fun `getStudentsWithDues returns only students with positive balance`() = runTest {
        val entities = listOf(
            createStudentEntity(id = 1),
            createStudentEntity(id = 2),
            createStudentEntity(id = 3)
        )
        every { studentDao.getAllActiveStudents() } returns flowOf(entities)
        every { ledgerDao.getStudentIdsWithDues() } returns flowOf(listOf(1L, 3L))
        coEvery { ledgerDao.getCurrentBalance(1L) } returns 5000.0
        coEvery { ledgerDao.getCurrentBalance(3L) } returns 3000.0

        repository.getStudentsWithDues().test {
            val studentsWithDues = awaitItem()
            assertThat(studentsWithDues).hasSize(2)
            assertThat(studentsWithDues.map { it.student.id }).containsExactly(1L, 3L)
            awaitComplete()
        }
    }

    @Test
    fun `getStudentsWithDues returns empty list when no dues`() = runTest {
        val entities = listOf(createStudentEntity(id = 1))
        every { studentDao.getAllActiveStudents() } returns flowOf(entities)
        every { ledgerDao.getStudentIdsWithDues() } returns flowOf(emptyList())

        repository.getStudentsWithDues().test {
            val studentsWithDues = awaitItem()
            assertThat(studentsWithDues).isEmpty()
            awaitComplete()
        }
    }
}
