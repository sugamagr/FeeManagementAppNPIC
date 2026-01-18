package com.navoditpublic.fees.presentation.screens.reports.dues

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.data.local.entity.SavedReportViewEntity
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.theme.*
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomDuesReportScreen(
    navController: NavController,
    viewModel: CustomDuesReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
    
    var showColumns by remember { mutableStateOf(true) }
    var showFilters by remember { mutableStateOf(true) }
    
    // Staggered animation states
    var showHeader by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        showHeader = true
        delay(100)
        showSummary = true
        delay(100)
        showContent = true
    }
    
    // Save View Dialog
    if (state.showSaveDialog) {
        StyledAlertDialog(
            title = "Save View",
            onDismiss = viewModel::dismissDialogs,
            content = {
                Column {
                    Text(
                        "Save current column selection and filters as a view",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.viewNameInput,
                        onValueChange = viewModel::updateViewNameInput,
                        label = { Text("View Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Saffron,
                            cursorColor = Saffron
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveCurrentView,
                    enabled = state.viewNameInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialogs) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
    
    // Load View Dialog
    if (state.showLoadDialog) {
        StyledAlertDialog(
            title = "Load Saved View",
            onDismiss = viewModel::dismissDialogs,
            content = {
                if (state.savedViews.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextMuted
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No saved views yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                        Text(
                            "Create one using the Save button",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(state.savedViews) { view ->
                            SavedViewItem(
                                view = view,
                                isSelected = state.currentViewId == view.id,
                                onLoad = { viewModel.loadView(view) },
                                onDelete = { viewModel.deleteView(view) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::dismissDialogs) {
                    Text("Close", color = Saffron, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = null
        )
    }
    
    Scaffold(
        containerColor = Cream,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Gradient Header with Floating Actions
                item {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    AnimatedVisibility(
                        visible = showHeader,
                        enter = fadeIn() + slideInVertically { -it }
                    ) {
                        GradientHeader(
                            studentCount = state.filteredStudents.size,
                            onBack = { navController.popBackStack() },
                            onLoad = viewModel::showLoadDialog,
                            onSave = viewModel::showSaveDialog,
                            onExportPdf = { viewModel.exportToPdf(context) },
                            onExportExcel = { viewModel.exportToExcel(context) }
                        )
                    }
                }
                
                // Current View Indicator
                if (state.currentViewId != null) {
                    item {
                        val currentView = state.savedViews.find { it.id == state.currentViewId }
                        if (currentView != null) {
                            CurrentViewBadge(viewName = currentView.viewName)
                        }
                    }
                }
                
                // Hero Summary Cards
                item {
                    AnimatedVisibility(
                        visible = showSummary,
                        enter = fadeIn(tween(300)) + expandVertically()
                    ) {
                        HeroSummarySection(
                            studentCount = state.filteredStudents.size,
                            totalDues = state.filteredStudents.sumOf { it.netDues.coerceAtLeast(0.0) },
                            currencyFormat = currencyFormat
                        )
                    }
                }
                
                // Main Content
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(400)) + expandVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Filters Section - MOVED ABOVE COLUMNS
                            PremiumExpandableSection(
                                title = "Filters",
                                icon = Icons.Default.FilterList,
                                isExpanded = showFilters,
                                onToggle = { showFilters = !showFilters },
                                badge = buildFilterSummary(state)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Search Bar - Full width
                                    OutlinedTextField(
                                        value = state.searchQuery,
                                        onValueChange = viewModel::updateSearchQuery,
                                        placeholder = { 
                                            Text(
                                                "Search by name, SR No...", 
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted
                                            ) 
                                        },
                                        leadingIcon = { 
                                            Icon(
                                                Icons.Default.Search, 
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = TextMuted
                                            ) 
                                        },
                                        trailingIcon = {
                                            if (state.searchQuery.isNotBlank()) {
                                                IconButton(
                                                    onClick = { viewModel.updateSearchQuery("") },
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Clear",
                                                        modifier = Modifier.size(16.dp),
                                                        tint = TextSecondary
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
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
                                    
                                    // Class Selection - Chips showing all classes
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            "Class",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextTertiary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            state.classes.forEach { className ->
                                                ClassChip(
                                                    label = className,
                                                    isSelected = state.selectedClass == className,
                                                    onClick = { viewModel.updateSelectedClass(className) }
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Sort Option
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            "Sort By",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextTertiary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        SortOptionSelector(
                                            selectedOption = state.sortOption,
                                            onOptionSelected = { viewModel.updateSortOption(it) }
                                        )
                                    }
                                    
                                    // Toggle Switches with dynamic subtexts
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterToggleRow(
                                            label = "Dues Filter",
                                            subtitle = if (state.showOnlyWithDues) 
                                                "Showing only students with dues" 
                                            else 
                                                "Showing all students",
                                            checked = state.showOnlyWithDues,
                                            onCheckedChange = { viewModel.toggleShowOnlyWithDues() }
                                        )
                                        FilterToggleRow(
                                            label = "Transport Filter",
                                            subtitle = if (state.showOnlyWithTransport) 
                                                "Showing students with transport" 
                                            else 
                                                "Hiding students with transport",
                                            checked = state.showOnlyWithTransport,
                                            onCheckedChange = { viewModel.toggleShowOnlyWithTransport() }
                                        )
                                    }
                                }
                            }
                            
                            // Column Selection - NOW BELOW FILTERS
                            PremiumExpandableSection(
                                title = "Columns",
                                icon = Icons.Default.ViewColumn,
                                isExpanded = showColumns,
                                onToggle = { showColumns = !showColumns },
                                badge = "${state.selectedColumns.size} selected"
                            ) {
                                // All Column Pills - no category tabs
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    DuesReportColumn.entries.forEach { column ->
                                        StyledColumnPill(
                                            column = column,
                                            isSelected = state.selectedColumns.contains(column),
                                            onClick = { viewModel.toggleColumn(column) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Data Table
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(500)) + expandVertically()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                    if (state.filteredStudents.isNotEmpty()) {
                                PremiumDuesTable(
                            columns = state.selectedColumns.toList(),
                            data = state.filteredStudents,
                            currencyFormat = currencyFormat
                        )
                    } else {
                                EmptyStateCard()
                    }
                }
                    }
                }
            }
        }
    }
}

@Composable
fun GradientHeader(
    studentCount: Int,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onSave: () -> Unit,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                )
            )
            .padding(bottom = 16.dp)
    ) {
        // Top Row with back button and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Custom Dues Report",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$studentCount students",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Action Pills - Row 1: Load & Save
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionPill(
                icon = Icons.Default.FolderOpen,
                label = "Load",
                onClick = onLoad,
                modifier = Modifier.weight(1f)
            )
            FloatingActionPill(
                icon = Icons.Default.Save,
                label = "Save",
                onClick = onSave,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Action Pills - Row 2: Export PDF & Excel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionPill(
                icon = Icons.Default.PictureAsPdf,
                label = "PDF",
                onClick = onExportPdf,
                modifier = Modifier.weight(1f)
            )
            FloatingActionPill(
                icon = Icons.Default.GridOn,
                label = "Excel",
                onClick = onExportExcel,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FloatingActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compact icon container
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(SaffronContainer, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = SaffronDark
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun CurrentViewBadge(viewName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(SaffronContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.TableChart,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = SaffronDark
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "View: $viewName",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = SaffronDark
        )
    }
}

@Composable
fun HeroSummarySection(
    studentCount: Int,
    totalDues: Double,
    currencyFormat: NumberFormat
) {
    var animatedStudentCount by remember { mutableIntStateOf(0) }
    var animatedDues by remember { mutableStateOf(0.0) }
    
    LaunchedEffect(studentCount, totalDues) {
        // Animate count up
        val steps = 20
        for (i in 1..steps) {
            animatedStudentCount = (studentCount * i / steps)
            animatedDues = totalDues * i / steps
            delay(30)
        }
        animatedStudentCount = studentCount
        animatedDues = totalDues
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Students Card - Compact
        CompactMetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.People,
            iconBackgroundColor = SaffronContainer,
            iconTint = SaffronDark,
            value = animatedStudentCount.toString(),
            label = "Filtered Students",
            valueColor = TextPrimary
        )
        
        // Dues Card - Compact with red accent
        CompactMetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.CurrencyRupee,
            iconBackgroundColor = DueChipBackground,
            iconTint = ErrorRed,
            value = currencyFormat.format(animatedDues),
            label = "Filtered Dues",
            valueColor = ErrorRed
        )
    }
}

@Composable
fun CompactMetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    value: String,
    label: String,
    valueColor: Color = TextPrimary
) {
    Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .height(72.dp), // Fixed height for even sizing
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compact icon container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBackgroundColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = iconTint
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    letterSpacing = (-0.25).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun PremiumExpandableSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    badge: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header - clickable row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggle)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Compact icon container
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SaffronContainer, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = SaffronDark
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
                
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = TextSecondary
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(spring(stiffness = Spring.StiffnessMedium)),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = DividerSoft, modifier = Modifier.padding(bottom = 12.dp))
                    content()
                }
            }
        }
    }
}

@Composable
fun StyledColumnPill(
    column: DuesReportColumn,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Consistent Saffron theme for all selected pills
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Saffron else Color.White,
        label = "bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        label = "content"
    )
    
    // Use Box with border instead of Surface to avoid double border
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .then(
                if (!isSelected) Modifier.border(1.dp, DividerSoft, RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = contentColor
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                column.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortOptionSelector(
    selectedOption: DuesReportSortOption,
    onOptionSelected: (DuesReportSortOption) -> Unit
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
                .clip(RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = if (expanded) 8.dp else 2.dp,
            border = BorderStroke(
                width = 1.dp,
                color = if (expanded) Saffron else DividerSoft.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sort icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Saffron.copy(alpha = 0.15f), SaffronLight.copy(alpha = 0.1f))
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Sort By",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = selectedOption.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Animated dropdown indicator
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (expanded) Saffron else TextSecondary,
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (expanded) Saffron.copy(alpha = 0.1f) else Color.Transparent,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                )
            }
        }
        
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(
                extraSmall = RoundedCornerShape(14.dp)
            )
        ) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
            ) {
                DuesReportSortOption.entries.forEachIndexed { index, option ->
                    val isSelected = option == selectedOption
                    
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Option icon based on type
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            color = if (isSelected) Saffron.copy(alpha = 0.15f) else CreamLight,
                                            shape = RoundedCornerShape(6.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getSortOptionIcon(option),
                                        contentDescription = null,
                                        tint = if (isSelected) Saffron else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                
                                Text(
                                    text = option.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Saffron else TextPrimary
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // Checkmark for selected
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Saffron,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        modifier = Modifier
                            .background(
                                if (isSelected) Saffron.copy(alpha = 0.05f) else Color.Transparent
                            ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    )
                    
                    // Add divider after certain groups
                    if (index == 1 || index == 3 || index == 5) {
                        HorizontalDivider(
                            color = DividerSoft.copy(alpha = 0.3f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getSortOptionIcon(option: DuesReportSortOption): ImageVector {
    return when (option) {
        DuesReportSortOption.DUES_HIGH_TO_LOW -> Icons.Default.TrendingDown
        DuesReportSortOption.DUES_LOW_TO_HIGH -> Icons.Default.TrendingUp
        DuesReportSortOption.NAME_A_TO_Z -> Icons.Default.SortByAlpha
        DuesReportSortOption.NAME_Z_TO_A -> Icons.Default.SortByAlpha
        DuesReportSortOption.CLASS_ASC -> Icons.Default.School
        DuesReportSortOption.CLASS_DESC -> Icons.Default.School
        DuesReportSortOption.ACCOUNT_NUMBER -> Icons.Default.Tag
    }
}

@Composable
fun ClassChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Saffron else Color.White,
        label = "bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        label = "content"
    )
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .then(
                if (!isSelected) Modifier.border(1.dp, DividerSoft, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}

@Composable
fun FilterToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onCheckedChange)
            .background(if (checked) SaffronContainer.copy(alpha = 0.3f) else CreamLight)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (checked) SaffronDark else TextTertiary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Saffron,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = DividerLight
            )
        )
    }
}

@Composable
fun PremiumDuesTable(
    columns: List<DuesReportColumn>,
    data: List<DuesReportStudentData>,
    currencyFormat: NumberFormat
) {
    val horizontalScrollState = rememberScrollState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Fixed Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(SaffronDark, Saffron, SaffronAmber)
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                columns.forEach { column ->
                    Text(
                        text = column.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.width(getColumnWidth(column)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // LazyColumn for performant scrolling - only renders visible rows
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .horizontalScroll(horizontalScrollState)
            ) {
                itemsIndexed(
                    items = data,
                    key = { _, item -> item.student.id }
                ) { index, studentData ->
                    SimpleTableRow(
                        index = index,
                        studentData = studentData,
                        columns = columns,
                        currencyFormat = currencyFormat
                    )
                }
            }
            
            // Footer with record count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CreamLight)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${data.size} records",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Text(
                    text = "↕ Scroll",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun SimpleTableRow(
    index: Int,
    studentData: DuesReportStudentData,
    columns: List<DuesReportColumn>,
    currencyFormat: NumberFormat
) {
    // Simple alternating colors - no animations for performance
    val rowColor = if (index % 2 == 0) Color.White else SaffronContainer.copy(alpha = 0.12f)
    
    Column {
        Row(
            modifier = Modifier
                .background(rowColor)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEach { column ->
                val value = remember(studentData, column) {
                    when (column) {
                        DuesReportColumn.SR_NUMBER -> studentData.student.srNumber
                        DuesReportColumn.ACCOUNT_NUMBER -> studentData.student.accountNumber
                        DuesReportColumn.STUDENT_NAME -> studentData.student.name
                        DuesReportColumn.FATHER_NAME -> studentData.student.fatherName
                        DuesReportColumn.CLASS_SECTION -> studentData.student.classSection
                        DuesReportColumn.VILLAGE -> studentData.student.addressLine2
                        DuesReportColumn.PHONE -> studentData.student.phonePrimary
                        DuesReportColumn.PHONE_SECONDARY -> studentData.student.phoneSecondary.ifBlank { "-" }
                        DuesReportColumn.EXPECTED_FEE -> currencyFormat.format(studentData.expectedFee)
                        DuesReportColumn.PAID_AMOUNT -> currencyFormat.format(studentData.paidAmount)
                        DuesReportColumn.OPENING_BALANCE -> currencyFormat.format(studentData.openingBalance)
                        DuesReportColumn.NET_DUES -> currencyFormat.format(studentData.netDues)
                        DuesReportColumn.TRANSPORT_STATUS -> if (studentData.student.hasTransport) "Yes" else "No"
                        DuesReportColumn.TRANSPORT_ROUTE -> studentData.transportRouteName ?: "-"
                        DuesReportColumn.TRANSPORT_FEE -> currencyFormat.format(studentData.transportFee)
                        DuesReportColumn.ADMISSION_DATE -> {
                            val format = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            format.format(java.util.Date(studentData.student.admissionDate))
                        }
                    }
                }
                
                when (column) {
                    DuesReportColumn.STUDENT_NAME -> {
                        Row(
                            modifier = Modifier.width(getColumnWidth(column)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Simple colored circle avatar
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Saffron, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = studentData.student.name.take(1).uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    DuesReportColumn.NET_DUES -> {
                        val hasDues = studentData.netDues > 0
                        Box(
                            modifier = Modifier
                                .width(getColumnWidth(column))
                                .background(
                                    color = if (hasDues) DueChipBackground else PaidChipBackground,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (hasDues) DueChipText else PaidChipText
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            modifier = Modifier.width(getColumnWidth(column)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = DividerSoft.copy(alpha = 0.3f), thickness = 0.5.dp)
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
                .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(CreamWarm, CircleShape),
                contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.TableChart,
            contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = TextMuted
        )
            }
            
            Spacer(Modifier.height(20.dp))
            
        Text(
            text = "No Data Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
        )
            
            Spacer(Modifier.height(8.dp))
            
        Text(
                text = "Try adjusting your filters to see results",
            style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
            textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(24.dp))
            
            Surface(
                onClick = { },
                shape = RoundedCornerShape(12.dp),
                color = SaffronContainer
            ) {
                Text(
                    text = "Clear Filters",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = SaffronDark
                )
            }
        }
    }
}

@Composable
fun SavedViewItem(
    view: SavedReportViewEntity,
    isSelected: Boolean,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) SaffronContainer else CreamWarm,
        label = "bg"
    )
    
    Surface(
        onClick = onLoad,
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isSelected) Saffron else SaffronContainer,
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TableChart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected) Color.White else SaffronDark
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = view.viewName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${view.selectedColumns.split(",").size} columns",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ErrorRedSoft
                )
            }
        }
    }
}

@Composable
fun StyledAlertDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = { content() },
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

private fun getColumnWidth(column: DuesReportColumn): androidx.compose.ui.unit.Dp {
    return when (column) {
        DuesReportColumn.SR_NUMBER -> 80.dp
        DuesReportColumn.ACCOUNT_NUMBER -> 95.dp
        DuesReportColumn.STUDENT_NAME -> 160.dp
        DuesReportColumn.FATHER_NAME -> 140.dp
        DuesReportColumn.CLASS_SECTION -> 85.dp
        DuesReportColumn.VILLAGE -> 120.dp
        DuesReportColumn.PHONE -> 105.dp
        DuesReportColumn.PHONE_SECONDARY -> 105.dp
        DuesReportColumn.EXPECTED_FEE -> 105.dp
        DuesReportColumn.PAID_AMOUNT -> 95.dp
        DuesReportColumn.OPENING_BALANCE -> 105.dp
        DuesReportColumn.NET_DUES -> 100.dp
        DuesReportColumn.TRANSPORT_STATUS -> 75.dp
        DuesReportColumn.TRANSPORT_ROUTE -> 120.dp
        DuesReportColumn.TRANSPORT_FEE -> 105.dp
        DuesReportColumn.ADMISSION_DATE -> 105.dp
    }
}

private fun buildFilterSummary(state: CustomDuesReportState): String {
    val parts = mutableListOf<String>()
    if (state.selectedClass != "All") parts.add(state.selectedClass)
    parts.add(if (state.showOnlyWithDues) "With Dues" else "All Students")
    parts.add(if (state.showOnlyWithTransport) "With Transport" else "No Transport")
    return parts.joinToString(" · ")
}
