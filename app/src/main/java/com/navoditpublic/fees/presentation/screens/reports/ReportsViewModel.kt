package com.navoditpublic.fees.presentation.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportsSummaryState(
    val isLoading: Boolean = true,
    // Dues summary
    val totalPendingDues: Double = 0.0,
    val totalStudents: Int = 0,
    // Collection summary
    val todayCollection: Double = 0.0,
    val monthCollection: Double = 0.0,
    val todayReceiptCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val feeRepository: FeeRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ReportsSummaryState())
    val state: StateFlow<ReportsSummaryState> = _state.asStateFlow()
    
    init {
        loadSummaryData()
    }
    
    private fun loadSummaryData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                val startOfMonth = DateUtils.getStartOfMonth()
                val endOfMonth = DateUtils.getEndOfMonth()
                
                combine(
                    feeRepository.getTotalPendingDues(),
                    feeRepository.getDailyCollectionTotal(System.currentTimeMillis()),
                    feeRepository.getMonthlyCollectionTotal(startOfMonth, endOfMonth),
                    feeRepository.getDailyReceipts(System.currentTimeMillis()),
                    studentRepository.getActiveStudentCount()
                ) { totalDues, todayCollection, monthCollection, todayReceipts, totalStudents ->
                    ReportsSummaryState(
                        isLoading = false,
                        totalPendingDues = totalDues ?: 0.0,
                        totalStudents = totalStudents,
                        todayCollection = todayCollection ?: 0.0,
                        monthCollection = monthCollection ?: 0.0,
                        todayReceiptCount = todayReceipts.size,
                        error = null
                    )
                }.collect { summaryState ->
                    _state.value = summaryState
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun refresh() {
        loadSummaryData()
    }
}
