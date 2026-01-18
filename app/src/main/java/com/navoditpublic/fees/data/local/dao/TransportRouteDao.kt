package com.navoditpublic.fees.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.TransportRouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransportRouteDao {
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(route: TransportRouteEntity): Long
    
    @Update
    suspend fun update(route: TransportRouteEntity)
    
    @Delete
    suspend fun delete(route: TransportRouteEntity)
    
    @Query("SELECT * FROM transport_routes WHERE id = :id")
    suspend fun getById(id: Long): TransportRouteEntity?
    
    @Query("SELECT * FROM transport_routes WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<TransportRouteEntity?>
    
    @Query("SELECT * FROM transport_routes WHERE is_active = 1 ORDER BY route_name")
    fun getAllActiveRoutes(): Flow<List<TransportRouteEntity>>
    
    @Query("SELECT * FROM transport_routes ORDER BY route_name")
    fun getAllRoutes(): Flow<List<TransportRouteEntity>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM transport_routes WHERE route_name = :routeName)")
    suspend fun routeNameExists(routeName: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM transport_routes WHERE route_name = :routeName AND id != :excludeId)")
    suspend fun routeNameExistsExcluding(routeName: String, excludeId: Long): Boolean
    
    @Query("SELECT COUNT(*) FROM students WHERE transport_route_id = :routeId AND is_active = 1")
    fun getStudentCountForRoute(routeId: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM transport_routes")
    suspend fun getCount(): Int
    
    @Query("SELECT * FROM transport_routes WHERE is_active = 1")
    suspend fun getAllRoutesList(): List<TransportRouteEntity>
}


