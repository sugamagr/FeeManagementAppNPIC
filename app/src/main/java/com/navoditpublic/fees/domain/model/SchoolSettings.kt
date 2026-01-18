package com.navoditpublic.fees.domain.model

import com.navoditpublic.fees.data.local.entity.SchoolSettingsEntity

data class SchoolSettings(
    val id: Int = 1,
    val schoolName: String = "Navodit Public Inter College",
    val tagline: String = "Approved By the Government",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val district: String = "",
    val state: String = "Uttar Pradesh",
    val pincode: String = "",
    val phone: String = "",
    val email: String = "",
    val logoPath: String? = null,
    val lastReceiptNumber: Int = 0,
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
    
    val nextSuggestedReceiptNumber: Int
        get() = lastReceiptNumber + 1
    
    fun toEntity(): SchoolSettingsEntity = SchoolSettingsEntity(
        id = id,
        schoolName = schoolName,
        tagline = tagline,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        district = district,
        state = state,
        pincode = pincode,
        phone = phone,
        email = email,
        logoPath = logoPath,
        lastReceiptNumber = lastReceiptNumber,
        updatedAt = updatedAt
    )
    
    companion object {
        fun fromEntity(entity: SchoolSettingsEntity): SchoolSettings = SchoolSettings(
            id = entity.id,
            schoolName = entity.schoolName,
            tagline = entity.tagline,
            addressLine1 = entity.addressLine1,
            addressLine2 = entity.addressLine2,
            district = entity.district,
            state = entity.state,
            pincode = entity.pincode,
            phone = entity.phone,
            email = entity.email,
            logoPath = entity.logoPath,
            lastReceiptNumber = entity.lastReceiptNumber,
            updatedAt = entity.updatedAt
        )
    }
}


