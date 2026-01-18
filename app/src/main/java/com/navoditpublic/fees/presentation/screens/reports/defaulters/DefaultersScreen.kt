package com.navoditpublic.fees.presentation.screens.reports.defaulters

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.EmptyState
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.util.DateUtils
import com.navoditpublic.fees.util.ExcelGenerator
import com.navoditpublic.fees.util.PdfGenerator
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Phone action type for selection dialog
private enum class PhoneActionType { CALL, WHATSAPP }

// Color Palette
private val DuesRed = Color(0xFFE53935)
private val DuesRedLight = Color(0xFFFFEBEE)
private val AccentBlue = Color(0xFF2196F3)
private val AccentGreen = Color(0xFF4CAF50)
private val AccentPurple = Color(0xFF7C4DFF)
private val WhatsAppGreen = Color(0xFF25D366)
private val WarmBackground = Color(0xFFFAF8F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultersScreen(
    navController: NavController,
    viewModel: DefaultersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showExportMenu by remember { mutableStateOf(false) }
    var showAmountFilter by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    
    // Phone selection state
    var showPhoneSelectionDialog by remember { mutableStateOf(false) }
    var phoneSelectionType by remember { mutableStateOf<PhoneActionType?>(null) }
    var selectedDefaulterForPhone by remember { mutableStateOf<DefaulterInfo?>(null) }
    
    val sortSheetState = rememberModalBottomSheetState()
    
    Scaffold(
        containerColor = WarmBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header
            item {
                DefaultersHeader(
                    totalDefaulters = state.filteredDefaulters.size,
                    onBackClick = { navController.popBackStack() },
                    onExportClick = { showExportMenu = true },
                    showExportMenu = showExportMenu,
                    onDismissExportMenu = { showExportMenu = false },
                    onExportPdf = {
                        showExportMenu = false
                        scope.launch {
                            exportToPdf(context, state)
                        }
                    },
                    onExportExcel = {
                        showExportMenu = false
                            scope.launch {
                            exportToExcel(context, state)
                        }
                    },
                    onShare = {
                        showExportMenu = false
                        shareDefaultersList(context, state)
                    }
                )
            }
            
            // Hero Summary Card
            item {
                HeroSummaryCard(
                    totalDues = state.filteredTotalDues,
                    totalDefaulters = state.filteredDefaulters.size,
                    averageDue = state.averageDue,
                    highestDue = state.highestDue,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Search Bar
            item {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::setSearchQuery,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Class Filter - Two Row Selection
            item {
                ClassFilterSection(
                    selectedClass = state.selectedClass,
                    availableClasses = state.availableClasses,
                    onClassSelect = viewModel::setClassFilter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Other Filters Row (Amount & Sort)
            item {
                OtherFiltersRow(
                    selectedAmountRange = state.selectedAmountRange,
                    selectedSort = state.selectedSort,
                    onAmountClick = { showAmountFilter = true },
                    onSortClick = { showSortSheet = true },
                    showAmountDropdown = showAmountFilter,
                    onDismissAmountDropdown = { showAmountFilter = false },
                    onAmountSelect = {
                        viewModel.setAmountRange(it)
                        showAmountFilter = false
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            
            // Results count
            item {
                if (state.searchQuery.isNotBlank() || state.selectedClass != "All" || state.selectedAmountRange != DueAmountRange.ALL) {
                    Text(
                        text = "${state.filteredDefaulters.size} of ${state.allDefaulters.size} students",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
            
            // Content
            if (state.isLoading) {
                item { LoadingScreen() }
            } else if (state.filteredDefaulters.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.CheckCircle,
                        title = if (state.allDefaulters.isEmpty()) "No Defaulters" else "No Results",
                        subtitle = if (state.allDefaulters.isEmpty()) 
                            "All students have cleared their dues" 
                        else 
                            "Try adjusting your filters"
                    )
                }
            } else {
                items(state.filteredDefaulters, key = { it.studentId }) { defaulter ->
                    DefaulterCard(
                        defaulter = defaulter,
                        onCardClick = {
                            navController.navigate(Screen.StudentLedger.createRoute(defaulter.studentId))
                        },
                        onCollectClick = {
                            navController.navigate(Screen.CollectFee.createRoute(defaulter.studentId))
                        },
                        onCallClick = {
                            if (defaulter.hasMultiplePhones) {
                                // Show selection dialog
                                selectedDefaulterForPhone = defaulter
                                phoneSelectionType = PhoneActionType.CALL
                                showPhoneSelectionDialog = true
                            } else if (defaulter.hasPhone) {
                                // Direct call
                                val phone = defaulter.availablePhones.first()
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$phone")
                                }
                                context.startActivity(intent)
                            }
                        },
                        onWhatsAppClick = {
                            if (defaulter.hasMultiplePhones) {
                                // Show selection dialog
                                selectedDefaulterForPhone = defaulter
                                phoneSelectionType = PhoneActionType.WHATSAPP
                                showPhoneSelectionDialog = true
                            } else if (defaulter.hasPhone) {
                                // Direct WhatsApp
                                val phone = defaulter.availablePhones.first()
                                openWhatsApp(context, phone, defaulter)
                            } else {
                                Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
            
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
    
    // Sort Bottom Sheet
    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = sortSheetState
        ) {
            SortOptionsContent(
                selectedSort = state.selectedSort,
                onSortSelect = {
                    viewModel.setSort(it)
                    showSortSheet = false
                }
            )
        }
    }
    
    // Phone Selection Dialog
    if (showPhoneSelectionDialog && selectedDefaulterForPhone != null) {
        PhoneSelectionDialog(
            defaulter = selectedDefaulterForPhone!!,
            actionType = phoneSelectionType!!,
            onPhoneSelected = { phone ->
                showPhoneSelectionDialog = false
                when (phoneSelectionType) {
                    PhoneActionType.CALL -> {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phone")
                        }
                        context.startActivity(intent)
                    }
                    PhoneActionType.WHATSAPP -> {
                        openWhatsApp(context, phone, selectedDefaulterForPhone!!)
                    }
                    null -> {}
                }
                selectedDefaulterForPhone = null
                phoneSelectionType = null
            },
            onDismiss = {
                showPhoneSelectionDialog = false
                selectedDefaulterForPhone = null
                phoneSelectionType = null
            }
        )
    }
}

@Composable
private fun DefaultersHeader(
    totalDefaulters: Int,
    onBackClick: () -> Unit,
    onExportClick: () -> Unit,
    showExportMenu: Boolean,
    onDismissExportMenu: () -> Unit,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit,
    onShare: () -> Unit
) {
    Box(
            modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Saffron, SaffronDark)
                )
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = 280.dp, y = (-20).dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(x = 320.dp, y = 50.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )
        
        Row(
                modifier = Modifier
                    .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 4.dp, end = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Defaulters",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "$totalDefaulters students with pending dues",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            
            // PDF Quick Export
            IconButton(onClick = onExportPdf) {
                Icon(
                    Icons.Default.PictureAsPdf,
                    contentDescription = "Export PDF",
                    tint = Color.White
                )
            }
            
            // More Export Options
            Box {
                IconButton(onClick = onExportClick) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White
                    )
                }
                
                DropdownMenu(
                    expanded = showExportMenu,
                    onDismissRequest = onDismissExportMenu
                ) {
                    DropdownMenuItem(
                        text = { Text("Export as PDF") },
                        leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) },
                        onClick = onExportPdf
                    )
                    DropdownMenuItem(
                        text = { Text("Export as Excel") },
                        leadingIcon = { Icon(Icons.Default.TableChart, null) },
                        onClick = onExportExcel
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Share List") },
                        leadingIcon = { Icon(Icons.Default.Share, null) },
                        onClick = onShare
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSummaryCard(
    totalDues: Double,
    totalDefaulters: Int,
    averageDue: Double,
    highestDue: Double,
    modifier: Modifier = Modifier
) {
            Card(
        modifier = modifier
                    .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = DuesRed.copy(alpha = 0.15f)
                ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Total Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(DuesRed, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Total Outstanding",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        text = totalDues.toRupees(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = DuesRed
                    )
                    
                    Text(
                        text = "$totalDefaulters students",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DuesRedLight
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = DuesRed,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(28.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Average",
                    value = averageDue.toRupees(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Highest",
                    value = highestDue.toRupees(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                    Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
                    Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
                    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(Modifier.width(12.dp))
            
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                cursorBrush = SolidColor(Saffron),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                        Text(
                                "Search by name, father, phone...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            if (query.isNotBlank()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassFilterSection(
    selectedClass: String,
    availableClasses: List<String>,
    onClassSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sort classes properly
    val sortedClasses = availableClasses
        .filter { it != "All" }
        .sortedWith(compareBy { getClassSortOrder(it) })
    
    // Split into two rows
    val midPoint = (sortedClasses.size + 1) / 2 + 1 // +1 for "All" in first row
    val firstRowClasses = listOf("All") + sortedClasses.take(midPoint - 1)
    val secondRowClasses = sortedClasses.drop(midPoint - 1)
    
    // Scroll states for both rows
    val firstRowScrollState = rememberScrollState()
    val secondRowScrollState = rememberScrollState()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Filter by Class",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.weight(1f))
                
                // Scroll hint
                    Text(
                    "Scroll →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            // First Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(firstRowScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                firstRowClasses.forEach { className ->
                    ClassChip(
                        label = className,
                        isSelected = selectedClass == className,
                        onClick = { onClassSelect(className) }
                    )
                }
            }
            
            // Second Row (if needed)
            if (secondRowClasses.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(secondRowScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    secondRowClasses.forEach { className ->
                        ClassChip(
                            label = className,
                            isSelected = selectedClass == className,
                            onClick = { onClassSelect(className) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ClassChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Saffron else Color.Transparent,
        animationSpec = tween(200),
        label = "chipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "chipContent"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Saffron else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(200),
        label = "chipBorder"
    )
    
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Text(
            text = if (label == "All") "All" else label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun OtherFiltersRow(
    selectedAmountRange: DueAmountRange,
    selectedSort: DefaulterSort,
    onAmountClick: () -> Unit,
    onSortClick: () -> Unit,
    showAmountDropdown: Boolean,
    onDismissAmountDropdown: () -> Unit,
    onAmountSelect: (DueAmountRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Amount Range Filter
        Box {
            FilterChip(
                selected = selectedAmountRange != DueAmountRange.ALL,
                onClick = onAmountClick,
                label = { 
                    Text(
                        if (selectedAmountRange == DueAmountRange.ALL) "Amount" else selectedAmountRange.label,
                        maxLines = 1
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DuesRed.copy(alpha = 0.15f),
                    selectedLabelColor = DuesRed,
                    selectedLeadingIconColor = DuesRed
                )
            )
            
            DropdownMenu(
                expanded = showAmountDropdown,
                onDismissRequest = onDismissAmountDropdown
            ) {
                DueAmountRange.entries.forEach { range ->
                    DropdownMenuItem(
                        text = { Text(range.label) },
                        onClick = { onAmountSelect(range) },
                        leadingIcon = if (range == selectedAmountRange) {
                            { Icon(Icons.Default.CheckCircle, null, tint = Saffron) }
                        } else null
                    )
                }
            }
        }
        
        // Sort Chip
        FilterChip(
            selected = true,
            onClick = onSortClick,
            label = { Text(selectedSort.label, maxLines = 1) },
            leadingIcon = {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AccentPurple.copy(alpha = 0.15f),
                selectedLabelColor = AccentPurple,
                selectedLeadingIconColor = AccentPurple
            )
        )
    }
}

@Composable
private fun SortOptionsContent(
    selectedSort: DefaulterSort,
    onSortSelect: (DefaulterSort) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Sort By",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
        
        DefaulterSort.entries.forEach { sort ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSortSelect(sort) },
                color = if (sort == selectedSort) Saffron.copy(alpha = 0.1f) else Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        sort.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (sort == selectedSort) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (sort == selectedSort) SaffronDark else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (sort == selectedSort) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Saffron
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaulterCard(
    defaulter: DefaulterInfo,
    onCardClick: () -> Unit,
    onCollectClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {
            isPressed = true
            onCardClick()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Saffron.copy(alpha = 0.8f),
                                SaffronDark
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                        Text(
                            text = defaulter.name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                    color = Color.White
                        )
                }
                
            Spacer(Modifier.width(14.dp))
                
            // Info Section
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = defaulter.name,
                        style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                    )
                
                Spacer(Modifier.height(2.dp))
                
                        Text(
                    text = "Class ${defaulter.className} • ${defaulter.fatherName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                    )
                
                Spacer(Modifier.height(6.dp))
                
                // Due Amount Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = DuesRedLight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = defaulter.dueAmount.toRupees(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = DuesRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    // Days since payment
                    defaulter.daysSincePayment?.let { days ->
                        if (days > 0) {
                            Text(
                                text = "$days days ago",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // Action Buttons Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Collect Fee Button
                    Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onCollectClick() },
                    shape = RoundedCornerShape(10.dp),
                    color = AccentGreen.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Payment,
                            contentDescription = "Collect",
                            modifier = Modifier.size(16.dp),
                            tint = AccentGreen
                        )
                        Spacer(Modifier.width(4.dp))
                    Text(
                            "Collect",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentGreen
                        )
                    }
                }
                
                // Quick Actions Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Call Button
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(enabled = defaulter.hasPhone) { onCallClick() },
                        shape = CircleShape,
                        color = if (defaulter.hasPhone) 
                            Saffron.copy(alpha = 0.12f) 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                                Icons.Default.Phone,
                                contentDescription = "Call",
                                modifier = Modifier.size(16.dp),
                                tint = if (defaulter.hasPhone) 
                                    Saffron 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                    
                    // WhatsApp Button
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(enabled = defaulter.hasPhone) { onWhatsAppClick() },
                        shape = CircleShape,
                        color = if (defaulter.hasPhone) 
                            WhatsAppGreen 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Outlined.ChatBubble,
                                contentDescription = "WhatsApp",
                                modifier = Modifier.size(16.dp),
                                tint = if (defaulter.hasPhone) 
                                    Color.White 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper functions
private suspend fun exportToPdf(context: android.content.Context, state: DefaultersState) {
    try {
        val headers = listOf("Name", "Father", "Class", "Phone", "Due Amount")
        val rows = state.filteredDefaulters.map { d ->
            listOf(
                d.name,
                d.fatherName,
                "${d.className}-${d.section}",
                d.availablePhones.joinToString(", ").ifBlank { "-" },
                d.dueAmount.toRupees()
            )
        }
        val dateFormat = SimpleDateFormat("dd_MMM_yyyy", Locale.getDefault())
        PdfGenerator.generateReport(
            context = context,
            title = "Defaulters List",
            headers = headers,
            rows = rows,
            summary = mapOf(
                "Total Defaulters" to state.filteredDefaulters.size.toString(),
                "Total Dues" to state.filteredTotalDues.toRupees()
            ),
            fileName = "Defaulters_${dateFormat.format(Date())}.pdf"
        )
        Toast.makeText(context, "PDF exported!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private suspend fun exportToExcel(context: android.content.Context, state: DefaultersState) {
    try {
        val headers = listOf("S.No", "Name", "Father Name", "Class", "Section", "Phone", "Village", "Due Amount")
        val rows = state.filteredDefaulters.mapIndexed { index, d ->
            listOf(
                (index + 1).toString(),
                d.name,
                d.fatherName,
                d.className,
                d.section,
                d.availablePhones.joinToString(", ").ifBlank { "-" },
                d.village.ifBlank { "-" },
                d.dueAmount.toLong().toString()  // Raw numeric value
            )
        }
        val dateFormat = SimpleDateFormat("dd_MMM_yyyy", Locale.getDefault())
        // Numeric columns: S.No (0), Due Amount (7)
        ExcelGenerator.generateReport(
            context = context,
            title = "Defaulters List",
            headers = headers,
            rows = rows,
            summary = mapOf(
                "Total Defaulters" to state.filteredDefaulters.size.toString(),
                "Total Dues" to state.filteredTotalDues.toLong().toString(),
                "Average Due" to state.averageDue.toLong().toString(),
                "Highest Due" to state.highestDue.toLong().toString()
            ),
            fileName = "Defaulters_${dateFormat.format(Date())}.csv",
            numericColumns = setOf(0, 7)
        )
        Toast.makeText(context, "Excel file exported!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun PhoneSelectionDialog(
    defaulter: DefaulterInfo,
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Choose a number for ${defaulter.name}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Primary Phone
                if (defaulter.phonePrimary.isNotBlank()) {
                    PhoneOptionItem(
                        phone = defaulter.phonePrimary,
                        label = "Primary",
                        actionType = actionType,
                        onClick = { onPhoneSelected(defaulter.phonePrimary) }
                    )
                }
                
                // Secondary Phone
                if (defaulter.phoneSecondary.isNotBlank()) {
                    PhoneOptionItem(
                        phone = defaulter.phoneSecondary,
                        label = "Secondary",
                        actionType = actionType,
                        onClick = { onPhoneSelected(defaulter.phoneSecondary) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PhoneOptionItem(
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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
            ) {
                Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f)
                ) {
                    Icon(
                    if (actionType == PhoneActionType.CALL) Icons.Default.Phone else Icons.Outlined.ChatBubble,
                        contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = color
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                    Text(
                    text = phone,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = color
            )
        }
    }
}

/**
 * Opens WhatsApp with pre-filled message
 */
private fun openWhatsApp(context: android.content.Context, phone: String, defaulter: DefaulterInfo) {
    val message = buildWhatsAppMessage(defaulter)
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

/**
 * Builds WhatsApp message for fee reminder (Hindi first, then English)
 */
private fun buildWhatsAppMessage(defaulter: DefaulterInfo): String {
    return """
🏫 *फीस अनुस्मारक*

प्रिय अभिभावक,

यह एक विनम्र अनुस्मारक है कि आपके बच्चे की फीस बकाया है।

👤 *विद्यार्थी:* ${defaulter.name}
📚 *कक्षा:* ${defaulter.className}
💰 *बकाया राशि:* ${defaulter.dueAmount.toRupees()}

कृपया जल्द से जल्द बकाया राशि का भुगतान करें।

⚠️ _यदि भुगतान हो चुका है, तो कृपया इस संदेश को अनदेखा करें।_

धन्यवाद 🙏

━━━━━━━━━━━━━━━

🏫 *Fee Reminder*

Dear Parent,

This is a gentle reminder that fee is pending for your ward.

👤 *Student:* ${defaulter.name}
📚 *Class:* ${defaulter.className}
💰 *Due Amount:* ${defaulter.dueAmount.toRupees()}

Please clear the dues at your earliest convenience.

⚠️ _If already paid, please ignore this message._

Thank you 🙏
    """.trimIndent()
}

/**
 * Formats phone number for WhatsApp (adds country code if needed)
 */
private fun formatPhoneForWhatsApp(phone: String): String {
    // Remove all non-digit characters
    val digitsOnly = phone.replace(Regex("[^0-9]"), "")
    
    return when {
        // Already has country code (10+ digits starting with country code)
        digitsOnly.length > 10 -> digitsOnly
        // Indian number without country code (10 digits)
        digitsOnly.length == 10 -> "91$digitsOnly"
        // Other cases - return as is
        else -> digitsOnly
    }
}

/**
 * Returns sort order for class names
 * Handles: Nursery, LKG, UKG, 1st-12th, with optional sections (A, B, C)
 * Examples: "Nursery" -> 0, "LKG" -> 1, "UKG" -> 2, "1st" -> 3, "1st-A" -> 3, "10th-B" -> 12
 */
private fun getClassSortOrder(className: String): Int {
    val upperName = className.uppercase().trim()
    
    // Pre-primary classes
    return when {
        upperName.startsWith("NUR") || upperName == "NC" -> 0
        upperName.startsWith("LKG") || upperName.startsWith("L.K.G") || upperName == "LK" -> 1
        upperName.startsWith("UKG") || upperName.startsWith("U.K.G") || upperName == "UK" -> 2
        upperName.startsWith("PREP") || upperName.startsWith("KG") -> 1
        else -> {
            // Extract number from class name (handles "1st", "2nd", "10th", "10th-A", "10-A", etc.)
            val numberStr = upperName.replace(Regex("[^0-9]"), "")
            val classNumber = numberStr.toIntOrNull() ?: 100
            
            // Add 2 to account for pre-primary (0=Nur, 1=LKG, 2=UKG, 3=1st, 4=2nd, etc.)
            classNumber + 2
        }
    }
}

private fun shareDefaultersList(context: android.content.Context, state: DefaultersState) {
    val text = buildString {
        appendLine("📋 DEFAULTERS LIST")
        appendLine("━━━━━━━━━━━━━━━━━━")
        appendLine("Total: ${state.filteredDefaulters.size} students")
        appendLine("Total Dues: ${state.filteredTotalDues.toRupees()}")
        appendLine()
        
        state.filteredDefaulters.take(20).forEachIndexed { index, d ->
            appendLine("${index + 1}. ${d.name}")
            appendLine("   Class ${d.className} | ${d.dueAmount.toRupees()}")
        }
        
        if (state.filteredDefaulters.size > 20) {
            appendLine()
            appendLine("... and ${state.filteredDefaulters.size - 20} more")
        }
    }
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Defaulters List"))
}
