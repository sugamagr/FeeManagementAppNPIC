package com.navoditpublic.fees.presentation.screens.ledger.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val error: String? = null
)

@HiltViewModel
class LedgerMainViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(LedgerMainState())
    val state: StateFlow<LedgerMainState> = _state.asStateFlow()
    
    private val classOrder = listOf(
        "NC", "LKG", "UKG", "1st", "2nd", "3rd", "4th", "5th", 
        "6th", "7th", "8th", "9th", "10th", "11th", "12th"
    )
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val currentSession = settingsRepository.getCurrentSession()
                val sessionId = currentSession?.id ?: 0L
                
                val allStudents = studentRepository.getAllActiveStudents().first()
                
                // Group students by class
                val studentsByClass = allStudents.groupBy { it.currentClass }
                
                val summaries = mutableListOf<ClassLedgerSummary>()
                var grandTotalDues = 0.0
                var grandTotalPaid = 0.0
                var totalWithDues = 0
                
                for (className in classOrder) {
                    val students = studentsByClass[className] ?: continue
                    if (students.isEmpty()) continue
                    
                    var classDues = 0.0
                    var classPaid = 0.0
                    var classStudentsWithDues = 0
                    
                    for (student in students) {
                        // Ledger balance includes all fees (opening balance, tuition, transport, admission) minus payments
                        val totalDue = feeRepository.getCurrentBalance(student.id)
                        val paid = feeRepository.getTotalCredits(student.id)
                        
                        classDues += totalDue
                        classPaid += paid
                        
                        if (totalDue > 0) {
                            classStudentsWithDues++
                        }
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
                    sessionName = currentSession?.sessionName ?: "Current Session",
                    classSummaries = summaries,
                    totalStudents = allStudents.size,
                    totalDues = grandTotalDues,
                    totalCollected = grandTotalPaid,
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
}

