package com.navoditpublic.fees.presentation.screens.reports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.navigation.LocalDrawerState
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Color Palette
private val DuesRed = Color(0xFFE53935)
private val DuesRedLight = Color(0xFFFFEBEE)
private val CollectionGreen = Color(0xFF43A047)
private val CollectionGreenLight = Color(0xFFE8F5E9)
private val AccentPurple = Color(0xFF7C4DFF)
private val AccentBlue = Color(0xFF2196F3)
private val AccentOrange = Color(0xFFFF9800)
private val AccentTeal = Color(0xFF00BCD4)
private val WarmBackground = Color(0xFFFAF8F5)

data class ReportItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val route: String,
    val accentColor: Color,
    val previewValue: String? = null,
    val previewLabel: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()
    var animateItems by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        delay(100)
        animateItems = true
    }
    
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }
    
    val drawerState = LocalDrawerState.current
    
    Scaffold(
        containerColor = WarmBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 24.dp)
        ) {
            // Header
            item {
                ReportsHeader(
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            
            // Modern Pill Tabs
            item {
                ModernPillTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        scope.launch { pagerState.animateScrollToPage(tab) }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            
            // Content based on selected tab
            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    when (page) {
                        0 -> DuesTabContent(
                            state = state,
                            animate = animateItems,
                            navController = navController
                        )
                        1 -> CollectionTabContent(
                            state = state,
                            animate = animateItems,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsHeader(
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Saffron, SaffronDark)
                )
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 280.dp, y = 10.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(50.dp)
                .offset(x = 320.dp, y = 60.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 4.dp, end = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, "Menu", tint = Color.White)
            }
            
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Assessment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(14.dp))
            
            Column {
                Text(
                    "Reports",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "View & analyze your data",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun ModernPillTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Dues Tab
            PillTab(
                title = "Dues",
                icon = Icons.Default.AccountBalance,
                isSelected = selectedTab == 0,
                selectedColor = DuesRed,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            
            // Collection Tab
            PillTab(
                title = "Collection",
                icon = Icons.Default.MonetizationOn,
                isSelected = selectedTab == 1,
                selectedColor = CollectionGreen,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PillTab(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else Color.Transparent,
        animationSpec = tween(300),
        label = "tabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "tabContent"
    )
    
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun DuesTabContent(
    state: ReportsSummaryState,
    animate: Boolean,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Summary Card
        AnimatedVisibility(
            visible = animate,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -30 }
        ) {
            DuesHeroCard(
                totalDues = state.totalPendingDues,
                totalStudents = state.totalStudents,
                totalClasses = state.totalClasses
            )
        }
        
        // Quick Reports Grid
        AnimatedVisibility(
            visible = animate,
            enter = fadeIn(tween(400, delayMillis = 150)) + slideInVertically(tween(400, delayMillis = 150)) { 30 }
        ) {
            Column {
                Text(
                    "Quick Reports",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompactReportCard(
                        icon = Icons.Default.Warning,
                        title = "Defaulters",
                        subtitle = "Students with dues",
                        accentColor = DuesRed,
                        onClick = { navController.navigate(Screen.DefaultersReport.route) },
                        modifier = Modifier.weight(1f)
                    )
                    CompactReportCard(
                        icon = Icons.Default.Group,
                        title = "Class-wise",
                        subtitle = "By class",
                        accentColor = AccentPurple,
                        onClick = { navController.navigate(Screen.ClassWiseDuesReport.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompactReportCard(
                        icon = Icons.Default.DirectionsBus,
                        title = "Transport",
                        subtitle = "Route-wise dues",
                        accentColor = AccentOrange,
                        onClick = { navController.navigate(Screen.TransportDuesReport.route) },
                        modifier = Modifier.weight(1f)
                    )
                    CompactReportCard(
                        icon = Icons.Default.SaveAlt,
                        title = "Saved Views",
                        subtitle = "Your reports",
                        accentColor = Color(0xFF607D8B),
                        onClick = { navController.navigate(Screen.SavedDuesViews.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Custom Reports Section
        AnimatedVisibility(
            visible = animate,
            enter = fadeIn(tween(400, delayMillis = 300)) + slideInVertically(tween(400, delayMillis = 300)) { 30 }
        ) {
            CustomReportSection(
                title = "Custom Dues Report",
                subtitle = "Create personalized reports with your own filters and columns",
                accentColor = AccentOrange,
                onClick = { navController.navigate(Screen.CustomDuesReport.route) }
            )
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CollectionTabContent(
    state: ReportsSummaryState,
    animate: Boolean,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Summary Card
        AnimatedVisibility(
            visible = animate,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -30 }
        ) {
            CollectionHeroCard(
                monthCollection = state.monthCollection,
                todayCollection = state.todayCollection,
                todayReceipts = state.todayReceiptCount
            )
        }
        
        // Quick Reports Grid
        AnimatedVisibility(
            visible = animate,
            enter = fadeIn(tween(400, delayMillis = 150)) + slideInVertically(tween(400, delayMillis = 150)) { 30 }
        ) {
            Column {
                Text(
                    "Quick Reports",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompactReportCard(
                        icon = Icons.Default.CalendarToday,
                        title = "Daily",
                        subtitle = "By date",
                        accentColor = CollectionGreen,
                        previewText = if (state.todayCollection > 0) state.todayCollection.toRupees() else null,
                        onClick = { navController.navigate(Screen.DailyCollectionReport.route) },
                        modifier = Modifier.weight(1f)
                    )
                    CompactReportCard(
                        icon = Icons.Default.CalendarMonth,
                        title = "Monthly",
                        subtitle = "By month",
                        accentColor = AccentTeal,
                        onClick = { navController.navigate(Screen.MonthlyCollectionReport.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Receipt Register - Full Width
                FullWidthReportCard(
                    icon = Icons.Default.Receipt,
                    title = "Receipt Register",
                    subtitle = "Complete list of all receipts in order",
                    accentColor = AccentBlue,
                    onClick = { navController.navigate(Screen.ReceiptRegister.route) }
                )
            }
        }
        
        // Custom Reports Section
        AnimatedVisibility(
            visible = animate,
            enter = fadeIn(tween(400, delayMillis = 300)) + slideInVertically(tween(400, delayMillis = 300)) { 30 }
        ) {
            CustomReportSection(
                title = "Custom Collection Report",
                subtitle = "Create personalized reports with your own date ranges and filters",
                accentColor = AccentOrange,
                onClick = { navController.navigate(Screen.CustomCollectionReport.route) }
            )
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DuesHeroCard(
    totalDues: Double,
    totalStudents: Int,
    totalClasses: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = DuesRed.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(DuesRed, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Total Outstanding",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = totalDues.toRupees(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = DuesRed
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DuesRedLight
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = DuesRed,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(28.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    icon = Icons.Default.School,
                    value = totalStudents.toString(),
                    label = "Students",
                    color = DuesRed,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Default.Group,
                    value = totalClasses.toString(),
                    label = "Classes",
                    color = AccentPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CollectionHeroCard(
    monthCollection: Double,
    todayCollection: Double,
    todayReceipts: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = CollectionGreen.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CollectionGreen, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "This Month",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = monthCollection.toRupees(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = CollectionGreen
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CollectionGreenLight
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = CollectionGreen,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(28.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    icon = Icons.Default.CalendarToday,
                    value = todayCollection.toRupees(),
                    label = "Today",
                    color = CollectionGreen,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Default.Receipt,
                    value = todayReceipts.toString(),
                    label = "Receipts",
                    color = AccentBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompactReportCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewText: String? = null
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable {
                pressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (previewText != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    previewText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
private fun FullWidthReportCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                pressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomReportSection(
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TableChart,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Custom",
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            Surface(
                shape = CircleShape,
                color = accentColor
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
