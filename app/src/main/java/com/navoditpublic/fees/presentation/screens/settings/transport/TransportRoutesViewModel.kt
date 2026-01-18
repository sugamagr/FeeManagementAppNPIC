package com.navoditpublic.fees.presentation.screens.settings.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navoditpublic.fees.data.local.dao.TransportFeeHistoryDao
import com.navoditpublic.fees.data.local.dao.TransportRouteDao
import com.navoditpublic.fees.data.local.entity.TransportFeeHistoryEntity
import com.navoditpublic.fees.domain.model.TransportRoute
import com.navoditpublic.fees.domain.repository.SettingsRepository
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

data class TransportRoutesState(
    val isLoading: Boolean = true,
    val routes: List<TransportRoute> = emptyList(),
    val activeRoutes: List<TransportRoute> = emptyList(),
    val closedRoutes: List<TransportRoute> = emptyList(),
    val studentCounts: Map<Long, Int> = emptyMap(),
    val totalStudents: Int = 0,
    val estimatedMonthlyRevenue: Double = 0.0,
    val searchQuery: String = "",
    val error: String? = null
)

sealed class TransportEvent {
    data class Success(val message: String) : TransportEvent()
    data class Error(val message: String) : TransportEvent()
}

@HiltViewModel
class TransportRoutesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val transportRouteDao: TransportRouteDao,
    private val transportFeeHistoryDao: TransportFeeHistoryDao
) : ViewModel() {
    
    private val _state = MutableStateFlow(TransportRoutesState())
    val state: StateFlow<TransportRoutesState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<TransportEvent>()
    val events: SharedFlow<TransportEvent> = _events.asSharedFlow()
    
    init {
        loadRoutes()
    }
    
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
    
    private fun loadRoutes() {
        viewModelScope.launch {
            settingsRepository.getAllRoutes().collect { routes ->
                // Load student counts for each route
                val studentCounts = mutableMapOf<Long, Int>()
                var totalStudents = 0
                var estimatedRevenue = 0.0
                
                for (route in routes) {
                    val count = settingsRepository.getStudentCountForRoute(route.id).first()
                    studentCounts[route.id] = count
                    if (!route.isClosed) {
                        totalStudents += count
                        // Use average fee for revenue estimation
                        val avgFee = (route.feeNcTo5 + route.fee6To8 + route.fee9To12) / 3
                        estimatedRevenue += avgFee * count
                    }
                }
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    routes = routes,
                    activeRoutes = routes.filter { !it.isClosed },
                    closedRoutes = routes.filter { it.isClosed },
                    studentCounts = studentCounts,
                    totalStudents = totalStudents,
                    estimatedMonthlyRevenue = estimatedRevenue,
                    error = null
                )
            }
        }
    }
    
    fun addRoute(name: String, feeNcTo5: Double, fee6To8: Double, fee9To12: Double, description: String) {
        viewModelScope.launch {
            try {
                if (settingsRepository.routeNameExists(name)) {
                    _events.emit(TransportEvent.Error("Route name already exists"))
                    return@launch
                }
                
                val route = TransportRoute(
                    routeName = name.trim(),
                    monthlyFee = feeNcTo5, // Legacy field
                    feeNcTo5 = feeNcTo5,
                    fee6To8 = fee6To8,
                    fee9To12 = fee9To12,
                    description = description.trim()
                )
                
                settingsRepository.insertRoute(route).onSuccess { routeId ->
                    // Create initial fee history entry with all fee tiers
                    val feeHistory = TransportFeeHistoryEntity(
                        routeId = routeId,
                        monthlyFee = feeNcTo5, // Legacy field
                        feeNcTo5 = feeNcTo5,
                        fee6To8 = fee6To8,
                        fee9To12 = fee9To12,
                        effectiveFrom = System.currentTimeMillis(),
                        notes = "Initial fee"
                    )
                    transportFeeHistoryDao.insert(feeHistory)
                    _events.emit(TransportEvent.Success("Route added"))
                }.onFailure { e ->
                    _events.emit(TransportEvent.Error(e.message ?: "Failed to add route"))
                }
            } catch (e: Exception) {
                _events.emit(TransportEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
    
    fun updateRoute(id: Long, name: String, feeNcTo5: Double, fee6To8: Double, fee9To12: Double, description: String) {
        viewModelScope.launch {
            try {
                if (settingsRepository.routeNameExistsExcluding(name, id)) {
                    _events.emit(TransportEvent.Error("Route name already exists"))
                    return@launch
                }
                
                val existingRoute = settingsRepository.getRouteById(id)
                if (existingRoute == null) {
                    _events.emit(TransportEvent.Error("Route not found"))
                    return@launch
                }
                
                val route = existingRoute.copy(
                    routeName = name.trim(),
                    monthlyFee = feeNcTo5, // Legacy field
                    feeNcTo5 = feeNcTo5,
                    fee6To8 = fee6To8,
                    fee9To12 = fee9To12,
                    description = description.trim(),
                    updatedAt = System.currentTimeMillis()
                )
                
                settingsRepository.updateRoute(route).onSuccess {
                    _events.emit(TransportEvent.Success("Route updated"))
                }.onFailure { e ->
                    _events.emit(TransportEvent.Error(e.message ?: "Failed to update"))
                }
            } catch (e: Exception) {
                _events.emit(TransportEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
    
    /**
     * Update fee with effective date - creates a new fee history entry
     */
    fun updateFeeWithEffectiveDate(
        routeId: Long,
        feeNcTo5: Double,
        fee6To8: Double,
        fee9To12: Double,
        effectiveFrom: Long,
        notes: String = ""
    ) {
        viewModelScope.launch {
            try {
                val route = settingsRepository.getRouteById(routeId)
                if (route == null) {
                    _events.emit(TransportEvent.Error("Route not found"))
                    return@launch
                }
                
                // Create fee history entry with all fee tiers
                val historyNotes = notes.trim().ifBlank { "Fee revised" }
                val feeHistory = TransportFeeHistoryEntity(
                    routeId = routeId,
                    monthlyFee = feeNcTo5, // Legacy field
                    feeNcTo5 = feeNcTo5,
                    fee6To8 = fee6To8,
                    fee9To12 = fee9To12,
                    effectiveFrom = effectiveFrom,
                    notes = historyNotes
                )
                transportFeeHistoryDao.insert(feeHistory)
                
                // Update current route fees only if effective date is today or earlier
                if (effectiveFrom <= System.currentTimeMillis()) {
                    val updatedRoute = route.copy(
                        monthlyFee = feeNcTo5, // Legacy field
                        feeNcTo5 = feeNcTo5,
                        fee6To8 = fee6To8,
                        fee9To12 = fee9To12,
                        updatedAt = System.currentTimeMillis()
                    )
                    settingsRepository.updateRoute(updatedRoute)
                }
                
                _events.emit(TransportEvent.Success("Fee updated from ${formatDate(effectiveFrom)}"))
            } catch (e: Exception) {
                _events.emit(TransportEvent.Error(e.message ?: "Failed to update fee"))
            }
        }
    }
    
    /**
     * Close a route - marks as closed but preserves all history
     */
    fun closeRoute(routeId: Long, closeDate: Long, reason: String = "") {
        viewModelScope.launch {
            try {
                val route = settingsRepository.getRouteById(routeId)
                if (route == null) {
                    _events.emit(TransportEvent.Error("Route not found"))
                    return@launch
                }
                
                // Check if any students are still on this route
                val studentCount = settingsRepository.getStudentCountForRoute(routeId).first()
                if (studentCount > 0) {
                    _events.emit(TransportEvent.Error("$studentCount students are still on this route. Please reassign them first."))
                    return@launch
                }
                
                val closedRoute = route.copy(
                    isClosed = true,
                    closedDate = closeDate,
                    closeReason = reason.trim().ifBlank { "Route closed" },
                    updatedAt = System.currentTimeMillis()
                )
                
                settingsRepository.updateRoute(closedRoute).onSuccess {
                    _events.emit(TransportEvent.Success("Route closed"))
                }.onFailure { e ->
                    _events.emit(TransportEvent.Error(e.message ?: "Failed to close route"))
                }
            } catch (e: Exception) {
                _events.emit(TransportEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
    
    /**
     * Reopen a closed route
     */
    fun reopenRoute(routeId: Long) {
        viewModelScope.launch {
            try {
                val route = settingsRepository.getRouteById(routeId)
                if (route == null) {
                    _events.emit(TransportEvent.Error("Route not found"))
                    return@launch
                }
                
                val reopenedRoute = route.copy(
                    isClosed = false,
                    closedDate = null,
                    closeReason = null,
                    updatedAt = System.currentTimeMillis()
                )
                
                settingsRepository.updateRoute(reopenedRoute).onSuccess {
                    _events.emit(TransportEvent.Success("Route reopened"))
                }.onFailure { e ->
                    _events.emit(TransportEvent.Error(e.message ?: "Failed to reopen route"))
                }
            } catch (e: Exception) {
                _events.emit(TransportEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
    
    /**
     * Delete a route permanently - removes all history and records
     */
    fun deleteRoute(routeId: Long) {
        viewModelScope.launch {
            try {
                val route = settingsRepository.getRouteById(routeId)
                if (route == null) {
                    _events.emit(TransportEvent.Error("Route not found"))
                    return@launch
                }
                
                // Check if any students are still on this route
                val studentCount = settingsRepository.getStudentCountForRoute(routeId).first()
                if (studentCount > 0) {
                    _events.emit(TransportEvent.Error("$studentCount students are still on this route. Please reassign them first."))
                    return@launch
                }
                
                // Delete fee history first (cascade should handle this, but be explicit)
                transportFeeHistoryDao.deleteAllForRoute(routeId)
                
                // Delete the route
                settingsRepository.deleteRoute(route).onSuccess {
                    _events.emit(TransportEvent.Success("Route deleted permanently"))
                }.onFailure { e ->
                    _events.emit(TransportEvent.Error(e.message ?: "Failed to delete route"))
                }
            } catch (e: Exception) {
                _events.emit(TransportEvent.Error(e.message ?: "An error occurred"))
            }
        }
    }
    
    suspend fun getFeeHistory(routeId: Long): List<TransportFeeHistoryEntity> {
        return transportFeeHistoryDao.getFeeHistoryForRoute(routeId).first()
    }
    
    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
