package com.navoditpublic.fees.presentation.screens.receipt

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.domain.usecase.SessionAccessLevel
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.util.NumberToWords
import com.navoditpublic.fees.util.toRupees
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Premium Colors
private val CashGreen = Color(0xFF10B981)
private val CashGreenLight = Color(0xFFD1FAE5)
private val OnlineBlue = Color(0xFF3B82F6)
private val OnlineBlueLight = Color(0xFFDBEAFE)
private val CancelledRed = Color(0xFFEF4444)
private val CancelledRedLight = Color(0xFFFEE2E2)
private val PaperCream = Color(0xFFFFFBF5)
private val PaperShadow = Color(0xFFF5EFE6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(
    receiptId: Long,
    navController: NavController,
    viewModel: ReceiptDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    var animateContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(receiptId) {
        viewModel.loadReceipt(receiptId)
    }
    
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && state.receipt != null) {
            animateContent = true
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ReceiptDetailEvent.Success -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    showCancelDialog = false
                }
                is ReceiptDetailEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                is ReceiptDetailEvent.BalanceAdjusted -> {
                    Toast.makeText(
                        context, 
                        "Opening balance adjusted to ₹${String.format("%.0f", event.newBalance)}", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    // Session Warning Dialog
    if (state.showSessionWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSessionWarning() },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    "Previous Session",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = state.sessionWarningMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissSessionWarning() },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("I Understand", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
    
    // Edit Receipt Dialog
    if (state.isEditMode) {
        EditReceiptDialog(
            currentAmount = state.editAmount,
            paymentMode = state.editPaymentMode,
            onlineReference = state.editOnlineReference,
            remarks = state.editRemarks,
            isSaving = state.isSaving,
            isPreviousSession = state.sessionAccessLevel == SessionAccessLevel.PREVIOUS_SESSION,
            onAmountChange = { viewModel.updateEditAmount(it) },
            onPaymentModeChange = { viewModel.updateEditPaymentMode(it) },
            onOnlineReferenceChange = { viewModel.updateEditOnlineReference(it) },
            onRemarksChange = { viewModel.updateEditRemarks(it) },
            onSave = { viewModel.saveEdit() },
            onDismiss = { viewModel.exitEditMode() }
        )
    }
    
    // Cancel Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CancelledRedLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = null,
                        tint = CancelledRed,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = { 
                Text(
                    "Cancel Receipt?",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = {
                val cancelFocusManager = LocalFocusManager.current
                Column {
                    Text(
                        text = "This will cancel receipt #${state.receipt?.receiptNumber} and reverse the payment in the student's ledger.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason for cancellation") },
                        placeholder = { Text("e.g., Duplicate receipt") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { cancelFocusManager.clearFocus() }
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.cancelReceipt(cancelReason) },
                    colors = ButtonDefaults.buttonColors(containerColor = CancelledRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel Receipt", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Go Back", color = Color.Gray)
                }
            }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperCream)
    ) {
        if (state.isLoading) {
            LoadingScreen()
        } else if (state.receipt == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Receipt not found", color = Color.Gray)
            }
        } else {
            val receipt = state.receipt!!
            val isCancelled = receipt.isCancelled
            val isCash = receipt.paymentMode == PaymentMode.CASH
            
            val accentColor = when {
                isCancelled -> CancelledRed
                isCash -> CashGreen
                else -> OnlineBlue
            }
            
            Column(modifier = Modifier.fillMaxSize()) {
                // Fixed Header at top - no gap with status bar
                ReceiptHeader(
                    receiptNumber = receipt.receiptNumber,
                    isCancelled = isCancelled,
                    accentColor = accentColor,
                    onBackClick = { navController.popBackStack() }
                )
                
                // Scrollable content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Receipt Paper
                    item {
                        AnimatedVisibility(
                            visible = animateContent,
                            enter = fadeIn(tween(150)) + slideInVertically(tween(150)) { 30 }
                        ) {
                            ReceiptPaper(
                                receipt = receipt,
                                items = state.receiptItems,
                                dateFormat = dateFormat,
                                timeFormat = timeFormat,
                                accentColor = accentColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                    
                    // Student Actions Card
                    item {
                        AnimatedVisibility(
                            visible = animateContent,
                            enter = fadeIn(tween(150, 50)) + slideInVertically(tween(150, 50)) { 30 }
                        ) {
                            StudentActionsCard(
                                studentName = receipt.studentName ?: "Unknown",
                                className = "${receipt.studentClass ?: ""} ${receipt.studentSection ?: ""}".trim(),
                                onViewProfile = {
                                    navController.navigate(Screen.StudentDetail.createRoute(receipt.studentId))
                                },
                                onViewLedger = {
                                    navController.navigate(Screen.StudentLedger.createRoute(receipt.studentId))
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                    
                    // Remarks (if any)
                    if (receipt.remarks?.isNotBlank() == true) {
                        item {
                            AnimatedVisibility(
                                visible = animateContent,
                                enter = fadeIn(tween(150, 100)) + slideInVertically(tween(150, 100)) { 30 }
                            ) {
                                RemarksCard(
                                    remarks = receipt.remarks!!,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    
                    // Cancellation Reason
                    if (isCancelled && receipt.cancellationReason?.isNotBlank() == true) {
                        item {
                            AnimatedVisibility(
                                visible = animateContent,
                                enter = fadeIn(tween(150, 100)) + slideInVertically(tween(150, 100)) { 30 }
                            ) {
                                CancellationReasonCard(
                                    reason = receipt.cancellationReason!!,
                                    cancelledAt = receipt.cancelledAt,
                                    dateFormat = dateFormat,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    
                    // Action Buttons (Edit & Cancel)
                    if (!isCancelled) {
                        item {
                            AnimatedVisibility(
                                visible = animateContent,
                                enter = fadeIn(tween(150, 150)) + slideInVertically(tween(150, 150)) { 30 }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Session access indicator
                                    if (state.sessionAccessLevel != SessionAccessLevel.FULL_ACCESS) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = when (state.sessionAccessLevel) {
                                                SessionAccessLevel.PREVIOUS_SESSION -> Color(0xFFFFF3E0)
                                                SessionAccessLevel.READ_ONLY -> CancelledRedLight
                                                else -> Color.Transparent
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = when (state.sessionAccessLevel) {
                                                        SessionAccessLevel.PREVIOUS_SESSION -> Color(0xFFFF9800)
                                                        SessionAccessLevel.READ_ONLY -> CancelledRed
                                                        else -> Color.Gray
                                                    },
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = when (state.sessionAccessLevel) {
                                                        SessionAccessLevel.PREVIOUS_SESSION -> 
                                                            "Previous session - changes will auto-adjust opening balance"
                                                        SessionAccessLevel.READ_ONLY -> 
                                                            "Read-only - this session cannot be edited"
                                                        else -> ""
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = when (state.sessionAccessLevel) {
                                                        SessionAccessLevel.PREVIOUS_SESSION -> Color(0xFFE65100)
                                                        SessionAccessLevel.READ_ONLY -> CancelledRed
                                                        else -> Color.Gray
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Edit Button (disabled for read-only sessions)
                                    if (state.sessionAccessLevel != SessionAccessLevel.READ_ONLY) {
                                        Button(
                                            onClick = { viewModel.enterEditMode() },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = OnlineBlue.copy(alpha = 0.1f),
                                                contentColor = OnlineBlue
                                            ),
                                            contentPadding = PaddingValues(vertical = 16.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Edit Receipt", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    
                                    // Cancel Button (disabled for read-only sessions)
                                    if (state.sessionAccessLevel != SessionAccessLevel.READ_ONLY) {
                                        Button(
                                            onClick = { showCancelDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = CancelledRed.copy(alpha = 0.1f),
                                                contentColor = CancelledRed
                                            ),
                                            contentPadding = PaddingValues(vertical = 16.dp)
                                        ) {
                                            Icon(Icons.Default.Cancel, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Cancel This Receipt", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// HEADER - Compact App Bar Style
// ============================================================================

@Composable
private fun ReceiptHeader(
    receiptNumber: Int,
    isCancelled: Boolean,
    accentColor: Color,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Saffron,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            // Receipt icon + number
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Receipt #$receiptNumber",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Fee Receipt Details",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Status badge (if cancelled)
            if (isCancelled) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "CANCELLED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

// ============================================================================
// RECEIPT PAPER
// ============================================================================

@Composable
private fun ReceiptPaper(
    receipt: com.navoditpublic.fees.domain.model.Receipt,
    items: List<com.navoditpublic.fees.domain.model.ReceiptItem>,
    dateFormat: SimpleDateFormat,
    timeFormat: SimpleDateFormat,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val isCash = receipt.paymentMode == PaymentMode.CASH
    val isCancelled = receipt.isCancelled
    
    // Animation - fast amount counter
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatedProgress.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
    }
    val displayAmount = receipt.totalAmount * animatedProgress.value
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = PaperShadow),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Paper texture top edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.8f),
                                accentColor,
                                accentColor.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
            
            Column(modifier = Modifier.padding(20.dp)) {
                // Date & Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoBadge(
                        icon = Icons.Default.DateRange,
                        label = "Date",
                        value = dateFormat.format(Date(receipt.receiptDate)),
                        color = Color.Gray
                    )
                    InfoBadge(
                        icon = Icons.Default.DateRange,
                        label = "Time",
                        value = timeFormat.format(Date(receipt.receiptDate)),
                        color = Color.Gray,
                        alignment = Alignment.End
                    )
                }
                
                Spacer(Modifier.height(20.dp))
                
                // Total Amount - Hero
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (isCancelled) 
                                    listOf(CancelledRedLight, CancelledRedLight.copy(alpha = 0.5f))
                                else 
                                    listOf(accentColor.copy(alpha = 0.1f), accentColor.copy(alpha = 0.05f))
                            )
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Amount",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isCancelled) CancelledRed else Color.Gray
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "₹${String.format("%,.0f", displayAmount)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCancelled) CancelledRed else accentColor
                        )
                        
                        if (receipt.discountAmount > 0 && !isCancelled) {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CashGreenLight
                            ) {
                                Text(
                                    text = "Includes ${receipt.discountAmount.toRupees()} bonus",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CashGreen,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Amount in Words
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = PaperCream
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Amount in Words",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = NumberToWords.convert(receipt.totalAmount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = PaperShadow)
                Spacer(Modifier.height(20.dp))
                
                // Payment Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Payment Mode
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCash) CashGreenLight else OnlineBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isCash) Icons.Default.Money else Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = if (isCash) CashGreen else OnlineBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Payment Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = if (isCash) "Cash" else "Online",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCash) CashGreen else OnlineBlue
                            )
                        }
                    }
                    
                    // Reference Number (if online)
                    if (!isCash && receipt.onlineReference?.isNotBlank() == true) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Reference",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = receipt.onlineReference!!,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
                
                // Fee Items
                if (items.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = PaperShadow)
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        text = "Fee Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    items.forEachIndexed { index, item ->
                        FeeItemRow(
                            item = item,
                            isLast = index == items.lastIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBadge(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        horizontalAlignment = alignment
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
private fun FeeItemRow(
    item: com.navoditpublic.fees.domain.model.ReceiptItem,
    isLast: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                if (item.months.isNotBlank()) {
                    Text(
                        text = item.months,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Text(
                text = item.amount.toRupees(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Saffron
            )
        }
        if (!isLast) {
            HorizontalDivider(color = PaperShadow.copy(alpha = 0.5f))
        }
    }
}

// ============================================================================
// STUDENT ACTIONS CARD
// ============================================================================

@Composable
private fun StudentActionsCard(
    studentName: String,
    className: String,
    onViewProfile: () -> Unit,
    onViewLedger: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Student Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Saffron, SaffronDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = studentName
                            .split(" ")
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .joinToString(""),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (className.isNotBlank()) {
                        Text(
                            text = "Class $className",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudentActionButton(
                    icon = Icons.Default.Person,
                    label = "View Profile",
                    color = OnlineBlue,
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f)
                )
                StudentActionButton(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "View Ledger",
                    color = CashGreen,
                    onClick = onViewLedger,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StudentActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ============================================================================
// REMARKS CARD
// ============================================================================

@Composable
private fun RemarksCard(
    remarks: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Numbers,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Remarks",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = remarks,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
    }
}

// ============================================================================
// CANCELLATION REASON CARD
// ============================================================================

@Composable
private fun CancellationReasonCard(
    reason: String,
    cancelledAt: Long?,
    dateFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CancelledRedLight.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = null,
                    tint = CancelledRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Cancellation Reason",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CancelledRed
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            if (cancelledAt != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Cancelled on ${dateFormat.format(Date(cancelledAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// ============================================================================
// EDIT RECEIPT DIALOG
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditReceiptDialog(
    currentAmount: String,
    paymentMode: PaymentMode,
    onlineReference: String,
    remarks: String,
    isSaving: Boolean,
    isPreviousSession: Boolean,
    onAmountChange: (String) -> Unit,
    onPaymentModeChange: (PaymentMode) -> Unit,
    onOnlineReferenceChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(OnlineBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = OnlineBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Edit Receipt",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Previous session warning
                if (isPreviousSession) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3E0)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Opening balance will be automatically adjusted",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }
                
                // Amount Field
                OutlinedTextField(
                    value = currentAmount,
                    onValueChange = onAmountChange,
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.clearFocus() }
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Text(
                            "₹",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Saffron
                        )
                    }
                )
                
                // Payment Mode
                Text(
                    text = "Payment Mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = paymentMode == PaymentMode.CASH,
                        onClick = { onPaymentModeChange(PaymentMode.CASH) },
                        label = { Text("Cash") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Money,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CashGreenLight,
                            selectedLabelColor = CashGreen,
                            selectedLeadingIconColor = CashGreen
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = paymentMode == PaymentMode.ONLINE,
                        onClick = { onPaymentModeChange(PaymentMode.ONLINE) },
                        label = { Text("Online") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OnlineBlueLight,
                            selectedLabelColor = OnlineBlue,
                            selectedLeadingIconColor = OnlineBlue
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Online Reference (if online mode)
                if (paymentMode == PaymentMode.ONLINE) {
                    OutlinedTextField(
                        value = onlineReference,
                        onValueChange = onOnlineReferenceChange,
                        label = { Text("Reference Number") },
                        placeholder = { Text("Transaction ID / UPI Ref") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
                
                // Remarks
                OutlinedTextField(
                    value = remarks,
                    onValueChange = onRemarksChange,
                    label = { Text("Remarks / Particulars") },
                    placeholder = { Text("e.g., April-June Tuition") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving && currentAmount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (isSaving) "Saving..." else "Save Changes",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
