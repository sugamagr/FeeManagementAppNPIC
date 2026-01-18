package com.navoditpublic.fees.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.ClassSectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassSectionDao {
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(classSection: ClassSectionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(classSections: List<ClassSectionEntity>)
    
    @Update
    suspend fun update(classSection: ClassSectionEntity)
    
    @Delete
    suspend fun delete(classSection: ClassSectionEntity)
    
    @Query("SELECT * FROM class_sections WHERE id = :id")
    suspend fun getById(id: Long): ClassSectionEntity?
    
    @Query("SELECT * FROM class_sections WHERE is_active = 1 ORDER BY display_order, class_name, section_name")
    fun getAllActiveClassSections(): Flow<List<ClassSectionEntity>>
    
    @Query("SELECT DISTINCT class_name FROM class_sections WHERE is_active = 1 ORDER BY display_order")
    fun getAllActiveClasses(): Flow<List<String>>
    
    @Query("SELECT DISTINCT section_name FROM class_sections WHERE class_name = :className AND is_active = 1 ORDER BY section_name")
    fun getSectionsForClass(className: String): Flow<List<String>>
    
    @Query("SELECT DISTINCT section_name FROM class_sections WHERE is_active = 1 ORDER BY section_name")
    fun getAllActiveSections(): Flow<List<String>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM class_sections WHERE class_name = :className AND section_name = :sectionName)")
    suspend fun classSectionExists(className: String, sectionName: String): Boolean
    
    @Query("SELECT COUNT(*) FROM class_sections")
    suspend fun getCount(): Int
    
    @Query("SELECT COUNT(*) FROM students WHERE current_class = :className AND section = :sectionName AND is_active = 1")
    suspend fun getStudentCountInSection(className: String, sectionName: String): Int
    
    @Query("DELETE FROM class_sections WHERE class_name = :className AND section_name = :sectionName")
    suspend fun deleteSection(className: String, sectionName: String)
    
    @Query("SELECT COUNT(*) FROM students WHERE current_class = :className AND is_active = 1")
    suspend fun getStudentCountInClass(className: String): Int
    
    @Query("SELECT COUNT(*) FROM students WHERE is_active = 1")
    suspend fun getTotalStudentCount(): Int
    
    @Query("SELECT COUNT(*) FROM students WHERE is_active = 1")
    fun getTotalStudentCountFlow(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM class_sections WHERE is_active = 1")
    suspend fun getTotalSectionCount(): Int
}


