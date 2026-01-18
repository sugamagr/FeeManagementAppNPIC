package com.navoditpublic.fees.domain.model

import com.google.common.truth.Truth.assertThat
import com.navoditpublic.fees.data.local.entity.FeeStructureEntity
import com.navoditpublic.fees.data.local.entity.FeeType
import org.junit.Test

/**
 * Unit tests for FeeStructure domain model.
 */
class FeeStructureTest {

    private fun createFeeStructure(
        id: Long = 1L,
        sessionId: Long = 1L,
        className: String = "5th",
        feeType: FeeType = FeeType.MONTHLY,
        amount: Double = 1200.0,
        fullYearDiscountMonths: Int = 1,
        isActive: Boolean = true
    ) = FeeStructure(
        id = id,
        sessionId = sessionId,
        className = className,
        feeType = feeType,
        amount = amount,
        fullYearDiscountMonths = fullYearDiscountMonths,
        isActive = isActive
    )

    // ===== Display Name Tests =====

    @Test
    fun `displayName for MONTHLY fee type`() {
        val fee = createFeeStructure(feeType = FeeType.MONTHLY, className = "5th")
        assertThat(fee.displayName).isEqualTo("Monthly Fee - 5th")
    }

    @Test
    fun `displayName for ANNUAL fee type`() {
        val fee = createFeeStructure(feeType = FeeType.ANNUAL, className = "9th")
        assertThat(fee.displayName).isEqualTo("Annual Fee - 9th")
    }

    @Test
    fun `displayName for ADMISSION fee with ALL classes`() {
        val fee = createFeeStructure(feeType = FeeType.ADMISSION, className = "ALL")
        assertThat(fee.displayName).isEqualTo("Admission Fee")
    }

    @Test
    fun `displayName for ADMISSION fee with specific class`() {
        val fee = createFeeStructure(feeType = FeeType.ADMISSION, className = "5th")
        assertThat(fee.displayName).isEqualTo("Admission Fee - 5th")
    }

    @Test
    fun `displayName for REGISTRATION fee type`() {
        val fee = createFeeStructure(feeType = FeeType.REGISTRATION, className = "9th")
        assertThat(fee.displayName).isEqualTo("Registration Fee - 9th")
    }

    // ===== isMonthlyFeeClass Tests =====

    @Test
    fun `isMonthlyFeeClass returns true for NC to 8th`() {
        val monthlyClasses = listOf("NC", "LKG", "UKG", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th")
        
        monthlyClasses.forEach { className ->
            val fee = createFeeStructure(className = className)
            assertThat(fee.isMonthlyFeeClass).isTrue()
        }
    }

    @Test
    fun `isMonthlyFeeClass returns false for 9th to 12th`() {
        val annualClasses = listOf("9th", "10th", "11th", "12th")
        
        annualClasses.forEach { className ->
            val fee = createFeeStructure(className = className)
            assertThat(fee.isMonthlyFeeClass).isFalse()
        }
    }

    // ===== isAnnualFeeClass Tests =====

    @Test
    fun `isAnnualFeeClass returns true for 9th to 12th`() {
        val annualClasses = listOf("9th", "10th", "11th", "12th")
        
        annualClasses.forEach { className ->
            val fee = createFeeStructure(className = className)
            assertThat(fee.isAnnualFeeClass).isTrue()
        }
    }

    @Test
    fun `isAnnualFeeClass returns false for NC to 8th`() {
        val monthlyClasses = listOf("NC", "LKG", "UKG", "1st", "2nd", "3rd", "4th", "5th", "8th")
        
        monthlyClasses.forEach { className ->
            val fee = createFeeStructure(className = className)
            assertThat(fee.isAnnualFeeClass).isFalse()
        }
    }

    // ===== Companion Object Constants Tests =====

    @Test
    fun `MONTHLY_FEE_CLASSES contains correct classes`() {
        assertThat(FeeStructure.MONTHLY_FEE_CLASSES).containsExactly(
            "NC", "LKG", "UKG", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"
        )
    }

    @Test
    fun `ANNUAL_FEE_CLASSES contains correct classes`() {
        assertThat(FeeStructure.ANNUAL_FEE_CLASSES).containsExactly(
            "9th", "10th", "11th", "12th"
        )
    }

    @Test
    fun `REGISTRATION_FEE_CLASSES contains correct classes`() {
        assertThat(FeeStructure.REGISTRATION_FEE_CLASSES).containsExactly(
            "9th", "10th", "11th", "12th"
        )
    }

    @Test
    fun `MONTHLY and ANNUAL classes are mutually exclusive`() {
        val intersection = FeeStructure.MONTHLY_FEE_CLASSES.intersect(FeeStructure.ANNUAL_FEE_CLASSES.toSet())
        assertThat(intersection).isEmpty()
    }

    // ===== Entity Mapping Tests =====

    @Test
    fun `toEntity converts FeeStructure to FeeStructureEntity correctly`() {
        val fee = createFeeStructure(
            id = 1L,
            sessionId = 2L,
            className = "5th",
            feeType = FeeType.MONTHLY,
            amount = 1200.0,
            fullYearDiscountMonths = 1,
            isActive = true
        )
        
        val entity = fee.toEntity()
        
        assertThat(entity.id).isEqualTo(1L)
        assertThat(entity.sessionId).isEqualTo(2L)
        assertThat(entity.className).isEqualTo("5th")
        assertThat(entity.feeType).isEqualTo(FeeType.MONTHLY)
        assertThat(entity.amount).isEqualTo(1200.0)
        assertThat(entity.fullYearDiscountMonths).isEqualTo(1)
        assertThat(entity.isActive).isTrue()
    }

    @Test
    fun `fromEntity converts FeeStructureEntity to FeeStructure correctly`() {
        val timestamp = System.currentTimeMillis()
        val entity = FeeStructureEntity(
            id = 1L,
            sessionId = 2L,
            className = "9th",
            feeType = FeeType.ANNUAL,
            amount = 15000.0,
            fullYearDiscountMonths = 0,
            isActive = true,
            createdAt = timestamp,
            updatedAt = timestamp
        )
        
        val fee = FeeStructure.fromEntity(entity)
        
        assertThat(fee.id).isEqualTo(1L)
        assertThat(fee.sessionId).isEqualTo(2L)
        assertThat(fee.className).isEqualTo("9th")
        assertThat(fee.feeType).isEqualTo(FeeType.ANNUAL)
        assertThat(fee.amount).isEqualTo(15000.0)
        assertThat(fee.fullYearDiscountMonths).isEqualTo(0)
        assertThat(fee.isActive).isTrue()
    }

    @Test
    fun `toEntity and fromEntity roundtrip preserves all data`() {
        val original = createFeeStructure(
            id = 5L,
            sessionId = 10L,
            className = "LKG",
            feeType = FeeType.MONTHLY,
            amount = 800.0,
            fullYearDiscountMonths = 1,
            isActive = false
        )
        
        val entity = original.toEntity()
        val restored = FeeStructure.fromEntity(entity)
        
        assertThat(restored.id).isEqualTo(original.id)
        assertThat(restored.sessionId).isEqualTo(original.sessionId)
        assertThat(restored.className).isEqualTo(original.className)
        assertThat(restored.feeType).isEqualTo(original.feeType)
        assertThat(restored.amount).isEqualTo(original.amount)
        assertThat(restored.fullYearDiscountMonths).isEqualTo(original.fullYearDiscountMonths)
        assertThat(restored.isActive).isEqualTo(original.isActive)
    }

    // ===== Fee Type Enum Tests =====

    @Test
    fun `FeeType enum has all expected values`() {
        val feeTypes = FeeType.values()
        
        assertThat(feeTypes).hasLength(4)
        assertThat(feeTypes).asList().containsExactly(
            FeeType.MONTHLY,
            FeeType.ANNUAL,
            FeeType.ADMISSION,
            FeeType.REGISTRATION
        )
    }
}
