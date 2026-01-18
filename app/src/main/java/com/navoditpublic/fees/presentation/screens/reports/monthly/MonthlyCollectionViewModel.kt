package com.navoditpublic.fees.presentation.screens.reports.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.domain.model.ReceiptWithStudent
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class MonthlyPaymentModeFilter(val displayName: String) {
    ALL("All"),
    CASH("Cash"),
    ONLINE("Online")
}

enum class MonthlySortOption(val displayName: String) {
    DATE_DESC("Date: Newest"),
    DATE_ASC("Date: Oldest"),
    AMOUNT_HIGH("Amount: High → Low"),
    AMOUNT_LOW("Amount: Low → High")
}

data class DayCollection(
    val day: Int,
    val dayOfWeek: String,
    val totalAmount: Double,
    val receiptCount: Int,
    val cashAmount: Double,
    val cashCount: Int,
    val onlineAmount: Double,
    val onlineCount: Int,
    val receipts: List<ReceiptWithStudent>
)

data class MonthlyCollectionState(
    val isLoading: Boolean = true,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isCurrentMonth: Boolean = true,
    
    // All receipts
    val allReceipts: List<ReceiptWithStudent> = emptyList(),
    val filteredReceipts: List<ReceiptWithStudent> = emptyList(),
    
    // Summary
    val totalCollection: Double = 0.0,
    val receiptCount: Int = 0,
    
    // Payment breakdown
    val cashTotal: Double = 0.0,
    val cashCount: Int = 0,
    val onlineTotal: Double = 0.0,
    val onlineCount: Int = 0,
    
    // Month comparison
    val previousMonthTotal: Double = 0.0,
    val percentageChange: Double? = null,
    
    // Daily breakdown (original = unfiltered, dailyBreakdown = filtered for display)
    val originalDailyBreakdown: List<DayCollection> = emptyList(),
    val dailyBreakdown: List<DayCollection> = emptyList(),
    val daysWithCollection: Set<Int> = emptySet(),
    
    // Chart data (day -> amount)
    val chartData: List<Pair<Int, Double>> = emptyList(),
    val maxChartValue: Double = 0.0,
    
    // Filtering & Sorting
    val paymentModeFilter: MonthlyPaymentModeFilter = MonthlyPaymentModeFilter.ALL,
    val sortOption: MonthlySortOption = MonthlySortOption.DATE_DESC,
    
    // Expanded day (for expandable cards)
    val expandedDay: Int? = null
)

@HiltViewModel
class MonthlyCollectionViewModel @Inject constructor(
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(MonthlyCollectionState())
    val state: StateFlow<MonthlyCollectionState> = _state.asStateFlow()
    
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val (startDate, endDate) = getMonthDateRange(
                _state.value.selectedMonth,
                _state.value.selectedYear
            )
            
            // Load current month data
            feeRepository.getReceiptsWithStudentsByDateRange(startDate, endDate).collect { receiptsWithStudents ->
                val validReceipts = receiptsWithStudents.filter { !it.receipt.isCancelled }
                
                // Calculate totals
                val total = validReceipts.sumOf { it.receipt.netAmount }
                val cashReceipts = validReceipts.filter { it.receipt.paymentMode == PaymentMode.CASH }
                val onlineReceipts = validReceipts.filter { it.receipt.paymentMode == PaymentMode.ONLINE }
                
                // Build daily breakdown
                val calendar = Calendar.getInstance()
                val dailyGrouped = validReceipts.groupBy { 
                    calendar.timeInMillis = it.receipt.receiptDate
                    calendar.get(Calendar.DAY_OF_MONTH)
                }
                
                val dailyBreakdown = dailyGrouped.map { (day, receipts) ->
                    calendar.set(Calendar.YEAR, _state.value.selectedYear)
                    calendar.set(Calendar.MONTH, _state.value.selectedMonth)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    
                    val dayCash = receipts.filter { it.receipt.paymentMode == PaymentMode.CASH }
                    val dayOnline = receipts.filter { it.receipt.paymentMode == PaymentMode.ONLINE }
                    
                    DayCollection(
                        day = day,
                        dayOfWeek = dayOfWeekFormat.format(calendar.time),
                        totalAmount = receipts.sumOf { it.receipt.netAmount },
                        receiptCount = receipts.size,
                        cashAmount = dayCash.sumOf { it.receipt.netAmount },
                        cashCount = dayCash.size,
                        onlineAmount = dayOnline.sumOf { it.receipt.netAmount },
                        onlineCount = dayOnline.size,
                        receipts = receipts.sortedByDescending { it.receipt.createdAt }
                    )
                }.sortedByDescending { it.day }
                
                // Days with collection for mini-calendar
                val daysWithCollection = dailyGrouped.keys
                
                // Chart data
                val chartData = (1..getLastDayOfMonth()).map { day ->
                    day to (dailyGrouped[day]?.sumOf { it.receipt.netAmount } ?: 0.0)
                }
                val maxChartValue = chartData.maxOfOrNull { it.second } ?: 0.0
                
                // Check if current month
                val now = Calendar.getInstance()
                val isCurrentMonth = _state.value.selectedMonth == now.get(Calendar.MONTH) &&
                        _state.value.selectedYear == now.get(Calendar.YEAR)
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    isCurrentMonth = isCurrentMonth,
                    allReceipts = validReceipts,
                    totalCollection = total,
                    receiptCount = validReceipts.size,
                    cashTotal = cashReceipts.sumOf { it.receipt.netAmount },
                    cashCount = cashReceipts.size,
                    onlineTotal = onlineReceipts.sumOf { it.receipt.netAmount },
                    onlineCount = onlineReceipts.size,
                    originalDailyBreakdown = dailyBreakdown,
                    dailyBreakdown = dailyBreakdown,
                    daysWithCollection = daysWithCollection,
                    chartData = chartData,
                    maxChartValue = maxChartValue
                )
                
                // Apply filters and sorting
                applyFiltersAndSort()
                
                // Load previous month for comparison
                loadPreviousMonthComparison()
            }
        }
    }
    
    private fun loadPreviousMonthComparison() {
        viewModelScope.launch {
            val prevCalendar = Calendar.getInstance().apply {
                set(Calendar.MONTH, _state.value.selectedMonth)
                set(Calendar.YEAR, _state.value.selectedYear)
                add(Calendar.MONTH, -1)
            }
            
            val (prevStart, prevEnd) = getMonthDateRange(
                prevCalendar.get(Calendar.MONTH),
                prevCalendar.get(Calendar.YEAR)
            )
            
            try {
                val prevReceipts = feeRepository.getReceiptsWithStudentsByDateRange(prevStart, prevEnd).first()
                val prevTotal = prevReceipts.filter { !it.receipt.isCancelled }.sumOf { it.receipt.netAmount }
                
                val percentageChange = if (prevTotal > 0) {
                    ((_state.value.totalCollection - prevTotal) / prevTotal) * 100
                } else if (_state.value.totalCollection > 0) {
                    100.0
                } else {
                    null
                }
                
                _state.value = _state.value.copy(
                    previousMonthTotal = prevTotal,
                    percentageChange = percentageChange
                )
            } catch (e: Exception) {
                // Ignore comparison errors
            }
        }
    }
    
    private fun applyFiltersAndSort() {
        // Always start from original unfiltered data
        var filtered = _state.value.originalDailyBreakdown
        
        // Apply payment mode filter
        when (_state.value.paymentModeFilter) {
            MonthlyPaymentModeFilter.CASH -> {
                filtered = filtered.map { day ->
                    day.copy(
                        totalAmount = day.cashAmount,
                        receiptCount = day.cashCount,
                        receipts = day.receipts.filter { it.receipt.paymentMode == PaymentMode.CASH }
                    )
                }.filter { it.receiptCount > 0 }
            }
            MonthlyPaymentModeFilter.ONLINE -> {
                filtered = filtered.map { day ->
                    day.copy(
                        totalAmount = day.onlineAmount,
                        receiptCount = day.onlineCount,
                        receipts = day.receipts.filter { it.receipt.paymentMode == PaymentMode.ONLINE }
                    )
                }.filter { it.receiptCount > 0 }
            }
            MonthlyPaymentModeFilter.ALL -> { /* Keep original data */ }
        }
        
        // Apply sorting
        filtered = when (_state.value.sortOption) {
            MonthlySortOption.DATE_DESC -> filtered.sortedByDescending { it.day }
            MonthlySortOption.DATE_ASC -> filtered.sortedBy { it.day }
            MonthlySortOption.AMOUNT_HIGH -> filtered.sortedByDescending { it.totalAmount }
            MonthlySortOption.AMOUNT_LOW -> filtered.sortedBy { it.totalAmount }
        }
        
        // Also filter receipts list
        var filteredReceipts = _state.value.allReceipts
        when (_state.value.paymentModeFilter) {
            MonthlyPaymentModeFilter.CASH -> {
                filteredReceipts = filteredReceipts.filter { it.receipt.paymentMode == PaymentMode.CASH }
            }
            MonthlyPaymentModeFilter.ONLINE -> {
                filteredReceipts = filteredReceipts.filter { it.receipt.paymentMode == PaymentMode.ONLINE }
            }
            MonthlyPaymentModeFilter.ALL -> { /* Keep as is */ }
        }
        
        _state.value = _state.value.copy(
            dailyBreakdown = filtered,
            filteredReceipts = filteredReceipts
        )
    }
    
    fun updatePaymentModeFilter(filter: MonthlyPaymentModeFilter) {
        _state.value = _state.value.copy(paymentModeFilter = filter)
        applyFiltersAndSort()
    }
    
    fun updateSortOption(option: MonthlySortOption) {
        _state.value = _state.value.copy(sortOption = option)
        applyFiltersAndSort()
    }
    
    fun toggleDayExpanded(day: Int) {
        _state.value = _state.value.copy(
            expandedDay = if (_state.value.expandedDay == day) null else day
        )
    }
    
    fun setMonth(month: Int, year: Int) {
        _state.value = _state.value.copy(
            selectedMonth = month,
            selectedYear = year,
            expandedDay = null
        )
        loadData()
    }
    
    fun previousMonth() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, _state.value.selectedMonth)
            set(Calendar.YEAR, _state.value.selectedYear)
            add(Calendar.MONTH, -1)
        }
        setMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
    }
    
    fun nextMonth() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, _state.value.selectedMonth)
            set(Calendar.YEAR, _state.value.selectedYear)
            add(Calendar.MONTH, 1)
        }
        // Don't allow future months
        val now = Calendar.getInstance()
        if (calendar.get(Calendar.YEAR) < now.get(Calendar.YEAR) ||
            (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) && 
             calendar.get(Calendar.MONTH) <= now.get(Calendar.MONTH))) {
            setMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
        }
    }
    
    private fun getLastDayOfMonth(): Int {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, _state.value.selectedYear)
            set(Calendar.MONTH, _state.value.selectedMonth)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    
    private fun getMonthDateRange(month: Int, year: Int): Pair<Long, Long> {
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val endCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        
        return startCal.timeInMillis to endCal.timeInMillis
    }
    
    fun getMonthDisplayName(): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, _state.value.selectedMonth)
            set(Calendar.YEAR, _state.value.selectedYear)
        }
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }
    
    fun getShortMonthName(): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, _state.value.selectedMonth)
        }
        return SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
    }
    
    fun getFirstDayOfWeek(): Int {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, _state.value.selectedYear)
            set(Calendar.MONTH, _state.value.selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }.get(Calendar.DAY_OF_WEEK)
    }
    
    fun getDaysInMonth(): Int {
        return getLastDayOfMonth()
    }
    
    fun getDateForDay(day: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, _state.value.selectedYear)
            set(Calendar.MONTH, _state.value.selectedMonth)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    // For PDF/Excel export
    fun getExportData(): Triple<List<String>, List<List<String>>, Map<String, String>> {
        val monthAbbr = getShortMonthName()
        val headers = listOf("Date", "Day", "Collection", "Receipts", "Cash", "Online")
        val rows = _state.value.dailyBreakdown.sortedBy { it.day }.map { day ->
            listOf(
                "$monthAbbr ${day.day}",
                day.dayOfWeek,
                "₹${String.format("%,.0f", day.totalAmount)}",
                day.receiptCount.toString(),
                "₹${String.format("%,.0f", day.cashAmount)}",
                "₹${String.format("%,.0f", day.onlineAmount)}"
            )
        }
        val summary = mapOf(
            "Total Collection" to "₹${String.format("%,.0f", _state.value.totalCollection)}",
            "Total Receipts" to _state.value.receiptCount.toString(),
            "Cash Total" to "₹${String.format("%,.0f", _state.value.cashTotal)} (${_state.value.cashCount})",
            "Online Total" to "₹${String.format("%,.0f", _state.value.onlineTotal)} (${_state.value.onlineCount})"
        )
        return Triple(headers, rows, summary)
    }
}
