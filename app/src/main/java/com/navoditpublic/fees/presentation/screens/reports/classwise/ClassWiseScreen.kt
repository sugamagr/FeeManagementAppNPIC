package com.navoditpublic.fees.presentation.screens.reports.classwise

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.EmptyState
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.util.ExcelGenerator
import com.navoditpublic.fees.util.PdfGenerator
import com.navoditpublic.fees.util.ReminderTemplate
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Colors
private val GoodGreen = Color(0xFF4CAF50)
private val AverageOrange = Color(0xFFFF9800)
private val CriticalRed = Color(0xFFE53935)
private val WhatsAppGreen = Color(0xFF25D366)
private val WarmBackground = Color(0xFFFAF8F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassWiseScreen(
    navController: NavController,
    viewModel: ClassWiseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Dialog states
    var showExportMenu by remember { mutableStateOf(false) }
    var showReminderSheet by remember { mutableStateOf(false) }
    var showCallSheet by remember { mutableStateOf(false) }
    var showSendingProgress by remember { mutableStateOf(false) }
    var showTemplateManager by remember { mutableStateOf(false) }
    var showTemplateEditor by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<ReminderTemplate?>(null) }
    
    // Reminder sending state
    var currentSendingIndex by remember { mutableIntStateOf(0) }
    var sentCount by remember { mutableIntStateOf(0) }
    var skippedCount by remember { mutableIntStateOf(0) }
    
    // Call tracking state (session only)
    val calledStudents = remember { mutableStateMapOf<Long, Boolean>() }
    var sendingCancelled by remember { mutableStateOf(false) }
    
    val reminderSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    Scaffold(
        containerColor = WarmBackground
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Header
                item {
                    ClassWiseHeader(
                        onBackClick = { navController.popBackStack() },
                        onExportClick = { showExportMenu = true },
                        showExportMenu = showExportMenu,
                        onExportDismiss = { showExportMenu = false },
                        onExportPdf = {
                            showExportMenu = false
                            scope.launch { exportToPdf(context, state) }
                        },
                        onExportExcel = {
                            showExportMenu = false
                            scope.launch { exportToExcel(context, state) }
                        }
                    )
                }
                
                // Hero Summary Card
                item {
                    HeroSummaryCard(
                        totalStudentsWithDues = state.totalStudentsWithDues,
                        totalPending = state.totalPending,
                        totalCollected = state.totalCollected,
                        collectionRate = state.collectionRate,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Search Bar
                item {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Sort Chips
                item {
                    SortChipsRow(
                        selectedSort = state.selectedSort,
                        onSortToggle = { viewModel.toggleSort(it) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                
                // Loading / Empty / Content
                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Saffron)
                        }
                    }
                } else if (state.filteredClassSummaries.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.School,
                            title = if (state.searchQuery.isNotBlank()) "No Classes Found" else "No Data",
                            subtitle = if (state.searchQuery.isNotBlank()) 
                                "Try adjusting your search or filters" 
                            else 
                                "No student data available"
                        )
                    }
                } else {
                    // Class Cards
                    items(
                        items = state.filteredClassSummaries,
                        key = { it.className }
                    ) { summary ->
                        ClassSummaryCard(
                            summary = summary,
                            onViewStudents = {
                                navController.navigate(Screen.LedgerClass.createRoute(summary.className))
                            },
                            onWhatsApp = {
                                if (summary.studentsWithPhone > 0) {
                                    viewModel.selectClassForReminder(summary)
                                    showReminderSheet = true
                                } else {
                                    Toast.makeText(context, "No students with phone numbers in ${summary.className}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCall = {
                                if (summary.studentsWithPhone > 0) {
                                    viewModel.selectClassForReminder(summary)
                                    showCallSheet = true
                                } else {
                                    Toast.makeText(context, "No students with phone numbers in ${summary.className}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
                
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
    
    // Reminder Preview Bottom Sheet
    if (showReminderSheet) {
        state.selectedClassForReminder?.let { classSummary ->
            ModalBottomSheet(
                onDismissRequest = { 
                    showReminderSheet = false
                    viewModel.selectClassForReminder(null)
                },
                sheetState = reminderSheetState,
                containerColor = Color.White
            ) {
                ReminderPreviewContent(
                    classSummary = classSummary,
                templates = state.reminderTemplates,
                selectedTemplate = state.selectedTemplate,
                onTemplateSelect = { viewModel.selectTemplate(it) },
                onManageTemplates = {
                    showTemplateManager = true
                },
                onStartSending = {
                    showReminderSheet = false
                    // Reset sending state
                    currentSendingIndex = 0
                    sentCount = 0
                    skippedCount = 0
                    sendingCancelled = false
                    showSendingProgress = true
                },
                onDismiss = {
                    showReminderSheet = false
                    viewModel.selectClassForReminder(null)
                }
            )
            }
        }
    }
    
    // Call List Bottom Sheet
    if (showCallSheet) {
        state.selectedClassForReminder?.let { selectedClass ->
            val studentsWithPhone = selectedClass.studentsWithDues.filter { it.hasPhone }
            
            ModalBottomSheet(
            onDismissRequest = { 
                showCallSheet = false
                viewModel.selectClassForReminder(null)
            },
            sheetState = reminderSheetState,
            containerColor = Color.White
        ) {
            CallListContent(
                className = selectedClass.className,
                students = studentsWithPhone,
                calledStudents = calledStudents,
                onCallStudent = { student, phone ->
                    calledStudents[student.id] = true
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                    }
                    context.startActivity(intent)
                },
                onToggleCalled = { studentId ->
                    calledStudents[studentId] = !(calledStudents[studentId] ?: false)
                },
                onDismiss = {
                    showCallSheet = false
                    viewModel.selectClassForReminder(null)
                }
            )
            }
        }
    }
    
    // Sending Progress Dialog
    if (showSendingProgress) {
        state.selectedClassForReminder?.let { classForReminder ->
            val studentsToRemind = classForReminder.studentsWithDues.filter { it.hasPhone }
            
            SendingProgressDialog(
            students = studentsToRemind,
            currentIndex = currentSendingIndex,
            sentCount = sentCount,
            onSendToStudent = { studentInfo ->
                val message = viewModel.buildReminderMessage(studentInfo.student, studentInfo.dueAmount)
                val phone = formatPhoneForWhatsApp(studentInfo.availablePhone)
                val encodedMessage = Uri.encode(message)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/$phone?text=$encodedMessage")
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                }
            },
            onMarkSent = {
                sentCount++
                currentSendingIndex++
            },
            onSkip = {
                skippedCount++
                currentSendingIndex++
            },
            onCancel = {
                sendingCancelled = true
                showSendingProgress = false
                viewModel.selectClassForReminder(null)
            },
            onComplete = {
                showSendingProgress = false
                viewModel.selectClassForReminder(null)
                Toast.makeText(
                    context, 
                    "Sent: $sentCount, Skipped: $skippedCount", 
                    Toast.LENGTH_LONG
                ).show()
            }
        )
        }
    }
    
    // Template Manager Dialog
    if (showTemplateManager) {
        TemplateManagerDialog(
            templates = state.reminderTemplates,
            canAddMore = state.canAddMoreTemplates,
            onEdit = { template ->
                editingTemplate = template
                showTemplateEditor = true
            },
            onDelete = { templateId ->
                viewModel.deleteTemplate(templateId)
            },
            onAddNew = {
                editingTemplate = null
                showTemplateEditor = true
            },
            onDismiss = { showTemplateManager = false }
        )
    }
    
    // Template Editor Dialog
    if (showTemplateEditor) {
        TemplateEditorDialog(
            template = editingTemplate,
            onSave = { name, hindi, english ->
                editingTemplate?.let { template ->
                    viewModel.updateTemplate(
                        template.copy(
                            name = name,
                            hindiMessage = hindi,
                            englishMessage = english
                        )
                    )
                } ?: viewModel.addTemplate(name, hindi, english)
                showTemplateEditor = false
                editingTemplate = null
            },
            onDismiss = {
                showTemplateEditor = false
                editingTemplate = null
            }
        )
    }
}

// ==================== HEADER ====================

@Composable
private fun ClassWiseHeader(
    onBackClick: () -> Unit,
    onExportClick: () -> Unit,
    showExportMenu: Boolean,
    onExportDismiss: () -> Unit,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit
) {
    Box(
            modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Saffron, SaffronDark)
                )
            )
            .statusBarsPadding()
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-40).dp, y = (-20).dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = 30.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        
        Row(
                modifier = Modifier
                    .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Text(
                text = "Class-wise Dues",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            
            // Export button with dropdown
            Box {
                IconButton(onClick = onExportClick) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = "Export",
                        tint = Color.White
                    )
                }
                
                DropdownMenu(
                    expanded = showExportMenu,
                    onDismissRequest = onExportDismiss
                ) {
                    DropdownMenuItem(
                        text = { Text("Export as PDF") },
                        leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) },
                        onClick = onExportPdf
                    )
                    DropdownMenuItem(
                        text = { Text("Export as Excel") },
                        leadingIcon = { Icon(Icons.Default.TableChart, null) },
                        onClick = onExportExcel
                    )
                }
            }
        }
    }
}

// ==================== HERO CARD ====================

@Composable
private fun HeroSummaryCard(
    totalStudentsWithDues: Int,
    totalPending: Double,
    totalCollected: Double,
    collectionRate: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Saffron.copy(alpha = 0.2f),
                spotColor = Saffron.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
                    ) {
                // Left side - Total Pending
                        Column {
                            Text(
                        text = "Total Outstanding",
                                style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                        text = totalPending.toRupees(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                        color = CriticalRed
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "From $totalStudentsWithDues students",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Right side - Collection Rate Circle
                Box(contentAlignment = Alignment.Center) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = collectionRate.toFloat(),
                        animationSpec = tween(1000),
                        label = "progress"
                    )
                    
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 8.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        color = when {
                            collectionRate >= 0.7 -> GoodGreen
                            collectionRate >= 0.4 -> AverageOrange
                            else -> CriticalRed
                        },
                        strokeCap = StrokeCap.Round
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                            text = "${(collectionRate * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                            )
                            Text(
                            text = "Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    
            // Bottom stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                        text = "Collected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                        text = totalCollected.toRupees(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = GoodGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                        text = "Pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                        text = totalPending.toRupees(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CriticalRed
                    )
                }
            }
        }
    }
}

// ==================== SEARCH BAR ====================

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search classes...") },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Saffron,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

// ==================== SORT CHIPS ====================

@Composable
private fun SortChipsRow(
    selectedSort: ClassSortOption?,
    onSortToggle: (SortType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Class sort chip
        SortChip(
            label = "Class",
            isSelected = selectedSort?.type == SortType.CLASS,
            ascending = selectedSort?.ascending ?: true,
            ascLabel = "A → Z",
            descLabel = "Z → A",
            onClick = { onSortToggle(SortType.CLASS) }
        )
        
        // Dues sort chip
        SortChip(
            label = "Dues",
            isSelected = selectedSort?.type == SortType.DUES,
            ascending = selectedSort?.ascending ?: false,
            ascLabel = "Low → High",
            descLabel = "High → Low",
            onClick = { onSortToggle(SortType.DUES) }
        )
        
        // Students sort chip
        SortChip(
            label = "Students",
            isSelected = selectedSort?.type == SortType.STUDENTS,
            ascending = selectedSort?.ascending ?: false,
            ascLabel = "Least → Most",
            descLabel = "Most → Least",
            onClick = { onSortToggle(SortType.STUDENTS) }
        )
    }
}

@Composable
private fun SortChip(
    label: String,
    isSelected: Boolean,
    ascending: Boolean,
    ascLabel: String,
    descLabel: String,
    onClick: () -> Unit
) {
    val displayText = if (isSelected) {
        "$label: ${if (ascending) ascLabel else descLabel}"
    } else {
        label
    }
    
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { 
            Text(
                displayText,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                style = MaterialTheme.typography.labelMedium
            ) 
        },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = if (ascending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Saffron
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Saffron.copy(alpha = 0.15f),
            selectedLabelColor = Saffron
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) Saffron else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            selectedBorderColor = Saffron
        )
    )
}

// ==================== CLASS CARD ====================

@Composable
private fun ClassSummaryCard(
    summary: ClassSummary,
    onViewStudents: () -> Unit,
    onWhatsApp: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (summary.performanceCategory) {
        PerformanceCategory.GOOD -> GoodGreen
        PerformanceCategory.AVERAGE -> AverageOrange
        PerformanceCategory.CRITICAL -> CriticalRed
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
        modifier = Modifier
            .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Colored accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            
        Column(
            modifier = Modifier
                .fillMaxWidth()
                    .padding(12.dp)
        ) {
                // Top row: Class badge + Rate + Students count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Class Badge
                Surface(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = summary.className,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    // Rate badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${(summary.collectionRate * 100).toInt()}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    
                    Spacer(Modifier.weight(1f))
                    
                    // Students count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${summary.studentCount}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(Modifier.height(10.dp))
                
                // Stats row: Collected | Pending | With Dues
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Collected
                    Column {
                        Text(
                            text = "Collected",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = summary.collected.toRupees(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = GoodGreen
                        )
                    }
                    
                    // Pending
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Pending",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = summary.pending.toRupees(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CriticalRed
                        )
                    }
                    
                    // With Dues
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "With Dues",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${summary.studentsWithDuesCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(Modifier.height(10.dp))
                
                // Action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // View Students
                    Surface(
                        onClick = onViewStudents,
                        modifier = Modifier.weight(1f),
                        color = Saffron,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "View",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // WhatsApp
                    Surface(
                        onClick = onWhatsApp,
                        modifier = Modifier.weight(1f),
                        color = if (summary.studentsWithPhone > 0) 
                            WhatsAppGreen 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.ChatBubble,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (summary.studentsWithPhone > 0) 
                                    Color.White 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "WhatsApp",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (summary.studentsWithPhone > 0) 
                                    Color.White 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Call
                    Surface(
                        onClick = onCall,
                        modifier = Modifier.weight(1f),
                        color = if (summary.studentsWithPhone > 0) 
                            Color(0xFF2196F3) // Blue for calls
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (summary.studentsWithPhone > 0) 
                                    Color.White 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Call",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (summary.studentsWithPhone > 0) 
                                    Color.White 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== CALL LIST ====================

enum class CallFilterOption { ALL, NOT_CALLED, CALLED }

@Composable
private fun CallListContent(
    className: String,
    students: List<StudentReminderInfo>,
    calledStudents: Map<Long, Boolean>,
    onCallStudent: (StudentReminderInfo, String) -> Unit,
    onToggleCalled: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(CallFilterOption.ALL) }
    var selectedPhoneDialog by remember { mutableStateOf<StudentReminderInfo?>(null) }
    
    val filteredStudents = remember(students, calledStudents, selectedFilter) {
        when (selectedFilter) {
            CallFilterOption.ALL -> students
            CallFilterOption.NOT_CALLED -> students.filter { !(calledStudents[it.id] ?: false) }
            CallFilterOption.CALLED -> students.filter { calledStudents[it.id] == true }
        }
    }
    
    val calledCount = students.count { calledStudents[it.id] == true }
    val progress = if (students.isNotEmpty()) calledCount.toFloat() / students.size else 0f
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        // Header with icon and title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Saffron.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Call Students",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = className,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Saffron.copy(alpha = 0.08f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$calledCount / ${students.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Saffron
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Saffron,
                    trackColor = Saffron.copy(alpha = 0.2f)
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Filter tabs - pill style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CallFilterOption.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                val label = when (filter) {
                    CallFilterOption.ALL -> "All"
                    CallFilterOption.NOT_CALLED -> "Pending"
                    CallFilterOption.CALLED -> "Done"
                }
                val count = when (filter) {
                    CallFilterOption.ALL -> students.size
                    CallFilterOption.NOT_CALLED -> students.size - calledCount
                    CallFilterOption.CALLED -> calledCount
                }
                
                Surface(
                    onClick = { selectedFilter = filter },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Saffron else Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Student list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 350.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredStudents, key = { it.id }) { student ->
                val isCalled = calledStudents[student.id] == true
                
                CallStudentCard(
                    student = student,
                    isCalled = isCalled,
                    onCall = { phone ->
                        if (student.hasMultiplePhones) {
                            selectedPhoneDialog = student
                        } else {
                            onCallStudent(student, phone)
                        }
                    },
                    onToggleCalled = { onToggleCalled(student.id) }
                )
            }
            
            if (filteredStudents.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = when (selectedFilter) {
                                CallFilterOption.ALL -> Icons.Default.Phone
                                CallFilterOption.NOT_CALLED -> Icons.Default.CheckCircle
                                CallFilterOption.CALLED -> Icons.Default.Phone
                            },
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = when (selectedFilter) {
                                CallFilterOption.ALL -> "No students with phone numbers"
                                CallFilterOption.NOT_CALLED -> "All done! 🎉"
                                CallFilterOption.CALLED -> "No calls made yet"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Done button
        Surface(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Saffron
        ) {
            Text(
                text = "Done",
                modifier = Modifier.padding(vertical = 14.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
    
    // Phone selection dialog for multiple numbers
    selectedPhoneDialog?.let { student ->
        AlertDialog(
            onDismissRequest = { selectedPhoneDialog = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Saffron.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Select Number",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            student.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Primary phone
                    if (student.phonePrimary.isNotBlank()) {
                        Surface(
                            onClick = {
                                onCallStudent(student, student.phonePrimary)
                                selectedPhoneDialog = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Saffron.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Saffron.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "1",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Saffron
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Primary",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        student.phonePrimary,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = Saffron
                                    )
                                }
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Saffron,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    // Secondary phone
                    if (student.phoneSecondary.isNotBlank()) {
                        Surface(
                            onClick = {
                                onCallStudent(student, student.phoneSecondary)
                                selectedPhoneDialog = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "2",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Secondary",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        student.phoneSecondary,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPhoneDialog = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun CallStudentCard(
    student: StudentReminderInfo,
    isCalled: Boolean,
    onCall: (String) -> Unit,
    onToggleCalled: () -> Unit
) {
    Surface(
        onClick = onToggleCalled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isCalled) Color(0xFF4CAF50).copy(alpha = 0.06f) else Color.White,
        shadowElevation = if (isCalled) 0.dp else 2.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with checkmark overlay
            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isCalled) Color(0xFF4CAF50).copy(alpha = 0.15f)
                            else Saffron.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCalled) Color(0xFF4CAF50) else Saffron
                    )
                }
                // Checkmark badge when called
                if (isCalled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.White
                        )
                    }
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Student info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCalled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "F: ${student.fatherName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = student.availablePhone,
                        style = MaterialTheme.typography.labelMedium,
                        color = Saffron,
                        fontWeight = FontWeight.Medium
                    )
                    if (student.hasMultiplePhones) {
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Saffron.copy(alpha = 0.1f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "+1",
                                style = MaterialTheme.typography.labelSmall,
                                color = Saffron,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Right side - Due amount and call button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Due badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CriticalRed.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = student.dueAmount.toRupees(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CriticalRed
                    )
                }
                
                // Call button
                Surface(
                    onClick = { onCall(student.availablePhone) },
                    color = Saffron,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Call",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ==================== REMINDER PREVIEW ====================

@Composable
private fun ReminderPreviewContent(
    classSummary: ClassSummary,
    templates: List<ReminderTemplate>,
    selectedTemplate: ReminderTemplate?,
    onTemplateSelect: (ReminderTemplate) -> Unit,
    onManageTemplates: () -> Unit,
    onStartSending: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Title
        Text(
            text = "Send Fee Reminders",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Text(
            text = "Class: ${classSummary.className}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(Modifier.height(20.dp))
        
        // Summary Card
        Card(
                    modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Saffron.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📊 Summary",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                
                SummaryRow("Students with dues", "${classSummary.studentsWithDuesCount}")
                SummaryRow("Have phone number", "${classSummary.studentsWithPhone}", GoodGreen)
                SummaryRow("No phone (skip)", "${classSummary.studentsWithoutPhone}", CriticalRed)
            }
        }
        
                Spacer(Modifier.height(16.dp))
                
        // Template Selection
        Text(
            text = "📝 Select Template",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(Modifier.height(8.dp))
        
        templates.forEach { template ->
            val isSelected = selectedTemplate?.id == template.id
            Surface(
                onClick = { onTemplateSelect(template) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Saffron.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Saffron) else null
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (template.isDefault) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Manage Templates button
        TextButton(
            onClick = onManageTemplates,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Manage Templates")
                }
                
                Spacer(Modifier.height(16.dp))
                
        // Info text
                Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("⚡", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "WhatsApp will open for each student. You'll need to tap Send for each message.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Surface(
                onClick = {
                    if (classSummary.studentsWithPhone > 0) {
                        onStartSending()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (classSummary.studentsWithPhone > 0) WhatsAppGreen else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                        Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                        tint = if (classSummary.studentsWithPhone > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                        text = "Start Sending",
                        color = if (classSummary.studentsWithPhone > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

// ==================== SENDING PROGRESS ====================

@Composable
private fun SendingProgressDialog(
    students: List<StudentReminderInfo>,
    currentIndex: Int,
    sentCount: Int,
    onSendToStudent: (StudentReminderInfo) -> Unit,
    onMarkSent: () -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    val isComplete = currentIndex >= students.size
    
    AlertDialog(
        onDismissRequest = { /* Prevent dismiss by tapping outside */ },
        title = {
            Text(
                text = if (isComplete) "✓ Reminders Sent!" else "Sending Reminders",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isComplete) {
                    // Completion summary
                    Spacer(Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GoodGreen.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("✓ Sent:", fontWeight = FontWeight.Medium)
                                Text("$sentCount", fontWeight = FontWeight.Bold, color = GoodGreen)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("⊘ Skipped:", fontWeight = FontWeight.Medium)
                                Text("${students.size - sentCount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    // Progress
                    val progress = if (students.isNotEmpty()) currentIndex.toFloat() / students.size else 0f
                    
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = WhatsAppGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        text = "${currentIndex + 1} of ${students.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Current student
                    val currentStudent = students.getOrNull(currentIndex)
                    if (currentStudent != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = currentStudent.student.name,
            style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Due: ${currentStudent.dueAmount.toRupees()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CriticalRed
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Text(
                            text = "📱 Tap 'Open WhatsApp' to send the message, then return here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Sent list (scrollable)
                    if (currentIndex > 0) {
                        Text(
                            text = "Recent:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            students.take(currentIndex).reversed().take(5).forEach { info ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = GoodGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = info.student.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isComplete) {
                TextButton(onClick = onComplete) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            } else {
                val currentStudent = students.getOrNull(currentIndex)
                Row {
                    TextButton(onClick = onSkip) {
                        Text("Skip")
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        onClick = {
                            if (currentStudent != null) {
                                onSendToStudent(currentStudent)
                                onMarkSent()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = WhatsAppGreen
                    ) {
                        Text(
                            text = "Open WhatsApp",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        dismissButton = {
            if (!isComplete) {
                TextButton(onClick = onCancel) {
                    Text("Cancel All", color = CriticalRed)
                }
            }
        }
    )
}

// ==================== TEMPLATE MANAGER ====================

@Composable
private fun TemplateManagerDialog(
    templates: List<ReminderTemplate>,
    canAddMore: Boolean,
    onEdit: (ReminderTemplate) -> Unit,
    onDelete: (Int) -> Unit,
    onAddNew: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Manage Templates", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                templates.forEach { template ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = template.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (template.isDefault) {
                                    Text(
                                        text = "Default template",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Saffron
                                    )
                                }
                            }
                            
                            IconButton(onClick = { onEdit(template) }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            if (!template.isDefault) {
                                IconButton(onClick = { onDelete(template.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = CriticalRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                if (canAddMore) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        onClick = onAddNew,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Saffron.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Saffron)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Add New Template",
                                color = Saffron,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Maximum 2 custom templates allowed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

// ==================== TEMPLATE EDITOR ====================

@Composable
private fun TemplateEditorDialog(
    template: ReminderTemplate?,
    onSave: (name: String, hindi: String, english: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(template?.name ?: "") }
    var hindiMessage by remember { mutableStateOf(template?.hindiMessage ?: "") }
    var englishMessage by remember { mutableStateOf(template?.englishMessage ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (template != null) "Edit Template" else "New Template",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                val templateFocusManager = LocalFocusManager.current
                val hindiFocus = remember { FocusRequester() }
                val englishFocus = remember { FocusRequester() }
                
                Text(
                    text = "Use placeholders: {student_name}, {class}, {amount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Saffron
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = template?.isDefault != true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { hindiFocus.requestFocus() })
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = hindiMessage,
                    onValueChange = { hindiMessage = it },
                    label = { Text("Hindi Message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .focusRequester(hindiFocus),
                    maxLines = 10,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { englishFocus.requestFocus() })
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = englishMessage,
                    onValueChange = { englishMessage = it },
                    label = { Text("English Message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .focusRequester(englishFocus),
                    maxLines = 10,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { templateFocusManager.clearFocus() })
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && hindiMessage.isNotBlank() && englishMessage.isNotBlank()) {
                        onSave(name, hindiMessage, englishMessage)
                    }
                },
                enabled = name.isNotBlank() && hindiMessage.isNotBlank() && englishMessage.isNotBlank()
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==================== HELPER FUNCTIONS ====================

private fun formatPhoneForWhatsApp(phone: String): String {
    val cleaned = phone.replace(Regex("[^0-9]"), "")
    return if (cleaned.startsWith("91")) cleaned
    else if (cleaned.length == 10) "91$cleaned"
    else cleaned
}

private suspend fun exportToPdf(context: android.content.Context, state: ClassWiseState) {
    try {
        val headers = listOf("Class", "Students", "Collected", "Pending", "Rate")
        val rows = state.allClassSummaries.map { s ->
            listOf(
                s.className,
                s.studentCount.toString(),
                s.collected.toRupees(),
                s.pending.toRupees(),
                "${(s.collectionRate * 100).toInt()}%"
            )
        }
        val dateFormat = SimpleDateFormat("dd_MMM_yyyy", Locale.getDefault())
        PdfGenerator.generateReport(
            context = context,
            title = "Class-wise Dues Summary",
            headers = headers,
            rows = rows,
            summary = mapOf(
                "Total Students" to state.totalStudents.toString(),
                "Total Collected" to state.totalCollected.toRupees(),
                "Total Pending" to state.totalPending.toRupees(),
                "Collection Rate" to "${(state.collectionRate * 100).toInt()}%"
            ),
            fileName = "ClassWise_Dues_${dateFormat.format(Date())}.pdf"
        )
        Toast.makeText(context, "PDF exported!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private suspend fun exportToExcel(context: android.content.Context, state: ClassWiseState) {
    try {
        val headers = listOf("S.No", "Class", "Students", "Collected", "Pending", "Rate (%)")
        val rows = state.allClassSummaries.mapIndexed { index, s ->
            listOf(
                (index + 1).toString(),
                s.className,
                s.studentCount.toString(),
                s.collected.toLong().toString(),  // Raw number
                s.pending.toLong().toString(),    // Raw number
                (s.collectionRate * 100).toInt().toString()  // Raw percentage number
            )
        }
        val dateFormat = SimpleDateFormat("dd_MMM_yyyy", Locale.getDefault())
        // Numeric columns: S.No (0), Students (2), Collected (3), Pending (4), Rate (5)
        ExcelGenerator.generateReport(
            context = context,
            title = "Class-wise Dues Summary",
            headers = headers,
            rows = rows,
            summary = mapOf(
                "Total Students" to state.totalStudents.toString(),
                "Total Collected" to state.totalCollected.toLong().toString(),
                "Total Pending" to state.totalPending.toLong().toString(),
                "Collection Rate (%)" to (state.collectionRate * 100).toInt().toString()
            ),
            fileName = "ClassWise_Dues_${dateFormat.format(Date())}.csv",
            numericColumns = setOf(0, 2, 3, 4, 5)
        )
        Toast.makeText(context, "Excel exported!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
