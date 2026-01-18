package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_settings")
data class SchoolSettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Single row table
    
    @ColumnInfo(name = "school_name")
    val schoolName: String = "Navodit Public Inter College",
    
    val tagline: String = "Approved By the Government",
    
    @ColumnInfo(name = "address_line1")
    val addressLine1: String = "Myuna Khudaganj",
    
    @ColumnInfo(name = "address_line2")
    val addressLine2: String = "Shahjahanpur",
    
    val district: String = "Shahjahanpur",
    
    val state: String = "Uttar Pradesh",
    
    val pincode: String = "",
    
    val phone: String = "",
    
    val email: String = "",
    
    @ColumnInfo(name = "logo_path")
    val logoPath: String? = null,
    
    @ColumnInfo(name = "last_receipt_number")
    val lastReceiptNumber: Int = 0, // Track last used receipt for suggestion
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)


