package com.navoditpublic.fees.presentation.screens.ledger

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.ChatBubble
import android.widget.Toast
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.data.local.entity.LedgerEntryType
import com.navoditpublic.fees.domain.model.LedgerEntry
import com.navoditpublic.fees.presentation.components.EmptyState
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.AdvanceChipBackground
import com.navoditpublic.fees.presentation.theme.AdvanceChipText
import com.navoditpublic.fees.presentation.theme.DueChipBackground
import com.navoditpublic.fees.presentation.theme.DueChipText
import com.navoditpublic.fees.presentation.theme.PaidChipBackground
import com.navoditpublic.fees.presentation.theme.PaidChipText
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.util.DateUtils
import com.navoditpublic.fees.util.toRupees

// Compact table column widths (merged Amount column)
private val CompactDateWidth = 70.dp
private val CompactParticularsWidth = 140.dp
private val CompactAmountWidth = 85.dp
private val CompactBalanceWidth = 85.dp

// Full table column widths (separate Debit/Credit)
private val FullDateWidth = 85.dp
private val FullParticularsWidth = 220.dp
private val FullDebitWidth = 85.dp
private val FullCreditWidth = 85.dp
private val FullBalanceWidth = 95.dp

// WhatsApp color
private val WhatsAppGreen = Color(0xFF25D366)

// Phone action type enum
private enum class PhoneActionType { CALL, WHATSAPP }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLedgerScreen(
    studentId: Long,
    navController: NavController,
    viewModel: StudentLedgerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showFullTableDialog by remember { mutableStateOf(false) }
    var showPhoneSelectionDialog by remember { mutableStateOf(false) }
    var phoneActionType by remember { mutableStateOf(PhoneActionType.CALL) }
    
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
    
    // Full Table Dialog
    if (showFullTableDialog) {
        FullTableDialog(
            entries = state.entries,
            totalDebit = state.totalDebits,
            totalCredit = state.totalCredits,
            balance = state.currentBalance,
            studentName = state.student?.name ?: "",
            onDismiss = { showFullTableDialog = false }
        )
    }
    
    // Phone Selection Dialog
    if (showPhoneSelectionDialog && state.student != null) {
        val student = state.student!!
        PhoneSelectionDialog(
            studentName = student.name,
            primaryPhone = student.phonePrimary,
            secondaryPhone = student.phoneSecondary,
            actionType = phoneActionType,
            onPhoneSelected = { phone ->
                showPhoneSelectionDialog = false
                when (phoneActionType) {
                    PhoneActionType.CALL -> {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phone")
                        }
                        context.startActivity(intent)
                    }
                    PhoneActionType.WHATSAPP -> {
                        openWhatsApp(
                            context = context,
                            phone = phone,
                            studentName = student.name,
                            className = student.currentClass,
                            dueAmount = state.currentBalance
                        )
                    }
                }
            },
            onDismiss = { showPhoneSelectionDialog = false }
        )
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else if (state.entries.isEmpty() && state.student == null) {
            EmptyState(
                icon = Icons.Default.Receipt,
                title = "No Ledger Entries",
                subtitle = "Ledger entries will appear here when fees are charged or collected",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 16.dp)
            ) {
                // Hero Header
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        // Curved gradient background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
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
                            
                            // Header content
            Column(
                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color.White
                                        )
                                    }
                                    
                                    Spacer(Modifier.width(8.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        state.student?.let { student ->
                                            Text(
                                                text = student.name,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Ledger",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.White.copy(alpha = 0.85f)
                                                )
                                                Text(
                                                    text = "  •  ",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.White.copy(alpha = 0.5f)
                                                )
                                                Text(
                                                    text = "Class ${student.currentClass}-${student.section}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.White.copy(alpha = 0.85f)
                                                )
                                            }
                                        } ?: run {
                                            Text(
                                                "Student Ledger",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Floating Balance Card
                        state.student?.let { student ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .align(Alignment.BottomCenter)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(24.dp),
                                        ambientColor = Saffron.copy(alpha = 0.3f),
                                        spotColor = Saffron.copy(alpha = 0.3f)
                                    ),
                                shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Current Balance",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Balance Amount with Status
                                    val hasDues = state.currentBalance > 0
                                    val hasAdvance = state.currentBalance < 0
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (hasAdvance) 
                                                (state.currentBalance * -1).toRupees() 
                                            else 
                                                state.currentBalance.toRupees(),
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                hasDues -> DueChipText
                                                hasAdvance -> AdvanceChipText
                                                else -> PaidChipText
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when {
                                                hasDues -> DueChipBackground
                                                hasAdvance -> AdvanceChipBackground
                                                else -> PaidChipBackground
                                            }
                                        ) {
                                            Text(
                                                text = when {
                                                    hasDues -> "Due"
                                                    hasAdvance -> "Advance"
                                                    else -> "Clear"
                                                },
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = when {
                                                    hasDues -> DueChipText
                                                    hasAdvance -> AdvanceChipText
                                                    else -> PaidChipText
                                                }
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Stats Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        StatColumn(
                                            label = "Total Charged",
                                            value = state.totalDebits.toRupees(),
                                            color = DueChipText
                                        )
                                        
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(40.dp)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                        )
                                        
                                        StatColumn(
                                            label = "Total Paid",
                                            value = state.totalCredits.toRupees(),
                                            color = PaidChipText
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Account Info Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        InfoChip(label = "A/C", value = student.accountNumber)
                                        InfoChip(label = "SR", value = student.srNumber)
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Student Details Card
                state.student?.let { student ->
                    item {
                        StudentDetailsCard(
                            fatherName = student.fatherName,
                            village = student.district.ifBlank { student.addressLine1 },
                            admissionFeePaid = student.admissionFeePaid,
                            hasTransport = student.hasTransport,
                            transportRouteName = state.transportRoute?.routeName,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    // Quick Actions Row
                    item {
                        QuickActionsRow(
                            onViewDetails = {
                                navController.navigate(Screen.StudentDetail.createRoute(studentId))
                            },
                            onViewReceipts = {
                                navController.navigate(Screen.StudentReceipts.createRoute(studentId))
                            },
                            onCollectFee = {
                                navController.navigate(Screen.CollectFee.createRoute(studentId))
                            },
                            onCall = {
                                val hasPrimary = student.phonePrimary.isNotBlank()
                                val hasSecondary = student.phoneSecondary.isNotBlank()
                                
                                if (hasPrimary && hasSecondary) {
                                    // Multiple phones - show selection dialog
                                    phoneActionType = PhoneActionType.CALL
                                    showPhoneSelectionDialog = true
                                } else {
                                    // Single phone - direct call
                                    val phoneNumber = student.phonePrimary.ifBlank { student.phoneSecondary }
                                    if (phoneNumber.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phoneNumber")
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            },
                            onWhatsApp = {
                                val hasPrimary = student.phonePrimary.isNotBlank()
                                val hasSecondary = student.phoneSecondary.isNotBlank()
                                
                                if (hasPrimary && hasSecondary) {
                                    // Multiple phones - show selection dialog
                                    phoneActionType = PhoneActionType.WHATSAPP
                                    showPhoneSelectionDialog = true
                                } else if (hasPrimary || hasSecondary) {
                                    // Single phone - direct WhatsApp
                                    val phoneNumber = student.phonePrimary.ifBlank { student.phoneSecondary }
                                    openWhatsApp(
                                        context = context,
                                        phone = phoneNumber,
                                        studentName = student.name,
                                        className = student.currentClass,
                                        dueAmount = state.currentBalance
                                    )
                                } else {
                                    Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
                                }
                            },
                            hasPhoneNumber = student.phonePrimary.isNotBlank() || student.phoneSecondary.isNotBlank(),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                // Transactions Section Header with Full View Toggle
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TableChart,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Saffron
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                    text = "Transaction Ledger",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                            )
                            Text(
                                    text = "${state.entries.size} entries",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Full View Toggle Button
                        Surface(
                            onClick = { showFullTableDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Saffron.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Fullscreen,
                                    contentDescription = "Full View",
                                    modifier = Modifier.size(18.dp),
                                    tint = Saffron
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Full View",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Saffron
                                )
                            }
                        }
                    }
                }
                
                // Compact Ledger Table
                if (state.entries.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Receipt,
                            title = "No Transactions",
                            subtitle = "Transactions will appear here"
                        )
                    }
                } else {
                    // Compact Table Card Container
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Compact Table Header
                                CompactTableHeader()
                                
                                // Table Rows
                                state.entries.forEachIndexed { index, entry ->
                                    CompactTableRow(
                                        entry = entry,
                                        index = index,
                                        isLast = index == state.entries.size - 1
                                    )
                                }
                                
                                // Table Footer - Totals
                                CompactTableFooter(
                                    totalDebit = state.totalDebits,
                                    totalCredit = state.totalCredits,
                                    balance = state.currentBalance
                                )
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ==================== COMPACT TABLE COMPONENTS ====================

@Composable
private fun CompactTableHeader() {
    val headerBg = Saffron.copy(alpha = 0.12f)
    
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(headerBg, Saffron.copy(alpha = 0.08f))
                ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Date",
            modifier = Modifier.width(CompactDateWidth),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
            color = SaffronDark
                    )
                    Text(
                        text = "Particulars",
            modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
            color = SaffronDark
                    )
                    Text(
            text = "Amount",
            modifier = Modifier.width(CompactAmountWidth),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
            color = SaffronDark,
                        textAlign = TextAlign.End
                    )
                    Text(
            text = "Balance",
            modifier = Modifier.width(CompactBalanceWidth),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
            color = SaffronDark,
                        textAlign = TextAlign.End
                    )
    }
}

@Composable
private fun CompactTableRow(
    entry: LedgerEntry,
    index: Int,
    isLast: Boolean
) {
    val isDebit = entry.entryType == LedgerEntryType.DEBIT
    val amount = if (isDebit) entry.debitAmount else entry.creditAmount
    val amountColor = if (isDebit) DueChipText else PaidChipText
    
    val rowBg = if (index % 2 == 0) 
        Color.Transparent 
    else 
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBg)
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date Column - Short format "01 Jan"
                    Text(
                text = DateUtils.formatDayMonth(entry.entryDate),
                modifier = Modifier.width(CompactDateWidth),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Particulars Column
            Text(
                text = entry.particulars,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Amount Column (merged Debit/Credit)
            Text(
                text = "${amount.toRupees()} ${if (isDebit) "Dr" else "Cr"}",
                modifier = Modifier.width(CompactAmountWidth),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = amountColor,
                textAlign = TextAlign.End
            )
            
            // Balance Column
            val balanceColor = when {
                entry.balance > 0 -> DueChipText
                entry.balance < 0 -> AdvanceChipText
                else -> PaidChipText
            }
            Text(
                text = if (entry.balance < 0) 
                    "${(entry.balance * -1).toRupees()}" 
                else 
                    entry.balance.toRupees(),
                modifier = Modifier.width(CompactBalanceWidth),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = balanceColor,
                textAlign = TextAlign.End
            )
        }
        
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
private fun CompactTableFooter(
    totalDebit: Double,
    totalCredit: Double,
    balance: Double
) {
    val footerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(footerBg, shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "",
                modifier = Modifier.width(CompactDateWidth)
            )
            Text(
                text = "Total",
                modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Show net amount
            val netAmount = totalDebit - totalCredit
            val isNetDebit = netAmount > 0
            Text(
                text = "Dr: ${totalDebit.toRupees()}",
                modifier = Modifier.width(CompactAmountWidth),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DueChipText,
                        textAlign = TextAlign.End
                    )
            
            val balanceColor = when {
                balance > 0 -> DueChipText
                balance < 0 -> AdvanceChipText
                else -> PaidChipText
            }
            Text(
                text = if (balance < 0) 
                    "${(balance * -1).toRupees()}" 
                else 
                    balance.toRupees(),
                modifier = Modifier.width(CompactBalanceWidth),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = balanceColor,
                textAlign = TextAlign.End
            )
        }
    }
}

// ==================== FULL TABLE DIALOG ====================

@Composable
private fun FullTableDialog(
    entries: List<LedgerEntry>,
    totalDebit: Double,
    totalCredit: Double,
    balance: Double,
    studentName: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        val horizontalScrollState = rememberScrollState()
        val tableWidth = FullDateWidth + FullParticularsWidth + FullDebitWidth + FullCreditWidth + FullBalanceWidth + 32.dp
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .fillMaxHeight(0.85f), // Use 85% of screen height
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Saffron, SaffronDark)
                            ),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Complete Ledger",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = studentName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
                
                // Table Header (fixed)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Box(modifier = Modifier.width(tableWidth)) {
                        FullTableHeader()
                    }
                }
                
                // Scrollable Table Content
                val verticalScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                        .verticalScroll(verticalScrollState)
                ) {
                    Column(modifier = Modifier.width(tableWidth)) {
                        entries.forEachIndexed { index, entry ->
                            FullTableRow(
                                entry = entry,
                                index = index,
                                isLast = index == entries.size - 1
                            )
                        }
                    }
                }
                
                // Table Footer (fixed at bottom)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Box(modifier = Modifier.width(tableWidth)) {
                        FullTableFooter(
                            totalDebit = totalDebit,
                            totalCredit = totalCredit,
                            balance = balance
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun FullTableHeader() {
    val headerBg = Saffron.copy(alpha = 0.15f)
    
    Row(
        modifier = Modifier
            .background(headerBg)
            .padding(vertical = 14.dp, horizontal = 16.dp)
    ) {
        TableHeaderCell(text = "Date", width = FullDateWidth, textAlign = TextAlign.Start)
        TableHeaderCell(text = "Particulars", width = FullParticularsWidth, textAlign = TextAlign.Start)
        TableHeaderCell(text = "Debit", width = FullDebitWidth, textAlign = TextAlign.End)
        TableHeaderCell(text = "Credit", width = FullCreditWidth, textAlign = TextAlign.End)
        TableHeaderCell(text = "Balance", width = FullBalanceWidth, textAlign = TextAlign.End)
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    width: Dp,
    textAlign: TextAlign
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        contentAlignment = when (textAlign) {
            TextAlign.End -> Alignment.CenterEnd
            TextAlign.Center -> Alignment.Center
            else -> Alignment.CenterStart
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SaffronDark
        )
    }
}

@Composable
private fun FullTableRow(
    entry: LedgerEntry,
    index: Int,
    isLast: Boolean
) {
    val isDebit = entry.entryType == LedgerEntryType.DEBIT
    val rowBg = if (index % 2 == 0) 
        Color.Transparent 
    else 
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    
    Column {
    Row(
        modifier = Modifier
                .background(rowBg)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Date - "01 Jan 25" format
        Text(
                text = DateUtils.formatDayMonthYear(entry.entryDate),
                modifier = Modifier
                    .width(FullDateWidth)
                    .padding(end = 6.dp),
            style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
            
            // Particulars - Full text, wraps to multiple lines
        Text(
            text = entry.particulars,
                modifier = Modifier
                    .width(FullParticularsWidth)
                    .padding(end = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Debit
        Text(
                text = if (isDebit && entry.debitAmount > 0) entry.debitAmount.toRupees() else "-",
                modifier = Modifier
                    .width(FullDebitWidth)
                    .padding(horizontal = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isDebit) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isDebit) DueChipText else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            textAlign = TextAlign.End
        )
            
            // Credit
        Text(
                text = if (!isDebit && entry.creditAmount > 0) entry.creditAmount.toRupees() else "-",
                modifier = Modifier
                    .width(FullCreditWidth)
                    .padding(horizontal = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (!isDebit) FontWeight.SemiBold else FontWeight.Normal,
                color = if (!isDebit) PaidChipText else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            textAlign = TextAlign.End
        )
            
            // Balance
            val balanceColor = when {
                entry.balance > 0 -> DueChipText
                entry.balance < 0 -> AdvanceChipText
                else -> PaidChipText
            }
        Text(
                text = if (entry.balance < 0) 
                    "${(entry.balance * -1).toRupees()} Cr" 
                else 
                    entry.balance.toRupees(),
                modifier = Modifier
                    .width(FullBalanceWidth)
                    .padding(start = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = balanceColor,
            textAlign = TextAlign.End
        )
        }
        
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
private fun FullTableFooter(
    totalDebit: Double,
    totalCredit: Double,
    balance: Double
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.5.dp)
        
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Empty Date
            Spacer(modifier = Modifier.width(FullDateWidth))
            
            // Total Label
            Text(
                text = "TOTAL",
                modifier = Modifier
                    .width(FullParticularsWidth)
                    .padding(end = 6.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Total Debit
            Surface(
                modifier = Modifier.width(FullDebitWidth).padding(horizontal = 4.dp),
                shape = RoundedCornerShape(6.dp),
                color = DueChipBackground.copy(alpha = 0.8f)
            ) {
                Text(
                    text = totalDebit.toRupees(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = DueChipText,
                    textAlign = TextAlign.End
                )
            }
            
            // Total Credit
            Surface(
                modifier = Modifier.width(FullCreditWidth).padding(horizontal = 4.dp),
                shape = RoundedCornerShape(6.dp),
                color = PaidChipBackground.copy(alpha = 0.8f)
            ) {
                Text(
                    text = totalCredit.toRupees(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PaidChipText,
                    textAlign = TextAlign.End
                )
            }
            
            // Final Balance
            val balanceColor = when {
                balance > 0 -> DueChipText
                balance < 0 -> AdvanceChipText
                else -> PaidChipText
            }
            val balanceBg = when {
                balance > 0 -> DueChipBackground
                balance < 0 -> AdvanceChipBackground
                else -> PaidChipBackground
            }
            Surface(
                modifier = Modifier.width(FullBalanceWidth).padding(start = 4.dp),
                shape = RoundedCornerShape(6.dp),
                color = balanceBg.copy(alpha = 0.8f)
            ) {
                Text(
                    text = if (balance < 0) 
                        "${(balance * -1).toRupees()} Cr" 
                    else 
                        balance.toRupees(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// ==================== HELPER COMPONENTS ====================

@Composable
private fun StatColumn(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StudentDetailsCard(
    fatherName: String,
    village: String,
    admissionFeePaid: Boolean,
    hasTransport: Boolean,
    transportRouteName: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Student Details",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
                // Left column
                Column(modifier = Modifier.weight(1f)) {
                    DetailItem(
                        icon = Icons.Default.Person,
                        label = "Father",
                        value = fatherName
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailItem(
                        icon = Icons.Default.Home,
                        label = "Village/Area",
                        value = village.ifBlank { "Not specified" }
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Right column
                Column(modifier = Modifier.weight(1f)) {
                    DetailItem(
                        icon = Icons.Default.School,
                        label = "Admission Fee",
                        value = if (admissionFeePaid) "Paid" else "Pending",
                        valueColor = if (admissionFeePaid) PaidChipText else DueChipText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailItem(
                        icon = Icons.Default.DirectionsBus,
                        label = "Transport",
                        value = if (hasTransport) (transportRouteName ?: "Yes") else "No",
                        valueColor = if (hasTransport) Saffron else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
        Text(
            text = label,
                style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
                style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onViewDetails: () -> Unit,
    onViewReceipts: () -> Unit,
    onCollectFee: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    hasPhoneNumber: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // View Details Button
            QuickActionButton(
                icon = Icons.Default.AccountCircle,
                label = "Details",
                onClick = onViewDetails,
                tint = Color(0xFF5C6BC0),
                backgroundColor = Color(0xFF5C6BC0).copy(alpha = 0.1f)
            )
            
            // View Receipts Button
            QuickActionButton(
                icon = Icons.Default.Receipt,
                label = "Receipts",
                onClick = onViewReceipts,
                tint = Color(0xFF7B1FA2),
                backgroundColor = Color(0xFF7B1FA2).copy(alpha = 0.1f)
            )
            
            // Collect Fee Button
            QuickActionButton(
                icon = Icons.Default.Payment,
                label = "Collect",
                onClick = onCollectFee,
                tint = PaidChipText,
                backgroundColor = PaidChipBackground.copy(alpha = 0.7f)
            )
            
            // Call Button
            QuickActionButton(
                icon = Icons.Default.Phone,
                label = "Call",
                onClick = onCall,
                tint = if (hasPhoneNumber) Saffron else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                backgroundColor = if (hasPhoneNumber) Saffron.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                enabled = hasPhoneNumber
            )
            
            // WhatsApp Button
            QuickActionButton(
                icon = Icons.Outlined.ChatBubble,
                label = "WhatsApp",
                onClick = onWhatsApp,
                tint = if (hasPhoneNumber) WhatsAppGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                backgroundColor = if (hasPhoneNumber) WhatsAppGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                enabled = hasPhoneNumber
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color,
    backgroundColor: Color,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = tint
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun PhoneSelectionDialog(
    studentName: String,
    primaryPhone: String,
    secondaryPhone: String,
    actionType: PhoneActionType,
    onPhoneSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isCall = actionType == PhoneActionType.CALL
    val accentColor = if (isCall) Saffron else WhatsAppGreen
    val icon = if (isCall) Icons.Default.Phone else Icons.Outlined.ChatBubble
    val title = if (isCall) "Select Number to Call" else "Select Number for WhatsApp"
    val subtitle = if (isCall) "Choose a number to call $studentName:" else "Choose a number to send WhatsApp message:"
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor
                )
                Spacer(Modifier.width(12.dp))
            Text(
                    text = title,
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
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Primary Phone
                if (primaryPhone.isNotBlank()) {
                    PhoneOptionCard(
                        phone = primaryPhone,
                        label = "Primary",
                        actionType = actionType,
                        onClick = { onPhoneSelected(primaryPhone) }
                    )
                }
                
                // Secondary Phone
                if (secondaryPhone.isNotBlank()) {
                    PhoneOptionCard(
                        phone = secondaryPhone,
                        label = "Secondary",
                        actionType = actionType,
                        onClick = { onPhoneSelected(secondaryPhone) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PhoneOptionCard(
    phone: String,
    label: String,
    actionType: PhoneActionType,
    onClick: () -> Unit
) {
    val isCall = actionType == PhoneActionType.CALL
    val accentColor = if (isCall) Saffron else WhatsAppGreen
    val icon = if (isCall) Icons.Default.Phone else Icons.Outlined.ChatBubble
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.1f)
) {
    Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = accentColor
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
                icon,
                contentDescription = if (isCall) "Call" else "WhatsApp",
                tint = accentColor
            )
        }
    }
}

/**
 * Opens WhatsApp with pre-filled message for fee reminder
 */
private fun openWhatsApp(
    context: android.content.Context,
    phone: String,
    studentName: String,
    className: String,
    dueAmount: Double
) {
    val message = buildWhatsAppMessage(studentName, className, dueAmount)
    val phoneNumber = formatPhoneForWhatsApp(phone)
    val encodedMessage = Uri.encode(message)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://wa.me/$phoneNumber?text=$encodedMessage")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Builds bilingual WhatsApp message (Hindi first, then English)
 */
private fun buildWhatsAppMessage(
    studentName: String,
    className: String,
    dueAmount: Double
): String {
    val formattedAmount = dueAmount.toRupees()
    
    return buildString {
        // Hindi message
        appendLine("नमस्कार,")
        appendLine()
        appendLine("यह संदेश $studentName (कक्षा: $className) के अभिभावक को भेजा जा रहा है।")
        appendLine()
        appendLine("हमारे रिकॉर्ड के अनुसार, आपके बच्चे की बकाया फीस $formattedAmount है।")
        appendLine()
        appendLine("कृपया जल्द से जल्द भुगतान करें।")
        appendLine()
        appendLine("यदि आपने पहले ही भुगतान कर दिया है, तो कृपया इस संदेश को अनदेखा करें।")
        appendLine()
        appendLine("धन्यवाद।")
        appendLine()
        appendLine("---")
        appendLine()
        // English message
        appendLine("Hello,")
        appendLine()
        appendLine("This message is for the parent/guardian of $studentName (Class: $className).")
        appendLine()
        appendLine("As per our records, the pending fee amount is $formattedAmount.")
        appendLine()
        appendLine("Please clear the dues at your earliest convenience.")
        appendLine()
        appendLine("If you have already paid, please ignore this message.")
        appendLine()
        appendLine("Thank you.")
    }
}

/**
 * Formats phone number for WhatsApp (adds country code if needed)
 */
private fun formatPhoneForWhatsApp(phone: String): String {
    val cleaned = phone.replace(Regex("[^0-9]"), "")
    return if (cleaned.startsWith("91")) cleaned
    else if (cleaned.length == 10) "91$cleaned"
    else cleaned
}
