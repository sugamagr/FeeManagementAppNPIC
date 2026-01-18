package com.navoditpublic.fees.domain.model

import com.navoditpublic.fees.data.local.entity.FeeStructureEntity
import com.navoditpublic.fees.data.local.entity.FeeType

data class FeeStructure(
    val id: Long = 0,
    val sessionId: Long,
    val className: String,
    val feeType: FeeType,
    val amount: Double,
    val fullYearDiscountMonths: Int = 1,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = when (feeType) {
            FeeType.MONTHLY -> "Monthly Fee - $className"
            FeeType.ANNUAL -> "Annual Fee - $className"
            FeeType.ADMISSION -> if (className == "ALL") "Admission Fee" else "Admission Fee - $className"
            FeeType.REGISTRATION -> "Registration Fee - $className"
        }
    
    val isMonthlyFeeClass: Boolean
        get() = className in listOf("NC", "LKG", "UKG", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th")
    
    val isAnnualFeeClass: Boolean
        get() = className in listOf("9th", "10th", "11th", "12th")
    
    fun toEntity(): FeeStructureEntity = FeeStructureEntity(
        id = id,
        sessionId = sessionId,
        className = className,
        feeType = feeType,
        amount = amount,
        fullYearDiscountMonths = fullYearDiscountMonths,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    companion object {
        fun fromEntity(entity: FeeStructureEntity): FeeStructure = FeeStructure(
            id = entity.id,
            sessionId = entity.sessionId,
            className = entity.className,
            feeType = entity.feeType,
            amount = entity.amount,
            fullYearDiscountMonths = entity.fullYearDiscountMonths,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
        
        // Classes that have monthly fee structure
        val MONTHLY_FEE_CLASSES = listOf("NC", "LKG", "UKG", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th")
        
        // Classes that have annual/lump sum fee structure  
        val ANNUAL_FEE_CLASSES = listOf("9th", "10th", "11th", "12th")
        
        // Classes eligible for registration fee
        val REGISTRATION_FEE_CLASSES = listOf("9th", "10th", "11th", "12th")
    }
}


