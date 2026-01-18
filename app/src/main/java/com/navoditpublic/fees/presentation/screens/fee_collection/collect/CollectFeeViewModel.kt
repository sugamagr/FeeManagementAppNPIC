package com.navoditpublic.fees.presentation.screens.fee_collection.collect

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.entity.FeeType
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.domain.model.AuditLog
import com.navoditpublic.fees.domain.model.FeeStructure
import com.navoditpublic.fees.domain.model.Receipt
import com.navoditpublic.fees.domain.model.ReceiptItem
import com.navoditpublic.fees.domain.model.StudentWithBalance
import com.navoditpublic.fees.domain.repository.AuditRepository
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectFeeState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    
    // Receipt info - Receipt number must be entered manually
    val receiptNumber: String = "",
    val receiptNumberError: String? = null,
    val showDuplicateWarning: Boolean = false,
    val receiptDate: Long = System.currentTimeMillis(),
    val isBackdatedReceipt: Boolean = false,  // True if receipt date is before today
    val showBackdateWarning: Boolean = false,  // Show warning dialog for backdated receipt
    val dateError: String? = null,  // Error for future date
    
    // Student
    val studentSearchQuery: String = "",
    val searchResults: List<StudentWithBalance> = emptyList(),
    val allStudents: List<StudentWithBalance> = emptyList(),
    val selectedStudent: StudentWithBalance? = null,
    
    // Simplified fee collection - just enter amount
    val currentDues: Double = 0.0,
    val amountReceived: String = "",
    val isFullYearPayment: Boolean = false,  // Checkbox for full year discount
    val fullYearDiscountAmount: Double = 0.0,  // 1 month fee discount
    
    // Payment
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val onlineReference: String = "",
    val details: String = "",  // Details/remarks field
    
    // Totals
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val remainingDues: Double = 0.0,
    
    val error: String? = null
)

sealed class CollectFeeEvent {
    data object Success : CollectFeeEvent()
    data class Error(val message: String) : CollectFeeEvent()
}

@HiltViewModel
class CollectFeeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository,
    private val auditRepository: AuditRepository
) : ViewModel() {
    
    private val preSelectedStudentId: Long? = savedStateHandle.get<String>("studentId")?.toLongOrNull()
    
    private val _state = MutableStateFlow(CollectFeeState())
    val state: StateFlow<CollectFeeState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<CollectFeeEvent>()
    val events: SharedFlow<CollectFeeEvent> = _events.asSharedFlow()
    
    private var currentSessionId: Long = 0
    private var monthlyFeeAmount: Double = 0.0  // For calculating full year discount
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                // Get current session
                val currentSession = settingsRepository.getCurrentSession()
                currentSessionId = currentSession?.id ?: 0
                
                // Load all students for scrolling selection with expected session dues
                val allStudents = studentRepository.getAllActiveStudents().first().map { student ->
                    // Use expected session dues instead of just ledger balance
                    val expectedDues = if (currentSessionId > 0) {
                        feeRepository.calculateExpectedSessionDues(student.id, currentSessionId)
                    } else {
                        feeRepository.getCurrentBalance(student.id)
                    }
                    StudentWithBalance(student, expectedDues)
                }
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    receiptNumber = "",  // Empty - user must enter manually
                    allStudents = allStudents
                )
                
                // Pre-select student if provided
                if (preSelectedStudentId != null) {
                    val student = studentRepository.getById(preSelectedStudentId)
                    if (student != null) {
                        val expectedDues = if (currentSessionId > 0) {
                            feeRepository.calculateExpectedSessionDues(preSelectedStudentId, currentSessionId)
                        } else {
                            feeRepository.getCurrentBalance(preSelectedStudentId)
                        }
                        selectStudent(StudentWithBalance(student, expectedDues))
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun updateReceiptNumber(value: String) {
        val filtered = value.filter { it.isDigit() }
        _state.value = _state.value.copy(
            receiptNumber = filtered,
            receiptNumberError = null,
            showDuplicateWarning = false
        )
        
        // Check for duplicate
        viewModelScope.launch {
            val number = filtered.toIntOrNull() ?: return@launch
            val exists = feeRepository.receiptNumberExists(number)
            _state.value = _state.value.copy(showDuplicateWarning = exists)
        }
    }
    
    fun updateReceiptDate(date: Long) {
        val today = getStartOfToday()
        val isFutureDate = date > today + 24 * 60 * 60 * 1000 // Allow up to end of today
        val isBackdated = date < today
        
        if (isFutureDate) {
            _state.value = _state.value.copy(
                dateError = "Future dated receipts are not allowed",
                isBackdatedReceipt = false,
                showBackdateWarning = false
            )
        } else {
            _state.value = _state.value.copy(
                receiptDate = date,
                dateError = null,
                isBackdatedReceipt = isBackdated,
                showBackdateWarning = isBackdated  // Show warning for backdated receipts
            )
        }
    }
    
    fun dismissBackdateWarning() {
        _state.value = _state.value.copy(showBackdateWarning = false)
    }
    
    /**
     * Get start of today (midnight) in milliseconds
     */
    private fun getStartOfToday(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun searchStudents(query: String) {
        _state.value = _state.value.copy(studentSearchQuery = query)
        
        if (query.length >= 2) {
            viewModelScope.launch {
                studentRepository.searchStudents(query).first().let { students ->
                    val studentsWithBalance = students.map { student ->
                        // Use calculateExpectedSessionDues for accurate current session dues
                        val balance = feeRepository.calculateExpectedSessionDues(student.id, currentSessionId)
                        StudentWithBalance(student, balance)
                    }
                    _state.value = _state.value.copy(searchResults = studentsWithBalance)
                }
            }
        } else {
            _state.value = _state.value.copy(searchResults = emptyList())
        }
    }
    
    fun selectStudent(studentWithBalance: StudentWithBalance) {
        viewModelScope.launch {
            val student = studentWithBalance.student
            val className = student.currentClass
            
            // Get monthly fee for discount calculation (if monthly class)
            val isMonthlyClass = className in FeeStructure.MONTHLY_FEE_CLASSES
            if (isMonthlyClass) {
                val monthlyFee = feeRepository.getFeeForClass(currentSessionId, className, FeeType.MONTHLY)
                monthlyFeeAmount = monthlyFee?.amount ?: 0.0
            } else {
                monthlyFeeAmount = 0.0  // No monthly discount for annual classes
            }
            
            _state.value = _state.value.copy(
                selectedStudent = studentWithBalance,
                studentSearchQuery = "",
                searchResults = emptyList(),
                currentDues = studentWithBalance.currentBalance,
                fullYearDiscountAmount = monthlyFeeAmount,  // 1 month discount
                amountReceived = "",
                isFullYearPayment = false,
                subtotal = 0.0,
                discount = 0.0,
                total = 0.0,
                remainingDues = studentWithBalance.currentBalance
            )
        }
    }
    
    fun clearStudent() {
        _state.value = _state.value.copy(
            selectedStudent = null,
            currentDues = 0.0,
            amountReceived = "",
            isFullYearPayment = false,
            subtotal = 0.0,
            discount = 0.0,
            total = 0.0,
            remainingDues = 0.0
        )
    }
    
    fun updateAmountReceived(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _state.value = _state.value.copy(amountReceived = filtered)
        recalculateTotals()
    }
    
    fun toggleFullYearPayment(isFullYear: Boolean) {
        _state.value = _state.value.copy(isFullYearPayment = isFullYear)
        recalculateTotals()
    }
    
    fun updateDetails(value: String) {
        _state.value = _state.value.copy(details = value)
    }
    
    fun updatePaymentMode(mode: PaymentMode) {
        _state.value = _state.value.copy(paymentMode = mode)
    }
    
    fun updateOnlineReference(value: String) {
        _state.value = _state.value.copy(onlineReference = value)
    }
    
    private fun recalculateTotals() {
        val currentState = _state.value
        val amount = currentState.amountReceived.toDoubleOrNull() ?: 0.0
        
        // Apply discount only if full year payment is checked AND there's a monthly fee discount available
        val discount = if (currentState.isFullYearPayment && monthlyFeeAmount > 0) {
            monthlyFeeAmount  // 1 month free
        } else {
            0.0
        }
        
        val total = amount
        val effectivePayment = amount + discount  // Actual dues cleared (payment + discount)
        val remainingDues = (currentState.currentDues - effectivePayment).coerceAtLeast(0.0)
        
        _state.value = currentState.copy(
            subtotal = amount,
            discount = discount,
            total = total,
            remainingDues = remainingDues
        )
    }
    
    fun saveReceipt() {
        viewModelScope.launch {
            try {
                // Validate
                if (_state.value.receiptNumber.isBlank()) {
                    _state.value = _state.value.copy(receiptNumberError = "Receipt number is required")
                    return@launch
                }
                
                val receiptNumber = _state.value.receiptNumber.toIntOrNull()
                if (receiptNumber == null) {
                    _state.value = _state.value.copy(receiptNumberError = "Invalid receipt number")
                    return@launch
                }
                
                if (_state.value.selectedStudent == null) {
                    _events.emit(CollectFeeEvent.Error("Please select a student"))
                    return@launch
                }
                
                val amount = _state.value.amountReceived.toDoubleOrNull() ?: 0.0
                if (amount <= 0) {
                    _events.emit(CollectFeeEvent.Error("Please enter a valid amount"))
                    return@launch
                }
                
                // Validate full year discount - should only be allowed if paying at least 11 months tuition
                if (_state.value.isFullYearPayment && monthlyFeeAmount > 0) {
                    val minimumForFullYear = monthlyFeeAmount * 11
                    if (amount < minimumForFullYear) {
                        _events.emit(CollectFeeEvent.Error("Full year discount requires minimum payment of ₹${minimumForFullYear.toInt()} (11 months tuition)"))
                        return@launch
                    }
                }
                
                _state.value = _state.value.copy(isSaving = true)
                
                val student = _state.value.selectedStudent!!.student
                val discount = _state.value.discount
                
                // Build description for receipt
                val description = buildString {
                    append("Fee Payment")
                    if (_state.value.isFullYearPayment) {
                        append(" (Full Year)")
                    }
                    if (_state.value.details.isNotBlank()) {
                        append(" - ${_state.value.details}")
                    }
                }
                
                // Create receipt
                // totalAmount = Total dues cleared (includes discount value)
                // netAmount = Actual money received from parent
                // Example: If fee is ₹12000, pay ₹11000 with full year discount of ₹1000
                //          totalAmount = 12000, discountAmount = 1000, netAmount = 11000
                val receipt = Receipt(
                    receiptNumber = receiptNumber,
                    studentId = student.id,
                    sessionId = currentSessionId,
                    receiptDate = _state.value.receiptDate,
                    totalAmount = amount + discount,  // Total dues cleared
                    discountAmount = discount,
                    netAmount = amount,  // Actual money received
                    paymentMode = _state.value.paymentMode,
                    onlineReference = if (_state.value.paymentMode == PaymentMode.ONLINE) _state.value.onlineReference else null,
                    remarks = _state.value.details
                )
                
                // Create single receipt item
                val items = listOf(
                    ReceiptItem(
                        feeType = "FEE_PAYMENT",
                        description = description,
                        monthYear = null,
                        amount = amount
                    )
                )
                
                // Save
                feeRepository.insertReceipt(receipt, items).onSuccess { receiptId ->
                    // Log audit
                    auditRepository.logCreate(
                        entityType = AuditLog.ENTITY_RECEIPT,
                        entityId = receiptId,
                        entityName = "Receipt #$receiptNumber"
                    )
                    
                    _events.emit(CollectFeeEvent.Success)
                }.onFailure { e ->
                    val errorMsg = when {
                        e.message?.contains("UNIQUE constraint failed") == true -> 
                            "Receipt number already exists"
                        else -> e.message ?: "Failed to save receipt"
                    }
                    _events.emit(CollectFeeEvent.Error(errorMsg))
                }
            } catch (e: Exception) {
                _events.emit(CollectFeeEvent.Error(e.message ?: "An error occurred"))
            } finally {
                _state.value = _state.value.copy(isSaving = false)
            }
        }
    }
}


