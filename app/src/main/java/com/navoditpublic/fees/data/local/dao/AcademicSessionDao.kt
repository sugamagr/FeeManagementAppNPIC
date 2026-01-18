package com.navoditpublic.fees.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.AcademicSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicSessionDao {
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: AcademicSessionEntity): Long
    
    @Update
    suspend fun update(session: AcademicSessionEntity)
    
    @Delete
    suspend fun delete(session: AcademicSessionEntity)
    
    @Query("SELECT * FROM academic_sessions WHERE id = :id")
    suspend fun getById(id: Long): AcademicSessionEntity?
    
    @Query("SELECT * FROM academic_sessions WHERE is_current = 1 LIMIT 1")
    suspend fun getCurrentSession(): AcademicSessionEntity?
    
    @Query("SELECT * FROM academic_sessions WHERE is_current = 1 LIMIT 1")
    fun getCurrentSessionFlow(): Flow<AcademicSessionEntity?>
    
    @Query("SELECT * FROM academic_sessions WHERE is_active = 1 ORDER BY start_date DESC")
    fun getAllActiveSessions(): Flow<List<AcademicSessionEntity>>
    
    @Query("SELECT * FROM academic_sessions ORDER BY start_date DESC")
    fun getAllSessions(): Flow<List<AcademicSessionEntity>>
    
    @Query("UPDATE academic_sessions SET is_current = 0")
    suspend fun clearCurrentSession()
    
    @Query("UPDATE academic_sessions SET is_current = 1 WHERE id = :sessionId")
    suspend fun setCurrentSession(sessionId: Long)
    
    @Query("SELECT EXISTS(SELECT 1 FROM academic_sessions WHERE session_name = :sessionName)")
    suspend fun sessionNameExists(sessionName: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM academic_sessions WHERE session_name = :sessionName AND id != :excludeId)")
    suspend fun sessionNameExistsExcluding(sessionName: String, excludeId: Long): Boolean
}


