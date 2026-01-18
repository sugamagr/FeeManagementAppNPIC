package com.navoditpublic.fees.presentation.screens.reports.custom

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.theme.Cream
import com.navoditpublic.fees.presentation.theme.DueChipBackground
import com.navoditpublic.fees.presentation.theme.DueChipText
import com.navoditpublic.fees.presentation.theme.ErrorRed
import com.navoditpublic.fees.presentation.theme.GradientEnd
import com.navoditpublic.fees.presentation.theme.GradientMiddle
import com.navoditpublic.fees.presentation.theme.GradientStart
import com.navoditpublic.fees.presentation.theme.PaidChipBackground
import com.navoditpublic.fees.presentation.theme.PaidChipText
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronAmber
import com.navoditpublic.fees.presentation.theme.SaffronContainer
import com.navoditpublic.fees.presentation.theme.SaffronContainerLight
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.presentation.theme.SaffronLight
import com.navoditpublic.fees.presentation.theme.SuccessGreen
import com.navoditpublic.fees.presentation.theme.SuccessGreenLight
import com.navoditpublic.fees.presentation.theme.TextMuted
import com.navoditpublic.fees.presentation.theme.TextPrimary
import com.navoditpublic.fees.presentation.theme.TextSecondary
import com.navoditpublic.fees.presentation.theme.TextTertiary
import com.navoditpublic.fees.presentation.theme.WarningAmber
import com.navoditpublic.fees.presentation.theme.WarningOrange
import com.navoditpublic.fees.util.ExcelGenerator
import com.navoditpublic.fees.util.PdfGenerator
import kotlinx.coroutines.launch

// Private colors for this screen
private val SurfaceWhite = Color(0xFFFAFAFA)
private val CardWhite = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomReportScreen(
    navController: NavController,
    viewModel: CustomReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showExportSheet by remember { mutableStateOf(false) }
    var showColumnSelector by remember { mutableStateOf(true) }
    var showClassDropdown by remember { mutableStateOf(false) }
    var showSortDropdown by remember { mutableStateOf(false) }
    
    val exportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
            Box(
                modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Hero Header with Stats
            item {
                HeroHeader(
                    totalStudents = state.totalStudents,
                    totalCollected = state.totalCollected,
                    collectionRate = state.collectionRate,
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // Filters Section
            item {
                FiltersSection(
                    searchQuery = state.searchQuery,
                    onSearchChange = viewModel::setSearchQuery,
                    selectedClass = state.selectedClass,
                    availableClasses = state.availableClasses,
                    onClassChange = viewModel::setClassFilter,
                    showClassDropdown = showClassDropdown,
                    onClassDropdownChange = { showClassDropdown = it },
                    selectedPreset = state.selectedPreset,
                    onPresetChange = viewModel::setPreset,
                    sortOption = state.sortOption,
                    onSortChange = viewModel::setSortOption,
                    showSortDropdown = showSortDropdown,
                    onSortDropdownChange = { showSortDropdown = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Column Selection Card
            item {
                ColumnSelectionCard(
                    selectedColumns = state.selectedColumns,
                    onToggleColumn = viewModel::toggleColumn,
                    onSelectAll = viewModel::selectAllColumns,
                    onClearAll = viewModel::clearAllColumns,
                    onCategoryClick = viewModel::selectColumnsByCategory,
                    isExpanded = showColumnSelector,
                    onExpandToggle = { showColumnSelector = !showColumnSelector },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Summary Stats Bar
            item {
                SummaryStatsBar(
                    totalStudents = state.totalStudents,
                    totalDues = state.totalDues,
                    totalCollected = state.totalCollected,
                    studentsWithDues = state.studentsWithDues,
                    fullyPaidStudents = state.fullyPaidStudents,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Preview Table Header
            item {
                PreviewHeader(
                    studentCount = state.reportData.size,
                    columnCount = state.selectedColumns.size,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Preview Table
            item {
                if (state.isLoading) {
                    LoadingScreen(modifier = Modifier.height(300.dp))
                } else if (state.reportData.isEmpty()) {
                    EmptyPreview(modifier = Modifier.padding(32.dp))
                } else {
                    PreviewTable(
                        columns = state.selectedColumns.toList(),
                        data = state.reportData.take(50),
                        getDuesValue = viewModel::getDuesValue,
                        getCollectionRate = viewModel::getCollectionRateValue,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    if (state.reportData.size > 50) {
                    Text(
                            text = "+ ${state.reportData.size - 50} more students in export",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Floating Export Button
        FloatingExportButton(
            studentCount = state.reportData.size,
            onClick = { showExportSheet = true },
            enabled = state.reportData.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
        
        // Export Bottom Sheet
        if (showExportSheet) {
            ModalBottomSheet(
                onDismissRequest = { showExportSheet = false },
                sheetState = exportSheetState,
                containerColor = CardWhite,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                val summaryData = viewModel.getSummaryData()
                ExportBottomSheet(
                    studentCount = state.reportData.size,
                    summaryData = summaryData,
                    onPdfExport = {
                        scope.launch {
                            try {
                                PdfGenerator.generateReport(
                                    context = context,
                                    title = viewModel.getReportTitle(),
                                    headers = viewModel.getHeaders(),
                                    rows = viewModel.getRows(),
                                    summary = summaryData,
                                    fileName = viewModel.getFileName()
                                )
                                Toast.makeText(context, "PDF exported successfully!", Toast.LENGTH_SHORT).show()
                                showExportSheet = false
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onExcelExport = {
                        scope.launch {
                            try {
                                ExcelGenerator.generateReport(
                                    context = context,
                                    title = viewModel.getReportTitle(),
                                    headers = viewModel.getHeaders(),
                                    rows = viewModel.getRows(),
                                    summary = summaryData,
                                    fileName = viewModel.getExcelFileName()
                                )
                                Toast.makeText(context, "Excel exported successfully!", Toast.LENGTH_SHORT).show()
                                showExportSheet = false
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onDismiss = { showExportSheet = false }
                )
            }
        }
    }
}

// ============================================================================
// HERO HEADER
// ============================================================================

@Composable
private fun HeroHeader(
    totalStudents: Int,
    totalCollected: Double,
    collectionRate: Float,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation for counters
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(totalCollected) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                )
            )
    ) {
        Column(
                    modifier = Modifier
                        .fillMaxWidth()
                .statusBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Custom Report",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Build your custom student report",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Students Card
                StatCard(
                    icon = Icons.Default.Groups,
                    value = "$totalStudents",
                    label = "Students",
                    modifier = Modifier.weight(1f)
                )
                
                // Collection Card
                StatCard(
                    icon = Icons.Default.AccountBalance,
                    value = "₹${String.format("%,.0f", totalCollected * animatedProgress.value)}",
                    label = "Collected",
                    modifier = Modifier.weight(1f)
                )
                
                // Rate Card
                StatCard(
                    icon = Icons.Default.TrendingUp,
                    value = "${String.format("%.0f", collectionRate * animatedProgress.value)}%",
                    label = "Rate",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// ============================================================================
// FILTERS SECTION
// ============================================================================

@Composable
private fun FiltersSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedClass: String,
    availableClasses: List<String>,
    onClassChange: (String) -> Unit,
    showClassDropdown: Boolean,
    onClassDropdownChange: (Boolean) -> Unit,
    selectedPreset: ReportPreset,
    onPresetChange: (ReportPreset) -> Unit,
    sortOption: ReportSortOption,
    onSortChange: (ReportSortOption) -> Unit,
    showSortDropdown: Boolean,
    onSortDropdownChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        // Search Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CardWhite,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(Modifier.width(10.dp))
                
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    cursorBrush = SolidColor(Saffron),
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search name, SR number, phone...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Report Presets
        Text(
            text = "Quick Filters",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportPreset.entries.forEach { preset ->
                item {
                    PresetChip(
                        preset = preset,
                        isSelected = selectedPreset == preset,
                        onClick = { onPresetChange(preset) }
                    )
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Class + Sort Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Class Dropdown
            ClassDropdown(
                selectedClass = selectedClass,
                availableClasses = availableClasses,
                expanded = showClassDropdown,
                onExpandedChange = onClassDropdownChange,
                onClassSelected = {
                    onClassChange(it)
                    onClassDropdownChange(false)
                },
                            modifier = Modifier.weight(1f)
            )
            
            // Sort Dropdown
            SortDropdown(
                selectedOption = sortOption,
                expanded = showSortDropdown,
                onExpandedChange = onSortDropdownChange,
                onOptionSelected = {
                    onSortChange(it)
                    onSortDropdownChange(false)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PresetChip(
    preset: ReportPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Saffron else CardWhite,
        animationSpec = tween(200),
        label = "presetBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        animationSpec = tween(200),
        label = "presetContent"
    )
    
    val presetIcon = when (preset) {
        ReportPreset.ALL_STUDENTS -> Icons.Default.Groups
        ReportPreset.WITH_DUES -> Icons.Default.Payments
        ReportPreset.FULLY_PAID -> Icons.Default.TaskAlt
        ReportPreset.HIGH_DUES -> Icons.Default.Warning
        ReportPreset.WITH_TRANSPORT -> Icons.Default.DirectionsBus
        ReportPreset.RECENT_PAYMENTS -> Icons.Default.CalendarMonth
    }
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                presetIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = preset.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ClassDropdown(
    selectedClass: String,
    availableClasses: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClassSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFiltered = selectedClass != "All"
    
    Box(modifier = modifier) {
        Surface(
            onClick = { onExpandedChange(!expanded) },
            shape = RoundedCornerShape(12.dp),
            color = if (isFiltered) Saffron else CardWhite,
            shadowElevation = 2.dp
        ) {
            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = if (isFiltered) Color.White else Saffron,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Class",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFiltered) Color.White.copy(alpha = 0.8f) else TextMuted
                    )
                    Text(
                        text = if (selectedClass == "All") "All Classes" else selectedClass,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isFiltered) Color.White else TextPrimary
                    )
                }
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isFiltered) Color.White else TextMuted
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .background(CardWhite, RoundedCornerShape(12.dp))
                .heightIn(max = 300.dp)
        ) {
            availableClasses.forEach { className ->
                val isSelected = className == selectedClass
                                    DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Saffron,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                            }
                            Text(
                                text = if (className == "All") "All Classes" else className,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Saffron else TextPrimary
                            )
                        }
                    },
                    onClick = { onClassSelected(className) },
                    modifier = Modifier.background(
                        if (isSelected) Saffron.copy(alpha = 0.08f) else Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
private fun SortDropdown(
    selectedOption: ReportSortOption,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOptionSelected: (ReportSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Surface(
            onClick = { onExpandedChange(!expanded) },
            shape = RoundedCornerShape(12.dp),
            color = CardWhite,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sort by",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = selectedOption.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(CardWhite, RoundedCornerShape(12.dp))
        ) {
            ReportSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.displayName,
                            fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal,
                            color = if (option == selectedOption) Saffron else TextPrimary
                        )
                    },
                    onClick = { onOptionSelected(option) },
                    leadingIcon = if (option == selectedOption) {
                        {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Saffron,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

// ============================================================================
// COLUMN SELECTION CARD
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnSelectionCard(
    selectedColumns: Set<ReportColumn>,
    onToggleColumn: (ReportColumn) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    onCategoryClick: (ColumnCategory) -> Unit,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() },
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
                        text = "Columns",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = Saffron.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${selectedColumns.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Saffron,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick actions
                    Text(
                        text = "All",
                        style = MaterialTheme.typography.labelSmall,
                        color = Saffron,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onSelectAll() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onClearAll() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextMuted
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                // All columns in a flat FlowRow
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ReportColumn.entries.forEach { column ->
                        ColumnChip(
                            column = column,
                            isSelected = column in selectedColumns,
                            onClick = { onToggleColumn(column) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnChip(
    column: ReportColumn,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Saffron else Color.Transparent,
        animationSpec = tween(150),
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Saffron else TextMuted.copy(alpha = 0.4f),
        animationSpec = tween(150),
        label = "chipBorder"
    )
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 0.dp else 1.dp, 
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = column.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}

// ============================================================================
// SUMMARY STATS BAR
// ============================================================================

@Composable
private fun SummaryStatsBar(
    totalStudents: Int,
    totalDues: Double,
    totalCollected: Double,
    studentsWithDues: Int,
    fullyPaidStudents: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SaffronContainerLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryStatItem(
                value = "₹${formatAmount(totalCollected)}",
                label = "Collected",
                color = SuccessGreen
            )
            
            VerticalDivider()
            
            SummaryStatItem(
                value = "₹${formatAmount(totalDues)}",
                label = "Pending",
                color = if (totalDues > 0) ErrorRed else SuccessGreen
            )
            
            VerticalDivider()
            
            SummaryStatItem(
                value = "$fullyPaidStudents",
                label = "Paid",
                color = SuccessGreen
            )
            
            VerticalDivider()
            
            SummaryStatItem(
                value = "$studentsWithDues",
                label = "With Dues",
                color = if (studentsWithDues > 0) WarningOrange else SuccessGreen
            )
        }
    }
}

@Composable
private fun SummaryStatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(30.dp)
            .background(SaffronLight.copy(alpha = 0.3f))
    )
}

// ============================================================================
// PREVIEW TABLE
// ============================================================================

@Composable
private fun PreviewHeader(
    studentCount: Int,
    columnCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
                                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Saffron.copy(alpha = 0.1f)
        ) {
                            Text(
                text = "$studentCount students • $columnCount cols",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = Saffron,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun EmptyPreview(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.FilterList,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
                            Text(
                                text = "No students match filters",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
        Text(
            text = "Try adjusting your filters",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

@Composable
private fun PreviewTable(
    columns: List<ReportColumn>,
    data: List<Map<ReportColumn, String>>,
    getDuesValue: (Map<ReportColumn, String>) -> Double,
    getCollectionRate: (Map<ReportColumn, String>) -> Float,
    modifier: Modifier = Modifier
) {
                        val horizontalScrollState = rememberScrollState()
                        
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
                        Column(
                            modifier = Modifier
                .fillMaxWidth()
                                .horizontalScroll(horizontalScrollState)
                        ) {
                            // Table Header
                            Row(
                                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Saffron, SaffronDark)
                        )
                    )
                    .padding(vertical = 10.dp, horizontal = 8.dp)
            ) {
                columns.forEach { column ->
                    val width = when {
                        column == ReportColumn.NAME -> 140.dp
                        column == ReportColumn.COLLECTION_RATE -> 100.dp
                        column.isFinancial -> 100.dp
                        else -> 90.dp
                    }
                    
                                    Text(
                                        text = column.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                        modifier = Modifier.width(width),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            
                            // Table Rows
            data.forEachIndexed { index, studentData ->
                val dues = getDuesValue(studentData)
                val collectionRate = getCollectionRate(studentData)
                
                val rowColor = when {
                    index % 2 == 0 -> CardWhite
                    else -> SaffronContainerLight.copy(alpha = 0.3f)
                }
                
                                    Row(
                                        modifier = Modifier
                        .background(rowColor)
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                ) {
                    columns.forEach { column ->
                        val value = studentData[column] ?: "-"
                        val width = when {
                            column == ReportColumn.NAME -> 140.dp
                            column == ReportColumn.COLLECTION_RATE -> 100.dp
                            column.isFinancial -> 100.dp
                            else -> 90.dp
                        }
                        
                        when (column) {
                            ReportColumn.DUES -> {
                                DuesCell(
                                    value = value,
                                    duesAmount = dues,
                                    modifier = Modifier.width(width)
                                )
                            }
                            ReportColumn.COLLECTION_RATE -> {
                                CollectionRateCell(
                                    value = value,
                                    rate = collectionRate,
                                    modifier = Modifier.width(width)
                                )
                            }
                            else -> {
                                            Text(
                                    text = value,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.width(width),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuesCell(
    value: String,
    duesAmount: Double,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when {
        duesAmount <= 0 -> PaidChipBackground to PaidChipText
        duesAmount < 5000 -> WarningAmber.copy(alpha = 0.15f) to WarningOrange
        else -> DueChipBackground to DueChipText
    }
    
    Surface(
        modifier = modifier.padding(end = 4.dp),
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
                                        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CollectionRateCell(
    value: String,
    rate: Float,
    modifier: Modifier = Modifier
) {
    val progressColor = when {
        rate >= 100f -> SuccessGreen
        rate >= 50f -> WarningOrange
        else -> ErrorRed
    }
    
    Column(modifier = modifier.padding(end = 4.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = progressColor
        )
        LinearProgressIndicator(
            progress = { (rate / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

// ============================================================================
// FLOATING EXPORT BUTTON
// ============================================================================

@Composable
private fun FloatingExportButton(
    studentCount: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val buttonColor by animateColorAsState(
        targetValue = if (enabled) Saffron else TextMuted,
        animationSpec = tween(200),
        label = "exportBtnColor"
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = buttonColor,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Export Report ($studentCount students)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ============================================================================
// EXPORT BOTTOM SHEET
// ============================================================================

@Composable
private fun ExportBottomSheet(
    studentCount: Int,
    summaryData: Map<String, String>,
    onPdfExport: () -> Unit,
    onExcelExport: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(TextMuted.copy(alpha = 0.3f))
                .align(Alignment.CenterHorizontally)
        )
        
        Spacer(Modifier.height(20.dp))
        
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Upload,
                contentDescription = null,
                tint = Saffron,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Export Report",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        Text(
            text = "$studentCount students • ${summaryData.size} summary fields",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 38.dp)
        )
        
        Spacer(Modifier.height(20.dp))
        
        // Summary Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SaffronContainerLight)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Summary Preview",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SaffronDark
                )
                Spacer(Modifier.height(8.dp))
                summaryData.entries.take(3).forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
        
        Spacer(Modifier.height(24.dp))
        
        // Export Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // PDF Button
            ExportOptionButton(
                icon = Icons.Default.PictureAsPdf,
                title = "PDF",
                subtitle = "Print-ready",
                color = ErrorRed,
                onClick = onPdfExport,
                modifier = Modifier.weight(1f)
            )
            
            // Excel Button
            ExportOptionButton(
                icon = Icons.Default.Description,
                title = "Excel",
                subtitle = "Spreadsheet",
                color = SuccessGreen,
                onClick = onExcelExport,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ExportOptionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

private fun formatAmount(amount: Double): String {
    return when {
        amount >= 10000000 -> String.format("%.1fCr", amount / 10000000)
        amount >= 100000 -> String.format("%.1fL", amount / 100000)
        amount >= 1000 -> String.format("%.1fK", amount / 1000)
        else -> String.format("%.0f", amount)
    }
}
