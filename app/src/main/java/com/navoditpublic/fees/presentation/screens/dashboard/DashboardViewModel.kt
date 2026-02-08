package com.navoditpublic.fees.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.DataSeeder
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.model.ReceiptWithStudent
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.domain.session.SelectedSessionInfo
import com.navoditpublic.fees.domain.session.SelectedSessionManager
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
    val error: String? = null,
    // Session viewing state
    val selectedSessionInfo: SelectedSessionInfo? = null,
    val isViewingCurrentSession: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository,
    private val dataSeeder: DataSeeder,
    private val selectedSessionManager: SelectedSessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()
    
    init {
        initializeData()
        observeSelectedSession()
    }
    
    private fun initializeData() {
        viewModelScope.launch {
            // Seed initial data if needed
            dataSeeder.seedInitialData()
            
            // Initialize session manager if not already done
            selectedSessionManager.initialize()
            
            // Load dashboard data
            loadDashboardData()
        }
    }
    
    private fun observeSelectedSession() {
        viewModelScope.launch {
            selectedSessionManager.selectedSessionInfo.collect { sessionInfo ->
                _state.value = _state.value.copy(
                    selectedSessionInfo = sessionInfo,
                    isViewingCurrentSession = sessionInfo?.isCurrentSession ?: true
                )
                // Reload data when session changes
                if (sessionInfo != null) {
                    loadDashboardData()
                }
            }
        }
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                // Get current session and selected session info
                val currentSession = settingsRepository.getCurrentSession()
                val selectedInfo = selectedSessionManager.selectedSessionInfo.value
                val isViewingCurrent = selectedInfo?.isCurrentSession ?: true
                
                if (isViewingCurrent || selectedInfo == null) {
                    // Load current session data with reactive flows
                    loadCurrentSessionData(currentSession, selectedInfo)
                } else {
                    // Load historical session data
                    loadHistoricalSessionData(selectedInfo)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }
    
    private suspend fun loadCurrentSessionData(
        currentSession: AcademicSession?,
        selectedInfo: SelectedSessionInfo?
    ) {
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
                error = null,
                selectedSessionInfo = selectedInfo,
                isViewingCurrentSession = true
            )
        }.collect { dashboardState ->
            _state.value = dashboardState
        }
    }
    
    private suspend fun loadHistoricalSessionData(selectedInfo: SelectedSessionInfo) {
        val session = selectedInfo.session
        
        // For historical sessions:
        // - Today/Yesterday/Month collection: Show as N/A (handled in UI, we pass -1 to indicate N/A)
        // - Pending dues: Session-specific dues (from ledger entries in that session only)
        // - Students: Count of students who had entries in that session
        // - Recent receipts: From that session only
        
        try {
            // Get collection for the session using session-specific query
            val sessionCollection = feeRepository.getTotalCollectionForSession(session.id)
            
            // Get pending dues for this specific session
            val pendingDues = feeRepository.getTotalPendingDuesForSession(session.id)
            
            // Get count of students who had entries in this session
            val studentIds = feeRepository.getStudentIdsWithEntriesInSession(session.id)
            val studentCount = studentIds.size
            
            // Get recent receipts for this session
            val recentReceipts = feeRepository.getRecentReceiptsWithStudents(10).let { flow ->
                var result = emptyList<ReceiptWithStudent>()
                flow.collect { receipts ->
                    // Filter to only show receipts from this session's date range
                    result = receipts.filter { receipt ->
                        receipt.receipt.sessionId == session.id
                    }.take(5)
                }
                result
            }
            
            _state.value = DashboardState(
                isLoading = false,
                // For historical sessions, -1 indicates "N/A" (not applicable)
                todayCollection = -1.0,
                yesterdayCollection = -1.0,
                monthCollection = sessionCollection, // Show session total instead
                totalStudents = studentCount,
                pendingDues = pendingDues,
                recentReceipts = recentReceipts,
                currentSession = session,
                error = null,
                selectedSessionInfo = selectedInfo,
                isViewingCurrentSession = false
            )
        } catch (e: Exception) {
            android.util.Log.e("DashboardViewModel", "Failed to load historical session data", e)
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message ?: "Failed to load historical session data",
                selectedSessionInfo = selectedInfo,
                isViewingCurrentSession = false
            )
        }
    }
    
    fun refresh() {
        loadDashboardData()
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


