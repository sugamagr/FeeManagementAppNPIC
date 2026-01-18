package com.navoditpublic.fees.presentation.screens.settings.classes

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.EmptyState
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.*
import kotlinx.coroutines.delay

// ==================== COLOR PALETTES FOR CLASS LEVELS ====================
private data class ClassLevelColors(
    val primary: Color,
    val secondary: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val containerBg: Color,
    val onContainer: Color
)

private fun getClassLevelColors(level: ClassLevel): ClassLevelColors {
    return when (level) {
        ClassLevel.PRE_PRIMARY -> ClassLevelColors(
            primary = NurseryPrimary,
            secondary = PrePrimaryPrimary,
            gradientStart = NurseryGradientStart,
            gradientEnd = PrePrimaryGradientEnd,
            containerBg = Color(0xFFFFF5F5),
            onContainer = NurseryPrimary
        )
        ClassLevel.PRIMARY -> ClassLevelColors(
            primary = JuniorPrimary,
            secondary = JuniorSecondary,
            gradientStart = JuniorGradientStart,
            gradientEnd = JuniorGradientEnd,
            containerBg = Color(0xFFF0FDF9),
            onContainer = JuniorPrimary
        )
        ClassLevel.MIDDLE -> ClassLevelColors(
            primary = MiddlePrimary,
            secondary = MiddleSecondary,
            gradientStart = MiddleGradientStart,
            gradientEnd = MiddleGradientEnd,
            containerBg = Color(0xFFF5F3FF),
            onContainer = MiddlePrimary
        )
        ClassLevel.SECONDARY -> ClassLevelColors(
            primary = SeniorPrimary,
            secondary = SeniorSecondary,
            gradientStart = SeniorGradientStart,
            gradientEnd = SeniorGradientEnd,
            containerBg = Color(0xFFEEF2FF),
            onContainer = SeniorPrimary
        )
        ClassLevel.SENIOR_SECONDARY -> ClassLevelColors(
            primary = HigherPrimary,
            secondary = HigherSecondary,
            gradientStart = HigherGradientStart,
            gradientEnd = HigherGradientEnd,
            containerBg = Color(0xFFF8FAFC),
            onContainer = HigherPrimary
        )
    }
}

// Background color for the screen
private val WarmBackground = Color(0xFFFAF8F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesSectionsScreen(
    navController: NavController,
    viewModel: ClassesSectionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var selectedClassForSection by remember { mutableStateOf("") }
    var selectedClassLevel by remember { mutableStateOf(ClassLevel.PRE_PRIMARY) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var sectionToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    var animateItems by remember { mutableStateOf(false) }
    
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            delay(100)
            animateItems = true
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ClassesSectionsEvent.Success -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    showAddSectionDialog = false
                    showDeleteConfirmDialog = false
                    sectionToDelete = null
                }
                is ClassesSectionsEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    // Add Section Dialog - Enhanced
    if (showAddSectionDialog) {
        EnhancedAddSectionDialog(
            className = selectedClassForSection,
            classLevel = selectedClassLevel,
            existingSections = state.classesWithSections[selectedClassForSection] ?: emptyList(),
            onDismiss = { showAddSectionDialog = false },
            onAddSingle = { section -> viewModel.addSection(selectedClassForSection, section) },
            onAddMultiple = { sections -> viewModel.addMultipleSections(selectedClassForSection, sections) }
        )
    }
    
    // Delete Confirmation Dialog - Enhanced
    if (showDeleteConfirmDialog && sectionToDelete != null) {
        DeleteConfirmationDialog(
            className = sectionToDelete!!.first,
            sectionName = sectionToDelete!!.second,
            onConfirm = { viewModel.deleteSection(sectionToDelete!!.first, sectionToDelete!!.second) },
            onDismiss = { 
                showDeleteConfirmDialog = false 
                sectionToDelete = null
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (state.showSearch) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Search classes or sections...", color = Color.White.copy(alpha = 0.7f)) },
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
                        Text("Classes & Sections")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (state.showSearch) {
                            viewModel.clearSearch()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            if (state.showSearch) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSearch() }) {
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
        containerColor = WarmBackground
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else if (state.classesWithSections.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Filled.List,
                title = "No Classes",
                subtitle = "Classes will be seeded automatically",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            val filteredClasses = viewModel.getFilteredClasses()
            val groupedByLevel = filteredClasses.groupBy { it.level }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Hero Statistics Card
                item {
                    AnimatedVisibility(
                        visible = animateItems,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -30 }
                    ) {
                        HeroStatisticsCard(
                            totalClasses = state.totalClasses,
                            totalSections = state.totalSections,
                            totalStudents = state.totalStudents
                        )
                    }
                }
                
                // Search Results Info
                if (state.searchQuery.isNotBlank()) {
                    item {
                        AnimatedVisibility(
                            visible = animateItems,
                            enter = fadeIn(tween(300))
                        ) {
                            SearchResultsInfo(
                                query = state.searchQuery,
                                resultCount = filteredClasses.size
                            )
                        }
                    }
                }
                
                // Grouped Class Sections
                ClassLevel.entries.forEach { level ->
                    val classesInLevel = groupedByLevel[level] ?: emptyList()
                    if (classesInLevel.isNotEmpty()) {
                        // Level Header
                        item {
                            val levelIndex = ClassLevel.entries.indexOf(level)
                            AnimatedVisibility(
                                visible = animateItems,
                                enter = fadeIn(tween(400, delayMillis = 100 + (levelIndex * 50))) +
                                        slideInVertically(tween(400, delayMillis = 100 + (levelIndex * 50))) { 20 }
                            ) {
                                ClassLevelHeader(
                                    level = level,
                                    classCount = classesInLevel.size,
                                    studentCount = classesInLevel.sumOf { it.totalStudents },
                                    isExpanded = level in state.expandedLevels,
                                    onToggle = { viewModel.toggleLevelExpanded(level) }
                                )
                            }
                        }
                        
                        // Class Cards within Level
                        if (level in state.expandedLevels) {
                            itemsIndexed(
                                items = classesInLevel,
                                key = { _, classInfo -> "${level.name}_${classInfo.className}" }
                            ) { index, classInfo ->
                                val levelIndex = ClassLevel.entries.indexOf(level)
                                AnimatedVisibility(
                                    visible = animateItems,
                                    enter = fadeIn(tween(400, delayMillis = 150 + (levelIndex * 50) + (index * 30))) +
                                            slideInVertically(tween(400, delayMillis = 150 + (levelIndex * 50) + (index * 30))) { 30 },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    EnhancedClassCard(
                                        classInfo = classInfo,
                                        onAddSection = {
                                            selectedClassForSection = classInfo.className
                                            selectedClassLevel = classInfo.level
                                            showAddSectionDialog = true
                                        },
                                        onDeleteSection = { sectionName ->
                                            sectionToDelete = Pair(classInfo.className, sectionName)
                                            showDeleteConfirmDialog = true
                                        },
                                        onSectionClick = { sectionName ->
                                            navController.navigate(
                                                Screen.StudentListByClass.createRoute(classInfo.className, sectionName)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // No results state
                if (filteredClasses.isEmpty() && state.searchQuery.isNotBlank()) {
                    item {
                        NoResultsState(query = state.searchQuery)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// ==================== HERO STATISTICS CARD ====================
@Composable
private fun HeroStatisticsCard(
    totalClasses: Int,
    totalSections: Int,
    totalStudents: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gradient Icon Container
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Saffron, SaffronAmber)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Academic Structure",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$totalClasses Classes · $totalSections Sections",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Total Classes",
                    value = totalClasses.toString(),
                    icon = Icons.Outlined.Class,
                    color = JuniorPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Sections",
                    value = totalSections.toString(),
                    icon = Icons.Outlined.Layers,
                    color = MiddlePrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Students",
                    value = totalStudents.toString(),
                    icon = Icons.Outlined.Groups,
                    color = Saffron,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== SEARCH RESULTS INFO ====================
@Composable
private fun SearchResultsInfo(
    query: String,
    resultCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Saffron.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = Saffron,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Found $resultCount class${if (resultCount != 1) "es" else ""} matching \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ==================== CLASS LEVEL HEADER ====================
@Composable
private fun ClassLevelHeader(
    level: ClassLevel,
    classCount: Int,
    studentCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val colors = getClassLevelColors(level)
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "rotation"
    )
    
    Surface(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.containerBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Level Indicator Bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(colors.gradientStart, colors.gradientEnd)
                            )
                        )
                )
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column {
                    Text(
                        text = level.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Text(
                        text = "$classCount classes · $studentCount students",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.primary.copy(alpha = 0.7f)
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = colors.primary,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotationAngle)
            )
        }
    }
}

// ==================== ENHANCED CLASS CARD ====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EnhancedClassCard(
    classInfo: ClassInfo,
    onAddSection: () -> Unit,
    onDeleteSection: (String) -> Unit,
    onSectionClick: (String) -> Unit
) {
    val colors = getClassLevelColors(classInfo.level)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Class Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Gradient Class Badge
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(colors.gradientStart, colors.gradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = classInfo.className.take(3),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    Column {
                        Text(
                            text = "Class ${classInfo.className}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Student count badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.primary.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Groups,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${classInfo.totalStudents} students",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.primary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "${classInfo.sections.size} section${if (classInfo.sections.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Add Button
                FilledTonalIconButton(
                    onClick = onAddSection,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = colors.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Section",
                        tint = colors.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sections Grid
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                classInfo.sections.forEach { section ->
                    EnhancedSectionChip(
                        section = section,
                        canDelete = classInfo.sections.size > 1,
                        colors = colors,
                        onDelete = { onDeleteSection(section.name) },
                        onClick = { onSectionClick(section.name) }
                    )
                }
            }
        }
    }
}

// ==================== ENHANCED SECTION CHIP ====================
@Composable
private fun EnhancedSectionChip(
    section: SectionInfo,
    canDelete: Boolean,
    colors: ClassLevelColors,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = colors.containerBg,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = colors.primary.copy(alpha = 0.2f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = 14.dp, 
                end = if (canDelete) 6.dp else 14.dp, 
                top = 10.dp, 
                bottom = 10.dp
            )
        ) {
            Column {
                Text(
                    text = "Section ${section.name}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary
                )
                if (section.studentCount > 0) {
                    Text(
                        text = "${section.studentCount} student${if (section.studentCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = "No students",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            
            if (canDelete) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = onDelete,
                    color = colors.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete section ${section.name}",
                            tint = colors.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
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
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No classes found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "No results for \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== ENHANCED ADD SECTION DIALOG ====================
@Composable
private fun EnhancedAddSectionDialog(
    className: String,
    classLevel: ClassLevel,
    existingSections: List<String>,
    onDismiss: () -> Unit,
    onAddSingle: (String) -> Unit,
    onAddMultiple: (List<String>) -> Unit
) {
    var newSectionName by remember { mutableStateOf("") }
    val colors = getClassLevelColors(classLevel)
    
    // Quick templates
    val templates = listOf(
        "A-B" to listOf("A", "B"),
        "A-C" to listOf("A", "B", "C"),
        "A-D" to listOf("A", "B", "C", "D"),
        "A-E" to listOf("A", "B", "C", "D", "E")
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(colors.gradientStart, colors.gradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = { 
            Text(
                "Add Section to $className",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            ) 
        },
        text = {
            Column {
                // Quick Templates Section
                Text(
                    text = "Quick Templates",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    templates.forEach { (label, sections) ->
                        val newSections = sections.filter { it !in existingSections }
                        val isDisabled = newSections.isEmpty()
                        
                        Surface(
                            onClick = { if (!isDisabled) onAddMultiple(newSections) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDisabled) 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else 
                                colors.primary.copy(alpha = 0.1f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = if (isDisabled) 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                else 
                                    colors.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Divider with text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Text(
                        text = "  or add custom  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Custom Section Input
                OutlinedTextField(
                    value = newSectionName,
                    onValueChange = { newSectionName = it.uppercase().filter { c -> c.isLetter() }.take(1) },
                    label = { Text("Section Name") },
                    placeholder = { Text("e.g., B") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        focusedLabelColor = colors.primary,
                        cursorColor = colors.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Existing sections info
                if (existingSections.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Existing: ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = existingSections.joinToString(", "),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddSingle(newSectionName) },
                enabled = newSectionName.isNotBlank() && newSectionName !in existingSections,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Section")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ==================== DELETE CONFIRMATION DIALOG ====================
@Composable
private fun DeleteConfirmationDialog(
    className: String,
    sectionName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(ErrorRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = { 
            Text(
                "Delete Section?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            ) 
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Class $className - Section $sectionName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "This action cannot be undone. Make sure no students are assigned to this section.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
