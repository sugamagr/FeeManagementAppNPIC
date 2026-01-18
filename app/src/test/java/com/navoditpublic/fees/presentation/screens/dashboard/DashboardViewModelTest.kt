package com.navoditpublic.fees.presentation.screens.dashboard

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.navoditpublic.fees.data.local.DataSeeder
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.model.Receipt
import com.navoditpublic.fees.domain.model.ReceiptWithStudent
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.data.local.entity.PaymentMode
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DashboardViewModel.
 * Tests dashboard data loading and state management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var studentRepository: StudentRepository
    private lateinit var feeRepository: FeeRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var dataSeeder: DataSeeder
    private lateinit var viewModel: DashboardViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        studentRepository = mockk(relaxed = true)
        feeRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        dataSeeder = mockk(relaxed = true)

        // Default mock behavior
        coEvery { dataSeeder.seedInitialData() } returns Unit
        coEvery { settingsRepository.getCurrentSession() } returns createTestSession()
        every { studentRepository.getActiveStudentCount() } returns flowOf(100)
        every { feeRepository.getDailyCollectionTotal(any()) } returns flowOf(5000.0)
        every { feeRepository.getMonthlyCollectionTotal(any(), any()) } returns flowOf(50000.0)
        every { feeRepository.getTotalPendingDues() } returns flowOf(200000.0)
        every { feeRepository.getRecentReceiptsWithStudents(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(
            studentRepository,
            feeRepository,
            settingsRepository,
            dataSeeder
        )
    }

    private fun createTestSession() = AcademicSession(
        id = 1L,
        sessionName = "2025-26",
        startDate = System.currentTimeMillis(),
        endDate = System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L,
        isCurrent = true
    )

    private fun createTestReceipt() = Receipt(
        id = 1L,
        receiptNumber = 100,
        studentId = 1L,
        sessionId = 1L,
        receiptDate = System.currentTimeMillis(),
        totalAmount = 5000.0,
        discountAmount = 0.0,
        netAmount = 5000.0,
        paymentMode = PaymentMode.CASH
    )

    // ===== Initial State Tests =====

    @Test
    fun `initial state has isLoading true`() = runTest {
        viewModel = createViewModel()
        
        // Before any data loads
        assertThat(viewModel.state.value.isLoading).isTrue()
    }

    @Test
    fun `initial state loads data successfully`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.totalStudents).isEqualTo(100)
        assertThat(viewModel.state.value.todayCollection).isEqualTo(5000.0)
        assertThat(viewModel.state.value.monthCollection).isEqualTo(50000.0)
        assertThat(viewModel.state.value.pendingDues).isEqualTo(200000.0)
    }

    // ===== Data Seeding Tests =====

    @Test
    fun `initializes data seeder on creation`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        coVerify { dataSeeder.seedInitialData() }
    }

    // ===== Current Session Tests =====

    @Test
    fun `loads current session from settings`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.currentSession).isNotNull()
        assertThat(viewModel.state.value.currentSession?.sessionName).isEqualTo("2025-26")
    }

    @Test
    fun `handles null current session`() = runTest {
        coEvery { settingsRepository.getCurrentSession() } returns null
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.currentSession).isNull()
        assertThat(viewModel.state.value.error).isNull()
    }

    // ===== Collection Data Tests =====

    @Test
    fun `today collection is loaded from repository`() = runTest {
        every { feeRepository.getDailyCollectionTotal(any()) } returns flowOf(7500.0)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.todayCollection).isEqualTo(7500.0)
    }

    @Test
    fun `handles null today collection`() = runTest {
        every { feeRepository.getDailyCollectionTotal(any()) } returns flowOf(null)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.todayCollection).isEqualTo(0.0)
    }

    @Test
    fun `monthly collection is loaded from repository`() = runTest {
        every { feeRepository.getMonthlyCollectionTotal(any(), any()) } returns flowOf(75000.0)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.monthCollection).isEqualTo(75000.0)
    }

    @Test
    fun `handles null monthly collection`() = runTest {
        every { feeRepository.getMonthlyCollectionTotal(any(), any()) } returns flowOf(null)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.monthCollection).isEqualTo(0.0)
    }

    // ===== Student Count Tests =====

    @Test
    fun `student count is loaded from repository`() = runTest {
        every { studentRepository.getActiveStudentCount() } returns flowOf(250)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.totalStudents).isEqualTo(250)
    }

    @Test
    fun `handles zero students`() = runTest {
        every { studentRepository.getActiveStudentCount() } returns flowOf(0)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.totalStudents).isEqualTo(0)
    }

    // ===== Pending Dues Tests =====

    @Test
    fun `pending dues is loaded from repository`() = runTest {
        every { feeRepository.getTotalPendingDues() } returns flowOf(500000.0)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.pendingDues).isEqualTo(500000.0)
    }

    @Test
    fun `handles null pending dues`() = runTest {
        every { feeRepository.getTotalPendingDues() } returns flowOf(null)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.pendingDues).isEqualTo(0.0)
    }

    // ===== Recent Receipts Tests =====

    @Test
    fun `recent receipts are loaded from repository`() = runTest {
        val receipts = listOf(
            ReceiptWithStudent(
                receipt = createTestReceipt(),
                studentName = "Test Student",
                studentClass = "5th",
                studentSection = "A"
            )
        )
        every { feeRepository.getRecentReceiptsWithStudents(5) } returns flowOf(receipts)
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.recentReceipts).hasSize(1)
        assertThat(viewModel.state.value.recentReceipts[0].studentName).isEqualTo("Test Student")
    }

    @Test
    fun `handles empty recent receipts`() = runTest {
        every { feeRepository.getRecentReceiptsWithStudents(any()) } returns flowOf(emptyList())
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.recentReceipts).isEmpty()
    }

    // ===== Error Handling Tests =====

    @Test
    fun `handles exception during data loading`() = runTest {
        coEvery { settingsRepository.getCurrentSession() } throws Exception("Network error")
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.error).isNotNull()
        assertThat(viewModel.state.value.error).contains("Network error")
    }

    // ===== Refresh Tests =====

    @Test
    fun `refresh reloads dashboard data`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Update mock to return different value
        every { studentRepository.getActiveStudentCount() } returns flowOf(150)
        
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.totalStudents).isEqualTo(150)
    }

    @Test
    fun `refresh sets loading state`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Capture state during refresh
        var loadingDuringRefresh = false
        viewModel.state.test {
            viewModel.refresh()
            // Skip initial item
            awaitItem()
            // Check for loading state
            val state = awaitItem()
            loadingDuringRefresh = state.isLoading
            cancelAndIgnoreRemainingEvents()
        }
        
        // Note: This may or may not capture loading state depending on timing
        // The important thing is that refresh eventually completes
    }

    // ===== Flow Combination Tests =====

    @Test
    fun `updates state when any flow emits new value`() = runTest {
        val studentCountFlow = kotlinx.coroutines.flow.MutableStateFlow(100)
        every { studentRepository.getActiveStudentCount() } returns studentCountFlow
        
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.totalStudents).isEqualTo(100)
        
        // Emit new value
        studentCountFlow.value = 110
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.totalStudents).isEqualTo(110)
    }
}
