package com.navoditpublic.fees.presentation.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showSeedConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showRefreshConfirm by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.Success -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is SettingsEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    if (showSeedConfirm) {
        AlertDialog(
            onDismissRequest = { showSeedConfirm = false },
            title = { Text("Load Demo Data?") },
            text = { Text("This will add 100+ demo students, fee structures, transport routes, and sample receipts for testing. You can remove them later.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.seedDemoData()
                    showSeedConfirm = false
                }) {
                    Text("Load Demo Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeedConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Demo Data?") },
            text = { Text("This will remove all demo students and their associated receipts and ledger entries. Real data will not be affected.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearDemoData()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Demo Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showRefreshConfirm) {
        AlertDialog(
            onDismissRequest = { showRefreshConfirm = false },
            title = { Text("Refresh Demo Data?") },
            text = { Text("This will clear existing demo data and create fresh data with:\n\n• 150 students across all classes\n• 200+ receipts (including today's)\n• Payment modes (Cash & Online)\n• Opening balances for aging reports\n• Transport enrollments\n\nAll collection reports will show data.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.refreshDemoData()
                        showRefreshConfirm = false
                    }
                ) {
                    Text("Refresh Demo Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRefreshConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    val saffron = Color(0xFFFF6F00)
    val saffronDark = Color(0xFFE65100)
    
    Scaffold(
        topBar = {
            // Modern Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(saffron, saffronDark)
                        )
                    )
                    .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "School",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.School,
                    title = "School Profile",
                    subtitle = "Name, address, contact details",
                    onClick = { navController.navigate(Screen.SchoolProfile.route) }
                )
            }
            
            item {
                Text(
                    text = "Academic",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.CalendarMonth,
                    title = "Academic Sessions",
                    subtitle = "Manage academic years",
                    onClick = { navController.navigate(Screen.AcademicSessions.route) }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.List,
                    title = "Classes & Sections",
                    subtitle = "Add or modify classes and sections",
                    onClick = { navController.navigate(Screen.ClassesAndSections.route) }
                )
            }
            
            item {
                Text(
                    text = "Fees",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Payment,
                    title = "Fee Structure",
                    subtitle = "Monthly, annual, admission fees",
                    onClick = { navController.navigate(Screen.FeeStructure.route) }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.DirectionsBus,
                    title = "Transport Routes",
                    subtitle = "Manage transport routes and fees",
                    onClick = { navController.navigate(Screen.TransportRoutes.route) }
                )
            }
            
            item {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.History,
                    title = "Audit Log",
                    subtitle = "View all changes and revert if needed",
                    onClick = { navController.navigate(Screen.AuditLog.route) }
                )
            }
            
            item {
                Text(
                    text = "Help & Info",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.HelpCenter,
                    title = "How to Use",
                    subtitle = "Complete guide to using the app",
                    onClick = { navController.navigate(Screen.HowToUse.route) }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "App version and developer info",
                    onClick = { navController.navigate(Screen.About.route) }
                )
            }
            
            item {
                Text(
                    text = "Developer",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (!state.hasDemoData) {
                item {
                    SettingsItem(
                        icon = Icons.Default.BugReport,
                        title = "Load Demo Data",
                        subtitle = "Add 150 test students + receipts for testing",
                        onClick = { showSeedConfirm = true }
                    )
                }
            } else {
                item {
                    SettingsItem(
                        icon = Icons.Default.BugReport,
                        title = "Refresh Demo Data",
                        subtitle = "Clear & reload with fresh receipts (for collection reports)",
                        onClick = { showRefreshConfirm = true }
                    )
                }
                
                item {
                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = "Clear Demo Data",
                        subtitle = "Remove all test data (DEMO prefix)",
                        onClick = { showClearConfirm = true }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


