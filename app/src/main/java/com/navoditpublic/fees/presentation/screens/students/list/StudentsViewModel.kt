package com.navoditpublic.fees.presentation.screens.students.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.StudentWithBalance
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

data class StudentsState(
    val isLoading: Boolean = true,
    val students: List<StudentWithBalance> = emptyList(),
    val filteredStudents: List<StudentWithBalance> = emptyList(),
    val searchQuery: String = "",
    val selectedClass: String? = null,
    val selectedSection: String? = null,
    val classes: List<String> = emptyList(),
    val sections: List<String> = emptyList(),
    val classSummaries: List<ClassSummary> = emptyList(),
    val totalStudentCount: Int = 0,
    val inactiveStudentCount: Int = 0,
    val totalDues: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val settingsRepository: SettingsRepository,
    private val feeRepository: FeeRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(StudentsState())
    val state: StateFlow<StudentsState> = _state.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                // Load classes
                val classes = settingsRepository.getAllActiveClasses().first()
                val sections = settingsRepository.getAllActiveSections().first()
                
                _state.value = _state.value.copy(
                    classes = classes,
                    sections = sections
                )
                
                // Load ALL students (including inactive) with balance
                studentRepository.getAllStudentsWithBalance().collect { studentsWithBalance ->
                    val activeStudents = studentsWithBalance.filter { it.student.isActive }
                    val inactiveCount = studentsWithBalance.count { !it.student.isActive }
                    val classSummaries = buildClassSummaries(activeStudents) // Class summaries only for active
                    val filtered = filterStudents(
                        studentsWithBalance,
                        _state.value.searchQuery,
                        _state.value.selectedClass,
                        _state.value.selectedSection
                    )
                    _state.value = _state.value.copy(
                        isLoading = false,
                        students = studentsWithBalance,
                        filteredStudents = filtered,
                        classSummaries = classSummaries,
                        totalStudentCount = studentsWithBalance.size,
                        inactiveStudentCount = inactiveCount,
                        totalDues = activeStudents.sumOf { maxOf(0.0, it.currentBalance) }, // Only active students for dues
                        error = null
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
    
    private fun buildClassSummaries(students: List<StudentWithBalance>): List<ClassSummary> {
        // Group students by class
        val byClass = students.groupBy { it.student.currentClass }
        
        return byClass.map { (className, classStudents) ->
            // Group by section within class
            val bySection = classStudents.groupBy { it.student.section }
            
            val sections = bySection.map { (sectionName, sectionStudents) ->
                SectionSummary(
                    sectionName = sectionName,
                    studentCount = sectionStudents.size,
                    totalDues = sectionStudents.sumOf { maxOf(0.0, it.currentBalance) }
                )
            }.sortedBy { it.sectionName }
            
            ClassSummary(
                className = className,
                totalStudents = classStudents.size,
                totalDues = classStudents.sumOf { maxOf(0.0, it.currentBalance) },
                sections = sections
            )
        }.sortedBy { summary ->
            // Sort classes in logical order
            when (summary.className.uppercase()) {
                "NC", "NURSERY" -> 0
                "LKG" -> 1
                "UKG" -> 2
                else -> {
                    val num = summary.className.filter { it.isDigit() }.toIntOrNull()
                    if (num != null) num + 2 else 100
                }
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun onClassSelected(className: String?) {
        _state.value = _state.value.copy(selectedClass = className)
        applyFilters()
    }
    
    fun onSectionSelected(section: String?) {
        _state.value = _state.value.copy(selectedSection = section)
        applyFilters()
    }
    
    fun clearFilters() {
        _state.value = _state.value.copy(
            searchQuery = "",
            selectedClass = null,
            selectedSection = null
        )
        applyFilters()
    }
    
    private fun applyFilters() {
        val filtered = filterStudents(
            _state.value.students,
            _state.value.searchQuery,
            _state.value.selectedClass,
            _state.value.selectedSection
        )
        _state.value = _state.value.copy(filteredStudents = filtered)
    }
    
    private fun filterStudents(
        students: List<StudentWithBalance>,
        query: String,
        className: String?,
        section: String?
    ): List<StudentWithBalance> {
        return students.filter { studentWithBalance ->
            val student = studentWithBalance.student
            val matchesQuery = query.isBlank() || 
                student.name.contains(query, ignoreCase = true) ||
                student.srNumber.contains(query, ignoreCase = true) ||
                student.accountNumber.contains(query, ignoreCase = true) ||
                student.fatherName.contains(query, ignoreCase = true) ||
                student.phonePrimary.contains(query)
            
            val matchesClass = className == null || student.currentClass == className
            val matchesSection = section == null || student.section == section
            
            matchesQuery && matchesClass && matchesSection
        }
    }
}
