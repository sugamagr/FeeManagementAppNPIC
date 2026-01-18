package com.navoditpublic.fees.presentation.screens.ledger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.LedgerEntry
import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.model.TransportRoute
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentLedgerState(
    val isLoading: Boolean = true,
    val student: Student? = null,
    val entries: List<LedgerEntry> = emptyList(),
    val currentBalance: Double = 0.0,
    val totalDebits: Double = 0.0,
    val totalCredits: Double = 0.0,
    val transportRoute: TransportRoute? = null,
    val error: String? = null
)

@HiltViewModel
class StudentLedgerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val studentId: Long = savedStateHandle.get<Long>("studentId") ?: 0L
    
    private val _state = MutableStateFlow(StudentLedgerState())
    val state: StateFlow<StudentLedgerState> = _state.asStateFlow()
    
    init {
        loadLedger()
    }
    
    private fun loadLedger() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                val student = studentRepository.getById(studentId)
                
                // Load transport route if student has transport
                val transportRoute = if (student?.hasTransport == true && student.transportRouteId != null) {
                    settingsRepository.getRouteById(student.transportRouteId)
                } else null
                
                // Combine ledger entries with reactive balance for real-time updates
                combine(
                    feeRepository.getLedgerForStudent(studentId),
                    feeRepository.getCurrentBalanceFlow(studentId)
                ) { entries, balance ->
                    // Calculate totals
                    val totalDebits = entries.sumOf { it.debitAmount }
                    val totalCredits = entries.sumOf { it.creditAmount }
                    
                    StudentLedgerState(
                        isLoading = false,
                        student = student,
                        entries = entries,
                        currentBalance = balance,
                        totalDebits = totalDebits,
                        totalCredits = totalCredits,
                        transportRoute = transportRoute,
                        error = null
                    )
                }.collect { state ->
                    _state.value = state
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
