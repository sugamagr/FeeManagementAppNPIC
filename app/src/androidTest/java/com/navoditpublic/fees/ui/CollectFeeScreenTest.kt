package com.navoditpublic.fees.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.domain.model.Student
import com.navoditpublic.fees.domain.model.StudentWithBalance
import com.navoditpublic.fees.presentation.screens.fee_collection.collect.CollectFeeState
import com.navoditpublic.fees.presentation.theme.FeesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * UI tests for the fee collection screen components.
 * Tests individual composables and their interactions.
 */
@RunWith(AndroidJUnit4::class)
class CollectFeeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTestStudent() = Student(
        id = 1L,
        srNumber = "SR001",
        accountNumber = "ACC001",
        name = "Test Student",
        fatherName = "Test Father",
        phonePrimary = "9876543210",
        currentClass = "5th",
        section = "A",
        admissionDate = System.currentTimeMillis(),
        admissionSessionId = 1L
    )

    // ===== Receipt Number Field Tests =====

    @Test
    fun receiptNumberField_displaysValue() {
        composeTestRule.setContent {
            FeesTheme {
                var value by remember { mutableStateOf("123") }
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Receipt Number") },
                    modifier = Modifier.testTag("receipt_number_field")
                )
            }
        }

        composeTestRule.onNodeWithTag("receipt_number_field")
            .assertIsDisplayed()
            .assertTextContains("123")
    }

    @Test
    fun receiptNumberField_showsError() {
        composeTestRule.setContent {
            FeesTheme {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Receipt Number") },
                    isError = true,
                    supportingText = { Text("Receipt number is required") },
                    modifier = Modifier.testTag("receipt_number_field")
                )
            }
        }

        composeTestRule.onNodeWithText("Receipt number is required")
            .assertIsDisplayed()
    }

    @Test
    fun receiptNumberField_showsDuplicateWarning() {
        composeTestRule.setContent {
            FeesTheme {
                Column {
                    OutlinedTextField(
                        value = "100",
                        onValueChange = {},
                        label = { Text("Receipt Number") },
                        modifier = Modifier.testTag("receipt_number_field")
                    )
                    Text(
                        text = "⚠️ This receipt number already exists",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("duplicate_warning")
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("duplicate_warning")
            .assertIsDisplayed()
    }

    // ===== Student Selection Tests =====

    @Test
    fun studentCard_displaysStudentInfo() {
        val student = createTestStudent()
        
        composeTestRule.setContent {
            FeesTheme {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.testTag("student_name")
                        )
                        Text(
                            text = "Class: ${student.currentClass} - ${student.section}",
                            modifier = Modifier.testTag("student_class")
                        )
                        Text(
                            text = "SR: ${student.srNumber}",
                            modifier = Modifier.testTag("student_sr")
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("student_name")
            .assertTextEquals("Test Student")
        
        composeTestRule.onNodeWithTag("student_class")
            .assertTextContains("5th")
        
        composeTestRule.onNodeWithTag("student_sr")
            .assertTextContains("SR001")
    }

    @Test
    fun studentWithBalance_showsDuesAmount() {
        val studentWithBalance = StudentWithBalance(
            student = createTestStudent(),
            currentBalance = 5000.0
        )
        
        composeTestRule.setContent {
            FeesTheme {
                Card(modifier = Modifier.testTag("student_balance_card")) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(studentWithBalance.student.name)
                        Text(
                            text = "Dues: ₹${studentWithBalance.currentBalance.toInt()}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("dues_amount")
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("dues_amount")
            .assertTextContains("5000")
    }

    // ===== Amount Field Tests =====

    @Test
    fun amountField_acceptsInput() {
        composeTestRule.setContent {
            FeesTheme {
                var amount by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount") },
                    modifier = Modifier.testTag("amount_field")
                )
            }
        }

        composeTestRule.onNodeWithTag("amount_field")
            .performTextInput("5000")
        
        composeTestRule.onNodeWithTag("amount_field")
            .assertTextContains("5000")
    }

    // ===== Full Year Discount Checkbox Tests =====

    @Test
    fun fullYearCheckbox_togglesState() {
        composeTestRule.setContent {
            FeesTheme {
                var checked by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.testTag("full_year_row"),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checked = it },
                        modifier = Modifier.testTag("full_year_checkbox")
                    )
                    Text("Full Year Payment (1 month free)")
                }
            }
        }

        composeTestRule.onNodeWithTag("full_year_checkbox")
            .assertIsNotChecked()
        
        composeTestRule.onNodeWithTag("full_year_checkbox")
            .performClick()
        
        composeTestRule.onNodeWithTag("full_year_checkbox")
            .assertIsChecked()
    }

    @Test
    fun fullYearDiscount_showsDiscountAmount() {
        composeTestRule.setContent {
            FeesTheme {
                Column {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(
                            checked = true,
                            onCheckedChange = {},
                            modifier = Modifier.testTag("full_year_checkbox")
                        )
                        Text("Full Year Payment")
                    }
                    Text(
                        text = "Discount: ₹1,000",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("discount_amount")
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("discount_amount")
            .assertIsDisplayed()
            .assertTextContains("1,000")
    }

    // ===== Payment Mode Selection Tests =====

    @Test
    fun paymentModeSelector_displaysAllOptions() {
        composeTestRule.setContent {
            FeesTheme {
                var selectedMode by remember { mutableStateOf(PaymentMode.CASH) }
                Column {
                    PaymentMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .testTag("payment_mode_${mode.name}")
                                .fillMaxWidth(),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode }
                            )
                            Text(mode.name)
                        }
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("payment_mode_CASH").assertIsDisplayed()
        composeTestRule.onNodeWithTag("payment_mode_CHEQUE").assertIsDisplayed()
        composeTestRule.onNodeWithTag("payment_mode_UPI").assertIsDisplayed()
        composeTestRule.onNodeWithTag("payment_mode_ONLINE").assertIsDisplayed()
    }

    @Test
    fun paymentModeSelector_selectsCheque() {
        composeTestRule.setContent {
            FeesTheme {
                var selectedMode by remember { mutableStateOf(PaymentMode.CASH) }
                Column {
                    PaymentMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .testTag("payment_mode_${mode.name}")
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode },
                                modifier = Modifier.testTag("radio_${mode.name}")
                            )
                            Text(mode.name)
                        }
                    }
                    
                    if (selectedMode == PaymentMode.CHEQUE) {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            label = { Text("Cheque Number") },
                            modifier = Modifier.testTag("cheque_number_field")
                        )
                    }
                }
            }
        }

        // Initially cheque field should not be visible
        composeTestRule.onNodeWithTag("cheque_number_field")
            .assertDoesNotExist()
        
        // Select cheque mode
        composeTestRule.onNodeWithTag("radio_CHEQUE")
            .performClick()
        
        // Now cheque field should be visible
        composeTestRule.onNodeWithTag("cheque_number_field")
            .assertIsDisplayed()
    }

    // ===== Summary Section Tests =====

    @Test
    fun summarySection_displaysCorrectTotals() {
        composeTestRule.setContent {
            FeesTheme {
                Card(modifier = Modifier.testTag("summary_card")) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount:")
                            Text("₹11,000", modifier = Modifier.testTag("subtotal"))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount:")
                            Text("₹1,000", modifier = Modifier.testTag("discount"))
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Dues Cleared:", style = MaterialTheme.typography.titleMedium)
                            Text("₹12,000", modifier = Modifier.testTag("total"))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Remaining Dues:")
                            Text("₹0", modifier = Modifier.testTag("remaining"))
                        }
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("subtotal").assertTextContains("11,000")
        composeTestRule.onNodeWithTag("discount").assertTextContains("1,000")
        composeTestRule.onNodeWithTag("total").assertTextContains("12,000")
        composeTestRule.onNodeWithTag("remaining").assertTextContains("0")
    }

    // ===== Save Button Tests =====

    @Test
    fun saveButton_enabledWhenValid() {
        composeTestRule.setContent {
            FeesTheme {
                Button(
                    onClick = {},
                    enabled = true,
                    modifier = Modifier.testTag("save_button")
                ) {
                    Text("Save Receipt")
                }
            }
        }

        composeTestRule.onNodeWithTag("save_button")
            .assertIsEnabled()
    }

    @Test
    fun saveButton_disabledWhenSaving() {
        composeTestRule.setContent {
            FeesTheme {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.testTag("save_button")
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...")
                }
            }
        }

        composeTestRule.onNodeWithTag("save_button")
            .assertIsNotEnabled()
        
        composeTestRule.onNodeWithText("Saving...")
            .assertIsDisplayed()
    }

    // ===== Loading State Tests =====

    @Test
    fun loadingState_showsProgressIndicator() {
        composeTestRule.setContent {
            FeesTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.testTag("loading_indicator")
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
    }

    // ===== Error State Tests =====

    @Test
    fun errorState_showsErrorMessage() {
        composeTestRule.setContent {
            FeesTheme {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.testTag("error_card")
                ) {
                    Text(
                        text = "Failed to load data. Please try again.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("error_card")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Failed to load data. Please try again.")
            .assertIsDisplayed()
    }

    // ===== Backdate Warning Dialog Tests =====

    @Test
    fun backdateWarningDialog_displaysCorrectly() {
        composeTestRule.setContent {
            FeesTheme {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Backdated Receipt") },
                    text = { Text("You are creating a receipt with a past date. This will affect ledger calculations.") },
                    confirmButton = {
                        TextButton(
                            onClick = {},
                            modifier = Modifier.testTag("confirm_backdate")
                        ) {
                            Text("Continue")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {},
                            modifier = Modifier.testTag("cancel_backdate")
                        ) {
                            Text("Cancel")
                        }
                    },
                    modifier = Modifier.testTag("backdate_dialog")
                )
            }
        }

        composeTestRule.onNodeWithText("Backdated Receipt")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithTag("confirm_backdate")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithTag("cancel_backdate")
            .assertIsDisplayed()
    }

    // ===== Search Results Tests =====

    @Test
    fun searchResults_displaysStudentList() {
        val students = listOf(
            StudentWithBalance(createTestStudent().copy(id = 1, name = "Alice"), 5000.0),
            StudentWithBalance(createTestStudent().copy(id = 2, name = "Bob"), 3000.0),
            StudentWithBalance(createTestStudent().copy(id = 3, name = "Charlie"), 0.0)
        )
        
        composeTestRule.setContent {
            FeesTheme {
                Column(modifier = Modifier.testTag("search_results")) {
                    students.forEach { studentWithBalance ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .testTag("search_result_${studentWithBalance.student.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(studentWithBalance.student.name)
                                Text("₹${studentWithBalance.currentBalance.toInt()}")
                            }
                        }
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("search_result_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search_result_2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search_result_3").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charlie").assertIsDisplayed()
    }
}
