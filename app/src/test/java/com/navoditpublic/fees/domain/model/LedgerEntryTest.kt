package com.navoditpublic.fees.domain.model

import com.google.common.truth.Truth.assertThat
import com.navoditpublic.fees.data.local.entity.LedgerEntryEntity
import com.navoditpublic.fees.data.local.entity.LedgerEntryType
import com.navoditpublic.fees.data.local.entity.LedgerReferenceType
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for LedgerEntry domain model.
 */
class LedgerEntryTest {

    private fun createLedgerEntry(
        id: Long = 1L,
        studentId: Long = 1L,
        sessionId: Long = 1L,
        entryDate: Long = System.currentTimeMillis(),
        particulars: String = "Test Entry",
        entryType: LedgerEntryType = LedgerEntryType.DEBIT,
        debitAmount: Double = 0.0,
        creditAmount: Double = 0.0,
        balance: Double = 0.0,
        referenceType: LedgerReferenceType = LedgerReferenceType.FEE_CHARGE,
        referenceId: Long? = null,
        isReversed: Boolean = false
    ) = LedgerEntry(
        id = id,
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

    // ===== Entry Date Formatted Tests =====

    @Test
    fun `entryDateFormatted returns correct format dd-MM-yy`() {
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 15)
        }
        val entry = createLedgerEntry(entryDate = calendar.timeInMillis)
        
        assertThat(entry.entryDateFormatted).isEqualTo("15-01-25")
    }

    // ===== Entry Type Tests =====

    @Test
    fun `isDebit returns true for DEBIT entry type`() {
        val entry = createLedgerEntry(entryType = LedgerEntryType.DEBIT)
        
        assertThat(entry.isDebit).isTrue()
        assertThat(entry.isCredit).isFalse()
    }

    @Test
    fun `isCredit returns true for CREDIT entry type`() {
        val entry = createLedgerEntry(entryType = LedgerEntryType.CREDIT)
        
        assertThat(entry.isDebit).isFalse()
        assertThat(entry.isCredit).isTrue()
    }

    // ===== Balance Status Tests =====

    @Test
    fun `balanceStatus returns Due when balance is positive`() {
        val entry = createLedgerEntry(balance = 1000.0)
        assertThat(entry.balanceStatus).isEqualTo("Due")
    }

    @Test
    fun `balanceStatus returns Advance when balance is negative`() {
        val entry = createLedgerEntry(balance = -500.0)
        assertThat(entry.balanceStatus).isEqualTo("Advance")
    }

    @Test
    fun `balanceStatus returns Cleared when balance is zero`() {
        val entry = createLedgerEntry(balance = 0.0)
        assertThat(entry.balanceStatus).isEqualTo("Cleared")
    }

    @Test
    fun `balanceStatus handles small values correctly`() {
        assertThat(createLedgerEntry(balance = 0.01).balanceStatus).isEqualTo("Due")
        assertThat(createLedgerEntry(balance = -0.01).balanceStatus).isEqualTo("Advance")
    }

    // ===== Entity Mapping Tests =====

    @Test
    fun `toEntity converts LedgerEntry to LedgerEntryEntity correctly`() {
        val timestamp = System.currentTimeMillis()
        val entry = createLedgerEntry(
            id = 1L,
            studentId = 5L,
            sessionId = 2L,
            entryDate = timestamp,
            particulars = "Monthly Fee - April 2025",
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 1200.0,
            creditAmount = 0.0,
            balance = 1200.0,
            referenceType = LedgerReferenceType.FEE_CHARGE,
            referenceId = null,
            isReversed = false
        )
        
        val entity = entry.toEntity()
        
        assertThat(entity.id).isEqualTo(1L)
        assertThat(entity.studentId).isEqualTo(5L)
        assertThat(entity.sessionId).isEqualTo(2L)
        assertThat(entity.particulars).isEqualTo("Monthly Fee - April 2025")
        assertThat(entity.entryType).isEqualTo(LedgerEntryType.DEBIT)
        assertThat(entity.debitAmount).isEqualTo(1200.0)
        assertThat(entity.creditAmount).isEqualTo(0.0)
        assertThat(entity.balance).isEqualTo(1200.0)
        assertThat(entity.referenceType).isEqualTo(LedgerReferenceType.FEE_CHARGE)
        assertThat(entity.isReversed).isFalse()
    }

    @Test
    fun `toEntity handles CREDIT entry correctly`() {
        val entry = createLedgerEntry(
            entryType = LedgerEntryType.CREDIT,
            debitAmount = 0.0,
            creditAmount = 1000.0,
            balance = 200.0,
            referenceType = LedgerReferenceType.RECEIPT,
            referenceId = 10L
        )
        
        val entity = entry.toEntity()
        
        assertThat(entity.entryType).isEqualTo(LedgerEntryType.CREDIT)
        assertThat(entity.debitAmount).isEqualTo(0.0)
        assertThat(entity.creditAmount).isEqualTo(1000.0)
        assertThat(entity.referenceType).isEqualTo(LedgerReferenceType.RECEIPT)
        assertThat(entity.referenceId).isEqualTo(10L)
    }

    @Test
    fun `fromEntity converts LedgerEntryEntity to LedgerEntry correctly`() {
        val timestamp = System.currentTimeMillis()
        val entity = LedgerEntryEntity(
            id = 1L,
            studentId = 5L,
            sessionId = 2L,
            entryDate = timestamp,
            particulars = "Receipt #100",
            entryType = LedgerEntryType.CREDIT,
            debitAmount = 0.0,
            creditAmount = 5000.0,
            balance = -500.0,
            referenceType = LedgerReferenceType.RECEIPT,
            referenceId = 100L,
            folioNumber = "F-001",
            isReversed = false,
            createdAt = timestamp
        )
        
        val entry = LedgerEntry.fromEntity(entity)
        
        assertThat(entry.id).isEqualTo(1L)
        assertThat(entry.studentId).isEqualTo(5L)
        assertThat(entry.sessionId).isEqualTo(2L)
        assertThat(entry.particulars).isEqualTo("Receipt #100")
        assertThat(entry.entryType).isEqualTo(LedgerEntryType.CREDIT)
        assertThat(entry.creditAmount).isEqualTo(5000.0)
        assertThat(entry.balance).isEqualTo(-500.0)
        assertThat(entry.referenceType).isEqualTo(LedgerReferenceType.RECEIPT)
        assertThat(entry.referenceId).isEqualTo(100L)
        assertThat(entry.folioNumber).isEqualTo("F-001")
    }

    @Test
    fun `toEntity and fromEntity roundtrip preserves all data`() {
        val original = createLedgerEntry(
            id = 5L,
            studentId = 10L,
            sessionId = 3L,
            particulars = "Adjustment Entry",
            entryType = LedgerEntryType.CREDIT,
            debitAmount = 0.0,
            creditAmount = 500.0,
            balance = -500.0,
            referenceType = LedgerReferenceType.ADJUSTMENT,
            referenceId = null,
            isReversed = false
        )
        
        val entity = original.toEntity()
        val restored = LedgerEntry.fromEntity(entity)
        
        assertThat(restored.id).isEqualTo(original.id)
        assertThat(restored.studentId).isEqualTo(original.studentId)
        assertThat(restored.sessionId).isEqualTo(original.sessionId)
        assertThat(restored.particulars).isEqualTo(original.particulars)
        assertThat(restored.entryType).isEqualTo(original.entryType)
        assertThat(restored.debitAmount).isEqualTo(original.debitAmount)
        assertThat(restored.creditAmount).isEqualTo(original.creditAmount)
        assertThat(restored.balance).isEqualTo(original.balance)
        assertThat(restored.referenceType).isEqualTo(original.referenceType)
        assertThat(restored.isReversed).isEqualTo(original.isReversed)
    }

    // ===== Enum Tests =====

    @Test
    fun `LedgerEntryType enum has all expected values`() {
        val types = LedgerEntryType.values()
        
        assertThat(types).hasLength(2)
        assertThat(types).asList().containsExactly(
            LedgerEntryType.DEBIT,
            LedgerEntryType.CREDIT
        )
    }

    @Test
    fun `LedgerReferenceType enum has all expected values`() {
        val types = LedgerReferenceType.values()
        
        assertThat(types).hasLength(6)
        assertThat(types).asList().containsExactly(
            LedgerReferenceType.FEE_CHARGE,
            LedgerReferenceType.RECEIPT,
            LedgerReferenceType.ADJUSTMENT,
            LedgerReferenceType.REVERSAL,
            LedgerReferenceType.OPENING_BALANCE,
            LedgerReferenceType.DISCOUNT
        )
    }

    // ===== Typical Usage Scenarios =====

    @Test
    fun `fee charge entry has correct structure`() {
        val feeCharge = createLedgerEntry(
            particulars = "Tuition Fee - Session 2025-26 (12 months @ Rs.1000/month)",
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 12000.0,
            creditAmount = 0.0,
            balance = 12000.0,
            referenceType = LedgerReferenceType.FEE_CHARGE
        )
        
        assertThat(feeCharge.isDebit).isTrue()
        assertThat(feeCharge.balanceStatus).isEqualTo("Due")
    }

    @Test
    fun `payment receipt entry reduces balance`() {
        val receipt = createLedgerEntry(
            particulars = "Receipt #100",
            entryType = LedgerEntryType.CREDIT,
            debitAmount = 0.0,
            creditAmount = 5000.0,
            balance = 7000.0, // Was 12000, now 7000 after payment
            referenceType = LedgerReferenceType.RECEIPT,
            referenceId = 100L
        )
        
        assertThat(receipt.isCredit).isTrue()
        assertThat(receipt.balanceStatus).isEqualTo("Due")
    }

    @Test
    fun `full payment clears balance`() {
        val fullPayment = createLedgerEntry(
            particulars = "Receipt #101 - Full Year Payment",
            entryType = LedgerEntryType.CREDIT,
            debitAmount = 0.0,
            creditAmount = 11000.0,
            balance = 0.0,
            referenceType = LedgerReferenceType.RECEIPT,
            referenceId = 101L
        )
        
        assertThat(fullPayment.balanceStatus).isEqualTo("Cleared")
    }

    @Test
    fun `advance payment creates negative balance`() {
        val advancePayment = createLedgerEntry(
            particulars = "Receipt #102 - Advance",
            entryType = LedgerEntryType.CREDIT,
            debitAmount = 0.0,
            creditAmount = 15000.0,
            balance = -3000.0, // Paid more than due
            referenceType = LedgerReferenceType.RECEIPT,
            referenceId = 102L
        )
        
        assertThat(advancePayment.balanceStatus).isEqualTo("Advance")
    }

    @Test
    fun `reversal entry increases balance back`() {
        val reversal = createLedgerEntry(
            particulars = "Cancelled Receipt #100 - Cheque bounced",
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 5000.0,
            creditAmount = 0.0,
            balance = 12000.0, // Balance restored
            referenceType = LedgerReferenceType.REVERSAL,
            referenceId = 100L
        )
        
        assertThat(reversal.isDebit).isTrue()
        assertThat(reversal.referenceType).isEqualTo(LedgerReferenceType.REVERSAL)
    }

    @Test
    fun `opening balance entry has correct structure`() {
        val openingBalance = createLedgerEntry(
            particulars = "Opening Balance - Previous Year Dues",
            entryType = LedgerEntryType.DEBIT,
            debitAmount = 3000.0,
            creditAmount = 0.0,
            balance = 3000.0,
            referenceType = LedgerReferenceType.OPENING_BALANCE,
            referenceId = 0L
        )
        
        assertThat(openingBalance.referenceType).isEqualTo(LedgerReferenceType.OPENING_BALANCE)
        assertThat(openingBalance.isDebit).isTrue()
    }

    @Test
    fun `discount entry reduces balance`() {
        val discount = createLedgerEntry(
            particulars = "Full Year Discount (1 month tuition free) - Receipt #103",
            entryType = LedgerEntryType.CREDIT,
            debitAmount = 0.0,
            creditAmount = 1000.0,
            balance = 0.0,
            referenceType = LedgerReferenceType.DISCOUNT,
            referenceId = 103L
        )
        
        assertThat(discount.referenceType).isEqualTo(LedgerReferenceType.DISCOUNT)
        assertThat(discount.isCredit).isTrue()
    }
}
