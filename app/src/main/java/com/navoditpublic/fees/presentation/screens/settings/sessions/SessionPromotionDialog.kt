package com.navoditpublic.fees.presentation.screens.settings.sessions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.navoditpublic.fees.domain.model.PromotionOptions
import com.navoditpublic.fees.domain.model.PromotionPreview
import com.navoditpublic.fees.domain.model.PromotionProgress
import com.navoditpublic.fees.domain.model.PromotionResult
import com.navoditpublic.fees.util.ClassUtils
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SessionPromotionDialog(
    sourceSessionName: String,
    targetSessionName: String,
    preview: PromotionPreview?,
    progress: PromotionProgress?,
    result: PromotionResult?,
    isPromoting: Boolean,
    onExecute: (PromotionOptions) -> Unit,
    onDismiss: () -> Unit
) {
    // Options state
    var copyFeeStructures by remember { mutableStateOf(true) }
    var carryForwardDues by remember { mutableStateOf(true) }
    var promoteClasses by remember { mutableStateOf(true) }
    var deactivate12th by remember { mutableStateOf(false) }
    var addTuitionFees by remember { mutableStateOf(true) }
    var addTransportFees by remember { mutableStateOf(true) }
    var setAsCurrent by remember { mutableStateOf(true) }
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    Dialog(
        onDismissRequest = { if (!isPromoting) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isPromoting,
            dismissOnClickOutside = !isPromoting,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = screenHeight * 0.85f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = sourceSessionName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = targetSessionName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = "Session Promotion Wizard",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show different content based on state
                when {
                    result != null -> {
                        // Show result
                        PromotionResultContent(
                            result = result,
                            currencyFormat = currencyFormat,
                            onDismiss = onDismiss
                        )
                    }
                    isPromoting || progress?.error != null -> {
                        // Show progress or error state
                        PromotionProgressContent(
                            progress = progress,
                            onDismiss = onDismiss
                        )
                    }
                    else -> {
                        // Show options
                        PromotionOptionsContent(
                            preview = preview,
                            currencyFormat = currencyFormat,
                            copyFeeStructures = copyFeeStructures,
                            onCopyFeeStructuresChange = { copyFeeStructures = it },
                            carryForwardDues = carryForwardDues,
                            onCarryForwardDuesChange = { carryForwardDues = it },
                            promoteClasses = promoteClasses,
                            onPromoteClassesChange = { promoteClasses = it },
                            deactivate12th = deactivate12th,
                            onDeactivate12thChange = { deactivate12th = it },
                            addTuitionFees = addTuitionFees,
                            onAddTuitionFeesChange = { addTuitionFees = it },
                            addTransportFees = addTransportFees,
                            onAddTransportFeesChange = { addTransportFees = it },
                            setAsCurrent = setAsCurrent,
                            onSetAsCurrentChange = { setAsCurrent = it },
                            onExecute = {
                                onExecute(
                                    PromotionOptions(
                                        copyFeeStructures = copyFeeStructures,
                                        carryForwardDues = carryForwardDues,
                                        promoteClasses = promoteClasses,
                                        deactivate12thStudents = deactivate12th,
                                        addTuitionFees = addTuitionFees,
                                        addTransportFees = addTransportFees,
                                        setAsCurrent = setAsCurrent
                                    )
                                )
                            },
                            onDismiss = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PromotionOptionsContent(
    preview: PromotionPreview?,
    currencyFormat: NumberFormat,
    copyFeeStructures: Boolean,
    onCopyFeeStructuresChange: (Boolean) -> Unit,
    carryForwardDues: Boolean,
    onCarryForwardDuesChange: (Boolean) -> Unit,
    promoteClasses: Boolean,
    onPromoteClassesChange: (Boolean) -> Unit,
    deactivate12th: Boolean,
    onDeactivate12thChange: (Boolean) -> Unit,
    addTuitionFees: Boolean,
    onAddTuitionFeesChange: (Boolean) -> Unit,
    addTransportFees: Boolean,
    onAddTransportFeesChange: (Boolean) -> Unit,
    setAsCurrent: Boolean,
    onSetAsCurrentChange: (Boolean) -> Unit,
    onExecute: () -> Unit,
    onDismiss: () -> Unit
) {
    // Preview section
    if (preview != null) {
        // Student Overview Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊 Student Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Total students with highlight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Active Students",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${preview.totalStudents}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Class-wise breakdown chips
                if (preview.classWiseCounts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Class-wise Distribution",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Sort classes in order
                        ClassUtils.ALL_CLASSES
                            .filter { preview.classWiseCounts.containsKey(it) }
                            .forEach { className ->
                                val count = preview.classWiseCounts[className] ?: 0
                                AssistChip(
                                    onClick = { },
                                    label = { 
                                        Text(
                                            "$className: $count",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (className == "12th") 
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                    }
                }
                
                if (preview.studentsIn12th > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${preview.studentsIn12th} students in 12th will pass out",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Financial Overview Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💰 Financial Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Pending dues
                if (preview.studentsWithDues > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Pending Dues",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${preview.studentsWithDues} students have dues",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = currencyFormat.format(preview.totalDuesAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Text(
                        text = "✓ No pending dues",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // Fee structures explanation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fee Structures to Copy",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Each class has multiple fee types (Monthly, Annual, Admission, etc.)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${preview.feeStructuresCount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // Transport enrollment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Transport Enrolled",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Will get transport fees in new session",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${preview.studentsWithTransport}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Options section
    Text(
        text = "Promotion Options",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // Option toggles
    OptionToggle(
        title = "Copy Fee Structures",
        description = "Copy tuition fees from previous session",
        checked = copyFeeStructures,
        onCheckedChange = onCopyFeeStructuresChange
    )
    
    OptionToggle(
        title = "Carry Forward Dues",
        description = if (preview != null) {
            "${preview.studentsWithDues} students with ${currencyFormat.format(preview.totalDuesAmount)} dues"
        } else {
            "Transfer pending dues as opening balance"
        },
        checked = carryForwardDues,
        onCheckedChange = onCarryForwardDuesChange
    )
    
    OptionToggle(
        title = "Promote Classes",
        description = "Move all students to next class (NC→LKG→...→12th)",
        checked = promoteClasses,
        onCheckedChange = onPromoteClassesChange
    )
    
    AnimatedVisibility(visible = promoteClasses) {
        Column {
            OptionToggle(
                title = "Deactivate 12th Class Students",
                description = if (preview != null) {
                    "Mark ${preview.studentsIn12th} students as passed out"
                } else {
                    "Mark 12th class students as inactive"
                },
                checked = deactivate12th,
                onCheckedChange = onDeactivate12thChange,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
    
    OptionToggle(
        title = "Add Tuition Fees",
        description = "Add monthly tuition fees for all students",
        checked = addTuitionFees,
        onCheckedChange = onAddTuitionFeesChange
    )
    
    OptionToggle(
        title = "Add Transport Fees",
        description = if (preview != null) {
            "Add transport fees for ${preview.studentsWithTransport} enrolled students"
        } else {
            "Add transport fees for enrolled students"
        },
        checked = addTransportFees,
        onCheckedChange = onAddTransportFeesChange
    )
    
    OptionToggle(
        title = "Set as Current Session",
        description = "Make this the active session after promotion",
        checked = setAsCurrent,
        onCheckedChange = onSetAsCurrentChange
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Warning
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "This action can be reverted, but it's recommended to backup your data first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Action buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onExecute,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Start Promotion")
        }
    }
}

@Composable
private fun PromotionProgressContent(
    progress: PromotionProgress?,
    onDismiss: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Show error state if there's an error
        if (progress?.error != null) {
            // Error icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(56.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Promotion Failed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = progress.error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Warning about partial state
            if (progress.percentComplete > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Partial Changes Made",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The promotion was ${progress.percentComplete}% complete when it failed. " +
                                   "Some changes may have been applied to your data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Suggestion: Use the \"Revert\" option on the target session to undo any partial changes.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Close")
            }
        } else {
            // Normal progress state
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (progress != null) {
                Text(
                    text = progress.currentStep,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val animatedProgress by animateFloatAsState(
                    targetValue = progress.percentComplete / 100f,
                    label = "progress"
                )
                
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${progress.percentComplete}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Please wait, this may take a few minutes...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PromotionResultContent(
    result: PromotionResult,
    currencyFormat: NumberFormat,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success icon with animation feel
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Color(0xFF4CAF50).copy(alpha = 0.1f),
                    RoundedCornerShape(40.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(56.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Promotion Complete!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "New session is ready to use",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results summary - organized in sections
        
        // Fee Structures Section
        if (result.feeStructuresCopied > 0) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📋 Fee Structure Setup",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ResultRowEnhanced(
                        label = "Fee structures copied",
                        value = "${result.feeStructuresCopied}",
                        subtitle = "Tuition fees for all classes"
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Student Movement Section
        if (result.studentsPromoted > 0 || result.studentsDeactivated > 0) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎓 Class Promotion",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (result.studentsPromoted > 0) {
                        ResultRowEnhanced(
                            label = "Students promoted",
                            value = "${result.studentsPromoted}",
                            subtitle = "Moved to next class"
                        )
                    }
                    
                    if (result.studentsDeactivated > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ResultRowEnhanced(
                            label = "12th class passed out",
                            value = "${result.studentsDeactivated}",
                            subtitle = "Marked as inactive",
                            valueColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Financial Section
        if (result.studentsWithDuesCarried > 0 || result.studentsWithFeesAdded > 0) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💰 Financial Setup",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (result.studentsWithDuesCarried > 0) {
                        ResultRowEnhanced(
                            label = "Dues carried forward",
                            value = currencyFormat.format(result.duesCarriedForward),
                            subtitle = "${result.studentsWithDuesCarried} students with pending dues",
                            valueColor = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    if (result.studentsWithFeesAdded > 0) {
                        ResultRowEnhanced(
                            label = "Fees added to ledger",
                            value = currencyFormat.format(result.totalFeesAdded),
                            subtitle = "${result.studentsWithFeesAdded} students (tuition + transport)"
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tip card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "You can revert this promotion from Sessions menu if needed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4CAF50)
        )
    }
}

@Composable
private fun ResultRowEnhanced(
    label: String,
    value: String,
    subtitle: String,
    valueColor: Color = Color(0xFF4CAF50)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun OptionToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
