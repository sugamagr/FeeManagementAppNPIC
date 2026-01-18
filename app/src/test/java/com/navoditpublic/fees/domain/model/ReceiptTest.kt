package com.navoditpublic.fees.domain.model

import com.google.common.truth.Truth.assertThat
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.data.local.entity.ReceiptEntity
import com.navoditpublic.fees.data.local.entity.ReceiptItemEntity
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for Receipt domain model.
 */
class ReceiptTest {

    private fun createReceipt(
        id: Long = 1L,
        receiptNumber: Int = 1,
        studentId: Long = 1L,
        sessionId: Long = 1L,
        receiptDate: Long = System.currentTimeMillis(),
        totalAmount: Double = 12000.0,
        discountAmount: Double = 1000.0,
        netAmount: Double = 11000.0,
        paymentMode: PaymentMode = PaymentMode.CASH,
        chequeNumber: String? = null,
        chequeDate: Long? = null,
        upiReference: String? = null,
        onlineReference: String? = null,
        remarks: String? = "",
        isCancelled: Boolean = false
    ) = Receipt(
        id = id,
        receiptNumber = receiptNumber,
        studentId = studentId,
        sessionId = sessionId,
        receiptDate = receiptDate,
        totalAmount = totalAmount,
        discountAmount = discountAmount,
        netAmount = netAmount,
        paymentMode = paymentMode,
        chequeNumber = chequeNumber,
        chequeDate = chequeDate,
        upiReference = upiReference,
        onlineReference = onlineReference,
        remarks = remarks,
        isCancelled = isCancelled
    )

    // ===== Receipt Date Formatted Tests =====

    @Test
    fun `receiptDateFormatted returns correct format`() {
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 15)
        }
        val receipt = createReceipt(receiptDate = calendar.timeInMillis)
        
        assertThat(receipt.receiptDateFormatted).isEqualTo("15-01-2025")
    }

    // ===== Cheque Date Formatted Tests =====

    @Test
    fun `chequeDateFormatted returns null when chequeDate is null`() {
        val receipt = createReceipt(chequeDate = null)
        assertThat(receipt.chequeDateFormatted).isNull()
    }

    @Test
    fun `chequeDateFormatted returns formatted date when chequeDate is present`() {
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 20)
        }
        val receipt = createReceipt(chequeDate = calendar.timeInMillis)
        
        assertThat(receipt.chequeDateFormatted).isEqualTo("20-01-2025")
    }

    // ===== Payment Reference Tests =====

    @Test
    fun `paymentReference returns null for CASH payment`() {
        val receipt = createReceipt(paymentMode = PaymentMode.CASH)
        assertThat(receipt.paymentReference).isNull()
    }

    @Test
    fun `paymentReference returns chequeNumber for CHEQUE payment`() {
        val receipt = createReceipt(
            paymentMode = PaymentMode.CHEQUE,
            chequeNumber = "CHQ12345"
        )
        assertThat(receipt.paymentReference).isEqualTo("CHQ12345")
    }

    @Test
    fun `paymentReference returns upiReference for UPI payment`() {
        val receipt = createReceipt(
            paymentMode = PaymentMode.UPI,
            upiReference = "UPI123456789"
        )
        assertThat(receipt.paymentReference).isEqualTo("UPI123456789")
    }

    @Test
    fun `paymentReference returns onlineReference for ONLINE payment`() {
        val receipt = createReceipt(
            paymentMode = PaymentMode.ONLINE,
            onlineReference = "NEFT123456"
        )
        assertThat(receipt.paymentReference).isEqualTo("NEFT123456")
    }

    @Test
    fun `referenceNumber is same as paymentReference`() {
        val receipt = createReceipt(
            paymentMode = PaymentMode.UPI,
            upiReference = "UPI123"
        )
        assertThat(receipt.referenceNumber).isEqualTo(receipt.paymentReference)
    }

    // ===== Entity Mapping Tests =====

    @Test
    fun `toEntity converts Receipt to ReceiptEntity correctly`() {
        val receipt = createReceipt(
            id = 1L,
            receiptNumber = 100,
            studentId = 5L,
            sessionId = 2L,
            totalAmount = 12000.0,
            discountAmount = 1000.0,
            netAmount = 11000.0,
            paymentMode = PaymentMode.CHEQUE,
            chequeNumber = "CHQ123",
            remarks = "Full year payment"
        )
        
        val entity = receipt.toEntity()
        
        assertThat(entity.id).isEqualTo(1L)
        assertThat(entity.receiptNumber).isEqualTo(100)
        assertThat(entity.studentId).isEqualTo(5L)
        assertThat(entity.sessionId).isEqualTo(2L)
        assertThat(entity.totalAmount).isEqualTo(12000.0)
        assertThat(entity.discountAmount).isEqualTo(1000.0)
        assertThat(entity.netAmount).isEqualTo(11000.0)
        assertThat(entity.paymentMode).isEqualTo(PaymentMode.CHEQUE)
        assertThat(entity.chequeNumber).isEqualTo("CHQ123")
        assertThat(entity.remarks).isEqualTo("Full year payment")
    }

    @Test
    fun `fromEntity converts ReceiptEntity to Receipt correctly`() {
        val timestamp = System.currentTimeMillis()
        val entity = ReceiptEntity(
            id = 1L,
            receiptNumber = 100,
            studentId = 5L,
            sessionId = 2L,
            receiptDate = timestamp,
            totalAmount = 12000.0,
            discountAmount = 1000.0,
            netAmount = 11000.0,
            paymentMode = PaymentMode.UPI,
            upiReference = "UPI123",
            remarks = "Test",
            isCancelled = false,
            createdAt = timestamp,
            updatedAt = timestamp
        )
        
        val receipt = Receipt.fromEntity(entity)
        
        assertThat(receipt.id).isEqualTo(1L)
        assertThat(receipt.receiptNumber).isEqualTo(100)
        assertThat(receipt.paymentMode).isEqualTo(PaymentMode.UPI)
        assertThat(receipt.upiReference).isEqualTo("UPI123")
    }

    @Test
    fun `fromEntity with items populates items list`() {
        val entity = ReceiptEntity(
            id = 1L,
            receiptNumber = 100,
            studentId = 5L,
            sessionId = 2L,
            receiptDate = System.currentTimeMillis(),
            totalAmount = 12000.0,
            discountAmount = 0.0,
            netAmount = 12000.0,
            paymentMode = PaymentMode.CASH,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val items = listOf(
            ReceiptItem(id = 1, receiptId = 1, feeType = "TUITION", description = "Monthly Fee", amount = 1200.0),
            ReceiptItem(id = 2, receiptId = 1, feeType = "TRANSPORT", description = "Transport Fee", amount = 800.0)
        )
        
        val receipt = Receipt.fromEntity(entity, items)
        
        assertThat(receipt.items).hasSize(2)
        assertThat(receipt.items[0].feeType).isEqualTo("TUITION")
        assertThat(receipt.items[1].feeType).isEqualTo("TRANSPORT")
    }

    @Test
    fun `fromEntity with student details populates student fields`() {
        val entity = ReceiptEntity(
            id = 1L,
            receiptNumber = 100,
            studentId = 5L,
            sessionId = 2L,
            receiptDate = System.currentTimeMillis(),
            totalAmount = 1000.0,
            discountAmount = 0.0,
            netAmount = 1000.0,
            paymentMode = PaymentMode.CASH,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val receipt = Receipt.fromEntity(
            entity,
            studentName = "Test Student",
            studentClass = "5th",
            studentSection = "A"
        )
        
        assertThat(receipt.studentName).isEqualTo("Test Student")
        assertThat(receipt.studentClass).isEqualTo("5th")
        assertThat(receipt.studentSection).isEqualTo("A")
    }

    // ===== PaymentMode Enum Tests =====

    @Test
    fun `PaymentMode enum has all expected values`() {
        val modes = PaymentMode.values()
        
        assertThat(modes).hasLength(4)
        assertThat(modes).asList().containsExactly(
            PaymentMode.CASH,
            PaymentMode.CHEQUE,
            PaymentMode.UPI,
            PaymentMode.ONLINE
        )
    }
}

/**
 * Unit tests for ReceiptItem domain model.
 */
class ReceiptItemTest {

    @Test
    fun `months property returns monthYear when present`() {
        val item = ReceiptItem(
            feeType = "TUITION",
            description = "Monthly Fee",
            monthYear = "Apr 2025",
            amount = 1200.0
        )
        
        assertThat(item.months).isEqualTo("Apr 2025")
    }

    @Test
    fun `months property returns empty string when monthYear is null`() {
        val item = ReceiptItem(
            feeType = "ADMISSION",
            description = "Admission Fee",
            monthYear = null,
            amount = 5000.0
        )
        
        assertThat(item.months).isEmpty()
    }

    @Test
    fun `toEntity converts ReceiptItem to ReceiptItemEntity correctly`() {
        val item = ReceiptItem(
            id = 1L,
            receiptId = 5L,
            feeType = "TUITION",
            description = "Monthly Fee - April",
            monthYear = "04-2025",
            amount = 1200.0
        )
        
        val entity = item.toEntity()
        
        assertThat(entity.id).isEqualTo(1L)
        assertThat(entity.receiptId).isEqualTo(5L)
        assertThat(entity.feeType).isEqualTo("TUITION")
        assertThat(entity.description).isEqualTo("Monthly Fee - April")
        assertThat(entity.monthYear).isEqualTo("04-2025")
        assertThat(entity.amount).isEqualTo(1200.0)
    }

    @Test
    fun `fromEntity converts ReceiptItemEntity to ReceiptItem correctly`() {
        val entity = ReceiptItemEntity(
            id = 1L,
            receiptId = 5L,
            feeType = "TRANSPORT",
            description = "Transport Fee",
            monthYear = "04-2025",
            amount = 800.0,
            createdAt = System.currentTimeMillis()
        )
        
        val item = ReceiptItem.fromEntity(entity)
        
        assertThat(item.id).isEqualTo(1L)
        assertThat(item.receiptId).isEqualTo(5L)
        assertThat(item.feeType).isEqualTo("TRANSPORT")
        assertThat(item.description).isEqualTo("Transport Fee")
        assertThat(item.monthYear).isEqualTo("04-2025")
        assertThat(item.amount).isEqualTo(800.0)
    }
}

/**
 * Unit tests for ReceiptItemDisplay.
 */
class ReceiptItemDisplayTest {

    @Test
    fun `fromReceiptItem creates display correctly`() {
        val item = ReceiptItem(
            feeType = "TUITION",
            description = "Monthly Fee",
            monthYear = "Apr 2025",
            amount = 1200.0
        )
        
        val display = ReceiptItemDisplay.fromReceiptItem(item)
        
        assertThat(display.description).isEqualTo("Monthly Fee")
        assertThat(display.months).isEqualTo("Apr 2025")
        assertThat(display.amount).isEqualTo(1200.0)
    }

    @Test
    fun `fromReceiptItem handles null monthYear`() {
        val item = ReceiptItem(
            feeType = "ADMISSION",
            description = "Admission Fee",
            monthYear = null,
            amount = 5000.0
        )
        
        val display = ReceiptItemDisplay.fromReceiptItem(item)
        
        assertThat(display.months).isEmpty()
    }
}
