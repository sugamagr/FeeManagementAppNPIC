package com.navoditpublic.fees.presentation.screens.reports.monthly

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.EmptyState
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.*
import com.navoditpublic.fees.util.ExcelGenerator
import com.navoditpublic.fees.util.PdfGenerator
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Colors for payment modes
private val CashGreen = Color(0xFF2E7D32)
private val CashGreenLight = Color(0xFFE8F5E9)
private val OnlineBlue = Color(0xFF1565C0)
private val OnlineBlueLight = Color(0xFFE3F2FD)

@Composable
fun MonthlyCollectionScreen(
    navController: NavController,
    viewModel: MonthlyCollectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val pdfDateFormat = SimpleDateFormat("MMM_yyyy", Locale.getDefault())
    
    // Month picker calendar
    val calendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, state.selectedMonth)
        set(Calendar.YEAR, state.selectedYear)
    }
    
    // Export functions
    val exportPdf: () -> Unit = {
                            scope.launch {
            try {
                val (headers, rows, summary) = viewModel.getExportData()
                PdfGenerator.generateReport(
                    context = context,
                    title = "Monthly Collection - ${viewModel.getMonthDisplayName()}",
                    headers = headers,
                    rows = rows,
                    summary = summary,
                    fileName = "Monthly_Collection_${pdfDateFormat.format(calendar.time)}.pdf"
                )
                Toast.makeText(context, "PDF exported!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    val exportExcel: () -> Unit = {
        scope.launch {
            try {
                val (headers, rows, summary) = viewModel.getExportData()
                ExcelGenerator.generateReport(
                    context = context,
                    title = "Monthly Collection - ${viewModel.getMonthDisplayName()}",
                    headers = headers,
                    rows = rows.map { row -> 
                        row.mapIndexed { index, value -> 
                            if (index == 2 || index == 4 || index == 5) {
                                ExcelGenerator.sanitizeNumeric(value.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0)
                            } else value 
                        }
                    },
                    summary = summary,
                    fileName = "Monthly_Collection_${pdfDateFormat.format(calendar.time)}.csv",
                    numericColumns = setOf(2, 3, 4, 5)
                )
                Toast.makeText(context, "Excel exported!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    Scaffold(
        containerColor = Cream,
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        if (state.isLoading) {
            LoadingScreen()
        } else {
            LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp + navigationBarPadding)
            ) {
                // Premium Header
                item {
                    MonthlyCollectionHeader(
                        onBackClick = { navController.popBackStack() },
                        onExportPdf = exportPdf,
                        onExportExcel = exportExcel,
                        hasData = state.allReceipts.isNotEmpty()
                    )
                }
                
                // Month Navigator with Mini Calendar
                item {
                    MonthNavigator(
                        monthName = viewModel.getMonthDisplayName(),
                        isCurrentMonth = state.isCurrentMonth,
                        daysWithCollection = state.daysWithCollection,
                        firstDayOfWeek = viewModel.getFirstDayOfWeek(),
                        daysInMonth = viewModel.getDaysInMonth(),
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onMonthClick = { /* Could add month picker dialog */ },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Hero Summary Card
                item {
                    HeroSummaryCard(
                        totalCollection = state.totalCollection,
                        receiptCount = state.receiptCount,
                        cashTotal = state.cashTotal,
                        cashCount = state.cashCount,
                        onlineTotal = state.onlineTotal,
                        onlineCount = state.onlineCount,
                        percentageChange = state.percentageChange,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Collection Chart
                if (state.chartData.isNotEmpty() && state.maxChartValue > 0) {
                    item {
                        CollectionChart(
                            chartData = state.chartData,
                            maxValue = state.maxChartValue,
                            monthAbbr = viewModel.getShortMonthName(),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                // Filter & Sort Section
                item {
                    MonthlyFilterSortSection(
                        paymentModeFilter = state.paymentModeFilter,
                        sortOption = state.sortOption,
                        filteredDays = state.dailyBreakdown.size,
                        totalDays = state.daysWithCollection.size,
                        onPaymentModeChange = viewModel::updatePaymentModeFilter,
                        onSortChange = viewModel::updateSortOption,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Daily Breakdown Section Header
                if (state.dailyBreakdown.isNotEmpty()) {
                    item {
                        Text(
                            text = "Daily Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                // Daily Breakdown Cards
                if (state.dailyBreakdown.isEmpty() && !state.isLoading) {
                    item {
                        EmptyState(
                            icon = Icons.Default.CalendarMonth,
                            title = "No Collections",
                            subtitle = "No receipts found for this month"
                        )
                    }
                } else {
                    itemsIndexed(
                        items = state.dailyBreakdown,
                        key = { _, item -> item.day }
                    ) { index, dayCollection ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(150, delayMillis = index * 30)) +
                                    expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                        ) {
                            DailyBreakdownCard(
                                dayCollection = dayCollection,
                                monthAbbr = viewModel.getShortMonthName(),
                                year = state.selectedYear,
                                isExpanded = state.expandedDay == dayCollection.day,
                                onToggleExpand = { viewModel.toggleDayExpanded(dayCollection.day) },
                                onNavigateToDay = { 
                                    // Navigate to daily collection for this specific day with the correct date
                                    val dateTimestamp = viewModel.getDateForDay(dayCollection.day)
                                    navController.navigate(Screen.DailyCollectionReport.createRoute(dateTimestamp))
                                },
                                onReceiptClick = { receiptId ->
                                    navController.navigate(Screen.ReceiptDetail.createRoute(receiptId))
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun MonthlyCollectionHeader(
    onBackClick: () -> Unit,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit,
    hasData: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Saffron, SaffronDark)
                )
            )
            .padding(top = 8.dp, bottom = 20.dp)
            ) {
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
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Monthly Collection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Report & Analytics",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            Row {
                IconButton(
                    onClick = onExportPdf,
                    enabled = hasData
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = "Export PDF",
                        tint = if (hasData) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
                IconButton(
                    onClick = onExportExcel,
                    enabled = hasData
                ) {
                    Icon(
                        Icons.Default.TableChart,
                        contentDescription = "Export Excel",
                        tint = if (hasData) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthNavigator(
    monthName: String,
    isCurrentMonth: Boolean,
    daysWithCollection: Set<Int>,
    firstDayOfWeek: Int,
    daysInMonth: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Month selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Month Button
                Surface(
                    onClick = onPreviousMonth,
                    shape = CircleShape,
                    color = CreamLight
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp),
                        tint = Saffron
                    )
                }
                
                // Month Name with badge
                Surface(
                    onClick = onMonthClick,
                    shape = RoundedCornerShape(12.dp),
                    color = Saffron.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = monthName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (isCurrentMonth) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SuccessGreen
                            ) {
                                Text(
                                    text = "Current",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                
                // Next Month Button
                Surface(
                    onClick = onNextMonth,
                    shape = CircleShape,
                    color = if (isCurrentMonth) DividerSoft else CreamLight
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp),
                        tint = if (isCurrentMonth) TextTertiary else Saffron
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Mini Calendar Heat Map
            MiniCalendarHeatMap(
                daysWithCollection = daysWithCollection,
                firstDayOfWeek = firstDayOfWeek,
                daysInMonth = daysInMonth
            )
        }
    }
}

@Composable
private fun MiniCalendarHeatMap(
    daysWithCollection: Set<Int>,
    firstDayOfWeek: Int,
    daysInMonth: Int
) {
    val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
    
    Column {
        // Week day headers
        Row(
                            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = TextTertiary,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Calendar grid
        val totalCells = firstDayOfWeek - 1 + daysInMonth
        val rows = (totalCells + 6) / 7
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - (firstDayOfWeek - 2)
                        
                        Box(
                            modifier = Modifier.size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day in 1..daysInMonth) {
                                val hasCollection = day in daysWithCollection
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        hasCollection -> Saffron.copy(alpha = 0.2f)
                                        else -> Color.Transparent
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = day.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (hasCollection) FontWeight.Bold else FontWeight.Normal,
                                            color = if (hasCollection) Saffron else TextTertiary
                                        )
                                        if (hasCollection) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(bottom = 2.dp)
                                                    .size(4.dp)
                                                    .background(Saffron, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSummaryCard(
    totalCollection: Double,
    receiptCount: Int,
    cashTotal: Double,
    cashCount: Int,
    onlineTotal: Double,
    onlineCount: Int,
    percentageChange: Double?,
    modifier: Modifier = Modifier
) {
    // Animated counter
    val animatedTotal = remember { Animatable(0f) }
    LaunchedEffect(totalCollection) {
        animatedTotal.animateTo(
            targetValue = totalCollection.toFloat(),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main Total Section with Gradient
            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Saffron, SaffronDark, SaffronDeep)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Total Collection",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.85f)
                                )
                    Spacer(Modifier.height(4.dp))
                                Text(
                        text = animatedTotal.value.toDouble().toRupees(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 40.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Receipt count badge
                                Surface(
                            color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                    Text(
                                    text = "$receiptCount Receipt${if (receiptCount != 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                        
                        // Percentage change badge
                        percentageChange?.let { change ->
                            val isPositive = change >= 0
                            Surface(
                                color = if (isPositive) SuccessGreen.copy(alpha = 0.3f) else ErrorRed.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${if (isPositive) "+" else ""}${String.format("%.1f", change)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Payment Breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cash
                PaymentBreakdownCard(
                    icon = Icons.Default.Money,
                    label = "Cash",
                    amount = cashTotal,
                    count = cashCount,
                    total = totalCollection,
                    color = CashGreen,
                    backgroundColor = CashGreenLight,
                    modifier = Modifier.weight(1f)
                )
                
                // Online
                PaymentBreakdownCard(
                    icon = Icons.Default.CreditCard,
                    label = "Online",
                    amount = onlineTotal,
                    count = onlineCount,
                    total = totalCollection,
                    color = OnlineBlue,
                    backgroundColor = OnlineBlueLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PaymentBreakdownCard(
    icon: ImageVector,
    label: String,
    amount: Double,
    count: Int,
    total: Double,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) (amount / total).toFloat() else 0f
    
    // Animated progress
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(percentage) {
        animatedProgress.animateTo(
            targetValue = percentage,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(8.dp)
                    )
                }
                Column {
                                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = color.copy(alpha = 0.8f)
                    )
                    Text(
                        text = amount.toRupees(),
                                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
            
            Spacer(Modifier.height(10.dp))
            
            // Progress bar
            Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.value)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
            
            Spacer(Modifier.height(6.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                    text = "$count rcpt${if (count != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.6f)
                                            )
                                            Text(
                    text = "${String.format("%.1f", percentage * 100)}%",
                    style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                    color = color.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }

@Composable
private fun CollectionChart(
    chartData: List<Pair<Int, Double>>,
    maxValue: Double,
    monthAbbr: String,
    modifier: Modifier = Modifier
) {
    var showChart by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        showChart = true
    }
    
                            Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
                            ) {
                                    Text(
                    text = "Collection Trend",
                                        style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Saffron.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = monthAbbr,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Saffron,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Bar Chart
            AnimatedVisibility(
                visible = showChart,
                enter = fadeIn(tween(400)) + expandVertically(tween(400))
            ) {
                Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val barWidth = (size.width - 20.dp.toPx()) / chartData.size
                        val maxHeight = size.height - 20.dp.toPx()
                        
                        chartData.forEachIndexed { index, (day, amount) ->
                            val barHeight = if (maxValue > 0) {
                                (amount / maxValue * maxHeight).toFloat()
                            } else 0f
                            
                            val x = index * barWidth + 10.dp.toPx()
                            val y = size.height - barHeight - 10.dp.toPx()
                            
                            // Draw bar
                            if (barHeight > 0) {
                                drawRoundRect(
                                    color = if (amount > maxValue * 0.7) Saffron 
                                           else if (amount > maxValue * 0.3) SaffronLight
                                           else SaffronContainer,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth - 4.dp.toPx(), barHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // X-axis labels (show every 5 days)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(1, 7, 14, 21, 28).filter { it <= chartData.size }.forEach { day ->
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyFilterSortSection(
    paymentModeFilter: MonthlyPaymentModeFilter,
    sortOption: MonthlySortOption,
    filteredDays: Int,
    totalDays: Int,
    onPaymentModeChange: (MonthlyPaymentModeFilter) -> Unit,
    onSortChange: (MonthlySortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Payment Mode Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                
                // Filter Chips
                MonthlyPaymentModeFilter.entries.forEach { filter ->
                    MonthlyFilterChip(
                        label = filter.displayName,
                        isSelected = paymentModeFilter == filter,
                        onClick = { onPaymentModeChange(filter) }
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                // Sort Dropdown
                MonthlySortDropdown(
                    options = MonthlySortOption.entries.map { it.displayName },
                    selectedOption = sortOption.displayName,
                    onOptionSelected = { displayName ->
                        MonthlySortOption.entries.find { it.displayName == displayName }?.let {
                            onSortChange(it)
                        }
                    }
                )
            }
            
            // Results count
            if (filteredDays != totalDays) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                    color = Saffron.copy(alpha = 0.1f)
                                                    ) {
                                                        Text(
                        text = "Showing $filteredDays of $totalDays days with collections",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Saffron,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Saffron else Color.Transparent,
        animationSpec = tween(150),
        label = "chipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        animationSpec = tween(150),
        label = "chipContent"
    )
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, DividerSoft) else null
    ) {
                                                Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun MonthlySortDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = CreamLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerSoft)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Sort,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(14.dp)
                )
                                            Text(
                    text = selectedOption,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
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
                    )
                )
            }
        }
    }
}

@Composable
private fun DailyBreakdownCard(
    dayCollection: DayCollection,
    monthAbbr: String,
    year: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNavigateToDay: () -> Unit,
    onReceiptClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onToggleExpand,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Main row (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day badge
                Surface(
                    color = Saffron.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(52.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = dayCollection.day.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Saffron
                        )
                        Text(
                            text = dayCollection.dayOfWeek,
                            style = MaterialTheme.typography.labelSmall,
                            color = Saffron.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
                
                Spacer(Modifier.width(14.dp))
                
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$monthAbbr ${dayCollection.day}, $year",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cash mini badge
                        if (dayCollection.cashCount > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Default.Money,
                                    contentDescription = null,
                                    tint = CashGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = dayCollection.cashAmount.toRupees(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CashGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        // Online mini badge
                        if (dayCollection.onlineCount > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = OnlineBlue,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = dayCollection.onlineAmount.toRupees(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnlineBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Amount and expand
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = dayCollection.totalAmount.toRupees(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Saffron
                    )
                    Text(
                        text = "${dayCollection.receiptCount} rcpt${if (dayCollection.receiptCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
                
                Spacer(Modifier.width(8.dp))
                
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(color = DividerSoft)
                    
                    // Receipts list
                    dayCollection.receipts.take(5).forEachIndexed { index, receipt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onReceiptClick(receipt.receipt.id) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Receipt number badge
                            val isCash = receipt.receipt.paymentMode == com.navoditpublic.fees.data.local.entity.PaymentMode.CASH
                            Surface(
                                color = if (isCash) CashGreenLight else OnlineBlueLight,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "#${receipt.receipt.receiptNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCash) CashGreen else OnlineBlue,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            Spacer(Modifier.width(10.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = receipt.studentName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Class ${receipt.studentClass}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            
                            Text(
                                text = receipt.receipt.netAmount.toRupees(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCash) CashGreen else OnlineBlue
                            )
                            
                            Icon(
                                Icons.Default.NavigateNext,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        if (index < dayCollection.receipts.take(5).size - 1) {
                            HorizontalDivider(
                                color = DividerSoft,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                    
                    // "View more" if there are more receipts
                    if (dayCollection.receipts.size > 5) {
                        Surface(
                            onClick = onNavigateToDay,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Saffron.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "View all ${dayCollection.receipts.size} receipts →",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Saffron,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
