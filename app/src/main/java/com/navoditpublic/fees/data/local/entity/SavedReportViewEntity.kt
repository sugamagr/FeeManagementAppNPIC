package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to store saved custom report views.
 * Users can create, edit, and delete their preferred report configurations.
 */
@Entity(tableName = "saved_report_views")
data class SavedReportViewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "view_name")
    val viewName: String,
    
    @ColumnInfo(name = "report_type")
    val reportType: String, // "DUES" or "COLLECTION"
    
    @ColumnInfo(name = "selected_columns")
    val selectedColumns: String, // JSON array of column names
    
    @ColumnInfo(name = "filter_config")
    val filterConfig: String = "", // JSON object with filter settings
    
    @ColumnInfo(name = "sort_column")
    val sortColumn: String = "",
    
    @ColumnInfo(name = "sort_ascending")
    val sortAscending: Boolean = true,
    
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

