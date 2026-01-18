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
import java.util.Calendar

/**
 * Instrumented tests for ReceiptDao.
 * Tests receipt operations and collection calculations.
 */
@RunWith(AndroidJUnit4::class)
class ReceiptDaoTest {

    private lateinit var database: FeesDatabase
    private lateinit var receiptDao: ReceiptDao
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
        
        receiptDao = database.receiptDao()
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

    private fun createReceiptEntity(
        receiptNumber: Int = 1,
        studentId: Long = testStudentId,
        sessionId: Long = testSessionId,
        receiptDate: Long = System.currentTimeMillis(),
        totalAmount: Double = 5000.0,
        discountAmount: Double = 0.0,
        netAmount: Double = 5000.0,
        paymentMode: PaymentMode = PaymentMode.CASH,
        isCancelled: Boolean = false
    ) = ReceiptEntity(
        id = 0,
        receiptNumber = receiptNumber,
        studentId = studentId,
        sessionId = sessionId,
        receiptDate = receiptDate,
        totalAmount = totalAmount,
        discountAmount = discountAmount,
        netAmount = netAmount,
        paymentMode = paymentMode,
        isCancelled = isCancelled
    )

    private fun createReceiptItemEntity(
        receiptId: Long,
        feeType: String = "TUITION",
        description: String = "Monthly Fee",
        amount: Double = 1200.0
    ) = ReceiptItemEntity(
        id = 0,
        receiptId = receiptId,
        feeType = feeType,
        description = description,
        monthYear = "04-2025",
        amount = amount
    )

    // ===== Insert Receipt Tests =====

    @Test
    fun insertReceipt_returnsGeneratedId() = runTest {
        val receipt = createReceiptEntity()
        
        val id = receiptDao.insertReceipt(receipt)
        
        assertThat(id).isGreaterThan(0L)
    }

    @Test
    fun insertReceipt_canBeRetrieved() = runTest {
        val receipt = createReceiptEntity(receiptNumber = 100, netAmount = 5000.0)
        val id = receiptDao.insertReceipt(receipt)
        
        val retrieved = receiptDao.getById(id)
        
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.receiptNumber).isEqualTo(100)
        assertThat(retrieved?.netAmount).isEqualTo(5000.0)
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun insertReceipt_duplicateReceiptNumber_throwsException() = runTest {
        val receipt1 = createReceiptEntity(receiptNumber = 100)
        val receipt2 = createReceiptEntity(receiptNumber = 100)
        
        receiptDao.insertReceipt(receipt1)
        receiptDao.insertReceipt(receipt2) // Should throw
    }

    // ===== Insert Receipt Items Tests =====

    @Test
    fun insertReceiptItems_insertsCorrectly() = runTest {
        val receiptId = receiptDao.insertReceipt(createReceiptEntity())
        val items = listOf(
            createReceiptItemEntity(receiptId, "TUITION", "Monthly Fee", 1200.0),
            createReceiptItemEntity(receiptId, "TRANSPORT", "Transport Fee", 800.0)
        )
        
        receiptDao.insertReceiptItems(items)
        
        val retrievedItems = receiptDao.getReceiptItems(receiptId)
        assertThat(retrievedItems).hasSize(2)
    }

    // ===== GetById Tests =====

    @Test
    fun getById_returnsNullWhenNotFound() = runTest {
        val result = receiptDao.getById(999L)
        
        assertThat(result).isNull()
    }

    // ===== GetByReceiptNumber Tests =====

    @Test
    fun getByReceiptNumber_returnsReceipt() = runTest {
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 150))
        
        val result = receiptDao.getByReceiptNumber(150)
        
        assertThat(result).isNotNull()
        assertThat(result?.receiptNumber).isEqualTo(150)
    }

    @Test
    fun getByReceiptNumber_returnsNullWhenNotFound() = runTest {
        val result = receiptDao.getByReceiptNumber(999)
        
        assertThat(result).isNull()
    }

    // ===== GetReceiptsForStudent Tests =====

    @Test
    fun getReceiptsForStudent_returnsStudentReceipts() = runTest {
        // Create another student
        val student2Id = studentDao.insert(
            createTestStudent().copy(srNumber = "SR002", accountNumber = "ACC002")
        )
        
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 1, studentId = testStudentId))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 2, studentId = testStudentId))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 3, studentId = student2Id))
        
        receiptDao.getReceiptsForStudent(testStudentId).test {
            val receipts = awaitItem()
            assertThat(receipts).hasSize(2)
            assertThat(receipts.all { it.studentId == testStudentId }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getReceiptsForStudent_orderedByDateDescending() = runTest {
        val now = System.currentTimeMillis()
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 1, receiptDate = now))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 2, receiptDate = now + 1000))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 3, receiptDate = now - 1000))
        
        receiptDao.getReceiptsForStudent(testStudentId).test {
            val receipts = awaitItem()
            assertThat(receipts.map { it.receiptNumber }).isEqualTo(listOf(2, 1, 3))
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetReceiptsByDateRange Tests =====

    @Test
    fun getReceiptsByDateRange_filtersCorrectly() = runTest {
        val now = System.currentTimeMillis()
        val dayInMs = 24 * 60 * 60 * 1000L
        
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 1, receiptDate = now - 2 * dayInMs))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 2, receiptDate = now))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 3, receiptDate = now + 2 * dayInMs))
        
        receiptDao.getReceiptsByDateRange(now - dayInMs, now + dayInMs).test {
            val receipts = awaitItem()
            assertThat(receipts).hasSize(1)
            assertThat(receipts[0].receiptNumber).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetDailyReceipts Tests =====

    @Test
    fun getDailyReceipts_excludesCancelled() = runTest {
        val now = System.currentTimeMillis()
        val startOfDay = getStartOfDay(now)
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L
        
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 1, receiptDate = now, isCancelled = false))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 2, receiptDate = now, isCancelled = true))
        
        receiptDao.getDailyReceipts(startOfDay, endOfDay).test {
            val receipts = awaitItem()
            assertThat(receipts).hasSize(1)
            assertThat(receipts[0].receiptNumber).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== GetDailyCollectionTotal Tests =====

    @Test
    fun getDailyCollectionTotal_sumsNetAmounts() = runTest {
        val now = System.currentTimeMillis()
        val startOfDay = getStartOfDay(now)
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L
        
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 1, receiptDate = now, netAmount = 5000.0))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 2, receiptDate = now, netAmount = 3000.0))
        
        receiptDao.getDailyCollectionTotal(startOfDay, endOfDay).test {
            val total = awaitItem()
            assertThat(total).isEqualTo(8000.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getDailyCollectionTotal_excludesCancelled() = runTest {
        val now = System.currentTimeMillis()
        val startOfDay = getStartOfDay(now)
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L
        
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 1, receiptDate = now, netAmount = 5000.0, isCancelled = false))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 2, receiptDate = now, netAmount = 3000.0, isCancelled = true))
        
        receiptDao.getDailyCollectionTotal(startOfDay, endOfDay).test {
            val total = awaitItem()
            assertThat(total).isEqualTo(5000.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getDailyCollectionTotal_returnsNullWhenNoReceipts() = runTest {
        val now = System.currentTimeMillis()
        val startOfDay = getStartOfDay(now)
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L
        
        receiptDao.getDailyCollectionTotal(startOfDay, endOfDay).test {
            val total = awaitItem()
            assertThat(total).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== ReceiptNumberExists Tests =====

    @Test
    fun receiptNumberExists_returnsTrueWhenExists() = runTest {
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 100))
        
        val result = receiptDao.receiptNumberExists(100)
        
        assertThat(result).isTrue()
    }

    @Test
    fun receiptNumberExists_returnsFalseWhenNotExists() = runTest {
        val result = receiptDao.receiptNumberExists(999)
        
        assertThat(result).isFalse()
    }

    // ===== GetMaxReceiptNumber Tests =====

    @Test
    fun getMaxReceiptNumber_returnsHighestNumber() = runTest {
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 50))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 150))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 100))
        
        val max = receiptDao.getMaxReceiptNumber()
        
        assertThat(max).isEqualTo(150)
    }

    @Test
    fun getMaxReceiptNumber_returnsNullWhenNoReceipts() = runTest {
        val max = receiptDao.getMaxReceiptNumber()
        
        assertThat(max).isNull()
    }

    // ===== GetRecentReceipts Tests =====

    @Test
    fun getRecentReceipts_limitsResults() = runTest {
        for (i in 1..10) {
            receiptDao.insertReceipt(createReceiptEntity(receiptNumber = i))
        }
        
        receiptDao.getRecentReceipts(5).test {
            val receipts = awaitItem()
            assertThat(receipts).hasSize(5)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRecentReceipts_orderedByDateDescending() = runTest {
        val now = System.currentTimeMillis()
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 1, receiptDate = now - 2000))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 2, receiptDate = now))
        receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 3, receiptDate = now - 1000))
        
        receiptDao.getRecentReceipts(10).test {
            val receipts = awaitItem()
            assertThat(receipts.map { it.receiptNumber }).isEqualTo(listOf(2, 3, 1))
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== Update Receipt Tests =====

    @Test
    fun updateReceipt_updatesData() = runTest {
        val id = receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 100))
        val receipt = receiptDao.getById(id)!!
        
        receiptDao.updateReceipt(receipt.copy(remarks = "Updated remarks"))
        
        val updated = receiptDao.getById(id)
        assertThat(updated?.remarks).isEqualTo("Updated remarks")
    }

    // ===== Cancellation Tests =====

    @Test
    fun cancelReceipt_setsFlags() = runTest {
        val id = receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 100))
        val receipt = receiptDao.getById(id)!!
        
        val cancelledAt = System.currentTimeMillis()
        receiptDao.updateReceipt(receipt.copy(
            isCancelled = true,
            cancelledAt = cancelledAt,
            cancellationReason = "Cheque bounced"
        ))
        
        val updated = receiptDao.getById(id)
        assertThat(updated?.isCancelled).isTrue()
        assertThat(updated?.cancellationReason).isEqualTo("Cheque bounced")
    }

    // ===== Receipt Items Cascade Delete Tests =====

    @Test
    fun deleteReceipt_cascadesDeleteToItems() = runTest {
        val receiptId = receiptDao.insertReceipt(createReceiptEntity(receiptNumber = 100))
        receiptDao.insertReceiptItems(listOf(
            createReceiptItemEntity(receiptId, amount = 1000.0),
            createReceiptItemEntity(receiptId, amount = 500.0)
        ))
        
        // Verify items exist
        assertThat(receiptDao.getReceiptItems(receiptId)).hasSize(2)
        
        // Delete receipt
        val receipt = receiptDao.getById(receiptId)!!
        receiptDao.deleteReceipt(receipt)
        
        // Items should be deleted due to cascade
        assertThat(receiptDao.getReceiptItems(receiptId)).isEmpty()
    }

    // ===== Payment Mode Tests =====

    @Test
    fun receiptWithChequePayment_storesChequeDetails() = runTest {
        val receipt = createReceiptEntity(
            receiptNumber = 100,
            paymentMode = PaymentMode.CHEQUE
        ).copy(
            chequeNumber = "CHQ12345",
            chequeDate = System.currentTimeMillis()
        )
        val id = receiptDao.insertReceipt(receipt)
        
        val retrieved = receiptDao.getById(id)
        assertThat(retrieved?.paymentMode).isEqualTo(PaymentMode.CHEQUE)
        assertThat(retrieved?.chequeNumber).isEqualTo("CHQ12345")
    }

    @Test
    fun receiptWithUpiPayment_storesUpiReference() = runTest {
        val receipt = createReceiptEntity(
            receiptNumber = 100,
            paymentMode = PaymentMode.UPI
        ).copy(
            upiReference = "UPI123456789"
        )
        val id = receiptDao.insertReceipt(receipt)
        
        val retrieved = receiptDao.getById(id)
        assertThat(retrieved?.paymentMode).isEqualTo(PaymentMode.UPI)
        assertThat(retrieved?.upiReference).isEqualTo("UPI123456789")
    }

    // ===== Helper Functions =====

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
