package com.navoditpublic.fees.presentation.screens.settings.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
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
    val error: String? = null,
    
    // Search & Filter
    val searchQuery: String = "",
    val filteredSessions: List<AcademicSession> = emptyList(),
    
    // For fee addition dialog
    val showAddFeesDialog: Boolean = false,
    val newSessionId: Long? = null,
    val newSessionName: String = "",
    val isAddingFees: Boolean = false,
    
    // For showing inactive sessions
    val showInactiveSessions: Boolean = false,
    
    // For duplicate dialog
    val sessionToDuplicate: AcademicSession? = null,
    
    // Animation trigger
    val animateItems: Boolean = false
)

sealed class SessionEvent {
    data class Success(val message: String) : SessionEvent()
    data class Error(val message: String) : SessionEvent()
    data class FeesAdded(val studentCount: Int, val totalFees: Double) : SessionEvent()
}

@HiltViewModel
class AcademicSessionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val feeRepository: FeeRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AcademicSessionsState())
    val state: StateFlow<AcademicSessionsState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<SessionEvent>()
    val events: SharedFlow<SessionEvent> = _events.asSharedFlow()
    
    init {
        loadSessions()
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
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    sessions = activeSessions,
                    filteredSessions = filtered,
                    inactiveSessions = inactiveSessions,
                    sessionStats = statsMap,
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
    
    fun toggleShowInactiveSessions() {
        _state.value = _state.value.copy(
            showInactiveSessions = !_state.value.showInactiveSessions
        )
    }
    
    fun showDuplicateDialog(session: AcademicSession) {
        _state.value = _state.value.copy(sessionToDuplicate = session)
    }
    
    fun dismissDuplicateDialog() {
        _state.value = _state.value.copy(sessionToDuplicate = null)
    }
    
    fun duplicateSession(sourceSession: AcademicSession, newSessionName: String) {
        viewModelScope.launch {
            try {
                // Validate format
                val regex = Regex("^\\d{4}-\\d{2}$")
                if (!regex.matches(newSessionName)) {
                    _events.emit(SessionEvent.Error("Invalid format. Use: 2025-26"))
                    return@launch
                }
                
                // Check if exists
                if (settingsRepository.sessionNameExists(newSessionName)) {
                    _events.emit(SessionEvent.Error("Session already exists"))
                    return@launch
                }
                
                // Parse years and create new session
                val parts = newSessionName.split("-")
                val startYear = parts[0].toInt()
                val endYear = startYear + 1
                
                val startCal = Calendar.getInstance().apply {
                    set(startYear, Calendar.APRIL, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                val endCal = Calendar.getInstance().apply {
                    set(endYear, Calendar.MARCH, 31, 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                
                val newSession = AcademicSession(
                    sessionName = newSessionName,
                    startDate = startCal.timeInMillis,
                    endDate = endCal.timeInMillis,
                    isCurrent = false
                )
                
                settingsRepository.insertSession(newSession).onSuccess { newSessionId ->
                    _state.value = _state.value.copy(sessionToDuplicate = null)
                    _events.emit(SessionEvent.Success("Session duplicated: $newSessionName"))
                    
                    // Show fees dialog for the new session
                    _state.value = _state.value.copy(
                        showAddFeesDialog = true,
                        newSessionId = newSessionId,
                        newSessionName = newSessionName
                    )
                }.onFailure { e ->
                    _events.emit(SessionEvent.Error(e.message ?: "Failed to duplicate session"))
                }
            } catch (e: Exception) {
                _events.emit(SessionEvent.Error(e.message ?: "Invalid session format"))
            }
        }
    }
    
    fun addSession(sessionName: String) {
        viewModelScope.launch {
            try {
                // Validate format (e.g., "2025-26")
                val regex = Regex("^\\d{4}-\\d{2}$")
                if (!regex.matches(sessionName)) {
                    _events.emit(SessionEvent.Error("Invalid format. Use: 2025-26"))
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
                    isCurrent = _state.value.sessions.isEmpty() // Make current if first
                )
                
                settingsRepository.insertSession(session).onSuccess { newSessionId ->
                    _events.emit(SessionEvent.Success("Session added"))
                    
                    // Show dialog to ask about adding fees for all students
                    _state.value = _state.value.copy(
                        showAddFeesDialog = true,
                        newSessionId = newSessionId,
                        newSessionName = sessionName
                    )
                }.onFailure { e ->
                    _events.emit(SessionEvent.Error(e.message ?: "Failed to add session"))
                }
            } catch (e: Exception) {
                _events.emit(SessionEvent.Error(e.message ?: "Invalid session format"))
            }
        }
    }
    
    fun dismissAddFeesDialog() {
        _state.value = _state.value.copy(
            showAddFeesDialog = false,
            newSessionId = null,
            newSessionName = ""
        )
    }
    
    fun addFeesForAllStudents(addTuition: Boolean, addTransport: Boolean) {
        viewModelScope.launch {
            val sessionId = _state.value.newSessionId ?: return@launch
            
            _state.value = _state.value.copy(isAddingFees = true)
            
            feeRepository.addSessionFeesForAllStudents(
                sessionId = sessionId,
                addTuition = addTuition,
                addTransport = addTransport
            ).onSuccess { (studentCount, totalFees) ->
                _state.value = _state.value.copy(
                    isAddingFees = false,
                    showAddFeesDialog = false,
                    newSessionId = null,
                    newSessionName = ""
                )
                _events.emit(SessionEvent.FeesAdded(studentCount, totalFees))
            }.onFailure { e ->
                _state.value = _state.value.copy(isAddingFees = false)
                _events.emit(SessionEvent.Error(e.message ?: "Failed to add fees"))
            }
        }
    }
    
    fun setCurrentSession(sessionId: Long) {
        viewModelScope.launch {
            settingsRepository.setCurrentSession(sessionId).onSuccess {
                _events.emit(SessionEvent.Success("Current session updated"))
            }.onFailure { e ->
                _events.emit(SessionEvent.Error(e.message ?: "Failed to update"))
            }
        }
    }
    
    fun deactivateSession(sessionId: Long) {
        viewModelScope.launch {
            // Check if it's the current session
            val session = _state.value.sessions.find { it.id == sessionId }
            if (session?.isCurrent == true) {
                _events.emit(SessionEvent.Error("Cannot deactivate current session. Set another session as current first."))
                return@launch
            }
            
            settingsRepository.setSessionActive(sessionId, false).onSuccess {
                _events.emit(SessionEvent.Success("Session moved to inactive"))
            }.onFailure { e ->
                _events.emit(SessionEvent.Error(e.message ?: "Failed to deactivate"))
            }
        }
    }
    
    fun reactivateSession(sessionId: Long) {
        viewModelScope.launch {
            settingsRepository.setSessionActive(sessionId, true).onSuccess {
                _events.emit(SessionEvent.Success("Session reactivated"))
            }.onFailure { e ->
                _events.emit(SessionEvent.Error(e.message ?: "Failed to reactivate"))
            }
        }
    }
    
    fun permanentlyDeleteSession(sessionId: Long) {
        viewModelScope.launch {
            // Only allow deletion of inactive sessions
            val session = _state.value.inactiveSessions.find { it.id == sessionId }
            if (session == null) {
                _events.emit(SessionEvent.Error("Only inactive sessions can be permanently deleted"))
                return@launch
            }
            
            settingsRepository.deleteSession(sessionId).onSuccess {
                _events.emit(SessionEvent.Success("Session permanently deleted"))
            }.onFailure { e ->
                _events.emit(SessionEvent.Error(e.message ?: "Failed to delete"))
            }
        }
    }
}
