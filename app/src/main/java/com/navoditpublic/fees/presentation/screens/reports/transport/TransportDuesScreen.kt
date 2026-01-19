package com.navoditpublic.fees.presentation.screens.reports.transport

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.theme.*
import com.navoditpublic.fees.util.toRupees

// Phone action type
private enum class PhoneActionType { CALL, WHATSAPP }

// WhatsApp green color
private val WhatsAppGreen = Color(0xFF25D366)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransportDuesScreen(
    navController: NavController,
    viewModel: TransportDuesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Pager state for two pages
    val pagerState = rememberPagerState(pageCount = { 2 })
    
    // Phone selection dialog state
    var showPhoneDialog by remember { mutableStateOf(false) }
    var selectedStudentForPhone by remember { mutableStateOf<TransportStudentData?>(null) }
    var phoneActionType by remember { mutableStateOf<PhoneActionType?>(null) }
    
    // Common phone action handlers
    val handleCallClick: (TransportStudentData) -> Unit = { student ->
        if (student.student.phonePrimary.isNotBlank() && student.student.phoneSecondary.isNotBlank()) {
            selectedStudentForPhone = student
            phoneActionType = PhoneActionType.CALL
            showPhoneDialog = true
        } else if (student.student.phonePrimary.isNotBlank()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${student.student.phonePrimary}")
            }
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
        }
    }
    
    val handleWhatsAppClick: (TransportStudentData) -> Unit = { student ->
        if (student.student.phonePrimary.isNotBlank() && student.student.phoneSecondary.isNotBlank()) {
            selectedStudentForPhone = student
            phoneActionType = PhoneActionType.WHATSAPP
            showPhoneDialog = true
        } else if (student.student.phonePrimary.isNotBlank()) {
            openWhatsApp(context, student.student.phonePrimary, student)
        } else {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
        }
    }
    
    if (state.isLoading) {
        LoadingScreen()
        return
    }
    
    Scaffold(
        containerColor = Cream,
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // FIXED: Header (always visible)
            TransportHeader(
                sessionName = state.sessionName,
                onBackClick = { navController.popBackStack() },
                onExportPdf = { viewModel.exportToPdf(context) },
                onExportExcel = { viewModel.exportToExcel(context) }
            )
            
            // FIXED: Tab Indicator (always visible below header)
            TransportTabRow(
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                routeCount = state.activeRoutesCount,
                studentCount = state.filteredStudents.size,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // SCROLLABLE: Horizontal Pager for two pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> RouteWiseSummaryPage(
                        totalStudents = state.totalTransportStudents,
                        totalInSchool = state.totalStudentsInSchool,
                        totalDues = state.totalDues,
                        activeRoutes = state.activeRoutesCount,
                        collectionRate = state.overallCollectionRate,
                        routeSummaries = state.routeSummaries,
                        getStudentsForRoute = viewModel::getStudentsForRoute,
                        onToggleExpand = viewModel::toggleRouteExpanded,
                        onCallClick = handleCallClick,
                        onWhatsAppClick = handleWhatsAppClick
                    )
                    1 -> StudentDetailsPage(
                        state = state,
                        onRouteToggle = viewModel::toggleRouteSelection,
                        onSelectAllRoutes = viewModel::selectAllRoutes,
                        onClassChange = viewModel::updateSelectedClass,
                        onDueStatusChange = viewModel::updateDueStatusFilter,
                        onSearchChange = viewModel::updateSearchQuery,
                        onSortChange = viewModel::updateSortOption,
                        onCallClick = handleCallClick,
                        onWhatsAppClick = handleWhatsAppClick
                    )
                }
            }
        }
    }
    
    // Phone Selection Dialog
    if (showPhoneDialog) {
        selectedStudentForPhone?.let { studentData ->
            phoneActionType?.let { actionType ->
                PhoneSelectionDialog(
                    student = studentData,
                    actionType = actionType,
                    onPhoneSelected = { phone ->
                        showPhoneDialog = false
                        when (actionType) {
                            PhoneActionType.CALL -> {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$phone")
                                }
                                context.startActivity(intent)
                            }
                            PhoneActionType.WHATSAPP -> {
                                openWhatsApp(context, phone, studentData)
                            }
                        }
                        selectedStudentForPhone = null
                        phoneActionType = null
                    },
                    onDismiss = {
                        showPhoneDialog = false
                        selectedStudentForPhone = null
                        phoneActionType = null
                    }
                )
            }
        }
    }
}

// Tab Row for switching between pages
@Composable
private fun TransportTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    routeCount: Int,
    studentCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Route Summary Tab
            TransportTab(
                title = "Route Summary",
                subtitle = "$routeCount routes",
                icon = Icons.Default.Route,
                isSelected = selectedTabIndex == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            
            // Student Details Tab
            TransportTab(
                title = "Student Details",
                subtitle = "$studentCount students",
                icon = Icons.Default.People,
                isSelected = selectedTabIndex == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TransportTab(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Saffron else Color.Transparent,
        animationSpec = tween(200),
        label = "tabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        animationSpec = tween(200),
        label = "tabContent"
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// Page 1: Route-wise Summary
@Composable
private fun RouteWiseSummaryPage(
    totalStudents: Int,
    totalInSchool: Int,
    totalDues: Double,
    activeRoutes: Int,
    collectionRate: Float,
    routeSummaries: List<RouteSummary>,
    getStudentsForRoute: (Long) -> List<TransportStudentData>,
    onToggleExpand: (Long) -> Unit,
    onCallClick: (TransportStudentData) -> Unit,
    onWhatsAppClick: (TransportStudentData) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Metrics Dashboard (scrolls with content)
        item {
            MetricsDashboard(
                totalStudents = totalStudents,
                totalInSchool = totalInSchool,
                totalDues = totalDues,
                activeRoutes = activeRoutes,
                collectionRate = collectionRate,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        
        // Section header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Route,
                        contentDescription = null,
                        tint = Saffron,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Route-wise Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Saffron.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "$activeRoutes routes",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Saffron,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        // Route cards
        items(routeSummaries, key = { it.routeId }) { summary ->
            RouteCard(
                summary = summary,
                students = getStudentsForRoute(summary.routeId),
                onToggleExpand = { onToggleExpand(summary.routeId) },
                onCallClick = onCallClick,
                onWhatsAppClick = onWhatsAppClick,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
        
        // Swipe hint at bottom
        item {
            SwipeHint(text = "Swipe left for student details →")
        }
    }
}

// Page 2: Student Details (Filters + Table)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudentDetailsPage(
    state: TransportDuesState,
    onRouteToggle: (Long) -> Unit,
    onSelectAllRoutes: () -> Unit,
    onClassChange: (String) -> Unit,
    onDueStatusChange: (DueStatusFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onSortChange: (TransportSortOption) -> Unit,
    onCallClick: (TransportStudentData) -> Unit,
    onWhatsAppClick: (TransportStudentData) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Metrics Dashboard (scrolls with content)
        item {
            MetricsDashboard(
                totalStudents = state.totalTransportStudents,
                totalInSchool = state.totalStudentsInSchool,
                totalDues = state.totalDues,
                activeRoutes = state.activeRoutesCount,
                collectionRate = state.overallCollectionRate,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        
        // Swipe hint
        item {
            SwipeHint(text = "← Swipe right for route summary")
        }
        
        // Filters Section
        item {
            FiltersSection(
                routes = state.routes,
                selectedRoutes = state.selectedRoutes,
                classes = state.classes,
                selectedClass = state.selectedClass,
                dueStatusFilter = state.dueStatusFilter,
                searchQuery = state.searchQuery,
                sortOption = state.sortOption,
                onRouteToggle = onRouteToggle,
                onSelectAllRoutes = onSelectAllRoutes,
                onClassChange = onClassChange,
                onDueStatusChange = onDueStatusChange,
                onSearchChange = onSearchChange,
                onSortChange = onSortChange,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Table Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TableChart,
                        contentDescription = null,
                        tint = Saffron,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Student List",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Saffron.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${state.filteredStudents.size} results",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Saffron,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        // Data Table
        item {
            TransportDataTable(
                students = state.filteredStudents,
                onCallClick = onCallClick,
                onWhatsAppClick = onWhatsAppClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// Swipe hint component
@Composable
private fun SwipeHint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TransportHeader(
    sessionName: String,
    onBackClick: () -> Unit,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SaffronDark, Saffron, SaffronLight)
                )
            )
            .padding(top = 8.dp, bottom = 20.dp)
    ) {
        Column {
            // Top row with back button and export actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // PDF Export Button
                    Surface(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.GridView,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "PDF",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    // Excel Export Button
                    Surface(
                        onClick = onExportExcel,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.TableChart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Excel",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            
            // Title section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bus Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Transport Dues",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = sessionName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricsDashboard(
    totalStudents: Int,
    totalInSchool: Int,
    totalDues: Double,
    activeRoutes: Int,
    collectionRate: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Transport Students
            MetricCard(
                icon = Icons.Rounded.People,
                iconTint = Saffron,
                title = "Transport Students",
                value = totalStudents.toString(),
                subtitle = "of $totalInSchool total",
                modifier = Modifier.weight(1f)
            )
            
            // Total Dues
            MetricCard(
                icon = Icons.Outlined.CurrencyRupee,
                iconTint = ErrorRed,
                title = "Total Dues",
                value = totalDues.toRupees(),
                subtitle = "pending amount",
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Active Routes
            MetricCard(
                icon = Icons.Default.Route,
                iconTint = Teal,
                title = "Active Routes",
                value = activeRoutes.toString(),
                subtitle = "serving areas",
                modifier = Modifier.weight(1f)
            )
            
            // Collection Rate with progress
            MetricCardWithProgress(
                title = "Collection Rate",
                value = "${(collectionRate * 100).toInt()}%",
                progress = collectionRate,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Explanatory note
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Dues shown include all fee types (tuition, transport, admission, etc.) for students enrolled in transport.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = iconTint.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun MetricCardWithProgress(
    title: String,
    value: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val progressColor = when {
        progress >= 0.8f -> SuccessGreen
        progress >= 0.5f -> WarningOrange
        else -> ErrorRed
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = progressColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = progressColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun PremiumSectionHeader(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    badge: String,
    modifier: Modifier = Modifier,
    showToggle: Boolean = true
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (showToggle) Modifier.clickable { onToggle() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Saffron.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Saffron,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            
            if (showToggle) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun RouteCard(
    summary: RouteSummary,
    students: List<TransportStudentData>,
    onToggleExpand: () -> Unit,
    onCallClick: (TransportStudentData) -> Unit,
    onWhatsAppClick: (TransportStudentData) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        summary.collectionRate >= 0.8f -> SuccessGreen
        summary.collectionRate >= 0.5f -> WarningOrange
        else -> ErrorRed
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            // Route Header - Use Surface with onClick for reliable gesture handling
            Surface(
                onClick = onToggleExpand,
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(statusColor, CircleShape)
                    )
                    
                    Column {
                        Text(
                            text = summary.routeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${summary.studentCount} students",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = summary.pendingTotal.toRupees(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.pendingTotal > 0) ErrorRed else SuccessGreen
                        )
                        Text(
                            text = "pending",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                    Icon(
                        if (summary.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
                }
            }
            
            // Progress bar
            LinearProgressIndicator(
                progress = { summary.collectionRate },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Square
            )
            
            // Expanded content - Student list
            AnimatedVisibility(
                visible = summary.isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    students.forEach { student ->
                        RouteStudentRow(
                            student = student,
                            onCallClick = { onCallClick(student) },
                            onWhatsAppClick = { onWhatsAppClick(student) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteStudentRow(
    student: TransportStudentData,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = student.student.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "${student.student.currentClass} • ${student.student.fatherName}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = student.netDues.toRupees(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (student.netDues > 0) ErrorRed else SuccessGreen
            )
            
            // Call button
            Surface(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(enabled = student.student.phonePrimary.isNotBlank()) { onCallClick() },
                shape = CircleShape,
                color = if (student.student.phonePrimary.isNotBlank()) Saffron else Color.Gray.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "Call",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            }
            
            // WhatsApp button
            Surface(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(enabled = student.student.phonePrimary.isNotBlank()) { onWhatsAppClick() },
                shape = CircleShape,
                color = if (student.student.phonePrimary.isNotBlank()) WhatsAppGreen else Color.Gray.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Outlined.ChatBubble,
                        contentDescription = "WhatsApp",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
    HorizontalDivider(color = DividerSoft.copy(alpha = 0.5f), thickness = 0.5.dp)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FiltersSection(
    routes: List<com.navoditpublic.fees.domain.model.TransportRoute>,
    selectedRoutes: Set<Long>,
    classes: List<String>,
    selectedClass: String,
    dueStatusFilter: DueStatusFilter,
    searchQuery: String,
    sortOption: TransportSortOption,
    onRouteToggle: (Long) -> Unit,
    onSelectAllRoutes: () -> Unit,
    onClassChange: (String) -> Unit,
    onDueStatusChange: (DueStatusFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onSortChange: (TransportSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name, account, route...", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = DividerSoft,
                    focusedBorderColor = Saffron,
                    unfocusedContainerColor = CreamLight,
                    focusedContainerColor = Color.White
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )
            
            // Routes chips - All visible using FlowRow
            Column {
                Text(
                    text = "Routes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // "All" chip - selected when no specific routes are selected
                    FilterChip(
                        label = "All",
                        isSelected = selectedRoutes.isEmpty(),
                        onClick = onSelectAllRoutes
                    )
                    // Route chips - only highlighted when specifically selected
                    routes.forEach { route ->
                        FilterChip(
                            label = route.routeName,
                            isSelected = selectedRoutes.contains(route.id),
                            onClick = { onRouteToggle(route.id) }
                        )
                    }
                }
            }
            
            // Class and Due Status filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Class Dropdown
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Class",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    PremiumDropdown(
                        options = classes,
                        selectedOption = selectedClass,
                        onOptionSelected = onClassChange
                    )
                }
                
                // Due Status Dropdown
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    PremiumDropdown(
                        options = DueStatusFilter.entries.map { it.displayName },
                        selectedOption = dueStatusFilter.displayName,
                        onOptionSelected = { displayName ->
                            DueStatusFilter.entries.find { it.displayName == displayName }?.let {
                                onDueStatusChange(it)
                            }
                        }
                    )
                }
            }
            
            // Sort option
            Column {
                Text(
                    text = "Sort By",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                PremiumDropdown(
                    options = TransportSortOption.entries.map { it.displayName },
                    selectedOption = sortOption.displayName,
                    onOptionSelected = { displayName ->
                        TransportSortOption.entries.find { it.displayName == displayName }?.let {
                            onSortChange(it)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Saffron else Color.White,
        animationSpec = tween(200),
        label = "chipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextPrimary,
        animationSpec = tween(200),
        label = "chipContent"
    )
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, DividerSoft) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = if (expanded) Color.White else CreamLight,
            shadowElevation = if (expanded) 4.dp else 0.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (expanded) Saffron else DividerSoft.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedOption,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (expanded) Saffron else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(
                extraSmall = RoundedCornerShape(12.dp)
            )
        ) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEachIndexed { index, option ->
                    val isSelected = option == selectedOption
                    
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    option,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Saffron else TextPrimary
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Saffron,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        modifier = Modifier.background(
                            if (isSelected) Saffron.copy(alpha = 0.08f) else Color.Transparent
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    )
                    
                    // Add divider between groups if needed
                    if (index < options.size - 1) {
                        HorizontalDivider(
                            color = DividerSoft.copy(alpha = 0.3f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransportDataTable(
    students: List<TransportStudentData>,
    onCallClick: (TransportStudentData) -> Unit,
    onWhatsAppClick: (TransportStudentData) -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
            // Header Row
            Surface(
                color = Saffron,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TableHeader("A/C No", 80.dp)
                    TableHeader("Name", 140.dp)
                    TableHeader("Class", 60.dp)
                    TableHeader("Route", 100.dp)
                    TableHeader("Fee", 80.dp)
                    TableHeader("Dues", 80.dp)
                    TableHeader("Actions", 70.dp)
                }
            }
            
            // Data Rows
            Box(
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                LazyColumn {
                    itemsIndexed(
                        items = students,
                        key = { _, item -> item.student.id }
                    ) { index, student ->
                        TransportTableRow(
                            student = student,
                            isEvenRow = index % 2 == 0,
                            onCallClick = { onCallClick(student) },
                            onWhatsAppClick = { onWhatsAppClick(student) }
                        )
                        if (index < students.size - 1) {
                            HorizontalDivider(color = DividerSoft.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
                    }
                }
            }
            
            // Footer
            Surface(
                color = Saffron.copy(alpha = 0.1f),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${students.size} students",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Scroll for more →",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHeader(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.width(width),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TransportTableRow(
    student: TransportStudentData,
    isEvenRow: Boolean,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    val bgColor = if (isEvenRow) Color.White else CreamLight.copy(alpha = 0.5f)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = student.student.accountNumber,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.width(80.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = student.student.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.width(140.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = student.student.currentClass,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(60.dp),
            maxLines = 1
        )
        Text(
            text = student.routeName,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(100.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = student.totalFee.toRupees(),
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            modifier = Modifier.width(80.dp),
            maxLines = 1
        )
        Text(
            text = if (student.netDues > 0) student.netDues.toRupees() else "✓ Paid",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (student.netDues > 0) ErrorRed else SuccessGreen,
            modifier = Modifier.width(80.dp),
            maxLines = 1
        )
        
        // Action buttons
        Row(
            modifier = Modifier.width(70.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .clickable(enabled = student.student.phonePrimary.isNotBlank()) { onCallClick() },
                shape = CircleShape,
                color = if (student.student.phonePrimary.isNotBlank()) Saffron else Color.Gray.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "Call",
                        modifier = Modifier.size(13.dp),
                        tint = Color.White
                    )
                }
            }
            
            Surface(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .clickable(enabled = student.student.phonePrimary.isNotBlank()) { onWhatsAppClick() },
                shape = CircleShape,
                color = if (student.student.phonePrimary.isNotBlank()) WhatsAppGreen else Color.Gray.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Outlined.ChatBubble,
                        contentDescription = "WhatsApp",
                        modifier = Modifier.size(13.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneSelectionDialog(
    student: TransportStudentData,
    actionType: PhoneActionType,
    onPhoneSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (actionType == PhoneActionType.CALL) Icons.Default.Phone else Icons.Outlined.ChatBubble,
                    contentDescription = null,
                    tint = if (actionType == PhoneActionType.CALL) Saffron else WhatsAppGreen
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (actionType == PhoneActionType.CALL) "Select Number to Call" else "Select Number for WhatsApp",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Choose a number for ${student.student.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                if (student.student.phonePrimary.isNotBlank()) {
                    PhoneOptionRow(
                        phone = student.student.phonePrimary,
                        label = "Primary",
                        actionType = actionType,
                        onClick = { onPhoneSelected(student.student.phonePrimary) }
                    )
                }
                
                if (student.student.phoneSecondary.isNotBlank()) {
                    PhoneOptionRow(
                        phone = student.student.phoneSecondary,
                        label = "Secondary",
                        actionType = actionType,
                        onClick = { onPhoneSelected(student.student.phoneSecondary) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun PhoneOptionRow(
    phone: String,
    label: String,
    actionType: PhoneActionType,
    onClick: () -> Unit
) {
    val color = if (actionType == PhoneActionType.CALL) Saffron else WhatsAppGreen
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (actionType == PhoneActionType.CALL) Icons.Default.Phone else Icons.Outlined.ChatBubble,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = color
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = phone,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

private fun getActiveFilterCount(state: TransportDuesState): String {
    var count = 0
    if (state.selectedRoutes.isNotEmpty()) count++
    if (state.selectedClass != "All") count++
    if (state.dueStatusFilter != DueStatusFilter.ALL) count++
    if (state.searchQuery.isNotBlank()) count++
    return if (count > 0) "$count active" else "no filters"
}

private fun openWhatsApp(context: android.content.Context, phone: String, student: TransportStudentData) {
    val message = buildWhatsAppMessage(student)
    val phoneNumber = formatPhoneForWhatsApp(phone)
    val encodedMessage = Uri.encode(message)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://wa.me/$phoneNumber?text=$encodedMessage")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
    }
}

private fun buildWhatsAppMessage(student: TransportStudentData): String {
    return """
🚌 *ट्रांसपोर्ट फीस अनुस्मारक*

प्रिय अभिभावक,

आपके बच्चे की ट्रांसपोर्ट फीस बकाया है।

👤 *विद्यार्थी:* ${student.student.name}
📚 *कक्षा:* ${student.student.currentClass}
🛣️ *रूट:* ${student.routeName}
💰 *बकाया राशि:* ${student.netDues.toRupees()}

कृपया जल्द से जल्द बकाया राशि का भुगतान करें।

⚠️ _यदि भुगतान हो चुका है, तो कृपया इस संदेश को अनदेखा करें।_

धन्यवाद 🙏

━━━━━━━━━━━━━━━

🚌 *Transport Fee Reminder*

Dear Parent,

Transport fee is pending for your ward.

👤 *Student:* ${student.student.name}
📚 *Class:* ${student.student.currentClass}
🛣️ *Route:* ${student.routeName}
💰 *Due Amount:* ${student.netDues.toRupees()}

Please clear the dues at your earliest convenience.

⚠️ _If already paid, please ignore this message._

Thank you 🙏
    """.trimIndent()
}

private fun formatPhoneForWhatsApp(phone: String): String {
    val digitsOnly = phone.replace(Regex("[^0-9]"), "")
    return when {
        digitsOnly.length > 10 -> digitsOnly
        digitsOnly.length == 10 -> "91$digitsOnly"
        else -> digitsOnly
    }
}
