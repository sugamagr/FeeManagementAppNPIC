package com.navoditpublic.fees.presentation.screens.ledger.main

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.components.SessionBannerCompact
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.AdvanceChipBackground
import com.navoditpublic.fees.presentation.theme.AdvanceChipText
import com.navoditpublic.fees.presentation.theme.PaidChipBackground
import com.navoditpublic.fees.presentation.theme.PaidChipText
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.presentation.theme.SaffronLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LedgerClassScreen(
    className: String,
    navController: NavController,
    viewModel: LedgerClassViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Check if alphabetical sorting is active
    val isAlphabeticalSort = state.sort == LedgerSort.NAME_ASC
    
    // State for alphabet picker dialog
    var showAlphabetPicker by remember { mutableStateOf(false) }
    
    // Phone selection state
    var showPhoneSelectionDialog by remember { mutableStateOf(false) }
    var selectedStudentForCall by remember { mutableStateOf<StudentLedgerInfo?>(null) }
    
    // Animation for floating circles
    val floatAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        floatAnim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else {
            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.loadData() },
                modifier = Modifier.fillMaxSize()
            ) {
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            bottom = paddingValues.calculateBottomPadding() + 16.dp
                        )
                    ) {
                        // Hero Header with curved bottom
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                // Curved gradient background
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(Saffron, SaffronDark, SaffronDark)
                                            )
                                        )
                                ) {
                                    // Decorative floating circles
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .offset(
                                                x = (-30).dp + (floatAnim.value * 8).dp,
                                                y = (-20).dp + (floatAnim.value * 5).dp
                                            )
                                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(
                                                x = 25.dp - (floatAnim.value * 6).dp,
                                                y = 15.dp + (floatAnim.value * 4).dp
                                            )
                                            .background(Color.White.copy(alpha = 0.06f), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .align(Alignment.CenterEnd)
                                            .offset(
                                                x = 5.dp,
                                                y = 30.dp - (floatAnim.value * 8).dp
                                            )
                                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                    )
                                    
                                    // Header content
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .statusBarsPadding()
                                            .padding(horizontal = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = { navController.popBackStack() }) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back",
                                                        tint = Color.White
                                                    )
                                                }
                                                
                                                // Class badge
                                                Surface(
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = Color.White.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(46.dp)
                                                ) {
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier.fillMaxSize()
                                                    ) {
                                                        Text(
                                                            text = when (state.className) {
                                                                "NC" -> "NC"
                                                                "LKG" -> "LK"
                                                                "UKG" -> "UK"
                                                                else -> state.className.filter { it.isDigit() }.take(2)
                                                            },
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                                
                                                Spacer(Modifier.width(14.dp))
                                                
                                                Column {
                                                    Text(
                                                        "Class ${state.className}",
                                                        style = MaterialTheme.typography.headlineMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = "${state.totalStudents} Students",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.White.copy(alpha = 0.85f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Floating summary card
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .align(Alignment.BottomCenter)
                                        .offset(y = 20.dp)
                                        .shadow(
                                            elevation = 16.dp,
                                            shape = RoundedCornerShape(20.dp),
                                            ambientColor = Saffron.copy(alpha = 0.3f),
                                            spotColor = Saffron.copy(alpha = 0.3f)
                                        ),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    ClassSummaryContent(
                                        totalDues = state.totalDues,
                                        totalPaid = state.totalPaid,
                                        currencyFormat = currencyFormat
                                    )
                                }
                            }
                        }
                        
                        // Spacer for floating card
                        item { Spacer(modifier = Modifier.height(36.dp)) }
                        
                        // Session Banner (shows when viewing historical session)
                        item {
                            SessionBannerCompact(
                                sessionInfo = state.selectedSessionInfo,
                                onSwitchClick = { navController.navigate(Screen.AcademicSessions.route) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        
                        // Search Bar
                        item {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::updateSearchQuery,
                                placeholder = { 
                                    Text(
                                        text = "Search student...",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    ) 
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (state.searchQuery.isNotBlank()) {
                                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                singleLine = true
                            )
                        }
                        
                        item { Spacer(modifier = Modifier.height(12.dp)) }
                        
                        // Filter chips row
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LedgerFilterChip(
                                    text = "All",
                                    selected = state.filter == LedgerFilter.ALL,
                                    onClick = { viewModel.setFilter(LedgerFilter.ALL) }
                                )
                                LedgerFilterChip(
                                    text = "Dues",
                                    selected = state.filter == LedgerFilter.WITH_DUES,
                                    onClick = { viewModel.setFilter(LedgerFilter.WITH_DUES) },
                                    selectedColor = MaterialTheme.colorScheme.error
                                )
                                LedgerFilterChip(
                                    text = "Clear",
                                    selected = state.filter == LedgerFilter.CLEAR,
                                    onClick = { viewModel.setFilter(LedgerFilter.CLEAR) },
                                    selectedColor = PaidChipText
                                )
                                
                                Spacer(Modifier.weight(1f))
                                
                                // Sort chip
                                LedgerSortChip(
                                    currentSort = state.sort,
                                    onClick = {
                                        viewModel.cycleSort()
                                        scope.launch {
                                            listState.scrollToItem(0)
                                        }
                                    }
                                )
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        
                        // Students Header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Students",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tap to view ledger",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Saffron.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "${state.students.size} found",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Saffron
                                    )
                                }
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(12.dp)) }
                        
                        // Student list - grouped or flat depending on sort
                        if (isAlphabeticalSort && state.groupedStudents.isNotEmpty()) {
                            // Grouped by alphabet with headers
                            state.groupedStudents.forEach { (letter, students) ->
                                // Clickable alphabet header
                                item(key = "header_$letter") {
                                    AlphabetHeader(
                                        letter = letter,
                                        onClick = { showAlphabetPicker = true }
                                    )
                                }
                                
                                // Students under this letter
                                items(
                                    items = students,
                                    key = { it.student.id }
                                ) { info ->
                                    val hasPrimary = info.student.phonePrimary.isNotBlank()
                                    val hasSecondary = info.student.phoneSecondary.isNotBlank()
                                    val hasPhone = hasPrimary || hasSecondary
                                    StudentLedgerCard(
                                        info = info,
                                        currencyFormat = currencyFormat,
                                        onClick = {
                                            navController.navigate(Screen.StudentLedger.createRoute(info.student.id))
                                        },
                                        onCall = {
                                            if (hasPrimary && hasSecondary) {
                                                // Multiple phones - show selection dialog
                                                selectedStudentForCall = info
                                                showPhoneSelectionDialog = true
                                            } else if (hasPhone) {
                                                // Single phone - direct call
                                                val phoneNumber = info.student.phonePrimary.ifBlank { info.student.phoneSecondary }
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:$phoneNumber")
                                                }
                                                context.startActivity(intent)
                                            }
                                        },
                                        hasPhoneNumber = hasPhone,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        } else {
                            // Flat list for other sorts
                            items(
                                items = state.students,
                                key = { it.student.id }
                            ) { info ->
                                val hasPrimary = info.student.phonePrimary.isNotBlank()
                                val hasSecondary = info.student.phoneSecondary.isNotBlank()
                                val hasPhone = hasPrimary || hasSecondary
                                StudentLedgerCard(
                                    info = info,
                                    currencyFormat = currencyFormat,
                                    onClick = {
                                        navController.navigate(Screen.StudentLedger.createRoute(info.student.id))
                                    },
                                    onCall = {
                                        if (hasPrimary && hasSecondary) {
                                            // Multiple phones - show selection dialog
                                            selectedStudentForCall = info
                                            showPhoneSelectionDialog = true
                                        } else if (hasPhone) {
                                            // Single phone - direct call
                                            val phoneNumber = info.student.phonePrimary.ifBlank { info.student.phoneSecondary }
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:$phoneNumber")
                                            }
                                            context.startActivity(intent)
                                        }
                                    },
                                    hasPhoneNumber = hasPhone,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                        
                        // Empty State
                        if (state.students.isEmpty()) {
                            item {
                                EmptyStudentState(searchActive = state.searchQuery.isNotBlank() || state.filter != LedgerFilter.ALL)
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    
                }
                
                // Alphabet Picker Dialog
                if (showAlphabetPicker && state.availableLetters.isNotEmpty()) {
                    AlphabetPickerDialog(
                        letters = state.availableLetters,
                        onLetterSelected = { letter ->
                            showAlphabetPicker = false
                            scope.launch {
                                val index = state.getIndexForLetter(letter)
                                if (index >= 0) {
                                    // Add offset for header items (hero, spacer, search, filters, etc.)
                                    listState.animateScrollToItem(index + 7)
                                }
                            }
                        },
                        onDismiss = { showAlphabetPicker = false }
                    )
                }
                
                // Phone Selection Dialog
                if (showPhoneSelectionDialog && selectedStudentForCall != null) {
                    PhoneSelectionDialog(
                        studentName = selectedStudentForCall!!.student.name,
                        primaryPhone = selectedStudentForCall!!.student.phonePrimary,
                        secondaryPhone = selectedStudentForCall!!.student.phoneSecondary,
                        onPhoneSelected = { phone ->
                            showPhoneSelectionDialog = false
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phone")
                            }
                            context.startActivity(intent)
                            selectedStudentForCall = null
                        },
                        onDismiss = {
                            showPhoneSelectionDialog = false
                            selectedStudentForCall = null
                        }
                    )
                }
            }
        }
    }
}

// ==================== FILTER CHIP ====================

@Composable
private fun LedgerFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = Saffron,
    icon: ImageVector? = null
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) selectedColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (selected) BorderStroke(1.dp, selectedColor.copy(alpha = 0.5f)) else null,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(14.dp),
                    tint = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== SORT CHIP ====================

@Composable
private fun LedgerSortChip(
    currentSort: LedgerSort,
    onClick: () -> Unit
) {
    val sortText = when (currentSort) {
        LedgerSort.NAME_ASC -> "Name"
        LedgerSort.DUES_HIGH -> "₹ High"
        LedgerSort.DUES_LOW -> "₹ Low"
        LedgerSort.ACCOUNT_NUMBER -> "A/C"
    }
    
    val isNonDefault = currentSort != LedgerSort.NAME_ASC
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isNonDefault) Saffron.copy(alpha = 0.15f) 
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = if (isNonDefault) BorderStroke(1.dp, Saffron.copy(alpha = 0.5f)) else null,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Sort",
                modifier = Modifier.size(16.dp),
                tint = if (isNonDefault) Saffron else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                sortText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isNonDefault) Saffron else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== ALPHABET HEADER ====================

@Composable
private fun AlphabetHeader(
    letter: Char,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Letter badge
        Surface(
            shape = CircleShape,
            color = Saffron.copy(alpha = 0.15f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = letter.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Saffron
                )
            }
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        // Divider line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Saffron.copy(alpha = 0.2f))
        )
        
        Spacer(modifier = Modifier.width(10.dp))
        
        // Tap hint
        Text(
            text = "Tap to jump",
            style = MaterialTheme.typography.labelSmall,
            color = Saffron.copy(alpha = 0.6f)
        )
    }
}

// ==================== ALPHABET PICKER DIALOG ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlphabetPickerDialog(
    letters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.8f),
            exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.8f)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Text(
                        text = "Jump to Letter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "Select a letter to quickly navigate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Letter grid
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        maxItemsInEachRow = 7
                    ) {
                        letters.forEach { letter ->
                            Surface(
                                modifier = Modifier
                                    .size(42.dp)
                                    .padding(3.dp)
                                    .clickable { onLetterSelected(letter) },
                                shape = CircleShape,
                                color = Saffron.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Saffron.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Saffron
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Cancel button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onDismiss),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Cancel",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ==================== SUMMARY CONTENT ====================

@Composable
private fun ClassSummaryContent(
    totalDues: Double,
    totalPaid: Double,
    currencyFormat: NumberFormat
) {
    val total = totalDues + totalPaid
    val collectionProgress = if (total > 0) (totalPaid / total).toFloat() else 1f
    val progressPercent = (collectionProgress * 100).toInt()
    
    // Animated progress
    var animatedPercent by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(progressPercent) {
        val startPercent = animatedPercent
        val duration = 800
        val startTime = System.currentTimeMillis()
        
        while (animatedPercent != progressPercent) {
            val elapsed = System.currentTimeMillis() - startTime
            val fraction = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            animatedPercent = (startPercent + (progressPercent - startPercent) * fraction).toInt()
            if (fraction >= 1f) break
            delay(16)
        }
        animatedPercent = progressPercent
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Total Dues
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Total Dues",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currencyFormat.format(totalDues),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (totalDues > 0) MaterialTheme.colorScheme.error else PaidChipText
            )
        }
        
        // Mini Circular Progress
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(70.dp)
        ) {
            val animatedProgressValue by animateFloatAsState(
                targetValue = collectionProgress,
                animationSpec = tween(800, easing = FastOutSlowInEasing),
                label = "progress"
            )
            
            Canvas(modifier = Modifier.size(70.dp)) {
                val strokeWidth = 8.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)
                
                // Background track
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                
                // Progress arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(SaffronLight, Saffron, SaffronDark, Saffron)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgressValue,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            Text(
                text = "$animatedPercent%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Saffron
            )
        }
        
        // Total Paid
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Total Paid",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currencyFormat.format(totalPaid),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PaidChipText
            )
        }
    }
}

// ==================== STUDENT CARD ====================

@Composable
fun StudentLedgerCard(
    info: StudentLedgerInfo,
    currencyFormat: NumberFormat,
    onClick: () -> Unit,
    onCall: () -> Unit,
    hasPhoneNumber: Boolean,
    modifier: Modifier = Modifier
) {
    val hasDues = info.netBalance > 0
    val hasAdvance = info.netBalance < 0
    
    // Status color
    val statusColor = when {
        hasDues -> Color(0xFFE53935)
        hasAdvance -> Color(0xFF1E88E5)
        else -> Color(0xFF43A047)
    }
    
    val statusBgColor = when {
        hasDues -> MaterialTheme.colorScheme.errorContainer
        hasAdvance -> AdvanceChipBackground
        else -> PaidChipBackground
    }
    
    val statusTextColor = when {
        hasDues -> MaterialTheme.colorScheme.error
        hasAdvance -> AdvanceChipText
        else -> PaidChipText
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Avatar with phone badge
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(statusBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = info.student.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor
                    )
                }
                
                // Phone badge on avatar
                if (hasPhoneNumber) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .offset(x = 2.dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366)) // WhatsApp green
                            .clickable(onClick = onCall),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "Call",
                            modifier = Modifier.size(11.dp),
                            tint = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Student info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = info.student.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBgColor
                    ) {
                        Text(
                            text = when {
                                hasDues -> currencyFormat.format(info.netBalance)
                                hasAdvance -> "+${currencyFormat.format(-info.netBalance)}"
                                else -> "Clear"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Father name and Account in one row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "S/o ${info.student.fatherName}  •  A/C: ${info.student.accountNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Arrow
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "View",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ==================== EMPTY STATE ====================

@Composable
fun EmptyStudentState(searchActive: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (searchActive) Icons.Default.Search else Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (searchActive) "No Students Match" else "No Students",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (searchActive) "Try adjusting your search or filters" else "No students in this class",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ==================== PHONE SELECTION DIALOG ====================

@Composable
private fun PhoneSelectionDialog(
    studentName: String,
    primaryPhone: String,
    secondaryPhone: String,
    onPhoneSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    tint = Saffron
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Select Number",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Choose a number to call $studentName:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Primary Phone
                if (primaryPhone.isNotBlank()) {
                    PhoneOptionCard(
                        phone = primaryPhone,
                        label = "Primary",
                        onClick = { onPhoneSelected(primaryPhone) }
                    )
                }
                
                // Secondary Phone
                if (secondaryPhone.isNotBlank()) {
                    PhoneOptionCard(
                        phone = secondaryPhone,
                        label = "Secondary",
                        onClick = { onPhoneSelected(secondaryPhone) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PhoneOptionCard(
    phone: String,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Saffron.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Saffron.copy(alpha = 0.15f)
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = Saffron
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = phone,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.Phone,
                contentDescription = "Call",
                tint = Saffron
            )
        }
    }
}
