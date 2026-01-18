package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "academic_sessions",
    indices = [
        Index(value = ["session_name"], unique = true),
        Index(value = ["is_current"])
    ]
)
data class AcademicSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "session_name")
    val sessionName: String, // e.g., "2025-26"
    
    @ColumnInfo(name = "start_date")
    val startDate: Long, // April 1 epoch millis
    
    @ColumnInfo(name = "end_date")
    val endDate: Long, // March 31 epoch millis
    
    @ColumnInfo(name = "is_current")
    val isCurrent: Boolean = false,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)


