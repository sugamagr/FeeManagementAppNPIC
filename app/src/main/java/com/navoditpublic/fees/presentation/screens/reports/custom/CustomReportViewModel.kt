package com.navoditpublic.fees.presentation.screens.reports.custom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.model.TransportRoute
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Column categories for visual grouping
 */
enum class ColumnCategory(val displayName: String) {
    IDENTITY("Identity"),
    FAMILY("Family"),
    ACADEMIC("Academic"),
    LOCATION("Location"),
    CONTACT("Contact"),
    TRANSPORT("Transport"),
    FINANCIAL("Financial")
}

/**
 * Available columns for custom report - organized by category
 */
enum class ReportColumn(
    val displayName: String, 
    val key: String,
    val category: ColumnCategory,
    val isFinancial: Boolean = false
) {
    // Identity
    SR_NUMBER("SR Number", "sr_number", ColumnCategory.IDENTITY),
    ACCOUNT_NUMBER("Account #", "account_number", ColumnCategory.IDENTITY),
    NAME("Student Name", "name", ColumnCategory.IDENTITY),
    
    // Family
    FATHER_NAME("Father's Name", "father_name", ColumnCategory.FAMILY),
    MOTHER_NAME("Mother's Name", "mother_name", ColumnCategory.FAMILY),
    
    // Academic
    CLASS_SECTION("Class & Section", "class_section", ColumnCategory.ACADEMIC),
    ADMISSION_DATE("Admission Date", "admission_date", ColumnCategory.ACADEMIC),
    
    // Location
    VILLAGE("Village", "village", ColumnCategory.LOCATION),
    DISTRICT("District", "district", ColumnCategory.LOCATION),
    
    // Contact
    PHONE("Phone Number", "phone", ColumnCategory.CONTACT),
    PHONE_SECONDARY("Phone 2", "phone_secondary", ColumnCategory.CONTACT),
    
    // Transport
    TRANSPORT_ROUTE("Transport Route", "transport", ColumnCategory.TRANSPORT),
    TRANSPORT_FEE("Transport Fee", "transport_fee", ColumnCategory.TRANSPORT, true),
    
    // Financial - Collection focused
    SESSION_FEES("Session Fees", "session_fees", ColumnCategory.FINANCIAL, true),
    TOTAL_PAID("Total Paid", "total_paid", ColumnCategory.FINANCIAL, true),
    DUES("Pending Dues", "dues", ColumnCategory.FINANCIAL, true),
    COLLECTION_RATE("Collection %", "collection_rate", ColumnCategory.FINANCIAL, true),
    LAST_PAYMENT("Last Payment", "last_payment", ColumnCategory.FINANCIAL),
    PAYMENTS_COUNT("Receipts", "payments_count", ColumnCategory.FINANCIAL)
}

/**
 * Report type presets for quick filtering
 */
enum class ReportPreset(val displayName: String) {
    ALL_STUDENTS("All Students"),
    WITH_DUES("With Dues"),
    FULLY_PAID("Fully Paid"),
    HIGH_DUES("High Dues"),
    WITH_TRANSPORT("Transport"),
    RECENT_PAYMENTS("Recent Pay")
}

/**
 * Sort options for the report
 */
enum class ReportSortOption(val displayName: String) {
    NAME_ASC("Name A-Z"),
    NAME_DESC("Name Z-A"),
    CLASS_ASC("Class ↑"),
    CLASS_DESC("Class ↓"),
    DUES_HIGH("Dues High→Low"),
    DUES_LOW("Dues Low→High"),
    COLLECTION_HIGH("Collection High→Low"),
    COLLECTION_LOW("Collection Low→High"),
    RECENT_PAYMENT("Recent Payment")
}

data class CustomReportState(
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    
    // Filters
    val selectedClass: String = "All",
    val availableClasses: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedPreset: ReportPreset = ReportPreset.ALL_STUDENTS,
    val sortOption: ReportSortOption = ReportSortOption.NAME_ASC,
    val highDuesThreshold: Double = 5000.0,
    
    // Column selection - default columns for practical use
    val selectedColumns: Set<ReportColumn> = setOf(
        ReportColumn.ACCOUNT_NUMBER,
        ReportColumn.NAME,
        ReportColumn.CLASS_SECTION,
        ReportColumn.FATHER_NAME,
        ReportColumn.PHONE,
        ReportColumn.TOTAL_PAID,
        ReportColumn.DUES
    ),
    
    // Summary stats
    val totalStudents: Int = 0,
    val totalSessionFees: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalDues: Double = 0.0,
    val collectionRate: Float = 0f,
    val studentsWithDues: Int = 0,
    val fullyPaidStudents: Int = 0,
    
    // Preview data
    val students: List<Student> = emptyList(),
    val reportData: List<Map<ReportColumn, String>> = emptyList(),
    
    // Raw numeric data for sorting/filtering
    val studentFinancials: Map<Long, StudentFinancials> = emptyMap()
)

data class StudentFinancials(
    val studentId: Long,
    val sessionFees: Double,
    val totalPaid: Double,
    val dues: Double,
    val collectionRate: Float,
    val lastPaymentDate: Long?,
    val paymentsCount: Int
)

@HiltViewModel
class CustomReportViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(CustomReportState())
    val state: StateFlow<CustomReportState> = _state.asStateFlow()
    
    private var transportRoutes: Map<Long, TransportRoute> = emptyMap()
    private var currentSessionId: Long = 0
    private var allStudentsCache: List<Student> = emptyList()
    private var allFinancialsCache: Map<Long, StudentFinancials> = emptyMap()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            // Get current session
            val session = settingsRepository.getCurrentSession()
            currentSessionId = session?.id ?: 0
            
            // Load classes
            val classes = settingsRepository.getAllActiveClasses().first()
            
            // Load transport routes
            val routes = settingsRepository.getAllRoutes().first()
            transportRoutes = routes.associateBy { it.id }
            
            // Load all students and their financials
            allStudentsCache = studentRepository.getAllActiveStudents().first()
            allFinancialsCache = loadAllFinancials(allStudentsCache)
            
            _state.value = _state.value.copy(
                isLoading = false,
                availableClasses = listOf("All") + classes,
                studentFinancials = allFinancialsCache
            )
            
            generatePreview()
        }
    }
    
    private suspend fun loadAllFinancials(students: List<Student>): Map<Long, StudentFinancials> {
        return students.associate { student ->
            // Use ledger as single source of truth
            val sessionFees = feeRepository.getTotalDebits(student.id) // All fees charged (opening balance, tuition, transport, admission)
            val totalPaid = feeRepository.getTotalCredits(student.id) // All payments
            val dues = feeRepository.getCurrentBalance(student.id) // Current balance owed
            val lastPayment = feeRepository.getLastPaymentDate(student.id, currentSessionId)
            val paymentsCount = feeRepository.getPaymentsCount(student.id, currentSessionId)
            val rate = if (sessionFees > 0) ((totalPaid / sessionFees) * 100).toFloat() else 100f
            
            student.id to StudentFinancials(
                studentId = student.id,
                sessionFees = sessionFees,
                totalPaid = totalPaid,
                dues = dues,
                collectionRate = rate.coerceIn(0f, 100f),
                lastPaymentDate = lastPayment,
                paymentsCount = paymentsCount
            )
        }
    }
    
    fun toggleColumn(column: ReportColumn) {
        val currentColumns = _state.value.selectedColumns.toMutableSet()
        if (column in currentColumns) {
            if (currentColumns.size > 1) {
                currentColumns.remove(column)
            }
        } else {
            currentColumns.add(column)
        }
        _state.value = _state.value.copy(selectedColumns = currentColumns)
        generatePreview()
    }
    
    fun selectAllColumns() {
        _state.value = _state.value.copy(selectedColumns = ReportColumn.entries.toSet())
        generatePreview()
    }
    
    fun clearAllColumns() {
        // Keep at least one column (Name)
        _state.value = _state.value.copy(selectedColumns = setOf(ReportColumn.NAME))
        generatePreview()
    }
    
    fun selectColumnsByCategory(category: ColumnCategory) {
        val categoryColumns = ReportColumn.entries.filter { it.category == category }.toSet()
        val currentColumns = _state.value.selectedColumns.toMutableSet()
        
        // If all category columns are selected, deselect them; otherwise select all
        if (currentColumns.containsAll(categoryColumns)) {
            currentColumns.removeAll(categoryColumns)
            if (currentColumns.isEmpty()) {
                currentColumns.add(ReportColumn.NAME)
            }
        } else {
            currentColumns.addAll(categoryColumns)
        }
        
        _state.value = _state.value.copy(selectedColumns = currentColumns)
        generatePreview()
    }
    
    fun setClassFilter(className: String) {
        _state.value = _state.value.copy(selectedClass = className)
        generatePreview()
    }
    
    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        generatePreview()
    }
    
    fun setPreset(preset: ReportPreset) {
        _state.value = _state.value.copy(selectedPreset = preset)
        generatePreview()
    }
    
    fun setSortOption(option: ReportSortOption) {
        _state.value = _state.value.copy(sortOption = option)
        generatePreview()
    }
    
    private fun generatePreview() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            var students = allStudentsCache
            val financials = allFinancialsCache
            
            // Apply class filter
            if (_state.value.selectedClass != "All") {
                students = students.filter { it.currentClass == _state.value.selectedClass }
            }
            
            // Apply search filter
            if (_state.value.searchQuery.isNotBlank()) {
                val query = _state.value.searchQuery.lowercase()
                students = students.filter { student ->
                    student.name.lowercase().contains(query) ||
                    student.srNumber.lowercase().contains(query) ||
                    student.fatherName.lowercase().contains(query) ||
                    student.phonePrimary.contains(query)
                }
            }
            
            // Apply preset filter
            students = when (_state.value.selectedPreset) {
                ReportPreset.ALL_STUDENTS -> students
                ReportPreset.WITH_DUES -> students.filter { 
                    (financials[it.id]?.dues ?: 0.0) > 0 
                }
                ReportPreset.FULLY_PAID -> students.filter { 
                    (financials[it.id]?.dues ?: 0.0) <= 0 
                }
                ReportPreset.HIGH_DUES -> students.filter { 
                    (financials[it.id]?.dues ?: 0.0) >= _state.value.highDuesThreshold 
                }
                ReportPreset.WITH_TRANSPORT -> students.filter { it.hasTransport }
                ReportPreset.RECENT_PAYMENTS -> {
                    val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
                    students.filter { 
                        (financials[it.id]?.lastPaymentDate ?: 0L) >= sevenDaysAgo 
                    }
                }
            }
            
            // Apply sorting
            students = when (_state.value.sortOption) {
                ReportSortOption.NAME_ASC -> students.sortedBy { it.name }
                ReportSortOption.NAME_DESC -> students.sortedByDescending { it.name }
                ReportSortOption.CLASS_ASC -> students.sortedBy { it.currentClass }
                ReportSortOption.CLASS_DESC -> students.sortedByDescending { it.currentClass }
                ReportSortOption.DUES_HIGH -> students.sortedByDescending { financials[it.id]?.dues ?: 0.0 }
                ReportSortOption.DUES_LOW -> students.sortedBy { financials[it.id]?.dues ?: 0.0 }
                ReportSortOption.COLLECTION_HIGH -> students.sortedByDescending { financials[it.id]?.collectionRate ?: 0f }
                ReportSortOption.COLLECTION_LOW -> students.sortedBy { financials[it.id]?.collectionRate ?: 0f }
                ReportSortOption.RECENT_PAYMENT -> students.sortedByDescending { financials[it.id]?.lastPaymentDate ?: 0L }
            }
            
            // Generate report data
            val reportData = students.map { student ->
                generateStudentData(student, financials[student.id])
            }
            
            // Calculate summary stats
            val filteredFinancials = students.mapNotNull { financials[it.id] }
            val totalSessionFees = filteredFinancials.sumOf { it.sessionFees }
            val totalCollected = filteredFinancials.sumOf { it.totalPaid }
            val totalDues = filteredFinancials.sumOf { it.dues }
            val overallRate = if (totalSessionFees > 0) ((totalCollected / totalSessionFees) * 100).toFloat() else 0f
            
            _state.value = _state.value.copy(
                isLoading = false,
                students = students,
                reportData = reportData,
                totalStudents = students.size,
                totalSessionFees = totalSessionFees,
                totalCollected = totalCollected,
                totalDues = totalDues,
                collectionRate = overallRate,
                studentsWithDues = filteredFinancials.count { it.dues > 0 },
                fullyPaidStudents = filteredFinancials.count { it.dues <= 0 }
            )
        }
    }
    
    private fun generateStudentData(
        student: Student, 
        financials: StudentFinancials?
    ): Map<ReportColumn, String> {
        val dateFormat = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
        val transportRoute = student.transportRouteId?.let { transportRoutes[it] }
        
        val lastPaymentStr = financials?.lastPaymentDate?.let { 
            if (it > 0) dateFormat.format(java.util.Date(it)) else "No payments" 
        } ?: "No payments"
        
        // Get class-appropriate transport fee
        val transportFee = transportRoute?.getFeeForClass(student.currentClass)
        
        return mapOf(
            ReportColumn.SR_NUMBER to student.srNumber,
            ReportColumn.ACCOUNT_NUMBER to student.accountNumber,
            ReportColumn.NAME to student.name,
            ReportColumn.FATHER_NAME to student.fatherName,
            ReportColumn.MOTHER_NAME to student.motherName,
            ReportColumn.CLASS_SECTION to "${student.currentClass}-${student.section}",
            ReportColumn.VILLAGE to student.addressLine2,
            ReportColumn.DISTRICT to student.district,
            ReportColumn.PHONE to student.phonePrimary,
            ReportColumn.PHONE_SECONDARY to student.phoneSecondary.ifBlank { "-" },
            ReportColumn.TRANSPORT_ROUTE to (transportRoute?.routeName ?: "N/A"),
            ReportColumn.TRANSPORT_FEE to (transportFee?.let { "₹${it.toInt()}/mo" } ?: "N/A"),
            ReportColumn.SESSION_FEES to "₹${String.format("%,.0f", financials?.sessionFees ?: 0.0)}",
            ReportColumn.TOTAL_PAID to "₹${String.format("%,.0f", financials?.totalPaid ?: 0.0)}",
            ReportColumn.DUES to "₹${String.format("%,.0f", financials?.dues ?: 0.0)}",
            ReportColumn.COLLECTION_RATE to "${String.format("%.0f", financials?.collectionRate ?: 0f)}%",
            ReportColumn.LAST_PAYMENT to lastPaymentStr,
            ReportColumn.PAYMENTS_COUNT to "${financials?.paymentsCount ?: 0}",
            ReportColumn.ADMISSION_DATE to dateFormat.format(java.util.Date(student.admissionDate))
        )
    }
    
    fun getHeaders(): List<String> {
        return _state.value.selectedColumns.map { it.displayName }
    }
    
    fun getRows(): List<List<String>> {
        return _state.value.reportData.map { studentData ->
            _state.value.selectedColumns.map { column ->
                studentData[column] ?: ""
            }
        }
    }
    
    fun getReportTitle(): String {
        val classFilter = if (_state.value.selectedClass == "All") "" else " - ${_state.value.selectedClass}"
        val presetText = when (_state.value.selectedPreset) {
            ReportPreset.ALL_STUDENTS -> ""
            else -> " (${_state.value.selectedPreset.displayName})"
        }
        return "Student Report$classFilter$presetText"
    }
    
    fun getFileName(): String {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            .format(java.util.Date())
        return "Custom_Report_$timestamp.pdf"
    }
    
    fun getExcelFileName(): String {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            .format(java.util.Date())
        return "Custom_Report_$timestamp.csv"
    }
    
    fun getSummaryData(): Map<String, String> {
        return linkedMapOf(
            "Total Students" to "${_state.value.totalStudents}",
            "Total Session Fees" to "₹${String.format("%,.0f", _state.value.totalSessionFees)}",
            "Total Collected" to "₹${String.format("%,.0f", _state.value.totalCollected)}",
            "Total Pending" to "₹${String.format("%,.0f", _state.value.totalDues)}",
            "Collection Rate" to "${String.format("%.1f", _state.value.collectionRate)}%"
        )
    }
    
    // Get numeric dues value for a student (for color coding)
    fun getDuesValue(studentData: Map<ReportColumn, String>): Double {
        val duesStr = studentData[ReportColumn.DUES] ?: "0"
        return duesStr.replace("₹", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0
    }
    
    // Get collection rate for a student (for progress bar)
    fun getCollectionRateValue(studentData: Map<ReportColumn, String>): Float {
        val rateStr = studentData[ReportColumn.COLLECTION_RATE] ?: "0"
        return rateStr.replace("%", "").trim().toFloatOrNull() ?: 0f
    }
}
