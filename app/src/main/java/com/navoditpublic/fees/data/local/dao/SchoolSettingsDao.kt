package com.navoditpublic.fees.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.SchoolSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolSettingsDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: SchoolSettingsEntity)
    
    @Update
    suspend fun update(settings: SchoolSettingsEntity)
    
    @Query("SELECT * FROM school_settings WHERE id = 1")
    suspend fun getSettings(): SchoolSettingsEntity?
    
    @Query("SELECT * FROM school_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<SchoolSettingsEntity?>
    
    @Query("UPDATE school_settings SET last_receipt_number = :receiptNumber WHERE id = 1")
    suspend fun updateLastReceiptNumber(receiptNumber: Int)
    
    @Query("SELECT last_receipt_number FROM school_settings WHERE id = 1")
    suspend fun getLastReceiptNumber(): Int?
}


