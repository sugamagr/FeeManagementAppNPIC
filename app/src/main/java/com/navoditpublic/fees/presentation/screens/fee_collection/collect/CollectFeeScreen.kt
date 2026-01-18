package com.navoditpublic.fees.presentation.screens.fee_collection.collect

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.domain.model.StudentWithBalance
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.LocalDrawerState
import com.navoditpublic.fees.presentation.components.SearchBar
import com.navoditpublic.fees.presentation.theme.PaidChipBackground
import com.navoditpublic.fees.presentation.theme.PaidChipText
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.util.DateUtils
import com.navoditpublic.fees.util.NumberToWords
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.launch

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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val drawerState = LocalDrawerState.current
    
    // Determine if accessed from bottom nav (no preselected student) or from other screens
    val isFromBottomNav = preSelectedStudentId == null || preSelectedStudentId == -1L
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CollectFeeEvent.Success -> {
                    Toast.makeText(context, "Receipt saved!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                is CollectFeeEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Saffron, SaffronDark)))
                    .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (isFromBottomNav) {
                        // Hamburger Menu for bottom nav access
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                        }
                    } else {
                        // Back button when accessed from other screens
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                    Text("Collect Fee", style = MaterialTheme.typography.titleMedium, 
                         fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        bottomBar = {
            if (state.selectedStudent != null && state.total > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Paying", style = MaterialTheme.typography.labelSmall, 
                                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.total.toRupees(), style = MaterialTheme.typography.titleMedium,
                                     fontWeight = FontWeight.Bold, color = Saffron)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Remaining", style = MaterialTheme.typography.labelSmall,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.remainingDues.toRupees(), style = MaterialTheme.typography.titleMedium,
                                     fontWeight = FontWeight.Bold,
                                     color = if (state.remainingDues > 0) MaterialTheme.colorScheme.error else PaidChipText)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.saveReceipt() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = state.receiptNumber.isNotBlank() && !state.showDuplicateWarning && !state.isSaving && state.dateError == null,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                        ) {
                            if (state.isSaving) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save Receipt", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Receipt Number & Date
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = state.receiptNumber,
                                    onValueChange = viewModel::updateReceiptNumber,
                                    label = { Text("Receipt No.*", style = MaterialTheme.typography.labelSmall) },
                                    placeholder = { Text("Enter") },
                                    modifier = Modifier.weight(1f),
                                    isError = state.showDuplicateWarning,
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Saffron,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                )
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { showDatePicker = true },
                                    color = if (state.isBackdatedReceipt) 
                                        MaterialTheme.colorScheme.tertiaryContainer 
                                    else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = if (state.dateError != null) 
                                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error) 
                                    else null
                                ) {
                                    Row(Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (state.isBackdatedReceipt) Icons.Default.History else Icons.Default.CalendarToday, 
                                            null, 
                                            Modifier.size(16.dp), 
                                            tint = if (state.isBackdatedReceipt) 
                                                MaterialTheme.colorScheme.tertiary 
                                            else Saffron
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            DateUtils.formatDate(state.receiptDate), 
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (state.isBackdatedReceipt) 
                                                MaterialTheme.colorScheme.tertiary 
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            // Date error message
                            if (state.dateError != null) {
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, null, Modifier.size(14.dp), 
                                         tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(4.dp))
                                    Text(state.dateError!!, style = MaterialTheme.typography.labelSmall,
                                         color = MaterialTheme.colorScheme.error)
                                }
                            }
                            // Backdated indicator
                            if (state.isBackdatedReceipt && state.dateError == null) {
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.History, null, Modifier.size(14.dp), 
                                         tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Backdated receipt - balances will be recalculated", 
                                         style = MaterialTheme.typography.labelSmall,
                                         color = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        }
                    }
                }
                
                // Error message
                if (state.showDuplicateWarning) {
                    item {
                        Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, Modifier.size(14.dp), 
                                 tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(4.dp))
                            Text("Receipt number exists!", style = MaterialTheme.typography.labelSmall,
                                 color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                
                // Progress Steps
                item {
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        val step = when {
                            state.receiptNumber.isBlank() -> 0
                            state.selectedStudent == null -> 1
                            state.total <= 0 -> 2
                            else -> 3
                        }
                        StepIndicator(1, "Student", step >= 1, step > 1)
                        Box(Modifier.width(24.dp).height(1.dp)
                            .background(if (step > 1) Saffron else MaterialTheme.colorScheme.outlineVariant))
                        StepIndicator(2, "Amount", step >= 2, step > 2)
                        Box(Modifier.width(24.dp).height(1.dp)
                            .background(if (step > 2) Saffron else MaterialTheme.colorScheme.outlineVariant))
                        StepIndicator(3, "Pay", step >= 3, false)
                    }
                }
                
                // Student Selection
                item {
                    SectionCard(title = "Student", stepNum = 1, isComplete = state.selectedStudent != null) {
                        if (state.selectedStudent == null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    .clickable { showStudentSheet = true },
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Search, null, Modifier.size(18.dp),
                                         tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Tap to select student", 
                                         style = MaterialTheme.typography.bodyMedium,
                                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(36.dp).clip(CircleShape)
                                    .background(Saffron.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center) {
                                    Text(state.selectedStudent!!.student.name.take(2).uppercase(),
                                         style = MaterialTheme.typography.labelMedium,
                                         fontWeight = FontWeight.Bold, color = Saffron)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(state.selectedStudent!!.student.name,
                                         style = MaterialTheme.typography.bodyMedium,
                                         fontWeight = FontWeight.SemiBold, maxLines = 1,
                                         overflow = TextOverflow.Ellipsis)
                                    Text("Class ${state.selectedStudent!!.student.classSection}",
                                         style = MaterialTheme.typography.labelSmall,
                                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { viewModel.clearStudent(); showStudentSheet = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp)) {
                                    Text("Change", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
                
                // Amount Entry
                if (state.selectedStudent != null) {
                    item {
                        SectionCard(title = "Amount", stepNum = 2, isComplete = state.total > 0) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Current dues
                                Row(Modifier.fillMaxWidth().background(
                                    if (state.currentDues > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    else PaidChipBackground, RoundedCornerShape(8.dp)).padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccountBalance, null, Modifier.size(16.dp),
                                             tint = if (state.currentDues > 0) MaterialTheme.colorScheme.error else PaidChipText)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Current Dues", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text(state.currentDues.toRupees(), style = MaterialTheme.typography.titleSmall,
                                         fontWeight = FontWeight.Bold,
                                         color = if (state.currentDues > 0) MaterialTheme.colorScheme.error else PaidChipText)
                                }
                                
                                // Amount field
                                OutlinedTextField(
                                    value = state.amountReceived,
                                    onValueChange = viewModel::updateAmountReceived,
                                    label = { Text("Amount") },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Saffron) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Saffron)
                                )
                                
                                // Quick amounts
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(500, 1000, 2000, 5000).forEach { amt ->
                                        Surface(
                                            modifier = Modifier.clip(RoundedCornerShape(16.dp))
                                                .clickable { viewModel.updateAmountReceived(amt.toString()) },
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text("₹$amt", Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                 style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                    if (state.currentDues > 0) {
                                        Surface(
                                            modifier = Modifier.clip(RoundedCornerShape(16.dp))
                                                .clickable { viewModel.updateAmountReceived(state.currentDues.toInt().toString()) },
                                            color = Saffron.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text("Full Due", Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                 style = MaterialTheme.typography.labelMedium,
                                                 fontWeight = FontWeight.SemiBold, color = Saffron)
                                        }
                                    }
                                }
                                
                                // Full year offer
                                if (state.fullYearDiscountAmount > 0) {
                                    val bgColor by animateColorAsState(
                                        if (state.isFullYearPayment) Saffron.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), label = "")
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.toggleFullYearPayment(!state.isFullYearPayment) },
                                        color = bgColor,
                                        shape = RoundedCornerShape(8.dp),
                                        border = if (state.isFullYearPayment) 
                                            androidx.compose.foundation.BorderStroke(1.dp, Saffron) else null
                                    ) {
                                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalOffer, null, Modifier.size(18.dp),
                                                 tint = if (state.isFullYearPayment) Saffron 
                                                        else MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(Modifier.width(8.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text("Full Year Payment", style = MaterialTheme.typography.bodyMedium,
                                                     fontWeight = FontWeight.Medium)
                                                Text("1 month FREE (${state.fullYearDiscountAmount.toRupees()} off)",
                                                     style = MaterialTheme.typography.labelSmall,
                                                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Box(Modifier.size(20.dp).clip(CircleShape)
                                                .background(if (state.isFullYearPayment) Saffron 
                                                           else Color.Transparent)
                                                .border(1.dp, if (state.isFullYearPayment) Saffron 
                                                              else MaterialTheme.colorScheme.outline, CircleShape),
                                                contentAlignment = Alignment.Center) {
                                                if (state.isFullYearPayment) {
                                                    Icon(Icons.Default.Check, null, Modifier.size(12.dp), tint = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Payment Mode
                if (state.selectedStudent != null) {
                    item {
                        SectionCard(title = "Payment", stepNum = 3, isComplete = false) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PaymentModeChip(
                                        label = "Cash",
                                        icon = Icons.Default.Money,
                                        isSelected = state.paymentMode == PaymentMode.CASH,
                                        onClick = { viewModel.updatePaymentMode(PaymentMode.CASH) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    PaymentModeChip(
                                        label = "Online",
                                        icon = Icons.Default.CreditCard,
                                        isSelected = state.paymentMode == PaymentMode.ONLINE,
                                        onClick = { viewModel.updatePaymentMode(PaymentMode.ONLINE) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                OutlinedTextField(
                                    value = state.details,
                                    onValueChange = viewModel::updateDetails,
                                    label = { Text("Remarks") },
                                    placeholder = { Text("Optional notes...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }
                
                // Summary
                if (state.total > 0) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Summary", style = MaterialTheme.typography.labelMedium,
                                     fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Amount", style = MaterialTheme.typography.bodySmall)
                                    Text(state.subtotal.toRupees(), style = MaterialTheme.typography.bodySmall)
                                }
                                if (state.discount > 0) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Bonus", style = MaterialTheme.typography.bodySmall, color = PaidChipText)
                                        Text("+${state.discount.toRupees()}", style = MaterialTheme.typography.bodySmall, 
                                             color = PaidChipText)
                                    }
                                }
                                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Dues Cleared", style = MaterialTheme.typography.bodyMedium,
                                         fontWeight = FontWeight.SemiBold)
                                    Text((state.subtotal + state.discount).toRupees(),
                                         style = MaterialTheme.typography.bodyMedium,
                                         fontWeight = FontWeight.Bold, color = Saffron)
                                }
                                Text(NumberToWords.convert(state.total),
                                     style = MaterialTheme.typography.labelSmall,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                                     modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
                
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun StepIndicator(num: Int, label: String, isActive: Boolean, isComplete: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(24.dp).clip(CircleShape)
                .background(when { isComplete -> Saffron; isActive -> Saffron.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant })
                .then(if (isActive && !isComplete) Modifier.border(1.5.dp, Saffron, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (isComplete) Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = Color.White)
            else Text("$num", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                      color = if (isActive) Saffron else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(label, style = MaterialTheme.typography.labelSmall,
             color = if (isActive || isComplete) Saffron else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionCard(title: String, stepNum: Int, isComplete: Boolean, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Box(Modifier.size(22.dp).clip(CircleShape)
                    .background(if (isComplete) Saffron else Saffron.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center) {
                    if (isComplete) Icon(Icons.Default.Check, null, Modifier.size(12.dp), tint = Color.White)
                    else Text("$stepNum", style = MaterialTheme.typography.labelSmall, 
                              fontWeight = FontWeight.Bold, color = Saffron)
                }
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            content()
        }
    }
}

@Composable
private fun PaymentModeChip(
    label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Saffron.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Saffron) else null
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, Modifier.size(18.dp), 
                 tint = if (isSelected) Saffron else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium,
                 fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                 color = if (isSelected) Saffron else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StudentSelectionSheet(
    searchQuery: String, onSearchChange: (String) -> Unit,
    searchResults: List<StudentWithBalance>, allStudents: List<StudentWithBalance>,
    onStudentSelected: (StudentWithBalance) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Text("Select Student", style = MaterialTheme.typography.titleMedium,
             fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        SearchBar(query = searchQuery, onQueryChange = onSearchChange, 
                  placeholder = "Search name, SR number...")
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (searchQuery.length >= 2 && searchResults.isNotEmpty()) {
                item { Text("Results", style = MaterialTheme.typography.labelSmall, 
                           color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                items(searchResults) { StudentItem(it, onStudentSelected) }
                item { Spacer(Modifier.height(8.dp)) }
            }
            item { Text("All Students", style = MaterialTheme.typography.labelSmall,
                       color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(allStudents) { StudentItem(it, onStudentSelected) }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StudentItem(student: StudentWithBalance, onClick: (StudentWithBalance) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onClick(student) },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(Saffron.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center) {
                Text(student.student.name.take(2).uppercase(), style = MaterialTheme.typography.labelSmall,
                     fontWeight = FontWeight.Bold, color = Saffron)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(student.student.name, style = MaterialTheme.typography.bodyMedium,
                     maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${student.student.classSection} • SR:${student.student.srNumber}",
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                color = if (student.currentBalance > 0) MaterialTheme.colorScheme.errorContainer else PaidChipBackground,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    if (student.currentBalance > 0) student.currentBalance.toRupees() else "Paid",
                    Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (student.currentBalance > 0) MaterialTheme.colorScheme.error else PaidChipText
                )
            }
        }
    }
}
