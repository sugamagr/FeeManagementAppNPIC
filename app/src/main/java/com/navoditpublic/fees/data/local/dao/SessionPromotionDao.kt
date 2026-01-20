package com.navoditpublic.fees.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.PromotionStatus
import com.navoditpublic.fees.data.local.entity.SessionPromotionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionPromotionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(promotion: SessionPromotionEntity): Long
    
    @Update
    suspend fun update(promotion: SessionPromotionEntity)
    
    @Query("SELECT * FROM session_promotions WHERE id = :id")
    suspend fun getById(id: Long): SessionPromotionEntity?
    
    /**
     * Get promotion record for a target session
     */
    @Query("""
        SELECT * FROM session_promotions 
        WHERE target_session_id = :targetSessionId 
        AND status = 'COMPLETED'
        LIMIT 1
    """)
    suspend fun getPromotionForTargetSession(targetSessionId: Long): SessionPromotionEntity?
    
    /**
     * Get promotion record for a target session as Flow (for real-time updates)
     */
    @Query("""
        SELECT * FROM session_promotions 
        WHERE target_session_id = :targetSessionId 
        AND status = 'COMPLETED'
        LIMIT 1
    """)
    fun getPromotionForTargetSessionFlow(targetSessionId: Long): Flow<SessionPromotionEntity?>
    
    /**
     * Check if a session was created via promotion
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM session_promotions 
            WHERE target_session_id = :targetSessionId 
            AND status = 'COMPLETED'
        )
    """)
    suspend fun wasSessionPromoted(targetSessionId: Long): Boolean
    
    /**
     * Get all promotions ordered by date
     */
    @Query("SELECT * FROM session_promotions ORDER BY promoted_at DESC")
    fun getAllPromotions(): Flow<List<SessionPromotionEntity>>
    
    /**
     * Get all completed (not reverted) promotions
     */
    @Query("""
        SELECT * FROM session_promotions 
        WHERE status = 'COMPLETED' 
        ORDER BY promoted_at DESC
    """)
    fun getCompletedPromotions(): Flow<List<SessionPromotionEntity>>
    
    /**
     * Mark a promotion as reverted
     */
    @Query("""
        UPDATE session_promotions 
        SET status = 'REVERTED', 
            reverted_at = :revertedAt, 
            revert_reason = :reason 
        WHERE id = :promotionId
    """)
    suspend fun markAsReverted(promotionId: Long, revertedAt: Long, reason: String?)
    
    /**
     * Get promotion record where this session was the source (to find the target session).
     * Used to determine what session was created from this source.
     */
    @Query("""
        SELECT * FROM session_promotions 
        WHERE source_session_id = :sourceSessionId 
        AND status = 'COMPLETED'
        LIMIT 1
    """)
    suspend fun getPromotionForSourceSession(sourceSessionId: Long): SessionPromotionEntity?
}
