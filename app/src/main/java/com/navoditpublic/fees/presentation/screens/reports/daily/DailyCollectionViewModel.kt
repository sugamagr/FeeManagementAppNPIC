package com.navoditpublic.fees.presentation.screens.reports.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.domain.model.ReceiptWithStudent
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.session.SelectedSessionInfo
import com.navoditpublic.fees.domain.session.SelectedSessionManager
import com.navoditpublic.fees.util.ClassUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class PaymentModeFilter(val displayName: String) {
    ALL("All"),
    CASH("Cash"),
    ONLINE("Online")
}

enum class ReceiptSortOption(val displayName: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    AMOUNT_HIGH("Amount: High → Low"),
    AMOUNT_LOW("Amount: Low → High"),
    NAME_AZ("Name: A → Z")
}

data class DailyCollectionState(
    val isLoading: Boolean = true,
    val selectedDate: Long = getStartOfDay(System.currentTimeMillis()),
    val isToday: Boolean = true,
    val allReceipts: List<ReceiptWithStudent> = emptyList(),
    val receipts: List<ReceiptWithStudent> = emptyList(),
    val totalCollection: Double = 0.0,
    val cashTotal: Double = 0.0,
    val onlineTotal: Double = 0.0,
    val cashCount: Int = 0,
    val onlineCount: Int = 0,
    val paymentModeTotals: Map<String, Double> = emptyMap(),
    val paymentModeFilter: PaymentModeFilter = PaymentModeFilter.ALL,
    val sortOption: ReceiptSortOption = ReceiptSortOption.NEWEST_FIRST,
    val classes: List<String> = emptyList(),
    val selectedClass: String = "All",
    // Session viewing state
    val selectedSessionInfo: SelectedSessionInfo? = null,
    val isViewingCurrentSession: Boolean = true
)

private fun getStartOfDay(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun getEndOfDay(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}

@HiltViewModel
class DailyCollectionViewModel @Inject constructor(
    private val feeRepository: FeeRepository,
    private val selectedSessionManager: SelectedSessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(DailyCollectionState())
    val state: StateFlow<DailyCollectionState> = _state.asStateFlow()
    
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
    
    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val startOfDay = _state.value.selectedDate
            val endOfDay = getEndOfDay(startOfDay)
            val selectedInfo = selectedSessionManager.selectedSessionInfo.value
            
            feeRepository.getReceiptsWithStudentsByDateRange(startOfDay, endOfDay).collect { receiptsWithStudents ->
                // Filter by session if viewing historical session
                val sessionFiltered = if (selectedInfo != null && !selectedInfo.isCurrentSession) {
                    receiptsWithStudents.filter { it.receipt.sessionId == selectedInfo.session.id }
                } else {
                    receiptsWithStudents
                }
                val activeReceipts = sessionFiltered.filter { !it.receipt.isCancelled }
                val total = activeReceipts.sumOf { it.receipt.netAmount }
                val byPaymentMode = activeReceipts
                    .groupBy { it.receipt.paymentMode.name }
                    .mapValues { entry -> entry.value.sumOf { it.receipt.netAmount } }
                
                val cashReceipts = activeReceipts.filter { it.receipt.paymentMode == PaymentMode.CASH }
                val onlineReceipts = activeReceipts.filter { it.receipt.paymentMode == PaymentMode.ONLINE }
                
                // Extract unique classes
                val uniqueClasses = activeReceipts
                    .map { it.studentClass }
                    .distinct()
                    .sortedWith(compareBy { ClassUtils.getClassOrder(it) })
                
                val today = getStartOfDay(System.currentTimeMillis())
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    allReceipts = activeReceipts,
                    totalCollection = total,
                    cashTotal = cashReceipts.sumOf { it.receipt.netAmount },
                    onlineTotal = onlineReceipts.sumOf { it.receipt.netAmount },
                    cashCount = cashReceipts.size,
                    onlineCount = onlineReceipts.size,
                    paymentModeTotals = byPaymentMode,
                    classes = listOf("All") + uniqueClasses,
                    isToday = _state.value.selectedDate >= today
                )
                
                applyFiltersAndSort()
            }
        }
    }
    
    private fun applyFiltersAndSort() {
        var filtered = _state.value.allReceipts
        
        // Apply payment mode filter
        filtered = when (_state.value.paymentModeFilter) {
            PaymentModeFilter.ALL -> filtered
            PaymentModeFilter.CASH -> filtered.filter { it.receipt.paymentMode == PaymentMode.CASH }
            PaymentModeFilter.ONLINE -> filtered.filter { it.receipt.paymentMode == PaymentMode.ONLINE }
        }
        
        // Apply class filter
        if (_state.value.selectedClass != "All") {
            filtered = filtered.filter { it.studentClass == _state.value.selectedClass }
        }
        
        // Apply sort
        filtered = when (_state.value.sortOption) {
            ReceiptSortOption.NEWEST_FIRST -> filtered.sortedByDescending { it.receipt.createdAt }
            ReceiptSortOption.OLDEST_FIRST -> filtered.sortedBy { it.receipt.createdAt }
            ReceiptSortOption.AMOUNT_HIGH -> filtered.sortedByDescending { it.receipt.netAmount }
            ReceiptSortOption.AMOUNT_LOW -> filtered.sortedBy { it.receipt.netAmount }
            ReceiptSortOption.NAME_AZ -> filtered.sortedBy { it.studentName }
        }
        
        _state.value = _state.value.copy(receipts = filtered)
    }
    
    fun updatePaymentModeFilter(filter: PaymentModeFilter) {
        _state.value = _state.value.copy(paymentModeFilter = filter)
        applyFiltersAndSort()
    }
    
    fun updateSortOption(option: ReceiptSortOption) {
        _state.value = _state.value.copy(sortOption = option)
        applyFiltersAndSort()
    }
    
    fun updateSelectedClass(className: String) {
        _state.value = _state.value.copy(selectedClass = className)
        applyFiltersAndSort()
    }
    
    fun setDate(date: Long) {
        val startOfDay = getStartOfDay(date)
        val today = getStartOfDay(System.currentTimeMillis())
        _state.value = _state.value.copy(
            selectedDate = startOfDay,
            isToday = startOfDay >= today
        )
        loadData()
    }
    
    fun previousDay() {
        val newDate = Calendar.getInstance().apply {
            timeInMillis = _state.value.selectedDate
            add(Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis
        setDate(newDate)
    }
    
    fun nextDay() {
        if (!_state.value.isToday) {
            val newDate = Calendar.getInstance().apply {
                timeInMillis = _state.value.selectedDate
                add(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis
            setDate(newDate)
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

