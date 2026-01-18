package com.navoditpublic.fees.presentation.screens.fee_collection.collect

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
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
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.domain.model.StudentWithBalance
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.components.SearchBar
import com.navoditpublic.fees.presentation.navigation.LocalDrawerState
import com.navoditpublic.fees.presentation.theme.DueChipBackground
import com.navoditpublic.fees.presentation.theme.DueChipText
import com.navoditpublic.fees.presentation.theme.PaidChipBackground
import com.navoditpublic.fees.presentation.theme.PaidChipText
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.presentation.theme.SaffronLight
import com.navoditpublic.fees.util.DateUtils
import com.navoditpublic.fees.util.NumberToWords
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

// Premium Colors
private val SuccessGreen = Color(0xFF10B981)
private val SuccessGreenLight = Color(0xFFD1FAE5)
private val DeepTeal = Color(0xFF0D9488)
private val SoftPurple = Color(0xFF8B5CF6)
private val WarmOrange = Color(0xFFF97316)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CollectFeeScreen(
    preSelectedStudentId: Long?,
    navController: NavController,
    viewModel: CollectFeeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showStudentSheet by remember { mutableStateOf(false) }
    var showReceiptPreview by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val previewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val drawerState = LocalDrawerState.current
    
    // Animation states for staggered entrance
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }
    
    val isFromBottomNav = preSelectedStudentId == null || preSelectedStudentId == -1L
    
    // Handle back navigation properly for nested states
    BackHandler(
        enabled = state.selectedStudent != null || showReceiptPreview || showDatePicker || showStudentSheet
    ) {
        when {
            // Close receipt preview first
            showReceiptPreview -> {
                showReceiptPreview = false
            }
            // Close date picker
            showDatePicker -> {
                showDatePicker = false
            }
            // Close student sheet
            showStudentSheet -> {
                scope.launch { sheetState.hide(); showStudentSheet = false }
            }
            // Clear selected student and go back to student selection
            state.selectedStudent != null -> {
                viewModel.clearStudent()
            }
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CollectFeeEvent.Success -> {
                    showReceiptPreview = false
                    showSuccessAnimation = true
                    delay(2500)
                    navController.popBackStack()
                }
                is CollectFeeEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    // Success Animation Overlay
    AnimatedVisibility(
        visible = showSuccessAnimation,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300))
    ) {
        SuccessOverlay(
            receiptNumber = state.receiptNumber,
            studentName = state.selectedStudent?.student?.name ?: "",
            amount = state.total
        )
    }
    
    // Receipt Preview Bottom Sheet
    if (showReceiptPreview) {
        ModalBottomSheet(
            onDismissRequest = { showReceiptPreview = false },
            sheetState = previewSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            ReceiptPreviewContent(
                receiptNumber = state.receiptNumber,
                studentName = state.selectedStudent?.student?.name ?: "",
                className = state.selectedStudent?.student?.classSection ?: "",
                amount = state.total,
                discount = state.discount,
                paymentMode = state.paymentMode,
                receiptDate = state.receiptDate,
                remarks = state.details,
                isSaving = state.isSaving,
                onConfirm = { viewModel.saveReceipt() },
                onCancel = { showReceiptPreview = false }
            )
        }
    }
    
    // Date Picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.receiptDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateReceiptDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
    
    // Student Selection Sheet
    if (showStudentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStudentSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            StudentSelectionSheet(
                searchQuery = state.studentSearchQuery,
                onSearchChange = viewModel::searchStudents,
                searchResults = state.searchResults,
                allStudents = state.allStudents,
                onStudentSelected = { student ->
                    viewModel.selectStudent(student)
                    scope.launch { sheetState.hide(); showStudentSheet = false }
                }
            )
        }
    }
    
    Scaffold(
        topBar = {
            // Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Saffron, SaffronDark)
                        )
                    )
                    .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Show back arrow when student is selected, otherwise show menu (for bottom nav) or back (for direct nav)
                    if (state.selectedStudent != null) {
                        // When student is selected, back clears the student
                        IconButton(onClick = { viewModel.clearStudent() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to student selection", tint = Color.White)
                        }
                    } else if (isFromBottomNav) {
                        // From bottom nav with no student selected - show menu
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                        }
                    } else {
                        // Direct navigation with no student selected - normal back
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (state.selectedStudent != null) "Collect Fee" else "Select Student",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (state.selectedStudent != null) {
                            Text(
                                state.selectedStudent!!.student.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Floating Action Bottom Bar
            AnimatedVisibility(
                visible = state.selectedStudent != null && state.total > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        // Summary Row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Amount to Pay",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    state.total.toRupees(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Saffron
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Balance After",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    state.remainingDues.toRupees(),
                                    style = MaterialTheme.typography.titleMedium,
                                     fontWeight = FontWeight.Bold,
                                    color = if (state.remainingDues > 0) 
                                        MaterialTheme.colorScheme.error else PaidChipText
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        // Main CTA Button
                        Button(
                            onClick = { showReceiptPreview = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            enabled = state.receiptNumber.isNotBlank() && 
                                     !state.showDuplicateWarning && 
                                     !state.isSaving && 
                                     state.dateError == null &&
                                     state.details.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        ) {
                            Icon(Icons.Default.Receipt, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Preview Receipt",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else {
            // Show full-screen student selection when no student selected
            if (state.selectedStudent == null) {
                StudentSelectionFullScreen(
                    allStudents = state.allStudents,
                    searchQuery = state.studentSearchQuery,
                    searchResults = state.searchResults,
                    onSearchChange = viewModel::searchStudents,
                    onStudentSelected = viewModel::selectStudent,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            } else {
                // Show normal fee collection flow when student is selected
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Receipt Info Card - only after student selection
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(300)) + slideInVertically(
                                initialOffsetY = { -20 },
                                animationSpec = tween(300)
                            )
                        ) {
                            ReceiptInfoCard(
                                receiptNumber = state.receiptNumber,
                                onReceiptNumberChange = viewModel::updateReceiptNumber,
                                showDuplicateWarning = state.showDuplicateWarning,
                                receiptDate = state.receiptDate,
                                isBackdated = state.isBackdatedReceipt,
                                dateError = state.dateError,
                                onDateClick = { showDatePicker = true }
                            )
                        }
                    }
                    
                    // Student Info Card
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(300, delayMillis = 100)) + slideInVertically(
                                initialOffsetY = { -20 },
                                animationSpec = tween(300, delayMillis = 100)
                            )
                        ) {
                            EnhancedStudentCard(
                                student = state.selectedStudent!!,
                                duesBreakdown = state.duesBreakdown,
                                onChangeStudent = { 
                                    viewModel.clearStudent()
                                }
                            )
                        }
                    }
                
                // Amount Entry Section
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(300, delayMillis = 200)) + slideInVertically(
                            initialOffsetY = { -20 },
                            animationSpec = tween(300, delayMillis = 200)
                        )
                    ) {
                        AmountEntryCard(
                            amountReceived = state.amountReceived,
                            onAmountChange = viewModel::updateAmountReceived,
                            duesBreakdown = state.duesBreakdown,
                            isFullYearPayment = state.isFullYearPayment,
                            fullYearDiscountAmount = state.fullYearDiscountAmount,
                            onToggleFullYear = viewModel::toggleFullYearPayment,
                            remarks = state.details,
                            onRemarksChange = viewModel::updateDetails
                        )
                    }
                }
                
                // Payment Mode Selection
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(300, delayMillis = 300)) + slideInVertically(
                            initialOffsetY = { -20 },
                            animationSpec = tween(300, delayMillis = 300)
                        )
                    ) {
                        PaymentModeCard(
                            selectedMode = state.paymentMode,
                            onModeSelect = viewModel::updatePaymentMode
                        )
                    }
                }
                
                // Summary Card
                if (state.total > 0) {
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(300, delayMillis = 400)) + slideInVertically(
                                initialOffsetY = { -20 },
                                animationSpec = tween(300, delayMillis = 400)
                            )
                        ) {
                            SummaryCard(
                                subtotal = state.subtotal,
                                discount = state.discount,
                                total = state.total
                            )
                        }
                    }
                }
                
                // Bottom spacing for FAB
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}
}

// ============================================================================
// RECEIPT INFO CARD
// ============================================================================

@Composable
private fun ReceiptInfoCard(
    receiptNumber: String,
    onReceiptNumberChange: (String) -> Unit,
    showDuplicateWarning: Boolean,
    receiptDate: Long,
    isBackdated: Boolean,
    dateError: String?,
    onDateClick: () -> Unit
) {
    Card(
                        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Saffron.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = Saffron,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Receipt Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Receipt Number
                                OutlinedTextField(
                    value = receiptNumber,
                    onValueChange = onReceiptNumberChange,
                    label = { Text("Receipt No.*") },
                    placeholder = { Text("Enter number") },
                                    modifier = Modifier.weight(1f),
                    isError = showDuplicateWarning,
                                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Saffron,
                        cursorColor = Saffron
                    ),
                    supportingText = if (showDuplicateWarning) {
                        { 
                            Text(
                                "Already exists!", 
                                color = MaterialTheme.colorScheme.error
                            ) 
                        }
                    } else null
                )
                
                // Date Selector
                                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onDateClick),
                    color = when {
                        dateError != null -> MaterialTheme.colorScheme.errorContainer
                        isBackdated -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                                        Icon(
                            if (isBackdated) Icons.Default.History else Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = when {
                                dateError != null -> MaterialTheme.colorScheme.error
                                isBackdated -> MaterialTheme.colorScheme.tertiary
                                else -> Saffron
                            }
                        )
                        Spacer(Modifier.height(4.dp))
                                        Text(
                            DateUtils.formatDate(receiptDate),
                                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Warning messages
            if (dateError != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        dateError,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (isBackdated) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.History,
                        null,
                        Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Backdated receipt - balances will be recalculated",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

// ============================================================================
// STUDENT SELECTION FULL SCREEN
// ============================================================================

private enum class SortOption(val label: String) {
    BY_CLASS("By Class"),
    ALPHABETICAL("A-Z"),
    DUES_HIGH("Dues ↓"),
    DUES_LOW("Dues ↑")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudentSelectionFullScreen(
    allStudents: List<StudentWithBalance>,
    searchQuery: String,
    searchResults: List<StudentWithBalance>,
    onSearchChange: (String) -> Unit,
    onStudentSelected: (StudentWithBalance) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedClassSection by remember { mutableStateOf<String?>(null) } // null = All
    var showClassPicker by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(SortOption.BY_CLASS) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    // Handle back press to close dialogs/menus first
    BackHandler(enabled = showClassPicker || showSortMenu) {
        when {
            showClassPicker -> showClassPicker = false
            showSortMenu -> showSortMenu = false
        }
    }
    
    // Get unique class-sections from students
    val classSections = remember(allStudents) {
        allStudents
            .map { it.student.classSection }
            .distinct()
            .sortedWith(compareBy(
                { classOrder(it.substringBefore("-").trim()) },
                { it.substringAfter("-", "A").trim() }
            ))
    }
    
    // Filter students by selected class
    val filteredByClass = remember(allStudents, selectedClassSection) {
        if (selectedClassSection == null) {
            allStudents
        } else {
            allStudents.filter { it.student.classSection == selectedClassSection }
        }
    }
    
    // Track if we're actively searching (query >= 2 chars)
    val isSearching = searchQuery.length >= 2
    
    // Determine which students to show (search takes priority)
    val studentsToShow = remember(searchQuery, searchResults, filteredByClass, sortOption, selectedClassSection, isSearching) {
        val baseList = if (isSearching) {
            // When searching, use search results (even if empty - to show "no results" state)
            if (selectedClassSection != null) {
                searchResults.filter { it.student.classSection == selectedClassSection }
            } else {
                searchResults
            }
        } else {
            filteredByClass
        }
        
        // Apply sorting
        val effectiveSortOption = if (selectedClassSection != null && sortOption == SortOption.BY_CLASS) {
            SortOption.ALPHABETICAL // When class is selected, default to alphabetical
        } else {
            sortOption
        }
        
        when (effectiveSortOption) {
            SortOption.BY_CLASS -> baseList.sortedWith(compareBy(
                { classOrder(it.student.currentClass) },
                { it.student.section },
                { it.student.name }
            ))
            SortOption.ALPHABETICAL -> baseList.sortedBy { it.student.name }
            SortOption.DUES_HIGH -> baseList.sortedByDescending { it.currentBalance }
            SortOption.DUES_LOW -> baseList.sortedBy { it.currentBalance }
        }
    }
    
    val duesCount = studentsToShow.count { it.currentBalance > 0 }
    
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Saffron.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Select Student",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${studentsToShow.size} students${if (duesCount > 0) " • $duesCount with dues" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = onSearchChange,
            placeholder = "Search by name, account no, father name...",
            prominent = true
        )
        
        Spacer(Modifier.height(12.dp))
        
        // Filter & Sort Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Class Filter Chip
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showClassPicker = true },
                color = if (selectedClassSection != null) Saffron.copy(alpha = 0.15f) 
                       else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = if (selectedClassSection != null) BorderStroke(1.dp, Saffron) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (selectedClassSection != null) Saffron else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        selectedClassSection ?: "All Classes",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedClassSection != null) Saffron else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (selectedClassSection != null) Saffron else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Sort Chip
            Box {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showSortMenu = true },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Sort,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            sortOption.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                sortOption = option
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortOption == option) {
                                    Icon(Icons.Default.Check, null, tint = Saffron)
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.weight(1f))
            
            // Clear filter button (when class is selected)
            if (selectedClassSection != null) {
                Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { selectedClassSection = null },
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear filter",
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Student List - Full height
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(studentsToShow, key = { it.student.id }) { student ->
                CompactStudentItem(
                    student = student,
                    onClick = { onStudentSelected(student) }
                )
            }
            
            if (studentsToShow.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            when {
                                isSearching -> "No students found for \"$searchQuery\""
                                selectedClassSection != null -> "No students in $selectedClassSection"
                                else -> "No students available"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (isSearching) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Try a different search term",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Class Picker Dialog
    if (showClassPicker) {
        ClassPickerDialog(
            classSections = classSections,
            selectedClassSection = selectedClassSection,
            onClassSelected = { 
                selectedClassSection = it
                // Reset sort to alphabetical when class is selected
                if (it != null && sortOption == SortOption.BY_CLASS) {
                    sortOption = SortOption.ALPHABETICAL
                }
                showClassPicker = false
            },
            onDismiss = { showClassPicker = false },
            allStudents = allStudents
        )
    }
}

// Helper function to order classes correctly
private fun classOrder(className: String): Int {
    return when (className.uppercase().trim()) {
        "NC" -> 0
        "LKG" -> 1
        "UKG" -> 2
        "1ST" -> 3
        "2ND" -> 4
        "3RD" -> 5
        "4TH" -> 6
        "5TH" -> 7
        "6TH" -> 8
        "7TH" -> 9
        "8TH" -> 10
        "9TH" -> 11
        "10TH" -> 12
        "11TH" -> 13
        "12TH" -> 14
        else -> 99
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClassPickerDialog(
    classSections: List<String>,
    selectedClassSection: String?,
    onClassSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    allStudents: List<StudentWithBalance>
) {
    // Group class-sections by class
    val groupedClasses = remember(classSections) {
        classSections.groupBy { it.substringBefore("-").trim() }
            .toSortedMap(compareBy { classOrder(it) })
    }
    
    // Calculate totals for all students
    val totalStudents = allStudents.size
    val totalWithDues = remember(allStudents) {
        allStudents.count { it.currentBalance > 0 }
    }
    val totalDuesAmount = remember(allStudents) {
        allStudents.filter { it.currentBalance > 0 }.sumOf { it.currentBalance.toLong() }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Saffron, SaffronDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Select Class",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${classSections.size} sections available",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // All Classes Hero Card
                AllClassesHeroCard(
                    isSelected = selectedClassSection == null,
                    totalStudents = totalStudents,
                    studentsWithDues = totalWithDues,
                    totalDuesAmount = totalDuesAmount,
                    onClick = { onClassSelected(null) }
                )
                
                Spacer(Modifier.height(20.dp))
                
                // Grouped classes with grid layout
                groupedClasses.forEach { (className, sections) ->
                    // Class group header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(getClassAccentColor(className))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "CLASS ${className.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                    
                    // Grid of section cards (2 columns)
                    val sortedSections = sections.sorted()
                    val rows = sortedSections.chunked(2)
                    
                    rows.forEach { rowSections ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowSections.forEach { classSection ->
                                val isSelected = selectedClassSection == classSection
                                val studentCount = allStudents.count { it.student.classSection == classSection }
                                val duesCount = allStudents.count { 
                                    it.student.classSection == classSection && it.currentBalance > 0 
                                }
                                val duesAmount = allStudents
                                    .filter { it.student.classSection == classSection && it.currentBalance > 0 }
                                    .sumOf { it.currentBalance.toLong() }
                                
                                ClassSectionCard(
                                    classSection = classSection,
                                    isSelected = isSelected,
                                    studentCount = studentCount,
                                    duesCount = duesCount,
                                    duesAmount = duesAmount,
                                    accentColor = getClassAccentColor(className),
                                    onClick = { onClassSelected(classSection) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill empty space if odd number of sections in row
                            if (rowSections.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = null
    )
}

// Get accent color based on class name
@Composable
private fun getClassAccentColor(className: String): Color {
    return when {
        className.lowercase() in listOf("nursery", "nur", "play", "pg") -> Color(0xFFFF6B6B)
        className.lowercase() in listOf("lkg", "ukg", "kg1", "kg2", "prep") -> Color(0xFFFFB347)
        className in listOf("1", "2", "3", "I", "II", "III") -> Color(0xFF4ECDC4)
        className in listOf("4", "5", "6", "IV", "V", "VI") -> Color(0xFF7C3AED)
        className in listOf("7", "8", "9", "10", "VII", "VIII", "IX", "X") -> Color(0xFF4F46E5)
        className in listOf("11", "12", "XI", "XII") -> Color(0xFF475569)
        else -> Saffron
    }
}

@Composable
private fun AllClassesHeroCard(
    isSelected: Boolean,
    totalStudents: Int,
    studentsWithDues: Int,
    totalDuesAmount: Long,
    onClick: () -> Unit
) {
    val paidStudents = totalStudents - studentsWithDues
    val paidPercentage = if (totalStudents > 0) (paidStudents * 100f / totalStudents) else 0f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF334155)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFFF59E0B)) else null
    ) {
        Box {
            // Subtle pattern overlay
            Canvas(modifier = Modifier.matchParentSize()) {
                val patternColor = Color.White.copy(alpha = 0.03f)
                for (i in 0..10) {
                    drawCircle(
                        color = patternColor,
                        radius = 40.dp.toPx(),
                        center = Offset(
                            x = size.width * (0.1f + i * 0.15f),
                            y = size.height * 0.3f
                        )
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            "All Classes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // Paid percentage badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (paidPercentage >= 80) Color(0xFF10B981).copy(alpha = 0.2f)
                               else if (paidPercentage >= 50) Color(0xFFF59E0B).copy(alpha = 0.2f)
                               else Color(0xFFEF4444).copy(alpha = 0.2f)
                    ) {
                        Text(
                            "${paidPercentage.toInt()}% Clear",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (paidPercentage >= 80) Color(0xFF10B981)
                                   else if (paidPercentage >= 50) Color(0xFFF59E0B)
                                   else Color(0xFFEF4444)
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Total Students
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Total Students",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            "$totalStudents",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // Dues Pending
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B).copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Dues Pending",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            "$studentsWithDues",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
                
                if (totalDuesAmount > 0) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total Pending Amount",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                totalDuesAmount.toRupees(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassSectionCard(
    classSection: String,
    isSelected: Boolean,
    studentCount: Int,
    duesCount: Int,
    duesAmount: Long,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paidCount = studentCount - duesCount
    val allPaid = duesCount == 0 && studentCount > 0
    
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.08f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        border = if (isSelected) BorderStroke(2.dp, accentColor) 
                else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header with class name and selection indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Class badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(accentColor, accentColor.copy(alpha = 0.7f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        classSection.substringAfter("-").take(2).ifEmpty { 
                            classSection.take(2) 
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else if (allPaid) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.1f)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "All Paid",
                            tint = Color(0xFF10B981),
                            modifier = Modifier
                                .padding(4.dp)
                                .size(14.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(10.dp))
            
            // Class section name
            Text(
                classSection,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(10.dp))
            
            // Stats with clear labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total students
                Column {
                    Text(
                        "Students",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Text(
                        "$studentCount",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Dues count
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (duesCount > 0) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) 
                               else Color(0xFF10B981).copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                    Text(
                        if (duesCount > 0) "$duesCount" else "0",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (duesCount > 0) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                    )
                }
            }
            
            // Progress bar showing paid vs pending
            if (studentCount > 0) {
                Spacer(Modifier.height(10.dp))
                
                val paidPercentage = paidCount.toFloat() / studentCount.toFloat()
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(paidPercentage)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (allPaid) Color(0xFF10B981)
                                else if (paidPercentage >= 0.7f) accentColor
                                else if (paidPercentage >= 0.4f) Color(0xFFF59E0B)
                                else MaterialTheme.colorScheme.error
                            )
                    )
                }
                
                Spacer(Modifier.height(6.dp))
                
                // Amount pending (if any)
                if (duesAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            duesAmount.toRupees(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                            fontSize = 10.sp
                        )
                    }
                } else {
                    Text(
                        "All Clear ✓",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF10B981),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactStudentItem(
    student: StudentWithBalance,
    onClick: () -> Unit
) {
    val village = student.student.addressLine2.ifBlank { student.student.district }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Saffron.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    student.student.name.take(2).uppercase(),
                                         style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Saffron
                )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        student.student.name,
                                         style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (student.student.hasTransport) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.DirectionsBus,
                            contentDescription = "Transport",
                            modifier = Modifier.size(14.dp),
                            tint = SoftPurple
                        )
                    }
                }
                Text(
                    "F: ${student.student.fatherName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${student.student.classSection} • ${student.student.accountNumber}${if (village.isNotBlank()) " • $village" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (student.currentBalance > 0)
                    MaterialTheme.colorScheme.errorContainer
                else
                    PaidChipBackground
            ) {
                Text(
                    if (student.currentBalance > 0) student.currentBalance.toRupees() else "Paid",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (student.currentBalance > 0)
                        MaterialTheme.colorScheme.error
                    else
                        PaidChipText
                )
            }
        }
    }
}

// ============================================================================
// ENHANCED STUDENT CARD WITH DUES BREAKDOWN
// ============================================================================

@Composable
private fun EnhancedStudentCard(
    student: StudentWithBalance,
    duesBreakdown: DuesBreakdown,
    onChangeStudent: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
// Gradient Header
            val village = student.student.addressLine2.ifBlank { student.student.district }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Saffron, SaffronDark)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                student.student.name
                                    .split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.uppercase() }
                                    .joinToString(""),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(Modifier.width(14.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                student.student.name,
                                style = MaterialTheme.typography.titleMedium,
                                         fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "F/o ${student.student.fatherName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "Class ${student.student.classSection}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    student.student.accountNumber,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        
                        // Change Button
                        IconButton(
                            onClick = onChangeStudent,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Change",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    // Village and Transport Info Row
                    Spacer(Modifier.height(10.dp))
                    Row(
                                    modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Village
                        if (village.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        village,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        
                        // Transport Status
                                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (duesBreakdown.hasTransport) 
                                SoftPurple.copy(alpha = 0.3f) 
                            else 
                                Color.White.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.White
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (duesBreakdown.hasTransport) 
                                        "${duesBreakdown.transportRouteName} @ ₹${duesBreakdown.monthlyTransportRate.toInt()}/mo"
                                    else 
                                        "No Transport",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
            
            // Quick Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Call Button
                if (student.student.phonePrimary.isNotBlank()) {
                                        Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${student.student.phonePrimary}")
                                }
                                context.startActivity(intent)
                            },
                        color = DeepTeal.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                null,
                                Modifier.size(18.dp),
                                tint = DeepTeal
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Call",
                                                 style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = DeepTeal
                            )
                        }
                    }
                    
                    // SMS Button
                                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("smsto:${student.student.phonePrimary}")
                                }
                                context.startActivity(intent)
                            },
                        color = SoftPurple.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Sms,
                                null,
                                Modifier.size(18.dp),
                                tint = SoftPurple
                            )
                                            Spacer(Modifier.width(8.dp))
                            Text(
                                "SMS",
                                                 style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = SoftPurple
                            )
                        }
                    }
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            // Dues Breakdown Section
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "DUES BREAKDOWN",
                                                     style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                
                Spacer(Modifier.height(4.dp))
                
                // Fee Items
                if (duesBreakdown.previousYearDues > 0) {
                    DuesLineItem(
                        icon = Icons.Default.History,
                        label = "Previous Year Dues",
                        amount = duesBreakdown.previousYearDues,
                        iconColor = WarmOrange
                    )
                }
                
                if (duesBreakdown.tuitionFee > 0) {
                    DuesLineItem(
                        icon = Icons.Default.School,
                        label = "Tuition Fee",
                        amount = duesBreakdown.tuitionFee,
                        iconColor = DeepTeal
                    )
                }
                
                if (duesBreakdown.transportFee > 0) {
                    DuesLineItem(
                        icon = Icons.Default.DirectionsBus,
                        label = "Transport Fee (11 months)",
                        amount = duesBreakdown.transportFee,
                        iconColor = SoftPurple
                    )
                }
                
                if (duesBreakdown.admissionFee > 0) {
                    DuesLineItem(
                        icon = Icons.Default.Person,
                        label = "Admission Fee",
                        amount = duesBreakdown.admissionFee,
                        iconColor = Saffron
                    )
                }
                
                if (duesBreakdown.registrationFee > 0) {
                    DuesLineItem(
                        icon = Icons.Default.Receipt,
                        label = "Registration Fee",
                        amount = duesBreakdown.registrationFee,
                        iconColor = Color(0xFF6366F1)
                    )
                }
                
                // Total Charges Line
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total Charges",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        duesBreakdown.totalCharges.toRupees(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Payments Applied
                if (duesBreakdown.totalPayments > 0) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                Modifier.size(16.dp),
                                tint = SuccessGreen
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Payments Applied",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SuccessGreen
                            )
                        }
                        Text(
                            "-${duesBreakdown.totalPayments.toRupees()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }
                }
                
                // Balance Due - Highlighted
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 2.dp,
                    color = if (duesBreakdown.balanceDue > 0) 
                        MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    else 
                        SuccessGreen.copy(alpha = 0.3f)
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (duesBreakdown.balanceDue > 0)
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else
                        SuccessGreenLight
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (duesBreakdown.balanceDue > 0) Icons.Default.AccountBalance 
                                else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (duesBreakdown.balanceDue > 0)
                                    MaterialTheme.colorScheme.error
                                else
                                    SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (duesBreakdown.balanceDue > 0) "BALANCE DUE" else "FULLY PAID",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (duesBreakdown.balanceDue > 0)
                                    MaterialTheme.colorScheme.error
                                else
                                    SuccessGreen
                            )
                        }
                        Text(
                            duesBreakdown.balanceDue.toRupees(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (duesBreakdown.balanceDue > 0)
                                MaterialTheme.colorScheme.error
                            else
                                SuccessGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DuesLineItem(
    icon: ImageVector,
    label: String,
    amount: Double,
    iconColor: Color
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Text(
            amount.toRupees(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// ============================================================================
// AMOUNT ENTRY CARD
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmountEntryCard(
    amountReceived: String,
    onAmountChange: (String) -> Unit,
    duesBreakdown: DuesBreakdown,
    isFullYearPayment: Boolean,
    fullYearDiscountAmount: Double,
    onToggleFullYear: (Boolean) -> Unit,
    remarks: String,
    onRemarksChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DeepTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        tint = DeepTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Enter Amount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Amount Input Field
                                OutlinedTextField(
                value = amountReceived,
                onValueChange = onAmountChange,
                                    modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(
                        "0",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                },
                leadingIcon = {
                    Text(
                        "₹",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Saffron
                    )
                },
                                    singleLine = true,
                shape = RoundedCornerShape(14.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron,
                    cursorColor = Saffron,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Particulars / Description Field (Mandatory)
            OutlinedTextField(
                value = remarks,
                onValueChange = onRemarksChange,
                label = { Text("Particulars *") },
                placeholder = { 
                    Text(
                        "e.g., April-June (Tuition + Transport)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = remarks.isBlank() && amountReceived.isNotBlank(),
                supportingText = {
                    Text(
                        "Describe the payment (months, fee type)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron,
                    cursorColor = Saffron,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Contextual Quick Amounts
            var showCalculatorDialog by remember { mutableStateOf(false) }
            
            // For monthly-fee classes: tuition + transport combined
            val monthlyTotal = duesBreakdown.monthlyTuitionRate + duesBreakdown.monthlyTransportRate
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "QUICK SELECT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                
                // Calculator Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showCalculatorDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    color = SoftPurple.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = SoftPurple
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Calculator",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SoftPurple
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Column(modifier = Modifier.fillMaxWidth()) {
                // Show hint for monthly classes
                if (duesBreakdown.isMonthlyFeeClass && monthlyTotal > 0) {
                    Text(
                        "Monthly = Tuition + Transport",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (duesBreakdown.isMonthlyFeeClass) {
                        // Monthly fee classes: Show 1M, 3M, 6M (tuition + transport)
                        if (monthlyTotal > 0) {
                            QuickAmountChip(
                                label = "1M",
                                amount = monthlyTotal,
                                onClick = { onAmountChange(monthlyTotal.toInt().toString()) },
                                color = DeepTeal,
                                modifier = Modifier.weight(1f)
                            )
                            
                            QuickAmountChip(
                                label = "3M",
                                amount = monthlyTotal * 3,
                                onClick = { onAmountChange((monthlyTotal * 3).toInt().toString()) },
                                color = DeepTeal,
                                modifier = Modifier.weight(1f)
                            )
                            
                            QuickAmountChip(
                                label = "6M",
                                amount = monthlyTotal * 6,
                                onClick = { onAmountChange((monthlyTotal * 6).toInt().toString()) },
                                color = DeepTeal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // Annual fee classes: Show fixed amounts 1000, 3000, 5000
                        QuickAmountChip(
                            label = "₹1K",
                            amount = 1000.0,
                            onClick = { onAmountChange("1000") },
                            color = DeepTeal,
                            modifier = Modifier.weight(1f)
                        )
                        
                        QuickAmountChip(
                            label = "₹3K",
                            amount = 3000.0,
                            onClick = { onAmountChange("3000") },
                            color = DeepTeal,
                            modifier = Modifier.weight(1f)
                        )
                        
                        QuickAmountChip(
                            label = "₹5K",
                            amount = 5000.0,
                            onClick = { onAmountChange("5000") },
                            color = DeepTeal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Full Balance (for all classes)
                    if (duesBreakdown.balanceDue > 0) {
                        QuickAmountChip(
                            label = "Full",
                            amount = duesBreakdown.balanceDue,
                            onClick = { onAmountChange(duesBreakdown.balanceDue.toInt().toString()) },
                            color = Saffron,
                            isPrimary = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Calculator Dialog
            if (showCalculatorDialog) {
                FeeCalculatorDialog(
                    duesBreakdown = duesBreakdown,
                    onDismiss = { showCalculatorDialog = false },
                    onApplyAmount = { amount ->
                        onAmountChange(amount.toInt().toString())
                        showCalculatorDialog = false
                    }
                )
            }
            
            // Full Year Discount Offer
            if (fullYearDiscountAmount > 0) {
                Spacer(Modifier.height(16.dp))
                
                val bgColor by animateColorAsState(
                    targetValue = if (isFullYearPayment) 
                        Saffron.copy(alpha = 0.1f) 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    label = "fullYearBg"
                )
                
                        Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleFullYear(!isFullYearPayment) },
                    color = bgColor,
                    shape = RoundedCornerShape(12.dp),
                    border = if (isFullYearPayment)
                        androidx.compose.foundation.BorderStroke(2.dp, Saffron)
                    else null
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = if (isFullYearPayment) Saffron 
                                  else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Full Year Payment",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Get 1 month FREE (${fullYearDiscountAmount.toRupees()} off)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Toggle indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFullYearPayment) Saffron
                                    else Color.Transparent
                                )
                                .border(
                                    2.dp,
                                    if (isFullYearPayment) Saffron
                                    else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFullYearPayment) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
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
private fun QuickAmountChip(
    label: String,
    amount: Double,
    onClick: () -> Unit,
    color: Color,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isPrimary) color.copy(alpha = 0.15f) 
               else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp),
        border = if (isPrimary) 
            androidx.compose.foundation.BorderStroke(1.dp, color) 
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = if (isPrimary) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "₹${amount.toInt()}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPrimary) color else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}


// ============================================================================
// PAYMENT MODE CARD
// ============================================================================

@Composable
private fun PaymentModeCard(
    selectedMode: PaymentMode,
    onModeSelect: (PaymentMode) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SoftPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = SoftPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Payment Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Payment Mode Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentModeOption(
                    icon = Icons.Default.Money,
                    label = "Cash",
                    isSelected = selectedMode == PaymentMode.CASH,
                    onClick = { onModeSelect(PaymentMode.CASH) },
                    modifier = Modifier.weight(1f),
                    color = SuccessGreen
                )
                
                PaymentModeOption(
                    icon = Icons.Default.CreditCard,
                    label = "Online/UPI",
                    isSelected = selectedMode == PaymentMode.ONLINE,
                    onClick = { onModeSelect(PaymentMode.ONLINE) },
                    modifier = Modifier.weight(1f),
                    color = SoftPurple
                )
            }
        }
    }
}

@Composable
private fun PaymentModeOption(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    
    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.1f)
               else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, color)
        else
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) color.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ============================================================================
// SUMMARY CARD
// ============================================================================

@Composable
private fun SummaryCard(
    subtotal: Double,
    discount: Double,
    total: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Saffron.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "PAYMENT SUMMARY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Saffron,
                letterSpacing = 1.sp
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Amount Received",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    subtotal.toRupees(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (discount > 0) {
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalOffer,
                            null,
                            Modifier.size(14.dp),
                            tint = SuccessGreen
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Full Year Bonus",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuccessGreen
                        )
                    }
                    Text(
                        "+${discount.toRupees()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = SuccessGreen
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = Saffron.copy(alpha = 0.3f)
            )
            
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total Dues Cleared",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    (subtotal + discount).toRupees(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Saffron
                )
            }
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                NumberToWords.convert(total),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// ============================================================================
// STUDENT SELECTION SHEET
// ============================================================================

@Composable
private fun StudentSelectionSheet(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    searchResults: List<StudentWithBalance>,
    allStudents: List<StudentWithBalance>,
    onStudentSelected: (StudentWithBalance) -> Unit
) {
    val isSearching = searchQuery.length >= 2
    
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Select Student",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        SearchBar(
            query = searchQuery,
            onQueryChange = onSearchChange,
            placeholder = "Search by name, account number, father name...",
            prominent = true
        )
        
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                // Show search results section when actively searching
                item {
                    Text(
                        "SEARCH RESULTS (${searchResults.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Saffron,
                        letterSpacing = 1.sp
                    )
                }
                
                if (searchResults.isEmpty()) {
                    // No results found
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No students found for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Try a different search term",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    items(searchResults) { student ->
                        StudentListItem(student, onStudentSelected)
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
            
            // Show all students only when not searching
            if (!isSearching) {
                item {
                    Text(
                        "ALL STUDENTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
                items(allStudents) { student ->
                    StudentListItem(student, onStudentSelected)
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun StudentListItem(
    student: StudentWithBalance,
    onClick: (StudentWithBalance) -> Unit
) {
    val village = student.student.addressLine2.ifBlank { student.student.district }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(student) },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Saffron.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    student.student.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Saffron
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        student.student.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (student.student.hasTransport) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.DirectionsBus,
                            contentDescription = "Transport",
                            modifier = Modifier.size(14.dp),
                            tint = SoftPurple
                        )
                    }
                }
                Text(
                    "F/o ${student.student.fatherName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${student.student.classSection} • ${student.student.accountNumber}${if (village.isNotBlank()) " • $village" else ""}",
                     style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (student.currentBalance > 0)
                    MaterialTheme.colorScheme.errorContainer
                else
                    PaidChipBackground
            ) {
                Text(
                    if (student.currentBalance > 0) student.currentBalance.toRupees() else "Paid",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (student.currentBalance > 0)
                        MaterialTheme.colorScheme.error
                    else
                        PaidChipText
                )
            }
        }
    }
}

// ============================================================================
// SUCCESS ANIMATION OVERLAY
// ============================================================================

@Composable
private fun SuccessOverlay(
    receiptNumber: String,
    studentName: String,
    amount: Double
) {
    val particles = remember {
        List(50) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.5f,
                angle = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 2f + 1f,
                size = Random.nextFloat() * 12f + 6f,
                color = listOf(
                    Saffron, SaffronLight, SuccessGreen,
                    Color(0xFF3B82F6), Color(0xFFEC4899), Color(0xFF8B5CF6)
                ).random()
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    val checkScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        checkScale.animateTo(1.2f, tween(300, easing = FastOutSlowInEasing))
        checkScale.animateTo(1f, tween(150))
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val progress = (animProgress + particle.y) % 1f
                val x = particle.x * size.width + sin(progress * 10f + particle.angle) * 50f
                val y = progress * size.height * 1.5f
                
                drawCircle(
                    color = particle.color.copy(alpha = (1f - progress).coerceIn(0f, 1f)),
                    radius = particle.size,
                    center = Offset(x, y)
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(checkScale.value)
                    .clip(CircleShape)
                    .background(SuccessGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                "Payment Saved!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Receipt #$receiptNumber",
                style = MaterialTheme.typography.titleMedium,
                color = SuccessGreen
            )
            
            Spacer(Modifier.height(24.dp))
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        amount.toRupees(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)

// ============================================================================
// RECEIPT PREVIEW
// ============================================================================

@Composable
private fun ReceiptPreviewContent(
    receiptNumber: String,
    studentName: String,
    className: String,
    amount: Double,
    discount: Double,
    paymentMode: PaymentMode,
    receiptDate: Long,
    remarks: String,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Receipt Preview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Saffron.copy(alpha = 0.1f)
            ) {
                Text(
                    "#$receiptNumber",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Saffron,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF5)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Saffron, SaffronDark))
                        )
                )
                
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Saffron, SaffronDark))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                studentName
                                    .split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.uppercase() }
                                    .joinToString(""),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(Modifier.width(14.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                studentName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Class $className",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(20.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Saffron.copy(alpha = 0.08f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Amount Received",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                amount.toRupees(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Saffron
                            )
                            if (discount > 0) {
                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SuccessGreenLight
                                ) {
                                    Text(
                                        "+${discount.toRupees()} bonus",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = SuccessGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    null,
                                    Modifier.size(14.dp),
                                    tint = Color.Gray
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Date", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                dateFormat.format(java.util.Date(receiptDate)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (paymentMode == PaymentMode.CASH) Icons.Default.Money 
                                    else Icons.Default.CreditCard,
                                    null,
                                    Modifier.size(14.dp),
                                    tint = Color.Gray
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Mode", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (paymentMode == PaymentMode.CASH) "Cash" else "Online",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    if (remarks.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Gray.copy(alpha = 0.05f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Remarks", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Spacer(Modifier.height(2.dp))
                                Text(remarks, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        NumberToWords.convert(amount),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray.copy(alpha = 0.1f),
                    contentColor = Color.DarkGray
                ),
                enabled = !isSaving
            ) {
                Text("Edit", fontWeight = FontWeight.SemiBold)
            }
            
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(2f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm & Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Fee Calculator Dialog
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeeCalculatorDialog(
    duesBreakdown: DuesBreakdown,
    onDismiss: () -> Unit,
    onApplyAmount: (Double) -> Unit
) {
    var selectedMonths by remember { mutableStateOf(1) }
    var includeTuition by remember { mutableStateOf(true) }
    var includeAnnualTuition by remember { mutableStateOf(false) } // For annual fee classes
    var includeTransport by remember { mutableStateOf(duesBreakdown.hasTransport) }
    var includePreviousDues by remember { mutableStateOf(false) }
    var includeAdmissionFee by remember { mutableStateOf(false) }
    var includeRegistrationFee by remember { mutableStateOf(false) }
    
    // Calculate totals based on class type
    val tuitionAmount = if (duesBreakdown.isMonthlyFeeClass) {
        // Monthly fee class - multiply by months
        if (includeTuition) duesBreakdown.monthlyTuitionRate * selectedMonths else 0.0
    } else {
        // Annual fee class - full amount if selected
        if (includeAnnualTuition) duesBreakdown.annualTuitionRate else 0.0
    }
    val transportAmount = if (includeTransport && duesBreakdown.hasTransport) 
        duesBreakdown.monthlyTransportRate * selectedMonths else 0.0
    val previousDuesAmount = if (includePreviousDues && duesBreakdown.previousYearDues > 0) 
        duesBreakdown.previousYearDues else 0.0
    val admissionFeeAmount = if (includeAdmissionFee && duesBreakdown.admissionFee > 0) 
        duesBreakdown.admissionFee else 0.0
    val registrationFeeAmount = if (includeRegistrationFee && duesBreakdown.registrationFee > 0) 
        duesBreakdown.registrationFee else 0.0
    
    val totalAmount = tuitionAmount + transportAmount + previousDuesAmount + 
        admissionFeeAmount + registrationFeeAmount
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = null,
                        tint = SoftPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Fee Calculator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Month Selector
                Text(
                    "SELECT MONTHS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Month Grid (1-12)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..12).forEach { month ->
                        val isSelected = selectedMonths == month
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedMonths = month },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) DeepTeal else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Text(
                                month.toString(),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                // Fee Selection
                Text(
                    "INCLUDE FEES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Tuition & Transport Section
                val showMonthlyTuition = duesBreakdown.isMonthlyFeeClass && duesBreakdown.monthlyTuitionRate > 0
                val showAnnualTuition = !duesBreakdown.isMonthlyFeeClass && duesBreakdown.annualTuitionRate > 0
                val showTransport = duesBreakdown.hasTransport
                
                if (showMonthlyTuition || showAnnualTuition || showTransport) {
                    // Monthly Fees Section (for monthly-fee classes or transport)
                    if (showMonthlyTuition || showTransport) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Monthly Fees (× $selectedMonths months)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(Modifier.height(8.dp))
                                
                                // Monthly Tuition Fee (only for monthly-fee classes)
                                if (showMonthlyTuition) {
                                    CalculatorFeeRow(
                                        label = "Tuition Fee",
                                        rate = duesBreakdown.monthlyTuitionRate,
                                        months = selectedMonths,
                                        isChecked = includeTuition,
                                        onCheckedChange = { includeTuition = it },
                                        color = DeepTeal
                                    )
                                }
                                
                                // Transport Fee - show if student has transport enrolled
                                if (showTransport) {
                                    if (showMonthlyTuition) {
                                        Spacer(Modifier.height(6.dp))
                                    }
                                    val transportLabel = if (duesBreakdown.transportRouteName.isNotBlank()) 
                                        "Transport (${duesBreakdown.transportRouteName})"
                                    else 
                                        "Transport Fee"
                                        
                                    CalculatorFeeRow(
                                        label = transportLabel,
                                        rate = duesBreakdown.monthlyTransportRate,
                                        months = selectedMonths,
                                        isChecked = includeTransport,
                                        onCheckedChange = { includeTransport = it },
                                        color = SoftPurple,
                                        enabled = duesBreakdown.monthlyTransportRate > 0
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))
                    }
                    
                    // Annual Tuition Fee Section (for 9th-12th classes)
                    if (showAnnualTuition) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Annual Tuition Fee",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(Modifier.height(8.dp))
                                
                                CalculatorOneTimeFeeRow(
                                    label = "Tuition Fee (Full Year)",
                                    amount = duesBreakdown.annualTuitionRate,
                                    isChecked = includeAnnualTuition,
                                    onCheckedChange = { includeAnnualTuition = it },
                                    color = DeepTeal
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))
                    }
                }
                
                // One-time/Other Fees Section
                val hasOtherFees = duesBreakdown.previousYearDues > 0 || 
                    duesBreakdown.admissionFee > 0 || 
                    duesBreakdown.registrationFee > 0
                
                if (hasOtherFees) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Other Fees (from Ledger)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            // Previous Year Dues
                            if (duesBreakdown.previousYearDues > 0) {
                                CalculatorOneTimeFeeRow(
                                    label = "Previous Year Dues",
                                    amount = duesBreakdown.previousYearDues,
                                    isChecked = includePreviousDues,
                                    onCheckedChange = { includePreviousDues = it },
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            
                            // Admission Fee
                            if (duesBreakdown.admissionFee > 0) {
                                if (duesBreakdown.previousYearDues > 0) {
                                    Spacer(Modifier.height(8.dp))
                                }
                                CalculatorOneTimeFeeRow(
                                    label = "Admission Fee",
                                    amount = duesBreakdown.admissionFee,
                                    isChecked = includeAdmissionFee,
                                    onCheckedChange = { includeAdmissionFee = it },
                                    color = Saffron
                                )
                            }
                            
                            // Registration Fee
                            if (duesBreakdown.registrationFee > 0) {
                                if (duesBreakdown.previousYearDues > 0 || duesBreakdown.admissionFee > 0) {
                                    Spacer(Modifier.height(8.dp))
                                }
                                CalculatorOneTimeFeeRow(
                                    label = "Registration Fee",
                                    amount = duesBreakdown.registrationFee,
                                    isChecked = includeRegistrationFee,
                                    onCheckedChange = { includeRegistrationFee = it },
                                    color = DeepTeal
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Total Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Saffron.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Saffron.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Breakdown
                        if (tuitionAmount > 0) {
                            CalculatorBreakdownRow(
                                label = "Tuition",
                                calculation = "₹${duesBreakdown.monthlyTuitionRate.toInt()} × $selectedMonths",
                                amount = tuitionAmount
                            )
                        }
                        if (transportAmount > 0) {
                            CalculatorBreakdownRow(
                                label = "Transport",
                                calculation = "₹${duesBreakdown.monthlyTransportRate.toInt()} × $selectedMonths",
                                amount = transportAmount
                            )
                        }
                        if (previousDuesAmount > 0) {
                            CalculatorBreakdownRow(
                                label = "Previous Dues",
                                calculation = "",
                                amount = previousDuesAmount
                            )
                        }
                        if (admissionFeeAmount > 0) {
                            CalculatorBreakdownRow(
                                label = "Admission Fee",
                                calculation = "",
                                amount = admissionFeeAmount
                            )
                        }
                        if (registrationFeeAmount > 0) {
                            CalculatorBreakdownRow(
                                label = "Registration Fee",
                                calculation = "",
                                amount = registrationFeeAmount
                            )
                        }
                        
                        if (totalAmount > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Saffron.copy(alpha = 0.3f)
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total Amount",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                totalAmount.toRupees(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Saffron
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApplyAmount(totalAmount) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                enabled = totalAmount > 0
            ) {
                Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Apply ${if (totalAmount > 0) totalAmount.toRupees() else ""}",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = null
    )
}

@Composable
private fun CalculatorFeeRow(
    label: String,
    rate: Double,
    months: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: Color,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onCheckedChange(!isChecked) },
        color = if (isChecked && enabled) color.copy(alpha = 0.08f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = null, // Handled by parent Surface click
                modifier = Modifier.size(24.dp),
                enabled = enabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = color,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                    disabledCheckedColor = color.copy(alpha = 0.5f),
                    disabledUncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isChecked && enabled) MaterialTheme.colorScheme.onSurface 
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (rate > 0) {
                    Text(
                        "₹${rate.toInt()}/month × $months = ₹${(rate * months).toInt()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isChecked && enabled) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                } else {
                    Text(
                        "Rate not configured",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
            // Show calculated amount on the right
            if (rate > 0 && isChecked) {
                Text(
                    "₹${(rate * months).toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun CalculatorOneTimeFeeRow(
    label: String,
    amount: Double,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!isChecked) },
        color = if (isChecked) color.copy(alpha = 0.08f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = null, // Handled by parent Surface click
                modifier = Modifier.size(24.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = color,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            )
            Text(
                amount.toRupees(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isChecked) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun CalculatorBreakdownRow(
    label: String,
    calculation: String,
    amount: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (calculation.isNotBlank()) {
                Text(
                    " ($calculation)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        Text(
            amount.toRupees(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}