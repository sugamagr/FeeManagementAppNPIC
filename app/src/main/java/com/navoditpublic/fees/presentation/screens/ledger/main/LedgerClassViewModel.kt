package com.navoditpublic.fees.presentation.screens.ledger.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.Student
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

data class StudentLedgerInfo(
    val student: Student,
    val expectedFees: Double,
    val paidAmount: Double,
    val openingBalance: Double,
    val netBalance: Double // Positive = Due, Negative = Advance
)

enum class LedgerFilter {
    ALL, WITH_DUES, CLEAR
}

enum class LedgerSort {
    NAME_ASC, DUES_HIGH, DUES_LOW, ACCOUNT_NUMBER
}

data class LedgerClassState(
    val isLoading: Boolean = true,
    val className: String = "",
    val sessionName: String = "",
    val students: List<StudentLedgerInfo> = emptyList(),
    val totalStudents: Int = 0,
    val totalDues: Double = 0.0,
    val totalPaid: Double = 0.0,
    val searchQuery: String = "",
    val filter: LedgerFilter = LedgerFilter.ALL,
    val sort: LedgerSort = LedgerSort.NAME_ASC,
    val error: String? = null,
    // For alphabet rail
    val groupedStudents: Map<Char, List<StudentLedgerInfo>> = emptyMap(),
    val availableLetters: List<Char> = emptyList()
) {
    // Get index for alphabet rail navigation
    fun getIndexForLetter(letter: Char): Int {
        var index = 0
        for ((key, studentList) in groupedStudents) {
            if (key == letter) {
                return index
            }
            index += 1 + studentList.size // header + items
        }
        return -1
    }
}

// Keep old enum for backward compatibility (used in old code)
enum class SortOption {
    NAME, DUES_HIGH, DUES_LOW, SR_NUMBER
}

@HiltViewModel
class LedgerClassViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val className: String = savedStateHandle.get<String>("className") ?: ""
    
    private val _state = MutableStateFlow(LedgerClassState(className = className))
    val state: StateFlow<LedgerClassState> = _state.asStateFlow()
    
    private var allStudents: List<StudentLedgerInfo> = emptyList()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val currentSession = settingsRepository.getCurrentSession()
                val sessionId = currentSession?.id ?: 0L
                
                val students = studentRepository.getStudentsByClass(className).first()
                    .filter { it.isActive }
                
                val studentInfoList = students.map { student ->
                    // Use ledger as single source of truth
                    val totalDebits = feeRepository.getTotalDebits(student.id) // All fees charged (opening balance, tuition, transport, admission)
                    val paidAmount = feeRepository.getTotalCredits(student.id) // All payments
                    val netBalance = feeRepository.getCurrentBalance(student.id) // Current balance owed
                    
                    StudentLedgerInfo(
                        student = student,
                        expectedFees = totalDebits, // Total fees charged
                        paidAmount = paidAmount,
                        openingBalance = student.openingBalance, // For display purposes
                        netBalance = netBalance
                    )
                }
                
                allStudents = studentInfoList
                
                val filteredAndSorted = filterAndSortStudents(studentInfoList)
                val grouped = groupStudentsByLetter(filteredAndSorted)
                val letters = grouped.keys.toList()
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    sessionName = currentSession?.sessionName ?: "Current Session",
                    students = filteredAndSorted,
                    totalStudents = studentInfoList.size,
                    totalDues = studentInfoList.sumOf { it.netBalance.coerceAtLeast(0.0) },
                    totalPaid = studentInfoList.sumOf { it.paidAmount },
                    groupedStudents = grouped,
                    availableLetters = letters,
                    error = null
                )
            } catch (e: Exception) {
                android.util.Log.e("LedgerClassViewModel", "Failed to load ledger data for class $className", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load ledger data"
                )
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFiltersAndSort()
    }
    
    fun setFilter(filter: LedgerFilter) {
        _state.value = _state.value.copy(filter = filter)
        applyFiltersAndSort()
    }
    
    fun setSort(sort: LedgerSort) {
        _state.value = _state.value.copy(sort = sort)
        applyFiltersAndSort()
    }
    
    fun cycleSort() {
        val nextSort = when (_state.value.sort) {
            LedgerSort.NAME_ASC -> LedgerSort.DUES_HIGH
            LedgerSort.DUES_HIGH -> LedgerSort.DUES_LOW
            LedgerSort.DUES_LOW -> LedgerSort.ACCOUNT_NUMBER
            LedgerSort.ACCOUNT_NUMBER -> LedgerSort.NAME_ASC
        }
        setSort(nextSort)
    }
    
    // Legacy method for backward compatibility
    fun updateSortOption(option: SortOption) {
        val newSort = when (option) {
            SortOption.NAME -> LedgerSort.NAME_ASC
            SortOption.DUES_HIGH -> LedgerSort.DUES_HIGH
            SortOption.DUES_LOW -> LedgerSort.DUES_LOW
            SortOption.SR_NUMBER -> LedgerSort.ACCOUNT_NUMBER
        }
        setSort(newSort)
    }
    
    private fun applyFiltersAndSort() {
        val filteredAndSorted = filterAndSortStudents(allStudents)
        val grouped = groupStudentsByLetter(filteredAndSorted)
        val letters = grouped.keys.toList()
        
        _state.value = _state.value.copy(
            students = filteredAndSorted,
            groupedStudents = grouped,
            availableLetters = letters
        )
    }
    
    private fun filterAndSortStudents(students: List<StudentLedgerInfo>): List<StudentLedgerInfo> {
        val query = _state.value.searchQuery.lowercase()
        val filter = _state.value.filter
        val sort = _state.value.sort
        
        // Apply search filter
        var filtered = if (query.isBlank()) {
            students
        } else {
            students.filter { info ->
                info.student.name.lowercase().contains(query) ||
                info.student.fatherName.lowercase().contains(query) ||
                info.student.srNumber.lowercase().contains(query) ||
                info.student.accountNumber.lowercase().contains(query)
            }
        }
        
        // Apply status filter
        filtered = when (filter) {
            LedgerFilter.ALL -> filtered
            LedgerFilter.WITH_DUES -> filtered.filter { it.netBalance > 0 }
            LedgerFilter.CLEAR -> filtered.filter { it.netBalance <= 0 } // Clear includes advance (no dues)
        }
        
        // Apply sort
        return when (sort) {
            LedgerSort.NAME_ASC -> filtered.sortedBy { it.student.name.lowercase() }
            LedgerSort.DUES_HIGH -> filtered.sortedByDescending { it.netBalance }
            LedgerSort.DUES_LOW -> filtered.sortedBy { it.netBalance }
            // Numeric sorting for account numbers (1, 2, 10 instead of 1, 10, 2)
            LedgerSort.ACCOUNT_NUMBER -> filtered.sortedWith(
                compareBy { it.student.accountNumber.toIntOrNull() ?: Int.MAX_VALUE }
            )
        }
    }
    
    private fun groupStudentsByLetter(students: List<StudentLedgerInfo>): Map<Char, List<StudentLedgerInfo>> {
        if (_state.value.sort != LedgerSort.NAME_ASC) {
            return emptyMap()
        }
        
        return students.groupBy { info ->
            info.student.name.firstOrNull()?.uppercaseChar() ?: '#'
        }.toSortedMap()
    }
}
