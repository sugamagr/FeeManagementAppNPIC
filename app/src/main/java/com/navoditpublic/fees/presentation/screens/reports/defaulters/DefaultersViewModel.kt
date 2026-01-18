package com.navoditpublic.fees.presentation.screens.reports.defaulters

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

data class DefaulterInfo(
    val studentId: Long,
    val accountNumber: String,
    val name: String,
    val fatherName: String,
    val className: String,
    val section: String,
    val dueAmount: Double,
    val phonePrimary: String,
    val phoneSecondary: String,
    val village: String,
    val lastPaymentDate: Long?,
    val lastPaymentAmount: Double?
) {
    val daysSincePayment: Int?
        get() = lastPaymentDate?.let {
            val days = (System.currentTimeMillis() - it) / (1000 * 60 * 60 * 24)
            days.toInt()
        }
    
    /** Returns list of available phone numbers */
    val availablePhones: List<String>
        get() = listOfNotNull(
            phonePrimary.takeIf { it.isNotBlank() },
            phoneSecondary.takeIf { it.isNotBlank() }
        )
    
    /** Check if student has any phone number */
    val hasPhone: Boolean
        get() = phonePrimary.isNotBlank() || phoneSecondary.isNotBlank()
    
    /** Check if student has multiple phone numbers */
    val hasMultiplePhones: Boolean
        get() = phonePrimary.isNotBlank() && phoneSecondary.isNotBlank()
}

enum class DueAmountRange(val label: String, val min: Double, val max: Double) {
    ALL("All", 0.0, Double.MAX_VALUE),
    BELOW_5K("Below ₹5,000", 0.0, 4999.99),
    RANGE_5K_10K("₹5,000 - ₹10,000", 5000.0, 9999.99),
    RANGE_10K_20K("₹10,000 - ₹20,000", 10000.0, 19999.99),
    ABOVE_20K("Above ₹20,000", 20000.0, Double.MAX_VALUE)
}

enum class DefaulterSort(val label: String) {
    AMOUNT_HIGH_LOW("Amount: High → Low"),
    AMOUNT_LOW_HIGH("Amount: Low → High"),
    NAME_AZ("Name: A → Z"),
    NAME_ZA("Name: Z → A"),
    CLASS("Class"),
    DAYS_OVERDUE("Days Overdue")
}

data class DefaultersState(
    val isLoading: Boolean = true,
    val allDefaulters: List<DefaulterInfo> = emptyList(),
    val filteredDefaulters: List<DefaulterInfo> = emptyList(),
    val totalDues: Double = 0.0,
    val filteredTotalDues: Double = 0.0,
    // Filters
    val searchQuery: String = "",
    val selectedClass: String = "All",
    val selectedAmountRange: DueAmountRange = DueAmountRange.ALL,
    val selectedSort: DefaulterSort = DefaulterSort.AMOUNT_HIGH_LOW,
    // Available classes
    val availableClasses: List<String> = emptyList(),
    // Stats
    val averageDue: Double = 0.0,
    val highestDue: Double = 0.0
)

@HiltViewModel
class DefaultersViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(DefaultersState())
    val state: StateFlow<DefaultersState> = _state.asStateFlow()
    
    init {
        loadDefaulters()
        loadClasses()
    }
    
    private fun loadClasses() {
        viewModelScope.launch {
            settingsRepository.getAllActiveClasses().collect { classes ->
                _state.value = _state.value.copy(
                    availableClasses = listOf("All") + classes
                )
            }
        }
    }
    
    private fun loadDefaulters() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                val currentSession = settingsRepository.getCurrentSession()
                val sessionId = currentSession?.id ?: return@launch
                
                val allStudents = studentRepository.getAllActiveStudents().first()
                val defaultersList = mutableListOf<DefaulterInfo>()
                
                for (student in allStudents) {
                    val expectedDues = feeRepository.calculateExpectedSessionDues(student.id, sessionId)
                    
                    if (expectedDues > 0) {
                        // Get last payment info
                        val receipts = feeRepository.getReceiptsForStudent(student.id).first()
                        val lastReceipt = receipts.filter { !it.isCancelled }.maxByOrNull { it.receiptDate }
                        
                        defaultersList.add(
                            DefaulterInfo(
                                studentId = student.id,
                                accountNumber = student.accountNumber,
                                name = student.name,
                                fatherName = student.fatherName,
                                className = student.currentClass,
                                section = student.section,
                                dueAmount = expectedDues,
                                phonePrimary = student.phonePrimary,
                                phoneSecondary = student.phoneSecondary,
                                village = student.addressLine2.ifBlank { student.district },
                                lastPaymentDate = lastReceipt?.receiptDate,
                                lastPaymentAmount = lastReceipt?.totalAmount
                            )
                        )
                    }
                }
                
                val totalDues = defaultersList.sumOf { it.dueAmount }
                val averageDue = if (defaultersList.isNotEmpty()) totalDues / defaultersList.size else 0.0
                val highestDue = defaultersList.maxOfOrNull { it.dueAmount } ?: 0.0
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    allDefaulters = defaultersList,
                    totalDues = totalDues,
                    averageDue = averageDue,
                    highestDue = highestDue
                )
                
                applyFilters()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun setClassFilter(className: String) {
        _state.value = _state.value.copy(selectedClass = className)
        applyFilters()
    }
    
    fun setAmountRange(range: DueAmountRange) {
        _state.value = _state.value.copy(selectedAmountRange = range)
        applyFilters()
    }
    
    fun setSort(sort: DefaulterSort) {
        _state.value = _state.value.copy(selectedSort = sort)
        applyFilters()
    }
    
    private fun applyFilters() {
        val state = _state.value
        var filtered = state.allDefaulters
        
        // Apply search
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(query) ||
                it.fatherName.lowercase().contains(query) ||
                it.accountNumber.lowercase().contains(query) ||
                it.phonePrimary.contains(query) ||
                it.phoneSecondary.contains(query)
            }
        }
        
        // Apply class filter
        if (state.selectedClass != "All") {
            filtered = filtered.filter { it.className == state.selectedClass }
        }
        
        // Apply amount range filter
        if (state.selectedAmountRange != DueAmountRange.ALL) {
            filtered = filtered.filter {
                it.dueAmount >= state.selectedAmountRange.min &&
                it.dueAmount <= state.selectedAmountRange.max
            }
        }
        
        // Apply sorting
        filtered = when (state.selectedSort) {
            DefaulterSort.AMOUNT_HIGH_LOW -> filtered.sortedByDescending { it.dueAmount }
            DefaulterSort.AMOUNT_LOW_HIGH -> filtered.sortedBy { it.dueAmount }
            DefaulterSort.NAME_AZ -> filtered.sortedBy { it.name.lowercase() }
            DefaulterSort.NAME_ZA -> filtered.sortedByDescending { it.name.lowercase() }
            DefaulterSort.CLASS -> filtered.sortedWith(
                compareBy({ getClassOrder(it.className) }, { it.name.lowercase() })
            )
            DefaulterSort.DAYS_OVERDUE -> filtered.sortedByDescending { it.daysSincePayment ?: 0 }
        }
        
        _state.value = _state.value.copy(
            filteredDefaulters = filtered,
            filteredTotalDues = filtered.sumOf { it.dueAmount }
        )
    }
    
    private fun getClassOrder(className: String): Int {
        return when (className.uppercase()) {
            "NURSERY", "NUR" -> 0
            "LKG", "L.K.G" -> 1
            "UKG", "U.K.G" -> 2
            else -> {
                val numStr = className.filter { it.isDigit() }
                numStr.toIntOrNull()?.plus(2) ?: 100
            }
        }
    }
    
    fun refresh() {
        loadDefaulters()
    }
}
