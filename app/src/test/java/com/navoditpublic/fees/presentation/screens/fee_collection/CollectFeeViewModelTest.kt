package com.navoditpublic.fees.presentation.screens.fee_collection

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.navoditpublic.fees.data.local.entity.FeeType
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.model.FeeStructure
import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.model.StudentWithBalance
import com.navoditpublic.fees.domain.repository.AuditRepository
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.presentation.screens.fee_collection.collect.CollectFeeEvent
import com.navoditpublic.fees.presentation.screens.fee_collection.collect.CollectFeeViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for CollectFeeViewModel.
 * Tests fee collection flow, validation, and state management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CollectFeeViewModelTest {

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var studentRepository: StudentRepository
    private lateinit var feeRepository: FeeRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var auditRepository: AuditRepository
    private lateinit var viewModel: CollectFeeViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        savedStateHandle = mockk(relaxed = true)
        studentRepository = mockk(relaxed = true)
        feeRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        auditRepository = mockk(relaxed = true)

        // Default mock behavior
        every { savedStateHandle.get<String>("studentId") } returns null
        coEvery { settingsRepository.getCurrentSession() } returns createTestSession()
        every { studentRepository.getAllActiveStudents() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CollectFeeViewModel {
        return CollectFeeViewModel(
            savedStateHandle,
            studentRepository,
            feeRepository,
            settingsRepository,
            auditRepository
        )
    }

    private fun createTestStudent(
        id: Long = 1L,
        name: String = "Test Student",
        currentClass: String = "5th"
    ) = Student(
        id = id,
        srNumber = "SR001",
        accountNumber = "ACC001",
        name = name,
        fatherName = "Test Father",
        phonePrimary = "9876543210",
        currentClass = currentClass,
        admissionDate = System.currentTimeMillis(),
        admissionSessionId = 1L
    )

    private fun createTestSession() = AcademicSession(
        id = 1L,
        sessionName = "2025-26",
        startDate = System.currentTimeMillis(),
        endDate = System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L,
        isCurrent = true
    )

    // ===== Initial State Tests =====

    @Test
    fun `initial state has isLoading true`() = runTest {
        viewModel = createViewModel()
        
        assertThat(viewModel.state.value.isLoading).isTrue()
    }

    @Test
    fun `initial state has empty receipt number`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.receiptNumber).isEmpty()
    }

    // ===== Receipt Number Tests =====

    @Test
    fun `updateReceiptNumber filters non-digit characters`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateReceiptNumber("abc123def456")
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.receiptNumber).isEqualTo("123456")
    }

    @Test
    fun `updateReceiptNumber clears error`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // First set an error state (simulate by updating state manually via the flow)
        viewModel.updateReceiptNumber("100")
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.receiptNumberError).isNull()
    }

    @Test
    fun `updateReceiptNumber checks for duplicates`() = runTest {
        coEvery { feeRepository.receiptNumberExists(100) } returns true
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateReceiptNumber("100")
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.showDuplicateWarning).isTrue()
    }

    @Test
    fun `updateReceiptNumber shows no warning when receipt does not exist`() = runTest {
        coEvery { feeRepository.receiptNumberExists(100) } returns false
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateReceiptNumber("100")
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.showDuplicateWarning).isFalse()
    }

    // ===== Receipt Date Tests =====

    @Test
    fun `updateReceiptDate rejects future dates`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val futureDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L // 7 days in future
        viewModel.updateReceiptDate(futureDate)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.dateError).isNotNull()
        assertThat(viewModel.state.value.dateError).contains("Future")
    }

    @Test
    fun `updateReceiptDate flags backdated receipts`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val pastDate = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L // 3 days ago
        viewModel.updateReceiptDate(pastDate)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.isBackdatedReceipt).isTrue()
        assertThat(viewModel.state.value.showBackdateWarning).isTrue()
    }

    @Test
    fun `updateReceiptDate accepts today date`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }.timeInMillis
        
        viewModel.updateReceiptDate(today)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.dateError).isNull()
    }

    @Test
    fun `dismissBackdateWarning clears warning flag`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val pastDate = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L
        viewModel.updateReceiptDate(pastDate)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.dismissBackdateWarning()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.showBackdateWarning).isFalse()
        assertThat(viewModel.state.value.isBackdatedReceipt).isTrue() // Still backdated
    }

    // ===== Student Search Tests =====

    @Test
    fun `searchStudents requires minimum 2 characters`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.searchStudents("A")
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.searchResults).isEmpty()
    }

    @Test
    fun `searchStudents calls repository when query is 2+ characters`() = runTest {
        val students = listOf(createTestStudent())
        every { studentRepository.searchStudents("Te") } returns flowOf(students)
        coEvery { feeRepository.calculateExpectedSessionDues(any(), any()) } returns 5000.0
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.searchStudents("Te")
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.searchResults).isNotEmpty()
    }

    // ===== Select Student Tests =====

    @Test
    fun `selectStudent updates state with student and balance`() = runTest {
        val student = createTestStudent()
        val studentWithBalance = StudentWithBalance(student, 5000.0)
        coEvery { feeRepository.getFeeForClass(any(), any(), FeeType.MONTHLY) } returns 
            FeeStructure(sessionId = 1, className = "5th", feeType = FeeType.MONTHLY, amount = 1000.0)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectStudent(studentWithBalance)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.selectedStudent).isNotNull()
        assertThat(viewModel.state.value.selectedStudent?.student?.name).isEqualTo("Test Student")
        assertThat(viewModel.state.value.currentDues).isEqualTo(5000.0)
    }

    @Test
    fun `selectStudent clears search results`() = runTest {
        val student = createTestStudent()
        val studentWithBalance = StudentWithBalance(student, 5000.0)
        coEvery { feeRepository.getFeeForClass(any(), any(), FeeType.MONTHLY) } returns null
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectStudent(studentWithBalance)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.studentSearchQuery).isEmpty()
        assertThat(viewModel.state.value.searchResults).isEmpty()
    }

    @Test
    fun `selectStudent calculates monthly discount for monthly class`() = runTest {
        val student = createTestStudent(currentClass = "5th")
        val studentWithBalance = StudentWithBalance(student, 12000.0)
        coEvery { feeRepository.getFeeForClass(any(), "5th", FeeType.MONTHLY) } returns 
            FeeStructure(sessionId = 1, className = "5th", feeType = FeeType.MONTHLY, amount = 1000.0)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectStudent(studentWithBalance)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.fullYearDiscountAmount).isEqualTo(1000.0)
    }

    @Test
    fun `selectStudent sets zero discount for annual class`() = runTest {
        val student = createTestStudent(currentClass = "9th")
        val studentWithBalance = StudentWithBalance(student, 20000.0)
        // 9th is not in MONTHLY_FEE_CLASSES, so no monthly fee lookup
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectStudent(studentWithBalance)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.fullYearDiscountAmount).isEqualTo(0.0)
    }

    // ===== Clear Student Tests =====

    @Test
    fun `clearStudent resets student-related state`() = runTest {
        val student = createTestStudent()
        val studentWithBalance = StudentWithBalance(student, 5000.0)
        coEvery { feeRepository.getFeeForClass(any(), any(), FeeType.MONTHLY) } returns null
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectStudent(studentWithBalance)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.clearStudent()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.selectedStudent).isNull()
        assertThat(viewModel.state.value.currentDues).isEqualTo(0.0)
        assertThat(viewModel.state.value.amountReceived).isEmpty()
    }

    // ===== Amount Received Tests =====

    @Test
    fun `updateAmountReceived filters invalid characters`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateAmountReceived("abc11000xyz")
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.amountReceived).isEqualTo("11000")
    }

    @Test
    fun `updateAmountReceived allows decimal point`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateAmountReceived("1000.50")
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.amountReceived).isEqualTo("1000.50")
    }

    // ===== Full Year Payment Tests =====

    @Test
    fun `toggleFullYearPayment applies discount when enabled`() = runTest {
        val student = createTestStudent(currentClass = "5th")
        val studentWithBalance = StudentWithBalance(student, 12000.0)
        coEvery { feeRepository.getFeeForClass(any(), "5th", FeeType.MONTHLY) } returns 
            FeeStructure(sessionId = 1, className = "5th", feeType = FeeType.MONTHLY, amount = 1000.0)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectStudent(studentWithBalance)
        testDispatcher.scheduler.advanceUntilIdle()  // Wait for selectStudent coroutine
        
        viewModel.updateAmountReceived("11000")
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.toggleFullYearPayment(true)
        testDispatcher.scheduler.advanceUntilIdle()  // Wait for recalculateTotals
        
        assertThat(viewModel.state.value.isFullYearPayment).isTrue()
        assertThat(viewModel.state.value.discount).isEqualTo(1000.0)
    }

    @Test
    fun `toggleFullYearPayment removes discount when disabled`() = runTest {
        val student = createTestStudent(currentClass = "5th")
        val studentWithBalance = StudentWithBalance(student, 12000.0)
        coEvery { feeRepository.getFeeForClass(any(), "5th", FeeType.MONTHLY) } returns 
            FeeStructure(sessionId = 1, className = "5th", feeType = FeeType.MONTHLY, amount = 1000.0)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectStudent(studentWithBalance)
        viewModel.updateAmountReceived("11000")
        viewModel.toggleFullYearPayment(true)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleFullYearPayment(false)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.discount).isEqualTo(0.0)
    }

    // ===== Payment Mode Tests =====

    @Test
    fun `updatePaymentMode changes payment mode`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updatePaymentMode(PaymentMode.UPI)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.paymentMode).isEqualTo(PaymentMode.UPI)
    }

    @Test
    fun `default payment mode is CASH`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.paymentMode).isEqualTo(PaymentMode.CASH)
    }

    // ===== Save Receipt Validation Tests =====

    @Test
    fun `saveReceipt fails when receipt number is empty`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.saveReceipt()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.receiptNumberError).isNotNull()
    }

    @Test
    fun `saveReceipt fails when no student selected`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateReceiptNumber("100")
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.events.test {
            viewModel.saveReceipt()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val event = awaitItem()
            assertThat(event).isInstanceOf(CollectFeeEvent.Error::class.java)
            assertThat((event as CollectFeeEvent.Error).message).contains("student")
        }
    }

    @Test
    fun `saveReceipt fails when amount is zero`() = runTest {
        val student = createTestStudent()
        val studentWithBalance = StudentWithBalance(student, 5000.0)
        coEvery { feeRepository.getFeeForClass(any(), any(), FeeType.MONTHLY) } returns null
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateReceiptNumber("100")
        viewModel.selectStudent(studentWithBalance)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.events.test {
            viewModel.saveReceipt()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val event = awaitItem()
            assertThat(event).isInstanceOf(CollectFeeEvent.Error::class.java)
            assertThat((event as CollectFeeEvent.Error).message).contains("amount")
        }
    }

    @Test
    fun `saveReceipt fails when full year discount with insufficient amount`() = runTest {
        val student = createTestStudent(currentClass = "5th")
        val studentWithBalance = StudentWithBalance(student, 12000.0)
        coEvery { feeRepository.getFeeForClass(any(), "5th", FeeType.MONTHLY) } returns 
            FeeStructure(sessionId = 1, className = "5th", feeType = FeeType.MONTHLY, amount = 1000.0)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateReceiptNumber("100")
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectStudent(studentWithBalance)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateAmountReceived("5000") // Less than 11 * 1000 = 11000
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.toggleFullYearPayment(true)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.events.test {
            viewModel.saveReceipt()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val event = awaitItem()
            assertThat(event).isInstanceOf(CollectFeeEvent.Error::class.java)
            assertThat((event as CollectFeeEvent.Error).message).contains("minimum")
        }
    }

    @Test
    fun `saveReceipt succeeds with valid data`() = runTest {
        val student = createTestStudent()
        val studentWithBalance = StudentWithBalance(student, 5000.0)
        coEvery { feeRepository.getFeeForClass(any(), any(), FeeType.MONTHLY) } returns null
        coEvery { feeRepository.insertReceipt(any(), any()) } returns Result.success(1L)
        coEvery { auditRepository.logCreate(any(), any(), any()) } returns 1L
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateReceiptNumber("100")
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectStudent(studentWithBalance)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateAmountReceived("5000")
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.events.test {
            viewModel.saveReceipt()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val event = awaitItem()
            assertThat(event).isInstanceOf(CollectFeeEvent.Success::class.java)
        }
    }

    @Test
    fun `saveReceipt handles duplicate receipt number error`() = runTest {
        val student = createTestStudent()
        val studentWithBalance = StudentWithBalance(student, 5000.0)
        coEvery { feeRepository.getFeeForClass(any(), any(), FeeType.MONTHLY) } returns null
        coEvery { feeRepository.insertReceipt(any(), any()) } returns 
            Result.failure(Exception("UNIQUE constraint failed"))
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateReceiptNumber("100")
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectStudent(studentWithBalance)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.updateAmountReceived("5000")
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.events.test {
            viewModel.saveReceipt()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val event = awaitItem()
            assertThat(event).isInstanceOf(CollectFeeEvent.Error::class.java)
            assertThat((event as CollectFeeEvent.Error).message).contains("already exists")
        }
    }

    // ===== Pre-selected Student Tests =====

    @Test
    fun `pre-selected student is loaded from savedStateHandle`() = runTest {
        val student = createTestStudent(id = 5L)
        every { savedStateHandle.get<String>("studentId") } returns "5"
        coEvery { studentRepository.getById(5L) } returns student
        coEvery { feeRepository.calculateExpectedSessionDues(5L, any()) } returns 8000.0
        coEvery { feeRepository.getFeeForClass(any(), any(), FeeType.MONTHLY) } returns null
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.selectedStudent).isNotNull()
        assertThat(viewModel.state.value.selectedStudent?.student?.id).isEqualTo(5L)
    }
}
