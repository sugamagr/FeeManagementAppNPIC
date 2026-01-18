package com.navoditpublic.fees.presentation.screens.students.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.LedgerEntry
import com.navoditpublic.fees.domain.model.Receipt
import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.model.TransportRoute
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentDetailState(
    val isLoading: Boolean = true,
    val student: Student? = null,
    val currentBalance: Double = 0.0,
    val totalDebits: Double = 0.0,
    val totalCredits: Double = 0.0,
    val transportRoute: TransportRoute? = null,
    val admissionSessionName: String = "",
    val recentReceipts: List<Receipt> = emptyList(),
    val recentLedgerEntries: List<LedgerEntry> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class StudentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val studentId: Long = savedStateHandle.get<Long>("studentId") ?: 0L
    
    private val _state = MutableStateFlow(StudentDetailState())
    val state: StateFlow<StudentDetailState> = _state.asStateFlow()
    
    init {
        loadStudentDetails()
    }
    
    private fun loadStudentDetails() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                val student = studentRepository.getById(studentId)
                if (student == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Student not found"
                    )
                    return@launch
                }
                
                // Load transport route if applicable
                val transportRoute = student.transportRouteId?.let { 
                    settingsRepository.getRouteById(it) 
                }
                
                // Load admission session name
                val admissionSession = settingsRepository.getSessionById(student.admissionSessionId)
                val admissionSessionName = admissionSession?.sessionName ?: ""
                
                // Get current session for proper dues calculation
                val currentSession = settingsRepository.getCurrentSession()
                val sessionId = currentSession?.id ?: 0L
                
                // Use ledger as single source of truth for all financial data
                val balance = feeRepository.getCurrentBalance(studentId) // Current dues (all fees minus payments)
                val totalDebits = feeRepository.getTotalDebits(studentId) // All fees charged
                val totalCredits = feeRepository.getTotalCredits(studentId) // All payments made
                
                // Load recent receipts
                val receipts = feeRepository.getReceiptsForStudent(studentId).first().take(5)
                
                // Load recent ledger entries
                val ledgerEntries = feeRepository.getLedgerForStudent(studentId).first().takeLast(10).reversed()
                
                _state.value = StudentDetailState(
                    isLoading = false,
                    student = student,
                    currentBalance = balance,
                    totalDebits = totalDebits,
                    totalCredits = totalCredits,
                    transportRoute = transportRoute,
                    admissionSessionName = admissionSessionName,
                    recentReceipts = receipts,
                    recentLedgerEntries = ledgerEntries,
                    error = null  // Clear any previous error
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun refresh() {
        loadStudentDetails()
    }
}


