package com.navoditpublic.fees.domain.model

import com.navoditpublic.fees.data.local.entity.StudentEntity

data class Student(
    val id: Long = 0,
    val srNumber: String,
    val accountNumber: String,
    val name: String,
    val fatherName: String,
    val motherName: String = "",
    val phonePrimary: String,
    val phoneSecondary: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val district: String = "",
    val state: String = "Uttar Pradesh",
    val pincode: String = "",
    val photoPath: String? = null,
    val currentClass: String,
    val section: String = "A",
    val admissionDate: Long,
    val admissionSessionId: Long,
    val hasTransport: Boolean = false,
    val transportRouteId: Long? = null,
    // Opening Balance for data migration
    val openingBalance: Double = 0.0,
    val openingBalanceRemarks: String = "",
    val openingBalanceDate: Long? = null,
    // Admission fee tracking
    val admissionFeePaid: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val fullAddress: String
        get() = buildString {
            if (addressLine1.isNotBlank()) append(addressLine1)
            if (addressLine2.isNotBlank()) {
                if (isNotBlank()) append(", ")
                append(addressLine2)
            }
            if (district.isNotBlank()) {
                if (isNotBlank()) append(", ")
                append(district)
            }
            if (state.isNotBlank()) {
                if (isNotBlank()) append(", ")
                append(state)
            }
            if (pincode.isNotBlank()) {
                if (isNotBlank()) append(" - ")
                append(pincode)
            }
        }
    
    val classSection: String
        get() = "$currentClass - $section"
    
    fun toEntity(): StudentEntity = StudentEntity(
        id = id,
        srNumber = srNumber,
        accountNumber = accountNumber,
        name = name,
        fatherName = fatherName,
        motherName = motherName,
        phonePrimary = phonePrimary,
        phoneSecondary = phoneSecondary,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        district = district,
        state = state,
        pincode = pincode,
        photoPath = photoPath,
        currentClass = currentClass,
        section = section,
        admissionDate = admissionDate,
        admissionSessionId = admissionSessionId,
        hasTransport = hasTransport,
        transportRouteId = transportRouteId,
        openingBalance = openingBalance,
        openingBalanceRemarks = openingBalanceRemarks,
        openingBalanceDate = openingBalanceDate,
        admissionFeePaid = admissionFeePaid,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    companion object {
        fun fromEntity(entity: StudentEntity): Student = Student(
            id = entity.id,
            srNumber = entity.srNumber,
            accountNumber = entity.accountNumber,
            name = entity.name,
            fatherName = entity.fatherName,
            motherName = entity.motherName,
            phonePrimary = entity.phonePrimary,
            phoneSecondary = entity.phoneSecondary,
            addressLine1 = entity.addressLine1,
            addressLine2 = entity.addressLine2,
            district = entity.district,
            state = entity.state,
            pincode = entity.pincode,
            photoPath = entity.photoPath,
            currentClass = entity.currentClass,
            section = entity.section,
            admissionDate = entity.admissionDate,
            admissionSessionId = entity.admissionSessionId,
            hasTransport = entity.hasTransport,
            transportRouteId = entity.transportRouteId,
            openingBalance = entity.openingBalance,
            openingBalanceRemarks = entity.openingBalanceRemarks,
            openingBalanceDate = entity.openingBalanceDate,
            admissionFeePaid = entity.admissionFeePaid,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}

/**
 * Student with balance information for list display
 */
data class StudentWithBalance(
    val student: Student,
    val currentBalance: Double
) {
    val hasDues: Boolean get() = currentBalance > 0
    val hasAdvance: Boolean get() = currentBalance < 0
    val isCleared: Boolean get() = currentBalance == 0.0
}


