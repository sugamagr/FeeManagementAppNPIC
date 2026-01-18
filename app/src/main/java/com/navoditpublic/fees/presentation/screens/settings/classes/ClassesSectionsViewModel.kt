package com.navoditpublic.fees.presentation.screens.settings.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.dao.ClassSectionDao
import com.navoditpublic.fees.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class for section with student count
data class SectionInfo(
    val name: String,
    val studentCount: Int
)

// Data class for class with all its info
data class ClassInfo(
    val className: String,
    val sections: List<SectionInfo>,
    val totalStudents: Int,
    val level: ClassLevel
)

// Enum for class levels with display order
enum class ClassLevel(val displayName: String, val order: Int) {
    PRE_PRIMARY("Pre-Primary", 0),
    PRIMARY("Primary (1-3)", 1),
    MIDDLE("Middle (4-6)", 2),
    SECONDARY("Secondary (7-10)", 3),
    SENIOR_SECONDARY("Senior Secondary (11-12)", 4)
}

data class ClassesSectionsState(
    val isLoading: Boolean = true,
    val classesWithSections: Map<String, List<String>> = emptyMap(),
    val classInfoList: List<ClassInfo> = emptyList(),
    val totalClasses: Int = 0,
    val totalSections: Int = 0,
    val totalStudents: Int = 0,
    val searchQuery: String = "",
    val showSearch: Boolean = false,
    val expandedLevels: Set<ClassLevel> = ClassLevel.entries.toSet(),
    val error: String? = null
)

sealed class ClassesSectionsEvent {
    data class Success(val message: String) : ClassesSectionsEvent()
    data class Error(val message: String) : ClassesSectionsEvent()
}

@HiltViewModel
class ClassesSectionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val classSectionDao: ClassSectionDao
) : ViewModel() {
    
    private val _state = MutableStateFlow(ClassesSectionsState())
    val state: StateFlow<ClassesSectionsState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<ClassesSectionsEvent>()
    val events: SharedFlow<ClassesSectionsEvent> = _events.asSharedFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // Combine class sections Flow with student count Flow
            // This ensures we refresh when EITHER classes change OR students change
            combine(
                classSectionDao.getAllActiveClassSections(),
                classSectionDao.getTotalStudentCountFlow()
            ) { classSections, totalStudentCount ->
                Pair(classSections, totalStudentCount)
            }.collect { (classSections, _) ->
                val grouped = classSections
                    .groupBy { it.className }
                    .mapValues { entry -> entry.value.map { it.sectionName }.sorted() }
                    .toSortedMap(compareBy { 
                        // Custom sorting for classes
                        when (it) {
                            "NC" -> 0
                            "LKG" -> 1
                            "UKG" -> 2
                            else -> {
                                val num = it.replace("st", "").replace("nd", "").replace("rd", "").replace("th", "").toIntOrNull()
                                (num ?: 0) + 3
                            }
                        }
                    })
                
                // Build enhanced class info with student counts
                val classInfoList = grouped.map { (className, sections) ->
                    val sectionInfoList = sections.map { sectionName ->
                        SectionInfo(
                            name = sectionName,
                            studentCount = classSectionDao.getStudentCountInSection(className, sectionName)
                        )
                    }
                    ClassInfo(
                        className = className,
                        sections = sectionInfoList,
                        totalStudents = sectionInfoList.sumOf { it.studentCount },
                        level = getClassLevel(className)
                    )
                }
                
                // Get totals
                val totalStudents = classSectionDao.getTotalStudentCount()
                val totalSections = classSectionDao.getTotalSectionCount()
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    classesWithSections = grouped,
                    classInfoList = classInfoList,
                    totalClasses = grouped.size,
                    totalSections = totalSections,
                    totalStudents = totalStudents
                )
            }
        }
    }
    
    private fun getClassLevel(className: String): ClassLevel {
        return when (className) {
            "NC", "LKG", "UKG" -> ClassLevel.PRE_PRIMARY
            "1st", "2nd", "3rd" -> ClassLevel.PRIMARY
            "4th", "5th", "6th" -> ClassLevel.MIDDLE
            "7th", "8th", "9th", "10th" -> ClassLevel.SECONDARY
            "11th", "12th" -> ClassLevel.SENIOR_SECONDARY
            else -> ClassLevel.PRIMARY
        }
    }
    
    fun toggleSearch() {
        _state.value = _state.value.copy(
            showSearch = !_state.value.showSearch,
            searchQuery = if (_state.value.showSearch) "" else _state.value.searchQuery
        )
    }
    
    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
    
    fun clearSearch() {
        _state.value = _state.value.copy(searchQuery = "", showSearch = false)
    }
    
    fun toggleLevelExpanded(level: ClassLevel) {
        val current = _state.value.expandedLevels
        _state.value = _state.value.copy(
            expandedLevels = if (level in current) current - level else current + level
        )
    }
    
    fun getFilteredClasses(): List<ClassInfo> {
        val query = _state.value.searchQuery.lowercase()
        if (query.isBlank()) return _state.value.classInfoList
        
        return _state.value.classInfoList.filter { classInfo ->
            classInfo.className.lowercase().contains(query) ||
            classInfo.sections.any { it.name.lowercase().contains(query) }
        }
    }
    
    fun addSection(className: String, sectionName: String) {
        viewModelScope.launch {
            try {
                if (classSectionDao.classSectionExists(className, sectionName)) {
                    _events.emit(ClassesSectionsEvent.Error("Section $sectionName already exists for $className"))
                    return@launch
                }
                
                settingsRepository.addSection(className, sectionName).onSuccess {
                    _events.emit(ClassesSectionsEvent.Success("Section $sectionName added to $className"))
                }.onFailure { e ->
                    _events.emit(ClassesSectionsEvent.Error(e.message ?: "Failed to add section"))
                }
            } catch (e: Exception) {
                _events.emit(ClassesSectionsEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
    
    fun addMultipleSections(className: String, sections: List<String>) {
        viewModelScope.launch {
            try {
                var addedCount = 0
                var skippedCount = 0
                
                for (sectionName in sections) {
                    if (!classSectionDao.classSectionExists(className, sectionName)) {
                        settingsRepository.addSection(className, sectionName).onSuccess {
                            addedCount++
                        }
                    } else {
                        skippedCount++
                    }
                }
                
                val message = when {
                    addedCount > 0 && skippedCount > 0 -> "Added $addedCount sections, $skippedCount already existed"
                    addedCount > 0 -> "Added $addedCount sections to $className"
                    else -> "All sections already exist for $className"
                }
                _events.emit(ClassesSectionsEvent.Success(message))
            } catch (e: Exception) {
                _events.emit(ClassesSectionsEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
    
    fun deleteSection(className: String, sectionName: String) {
        viewModelScope.launch {
            try {
                // Check if there are students in this section
                val studentCount = classSectionDao.getStudentCountInSection(className, sectionName)
                if (studentCount > 0) {
                    _events.emit(ClassesSectionsEvent.Error("Cannot delete: $studentCount students are in $className - $sectionName"))
                    return@launch
                }
                
                // Check if this is the last section for the class
                val sectionsCount = _state.value.classesWithSections[className]?.size ?: 0
                if (sectionsCount <= 1) {
                    _events.emit(ClassesSectionsEvent.Error("Cannot delete: Each class must have at least one section"))
                    return@launch
                }
                
                settingsRepository.removeSection(className, sectionName).onSuccess {
                    _events.emit(ClassesSectionsEvent.Success("Section $sectionName removed from $className"))
                }.onFailure { e ->
                    _events.emit(ClassesSectionsEvent.Error(e.message ?: "Failed to remove section"))
                }
            } catch (e: Exception) {
                _events.emit(ClassesSectionsEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
}
