package com.navoditpublic.fees.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.DemoDataSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val isLoading: Boolean = false,
    val hasDemoData: Boolean = false
)

sealed class SettingsEvent {
    data class Success(val message: String) : SettingsEvent()
    data class Error(val message: String) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val demoDataSeeder: DemoDataSeeder
) : ViewModel() {
    
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()
    
    init {
        checkDemoData()
    }
    
    private fun checkDemoData() {
        viewModelScope.launch {
            val hasDemoData = demoDataSeeder.hasDemoData()
            _state.value = _state.value.copy(hasDemoData = hasDemoData)
        }
    }
    
    fun seedDemoData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                demoDataSeeder.seedDemoData()
                _state.value = _state.value.copy(isLoading = false, hasDemoData = true)
                _events.emit(SettingsEvent.Success("Demo data loaded successfully! 100+ students added."))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _events.emit(SettingsEvent.Error(e.message ?: "Failed to load demo data"))
            }
        }
    }
    
    fun clearDemoData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                demoDataSeeder.clearDemoData()
                _state.value = _state.value.copy(isLoading = false, hasDemoData = false)
                _events.emit(SettingsEvent.Success("Demo data cleared successfully!"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _events.emit(SettingsEvent.Error(e.message ?: "Failed to clear demo data"))
            }
        }
    }
    
    fun refreshDemoData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                // Clear existing demo data first
                demoDataSeeder.clearDemoData()
                // Then seed fresh demo data with all receipts
                demoDataSeeder.seedDemoData()
                _state.value = _state.value.copy(isLoading = false, hasDemoData = true)
                _events.emit(SettingsEvent.Success("Demo data refreshed! 150 students + 200+ receipts created."))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _events.emit(SettingsEvent.Error(e.message ?: "Failed to refresh demo data"))
            }
        }
    }
}

