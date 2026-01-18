package com.navoditpublic.fees.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.AuditAction
import com.navoditpublic.fees.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLogEntity): Long
    
    @Update
    suspend fun update(log: AuditLogEntity)
    
    @Query("SELECT * FROM audit_log WHERE id = :id")
    suspend fun getById(id: Long): AuditLogEntity?
    
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>
    
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<AuditLogEntity>>
    
    @Query("""
        SELECT * FROM audit_log 
        WHERE entity_type = :entityType 
        ORDER BY timestamp DESC
    """)
    fun getLogsByEntityType(entityType: String): Flow<List<AuditLogEntity>>
    
    @Query("""
        SELECT * FROM audit_log 
        WHERE entity_type = :entityType 
        AND entity_id = :entityId 
        ORDER BY timestamp DESC
    """)
    fun getLogsForEntity(entityType: String, entityId: Long): Flow<List<AuditLogEntity>>
    
    @Query("""
        SELECT * FROM audit_log 
        WHERE action = :action 
        ORDER BY timestamp DESC
    """)
    fun getLogsByAction(action: AuditAction): Flow<List<AuditLogEntity>>
    
    @Query("""
        SELECT * FROM audit_log 
        WHERE timestamp >= :startDate 
        AND timestamp <= :endDate 
        ORDER BY timestamp DESC
    """)
    fun getLogsByDateRange(startDate: Long, endDate: Long): Flow<List<AuditLogEntity>>
    
    @Query("""
        SELECT * FROM audit_log 
        WHERE can_revert = 1 
        AND is_reverted = 0 
        ORDER BY timestamp DESC
    """)
    fun getRevertibleLogs(): Flow<List<AuditLogEntity>>
    
    @Query("""
        UPDATE audit_log 
        SET is_reverted = 1, reverted_at = :revertedAt 
        WHERE id = :id
    """)
    suspend fun markAsReverted(id: Long, revertedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM audit_log")
    fun getTotalLogCount(): Flow<Int>
    
    @Query("DELETE FROM audit_log WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldLogs(beforeTimestamp: Long)
}


