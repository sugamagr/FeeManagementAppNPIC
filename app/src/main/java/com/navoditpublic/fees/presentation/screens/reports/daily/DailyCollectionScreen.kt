package com.navoditpublic.fees.presentation.screens.reports.daily

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.presentation.components.EmptyState
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.components.SessionBannerCompact
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCollectionScreen(
    navController: NavController,
    initialDate: Long? = null,
    viewModel: DailyCollectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Set initial date if provided (e.g., from Monthly Collection screen)
    LaunchedEffect(initialDate) {
        if (initialDate != null && initialDate > 0L) {
            viewModel.setDate(initialDate)
        }
    }
    
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    val shortDateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val pdfDateFormat = SimpleDateFormat("dd_MMM_yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    val calendar = Calendar.getInstance().apply { timeInMillis = state.selectedDate }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            viewModel.setDate(newCalendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    // Export functions
    val exportPdf: () -> Unit = {
                            scope.launch {
                                try {
                val headers = listOf("Receipt #", "Student", "Class", "Amount", "Mode", "Time")
                                    val rows = state.receipts.map { r ->
                                        listOf(
                                            "#${r.receipt.receiptNumber}",
                                            r.studentName,
                                            r.studentClass,
                                            r.receipt.netAmount.toRupees(),
                        r.receipt.paymentMode.name,
                        timeFormat.format(Date(r.receipt.createdAt))
                                        )
                                    }
                                    PdfGenerator.generateReport(
                                        context = context,
                                        title = "Daily Collection - ${dateFormat.format(Date(state.selectedDate))}",
                                        headers = headers,
                                        rows = rows,
                                        summary = mapOf(
                                            "Total Collection" to state.totalCollection.toRupees(),
                        "Total Receipts" to state.allReceipts.size.toString(),
                        "Cash" to "${state.cashTotal.toRupees()} (${state.cashCount})",
                        "Online" to "${state.onlineTotal.toRupees()} (${state.onlineCount})"
                    ),
                                        fileName = "Daily_Collection_${pdfDateFormat.format(Date(state.selectedDate))}.pdf"
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
                val headers = listOf("Receipt #", "Student", "Class", "Father Name", "Amount", "Mode", "Time")
                val rows = state.receipts.map { r ->
                    listOf(
                        r.receipt.receiptNumber.toString(),
                        r.studentName,
                        r.studentClass,
                        r.fatherName,
                        ExcelGenerator.sanitizeNumeric(r.receipt.netAmount),
                        r.receipt.paymentMode.name,
                        timeFormat.format(Date(r.receipt.createdAt))
                    )
                }
                ExcelGenerator.generateReport(
                    context = context,
                    title = "Daily Collection - ${dateFormat.format(Date(state.selectedDate))}",
                    headers = headers,
                    rows = rows,
                    summary = mapOf(
                        "Total Collection" to state.totalCollection.toRupees(),
                        "Cash Total" to state.cashTotal.toRupees(),
                        "Online Total" to state.onlineTotal.toRupees()
                    ),
                    fileName = "Daily_Collection_${pdfDateFormat.format(Date(state.selectedDate))}.csv",
                    numericColumns = setOf(0, 4)
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
                    DailyCollectionHeader(
                        onBackClick = { navController.popBackStack() },
                        onExportPdf = exportPdf,
                        onExportExcel = exportExcel,
                        hasData = state.allReceipts.isNotEmpty()
                    )
                }
                
                // Date Navigation
                item {
                    DateNavigator(
                        selectedDate = state.selectedDate,
                        isToday = state.isToday,
                        dateFormat = dateFormat,
                        onPreviousDay = { viewModel.previousDay() },
                        onNextDay = { viewModel.nextDay() },
                        onDateClick = { datePickerDialog.show() },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Summary Card with integrated payment breakdown
                item {
                    CollectionSummaryCard(
                        totalCollection = state.totalCollection,
                        receiptCount = state.allReceipts.size,
                        cashTotal = state.cashTotal,
                        cashCount = state.cashCount,
                        onlineTotal = state.onlineTotal,
                        onlineCount = state.onlineCount,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Session Banner (when viewing historical session)
                if (!state.isViewingCurrentSession && state.selectedSessionInfo != null) {
                    item {
                        SessionBannerCompact(
                            sessionInfo = state.selectedSessionInfo,
                            onSwitchClick = { navController.navigate(Screen.AcademicSessions.route) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                // Filter & Sort Section
                item {
                    FilterSortSection(
                        paymentModeFilter = state.paymentModeFilter,
                        sortOption = state.sortOption,
                        classes = state.classes,
                        selectedClass = state.selectedClass,
                        filteredCount = state.receipts.size,
                        totalCount = state.allReceipts.size,
                        onPaymentModeChange = viewModel::updatePaymentModeFilter,
                        onSortChange = viewModel::updateSortOption,
                        onClassChange = viewModel::updateSelectedClass,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Receipt List or Empty State
                if (state.receipts.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Receipt,
                            title = if (state.allReceipts.isEmpty()) "No Collections" else "No Results",
                            subtitle = if (state.allReceipts.isEmpty()) 
                                "No receipts for this date" 
                            else 
                                "No receipts match your filter"
                        )
                    }
                } else {
                    itemsIndexed(
                        items = state.receipts,
                        key = { _, item -> item.receipt.id }
                    ) { index, receiptWithStudent ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(150, delayMillis = index * 30)) + 
                                    expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                        ) {
                            ReceiptCard(
                                receiptNumber = receiptWithStudent.receipt.receiptNumber,
                                studentName = receiptWithStudent.studentName,
                                studentClass = receiptWithStudent.studentClass,
                                amount = receiptWithStudent.receipt.netAmount,
                                paymentMode = receiptWithStudent.receipt.paymentMode,
                                time = timeFormat.format(Date(receiptWithStudent.receipt.createdAt)),
                                onClick = {
                                    navController.navigate(Screen.ReceiptDetail.createRoute(receiptWithStudent.receipt.id))
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
private fun DailyCollectionHeader(
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
            .padding(top = 8.dp, bottom = 16.dp)
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
                    text = "Daily Collection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
private fun DateNavigator(
    selectedDate: Long,
    isToday: Boolean,
    dateFormat: SimpleDateFormat,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateClick: () -> Unit,
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
                .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
            // Previous Day Button
            Surface(
                onClick = onPreviousDay,
                shape = CircleShape,
                color = CreamLight
            ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Previous Day",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp),
                    tint = Saffron
                        )
                    }
                    
            // Date Picker
                    Surface(
                onClick = onDateClick,
                        shape = RoundedCornerShape(12.dp),
                color = Saffron.copy(alpha = 0.1f)
                    ) {
                        Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                        tint = Saffron,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = dateFormat.format(Date(selectedDate)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (isToday) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen
                        ) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Next Day Button
            Surface(
                onClick = onNextDay,
                shape = CircleShape,
                color = if (isToday) DividerSoft else CreamLight,
                modifier = Modifier.then(
                    if (isToday) Modifier else Modifier
                )
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next Day",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp),
                    tint = if (isToday) TextTertiary else Saffron
                )
            }
        }
    }
}

@Composable
private fun CollectionSummaryCard(
    totalCollection: Double,
    receiptCount: Int,
    cashTotal: Double,
    cashCount: Int,
    onlineTotal: Double,
    onlineCount: Int,
    modifier: Modifier = Modifier
) {
            Card(
        modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
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
                            colors = listOf(Saffron, SaffronDark)
                        )
                    )
                    .padding(20.dp)
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
                        text = totalCollection.toRupees(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 36.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(
                        color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                            text = "$receiptCount Receipt${if (receiptCount != 1) "s" else ""}",
                                style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                    }
                }
            }
            
            // Payment Breakdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cash
                PaymentBreakdownItem(
                    icon = Icons.Default.Money,
                    label = "Cash",
                    amount = cashTotal,
                    count = cashCount,
                    color = CashGreen,
                    backgroundColor = CashGreenLight,
                    modifier = Modifier.weight(1f)
                )
                
                // Online
                PaymentBreakdownItem(
                    icon = Icons.Default.CreditCard,
                    label = "Online",
                    amount = onlineTotal,
                    count = onlineCount,
                    color = OnlineBlue,
                    backgroundColor = OnlineBlueLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PaymentBreakdownItem(
    icon: ImageVector,
    label: String,
    amount: Double,
    count: Int,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
                        Surface(
        modifier = modifier,
                            shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                Text(
                    text = "$count rcpt${if (count != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

@Composable
private fun FilterSortSection(
    paymentModeFilter: PaymentModeFilter,
    sortOption: ReceiptSortOption,
    classes: List<String>,
    selectedClass: String,
    filteredCount: Int,
    totalCount: Int,
    onPaymentModeChange: (PaymentModeFilter) -> Unit,
    onSortChange: (ReceiptSortOption) -> Unit,
    onClassChange: (String) -> Unit,
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
                PaymentModeFilter.entries.forEach { filter ->
                    FilterChip(
                        label = filter.displayName,
                        isSelected = paymentModeFilter == filter,
                        onClick = { onPaymentModeChange(filter) }
                    )
                }
            }
            
            // Class and Sort Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Class Dropdown
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Class",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    PremiumDropdown(
                        options = classes,
                        selectedOption = selectedClass,
                        onOptionSelected = onClassChange,
                        icon = Icons.Default.FilterList
                    )
                }
                
                // Sort Dropdown
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sort By",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    PremiumDropdown(
                        options = ReceiptSortOption.entries.map { it.displayName },
                        selectedOption = sortOption.displayName,
                        onOptionSelected = { displayName ->
                            ReceiptSortOption.entries.find { it.displayName == displayName }?.let {
                                onSortChange(it)
                            }
                        },
                        icon = Icons.Default.Sort
                    )
                }
            }
            
            // Results count
            if (filteredCount != totalCount) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Saffron.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Showing $filteredCount of $totalCount receipts",
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
private fun PremiumDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // Dropdown Trigger Button
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(10.dp),
            color = CreamLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerSoft)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Saffron,
                        modifier = Modifier.size(16.dp)
                    )
                                            Text(
                        text = selectedOption,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        // Dropdown Menu with limited height and scroll
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .heightIn(max = 250.dp)
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
                    modifier = Modifier
                        .background(if (isSelected) Saffron.copy(alpha = 0.08f) else Color.Transparent)
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
private fun ReceiptCard(
    receiptNumber: Int,
    studentName: String,
    studentClass: String,
    amount: Double,
    paymentMode: PaymentMode,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCash = paymentMode == PaymentMode.CASH
    val badgeColor = if (isCash) CashGreen else OnlineBlue
    val badgeBgColor = if (isCash) CashGreenLight else OnlineBlueLight
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Receipt Number Badge (color-coded)
            Surface(
                color = badgeBgColor,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "#$receiptNumber",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }
            
            Spacer(Modifier.width(14.dp))
            
            // Student Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Class $studentClass",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Icon(
                        if (isCash) Icons.Default.Money else Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = paymentMode.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }
            
            // Amount
            Text(
                text = amount.toRupees(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Saffron
            )
        }
    }
}
