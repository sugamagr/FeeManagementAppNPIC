package com.navoditpublic.fees.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Science
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.R
import com.navoditpublic.fees.presentation.screens.settings.SettingsViewModel
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronAmber
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
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // ==================== HEADER ====================
            DrawerHeader(currentSession = currentSession)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ==================== SCHOOL SECTION ====================
            DrawerCategoryCard(
                title = "SCHOOL",
                accentColor = SchoolAccent,
                items = listOf(
                    DrawerItemData(
                        icon = Icons.Outlined.School,
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ==================== ACADEMIC SECTION ====================
            DrawerCategoryCard(
                title = "ACADEMIC",
                accentColor = AcademicAccent,
                items = listOf(
                    DrawerItemData(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Academic Sessions",
                        subtitle = "Manage academic years",
                        route = Screen.AcademicSessions.route
                    ),
                    DrawerItemData(
                        icon = Icons.Outlined.Class,
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ==================== FEES SECTION ====================
            DrawerCategoryCard(
                title = "FEES",
                accentColor = FeesAccent,
                items = listOf(
                    DrawerItemData(
                        icon = Icons.Outlined.Payments,
                        title = "Fee Structure",
                        subtitle = "Monthly, annual, admission fees",
                        route = Screen.FeeStructure.route
                    ),
                    DrawerItemData(
                        icon = Icons.Outlined.DirectionsBus,
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ==================== HISTORY SECTION ====================
            DrawerCategoryCard(
                title = "HISTORY",
                accentColor = HistoryAccent,
                items = listOf(
                    DrawerItemData(
                        icon = Icons.Outlined.History,
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ==================== HELP & INFO SECTION ====================
            DrawerCategoryCard(
                title = "HELP & INFO",
                accentColor = HelpAccent,
                items = listOf(
                    DrawerItemData(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        title = "How to Use",
                        subtitle = "Complete guide to using the app",
                        route = Screen.HowToUse.route
                    ),
                    DrawerItemData(
                        icon = Icons.Outlined.Info,
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ==================== DEVELOPER SECTION ====================
            DrawerDevSection(
                hasDemoData = state.hasDemoData,
                onLoadDemo = { showSeedConfirm = true },
                onRefreshDemo = { showRefreshConfirm = true },
                onClearDemo = { showClearConfirm = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ==================== FOOTER ====================
            DrawerFooter()
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==================== HEADER COMPONENT ====================
@Composable
private fun DrawerHeader(currentSession: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Decorative circle - top right
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = 100.dp.toPx(),
                    center = Offset(size.width * 0.95f, size.height * 0.1f)
                )
                // Decorative circle - bottom left
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = 70.dp.toPx(),
                    center = Offset(size.width * 0.05f, size.height * 0.9f)
                )
            }
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SaffronDeep, Saffron, SaffronAmber),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // School Logo
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.size(64.dp)
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.npic),
                    contentDescription = "School Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // School name
                Text(
                    text = "NPIC Fees",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.3).sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Session badge
                if (currentSession != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.95f),
                    ) {
                        Text(
                            text = currentSession,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDeep,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
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
        animationSpec = tween(100),
        label = "bg"
    )
    
    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
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
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
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
                        icon = Icons.Outlined.Science,
                        title = "Load Demo Data",
                        subtitle = "Add 150 test students + receipts for testing",
                        onClick = onLoadDemo
                    )
                } else {
                    DevMenuItem(
                        icon = Icons.Outlined.Science,
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
                        icon = Icons.Outlined.Delete,
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
