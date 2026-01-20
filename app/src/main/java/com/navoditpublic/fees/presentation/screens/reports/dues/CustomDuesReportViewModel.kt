package com.navoditpublic.fees.presentation.screens.reports.dues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.dao.SavedReportViewDao
import com.navoditpublic.fees.data.local.entity.SavedReportViewEntity
import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sort options for the report
enum class DuesReportSortOption(val displayName: String) {
    DUES_HIGH_TO_LOW("Dues: High to Low"),
    DUES_LOW_TO_HIGH("Dues: Low to High"),
    NAME_A_TO_Z("Name: A to Z"),
    NAME_Z_TO_A("Name: Z to A"),
    CLASS_ASC("Class: Ascending"),
    CLASS_DESC("Class: Descending"),
    ACCOUNT_NUMBER("A/C Number")
}

// Three-state transport filter
enum class TransportFilter(val displayName: String) {
    ALL("All Students"),
    WITH_TRANSPORT("Transport Only"),
    WITHOUT_TRANSPORT("Non-Transport Only")
}

// Available columns for dues report
enum class DuesReportColumn(val displayName: String, val key: String) {
    SR_NUMBER("SR Number", "sr_number"),
    ACCOUNT_NUMBER("A/C Number", "account_number"),
    STUDENT_NAME("Student Name", "student_name"),
    FATHER_NAME("Father's Name", "father_name"),
    CLASS_SECTION("Class/Section", "class_section"),
    VILLAGE("Village", "village"),
    PHONE("Phone", "phone"),
    PHONE_SECONDARY("Other Phone", "phone_secondary"),
    EXPECTED_FEE("Expected Fee", "expected_fee"),
    PAID_AMOUNT("Paid Amount", "paid_amount"),
    OPENING_BALANCE("Opening Balance", "opening_balance"),
    NET_DUES("Net Dues", "net_dues"),
    TRANSPORT_STATUS("Transport", "transport_status"),
    TRANSPORT_ROUTE("Route", "transport_route"),
    TRANSPORT_FEE("Transport Fee", "transport_fee"),
    ADMISSION_DATE("Admission Date", "admission_date")
}

data class DuesReportStudentData(
    val student: Student,
    val expectedFee: Double,
    val paidAmount: Double,
    val openingBalance: Double,
    val netDues: Double,
    val transportRouteName: String?,
    val transportFee: Double
)

data class CustomDuesReportState(
    val isLoading: Boolean = true,
    val selectedColumns: Set<DuesReportColumn> = setOf(
        DuesReportColumn.ACCOUNT_NUMBER,
        DuesReportColumn.STUDENT_NAME,
        DuesReportColumn.CLASS_SECTION,
        DuesReportColumn.FATHER_NAME,
        DuesReportColumn.NET_DUES,
        DuesReportColumn.PHONE
    ),
    val students: List<DuesReportStudentData> = emptyList(),
    val filteredStudents: List<DuesReportStudentData> = emptyList(),
    
    // Filters
    val selectedClass: String = "All",
    val showOnlyWithDues: Boolean = true,  // Default: show only students with dues
    val transportFilter: TransportFilter = TransportFilter.ALL,  // Default: show all students
    val minimumDuesAmount: Double = 0.0,
    val searchQuery: String = "",
    val sortOption: DuesReportSortOption = DuesReportSortOption.DUES_HIGH_TO_LOW,  // Default sort
    
    // Available options
    val classes: List<String> = emptyList(),
    val sessionName: String = "",
    
    // Saved Views
    val savedViews: List<SavedReportViewEntity> = emptyList(),
    val currentViewId: Long? = null,
    val showSaveDialog: Boolean = false,
    val showLoadDialog: Boolean = false,
    val viewNameInput: String = "",
    
    val error: String? = null
)

@HiltViewModel
class CustomDuesReportViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository,
    private val savedReportViewDao: SavedReportViewDao
) : ViewModel() {
    
    private val _state = MutableStateFlow(CustomDuesReportState())
    val state: StateFlow<CustomDuesReportState> = _state.asStateFlow()
    
    init {
        loadData()
        loadSavedViews()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val currentSession = settingsRepository.getCurrentSession()
                val sessionId = currentSession?.id ?: 0L
                val classes = settingsRepository.getAllActiveClasses().first()
                val routes = settingsRepository.getAllActiveRoutes().first()
                
                val allStudents = studentRepository.getAllActiveStudents().first()
                
                val studentDataList = allStudents.map { student ->
                    // Use ledger as single source of truth
                    val totalDebits = feeRepository.getTotalDebits(student.id) // All fees charged
                    val paidAmount = feeRepository.getTotalCredits(student.id) // All payments
                    val netDues = feeRepository.getCurrentBalance(student.id) // Current balance owed
                    val route = routes.find { it.id == student.transportRouteId }
                    
                    DuesReportStudentData(
                        student = student,
                        expectedFee = totalDebits,
                        paidAmount = paidAmount,
                        openingBalance = student.openingBalance, // For display purposes
                        netDues = netDues,
                        transportRouteName = route?.routeName,
                        // Use class-based transport fee, 11 months (June excluded)
                        transportFee = route?.getFeeForClass(student.currentClass)?.times(11) ?: 0.0
                    )
                }
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    students = studentDataList,
                    classes = listOf("All") + classes,
                    sessionName = currentSession?.sessionName ?: "Current Session",
                    error = null
                )
                
                applyFilters()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun loadSavedViews() {
        viewModelScope.launch {
            savedReportViewDao.getByReportType("DUES").collect { views ->
                _state.value = _state.value.copy(savedViews = views)
            }
        }
    }
    
    fun toggleColumn(column: DuesReportColumn) {
        val current = _state.value.selectedColumns.toMutableSet()
        if (current.contains(column)) {
            if (current.size > 1) { // Keep at least one column
                current.remove(column)
            }
        } else {
            current.add(column)
        }
        _state.value = _state.value.copy(selectedColumns = current, currentViewId = null)
    }
    
    fun updateSelectedClass(className: String) {
        _state.value = _state.value.copy(selectedClass = className)
        applyFilters()
    }
    
    fun toggleShowOnlyWithDues() {
        _state.value = _state.value.copy(showOnlyWithDues = !_state.value.showOnlyWithDues)
        applyFilters()
    }
    
    fun updateTransportFilter(filter: TransportFilter) {
        _state.value = _state.value.copy(transportFilter = filter)
        applyFilters()
    }
    
    fun updateMinimumDues(amount: String) {
        val amountValue = amount.toDoubleOrNull() ?: 0.0
        _state.value = _state.value.copy(minimumDuesAmount = amountValue)
        applyFilters()
    }
    
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun updateSortOption(option: DuesReportSortOption) {
        _state.value = _state.value.copy(sortOption = option)
        applyFilters()
    }
    
    private fun applyFilters() {
        val allStudents = _state.value.students
        val filtered = allStudents.filter { data ->
            val matchesClass = _state.value.selectedClass == "All" || 
                              data.student.currentClass == _state.value.selectedClass
            val matchesDues = !_state.value.showOnlyWithDues || data.netDues > 0
            // Three-state transport filter
            val matchesTransport = when (_state.value.transportFilter) {
                TransportFilter.ALL -> true // Show all students
                TransportFilter.WITH_TRANSPORT -> data.student.hasTransport // Show only transport students
                TransportFilter.WITHOUT_TRANSPORT -> !data.student.hasTransport // Show only non-transport students
            }
            val matchesMinDues = data.netDues >= _state.value.minimumDuesAmount
            val matchesSearch = _state.value.searchQuery.isBlank() ||
                              data.student.name.contains(_state.value.searchQuery, ignoreCase = true) ||
                              data.student.fatherName.contains(_state.value.searchQuery, ignoreCase = true) ||
                              data.student.srNumber.contains(_state.value.searchQuery, ignoreCase = true)
            
            matchesClass && matchesDues && matchesTransport && matchesMinDues && matchesSearch
        }
        
        // Apply sorting based on selected option
        val sorted = when (_state.value.sortOption) {
            DuesReportSortOption.DUES_HIGH_TO_LOW -> filtered.sortedByDescending { it.netDues }
            DuesReportSortOption.DUES_LOW_TO_HIGH -> filtered.sortedBy { it.netDues }
            DuesReportSortOption.NAME_A_TO_Z -> filtered.sortedBy { it.student.name.lowercase() }
            DuesReportSortOption.NAME_Z_TO_A -> filtered.sortedByDescending { it.student.name.lowercase() }
            DuesReportSortOption.CLASS_ASC -> filtered.sortedBy { it.student.currentClass }
            DuesReportSortOption.CLASS_DESC -> filtered.sortedByDescending { it.student.currentClass }
            // Numeric sorting for account numbers (1, 2, 10 instead of 1, 10, 2)
            DuesReportSortOption.ACCOUNT_NUMBER -> filtered.sortedWith(
                compareBy { it.student.accountNumber.toIntOrNull() ?: Int.MAX_VALUE }
            )
        }
        
        _state.value = _state.value.copy(filteredStudents = sorted)
    }
    
    // Save/Load View Functions
    fun showSaveDialog() {
        _state.value = _state.value.copy(showSaveDialog = true, viewNameInput = "")
    }
    
    fun showLoadDialog() {
        _state.value = _state.value.copy(showLoadDialog = true)
    }
    
    fun dismissDialogs() {
        _state.value = _state.value.copy(showSaveDialog = false, showLoadDialog = false)
    }
    
    fun updateViewNameInput(name: String) {
        _state.value = _state.value.copy(viewNameInput = name)
    }
    
    fun saveCurrentView() {
        viewModelScope.launch {
            val name = _state.value.viewNameInput.trim()
            if (name.isBlank()) return@launch
            
            val selectedColumnKeys = _state.value.selectedColumns.map { it.key }
            val filterConfig = buildString {
                append("{")
                append("\"selectedClass\":\"${_state.value.selectedClass}\",")
                append("\"showOnlyWithDues\":${_state.value.showOnlyWithDues},")
                append("\"transportFilter\":\"${_state.value.transportFilter.name}\",")
                append("\"minimumDuesAmount\":${_state.value.minimumDuesAmount}")
                append("}")
            }
            
            val view = SavedReportViewEntity(
                viewName = name,
                reportType = "DUES",
                selectedColumns = selectedColumnKeys.joinToString(","),
                filterConfig = filterConfig
            )
            
            val viewId = savedReportViewDao.insert(view)
            _state.value = _state.value.copy(
                currentViewId = viewId,
                showSaveDialog = false
            )
        }
    }
    
    fun loadView(view: SavedReportViewEntity) {
        // Parse columns
        val columnKeys = view.selectedColumns.split(",")
        val columns = columnKeys.mapNotNull { key ->
            DuesReportColumn.entries.find { it.key == key }
        }.toSet()
        
        // Parse filter config
        try {
            val filterConfig = view.filterConfig
            val selectedClass = Regex("\"selectedClass\":\"([^\"]+)\"").find(filterConfig)?.groupValues?.get(1) ?: "All"
            val showOnlyWithDues = filterConfig.contains("\"showOnlyWithDues\":true")
            val minimumDuesAmount = Regex("\"minimumDuesAmount\":([0-9.]+)").find(filterConfig)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            
            // Parse transport filter with backward compatibility
            val transportFilter = Regex("\"transportFilter\":\"([^\"]+)\"").find(filterConfig)?.groupValues?.get(1)?.let {
                try { TransportFilter.valueOf(it) } catch (e: Exception) { null }
            } ?: run {
                // Backward compatibility: convert old showOnlyWithTransport boolean to new enum
                if (filterConfig.contains("\"showOnlyWithTransport\":true")) {
                    TransportFilter.WITH_TRANSPORT
                } else if (filterConfig.contains("\"showOnlyWithTransport\":false")) {
                    TransportFilter.WITHOUT_TRANSPORT
                } else {
                    TransportFilter.ALL
                }
            }
            
            _state.value = _state.value.copy(
                selectedColumns = if (columns.isNotEmpty()) columns else _state.value.selectedColumns,
                selectedClass = selectedClass,
                showOnlyWithDues = showOnlyWithDues,
                transportFilter = transportFilter,
                minimumDuesAmount = minimumDuesAmount,
                currentViewId = view.id,
                showLoadDialog = false
            )
            
            applyFilters()
        } catch (e: Exception) {
            // If parsing fails, just load columns
            _state.value = _state.value.copy(
                selectedColumns = if (columns.isNotEmpty()) columns else _state.value.selectedColumns,
                currentViewId = view.id,
                showLoadDialog = false
            )
        }
    }
    
    fun deleteView(view: SavedReportViewEntity) {
        viewModelScope.launch {
            savedReportViewDao.delete(view)
            if (_state.value.currentViewId == view.id) {
                _state.value = _state.value.copy(currentViewId = null)
            }
        }
    }
    
    /**
     * Export the filtered data to PDF
     */
    fun exportToPdf(context: Context) {
        viewModelScope.launch {
            try {
                val headers = _state.value.selectedColumns.map { it.displayName }
                val rows = _state.value.filteredStudents.map { data ->
                    _state.value.selectedColumns.map { column ->
                        getColumnValue(data, column)
                    }
                }
                
                val totalDues = _state.value.filteredStudents.sumOf { it.netDues.coerceAtLeast(0.0) }
                val summary = mapOf(
                    "Total Students" to _state.value.filteredStudents.size.toString(),
                    "Total Dues" to formatCurrency(totalDues),
                    "Class Filter" to _state.value.selectedClass,
                    "Sort By" to _state.value.sortOption.displayName
                )
                
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val fileName = "DuesReport_$timestamp.pdf"
                
                com.navoditpublic.fees.util.PdfGenerator.generateReport(
                    context = context,
                    title = "Custom Dues Report",
                    headers = headers,
                    rows = rows,
                    summary = summary,
                    fileName = fileName
                )
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    context,
                    "Error generating PDF: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    /**
     * Export the filtered data to Excel (CSV)
     */
    fun exportToExcel(context: Context) {
        viewModelScope.launch {
            try {
                val selectedCols = _state.value.selectedColumns.toList()
                val headers = selectedCols.map { it.displayName }
                
                // Determine which columns are numeric (for proper Excel formatting)
                val numericColumns = selectedCols.mapIndexedNotNull { index, column ->
                    if (column in listOf(
                        DuesReportColumn.EXPECTED_FEE,
                        DuesReportColumn.PAID_AMOUNT,
                        DuesReportColumn.OPENING_BALANCE,
                        DuesReportColumn.NET_DUES,
                        DuesReportColumn.TRANSPORT_FEE
                    )) index else null
                }.toSet()
                
                val rows = _state.value.filteredStudents.map { data ->
                    selectedCols.map { column ->
                        getColumnValueForExcel(data, column)
                    }
                }
                
                val totalDues = _state.value.filteredStudents.sumOf { it.netDues.coerceAtLeast(0.0) }
                val summary = mapOf(
                    "Total Students" to _state.value.filteredStudents.size.toString(),
                    "Total Dues" to totalDues.toLong().toString(),
                    "Class Filter" to _state.value.selectedClass,
                    "Sort By" to _state.value.sortOption.displayName
                )
                
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val fileName = "DuesReport_$timestamp.csv"
                
                com.navoditpublic.fees.util.ExcelGenerator.generateReport(
                    context = context,
                    title = "Custom Dues Report",
                    headers = headers,
                    rows = rows,
                    summary = summary,
                    fileName = fileName,
                    numericColumns = numericColumns
                )
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    context,
                    "Error generating Excel: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun getColumnValue(data: DuesReportStudentData, column: DuesReportColumn): String {
        return when (column) {
            DuesReportColumn.SR_NUMBER -> data.student.srNumber
            DuesReportColumn.ACCOUNT_NUMBER -> data.student.accountNumber
            DuesReportColumn.STUDENT_NAME -> data.student.name
            DuesReportColumn.FATHER_NAME -> data.student.fatherName
            DuesReportColumn.CLASS_SECTION -> data.student.classSection
            DuesReportColumn.VILLAGE -> data.student.addressLine2
            DuesReportColumn.PHONE -> data.student.phonePrimary
            DuesReportColumn.PHONE_SECONDARY -> data.student.phoneSecondary.ifBlank { "-" }
            DuesReportColumn.EXPECTED_FEE -> formatCurrency(data.expectedFee)
            DuesReportColumn.PAID_AMOUNT -> formatCurrency(data.paidAmount)
            DuesReportColumn.OPENING_BALANCE -> formatCurrency(data.openingBalance)
            DuesReportColumn.NET_DUES -> formatCurrency(data.netDues)
            DuesReportColumn.TRANSPORT_STATUS -> if (data.student.hasTransport) "Yes" else "No"
            DuesReportColumn.TRANSPORT_ROUTE -> data.transportRouteName ?: "-"
            DuesReportColumn.TRANSPORT_FEE -> formatCurrency(data.transportFee)
            DuesReportColumn.ADMISSION_DATE -> {
                val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                format.format(java.util.Date(data.student.admissionDate))
            }
        }
    }
    
    /**
     * Get column value for Excel export - returns raw numeric values without currency formatting
     */
    private fun getColumnValueForExcel(data: DuesReportStudentData, column: DuesReportColumn): String {
        return when (column) {
            DuesReportColumn.SR_NUMBER -> data.student.srNumber
            DuesReportColumn.ACCOUNT_NUMBER -> data.student.accountNumber
            DuesReportColumn.STUDENT_NAME -> data.student.name
            DuesReportColumn.FATHER_NAME -> data.student.fatherName
            DuesReportColumn.CLASS_SECTION -> data.student.classSection
            DuesReportColumn.VILLAGE -> data.student.addressLine2
            DuesReportColumn.PHONE -> data.student.phonePrimary
            DuesReportColumn.PHONE_SECONDARY -> data.student.phoneSecondary.ifBlank { "-" }
            // Numeric columns - return raw numbers
            DuesReportColumn.EXPECTED_FEE -> data.expectedFee.toLong().toString()
            DuesReportColumn.PAID_AMOUNT -> data.paidAmount.toLong().toString()
            DuesReportColumn.OPENING_BALANCE -> data.openingBalance.toLong().toString()
            DuesReportColumn.NET_DUES -> data.netDues.toLong().toString()
            DuesReportColumn.TRANSPORT_FEE -> data.transportFee.toLong().toString()
            // Non-numeric columns
            DuesReportColumn.TRANSPORT_STATUS -> if (data.student.hasTransport) "Yes" else "No"
            DuesReportColumn.TRANSPORT_ROUTE -> data.transportRouteName ?: "-"
            DuesReportColumn.ADMISSION_DATE -> {
                val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                format.format(java.util.Date(data.student.admissionDate))
            }
        }
    }
    
    private fun formatCurrency(amount: Double): String {
        val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }
}

