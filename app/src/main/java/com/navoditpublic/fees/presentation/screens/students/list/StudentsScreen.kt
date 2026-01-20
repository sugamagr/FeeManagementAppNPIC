package com.navoditpublic.fees.presentation.screens.students.list

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.domain.model.StudentWithBalance
import com.navoditpublic.fees.presentation.components.Avatar
import com.navoditpublic.fees.presentation.components.BalanceChip
import com.navoditpublic.fees.presentation.components.EmptyState
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.components.SearchBar
import com.navoditpublic.fees.presentation.navigation.LocalDrawerState
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.*
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler

// ==================== MAIN STUDENTS SCREEN (CLASS LIST) ====================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudentsScreen(
    navController: NavController,
    viewModel: StudentsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var expandedClass by remember { mutableStateOf<String?>(null) }
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()
    
    // Handle back press: Clear expanded class or search query before navigating away
    BackHandler(enabled = expandedClass != null || state.searchQuery.isNotEmpty()) {
        when {
            expandedClass != null -> expandedClass = null
            state.searchQuery.isNotEmpty() -> viewModel.onSearchQueryChange("")
        }
    }
    
    Scaffold(
        containerColor = Cream
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Premium Curved Header with Search
                PremiumHeader(
                    totalStudents = state.totalStudentCount,
                    inactiveStudents = state.inactiveStudentCount,
                    searchQuery = state.searchQuery,
                    onSearchChange = viewModel::onSearchQueryChange,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onAddClick = { navController.navigate(Screen.AddEditStudent.createRoute()) }
                )
                
                // Show search results if searching
                if (state.searchQuery.length >= 2) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column {
                    Text(
                        "Search Results (${state.filteredStudents.size})",
                        style = MaterialTheme.typography.labelMedium,
                                color = Saffron,
                        fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    if (state.filteredStudents.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Group, 
                            title = "No Results", 
                            subtitle = "Try a different search term"
                        )
                    } else {
                        LazyColumn(
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.filteredStudents, key = { it.student.id }) { student ->
                                StudentCardCompact(
                                    studentWithBalance = student,
                                    onClick = { navController.navigate(Screen.StudentDetail.createRoute(student.student.id)) },
                                    onCollectClick = { navController.navigate(Screen.CollectFee.createRoute(student.student.id)) },
                                            showClass = true
                                )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Class cards grid
                    if (state.classSummaries.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.School, 
                            title = "No Students Yet",
                            subtitle = "Add your first student to get started"
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // All Students Hero Card - spans full width
                            item(
                                span = { GridItemSpan(2) },
                                key = "all_students"
                            ) {
                                PremiumAllStudentsCard(
                                    activeStudents = state.totalStudentCount - state.inactiveStudentCount,
                                    inactiveStudents = state.inactiveStudentCount,
                                    totalDues = state.totalDues,
                                    classCount = state.classSummaries.size,
                                    onClick = {
                                        navController.navigate(
                                            Screen.StudentListByClass.createRoute("ALL", "ALL")
                                        )
                                    }
                                )
                            }
                            
                            // Class cards - fast simultaneous load with subtle stagger
                            items(
                                items = state.classSummaries,
                                key = { it.className }
                            ) { classSummary ->
                                DribbbleClassCard(
                                    summary = classSummary,
                                    index = state.classSummaries.indexOf(classSummary),
                                    onClick = {
                                        if (classSummary.sections.size == 1) {
                                            navController.navigate(
                                                Screen.StudentListByClass.createRoute(
                                                    classSummary.className, 
                                                    classSummary.sections.first().sectionName
                                                )
                                            )
                                        } else {
                                            expandedClass = classSummary.className
                                        }
                                    }
                                )
                            }
                            
                            // Inactive Students Card - spans full width, shown after class cards
                            if (state.inactiveStudentCount > 0) {
                                item(
                                    span = { GridItemSpan(2) },
                                    key = "inactive_students"
                                ) {
                                    InactiveStudentsCard(
                                        inactiveCount = state.inactiveStudentCount,
                                        onClick = {
                                            navController.navigate(
                                                Screen.StudentListByClass.createRoute("INACTIVE", "ALL")
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Section selection dialog
            if (expandedClass != null) {
                val classSummary = state.classSummaries.find { it.className == expandedClass }
                if (classSummary != null && classSummary.sections.size > 1) {
                    SectionSelectionSheet(
                        className = classSummary.className,
                        sections = classSummary.sections,
                        onSectionClick = { section ->
                            navController.navigate(
                                Screen.StudentListByClass.createRoute(classSummary.className, section)
                            )
                            expandedClass = null
                        },
                        onDismiss = { expandedClass = null }
                    )
                }
            }
        }
    }
}

// ==================== PREMIUM CURVED HEADER ====================

@Composable
private fun PremiumHeader(
    totalStudents: Int,
    inactiveStudents: Int = 0,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onAddClick: () -> Unit
) {
    // Animated student count
    val animatedCount by animateIntAsState(
        targetValue = totalStudents,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "studentCount"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Main gradient background
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF6F00),
                            Color(0xFFE65100)
                        )
                    )
                )
                
                // Decorative circles
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = 180.dp.toPx(),
                    center = Offset(size.width + 40.dp.toPx(), -20.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = 120.dp.toPx(),
                    center = Offset(-30.dp.toPx(), size.height - 20.dp.toPx())
                )
                
                // Wave/curve at bottom
                val wavePath = Path().apply {
                    moveTo(0f, size.height - 24.dp.toPx())
                    quadraticBezierTo(
                        size.width * 0.25f, size.height - 8.dp.toPx(),
                        size.width * 0.5f, size.height - 16.dp.toPx()
                    )
                    quadraticBezierTo(
                        size.width * 0.75f, size.height - 24.dp.toPx(),
                        size.width, size.height - 12.dp.toPx()
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = wavePath,
                    color = Cream,
                    style = Fill
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            // Top bar with menu and add button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            Icons.Default.Menu, 
                            "Menu", 
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            "Students",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            if (inactiveStudents > 0) "$animatedCount total ($inactiveStudents inactive)" else "$animatedCount total",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Premium "New" button with pill shape
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .height(40.dp)
                        .clickable(onClick = onAddClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "New",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Saffron
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Integrated Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                shadowElevation = 12.dp,
                tonalElevation = 4.dp
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchChange,
                    placeholder = "Search by name, father's name, phone...",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ==================== PREMIUM ALL STUDENTS CARD (Glassmorphism) ====================

@Composable
private fun PremiumAllStudentsCard(
    activeStudents: Int,
    inactiveStudents: Int,
    totalDues: Double,
    classCount: Int,
    onClick: () -> Unit
) {
    // Animated values
    val animatedActiveCount by animateIntAsState(
        targetValue = activeStudents,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "activeStudents"
    )
    
    // Press animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 16.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "elevation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(24.dp),
                spotColor = HeroCardPrimary.copy(alpha = 0.25f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            HeroCardGradientStart,
                            HeroCardGradientEnd,
                            Color(0xFF334155)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Decorative gradient orbs
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-40).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                HeroCardAccent.copy(alpha = 0.3f),
                                HeroCardAccent.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-30).dp, y = 30.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Saffron.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            
            // Glassmorphism overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Premium Icon with glow
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape, spotColor = HeroCardAccent)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(HeroCardAccent, Color(0xFFD97706))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Groups,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                        Column {
                            Text(
                            "All Students",
                            style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.3.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                            Text(
                            "$classCount Classes",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            if (totalDues > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = HeroCardAccent
                                ) {
                                    Text(
                                        totalDues.toRupees(),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$animatedActiveCount",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        if (inactiveStudents > 0) "Active" else "Students",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    if (inactiveStudents > 0) {
                        Text(
                            "($inactiveStudents inactive)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ==================== INACTIVE STUDENTS CARD ====================

@Composable
private fun InactiveStudentsCard(
    inactiveCount: Int,
    onClick: () -> Unit
) {
    val animatedCount by animateIntAsState(
        targetValue = inactiveCount,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "inactive"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF475569),
                            Color(0xFF334155),
                            Color(0xFF1E293B)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Subtle decorative element
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 40.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF64748B).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF64748B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PersonOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    
                    Spacer(Modifier.width(14.dp))
                    
                    Column {
                            Text(
                            "Inactive Students",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        Text(
                            "View all deactivated students",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$animatedCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "students",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// ==================== DRIBBBLE CLASS CARD (Color-coded) ====================

data class SectionSummary(
    val sectionName: String,
    val studentCount: Int,
    val totalDues: Double
)

// Class color palette data class
private data class ClassColorPalette(
    val gradientStart: Color,
    val gradientEnd: Color,
    val accent: Color,
    val icon: ImageVector
)

// Get color palette based on class - flexible matching for various formats
@Composable
private fun getClassColorPalette(className: String): ClassColorPalette {
    val normalizedName = className.uppercase().trim()
    
    // Extract numeric part if present (handles "1", "1st", "Class 1", "I", etc.)
    val classNumber = normalizedName
        .replace(Regex("[^0-9]"), "")
        .toIntOrNull()
    
    return when {
        // Nursery variations
        normalizedName.contains("NC") || 
        normalizedName.contains("NURSERY") || 
        normalizedName.contains("NUR") -> ClassColorPalette(
            gradientStart = NurseryGradientStart,
            gradientEnd = NurseryGradientEnd,
            accent = NurseryAccent,
            icon = Icons.Default.ChildCare
        )
        
        // LKG variations
        normalizedName.contains("LKG") || 
        normalizedName.contains("LK") ||
        normalizedName.contains("LOWER KG") -> ClassColorPalette(
            gradientStart = PrePrimaryGradientStart,
            gradientEnd = PrePrimaryGradientEnd,
            accent = PrePrimaryAccent,
            icon = Icons.Default.Draw
        )
        
        // UKG variations
        normalizedName.contains("UKG") || 
        normalizedName.contains("UK") ||
        normalizedName.contains("UPPER KG") -> ClassColorPalette(
            gradientStart = Color(0xFFFFAB40),
            gradientEnd = Color(0xFFFF9100),
            accent = Color(0xFFFFE0B2),
            icon = Icons.Default.AutoStories
        )
        
        // Class 1-3: Fresh Teal/Mint
        classNumber in 1..3 -> ClassColorPalette(
            gradientStart = JuniorGradientStart,
            gradientEnd = JuniorGradientEnd,
            accent = JuniorAccent,
            icon = Icons.Default.MenuBook
        )
        
        // Class 4-6: Royal Purple
        classNumber in 4..6 -> ClassColorPalette(
            gradientStart = MiddleGradientStart,
            gradientEnd = MiddleGradientEnd,
            accent = MiddleAccent,
            icon = Icons.Default.Science
        )
        
        // Class 7-10: Deep Indigo
        classNumber in 7..10 -> ClassColorPalette(
            gradientStart = SeniorGradientStart,
            gradientEnd = SeniorGradientEnd,
            accent = SeniorAccent,
            icon = Icons.Default.Psychology
        )
        
        // Class 11-12: Elegant Slate
        classNumber in 11..12 -> ClassColorPalette(
            gradientStart = HigherGradientStart,
            gradientEnd = HigherGradientEnd,
            accent = HigherAccent,
            icon = Icons.Default.School
        )
        
        // Default fallback - Warm Saffron
        else -> ClassColorPalette(
            gradientStart = Saffron,
            gradientEnd = SaffronDark,
            accent = SaffronLight,
            icon = Icons.Default.Backpack
        )
    }
}

@Composable
private fun DribbbleClassCard(
    summary: ClassSummary,
    index: Int,
    onClick: () -> Unit
) {
    val palette = getClassColorPalette(summary.className)
    
    // Animated student count
    val animatedCount by animateIntAsState(
        targetValue = summary.totalStudents,
        animationSpec = tween(800 + (index * 100), easing = FastOutSlowInEasing),
        label = "count_${summary.className}"
    )
    
    // Press animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 8.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "elevation"
    )
    
    Card(
                                modifier = Modifier
            .fillMaxWidth()
            .height(155.dp)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(20.dp),
                spotColor = palette.gradientStart.copy(alpha = 0.3f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(palette.gradientStart, palette.gradientEnd),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Background decorative elements
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-25).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-20).dp, y = 20.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            
            // Floating icon in background
            Icon(
                imageVector = palette.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.12f),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section - Class name and sections
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Small icon badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = palette.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                    Text(
                        text = when (summary.className.uppercase()) {
                            "NC", "NURSERY" -> "Nursery"
                            "LKG" -> "LKG"
                            "UKG" -> "UKG"
                            else -> "Class ${summary.className}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.3.sp
                    )
                    }
                    
                    // Section badges
                    if (summary.sections.size > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            summary.sections.take(3).forEach { section ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        section.sectionName,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                            if (summary.sections.size > 3) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "+${summary.sections.size - 3}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Bottom section - Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "$animatedCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            "Students",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Dues/Paid badge
                            Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (summary.totalDues > 0) 
                            Color.White.copy(alpha = 0.2f) 
                        else 
                            Color.White.copy(alpha = 0.3f),
                        border = BorderStroke(
                            1.dp, 
                            Color.White.copy(alpha = if (summary.totalDues > 0) 0.3f else 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (summary.totalDues <= 0) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = if (summary.totalDues > 0) 
                                    summary.totalDues.toRupees() 
                                else 
                                    "All Paid",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Premium Section Selection Sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionSelectionSheet(
    className: String,
    sections: List<SectionSummary>,
    onSectionClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = getClassColorPalette(className)
    
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Cream,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = palette.gradientStart.copy(alpha = 0.4f)
            ) {}
        }
    ) {
        Column(
                                modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header with class icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.gradientStart.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = palette.icon,
                            contentDescription = null,
                            tint = palette.gradientStart,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
            Text(
                        "Class $className",
                        style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "Select a section",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            // Section cards with staggered animation
            sections.forEachIndexed { index, section ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(index * 80L)
                    visible = true
                }
                
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(200)) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(200)
                    )
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val cardScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.97f else 1f,
                        label = "sectionScale"
                    )
                    
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .scale(cardScale)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onSectionClick(section.sectionName) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                // Section badge
                                Surface(
                                    shape = CircleShape,
                                    color = palette.gradientStart,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    section.sectionName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                            color = Color.White
                                )
                            }
                                }
                                Spacer(Modifier.width(14.dp))
                        Column {
                                Text(
                                    "Section ${section.sectionName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                )
                                Text(
                                    "${section.studentCount} students",
                                    style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            
                            // Dues badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (section.totalDues > 0)
                                    DueChipBackground
                                else
                                    PaidChipBackground
                            ) {
                            Text(
                                    text = if (section.totalDues > 0)
                                        section.totalDues.toRupees()
                                    else
                                        "✓ Paid",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                    color = if (section.totalDues > 0)
                                        DueChipText
                                    else
                                        PaidChipText
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ==================== OLD CLASS CARD (kept for reference) ====================

data class ClassSummary(
    val className: String,
    val totalStudents: Int,
    val totalDues: Double,
    val sections: List<SectionSummary>
)

@Composable
private fun ClassCard(
    summary: ClassSummary,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSectionClick: (String) -> Unit,
    onClassClick: () -> Unit
) {
    val rotationAngle by animateFloatAsState(if (isExpanded) 180f else 0f, label = "expand")
    val hasSections = summary.sections.size > 1
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { 
                if (hasSections) onToggleExpand() else onClassClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Main row
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Class icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Saffron.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (summary.className) {
                            "NC", "Nursery" -> "NC"
                            "LKG" -> "LK"
                            "UKG" -> "UK"
                            else -> summary.className.filter { it.isDigit() }.take(2).ifEmpty { summary.className.take(2) }
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Saffron
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(Modifier.weight(1f)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Class ${summary.className}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${summary.totalStudents} students",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Section badges
                        if (hasSections) {
                            summary.sections.forEach { section ->
                            Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        "${section.sectionName}(${section.studentCount})",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        // Dues
                        if (summary.totalDues > 0) {
                            Text(
                                summary.totalDues.toRupees(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("All Paid", style = MaterialTheme.typography.labelSmall, color = PaidChipText)
                        }
                    }
                }
                
                if (hasSections) {
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.size(20.dp).rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "View",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Expanded sections
            AnimatedVisibility(visible = isExpanded && hasSections) {
        Column(
                    modifier = Modifier.padding(start = 72.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    summary.sections.forEach { section ->
                        SectionRow(
                            section = section,
                            onClick = { onSectionClick(section.sectionName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionRow(section: SectionSummary, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Section ${section.sectionName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${section.studentCount} students",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            if (section.totalDues > 0) {
                Text(
                    section.totalDues.toRupees(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== STUDENT LIST BY CLASS SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun StudentListByClassScreen(
    className: String,
    section: String,
    navController: NavController,
    viewModel: StudentListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val isAlphabeticalSort = state.sort == StudentSort.NAME_ASC
    
    LaunchedEffect(className, section) {
        viewModel.loadStudents(className, section)
    }
    
    val isAllStudents = className == "ALL" && section == "ALL"
    val isInactiveView = className == "INACTIVE"
    
    // Get class-specific palette (null for ALL and INACTIVE views)
    val palette = if (!isAllStudents && !isInactiveView) getClassColorPalette(className) else null
    
    // Class filter for inactive view
    var selectedClassFilter by remember { mutableStateOf("ALL") }
    val availableClasses = remember(state.students) {
        listOf("ALL") + state.students.map { it.student.currentClass }.distinct().sorted()
    }
    
    // Compute displayed count for header (accounts for class filter in inactive view)
    val displayedCount = remember(state.filteredStudents, selectedClassFilter, isInactiveView) {
        if (isInactiveView && selectedClassFilter != "ALL") {
            state.filteredStudents.count { it.student.currentClass == selectedClassFilter }
        } else {
            state.filteredStudents.size
        }
    }
    
    // Handle back press
    BackHandler(enabled = state.searchQuery.isNotEmpty()) {
        viewModel.onSearchQueryChange("")
    }
    
    Scaffold(
        containerColor = Cream,
        topBar = {
            // Premium color-coded header
            Box(
            modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        // Gradient background
                        drawRect(
                            brush = when {
                                isInactiveView -> Brush.linearGradient(
                                    colors = listOf(Color(0xFF475569), Color(0xFF1E293B))
                                )
                                isAllStudents -> Brush.linearGradient(
                                    colors = listOf(HeroCardGradientStart, HeroCardGradientEnd)
                                )
                                else -> Brush.linearGradient(
                                    colors = listOf(
                                        palette!!.gradientStart,
                                        palette.gradientEnd
                                    )
                                )
                            }
                        )
                        
                        // Decorative circles
                        drawCircle(
                            color = Color.White.copy(alpha = 0.1f),
                            radius = 100.dp.toPx(),
                            center = Offset(size.width + 20.dp.toPx(), 30.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.06f),
                            radius = 60.dp.toPx(),
                            center = Offset(-20.dp.toPx(), size.height - 10.dp.toPx())
                        )
                    }
                    .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            "Back", 
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    
                    // Class icon for non-all views (including inactive view)
                    if (isInactiveView) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonOff,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                    } else if (!isAllStudents && palette != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = palette.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    
                    Column(Modifier.weight(1f)) {
                        Text(
                            when {
                                isInactiveView -> "Inactive Students"
                                isAllStudents -> "All Students"
                                else -> "Class $className - $section"
                            }, 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold, 
                            color = Color.White,
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            when {
                                isInactiveView && selectedClassFilter != "ALL" -> 
                                    "$displayedCount inactive in Class $selectedClassFilter"
                                isInactiveView -> "$displayedCount inactive students"
                                state.inactiveCount > 0 -> "${state.filteredStudents.size} students (${state.inactiveCount} inactive)"
                                else -> "${state.filteredStudents.size} students"
                            }, 
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Premium "New" button (hidden for ALL and INACTIVE views)
                    if (!isAllStudents && !isInactiveView) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .height(36.dp)
                                .clickable { 
                                    navController.navigate(Screen.AddEditStudent.createRoute()) 
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add, 
                                    contentDescription = null, 
                                    tint = palette?.gradientStart ?: Saffron, 
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "New",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = palette?.gradientStart ?: Saffron
                                )
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
            // Apply class filter for inactive view - defined at Box level for alphabet rail access
            val displayedStudents = remember(state.filteredStudents, selectedClassFilter, isInactiveView) {
                if (isInactiveView && selectedClassFilter != "ALL") {
                    state.filteredStudents.filter { it.student.currentClass == selectedClassFilter }
                } else {
                    state.filteredStudents
                }
            }
            
            val displayedGrouped = remember(state.groupedStudents, selectedClassFilter, isInactiveView) {
                if (isInactiveView && selectedClassFilter != "ALL") {
                    state.groupedStudents.mapValues { (_, students) ->
                        students.filter { it.student.currentClass == selectedClassFilter }
                    }.filterValues { it.isNotEmpty() }
                } else {
                    state.groupedStudents
                }
            }
            
            // Compute available letters for alphabet rail based on displayed data
            val displayedLetters = remember(displayedGrouped) {
                displayedGrouped.keys.filter { it.isLetter() }.toList()
            }
            
            // Function to get index in the displayed list
            fun getDisplayedIndexForLetter(letter: Char): Int {
                var index = 0
                for ((key, students) in displayedGrouped) {
                    if (key == letter) return index
                    index += 1 + students.size // +1 for the header
                }
                return -1
            }
            
            Box(Modifier.fillMaxSize().padding(paddingValues)) {
                Column(Modifier.fillMaxSize()) {
                    // Search bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                        placeholder = "Search by name, father's name...",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            
                    // Modern Filter chips row with horizontal scroll
                    if (isInactiveView) {
                        // Class filter dropdown for inactive view
                        var classDropdownExpanded by remember { mutableStateOf(false) }
                        val slateColor = Color(0xFF64748B)
                        
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Class dropdown styled like filter chip
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (selectedClassFilter != "ALL") 
                                        slateColor.copy(alpha = 0.12f)
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = if (selectedClassFilter != "ALL") 
                                        BorderStroke(1.dp, slateColor.copy(alpha = 0.5f)) 
                                    else null,
                                    modifier = Modifier.clickable { classDropdownExpanded = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.School,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (selectedClassFilter != "ALL") 
                                                slateColor
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            if (selectedClassFilter == "ALL") "All Classes" else "Class $selectedClassFilter",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selectedClassFilter != "ALL") FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (selectedClassFilter != "ALL") 
                                                slateColor
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (selectedClassFilter != "ALL") 
                                                slateColor
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                DropdownMenu(
                                    expanded = classDropdownExpanded,
                                    onDismissRequest = { classDropdownExpanded = false }
                                ) {
                                    availableClasses.forEach { classOption ->
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    if (classOption == "ALL") "All Classes" else "Class $classOption",
                                                    fontWeight = if (selectedClassFilter == classOption) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (selectedClassFilter == classOption) slateColor else MaterialTheme.colorScheme.onSurface
                                                ) 
                                            },
                                            onClick = {
                                                selectedClassFilter = classOption
                                                classDropdownExpanded = false
                                            },
                                            leadingIcon = {
                                                if (selectedClassFilter == classOption) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color(0xFF64748B)
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            
                            Spacer(Modifier.weight(1f))
                            
                            // Cycling Sort Chip
                            SortChip(
                                currentSort = state.sort,
                                onClick = { 
                                    viewModel.cycleSort()
                                    scope.launch {
                                        listState.scrollToItem(0)
                                    }
                                }
                            )
                            
                            // Inactive count indicator
                            Text(
                                "$displayedCount students",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModernFilterChip(
                                text = "All",
                                selected = state.filter == StudentFilter.ALL,
                                onClick = { viewModel.setFilter(StudentFilter.ALL) }
                            )
                            ModernFilterChip(
                                text = "Dues",
                                selected = state.filter == StudentFilter.WITH_DUES,
                                onClick = { viewModel.setFilter(StudentFilter.WITH_DUES) },
                                selectedColor = MaterialTheme.colorScheme.error
                            )
                            ModernFilterChip(
                                text = "Paid",
                                selected = state.filter == StudentFilter.NO_DUES,
                                onClick = { viewModel.setFilter(StudentFilter.NO_DUES) },
                                selectedColor = PaidChipText
                            )
                            ModernFilterChip(
                                text = "Bus",
                                selected = state.filter == StudentFilter.TRANSPORT,
                                onClick = { viewModel.setFilter(StudentFilter.TRANSPORT) },
                                icon = Icons.Default.DirectionsBus
                            )
                        
                            // Cycling Sort Chip
                        SortChip(
                            currentSort = state.sort,
                            onClick = { 
                                viewModel.cycleSort()
                                scope.launch {
                                    listState.scrollToItem(0)
                                }
                            }
                        )
                            
                            // Inactive filter - at the end
                            ModernFilterChip(
                                text = "Inactive",
                                selected = state.filter == StudentFilter.INACTIVE,
                                onClick = { 
                                    // Toggle behavior - click again to deselect
                                    if (state.filter == StudentFilter.INACTIVE) {
                                        viewModel.setFilter(StudentFilter.ALL)
                                    } else {
                                        viewModel.setFilter(StudentFilter.INACTIVE)
                                    }
                                },
                                icon = Icons.Default.PersonOff,
                                selectedColor = Color(0xFF64748B)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Student list
                    if (displayedStudents.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Group,
                            title = "No Students", 
                            subtitle = "Try adjusting filters"
                )
            } else {
                LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp, 
                                end = if (isAlphabeticalSort) 44.dp else 16.dp, 
                                top = 4.dp,
                                bottom = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isAlphabeticalSort) {
                                // Grouped by alphabet with headers
                                displayedGrouped.forEach { (letter, students) ->
                                    // Non-sticky alphabet header (fixes the overlap issue)
                                    item(key = "header_$letter") {
                                        Text(
                                            letter.toString(),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isInactiveView) Color(0xFF64748B) else Saffron,
                                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                        )
                                    }
                                    
                                    // Students under this letter
                                    items(students, key = { it.student.id }) { student ->
                                        PremiumStudentCard(
                                            studentWithBalance = student,
                            onClick = {
                                                navController.navigate(Screen.StudentDetail.createRoute(student.student.id)) 
                                            },
                                            onCollectClick = { 
                                                navController.navigate(Screen.CollectFee.createRoute(student.student.id)) 
                                            },
                                            showClass = isAllStudents || isInactiveView || state.searchQuery.isNotBlank(),
                                            hideInactiveBadge = true  // Header already shows inactive count
                                        )
                                    }
                                }
                            } else {
                                // Flat list without alphabet headers for other sorts
                                items(displayedStudents, key = { it.student.id }) { student ->
                                    PremiumStudentCard(
                                        studentWithBalance = student,
                                        onClick = { 
                                            navController.navigate(Screen.StudentDetail.createRoute(student.student.id)) 
                                        },
                                        onCollectClick = { 
                                            navController.navigate(Screen.CollectFee.createRoute(student.student.id)) 
                                        },
                                        showClass = isAllStudents || isInactiveView || state.searchQuery.isNotBlank(),
                                        hideInactiveBadge = true  // Header already shows inactive count
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Alphabet Rail - smooth animated visibility for alphabetical sorting
                AnimatedVisibility(
                    visible = isAlphabeticalSort && displayedStudents.isNotEmpty() && displayedLetters.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(200)) + slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(200)
                    ),
                    exit = fadeOut(animationSpec = tween(150)) + slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(150)
                    ),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    ContactsAlphabetRail(
                        letters = displayedLetters,
                        onLetterSelected = { letter ->
                            scope.launch {
                                val index = getDisplayedIndexForLetter(letter)
                                if (index >= 0) {
                                    listState.animateScrollToItem(index)
                                }
                            }
                        },
                        modifier = Modifier.padding(top = 130.dp, bottom = 16.dp)
                    )
                }
            }
        }
    }
}

// ==================== SORT CHIP ====================

@Composable
private fun SortChip(
    currentSort: StudentSort,
    onClick: () -> Unit
) {
    val sortText = when (currentSort) {
        StudentSort.NAME_ASC -> "Name"
        StudentSort.DUES_HIGH -> "₹ High"
        StudentSort.DUES_LOW -> "₹ Low"
    }
    
    val isNonDefault = currentSort != StudentSort.NAME_ASC
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isNonDefault) Saffron.copy(alpha = 0.15f) 
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = if (isNonDefault) 
            androidx.compose.foundation.BorderStroke(1.dp, Saffron.copy(alpha = 0.5f)) else null,
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

// ==================== MODERN FILTER CHIP ====================

@Composable
private fun ModernFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = Saffron,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) selectedColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, selectedColor.copy(alpha = 0.5f)) else null,
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

// ==================== CONTACTS-STYLE DRAGGABLE ALPHABET RAIL ====================

@Composable
private fun ContactsAlphabetRail(
    letters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var railHeight by remember { mutableStateOf(0f) }
    
    Box(modifier = modifier.fillMaxHeight()) {
        // Letter preview bubble (shows when dragging)
        AnimatedVisibility(
            visible = isDragging && selectedLetter != null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-48).dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Saffron,
                shadowElevation = 8.dp,
                modifier = Modifier.size(52.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedLetter?.toString() ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        
        // The alphabet rail - spans full height like contacts app with premium styling
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(end = 6.dp, top = 4.dp, bottom = 4.dp)
                .width(22.dp),
            shape = RoundedCornerShape(11.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp, vertical = 6.dp)
                    .pointerInput(letters) {
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                railHeight = size.height.toFloat()
                                if (letters.isNotEmpty() && railHeight > 0) {
                                    val index = ((offset.y / railHeight) * letters.size)
                                        .toInt()
                                        .coerceIn(0, letters.size - 1)
                                    selectedLetter = letters[index]
                                    onLetterSelected(letters[index])
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                            },
                            onDragCancel = {
                                isDragging = false
                            },
                            onVerticalDrag = { change, _ ->
                                change.consume()
                                if (letters.isNotEmpty() && railHeight > 0) {
                                    val index = ((change.position.y / railHeight) * letters.size)
                                        .toInt()
                                        .coerceIn(0, letters.size - 1)
                                    if (selectedLetter != letters[index]) {
                                        selectedLetter = letters[index]
                                        onLetterSelected(letters[index])
                                    }
                                }
                            }
                        )
                    }
                    .pointerInput(letters) {
                        detectTapGestures { offset ->
                            railHeight = size.height.toFloat()
                            if (letters.isNotEmpty() && railHeight > 0) {
                                val index = ((offset.y / railHeight) * letters.size)
                                    .toInt()
                                    .coerceIn(0, letters.size - 1)
                                onLetterSelected(letters[index])
                            }
                        }
                    },
                // SpaceBetween ensures letters are evenly distributed across full height
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                letters.forEach { letter ->
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDragging && selectedLetter == letter) 
                            Saffron else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==================== PREMIUM STUDENT CARD (with press animation) ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PremiumStudentCard(
    studentWithBalance: StudentWithBalance,
    onClick: () -> Unit,
    onCollectClick: () -> Unit,
    showClass: Boolean = false,
    hideInactiveBadge: Boolean = false  // Hide badge when in inactive-only view
) {
    val student = studentWithBalance.student
    val balance = studentWithBalance.currentBalance
    val hasAdvance = balance < 0
    val hasDues = balance > 0
    val isInactive = !student.isActive
    
    // Press animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "studentCardScale"
    )
    
    // Get avatar color based on status
    val avatarColors = when {
        isInactive -> listOf(
            Color.Gray.copy(alpha = 0.15f),
            Color.Gray.copy(alpha = 0.08f)
        )
        hasDues -> listOf(
            ErrorRed.copy(alpha = 0.15f),
            ErrorRed.copy(alpha = 0.08f)
        )
        hasAdvance -> listOf(
            PaidChipText.copy(alpha = 0.15f),
            PaidChipText.copy(alpha = 0.08f)
        )
        else -> listOf(
            Saffron.copy(alpha = 0.12f),
            Saffron.copy(alpha = 0.06f)
        )
    }
    
    val avatarTextColor = when {
        isInactive -> Color.Gray
        hasDues -> ErrorRed
        hasAdvance -> PaidChipText
        else -> Saffron
    }
    
    // Card opacity for inactive students
    val cardAlpha = if (isInactive) 0.7f else 1f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(cardAlpha)
            .shadow(
                elevation = if (isPressed) 2.dp else 4.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (hasDues && !isInactive) ErrorRed.copy(alpha = 0.1f) else ShadowWarm
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Premium Avatar with gradient
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(avatarColors)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    student.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = avatarTextColor
                )
            }
            
            Spacer(Modifier.width(14.dp))
            
            // Student Info
            Column(Modifier.weight(1f)) {
                Text(
                    student.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(2.dp))
                
                Text(
                    "S/o ${student.fatherName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(4.dp))
                
                // Class and Account number row - using FlowRow for better wrapping
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (showClass) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Saffron.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "${student.currentClass}-${student.section}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Saffron,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SaffronContainerLight
                    ) {
                        Text(
                            "A/C: ${student.accountNumber}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Inactive badge (hidden in inactive-only view since all are inactive)
                    if (isInactive && !hideInactiveBadge) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ErrorRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "INACTIVE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.width(8.dp))
            
            // Balance Badge - Premium Style (shown for all students including inactive)
                Surface(
                shape = RoundedCornerShape(12.dp),
                    color = when {
                    isInactive && hasDues -> Color.Gray.copy(alpha = 0.15f)
                    isInactive -> Color.Gray.copy(alpha = 0.1f)
                    hasDues -> DueChipBackground
                    hasAdvance -> AdvanceChipBackground
                        else -> PaidChipBackground
                    }
                ) {
                    Text(
                        text = when {
                            hasDues -> balance.toRupees() + " Due"
                        hasAdvance -> (balance * -1).toRupees() + " Adv"
                        else -> "✓ Paid"
                        },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                        isInactive && hasDues -> Color.Gray
                        isInactive -> Color.Gray.copy(alpha = 0.7f)
                        hasDues -> DueChipText
                        hasAdvance -> AdvanceChipText
                            else -> PaidChipText
                        }
                    )
            }
        }
    }
}

// ==================== COMPACT STUDENT CARD (for search results) ====================

@Composable
fun StudentCardCompact(
    studentWithBalance: StudentWithBalance,
    onClick: () -> Unit,
    onCollectClick: (() -> Unit)? = null,
    showClass: Boolean = false,
    hideInactiveBadge: Boolean = false
) {
    val student = studentWithBalance.student
    val balance = studentWithBalance.currentBalance
    val hasDues = balance > 0
    val hasAdvance = balance < 0
    val isInactive = !student.isActive
    
    // Press animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "compactScale"
    )
    
    // Card opacity for inactive students
    val cardAlpha = if (isInactive) 0.7f else 1f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(cardAlpha)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isInactive -> Color.Gray.copy(alpha = 0.1f)
                            hasDues -> ErrorRed.copy(alpha = 0.1f)
                            hasAdvance -> PaidChipText.copy(alpha = 0.1f)
                            else -> Saffron.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    student.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isInactive -> Color.Gray
                        hasDues -> ErrorRed
                        hasAdvance -> PaidChipText
                        else -> Saffron
                    }
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Info
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        student.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (showClass) {
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = Saffron.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "${student.currentClass}-${student.section}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Saffron,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    // Inactive badge (hidden in inactive-only view)
                    if (isInactive && !hideInactiveBadge) {
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = ErrorRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "INACTIVE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    "S/o ${student.fatherName} • A/C: ${student.accountNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(Modifier.width(8.dp))
            
            // Balance badge (shown for all students including inactive)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = when {
                    isInactive && hasDues -> Color.Gray.copy(alpha = 0.15f)
                    isInactive -> Color.Gray.copy(alpha = 0.1f)
                    hasDues -> DueChipBackground
                    hasAdvance -> AdvanceChipBackground
                    else -> PaidChipBackground
                }
            ) {
                Text(
                    text = when {
                        hasDues -> balance.toRupees()
                        hasAdvance -> (balance * -1).toRupees()
                        else -> "✓ Paid"
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        isInactive && hasDues -> Color.Gray
                        isInactive -> Color.Gray.copy(alpha = 0.7f)
                        hasDues -> DueChipText
                        hasAdvance -> AdvanceChipText
                        else -> PaidChipText
                    }
                )
            }
        }
    }
}
