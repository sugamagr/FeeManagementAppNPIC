package com.navoditpublic.fees.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.navoditpublic.fees.data.local.FeesDatabase
import com.navoditpublic.fees.data.local.entity.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for LedgerDao.
 * Tests ledger entry operations and balance calculations.
 */
@RunWith(AndroidJUnit4::class)
class LedgerDaoTest {

    private lateinit var database: FeesDatabase
    private lateinit var ledgerDao: LedgerDao
    private lateinit var studentDao: StudentDao
    private lateinit var academicSessionDao: AcademicSessionDao

    private var testStudentId: Long = 0
    private var testSessionId: Long = 0

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            FeesDatabase::class.java
        ).allowMainThreadQueries().build()
        
        ledgerDao = database.ledgerDao()
        studentDao = database.studentDao()
        academicSessionDao = database.academicSessionDao()
        
        // Insert required entities for foreign keys
        runTest {
            testSessionId = academicSessionDao.insert(createTestSession())
            testStudentId = studentDao.insert(createTestStudent())
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

    private fun createLedgerEntry(
        studentId: Long = testStudentId,
        sessionId: Long = testSessionId,
        entryDate: Long = System.currentTimeMillis(),
        particulars: String = "Test Entry",
        entryType: LedgerEntryType = LedgerEntryType.DEBIT,
        debitAmount: Double = 0.0,
        creditAmount: Double = 0.0,
        balance: Double = 0.0,
        referenceType: LedgerReferenceType = LedgerReferenceType.FEE_CHARGE,
        referenceId: Long? = null,
        isReversed: Boolean = false
    ) = LedgerEntryEntity(
        id = 0,
        studentId = studentId,
        sessionId = sessionId,
        entryDate = entryDate,
        particulars = particulars,
        entryType = entryType,
        debitAmount = debitAmount,
        creditAmount = creditAmount,
        balance = balance,
        referenceType = referenceType,
        referenceId = referenceId,
        isReversed = isReversed
    )

    // ===== Insert Tests =====

    @Test
    fun insertEntry_returnsGeneratedId() = runTest {
        val entry = createLedgerEntry(debitAmount = 1000.0, balance = 1000.0)
        
        val id = ledgerDao.insert(entry)
        
        assertThat(id).isGreaterThan(0L)
    }

    @Test
    fun insertEntry_canBeRetrieved() = runTest {
        val entry = createLedgerEntry(particulars = "Monthly Fee", debitAmount = 1200.0, balance = 1200.0)
        val id = ledgerDao.insert(entry)
        
        val retrieved = ledgerDao.getById(id)
        
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.particulars).isEqualTo("Monthly Fee")
        assertThat(retrieved?.debitAmount).isEqualTo(1200.0)
    }

    // ===== InsertAll Tests =====

    @Test
    fun insertAll_insertsMultipleEntries() = runTest {
        val entries = listOf(
            createLedgerEntry(particulars = "Entry 1", debitAmount = 1000.0, balance = 1000.0, entryDate = 1000L),
            createLedgerEntry(particulars = "Entry 2", debitAmount = 500.0, balance = 1500.0, entryDate = 2000L)
        )
        
        ledgerDao.insertAll(entries)
        
        ledgerDao.getLedgerForStudent(testStudentId).test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetLedgerForStudent Tests =====

    @Test
    fun getLedgerForStudent_orderedByDateAscending() = runTest {
        val now = System.currentTimeMillis()
        ledgerDao.insert(createLedgerEntry(particulars = "Second", entryDate = now + 1000, balance = 2000.0))
        ledgerDao.insert(createLedgerEntry(particulars = "First", entryDate = now, balance = 1000.0))
        ledgerDao.insert(createLedgerEntry(particulars = "Third", entryDate = now + 2000, balance = 3000.0))
        
        ledgerDao.getLedgerForStudent(testStudentId).test {
            val entries = awaitItem()
            assertThat(entries.map { it.particulars }).isEqualTo(listOf("First", "Second", "Third"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getLedgerForStudent_excludesReversedEntries() = runTest {
        ledgerDao.insert(createLedgerEntry(particulars = "Normal", isReversed = false, balance = 1000.0))
        ledgerDao.insert(createLedgerEntry(particulars = "Reversed", isReversed = true, balance = 0.0))
        
        ledgerDao.getLedgerForStudent(testStudentId).test {
            val entries = awaitItem()
            assertThat(entries).hasSize(1)
            assertThat(entries[0].particulars).isEqualTo("Normal")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetLedgerForStudentBySession Tests =====

    @Test
    fun getLedgerForStudentBySession_filtersCorrectly() = runTest {
        // Create another session
        val session2Id = academicSessionDao.insert(
            AcademicSessionEntity(
                id = 0,
                sessionName = "2024-25",
                startDate = System.currentTimeMillis() - 365 * 24 * 60 * 60 * 1000L,
                endDate = System.currentTimeMillis(),
                isCurrent = false,
                isActive = true
            )
        )
        
        ledgerDao.insert(createLedgerEntry(sessionId = testSessionId, particulars = "Current Session", balance = 1000.0))
        ledgerDao.insert(createLedgerEntry(sessionId = session2Id, particulars = "Previous Session", balance = 500.0))
        
        ledgerDao.getLedgerForStudentBySession(testStudentId, testSessionId).test {
            val entries = awaitItem()
            assertThat(entries).hasSize(1)
            assertThat(entries[0].particulars).isEqualTo("Current Session")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetCurrentBalance Tests =====

    @Test
    fun getCurrentBalance_returnsLatestBalance() = runTest {
        val now = System.currentTimeMillis()
        ledgerDao.insert(createLedgerEntry(entryDate = now, balance = 1000.0))
        ledgerDao.insert(createLedgerEntry(entryDate = now + 1000, balance = 1500.0))
        ledgerDao.insert(createLedgerEntry(entryDate = now + 2000, balance = 500.0))
        
        val balance = ledgerDao.getCurrentBalance(testStudentId)
        
        assertThat(balance).isEqualTo(500.0)
    }

    @Test
    fun getCurrentBalance_returnsZeroWhenNoEntries() = runTest {
        val balance = ledgerDao.getCurrentBalance(testStudentId)
        
        assertThat(balance).isEqualTo(0.0)
    }

    @Test
    fun getCurrentBalance_excludesReversedEntries() = runTest {
        val now = System.currentTimeMillis()
        ledgerDao.insert(createLedgerEntry(entryDate = now, balance = 1000.0, isReversed = false))
        ledgerDao.insert(createLedgerEntry(entryDate = now + 1000, balance = 0.0, isReversed = true))
        
        val balance = ledgerDao.getCurrentBalance(testStudentId)
        
        assertThat(balance).isEqualTo(1000.0)
    }

    // ===== GetCurrentBalanceFlow Tests =====

    @Test
    fun getCurrentBalanceFlow_emitsUpdates() = runTest {
        ledgerDao.getCurrentBalanceFlow(testStudentId).test {
            assertThat(awaitItem()).isEqualTo(0.0)
            
            ledgerDao.insert(createLedgerEntry(balance = 1000.0))
            assertThat(awaitItem()).isEqualTo(1000.0)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetTotalDebits Tests =====

    @Test
    fun getTotalDebits_sumsDebitAmounts() = runTest {
        ledgerDao.insert(createLedgerEntry(
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 1000.0,
            balance = 1000.0
        ))
        ledgerDao.insert(createLedgerEntry(
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 500.0,
            balance = 1500.0
        ))
        
        val total = ledgerDao.getTotalDebits(testStudentId)
        
        assertThat(total).isEqualTo(1500.0)
    }

    @Test
    fun getTotalDebits_excludesReversed() = runTest {
        ledgerDao.insert(createLedgerEntry(
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 1000.0,
            balance = 1000.0,
            isReversed = false
        ))
        ledgerDao.insert(createLedgerEntry(
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 500.0,
            balance = 0.0,
            isReversed = true
        ))
        
        val total = ledgerDao.getTotalDebits(testStudentId)
        
        assertThat(total).isEqualTo(1000.0)
    }

    // ===== GetTotalCredits Tests =====

    @Test
    fun getTotalCredits_sumsCreditAmounts() = runTest {
        ledgerDao.insert(createLedgerEntry(
            entryType = LedgerEntryType.CREDIT,
            creditAmount = 500.0,
            balance = -500.0
        ))
        ledgerDao.insert(createLedgerEntry(
            entryType = LedgerEntryType.CREDIT,
            creditAmount = 300.0,
            balance = -800.0
        ))
        
        val total = ledgerDao.getTotalCredits(testStudentId)
        
        assertThat(total).isEqualTo(800.0)
    }

    // ===== GetEntriesByReceipt Tests =====

    @Test
    fun getEntriesByReceipt_returnsMatchingEntries() = runTest {
        ledgerDao.insert(createLedgerEntry(
            referenceType = LedgerReferenceType.RECEIPT,
            referenceId = 100L,
            balance = 500.0
        ))
        ledgerDao.insert(createLedgerEntry(
            referenceType = LedgerReferenceType.RECEIPT,
            referenceId = 200L,
            balance = 300.0
        ))
        
        val entries = ledgerDao.getEntriesByReceipt(100L)
        
        assertThat(entries).hasSize(1)
        assertThat(entries[0].referenceId).isEqualTo(100L)
    }

    // ===== ReverseEntriesForReceipt Tests =====

    @Test
    fun reverseEntriesForReceipt_marksAsReversed() = runTest {
        val entryId = ledgerDao.insert(createLedgerEntry(
            referenceType = LedgerReferenceType.RECEIPT,
            referenceId = 100L,
            isReversed = false,
            balance = 500.0
        ))
        
        ledgerDao.reverseEntriesForReceipt(100L)
        
        val entry = ledgerDao.getById(entryId)
        assertThat(entry?.isReversed).isTrue()
    }

    // ===== GetTotalPendingDues Tests =====

    @Test
    fun getTotalPendingDues_calculatesCorrectly() = runTest {
        // Add debit entries
        ledgerDao.insert(createLedgerEntry(
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 12000.0,
            balance = 12000.0
        ))
        // Add credit entry
        ledgerDao.insert(createLedgerEntry(
            entryType = LedgerEntryType.CREDIT,
            creditAmount = 5000.0,
            balance = 7000.0
        ))
        
        ledgerDao.getTotalPendingDues().test {
            val dues = awaitItem()
            assertThat(dues).isEqualTo(7000.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetStudentIdsWithDues Tests =====

    @Test
    fun getStudentIdsWithDues_returnsStudentsWithPositiveBalance() = runTest {
        // Create another student
        val student2Id = studentDao.insert(
            createTestStudent().copy(srNumber = "SR002", accountNumber = "ACC002")
        )
        
        // Student 1 has dues
        ledgerDao.insert(createLedgerEntry(
            studentId = testStudentId,
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 1000.0,
            balance = 1000.0
        ))
        
        // Student 2 has no dues (paid in full)
        ledgerDao.insert(createLedgerEntry(
            studentId = student2Id,
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 1000.0,
            balance = 1000.0
        ))
        ledgerDao.insert(createLedgerEntry(
            studentId = student2Id,
            entryType = LedgerEntryType.CREDIT,
            creditAmount = 1000.0,
            balance = 0.0
        ))
        
        ledgerDao.getStudentIdsWithDues().test {
            val ids = awaitItem()
            assertThat(ids).contains(testStudentId)
            assertThat(ids).doesNotContain(student2Id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== HasFeeChargeEntries Tests =====

    @Test
    fun hasFeeChargeEntries_returnsTrueWhenExists() = runTest {
        ledgerDao.insert(createLedgerEntry(
            referenceType = LedgerReferenceType.FEE_CHARGE,
            balance = 12000.0
        ))
        
        val result = ledgerDao.hasFeeChargeEntries(testStudentId, testSessionId)
        
        assertThat(result).isTrue()
    }

    @Test
    fun hasFeeChargeEntries_returnsFalseWhenNotExists() = runTest {
        val result = ledgerDao.hasFeeChargeEntries(testStudentId, testSessionId)
        
        assertThat(result).isFalse()
    }

    @Test
    fun hasFeeChargeEntries_excludesReversed() = runTest {
        ledgerDao.insert(createLedgerEntry(
            referenceType = LedgerReferenceType.FEE_CHARGE,
            isReversed = true,
            balance = 0.0
        ))
        
        val result = ledgerDao.hasFeeChargeEntries(testStudentId, testSessionId)
        
        assertThat(result).isFalse()
    }

    // ===== GetAllEntriesForStudentChronological Tests =====

    @Test
    fun getAllEntriesForStudentChronological_orderedCorrectly() = runTest {
        val now = System.currentTimeMillis()
        ledgerDao.insert(createLedgerEntry(entryDate = now + 2000, particulars = "Third", balance = 3000.0))
        ledgerDao.insert(createLedgerEntry(entryDate = now, particulars = "First", balance = 1000.0))
        ledgerDao.insert(createLedgerEntry(entryDate = now + 1000, particulars = "Second", balance = 2000.0))
        
        val entries = ledgerDao.getAllEntriesForStudentChronological(testStudentId)
        
        assertThat(entries.map { it.particulars }).isEqualTo(listOf("First", "Second", "Third"))
    }

    // ===== UpdateBalance Tests =====

    @Test
    fun updateBalance_updatesCorrectly() = runTest {
        val entryId = ledgerDao.insert(createLedgerEntry(balance = 1000.0))
        
        ledgerDao.updateBalance(entryId, 1500.0)
        
        val entry = ledgerDao.getById(entryId)
        assertThat(entry?.balance).isEqualTo(1500.0)
    }

    // ===== DeleteByStudentId Tests =====

    @Test
    fun deleteByStudentId_removesAllEntries() = runTest {
        ledgerDao.insert(createLedgerEntry(balance = 1000.0))
        ledgerDao.insert(createLedgerEntry(balance = 2000.0))
        
        ledgerDao.deleteByStudentId(testStudentId)
        
        ledgerDao.getLedgerForStudent(testStudentId).test {
            val entries = awaitItem()
            assertThat(entries).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== Balance Calculation Scenarios =====

    @Test
    fun balanceScenario_feeChargeAndPayment() = runTest {
        val now = System.currentTimeMillis()
        
        // Fee charge (DEBIT)
        ledgerDao.insert(createLedgerEntry(
            entryDate = now,
            particulars = "Tuition Fee",
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 12000.0,
            balance = 12000.0,
            referenceType = LedgerReferenceType.FEE_CHARGE
        ))
        
        // Payment (CREDIT)
        ledgerDao.insert(createLedgerEntry(
            entryDate = now + 1000,
            particulars = "Receipt #1",
            entryType = LedgerEntryType.CREDIT,
            creditAmount = 5000.0,
            balance = 7000.0,
            referenceType = LedgerReferenceType.RECEIPT,
            referenceId = 1L
        ))
        
        val balance = ledgerDao.getCurrentBalance(testStudentId)
        assertThat(balance).isEqualTo(7000.0)
        
        val totalDebits = ledgerDao.getTotalDebits(testStudentId)
        assertThat(totalDebits).isEqualTo(12000.0)
        
        val totalCredits = ledgerDao.getTotalCredits(testStudentId)
        assertThat(totalCredits).isEqualTo(5000.0)
    }

    @Test
    fun balanceScenario_fullPaymentWithDiscount() = runTest {
        val now = System.currentTimeMillis()
        
        // Fee charge
        ledgerDao.insert(createLedgerEntry(
            entryDate = now,
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 12000.0,
            balance = 12000.0,
            referenceType = LedgerReferenceType.FEE_CHARGE
        ))
        
        // Payment
        ledgerDao.insert(createLedgerEntry(
            entryDate = now + 1000,
            entryType = LedgerEntryType.CREDIT,
            creditAmount = 11000.0,
            balance = 1000.0,
            referenceType = LedgerReferenceType.RECEIPT
        ))
        
        // Discount
        ledgerDao.insert(createLedgerEntry(
            entryDate = now + 1001,
            entryType = LedgerEntryType.CREDIT,
            creditAmount = 1000.0,
            balance = 0.0,
            referenceType = LedgerReferenceType.DISCOUNT
        ))
        
        val balance = ledgerDao.getCurrentBalance(testStudentId)
        assertThat(balance).isEqualTo(0.0)
    }

    @Test
    fun balanceScenario_advancePayment() = runTest {
        val now = System.currentTimeMillis()
        
        // Fee charge
        ledgerDao.insert(createLedgerEntry(
            entryDate = now,
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 12000.0,
            balance = 12000.0,
            referenceType = LedgerReferenceType.FEE_CHARGE
        ))
        
        // Overpayment
        ledgerDao.insert(createLedgerEntry(
            entryDate = now + 1000,
            entryType = LedgerEntryType.CREDIT,
            creditAmount = 15000.0,
            balance = -3000.0,
            referenceType = LedgerReferenceType.RECEIPT
        ))
        
        val balance = ledgerDao.getCurrentBalance(testStudentId)
        assertThat(balance).isEqualTo(-3000.0) // Negative = advance
    }
}
