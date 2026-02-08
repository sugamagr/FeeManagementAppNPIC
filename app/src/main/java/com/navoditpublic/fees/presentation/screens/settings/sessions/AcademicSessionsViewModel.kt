package com.navoditpublic.fees.presentation.screens.settings.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.model.PromotionOptions
import com.navoditpublic.fees.domain.model.PromotionPreview
import com.navoditpublic.fees.domain.model.PromotionProgress
import com.navoditpublic.fees.domain.model.PromotionResult
import com.navoditpublic.fees.domain.model.RevertResult
import com.navoditpublic.fees.domain.model.RevertSafetyCheck
import com.navoditpublic.fees.domain.model.SessionPromotion
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.domain.session.SelectedSessionManager
import com.navoditpublic.fees.domain.usecase.RevertSessionPromotionUseCase
import com.navoditpublic.fees.domain.usecase.SessionAccessLevel
import com.navoditpublic.fees.domain.usecase.SessionPromotionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Statistics for a session
 */
data class SessionStats(
    val sessionId: Long,
    val studentsCount: Int = 0,
    val totalFees: Double = 0.0,
    val collected: Double = 0.0,
    val pending: Double = 0.0,
    val collectionRate: Float = 0f
)

data class AcademicSessionsState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val sessions: List<AcademicSession> = emptyList(),
    val inactiveSessions: List<AcademicSession> = emptyList(),
    val sessionStats: Map<Long, SessionStats> = emptyMap(),
    val sessionPromotions: Map<Long, SessionPromotion> = emptyMap(), // Keyed by target session ID
    val error: String? = null,
    
    // Search & Filter
    val searchQuery: String = "",
    val filteredSessions: List<AcademicSession> = emptyList(),
    
    // Animation trigger
    val animateItems: Boolean = false,
    
    // Session Selection (for viewing historical data)
    val showSessionSelectionDialog: Boolean = false,
    val sessionToSelect: AcademicSession? = null,
    val sessionSelectionAccessLevel: SessionAccessLevel = SessionAccessLevel.FULL_ACCESS,
    
    // Session Creation Restriction
    val canCreateNewSession: Boolean = false,
    val createSessionBlockedMessage: String? = null,
    
    // Session Promotion
    val showPromotionWizard: Boolean = false,
    val sourceSessionId: Long? = null,
    val targetSessionId: Long? = null,
    val promotionPreview: PromotionPreview? = null,
    val promotionProgress: PromotionProgress? = null,
    val promotionResult: PromotionResult? = null,
    val isPromoting: Boolean = false,
    
    // Session Revert
    val showRevertDialog: Boolean = false,
    val promotionToRevert: SessionPromotion? = null,
    val revertSafetyCheck: RevertSafetyCheck? = null,
    val revertProgress: PromotionProgress? = null,
    val revertResult: RevertResult? = null,
    val isReverting: Boolean = false
)

sealed class SessionEvent {
    data class Success(val message: String) : SessionEvent()
    data class Error(val message: String) : SessionEvent()
    data class PromotionComplete(val result: PromotionResult) : SessionEvent()
    data class RevertComplete(val result: RevertResult) : SessionEvent()
    data class SessionSelected(val session: AcademicSession) : SessionEvent()
}

@HiltViewModel
class AcademicSessionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val feeRepository: FeeRepository,
    private val studentRepository: StudentRepository,
    private val sessionPromotionUseCase: SessionPromotionUseCase,
    private val revertSessionPromotionUseCase: RevertSessionPromotionUseCase,
    private val selectedSessionManager: SelectedSessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(AcademicSessionsState())
    val state: StateFlow<AcademicSessionsState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<SessionEvent>()
    val events: SharedFlow<SessionEvent> = _events.asSharedFlow()
    
    init {
        loadSessions()
        checkCanCreateNewSession()
    }
    
    private fun checkCanCreateNewSession() {
        viewModelScope.launch {
            val (canCreate, blockedMessage) = selectedSessionManager.canCreateNewSession()
            _state.value = _state.value.copy(
                canCreateNewSession = canCreate,
                createSessionBlockedMessage = blockedMessage
            )
        }
    }
    
    private fun loadSessions() {
        viewModelScope.launch {
            settingsRepository.getAllSessions().collect { allSessions ->
                val activeSessions = allSessions.filter { it.isActive }
                val inactiveSessions = allSessions.filter { !it.isActive }
                
                // Load stats for each session
                val statsMap = mutableMapOf<Long, SessionStats>()
                activeSessions.forEach { session ->
                    statsMap[session.id] = loadSessionStats(session.id)
                }
                
                val filtered = if (_state.value.searchQuery.isBlank()) {
                    activeSessions
                } else {
                    activeSessions.filter { 
                        it.sessionName.contains(_state.value.searchQuery, ignoreCase = true) 
                    }
                }
                
                // Load promotions for all sessions
                val promotions = mutableMapOf<Long, SessionPromotion>()
                allSessions.forEach { session ->
                    settingsRepository.getPromotionForSession(session.id)?.let {
                        promotions[session.id] = it
                    }
                }
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    sessions = activeSessions,
                    filteredSessions = filtered,
                    inactiveSessions = inactiveSessions,
                    sessionStats = statsMap,
                    sessionPromotions = promotions,
                    animateItems = true
                )
            }
        }
    }
    
    private suspend fun loadSessionStats(sessionId: Long): SessionStats {
        return try {
            val studentCount = studentRepository.getActiveStudentCount().first()
            val totalPending = feeRepository.getTotalPendingDues().first() ?: 0.0
            
            // Get session collection total
            val session = settingsRepository.getSessionById(sessionId)
            val collected = if (session != null) {
                feeRepository.getCollectionForPeriod(sessionId, session.startDate, session.endDate).first() ?: 0.0
            } else 0.0
            
            val totalFees = collected + totalPending
            val rate = if (totalFees > 0) ((collected / totalFees) * 100).toFloat() else 0f
            
            SessionStats(
                sessionId = sessionId,
                studentsCount = studentCount,
                totalFees = totalFees,
                collected = collected,
                pending = totalPending,
                collectionRate = rate.coerceIn(0f, 100f)
            )
        } catch (e: Exception) {
            SessionStats(sessionId = sessionId)
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            loadSessions()
        }
    }
    
    fun onSearchQueryChange(query: String) {
        val filtered = if (query.isBlank()) {
            _state.value.sessions
        } else {
            _state.value.sessions.filter { 
                it.sessionName.contains(query, ignoreCase = true) 
            }
        }
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredSessions = filtered
        )
    }
    
    fun clearSearch() {
        _state.value = _state.value.copy(
            searchQuery = "",
            filteredSessions = _state.value.sessions
        )
    }
    
    /**
     * Add the first session. This is only used when no sessions exist.
     * After the first session, new sessions are created through migration.
     */
    fun addFirstSession(sessionName: String) {
        viewModelScope.launch {
            try {
                // Check if active sessions already exist - only allow if no active sessions
                // (inactive sessions from legacy data are ignored)
                if (_state.value.sessions.isNotEmpty()) {
                    _events.emit(SessionEvent.Error("Active sessions already exist. Use migration to create new sessions."))
                    return@launch
                }
                
                // Validate format (e.g., "2024-25")
                val regex = Regex("^\\d{4}-\\d{2}$")
                if (!regex.matches(sessionName)) {
                    _events.emit(SessionEvent.Error("Invalid format. Use: 2024-25"))
                    return@launch
                }
                
                // Check if exists
                if (settingsRepository.sessionNameExists(sessionName)) {
                    _events.emit(SessionEvent.Error("Session already exists"))
                    return@launch
                }
                
                // Parse years
                val parts = sessionName.split("-")
                val startYear = parts[0].toInt()
                val endYear = startYear + 1
                
                // Create dates (April 1 to March 31)
                val startCal = Calendar.getInstance().apply {
                    set(startYear, Calendar.APRIL, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                val endCal = Calendar.getInstance().apply {
                    set(endYear, Calendar.MARCH, 31, 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                
                val session = AcademicSession(
                    sessionName = sessionName,
                    startDate = startCal.timeInMillis,
                    endDate = endCal.timeInMillis,
                    isCurrent = true // First session is always current
                )
                
                settingsRepository.insertSession(session).onSuccess {
                    _events.emit(SessionEvent.Success("First session created: $sessionName"))
                }.onFailure { e ->
                    _events.emit(SessionEvent.Error(e.message ?: "Failed to create session"))
                }
            } catch (e: Exception) {
                _events.emit(SessionEvent.Error(e.message ?: "Invalid session format"))
            }
        }
    }
    
    // NOTE: setCurrentSession removed - sessions can only change via promotion/migration
    
    // ========== Session Selection Methods (for viewing historical data) ==========
    
    /**
     * Called when user clicks on a non-current session.
     * Shows confirmation dialog before switching view.
     */
    fun onSessionClick(session: AcademicSession) {
        if (session.isCurrent) {
            // Already viewing current session, no action needed
            return
        }
        
        viewModelScope.launch {
            // Determine access level for this session
            val isPrevious = settingsRepository.isPreviousSession(session.id)
            val accessLevel = when {
                isPrevious -> SessionAccessLevel.PREVIOUS_SESSION
                else -> SessionAccessLevel.READ_ONLY
            }
            
            _state.value = _state.value.copy(
                showSessionSelectionDialog = true,
                sessionToSelect = session,
                sessionSelectionAccessLevel = accessLevel
            )
        }
    }
    
    /**
     * Called when user confirms session selection from the dialog.
     */
    fun confirmSessionSelection() {
        viewModelScope.launch {
            val session = _state.value.sessionToSelect ?: return@launch
            
            // Select the session in the manager
            selectedSessionManager.selectSession(session.id)
            
            _state.value = _state.value.copy(
                showSessionSelectionDialog = false,
                sessionToSelect = null
            )
            
            _events.emit(SessionEvent.SessionSelected(session))
        }
    }
    
    /**
     * Called when user dismisses the session selection dialog.
     */
    fun dismissSessionSelectionDialog() {
        _state.value = _state.value.copy(
            showSessionSelectionDialog = false,
            sessionToSelect = null
        )
    }
    
    /**
     * Switch back to viewing the current session.
     */
    fun selectCurrentSession() {
        viewModelScope.launch {
            selectedSessionManager.selectCurrentSession()
            _events.emit(SessionEvent.Success("Switched to current session"))
        }
    }
    
    // ========== Session Promotion Methods ==========
    
    /**
     * Open promotion wizard with source and target session.
     * Called when user wants to promote from one session to another.
     */
    fun openPromotionWizard(sourceSession: AcademicSession, targetSession: AcademicSession) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                showPromotionWizard = true,
                sourceSessionId = sourceSession.id,
                targetSessionId = targetSession.id,
                promotionPreview = null,
                promotionProgress = null,
                promotionResult = null,
                isPromoting = false
            )
            
            // Load preview data
            loadPromotionPreview(sourceSession.id)
        }
    }
    
    /**
     * Open promotion wizard to create and promote to a new session.
     */
    fun startPromotionToNewSession(sourceSession: AcademicSession) {
        viewModelScope.launch {
            // Auto-generate next session name (e.g., 2024-25 -> 2025-26)
            val parts = sourceSession.sessionName.split("-")
            if (parts.size == 2) {
                try {
                    val startYear = parts[0].toInt() + 1
                    val endYearSuffix = "%02d".format((startYear + 1) % 100)
                    val newSessionName = "$startYear-$endYearSuffix"
                    
                    // Check if session already exists
                    if (settingsRepository.sessionNameExists(newSessionName)) {
                        // If exists, find it and open wizard with it
                        val sessions = settingsRepository.getAllSessions().first()
                        val targetSession = sessions.find { it.sessionName == newSessionName }
                        if (targetSession != null) {
                            openPromotionWizard(sourceSession, targetSession)
                        } else {
                            _events.emit(SessionEvent.Error("Session $newSessionName exists but couldn't be found"))
                        }
                        return@launch
                    }
                    
                    // Create dates (April 1 to March 31)
                    val startCal = Calendar.getInstance().apply {
                        set(startYear, Calendar.APRIL, 1, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    
                    val endCal = Calendar.getInstance().apply {
                        set(startYear + 1, Calendar.MARCH, 31, 23, 59, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    
                    val newSession = AcademicSession(
                        sessionName = newSessionName,
                        startDate = startCal.timeInMillis,
                        endDate = endCal.timeInMillis,
                        isCurrent = false
                    )
                    
                    settingsRepository.insertSession(newSession).onSuccess { newSessionId ->
                        // Open wizard with the new session
                        val targetSession = newSession.copy(id = newSessionId)
                        _state.value = _state.value.copy(
                            showPromotionWizard = true,
                            sourceSessionId = sourceSession.id,
                            targetSessionId = newSessionId,
                            promotionPreview = null,
                            promotionProgress = null,
                            promotionResult = null,
                            isPromoting = false
                        )
                        loadPromotionPreview(sourceSession.id)
                    }.onFailure { e ->
                        _events.emit(SessionEvent.Error(e.message ?: "Failed to create new session"))
                    }
                } catch (e: Exception) {
                    _events.emit(SessionEvent.Error("Could not parse session name"))
                }
            }
        }
    }
    
    private suspend fun loadPromotionPreview(sourceSessionId: Long) {
        try {
            val preview = sessionPromotionUseCase.getPromotionPreview(sourceSessionId)
            _state.value = _state.value.copy(promotionPreview = preview)
        } catch (e: Exception) {
            _events.emit(SessionEvent.Error("Failed to load preview: ${e.message}"))
        }
    }
    
    fun dismissPromotionWizard() {
        _state.value = _state.value.copy(
            showPromotionWizard = false,
            sourceSessionId = null,
            targetSessionId = null,
            promotionPreview = null,
            promotionProgress = null,
            promotionResult = null,
            isPromoting = false
        )
    }
    
    fun executePromotion(options: PromotionOptions) {
        viewModelScope.launch {
            val sourceId = _state.value.sourceSessionId ?: return@launch
            val targetId = _state.value.targetSessionId ?: return@launch
            
            _state.value = _state.value.copy(isPromoting = true)
            
            sessionPromotionUseCase.execute(
                sourceSessionId = sourceId,
                targetSessionId = targetId,
                options = options,
                onProgress = { progress ->
                    // Ensure state update happens on main thread
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        _state.value = _state.value.copy(promotionProgress = progress)
                    }
                }
            ).onSuccess { result ->
                _state.value = _state.value.copy(
                    isPromoting = false,
                    promotionResult = result
                )
                _events.emit(SessionEvent.PromotionComplete(result))
            }.onFailure { e ->
                // Preserve the progress percentage to show user how far we got
                val lastProgress = _state.value.promotionProgress?.percentComplete ?: 0
                _state.value = _state.value.copy(
                    isPromoting = false,
                    promotionProgress = PromotionProgress(
                        currentStep = "Failed",
                        percentComplete = lastProgress,
                        error = e.message ?: "An unexpected error occurred"
                    )
                )
                _events.emit(SessionEvent.Error("Promotion failed: ${e.message}"))
            }
        }
    }
    
    // ========== Session Revert Methods ==========
    
    /**
     * Open revert dialog for a session that was created via promotion.
     */
    fun openRevertDialog(session: AcademicSession) {
        viewModelScope.launch {
            val promotion = settingsRepository.getPromotionForSession(session.id)
            if (promotion == null) {
                _events.emit(SessionEvent.Error("This session was not created via promotion"))
                return@launch
            }
            
            _state.value = _state.value.copy(
                showRevertDialog = true,
                promotionToRevert = promotion,
                revertSafetyCheck = null,
                revertProgress = null,
                revertResult = null,
                isReverting = false
            )
            
            // Check safety
            loadRevertSafetyCheck(promotion)
        }
    }
    
    private suspend fun loadRevertSafetyCheck(promotion: SessionPromotion) {
        try {
            val safetyCheck = revertSessionPromotionUseCase.checkRevertSafety(promotion)
            _state.value = _state.value.copy(revertSafetyCheck = safetyCheck)
        } catch (e: Exception) {
            _events.emit(SessionEvent.Error("Failed to check safety: ${e.message}"))
        }
    }
    
    fun dismissRevertDialog() {
        _state.value = _state.value.copy(
            showRevertDialog = false,
            promotionToRevert = null,
            revertSafetyCheck = null,
            revertProgress = null,
            revertResult = null,
            isReverting = false
        )
    }
    
    fun executeRevert(forceDelete: Boolean, reason: String?) {
        viewModelScope.launch {
            val promotion = _state.value.promotionToRevert ?: return@launch
            
            _state.value = _state.value.copy(isReverting = true)
            
            revertSessionPromotionUseCase.execute(
                promotion = promotion,
                forceDelete = forceDelete,
                reason = reason,
                onProgress = { progress ->
                    // Ensure state update happens on main thread
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        _state.value = _state.value.copy(revertProgress = progress)
                    }
                }
            ).onSuccess { result ->
                _state.value = _state.value.copy(
                    isReverting = false,
                    revertResult = result
                )
                _events.emit(SessionEvent.RevertComplete(result))
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isReverting = false,
                    revertProgress = PromotionProgress(
                        currentStep = "Error",
                        percentComplete = 0,
                        error = e.message
                    )
                )
                _events.emit(SessionEvent.Error("Revert failed: ${e.message}"))
            }
        }
    }
    
    /**
     * Check if a session was promoted and can be reverted.
     */
    fun getPromotionForSession(sessionId: Long): SessionPromotion? {
        return _state.value.sessionPromotions[sessionId]
    }
}
