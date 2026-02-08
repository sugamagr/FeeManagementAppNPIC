package com.navoditpublic.fees.presentation.screens.ledger.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.domain.session.SelectedSessionInfo
import com.navoditpublic.fees.domain.session.SelectedSessionManager
import com.navoditpublic.fees.util.ClassUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassLedgerSummary(
    val className: String,
    val studentCount: Int,
    val totalDues: Double,
    val totalPaid: Double,
    val studentsWithDues: Int
)

data class LedgerMainState(
    val isLoading: Boolean = true,
    val sessionName: String = "",
    val classSummaries: List<ClassLedgerSummary> = emptyList(),
    val totalStudents: Int = 0,
    val totalDues: Double = 0.0,
    val totalCollected: Double = 0.0,
    val error: String? = null,
    // Session viewing state
    val selectedSessionInfo: SelectedSessionInfo? = null,
    val isViewingCurrentSession: Boolean = true
)

@HiltViewModel
class LedgerMainViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository,
    private val selectedSessionManager: SelectedSessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(LedgerMainState())
    val state: StateFlow<LedgerMainState> = _state.asStateFlow()
    
    init {
        loadData()
        observeSelectedSession()
    }
    
    private fun observeSelectedSession() {
        viewModelScope.launch {
            selectedSessionManager.selectedSessionInfo.collect { sessionInfo ->
                val isViewingCurrent = sessionInfo?.isCurrentSession ?: true
                _state.value = _state.value.copy(
                    selectedSessionInfo = sessionInfo,
                    isViewingCurrentSession = isViewingCurrent
                )
                // Reload data when session changes
                if (sessionInfo != null) {
                    loadData()
                }
            }
        }
    }
    
    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val selectedInfo = selectedSessionManager.selectedSessionInfo.value
                val isViewingCurrent = selectedInfo?.isCurrentSession ?: true
                val displaySession = selectedInfo?.session ?: settingsRepository.getCurrentSession()
                
                // For historical sessions, get students who had entries in that session
                // For current session, get all active students
                val allStudents = if (isViewingCurrent || selectedInfo == null) {
                    studentRepository.getAllActiveStudents().first()
                } else {
                    val studentIds = feeRepository.getStudentIdsWithEntriesInSession(selectedInfo.session.id)
                    studentRepository.getStudentsByIds(studentIds)
                }
                
                // Group students by class
                val studentsByClass = allStudents.groupBy { it.currentClass }
                
                val summaries = mutableListOf<ClassLedgerSummary>()
                var grandTotalDues = 0.0
                var grandTotalPaid = 0.0
                var totalWithDues = 0
                
                for (className in ClassUtils.ALL_CLASSES) {
                    val students = studentsByClass[className] ?: continue
                    if (students.isEmpty()) continue
                    
                    var classDues = 0.0
                    var classPaid = 0.0
                    var classStudentsWithDues = 0
                    
                    for (student in students) {
                        // Ledger balance includes all fees (opening balance, tuition, transport, admission) minus payments
                        val totalDue = feeRepository.getCurrentBalance(student.id)
                        val paid = feeRepository.getTotalCredits(student.id)
                        
                        // Only count positive dues (exclude overpayments/advances)
                        if (totalDue > 0) {
                            classDues += totalDue
                            classStudentsWithDues++
                        }
                        classPaid += paid
                    }
                    
                    summaries.add(
                        ClassLedgerSummary(
                            className = className,
                            studentCount = students.size,
                            totalDues = classDues,
                            totalPaid = classPaid,
                            studentsWithDues = classStudentsWithDues
                        )
                    )
                    
                    grandTotalDues += classDues
                    grandTotalPaid += classPaid
                    totalWithDues += classStudentsWithDues
                }
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    sessionName = displaySession?.sessionName ?: "Current Session",
                    classSummaries = summaries,
                    totalStudents = allStudents.size,
                    totalDues = grandTotalDues,
                    totalCollected = grandTotalPaid,
                    error = null,
                    selectedSessionInfo = selectedInfo,
                    isViewingCurrentSession = isViewingCurrent
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Switch back to viewing the current session.
     */
    fun switchToCurrentSession() {
        viewModelScope.launch {
            selectedSessionManager.selectCurrentSession()
        }
    }
}

