package com.navoditpublic.fees.presentation.screens.settings.fee_structure

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.components.EmptyState
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeStructureScreen(
    navController: NavController,
    viewModel: FeeStructureViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FeeStructureEvent.Success -> {
                    Toast.makeText(context, "Fee structure saved", Toast.LENGTH_SHORT).show()
                }
                is FeeStructureEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fee Structure") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else if (state.currentSession == null) {
            EmptyState(
                icon = Icons.Default.Payment,
                title = "No Session",
                subtitle = "Please add an academic session first",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Session: ${state.currentSession?.sessionName}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Fee structure applies to this session only. Previous session fees are preserved.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Admission Fee
                item { SectionHeader(title = "Admission Fee (All Classes)") }
                
                item {
                    val admissionFocusManager = LocalFocusManager.current
                    OutlinedTextField(
                        value = state.admissionFee,
                        onValueChange = viewModel::updateAdmissionFee,
                        label = { Text("Amount") },
                        prefix = { Text("Rs. ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { admissionFocusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                }
                
                // Monthly Fee Classes (NC to 8th)
                item { SectionHeader(title = "Monthly Fees (NC to 8th)") }
                
                item {
                    Text(
                        text = "Full year discount: Pay 12 months, get 1 month free",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                items(state.monthlyFees.entries.toList()) { (className, amount) ->
                    val entries = state.monthlyFees.entries.toList()
                    val isLast = entries.lastOrNull()?.key == className
                    FeeInputRow(
                        className = className,
                        amount = amount,
                        onAmountChange = { viewModel.updateMonthlyFee(className, it) },
                        suffix = "/month",
                        isLast = isLast && state.annualFees.isEmpty()
                    )
                }
                
                // Annual Fee Classes (9th to 12th)
                item { SectionHeader(title = "Annual Fees (9th to 12th)") }
                
                item {
                    Text(
                        text = "Lump sum annual fee for senior classes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                items(state.annualFees.entries.toList()) { (className, amount) ->
                    val entries = state.annualFees.entries.toList()
                    val isLast = entries.lastOrNull()?.key == className
                    FeeInputRow(
                        className = className,
                        amount = amount,
                        onAmountChange = { viewModel.updateAnnualFee(className, it) },
                        suffix = "/year",
                        isLast = isLast && state.registrationFees.isEmpty()
                    )
                }
                
                // Registration Fee - Separate for each class
                item { SectionHeader(title = "Registration Fees (9th to 12th)") }
                
                item {
                    Text(
                        text = "One-time registration fee per session, can vary by class",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                items(state.registrationFees.entries.toList()) { (className, amount) ->
                    val entries = state.registrationFees.entries.toList()
                    val isLast = entries.lastOrNull()?.key == className
                    FeeInputRow(
                        className = className,
                        amount = amount,
                        onAmountChange = { viewModel.updateRegistrationFee(className, it) },
                        suffix = "",
                        isLast = isLast
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.saveAll() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (state.isSaving) "Saving..." else "Save All",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun FeeInputRow(
    className: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    suffix: String = "",
    isLast: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = className,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(60.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { onAmountChange(it.filter { c -> c.isDigit() }) },
            prefix = { Text("Rs. ") },
            suffix = if (suffix.isNotBlank()) {{ Text(suffix, style = MaterialTheme.typography.bodySmall) }} else null,
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = if (isLast) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = { focusManager.clearFocus() }
            )
        )
    }
}
