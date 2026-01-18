package com.navoditpublic.fees.data.repository

import com.navoditpublic.fees.data.local.dao.AuditLogDao
import com.navoditpublic.fees.data.local.entity.AuditAction
import com.navoditpublic.fees.data.local.entity.AuditLogEntity
import com.navoditpublic.fees.domain.model.AuditLog
import com.navoditpublic.fees.domain.repository.AuditRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditRepositoryImpl @Inject constructor(
    private val auditLogDao: AuditLogDao
) : AuditRepository {
    
    override suspend fun log(
        action: AuditAction,
        entityType: String,
        entityId: Long,
        entityName: String,
        fieldName: String?,
        oldValue: String?,
        newValue: String?,
        canRevert: Boolean
    ): Long {
        val entry = AuditLogEntity(
            action = action,
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            fieldName = fieldName,
            oldValue = oldValue,
            newValue = newValue,
            canRevert = canRevert
        )
        return auditLogDao.insert(entry)
    }
    
    override suspend fun logCreate(
        entityType: String,
        entityId: Long,
        entityName: String
    ): Long {
        return log(
            action = AuditAction.CREATE,
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            canRevert = true
        )
    }
    
    override suspend fun logUpdate(
        entityType: String,
        entityId: Long,
        entityName: String,
        fieldName: String,
        oldValue: String?,
        newValue: String?
    ): Long {
        return log(
            action = AuditAction.UPDATE,
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            fieldName = fieldName,
            oldValue = oldValue,
            newValue = newValue,
            canRevert = true
        )
    }
    
    override suspend fun logDelete(
        entityType: String,
        entityId: Long,
        entityName: String,
        canRevert: Boolean
    ): Long {
        return log(
            action = AuditAction.DELETE,
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            canRevert = canRevert
        )
    }
    
    override suspend fun getById(id: Long): AuditLog? {
        return auditLogDao.getById(id)?.let { AuditLog.fromEntity(it) }
    }
    
    override fun getAllLogs(): Flow<List<AuditLog>> {
        return auditLogDao.getAllLogs().map { entities ->
            entities.map { AuditLog.fromEntity(it) }
        }
    }
    
    override fun getRecentLogs(limit: Int): Flow<List<AuditLog>> {
        return auditLogDao.getRecentLogs(limit).map { entities ->
            entities.map { AuditLog.fromEntity(it) }
        }
    }
    
    override fun getLogsByEntityType(entityType: String): Flow<List<AuditLog>> {
        return auditLogDao.getLogsByEntityType(entityType).map { entities ->
            entities.map { AuditLog.fromEntity(it) }
        }
    }
    
    override fun getLogsForEntity(entityType: String, entityId: Long): Flow<List<AuditLog>> {
        return auditLogDao.getLogsForEntity(entityType, entityId).map { entities ->
            entities.map { AuditLog.fromEntity(it) }
        }
    }
    
    override fun getLogsByAction(action: AuditAction): Flow<List<AuditLog>> {
        return auditLogDao.getLogsByAction(action).map { entities ->
            entities.map { AuditLog.fromEntity(it) }
        }
    }
    
    override fun getLogsByDateRange(startDate: Long, endDate: Long): Flow<List<AuditLog>> {
        return auditLogDao.getLogsByDateRange(startDate, endDate).map { entities ->
            entities.map { AuditLog.fromEntity(it) }
        }
    }
    
    override fun getRevertibleLogs(): Flow<List<AuditLog>> {
        return auditLogDao.getRevertibleLogs().map { entities ->
            entities.map { AuditLog.fromEntity(it) }
        }
    }
    
    override suspend fun markAsReverted(id: Long): Result<Unit> = runCatching {
        auditLogDao.markAsReverted(id)
    }
    
    override fun getTotalLogCount(): Flow<Int> {
        return auditLogDao.getTotalLogCount()
    }
    
    override suspend fun deleteOldLogs(beforeTimestamp: Long): Result<Unit> = runCatching {
        auditLogDao.deleteOldLogs(beforeTimestamp)
    }
}


