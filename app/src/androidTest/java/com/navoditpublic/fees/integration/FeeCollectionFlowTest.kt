package com.navoditpublic.fees.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.navoditpublic.fees.data.local.FeesDatabase
import com.navoditpublic.fees.data.local.entity.*
import com.navoditpublic.fees.data.repository.FeeRepositoryImpl
import com.navoditpublic.fees.domain.model.Receipt
import com.navoditpublic.fees.domain.model.ReceiptItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for the fee collection flow.
 * Tests the complete flow from receipt creation to ledger updates.
 */
@RunWith(AndroidJUnit4::class)
class FeeCollectionFlowTest {

    private lateinit var database: FeesDatabase
    private lateinit var repository: FeeRepositoryImpl

    private var testStudentId: Long = 0
    private var testSessionId: Long = 0

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            FeesDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = FeeRepositoryImpl(
            database = database,
            feeStructureDao = database.feeStructureDao(),
            receiptDao = database.receiptDao(),
            ledgerDao = database.ledgerDao(),
            studentDao = database.studentDao(),
            schoolSettingsDao = database.schoolSettingsDao(),
            transportRouteDao = database.transportRouteDao(),
            transportEnrollmentDao = database.transportEnrollmentDao(),
            academicSessionDao = database.academicSessionDao()
        )

        // Setup test data
        runTest {
            testSessionId = database.academicSessionDao().insert(createTestSession())
            testStudentId = database.studentDao().insert(createTestStudent())
            database.schoolSettingsDao().insert(createSchoolSettings())
            
            // Add fee structure
            database.feeStructureDao().insert(
                FeeStructureEntity(
                    sessionId = testSessionId,
                    className = "5th",
                    feeType = FeeType.MONTHLY,
                    amount = 1000.0
                )
            )
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createTestSession() = AcademicSessionEntity(
        id = 0,
        sessionName = "2025-26",
        startDate = System.currentTimeMillis(),
        endDate = System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L,
        isCurrent = true,
        isActive = true
    )

    private fun createTestStudent() = StudentEntity(
        id = 0,
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
        currentClass = "5th",
        section = "A",
        admissionDate = System.currentTimeMillis(),
        admissionSessionId = 1L,
        hasTransport = false,
        transportRouteId = null,
        openingBalance = 0.0,
        openingBalanceRemarks = "",
        openingBalanceDate = null,
        admissionFeePaid = false,
        isActive = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun createSchoolSettings() = SchoolSettingsEntity(
        id = 1,
        schoolName = "Test School",
        tagline = "Test Tagline",
        addressLine1 = "Test Address",
        addressLine2 = "",
        district = "Test District",
        state = "Test State",
        lastReceiptNumber = 0
    )

    // ===== Basic Receipt Creation Tests =====

    @Test
    fun createReceipt_createsReceiptAndLedgerEntry() = runTest {
        // First add session fees to create initial dues
        repository.addSessionFeesForStudent(testStudentId, testSessionId, addTuition = true, addTransport = false)
        
        val initialBalance = repository.getCurrentBalance(testStudentId)
        assertThat(initialBalance).isEqualTo(12000.0) // 12 months * 1000
        
        // Create receipt
        val receipt = Receipt(
            receiptNumber = 1,
            studentId = testStudentId,
            sessionId = testSessionId,
            receiptDate = System.currentTimeMillis(),
            totalAmount = 5000.0,
            discountAmount = 0.0,
            netAmount = 5000.0,
            paymentMode = PaymentMode.CASH
        )
        val items = listOf(
            ReceiptItem(feeType = "FEE_PAYMENT", description = "Fee Payment", amount = 5000.0)
        )
        
        val result = repository.insertReceipt(receipt, items)
        
        assertThat(result.isSuccess).isTrue()
        
        // Verify balance updated
        val newBalance = repository.getCurrentBalance(testStudentId)
        assertThat(newBalance).isEqualTo(7000.0) // 12000 - 5000
    }

    @Test
    fun createReceipt_withDiscount_appliesDiscountToLedger() = runTest {
        // Add session fees
        repository.addSessionFeesForStudent(testStudentId, testSessionId, addTuition = true, addTransport = false)
        
        // Create receipt with full year discount
        val receipt = Receipt(
            receiptNumber = 1,
            studentId = testStudentId,
            sessionId = testSessionId,
            receiptDate = System.currentTimeMillis(),
            totalAmount = 12000.0,
            discountAmount = 1000.0, // 1 month free
            netAmount = 11000.0,
            paymentMode = PaymentMode.CASH
        )
        val items = listOf(
            ReceiptItem(feeType = "FEE_PAYMENT", description = "Full Year Payment", amount = 11000.0)
        )
        
        val result = repository.insertReceipt(receipt, items)
        
        assertThat(result.isSuccess).isTrue()
        
        // Balance should be 0 (12000 - 11000 payment - 1000 discount)
        val balance = repository.getCurrentBalance(testStudentId)
        assertThat(balance).isEqualTo(0.0)
    }

    // ===== Receipt Cancellation Tests =====

    @Test
    fun cancelReceipt_reversesLedgerEntries() = runTest {
        // Setup: Add fees and create receipt
        repository.addSessionFeesForStudent(testStudentId, testSessionId, addTuition = true, addTransport = false)
        
        val receipt = Receipt(
            receiptNumber = 1,
            studentId = testStudentId,
            sessionId = testSessionId,
            receiptDate = System.currentTimeMillis(),
            totalAmount = 5000.0,
            discountAmount = 0.0,
            netAmount = 5000.0,
            paymentMode = PaymentMode.CASH
        )
        val items = listOf(
            ReceiptItem(feeType = "FEE_PAYMENT", description = "Fee Payment", amount = 5000.0)
        )
        
        val receiptId = repository.insertReceipt(receipt, items).getOrThrow()
        
        // Verify balance after payment
        assertThat(repository.getCurrentBalance(testStudentId)).isEqualTo(7000.0)
        
        // Cancel receipt
        val cancelResult = repository.cancelReceipt(receiptId, "Cheque bounced")
        
        assertThat(cancelResult.isSuccess).isTrue()
        
        // Balance should be restored
        assertThat(repository.getCurrentBalance(testStudentId)).isEqualTo(12000.0)
        
        // Verify receipt is marked as cancelled
        val cancelledReceipt = repository.getReceiptById(receiptId)
        assertThat(cancelledReceipt?.isCancelled).isTrue()
        assertThat(cancelledReceipt?.cancellationReason).isEqualTo("Cheque bounced")
    }

    @Test
    fun cancelReceipt_withDiscount_reversesDiscountToo() = runTest {
        // Setup
        repository.addSessionFeesForStudent(testStudentId, testSessionId, addTuition = true, addTransport = false)
        
        val receipt = Receipt(
            receiptNumber = 1,
            studentId = testStudentId,
            sessionId = testSessionId,
            receiptDate = System.currentTimeMillis(),
            totalAmount = 12000.0,
            discountAmount = 1000.0,
            netAmount = 11000.0,
            paymentMode = PaymentMode.CASH
        )
        val items = listOf(
            ReceiptItem(feeType = "FEE_PAYMENT", description = "Full Year Payment", amount = 11000.0)
        )
        
        val receiptId = repository.insertReceipt(receipt, items).getOrThrow()
        
        // Balance should be 0 after full payment with discount
        assertThat(repository.getCurrentBalance(testStudentId)).isEqualTo(0.0)
        
        // Cancel receipt
        repository.cancelReceipt(receiptId, "Payment issue")
        
        // Balance should be restored to original (including discount reversal)
        assertThat(repository.getCurrentBalance(testStudentId)).isEqualTo(12000.0)
    }

    // ===== Session Fee Addition Tests =====

    @Test
    fun addSessionFeesForStudent_createsCorrectDebitEntries() = runTest {
        val result = repository.addSessionFeesForStudent(
            testStudentId, 
            testSessionId, 
            addTuition = true, 
            addTransport = false
        )
        
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(12000.0) // 12 * 1000
        
        // Verify ledger entries
        val entries = repository.getLedgerForStudent(testStudentId).first()
        assertThat(entries).hasSize(1)
        assertThat(entries[0].debitAmount).isEqualTo(12000.0)
        assertThat(entries[0].entryType).isEqualTo(LedgerEntryType.DEBIT)
    }

    @Test
    fun addSessionFeesForStudent_skipsIfAlreadyExists() = runTest {
        // Add fees first time
        repository.addSessionFeesForStudent(testStudentId, testSessionId, addTuition = true, addTransport = false)
        
        // Try to add again
        val result = repository.addSessionFeesForStudent(testStudentId, testSessionId, addTuition = true, addTransport = false)
        
        // Should return 0 because fees already exist
        assertThat(result.getOrNull()).isEqualTo(0.0)
        
        // Balance should still be 12000 (not doubled)
        assertThat(repository.getCurrentBalance(testStudentId)).isEqualTo(12000.0)
    }

    // ===== Opening Balance Tests =====

    @Test
    fun createOpeningBalanceEntry_createsDebitEntry() = runTest {
        val result = repository.createOpeningBalanceEntry(
            studentId = testStudentId,
            sessionId = testSessionId,
            amount = 3000.0,
            date = System.currentTimeMillis(),
            remarks = "Previous year dues"
        )
        
        assertThat(result.isSuccess).isTrue()
        
        val balance = repository.getCurrentBalance(testStudentId)
        assertThat(balance).isEqualTo(3000.0)
        
        val entries = repository.getLedgerForStudent(testStudentId).first()
        assertThat(entries).hasSize(1)
        assertThat(entries[0].particulars).contains("Opening Balance")
        assertThat(entries[0].particulars).contains("Previous year dues")
    }

    // ===== Balance Recalculation Tests =====

    @Test
    fun backdatedReceipt_recalculatesBalancesCorrectly() = runTest {
        val now = System.currentTimeMillis()
        val dayInMs = 24 * 60 * 60 * 1000L
        
        // Add session fees dated at session start (April 1)
        repository.addSessionFeesForStudent(testStudentId, testSessionId, addTuition = true, addTransport = false)
        
        // Create a receipt dated today
        val receipt1 = Receipt(
            receiptNumber = 1,
            studentId = testStudentId,
            sessionId = testSessionId,
            receiptDate = now,
            totalAmount = 3000.0,
            discountAmount = 0.0,
            netAmount = 3000.0,
            paymentMode = PaymentMode.CASH
        )
        repository.insertReceipt(receipt1, listOf(
            ReceiptItem(feeType = "FEE_PAYMENT", description = "Payment 1", amount = 3000.0)
        ))
        
        // Balance should be 9000
        assertThat(repository.getCurrentBalance(testStudentId)).isEqualTo(9000.0)
        
        // Create a backdated receipt (before today's receipt)
        val receipt2 = Receipt(
            receiptNumber = 2,
            studentId = testStudentId,
            sessionId = testSessionId,
            receiptDate = now - dayInMs, // Yesterday
            totalAmount = 2000.0,
            discountAmount = 0.0,
            netAmount = 2000.0,
            paymentMode = PaymentMode.CASH
        )
        repository.insertReceipt(receipt2, listOf(
            ReceiptItem(feeType = "FEE_PAYMENT", description = "Payment 2", amount = 2000.0)
        ))
        
        // Balance should be 7000 (12000 - 3000 - 2000)
        assertThat(repository.getCurrentBalance(testStudentId)).isEqualTo(7000.0)
        
        // Verify ledger entries are in chronological order
        val entries = repository.getLedgerForStudent(testStudentId).first()
        assertThat(entries).hasSize(3) // Fee charge + 2 payments
        
        // Verify running balances are correct
        // Entry 0: Fee charge = 12000
        // Entry 1: Payment 2 (backdated) = 10000
        // Entry 2: Payment 1 = 7000
        assertThat(entries[0].balance).isEqualTo(12000.0)
        assertThat(entries[1].balance).isEqualTo(10000.0)
        assertThat(entries[2].balance).isEqualTo(7000.0)
    }

    // ===== Expected Session Dues Calculation Tests =====

    @Test
    fun calculateExpectedSessionDues_forMonthlyClass() = runTest {
        // Add session fees
        repository.addSessionFeesForStudent(testStudentId, testSessionId, addTuition = true, addTransport = false)
        
        // No payments yet
        val dues = repository.calculateExpectedSessionDues(testStudentId, testSessionId)
        assertThat(dues).isEqualTo(12000.0)
        
        // Make partial payment
        val receipt = Receipt(
            receiptNumber = 1,
            studentId = testStudentId,
            sessionId = testSessionId,
            receiptDate = System.currentTimeMillis(),
            totalAmount = 5000.0,
            discountAmount = 0.0,
            netAmount = 5000.0,
            paymentMode = PaymentMode.CASH
        )
        repository.insertReceipt(receipt, listOf(
            ReceiptItem(feeType = "FEE_PAYMENT", description = "Payment", amount = 5000.0)
        ))
        
        // Dues should be reduced
        val remainingDues = repository.calculateExpectedSessionDues(testStudentId, testSessionId)
        assertThat(remainingDues).isEqualTo(7000.0)
    }

    // ===== Multiple Students Tests =====

    @Test
    fun multipleStudents_independentBalances() = runTest {
        // Create second student
        val student2Id = database.studentDao().insert(
            createTestStudent().copy(srNumber = "SR002", accountNumber = "ACC002", name = "Student 2")
        )
        
        // Add fees for both students
        repository.addSessionFeesForStudent(testStudentId, testSessionId, addTuition = true, addTransport = false)
        repository.addSessionFeesForStudent(student2Id, testSessionId, addTuition = true, addTransport = false)
        
        // Make payment for student 1 only
        val receipt = Receipt(
            receiptNumber = 1,
            studentId = testStudentId,
            sessionId = testSessionId,
            receiptDate = System.currentTimeMillis(),
            totalAmount = 5000.0,
            discountAmount = 0.0,
            netAmount = 5000.0,
            paymentMode = PaymentMode.CASH
        )
        repository.insertReceipt(receipt, listOf(
            ReceiptItem(feeType = "FEE_PAYMENT", description = "Payment", amount = 5000.0)
        ))
        
        // Verify balances are independent
        assertThat(repository.getCurrentBalance(testStudentId)).isEqualTo(7000.0)
        assertThat(repository.getCurrentBalance(student2Id)).isEqualTo(12000.0)
    }

    // ===== Receipt Number Uniqueness Tests =====

    @Test
    fun duplicateReceiptNumber_fails() = runTest {
        val receipt1 = Receipt(
            receiptNumber = 100,
            studentId = testStudentId,
            sessionId = testSessionId,
            receiptDate = System.currentTimeMillis(),
            totalAmount = 1000.0,
            discountAmount = 0.0,
            netAmount = 1000.0,
            paymentMode = PaymentMode.CASH
        )
        repository.insertReceipt(receipt1, listOf(
            ReceiptItem(feeType = "FEE_PAYMENT", description = "Payment", amount = 1000.0)
        ))
        
        val receipt2 = Receipt(
            receiptNumber = 100, // Same number
            studentId = testStudentId,
            sessionId = testSessionId,
            receiptDate = System.currentTimeMillis(),
            totalAmount = 2000.0,
            discountAmount = 0.0,
            netAmount = 2000.0,
            paymentMode = PaymentMode.CASH
        )
        val result = repository.insertReceipt(receipt2, listOf(
            ReceiptItem(feeType = "FEE_PAYMENT", description = "Payment", amount = 2000.0)
        ))
        
        assertThat(result.isFailure).isTrue()
    }

    // ===== Advance Payment Tests =====

    @Test
    fun advancePayment_createsNegativeBalance() = runTest {
        // Add session fees
        repository.addSessionFeesForStudent(testStudentId, testSessionId, addTuition = true, addTransport = false)
        
        // Pay more than dues
        val receipt = Receipt(
            receiptNumber = 1,
            studentId = testStudentId,
            sessionId = testSessionId,
            receiptDate = System.currentTimeMillis(),
            totalAmount = 15000.0,
            discountAmount = 0.0,
            netAmount = 15000.0,
            paymentMode = PaymentMode.CASH
        )
        repository.insertReceipt(receipt, listOf(
            ReceiptItem(feeType = "FEE_PAYMENT", description = "Advance Payment", amount = 15000.0)
        ))
        
        // Balance should be negative (advance)
        assertThat(repository.getCurrentBalance(testStudentId)).isEqualTo(-3000.0)
    }
}
