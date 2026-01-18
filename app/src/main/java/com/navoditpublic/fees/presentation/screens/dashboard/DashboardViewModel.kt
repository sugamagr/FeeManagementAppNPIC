package com.navoditpublic.fees.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.DataSeeder
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.model.ReceiptWithStudent
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val isLoading: Boolean = true,
    val todayCollection: Double = 0.0,
    val yesterdayCollection: Double = 0.0,
    val monthCollection: Double = 0.0,
    val totalStudents: Int = 0,
    val pendingDues: Double = 0.0,
    val recentReceipts: List<ReceiptWithStudent> = emptyList(),
    val currentSession: AcademicSession? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository,
    private val dataSeeder: DataSeeder
) : ViewModel() {
    
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()
    
    init {
        initializeData()
    }
    
    private fun initializeData() {
        viewModelScope.launch {
            // Seed initial data if needed
            dataSeeder.seedInitialData()
            
            // Load dashboard data
            loadDashboardData()
        }
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                // Get current session
                val currentSession = settingsRepository.getCurrentSession()
                
                // Get month boundaries for monthly collection
                val startOfMonth = DateUtils.getStartOfMonth()
                val endOfMonth = DateUtils.getEndOfMonth()
                
                // Calculate yesterday's timestamp
                val yesterdayTimestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                
                // Combine all flows for reactive updates
                combine(
                    studentRepository.getActiveStudentCount(),
                    feeRepository.getDailyCollectionTotal(System.currentTimeMillis()),
                    feeRepository.getDailyCollectionTotal(yesterdayTimestamp),
                    feeRepository.getMonthlyCollectionTotal(startOfMonth, endOfMonth),
                    feeRepository.getTotalPendingDues(),
                    feeRepository.getRecentReceiptsWithStudents(5)
                ) { flows ->
                    val studentCount = flows[0] as Int
                    val todayCollection = flows[1] as? Double ?: 0.0
                    val yesterdayCollection = flows[2] as? Double ?: 0.0
                    val monthlyCollection = flows[3] as? Double ?: 0.0
                    val pendingDues = flows[4] as? Double ?: 0.0
                    @Suppress("UNCHECKED_CAST")
                    val recentReceipts = flows[5] as List<ReceiptWithStudent>
                    
                    DashboardState(
                        isLoading = false,
                        todayCollection = todayCollection,
                        yesterdayCollection = yesterdayCollection,
                        monthCollection = monthlyCollection,
                        totalStudents = studentCount,
                        pendingDues = pendingDues,
                        recentReceipts = recentReceipts,
                        currentSession = currentSession,
                        error = null
                    )
                }.collect { dashboardState ->
                    _state.value = dashboardState
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }
    
    fun refresh() {
        loadDashboardData()
    }
}


