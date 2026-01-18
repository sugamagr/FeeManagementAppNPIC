package com.navoditpublic.fees.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.FeeStructureEntity
import com.navoditpublic.fees.data.local.entity.FeeType
import kotlinx.coroutines.flow.Flow

@Dao
interface FeeStructureDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feeStructure: FeeStructureEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(feeStructures: List<FeeStructureEntity>)
    
    @Update
    suspend fun update(feeStructure: FeeStructureEntity)
    
    @Delete
    suspend fun delete(feeStructure: FeeStructureEntity)
    
    @Query("SELECT * FROM fee_structure WHERE id = :id")
    suspend fun getById(id: Long): FeeStructureEntity?
    
    @Query("SELECT * FROM fee_structure WHERE session_id = :sessionId AND is_active = 1 ORDER BY class_name")
    fun getFeeStructureBySession(sessionId: Long): Flow<List<FeeStructureEntity>>
    
    @Query("""
        SELECT * FROM fee_structure 
        WHERE session_id = :sessionId 
        AND class_name = :className 
        AND fee_type = :feeType 
        AND is_active = 1
        LIMIT 1
    """)
    suspend fun getFeeForClass(sessionId: Long, className: String, feeType: FeeType): FeeStructureEntity?
    
    @Query("""
        SELECT * FROM fee_structure 
        WHERE session_id = :sessionId 
        AND class_name = :className 
        AND is_active = 1
    """)
    fun getFeesForClass(sessionId: Long, className: String): Flow<List<FeeStructureEntity>>
    
    @Query("""
        SELECT * FROM fee_structure 
        WHERE session_id = :sessionId 
        AND class_name = :className 
        AND is_active = 1
    """)
    suspend fun getFeesForClassSync(sessionId: Long, className: String): List<FeeStructureEntity>
    
    @Query("""
        SELECT * FROM fee_structure 
        WHERE session_id = :sessionId 
        AND fee_type = :feeType 
        AND is_active = 1 
        ORDER BY class_name
    """)
    fun getFeesByType(sessionId: Long, feeType: FeeType): Flow<List<FeeStructureEntity>>
    
    @Query("""
        SELECT * FROM fee_structure 
        WHERE session_id = :sessionId 
        AND (class_name = :className OR class_name = 'ALL') 
        AND fee_type = 'ADMISSION' 
        AND is_active = 1
        LIMIT 1
    """)
    suspend fun getAdmissionFee(sessionId: Long, className: String): FeeStructureEntity?
    
    @Query("""
        SELECT * FROM fee_structure 
        WHERE session_id = :sessionId 
        AND class_name = :className 
        AND fee_type = 'REGISTRATION' 
        AND is_active = 1
        LIMIT 1
    """)
    suspend fun getRegistrationFee(sessionId: Long, className: String): FeeStructureEntity?
    
    @Query("SELECT COUNT(*) FROM fee_structure WHERE session_id = :sessionId")
    suspend fun getCountForSession(sessionId: Long): Int
}


