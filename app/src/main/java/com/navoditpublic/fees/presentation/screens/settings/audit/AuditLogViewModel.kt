package com.navoditpublic.fees.presentation.screens.settings.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.AuditLog
import com.navoditpublic.fees.domain.repository.AuditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuditLogState(
    val isLoading: Boolean = true,
    val logs: List<AuditLog> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AuditLogViewModel @Inject constructor(
    private val auditRepository: AuditRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AuditLogState())
    val state: StateFlow<AuditLogState> = _state.asStateFlow()
    
    init {
        loadLogs()
    }
    
    private fun loadLogs() {
        viewModelScope.launch {
            auditRepository.getAllLogs().collect { logs ->
                _state.value = AuditLogState(
                    isLoading = false,
                    logs = logs
                )
            }
        }
    }
}


