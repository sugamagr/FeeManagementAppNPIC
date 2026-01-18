package com.navoditpublic.fees.presentation.screens.students.add_edit

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.components.SectionHeader
import com.navoditpublic.fees.presentation.theme.Saffron
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
fun AddEditStudentScreen(
    studentId: Long?,
    navController: NavController,
    viewModel: AddEditStudentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddEditStudentEvent.Success -> {
                    Toast.makeText(
                        context,
                        if (state.isEditMode) "Student updated successfully" else "Student added successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.popBackStack()
                }
                is AddEditStudentEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    // Transport enrollment dialog
    if (state.showAddTransportDialog) {
        TransportEnrollmentDialog(
            enrollment = state.editingEnrollment,
            routes = state.transportRoutes,
            onRouteChange = viewModel::updateEditingEnrollmentRoute,
            onStartDateChange = viewModel::updateEditingEnrollmentStartDate,
            onEndDateChange = viewModel::updateEditingEnrollmentEndDate,
            onSave = viewModel::saveTransportEnrollment,
            onDismiss = viewModel::dismissTransportDialog
        )
    }
    
    // Admission Date Picker
    var showAdmissionDatePicker by remember { mutableStateOf(false) }
    if (showAdmissionDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.admissionDate)
        DatePickerDialog(
            onDismissRequest = { showAdmissionDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateAdmissionDate(it) }
                    showAdmissionDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showAdmissionDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
    
    // Backdate Warning Dialog for Admission
    if (state.showBackdateWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBackdateWarning() },
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { 
                Text(
                    "Backdated Admission",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column {
                    Text(
                        "You are setting an admission date in the past.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "The fee entries will be dated at the session start (April 1) regardless of admission date.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissBackdateWarning() },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                ) {
                    Text("I Understand")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit Student" else "Add Student") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.save() },
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading && !state.isEditMode && state.srNumber.isEmpty()) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Basic Information
                item { SectionHeader(title = "Basic Information") }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.srNumber,
                            onValueChange = viewModel::updateSrNumber,
                            label = { Text("SR Number *") },
                            modifier = Modifier.weight(1f),
                            isError = state.srNumberError != null,
                            supportingText = state.srNumberError?.let { { Text(it) } },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                        )
                        OutlinedTextField(
                            value = state.accountNumber,
                            onValueChange = viewModel::updateAccountNumber,
                            label = { Text("A/C Number *") },
                            modifier = Modifier.weight(1f),
                            isError = state.accountNumberError != null,
                            supportingText = state.accountNumberError?.let { { Text(it) } },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                        )
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("Student Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.nameError != null,
                        supportingText = state.nameError?.let { { Text(it) } },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = state.fatherName,
                        onValueChange = viewModel::updateFatherName,
                        label = { Text("Father's Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.fatherNameError != null,
                        supportingText = state.fatherNameError?.let { { Text(it) } },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = state.motherName,
                        onValueChange = viewModel::updateMotherName,
                        label = { Text("Mother's Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                }
                
                // Class Information
                item { SectionHeader(title = "Class Information") }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Class Dropdown
                        var classExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = classExpanded,
                            onExpandedChange = { classExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = state.currentClass.ifBlank { "Select Class *" },
                                onValueChange = {},
                                readOnly = true,
                                isError = state.classError != null,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = classExpanded,
                                onDismissRequest = { classExpanded = false }
                            ) {
                                state.classes.forEach { className ->
                                    DropdownMenuItem(
                                        text = { Text(className) },
                                        onClick = {
                                            viewModel.updateClass(className)
                                            classExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Section Dropdown
                        var sectionExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = sectionExpanded,
                            onExpandedChange = { sectionExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = state.section,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = sectionExpanded,
                                onDismissRequest = { sectionExpanded = false }
                            ) {
                                state.sections.forEach { section ->
                                    DropdownMenuItem(
                                        text = { Text(section) },
                                        onClick = {
                                            viewModel.updateSection(section)
                                            sectionExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Admission Date
                item {
                    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showAdmissionDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = if (state.isBackdatedAdmission) 
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (state.admissionDateError != null)
                            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Admission Date",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = dateFormatter.format(Date(state.admissionDate)),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = if (state.isBackdatedAdmission) 
                                        MaterialTheme.colorScheme.tertiary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Select date",
                                tint = if (state.isBackdatedAdmission) 
                                    MaterialTheme.colorScheme.tertiary
                                else Saffron
                            )
                        }
                    }
                    // Date error message
                    if (state.admissionDateError != null) {
                        Text(
                            text = state.admissionDateError!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
                
                // Admission Fee Checkbox
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = state.admissionFeePaid,
                            onCheckedChange = viewModel::updateAdmissionFeePaid
                        )
                        Text(
                            text = "Admission Fee Already Paid",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                // Contact Information
                item { SectionHeader(title = "Contact Information") }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.phonePrimary,
                            onValueChange = viewModel::updatePhonePrimary,
                            label = { Text("Phone *") },
                            modifier = Modifier.weight(1f),
                            isError = state.phonePrimaryError != null,
                            supportingText = state.phonePrimaryError?.let { { Text(it) } },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = state.phoneSecondary,
                            onValueChange = viewModel::updatePhoneSecondary,
                            label = { Text("Alt. Phone") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }
                }
                
                // Address
                item { SectionHeader(title = "Address") }
                
                item {
                    OutlinedTextField(
                        value = state.addressLine1,
                        onValueChange = viewModel::updateAddressLine1,
                        label = { Text("Address Line 1") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = state.addressLine2,
                        onValueChange = viewModel::updateAddressLine2,
                        label = { Text("Village") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.district,
                            onValueChange = viewModel::updateDistrict,
                            label = { Text("District") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = state.pincode,
                            onValueChange = viewModel::updatePincode,
                            label = { Text("Pincode") },
                            modifier = Modifier.weight(1f),
                            isError = state.pincodeError != null,
                            supportingText = state.pincodeError?.let { { Text(it) } },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                
                // Opening Balance Section
                item {
                    OpeningBalanceSection(
                        openingBalance = state.openingBalance,
                        openingBalanceRemarks = state.openingBalanceRemarks,
                        openingBalanceError = state.openingBalanceError,
                        sessionStartDate = state.currentSession?.startDate,
                        onBalanceChange = viewModel::updateOpeningBalance,
                        onRemarksChange = viewModel::updateOpeningBalanceRemarks
                    )
                }
                
                // Transport Section
                item {
                    TransportSection(
                        hasTransport = state.hasTransport,
                        transportRouteId = state.transportRouteId,
                        transportRoutes = state.transportRoutes,
                        transportEnrollments = state.transportEnrollments,
                        onHasTransportChange = viewModel::updateHasTransport,
                        onRouteChange = viewModel::updateTransportRoute,
                        onAddEnrollment = viewModel::showAddTransportDialog,
                        onEditEnrollment = viewModel::editTransportEnrollment,
                        onDeleteEnrollment = viewModel::deleteTransportEnrollment
                    )
                }
                
                // Current Session Fees Section (for new students)
                if (!state.isEditMode && state.currentClass.isNotBlank()) {
                    item {
                        CurrentSessionFeesSection(
                            sessionName = state.currentSession?.sessionName ?: "Current Session",
                            breakdown = state.sessionFeeBreakdown,
                            feesReceivedMode = state.feesReceivedMode,
                            feesReceivedAmount = state.feesReceivedAmount,
                            monthsPaid = state.monthsPaid,
                            monthNames = viewModel.monthNames,
                            calculatedFeesReceived = viewModel.getCalculatedFeesReceived(),
                            onModeChange = viewModel::updateFeesReceivedMode,
                            onAmountChange = viewModel::updateFeesReceivedAmount,
                            onMonthToggle = viewModel::toggleMonthPaid
                        )
                    }
                }
                
                // Save Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.save() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (state.isEditMode) "Update Student" else "Add Student",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun OpeningBalanceSection(
    openingBalance: String,
    openingBalanceRemarks: String,
    openingBalanceError: String?,
    sessionStartDate: Long?,
    onBalanceChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(openingBalance.isNotBlank() || openingBalanceRemarks.isNotBlank()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Saffron.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Previous Dues / Opening Balance",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "For carrying forward previous dues",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = openingBalance,
                        onValueChange = onBalanceChange,
                        label = { Text("Opening Balance Amount") },
                        prefix = { Text("₹ ") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = openingBalanceError != null,
                        supportingText = openingBalanceError?.let { { Text(it) } },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    OutlinedTextField(
                        value = openingBalanceRemarks,
                        onValueChange = onRemarksChange,
                        label = { Text("Remarks (optional)") },
                        placeholder = { Text("e.g., Fees due from 2024-25 session") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Show session start date
                    if (sessionStartDate != null) {
                        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Date: ${dateFormat.format(Date(sessionStartDate))} (Session Start)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransportSection(
    hasTransport: Boolean,
    transportRouteId: Long?,
    transportRoutes: List<com.navoditpublic.fees.domain.model.TransportRoute>,
    transportEnrollments: List<TransportEnrollmentUiEntry>,
    onHasTransportChange: (Boolean) -> Unit,
    onRouteChange: (Long?) -> Unit,
    onAddEnrollment: () -> Unit,
    onEditEnrollment: (TransportEnrollmentUiEntry) -> Unit,
    onDeleteEnrollment: (TransportEnrollmentUiEntry) -> Unit
) {
    var isExpanded by remember { mutableStateOf(hasTransport || transportEnrollments.isNotEmpty()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Transport",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (hasTransport) "Active" else "Not enrolled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
            
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Current transport toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = hasTransport,
                            onCheckedChange = onHasTransportChange
                        )
                        Text(
                            text = "Currently Uses Transport",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    // Current route selection
                    if (hasTransport) {
                        var routeExpanded by remember { mutableStateOf(false) }
                        val selectedRoute = transportRoutes.find { it.id == transportRouteId }
                        
                        ExposedDropdownMenuBox(
                            expanded = routeExpanded,
                            onExpandedChange = { routeExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedRoute?.displayText ?: "Select Route",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Current Route") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = routeExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = routeExpanded,
                                onDismissRequest = { routeExpanded = false }
                            ) {
                                transportRoutes.forEach { route ->
                                    DropdownMenuItem(
                                        text = { Text(route.displayText) },
                                        onClick = {
                                            onRouteChange(route.id)
                                            routeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Transport History
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transport History",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedButton(
                            onClick = onAddEnrollment,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Period")
                        }
                    }
                    
                    if (transportEnrollments.isEmpty()) {
                        Text(
                            text = "No transport history recorded",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        transportEnrollments.forEach { enrollment ->
                            TransportEnrollmentCard(
                                enrollment = enrollment,
                                onEdit = { onEditEnrollment(enrollment) },
                                onDelete = { onDeleteEnrollment(enrollment) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransportEnrollmentCard(
    enrollment: TransportEnrollmentUiEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transport Period?") },
            text = { Text("This will remove this transport period from the history.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = enrollment.routeName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${dateFormat.format(Date(enrollment.startDate))} - ${enrollment.endDate?.let { dateFormat.format(Date(it)) } ?: "Present"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹${enrollment.monthlyFee.toLong()}/month",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CurrentSessionFeesSection(
    sessionName: String,
    breakdown: SessionFeeBreakdown,
    feesReceivedMode: String,
    feesReceivedAmount: String,
    monthsPaid: Set<Int>,
    monthNames: List<String>,
    calculatedFeesReceived: Double,
    onModeChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onMonthToggle: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Session Fees",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = sessionName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
            
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Fee Breakdown
                    Text(
                        text = "Expected Fees",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    FeeBreakdownRow(breakdown.monthlyOrAnnualFeeLabel, breakdown.totalMonthlyFee)
                    
                    if (breakdown.registrationFee > 0) {
                        FeeBreakdownRow("Registration Fee", breakdown.registrationFee)
                    }
                    
                    if (breakdown.transportFee > 0) {
                        FeeBreakdownRow("Transport Fee (${breakdown.transportMonths} months)", breakdown.transportFee)
                    }
                    
                    HorizontalDivider()
                    
                    FeeBreakdownRow("Total Expected", breakdown.totalExpected, isTotal = true)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Fees Already Received
                    Text(
                        text = "Fees Already Received This Session",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = feesReceivedMode == "custom",
                            onClick = { onModeChange("custom") }
                        )
                        Text("Enter Custom Amount")
                    }
                    
                    if (feesReceivedMode == "custom") {
                        OutlinedTextField(
                            value = feesReceivedAmount,
                            onValueChange = onAmountChange,
                            label = { Text("Amount Received") },
                            prefix = { Text("₹ ") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = feesReceivedMode == "months",
                            onClick = { onModeChange("months") }
                        )
                        Text("Select Months Paid")
                    }
                    
                    if (feesReceivedMode == "months") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            monthNames.forEachIndexed { index, month ->
                                FilterChip(
                                    selected = monthsPaid.contains(index),
                                    onClick = { onMonthToggle(index) },
                                    label = { Text(month.take(3)) }
                                )
                            }
                        }
                    }
                    
                    if (calculatedFeesReceived > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Amount Received:", fontWeight = FontWeight.Medium)
                                Text("₹${calculatedFeesReceived.toLong()}", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        val remainingDue = breakdown.totalExpected - calculatedFeesReceived
                        Surface(
                            color = if (remainingDue > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remaining Due:", fontWeight = FontWeight.Medium)
                                Text(
                                    "₹${remainingDue.toLong().coerceAtLeast(0)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingDue > 0) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.secondary
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
fun FeeBreakdownRow(label: String, amount: Double, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = "₹${amount.toLong()}",
            style = if (isTotal) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportEnrollmentDialog(
    enrollment: TransportEnrollmentUiEntry?,
    routes: List<com.navoditpublic.fees.domain.model.TransportRoute>,
    onRouteChange: (Long) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    if (enrollment == null) return
    
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var hasEndDate by remember { mutableStateOf(enrollment.endDate != null) }
    
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = enrollment.startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onStartDateChange(it) }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = enrollment.endDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onEndDateChange(it) }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (enrollment.isNew && enrollment.id == 0L) "Add Transport Period" else "Edit Transport Period",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Route selection
                var routeExpanded by remember { mutableStateOf(false) }
                val selectedRoute = routes.find { it.id == enrollment.routeId }
                
                ExposedDropdownMenuBox(
                    expanded = routeExpanded,
                    onExpandedChange = { routeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRoute?.displayText ?: "Select Route",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Route") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = routeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = routeExpanded,
                        onDismissRequest = { routeExpanded = false }
                    ) {
                        routes.forEach { route ->
                            DropdownMenuItem(
                                text = { Text(route.displayText) },
                                onClick = {
                                    onRouteChange(route.id)
                                    routeExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Start Date
                OutlinedTextField(
                    value = dateFormat.format(Date(enrollment.startDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start Date") },
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select date")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartDatePicker = true },
                    shape = RoundedCornerShape(12.dp)
                )
                
                // End Date toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = hasEndDate,
                        onCheckedChange = { 
                            hasEndDate = it
                            if (!it) onEndDateChange(null)
                        }
                    )
                    Text("Has End Date (transport stopped)")
                }
                
                if (hasEndDate) {
                    OutlinedTextField(
                        value = enrollment.endDate?.let { dateFormat.format(Date(it)) } ?: "Select date",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("End Date") },
                        trailingIcon = {
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select date")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEndDatePicker = true },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Monthly fee info (shows class-appropriate fee)
                if (selectedRoute != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Monthly Fee:", style = MaterialTheme.typography.bodyMedium)
                            Text("₹${enrollment.monthlyFee.toLong()}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSave) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
