package com.navoditpublic.fees.presentation.screens.students.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.StudentWithBalance
import com.navoditpublic.fees.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StudentFilter {
    ALL, WITH_DUES, NO_DUES, TRANSPORT, INACTIVE
}

enum class StudentSort {
    NAME_ASC, DUES_HIGH, DUES_LOW
}

data class StudentListState(
    val isLoading: Boolean = true,
    val className: String = "",
    val section: String = "",
    val students: List<StudentWithBalance> = emptyList(),
    val filteredStudents: List<StudentWithBalance> = emptyList(),
    val groupedStudents: Map<Char, List<StudentWithBalance>> = emptyMap(),
    val availableLetters: List<Char> = emptyList(),
    val searchQuery: String = "",
    val filter: StudentFilter = StudentFilter.ALL,
    val sort: StudentSort = StudentSort.NAME_ASC,
    val inactiveCount: Int = 0,
    val error: String? = null
) {
    fun getIndexForLetter(letter: Char): Int {
        var index = 0
        for ((key, students) in groupedStudents) {
            if (key == letter) return index
            index += 1 + students.size // +1 for the header
        }
        return -1
    }
}

@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(StudentListState())
    val state: StateFlow<StudentListState> = _state.asStateFlow()
    
    fun loadStudents(className: String, section: String) {
        if (_state.value.className == className && _state.value.section == section && !_state.value.isLoading) {
            return // Already loaded
        }
        
        // For INACTIVE view, set the filter to INACTIVE automatically
        val initialFilter = if (className == "INACTIVE") StudentFilter.INACTIVE else StudentFilter.ALL
        
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                className = className,
                section = section,
                filter = initialFilter
            )
            
            try {
                // Use getAllStudentsWithBalance to include inactive students
                studentRepository.getAllStudentsWithBalance().collect { allStudents ->
                    // Handle special cases
                    val classStudents = when {
                        // INACTIVE view - show all inactive students from all classes
                        className == "INACTIVE" -> allStudents.filter { !it.student.isActive }
                        // ALL view - show all students
                        className == "ALL" && section == "ALL" -> allStudents
                        // Regular class/section view
                        else -> allStudents.filter { 
                            it.student.currentClass == className && it.student.section == section
                        }
                    }
                    
                    val inactiveCount = classStudents.count { !it.student.isActive }
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        students = classStudents,
                        inactiveCount = inactiveCount,
                        error = null
                    )
                    applyFiltersAndSort()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFiltersAndSort()
    }
    
    fun setFilter(filter: StudentFilter) {
        _state.value = _state.value.copy(filter = filter)
        applyFiltersAndSort()
    }
    
    fun setSort(sort: StudentSort) {
        _state.value = _state.value.copy(sort = sort)
        applyFiltersAndSort()
    }
    
    fun cycleSort() {
        val nextSort = when (_state.value.sort) {
            StudentSort.NAME_ASC -> StudentSort.DUES_HIGH
            StudentSort.DUES_HIGH -> StudentSort.DUES_LOW
            StudentSort.DUES_LOW -> StudentSort.NAME_ASC
        }
        setSort(nextSort)
    }
    
    private fun applyFiltersAndSort() {
        val currentState = _state.value
        var filtered = currentState.students
        
        // Apply search
        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.lowercase()
            filtered = filtered.filter { student ->
                student.student.name.lowercase().contains(query) ||
                student.student.fatherName.lowercase().contains(query) ||
                student.student.phonePrimary.contains(query) ||
                student.student.srNumber.lowercase().contains(query)
            }
        }
        
        // Apply filter
        filtered = when (currentState.filter) {
            StudentFilter.ALL -> filtered
            StudentFilter.WITH_DUES -> filtered.filter { it.currentBalance > 0 && it.student.isActive }
            StudentFilter.NO_DUES -> filtered.filter { it.currentBalance <= 0 && it.student.isActive }
            StudentFilter.TRANSPORT -> filtered.filter { it.student.hasTransport && it.student.isActive }
            StudentFilter.INACTIVE -> filtered.filter { !it.student.isActive }
        }
        
        // Apply sort
        filtered = when (currentState.sort) {
            StudentSort.NAME_ASC -> filtered.sortedBy { it.student.name.lowercase() }
            StudentSort.DUES_HIGH -> filtered.sortedByDescending { it.currentBalance }
            StudentSort.DUES_LOW -> filtered.sortedBy { it.currentBalance }
        }
        
        // Group by first letter for alphabet navigation (only for alphabetical sort)
        val grouped = if (currentState.sort == StudentSort.NAME_ASC) {
            filtered.groupBy { 
                it.student.name.firstOrNull()?.uppercaseChar() ?: '#' 
            }.toSortedMap()
        } else {
            // For other sorts, just use a flat list
            mapOf('#' to filtered)
        }
        
        val letters = if (currentState.sort == StudentSort.NAME_ASC) {
            grouped.keys.filter { it.isLetter() }.toList()
        } else {
            emptyList()
        }
        
        _state.value = currentState.copy(
            filteredStudents = filtered,
            groupedStudents = grouped,
            availableLetters = letters
        )
    }
}
