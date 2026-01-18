package com.navoditpublic.fees.presentation.screens.students.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.entity.AuditAction
import com.navoditpublic.fees.data.local.entity.LedgerEntryType
import com.navoditpublic.fees.data.local.entity.LedgerReferenceType
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.model.AuditLog
import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.model.TransportEnrollment
import com.navoditpublic.fees.domain.model.TransportEnrollmentWithRoute
import com.navoditpublic.fees.domain.model.TransportRoute
import com.navoditpublic.fees.domain.repository.AuditRepository
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.domain.repository.TransportEnrollmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Transport enrollment entry for UI
 */
data class TransportEnrollmentUiEntry(
    val id: Long = 0,
    val routeId: Long = 0,
    val routeName: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val monthlyFee: Double = 0.0,
    val isEditing: Boolean = false,
    val isNew: Boolean = true
)

/**
 * Current session fee breakdown
 */
data class SessionFeeBreakdown(
    val monthlyOrAnnualFee: Double = 0.0,
    val monthlyOrAnnualFeeLabel: String = "Monthly Fee",
    val totalMonthlyFee: Double = 0.0,
    val registrationFee: Double = 0.0,
    val transportFee: Double = 0.0,
    val transportMonths: Int = 0,
    val totalExpected: Double = 0.0
)

data class AddEditStudentState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val studentId: Long? = null,
    
    // Form fields
    val srNumber: String = "",
    val accountNumber: String = "",
    val name: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val phonePrimary: String = "",
    val phoneSecondary: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val district: String = "",
    val state: String = "Uttar Pradesh",
    val pincode: String = "",
    val currentClass: String = "",
    val section: String = "A",
    val admissionDate: Long = System.currentTimeMillis(),
    val isBackdatedAdmission: Boolean = false,  // True if admission date is before today
    val showBackdateWarning: Boolean = false,  // Show warning dialog for backdated admission
    val admissionDateError: String? = null,  // Error for future date
    val hasTransport: Boolean = false,
    val transportRouteId: Long? = null,
    
    // Opening Balance (for data migration)
    val openingBalance: String = "",
    val openingBalanceRemarks: String = "",
    val showOpeningBalanceSection: Boolean = true, // Show for new students or editable for existing
    
    // Admission Fee
    val admissionFeePaid: Boolean = false,
    
    // Transport History
    val transportEnrollments: List<TransportEnrollmentUiEntry> = emptyList(),
    val deletedEnrollmentIds: List<Long> = emptyList(), // Track deleted enrollments for DB removal
    val showAddTransportDialog: Boolean = false,
    val editingEnrollment: TransportEnrollmentUiEntry? = null,
    
    // Current Session Fees
    val currentSession: AcademicSession? = null,
    val sessionFeeBreakdown: SessionFeeBreakdown = SessionFeeBreakdown(),
    val feesReceivedMode: String = "custom", // "custom" or "months"
    val feesReceivedAmount: String = "",
    val monthsPaid: Set<Int> = emptySet(), // 0 = April, 11 = March
    val showSessionFeeSection: Boolean = true,
    
    // Dropdown options
    val classes: List<String> = emptyList(),
    val sections: List<String> = emptyList(),
    val transportRoutes: List<TransportRoute> = emptyList(),
    
    // Validation errors
    val srNumberError: String? = null,
    val accountNumberError: String? = null,
    val nameError: String? = null,
    val fatherNameError: String? = null,
    val phonePrimaryError: String? = null,
    val pincodeError: String? = null,
    val classError: String? = null,
    val openingBalanceError: String? = null,
    
    val error: String? = null
)

sealed class AddEditStudentEvent {
    data object Success : AddEditStudentEvent()
    data class Error(val message: String) : AddEditStudentEvent()
}

@HiltViewModel
class AddEditStudentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val settingsRepository: SettingsRepository,
    private val auditRepository: AuditRepository,
    private val transportEnrollmentRepository: TransportEnrollmentRepository,
    private val feeRepository: FeeRepository
) : ViewModel() {
    
    private val studentIdArg: String? = savedStateHandle.get<String>("studentId")
    private val studentId: Long? = studentIdArg?.toLongOrNull()
    
    private val _state = MutableStateFlow(AddEditStudentState())
    val state: StateFlow<AddEditStudentState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<AddEditStudentEvent>()
    val events: SharedFlow<AddEditStudentEvent> = _events.asSharedFlow()
    
    private var originalStudent: Student? = null
    
    // Month names for display
    val monthNames = listOf(
        "April", "May", "June", "July", "August", "September",
        "October", "November", "December", "January", "February", "March"
    )
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                // Load dropdown options
                val classes = settingsRepository.getAllActiveClasses().first()
                val sections = settingsRepository.getAllActiveSections().first()
                val routes = settingsRepository.getAllActiveRoutes().first()
                val currentSession = settingsRepository.getCurrentSession()
                
                // Set default admission date to April 1 of current session
                val defaultAdmissionDate = currentSession?.startDate ?: System.currentTimeMillis()
                
                _state.value = _state.value.copy(
                    classes = classes,
                    sections = sections,
                    transportRoutes = routes,
                    currentSession = currentSession,
                    admissionDate = defaultAdmissionDate
                )
                
                // Load existing student if editing
                if (studentId != null) {
                    val student = studentRepository.getById(studentId)
                    if (student != null) {
                        originalStudent = student
                        
                        // Load transport enrollments with class-based fee
                        val enrollments = transportEnrollmentRepository.getEnrollmentsWithRoute(studentId, student.currentClass)
                        
                        _state.value = _state.value.copy(
                            isEditMode = true,
                            studentId = studentId,
                            srNumber = student.srNumber,
                            accountNumber = student.accountNumber,
                            name = student.name,
                            fatherName = student.fatherName,
                            motherName = student.motherName,
                            phonePrimary = student.phonePrimary,
                            phoneSecondary = student.phoneSecondary,
                            addressLine1 = student.addressLine1,
                            addressLine2 = student.addressLine2,
                            district = student.district,
                            state = student.state,
                            pincode = student.pincode,
                            currentClass = student.currentClass,
                            section = student.section,
                            admissionDate = student.admissionDate,
                            hasTransport = student.hasTransport,
                            transportRouteId = student.transportRouteId,
                            // Show opening balance without decimals since fees are always whole numbers
                            openingBalance = if (student.openingBalance > 0) student.openingBalance.toInt().toString() else "",
                            openingBalanceRemarks = student.openingBalanceRemarks,
                            admissionFeePaid = student.admissionFeePaid,
                            transportEnrollments = enrollments.map { it.toUiEntry() }
                        )
                        
                        // Calculate session fees for existing student
                        calculateSessionFees()
                    }
                }
                
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun TransportEnrollmentWithRoute.toUiEntry() = TransportEnrollmentUiEntry(
        id = enrollment.id,
        routeId = enrollment.routeId,
        routeName = routeName,
        startDate = enrollment.startDate,
        endDate = enrollment.endDate,
        monthlyFee = enrollment.monthlyFeeAtEnrollment,
        isEditing = false,
        isNew = false
    )
    
    // Calculate session fees based on current student class and transport
    fun calculateSessionFees() {
        viewModelScope.launch {
            val currentClass = _state.value.currentClass
            val currentSession = _state.value.currentSession ?: return@launch
            
            if (currentClass.isBlank()) {
                _state.value = _state.value.copy(
                    sessionFeeBreakdown = SessionFeeBreakdown()
                )
                return@launch
            }
            
            try {
                // Get fee structure for the class
                val feeStructures = feeRepository.getFeeStructuresForClass(currentClass, currentSession.id)
                
                // Determine if class uses monthly or annual fee
                val isHigherClass = currentClass in listOf("9th", "10th", "11th", "12th")
                
                var monthlyOrAnnualFee = 0.0
                var totalMonthlyFee = 0.0
                var registrationFee = 0.0
                var monthlyOrAnnualFeeLabel = "Monthly Fee"
                
                for (feeStructure in feeStructures) {
                    when (feeStructure.feeType.name.lowercase()) {
                        "monthly" -> {
                            monthlyOrAnnualFee = feeStructure.amount
                            // Calculate for 12 months (discount applied at payment time when full year is paid)
                            totalMonthlyFee = feeStructure.amount * 12
                            monthlyOrAnnualFeeLabel = "Monthly Fee (12 months)"
                        }
                        "annual" -> {
                            monthlyOrAnnualFee = feeStructure.amount
                            totalMonthlyFee = feeStructure.amount
                            monthlyOrAnnualFeeLabel = "Annual Fee"
                        }
                        "registration" -> {
                            if (isHigherClass) {
                                registrationFee = feeStructure.amount
                            }
                        }
                    }
                }
                
                // Calculate transport fee based on enrollments
                var transportFee = 0.0
                var transportMonths = 0
                
                if (_state.value.hasTransport && currentSession.startDate != null && currentSession.endDate != null) {
                    val result = calculateTransportFromEnrollments(
                        currentSession.startDate,
                        currentSession.endDate
                    )
                    transportMonths = result.first
                    transportFee = result.second
                }
                
                val totalExpected = totalMonthlyFee + registrationFee + transportFee
                
                _state.value = _state.value.copy(
                    sessionFeeBreakdown = SessionFeeBreakdown(
                        monthlyOrAnnualFee = monthlyOrAnnualFee,
                        monthlyOrAnnualFeeLabel = monthlyOrAnnualFeeLabel,
                        totalMonthlyFee = totalMonthlyFee,
                        registrationFee = registrationFee,
                        transportFee = transportFee,
                        transportMonths = transportMonths,
                        totalExpected = totalExpected
                    )
                )
            } catch (e: Exception) {
                // Log error for debugging but don't crash the UI
                android.util.Log.e("AddEditStudentVM", "Error calculating session fees", e)
            }
        }
    }
    
    private fun calculateTransportFromEnrollments(sessionStart: Long, sessionEnd: Long): Pair<Int, Double> {
        val enrollments = _state.value.transportEnrollments
        val currentClass = _state.value.currentClass
        
        if (enrollments.isEmpty()) {
            // Use current route if no history
            val routeId = _state.value.transportRouteId ?: return Pair(0, 0.0)
            val route = _state.value.transportRoutes.find { it.id == routeId } ?: return Pair(0, 0.0)
            // 11 months (June excluded) - use class-based fee
            val classFee = route.getFeeForClass(currentClass)
            return Pair(11, classFee * 11)
        }
        
        // Calculate based on enrollment history
        val allMonths = getMonthsInRange(sessionStart, sessionEnd)
        var totalMonths = 0
        var totalFee = 0.0
        
        for ((year, month) in allMonths) {
            // Skip June (month index 5) - no transport fee in June
            if (month == java.util.Calendar.JUNE) continue
            
            val monthStart = getMonthStart(year, month)
            val monthEnd = getMonthEnd(year, month)
            
            val activeEnrollment = enrollments.find { enrollment ->
                val enrollmentStart = enrollment.startDate
                val enrollmentEnd = enrollment.endDate ?: Long.MAX_VALUE
                enrollmentStart <= monthEnd && enrollmentEnd >= monthStart
            }
            
            if (activeEnrollment != null) {
                totalMonths++
                totalFee += activeEnrollment.monthlyFee
            }
        }
        
        return Pair(totalMonths, totalFee)
    }
    
    // Update functions for each field
    fun updateSrNumber(value: String) {
        _state.value = _state.value.copy(srNumber = value.uppercase(), srNumberError = null)
    }
    
    fun updateAccountNumber(value: String) {
        _state.value = _state.value.copy(accountNumber = value.uppercase(), accountNumberError = null)
    }
    
    fun updateName(value: String) {
        _state.value = _state.value.copy(name = value, nameError = null)
    }
    
    fun updateFatherName(value: String) {
        _state.value = _state.value.copy(fatherName = value, fatherNameError = null)
    }
    
    fun updateMotherName(value: String) {
        _state.value = _state.value.copy(motherName = value)
    }
    
    fun updatePhonePrimary(value: String) {
        val filtered = value.filter { it.isDigit() }.take(10)
        _state.value = _state.value.copy(phonePrimary = filtered, phonePrimaryError = null)
    }
    
    fun updatePhoneSecondary(value: String) {
        val filtered = value.filter { it.isDigit() }.take(10)
        _state.value = _state.value.copy(phoneSecondary = filtered)
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
    
    fun updateState(value: String) {
        _state.value = _state.value.copy(state = value)
    }
    
    fun updatePincode(value: String) {
        val filtered = value.filter { it.isDigit() }.take(6)
        _state.value = _state.value.copy(pincode = filtered, pincodeError = null)
    }
    
    fun updateClass(value: String) {
        _state.value = _state.value.copy(currentClass = value, classError = null)
        calculateSessionFees()
    }
    
    fun updateSection(value: String) {
        _state.value = _state.value.copy(section = value)
    }
    
    fun updateAdmissionDate(value: Long) {
        val today = getStartOfToday()
        val isFutureDate = value > today + 24 * 60 * 60 * 1000 // Allow up to end of today
        val isBackdated = value < today
        
        if (isFutureDate) {
            _state.value = _state.value.copy(
                admissionDateError = "Future dated admissions are not allowed",
                isBackdatedAdmission = false,
                showBackdateWarning = false
            )
        } else {
            _state.value = _state.value.copy(
                admissionDate = value,
                admissionDateError = null,
                isBackdatedAdmission = isBackdated,
                showBackdateWarning = isBackdated  // Show warning for backdated admissions
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
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun updateHasTransport(value: Boolean) {
        _state.value = _state.value.copy(
            hasTransport = value,
            transportRouteId = if (!value) null else _state.value.transportRouteId
        )
        calculateSessionFees()
    }
    
    fun updateTransportRoute(routeId: Long?) {
        _state.value = _state.value.copy(transportRouteId = routeId)
        calculateSessionFees()
    }
    
    // Opening Balance functions
    fun updateOpeningBalance(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _state.value = _state.value.copy(openingBalance = filtered, openingBalanceError = null)
    }
    
    fun updateOpeningBalanceRemarks(value: String) {
        _state.value = _state.value.copy(openingBalanceRemarks = value)
    }
    
    // Admission Fee
    fun updateAdmissionFeePaid(value: Boolean) {
        _state.value = _state.value.copy(admissionFeePaid = value)
    }
    
    // Fees Received functions
    fun updateFeesReceivedMode(mode: String) {
        _state.value = _state.value.copy(feesReceivedMode = mode)
    }
    
    fun updateFeesReceivedAmount(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _state.value = _state.value.copy(feesReceivedAmount = filtered)
    }
    
    fun toggleMonthPaid(monthIndex: Int) {
        val current = _state.value.monthsPaid.toMutableSet()
        if (current.contains(monthIndex)) {
            current.remove(monthIndex)
        } else {
            current.add(monthIndex)
        }
        _state.value = _state.value.copy(monthsPaid = current)
    }
    
    fun getCalculatedFeesReceived(): Double {
        return when (_state.value.feesReceivedMode) {
            "custom" -> _state.value.feesReceivedAmount.toDoubleOrNull() ?: 0.0
            "months" -> {
                val monthCount = _state.value.monthsPaid.size
                val breakdown = _state.value.sessionFeeBreakdown
                val isHigherClass = _state.value.currentClass in listOf("9th", "10th", "11th", "12th")
                
                if (isHigherClass) {
                    // For higher classes with annual fee, if all months selected = full fee
                    if (monthCount == 12) breakdown.totalMonthlyFee else 0.0
                } else {
                    // For monthly fee classes
                    val monthlyRate = breakdown.monthlyOrAnnualFee
                    monthlyRate * monthCount
                }
            }
            else -> 0.0
        }
    }
    
    // Transport Enrollment functions
    fun showAddTransportDialog() {
        val routes = _state.value.transportRoutes
        val selectedRoute = routes.firstOrNull()
        val studentClass = _state.value.currentClass
        _state.value = _state.value.copy(
            showAddTransportDialog = true,
            editingEnrollment = TransportEnrollmentUiEntry(
                routeId = selectedRoute?.id ?: 0,
                routeName = selectedRoute?.routeName ?: "",
                monthlyFee = selectedRoute?.getFeeForClass(studentClass) ?: 0.0,
                startDate = _state.value.currentSession?.startDate ?: System.currentTimeMillis()
            )
        )
    }
    
    fun editTransportEnrollment(enrollment: TransportEnrollmentUiEntry) {
        _state.value = _state.value.copy(
            showAddTransportDialog = true,
            editingEnrollment = enrollment.copy(isEditing = true, isNew = false)
        )
    }
    
    fun dismissTransportDialog() {
        _state.value = _state.value.copy(
            showAddTransportDialog = false,
            editingEnrollment = null
        )
    }
    
    fun updateEditingEnrollmentRoute(routeId: Long) {
        val route = _state.value.transportRoutes.find { it.id == routeId }
        val studentClass = _state.value.currentClass
        _state.value = _state.value.copy(
            editingEnrollment = _state.value.editingEnrollment?.copy(
                routeId = routeId,
                routeName = route?.routeName ?: "",
                monthlyFee = route?.getFeeForClass(studentClass) ?: 0.0
            )
        )
    }
    
    fun updateEditingEnrollmentStartDate(date: Long) {
        _state.value = _state.value.copy(
            editingEnrollment = _state.value.editingEnrollment?.copy(startDate = date)
        )
    }
    
    fun updateEditingEnrollmentEndDate(date: Long?) {
        _state.value = _state.value.copy(
            editingEnrollment = _state.value.editingEnrollment?.copy(endDate = date)
        )
    }
    
    fun saveTransportEnrollment() {
        val editing = _state.value.editingEnrollment ?: return
        
        val currentList = _state.value.transportEnrollments.toMutableList()
        
        if (editing.isNew || editing.id == 0L) {
            // Add new entry
            currentList.add(editing.copy(isNew = true))
        } else {
            // Update existing
            val index = currentList.indexOfFirst { it.id == editing.id }
            if (index >= 0) {
                currentList[index] = editing
            }
        }
        
        _state.value = _state.value.copy(
            transportEnrollments = currentList,
            showAddTransportDialog = false,
            editingEnrollment = null,
            hasTransport = currentList.any { it.endDate == null } // Active if any enrollment is open
        )
        
        calculateSessionFees()
    }
    
    fun deleteTransportEnrollment(enrollment: TransportEnrollmentUiEntry) {
        val currentList = _state.value.transportEnrollments.toMutableList()
        currentList.removeAll { it.id == enrollment.id && it.startDate == enrollment.startDate }
        
        // Track deletion for existing database enrollments (not new ones)
        val deletedIds = _state.value.deletedEnrollmentIds.toMutableList()
        if (!enrollment.isNew && enrollment.id > 0) {
            deletedIds.add(enrollment.id)
        }
        
        _state.value = _state.value.copy(
            transportEnrollments = currentList,
            deletedEnrollmentIds = deletedIds,
            hasTransport = currentList.any { it.endDate == null }
        )
        
        calculateSessionFees()
    }
    
    fun save() {
        viewModelScope.launch {
            if (!validate()) return@launch
            
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val currentSession = settingsRepository.getCurrentSession()
                val sessionId = currentSession?.id ?: 0L
                
                // Parse opening balance
                val openingBalanceAmount = _state.value.openingBalance.toDoubleOrNull() ?: 0.0
                val openingBalanceDate = currentSession?.startDate
                
                val student = Student(
                    id = studentId ?: 0,
                    srNumber = _state.value.srNumber.trim(),
                    accountNumber = _state.value.accountNumber.trim(),
                    name = _state.value.name.trim(),
                    fatherName = _state.value.fatherName.trim(),
                    motherName = _state.value.motherName.trim(),
                    phonePrimary = _state.value.phonePrimary,
                    phoneSecondary = _state.value.phoneSecondary,
                    addressLine1 = _state.value.addressLine1.trim(),
                    addressLine2 = _state.value.addressLine2.trim(),
                    district = _state.value.district.trim(),
                    state = _state.value.state.trim(),
                    pincode = _state.value.pincode,
                    currentClass = _state.value.currentClass,
                    section = _state.value.section,
                    admissionDate = _state.value.admissionDate,
                    admissionSessionId = sessionId,
                    hasTransport = _state.value.hasTransport,
                    transportRouteId = _state.value.transportRouteId,
                    openingBalance = openingBalanceAmount,
                    openingBalanceRemarks = _state.value.openingBalanceRemarks.trim(),
                    openingBalanceDate = openingBalanceDate,
                    admissionFeePaid = _state.value.admissionFeePaid
                )
                
                if (_state.value.isEditMode) {
                    studentRepository.update(student).onSuccess {
                        // Update transport enrollments
                        saveTransportEnrollments(studentId!!)
                        
                        // Log the update
                        auditRepository.logUpdate(
                            entityType = AuditLog.ENTITY_STUDENT,
                            entityId = student.id,
                            entityName = student.name,
                            fieldName = "student_details",
                            oldValue = originalStudent?.name,
                            newValue = student.name
                        )
                        _events.emit(AddEditStudentEvent.Success)
                    }.onFailure { e ->
                        _events.emit(AddEditStudentEvent.Error(e.message ?: "Failed to update student"))
                    }
                } else {
                    studentRepository.insert(student).onSuccess { newId ->
                        // Save transport enrollments
                        saveTransportEnrollments(newId)
                        
                        // Create opening balance DEBIT entry if any (previous year dues)
                        if (openingBalanceAmount > 0 && currentSession != null) {
                            feeRepository.createOpeningBalanceEntry(
                                studentId = newId,
                                sessionId = sessionId,
                                amount = openingBalanceAmount,
                                date = currentSession.startDate ?: System.currentTimeMillis(),
                                remarks = _state.value.openingBalanceRemarks.trim()
                            )
                        }
                        
                        // Add session fees for the student (tuition + transport) as DEBIT entries
                        if (currentSession != null) {
                            feeRepository.addSessionFeesForStudent(
                                studentId = newId,
                                sessionId = sessionId,
                                addTuition = true,
                                addTransport = _state.value.hasTransport
                            )
                        }
                        
                        // Create initial ledger entry for fees received if any (data migration - CREDIT entry)
                        val feesReceived = getCalculatedFeesReceived()
                        if (feesReceived > 0 && currentSession != null) {
                            feeRepository.createInitialPaymentEntry(
                                studentId = newId,
                                sessionId = sessionId,
                                amount = feesReceived,
                                date = currentSession.startDate ?: System.currentTimeMillis(),
                                description = "Fees received (data migration)"
                            )
                        }
                        
                        // Log the creation
                        auditRepository.logCreate(
                            entityType = AuditLog.ENTITY_STUDENT,
                            entityId = newId,
                            entityName = student.name
                        )
                        _events.emit(AddEditStudentEvent.Success)
                    }.onFailure { e ->
                        val errorMsg = when {
                            e.message?.contains("UNIQUE constraint failed") == true &&
                                e.message?.contains("sr_number") == true -> "SR Number already exists"
                            e.message?.contains("UNIQUE constraint failed") == true &&
                                e.message?.contains("account_number") == true -> "Account Number already exists"
                            else -> e.message ?: "Failed to add student"
                        }
                        _events.emit(AddEditStudentEvent.Error(errorMsg))
                    }
                }
            } catch (e: Exception) {
                _events.emit(AddEditStudentEvent.Error(e.message ?: "An error occurred"))
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    private suspend fun saveTransportEnrollments(studentId: Long) {
        val enrollments = _state.value.transportEnrollments
        val deletedIds = _state.value.deletedEnrollmentIds
        
        // Delete removed enrollments from database
        for (enrollmentId in deletedIds) {
            val enrollment = transportEnrollmentRepository.getById(enrollmentId)
            if (enrollment != null) {
                transportEnrollmentRepository.delete(enrollment)
            }
        }
        
        // Save/update enrollments
        for (entry in enrollments) {
            if (entry.isNew && entry.id == 0L) {
                // Create new enrollment
                val enrollment = TransportEnrollment(
                    studentId = studentId,
                    routeId = entry.routeId,
                    startDate = entry.startDate,
                    endDate = entry.endDate,
                    monthlyFeeAtEnrollment = entry.monthlyFee
                )
                transportEnrollmentRepository.insert(enrollment)
            } else if (!entry.isNew) {
                // Update existing
                val enrollment = TransportEnrollment(
                    id = entry.id,
                    studentId = studentId,
                    routeId = entry.routeId,
                    startDate = entry.startDate,
                    endDate = entry.endDate,
                    monthlyFeeAtEnrollment = entry.monthlyFee
                )
                transportEnrollmentRepository.update(enrollment)
            }
        }
    }
    
    private suspend fun validate(): Boolean {
        var isValid = true
        
        // Trim values before validation to match what will be saved
        val srNumber = _state.value.srNumber.trim()
        val accountNumber = _state.value.accountNumber.trim()
        val name = _state.value.name.trim()
        
        // SR Number validation
        if (srNumber.isBlank()) {
            _state.value = _state.value.copy(srNumberError = "SR Number is required")
            isValid = false
        } else {
            val exists = if (_state.value.isEditMode) {
                studentRepository.srNumberExistsExcluding(srNumber, studentId!!)
            } else {
                studentRepository.srNumberExists(srNumber)
            }
            if (exists) {
                _state.value = _state.value.copy(srNumberError = "SR Number already exists")
                isValid = false
            }
        }
        
        // Account Number validation
        if (accountNumber.isBlank()) {
            _state.value = _state.value.copy(accountNumberError = "Account Number is required")
            isValid = false
        } else {
            val exists = if (_state.value.isEditMode) {
                studentRepository.accountNumberExistsExcluding(accountNumber, studentId!!)
            } else {
                studentRepository.accountNumberExists(accountNumber)
            }
            if (exists) {
                _state.value = _state.value.copy(accountNumberError = "Account Number already exists")
                isValid = false
            }
        }
        
        // Name validation
        if (name.isBlank()) {
            _state.value = _state.value.copy(nameError = "Name is required")
            isValid = false
        }
        
        // Father's Name validation
        if (_state.value.fatherName.isBlank()) {
            _state.value = _state.value.copy(fatherNameError = "Father's Name is required")
            isValid = false
        }
        
        // Phone validation
        if (_state.value.phonePrimary.length != 10) {
            _state.value = _state.value.copy(phonePrimaryError = "Enter valid 10-digit phone number")
            isValid = false
        }
        
        // Pincode validation (optional but if provided must be 6 digits)
        if (_state.value.pincode.isNotBlank() && _state.value.pincode.length != 6) {
            _state.value = _state.value.copy(pincodeError = "Pincode must be 6 digits")
            isValid = false
        }
        
        // Class validation
        if (_state.value.currentClass.isBlank()) {
            _state.value = _state.value.copy(classError = "Class is required")
            isValid = false
        }
        
        // Opening balance validation (must be a valid number if provided)
        if (_state.value.openingBalance.isNotBlank()) {
            val amount = _state.value.openingBalance.toDoubleOrNull()
            if (amount == null || amount < 0) {
                _state.value = _state.value.copy(openingBalanceError = "Enter a valid amount")
                isValid = false
            }
        }
        
        return isValid
    }
    
    // Helper functions
    private fun getMonthsInRange(startDate: Long, endDate: Long): List<Pair<Int, Int>> {
        val months = mutableListOf<Pair<Int, Int>>()
        val calendar = Calendar.getInstance()
        
        calendar.timeInMillis = startDate
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        while (calendar.timeInMillis <= endDate) {
            months.add(Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)))
            calendar.add(Calendar.MONTH, 1)
        }
        
        return months
    }
    
    private fun getMonthStart(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getMonthEnd(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        return calendar.timeInMillis
    }
}
