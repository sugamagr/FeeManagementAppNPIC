package com.navoditpublic.fees.presentation.screens.fee_collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.ReceiptWithStudent
import com.navoditpublic.fees.domain.repository.FeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeeCollectionState(
    val isLoading: Boolean = true,
    val todayCollection: Double = 0.0,
    val recentReceipts: List<ReceiptWithStudent> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FeeCollectionViewModel @Inject constructor(
    private val feeRepository: FeeRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(FeeCollectionState())
    val state: StateFlow<FeeCollectionState> = _state.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                combine(
                    feeRepository.getDailyCollectionTotal(System.currentTimeMillis()),
                    feeRepository.getRecentReceiptsWithStudents(20)
                ) { todayTotal, receipts ->
                    FeeCollectionState(
                        isLoading = false,
                        todayCollection = todayTotal ?: 0.0,
                        recentReceipts = receipts
                    )
                }.collect { state ->
                    _state.value = state
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}


