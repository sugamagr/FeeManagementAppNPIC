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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassesSectionsState(
    val isLoading: Boolean = true,
    val classesWithSections: Map<String, List<String>> = emptyMap(),
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
            classSectionDao.getAllActiveClassSections().collect { classSections ->
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
                
                _state.value = ClassesSectionsState(
                    isLoading = false,
                    classesWithSections = grouped
                )
            }
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


