package com.navoditpublic.fees.presentation.screens.reports.register

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

enum class DatePreset(val displayName: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_QUARTER("Quarter"),
    CUSTOM("Custom")
}

enum class PaymentModeFilter(val displayName: String) {
    ALL("All"),
    CASH("Cash"),
    ONLINE("Online")
}

enum class ReceiptStatusFilter(val displayName: String) {
    ALL("All"),
    ACTIVE("Active"),
    CANCELLED("Cancelled")
}

enum class ReceiptSortOption(val displayName: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    AMOUNT_HIGH("Amount: High → Low"),
    AMOUNT_LOW("Amount: Low → High"),
    RECEIPT_NUM_DESC("Receipt #: High → Low"),
    RECEIPT_NUM_ASC("Receipt #: Low → High")
}

data class ReceiptRegisterState(
    val isLoading: Boolean = true,
    
    // All receipts (unfiltered)
    val allReceipts: List<ReceiptWithStudent> = emptyList(),
    // Filtered receipts for display
    val receipts: List<ReceiptWithStudent> = emptyList(),
    
    // Date range
    val startDate: Long = getStartOfMonth(),
    val endDate: Long = getEndOfDay(System.currentTimeMillis()),
    val datePreset: DatePreset = DatePreset.THIS_MONTH,
    
    // Filters
    val searchQuery: String = "",
    val paymentModeFilter: PaymentModeFilter = PaymentModeFilter.ALL,
    val statusFilter: ReceiptStatusFilter = ReceiptStatusFilter.ALL,
    val sortOption: ReceiptSortOption = ReceiptSortOption.NEWEST_FIRST,
    val selectedClass: String = "All",
    val availableClasses: List<String> = emptyList(),
    
    // Analytics
    val totalAmount: Double = 0.0,
    val activeReceiptCount: Int = 0,
    val cancelledReceiptCount: Int = 0,
    val cashTotal: Double = 0.0,
    val cashCount: Int = 0,
    val onlineTotal: Double = 0.0,
    val onlineCount: Int = 0,
    val cancelledTotal: Double = 0.0,
    
    // Session viewing state
    val selectedSessionInfo: SelectedSessionInfo? = null,
    val isViewingCurrentSession: Boolean = true
)

private fun getStartOfMonth(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun getStartOfWeek(): Long {
    return Calendar.getInstance().apply {
        // Always use Monday as the start of week
        val dayOfWeek = get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun getStartOfQuarter(): Long {
    return Calendar.getInstance().apply {
        val currentMonth = get(Calendar.MONTH)
        val quarterStartMonth = (currentMonth / 3) * 3
        set(Calendar.MONTH, quarterStartMonth)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
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
class ReceiptRegisterViewModel @Inject constructor(
    private val feeRepository: FeeRepository,
    private val selectedSessionManager: SelectedSessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(ReceiptRegisterState())
    val state: StateFlow<ReceiptRegisterState> = _state.asStateFlow()
    
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
            
            val selectedInfo = selectedSessionManager.selectedSessionInfo.value
            
            feeRepository.getReceiptsWithStudentsByDateRange(
                _state.value.startDate,
                _state.value.endDate
            ).collect { receiptsWithStudents ->
                // Filter by session if viewing historical session
                val sessionFiltered = if (selectedInfo != null && !selectedInfo.isCurrentSession) {
                    receiptsWithStudents.filter { it.receipt.sessionId == selectedInfo.session.id }
                } else {
                    receiptsWithStudents
                }
                val sortedReceipts = sessionFiltered.sortedByDescending { it.receipt.receiptNumber }
                
                // Calculate analytics
                val activeReceipts = sortedReceipts.filter { !it.receipt.isCancelled }
                val cancelledReceipts = sortedReceipts.filter { it.receipt.isCancelled }
                
                val cashReceipts = activeReceipts.filter { it.receipt.paymentMode == PaymentMode.CASH }
                val onlineReceipts = activeReceipts.filter { it.receipt.paymentMode == PaymentMode.ONLINE }
                
                // Extract unique classes
                val classes = sortedReceipts
                    .map { it.studentClass }
                    .distinct()
                    .sortedWith(compareBy { ClassUtils.getClassOrder(it) })
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    allReceipts = sortedReceipts,
                    totalAmount = activeReceipts.sumOf { it.receipt.netAmount },
                    activeReceiptCount = activeReceipts.size,
                    cancelledReceiptCount = cancelledReceipts.size,
                    cashTotal = cashReceipts.sumOf { it.receipt.netAmount },
                    cashCount = cashReceipts.size,
                    onlineTotal = onlineReceipts.sumOf { it.receipt.netAmount },
                    onlineCount = onlineReceipts.size,
                    cancelledTotal = cancelledReceipts.sumOf { it.receipt.netAmount },
                    availableClasses = listOf("All") + classes
                )
                
                applyFiltersAndSort()
            }
        }
    }
    
    private fun applyFiltersAndSort() {
        var filtered = _state.value.allReceipts
        
        // Apply search filter
        if (_state.value.searchQuery.isNotBlank()) {
            val query = _state.value.searchQuery.lowercase()
            filtered = filtered.filter { receipt ->
                receipt.studentName.lowercase().contains(query) ||
                receipt.receipt.receiptNumber.toString().contains(query) ||
                receipt.studentClass.lowercase().contains(query)
            }
        }
        
        // Apply payment mode filter
        filtered = when (_state.value.paymentModeFilter) {
            PaymentModeFilter.CASH -> filtered.filter { it.receipt.paymentMode == PaymentMode.CASH }
            PaymentModeFilter.ONLINE -> filtered.filter { it.receipt.paymentMode == PaymentMode.ONLINE }
            PaymentModeFilter.ALL -> filtered
        }
        
        // Apply status filter
        filtered = when (_state.value.statusFilter) {
            ReceiptStatusFilter.ACTIVE -> filtered.filter { !it.receipt.isCancelled }
            ReceiptStatusFilter.CANCELLED -> filtered.filter { it.receipt.isCancelled }
            ReceiptStatusFilter.ALL -> filtered
        }
        
        // Apply class filter
        if (_state.value.selectedClass != "All") {
            filtered = filtered.filter { it.studentClass == _state.value.selectedClass }
        }
        
        // Apply sorting
        filtered = when (_state.value.sortOption) {
            ReceiptSortOption.NEWEST_FIRST -> filtered.sortedByDescending { it.receipt.createdAt }
            ReceiptSortOption.OLDEST_FIRST -> filtered.sortedBy { it.receipt.createdAt }
            ReceiptSortOption.AMOUNT_HIGH -> filtered.sortedByDescending { it.receipt.netAmount }
            ReceiptSortOption.AMOUNT_LOW -> filtered.sortedBy { it.receipt.netAmount }
            ReceiptSortOption.RECEIPT_NUM_DESC -> filtered.sortedByDescending { it.receipt.receiptNumber }
            ReceiptSortOption.RECEIPT_NUM_ASC -> filtered.sortedBy { it.receipt.receiptNumber }
        }
        
        _state.value = _state.value.copy(receipts = filtered)
    }
    
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFiltersAndSort()
    }
    
    fun updatePaymentModeFilter(filter: PaymentModeFilter) {
        _state.value = _state.value.copy(paymentModeFilter = filter)
        applyFiltersAndSort()
    }
    
    fun updateStatusFilter(filter: ReceiptStatusFilter) {
        _state.value = _state.value.copy(statusFilter = filter)
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
    
    fun setDatePreset(preset: DatePreset) {
        val now = System.currentTimeMillis()
        val (start, end) = when (preset) {
            DatePreset.TODAY -> getStartOfDay() to getEndOfDay(now)
            DatePreset.THIS_WEEK -> getStartOfWeek() to getEndOfDay(now)
            DatePreset.THIS_MONTH -> getStartOfMonth() to getEndOfDay(now)
            DatePreset.THIS_QUARTER -> getStartOfQuarter() to getEndOfDay(now)
            DatePreset.CUSTOM -> _state.value.startDate to _state.value.endDate
        }
        
        _state.value = _state.value.copy(
            datePreset = preset,
            startDate = start,
            endDate = end
        )
        
        if (preset != DatePreset.CUSTOM) {
            loadData()
        }
    }
    
    fun setStartDate(date: Long) {
        _state.value = _state.value.copy(
            startDate = getStartOfDay(date),
            datePreset = DatePreset.CUSTOM
        )
        loadData()
    }
    
    fun setEndDate(date: Long) {
        _state.value = _state.value.copy(
            endDate = getEndOfDay(date),
            datePreset = DatePreset.CUSTOM
        )
        loadData()
    }
    
    // For export - clean numbers without formatting for Excel compatibility
    fun getExportData(): Triple<List<String>, List<List<String>>, Map<String, String>> {
        val headers = listOf("Receipt No", "Student", "Class", "Date", "Amount", "Mode", "Status")
        val rows = _state.value.receipts.map { r ->
            val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            listOf(
                r.receipt.receiptNumber.toString(),  // Clean number, no # prefix
                r.studentName,
                r.studentClass,
                dateFormat.format(java.util.Date(r.receipt.receiptDate)),
                r.receipt.netAmount.toLong().toString(),  // Clean number, no currency symbol
                r.receipt.paymentMode.name,
                if (r.receipt.isCancelled) "Cancelled" else "Active"
            )
        }
        val summary = mapOf(
            "Total Receipts" to _state.value.allReceipts.size.toString(),
            "Active Receipts" to _state.value.activeReceiptCount.toString(),
            "Cancelled Receipts" to _state.value.cancelledReceiptCount.toString(),
            "Total Amount" to _state.value.totalAmount.toLong().toString(),  // Clean number
            "Cash Total" to _state.value.cashTotal.toLong().toString(),  // Clean number
            "Cash Count" to _state.value.cashCount.toString(),
            "Online Total" to _state.value.onlineTotal.toLong().toString(),  // Clean number
            "Online Count" to _state.value.onlineCount.toString()
        )
        return Triple(headers, rows, summary)
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
