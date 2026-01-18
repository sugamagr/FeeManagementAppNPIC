package com.navoditpublic.fees.presentation.screens.reports.dues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.navoditpublic.fees.data.local.dao.SavedReportViewDao
import com.navoditpublic.fees.data.local.entity.SavedReportViewEntity
import com.navoditpublic.fees.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedViewsState(
    val isLoading: Boolean = true,
    val views: List<SavedReportViewEntity> = emptyList(),
    val showEditDialog: Boolean = false,
    val editingView: SavedReportViewEntity? = null,
    val editViewName: String = "",
    val showDeleteConfirm: Boolean = false,
    val viewToDelete: SavedReportViewEntity? = null,
    val error: String? = null
)

@HiltViewModel
class SavedViewsViewModel @Inject constructor(
    private val savedReportViewDao: SavedReportViewDao
) : ViewModel() {
    
    private val _state = MutableStateFlow(SavedViewsState())
    val state: StateFlow<SavedViewsState> = _state.asStateFlow()
    
    init {
        loadViews()
    }
    
    private fun loadViews() {
        viewModelScope.launch {
            savedReportViewDao.getAll().collect { views ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    views = views
                )
            }
        }
    }
    
    fun openView(view: SavedReportViewEntity, navController: NavController) {
        when (view.reportType) {
            "DUES" -> navController.navigate(Screen.CustomDuesReport.route)
            "COLLECTION" -> navController.navigate(Screen.CustomCollectionReport.route)
        }
    }
    
    fun showEditDialog(view: SavedReportViewEntity) {
        _state.value = _state.value.copy(
            showEditDialog = true,
            editingView = view,
            editViewName = view.viewName
        )
    }
    
    fun dismissEditDialog() {
        _state.value = _state.value.copy(
            showEditDialog = false,
            editingView = null,
            editViewName = ""
        )
    }
    
    fun updateEditViewName(name: String) {
        _state.value = _state.value.copy(editViewName = name)
    }
    
    fun saveEditedView() {
        viewModelScope.launch {
            val view = _state.value.editingView ?: return@launch
            val newName = _state.value.editViewName.trim()
            if (newName.isBlank()) return@launch
            
            savedReportViewDao.update(
                view.copy(
                    viewName = newName,
                    updatedAt = System.currentTimeMillis()
                )
            )
            
            dismissEditDialog()
        }
    }
    
    fun showDeleteConfirm(view: SavedReportViewEntity) {
        _state.value = _state.value.copy(
            showDeleteConfirm = true,
            viewToDelete = view
        )
    }
    
    fun dismissDeleteConfirm() {
        _state.value = _state.value.copy(
            showDeleteConfirm = false,
            viewToDelete = null
        )
    }
    
    fun confirmDelete() {
        viewModelScope.launch {
            val view = _state.value.viewToDelete ?: return@launch
            savedReportViewDao.delete(view)
            dismissDeleteConfirm()
        }
    }
}

