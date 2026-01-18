package com.navoditpublic.fees.presentation.screens.settings.school_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.SchoolSettings
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

data class SchoolProfileState(
    val isLoading: Boolean = true,
    val schoolName: String = "",
    val tagline: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val district: String = "",
    val state: String = "Uttar Pradesh",
    val pincode: String = "",
    val phone: String = "",
    val email: String = "",
    val error: String? = null
)

sealed class SchoolProfileEvent {
    data object Success : SchoolProfileEvent()
    data class Error(val message: String) : SchoolProfileEvent()
}

@HiltViewModel
class SchoolProfileViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(SchoolProfileState())
    val state: StateFlow<SchoolProfileState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<SchoolProfileEvent>()
    val events: SharedFlow<SchoolProfileEvent> = _events.asSharedFlow()
    
    private var originalSettings: SchoolSettings? = null
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = settingsRepository.getSchoolSettings()
                originalSettings = settings
                
                if (settings != null) {
                    _state.value = SchoolProfileState(
                        isLoading = false,
                        schoolName = settings.schoolName,
                        tagline = settings.tagline,
                        addressLine1 = settings.addressLine1,
                        addressLine2 = settings.addressLine2,
                        district = settings.district,
                        state = settings.state,
                        pincode = settings.pincode,
                        phone = settings.phone,
                        email = settings.email
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun updateSchoolName(value: String) {
        _state.value = _state.value.copy(schoolName = value)
    }
    
    fun updateTagline(value: String) {
        _state.value = _state.value.copy(tagline = value)
    }
    
    fun updateAddressLine1(value: String) {
        _state.value = _state.value.copy(addressLine1 = value)
    }
    
    fun updateAddressLine2(value: String) {
        _state.value = _state.value.copy(addressLine2 = value)
    }
    
    fun updateDistrict(value: String) {
        _state.value = _state.value.copy(district = value)
    }
    
    fun updatePincode(value: String) {
        val filtered = value.filter { it.isDigit() }.take(6)
        _state.value = _state.value.copy(pincode = filtered)
    }
    
    fun updatePhone(value: String) {
        val filtered = value.filter { it.isDigit() }.take(10)
        _state.value = _state.value.copy(phone = filtered)
    }
    
    fun updateEmail(value: String) {
        _state.value = _state.value.copy(email = value)
    }
    
    fun save() {
        viewModelScope.launch {
            try {
                val settings = SchoolSettings(
                    schoolName = _state.value.schoolName,
                    tagline = _state.value.tagline,
                    addressLine1 = _state.value.addressLine1,
                    addressLine2 = _state.value.addressLine2,
                    district = _state.value.district,
                    state = _state.value.state,
                    pincode = _state.value.pincode,
                    phone = _state.value.phone,
                    email = _state.value.email,
                    lastReceiptNumber = originalSettings?.lastReceiptNumber ?: 0
                )
                
                settingsRepository.updateSchoolSettings(settings).onSuccess {
                    _events.emit(SchoolProfileEvent.Success)
                }.onFailure { e ->
                    _events.emit(SchoolProfileEvent.Error(e.message ?: "Failed to save"))
                }
            } catch (e: Exception) {
                _events.emit(SchoolProfileEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
}


