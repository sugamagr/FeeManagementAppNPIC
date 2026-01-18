package com.navoditpublic.fees.presentation.screens.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.model.TransportEnrollment
import com.navoditpublic.fees.domain.model.TransportRoute
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
import javax.inject.Inject

/**
 * Data class for student with their transport information
 */
data class StudentTransportInfo(
    val student: Student,
    val enrollment: TransportEnrollment?,
    val routeName: String,
    val monthlyFee: Double,
    val startDate: Long
)

/**
 * Sort options for transport students list
 */
enum class TransportSortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    CLASS_ASC("Class (Low-High)"),
    CLASS_DESC("Class (High-Low)"),
    ROUTE_NAME("Route Name"),
    FEE_HIGH("Fee (High-Low)"),
    FEE_LOW("Fee (Low-High)"),
    DATE_RECENT("Recently Enrolled"),
    DATE_OLDEST("Oldest First")
}

data class TransportQuickState(
    val isLoading: Boolean = true,
    val enrolledStudents: List<StudentTransportInfo> = emptyList(),
    val filteredStudents: List<StudentTransportInfo> = emptyList(),
    val studentsWithoutTransport: List<Student> = emptyList(),
    val filteredStudentsWithoutTransport: List<Student> = emptyList(),
    val routes: List<TransportRoute> = emptyList(),
    val availableClasses: List<String> = emptyList(),
    val searchQuery: String = "",
    val enrollSearchQuery: String = "",
    val selectedRouteFilter: Long? = null, // null = All routes
    val selectedClassFilter: String? = null, // null = All classes
    val sortOption: TransportSortOption = TransportSortOption.CLASS_ASC,
    val totalEnrolled: Int = 0,
    val totalWithoutTransport: Int = 0,
    val error: String? = null
)

sealed class TransportQuickEvent {
    data class Success(val message: String) : TransportQuickEvent()
    data class Error(val message: String) : TransportQuickEvent()
}

@HiltViewModel
class TransportQuickViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val transportEnrollmentRepository: TransportEnrollmentRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(TransportQuickState())
    val state: StateFlow<TransportQuickState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<TransportQuickEvent>()
    val events: SharedFlow<TransportQuickEvent> = _events.asSharedFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                // Load routes
                val routes = settingsRepository.getAllActiveRoutes().first()
                
                // Load all students
                val allStudents = studentRepository.getAllActiveStudents().first()
                
                // Separate students with and without transport
                val withTransport = mutableListOf<StudentTransportInfo>()
                val withoutTransport = mutableListOf<Student>()
                
                for (student in allStudents) {
                    if (student.hasTransport && student.transportRouteId != null) {
                        val enrollment = transportEnrollmentRepository.getActiveEnrollment(student.id)
                        val route = routes.find { it.id == student.transportRouteId }
                        
                        if (route != null) {
                            withTransport.add(
                                StudentTransportInfo(
                                    student = student,
                                    enrollment = enrollment,
                                    routeName = route.routeName,
                                    monthlyFee = enrollment?.monthlyFeeAtEnrollment 
                                        ?: route.getFeeForClass(student.currentClass),
                                    startDate = enrollment?.startDate ?: student.createdAt
                                )
                            )
                        }
                    } else {
                        withoutTransport.add(student)
                    }
                }
                
                // Extract unique classes from enrolled students
                val availableClasses = withTransport
                    .map { it.student.currentClass }
                    .distinct()
                    .sortedWith(compareBy { classOrder(it) })
                
                // Sort by default (name)
                val sortedWithTransport = withTransport.sortedBy { it.student.name }
                val sortedWithoutTransport = withoutTransport.sortedBy { it.name }
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    enrolledStudents = sortedWithTransport,
                    filteredStudents = sortedWithTransport,
                    studentsWithoutTransport = sortedWithoutTransport,
                    filteredStudentsWithoutTransport = sortedWithoutTransport,
                    routes = routes,
                    availableClasses = availableClasses,
                    totalEnrolled = sortedWithTransport.size,
                    totalWithoutTransport = sortedWithoutTransport.size,
                    error = null
                )
                
                // Apply current filters after loading
                applyFiltersAndSort()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load data"
                )
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFiltersAndSort()
    }
    
    fun updateEnrollSearchQuery(query: String) {
        _state.value = _state.value.copy(enrollSearchQuery = query)
        applyEnrollFilters()
    }
    
    fun updateRouteFilter(routeId: Long?) {
        _state.value = _state.value.copy(selectedRouteFilter = routeId)
        applyFiltersAndSort()
    }
    
    fun updateClassFilter(className: String?) {
        _state.value = _state.value.copy(selectedClassFilter = className)
        applyFiltersAndSort()
    }
    
    fun updateSortOption(option: TransportSortOption) {
        _state.value = _state.value.copy(sortOption = option)
        applyFiltersAndSort()
    }
    
    private fun applyFiltersAndSort() {
        val query = _state.value.searchQuery.lowercase()
        val routeId = _state.value.selectedRouteFilter
        val classFilter = _state.value.selectedClassFilter
        val sortOption = _state.value.sortOption
        
        // Filter
        var filtered = _state.value.enrolledStudents.filter { info ->
            val matchesQuery = query.isEmpty() || 
                info.student.name.lowercase().contains(query) ||
                info.student.accountNumber.lowercase().contains(query) ||
                info.student.fatherName.lowercase().contains(query)
            
            val matchesRoute = routeId == null || info.student.transportRouteId == routeId
            val matchesClass = classFilter == null || info.student.currentClass == classFilter
            
            matchesQuery && matchesRoute && matchesClass
        }
        
        // Sort
        filtered = when (sortOption) {
            TransportSortOption.NAME_ASC -> filtered.sortedBy { it.student.name.lowercase() }
            TransportSortOption.NAME_DESC -> filtered.sortedByDescending { it.student.name.lowercase() }
            TransportSortOption.CLASS_ASC -> filtered.sortedBy { classOrder(it.student.currentClass) }
            TransportSortOption.CLASS_DESC -> filtered.sortedByDescending { classOrder(it.student.currentClass) }
            TransportSortOption.ROUTE_NAME -> filtered.sortedBy { it.routeName.lowercase() }
            TransportSortOption.FEE_HIGH -> filtered.sortedByDescending { it.monthlyFee }
            TransportSortOption.FEE_LOW -> filtered.sortedBy { it.monthlyFee }
            TransportSortOption.DATE_RECENT -> filtered.sortedByDescending { it.startDate }
            TransportSortOption.DATE_OLDEST -> filtered.sortedBy { it.startDate }
        }
        
        _state.value = _state.value.copy(filteredStudents = filtered)
    }
    
    /**
     * Helper to get class order for sorting
     */
    private fun classOrder(className: String): Int {
        return when (className.uppercase()) {
            "NC" -> 0
            "LKG" -> 1
            "UKG" -> 2
            "1ST" -> 3
            "2ND" -> 4
            "3RD" -> 5
            "4TH" -> 6
            "5TH" -> 7
            "6TH" -> 8
            "7TH" -> 9
            "8TH" -> 10
            "9TH" -> 11
            "10TH" -> 12
            "11TH" -> 13
            "12TH" -> 14
            else -> 99
        }
    }
    
    private fun applyEnrollFilters() {
        val query = _state.value.enrollSearchQuery.lowercase()
        
        val filtered = if (query.isEmpty()) {
            _state.value.studentsWithoutTransport
        } else {
            _state.value.studentsWithoutTransport.filter { student ->
                student.name.lowercase().contains(query) ||
                student.accountNumber.lowercase().contains(query) ||
                student.fatherName.lowercase().contains(query)
            }
        }
        
        _state.value = _state.value.copy(filteredStudentsWithoutTransport = filtered)
    }
    
    /**
     * Start transport for a student (new enrollment)
     */
    fun startTransport(
        student: Student,
        routeId: Long,
        startDate: Long
    ) {
        viewModelScope.launch {
            try {
                val route = _state.value.routes.find { it.id == routeId }
                if (route == null) {
                    _events.emit(TransportQuickEvent.Error("Route not found"))
                    return@launch
                }
                
                val monthlyFee = route.getFeeForClass(student.currentClass)
                
                // Create enrollment
                val enrollment = TransportEnrollment(
                    studentId = student.id,
                    routeId = routeId,
                    startDate = startDate,
                    monthlyFeeAtEnrollment = monthlyFee
                )
                
                transportEnrollmentRepository.insert(enrollment).onSuccess {
                    // Update student
                    val updatedStudent = student.copy(
                        hasTransport = true,
                        transportRouteId = routeId,
                        updatedAt = System.currentTimeMillis()
                    )
                    studentRepository.update(updatedStudent)
                    
                    _events.emit(TransportQuickEvent.Success("${student.name} enrolled in transport"))
                    loadData() // Refresh
                }.onFailure { e ->
                    _events.emit(TransportQuickEvent.Error(e.message ?: "Failed to enroll"))
                }
            } catch (e: Exception) {
                _events.emit(TransportQuickEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
    
    /**
     * Stop transport for a student
     */
    fun stopTransport(
        student: Student,
        endDate: Long
    ) {
        viewModelScope.launch {
            try {
                val activeEnrollment = transportEnrollmentRepository.getActiveEnrollment(student.id)
                if (activeEnrollment == null) {
                    _events.emit(TransportQuickEvent.Error("No active enrollment found"))
                    return@launch
                }
                
                // End enrollment
                transportEnrollmentRepository.endEnrollment(activeEnrollment.id, endDate).onSuccess {
                    // Update student
                    val updatedStudent = student.copy(
                        hasTransport = false,
                        transportRouteId = null,
                        updatedAt = System.currentTimeMillis()
                    )
                    studentRepository.update(updatedStudent)
                    
                    _events.emit(TransportQuickEvent.Success("Transport stopped for ${student.name}"))
                    loadData() // Refresh
                }.onFailure { e ->
                    _events.emit(TransportQuickEvent.Error(e.message ?: "Failed to stop transport"))
                }
            } catch (e: Exception) {
                _events.emit(TransportQuickEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
    
    /**
     * Change route for a student
     */
    fun changeRoute(
        student: Student,
        newRouteId: Long,
        changeDate: Long
    ) {
        viewModelScope.launch {
            try {
                val newRoute = _state.value.routes.find { it.id == newRouteId }
                if (newRoute == null) {
                    _events.emit(TransportQuickEvent.Error("Route not found"))
                    return@launch
                }
                
                // End current enrollment
                val activeEnrollment = transportEnrollmentRepository.getActiveEnrollment(student.id)
                if (activeEnrollment != null) {
                    // End previous day
                    val previousDay = changeDate - 86400000 // 1 day in ms
                    transportEnrollmentRepository.endEnrollment(activeEnrollment.id, previousDay)
                }
                
                // Create new enrollment
                val monthlyFee = newRoute.getFeeForClass(student.currentClass)
                val newEnrollment = TransportEnrollment(
                    studentId = student.id,
                    routeId = newRouteId,
                    startDate = changeDate,
                    monthlyFeeAtEnrollment = monthlyFee
                )
                
                transportEnrollmentRepository.insert(newEnrollment).onSuccess {
                    // Update student
                    val updatedStudent = student.copy(
                        transportRouteId = newRouteId,
                        updatedAt = System.currentTimeMillis()
                    )
                    studentRepository.update(updatedStudent)
                    
                    _events.emit(TransportQuickEvent.Success("Route changed for ${student.name}"))
                    loadData() // Refresh
                }.onFailure { e ->
                    _events.emit(TransportQuickEvent.Error(e.message ?: "Failed to change route"))
                }
            } catch (e: Exception) {
                _events.emit(TransportQuickEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
    
    /**
     * Get transport history for a student
     */
    suspend fun getTransportHistory(studentId: Long, studentClass: String) = 
        transportEnrollmentRepository.getEnrollmentsWithRoute(studentId, studentClass)
    
    fun refresh() {
        loadData()
    }
}
