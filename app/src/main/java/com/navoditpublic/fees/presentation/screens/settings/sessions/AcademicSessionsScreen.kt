package com.navoditpublic.fees.presentation.screens.settings.sessions

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.delay

// Color palette - matching Dashboard
private val SuccessGreen = Color(0xFF4CAF50)
private val SuccessGreenLight = Color(0xFFE8F5E9)
private val AccentPurple = Color(0xFF7C4DFF)
private val AccentPurpleLight = Color(0xFFEDE7F6)
private val InactiveGray = Color(0xFF9E9E9E)
private val InactiveGrayLight = Color(0xFFF5F5F5)
private val WarmBackground = Color(0xFFFAF8F5)

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
    var addTuition by remember { mutableStateOf(true) }
    var addTransport by remember { mutableStateOf(true) }
    var showSearch by remember { mutableStateOf(false) }
    var animateItems by remember { mutableStateOf(false) }
    
    // Confirmation dialogs
    var sessionToDeactivate by remember { mutableStateOf<AcademicSession?>(null) }
    var sessionToDelete by remember { mutableStateOf<AcademicSession?>(null) }
    
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            delay(100)
            animateItems = true
        }
    }
    
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
    
    // Dialogs
    if (showAddDialog) {
        AddSessionDialog(
            sessionName = newSessionName,
            onSessionNameChange = { newSessionName = it },
            onDismiss = { showAddDialog = false; newSessionName = "" },
            onConfirm = { viewModel.addSession(newSessionName) }
        )
    }
    
    if (state.showAddFeesDialog) {
        AddFeesDialog(
            sessionName = state.newSessionName,
            addTuition = addTuition,
            addTransport = addTransport,
            isLoading = state.isAddingFees,
            onTuitionChange = { addTuition = it },
            onTransportChange = { addTransport = it },
            onDismiss = { if (!state.isAddingFees) viewModel.dismissAddFeesDialog() },
            onConfirm = { viewModel.addFeesForAllStudents(addTuition, addTransport) }
        )
    }
    
    state.sessionToDuplicate?.let { session ->
        DuplicateSessionDialog(
            sourceSession = session,
            onDismiss = { viewModel.dismissDuplicateDialog() },
            onConfirm = { newName -> viewModel.duplicateSession(session, newName) }
        )
    }
    
    sessionToDeactivate?.let { session ->
        ConfirmationDialog(
            icon = Icons.Outlined.Archive,
            iconColor = InactiveGray,
            title = "Archive Session?",
            message = "Session ${session.sessionName} will be archived. You can restore it later.",
            confirmText = "Archive",
            confirmColor = InactiveGray,
            onConfirm = {
                viewModel.deactivateSession(session.id)
                sessionToDeactivate = null
            },
            onDismiss = { sessionToDeactivate = null }
        )
    }
    
    sessionToDelete?.let { session ->
        ConfirmationDialog(
            icon = Icons.Outlined.Delete,
            iconColor = Color(0xFFE53935),
            title = "Delete Permanently?",
            message = "Session ${session.sessionName} will be permanently deleted. This cannot be undone.",
            confirmText = "Delete",
            confirmColor = Color(0xFFE53935),
            onConfirm = {
                viewModel.permanentlyDeleteSession(session.id)
                sessionToDelete = null
            },
            onDismiss = { sessionToDelete = null }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (showSearch) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Search sessions...", color = Color.White.copy(alpha = 0.7f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Academic Sessions")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (showSearch) {
                            showSearch = false
                            viewModel.clearSearch()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            if (showSearch) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (!showSearch) {
                        IconButton(
                            onClick = { viewModel.refresh() },
                            enabled = !state.isRefreshing
                        ) {
                            if (state.isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                            }
                        }
                    }
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Saffron,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Session")
            }
        },
        containerColor = WarmBackground
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Current Session Hero Card
                item {
                    AnimatedVisibility(
                        visible = animateItems,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 30 }
                    ) {
                        CurrentSessionCard(
                            currentSession = state.sessions.find { it.isCurrent },
                            stats = state.sessions.find { it.isCurrent }?.let { state.sessionStats[it.id] }
                        )
                    }
                }
                
                // Active Sessions Section
                if (state.filteredSessions.isNotEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = animateItems,
                            enter = fadeIn(tween(400, delayMillis = 100))
                        ) {
                            SectionHeader(
                                title = "All Sessions",
                                action = "${state.filteredSessions.size} total"
                            )
                        }
                    }
                    
                    itemsIndexed(
                        items = state.filteredSessions,
                        key = { _, session -> session.id }
                    ) { index, session ->
                        AnimatedVisibility(
                            visible = animateItems,
                            enter = fadeIn(tween(400, delayMillis = 150 + (index * 50))) +
                                    slideInVertically(tween(400, delayMillis = 150 + (index * 50))) { 30 },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            SessionCard(
                                session = session,
                                onSetCurrent = { viewModel.setCurrentSession(session.id) },
                                onArchive = { sessionToDeactivate = session },
                                onDuplicate = { viewModel.showDuplicateDialog(session) }
                            )
                        }
                    }
                } else if (state.searchQuery.isNotBlank()) {
                    item {
                        NoResultsState(query = state.searchQuery)
                    }
                } else {
                    item {
                        EmptySessionsState(onAddClick = { showAddDialog = true })
                    }
                }
                
                // Archived Sessions Section
                if (state.inactiveSessions.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    
                    item {
                        AnimatedVisibility(
                            visible = animateItems,
                            enter = fadeIn(tween(400, delayMillis = 300))
                        ) {
                            ArchivedSessionsSection(
                                sessions = state.inactiveSessions,
                                isExpanded = state.showInactiveSessions,
                                onToggleExpand = { viewModel.toggleShowInactiveSessions() },
                                onReactivate = { viewModel.reactivateSession(it.id) },
                                onDelete = { sessionToDelete = it }
                            )
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ==================== SECTION HEADER ====================
@Composable
private fun SectionHeader(
    title: String,
    action: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== CURRENT SESSION CARD ====================
@Composable
private fun CurrentSessionCard(
    currentSession: AcademicSession?,
    stats: SessionStats?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Accent bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(SuccessGreen, Color(0xFF2E7D32))
                            )
                        )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SuccessGreenLight,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Current Session",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = currentSession?.sessionName ?: "Not Set",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (currentSession != null) MaterialTheme.colorScheme.onSurface 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (currentSession != null) {
                        Text(
                            text = "${currentSession.startDateFormatted} - ${currentSession.endDateFormatted}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Progress indicator
                if (currentSession != null) {
                    val progress = calculateSessionProgress(currentSession)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(
                            text = "complete",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = SuccessGreen,
                            trackColor = SuccessGreenLight,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
            
            // Stats Row
            if (currentSession != null && stats != null) {
                Spacer(modifier = Modifier.height(14.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Days Remaining
                    val daysRemaining = calculateDaysRemaining(currentSession)
                    StatChip(
                        label = "Days Left",
                        value = daysRemaining.toString(),
                        color = AccentPurple,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Students Count
                    StatChip(
                        label = "Students",
                        value = stats.studentsCount.toString(),
                        color = Color(0xFF1E88E5),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Collection Rate
                    StatChip(
                        label = "Collected",
                        value = "${stats.collectionRate.toInt()}%",
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

// ==================== SESSION CARD ====================
@Composable
private fun SessionCard(
    session: AcademicSession,
    onSetCurrent: () -> Unit,
    onArchive: () -> Unit,
    onDuplicate: () -> Unit
) {
    val isCurrent = session.isCurrent
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isCurrent) SuccessGreen else AccentPurple)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.sessionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SuccessGreenLight
                        ) {
                            Text(
                                text = "CURRENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${session.startDateFormatted} → ${session.endDateFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action buttons
            if (!isCurrent) {
                IconButton(
                    onClick = onSetCurrent,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = "Set Current",
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            IconButton(
                onClick = onDuplicate,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = "Duplicate",
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            if (!isCurrent) {
                IconButton(
                    onClick = onArchive,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.Archive,
                        contentDescription = "Archive",
                        tint = InactiveGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ==================== ARCHIVED SESSIONS SECTION ====================
@Composable
private fun ArchivedSessionsSection(
    sessions: List<AcademicSession>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onReactivate: (AcademicSession) -> Unit,
    onDelete: (AcademicSession) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            onClick = onToggleExpand,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Archive,
                        contentDescription = null,
                        tint = InactiveGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Archived Sessions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${sessions.size} session${if (sessions.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(tween(300)) + fadeIn(tween(300)),
            exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sessions.forEach { session ->
                    ArchivedSessionCard(
                        session = session,
                        onReactivate = { onReactivate(session) },
                        onDelete = { onDelete(session) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchivedSessionCard(
    session: AcademicSession,
    onReactivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.sessionName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = InactiveGray
                )
                Text(
                    text = "${session.startDateFormatted} - ${session.endDateFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            IconButton(onClick = onReactivate, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Outlined.Restore,
                    contentDescription = "Restore",
                    tint = SuccessGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ==================== EMPTY STATE ====================
@Composable
private fun EmptySessionsState(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Academic Sessions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Create your first session to start managing fees",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Session")
            }
        }
    }
}

// ==================== NO RESULTS STATE ====================
@Composable
private fun NoResultsState(query: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "No sessions found",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "No results for \"$query\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== DIALOGS ====================
@Composable
private fun ConfirmationDialog(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
        },
        title = { 
            Text(title, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = { 
            Text(message, textAlign = TextAlign.Center)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                shape = RoundedCornerShape(10.dp)
            ) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AddSessionDialog(
    sessionName: String,
    onSessionNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val previewDates = remember(sessionName) {
        try {
            val regex = Regex("^\\d{4}-\\d{2}$")
            if (regex.matches(sessionName)) {
                val startYear = sessionName.split("-")[0].toInt()
                val endYear = startYear + 1
                "April 1, $startYear → March 31, $endYear"
            } else null
        } catch (e: Exception) { null }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Saffron.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = Saffron, modifier = Modifier.size(24.dp))
            }
        },
        title = { 
            Text("Add New Session", fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = {
            Column {
                val addSessionFocusManager = LocalFocusManager.current
                OutlinedTextField(
                    value = sessionName,
                    onValueChange = onSessionNameChange,
                    label = { Text("Session Name") },
                    placeholder = { Text("e.g., 2025-26") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (sessionName.isNotBlank()) {
                                onConfirm()
                            }
                            addSessionFocusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (previewDates != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SuccessGreenLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Session Period",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SuccessGreen
                            )
                            Text(
                                text = previewDates,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Enter session in format: 2025-26",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = sessionName.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
            ) { Text("Add Session") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun DuplicateSessionDialog(
    sourceSession: AcademicSession,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val suggestedName = remember(sourceSession) {
        try {
            val parts = sourceSession.sessionName.split("-")
            val startYear = parts[0].toInt() + 1
            val endYearShort = (startYear + 1).toString().takeLast(2)
            "$startYear-$endYearShort"
        } catch (e: Exception) { "" }
    }
    
    var newSessionName by remember { mutableStateOf(suggestedName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AccentPurple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ContentCopy, null, tint = AccentPurple, modifier = Modifier.size(24.dp))
            }
        },
        title = { 
            Text("Duplicate Session", fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = {
            Column {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("From:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Text(sourceSession.sessionName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AccentPurple)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val duplicateFocusManager = LocalFocusManager.current
                OutlinedTextField(
                    value = newSessionName,
                    onValueChange = { newSessionName = it },
                    label = { Text("New Session Name") },
                    placeholder = { Text("e.g., 2026-27") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newSessionName.isNotBlank() && newSessionName != sourceSession.sessionName) {
                                onConfirm(newSessionName)
                            }
                            duplicateFocusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newSessionName) },
                enabled = newSessionName.isNotBlank() && newSessionName != sourceSession.sessionName,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Duplicate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AddFeesDialog(
    sessionName: String,
    addTuition: Boolean,
    addTransport: Boolean,
    isLoading: Boolean,
    onTuitionChange: (Boolean) -> Unit,
    onTransportChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
            }
        },
        title = { 
            Text("Session Created!", fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = {
            Column {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SuccessGreenLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = sessionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Text("Add session fees for all students?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("Creates ledger entries dated April 1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Tuition checkbox
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(enabled = !isLoading) { onTuitionChange(!addTuition) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = addTuition, onCheckedChange = { if (!isLoading) onTuitionChange(it) }, enabled = !isLoading)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Tuition Fee", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("12 months, based on class fee structure", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Transport checkbox
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(enabled = !isLoading) { onTransportChange(!addTransport) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = addTransport, onCheckedChange = { if (!isLoading) onTransportChange(it) }, enabled = !isLoading)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Transport Fee", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("11 months, June excluded", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                if (isLoading) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Saffron)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Adding fees...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading && (addTuition || addTransport),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
            ) {
                Icon(Icons.Outlined.Payment, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add Fees")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Skip") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ==================== HELPER FUNCTIONS ====================
private fun calculateSessionProgress(session: AcademicSession): Float {
    val now = System.currentTimeMillis()
    val start = session.startDate
    val end = session.endDate
    
    return when {
        now < start -> 0f
        now > end -> 1f
        else -> ((now - start).toFloat() / (end - start).toFloat()).coerceIn(0f, 1f)
    }
}

private fun calculateDaysRemaining(session: AcademicSession): Int {
    val now = System.currentTimeMillis()
    val end = session.endDate
    return if (now > end) 0 else ((end - now) / (1000 * 60 * 60 * 24)).toInt()
}
