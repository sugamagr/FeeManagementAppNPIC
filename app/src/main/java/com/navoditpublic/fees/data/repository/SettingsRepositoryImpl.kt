package com.navoditpublic.fees.data.repository

import androidx.room.withTransaction
import com.navoditpublic.fees.data.local.FeesDatabase
import com.navoditpublic.fees.data.local.dao.AcademicSessionDao
import com.navoditpublic.fees.data.local.dao.ClassSectionDao
import com.navoditpublic.fees.data.local.dao.SchoolSettingsDao
import com.navoditpublic.fees.data.local.dao.TransportRouteDao
import com.navoditpublic.fees.data.local.entity.ClassSectionEntity
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.model.SchoolSettings
import com.navoditpublic.fees.domain.model.TransportRoute
import com.navoditpublic.fees.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val database: FeesDatabase,
    private val academicSessionDao: AcademicSessionDao,
    private val transportRouteDao: TransportRouteDao,
    private val schoolSettingsDao: SchoolSettingsDao,
    private val classSectionDao: ClassSectionDao
) : SettingsRepository {
    
    // Academic Sessions
    override suspend fun insertSession(session: AcademicSession): Result<Long> = runCatching {
        academicSessionDao.insert(session.toEntity())
    }
    
    override suspend fun updateSession(session: AcademicSession): Result<Unit> = runCatching {
        academicSessionDao.update(session.toEntity())
    }
    
    override suspend fun deleteSession(session: AcademicSession): Result<Unit> = runCatching {
        academicSessionDao.delete(session.toEntity())
    }
    
    override suspend fun deleteSession(sessionId: Long): Result<Unit> = runCatching {
        academicSessionDao.deleteById(sessionId)
    }
    
    override suspend fun setSessionActive(sessionId: Long, isActive: Boolean): Result<Unit> = runCatching {
        academicSessionDao.setSessionActive(sessionId, isActive)
    }
    
    override suspend fun getSessionById(id: Long): AcademicSession? {
        return academicSessionDao.getById(id)?.let { AcademicSession.fromEntity(it) }
    }
    
    override suspend fun getCurrentSession(): AcademicSession? {
        return academicSessionDao.getCurrentSession()?.let { AcademicSession.fromEntity(it) }
    }
    
    override fun getCurrentSessionFlow(): Flow<AcademicSession?> {
        return academicSessionDao.getCurrentSessionFlow().map { entity ->
            entity?.let { AcademicSession.fromEntity(it) }
        }
    }
    
    override fun getAllActiveSessions(): Flow<List<AcademicSession>> {
        return academicSessionDao.getAllActiveSessions().map { entities ->
            entities.map { AcademicSession.fromEntity(it) }
        }
    }
    
    override fun getAllSessions(): Flow<List<AcademicSession>> {
        return academicSessionDao.getAllSessions().map { entities ->
            entities.map { AcademicSession.fromEntity(it) }
        }
    }
    
    override suspend fun setCurrentSession(sessionId: Long): Result<Unit> = runCatching {
        // Wrap in transaction to prevent inconsistent state if setCurrentSession fails
        database.withTransaction {
            academicSessionDao.clearCurrentSession()
            academicSessionDao.setCurrentSession(sessionId)
        }
    }
    
    override suspend fun sessionNameExists(sessionName: String): Boolean {
        return academicSessionDao.sessionNameExists(sessionName)
    }
    
    override suspend fun sessionNameExistsExcluding(sessionName: String, excludeId: Long): Boolean {
        return academicSessionDao.sessionNameExistsExcluding(sessionName, excludeId)
    }
    
    // Transport Routes
    override suspend fun insertRoute(route: TransportRoute): Result<Long> = runCatching {
        transportRouteDao.insert(route.toEntity())
    }
    
    override suspend fun updateRoute(route: TransportRoute): Result<Unit> = runCatching {
        transportRouteDao.update(route.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }
    
    override suspend fun deleteRoute(route: TransportRoute): Result<Unit> = runCatching {
        // Hard delete - permanently removes the route
        // Use closeRoute() for soft delete with remarks preservation
        transportRouteDao.delete(route.toEntity())
    }
    
    override suspend fun getRouteById(id: Long): TransportRoute? {
        return transportRouteDao.getById(id)?.let { TransportRoute.fromEntity(it) }
    }
    
    override fun getRouteByIdFlow(id: Long): Flow<TransportRoute?> {
        return transportRouteDao.getByIdFlow(id).map { entity ->
            entity?.let { TransportRoute.fromEntity(it) }
        }
    }
    
    override fun getAllActiveRoutes(): Flow<List<TransportRoute>> {
        return transportRouteDao.getAllActiveRoutes().map { entities ->
            entities.map { TransportRoute.fromEntity(it) }
        }
    }
    
    override fun getAllRoutes(): Flow<List<TransportRoute>> {
        return transportRouteDao.getAllRoutes().map { entities ->
            entities.map { TransportRoute.fromEntity(it) }
        }
    }
    
    override suspend fun routeNameExists(routeName: String): Boolean {
        return transportRouteDao.routeNameExists(routeName)
    }
    
    override suspend fun routeNameExistsExcluding(routeName: String, excludeId: Long): Boolean {
        return transportRouteDao.routeNameExistsExcluding(routeName, excludeId)
    }
    
    override fun getStudentCountForRoute(routeId: Long): Flow<Int> {
        return transportRouteDao.getStudentCountForRoute(routeId)
    }
    
    // School Settings
    override suspend fun getSchoolSettings(): SchoolSettings? {
        return schoolSettingsDao.getSettings()?.let { SchoolSettings.fromEntity(it) }
    }
    
    override fun getSchoolSettingsFlow(): Flow<SchoolSettings?> {
        return schoolSettingsDao.getSettingsFlow().map { entity ->
            entity?.let { SchoolSettings.fromEntity(it) }
        }
    }
    
    override suspend fun updateSchoolSettings(settings: SchoolSettings): Result<Unit> = runCatching {
        schoolSettingsDao.update(settings.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }
    
    override suspend fun updateLastReceiptNumber(receiptNumber: Int): Result<Unit> = runCatching {
        schoolSettingsDao.updateLastReceiptNumber(receiptNumber)
    }
    
    override suspend fun getLastReceiptNumber(): Int? {
        return schoolSettingsDao.getLastReceiptNumber()
    }
    
    // Classes and Sections
    override fun getAllActiveClasses(): Flow<List<String>> {
        return classSectionDao.getAllActiveClasses()
    }
    
    override fun getSectionsForClass(className: String): Flow<List<String>> {
        return classSectionDao.getSectionsForClass(className)
    }
    
    override fun getAllActiveSections(): Flow<List<String>> {
        return classSectionDao.getAllActiveSections()
    }
    
    override suspend fun addSection(className: String, sectionName: String): Result<Long> = runCatching {
        val entity = ClassSectionEntity(
            className = className,
            sectionName = sectionName
        )
        classSectionDao.insert(entity)
    }
    
    override suspend fun removeSection(className: String, sectionName: String): Result<Unit> = runCatching {
        // Check if any students are in this section
        val studentCount = classSectionDao.getStudentCountInSection(className, sectionName)
        if (studentCount > 0) {
            throw IllegalStateException("Cannot delete section with $studentCount students")
        }
        // Actually delete the section from the database
        classSectionDao.deleteSection(className, sectionName)
    }
}


