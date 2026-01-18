package com.navoditpublic.fees.domain.repository

import com.navoditpublic.fees.data.local.entity.AuditAction
import com.navoditpublic.fees.domain.model.AuditLog
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Audit Log operations.
 */
interface AuditRepository {
    
    suspend fun log(
        action: AuditAction,
        entityType: String,
        entityId: Long,
        entityName: String,
        fieldName: String? = null,
        oldValue: String? = null,
        newValue: String? = null,
        canRevert: Boolean = true
    ): Long
    
    suspend fun logCreate(
        entityType: String,
        entityId: Long,
        entityName: String
    ): Long
    
    suspend fun logUpdate(
        entityType: String,
        entityId: Long,
        entityName: String,
        fieldName: String,
        oldValue: String?,
        newValue: String?
    ): Long
    
    suspend fun logDelete(
        entityType: String,
        entityId: Long,
        entityName: String,
        canRevert: Boolean = true
    ): Long
    
    suspend fun getById(id: Long): AuditLog?
    
    fun getAllLogs(): Flow<List<AuditLog>>
    
    fun getRecentLogs(limit: Int): Flow<List<AuditLog>>
    
    fun getLogsByEntityType(entityType: String): Flow<List<AuditLog>>
    
    fun getLogsForEntity(entityType: String, entityId: Long): Flow<List<AuditLog>>
    
    fun getLogsByAction(action: AuditAction): Flow<List<AuditLog>>
    
    fun getLogsByDateRange(startDate: Long, endDate: Long): Flow<List<AuditLog>>
    
    fun getRevertibleLogs(): Flow<List<AuditLog>>
    
    suspend fun markAsReverted(id: Long): Result<Unit>
    
    fun getTotalLogCount(): Flow<Int>
    
    suspend fun deleteOldLogs(beforeTimestamp: Long): Result<Unit>
}


