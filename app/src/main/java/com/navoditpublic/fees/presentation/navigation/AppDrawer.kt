package com.navoditpublic.fees.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Duotone
import com.navoditpublic.fees.presentation.screens.settings.SettingsViewModel
import com.navoditpublic.fees.presentation.theme.GlassBorder
import com.navoditpublic.fees.presentation.theme.GlassWhite
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronAmber
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.presentation.theme.SaffronDeep
import kotlinx.coroutines.launch

// Drawer category accent colors
private val SchoolAccent = Color(0xFF00897B)      // Teal
private val AcademicAccent = Color(0xFF7C3AED)    // Purple
private val FeesAccent = Color(0xFFFF6F00)        // Saffron
private val HistoryAccent = Color(0xFF5C6BC0)     // Indigo
private val HelpAccent = Color(0xFF607D8B)        // Blue Grey
private val DevAccent = Color(0xFFEF5350)         // Red

@Composable
fun AppDrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    currentSession: String?,
    currentRoute: String? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var showSeedConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showRefreshConfirm by remember { mutableStateOf(false) }
    
    // Animation state for staggered entry
    val visibleState = remember { MutableTransitionState(false) }
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            visibleState.targetState = true
        } else {
            visibleState.targetState = false
        }
    }
    
    // Dialogs
    if (showSeedConfirm) {
        StyledAlertDialog(
            onDismiss = { showSeedConfirm = false },
            title = "Load Demo Data?",
            text = "This will add 100+ demo students, fee structures, transport routes, and sample receipts for testing. You can remove them later.",
            confirmText = "Load Demo Data",
            onConfirm = {
                viewModel.seedDemoData()
                showSeedConfirm = false
            }
        )
    }
    
    if (showClearConfirm) {
        StyledAlertDialog(
            onDismiss = { showClearConfirm = false },
            title = "Clear Demo Data?",
            text = "This will remove all demo students and their associated receipts and ledger entries. Real data will not be affected.",
            confirmText = "Clear Demo Data",
            isDestructive = true,
            onConfirm = {
                viewModel.clearDemoData()
                showClearConfirm = false
            }
        )
    }
    
    if (showRefreshConfirm) {
        StyledAlertDialog(
            onDismiss = { showRefreshConfirm = false },
            title = "Refresh Demo Data?",
            text = "This will clear existing demo data and create fresh data with:\n\n• 150 students across all classes\n• 200+ receipts (including today's)\n• Payment modes (Cash & Online)\n• Opening balances for aging reports\n• Transport enrollments\n\nAll collection reports will show data.",
            confirmText = "Refresh Demo Data",
            onConfirm = {
                viewModel.refreshDemoData()
                showRefreshConfirm = false
            }
        )
    }
    
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // ==================== HEADER ====================
            DrawerHeader(currentSession = currentSession, visibleState = visibleState)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ==================== QUICK ACTIONS ====================
            AnimatedDrawerSection(visibleState = visibleState, delayIndex = 0) {
                QuickActionsBar(
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ==================== SCHOOL SECTION ====================
            AnimatedDrawerSection(visibleState = visibleState, delayIndex = 1) {
                DrawerCategoryCard(
                    title = "SCHOOL",
                    accentColor = SchoolAccent,
                    items = listOf(
                        DrawerItemData(
                            icon = PhosphorIcons.Duotone.Bank,
                            title = "School Profile",
                            subtitle = "Name, address, contact details",
                            route = Screen.SchoolProfile.route
                        )
                    ),
                    currentRoute = currentRoute,
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ==================== ACADEMIC SECTION ====================
            AnimatedDrawerSection(visibleState = visibleState, delayIndex = 2) {
                DrawerCategoryCard(
                    title = "ACADEMIC",
                    accentColor = AcademicAccent,
                    items = listOf(
                        DrawerItemData(
                            icon = PhosphorIcons.Duotone.CalendarDots,
                            title = "Academic Sessions",
                            subtitle = "Manage academic years",
                            route = Screen.AcademicSessions.route
                        ),
                        DrawerItemData(
                            icon = PhosphorIcons.Duotone.Books,
                            title = "Classes & Sections",
                            subtitle = "Add or modify classes and sections",
                            route = Screen.ClassesAndSections.route
                        )
                    ),
                    currentRoute = currentRoute,
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ==================== FEES SECTION ====================
            AnimatedDrawerSection(visibleState = visibleState, delayIndex = 3) {
                DrawerCategoryCard(
                    title = "FEES",
                    accentColor = FeesAccent,
                    items = listOf(
                        DrawerItemData(
                            icon = PhosphorIcons.Duotone.CurrencyInr,
                            title = "Fee Structure",
                            subtitle = "Monthly, annual, admission fees",
                            route = Screen.FeeStructure.route
                        ),
                        DrawerItemData(
                            icon = PhosphorIcons.Duotone.Bus,
                            title = "Transport Routes",
                            subtitle = "Manage transport routes and fees",
                            route = Screen.TransportRoutes.route
                        )
                    ),
                    currentRoute = currentRoute,
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ==================== HISTORY SECTION ====================
            AnimatedDrawerSection(visibleState = visibleState, delayIndex = 4) {
                DrawerCategoryCard(
                    title = "HISTORY",
                    accentColor = HistoryAccent,
                    items = listOf(
                        DrawerItemData(
                            icon = PhosphorIcons.Duotone.ClockCounterClockwise,
                            title = "Audit Log",
                            subtitle = "View all changes and revert if needed",
                            route = Screen.AuditLog.route
                        )
                    ),
                    currentRoute = currentRoute,
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ==================== HELP & INFO SECTION ====================
            AnimatedDrawerSection(visibleState = visibleState, delayIndex = 5) {
                DrawerCategoryCard(
                    title = "HELP & INFO",
                    accentColor = HelpAccent,
                    items = listOf(
                        DrawerItemData(
                            icon = PhosphorIcons.Duotone.Lifebuoy,
                            title = "How to Use",
                            subtitle = "Complete guide to using the app",
                            route = Screen.HowToUse.route
                        ),
                        DrawerItemData(
                            icon = PhosphorIcons.Duotone.Info,
                            title = "About",
                            subtitle = "App version and developer info",
                            route = Screen.About.route
                        )
                    ),
                    currentRoute = currentRoute,
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ==================== DEVELOPER SECTION ====================
            AnimatedDrawerSection(visibleState = visibleState, delayIndex = 6) {
                DrawerDevSection(
                    hasDemoData = state.hasDemoData,
                    onLoadDemo = { showSeedConfirm = true },
                    onRefreshDemo = { showRefreshConfirm = true },
                    onClearDemo = { showClearConfirm = true }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ==================== FOOTER ====================
            AnimatedDrawerSection(visibleState = visibleState, delayIndex = 7) {
                DrawerFooter()
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==================== HEADER COMPONENT ====================
@Composable
private fun DrawerHeader(
    currentSession: String?,
    visibleState: MutableTransitionState<Boolean>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .drawBehind {
                // Decorative circles
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = 140.dp.toPx(),
                    center = Offset(size.width * 0.85f, size.height * 0.15f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = 100.dp.toPx(),
                    center = Offset(size.width * 0.1f, size.height * 0.85f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = 60.dp.toPx(),
                    center = Offset(size.width * 0.5f, size.height * 0.3f)
                )
            }
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SaffronDeep, Saffron, SaffronAmber),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Content
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(tween(400, delayMillis = 100)) + 
                    slideInHorizontally(tween(400, delayMillis = 100, easing = EaseOutBack)) { -50 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Logo/Icon with glow effect
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(60.dp)
                ) {
                    // Glow behind
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .blur(12.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    // Icon container
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Duotone.GraduationCap,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // School name
                Text(
                    text = "NPIC Fees",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Session badge - glassmorphism style
                if (currentSession != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = GlassWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier
                    ) {
                        Text(
                            text = "Session $currentSession",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==================== QUICK ACTIONS BAR ====================
@Composable
private fun QuickActionsBar(
    navController: NavController,
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = PhosphorIcons.Duotone.MagnifyingGlass,
            label = "Search",
            onClick = {
                scope.launch {
                    drawerState.close()
                    navController.navigate(Screen.Students.route)
                }
            }
        )
        QuickActionButton(
            icon = PhosphorIcons.Duotone.ChartBar,
            label = "Reports",
            onClick = {
                scope.launch {
                    drawerState.close()
                    navController.navigate(Screen.Reports.route)
                }
            }
        )
        QuickActionButton(
            icon = Icons.Default.Add,
            label = "Collect",
            isPrimary = true,
            onClick = {
                scope.launch {
                    drawerState.close()
                    navController.navigate(Screen.FeeCollection.route)
                }
            }
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isPrimary) Saffron else MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = if (isPrimary) 4.dp else 0.dp,
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isPrimary) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== CATEGORY CARD ====================
data class DrawerItemData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val route: String
)

@Composable
private fun DrawerCategoryCard(
    title: String,
    accentColor: Color,
    items: List<DrawerItemData>,
    currentRoute: String?,
    navController: NavController,
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        // Section header with colored indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        // Grouped card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.2f),
                            accentColor.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    DrawerMenuItem(
                        icon = item.icon,
                        title = item.title,
                        subtitle = item.subtitle,
                        accentColor = accentColor,
                        isSelected = currentRoute == item.route,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(item.route)
                            }
                        }
                    )
                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor.copy(alpha = 0.12f)
            isPressed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "bg"
    )
    
    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "iconScale"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selected indicator
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (isSelected) accentColor else Color.Transparent
                )
        )
        
        Spacer(modifier = Modifier.width(if (isSelected) 10.dp else 0.dp))
        
        // Icon container
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = accentColor.copy(alpha = if (isSelected) 0.2f else 0.1f),
            modifier = Modifier
                .size(42.dp)
                .scale(iconScale)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = accentColor
                )
            }
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )
        }
        
        // Arrow indicator
        Icon(
            imageVector = PhosphorIcons.Duotone.CaretRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp)
        )
    }
}

// ==================== DEVELOPER SECTION ====================
@Composable
private fun DrawerDevSection(
    hasDemoData: Boolean,
    onLoadDemo: () -> Unit,
    onRefreshDemo: () -> Unit,
    onClearDemo: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        // Section header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DevAccent)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "DEVELOPER",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            DevAccent.copy(alpha = 0.2f),
                            DevAccent.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column {
                if (!hasDemoData) {
                    DevMenuItem(
                        icon = PhosphorIcons.Duotone.Flask,
                        title = "Load Demo Data",
                        subtitle = "Add 150 test students + receipts for testing",
                        onClick = onLoadDemo
                    )
                } else {
                    DevMenuItem(
                        icon = PhosphorIcons.Duotone.Flask,
                        title = "Refresh Demo Data",
                        subtitle = "Clear & reload with fresh receipts",
                        onClick = onRefreshDemo
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    DevMenuItem(
                        icon = PhosphorIcons.Duotone.Trash,
                        title = "Clear Demo Data",
                        subtitle = "Remove all test data (DEMO prefix)",
                        isDestructive = true,
                        onClick = onClearDemo
                    )
                }
            }
        }
    }
}

@Composable
private fun DevMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val accentColor = if (isDestructive) Color(0xFFDC2626) else DevAccent
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = accentColor.copy(alpha = 0.1f),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = accentColor
                )
            }
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) accentColor else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )
        }
    }
}

// ==================== FOOTER ====================
@Composable
private fun DrawerFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // App version badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = "NPIC Fees v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Footer links
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Help",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.clickable { }
            )
            Text(
                text = "Privacy",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.clickable { }
            )
            Text(
                text = "Support",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.clickable { }
            )
        }
    }
}

// ==================== ANIMATED SECTION WRAPPER ====================
@Composable
private fun AnimatedDrawerSection(
    visibleState: MutableTransitionState<Boolean>,
    delayIndex: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = delayIndex * 40,
                easing = FastOutSlowInEasing
            )
        ) + slideInHorizontally(
            animationSpec = tween(
                durationMillis = 350,
                delayMillis = delayIndex * 40,
                easing = EaseInOutCubic
            ),
            initialOffsetX = { -40 }
        )
    ) {
        content()
    }
}

// ==================== STYLED DIALOG ====================
@Composable
private fun StyledAlertDialog(
    onDismiss: () -> Unit,
    title: String,
    text: String,
    confirmText: String,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) Color(0xFFDC2626) else Saffron
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = confirmText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}
