package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "class_sections",
    indices = [
        Index(value = ["class_name", "section_name"], unique = true)
    ]
)
data class ClassSectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "class_name")
    val className: String, // NC, LKG, UKG, 1st, 2nd... 12th
    
    @ColumnInfo(name = "section_name")
    val sectionName: String, // A, B, C...
    
    @ColumnInfo(name = "display_order")
    val displayOrder: Int = 0, // For sorting classes properly
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)


