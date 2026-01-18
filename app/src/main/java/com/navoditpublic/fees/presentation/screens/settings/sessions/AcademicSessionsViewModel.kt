package com.navoditpublic.fees.presentation.screens.settings.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AcademicSessionsState(
    val isLoading: Boolean = true,
    val sessions: List<AcademicSession> = emptyList(),
    val inactiveSessions: List<AcademicSession> = emptyList(),
    val error: String? = null,
    // For fee addition dialog
    val showAddFeesDialog: Boolean = false,
    val newSessionId: Long? = null,
    val newSessionName: String = "",
    val isAddingFees: Boolean = false,
    // For showing inactive sessions
    val showInactiveSessions: Boolean = false
)

sealed class SessionEvent {
    data class Success(val message: String) : SessionEvent()
    data class Error(val message: String) : SessionEvent()
    data class FeesAdded(val studentCount: Int, val totalFees: Double) : SessionEvent()
}

@HiltViewModel
class AcademicSessionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val feeRepository: FeeRepository
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
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    sessions = activeSessions,
                    inactiveSessions = inactiveSessions
                )
            }
        }
    }
    
    fun toggleShowInactiveSessions() {
        _state.value = _state.value.copy(
            showInactiveSessions = !_state.value.showInactiveSessions
        )
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
                val endYearShort = parts[1].toInt()
                // Simple logic: end year is always start year + 1 for academic sessions
                // e.g., 2025-26 means 2025 to 2026, 2099-00 means 2099 to 2100
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
