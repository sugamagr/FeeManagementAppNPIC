package com.navoditpublic.fees.presentation.screens.receipts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.Receipt
import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentReceiptsState(
    val isLoading: Boolean = true,
    val student: Student? = null,
    val receipts: List<Receipt> = emptyList(),
    val totalPaid: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class StudentReceiptsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository
) : ViewModel() {
    
    private val studentId: Long = savedStateHandle.get<String>("studentId")?.toLongOrNull() ?: 0L
    
    private val _state = MutableStateFlow(StudentReceiptsState())
    val state: StateFlow<StudentReceiptsState> = _state.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                // Get student info
                val student = studentRepository.getById(studentId)
                
                // Get receipts for student
                feeRepository.getReceiptsForStudent(studentId).collect { receipts ->
                    val sortedReceipts = receipts.sortedByDescending { it.receiptDate }
                    val totalPaid = receipts
                        .filter { !it.isCancelled }
                        .sumOf { it.netAmount }
                    
                    _state.value = StudentReceiptsState(
                        isLoading = false,
                        student = student,
                        receipts = sortedReceipts,
                        totalPaid = totalPaid,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }
    
    fun refresh() {
        loadData()
    }
}
