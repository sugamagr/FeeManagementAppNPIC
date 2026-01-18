package com.navoditpublic.fees.presentation.screens.settings.sessions

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.presentation.components.EmptyState
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.theme.PaidChipBackground
import com.navoditpublic.fees.util.toRupees

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicSessionsScreen(
    navController: NavController,
    viewModel: AcademicSessionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var newSessionName by remember { mutableStateOf("") }
    
    // State for add fees dialog
    var addTuition by remember { mutableStateOf(true) }
    var addTransport by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SessionEvent.Success -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                    newSessionName = ""
                }
                is SessionEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                is SessionEvent.FeesAdded -> {
                    Toast.makeText(
                        context, 
                        "Added fees for ${event.studentCount} students. Total: ${event.totalFees.toRupees()}", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    // Add Session Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Session") },
            text = {
                Column {
                    Text(
                        text = "Enter session in format: 2025-26",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newSessionName,
                        onValueChange = { newSessionName = it },
                        label = { Text("Session Name") },
                        placeholder = { Text("e.g., 2025-26") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.addSession(newSessionName) },
                    enabled = newSessionName.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Add Fees Dialog (shown after session is created)
    if (state.showAddFeesDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!state.isAddingFees) {
                    viewModel.dismissAddFeesDialog() 
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { 
                Text(
                    "Add Session Fees",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Session ${state.newSessionName} created successfully!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Would you like to add fees for all students for this session?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This will create ledger entries dated April 1:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Tuition checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = addTuition,
                            onCheckedChange = { addTuition = it },
                            enabled = !state.isAddingFees
                        )
                        Text(
                            text = "Tuition Fee (12 months)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    // Transport checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = addTransport,
                            onCheckedChange = { addTransport = it },
                            enabled = !state.isAddingFees
                        )
                        Text(
                            text = "Transport Fee (11 months, June excluded)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    if (state.isAddingFees) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Adding fees for all students...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.addFeesForAllStudents(addTuition, addTransport) },
                    enabled = !state.isAddingFees && (addTuition || addTransport)
                ) {
                    Text("Add Fees")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissAddFeesDialog() },
                    enabled = !state.isAddingFees
                ) {
                    Text("Skip")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academic Sessions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Session")
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else if (state.sessions.isEmpty()) {
            EmptyState(
                icon = Icons.Default.CalendarMonth,
                title = "No Sessions",
                subtitle = "Add an academic session to get started",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.sessions) { session ->
                    SessionCard(
                        session = session,
                        onSetCurrent = { viewModel.setCurrentSession(session.id) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: AcademicSession,
    onSetCurrent: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (session.isCurrent) PaidChipBackground else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = session.isCurrent,
                onClick = onSetCurrent
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.sessionName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${session.startDateFormatted} - ${session.endDateFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (session.isCurrent) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Current",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


