package com.navoditpublic.fees.domain.session

import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.usecase.SessionAccessLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Information about the currently selected session for viewing.
 */
data class SelectedSessionInfo(
    val session: AcademicSession,
    val accessLevel: SessionAccessLevel,
    val isCurrentSession: Boolean,
    val isPreviousSession: Boolean
) {
    /**
     * Whether this is a historical session (not the current session).
     * Used to determine if "Currently Studying" / "Left" badges should be shown.
     */
    val isHistoricalSession: Boolean
        get() = !isCurrentSession
    
    /**
     * User-friendly description of the session type.
     */
    val sessionTypeLabel: String
        get() = when {
            isCurrentSession -> "Current"
            isPreviousSession -> "Previous"
            else -> "Read-Only"
        }
}

/**
 * Singleton manager that tracks which session the user is currently viewing.
 * 
 * This is separate from "current session" (the active business session).
 * - Current Session: The session where new data is created (e.g., 2025-26)
 * - Selected Session: The session the user is viewing (could be 2024-25 for historical data)
 * 
 * When the app starts, selected session = current session.
 * User can switch to view historical sessions via the Academic Sessions screen.
 */
@Singleton
class SelectedSessionManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    private val _selectedSessionInfo = MutableStateFlow<SelectedSessionInfo?>(null)
    val selectedSessionInfo: StateFlow<SelectedSessionInfo?> = _selectedSessionInfo.asStateFlow()
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    /**
     * Get the currently selected session ID.
     * Returns null if not initialized.
     */
    val selectedSessionId: Long?
        get() = _selectedSessionInfo.value?.session?.id
    
    /**
     * Check if currently viewing the current (active) session.
     */
    val isViewingCurrentSession: Boolean
        get() = _selectedSessionInfo.value?.isCurrentSession == true
    
    /**
     * Initialize with the current session.
     * Should be called when app starts.
     */
    suspend fun initialize() {
        if (_isInitialized.value) return
        
        val currentSession = settingsRepository.getCurrentSession()
        if (currentSession != null) {
            _selectedSessionInfo.value = SelectedSessionInfo(
                session = currentSession,
                accessLevel = SessionAccessLevel.FULL_ACCESS,
                isCurrentSession = true,
                isPreviousSession = false
            )
        }
        _isInitialized.value = true
    }
    
    /**
     * Select a session for viewing.
     * This updates the selected session and determines its access level.
     * 
     * @param sessionId The session to select
     * @return SelectedSessionInfo if successful, null if session not found
     */
    suspend fun selectSession(sessionId: Long): SelectedSessionInfo? {
        val session = settingsRepository.getSessionById(sessionId) ?: return null
        val currentSession = settingsRepository.getCurrentSession()
        
        val isCurrentSession = currentSession?.id == sessionId
        val isPreviousSession = if (!isCurrentSession) {
            settingsRepository.isPreviousSession(sessionId)
        } else {
            false
        }
        
        val accessLevel = when {
            isCurrentSession -> SessionAccessLevel.FULL_ACCESS
            isPreviousSession -> SessionAccessLevel.PREVIOUS_SESSION
            else -> SessionAccessLevel.READ_ONLY
        }
        
        val info = SelectedSessionInfo(
            session = session,
            accessLevel = accessLevel,
            isCurrentSession = isCurrentSession,
            isPreviousSession = isPreviousSession
        )
        
        _selectedSessionInfo.value = info
        return info
    }
    
    /**
     * Reset to the current session.
     * This is typically called after session selection confirmation dialogs
     * or when navigating back from historical session view.
     */
    suspend fun selectCurrentSession(): SelectedSessionInfo? {
        val currentSession = settingsRepository.getCurrentSession() ?: return null
        return selectSession(currentSession.id)
    }
    
    /**
     * Force refresh the selected session info.
     * Useful after session promotion/revert when session data may have changed.
     */
    suspend fun refresh() {
        val currentSelectedId = _selectedSessionInfo.value?.session?.id
        if (currentSelectedId != null) {
            selectSession(currentSelectedId)
        } else {
            selectCurrentSession()
        }
    }
    
    /**
     * Check if session creation is allowed.
     * New sessions can only be created from March of the current session's final year onwards.
     * 
     * @return Pair<Boolean, String?> - (canCreate, errorMessage if not allowed)
     */
    suspend fun canCreateNewSession(): Pair<Boolean, String?> {
        val currentSession = settingsRepository.getCurrentSession()
            ?: return true to null // No current session, allow creation
        
        val calendar = java.util.Calendar.getInstance()
        val currentMonth = calendar.get(java.util.Calendar.MONTH)
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        
        // Extract end year from session name (e.g., "2025-26" -> 26 -> 2026)
        val parts = currentSession.sessionName.split("-")
        if (parts.size != 2) return true to null // Can't parse, allow
        
        val endYearShort = parts[1].toIntOrNull() ?: return true to null
        val sessionEndYear = 2000 + endYearShort
        
        // Allow from March of the session's final year onwards
        // Session 2025-26 ends March 2026, so allow from March 2026
        val canCreate = (currentYear > sessionEndYear) || 
                       (currentYear == sessionEndYear && currentMonth >= java.util.Calendar.MARCH)
        
        return if (canCreate) {
            true to null
        } else {
            false to "New sessions can only be created from March $sessionEndYear onwards."
        }
    }
    
    /**
     * Generate suggested session name based on current session.
     * E.g., if current is "2025-26", suggest "2026-27"
     */
    suspend fun suggestNextSessionName(): String? {
        val currentSession = settingsRepository.getCurrentSession() ?: return null
        
        val parts = currentSession.sessionName.split("-")
        if (parts.size != 2) return null
        
        val startYear = parts[0].toIntOrNull() ?: return null
        val endYear = parts[1].toIntOrNull() ?: return null
        
        return "${startYear + 1}-${(endYear + 1).toString().takeLast(2)}"
    }
}
