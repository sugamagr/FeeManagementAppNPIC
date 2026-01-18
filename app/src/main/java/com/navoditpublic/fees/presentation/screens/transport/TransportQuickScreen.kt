package com.navoditpublic.fees.presentation.screens.transport

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.model.TransportEnrollmentWithRoute
import com.navoditpublic.fees.domain.model.TransportRoute
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.theme.GradientEnd
import com.navoditpublic.fees.presentation.theme.GradientStart
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.presentation.theme.SaffronLight
import com.navoditpublic.fees.presentation.theme.Teal
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Colors - Using App Theme
private val PrimaryColor = Saffron
private val PrimaryColorDark = SaffronDark
private val PrimaryColorLight = SaffronLight
private val AccentTeal = Teal
private val AccentPurple = Color(0xFF7C3AED)
private val CardBackground = Color(0xFFFFFAF5) // Warm cream background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportQuickScreen(
    navController: NavController,
    preSelectedStudentId: Long? = null,
    preSelectedAction: String? = null, // "manage" or "enroll"
    viewModel: TransportQuickViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    
    // Sheet states
    var showEnrollPicker by remember { mutableStateOf(false) }
    var showActionSheet by remember { mutableStateOf(false) }
    var showEnrollSheet by remember { mutableStateOf(false) }
    var showChangeRouteSheet by remember { mutableStateOf(false) }
    var showStopSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    
    // Selected data
    var selectedStudentInfo by remember { mutableStateOf<StudentTransportInfo?>(null) }
    var selectedStudentForEnroll by remember { mutableStateOf<Student?>(null) }
    var selectedRouteId by remember { mutableLongStateOf(0L) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var transportHistory by remember { mutableStateOf<List<TransportEnrollmentWithRoute>>(emptyList()) }
    
    // Track if we've handled the pre-selection
    var hasHandledPreSelection by remember { mutableStateOf(false) }
    
    // LazyListState for scrolling to pre-selected student
    val listState = rememberLazyListState()
    
    // Handle pre-selected student when data is loaded
    LaunchedEffect(state.isLoading, state.filteredStudents, preSelectedStudentId, preSelectedAction) {
        if (!state.isLoading && preSelectedStudentId != null && !hasHandledPreSelection) {
            hasHandledPreSelection = true
            
            when (preSelectedAction) {
                "manage" -> {
                    // Find the enrolled student and show action sheet
                    val studentInfo = state.enrolledStudents.find { it.student.id == preSelectedStudentId }
                    if (studentInfo != null) {
                        selectedStudentInfo = studentInfo
                        showActionSheet = true
                        
                        // Scroll to the student in the filtered list
                        val index = state.filteredStudents.indexOfFirst { it.student.id == preSelectedStudentId }
                        if (index >= 0) {
                            listState.animateScrollToItem(index)
                        }
                    }
                }
                "enroll" -> {
                    // Find the student without transport and show enroll sheet
                    val student = state.studentsWithoutTransport.find { it.id == preSelectedStudentId }
                    if (student != null) {
                        selectedStudentForEnroll = student
                        selectedDate = System.currentTimeMillis()
                        showEnrollSheet = true
                    }
                }
            }
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TransportQuickEvent.Success -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is TransportQuickEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    if (state.isLoading) {
        LoadingScreen()
        return
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(GradientStart, GradientEnd))
                    )
                    .statusBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                onClick = { navController.popBackStack() },
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            
                            Spacer(Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    "Transport",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "${state.totalEnrolled} students enrolled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        
                        // Enroll button
                        Surface(
                            onClick = { showEnrollPicker = true },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Enroll",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Enroll",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Search bar
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { 
                            Text("Search enrolled students...", color = Color.White.copy(alpha = 0.6f)) 
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.7f))
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Close, null, tint = Color.White)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        singleLine = true
                    )
                }
            }
            
            // Content
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = CardBackground
            ) {
                // State for filter/sort sheets
                var showClassFilterSheet by remember { mutableStateOf(false) }
                var showSortSheet by remember { mutableStateOf(false) }
                
                Column {
                    // Filter & Sort Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Class Filter Dropdown
                        Surface(
                            onClick = { showClassFilterSheet = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (state.selectedClassFilter != null) PrimaryColor.copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (state.selectedClassFilter != null) PrimaryColor else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    state.selectedClassFilter ?: "All Classes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (state.selectedClassFilter != null) PrimaryColor else Color.DarkGray,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        // Route Filter Dropdown  
                        Surface(
                            onClick = { /* Route filter is in chips below */ },
                            shape = RoundedCornerShape(10.dp),
                            color = if (state.selectedRouteFilter != null) PrimaryColor.copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    tint = if (state.selectedRouteFilter != null) PrimaryColor else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    state.routes.find { it.id == state.selectedRouteFilter }?.routeName ?: "All Routes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (state.selectedRouteFilter != null) PrimaryColor else Color.DarkGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        // Sort Button
                        Surface(
                            onClick = { showSortSheet = true },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = AccentPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    
                    // Route filter chips (scrollable)
                    if (state.routes.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = state.selectedRouteFilter == null,
                                    onClick = { viewModel.updateRouteFilter(null) },
                                    label = { Text("All") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            items(state.routes) { route ->
                                FilterChip(
                                    selected = state.selectedRouteFilter == route.id,
                                    onClick = { viewModel.updateRouteFilter(route.id) },
                                    label = { Text(route.routeName, maxLines = 1) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                    
                    // Active filters indicator
                    val activeFilters = listOfNotNull(
                        state.selectedClassFilter?.let { "Class: $it" },
                        state.selectedRouteFilter?.let { "Route: ${state.routes.find { r -> r.id == it }?.routeName}" }
                    )
                    if (activeFilters.isNotEmpty() || state.sortOption != TransportSortOption.NAME_ASC) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${state.filteredStudents.size} students",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            if (state.sortOption != TransportSortOption.NAME_ASC) {
                                Text("•", color = Color.Gray)
                                Text(
                                    state.sortOption.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentPurple,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    // Class Filter Sheet
                    if (showClassFilterSheet) {
                        ClassFilterSheet(
                            availableClasses = state.availableClasses,
                            selectedClass = state.selectedClassFilter,
                            onClassSelect = { 
                                viewModel.updateClassFilter(it)
                                showClassFilterSheet = false
                            },
                            onDismiss = { showClassFilterSheet = false }
                        )
                    }
                    
                    // Sort Sheet
                    if (showSortSheet) {
                        SortOptionsSheet(
                            currentSort = state.sortOption,
                            onSortSelect = {
                                viewModel.updateSortOption(it)
                                showSortSheet = false
                            },
                            onDismiss = { showSortSheet = false }
                        )
                    }
                    
                    // Student list
                    if (state.filteredStudents.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryColor.copy(alpha = 0.1f),
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.DirectionsBus,
                                            contentDescription = null,
                                            tint = PrimaryColor,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    if (state.searchQuery.isNotEmpty() || state.selectedRouteFilter != null || state.selectedClassFilter != null)
                                        "No students match your filters"
                                    else
                                        "No students enrolled in transport",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    if (state.searchQuery.isNotEmpty() || state.selectedRouteFilter != null || state.selectedClassFilter != null)
                                        "Try different search or filter"
                                    else
                                        "Tap '+ Enroll' to add students",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(
                                items = state.filteredStudents,
                                key = { _, info -> info.student.id }
                            ) { index, info ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + slideInVertically { it / 2 }
                                ) {
                                    TransportStudentCard(
                                        info = info,
                                        dateFormat = dateFormat,
                                        onCardClick = {
                                            selectedStudentInfo = info
                                            showActionSheet = true
                                        },
                                        onChangeRoute = {
                                            selectedStudentInfo = info
                                            selectedRouteId = info.student.transportRouteId ?: 0L
                                            selectedDate = System.currentTimeMillis()
                                            showChangeRouteSheet = true
                                        },
                                        onStop = {
                                            selectedStudentInfo = info
                                            selectedDate = System.currentTimeMillis()
                                            showStopSheet = true
                                        },
                                        onHistory = {
                                            selectedStudentInfo = info
                                            scope.launch {
                                                transportHistory = viewModel.getTransportHistory(
                                                    info.student.id,
                                                    info.student.currentClass
                                                )
                                                showHistorySheet = true
                                            }
                                        }
                                    )
                                }
                            }
                            
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
    
    // ============ SHEETS ============
    
    // Student Picker Sheet (for enrolling new students)
    if (showEnrollPicker) {
        StudentPickerSheet(
            students = state.filteredStudentsWithoutTransport,
            totalCount = state.totalWithoutTransport,
            searchQuery = state.enrollSearchQuery,
            onSearchChange = { viewModel.updateEnrollSearchQuery(it) },
            onStudentSelect = { student ->
                selectedStudentForEnroll = student
                showEnrollPicker = false
                selectedRouteId = state.routes.firstOrNull()?.id ?: 0L
                selectedDate = System.currentTimeMillis()
                showEnrollSheet = true
            },
            onDismiss = { 
                showEnrollPicker = false 
                viewModel.updateEnrollSearchQuery("")
            }
        )
    }
    
    // Enroll Sheet (select route and date)
    if (showEnrollSheet && selectedStudentForEnroll != null) {
        EnrollTransportSheet(
            student = selectedStudentForEnroll!!,
            routes = state.routes,
            selectedRouteId = selectedRouteId,
            selectedDate = selectedDate,
            dateFormat = dateFormat,
            onRouteSelect = { selectedRouteId = it },
            onDateSelect = { selectedDate = it },
            onConfirm = {
                viewModel.startTransport(selectedStudentForEnroll!!, selectedRouteId, selectedDate)
                showEnrollSheet = false
                selectedStudentForEnroll = null
            },
            onDismiss = {
                showEnrollSheet = false
                selectedStudentForEnroll = null
            }
        )
    }
    
    // Action Sheet (for enrolled students)
    if (showActionSheet && selectedStudentInfo != null) {
        ActionBottomSheet(
            info = selectedStudentInfo!!,
            dateFormat = dateFormat,
            onChangeRoute = {
                showActionSheet = false
                selectedRouteId = selectedStudentInfo!!.student.transportRouteId ?: 0L
                selectedDate = System.currentTimeMillis()
                showChangeRouteSheet = true
            },
            onStop = {
                showActionSheet = false
                selectedDate = System.currentTimeMillis()
                showStopSheet = true
            },
            onHistory = {
                showActionSheet = false
                scope.launch {
                    transportHistory = viewModel.getTransportHistory(
                        selectedStudentInfo!!.student.id,
                        selectedStudentInfo!!.student.currentClass
                    )
                    showHistorySheet = true
                }
            },
            onDismiss = { showActionSheet = false }
        )
    }
    
    // Change Route Sheet
    if (showChangeRouteSheet && selectedStudentInfo != null) {
        ChangeRouteSheet(
            student = selectedStudentInfo!!.student,
            currentRouteId = selectedStudentInfo!!.student.transportRouteId ?: 0L,
            routes = state.routes,
            selectedRouteId = selectedRouteId,
            selectedDate = selectedDate,
            dateFormat = dateFormat,
            onRouteSelect = { selectedRouteId = it },
            onDateSelect = { selectedDate = it },
            onConfirm = {
                viewModel.changeRoute(selectedStudentInfo!!.student, selectedRouteId, selectedDate)
                showChangeRouteSheet = false
                selectedStudentInfo = null
            },
            onDismiss = {
                showChangeRouteSheet = false
                selectedStudentInfo = null
            }
        )
    }
    
    // Stop Transport Sheet
    if (showStopSheet && selectedStudentInfo != null) {
        StopTransportSheet(
            student = selectedStudentInfo!!.student,
            routeName = selectedStudentInfo!!.routeName,
            selectedDate = selectedDate,
            dateFormat = dateFormat,
            onDateSelect = { selectedDate = it },
            onConfirm = {
                viewModel.stopTransport(selectedStudentInfo!!.student, selectedDate)
                showStopSheet = false
                selectedStudentInfo = null
            },
            onDismiss = {
                showStopSheet = false
                selectedStudentInfo = null
            }
        )
    }
    
    // History Sheet
    if (showHistorySheet && selectedStudentInfo != null) {
        HistorySheet(
            student = selectedStudentInfo!!.student,
            history = transportHistory,
            dateFormat = dateFormat,
            onDismiss = {
                showHistorySheet = false
                selectedStudentInfo = null
            }
        )
    }
}

// ============ COMPONENTS ============

@Composable
private fun TransportStudentCard(
    info: StudentTransportInfo,
    dateFormat: SimpleDateFormat,
    onCardClick: () -> Unit,
    onChangeRoute: () -> Unit,
    onStop: () -> Unit,
    onHistory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Student info
                Row(modifier = Modifier.weight(1f)) {
                    // Avatar
                    Surface(
                        shape = CircleShape,
                        color = PrimaryColor.copy(alpha = 0.1f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                info.student.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryColor
                            )
                        }
                    }
                    
                    Spacer(Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            info.student.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Class ${info.student.currentClass}-${info.student.section} • ${info.student.accountNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                // Status badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryColorLight.copy(alpha = 0.15f)
                ) {
                    Text(
                        "● Active",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryColorLight,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Route info
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            info.routeName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Since ${dateFormat.format(Date(info.startDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Text(
                        info.monthlyFee.toRupees() + "/mo",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                }
            }
            
            Spacer(Modifier.height(10.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionChip(
                    text = "Change",
                    icon = Icons.Default.SwapHoriz,
                    color = AccentTeal,
                    onClick = onChangeRoute,
                    modifier = Modifier.weight(1f)
                )
                ActionChip(
                    text = "Stop",
                    icon = Icons.Default.Close,
                    color = Color(0xFFEF4444),
                    onClick = onStop,
                    modifier = Modifier.weight(1f)
                )
                ActionChip(
                    text = "History",
                    icon = Icons.Default.History,
                    color = AccentPurple,
                    onClick = onHistory,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

// ============ SHEETS ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentPickerSheet(
    students: List<Student>,
    totalCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onStudentSelect: (Student) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Enroll Student in Transport",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$totalCount students without transport",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by name or A/C number...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            Spacer(Modifier.height(12.dp))
            
            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (searchQuery.isNotEmpty()) "No students found"
                            else "All students have transport",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(students, key = { it.id }) { student ->
                        StudentPickerItem(
                            student = student,
                            onClick = { onStudentSelect(student) }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StudentPickerItem(
    student: Student,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryColor.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        student.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    student.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Class ${student.currentClass}-${student.section} • ${student.accountNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Icon(
                Icons.Default.Add,
                contentDescription = "Select",
                tint = PrimaryColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollTransportSheet(
    student: Student,
    routes: List<TransportRoute>,
    selectedRouteId: Long,
    selectedDate: Long,
    dateFormat: SimpleDateFormat,
    onRouteSelect: (Long) -> Unit,
    onDateSelect: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDatePicker by remember { mutableStateOf(false) }
    
    val selectedRoute = routes.find { it.id == selectedRouteId }
    val fee = selectedRoute?.getFeeForClass(student.currentClass) ?: 0.0
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelect(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.DirectionsBus,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Start Transport",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${student.name} • Class ${student.currentClass}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(Modifier.height(16.dp))
            
            // Route selection
            Text(
                "Select Route",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(routes) { route ->
                    val routeFee = route.getFeeForClass(student.currentClass)
                    RouteSelectionItem(
                        route = route,
                        fee = routeFee,
                        isSelected = route.id == selectedRouteId,
                        onClick = { onRouteSelect(route.id) }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Date selection
            Text(
                "Start Date",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            
            Surface(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, null, tint = PrimaryColor)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        dateFormat.format(Date(selectedDate)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Fee info
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryColor.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Monthly Fee", fontWeight = FontWeight.Medium)
                    Text(
                        fee.toRupees(),
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    enabled = selectedRouteId > 0
                ) {
                    Icon(Icons.Default.DirectionsBus, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Enroll")
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RouteSelectionItem(
    route: TransportRoute,
    fee: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryColor.copy(alpha = 0.1f) else Color(0xFFF8FAFC),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    route.routeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                fee.toRupees() + "/mo",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PrimaryColor else Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionBottomSheet(
    info: StudentTransportInfo,
    dateFormat: SimpleDateFormat,
    onChangeRoute: () -> Unit,
    onStop: () -> Unit,
    onHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Student header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            info.student.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        info.student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Class ${info.student.currentClass}-${info.student.section} • ${info.student.accountNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Current status
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryColor.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DirectionsBus, null, tint = PrimaryColor)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(info.routeName, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Since ${dateFormat.format(Date(info.startDate))} • ${info.monthlyFee.toRupees()}/mo",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryColorLight.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "Active",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryColorLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton(
                    text = "Change Route",
                    icon = Icons.Default.SwapHoriz,
                    color = AccentTeal,
                    onClick = onChangeRoute,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    text = "Stop",
                    icon = Icons.Default.Close,
                    color = Color(0xFFEF4444),
                    onClick = onStop,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(Modifier.height(10.dp))
            
            ActionButton(
                text = "View Transport History",
                icon = Icons.Default.History,
                color = AccentPurple,
                onClick = onHistory,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeRouteSheet(
    student: Student,
    currentRouteId: Long,
    routes: List<TransportRoute>,
    selectedRouteId: Long,
    selectedDate: Long,
    dateFormat: SimpleDateFormat,
    onRouteSelect: (Long) -> Unit,
    onDateSelect: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDatePicker by remember { mutableStateOf(false) }
    
    val selectedRoute = routes.find { it.id == selectedRouteId }
    val fee = selectedRoute?.getFeeForClass(student.currentClass) ?: 0.0
    val currentRoute = routes.find { it.id == currentRouteId }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelect(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentTeal.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = AccentTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Change Route",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${student.name} • Currently: ${currentRoute?.routeName ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Route selection
            Text(
                "Select New Route",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.height(180.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(routes.filter { it.id != currentRouteId }) { route ->
                    val routeFee = route.getFeeForClass(student.currentClass)
                    RouteSelectionItem(
                        route = route,
                        fee = routeFee,
                        isSelected = route.id == selectedRouteId,
                        onClick = { onRouteSelect(route.id) }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Date selection
            Text(
                "Effective From",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            
            Surface(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, null, tint = AccentTeal)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        dateFormat.format(Date(selectedDate)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Fee info
            if (selectedRouteId != currentRouteId && selectedRouteId > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentTeal.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("New Monthly Fee", fontWeight = FontWeight.Medium)
                        Text(
                            fee.toRupees(),
                            fontWeight = FontWeight.Bold,
                            color = AccentTeal
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))
            }
            
            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    enabled = selectedRouteId > 0 && selectedRouteId != currentRouteId
                ) {
                    Text("Change Route")
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StopTransportSheet(
    student: Student,
    routeName: String,
    selectedDate: Long,
    dateFormat: SimpleDateFormat,
    onDateSelect: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showDatePicker by remember { mutableStateOf(false) }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelect(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Stop Transport",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${student.name} • $routeName",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Warning
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF3C7)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️", fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "This will end the transport enrollment for ${student.name}. The history will be preserved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF92400E)
                    )
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Date selection
            Text(
                "Last Day of Transport",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            
            Surface(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, null, tint = Color(0xFFEF4444))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        dateFormat.format(Date(selectedDate)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Stop Transport")
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistorySheet(
    student: Student,
    history: List<TransportEnrollmentWithRoute>,
    dateFormat: SimpleDateFormat,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentPurple.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Transport History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        student.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No transport history", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(history) { index, item ->
                        HistoryItem(
                            item = item,
                            dateFormat = dateFormat,
                            isFirst = index == 0,
                            isLast = index == history.lastIndex
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("Done")
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HistoryItem(
    item: TransportEnrollmentWithRoute,
    dateFormat: SimpleDateFormat,
    isFirst: Boolean,
    isLast: Boolean
) {
    val isActive = item.enrollment.isActive
    
    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(8.dp)
                        .background(Color(0xFFE5E7EB))
                )
            } else {
                Spacer(Modifier.height(8.dp))
            }
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (isActive) PrimaryColor else Color(0xFFD1D5DB),
                        CircleShape
                    )
            )
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(50.dp)
                        .background(Color(0xFFE5E7EB))
                )
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Content
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = if (isActive) PrimaryColor.copy(alpha = 0.08f) else Color(0xFFF8FAFC)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.routeName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isActive) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PrimaryColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Active",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${dateFormat.format(Date(item.enrollment.startDate))} → ${
                        item.enrollment.endDate?.let { dateFormat.format(Date(it)) } ?: "Present"
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    "${item.enrollment.monthlyFeeAtEnrollment.toRupees()}/month",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) PrimaryColor else Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============ FILTER & SORT SHEETS ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassFilterSheet(
    availableClasses: List<String>,
    selectedClass: String?,
    onClassSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Filter by Class",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // All Classes option
            ClassFilterItem(
                label = "All Classes",
                isSelected = selectedClass == null,
                onClick = { onClassSelect(null) }
            )
            
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(Modifier.height(8.dp))
            
            // Class list
            LazyColumn(
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(availableClasses) { className ->
                    ClassFilterItem(
                        label = "Class $className",
                        isSelected = selectedClass == className,
                        onClick = { onClassSelect(className) }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ClassFilterItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryColor.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) PrimaryColor else Color.DarkGray
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortOptionsSheet(
    currentSort: TransportSortOption,
    onSortSelect: (TransportSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Sort,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Sort By",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Sort options
            TransportSortOption.entries.forEach { option ->
                SortOptionItem(
                    option = option,
                    isSelected = currentSort == option,
                    onClick = { onSortSelect(option) }
                )
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SortOptionItem(
    option: TransportSortOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AccentPurple.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                option.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) AccentPurple else Color.DarkGray
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
