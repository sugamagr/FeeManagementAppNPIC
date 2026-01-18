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
import kotlinx.coroutines.async
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
                
                // Parallelize all independent database calls using async
                val transportRouteDeferred = async {
                    student.transportRouteId?.let { settingsRepository.getRouteById(it) }
                }
                
                val admissionSessionDeferred = async {
                    settingsRepository.getSessionById(student.admissionSessionId)
                }
                
                val balanceDeferred = async {
                    feeRepository.getCurrentBalance(studentId)
                }
                
                val totalDebitsDeferred = async {
                    feeRepository.getTotalDebits(studentId)
                }
                
                val totalCreditsDeferred = async {
                    feeRepository.getTotalCredits(studentId)
                }
                
                // Await all results
                val transportRoute = transportRouteDeferred.await()
                val admissionSession = admissionSessionDeferred.await()
                val admissionSessionName = admissionSession?.sessionName ?: ""
                val balance = balanceDeferred.await()
                val totalDebits = totalDebitsDeferred.await()
                val totalCredits = totalCreditsDeferred.await()
                
                _state.value = StudentDetailState(
                    isLoading = false,
                    student = student,
                    currentBalance = balance,
                    totalDebits = totalDebits,
                    totalCredits = totalCredits,
                    transportRoute = transportRoute,
                    admissionSessionName = admissionSessionName,
                    recentReceipts = emptyList(), // Removed - not displayed on screen
                    recentLedgerEntries = emptyList(), // Removed - not displayed on screen
                    error = null
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


