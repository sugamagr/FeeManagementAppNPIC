package com.navoditpublic.fees.presentation.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.LocalDrawerState
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.presentation.theme.SaffronLight
import com.navoditpublic.fees.util.DateUtils
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

// Color palette
private val HeroGradientStart = Color(0xFFFF8F00)
private val HeroGradientEnd = Color(0xFFE65100)
private val SuccessGreen = Color(0xFF4CAF50)
private val SuccessGreenLight = Color(0xFFE8F5E9)
private val WarningRed = Color(0xFFE53935)
private val WarningRedLight = Color(0xFFFFEBEE)
private val InfoBlue = Color(0xFF1E88E5)
private val InfoBlueLight = Color(0xFFE3F2FD)
private val AccentPurple = Color(0xFF7C4DFF)
private val AccentPurpleLight = Color(0xFFEDE7F6)
private val WarmBackground = Color(0xFFFAF8F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var animateStats by remember { mutableStateOf(false) }
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            delay(100)
            animateStats = true
        }
    }
    
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = WarmBackground
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else {
            // Main Content - header scrolls with content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                )
            ) {
                // Scrollable Header
                item {
                    ScrollableHeader(
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        sessionName = state.currentSession?.sessionName,
                        todayCollection = state.todayCollection,
                        yesterdayCollection = state.yesterdayCollection,
                        animateStats = animateStats
                    )
                }
                
                // Date
                item {
                    AnimatedVisibility(
                        visible = animateStats,
                        enter = fadeIn(tween(400, delayMillis = 100)),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
                        Text(
                            text = dateFormat.format(Date()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }
                }
                
                // Stats Grid
                item {
                    AnimatedVisibility(
                        visible = animateStats,
                        enter = fadeIn(tween(500, delayMillis = 150)) + slideInVertically(tween(500, delayMillis = 150)) { 40 },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernStatsCard(
                                title = "Students",
                                value = state.totalStudents.toString(),
                                icon = Icons.Default.Group,
                                accentColor = InfoBlue,
                                backgroundColor = InfoBlueLight,
                                modifier = Modifier.weight(1f)
                            )
                            ModernStatsCard(
                                title = "Pending Dues",
                                value = state.pendingDues.toRupees(),
                                icon = Icons.Default.Warning,
                                accentColor = WarningRed,
                                backgroundColor = WarningRedLight,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Monthly Collection Card
                item {
                    AnimatedVisibility(
                        visible = animateStats,
                        enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200)) { 40 },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        MonthlyCollectionCard(
                            monthCollection = state.monthCollection
                        )
                    }
                }
                
                // Quick Actions Header
                item {
                    AnimatedVisibility(
                        visible = animateStats,
                        enter = fadeIn(tween(400, delayMillis = 250)),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }
                }
                
                // Quick Actions
                item {
                    AnimatedVisibility(
                        visible = animateStats,
                        enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300)) { 40 },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernQuickActionCard(
                                title = "New Student",
                                icon = Icons.Default.PersonAdd,
                                gradientColors = listOf(Color(0xFF43A047), Color(0xFF2E7D32)),
                                onClick = { navController.navigate(Screen.AddEditStudent.createRoute()) },
                                modifier = Modifier.weight(1f)
                            )
                            ModernQuickActionCard(
                                title = "Receipts",
                                icon = Icons.Default.Receipt,
                                gradientColors = listOf(Saffron, SaffronDark),
                                onClick = { navController.navigate(Screen.FeeCollection.route) },
                                modifier = Modifier.weight(1f)
                            )
                            ModernQuickActionCard(
                                title = "Transport",
                                icon = Icons.Default.DirectionsBus,
                                gradientColors = listOf(Color(0xFF7E57C2), Color(0xFF9575CD)),
                                onClick = { navController.navigate(Screen.TransportQuick.createRoute()) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Recent Activity Header
                item {
                    AnimatedVisibility(
                        visible = animateStats,
                        enter = fadeIn(tween(400, delayMillis = 350)),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Activity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                onClick = { navController.navigate(Screen.FeeCollection.route) },
                                shape = RoundedCornerShape(20.dp),
                                color = Saffron.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "View All",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SaffronDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = SaffronDark
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Recent Receipts
                if (state.recentReceipts.isEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = animateStats,
                            enter = fadeIn(tween(400, delayMillis = 400)),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            EmptyActivityCard()
                        }
                    }
                } else {
                    itemsIndexed(
                        items = state.recentReceipts,
                        key = { _, receipt -> receipt.receipt.id }
                    ) { index, receipt ->
                        AnimatedVisibility(
                            visible = animateStats,
                            enter = fadeIn(tween(400, delayMillis = 400 + (index * 50))) +
                                    slideInVertically(tween(400, delayMillis = 400 + (index * 50))) { 30 },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            ActivityCard(
                                receiptNumber = receipt.receipt.receiptNumber,
                                studentName = receipt.studentName,
                                className = receipt.studentClass,
                                amount = receipt.receipt.netAmount,
                                timestamp = receipt.receipt.receiptDate,
                                onClick = {
                                    navController.navigate(Screen.ReceiptDetail.createRoute(receipt.receipt.id))
                                }
                            )
                        }
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ScrollableHeader(
    onMenuClick: () -> Unit,
    sessionName: String?,
    todayCollection: Double,
    yesterdayCollection: Double,
    animateStats: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Main gradient background with curve
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height
                
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(width, 0f)
                    lineTo(width, height * 0.75f)
                    quadraticBezierTo(
                        width / 2f, height * 1.1f,
                        0f, height * 0.75f
                    )
                    close()
                }
                
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(HeroGradientStart, HeroGradientEnd)
                    )
                )
            }
            
            // Floating decorative circles
            FloatingDecorativeCircles()
            
            // Header content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Menu button
                    Surface(
                        onClick = onMenuClick,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "NPIC Fees",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (sessionName != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "Session $sessionName",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Hero Collection Card - overlapping the header
        AnimatedVisibility(
            visible = animateStats,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -50 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .offset(y = 40.dp)
        ) {
            HeroCollectionCard(
                todayCollection = todayCollection,
                yesterdayCollection = yesterdayCollection
            )
        }
    }
    
    // Add space for the overlapping card
    Spacer(modifier = Modifier.height(48.dp))
}

@Composable
private fun FloatingDecorativeCircles() {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset1"
    )
    
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset2"
    )
    
    val offset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset3"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Large circle top-right
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = 280.dp + offset1.dp, y = 20.dp + (offset1 * 0.5f).dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        
        // Medium circle mid-right
        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(x = 320.dp + offset2.dp, y = 100.dp + offset2.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )
        
        // Small circle left
        Box(
            modifier = Modifier
                .size(40.dp)
                .offset(x = (-10).dp + offset3.dp, y = 140.dp + (offset3 * 0.7f).dp)
                .background(Color.White.copy(alpha = 0.06f), CircleShape)
        )
        
        // Tiny circle center
        Box(
            modifier = Modifier
                .size(24.dp)
                .offset(x = 150.dp + (offset1 * 0.5f).dp, y = 60.dp + offset2.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
        )
    }
}

@Composable
private fun HeroCollectionCard(
    todayCollection: Double,
    yesterdayCollection: Double,
    modifier: Modifier = Modifier
) {
    val difference = todayCollection - yesterdayCollection
    val percentageChange = if (yesterdayCollection > 0) {
        ((difference / yesterdayCollection) * 100)
    } else if (todayCollection > 0) {
        100.0
    } else {
        0.0
    }
    val isPositive = difference >= 0
    
    // Animated progress
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(300)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Saffron.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Collection info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Today's Collection",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = todayCollection.toRupees(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Comparison with yesterday
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPositive) SuccessGreenLight else WarningRedLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isPositive) SuccessGreen else WarningRed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (isPositive) "+" else ""}${String.format("%.1f", percentageChange)}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) SuccessGreen else WarningRed
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "vs yesterday",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Right side - Circular progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
                // Background circle
                Canvas(modifier = Modifier.size(90.dp)) {
                    drawCircle(
                        color = SaffronLight.copy(alpha = 0.3f),
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                // Progress arc
                Canvas(modifier = Modifier.size(90.dp)) {
                    val sweepAngle = 360f * animatedProgress.value * 
                        (if (yesterdayCollection > 0) (todayCollection / (yesterdayCollection * 1.5f)).coerceIn(0.0, 1.0).toFloat() else 0.5f)
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(Saffron, SaffronDark, Saffron)
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                // Center content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Payment,
                        contentDescription = null,
                        tint = SaffronDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernStatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(200)
        visible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .graphicsLayer { alpha = scale },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = backgroundColor,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MonthlyCollectionCard(
    monthCollection: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(AccentPurple, Color(0xFF536DFE))
                        )
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "This Month's Collection",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = monthCollection.toRupees(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Mini chart visualization
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentPurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernQuickActionCard(
    title: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "press_scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = gradientColors.first().copy(alpha = 0.3f)
            )
            .clickable {
                pressed = true
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(colors = gradientColors)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ActivityCard(
    receiptNumber: Int,
    studentName: String,
    className: String,
    amount: Double,
    timestamp: Long,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            // Avatar with initials
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(SuccessGreen, Color(0xFF2E7D32))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = studentName.split(" ")
                        .take(2)
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .joinToString(""),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#$receiptNumber",
                        style = MaterialTheme.typography.labelSmall,
                        color = Saffron,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = studentName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "$className • ${DateUtils.getRelativeTimeString(timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SuccessGreenLight
            ) {
                Text(
                    text = amount.toRupees(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyActivityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "No recent activity",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Start collecting fees to see activity here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
