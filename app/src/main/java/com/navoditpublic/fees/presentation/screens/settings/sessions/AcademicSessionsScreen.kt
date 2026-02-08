package com.navoditpublic.fees.presentation.screens.settings.sessions

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.domain.model.AcademicSession
import com.navoditpublic.fees.domain.usecase.SessionAccessLevel
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
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
    var showSearch by remember { mutableStateOf(false) }
    var animateItems by remember { mutableStateOf(false) }
    
    // Check if this is the first session (no active sessions exist)
    // Allow creating first session if no active sessions, even if there are inactive ones (legacy data)
    val isFirstSession = state.sessions.isEmpty()
    
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
                is SessionEvent.PromotionComplete -> {
                    Toast.makeText(
                        context,
                        "Migration complete! ${event.result.studentsPromoted} students promoted.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is SessionEvent.RevertComplete -> {
                    Toast.makeText(
                        context,
                        "Session revert complete!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is SessionEvent.SessionSelected -> {
                    // Session was selected, navigate to dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { 
                            inclusive = true 
                        }
                    }
                }
            }
        }
    }
    
    // First Session Dialog - Only shown when no sessions exist
    if (showAddDialog && isFirstSession) {
        AddFirstSessionDialog(
            sessionName = newSessionName,
            onSessionNameChange = { newSessionName = it },
            onDismiss = { showAddDialog = false; newSessionName = "" },
            onConfirm = { viewModel.addFirstSession(newSessionName) }
        )
    }
    
    // Session Promotion Dialog
    if (state.showPromotionWizard) {
        val sourceSession = state.sessions.find { it.id == state.sourceSessionId }
            ?: state.inactiveSessions.find { it.id == state.sourceSessionId }
        val targetSession = state.sessions.find { it.id == state.targetSessionId }
            ?: state.inactiveSessions.find { it.id == state.targetSessionId }
        
        if (sourceSession != null && targetSession != null) {
            SessionPromotionDialog(
                sourceSessionName = sourceSession.sessionName,
                targetSessionName = targetSession.sessionName,
                preview = state.promotionPreview,
                progress = state.promotionProgress,
                result = state.promotionResult,
                isPromoting = state.isPromoting,
                onExecute = { options -> viewModel.executePromotion(options) },
                onDismiss = { viewModel.dismissPromotionWizard() }
            )
        }
    }
    
    // Revert Promotion Dialog
    if (state.showRevertDialog) {
        state.promotionToRevert?.let { promotion ->
            val sourceSession = state.sessions.find { it.id == promotion.sourceSessionId }
                ?: state.inactiveSessions.find { it.id == promotion.sourceSessionId }
            val targetSession = state.sessions.find { it.id == promotion.targetSessionId }
                ?: state.inactiveSessions.find { it.id == promotion.targetSessionId }
            
            RevertPromotionDialog(
                promotion = promotion,
                sourceSessionName = sourceSession?.sessionName ?: "Unknown",
                targetSessionName = targetSession?.sessionName ?: "Unknown",
                safetyCheck = state.revertSafetyCheck,
                progress = state.revertProgress,
                result = state.revertResult,
                isReverting = state.isReverting,
                onExecute = { forceDelete, reason -> viewModel.executeRevert(forceDelete, reason) },
                onDismiss = { viewModel.dismissRevertDialog() }
            )
        }
    }
    
    // Session Selection Confirmation Dialog
    if (state.showSessionSelectionDialog) {
        state.sessionToSelect?.let { session ->
            SessionSelectionDialog(
                session = session,
                accessLevel = state.sessionSelectionAccessLevel,
                onConfirm = {
                    viewModel.confirmSessionSelection()
                    // Navigate to dashboard after selection
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onDismiss = { viewModel.dismissSessionSelectionDialog() }
            )
        }
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
        // Only show FAB when no sessions exist (first time setup)
        floatingActionButton = {
            if (isFirstSession) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Saffron,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Session")
                }
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
                                isPromoted = state.sessionPromotions.containsKey(session.id),
                                isPreviousSession = isPreviousSession(session, state.sessions, state.sessionPromotions),
                                onSelect = { viewModel.onSessionClick(session) },
                                onPromote = { viewModel.startPromotionToNewSession(session) },
                                onRevert = { viewModel.openRevertDialog(session) }
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
                
                // Info note about session management
                if (state.sessions.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    
                    item {
                        AnimatedVisibility(
                            visible = animateItems,
                            enter = fadeIn(tween(400, delayMillis = 300))
                        ) {
                            SessionManagementNote()
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
    isPromoted: Boolean,
    isPreviousSession: Boolean,
    onSelect: () -> Unit,
    onPromote: () -> Unit,
    onRevert: () -> Unit
) {
    val isCurrent = session.isCurrent
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                // Make non-current sessions clickable to view their data
                if (!isCurrent) {
                    Modifier.clickable { onSelect() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
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
                        .background(
                            when {
                                isCurrent -> SuccessGreen
                                isPreviousSession -> AccentPurple
                                else -> InactiveGray
                            }
                        )
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
                        Spacer(modifier = Modifier.width(8.dp))
                        when {
                            isCurrent -> {
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
                            isPreviousSession -> {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AccentPurpleLight
                                ) {
                                    Text(
                                        text = "PREVIOUS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentPurple,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            else -> {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = InactiveGrayLight
                                ) {
                                    Text(
                                        text = "READ-ONLY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = InactiveGray,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
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
                
                // View data button for non-current sessions
                if (!isCurrent) {
                    Surface(
                        modifier = Modifier.clickable { onSelect() },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPreviousSession) AccentPurpleLight else InactiveGrayLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.History,
                                contentDescription = "View Data",
                                tint = if (isPreviousSession) AccentPurple else InactiveGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "View",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPreviousSession) AccentPurple else InactiveGray
                            )
                        }
                    }
                }
            }
            
            // Migrate button for current session
            if (isCurrent) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Saffron.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { onPromote() }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Migrate to Next Session",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SaffronDark
                            )
                            Text(
                                text = "Promote students and create new academic year",
                                style = MaterialTheme.typography.bodySmall,
                                color = Saffron
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Show promotion info and revert button if promoted
            if (isPromoted) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = AccentPurpleLight
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.History,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Created via migration",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentPurple,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = onRevert,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Undo,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Revert",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE53935)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== SESSION MANAGEMENT NOTE ====================
@Composable
private fun SessionManagementNote() {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Session Management",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "To create a new academic session, use the \"Migrate to Next Session\" option from the current session. This will promote all students to their next class and set up fees for the new year.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Current: Full access to all features\n• Previous: Can add/edit receipts (auto-adjusts balances)\n• Older: Read-only for historical reference",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper function to check if session is the previous session (one before current)
// Uses promotion records first (accurate), then falls back to date-based sorting (for legacy data)
private fun isPreviousSession(
    session: AcademicSession, 
    allSessions: List<AcademicSession>,
    promotions: Map<Long, com.navoditpublic.fees.domain.model.SessionPromotion> = emptyMap()
): Boolean {
    val currentSession = allSessions.find { it.isCurrent } ?: return false
    if (session.isCurrent) return false
    
    // First check: Was the current session promoted from this session?
    val currentPromotion = promotions[currentSession.id]
    if (currentPromotion != null) {
        return currentPromotion.sourceSessionId == session.id
    }
    
    // Fallback for legacy data: Use date-based sorting
    val sortedSessions = allSessions.sortedByDescending { it.startDate }
    val currentIndex = sortedSessions.indexOfFirst { it.isCurrent }
    
    // Previous session is the one right after current in the sorted list
    return currentIndex >= 0 && 
           currentIndex + 1 < sortedSessions.size && 
           sortedSessions[currentIndex + 1].id == session.id
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
private fun SessionSelectionDialog(
    session: AcademicSession,
    accessLevel: SessionAccessLevel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isPreviousSession = accessLevel == SessionAccessLevel.PREVIOUS_SESSION
    val iconColor = if (isPreviousSession) AccentPurple else InactiveGray
    val bgColor = if (isPreviousSession) AccentPurpleLight else InactiveGrayLight
    
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = if (isPreviousSession) "View Previous Session Data" else "View Historical Session Data",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text(
                    text = "You are about to view data for session ${session.sessionName}.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // What will be shown
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "This will show:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• All students who were part of this session\n" +
                                   "• Fee receipts from this session only\n" +
                                   "• Pending dues as of ${session.endDateFormatted}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Editing info
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPreviousSession) SuccessGreenLight else Color(0xFFFFF3E0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            if (isPreviousSession) Icons.Outlined.CheckCircle else Icons.Outlined.Info,
                            contentDescription = null,
                            tint = if (isPreviousSession) SuccessGreen else Color(0xFFFF9800),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isPreviousSession) "EDITING ALLOWED" else "READ-ONLY SESSION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPreviousSession) SuccessGreen else Color(0xFFE65100)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPreviousSession) {
                                    "Add/edit/cancel receipts\n(auto-adjusts current session balances)\nEdit student profiles & transport"
                                } else {
                                    "No financial changes can be made.\nStudent profiles can still be edited."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isPreviousSession) SuccessGreen.copy(alpha = 0.8f) else Color(0xFFE65100).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPreviousSession) AccentPurple else InactiveGray
                )
            ) {
                Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Session", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun AddFirstSessionDialog(
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
            Text("Create Your First Session", fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = {
            Column {
                Text(
                    text = "This will be your starting academic session. Future sessions will be created through migration.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val focusManager = LocalFocusManager.current
                OutlinedTextField(
                    value = sessionName,
                    onValueChange = onSessionNameChange,
                    label = { Text("Session Name") },
                    placeholder = { Text("e.g., 2024-25") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (sessionName.isNotBlank()) {
                                onConfirm()
                            }
                            focusManager.clearFocus()
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
                        text = "Enter session in format: 2024-25",
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
            ) { Text("Create Session") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
