package com.navoditpublic.fees.presentation.screens.students.add_edit

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.theme.Saffron
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Section color themes
private val SectionBasicInfo = Color(0xFFFF9800) // Orange/Saffron
private val SectionClassInfo = Color(0xFF009688) // Teal
private val SectionContact = Color(0xFF9C27B0) // Purple
private val SectionAddress = Color(0xFF4CAF50) // Green
private val SectionOpeningBalance = Color(0xFFFFC107) // Amber
private val SectionTransport = Color(0xFF2196F3) // Blue
private val SectionSessionFees = Color(0xFF3F51B5) // Indigo

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
    
    // Calculate form progress
    val formProgress by remember(state) {
        derivedStateOf {
            var filled = 0
            var total = 6 // Required fields
            if (state.srNumber.isNotBlank()) filled++
            if (state.accountNumber.isNotBlank()) filled++
            if (state.name.isNotBlank()) filled++
            if (state.fatherName.isNotBlank()) filled++
            if (state.phonePrimary.length == 10) filled++
            if (state.currentClass.isNotBlank()) filled++
            filled.toFloat() / total
        }
    }
    
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
    
    // Backdate Warning Dialog
    if (state.showBackdateWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBackdateWarning() },
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Backdated Admission", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("You are setting an admission date in the past.")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Fee entries will be dated at session start (April 1).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissBackdateWarning() },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                ) { Text("I Understand") }
            }
        )
    }
    
    // Resume Draft Dialog
    if (state.showDraftResumeDialog) {
        val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
        AlertDialog(
            onDismissRequest = { viewModel.discardDraft() },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Saffron.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = Saffron,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = { Text("Resume Previous Entry?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("You have an unsaved student entry:")
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = Saffron.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                state.draftSummary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Saffron
                            )
                            if (state.draftLastModified > 0) {
                                Text(
                                    "Last edited: ${dateFormat.format(Date(state.draftLastModified))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.discardDraft() }) { Text("Discard") }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resumeDraft() },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                ) { Text("Resume") }
            }
        )
    }
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            if (state.isEditMode) "Edit Student" else "Add Student",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Draft saved indicator in app bar
                        AnimatedVisibility(
                            visible = state.isDraftSaved,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Saved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.save() },
                            enabled = !state.isLoading
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Saffron,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
                
                // Progress indicator
                val animatedProgress by animateFloatAsState(
                    targetValue = formProgress,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "progress"
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        formProgress < 0.5f -> Color(0xFFE57373) // Red
                        formProgress < 1f -> Color(0xFFFFB74D) // Orange
                        else -> Color(0xFF81C784) // Green
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    ) { paddingValues ->
        // Focus management for keyboard navigation
        val focusManager = LocalFocusManager.current
        val srNumberFocus = remember { FocusRequester() }
        val accountNumberFocus = remember { FocusRequester() }
        val studentNameFocus = remember { FocusRequester() }
        val fatherNameFocus = remember { FocusRequester() }
        val motherNameFocus = remember { FocusRequester() }
        val phonePrimaryFocus = remember { FocusRequester() }
        val phoneSecondaryFocus = remember { FocusRequester() }
        val addressLine1Focus = remember { FocusRequester() }
        val villageFocus = remember { FocusRequester() }
        val pincodeFocus = remember { FocusRequester() }
        val districtFocus = remember { FocusRequester() }
        val stateFocus = remember { FocusRequester() }
        val openingBalanceFocus = remember { FocusRequester() }
        val currentYearPaidFocus = remember { FocusRequester() }
        
        // Section expansion state for auto-collapse on keyboard navigation
        var basicInfoExpanded by remember { mutableStateOf(true) }
        var classInfoExpanded by remember { mutableStateOf(true) }
        var contactExpanded by remember { mutableStateOf(true) }
        var addressExpanded by remember { mutableStateOf(true) }
        
        // Helper to collapse other sections when navigating to a new one
        fun collapseOtherSections(keepExpanded: String) {
            if (keepExpanded != "basicInfo") basicInfoExpanded = false
            if (keepExpanded != "classInfo") classInfoExpanded = false
            if (keepExpanded != "contact") contactExpanded = false
            if (keepExpanded != "address") addressExpanded = false
        }
        
        if (state.isLoading && !state.isEditMode && state.srNumber.isEmpty()) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Migration Mode Banner (only for new students)
                if (!state.isEditMode) {
                    item {
                        MigrationModeBanner(
                            isMigrationMode = state.isMigrationMode,
                            onToggle = viewModel::updateMigrationMode
                        )
                    }
                }
                
                // Basic Information Section
                item {
                    FormSectionCard(
                        title = "Basic Information",
                        subtitle = "Student identity details",
                        icon = Icons.Default.Person,
                        accentColor = SectionBasicInfo,
                        isComplete = state.srNumber.isNotBlank() && state.accountNumber.isNotBlank() && 
                                    state.name.isNotBlank() && state.fatherName.isNotBlank(),
                        isExpanded = basicInfoExpanded,
                        onExpandChange = { basicInfoExpanded = it }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StyledTextField(
                                    value = state.srNumber,
                                    onValueChange = viewModel::updateSrNumber,
                                    label = "SR Number *",
                                    modifier = Modifier.weight(1f),
                                    isError = state.srNumberError != null,
                                    errorMessage = state.srNumberError,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Characters,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { accountNumberFocus.requestFocus() }
                                    ),
                                    focusRequester = srNumberFocus
                                )
                                StyledTextField(
                                    value = state.accountNumber,
                                    onValueChange = viewModel::updateAccountNumber,
                                    label = "A/C Number *",
                                    modifier = Modifier.weight(1f),
                                    isError = state.accountNumberError != null,
                                    errorMessage = state.accountNumberError,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Characters,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { studentNameFocus.requestFocus() }
                                    ),
                                    focusRequester = accountNumberFocus
                                )
                            }
                            
                            StyledTextField(
                                value = state.name,
                                onValueChange = viewModel::updateName,
                                label = "Student Name *",
                                modifier = Modifier.fillMaxWidth(),
                                isError = state.nameError != null,
                                errorMessage = state.nameError,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { fatherNameFocus.requestFocus() }
                                ),
                                focusRequester = studentNameFocus
                            )
                            
                            StyledTextField(
                                value = state.fatherName,
                                onValueChange = viewModel::updateFatherName,
                                label = "Father's Name *",
                                modifier = Modifier.fillMaxWidth(),
                                isError = state.fatherNameError != null,
                                errorMessage = state.fatherNameError,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { motherNameFocus.requestFocus() }
                                ),
                                focusRequester = fatherNameFocus
                            )
                            
                            StyledTextField(
                                value = state.motherName,
                                onValueChange = viewModel::updateMotherName,
                                label = "Mother's Name",
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { 
                                        collapseOtherSections("contact")
                                        contactExpanded = true
                                        phonePrimaryFocus.requestFocus() 
                                    }
                                ),
                                focusRequester = motherNameFocus
                            )
                        }
                    }
                }
                
                // Class Information Section
                item {
                    FormSectionCard(
                        title = "Class Information",
                        subtitle = "Academic details",
                        icon = Icons.Default.School,
                        accentColor = SectionClassInfo,
                        isComplete = state.currentClass.isNotBlank(),
                        isExpanded = classInfoExpanded,
                        onExpandChange = { classInfoExpanded = it }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Class selector - Chip-based grid
                            Column {
                                Text(
                                    "Select Class *",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (state.classError != null) MaterialTheme.colorScheme.error 
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    state.classes.forEach { className ->
                                        val isSelected = state.currentClass == className
                                        Surface(
                                            onClick = { viewModel.updateClass(className) },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) SectionClassInfo 
                                                   else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            border = if (isSelected) null 
                                                    else androidx.compose.foundation.BorderStroke(
                                                        1.dp, 
                                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                    )
                                        ) {
                                            Text(
                                                text = className,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isSelected) Color.White 
                                                       else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                                if (state.classError != null) {
                                    Text(
                                        text = state.classError!!,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            
                            // Section selector - Compact chips
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Section:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                state.sections.forEach { section ->
                                    val isSelected = state.section == section
                                    Surface(
                                        onClick = { viewModel.updateSection(section) },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) SectionClassInfo.copy(alpha = 0.15f)
                                               else Color.Transparent,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) SectionClassInfo else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier.padding(end = 6.dp)
                                    ) {
                                        Text(
                                            text = section,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) SectionClassInfo 
                                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                            
                            // Admission Date - More compact
                            val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showAdmissionDatePicker = true },
                                shape = RoundedCornerShape(10.dp),
                                color = if (state.isBackdatedAdmission)
                                    SectionClassInfo.copy(alpha = 0.08f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (state.admissionDateError != null)
                                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                                else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Admission Date",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = dateFormatter.format(Date(state.admissionDate)),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = if (state.isBackdatedAdmission) SectionClassInfo
                                                    else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = "Select date",
                                        tint = SectionClassInfo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            if (state.admissionDateError != null) {
                                Text(
                                    text = state.admissionDateError!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                            
                            // New Admission Toggle (replaces checkbox)
                            NewAdmissionToggle(
                                isNewAdmission = state.isNewAdmission,
                                onToggle = viewModel::updateIsNewAdmission,
                                isMigrationMode = state.isMigrationMode,
                                isEditMode = state.isEditMode
                            )
                        }
                    }
                }
                
                // Contact Information Section
                item {
                    FormSectionCard(
                        title = "Contact Information",
                        subtitle = "Phone numbers",
                        icon = Icons.Default.Phone,
                        accentColor = SectionContact,
                        isComplete = state.phonePrimary.length == 10,
                        isExpanded = contactExpanded,
                        onExpandChange = { contactExpanded = it }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StyledTextField(
                                value = state.phonePrimary,
                                onValueChange = viewModel::updatePhonePrimary,
                                label = "Phone *",
                                modifier = Modifier.weight(1f),
                                isError = state.phonePrimaryError != null,
                                errorMessage = state.phonePrimaryError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { phoneSecondaryFocus.requestFocus() }
                                ),
                                focusRequester = phonePrimaryFocus
                            )
                            StyledTextField(
                                value = state.phoneSecondary,
                                onValueChange = viewModel::updatePhoneSecondary,
                                label = "Alt. Phone",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { 
                                        collapseOtherSections("address")
                                        addressExpanded = true
                                        addressLine1Focus.requestFocus() 
                                    }
                                ),
                                focusRequester = phoneSecondaryFocus
                            )
                        }
                    }
                }
                
                // Address Section
                item {
                    FormSectionCard(
                        title = "Address",
                        subtitle = "Residential details",
                        icon = Icons.Default.LocationOn,
                        accentColor = SectionAddress,
                        isComplete = state.addressLine1.isNotBlank(),
                        isExpanded = addressExpanded,
                        onExpandChange = { addressExpanded = it }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StyledTextField(
                                value = state.addressLine1,
                                onValueChange = viewModel::updateAddressLine1,
                                label = "Address Line 1",
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { villageFocus.requestFocus() }
                                ),
                                focusRequester = addressLine1Focus
                            )
                            
                            StyledTextField(
                                value = state.addressLine2,
                                onValueChange = viewModel::updateAddressLine2,
                                label = "Village",
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { districtFocus.requestFocus() }
                                ),
                                focusRequester = villageFocus
                            )
                            
                            // District with quick-fill
                            Column {
                                StyledTextField(
                                    value = state.district,
                                    onValueChange = viewModel::updateDistrict,
                                    label = "District",
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { stateFocus.requestFocus() }
                                    ),
                                    focusRequester = districtFocus
                                )
                                QuickFillChip(
                                    value = "Shahjahanpur",
                                    currentValue = state.district,
                                    onClick = { viewModel.updateDistrict("Shahjahanpur") },
                                    accentColor = SectionAddress
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // State with quick-fill
                                Column(modifier = Modifier.weight(1f)) {
                                    StyledTextField(
                                        value = state.state,
                                        onValueChange = viewModel::updateState,
                                        label = "State",
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(
                                            capitalization = KeyboardCapitalization.Words,
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { pincodeFocus.requestFocus() }
                                        ),
                                        focusRequester = stateFocus
                                    )
                                    QuickFillChip(
                                        value = "Uttar Pradesh",
                                        currentValue = state.state,
                                        onClick = { viewModel.updateState("Uttar Pradesh") },
                                        accentColor = SectionAddress
                                    )
                                }
                                
                                // Pincode with quick-fill
                                Column(modifier = Modifier.weight(1f)) {
                                    StyledTextField(
                                        value = state.pincode,
                                        onValueChange = viewModel::updatePincode,
                                        label = "Pincode",
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = state.pincodeError != null,
                                        errorMessage = state.pincodeError,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = if (state.isMigrationMode) ImeAction.Next else ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { 
                                                collapseOtherSections("none") // Collapse all sections
                                                openingBalanceFocus.requestFocus() 
                                            },
                                            onDone = { 
                                                collapseOtherSections("none")
                                                focusManager.clearFocus() 
                                            }
                                        ),
                                        focusRequester = pincodeFocus
                                    )
                                    QuickFillChip(
                                        value = "242305",
                                        currentValue = state.pincode,
                                        onClick = { viewModel.updatePincode("242305") },
                                        accentColor = SectionAddress
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Opening Balance Section (for data migration)
                item {
                    OpeningBalanceSectionCard(
                        openingBalance = state.openingBalance,
                        openingBalanceRemarks = state.openingBalanceRemarks,
                        currentYearPaidAmount = state.currentYearPaidAmount,
                        openingBalanceError = state.openingBalanceError,
                        sessionStartDate = state.currentSession?.startDate,
                        isMigrationMode = state.isMigrationMode,
                        onBalanceChange = viewModel::updateOpeningBalance,
                        onRemarksChange = viewModel::updateOpeningBalanceRemarks,
                        onCurrentYearPaidChange = viewModel::updateCurrentYearPaidAmount,
                        openingBalanceFocus = openingBalanceFocus,
                        currentYearPaidFocus = currentYearPaidFocus,
                        focusManager = focusManager
                    )
                }
                
                // Transport Section
                item {
                    TransportSectionCard(
                        hasTransport = state.hasTransport,
                        transportRouteId = state.transportRouteId,
                        transportRoutes = state.transportRoutes,
                        transportEnrollments = state.transportEnrollments,
                        isMigrationMode = state.isMigrationMode,
                        currentClass = state.currentClass,
                        onHasTransportChange = viewModel::updateHasTransport,
                        onRouteChange = viewModel::updateTransportRoute,
                        onAddEnrollment = viewModel::showAddTransportDialog,
                        onEditEnrollment = viewModel::editTransportEnrollment,
                        onDeleteEnrollment = viewModel::deleteTransportEnrollment
                    )
                }
                
                // Current Session Fees Summary (for new students with class selected)
                if (!state.isEditMode && state.currentClass.isNotBlank()) {
                    item {
                        CurrentSessionFeesSectionCard(
                            sessionName = state.currentSession?.sessionName ?: "Current Session",
                            breakdown = state.sessionFeeBreakdown,
                            isMigrationMode = state.isMigrationMode,
                            currentYearPaidAmount = state.currentYearPaidAmount
                        )
                    }
                }
                
                // Save Button
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.save() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                    ) {
                        Text(
                            text = if (state.isEditMode) "Update Student" else "Add Student",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// ==================== STYLED COMPONENTS ====================

@Composable
fun MigrationModeBanner(
    isMigrationMode: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isMigrationMode) Saffron.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isMigrationMode) 
            androidx.compose.foundation.BorderStroke(1.5.dp, Saffron.copy(alpha = 0.4f)) 
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isMigrationMode) Saffron.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = if (isMigrationMode) Saffron else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Data Migration Mode",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isMigrationMode) Saffron else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (isMigrationMode) "Entering existing student data"
                        else "Adding new admission",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = isMigrationMode,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Saffron,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun NewAdmissionToggle(
    isNewAdmission: Boolean,
    onToggle: (Boolean) -> Unit,
    isMigrationMode: Boolean,
    isEditMode: Boolean = false
) {
    // In edit mode, the toggle is disabled because fees may already be in the ledger
    val isEnabled = !isEditMode
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isNewAdmission) SectionClassInfo.copy(alpha = if (isEnabled) 0.08f else 0.04f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isEnabled) 0.25f else 0.15f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "New Admission This Year?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface 
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        if (isEditMode) "Cannot change - fees already applied"
                        else if (isNewAdmission) "Admission fee will be applied"
                        else "Already paid / continuing student",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isEditMode) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        onClick = { if (isEnabled) onToggle(true) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isNewAdmission) SectionClassInfo.copy(alpha = if (isEnabled) 1f else 0.5f) else Color.Transparent,
                        border = if (!isNewAdmission) androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isEnabled) 0.4f else 0.2f)
                        ) else null
                    ) {
                        Text(
                            "Yes",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isNewAdmission) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isNewAdmission) Color.White.copy(alpha = if (isEnabled) 1f else 0.7f) 
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isEnabled) 1f else 0.5f),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Surface(
                        onClick = { if (isEnabled) onToggle(false) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (!isNewAdmission) MaterialTheme.colorScheme.outline.copy(alpha = if (isEnabled) 1f else 0.5f) else Color.Transparent,
                        border = if (isNewAdmission) androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isEnabled) 0.4f else 0.2f)
                        ) else null
                    ) {
                        Text(
                            "No",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (!isNewAdmission) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (!isNewAdmission) Color.White.copy(alpha = if (isEnabled) 1f else 0.7f) 
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isEnabled) 1f else 0.5f),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            
            // Show hint when migration mode affects this - with icon instead of emoji
            AnimatedVisibility(visible = isMigrationMode && !isEditMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Icon(
                        Icons.Default.TipsAndUpdates,
                        contentDescription = null,
                        tint = Saffron,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Migration mode auto-selected 'No' (existing student)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Saffron
                    )
                }
            }
        }
    }
}

@Composable
fun FormSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isComplete: Boolean = false,
    isExpanded: Boolean = true,
    onExpandChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    // Use internal state if no external control provided
    var internalExpanded by remember { mutableStateOf(true) }
    val expanded = if (onExpandChange != null) isExpanded else internalExpanded
    val setExpanded: (Boolean) -> Unit = { value ->
        if (onExpandChange != null) onExpandChange(value) else internalExpanded = value
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header with gradient - more compact
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.12f),
                                accentColor.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .clickable { setExpanded(!expanded) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (isComplete) {
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Complete",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            // Content - more compact padding
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    prefix: @Composable (() -> Unit)? = null,
    focusRequester: FocusRequester? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = MaterialTheme.typography.bodySmall) },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
            isError = isError,
            singleLine = singleLine,
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            prefix = prefix,
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Saffron,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                focusedLabelColor = Saffron,
                cursorColor = Saffron,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp)
            )
        }
    }
}

@Composable
fun QuickFillChip(
    value: String,
    currentValue: String,
    onClick: () -> Unit,
    accentColor: Color
) {
    AnimatedVisibility(
        visible = currentValue.isBlank() || currentValue != value,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.TipsAndUpdates,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun OpeningBalanceSectionCard(
    openingBalance: String,
    openingBalanceRemarks: String,
    currentYearPaidAmount: String,
    openingBalanceError: String?,
    sessionStartDate: Long?,
    isMigrationMode: Boolean,
    onBalanceChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit,
    onCurrentYearPaidChange: (String) -> Unit,
    openingBalanceFocus: FocusRequester? = null,
    currentYearPaidFocus: FocusRequester? = null,
    focusManager: androidx.compose.ui.focus.FocusManager? = null
) {
    var isExpanded by remember { 
        mutableStateOf(isMigrationMode || openingBalance.isNotBlank() || currentYearPaidAmount.isNotBlank()) 
    }
    
    // Auto-expand when migration mode is enabled
    LaunchedEffect(isMigrationMode) {
        if (isMigrationMode) isExpanded = true
    }
    
    // Dynamic title and subtitle based on migration mode
    val sectionTitle = if (isMigrationMode) "Balance & Migration" else "Previous Dues"
    val sectionSubtitle = if (isMigrationMode) 
        "Carry forward dues & record payments already received"
    else 
        "Only if student has unpaid fees from before this session"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header - more compact
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                SectionOpeningBalance.copy(alpha = 0.12f),
                                SectionOpeningBalance.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SectionOpeningBalance.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = SectionOpeningBalance,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                sectionTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                sectionSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = SectionOpeningBalance,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Explanation message based on mode - more compact
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = SectionOpeningBalance,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isMigrationMode) {
                                    "Migrating existing data:\n" +
                                    "• Previous Dues: Unpaid fees from prior sessions\n" +
                                    "• Current Payments: Amount already collected this session\n\n" +
                                    "Note: Current session fees (Tuition + Registration + Transport) are auto-applied."
                                } else {
                                    "For rare cases where a new student has outstanding dues " +
                                    "(e.g., sibling transfer, previous admission left & returned). Leave empty for most admissions.\n\n" +
                                    "Note: Current session fees (Tuition + Registration + Transport) are auto-applied."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    
                    // Opening Balance (Previous Dues)
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (isMigrationMode) "Previous Dues" else "Outstanding Dues",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            StyledTextField(
                                value = openingBalance,
                                onValueChange = onBalanceChange,
                                label = "Amount Due",
                                prefix = { Text("₹ ") },
                                isError = openingBalanceError != null,
                                errorMessage = openingBalanceError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { currentYearPaidFocus?.requestFocus() ?: focusManager?.clearFocus() }
                                ),
                                focusRequester = openingBalanceFocus
                            )
                            Spacer(Modifier.height(6.dp))
                            StyledTextField(
                                value = openingBalanceRemarks,
                                onValueChange = onRemarksChange,
                                label = if (isMigrationMode) "Remarks (e.g., 2024-25 dues)" 
                                       else "Reason",
                                keyboardOptions = KeyboardOptions(
                                    imeAction = if (isMigrationMode) ImeAction.Next else ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { currentYearPaidFocus?.requestFocus() },
                                    onDone = { focusManager?.clearFocus() }
                                )
                            )
                        }
                    }
                    
                    // Current Year Paid Amount (ONLY for migration mode)
                    AnimatedVisibility(visible = isMigrationMode) {
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Current Session Payments",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Amount already collected this session (will be credited against fees)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                StyledTextField(
                                    value = currentYearPaidAmount,
                                    onValueChange = onCurrentYearPaidChange,
                                    label = "Amount Received",
                                    prefix = { Text("₹ ") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager?.clearFocus() }
                                    ),
                                    focusRequester = currentYearPaidFocus
                                )
                            }
                        }
                    }
                    
                    // Session start date info
                    if (sessionStartDate != null) {
                        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "All entries will be dated: ${dateFormat.format(Date(sessionStartDate))} (Session Start)",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportSectionCard(
    hasTransport: Boolean,
    transportRouteId: Long?,
    transportRoutes: List<com.navoditpublic.fees.domain.model.TransportRoute>,
    transportEnrollments: List<TransportEnrollmentUiEntry>,
    isMigrationMode: Boolean,
    currentClass: String,
    onHasTransportChange: (Boolean) -> Unit,
    onRouteChange: (Long?) -> Unit,
    onAddEnrollment: () -> Unit,
    onEditEnrollment: (TransportEnrollmentUiEntry) -> Unit,
    onDeleteEnrollment: (TransportEnrollmentUiEntry) -> Unit
) {
    var isExpanded by remember { mutableStateOf(hasTransport || (isMigrationMode && transportEnrollments.isNotEmpty())) }
    var showRouteSelector by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header - more compact
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                SectionTransport.copy(alpha = 0.12f),
                                SectionTransport.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SectionTransport.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = SectionTransport,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Transport",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (hasTransport) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        color = SectionTransport,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "ACTIVE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                if (hasTransport) "Bus service enrolled"
                                else "Not enrolled",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = SectionTransport,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Transport toggle - more compact
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = hasTransport,
                            onCheckedChange = onHasTransportChange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Uses Transport",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    // Route selection - custom selector
                    if (hasTransport) {
                        val selectedRoute = transportRoutes.find { it.id == transportRouteId }
                        
                        // Custom route selector button
                        Surface(
                            onClick = { showRouteSelector = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Route",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        selectedRoute?.routeName ?: "Select Route",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (selectedRoute != null && currentClass.isNotBlank()) {
                                    val feeForClass = selectedRoute.getFeeForClass(currentClass)
                                    Text(
                                        "₹${feeForClass.toInt()}/mo",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = SectionTransport,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select",
                                    tint = SectionTransport,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        // Route selector dialog
                        if (showRouteSelector) {
                            AlertDialog(
                                onDismissRequest = { showRouteSelector = false },
                                title = { 
                                    Column {
                                        Text("Select Route", fontWeight = FontWeight.SemiBold)
                                        if (currentClass.isNotBlank()) {
                                            Text(
                                                "Showing fees for Class $currentClass",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (transportRoutes.isEmpty()) {
                                            Text(
                                                "No transport routes available",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            transportRoutes.forEach { route ->
                                                val isSelected = route.id == transportRouteId
                                                val feeForClass = if (currentClass.isNotBlank()) {
                                                    route.getFeeForClass(currentClass)
                                                } else {
                                                    route.feeNcTo5 // Default to lowest
                                                }
                                                
                                                Surface(
                                                    onClick = {
                                                        onRouteChange(route.id)
                                                        showRouteSelector = false
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isSelected) SectionTransport.copy(alpha = 0.12f)
                                                           else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SectionTransport)
                                                            else null
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
                                                                route.routeName,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                            )
                                                            // Show all three fees for clarity
                                                            Text(
                                                                "NC-5: ₹${route.feeNcTo5.toInt()} | 6-8: ₹${route.fee6To8.toInt()} | 9-12: ₹${route.fee9To12.toInt()}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                        Column(horizontalAlignment = Alignment.End) {
                                                            if (currentClass.isNotBlank()) {
                                                                Text(
                                                                    "₹${feeForClass.toInt()}/mo",
                                                                    style = MaterialTheme.typography.labelLarge,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = SectionTransport
                                                                )
                                                            }
                                                            if (isSelected) {
                                                                Icon(
                                                                    Icons.Default.Check,
                                                                    contentDescription = null,
                                                                    tint = SectionTransport,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showRouteSelector = false }) {
                                        Text("Close")
                                    }
                                }
                            )
                        }
                    }
                    
                    // Transport History - Only show in migration mode
                    AnimatedVisibility(visible = isMigrationMode) {
                        Column {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Transport History",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                OutlinedButton(
                                    onClick = onAddEnrollment,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SectionTransport)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add Period")
                                }
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            if (transportEnrollments.isEmpty()) {
                                Text(
                                    "No transport history recorded",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SectionTransport.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp)
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
                    enrollment.routeName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${dateFormat.format(Date(enrollment.startDate))} - ${enrollment.endDate?.let { dateFormat.format(Date(it)) } ?: "Present"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "₹${enrollment.monthlyFee.toLong()}/month",
                    style = MaterialTheme.typography.bodySmall,
                    color = SectionTransport,
                    fontWeight = FontWeight.Medium
                )
            }
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(36.dp)) {
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

@Composable
fun CurrentSessionFeesSectionCard(
    sessionName: String,
    breakdown: SessionFeeBreakdown,
    isMigrationMode: Boolean,
    currentYearPaidAmount: String
) {
    var isExpanded by remember { mutableStateOf(true) }
    val creditAmount = currentYearPaidAmount.toDoubleOrNull() ?: 0.0
    val finalBalance = (breakdown.totalExpected - creditAmount).coerceAtLeast(0.0)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                SectionSessionFees.copy(alpha = 0.12f),
                                SectionSessionFees.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SectionSessionFees.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = SectionSessionFees,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Session Fees Summary",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                sessionName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Show total in header
                    Text(
                        "₹${breakdown.totalExpected.toLong()}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SectionSessionFees,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = SectionSessionFees,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Fee Breakdown - Compact
                    Text(
                        "Fees to be Applied",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    FeeBreakdownRow(breakdown.monthlyOrAnnualFeeLabel, breakdown.totalMonthlyFee)
                    if (breakdown.registrationFee > 0) {
                        FeeBreakdownRow("Registration Fee", breakdown.registrationFee)
                    }
                    if (breakdown.admissionFee > 0) {
                        FeeBreakdownRow("Admission Fee", breakdown.admissionFee)
                    }
                    if (breakdown.transportFee > 0) {
                        FeeBreakdownRow("Transport (${breakdown.transportMonths} mo)", breakdown.transportFee)
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    
                    // Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total Fees",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "₹${breakdown.totalExpected.toLong()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SectionSessionFees
                        )
                    }
                    
                    // For migration: show credit and final balance
                    if (isMigrationMode && creditAmount > 0) {
                        Spacer(Modifier.height(4.dp))
                        
                        // Credit amount
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Less: Amount Already Paid",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    "- ₹${creditAmount.toLong()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                        
                        // Net dues after payment
                        Surface(
                            color = if (finalBalance > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                    else Color(0xFF4CAF50).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    if (finalBalance > 0) "Current Net Dues" else "Fully Paid",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "₹${finalBalance.toLong()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (finalBalance > 0) MaterialTheme.colorScheme.error
                                            else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                    
                    // Info note
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "These fees will be automatically added to the student's ledger",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
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
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) SectionSessionFees else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
        ) { DatePicker(state = datePickerState) }
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
        ) { DatePicker(state = datePickerState) }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
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
                
                // Route selection - chip based
                val selectedRoute = routes.find { it.id == enrollment.routeId }
                
                Column {
                    Text(
                        "Select Route",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        routes.forEach { route ->
                            val isSelected = route.id == enrollment.routeId
                            Surface(
                                onClick = { onRouteChange(route.id) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) SectionTransport 
                                       else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = if (isSelected) null 
                                        else androidx.compose.foundation.BorderStroke(
                                            1.dp, 
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                            ) {
                                Text(
                                    text = route.displayText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White 
                                           else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
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
                
                // Monthly fee info
                if (selectedRoute != null) {
                    Surface(
                        color = SectionTransport.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Monthly Fee:")
                            Text(
                                "₹${enrollment.monthlyFee.toLong()}",
                                fontWeight = FontWeight.Bold,
                                color = SectionTransport
                            )
                        }
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(containerColor = SectionTransport)
                    ) { Text("Save") }
                }
            }
        }
    }
}
