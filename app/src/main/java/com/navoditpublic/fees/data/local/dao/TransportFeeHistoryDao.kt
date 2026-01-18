package com.navoditpublic.fees.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.TransportFeeHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransportFeeHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feeHistory: TransportFeeHistoryEntity): Long
    
    @Update
    suspend fun update(feeHistory: TransportFeeHistoryEntity)
    
    @Delete
    suspend fun delete(feeHistory: TransportFeeHistoryEntity)
    
    @Query("SELECT * FROM transport_fee_history WHERE id = :id")
    suspend fun getById(id: Long): TransportFeeHistoryEntity?
    
    @Query("SELECT * FROM transport_fee_history WHERE route_id = :routeId ORDER BY effective_from DESC")
    fun getFeeHistoryForRoute(routeId: Long): Flow<List<TransportFeeHistoryEntity>>
    
    @Query("SELECT * FROM transport_fee_history WHERE route_id = :routeId ORDER BY effective_from DESC LIMIT 1")
    suspend fun getCurrentFeeForRoute(routeId: Long): TransportFeeHistoryEntity?
    
    /**
     * Get the applicable fee for a route on a specific date.
     * Returns the most recent fee entry with effective_from <= the given date.
     */
    @Query("""
        SELECT * FROM transport_fee_history 
        WHERE route_id = :routeId AND effective_from <= :date 
        ORDER BY effective_from DESC 
        LIMIT 1
    """)
    suspend fun getFeeForRouteOnDate(routeId: Long, date: Long): TransportFeeHistoryEntity?
    
    @Query("SELECT COUNT(*) FROM transport_fee_history WHERE route_id = :routeId")
    suspend fun getFeeHistoryCountForRoute(routeId: Long): Int
    
    @Query("DELETE FROM transport_fee_history WHERE route_id = :routeId")
    suspend fun deleteAllForRoute(routeId: Long)
}


