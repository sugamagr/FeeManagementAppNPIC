package com.navoditpublic.fees.presentation.screens.reports.classwise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.domain.session.SelectedSessionInfo
import com.navoditpublic.fees.domain.session.SelectedSessionManager
import com.navoditpublic.fees.util.ClassUtils
import com.navoditpublic.fees.util.ReminderTemplate
import com.navoditpublic.fees.util.ReminderTemplateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentReminderInfo(
    val student: Student,
    val dueAmount: Double
) {
    val id: Long get() = student.id
    val name: String get() = student.name
    val fatherName: String get() = student.fatherName
    val phonePrimary: String get() = student.phonePrimary
    val phoneSecondary: String get() = student.phoneSecondary
    val hasPhone: Boolean get() = student.phonePrimary.isNotBlank() || student.phoneSecondary.isNotBlank()
    val hasMultiplePhones: Boolean get() = student.phonePrimary.isNotBlank() && student.phoneSecondary.isNotBlank()
    val availablePhone: String get() = student.phonePrimary.ifBlank { student.phoneSecondary }
}

data class ClassSummary(
    val className: String,
    val studentCount: Int,
    val collected: Double,
    val pending: Double,
    val collectionRate: Double,
    val studentsWithDues: List<StudentReminderInfo> = emptyList()
) {
    val studentsWithDuesCount: Int get() = studentsWithDues.size
    val studentsWithPhone: Int get() = studentsWithDues.count { it.hasPhone }
    val studentsWithoutPhone: Int get() = studentsWithDues.count { !it.hasPhone }
    
    // Performance category
    val performanceCategory: PerformanceCategory get() = when {
        collectionRate >= 0.7 -> PerformanceCategory.GOOD
        collectionRate >= 0.4 -> PerformanceCategory.AVERAGE
        else -> PerformanceCategory.CRITICAL
    }
}

enum class PerformanceCategory(val label: String) {
    GOOD("Good"),
    AVERAGE("Average"),
    CRITICAL("Critical")
}

enum class SortType {
    CLASS,
    DUES,
    STUDENTS
}

data class ClassSortOption(
    val type: SortType,
    val ascending: Boolean
) {
    companion object {
        val DEFAULT = ClassSortOption(SortType.CLASS, ascending = true)
    }
}


data class ClassWiseState(
    val isLoading: Boolean = true,
    val allClassSummaries: List<ClassSummary> = emptyList(),
    val filteredClassSummaries: List<ClassSummary> = emptyList(),
    val totalStudents: Int = 0,
    val totalStudentsWithDues: Int = 0,
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val collectionRate: Double = 0.0,
    
    // Search & Sort
    val searchQuery: String = "",
    val selectedSort: ClassSortOption? = null, // null = no sort, default class order
    
    // Reminder state
    val selectedClassForReminder: ClassSummary? = null,
    val reminderTemplates: List<ReminderTemplate> = emptyList(),
    val selectedTemplate: ReminderTemplate? = null,
    val canAddMoreTemplates: Boolean = true,
    
    // Error state
    val error: String? = null,
    
    // Session viewing state
    val selectedSessionInfo: SelectedSessionInfo? = null,
    val isViewingCurrentSession: Boolean = true
)

@HiltViewModel
class ClassWiseViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderTemplateManager: ReminderTemplateManager,
    private val selectedSessionManager: SelectedSessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(ClassWiseState())
    val state: StateFlow<ClassWiseState> = _state.asStateFlow()
    
    init {
        loadData()
        loadTemplates()
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
    
    private fun loadTemplates() {
        val templates = reminderTemplateManager.getTemplates()
        _state.value = _state.value.copy(
            reminderTemplates = templates,
            selectedTemplate = templates.firstOrNull { it.isDefault } ?: templates.firstOrNull(),
            canAddMoreTemplates = reminderTemplateManager.canAddMoreTemplates()
        )
    }
    
    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun toggleSort(type: SortType) {
        val currentSort = _state.value.selectedSort
        val newSort = when {
            // Not selected -> Apply with default direction
            currentSort == null || currentSort.type != type -> {
                when (type) {
                    SortType.CLASS -> ClassSortOption(type, ascending = true)  // A-Z default
                    SortType.DUES -> ClassSortOption(type, ascending = false)  // Highest first
                    SortType.STUDENTS -> ClassSortOption(type, ascending = false)  // Most first
                }
            }
            // Selected with default direction -> Toggle direction
            (type == SortType.CLASS && currentSort.ascending) ||
            (type == SortType.DUES && !currentSort.ascending) ||
            (type == SortType.STUDENTS && !currentSort.ascending) -> {
                currentSort.copy(ascending = !currentSort.ascending)
            }
            // Selected with toggled direction -> Remove (set to null)
            else -> null
        }
        _state.value = _state.value.copy(selectedSort = newSort)
        applyFilters()
    }
    
    fun selectClassForReminder(classSummary: ClassSummary?) {
        _state.value = _state.value.copy(selectedClassForReminder = classSummary)
    }
    
    fun selectTemplate(template: ReminderTemplate) {
        _state.value = _state.value.copy(selectedTemplate = template)
    }
    
    fun addTemplate(name: String, hindiMessage: String, englishMessage: String): Boolean {
        val success = reminderTemplateManager.addTemplate(name, hindiMessage, englishMessage)
        if (success) loadTemplates()
        return success
    }
    
    fun updateTemplate(template: ReminderTemplate): Boolean {
        val success = reminderTemplateManager.updateTemplate(template)
        if (success) loadTemplates()
        return success
    }
    
    fun deleteTemplate(templateId: Int): Boolean {
        val success = reminderTemplateManager.deleteTemplate(templateId)
        if (success) loadTemplates()
        return success
    }
    
    fun buildReminderMessage(student: Student, dueAmount: Double): String {
        val template = _state.value.selectedTemplate ?: return ""
        return reminderTemplateManager.buildMessage(
            template = template,
            studentName = student.name,
            className = student.currentClass,
            amount = formatAmount(dueAmount)
        )
    }
    
    private fun formatAmount(amount: Double): String {
        return "₹${String.format("%,.0f", amount)}"
    }
    
    private fun applyFilters() {
        val state = _state.value
        var filtered = state.allClassSummaries
        
        // Apply search
        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.className.lowercase().contains(state.searchQuery.lowercase())
            }
        }
        
        // Apply sort (null = default class order ascending)
        val sort = state.selectedSort
        filtered = if (sort == null) {
            filtered.sortedBy { getClassSortOrder(it.className) }
        } else {
            when (sort.type) {
                SortType.CLASS -> if (sort.ascending) {
                    filtered.sortedBy { getClassSortOrder(it.className) }
                } else {
                    filtered.sortedByDescending { getClassSortOrder(it.className) }
                }
                SortType.DUES -> if (sort.ascending) {
                    filtered.sortedBy { it.pending }
                } else {
                    filtered.sortedByDescending { it.pending }
                }
                SortType.STUDENTS -> if (sort.ascending) {
                    filtered.sortedBy { it.studentCount }
                } else {
                    filtered.sortedByDescending { it.studentCount }
                }
            }
        }
        
        _state.value = _state.value.copy(filteredClassSummaries = filtered)
    }
    
    private fun getClassSortOrder(className: String): Int {
        // Use unified class ordering from ClassUtils
        val baseName = className.split("-").first().trim()
        return ClassUtils.getClassOrder(baseName)
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                val selectedInfo = selectedSessionManager.selectedSessionInfo.value
                val isViewingCurrent = selectedInfo?.isCurrentSession ?: true
                val classes = settingsRepository.getAllActiveClasses().first()
                val summaries = mutableListOf<ClassSummary>()
                
                // For historical sessions, get the list of students who had entries in that session
                val sessionStudentIds = if (!isViewingCurrent && selectedInfo != null) {
                    feeRepository.getStudentIdsWithEntriesInSession(selectedInfo.session.id).toSet()
                } else {
                    emptySet()
                }
                
                for (className in classes) {
                    val allClassStudents = studentRepository.getStudentsByClass(className).first()
                    
                    // Filter students based on session context
                    val relevantStudents = if (isViewingCurrent || selectedInfo == null) {
                        // Current session: active students only
                        allClassStudents.filter { it.isActive }
                    } else {
                        // Historical session: students who had entries in that session
                        allClassStudents.filter { sessionStudentIds.contains(it.id) }
                    }
                    
                    var totalCollected = 0.0
                    var totalPending = 0.0
                    val studentsWithDues = mutableListOf<StudentReminderInfo>()
                    
                    for (student in relevantStudents) {
                        // Use ledger as single source of truth - includes all fees and payments
                        val balance = feeRepository.getCurrentBalance(student.id)
                        
                        if (balance > 0) {
                            totalPending += balance
                            studentsWithDues.add(StudentReminderInfo(student, balance))
                        }
                        
                        val credits = feeRepository.getTotalCredits(student.id)
                        totalCollected += credits
                    }
                    
                    val total = totalCollected + totalPending
                    val rate = if (total > 0) totalCollected / total else 0.0
                    
                    if (relevantStudents.isNotEmpty()) {
                        summaries.add(
                            ClassSummary(
                                className = className,
                                studentCount = relevantStudents.size,
                                collected = totalCollected,
                                pending = totalPending,
                                collectionRate = rate,
                                studentsWithDues = studentsWithDues.sortedByDescending { it.dueAmount }
                            )
                        )
                    }
                }
                
                // Sort by class order
                val sortedSummaries = summaries.sortedBy { getClassSortOrder(it.className) }
                
                val totalStudents = sortedSummaries.sumOf { it.studentCount }
                val totalStudentsWithDues = sortedSummaries.sumOf { it.studentsWithDuesCount }
                val totalCollected = sortedSummaries.sumOf { it.collected }
                val totalPending = sortedSummaries.sumOf { it.pending }
                val total = totalCollected + totalPending
                val overallRate = if (total > 0) totalCollected / total else 0.0
                
                _state.value = ClassWiseState(
                    isLoading = false,
                    allClassSummaries = sortedSummaries,
                    filteredClassSummaries = sortedSummaries,
                    totalStudents = totalStudents,
                    totalStudentsWithDues = totalStudentsWithDues,
                    totalCollected = totalCollected,
                    totalPending = totalPending,
                    collectionRate = overallRate,
                    reminderTemplates = _state.value.reminderTemplates,
                    selectedTemplate = _state.value.selectedTemplate,
                    canAddMoreTemplates = _state.value.canAddMoreTemplates,
                    selectedSessionInfo = selectedInfo,
                    isViewingCurrentSession = isViewingCurrent
                )
            } catch (e: Exception) {
                android.util.Log.e("ClassWiseViewModel", "Failed to load class-wise data", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load class-wise report"
                )
            }
        }
    }
    
    fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        loadData()
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
