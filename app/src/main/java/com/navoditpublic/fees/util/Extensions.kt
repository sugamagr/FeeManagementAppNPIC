package com.navoditpublic.fees.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Extension functions for common operations
 */

// Thread-safe currency formatting for Indian Rupees
// Creates new NumberFormat instances per call to avoid thread-safety issues
private fun createCurrencyFormat(): NumberFormat = 
    NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

private fun createCurrencyFormatWithDecimals(): NumberFormat = 
    NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

fun Double.toRupees(): String = createCurrencyFormat().format(this)

fun Double.toRupeesWithDecimals(): String = createCurrencyFormatWithDecimals().format(this)

fun Double.toRupeesPlain(): String = "Rs. ${createCurrencyFormat().format(this).replace("₹", "").trim()}"

fun Int.toRupees(): String = createCurrencyFormat().format(this)

fun Long.toRupees(): String = createCurrencyFormat().format(this)

// Phone number validation
fun String.isValidIndianPhone(): Boolean {
    val cleaned = this.replace(" ", "").replace("-", "")
    return cleaned.matches(Regex("^[6-9]\\d{9}$"))
}

// Pincode validation
fun String.isValidPincode(): Boolean {
    return this.matches(Regex("^[1-9]\\d{5}$"))
}

// String extensions
fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}

fun String.toTitleCase(): String = capitalizeWords()

// SR Number / Account Number formatting
fun String.formatSrNumber(): String {
    return this.uppercase().trim()
}

fun String.formatAccountNumber(): String {
    return this.uppercase().trim()
}

// List extensions
fun <T> List<T>.safeGet(index: Int): T? {
    return if (index in indices) get(index) else null
}


