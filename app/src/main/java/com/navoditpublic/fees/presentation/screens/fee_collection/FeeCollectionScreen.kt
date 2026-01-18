package com.navoditpublic.fees.presentation.screens.fee_collection

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.domain.model.ReceiptWithStudent
import com.navoditpublic.fees.presentation.components.EmptyState
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.presentation.theme.SaffronLight
import com.navoditpublic.fees.util.DateUtils
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

// Premium Color Palette
private val CashGreen = Color(0xFF10B981)
private val CashGreenLight = Color(0xFFD1FAE5)
private val CashGreenDark = Color(0xFF059669)
private val OnlineBlue = Color(0xFF3B82F6)
private val OnlineBlueLight = Color(0xFFDBEAFE)
private val OnlineBlueDark = Color(0xFF2563EB)
private val CancelledRed = Color(0xFFEF4444)
private val CancelledRedLight = Color(0xFFFEE2E2)
private val HeroGradientStart = Color(0xFFFF8F00)
private val HeroGradientEnd = Color(0xFFE65100)
private val SurfaceWhite = Color(0xFFFAFAFA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeCollectionScreen(
    navController: NavController,
    viewModel: FeeCollectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var animateStats by remember { mutableStateOf(false) }
    
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            delay(100)
            animateStats = true
        }
    }
    
    Scaffold(
        containerColor = SurfaceWhite,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CollectFee.createRoute()) },
                containerColor = Saffron,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(12.dp, CircleShape, spotColor = Saffron.copy(alpha = 0.4f))
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "New Receipt",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
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
                // Hero Header
                item {
                    HeroHeader(
                        todayCollection = state.todayCollection,
                        yesterdayCollection = state.yesterdayCollection,
                        todayCount = state.todayReceiptCount,
                        weekCollection = state.weekCollection,
                        monthCollection = state.monthCollection,
                        animateStats = animateStats
                    )
                }
                
                // Search Bar
                item {
                    AnimatedVisibility(
                        visible = animateStats,
                        enter = fadeIn(tween(400, 200)) + slideInVertically(tween(400, 200)) { 30 }
                    ) {
                        PremiumSearchBar(
                            query = state.searchQuery,
                            onQueryChange = viewModel::updateSearchQuery,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                // Tab Bar
                item {
                    AnimatedVisibility(
                        visible = animateStats,
                        enter = fadeIn(tween(400, 250)) + slideInVertically(tween(400, 250)) { 30 }
                    ) {
                        ReceiptTabBar(
                            selectedTab = state.selectedTab,
                            onTabSelected = viewModel::selectTab,
                            todayCount = state.todayReceiptCount,
                            weekCount = state.weekReceiptCount,
                            monthCount = state.monthReceiptCount,
                            totalCount = state.allReceipts.size,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                // Quick Filters
                item {
                    AnimatedVisibility(
                        visible = animateStats,
                        enter = fadeIn(tween(400, 300)) + slideInVertically(tween(400, 300)) { 30 }
                    ) {
                        QuickFilters(
                            paymentFilter = state.paymentFilter,
                            onPaymentFilterChange = viewModel::updatePaymentFilter,
                            statusFilter = state.statusFilter,
                            onStatusFilterChange = viewModel::updateStatusFilter,
                            cashCount = state.cashCount,
                            onlineCount = state.onlineCount,
                            cancelledCount = state.cancelledCount,
                            onClearFilters = viewModel::clearFilters,
                            hasActiveFilters = state.paymentFilter != PaymentFilter.ALL || 
                                              state.statusFilter != StatusFilter.ALL ||
                                              state.searchQuery.isNotBlank(),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Results Count
                item {
                    AnimatedVisibility(
                        visible = animateStats,
                        enter = fadeIn(tween(400, 350))
                    ) {
                        ResultsCount(
                            filteredCount = state.filteredReceipts.size,
                            totalCount = state.allReceipts.size,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }
                
                // Receipts List
                if (state.filteredReceipts.isEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = animateStats,
                            enter = fadeIn(tween(400, 400))
                        ) {
                            EmptyState(
                                icon = Icons.Default.Receipt,
                                title = if (state.searchQuery.isNotBlank() || 
                                           state.paymentFilter != PaymentFilter.ALL ||
                                           state.statusFilter != StatusFilter.ALL) 
                                    "No Results" else "No Receipts",
                                subtitle = if (state.searchQuery.isNotBlank()) 
                                    "Try a different search" 
                                else "Create your first receipt with the + button",
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = state.filteredReceipts,
                        key = { _, receipt -> receipt.receipt.id }
                    ) { index, receiptWithStudent ->
                        AnimatedVisibility(
                            visible = animateStats,
                            enter = fadeIn(tween(300, 400 + (index * 30).coerceAtMost(300))) + 
                                   slideInVertically(tween(300, 400 + (index * 30).coerceAtMost(300))) { 40 }
                        ) {
                            PremiumReceiptCard(
                                receiptWithStudent = receiptWithStudent,
                                onClick = {
                                    navController.navigate(Screen.ReceiptDetail.createRoute(receiptWithStudent.receipt.id))
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                
                // Bottom spacing
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ============================================================================
// HERO HEADER
// ============================================================================

@Composable
private fun HeroHeader(
    todayCollection: Double,
    yesterdayCollection: Double,
    todayCount: Int,
    weekCollection: Double,
    monthCollection: Double,
    animateStats: Boolean
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(animateStats) {
        if (animateStats) {
            animatedProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        }
    }
    
    val displayAmount = (todayCollection * animatedProgress.value)
    val percentChange = if (yesterdayCollection > 0) {
        ((todayCollection - yesterdayCollection) / yesterdayCollection * 100)
    } else if (todayCollection > 0) 100.0 else 0.0
    val isPositive = percentChange >= 0
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(HeroGradientStart, HeroGradientEnd)
                )
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = 280.dp, y = (-20).dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = (-30).dp, y = 100.dp)
                .background(Color.White.copy(alpha = 0.06f), CircleShape)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            // Title
            Text(
                text = "Receipt Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = "Manage all your fee receipts",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            
            Spacer(Modifier.height(20.dp))
            
            // Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Today's Collection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Today's Collection",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(4.dp))
                            AnimatedContent(
                                targetState = displayAmount,
                                transitionSpec = {
                                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                                },
                                label = "amount"
                            ) { amount ->
                                Text(
                                    text = "₹${String.format("%,.0f", amount)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            
                            Spacer(Modifier.height(6.dp))
                            
                            // Change badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPositive) CashGreenLight else CancelledRedLight
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .graphicsLayer { rotationZ = if (isPositive) 0f else 180f },
                                        tint = if (isPositive) CashGreen else CancelledRed
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${if (isPositive) "+" else ""}${String.format("%.1f", percentChange)}% vs yesterday",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isPositive) CashGreen else CancelledRed
                                    )
                                }
                            }
                        }
                        
                        // Receipt count badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Saffron.copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = todayCount.toString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Saffron
                                )
                                Text(
                                    text = "Receipts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SaffronDark
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Week/Month stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MiniStatCard(
                            label = "This Week",
                            value = weekCollection.toRupees(),
                            color = OnlineBlue,
                            modifier = Modifier.weight(1f)
                        )
                        MiniStatCard(
                            label = "This Month",
                            value = monthCollection.toRupees(),
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ============================================================================
// SEARCH BAR
// ============================================================================

@Composable
private fun PremiumSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            
            Spacer(Modifier.width(12.dp))
            
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                cursorBrush = SolidColor(Saffron),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search receipt #, student, amount...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            AnimatedVisibility(visible = query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// TAB BAR
// ============================================================================

@Composable
private fun ReceiptTabBar(
    selectedTab: ReceiptTab,
    onTabSelected: (ReceiptTab) -> Unit,
    todayCount: Int,
    weekCount: Int,
    monthCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ReceiptTab.entries) { tab ->
            val count = when (tab) {
                ReceiptTab.TODAY -> todayCount
                ReceiptTab.WEEK -> weekCount
                ReceiptTab.MONTH -> monthCount
                ReceiptTab.ALL -> totalCount
            }
            TabChip(
                tab = tab,
                count = count,
                isSelected = selectedTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
private fun TabChip(
    tab: ReceiptTab,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Saffron else Color.White,
        animationSpec = tween(200),
        label = "tabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Gray,
        animationSpec = tween(200),
        label = "tabContent"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = tween(200),
        label = "tabScale"
    )
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier.scale(scale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tab.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
            if (count > 0) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else Saffron.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Saffron,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// QUICK FILTERS
// ============================================================================

@Composable
private fun QuickFilters(
    paymentFilter: PaymentFilter,
    onPaymentFilterChange: (PaymentFilter) -> Unit,
    statusFilter: StatusFilter,
    onStatusFilterChange: (StatusFilter) -> Unit,
    cashCount: Int,
    onlineCount: Int,
    cancelledCount: Int,
    onClearFilters: () -> Unit,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Tune,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            
            // Payment filters
            FilterChip(
                label = "Cash",
                count = cashCount,
                icon = Icons.Default.Money,
                isSelected = paymentFilter == PaymentFilter.CASH,
                color = CashGreen,
                onClick = {
                    onPaymentFilterChange(
                        if (paymentFilter == PaymentFilter.CASH) PaymentFilter.ALL else PaymentFilter.CASH
                    )
                }
            )
            
            FilterChip(
                label = "Online",
                count = onlineCount,
                icon = Icons.Default.CreditCard,
                isSelected = paymentFilter == PaymentFilter.ONLINE,
                color = OnlineBlue,
                onClick = {
                    onPaymentFilterChange(
                        if (paymentFilter == PaymentFilter.ONLINE) PaymentFilter.ALL else PaymentFilter.ONLINE
                    )
                }
            )
            
            // Status filter
            if (cancelledCount > 0) {
                FilterChip(
                    label = "Cancelled",
                    count = cancelledCount,
                    icon = Icons.Default.Cancel,
                    isSelected = statusFilter == StatusFilter.CANCELLED,
                    color = CancelledRed,
                    onClick = {
                        onStatusFilterChange(
                            if (statusFilter == StatusFilter.CANCELLED) StatusFilter.ALL else StatusFilter.CANCELLED
                        )
                    }
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            // Clear filters
            AnimatedVisibility(visible = hasActiveFilters) {
                Surface(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent
                ) {
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelMedium,
                        color = Saffron,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    count: Int,
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) color else Color.White,
        animationSpec = tween(200),
        label = "filterBg"
    )
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (isSelected) Color.White else color
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "$label ($count)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else Color.DarkGray
            )
        }
    }
}

// ============================================================================
// RESULTS COUNT
// ============================================================================

@Composable
private fun ResultsCount(
    filteredCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Receipt,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (filteredCount == totalCount) {
                "$totalCount receipts"
            } else {
                "Showing $filteredCount of $totalCount"
            },
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

// ============================================================================
// PREMIUM RECEIPT CARD
// ============================================================================

@Composable
private fun PremiumReceiptCard(
    receiptWithStudent: ReceiptWithStudent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val receipt = receiptWithStudent.receipt
    val isCancelled = receipt.isCancelled
    val isCash = receipt.paymentMode == PaymentMode.CASH
    
    val accentColor = when {
        isCancelled -> CancelledRed
        isCash -> CashGreen
        else -> OnlineBlue
    }
    val accentColorLight = when {
        isCancelled -> CancelledRedLight
        isCash -> CashGreenLight
        else -> OnlineBlueLight
    }
    
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(100),
        label = "swipe"
    )
    
    Box(modifier = modifier) {
        // Swipe background (hint)
        if (animatedOffset.absoluteValue > 20f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColorLight),
                contentAlignment = if (animatedOffset > 0) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Text(
                    text = "View Details",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX.absoluteValue > 100f) {
                                onClick()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = { offsetX = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-150f, 150f)
                        }
                    )
                }
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCancelled) CancelledRedLight.copy(alpha = 0.4f) else Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isCancelled) 0.dp else 3.dp
            )
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Left accent strip
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(100.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(accentColor, accentColor.copy(alpha = 0.7f))
                            )
                        )
                )
                
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Receipt badge
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(accentColorLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "#${receipt.receiptNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                            Icon(
                                imageVector = when {
                                    isCancelled -> Icons.Default.Cancel
                                    isCash -> Icons.Default.Money
                                    else -> Icons.Default.CreditCard
                                },
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.width(14.dp))
                    
                    // Details
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Student initials avatar
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Saffron, SaffronDark)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = receiptWithStudent.studentName
                                        .split(" ")
                                        .take(2)
                                        .mapNotNull { it.firstOrNull()?.uppercase() }
                                        .joinToString(""),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                            
                            Spacer(Modifier.width(8.dp))
                            
                            Text(
                                text = receiptWithStudent.studentName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textDecoration = if (isCancelled) TextDecoration.LineThrough else null,
                                color = if (isCancelled) CancelledRed.copy(alpha = 0.7f) else Color.Black
                            )
                        }
                        
                        Spacer(Modifier.height(4.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = receiptWithStudent.studentClass,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = DateUtils.getRelativeTimeString(receipt.receiptDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        
                        if (isCancelled) {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CancelledRed
                            ) {
                                Text(
                                    text = "CANCELLED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    // Amount
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = receipt.netAmount.toRupees(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCancelled) CancelledRed.copy(alpha = 0.6f) else accentColor,
                            textDecoration = if (isCancelled) TextDecoration.LineThrough else null
                        )
                        
                        if (!isCancelled && receipt.discountAmount > 0) {
                            Text(
                                text = "Disc: ${receipt.discountAmount.toRupees()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
