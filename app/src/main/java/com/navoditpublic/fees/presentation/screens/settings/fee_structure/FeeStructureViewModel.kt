package com.navoditpublic.fees.presentation.screens.settings.fee_structure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.entity.FeeType
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.model.FeeStructure
import com.navoditpublic.fees.domain.repository.FeeRepository
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

data class FeeStructureState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val currentSession: AcademicSession? = null,
    val admissionFee: String = "1200",
    val registrationFees: Map<String, String> = emptyMap(), // Separate for each class 9th-12th
    val monthlyFees: Map<String, String> = emptyMap(),
    val annualFees: Map<String, String> = emptyMap(),
    val error: String? = null
)

sealed class FeeStructureEvent {
    data object Success : FeeStructureEvent()
    data class Error(val message: String) : FeeStructureEvent()
}

@HiltViewModel
class FeeStructureViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val feeRepository: FeeRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(FeeStructureState())
    val state: StateFlow<FeeStructureState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<FeeStructureEvent>()
    val events: SharedFlow<FeeStructureEvent> = _events.asSharedFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                val session = settingsRepository.getCurrentSession()
                
                // Initialize with default empty values using LinkedHashMap to preserve order
                val monthlyClasses = FeeStructure.MONTHLY_FEE_CLASSES
                val annualClasses = FeeStructure.ANNUAL_FEE_CLASSES
                val registrationClasses = FeeStructure.REGISTRATION_FEE_CLASSES
                
                // Use LinkedHashMap to maintain class order
                val monthlyFees = linkedMapOf<String, String>().apply {
                    monthlyClasses.forEach { put(it, "") }
                }
                val annualFees = linkedMapOf<String, String>().apply {
                    annualClasses.forEach { put(it, "") }
                }
                val registrationFees = linkedMapOf<String, String>().apply {
                    registrationClasses.forEach { put(it, "") }
                }
                
                var admissionFee = "1200"
                
                // Load existing values if session exists (only active fees)
                if (session != null) {
                    // Load monthly fees
                    monthlyClasses.forEach { className ->
                        val fee = feeRepository.getFeeForClass(session.id, className, FeeType.MONTHLY)
                        if (fee?.isActive == true) {
                            monthlyFees[className] = fee.amount.toInt().toString()
                        }
                    }
                    
                    // Load annual fees
                    annualClasses.forEach { className ->
                        val fee = feeRepository.getFeeForClass(session.id, className, FeeType.ANNUAL)
                        if (fee?.isActive == true) {
                            annualFees[className] = fee.amount.toInt().toString()
                        }
                    }
                    
                    // Load admission fee
                    val admFee = feeRepository.getAdmissionFee(session.id, "ALL")
                    if (admFee?.isActive == true) {
                        admissionFee = admFee.amount.toInt().toString()
                    }
                    
                    // Load registration fees for each class
                    registrationClasses.forEach { className ->
                        val regFee = feeRepository.getRegistrationFee(session.id, className)
                        if (regFee?.isActive == true) {
                            registrationFees[className] = regFee.amount.toInt().toString()
                        }
                    }
                }
                
                _state.value = FeeStructureState(
                    isLoading = false,
                    isSaving = false,
                    currentSession = session,
                    admissionFee = admissionFee,
                    registrationFees = registrationFees,
                    monthlyFees = monthlyFees,
                    annualFees = annualFees,
                    error = null  // Clear any previous error
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun updateAdmissionFee(value: String) {
        _state.value = _state.value.copy(admissionFee = value.filter { it.isDigit() })
    }
    
    fun updateRegistrationFee(className: String, value: String) {
        // Use LinkedHashMap to preserve order
        val current = LinkedHashMap(_state.value.registrationFees)
        current[className] = value.filter { it.isDigit() }
        _state.value = _state.value.copy(registrationFees = current)
    }
    
    fun updateMonthlyFee(className: String, value: String) {
        // Use LinkedHashMap to preserve order
        val current = LinkedHashMap(_state.value.monthlyFees)
        current[className] = value.filter { it.isDigit() }
        _state.value = _state.value.copy(monthlyFees = current)
    }
    
    fun updateAnnualFee(className: String, value: String) {
        // Use LinkedHashMap to preserve order
        val current = LinkedHashMap(_state.value.annualFees)
        current[className] = value.filter { it.isDigit() }
        _state.value = _state.value.copy(annualFees = current)
    }
    
    fun saveAll() {
        viewModelScope.launch {
            try {
                val session = _state.value.currentSession
                if (session == null) {
                    _events.emit(FeeStructureEvent.Error("No active session. Please create a session first."))
                    return@launch
                }
                
                _state.value = _state.value.copy(isSaving = true)
                
                // Save or soft-delete admission fee
                saveOrSoftDeleteFee(session.id, "ALL", FeeType.ADMISSION, _state.value.admissionFee)
                
                // Save or soft-delete registration fees for each class 9th-12th
                _state.value.registrationFees.forEach { (className, amountStr) ->
                    saveOrSoftDeleteFee(session.id, className, FeeType.REGISTRATION, amountStr)
                }
                
                // Save or soft-delete monthly fees
                _state.value.monthlyFees.forEach { (className, amountStr) ->
                    saveOrSoftDeleteFee(session.id, className, FeeType.MONTHLY, amountStr, fullYearDiscountMonths = 1)
                }
                
                // Save or soft-delete annual fees
                _state.value.annualFees.forEach { (className, amountStr) ->
                    saveOrSoftDeleteFee(session.id, className, FeeType.ANNUAL, amountStr)
                }
                
                _state.value = _state.value.copy(isSaving = false)
                _events.emit(FeeStructureEvent.Success)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                _events.emit(FeeStructureEvent.Error(e.message ?: "Failed to save"))
            }
        }
    }
    
    /**
     * Saves a fee if amount > 0, or soft-deletes it if amount is 0 or empty
     */
    private suspend fun saveOrSoftDeleteFee(
        sessionId: Long,
        className: String,
        feeType: FeeType,
        amountStr: String,
        fullYearDiscountMonths: Int = 1
    ) {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        
        if (amount > 0) {
            // Get existing fee to preserve ID for update
            val existingFee = feeRepository.getFeeForClass(sessionId, className, feeType)
            feeRepository.insertFeeStructure(
                FeeStructure(
                    id = existingFee?.id ?: 0,  // Use existing ID for update, 0 for insert
                    sessionId = sessionId,
                    className = className,
                    feeType = feeType,
                    amount = amount,
                    fullYearDiscountMonths = fullYearDiscountMonths,
                    isActive = true  // Ensure it's active when saving
                )
            )
        } else {
            // Soft delete - mark as inactive
            feeRepository.softDeleteFeeStructure(
                sessionId = sessionId,
                className = className,
                feeType = feeType,
                remarks = "Cleared by user"
            )
        }
    }
}
