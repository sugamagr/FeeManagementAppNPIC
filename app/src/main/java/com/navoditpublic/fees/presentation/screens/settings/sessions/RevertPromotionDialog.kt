package com.navoditpublic.fees.presentation.screens.settings.sessions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.navoditpublic.fees.domain.model.PromotionProgress
import com.navoditpublic.fees.domain.model.RevertResult
import com.navoditpublic.fees.domain.model.RevertSafetyCheck
import com.navoditpublic.fees.domain.model.SessionPromotion
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RevertPromotionDialog(
    promotion: SessionPromotion,
    sourceSessionName: String,
    targetSessionName: String,
    safetyCheck: RevertSafetyCheck?,
    progress: PromotionProgress?,
    result: RevertResult?,
    isReverting: Boolean,
    onExecute: (forceDelete: Boolean, reason: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var forceDelete by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    
    Dialog(
        onDismissRequest = { if (!isReverting) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isReverting,
            dismissOnClickOutside = !isReverting,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Revert Promotion",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Reverting $sourceSessionName → $targetSessionName",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when {
                    result != null -> {
                        RevertResultContent(
                            result = result,
                            onDismiss = onDismiss
                        )
                    }
                    isReverting -> {
                        RevertProgressContent(progress = progress)
                    }
                    else -> {
                        RevertOptionsContent(
                            promotion = promotion,
                            safetyCheck = safetyCheck,
                            currencyFormat = currencyFormat,
                            forceDelete = forceDelete,
                            onForceDeleteChange = { forceDelete = it },
                            reason = reason,
                            onReasonChange = { reason = it },
                            onExecute = { onExecute(forceDelete, reason.ifBlank { null }) },
                            onDismiss = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RevertOptionsContent(
    promotion: SessionPromotion,
    safetyCheck: RevertSafetyCheck?,
    currencyFormat: NumberFormat,
    forceDelete: Boolean,
    onForceDeleteChange: (Boolean) -> Unit,
    reason: String,
    onReasonChange: (String) -> Unit,
    onExecute: () -> Unit,
    onDismiss: () -> Unit
) {
    // What was done during promotion
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "What will be reverted",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (promotion.copiedFeeStructures) {
                RevertItem("Delete ${promotion.feeStructuresCopiedCount} fee structures")
            }
            if (promotion.carriedForwardDues) {
                RevertItem("Remove carried forward dues (${promotion.studentsWithDuesCarried} students)")
            }
            if (promotion.promotedClasses) {
                RevertItem("Demote ${promotion.studentsPromoted} students to previous classes")
            }
            if (promotion.deactivated12thStudents) {
                RevertItem("Reactivate ${promotion.studentsDeactivated} 12th class students")
            }
            if (promotion.addedTuitionFees || promotion.addedTransportFees) {
                RevertItem("Delete session fees (${currencyFormat.format(promotion.totalFeesAdded)})")
            }
            if (promotion.setAsCurrent) {
                RevertItem("Set previous session as current")
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Safety check
    if (safetyCheck != null) {
        if (!safetyCheck.canRevertSafely) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Warning: Data Loss",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    safetyCheck.warnings.forEach { warning ->
                        Text(
                            text = "• $warning",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = forceDelete,
                            onCheckedChange = onForceDeleteChange
                        )
                        Text(
                            text = "I understand and want to proceed anyway",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Safe to revert - no data will be lost",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Reason field
    OutlinedTextField(
        value = reason,
        onValueChange = onReasonChange,
        label = { Text("Reason for reverting (optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
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
            enabled = safetyCheck?.canRevertSafely == true || forceDelete,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Revert Promotion")
        }
    }
}

@Composable
private fun RevertProgressContent(
    progress: PromotionProgress?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.error
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
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.errorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${progress.percentComplete}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            progress.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Please wait, this may take a few minutes...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RevertResultContent(
    result: RevertResult,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (result.success) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Revert Complete!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Revert Failed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            result.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results summary
        if (result.success) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (result.feeStructuresDeleted > 0) {
                        ResultRow("Fee structures deleted", "${result.feeStructuresDeleted}")
                    }
                    if (result.openingBalanceEntriesDeleted > 0) {
                        ResultRow("Opening balance entries deleted", "${result.openingBalanceEntriesDeleted}")
                    }
                    if (result.classesReverted > 0) {
                        ResultRow("Students demoted", "${result.classesReverted}")
                    }
                    if (result.studentsReactivated > 0) {
                        ResultRow("Students reactivated", "${result.studentsReactivated}")
                    }
                    if (result.feeEntriesDeleted > 0) {
                        ResultRow("Fee entries deleted", "${result.feeEntriesDeleted}")
                    }
                    if (result.receiptsDeleted > 0) {
                        ResultRow("Receipts deleted", "${result.receiptsDeleted}")
                    }
                    if (result.studentsDeleted > 0) {
                        ResultRow("Students deleted", "${result.studentsDeleted}")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun RevertItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
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
            fontWeight = FontWeight.Medium
        )
    }
}
