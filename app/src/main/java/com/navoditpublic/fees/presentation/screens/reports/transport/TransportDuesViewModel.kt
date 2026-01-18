package com.navoditpublic.fees.presentation.screens.reports.transport

import android.content.Context
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

// Sort options for transport report
enum class TransportSortOption(val displayName: String) {
    DUES_HIGH_TO_LOW("Dues: High to Low"),
    DUES_LOW_TO_HIGH("Dues: Low to High"),
    NAME_A_TO_Z("Name: A to Z"),
    NAME_Z_TO_A("Name: Z to A"),
    ROUTE("By Route"),
    CLASS("By Class")
}

// Due status filter
enum class DueStatusFilter(val displayName: String) {
    ALL("All Students"),
    WITH_DUES("With Dues"),
    FULLY_PAID("Fully Paid")
}


// Student data for transport dues
data class TransportStudentData(
    val student: Student,
    val routeId: Long,
    val routeName: String,
    val monthlyFee: Double,
    val totalFee: Double,        // 11 months
    val paidAmount: Double,
    val netDues: Double,
    val isPaid: Boolean
) {
    val collectionRate: Float
        get() = if (totalFee > 0) (paidAmount / totalFee).toFloat().coerceIn(0f, 1f) else 1f
}

// Route summary data
data class RouteSummary(
    val routeId: Long,
    val routeName: String,
    val studentCount: Int,
    val expectedTotal: Double,
    val collectedTotal: Double,
    val pendingTotal: Double,
    val collectionRate: Float,
    val isExpanded: Boolean = false
)

// Main state
data class TransportDuesState(
    val isLoading: Boolean = true,
    
    // Data
    val allStudents: List<TransportStudentData> = emptyList(),
    val filteredStudents: List<TransportStudentData> = emptyList(),
    val routeSummaries: List<RouteSummary> = emptyList(),
    val routes: List<TransportRoute> = emptyList(),
    val classes: List<String> = emptyList(),
    
    // Metrics
    val totalTransportStudents: Int = 0,
    val totalStudentsInSchool: Int = 0,
    val totalExpected: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalDues: Double = 0.0,
    val activeRoutesCount: Int = 0,
    val overallCollectionRate: Float = 0f,
    
    // Filters
    val selectedRoutes: Set<Long> = emptySet(),  // empty = all selected
    val selectedClass: String = "All",
    val dueStatusFilter: DueStatusFilter = DueStatusFilter.ALL,
    val searchQuery: String = "",
    val sortOption: TransportSortOption = TransportSortOption.DUES_HIGH_TO_LOW,
    
    // Session
    val sessionName: String = "",
    val sessionId: Long = 0L,
    
    val error: String? = null
)

@HiltViewModel
class TransportDuesViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val feeRepository: FeeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(TransportDuesState())
    val state: StateFlow<TransportDuesState> = _state.asStateFlow()
    
    companion object {
        private val CLASSES_NC_TO_5 = listOf("NC", "LKG", "UKG", "1st", "2nd", "3rd", "4th", "5th")
        private val CLASSES_6_TO_8 = listOf("6th", "7th", "8th")
        private val CLASSES_9_TO_12 = listOf("9th", "10th", "11th", "12th")
        private const val TRANSPORT_MONTHS = 11  // June excluded
    }
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val currentSession = settingsRepository.getCurrentSession()
                val sessionId = currentSession?.id ?: 0L
                val routes = settingsRepository.getAllActiveRoutes().first()
                val allStudents = studentRepository.getAllActiveStudents().first()
                
                // Filter students with transport
                val transportStudents = allStudents.filter { it.hasTransport && it.transportRouteId != null }
                
                // Calculate transport dues for each student
                val studentDataList = transportStudents.mapNotNull { student ->
                    val route = routes.find { it.id == student.transportRouteId } ?: return@mapNotNull null
                    val monthlyFee = route.getFeeForClass(student.currentClass)
                    val totalFee = monthlyFee * TRANSPORT_MONTHS
                    
                    // Get transport-specific payments
                    // Calculate based on expected fees and payments ratio
                    // Use ledger as single source of truth - includes all fees (opening balance, tuition, transport, admission)
                    val totalExpectedFee = feeRepository.getTotalDebits(student.id)
                    
                    // Transport dues ratio (transport fee / total expected fee)
                    val transportRatio = if (totalExpectedFee > 0) totalFee / totalExpectedFee else 0.0
                    
                    // Estimate transport payment based on ratio
                    val totalPaid = feeRepository.getTotalCredits(student.id)
                    val estimatedTransportPaid = (totalPaid * transportRatio).coerceAtMost(totalFee)
                    val transportDues = (totalFee - estimatedTransportPaid).coerceAtLeast(0.0)
                    
                    TransportStudentData(
                        student = student,
                        routeId = route.id,
                        routeName = route.routeName,
                        monthlyFee = monthlyFee,
                        totalFee = totalFee,
                        paidAmount = estimatedTransportPaid,
                        netDues = transportDues,
                        isPaid = transportDues <= 0
                    )
                }
                
                // Calculate route summaries
                val routeSummaries = routes.map { route ->
                    val routeStudents = studentDataList.filter { it.routeId == route.id }
                    val expected = routeStudents.sumOf { it.totalFee }
                    val collected = routeStudents.sumOf { it.paidAmount }
                    val pending = routeStudents.sumOf { it.netDues }
                    
                    RouteSummary(
                        routeId = route.id,
                        routeName = route.routeName,
                        studentCount = routeStudents.size,
                        expectedTotal = expected,
                        collectedTotal = collected,
                        pendingTotal = pending,
                        collectionRate = if (expected > 0) (collected / expected).toFloat() else 1f
                    )
                }.filter { it.studentCount > 0 }
                    .sortedByDescending { it.pendingTotal }
                
                // Calculate overall metrics
                val totalExpected = studentDataList.sumOf { it.totalFee }
                val totalCollected = studentDataList.sumOf { it.paidAmount }
                val totalDues = studentDataList.sumOf { it.netDues }
                
                // Get unique classes from transport students
                val uniqueClasses = studentDataList.map { it.student.currentClass }
                    .distinct()
                    .sortedWith(compareBy { getClassSortOrder(it) })
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    allStudents = studentDataList,
                    routes = routes,
                    routeSummaries = routeSummaries,
                    classes = listOf("All") + uniqueClasses,
                    totalTransportStudents = studentDataList.size,
                    totalStudentsInSchool = allStudents.size,
                    totalExpected = totalExpected,
                    totalCollected = totalCollected,
                    totalDues = totalDues,
                    activeRoutesCount = routeSummaries.size,
                    overallCollectionRate = if (totalExpected > 0) (totalCollected / totalExpected).toFloat() else 1f,
                    sessionName = currentSession?.sessionName ?: "Current Session",
                    sessionId = sessionId,
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
    
    fun toggleRouteSelection(routeId: Long) {
        val current = _state.value.selectedRoutes.toMutableSet()
        if (current.contains(routeId)) {
            current.remove(routeId)
        } else {
            current.add(routeId)
        }
        _state.value = _state.value.copy(selectedRoutes = current)
        applyFilters()
    }
    
    fun selectAllRoutes() {
        _state.value = _state.value.copy(selectedRoutes = emptySet())
        applyFilters()
    }
    
    fun updateSelectedClass(className: String) {
        _state.value = _state.value.copy(selectedClass = className)
        applyFilters()
    }
    
    fun updateDueStatusFilter(filter: DueStatusFilter) {
        _state.value = _state.value.copy(dueStatusFilter = filter)
        applyFilters()
    }
    
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun updateSortOption(option: TransportSortOption) {
        _state.value = _state.value.copy(sortOption = option)
        applyFilters()
    }
    
    fun toggleRouteExpanded(routeId: Long) {
        val updatedSummaries = _state.value.routeSummaries.map { summary ->
            if (summary.routeId == routeId) {
                summary.copy(isExpanded = !summary.isExpanded)
            } else {
                summary
            }
        }
        _state.value = _state.value.copy(routeSummaries = updatedSummaries)
    }
    
    private fun applyFilters() {
        var filtered = _state.value.allStudents
        
        // Route filter
        if (_state.value.selectedRoutes.isNotEmpty()) {
            filtered = filtered.filter { it.routeId in _state.value.selectedRoutes }
        }
        
        // Class filter
        if (_state.value.selectedClass != "All") {
            filtered = filtered.filter { it.student.currentClass == _state.value.selectedClass }
        }
        
        // Due status filter
        filtered = when (_state.value.dueStatusFilter) {
            DueStatusFilter.ALL -> filtered
            DueStatusFilter.WITH_DUES -> filtered.filter { it.netDues > 0 }
            DueStatusFilter.FULLY_PAID -> filtered.filter { it.isPaid }
        }
        
        // Search query
        if (_state.value.searchQuery.isNotBlank()) {
            val query = _state.value.searchQuery.lowercase()
            filtered = filtered.filter {
                it.student.name.lowercase().contains(query) ||
                it.student.fatherName.lowercase().contains(query) ||
                it.student.accountNumber.lowercase().contains(query) ||
                it.routeName.lowercase().contains(query)
            }
        }
        
        // Sorting
        filtered = when (_state.value.sortOption) {
            TransportSortOption.DUES_HIGH_TO_LOW -> filtered.sortedByDescending { it.netDues }
            TransportSortOption.DUES_LOW_TO_HIGH -> filtered.sortedBy { it.netDues }
            TransportSortOption.NAME_A_TO_Z -> filtered.sortedBy { it.student.name.lowercase() }
            TransportSortOption.NAME_Z_TO_A -> filtered.sortedByDescending { it.student.name.lowercase() }
            TransportSortOption.ROUTE -> filtered.sortedBy { it.routeName }
            TransportSortOption.CLASS -> filtered.sortedWith(compareBy { getClassSortOrder(it.student.currentClass) })
        }
        
        _state.value = _state.value.copy(filteredStudents = filtered)
    }
    
    private fun getClassSortOrder(className: String): Int {
        return when {
            className in CLASSES_NC_TO_5 -> CLASSES_NC_TO_5.indexOf(className)
            className in CLASSES_6_TO_8 -> 8 + CLASSES_6_TO_8.indexOf(className)
            className in CLASSES_9_TO_12 -> 11 + CLASSES_9_TO_12.indexOf(className)
            else -> 100
        }
    }
    
    // Export functions
    fun exportToPdf(context: Context) {
        viewModelScope.launch {
            try {
                val headers = listOf("A/C No", "Name", "Class", "Father", "Route", "Fee", "Paid", "Dues")
                val rows = _state.value.filteredStudents.map { data ->
                    listOf(
                        data.student.accountNumber,
                        data.student.name,
                        data.student.currentClass,
                        data.student.fatherName,
                        data.routeName,
                        "₹${data.totalFee.toLong()}",
                        "₹${data.paidAmount.toLong()}",
                        "₹${data.netDues.toLong()}"
                    )
                }
                
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val fileName = "TransportDues_$timestamp.pdf"
                
                com.navoditpublic.fees.util.PdfGenerator.generateReport(
                    context = context,
                    title = "Transport Dues Report",
                    headers = headers,
                    rows = rows,
                    summary = mapOf(
                        "Total Students" to _state.value.filteredStudents.size.toString(),
                        "Total Dues" to "₹${_state.value.filteredStudents.sumOf { it.netDues }.toLong()}",
                        "Session" to _state.value.sessionName
                    ),
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
    
    fun exportToExcel(context: Context) {
        viewModelScope.launch {
            try {
                val headers = listOf("A/C No", "Name", "Class", "Father Name", "Route", "Monthly Fee", "Total Fee", "Paid", "Dues", "Phone")
                val rows = _state.value.filteredStudents.map { data ->
                    listOf(
                        data.student.accountNumber,
                        data.student.name,
                        data.student.currentClass,
                        data.student.fatherName,
                        data.routeName,
                        data.monthlyFee.toLong().toString(),
                        data.totalFee.toLong().toString(),
                        data.paidAmount.toLong().toString(),
                        data.netDues.toLong().toString(),
                        data.student.phonePrimary
                    )
                }
                
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val fileName = "TransportDues_$timestamp.csv"
                
                // Numeric columns: Monthly Fee (5), Total Fee (6), Paid (7), Dues (8)
                com.navoditpublic.fees.util.ExcelGenerator.generateReport(
                    context = context,
                    title = "Transport Dues Report",
                    headers = headers,
                    rows = rows,
                    summary = mapOf(
                        "Total Students" to _state.value.filteredStudents.size.toString(),
                        "Total Expected" to _state.value.filteredStudents.sumOf { it.totalFee }.toLong().toString(),
                        "Total Collected" to _state.value.filteredStudents.sumOf { it.paidAmount }.toLong().toString(),
                        "Total Dues" to _state.value.filteredStudents.sumOf { it.netDues }.toLong().toString(),
                        "Session" to _state.value.sessionName
                    ),
                    fileName = fileName,
                    numericColumns = setOf(5, 6, 7, 8)
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
    
    fun getStudentsForRoute(routeId: Long): List<TransportStudentData> {
        return _state.value.filteredStudents.filter { it.routeId == routeId }
    }
}
