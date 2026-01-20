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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val error: String? = null,
    
    // Status management state
    val canDelete: Boolean = false,
    val showInactiveDialog: Boolean = false,
    val showReactivateDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showCannotDeleteDialog: Boolean = false,
    val isProcessing: Boolean = false
)

sealed class StudentDetailEvent {
    data object StudentDeleted : StudentDetailEvent()
    data class ShowToast(val message: String) : StudentDetailEvent()
}

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
    
    private val _events = MutableSharedFlow<StudentDetailEvent>()
    val events: SharedFlow<StudentDetailEvent> = _events.asSharedFlow()
    
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
                
                val canDeleteDeferred = async {
                    feeRepository.canDeleteStudent(studentId)
                }
                
                // Await all results
                val transportRoute = transportRouteDeferred.await()
                val admissionSession = admissionSessionDeferred.await()
                val admissionSessionName = admissionSession?.sessionName ?: ""
                val balance = balanceDeferred.await()
                val totalDebits = totalDebitsDeferred.await()
                val totalCredits = totalCreditsDeferred.await()
                val canDelete = canDeleteDeferred.await()
                
                _state.value = StudentDetailState(
                    isLoading = false,
                    student = student,
                    currentBalance = balance,
                    totalDebits = totalDebits,
                    totalCredits = totalCredits,
                    transportRoute = transportRoute,
                    admissionSessionName = admissionSessionName,
                    recentReceipts = emptyList(),
                    recentLedgerEntries = emptyList(),
                    error = null,
                    canDelete = canDelete
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
    
    // ========== Dialog Controls ==========
    
    fun showInactiveDialog() {
        _state.value = _state.value.copy(showInactiveDialog = true)
    }
    
    fun dismissInactiveDialog() {
        _state.value = _state.value.copy(showInactiveDialog = false)
    }
    
    fun showReactivateDialog() {
        _state.value = _state.value.copy(showReactivateDialog = true)
    }
    
    fun dismissReactivateDialog() {
        _state.value = _state.value.copy(showReactivateDialog = false)
    }
    
    fun showDeleteDialog() {
        if (_state.value.canDelete) {
            _state.value = _state.value.copy(showDeleteDialog = true)
        } else {
            _state.value = _state.value.copy(showCannotDeleteDialog = true)
        }
    }
    
    fun dismissDeleteDialog() {
        _state.value = _state.value.copy(showDeleteDialog = false)
    }
    
    fun dismissCannotDeleteDialog() {
        _state.value = _state.value.copy(showCannotDeleteDialog = false)
    }
    
    // ========== Actions ==========
    
    fun markInactive() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true, showInactiveDialog = false)
            
            studentRepository.markInactive(studentId)
                .onSuccess {
                    _events.emit(StudentDetailEvent.ShowToast("Student marked as inactive"))
                    loadStudentDetails()
                }
                .onFailure { e ->
                    _events.emit(StudentDetailEvent.ShowToast("Failed: ${e.message}"))
                }
            
            _state.value = _state.value.copy(isProcessing = false)
        }
    }
    
    fun reactivate() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true, showReactivateDialog = false)
            
            studentRepository.reactivate(studentId)
                .onSuccess {
                    _events.emit(StudentDetailEvent.ShowToast("Student reactivated"))
                    loadStudentDetails()
                }
                .onFailure { e ->
                    _events.emit(StudentDetailEvent.ShowToast("Failed: ${e.message}"))
                }
            
            _state.value = _state.value.copy(isProcessing = false)
        }
    }
    
    fun deleteStudent() {
        val student = _state.value.student ?: return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true, showDeleteDialog = false)
            
            // Re-verify canDelete before proceeding (race condition safeguard)
            val canStillDelete = feeRepository.canDeleteStudent(studentId)
            if (!canStillDelete) {
                _events.emit(StudentDetailEvent.ShowToast("Cannot delete: Student now has financial records"))
                _state.value = _state.value.copy(isProcessing = false, canDelete = false)
                return@launch
            }
            
            studentRepository.hardDelete(student)
                .onSuccess {
                    _events.emit(StudentDetailEvent.ShowToast("Student deleted permanently"))
                    _events.emit(StudentDetailEvent.StudentDeleted)
                }
                .onFailure { e ->
                    _events.emit(StudentDetailEvent.ShowToast("Failed: ${e.message}"))
                }
            
            _state.value = _state.value.copy(isProcessing = false)
        }
    }
}


