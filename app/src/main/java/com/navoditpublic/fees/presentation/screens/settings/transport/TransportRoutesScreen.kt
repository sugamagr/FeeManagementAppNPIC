package com.navoditpublic.fees.presentation.screens.settings.transport

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.data.local.entity.TransportFeeHistoryEntity
import com.navoditpublic.fees.domain.model.TransportRoute
import com.navoditpublic.fees.presentation.components.LoadingScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Premium Color Palette
private val GradientOrange = Color(0xFFFF6B35)
private val GradientPink = Color(0xFFFF8E53)
private val AccentTeal = Color(0xFF00D9A5)
private val AccentBlue = Color(0xFF4DA3FF)
private val DeepPurple = Color(0xFF6366F1)
private val SoftPurple = Color(0xFF8B5CF6)
private val DarkSurface = Color(0xFF1A1A2E)
private val CardDark = Color(0xFF16213E)
private val SuccessGreen = Color(0xFF10B981)
private val WarningAmber = Color(0xFFF59E0B)
private val ErrorRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportRoutesScreen(
    navController: NavController,
    viewModel: TransportRoutesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddEditSheet by remember { mutableStateOf(false) }
    var showChangeFeeSheet by remember { mutableStateOf(false) }
    var showCloseSheet by remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    var showFeeHistorySheet by remember { mutableStateOf(false) }
    var showMoreOptionsSheet by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf<TransportRoute?>(null) }
    var feeHistory by remember { mutableStateOf<List<TransportFeeHistoryEntity>>(emptyList()) }
    
    // Form states
    var routeName by remember { mutableStateOf("") }
    var feeNcTo5 by remember { mutableStateOf("") }
    var fee6To8 by remember { mutableStateOf("") }
    var fee9To12 by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var effectiveDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var feeNotes by remember { mutableStateOf("") }
    var closeDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var closeReason by remember { mutableStateOf("") }
    
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TransportEvent.Success -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    showAddEditSheet = false
                    showChangeFeeSheet = false
                    showCloseSheet = false
                    showDeleteSheet = false
                    showMoreOptionsSheet = false
                    selectedRoute = null
                    routeName = ""
                    feeNcTo5 = ""
                    fee6To8 = ""
                    fee9To12 = ""
                    description = ""
                    feeNotes = ""
                    closeReason = ""
                }
                is TransportEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    val filteredActiveRoutes = state.activeRoutes.filter {
        it.routeName.contains(state.searchQuery, ignoreCase = true)
    }
    val filteredClosedRoutes = state.closedRoutes.filter {
        it.routeName.contains(state.searchQuery, ignoreCase = true)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientOrange,
                        GradientPink,
                        Color(0xFFFFF5F2)
                    ),
                    startY = 0f,
                    endY = 600f
                )
            )
    ) {
        // Main Content
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = "Transport Routes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(Modifier.weight(1f))
                    
                    // Add button in header
                    Surface(
                        onClick = {
                            selectedRoute = null
                            routeName = ""
                            feeNcTo5 = ""
                            fee6To8 = ""
                            fee9To12 = ""
                            description = ""
                            showAddEditSheet = true
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "New",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            
            // Content Card
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color(0xFFFAFAFC)
            ) {
                if (state.isLoading) {
                    LoadingScreen()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Compact Stats Row
                        item {
                            AnimatedVisibility(
                                visible = isVisible,
                                enter = fadeIn() + slideInVertically { -20 }
                            ) {
                                CompactStatsRow(
                                    totalRoutes = state.activeRoutes.size,
                                    totalStudents = state.totalStudents,
                                    estimatedRevenue = state.estimatedMonthlyRevenue
                                )
                            }
                        }
                        
                        // Search + Tabs Row
                        item {
                            CompactSearchAndTabs(
                                query = state.searchQuery,
                                onQueryChange = { viewModel.updateSearchQuery(it) },
                                selectedTab = selectedTab,
                                onTabChange = { selectedTab = it },
                                activeCount = filteredActiveRoutes.size,
                                closedCount = filteredClosedRoutes.size
                            )
                        }
                        
                        val displayRoutes = if (selectedTab == 0) filteredActiveRoutes else filteredClosedRoutes
                        
                        if (displayRoutes.isEmpty()) {
                            item {
                                CompactEmptyState(
                                    isActiveTab = selectedTab == 0,
                                    hasSearchQuery = state.searchQuery.isNotBlank()
                                )
                            }
                        } else {
                            itemsIndexed(displayRoutes, key = { _, route -> route.id }) { index, route ->
                                AnimatedVisibility(
                                    visible = isVisible,
                                    enter = fadeIn(tween(200, delayMillis = index * 30)) +
                                            slideInVertically(
                                                initialOffsetY = { 30 },
                                                animationSpec = tween(200, delayMillis = index * 30)
                                            )
                                ) {
                                    CompactRouteCard(
                                        route = route,
                                        studentCount = state.studentCounts[route.id] ?: 0,
                                        onEdit = {
                                            selectedRoute = route
                                            routeName = route.routeName
                                            description = route.description
                                            showAddEditSheet = true
                                        },
                                        onChangeFee = {
                                            selectedRoute = route
                                            feeNcTo5 = route.feeNcTo5.toInt().toString()
                                            fee6To8 = route.fee6To8.toInt().toString()
                                            fee9To12 = route.fee9To12.toInt().toString()
                                            effectiveDate = System.currentTimeMillis()
                                            feeNotes = ""
                                            showChangeFeeSheet = true
                                        },
                                        onMoreOptions = {
                                            selectedRoute = route
                                            showMoreOptionsSheet = true
                                        },
                                        onReopen = {
                                            viewModel.reopenRoute(route.id)
                                        },
                                        onDelete = {
                                            selectedRoute = route
                                            showDeleteSheet = true
                                        },
                                        onViewHistory = {
                                            selectedRoute = route
                                            scope.launch {
                                                feeHistory = viewModel.getFeeHistory(route.id)
                                                showFeeHistorySheet = true
                                            }
                                        },
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Bottom Sheets
    
    // More Options Sheet (Custom)
    if (showMoreOptionsSheet) {
        selectedRoute?.let { route ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            
            ModalBottomSheet(
                onDismissRequest = { showMoreOptionsSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = route.routeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OptionItem(
                        icon = Icons.Rounded.History,
                        title = "View Fee History",
                        subtitle = "See all fee changes",
                        color = AccentBlue,
                        onClick = {
                            showMoreOptionsSheet = false
                            scope.launch {
                                feeHistory = viewModel.getFeeHistory(route.id)
                                showFeeHistorySheet = true
                            }
                        }
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    OptionItem(
                        icon = Icons.Rounded.PowerSettingsNew,
                        title = "Close Route",
                        subtitle = "Preserve history, stop usage",
                        color = WarningAmber,
                        onClick = {
                            showMoreOptionsSheet = false
                            closeDate = System.currentTimeMillis()
                            closeReason = ""
                            showCloseSheet = true
                        }
                    )
                }
            }
        }
    }
    
    // Add/Edit Route Sheet
    if (showAddEditSheet) {
        // Capture route for safe access in callbacks
        val routeBeingEdited = selectedRoute
        val isEditing = routeBeingEdited != null
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        ModalBottomSheet(
            onDismissRequest = { 
                showAddEditSheet = false
                selectedRoute = null
            },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GradientIconBox(
                        icon = if (isEditing) Icons.Rounded.Edit else Icons.Rounded.DirectionsBus,
                        gradient = Brush.linearGradient(listOf(GradientOrange, GradientPink))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (isEditing) "Edit Route" else "New Route",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isEditing) "Update details" else "Add transport route",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                // Form
                StyledTextField(
                        value = routeName,
                        onValueChange = { routeName = it },
                    label = "Route Name",
                    placeholder = "e.g., Kurgaon"
                )
                
                Spacer(Modifier.height(12.dp))
                
                if (!isEditing) {
                    Text(
                        "Monthly Fees",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF666666)
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactFeeField(
                            value = feeNcTo5,
                            onValueChange = { feeNcTo5 = it.filter { c -> c.isDigit() } },
                            label = "NC-5",
                            modifier = Modifier.weight(1f)
                        )
                        CompactFeeField(
                            value = fee6To8,
                            onValueChange = { fee6To8 = it.filter { c -> c.isDigit() } },
                            label = "6-8",
                            modifier = Modifier.weight(1f)
                        )
                        CompactFeeField(
                            value = fee9To12,
                            onValueChange = { fee9To12 = it.filter { c -> c.isDigit() } },
                            label = "9-12",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                } else {
                    // Current fees display
                    routeBeingEdited?.let { route ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF8F9FA),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                FeeChip("NC-5", "₹${route.feeNcTo5.toInt()}")
                                FeeChip("6-8", "₹${route.fee6To8.toInt()}")
                                FeeChip("9-12", "₹${route.fee9To12.toInt()}")
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
                
                StyledTextField(
                        value = description,
                        onValueChange = { description = it },
                    label = "Description",
                    placeholder = "Optional notes"
                )
                
                Spacer(Modifier.height(20.dp))
                
                // Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { showAddEditSheet = false; selectedRoute = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    GradientButton(
                        text = if (isEditing) "Update" else "Add Route",
                    onClick = {
                        routeBeingEdited?.let { route ->
                            viewModel.updateRoute(
                                id = route.id,
                                name = routeName,
                                feeNcTo5 = route.feeNcTo5,
                                fee6To8 = route.fee6To8,
                                fee9To12 = route.fee9To12,
                                description = description
                            )
                        } ?: run {
                            viewModel.addRoute(
                                name = routeName,
                                feeNcTo5 = feeNcTo5.toDoubleOrNull() ?: 0.0,
                                fee6To8 = fee6To8.toDoubleOrNull() ?: 0.0,
                                fee9To12 = fee9To12.toDoubleOrNull() ?: 0.0,
                                description = description
                            )
                        }
                    },
                        enabled = routeName.isNotBlank() && (isEditing || feeNcTo5.isNotBlank()),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    
    // Change Fee Sheet
    if (showChangeFeeSheet) {
        selectedRoute?.let { route ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    effectiveDate = calendar.timeInMillis
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            
            ModalBottomSheet(
                onDismissRequest = { showChangeFeeSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GradientIconBox(
                            icon = Icons.Rounded.MonetizationOn,
                            gradient = Brush.linearGradient(listOf(AccentTeal, AccentBlue))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                        Text(
                                "Update Fees",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                route.routeName,
                            style = MaterialTheme.typography.bodySmall,
                                color = AccentTeal
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Current fees
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Current",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF22C55E)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Text("₹${route.feeNcTo5.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("₹${route.fee6To8.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("₹${route.fee9To12.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "New Fees",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF666666)
                )
                Spacer(Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactFeeField(
                        value = feeNcTo5,
                        onValueChange = { feeNcTo5 = it.filter { c -> c.isDigit() } },
                        label = "NC-5",
                        modifier = Modifier.weight(1f)
                    )
                    CompactFeeField(
                        value = fee6To8,
                        onValueChange = { fee6To8 = it.filter { c -> c.isDigit() } },
                        label = "6-8",
                        modifier = Modifier.weight(1f)
                    )
                    CompactFeeField(
                        value = fee9To12,
                        onValueChange = { fee9To12 = it.filter { c -> c.isDigit() } },
                        label = "9-12",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Date picker
                Surface(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF8F9FA),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Effective From", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(dateFormat.format(Date(effectiveDate)), fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                StyledTextField(
                        value = feeNotes,
                        onValueChange = { feeNotes = it },
                    label = "Notes",
                    placeholder = "e.g., Session 2025-26"
                )
                
                Spacer(Modifier.height(20.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { showChangeFeeSheet = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    
GradientButton(
                             text = "Update Fee",
                        onClick = {
                            viewModel.updateFeeWithEffectiveDate(
                                    routeId = route.id,
                                    feeNcTo5 = feeNcTo5.toDoubleOrNull() ?: 0.0,
                                    fee6To8 = fee6To8.toDoubleOrNull() ?: 0.0,
                                    fee9To12 = fee9To12.toDoubleOrNull() ?: 0.0,
                                    effectiveFrom = effectiveDate,
                                    notes = feeNotes
                                )
                            },
                            enabled = feeNcTo5.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            gradient = Brush.linearGradient(listOf(AccentTeal, AccentBlue))
                        )
                    }
                }
            }
        }
    }
    
    // Close Route Sheet
    if (showCloseSheet) {
        selectedRoute?.let { route ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    closeDate = calendar.timeInMillis
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            
            ModalBottomSheet(
                onDismissRequest = { showCloseSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GradientIconBox(
                            icon = Icons.Rounded.PowerSettingsNew,
                            gradient = Brush.linearGradient(listOf(WarningAmber, ErrorRed))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                        Text(
                                "Close Route",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                route.routeName,
                            style = MaterialTheme.typography.bodySmall,
                                color = WarningAmber
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "History will be preserved. You can reopen later.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Surface(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Close Date", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(dateFormat.format(Date(closeDate)), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    StyledTextField(
                            value = closeReason,
                            onValueChange = { closeReason = it },
                        label = "Reason",
                        placeholder = "Optional"
                    )
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showCloseSheet = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }
                        
                    Button(
                            onClick = { viewModel.closeRoute(route.id, closeDate, closeReason) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarningAmber
                        )
                    ) {
                        Text("Close Route")
                    }
                    }
                }
            }
        }
    }
    
    // Delete Sheet
    if (showDeleteSheet) {
        selectedRoute?.let { route ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        ModalBottomSheet(
            onDismissRequest = { showDeleteSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                )
            }
        ) {
                Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color(0xFFFEE2E2),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                    Text(
                    "Delete Permanently?",
                    style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                
                Spacer(Modifier.height(4.dp))
                
                    Text(
                    route.routeName,
                        style = MaterialTheme.typography.bodyMedium,
                    color = ErrorRed,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(Modifier.height(12.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "⚠️ This will permanently delete the route and all history. Cannot be undone!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF991B1B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                Spacer(Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteSheet = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { viewModel.deleteRoute(route.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
    
    // Fee History Sheet
    if (showFeeHistorySheet) {
        selectedRoute?.let { route ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            
            ModalBottomSheet(
                onDismissRequest = { showFeeHistorySheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GradientIconBox(
                            icon = Icons.Rounded.History,
                            gradient = Brush.linearGradient(listOf(DeepPurple, SoftPurple))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                                            Text(
                                "Fee History",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                route.routeName,
                                style = MaterialTheme.typography.bodySmall,
                                color = DeepPurple
                            )
                        }
                    }
                
                Spacer(Modifier.height(20.dp))
                
                if (feeHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.History,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).alpha(0.3f),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.height(8.dp))
                                        Text(
                                "No history yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    // Determine entry types based on effective dates
                    val now = System.currentTimeMillis()
                    // Find the current fee: most recent entry where effectiveFrom <= now
                    val currentEntryIndex = feeHistory.indexOfFirst { it.effectiveFrom <= now }
                    
                    feeHistory.forEachIndexed { index, entry ->
                        val entryType = when {
                            entry.effectiveFrom > now -> FeeEntryType.SCHEDULED
                            index == currentEntryIndex -> FeeEntryType.CURRENT
                            else -> FeeEntryType.PAST
                        }
                        TimelineEntry(
                            entry = entry,
                            dateFormat = dateFormat,
                            entryType = entryType,
                            isFirst = index == 0,
                            isLast = index == feeHistory.lastIndex
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = { showFeeHistorySheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPurple)
                ) {
                    Text("Done")
                }
            }
            }
        }
    }
}

// ============ Compact Components ============

@Composable
private fun CompactStatsRow(
    totalRoutes: Int,
    totalStudents: Int,
    estimatedRevenue: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatChip(
            icon = Icons.Rounded.Route,
            value = "$totalRoutes",
            label = "Routes",
            color = GradientOrange,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            icon = Icons.Rounded.People,
            value = "$totalStudents",
            label = "Students",
            color = AccentTeal,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            icon = Icons.Rounded.CurrencyRupee,
            value = if (estimatedRevenue >= 100000) "₹${(estimatedRevenue/1000).toInt()}K" else "₹${estimatedRevenue.toInt()}",
            label = "Est/mo",
            color = DeepPurple,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun CompactSearchAndTabs(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    activeCount: Int,
    closedCount: Int
) {
        Column(
            modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Search Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF3F4F6),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            "Search routes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Black
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Clear",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                .padding(3.dp)
        ) {
            TabButton(
                text = "Active ($activeCount)",
                isSelected = selectedTab == 0,
                onClick = { onTabChange(0) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "Closed ($closedCount)",
                isSelected = selectedTab == 1,
                onClick = { onTabChange(1) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.97f,
        label = "scale"
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier.scale(scale),
        color = if (isSelected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) GradientOrange else Color.Gray
        )
    }
}

@Composable
private fun CompactRouteCard(
    route: TransportRoute,
    studentCount: Int,
    onEdit: () -> Unit,
    onChangeFee: () -> Unit,
    onMoreOptions: () -> Unit,
    onReopen: () -> Unit,
    onDelete: () -> Unit,
    onViewHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon
                Box(
            modifier = Modifier
                        .size(36.dp)
                        .then(
                            if (route.isClosed) {
                                Modifier.background(Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                            } else {
                                Modifier.background(
                                    Brush.linearGradient(listOf(GradientOrange, GradientPink)),
                                    RoundedCornerShape(10.dp)
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.DirectionsBus,
                        contentDescription = null,
                        tint = if (route.isClosed) Color.Gray else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(Modifier.width(10.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = route.routeName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (route.isClosed) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = ErrorRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "CLOSED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = ErrorRed,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (route.description.isNotBlank()) {
                        Text(
                            route.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                        )
                    }
                }
                
                // Student Badge
                Surface(
                    color = if (route.isClosed) Color(0xFFE5E7EB) else AccentTeal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.People,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = if (route.isClosed) Color.Gray else AccentTeal
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            "$studentCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (route.isClosed) Color.Gray else AccentTeal
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(10.dp))
            
            // Fee Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeeItem("NC-5", route.feeNcTo5, route.isClosed)
                FeeItem("6-8", route.fee6To8, route.isClosed)
                FeeItem("9-12", route.fee9To12, route.isClosed)
            }
            
            Spacer(Modifier.height(10.dp))
            
            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!route.isClosed) {
                    ActionChip(
                        icon = Icons.Rounded.Edit,
                        text = "Edit",
                        color = AccentBlue,
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    )
                    ActionChip(
                        icon = Icons.Rounded.MonetizationOn,
                        text = "Fee",
                        color = AccentTeal,
                        onClick = onChangeFee,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        onClick = onMoreOptions,
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                                Icons.Rounded.MoreHoriz,
                                contentDescription = "More",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    ActionChip(
                        icon = Icons.Rounded.History,
                        text = "History",
                        color = DeepPurple,
                        onClick = onViewHistory,
                        modifier = Modifier.weight(1f)
                    )
                    ActionChip(
                        icon = Icons.Rounded.Refresh,
                        text = "Reopen",
                        color = SuccessGreen,
                        onClick = onReopen,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        onClick = onDelete,
                        color = ErrorRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = ErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeeItem(label: String, fee: Double, isClosed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 10.sp
        )
        Text(
            "₹${fee.toInt()}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isClosed) Color.Gray else GradientOrange
        )
    }
}

@Composable
private fun ActionChip(
    icon: ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
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
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun GradientIconBox(
    icon: ImageVector,
    gradient: Brush
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(gradient, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
                        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.linearGradient(listOf(GradientOrange, GradientPink))
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) gradient else Brush.linearGradient(listOf(Color.Gray, Color.Gray)),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color.LightGray) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GradientOrange,
            unfocusedBorderColor = Color(0xFFE5E7EB)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CompactFeeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp) },
        prefix = { Text("₹", fontSize = 12.sp) },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GradientOrange,
            unfocusedBorderColor = Color(0xFFE5E7EB)
        ),
        modifier = modifier
    )
}

@Composable
private fun FeeChip(label: String, fee: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(fee, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

private enum class FeeEntryType {
    SCHEDULED,  // Future date - not yet active
    CURRENT,    // Currently active fee
    PAST        // Historical entry
}

@Composable
private fun TimelineEntry(
    entry: TransportFeeHistoryEntity,
    dateFormat: SimpleDateFormat,
    entryType: FeeEntryType,
    isFirst: Boolean,
    isLast: Boolean
) {
    val now = System.currentTimeMillis()
    val isFuture = entry.effectiveFrom > now
    val isCurrent = entryType == FeeEntryType.CURRENT
    
    // Colors based on entry type
    val dotColor = when (entryType) {
        FeeEntryType.SCHEDULED -> AccentBlue
        FeeEntryType.CURRENT -> DeepPurple
        FeeEntryType.PAST -> Color(0xFFD1D5DB)
    }
    val bgColor = when (entryType) {
        FeeEntryType.SCHEDULED -> AccentBlue.copy(alpha = 0.08f)
        FeeEntryType.CURRENT -> DeepPurple.copy(alpha = 0.08f)
        FeeEntryType.PAST -> Color(0xFFF9FAFB)
    }
    val chipColor = when (entryType) {
        FeeEntryType.SCHEDULED -> AccentBlue
        FeeEntryType.CURRENT -> DeepPurple
        FeeEntryType.PAST -> GradientOrange
    }
    
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
                        .height(12.dp)
                        .background(Color(0xFFE5E7EB))
                )
                } else {
                Spacer(Modifier.height(12.dp))
            }
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(dotColor, CircleShape)
            )
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(56.dp)
                        .background(Color(0xFFE5E7EB))
                )
            }
        }
        
        Spacer(Modifier.width(10.dp))
        
        // Content
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 8.dp),
            color = bgColor,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Status label and date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status badge
                    Surface(
                        color = when (entryType) {
                            FeeEntryType.SCHEDULED -> AccentBlue.copy(alpha = 0.15f)
                            FeeEntryType.CURRENT -> DeepPurple.copy(alpha = 0.15f)
                            FeeEntryType.PAST -> Color(0xFFE5E7EB)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = when (entryType) {
                                FeeEntryType.SCHEDULED -> "📅 Scheduled"
                                FeeEntryType.CURRENT -> "✓ Current"
                                FeeEntryType.PAST -> "Past"
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (entryType != FeeEntryType.PAST) FontWeight.SemiBold else FontWeight.Normal,
                            color = when (entryType) {
                                FeeEntryType.SCHEDULED -> AccentBlue
                                FeeEntryType.CURRENT -> DeepPurple
                                FeeEntryType.PAST -> Color.Gray
                            },
                            fontSize = 9.sp
                        )
                    }
                    
                    // Date
                    Text(
                        dateFormat.format(Date(entry.effectiveFrom)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                
                Spacer(Modifier.height(6.dp))
                
                // All 3 fees in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HistoryFeeChip(
                        label = "NC-5",
                        fee = if (entry.feeNcTo5 > 0) entry.feeNcTo5 else entry.monthlyFee,
                        color = if (entryType == FeeEntryType.PAST) GradientOrange else chipColor
                    )
                    HistoryFeeChip(
                        label = "6-8",
                        fee = if (entry.fee6To8 > 0) entry.fee6To8 else entry.monthlyFee,
                        color = if (entryType == FeeEntryType.PAST) AccentTeal else chipColor
                    )
                    HistoryFeeChip(
                        label = "9-12",
                        fee = if (entry.fee9To12 > 0) entry.fee9To12 else entry.monthlyFee,
                        color = if (entryType == FeeEntryType.PAST) AccentBlue else chipColor
                    )
                }
                
                if (entry.notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        entry.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryFeeChip(label: String, fee: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 9.sp
        )
        Text(
            "₹${fee.toInt()}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}


@Composable
private fun CompactEmptyState(
    isActiveTab: Boolean,
    hasSearchQuery: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    GradientOrange.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.DirectionsBus,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = GradientOrange.copy(alpha = 0.5f)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = when {
                hasSearchQuery -> "No routes found"
                isActiveTab -> "No Active Routes"
                else -> "No Closed Routes"
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(4.dp))
        
        Text(
            text = when {
                hasSearchQuery -> "Try different search"
                isActiveTab -> "Tap + to add a route"
                else -> "Closed routes appear here"
            },
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
