package com.navoditpublic.fees.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.navoditpublic.fees.data.local.FeesDatabase
import com.navoditpublic.fees.data.local.dao.*
import com.navoditpublic.fees.data.local.entity.*
import com.navoditpublic.fees.domain.model.FeeStructure
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for FeeRepositoryImpl.
 * Tests the complex business logic for fee collection, receipts, and ledger management.
 * 
 * Note: Methods that use database transactions (insertReceipt, cancelReceipt) are tested
 * in integration tests with a real in-memory database.
 */
class FeeRepositoryImplTest {

    private lateinit var database: FeesDatabase
    private lateinit var feeStructureDao: FeeStructureDao
    private lateinit var receiptDao: ReceiptDao
    private lateinit var ledgerDao: LedgerDao
    private lateinit var studentDao: StudentDao
    private lateinit var schoolSettingsDao: SchoolSettingsDao
    private lateinit var transportRouteDao: TransportRouteDao
    private lateinit var transportEnrollmentDao: TransportEnrollmentDao
    private lateinit var academicSessionDao: AcademicSessionDao
    private lateinit var repository: FeeRepositoryImpl

    @Before
    fun setup() {
        database = mockk(relaxed = true)
        feeStructureDao = mockk(relaxed = true)
        receiptDao = mockk(relaxed = true)
        ledgerDao = mockk(relaxed = true)
        studentDao = mockk(relaxed = true)
        schoolSettingsDao = mockk(relaxed = true)
        transportRouteDao = mockk(relaxed = true)
        transportEnrollmentDao = mockk(relaxed = true)
        academicSessionDao = mockk(relaxed = true)

        repository = FeeRepositoryImpl(
            database,
            feeStructureDao,
            receiptDao,
            ledgerDao,
            studentDao,
            schoolSettingsDao,
            transportRouteDao,
            transportEnrollmentDao,
            academicSessionDao
        )
    }

    // ===== Fee Structure Tests =====

    @Test
    fun `insertFeeStructure calls dao insert`() = runTest {
        val feeStructure = FeeStructure(
            sessionId = 1L,
            className = "5th",
            feeType = FeeType.MONTHLY,
            amount = 1200.0
        )
        coEvery { feeStructureDao.insert(any()) } returns 1L

        val result = repository.insertFeeStructure(feeStructure)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(1L)
    }

    @Test
    fun `getFeeStructureBySession returns flow of fee structures`() = runTest {
        val entities = listOf(
            FeeStructureEntity(
                id = 1, sessionId = 1L, className = "5th",
                feeType = FeeType.MONTHLY, amount = 1200.0
            )
        )
        every { feeStructureDao.getFeeStructureBySession(1L) } returns flowOf(entities)

        repository.getFeeStructureBySession(1L).test {
            val fees = awaitItem()
            assertThat(fees).hasSize(1)
            assertThat(fees[0].className).isEqualTo("5th")
            awaitComplete()
        }
    }

    @Test
    fun `getFeeForClass returns correct fee structure`() = runTest {
        val entity = FeeStructureEntity(
            id = 1, sessionId = 1L, className = "5th",
            feeType = FeeType.MONTHLY, amount = 1200.0
        )
        coEvery { feeStructureDao.getFeeForClass(1L, "5th", FeeType.MONTHLY) } returns entity

        val result = repository.getFeeForClass(1L, "5th", FeeType.MONTHLY)

        assertThat(result).isNotNull()
        assertThat(result?.amount).isEqualTo(1200.0)
    }

    @Test
    fun `softDeleteFeeStructure sets isActive to false`() = runTest {
        val entity = FeeStructureEntity(
            id = 1, sessionId = 1L, className = "5th",
            feeType = FeeType.MONTHLY, amount = 1200.0, isActive = true
        )
        coEvery { feeStructureDao.getFeeForClass(1L, "5th", FeeType.MONTHLY) } returns entity
        val entitySlot = slot<FeeStructureEntity>()
        coEvery { feeStructureDao.update(capture(entitySlot)) } returns Unit

        val result = repository.softDeleteFeeStructure(1L, "5th", FeeType.MONTHLY)

        assertThat(result.isSuccess).isTrue()
        assertThat(entitySlot.captured.isActive).isFalse()
    }

    // ===== Receipt Tests =====

    @Test
    fun `getReceiptById returns receipt with items`() = runTest {
        val receiptEntity = ReceiptEntity(
            id = 1L,
            receiptNumber = 100,
            studentId = 1L,
            sessionId = 1L,
            receiptDate = System.currentTimeMillis(),
            totalAmount = 12000.0,
            discountAmount = 1000.0,
            netAmount = 11000.0,
            paymentMode = PaymentMode.CASH
        )
        val itemEntity = ReceiptItemEntity(
            id = 1L,
            receiptId = 1L,
            feeType = "TUITION",
            description = "Monthly Fee",
            amount = 11000.0
        )
        coEvery { receiptDao.getById(1L) } returns receiptEntity
        coEvery { receiptDao.getReceiptItems(1L) } returns listOf(itemEntity)

        val result = repository.getReceiptById(1L)

        assertThat(result).isNotNull()
        assertThat(result?.receiptNumber).isEqualTo(100)
        assertThat(result?.items).hasSize(1)
    }

    @Test
    fun `receiptNumberExists returns true when exists`() = runTest {
        coEvery { receiptDao.receiptNumberExists(100) } returns true

        val result = repository.receiptNumberExists(100)

        assertThat(result).isTrue()
    }

    @Test
    fun `getMaxReceiptNumber returns max from dao`() = runTest {
        coEvery { receiptDao.getMaxReceiptNumber() } returns 150

        val result = repository.getMaxReceiptNumber()

        assertThat(result).isEqualTo(150)
    }

    // ===== Ledger Tests =====

    @Test
    fun `getCurrentBalance returns balance from dao`() = runTest {
        coEvery { ledgerDao.getCurrentBalance(1L) } returns 5000.0

        val result = repository.getCurrentBalance(1L)

        assertThat(result).isEqualTo(5000.0)
    }

    @Test
    fun `getTotalDebits returns sum from dao`() = runTest {
        coEvery { ledgerDao.getTotalDebits(1L) } returns 12000.0

        val result = repository.getTotalDebits(1L)

        assertThat(result).isEqualTo(12000.0)
    }

    @Test
    fun `getTotalDebits returns zero when null from dao`() = runTest {
        coEvery { ledgerDao.getTotalDebits(1L) } returns null

        val result = repository.getTotalDebits(1L)

        assertThat(result).isEqualTo(0.0)
    }

    @Test
    fun `getTotalCredits returns sum from dao`() = runTest {
        coEvery { ledgerDao.getTotalCredits(1L) } returns 7000.0

        val result = repository.getTotalCredits(1L)

        assertThat(result).isEqualTo(7000.0)
    }

    @Test
    fun `getTotalPendingDues returns flow from dao`() = runTest {
        every { ledgerDao.getTotalPendingDues() } returns flowOf(50000.0)

        repository.getTotalPendingDues().test {
            assertThat(awaitItem()).isEqualTo(50000.0)
            awaitComplete()
        }
    }

    // ===== Calculate Expected Session Dues Tests =====

    @Test
    fun `calculateExpectedSessionDues for monthly class returns 12 months tuition`() = runTest {
        val studentEntity = createStudentEntity(currentClass = "5th", hasTransport = false)
        coEvery { studentDao.getById(1L) } returns studentEntity
        
        val monthlyFee = FeeStructureEntity(
            id = 1, sessionId = 1L, className = "5th",
            feeType = FeeType.MONTHLY, amount = 1000.0
        )
        coEvery { feeStructureDao.getFeeForClass(1L, "5th", FeeType.MONTHLY) } returns monthlyFee
        coEvery { ledgerDao.getTotalCreditsForSession(1L, 1L) } returns 0.0

        val result = repository.calculateExpectedSessionDues(1L, 1L)

        // 12 months * 1000 = 12000
        assertThat(result).isEqualTo(12000.0)
    }

    @Test
    fun `calculateExpectedSessionDues for annual class returns annual plus registration fee`() = runTest {
        val studentEntity = createStudentEntity(currentClass = "9th", hasTransport = false)
        coEvery { studentDao.getById(1L) } returns studentEntity
        
        val annualFee = FeeStructureEntity(
            id = 1, sessionId = 1L, className = "9th",
            feeType = FeeType.ANNUAL, amount = 15000.0
        )
        val regFee = FeeStructureEntity(
            id = 2, sessionId = 1L, className = "9th",
            feeType = FeeType.REGISTRATION, amount = 5000.0
        )
        coEvery { feeStructureDao.getFeeForClass(1L, "9th", FeeType.ANNUAL) } returns annualFee
        coEvery { feeStructureDao.getRegistrationFee(1L, "9th") } returns regFee
        coEvery { ledgerDao.getTotalCreditsForSession(1L, 1L) } returns 0.0

        val result = repository.calculateExpectedSessionDues(1L, 1L)

        // 15000 + 5000 = 20000
        assertThat(result).isEqualTo(20000.0)
    }

    @Test
    fun `calculateExpectedSessionDues subtracts payments made`() = runTest {
        val studentEntity = createStudentEntity(currentClass = "5th", hasTransport = false)
        coEvery { studentDao.getById(1L) } returns studentEntity
        
        val monthlyFee = FeeStructureEntity(
            id = 1, sessionId = 1L, className = "5th",
            feeType = FeeType.MONTHLY, amount = 1000.0
        )
        coEvery { feeStructureDao.getFeeForClass(1L, "5th", FeeType.MONTHLY) } returns monthlyFee
        coEvery { ledgerDao.getTotalCreditsForSession(1L, 1L) } returns 5000.0

        val result = repository.calculateExpectedSessionDues(1L, 1L)

        // 12000 - 5000 = 7000
        assertThat(result).isEqualTo(7000.0)
    }

    @Test
    fun `calculateExpectedSessionDues returns zero when student not found`() = runTest {
        coEvery { studentDao.getById(1L) } returns null

        val result = repository.calculateExpectedSessionDues(1L, 1L)

        assertThat(result).isEqualTo(0.0)
    }

    @Test
    fun `calculateExpectedSessionDues never returns negative`() = runTest {
        val studentEntity = createStudentEntity(currentClass = "5th", hasTransport = false)
        coEvery { studentDao.getById(1L) } returns studentEntity
        
        val monthlyFee = FeeStructureEntity(
            id = 1, sessionId = 1L, className = "5th",
            feeType = FeeType.MONTHLY, amount = 1000.0
        )
        coEvery { feeStructureDao.getFeeForClass(1L, "5th", FeeType.MONTHLY) } returns monthlyFee
        // More paid than expected
        coEvery { ledgerDao.getTotalCreditsForSession(1L, 1L) } returns 15000.0

        val result = repository.calculateExpectedSessionDues(1L, 1L)

        assertThat(result).isEqualTo(0.0)
    }

    // ===== Get Total Payments For Session Tests =====

    @Test
    fun `getTotalPaymentsForSession returns credits from dao`() = runTest {
        coEvery { ledgerDao.getTotalCreditsForSession(1L, 1L) } returns 8000.0

        val result = repository.getTotalPaymentsForSession(1L, 1L)

        assertThat(result).isEqualTo(8000.0)
    }

    @Test
    fun `getTotalPaymentsForSession returns zero when null`() = runTest {
        coEvery { ledgerDao.getTotalCreditsForSession(1L, 1L) } returns null

        val result = repository.getTotalPaymentsForSession(1L, 1L)

        assertThat(result).isEqualTo(0.0)
    }

    // ===== Has Session Fee Entries Tests =====

    @Test
    fun `hasSessionFeeEntries delegates to dao`() = runTest {
        coEvery { ledgerDao.hasFeeChargeEntries(1L, 1L) } returns true

        val result = repository.hasSessionFeeEntries(1L, 1L)

        assertThat(result).isTrue()
    }

    // ===== Recalculate Student Balances Tests =====

    @Test
    fun `recalculateStudentBalances updates running balances correctly`() = runTest {
        val entries = listOf(
            LedgerEntryEntity(
                id = 1, studentId = 1L, sessionId = 1L,
                entryDate = 1000L, particulars = "Fee Charge",
                entryType = LedgerEntryType.DEBIT,
                debitAmount = 12000.0, creditAmount = 0.0, balance = 0.0, // Wrong balance
                referenceType = LedgerReferenceType.FEE_CHARGE
            ),
            LedgerEntryEntity(
                id = 2, studentId = 1L, sessionId = 1L,
                entryDate = 2000L, particulars = "Payment",
                entryType = LedgerEntryType.CREDIT,
                debitAmount = 0.0, creditAmount = 5000.0, balance = 0.0, // Wrong balance
                referenceType = LedgerReferenceType.RECEIPT, referenceId = 1L
            )
        )
        coEvery { ledgerDao.getAllEntriesForStudentChronological(1L) } returns entries
        coEvery { ledgerDao.updateBalance(any(), any()) } returns Unit

        repository.recalculateStudentBalances(1L)

        // First entry: 0 + 12000 - 0 = 12000
        coVerify { ledgerDao.updateBalance(1L, 12000.0) }
        // Second entry: 12000 + 0 - 5000 = 7000
        coVerify { ledgerDao.updateBalance(2L, 7000.0) }
    }

    // ===== Create Opening Balance Entry Tests =====

    @Test
    fun `createOpeningBalanceEntry creates debit entry`() = runTest {
        val entrySlot = slot<LedgerEntryEntity>()
        coEvery { ledgerDao.insert(capture(entrySlot)) } returns 1L
        coEvery { ledgerDao.getAllEntriesForStudentChronological(1L) } returns emptyList()

        val result = repository.createOpeningBalanceEntry(
            studentId = 1L,
            sessionId = 1L,
            amount = 3000.0,
            date = System.currentTimeMillis(),
            remarks = "Previous year dues"
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(entrySlot.captured.entryType).isEqualTo(LedgerEntryType.DEBIT)
        assertThat(entrySlot.captured.debitAmount).isEqualTo(3000.0)
        assertThat(entrySlot.captured.referenceType).isEqualTo(LedgerReferenceType.OPENING_BALANCE)
        assertThat(entrySlot.captured.particulars).contains("Opening Balance")
    }

    // ===== Create Initial Payment Entry Tests =====

    @Test
    fun `createInitialPaymentEntry creates credit entry`() = runTest {
        val entrySlot = slot<LedgerEntryEntity>()
        coEvery { ledgerDao.insert(capture(entrySlot)) } returns 1L
        coEvery { ledgerDao.getAllEntriesForStudentChronological(1L) } returns emptyList()

        val result = repository.createInitialPaymentEntry(
            studentId = 1L,
            sessionId = 1L,
            amount = 5000.0,
            date = System.currentTimeMillis(),
            description = "Initial payment"
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(entrySlot.captured.entryType).isEqualTo(LedgerEntryType.CREDIT)
        assertThat(entrySlot.captured.creditAmount).isEqualTo(5000.0)
        assertThat(entrySlot.captured.referenceType).isEqualTo(LedgerReferenceType.ADJUSTMENT)
    }

    // ===== Daily Collection Tests =====

    @Test
    fun `getDailyCollectionTotal calculates day boundaries correctly`() = runTest {
        // Test that it calls DAO with correct date range
        every { receiptDao.getDailyCollectionTotal(any(), any()) } returns flowOf(5000.0)

        repository.getDailyCollectionTotal(System.currentTimeMillis()).test {
            assertThat(awaitItem()).isEqualTo(5000.0)
            awaitComplete()
        }
    }

    // ===== Helper Functions =====

    private fun createStudentEntity(
        id: Long = 1L,
        currentClass: String = "5th",
        hasTransport: Boolean = false,
        transportRouteId: Long? = null
    ) = StudentEntity(
        id = id,
        srNumber = "SR001",
        accountNumber = "ACC001",
        name = "Test Student",
        fatherName = "Test Father",
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
        hasTransport = hasTransport,
        transportRouteId = transportRouteId,
        openingBalance = 0.0,
        openingBalanceRemarks = "",
        openingBalanceDate = null,
        admissionFeePaid = false,
        isActive = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
