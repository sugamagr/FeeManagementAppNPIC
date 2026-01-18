package com.navoditpublic.fees.presentation.screens.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.Receipt
import com.navoditpublic.fees.domain.model.ReceiptItem
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
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
    val receipt: Receipt? = null,
    val receiptItems: List<ReceiptItem> = emptyList()
)

sealed class ReceiptDetailEvent {
    data class Success(val message: String) : ReceiptDetailEvent()
    data class Error(val message: String) : ReceiptDetailEvent()
}

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    private val feeRepository: FeeRepository,
    private val studentRepository: StudentRepository
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
                
                _state.value = ReceiptDetailState(
                    isLoading = false,
                    receipt = receiptWithStudent,
                    receiptItems = receipt?.items ?: emptyList()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _events.emit(ReceiptDetailEvent.Error(e.message ?: "Failed to load receipt"))
            }
        }
    }
    
    fun cancelReceipt(reason: String) {
        viewModelScope.launch {
            try {
                val receipt = _state.value.receipt ?: return@launch
                
                feeRepository.cancelReceipt(receipt.id, reason).onSuccess {
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

