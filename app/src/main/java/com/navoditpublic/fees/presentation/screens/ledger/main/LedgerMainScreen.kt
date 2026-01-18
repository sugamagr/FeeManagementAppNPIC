package com.navoditpublic.fees.presentation.screens.ledger.main

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.LocalDrawerState
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.PaidChipBackground
import com.navoditpublic.fees.presentation.theme.PaidChipText
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.presentation.theme.SaffronLight
import com.navoditpublic.fees.presentation.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerMainScreen(
    navController: NavController,
    viewModel: LedgerMainViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()
    
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 16.dp)
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
                                        .size(120.dp)
                                        .offset(
                                            x = (-40).dp + (floatAnim.value * 10).dp,
                                            y = (-30).dp + (floatAnim.value * 5).dp
                                        )
                                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(
                                            x = 30.dp - (floatAnim.value * 8).dp,
                                            y = 20.dp + (floatAnim.value * 6).dp
                                        )
                                        .background(Color.White.copy(alpha = 0.06f), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .align(Alignment.CenterEnd)
                                        .offset(
                                            x = 10.dp,
                                            y = 40.dp - (floatAnim.value * 10).dp
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
                                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                                Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    Icon(
                                                        Icons.Default.MenuBook,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(14.dp))
                                            Column {
                                                Text(
                                                    "Ledger",
                                                    style = MaterialTheme.typography.headlineMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                if (state.sessionName.isNotEmpty()) {
                                                    Text(
                                                        text = state.sessionName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.White.copy(alpha = 0.85f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Floating summary card that overlaps the header
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Active Students
                                    QuickStatItem(
                                        value = state.totalStudents.toString(),
                                        label = "Students",
                                        color = Saffron
                                    )
                                    
                                    VerticalDivider()
                                    
                                    // Classes
                                    QuickStatItem(
                                        value = state.classSummaries.size.toString(),
                                        label = "Classes",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    VerticalDivider()
                                    
                                    // Students with dues
                                    val totalWithDues = state.classSummaries.sumOf { it.studentsWithDues }
                                    QuickStatItem(
                                        value = totalWithDues.toString(),
                                        label = "With Dues",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    
                    // Spacer for floating card overlap
                    item { Spacer(modifier = Modifier.height(36.dp)) }
                    
                    // Collection Overview Section
                    item {
                        CollectionOverviewCard(
                            totalCollected = state.totalCollected,
                            totalPending = state.totalDues,
                            currencyFormat = currencyFormat,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                    
                    // Classes Section Header
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
                                    text = "Class-wise Ledger",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap a class to view details",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Saffron.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${state.classSummaries.size} Classes",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Saffron
                                )
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                    
                    // Class Cards
                    itemsIndexed(
                        items = state.classSummaries,
                        key = { _, summary -> summary.className }
                    ) { _, summary ->
                        ClassLedgerCard(
                            summary = summary,
                            currencyFormat = currencyFormat,
                            onClick = {
                                navController.navigate(Screen.LedgerClass.createRoute(summary.className))
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                    
                    // Empty state
                    if (state.classSummaries.isEmpty()) {
                        item {
                            EmptyLedgerState()
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
fun CollectionOverviewCard(
    totalCollected: Double,
    totalPending: Double,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    val total = totalCollected + totalPending
    val collectionProgress = if (total > 0) (totalCollected / total).toFloat() else 0f
    val progressPercent = (collectionProgress * 100).toInt()
    
    // Animated progress
    var animatedProgress by remember { mutableStateOf(0f) }
    var animatedPercent by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(collectionProgress) {
        // Animate the progress
        val startProgress = animatedProgress
        val duration = 1000
        val startTime = System.currentTimeMillis()
        
        while (animatedProgress < collectionProgress) {
            val elapsed = System.currentTimeMillis() - startTime
            val fraction = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            animatedProgress = startProgress + (collectionProgress - startProgress) * fraction
            animatedPercent = (animatedProgress * 100).toInt()
            if (fraction >= 1f) break
            delay(16)
        }
        animatedProgress = collectionProgress
        animatedPercent = progressPercent
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Saffron, SaffronDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Collection Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Circular Progress and Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Progress
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    // Progress animation
                    val animatedProgressValue by animateFloatAsState(
                        targetValue = collectionProgress,
                        animationSpec = tween(1000, easing = FastOutSlowInEasing),
                        label = "progress"
                    )
                    
                    Canvas(modifier = Modifier.size(120.dp)) {
                        val strokeWidth = 12.dp.toPx()
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
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$animatedPercent%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Saffron
                        )
                        Text(
                            text = "Collected",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Stats Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Collected
                    StatCard(
                        label = "Collected",
                        amount = currencyFormat.format(totalCollected),
                        backgroundColor = PaidChipBackground,
                        textColor = PaidChipText,
                        icon = Icons.AutoMirrored.Filled.TrendingUp
                    )
                    
                    // Pending
                    StatCard(
                        label = "Pending",
                        amount = currencyFormat.format(totalPending),
                        backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        textColor = MaterialTheme.colorScheme.error,
                        icon = Icons.Default.AccountBalance
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Total Amount Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Fees",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    amount: String,
    backgroundColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        modifier = Modifier.width(150.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.8f)
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun ClassLedgerCard(
    summary: ClassLedgerSummary,
    currencyFormat: NumberFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = summary.totalDues + summary.totalPaid
    val collectionProgress = if (total > 0) (summary.totalPaid / total).toFloat() else 1f
    
    // Determine accent color based on class type
    val accentColor = when {
        summary.className in listOf("NC", "LKG", "UKG") -> Color(0xFF7C4DFF) // Purple for pre-primary
        summary.className in listOf("1st", "2nd", "3rd", "4th", "5th") -> SuccessGreen // Green for primary
        summary.className in listOf("6th", "7th", "8th") -> Color(0xFF00BCD4) // Cyan for middle
        summary.className in listOf("9th", "10th") -> Color(0xFFFF7043) // Deep orange for secondary
        summary.className in listOf("11th", "12th") -> Color(0xFFE91E63) // Pink for senior
        else -> Saffron
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Colored accent bar on left
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(130.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(accentColor, accentColor.copy(alpha = 0.6f))
                        ),
                        shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                    )
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Class Icon
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (summary.className) {
                                    "NC" -> "NC"
                                    "LKG" -> "LK"
                                    "UKG" -> "UK"
                                    else -> summary.className.filter { it.isDigit() }.take(2)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "Class ${summary.className}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Group,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${summary.studentCount} Students",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Due badge
                    if (summary.studentsWithDues > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${summary.studentsWithDues} Due",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Mini progress bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Collection",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(collectionProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { collectionProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accentColor,
                        trackColor = accentColor.copy(alpha = 0.15f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Amounts Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Pending",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currencyFormat.format(summary.totalDues),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (summary.totalDues > 0) 
                                MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Collected",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currencyFormat.format(summary.totalPaid),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = PaidChipText
                        )
                    }
                    
                    // Arrow
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "View",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyLedgerState() {
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
                Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Students Found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add students to view ledger entries\nand track fee collection",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
