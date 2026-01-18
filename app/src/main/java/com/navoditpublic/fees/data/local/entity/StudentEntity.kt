package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["sr_number"], unique = true),
        Index(value = ["account_number"], unique = true),
        Index(value = ["current_class"]),
        Index(value = ["is_active"])
    ]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "sr_number")
    val srNumber: String,
    
    @ColumnInfo(name = "account_number")
    val accountNumber: String,
    
    val name: String,
    
    @ColumnInfo(name = "father_name")
    val fatherName: String,
    
    @ColumnInfo(name = "mother_name")
    val motherName: String = "",
    
    @ColumnInfo(name = "phone_primary")
    val phonePrimary: String,
    
    @ColumnInfo(name = "phone_secondary")
    val phoneSecondary: String = "",
    
    @ColumnInfo(name = "address_line1")
    val addressLine1: String = "",
    
    @ColumnInfo(name = "address_line2")
    val addressLine2: String = "",
    
    val district: String = "",
    
    val state: String = "Uttar Pradesh",
    
    val pincode: String = "",
    
    @ColumnInfo(name = "photo_path")
    val photoPath: String? = null,
    
    @ColumnInfo(name = "current_class")
    val currentClass: String,
    
    val section: String = "A",
    
    @ColumnInfo(name = "admission_date")
    val admissionDate: Long, // epoch millis
    
    @ColumnInfo(name = "admission_session_id")
    val admissionSessionId: Long,
    
    @ColumnInfo(name = "has_transport")
    val hasTransport: Boolean = false,
    
    @ColumnInfo(name = "transport_route_id")
    val transportRouteId: Long? = null,
    
    // Opening Balance for data migration (previous dues carried forward)
    @ColumnInfo(name = "opening_balance")
    val openingBalance: Double = 0.0,
    
    @ColumnInfo(name = "opening_balance_remarks")
    val openingBalanceRemarks: String = "",
    
    @ColumnInfo(name = "opening_balance_date")
    val openingBalanceDate: Long? = null, // Session start date when set
    
    // Admission fee tracking
    @ColumnInfo(name = "admission_fee_paid")
    val admissionFeePaid: Boolean = false,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)


