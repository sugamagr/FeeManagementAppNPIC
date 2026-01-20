package com.navoditpublic.fees.presentation.screens.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.domain.model.Receipt
import com.navoditpublic.fees.domain.model.ReceiptItem
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.domain.usecase.AutoAdjustOpeningBalanceUseCase
import com.navoditpublic.fees.domain.usecase.SessionAccessLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptDetailState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val receipt: Receipt? = null,
    val receiptItems: List<ReceiptItem> = emptyList(),
    
    // Edit mode
    val isEditMode: Boolean = false,
    val editAmount: String = "",
    val editPaymentMode: PaymentMode = PaymentMode.CASH,
    val editOnlineReference: String = "",
    val editRemarks: String = "",
    
    // Session access
    val sessionAccessLevel: SessionAccessLevel = SessionAccessLevel.FULL_ACCESS,
    val showSessionWarning: Boolean = false,
    val sessionWarningMessage: String = ""
)

sealed class ReceiptDetailEvent {
    data class Success(val message: String) : ReceiptDetailEvent()
    data class Error(val message: String) : ReceiptDetailEvent()
    data class BalanceAdjusted(val newBalance: Double) : ReceiptDetailEvent()
}

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    private val feeRepository: FeeRepository,
    private val studentRepository: StudentRepository,
    private val autoAdjustUseCase: AutoAdjustOpeningBalanceUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(ReceiptDetailState())
    val state: StateFlow<ReceiptDetailState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<ReceiptDetailEvent>()
    val events: SharedFlow<ReceiptDetailEvent> = _events.asSharedFlow()
    
    fun loadReceipt(receiptId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val receipt = feeRepository.getReceiptById(receiptId)
                
                // Load student details
                val receiptWithStudent = if (receipt != null) {
                    val student = studentRepository.getById(receipt.studentId)
                    receipt.copy(
                        studentName = student?.name,
                        studentClass = student?.currentClass,
                        studentSection = student?.section
                    )
                } else {
                    null
                }
                
                // Check session access level
                val accessLevel = if (receipt != null) {
                    autoAdjustUseCase.getSessionAccessLevel(receipt.sessionId)
                } else {
                    SessionAccessLevel.FULL_ACCESS
                }
                
                _state.value = ReceiptDetailState(
                    isLoading = false,
                    receipt = receiptWithStudent,
                    receiptItems = receipt?.items ?: emptyList(),
                    sessionAccessLevel = accessLevel
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _events.emit(ReceiptDetailEvent.Error(e.message ?: "Failed to load receipt"))
            }
        }
    }
    
    // ========== Edit Mode ==========
    
    fun enterEditMode() {
        val receipt = _state.value.receipt ?: return
        val accessLevel = _state.value.sessionAccessLevel
        
        // Cannot edit cancelled receipts
        if (receipt.isCancelled) {
            viewModelScope.launch {
                _events.emit(ReceiptDetailEvent.Error("Cannot edit a cancelled receipt."))
            }
            return
        }
        
        // Check if editing is allowed
        if (accessLevel == SessionAccessLevel.READ_ONLY) {
            viewModelScope.launch {
                _events.emit(ReceiptDetailEvent.Error("This session is read-only. Editing is not allowed."))
            }
            return
        }
        
        // If previous session, show warning
        if (accessLevel == SessionAccessLevel.PREVIOUS_SESSION) {
            _state.value = _state.value.copy(
                showSessionWarning = true,
                sessionWarningMessage = "This receipt is from a previous session. Changes will automatically adjust the opening balance in the current session."
            )
        }
        
        _state.value = _state.value.copy(
            isEditMode = true,
            editAmount = receipt.netAmount.toString(),
            editPaymentMode = receipt.paymentMode,
            editOnlineReference = receipt.onlineReference ?: "",
            editRemarks = receipt.remarks ?: ""
        )
    }
    
    fun exitEditMode() {
        _state.value = _state.value.copy(
            isEditMode = false,
            showSessionWarning = false
        )
    }
    
    fun dismissSessionWarning() {
        _state.value = _state.value.copy(showSessionWarning = false)
    }
    
    fun updateEditAmount(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _state.value = _state.value.copy(editAmount = filtered)
    }
    
    fun updateEditPaymentMode(mode: PaymentMode) {
        _state.value = _state.value.copy(editPaymentMode = mode)
    }
    
    fun updateEditOnlineReference(value: String) {
        _state.value = _state.value.copy(editOnlineReference = value)
    }
    
    fun updateEditRemarks(value: String) {
        _state.value = _state.value.copy(editRemarks = value)
    }
    
    fun saveEdit() {
        viewModelScope.launch {
            try {
                val receipt = _state.value.receipt ?: return@launch
                val newAmount = _state.value.editAmount.toDoubleOrNull()
                
                if (newAmount == null || newAmount <= 0) {
                    _events.emit(ReceiptDetailEvent.Error("Please enter a valid amount"))
                    return@launch
                }
                
                _state.value = _state.value.copy(isSaving = true)
                
                // Calculate new totals (discount stays same, total = amount + discount)
                val updatedReceipt = receipt.copy(
                    netAmount = newAmount,
                    totalAmount = newAmount + receipt.discountAmount,
                    paymentMode = _state.value.editPaymentMode,
                    onlineReference = if (_state.value.editPaymentMode == PaymentMode.ONLINE) 
                        _state.value.editOnlineReference else null,
                    remarks = _state.value.editRemarks.takeIf { it.isNotBlank() }
                )
                
                // Update receipt items if amount changed
                val updatedItems = if (newAmount != receipt.netAmount) {
                    receipt.items.map { item ->
                        if (receipt.items.size == 1) {
                            item.copy(amount = newAmount)
                        } else {
                            item // Keep as-is for multi-item receipts
                        }
                    }
                } else {
                    receipt.items
                }
                
                feeRepository.editReceiptWithLedger(updatedReceipt, updatedItems).onSuccess { oldAmount ->
                    // Auto-adjust opening balance if this is from a previous session
                    if (_state.value.sessionAccessLevel == SessionAccessLevel.PREVIOUS_SESSION) {
                        autoAdjustUseCase.adjustOpeningBalance(
                            studentId = receipt.studentId,
                            sourceSessionId = receipt.sessionId
                        ).onSuccess { newBalance ->
                            if (newBalance != null) {
                                _events.emit(ReceiptDetailEvent.BalanceAdjusted(newBalance))
                            }
                        }
                    }
                    
                    // Reload receipt
                    loadReceipt(receipt.id)
                    _state.value = _state.value.copy(isEditMode = false, isSaving = false)
                    _events.emit(ReceiptDetailEvent.Success("Receipt updated successfully"))
                }.onFailure { e ->
                    _state.value = _state.value.copy(isSaving = false)
                    _events.emit(ReceiptDetailEvent.Error(e.message ?: "Failed to update receipt"))
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                _events.emit(ReceiptDetailEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
    
    // ========== Cancel Receipt ==========
    
    fun cancelReceipt(reason: String) {
        viewModelScope.launch {
            try {
                val receipt = _state.value.receipt ?: return@launch
                val accessLevel = _state.value.sessionAccessLevel
                
                // Check if cancellation is allowed
                if (accessLevel == SessionAccessLevel.READ_ONLY) {
                    _events.emit(ReceiptDetailEvent.Error("This session is read-only. Cancellation is not allowed."))
                    return@launch
                }
                
                feeRepository.cancelReceipt(receipt.id, reason).onSuccess {
                    // Auto-adjust opening balance if this is from a previous session
                    if (accessLevel == SessionAccessLevel.PREVIOUS_SESSION) {
                        autoAdjustUseCase.adjustOpeningBalance(
                            studentId = receipt.studentId,
                            sourceSessionId = receipt.sessionId
                        ).onSuccess { newBalance ->
                            if (newBalance != null) {
                                _events.emit(ReceiptDetailEvent.BalanceAdjusted(newBalance))
                            }
                        }
                    }
                    
                    // Reload the receipt to show updated state
                    loadReceipt(receipt.id)
                    _events.emit(ReceiptDetailEvent.Success("Receipt cancelled successfully"))
                }.onFailure { e ->
                    _events.emit(ReceiptDetailEvent.Error(e.message ?: "Failed to cancel receipt"))
                }
            } catch (e: Exception) {
                _events.emit(ReceiptDetailEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
}

