package com.navoditpublic.fees.domain.model

import com.navoditpublic.fees.data.local.entity.AuditAction
import com.navoditpublic.fees.data.local.entity.AuditLogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AuditLog(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: AuditAction,
    val entityType: String,
    val entityId: Long,
    val entityName: String,
    val fieldName: String? = null,
    val oldValue: String? = null,
    val newValue: String? = null,
    val canRevert: Boolean = true,
    val isReverted: Boolean = false,
    val revertedAt: Long? = null,
    val userId: String? = null,
    val userName: String = "Admin"
) {
    val timestampFormatted: String
        get() = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    
    val dateFormatted: String
        get() = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(timestamp))
    
    val timeFormatted: String
        get() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    
    val actionDisplayText: String
        get() = when (action) {
            AuditAction.CREATE -> "Created"
            AuditAction.UPDATE -> "Updated"
            AuditAction.DELETE -> "Deleted"
            AuditAction.RESTORE -> "Restored"
        }
    
    val description: String
        get() = when (action) {
            AuditAction.CREATE -> "$entityType '$entityName' was created"
            AuditAction.UPDATE -> {
                if (fieldName != null) {
                    "$entityType '$entityName' - $fieldName changed from '$oldValue' to '$newValue'"
                } else {
                    "$entityType '$entityName' was updated"
                }
            }
            AuditAction.DELETE -> "$entityType '$entityName' was deleted"
            AuditAction.RESTORE -> "$entityType '$entityName' was restored"
        }
    
    fun toEntity(): AuditLogEntity = AuditLogEntity(
        id = id,
        timestamp = timestamp,
        action = action,
        entityType = entityType,
        entityId = entityId,
        entityName = entityName,
        fieldName = fieldName,
        oldValue = oldValue,
        newValue = newValue,
        canRevert = canRevert,
        isReverted = isReverted,
        revertedAt = revertedAt,
        userId = userId,
        userName = userName
    )
    
    companion object {
        fun fromEntity(entity: AuditLogEntity): AuditLog = AuditLog(
            id = entity.id,
            timestamp = entity.timestamp,
            action = entity.action,
            entityType = entity.entityType,
            entityId = entity.entityId,
            entityName = entity.entityName,
            fieldName = entity.fieldName,
            oldValue = entity.oldValue,
            newValue = entity.newValue,
            canRevert = entity.canRevert,
            isReverted = entity.isReverted,
            revertedAt = entity.revertedAt,
            userId = entity.userId,
            userName = entity.userName
        )
        
        // Entity type constants
        const val ENTITY_STUDENT = "Student"
        const val ENTITY_RECEIPT = "Receipt"
        const val ENTITY_FEE_STRUCTURE = "Fee Structure"
        const val ENTITY_TRANSPORT_ROUTE = "Transport Route"
        const val ENTITY_ACADEMIC_SESSION = "Academic Session"
        const val ENTITY_SCHOOL_SETTINGS = "School Settings"
        const val ENTITY_LEDGER_ENTRY = "Ledger Entry"
    }
}


