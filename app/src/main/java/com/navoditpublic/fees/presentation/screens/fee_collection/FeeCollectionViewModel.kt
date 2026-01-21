package com.navoditpublic.fees.presentation.screens.fee_collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.domain.model.ReceiptWithStudent
import com.navoditpublic.fees.domain.repository.FeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

// Reordered: ALL first (default), then Today, Week, Month
enum class ReceiptTab(val title: String) {
    ALL("All"),
    TODAY("Today"),
    WEEK("This Week"),
    MONTH("This Month")
}

enum class PaymentFilter(val displayName: String) {
    ALL("All"),
    CASH("Cash"),
    ONLINE("Online")
}

enum class StatusFilter(val displayName: String) {
    ALL("All"),
    ACTIVE("Active"),
    CANCELLED("Cancelled")
}

data class FeeCollectionState(
    val isLoading: Boolean = true,
    
    // Statistics
    val todayCollection: Double = 0.0,
    val weekCollection: Double = 0.0,
    val monthCollection: Double = 0.0,
    val yesterdayCollection: Double = 0.0,
    
    // Counts
    val todayReceiptCount: Int = 0,
    val weekReceiptCount: Int = 0,
    val monthReceiptCount: Int = 0,
    val cashCount: Int = 0,
    val onlineCount: Int = 0,
    val cancelledCount: Int = 0,
    
    // All receipts (master list)
    val allReceipts: List<ReceiptWithStudent> = emptyList(),
    
    // Filtered receipts for display
    val filteredReceipts: List<ReceiptWithStudent> = emptyList(),
    
    // Filters & Search - Default to ALL tab
    val selectedTab: ReceiptTab = ReceiptTab.ALL,
    val searchQuery: String = "",
    val paymentFilter: PaymentFilter = PaymentFilter.ALL,
    val statusFilter: StatusFilter = StatusFilter.ALL,
    
    // Class filter
    val availableClasses: List<String> = emptyList(),
    val selectedClass: String? = null, // null means "All Classes"
    
    // Error
    val error: String? = null
)

@HiltViewModel
class FeeCollectionViewModel @Inject constructor(
    private val feeRepository: FeeRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(FeeCollectionState())
    val state: StateFlow<FeeCollectionState> = _state.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val todayStart = getStartOfDay(now)
                val todayEnd = getEndOfDay(now)
                val weekStart = getStartOfWeek(now)
                val monthStart = getStartOfMonth(now)
                
                // Combine all necessary data flows
                combine(
                    feeRepository.getDailyCollectionTotal(now),
                    feeRepository.getMonthlyCollectionTotal(weekStart, todayEnd),
                    feeRepository.getMonthlyCollectionTotal(monthStart, todayEnd),
                    feeRepository.getDailyCollectionTotal(now - 24 * 60 * 60 * 1000),
                    feeRepository.getRecentReceiptsWithStudents(200) // Get more for filtering
                ) { todayTotal, weekTotal, monthTotal, yesterdayTotal, receipts ->
                    
                    // Calculate counts - exclude cancelled receipts for accurate statistics
                    val todayReceipts = receipts.filter { 
                        it.receipt.receiptDate >= todayStart && 
                        it.receipt.receiptDate <= todayEnd &&
                        !it.receipt.isCancelled
                    }
                    val weekReceipts = receipts.filter { 
                        it.receipt.receiptDate >= weekStart && 
                        it.receipt.receiptDate <= todayEnd &&
                        !it.receipt.isCancelled
                    }
                    val monthReceipts = receipts.filter { 
                        it.receipt.receiptDate >= monthStart && 
                        it.receipt.receiptDate <= todayEnd &&
                        !it.receipt.isCancelled
                    }
                    
                    val cashReceipts = receipts.filter { 
                        it.receipt.paymentMode == PaymentMode.CASH && !it.receipt.isCancelled
                    }
                    val onlineReceipts = receipts.filter { 
                        it.receipt.paymentMode == PaymentMode.ONLINE && !it.receipt.isCancelled
                    }
                    val cancelledReceipts = receipts.filter { it.receipt.isCancelled }
                    
                    // Extract unique classes for filter
                    val classes = receipts
                        .map { it.studentClass }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                    
                    FeeCollectionState(
                        isLoading = false,
                        todayCollection = todayTotal ?: 0.0,
                        weekCollection = weekTotal ?: 0.0,
                        monthCollection = monthTotal ?: 0.0,
                        yesterdayCollection = yesterdayTotal ?: 0.0,
                        todayReceiptCount = todayReceipts.size,
                        weekReceiptCount = weekReceipts.size,
                        monthReceiptCount = monthReceipts.size,
                        cashCount = cashReceipts.size,
                        onlineCount = onlineReceipts.size,
                        cancelledCount = cancelledReceipts.size,
                        allReceipts = receipts,
                        availableClasses = classes,
                        filteredReceipts = applyFilters(
                            receipts, 
                            _state.value.selectedTab, 
                            _state.value.searchQuery,
                            _state.value.paymentFilter,
                            _state.value.statusFilter,
                            _state.value.selectedClass
                        )
                    )
                }.collect { newState ->
                    _state.value = newState.copy(
                        selectedTab = _state.value.selectedTab,
                        searchQuery = _state.value.searchQuery,
                        paymentFilter = _state.value.paymentFilter,
                        statusFilter = _state.value.statusFilter,
                        selectedClass = _state.value.selectedClass,
                        filteredReceipts = applyFilters(
                            newState.allReceipts,
                            _state.value.selectedTab,
                            _state.value.searchQuery,
                            _state.value.paymentFilter,
                            _state.value.statusFilter,
                            _state.value.selectedClass
                        )
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun selectTab(tab: ReceiptTab) {
        _state.value = _state.value.copy(
            selectedTab = tab,
            filteredReceipts = applyFilters(
                _state.value.allReceipts, 
                tab, 
                _state.value.searchQuery,
                _state.value.paymentFilter,
                _state.value.statusFilter,
                _state.value.selectedClass
            )
        )
    }
    
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredReceipts = applyFilters(
                _state.value.allReceipts, 
                _state.value.selectedTab, 
                query,
                _state.value.paymentFilter,
                _state.value.statusFilter,
                _state.value.selectedClass
            )
        )
    }
    
    fun updatePaymentFilter(filter: PaymentFilter) {
        _state.value = _state.value.copy(
            paymentFilter = filter,
            filteredReceipts = applyFilters(
                _state.value.allReceipts, 
                _state.value.selectedTab, 
                _state.value.searchQuery,
                filter,
                _state.value.statusFilter,
                _state.value.selectedClass
            )
        )
    }
    
    fun updateStatusFilter(filter: StatusFilter) {
        _state.value = _state.value.copy(
            statusFilter = filter,
            filteredReceipts = applyFilters(
                _state.value.allReceipts, 
                _state.value.selectedTab, 
                _state.value.searchQuery,
                _state.value.paymentFilter,
                filter,
                _state.value.selectedClass
            )
        )
    }
    
    fun updateClassFilter(className: String?) {
        _state.value = _state.value.copy(
            selectedClass = className,
            filteredReceipts = applyFilters(
                _state.value.allReceipts, 
                _state.value.selectedTab, 
                _state.value.searchQuery,
                _state.value.paymentFilter,
                _state.value.statusFilter,
                className
            )
        )
    }
    
    fun clearFilters() {
        _state.value = _state.value.copy(
            searchQuery = "",
            paymentFilter = PaymentFilter.ALL,
            statusFilter = StatusFilter.ALL,
            selectedClass = null,
            filteredReceipts = applyFilters(
                _state.value.allReceipts, 
                _state.value.selectedTab, 
                "",
                PaymentFilter.ALL,
                StatusFilter.ALL,
                null
            )
        )
    }
    
    private fun applyFilters(
        receipts: List<ReceiptWithStudent>,
        tab: ReceiptTab,
        searchQuery: String,
        paymentFilter: PaymentFilter,
        statusFilter: StatusFilter,
        classFilter: String?
    ): List<ReceiptWithStudent> {
        val now = System.currentTimeMillis()
        val todayStart = getStartOfDay(now)
        val todayEnd = getEndOfDay(now)
        val weekStart = getStartOfWeek(now)
        val monthStart = getStartOfMonth(now)
        
        return receipts.filter { receiptWithStudent ->
            val receipt = receiptWithStudent.receipt
            
            // Tab filter (date range)
            val passesTabFilter = when (tab) {
                ReceiptTab.ALL -> true
                ReceiptTab.TODAY -> receipt.receiptDate >= todayStart && receipt.receiptDate <= todayEnd
                ReceiptTab.WEEK -> receipt.receiptDate >= weekStart && receipt.receiptDate <= todayEnd
                ReceiptTab.MONTH -> receipt.receiptDate >= monthStart && receipt.receiptDate <= todayEnd
            }
            
            // Search filter
            val passesSearchFilter = if (searchQuery.isBlank()) true else {
                val query = searchQuery.lowercase()
                receiptWithStudent.studentName.lowercase().contains(query) ||
                receipt.receiptNumber.toString().contains(query) ||
                receiptWithStudent.studentClass.lowercase().contains(query) ||
                receiptWithStudent.studentSrNumber.lowercase().contains(query) ||
                receipt.netAmount.toString().contains(query)
            }
            
            // Payment mode filter
            val passesPaymentFilter = when (paymentFilter) {
                PaymentFilter.ALL -> true
                PaymentFilter.CASH -> receipt.paymentMode == PaymentMode.CASH
                PaymentFilter.ONLINE -> receipt.paymentMode == PaymentMode.ONLINE
            }
            
            // Status filter
            val passesStatusFilter = when (statusFilter) {
                StatusFilter.ALL -> true
                StatusFilter.ACTIVE -> !receipt.isCancelled
                StatusFilter.CANCELLED -> receipt.isCancelled
            }
            
            // Class filter
            val passesClassFilter = classFilter == null || 
                receiptWithStudent.studentClass.equals(classFilter, ignoreCase = true)
            
            passesTabFilter && passesSearchFilter && passesPaymentFilter && passesStatusFilter && passesClassFilter
        }.sortedByDescending { it.receipt.receiptDate }
    }
    
    // Date utility functions
    private fun getStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply { 
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
    
    private fun getEndOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply { 
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }
    
    private fun getStartOfWeek(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply { 
            timeInMillis = timestamp
            // Calculate days to subtract to reach the start of the week
            val currentDayOfWeek = get(Calendar.DAY_OF_WEEK)
            var daysToSubtract = currentDayOfWeek - firstDayOfWeek
            if (daysToSubtract < 0) {
                daysToSubtract += 7 // Wrap around if current day is before first day of week
            }
            add(Calendar.DAY_OF_MONTH, -daysToSubtract)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
    
    private fun getStartOfMonth(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply { 
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
