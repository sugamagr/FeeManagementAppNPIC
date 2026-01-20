package com.navoditpublic.fees.domain.repository

import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.model.SchoolSettings
import com.navoditpublic.fees.domain.model.TransportRoute
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Settings operations.
 */
interface SettingsRepository {
    
    // Academic Sessions
    suspend fun insertSession(session: AcademicSession): Result<Long>
    
    suspend fun updateSession(session: AcademicSession): Result<Unit>
    
    suspend fun deleteSession(session: AcademicSession): Result<Unit>
    
    suspend fun deleteSession(sessionId: Long): Result<Unit>
    
    suspend fun setSessionActive(sessionId: Long, isActive: Boolean): Result<Unit>
    
    suspend fun getSessionById(id: Long): AcademicSession?
    
    suspend fun getCurrentSession(): AcademicSession?
    
    fun getCurrentSessionFlow(): Flow<AcademicSession?>
    
    fun getAllActiveSessions(): Flow<List<AcademicSession>>
    
    fun getAllSessions(): Flow<List<AcademicSession>>
    
    suspend fun setCurrentSession(sessionId: Long): Result<Unit>
    
    suspend fun sessionNameExists(sessionName: String): Boolean
    
    suspend fun sessionNameExistsExcluding(sessionName: String, excludeId: Long): Boolean
    
    // Transport Routes
    suspend fun insertRoute(route: TransportRoute): Result<Long>
    
    suspend fun updateRoute(route: TransportRoute): Result<Unit>
    
    suspend fun deleteRoute(route: TransportRoute): Result<Unit>
    
    suspend fun getRouteById(id: Long): TransportRoute?
    
    fun getRouteByIdFlow(id: Long): Flow<TransportRoute?>
    
    fun getAllActiveRoutes(): Flow<List<TransportRoute>>
    
    fun getAllRoutes(): Flow<List<TransportRoute>>
    
    suspend fun routeNameExists(routeName: String): Boolean
    
    suspend fun routeNameExistsExcluding(routeName: String, excludeId: Long): Boolean
    
    fun getStudentCountForRoute(routeId: Long): Flow<Int>
    
    // School Settings
    suspend fun getSchoolSettings(): SchoolSettings?
    
    fun getSchoolSettingsFlow(): Flow<SchoolSettings?>
    
    suspend fun updateSchoolSettings(settings: SchoolSettings): Result<Unit>
    
    suspend fun updateLastReceiptNumber(receiptNumber: Int): Result<Unit>
    
    suspend fun getLastReceiptNumber(): Int?
    
    // Classes and Sections
    fun getAllActiveClasses(): Flow<List<String>>
    
    fun getSectionsForClass(className: String): Flow<List<String>>
    
    fun getAllActiveSections(): Flow<List<String>>
    
    suspend fun addSection(className: String, sectionName: String): Result<Long>
    
    suspend fun removeSection(className: String, sectionName: String): Result<Unit>
    
    // ========== Session Promotion Methods ==========
    
    /**
     * Save a session promotion record
     */
    suspend fun saveSessionPromotion(promotion: com.navoditpublic.fees.domain.model.SessionPromotion): Result<Long>
    
    /**
     * Get promotion record for a target session
     */
    suspend fun getPromotionForSession(targetSessionId: Long): com.navoditpublic.fees.domain.model.SessionPromotion?
    
    /**
     * Check if a session was created via promotion
     */
    suspend fun wasSessionPromoted(targetSessionId: Long): Boolean
    
    /**
     * Mark a promotion as reverted
     */
    suspend fun markPromotionAsReverted(promotionId: Long, reason: String?): Result<Unit>
    
    /**
     * Get promotion record for a target session as Flow
     */
    fun getPromotionForSessionFlow(targetSessionId: Long): Flow<com.navoditpublic.fees.domain.model.SessionPromotion?>
    
    // ========== Session Access Level Methods ==========
    
    /**
     * Get the session that was created from this source session (via migration).
     * Returns null if this session was never migrated to another session.
     */
    suspend fun getTargetSessionFromSource(sourceSessionId: Long): AcademicSession?
    
    /**
     * Get the previous session (the session that was migrated to create the current session).
     * Returns null if current session was not created via migration.
     */
    suspend fun getPreviousSession(): AcademicSession?
    
    /**
     * Check if a session is the "previous" session (immediate source of current session).
     */
    suspend fun isPreviousSession(sessionId: Long): Boolean
    
    /**
     * Check if a session is "read-only" (older than previous session).
     */
    suspend fun isReadOnlySession(sessionId: Long): Boolean
}


