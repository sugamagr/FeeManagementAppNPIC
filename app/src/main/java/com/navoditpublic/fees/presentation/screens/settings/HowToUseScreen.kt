package com.navoditpublic.fees.presentation.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronDark

data class GuideSection(
    val icon: ImageVector,
    val title: String,
    val steps: List<GuideStep>
)

data class GuideStep(
    val title: String,
    val description: String
)

@Composable
fun HowToUseScreen(
    navController: NavController
) {
    val guideSections = listOf(
        GuideSection(
            icon = Icons.Default.Settings,
            title = "Initial Setup",
            steps = listOf(
                GuideStep(
                    "School Profile",
                    "Go to Settings → School Profile. Enter your school name, address, contact details, and upload logo."
                ),
                GuideStep(
                    "Academic Session",
                    "Go to Settings → Academic Sessions. Create the current session (e.g., 2025-26) and set it as active."
                ),
                GuideStep(
                    "Fee Structure",
                    "Go to Settings → Fee Structure. Set monthly fees for NC to 8th and annual fees for 9th to 12th classes."
                ),
                GuideStep(
                    "Transport Routes",
                    "Go to Settings → Transport Routes. Add all transport routes with class-wise fees (NC-5th, 6th-8th, 9th-12th)."
                )
            )
        ),
        GuideSection(
            icon = Icons.Default.Group,
            title = "Adding Students",
            steps = listOf(
                GuideStep(
                    "Add New Student",
                    "Go to Students tab → Click + button. Fill in student details: name, father's name, class, section, address, phone."
                ),
                GuideStep(
                    "SR & Account Number",
                    "Enter the SR Number and Account Number from your physical register."
                ),
                GuideStep(
                    "Opening Balance",
                    "For existing students with previous dues, enter the opening balance amount with remarks."
                ),
                GuideStep(
                    "Transport (Optional)",
                    "If student uses transport, enable transport option and select their route."
                )
            )
        ),
        GuideSection(
            icon = Icons.Default.Payment,
            title = "Fee Collection",
            steps = listOf(
                GuideStep(
                    "Select Student",
                    "Go to Collect tab (center button) → Search and select the student."
                ),
                GuideStep(
                    "Enter Receipt Details",
                    "Enter the receipt number from your physical receipt book. Select the date of payment."
                ),
                GuideStep(
                    "Select Fees",
                    "Select which months/fees the payment is for, or enter a custom amount."
                ),
                GuideStep(
                    "Payment Mode",
                    "Select payment mode (Cash/Online). Save the receipt."
                )
            )
        ),
        GuideSection(
            icon = Icons.Default.MenuBook,
            title = "Viewing Ledger",
            steps = listOf(
                GuideStep(
                    "Student Ledger",
                    "Go to Ledger tab → Select class → Select student. View all debits (fees charged) and credits (payments received)."
                ),
                GuideStep(
                    "Running Balance",
                    "The ledger shows a running balance. Positive balance = dues pending, Negative = advance paid."
                ),
                GuideStep(
                    "Transaction History",
                    "Each entry shows the date, particulars, debit/credit amount, and updated balance."
                )
            )
        ),
        GuideSection(
            icon = Icons.Default.Report,
            title = "Reports",
            steps = listOf(
                GuideStep(
                    "Dues Reports",
                    "Reports tab opens to Dues section by default. View defaulters, class-wise dues, month-wise dues, transport dues, and aging analysis."
                ),
                GuideStep(
                    "Collection Reports",
                    "Switch to Collection tab to view daily collection, monthly collection, and receipt register."
                ),
                GuideStep(
                    "Custom Reports",
                    "Use Custom Report option to select specific columns and filters. Save views for quick access."
                ),
                GuideStep(
                    "Export to PDF",
                    "Each report has an Export PDF button to generate and share reports."
                )
            )
        ),
        GuideSection(
            icon = Icons.Default.TipsAndUpdates,
            title = "Tips & Best Practices",
            steps = listOf(
                GuideStep(
                    "Physical First",
                    "Always write receipts in your physical book first, then record in the app. The app mirrors your manual system."
                ),
                GuideStep(
                    "Regular Backup",
                    "The app stores data locally. Take regular backups of your device."
                ),
                GuideStep(
                    "Check Reports Daily",
                    "Review daily collection report at end of each day to ensure all receipts are recorded."
                ),
                GuideStep(
                    "Use Search",
                    "Use the search feature to quickly find students by name, father's name, or class."
                )
            )
        )
    )

    var expandedSection by remember { mutableStateOf<Int?>(0) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Saffron, SaffronDark)
                        )
                    )
                    .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)
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
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.AutoMirrored.Filled.HelpCenter,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "How to Use",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Complete Guide",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Welcome Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Welcome to Navodit Fees",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "This guide will help you set up and use the app effectively. Follow the sections below step by step.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Guide Sections
            itemsIndexed(guideSections) { index, section ->
                GuideSectionCard(
                    section = section,
                    stepNumber = index + 1,
                    isExpanded = expandedSection == index,
                    onToggle = {
                        expandedSection = if (expandedSection == index) null else index
                    }
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun GuideSectionCard(
    section: GuideSection,
    stepNumber: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                color = if (isExpanded) Saffron.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step Number Badge
                    Surface(
                        shape = CircleShape,
                        color = Saffron,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = stepNumber.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // Icon and Title
                    Icon(
                        section.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Saffron
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Steps
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    
                    section.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Step indicator
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Saffron.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Saffron
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = step.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (index < section.steps.lastIndex) {
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

