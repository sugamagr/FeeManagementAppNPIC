package com.navoditpublic.fees.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.SavedReportViewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedReportViewDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(view: SavedReportViewEntity): Long
    
    @Update
    suspend fun update(view: SavedReportViewEntity)
    
    @Delete
    suspend fun delete(view: SavedReportViewEntity)
    
    @Query("SELECT * FROM saved_report_views WHERE id = :id")
    suspend fun getById(id: Long): SavedReportViewEntity?
    
    @Query("SELECT * FROM saved_report_views WHERE report_type = :reportType ORDER BY view_name")
    fun getByReportType(reportType: String): Flow<List<SavedReportViewEntity>>
    
    @Query("SELECT * FROM saved_report_views WHERE report_type = :reportType ORDER BY view_name")
    suspend fun getByReportTypeSync(reportType: String): List<SavedReportViewEntity>
    
    @Query("SELECT * FROM saved_report_views ORDER BY report_type, view_name")
    fun getAll(): Flow<List<SavedReportViewEntity>>
    
    @Query("SELECT * FROM saved_report_views WHERE is_default = 1 AND report_type = :reportType LIMIT 1")
    suspend fun getDefaultView(reportType: String): SavedReportViewEntity?
    
    @Query("UPDATE saved_report_views SET is_default = 0 WHERE report_type = :reportType")
    suspend fun clearDefaultForType(reportType: String)
    
    @Query("DELETE FROM saved_report_views WHERE id = :id")
    suspend fun deleteById(id: Long)
}

