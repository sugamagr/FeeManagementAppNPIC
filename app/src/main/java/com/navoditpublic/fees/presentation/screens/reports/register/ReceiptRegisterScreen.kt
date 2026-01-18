package com.navoditpublic.fees.presentation.screens.reports.register

import android.app.DatePickerDialog
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.domain.model.ReceiptWithStudent
import com.navoditpublic.fees.presentation.components.EmptyState
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.util.ExcelGenerator
import com.navoditpublic.fees.util.PdfGenerator
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Colors
private val CashGreen = Color(0xFF2E7D32)
private val CashGreenLight = Color(0xFFE8F5E9)
private val OnlineBlue = Color(0xFF1565C0)
private val OnlineBlueLight = Color(0xFFE3F2FD)
private val CancelledRed = Color(0xFFD32F2F)
private val CancelledRedLight = Color(0xFFFFEBEE)
private val SurfaceWhite = Color(0xFFFAFAFA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptRegisterScreen(
    navController: NavController,
    viewModel: ReceiptRegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val pdfDateFormat = SimpleDateFormat("dd_MMM_yyyy", Locale.getDefault())
    
    var showSortMenu by remember { mutableStateOf(false) }
    var showClassMenu by remember { mutableStateOf(false) }
    
    // Date pickers
    val startCalendar = Calendar.getInstance().apply { timeInMillis = state.startDate }
    val startDatePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            val calendar = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0) }
            viewModel.setStartDate(calendar.timeInMillis)
        },
        startCalendar.get(Calendar.YEAR),
        startCalendar.get(Calendar.MONTH),
        startCalendar.get(Calendar.DAY_OF_MONTH)
    )
    
    val endCalendar = Calendar.getInstance().apply { timeInMillis = state.endDate }
    val endDatePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            val calendar = Calendar.getInstance().apply { set(year, month, day, 23, 59, 59) }
            viewModel.setEndDate(calendar.timeInMillis)
        },
        endCalendar.get(Calendar.YEAR),
        endCalendar.get(Calendar.MONTH),
        endCalendar.get(Calendar.DAY_OF_MONTH)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Premium Header
            item {
                ReceiptRegisterHeader(
                    dateRange = "${dateFormat.format(Date(state.startDate))} - ${dateFormat.format(Date(state.endDate))}",
                    onBackClick = { navController.popBackStack() },
                    onPdfExport = {
                            scope.launch {
                                try {
                                val (headers, rows, summary) = viewModel.getExportData()
                                    PdfGenerator.generateReport(
                                        context = context,
                                    title = "Receipt Register",
                                        headers = headers,
                                        rows = rows,
                                    summary = summary,
                                        fileName = "Receipt_Register_${pdfDateFormat.format(Date(state.startDate))}_to_${pdfDateFormat.format(Date(state.endDate))}.pdf"
                                    )
                                Toast.makeText(context, "PDF exported successfully!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                    onExcelExport = {
                        scope.launch {
                            try {
                                val (headers, rows, summary) = viewModel.getExportData()
                                ExcelGenerator.generateReport(
                                    context = context,
                                    title = "Receipt Register",
                                    headers = headers,
                                    rows = rows,
                                    summary = summary,
                                    fileName = "Receipt_Register_${pdfDateFormat.format(Date(state.startDate))}_to_${pdfDateFormat.format(Date(state.endDate))}.csv"
                                )
                                Toast.makeText(context, "Excel exported successfully!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    isExportEnabled = state.receipts.isNotEmpty()
                )
            }
            
            // Date Presets
            item {
                DatePresetsSection(
                    selectedPreset = state.datePreset,
                    onPresetSelected = { viewModel.setDatePreset(it) },
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onStartDateClick = { startDatePicker.show() },
                    onEndDateClick = { endDatePicker.show() },
                    dateFormat = dateFormat
                )
            }
            
            // Hero Summary Card
            item {
                HeroSummaryCard(
                    totalAmount = state.totalAmount,
                    activeCount = state.activeReceiptCount,
                    cancelledCount = state.cancelledReceiptCount,
                    cashTotal = state.cashTotal,
                    cashCount = state.cashCount,
                    onlineTotal = state.onlineTotal,
                    onlineCount = state.onlineCount,
                    cancelledTotal = state.cancelledTotal,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Filters Section
            item {
                FiltersSection(
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    paymentModeFilter = state.paymentModeFilter,
                    onPaymentModeFilterChange = { viewModel.updatePaymentModeFilter(it) },
                    statusFilter = state.statusFilter,
                    onStatusFilterChange = { viewModel.updateStatusFilter(it) },
                    selectedClass = state.selectedClass,
                    availableClasses = state.availableClasses,
                    onClassSelected = { viewModel.updateSelectedClass(it) },
                    showClassMenu = showClassMenu,
                    onShowClassMenuChange = { showClassMenu = it },
                    sortOption = state.sortOption,
                    onSortOptionChange = { viewModel.updateSortOption(it) },
                    showSortMenu = showSortMenu,
                    onShowSortMenuChange = { showSortMenu = it },
                    filteredCount = state.receipts.size,
                    totalCount = state.allReceipts.size,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Loading / Empty / Receipt List
            if (state.isLoading) {
                item {
                    LoadingScreen(modifier = Modifier.height(300.dp))
                }
            } else if (state.receipts.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Receipt,
                        title = if (state.searchQuery.isNotBlank()) "No Results" else "No Receipts",
                        subtitle = if (state.searchQuery.isNotBlank()) 
                            "No receipts match your search" 
                        else 
                            "No receipts found for this period",
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                // Group receipts by date
                val groupedReceipts = state.receipts.groupBy { receipt ->
                    val cal = Calendar.getInstance().apply { timeInMillis = receipt.receipt.receiptDate }
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }.toSortedMap(compareByDescending { it })
                
                groupedReceipts.forEach { (dateTimestamp, receiptsForDate) ->
                    // Date section header
                    item(key = "header_$dateTimestamp") {
                        DateSectionHeader(
                            date = dateTimestamp,
                            receiptCount = receiptsForDate.size,
                            totalAmount = receiptsForDate.sumOf { it.receipt.netAmount },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    items(
                        items = receiptsForDate,
                        key = { receipt -> receipt.receipt.id }
                    ) { receiptWithStudent ->
                        EnhancedReceiptCard(
                            receiptWithStudent = receiptWithStudent,
                            onClick = {
                                navController.navigate(Screen.ReceiptDetail.createRoute(receiptWithStudent.receipt.id))
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ============================================================================
// HEADER
// ============================================================================

@Composable
private fun ReceiptRegisterHeader(
    dateRange: String,
    onBackClick: () -> Unit,
    onPdfExport: () -> Unit,
    onExcelExport: () -> Unit,
    isExportEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Saffron, SaffronDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(bottom = 20.dp)
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
                        text = "Receipt Register",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = dateRange,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                
                // Export buttons
                IconButton(
                    onClick = onPdfExport,
                    enabled = isExportEnabled
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = "Export PDF",
                        tint = if (isExportEnabled) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
                
                IconButton(
                    onClick = onExcelExport,
                    enabled = isExportEnabled
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = "Export Excel",
                        tint = if (isExportEnabled) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ============================================================================
// DATE PRESETS
// ============================================================================

@Composable
private fun DatePresetsSection(
    selectedPreset: DatePreset,
    onPresetSelected: (DatePreset) -> Unit,
    startDate: Long,
    endDate: Long,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    dateFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
                ) {
                    Column(
        modifier = modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
        // Presets row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DatePreset.entries.filter { it != DatePreset.CUSTOM }) { preset ->
                DatePresetChip(
                    preset = preset,
                    isSelected = selectedPreset == preset,
                    onClick = { onPresetSelected(preset) }
                )
            }
        }
        
        // Custom date range
        AnimatedVisibility(
            visible = selectedPreset == DatePreset.CUSTOM || true,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DatePickerButton(
                    label = "From",
                    date = dateFormat.format(Date(startDate)),
                    onClick = onStartDateClick,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(20.dp)
                )
                
                DatePickerButton(
                    label = "To",
                    date = dateFormat.format(Date(endDate)),
                    onClick = onEndDateClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePresetChip(
    preset: DatePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Saffron else Color.White,
        animationSpec = tween(200),
        label = "chipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Gray,
        animationSpec = tween(200),
        label = "chipContent"
    )
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier.animateContentSize()
                    ) {
                        Text(
            text = preset.displayName,
                            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun DatePickerButton(
    label: String,
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
                        ) {
                            Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                Icons.Default.CalendarMonth,
                                        contentDescription = null,
                tint = Saffron,
                modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
            Column {
                                    Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================================================
// HERO SUMMARY CARD
// ============================================================================

@Composable
private fun HeroSummaryCard(
    totalAmount: Double,
    activeCount: Int,
    cancelledCount: Int,
    cashTotal: Double,
    cashCount: Int,
    onlineTotal: Double,
    onlineCount: Int,
    cancelledTotal: Double,
    modifier: Modifier = Modifier
) {
    // Animation
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(totalAmount) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }
    val displayAmount = (totalAmount * animatedProgress.value)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Main total
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Saffron, SaffronDark)
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                        text = "₹${String.format("%,.0f", displayAmount)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Total Collection",
                                style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                            Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$activeCount Active",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        if (cancelledCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "$cancelledCount Cancelled",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Payment breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                PaymentBreakdownCard(
                    icon = Icons.Default.Money,
                    title = "Cash",
                    amount = cashTotal,
                    count = cashCount,
                    color = CashGreen,
                    backgroundColor = CashGreenLight,
                    totalAmount = totalAmount,
                                modifier = Modifier.weight(1f)
                )
                
                PaymentBreakdownCard(
                    icon = Icons.Default.CreditCard,
                    title = "Online",
                    amount = onlineTotal,
                    count = onlineCount,
                    color = OnlineBlue,
                    backgroundColor = OnlineBlueLight,
                    totalAmount = totalAmount,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PaymentBreakdownCard(
    icon: ImageVector,
    title: String,
    amount: Double,
    count: Int,
    color: Color,
    backgroundColor: Color,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    val percentage = if (totalAmount > 0) (amount / totalAmount).toFloat() else 0f
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(600),
        label = "percentage"
    )
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                        icon,
                                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                                    )
                }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = amount.toRupees(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = "$count receipts",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedPercentage)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
                            }
                            
                            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

// ============================================================================
// FILTERS SECTION
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    paymentModeFilter: PaymentModeFilter,
    onPaymentModeFilterChange: (PaymentModeFilter) -> Unit,
    statusFilter: ReceiptStatusFilter,
    onStatusFilterChange: (ReceiptStatusFilter) -> Unit,
    selectedClass: String,
    availableClasses: List<String>,
    onClassSelected: (String) -> Unit,
    showClassMenu: Boolean,
    onShowClassMenuChange: (Boolean) -> Unit,
    sortOption: ReceiptSortOption,
    onSortOptionChange: (ReceiptSortOption) -> Unit,
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
    filteredCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    Column(modifier = modifier) {
        // Search bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
                    tint = Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
                
                Spacer(Modifier.width(12.dp))
                
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    cursorBrush = SolidColor(Saffron),
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                    Text(
                                    text = "Search receipt #, student...",
                                style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Class Filter - Prominent position
        Text(
            text = "Filter by Class",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        CustomClassDropdown(
            selectedClass = selectedClass,
            availableClasses = availableClasses,
            expanded = showClassMenu,
            onExpandedChange = onShowClassMenuChange,
            onClassSelected = {
                onClassSelected(it)
                onShowClassMenuChange(false)
            }
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Filter Row: Sort + Payment + Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Sort Dropdown
            CustomSortDropdown(
                selectedOption = sortOption,
                expanded = showSortMenu,
                onExpandedChange = onShowSortMenuChange,
                onOptionSelected = {
                    onSortOptionChange(it)
                    onShowSortMenuChange(false)
                },
                modifier = Modifier.weight(1f)
            )
            
            // Payment Mode Dropdown
            CustomPaymentModeSelector(
                selectedFilter = paymentModeFilter,
                onFilterSelected = onPaymentModeFilterChange,
                modifier = Modifier.weight(1f)
            )
            
            // Status Dropdown
            CustomStatusSelector(
                selectedFilter = statusFilter,
                onFilterSelected = onStatusFilterChange,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Results count
                            Surface(
                                shape = RoundedCornerShape(8.dp),
            color = Saffron.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                        imageVector = if (filteredCount == totalCount) Icons.Default.Receipt else Icons.Default.Search,
                                        contentDescription = null,
                        tint = SaffronDark,
                        modifier = Modifier.size(16.dp)
                                    )
                    Spacer(Modifier.width(6.dp))
                                    Text(
                        text = if (filteredCount == totalCount) {
                            "$totalCount receipts"
                        } else {
                            "Showing $filteredCount of $totalCount"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = SaffronDark
                    )
                }
                
                if (selectedClass != "All" || paymentModeFilter != PaymentModeFilter.ALL || statusFilter != ReceiptStatusFilter.ALL) {
                    Surface(
                        onClick = {
                            onClassSelected("All")
                            onPaymentModeFilterChange(PaymentModeFilter.ALL)
                            onStatusFilterChange(ReceiptStatusFilter.ALL)
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = "Clear Filters",
                            style = MaterialTheme.typography.labelSmall,
                            color = Saffron,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
// ============================================================================
// CUSTOM DROPDOWN COMPONENTS
// ============================================================================

@Composable
private fun CustomClassDropdown(
    selectedClass: String,
    availableClasses: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClassSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFiltered = selectedClass != "All"
    
    Box(modifier = modifier) {
        // Selected value button
            Surface(
            onClick = { onExpandedChange(!expanded) },
            shape = RoundedCornerShape(12.dp),
            color = if (isFiltered) Saffron else Color.White,
            shadowElevation = if (isFiltered) 4.dp else 2.dp,
            modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                    .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isFiltered) Color.White.copy(alpha = 0.2f) else Saffron.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = if (isFiltered) Color.White else Saffron,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Class",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFiltered) Color.White.copy(alpha = 0.8f) else Color.Gray
                        )
                        Text(
                            text = if (selectedClass == "All") "All Classes" else selectedClass,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isFiltered) Color.White else Color.Black
                        )
                    }
                }
                
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isFiltered) Color.White else Color.Gray
                )
            }
        }
        
        // Dropdown menu (proper overlay)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(16.dp))
                .fillMaxWidth(0.92f)
        ) {
            // Scrollable list with always visible scrollbar
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .heightIn(max = 280.dp)
                    .verticalScroll(scrollState)
                    .drawWithContent {
                        drawContent()
                        // Draw scrollbar
                        val scrollbarHeight = (size.height * size.height) / 
                            (scrollState.maxValue + size.height).coerceAtLeast(1f)
                        val scrollbarY = (scrollState.value.toFloat() / 
                            (scrollState.maxValue).coerceAtLeast(1)) * (size.height - scrollbarHeight)
                        if (scrollState.maxValue > 0) {
                            drawRoundRect(
                                color = Saffron.copy(alpha = 0.7f),
                                topLeft = Offset(size.width - 5.dp.toPx(), scrollbarY),
                                size = Size(3.dp.toPx(), scrollbarHeight),
                                cornerRadius = CornerRadius(1.5f.dp.toPx())
                            )
                        }
                    }
                    .padding(end = 8.dp)
            ) {
                availableClasses.forEach { className ->
                    val isSelected = className == selectedClass
                    Surface(
                        onClick = { onClassSelected(className) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Saffron.copy(alpha = 0.12f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Saffron else Color.LightGray.copy(alpha = 0.5f))
                                )
                                Spacer(Modifier.width(12.dp))
                        Text(
                                    text = if (className == "All") "All Classes" else className,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Saffron else Color.DarkGray
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Saffron,
                                    modifier = Modifier.size(20.dp)
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
private fun CustomSortDropdown(
    selectedOption: ReceiptSortOption,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOptionSelected: (ReceiptSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Surface(
            onClick = { onExpandedChange(!expanded) },
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Sort",
                            style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
        
        // Dropdown menu (proper overlay)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(Color.White, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                ReceiptSortOption.entries.forEach { option ->
                    val isSelected = option == selectedOption
                    Surface(
                        onClick = { onOptionSelected(option) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Saffron.copy(alpha = 0.12f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                text = option.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Saffron else Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomPaymentModeSelector(
    selectedFilter: PaymentModeFilter,
    onFilterSelected: (PaymentModeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val color = when (selectedFilter) {
        PaymentModeFilter.ALL -> Saffron
        PaymentModeFilter.CASH -> CashGreen
        PaymentModeFilter.ONLINE -> OnlineBlue
    }
    
    val icon = when (selectedFilter) {
        PaymentModeFilter.CASH -> Icons.Default.Money
        PaymentModeFilter.ONLINE -> Icons.Default.CreditCard
        PaymentModeFilter.ALL -> Icons.Default.Payments
    }
    
    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(10.dp),
            color = if (selectedFilter != PaymentModeFilter.ALL) color else Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selectedFilter != PaymentModeFilter.ALL) Color.White else Saffron,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = selectedFilter.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedFilter != PaymentModeFilter.ALL) Color.White else Color.Gray
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                PaymentModeFilter.entries.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    val filterColor = when (filter) {
                        PaymentModeFilter.ALL -> Saffron
                        PaymentModeFilter.CASH -> CashGreen
                        PaymentModeFilter.ONLINE -> OnlineBlue
                    }
                    val filterIcon = when (filter) {
                        PaymentModeFilter.CASH -> Icons.Default.Money
                        PaymentModeFilter.ONLINE -> Icons.Default.CreditCard
                        PaymentModeFilter.ALL -> Icons.Default.Payments
                    }
                    Surface(
                        onClick = {
                            onFilterSelected(filter)
                            expanded = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) filterColor.copy(alpha = 0.12f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(filterColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        filterIcon,
                                        contentDescription = null,
                                        tint = filterColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = filter.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) filterColor else Color.DarkGray
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = filterColor,
                                    modifier = Modifier.size(18.dp)
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
private fun CustomStatusSelector(
    selectedFilter: ReceiptStatusFilter,
    onFilterSelected: (ReceiptStatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val color = when (selectedFilter) {
        ReceiptStatusFilter.ALL -> Saffron
        ReceiptStatusFilter.ACTIVE -> CashGreen
        ReceiptStatusFilter.CANCELLED -> CancelledRed
    }
    
    val icon = when (selectedFilter) {
        ReceiptStatusFilter.ACTIVE -> Icons.Default.TaskAlt
        ReceiptStatusFilter.CANCELLED -> Icons.Default.RemoveCircle
        ReceiptStatusFilter.ALL -> Icons.Default.Tune
    }
    
    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(10.dp),
            color = if (selectedFilter != ReceiptStatusFilter.ALL) color else Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selectedFilter != ReceiptStatusFilter.ALL) Color.White else Saffron,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = selectedFilter.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedFilter != ReceiptStatusFilter.ALL) Color.White else Color.Gray
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                ReceiptStatusFilter.entries.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    val filterColor = when (filter) {
                        ReceiptStatusFilter.ALL -> Saffron
                        ReceiptStatusFilter.ACTIVE -> CashGreen
                        ReceiptStatusFilter.CANCELLED -> CancelledRed
                    }
                    val filterIcon = when (filter) {
                        ReceiptStatusFilter.ACTIVE -> Icons.Default.TaskAlt
                        ReceiptStatusFilter.CANCELLED -> Icons.Default.RemoveCircle
                        ReceiptStatusFilter.ALL -> Icons.Default.Tune
                    }
                    Surface(
                        onClick = {
                            onFilterSelected(filter)
                            expanded = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) filterColor.copy(alpha = 0.12f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(filterColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        filterIcon,
                                        contentDescription = null,
                                        tint = filterColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = filter.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) filterColor else Color.DarkGray
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = filterColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// DATE SECTION HEADER
// ============================================================================

@Composable
private fun DateSectionHeader(
    date: Long,
    receiptCount: Int,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
    val today = Calendar.getInstance()
    val dateCalendar = Calendar.getInstance().apply { timeInMillis = date }
    
    val isToday = today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                  today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
    
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                      yesterday.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
    
    val displayDate = when {
        isToday -> "Today"
        isYesterday -> "Yesterday"
        else -> dateFormat.format(Date(date))
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Saffron.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Date icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Saffron.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Saffron,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "$receiptCount receipt${if (receiptCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            // Total amount for the day
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Saffron.copy(alpha = 0.12f)
            ) {
                Text(
                    text = totalAmount.toRupees(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Saffron,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ============================================================================
// ENHANCED RECEIPT CARD
// ============================================================================

@Composable
private fun EnhancedReceiptCard(
    receiptWithStudent: ReceiptWithStudent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
                        val receipt = receiptWithStudent.receipt
    val isCancelled = receipt.isCancelled
    val isCash = receipt.paymentMode == PaymentMode.CASH
    
    val dateTimeFormat = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
    
                        Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
            containerColor = when {
                isCancelled -> CancelledRedLight.copy(alpha = 0.4f)
                else -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCancelled) 0.dp else 2.dp
        ),
        onClick = onClick
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
            // Receipt badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isCancelled -> CancelledRed.copy(alpha = 0.1f)
                            isCash -> CashGreen.copy(alpha = 0.1f)
                            else -> OnlineBlue.copy(alpha = 0.1f)
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "#${receipt.receiptNumber}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                        color = when {
                            isCancelled -> CancelledRed
                            isCash -> CashGreen
                            else -> OnlineBlue
                        }
                    )
                    Icon(
                        imageVector = when {
                            isCancelled -> Icons.Default.Cancel
                            isCash -> Icons.Default.Money
                            else -> Icons.Default.CreditCard
                        },
                        contentDescription = null,
                        tint = when {
                            isCancelled -> CancelledRed
                            isCash -> CashGreen
                            else -> OnlineBlue
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(14.dp))
            
            // Details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = receiptWithStudent.studentName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isCancelled) TextDecoration.LineThrough else null,
                    color = if (isCancelled) CancelledRed.copy(alpha = 0.7f) else Color.Black
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                                        Text(
                        text = receiptWithStudent.studentClass,
                                            style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                                        )
                                        Text(
                        text = " • ",
                                            style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = dateTimeFormat.format(Date(receipt.receiptDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                if (isCancelled) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CancelledRed,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "CANCELLED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // Amount
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = receipt.netAmount.toRupees(),
                    style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                    color = when {
                        isCancelled -> CancelledRed.copy(alpha = 0.6f)
                        isCash -> CashGreen
                        else -> OnlineBlue
                    },
                    textDecoration = if (isCancelled) TextDecoration.LineThrough else null
                )
                
                if (!isCancelled && receipt.discountAmount > 0) {
                                                Text(
                        text = "Disc: ${receipt.discountAmount.toRupees()}",
                                                    style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

