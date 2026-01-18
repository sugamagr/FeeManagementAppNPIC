package com.navoditpublic.fees.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    RESTORE
}

@Entity(
    tableName = "audit_log",
    indices = [
        Index(value = ["entity_type", "entity_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["can_revert"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val timestamp: Long = System.currentTimeMillis(),
    
    val action: AuditAction,
    
    @ColumnInfo(name = "entity_type")
    val entityType: String, // "Student", "Receipt", "FeeStructure", etc.
    
    @ColumnInfo(name = "entity_id")
    val entityId: Long,
    
    @ColumnInfo(name = "entity_name")
    val entityName: String, // For display: student name, receipt number, etc.
    
    @ColumnInfo(name = "field_name")
    val fieldName: String? = null, // For UPDATE: which field changed
    
    @ColumnInfo(name = "old_value")
    val oldValue: String? = null, // JSON representation of old value
    
    @ColumnInfo(name = "new_value")
    val newValue: String? = null, // JSON representation of new value
    
    @ColumnInfo(name = "can_revert")
    val canRevert: Boolean = true,
    
    @ColumnInfo(name = "is_reverted")
    val isReverted: Boolean = false,
    
    @ColumnInfo(name = "reverted_at")
    val revertedAt: Long? = null,
    
    @ColumnInfo(name = "user_id")
    val userId: String? = null, // For future: Firebase user ID
    
    @ColumnInfo(name = "user_name")
    val userName: String = "Admin" // For future: Firebase user name
)


