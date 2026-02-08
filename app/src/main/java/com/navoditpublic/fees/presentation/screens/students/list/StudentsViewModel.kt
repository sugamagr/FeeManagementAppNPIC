package com.navoditpublic.fees.presentation.screens.students.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.StudentWithBalance
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.domain.session.SelectedSessionInfo
import com.navoditpublic.fees.domain.session.SelectedSessionManager
import com.navoditpublic.fees.util.ClassUtils
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
    val error: String? = null,
    // Session viewing state
    val selectedSessionInfo: SelectedSessionInfo? = null,
    val isViewingCurrentSession: Boolean = true
)

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val settingsRepository: SettingsRepository,
    private val feeRepository: FeeRepository,
    private val selectedSessionManager: SelectedSessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(StudentsState())
    val state: StateFlow<StudentsState> = _state.asStateFlow()
    
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
            
            try {
                // Load classes
                val classes = settingsRepository.getAllActiveClasses().first()
                val sections = settingsRepository.getAllActiveSections().first()
                
                _state.value = _state.value.copy(
                    classes = classes,
                    sections = sections
                )
                
                val selectedInfo = selectedSessionManager.selectedSessionInfo.value
                val isViewingCurrent = selectedInfo?.isCurrentSession ?: true
                
                if (isViewingCurrent || selectedInfo == null) {
                    // Current session: Load ALL students (including inactive) with balance
                    studentRepository.getAllStudentsWithBalance().collect { studentsWithBalance ->
                        val activeStudents = studentsWithBalance.filter { it.student.isActive }
                        val inactiveCount = studentsWithBalance.count { !it.student.isActive }
                        val classSummaries = buildClassSummaries(activeStudents)
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
                            totalDues = activeStudents.sumOf { maxOf(0.0, it.currentBalance) },
                            error = null,
                            selectedSessionInfo = selectedInfo,
                            isViewingCurrentSession = isViewingCurrent
                        )
                    }
                } else {
                    // Historical session: Load only students who had entries in that session
                    val sessionId = selectedInfo.session.id
                    val studentsWithBalance = studentRepository.getStudentsWithBalanceForSession(sessionId, feeRepository)
                    
                    val activeStudents = studentsWithBalance.filter { it.student.isActive }
                    val inactiveCount = studentsWithBalance.count { !it.student.isActive }
                    val classSummaries = buildClassSummaries(studentsWithBalance) // All students in session
                    val filtered = filterStudents(
                        studentsWithBalance,
                        _state.value.searchQuery,
                        _state.value.selectedClass,
                        _state.value.selectedSection
                    )
                    
                    // Get session-specific dues
                    val sessionDues = feeRepository.getTotalPendingDuesForSession(sessionId)
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        students = studentsWithBalance,
                        filteredStudents = filtered,
                        classSummaries = classSummaries,
                        totalStudentCount = studentsWithBalance.size,
                        inactiveStudentCount = inactiveCount,
                        totalDues = sessionDues,
                        error = null,
                        selectedSessionInfo = selectedInfo,
                        isViewingCurrentSession = isViewingCurrent
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("StudentsViewModel", "Failed to load students", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load students"
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
        }.sortedBy { summary -> ClassUtils.getClassOrder(summary.className) }
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
    
    /**
     * Switch back to viewing the current session.
     */
    fun switchToCurrentSession() {
        viewModelScope.launch {
            selectedSessionManager.selectCurrentSession()
        }
    }
}
