package com.navoditpublic.fees.presentation.screens.students.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.navigation.Screen
import com.navoditpublic.fees.presentation.theme.ErrorRed
import com.navoditpublic.fees.presentation.theme.PaidChipBackground
import com.navoditpublic.fees.presentation.theme.PaidChipText
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark
import com.navoditpublic.fees.presentation.theme.SuccessGreen
import com.navoditpublic.fees.util.DateUtils
import com.navoditpublic.fees.util.toRupees
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    studentId: Long,
    navController: NavController,
    viewModel: StudentDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showMenu by remember { mutableStateOf(false) }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is StudentDetailEvent.StudentDeleted -> {
                    navController.popBackStack()
                }
                is StudentDetailEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    // Dialogs
    if (state.showInactiveDialog) {
        MarkInactiveDialog(
            studentName = state.student?.name ?: "",
            onConfirm = viewModel::markInactive,
            onDismiss = viewModel::dismissInactiveDialog
        )
    }
    
    if (state.showReactivateDialog) {
        ReactivateDialog(
            studentName = state.student?.name ?: "",
            onConfirm = viewModel::reactivate,
            onDismiss = viewModel::dismissReactivateDialog
        )
    }
    
    if (state.showDeleteDialog) {
        DeleteStudentDialog(
            studentName = state.student?.name ?: "",
            onConfirm = viewModel::deleteStudent,
            onDismiss = viewModel::dismissDeleteDialog
        )
    }
    
    if (state.showCannotDeleteDialog) {
        CannotDeleteDialog(
            onDismiss = viewModel::dismissCannotDeleteDialog
        )
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else if (state.student == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Student not found")
            }
        } else {
            val student = state.student!!
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 16.dp)
            ) {
                // Header with gradient
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Saffron, SaffronDark)
                                )
                            )
                    ) {
                        // Decorative circles
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .offset(x = (-30).dp, y = (-20).dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 20.dp, y = 10.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        )
                        
                        // Top bar - compact padding with status bar inset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Text(
                                "Student Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Row {
                                IconButton(onClick = { 
                                    navController.navigate(Screen.AddEditStudent.createRoute(studentId))
                                }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.White
                                    )
                                }
                                
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "More options",
                                            tint = Color.White
                                        )
                                    }
                                    
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        // Mark Inactive / Reactivate option
                                        if (student.isActive) {
                                            DropdownMenuItem(
                                                text = { 
                                                    Text(
                                                        "Mark as Inactive",
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    ) 
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.PersonOff,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    viewModel.showInactiveDialog()
                                                }
                                            )
                                        } else {
                                            DropdownMenuItem(
                                                text = { 
                                                    Text(
                                                        "Reactivate",
                                                        color = SuccessGreen
                                                    ) 
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.PersonAdd,
                                                        contentDescription = null,
                                                        tint = SuccessGreen
                                                    )
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    viewModel.showReactivateDialog()
                                                }
                                            )
                                        }
                                        
                                        // Delete option
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    "Delete Student",
                                                    color = if (state.canDelete) ErrorRed else Color.Gray
                                                ) 
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = if (state.canDelete) ErrorRed else Color.Gray
                                                )
                                            },
                                            onClick = {
                                                showMenu = false
                                                viewModel.showDeleteDialog()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Compact Profile Card - Horizontal Layout
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar on left
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(Saffron, SaffronDark)
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(66.dp)
                                        .clip(CircleShape)
                                        .background(Saffron.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.name.take(2).uppercase(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Saffron
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Info on right
                            Column(modifier = Modifier.weight(1f)) {
                                // Name
                            Text(
                                text = student.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Class & Account
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PillBadge(
                                        text = "${student.currentClass}-${student.section}",
                                        isPrimary = true
                                    )
                                    PillBadge(
                                        text = student.accountNumber,
                                        isPrimary = false
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // SR Number & Status badges
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                            Text(
                                        text = "SR: ${student.srNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                                    // Admission Fee Status
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (student.admissionFeePaid) 
                                            PaidChipBackground 
                                        else 
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = if (student.admissionFeePaid) "✓ Adm" else "✗ Adm",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (student.admissionFeePaid) 
                                                PaidChipText 
                                            else 
                                                MaterialTheme.colorScheme.error
                                        )
                                    }
                                    
                                    // Inactive badge
                                    if (!student.isActive) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                                text = "Inactive",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Balance Summary Card
                item {
                    BalanceSummaryCard(
                        totalCharged = state.totalDebits,
                        totalPaid = state.totalCredits,
                        currentBalance = state.currentBalance,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                // Action Buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PremiumActionButton(
                            text = "Collect Fee",
                            icon = Icons.Default.Payment,
                            isPrimary = true,
                            onClick = { 
                                navController.navigate(Screen.CollectFee.createRoute(studentId))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        PremiumActionButton(
                            text = "View Ledger",
                            icon = Icons.Default.Receipt,
                            isPrimary = false,
                            onClick = { 
                                navController.navigate(Screen.StudentLedger.createRoute(studentId))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Transport Section
                if (state.transportRoute != null) {
                    item {
                        SectionTitle(
                            title = "Transport",
                            icon = Icons.Default.DirectionsBus,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    
                    item {
                        Card(
                            onClick = { navController.navigate(Screen.TransportQuick.createRoute(studentId = studentId, action = "manage")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Saffron.copy(alpha = 0.1f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                        .clip(CircleShape)
                                        .background(Saffron.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.DirectionsBus,
                                        contentDescription = null,
                                        tint = Saffron,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(Modifier.width(12.dp))
                                
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            state.transportRoute!!.routeName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF22C55E).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "● Active",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF22C55E),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Text(
                                        state.transportRoute!!.getFeeForClass(student.currentClass).toRupees() + "/month",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Saffron.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Manage",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Saffron
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            contentDescription = null,
                                            tint = Saffron,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (student.hasTransport == false) {
                    // Show "Add Transport" option for students without transport
                    item {
                        SectionTitle(
                            title = "Transport",
                            icon = Icons.Default.DirectionsBus,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    
                    item {
                        Card(
                            onClick = { navController.navigate(Screen.TransportQuick.createRoute(studentId = studentId, action = "enroll")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF1F5F9)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                        .clip(CircleShape)
                                        .background(Color(0xFFE2E8F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.DirectionsBus,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(Modifier.width(12.dp))
                                
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "No Transport",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray
                                    )
                                    Text(
                                        "Tap to enroll in transport",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray.copy(alpha = 0.7f)
                                    )
                                }
                                
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Saffron.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Add",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Saffron
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            contentDescription = null,
                                            tint = Saffron,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Academic Info Section
                item {
                    SectionTitle(
                        title = "Academic Info",
                        icon = Icons.Default.AccountBalance,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
                    )
                }
                
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Row 1: SR Number, Account
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                AcademicInfoChip(
                                    label = "SR Number",
                                    value = student.srNumber,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                                
                                AcademicInfoChip(
                                    label = "Account No.",
                                    value = student.accountNumber,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            
                            // Row 2: Session, Admission Date
                            Row(
                        modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                AcademicInfoChip(
                                    label = "Session",
                                    value = state.admissionSessionName.ifBlank { "-" },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                                
                                AcademicInfoChip(
                                    label = "Admission Date",
                                    value = DateUtils.formatDate(student.admissionDate),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                // Family & Contact Section
                item {
                    SectionTitle(
                        title = "Family & Contact",
                        icon = Icons.Default.Person,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
                    )
                }
                
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            PremiumInfoRow(
                                icon = Icons.Default.Person,
                                label = "Father",
                                value = student.fatherName,
                                showDivider = true
                            )
                            
                            if (student.motherName.isNotBlank()) {
                                PremiumInfoRow(
                                    icon = Icons.Default.Person,
                                    label = "Mother",
                                    value = student.motherName,
                                    showDivider = true
                                )
                            }
                            
                            PremiumInfoRow(
                                icon = Icons.Default.Phone,
                                label = "Primary Contact",
                                value = student.phonePrimary,
                                showDivider = student.phoneSecondary.isNotBlank() || student.fullAddress.isNotBlank(),
                                actionIcon = Icons.Default.Call,
                                onActionClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${student.phonePrimary}")
                                    }
                                    context.startActivity(intent)
                                },
                                secondaryActionIcon = Icons.Default.Sms,
                                onSecondaryActionClick = {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("sms:${student.phonePrimary}")
                                    }
                                    context.startActivity(intent)
                                }
                            )
                            
                            if (student.phoneSecondary.isNotBlank()) {
                                PremiumInfoRow(
                                    icon = Icons.Default.Phone,
                                    label = "Alternate Contact",
                                    value = student.phoneSecondary,
                                    showDivider = student.fullAddress.isNotBlank(),
                                    actionIcon = Icons.Default.Call,
                                    onActionClick = {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${student.phoneSecondary}")
                                        }
                                        context.startActivity(intent)
                                    }
                                )
                            }
                            
                            if (student.fullAddress.isNotBlank()) {
                                PremiumInfoRow(
                                    icon = Icons.Default.Home,
                                    label = "Address",
                                    value = student.fullAddress,
                                    showDivider = false,
                                    actionIcon = Icons.Default.ContentCopy,
                                    onActionClick = {
                                        clipboardManager.setText(AnnotatedString(student.fullAddress))
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Opening Balance Section (only if has opening balance)
                if (student.openingBalance != 0.0) {
                    item {
                        SectionTitle(
                            title = "Opening Balance",
                            icon = Icons.Default.AccountBalance,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
                        )
                    }
                    
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (student.openingBalance > 0) 
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                else 
                                    PaidChipBackground.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Amount Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Amount",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (student.openingBalance > 0) 
                                            student.openingBalance.toRupees() + " Due"
                                        else 
                                            (student.openingBalance * -1).toRupees() + " Advance",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (student.openingBalance > 0) 
                                            MaterialTheme.colorScheme.error
                                        else 
                                            PaidChipText
                                    )
                                }
                                
                                // Date Row (if available)
                                if (student.openingBalanceDate != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Date",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            DateUtils.formatDate(student.openingBalanceDate!!),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                
                                // Remarks Row (if available)
                                if (student.openingBalanceRemarks.isNotBlank()) {
                                    Column {
                                        Text(
                                            "Remarks",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            student.openingBalanceRemarks,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Quick Actions Bar
                item {
                    Spacer(Modifier.height(24.dp))
                    
                    QuickActionsBar(
                        onCallClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${student.phonePrimary}")
                            }
                            context.startActivity(intent)
                        },
                        onSmsClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("sms:${student.phonePrimary}")
                            }
                            context.startActivity(intent)
                        },
                        onLedgerClick = {
                            navController.navigate(Screen.StudentLedger.createRoute(studentId))
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

// ==================== COMPONENTS ====================

@Composable
private fun PillBadge(
    text: String,
    isPrimary: Boolean
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isPrimary) Saffron.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = if (isPrimary) androidx.compose.foundation.BorderStroke(1.dp, Saffron.copy(alpha = 0.3f)) else null
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isPrimary) Saffron else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BalanceSummaryCard(
    totalCharged: Double,
    totalPaid: Double,
    currentBalance: Double,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    val hasDues = currentBalance > 0
    val hasAdvance = currentBalance < 0
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Saffron.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Balance Summary",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(200))
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    
                    // Charged vs Paid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Charged
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Charged",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                totalCharged.toRupees(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(50.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        
                        // Paid
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = PaidChipText.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                    Text(
                                    "Paid",
                                    style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                            }
                            Spacer(Modifier.height(4.dp))
                                    Text(
                                totalPaid.toRupees(),
                                style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = PaidChipText
                                    )
                                }
                            }
                            
                    Spacer(Modifier.height(16.dp))
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Current Balance
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                            "Current Balance",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                hasDues -> MaterialTheme.colorScheme.errorContainer
                                hasAdvance -> PaidChipBackground
                                else -> PaidChipBackground
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!hasDues && !hasAdvance) {
                                    Text(
                                        "✓",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = PaidChipText
                                    )
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text(
                                    text = when {
                                        hasDues -> currentBalance.toRupees() + " Due"
                                        hasAdvance -> (currentBalance * -1).toRupees() + " Advance"
                                        else -> "No Dues"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        hasDues -> MaterialTheme.colorScheme.error
                                        else -> PaidChipText
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumActionButton(
    text: String,
    icon: ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isPrimary) Saffron else Color.Transparent,
        border = if (!isPrimary) androidx.compose.foundation.BorderStroke(1.5.dp, Saffron) else null,
        shadowElevation = if (isPrimary) 2.dp else 0.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isPrimary) Color.White else Saffron,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isPrimary) Color.White else Saffron
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Saffron,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun PremiumInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    showDivider: Boolean,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    secondaryActionIcon: ImageVector? = null,
    onSecondaryActionClick: (() -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Saffron.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.width(14.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            if (actionIcon != null && onActionClick != null) {
                Surface(
                    shape = CircleShape,
                    color = Saffron.copy(alpha = 0.1f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onActionClick)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            actionIcon,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            if (secondaryActionIcon != null && onSecondaryActionClick != null) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onSecondaryActionClick)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            secondaryActionIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 70.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun AcademicInfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun QuickActionsBar(
    onCallClick: () -> Unit,
    onSmsClick: () -> Unit,
    onLedgerClick: () -> Unit,
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
    Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionItem(
                icon = Icons.Default.Call,
                label = "Call",
                onClick = onCallClick
            )
            QuickActionItem(
                icon = Icons.Default.Sms,
                label = "SMS",
                onClick = onSmsClick
            )
            QuickActionItem(
                icon = Icons.Default.Receipt,
                label = "Ledger",
                onClick = onLedgerClick
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Saffron.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Saffron,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== Status Management Dialogs ====================

@Composable
private fun MarkInactiveDialog(
    studentName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.PersonOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Mark as Inactive?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Are you sure you want to mark $studentName as inactive?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "They will no longer appear in:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "• Fee collection search\n• Reports and statistics\n• Session promotions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Their data will be preserved and they can be reactivated later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Mark Inactive")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ReactivateDialog(
    studentName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Reactivate Student?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Are you sure you want to reactivate $studentName?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "They will appear in active student lists again and can be included in fee collection and reports.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen
                )
            ) {
                Text("Reactivate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DeleteStudentDialog(
    studentName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(ErrorRed.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                "Permanently Delete?",
                fontWeight = FontWeight.Bold,
                color = ErrorRed
            )
        },
        text = {
            Column {
                Text(
                    "This will permanently delete $studentName and all their data.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = ErrorRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "This action CANNOT be undone!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Their SR number and account number will become available for reuse.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Delete Forever")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CannotDeleteDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Cannot Delete",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "This student has financial records (fees charged or payments received).",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Students with financial history cannot be permanently deleted to preserve data integrity.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "You can mark them as inactive instead.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
