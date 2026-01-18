package com.navoditpublic.fees.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for generating Excel-compatible CSV reports
 * CSV format is used as it's lightweight, doesn't require external dependencies,
 * and is natively supported by Excel, Google Sheets, and other spreadsheet apps.
 */
object ExcelGenerator {
    
    /**
     * Sanitize a value for Excel export - removes currency symbols, commas from numbers
     * Use this for any numeric/currency fields
     */
    fun sanitizeNumeric(value: String): String {
        // Remove currency symbols (₹, $, Rs., etc.) and commas from numbers
        return value
            .replace("₹", "")
            .replace("$", "")
            .replace("Rs.", "")
            .replace("Rs", "")
            .replace(",", "")
            .trim()
    }
    
    /**
     * Sanitize a Double value for Excel export - returns plain number string
     */
    fun sanitizeNumeric(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString() // No decimals needed
        } else {
            String.format(Locale.US, "%.2f", value) // 2 decimal places
        }
    }
    
    /**
     * Sanitize a currency amount for Excel - strips formatting, returns plain number
     */
    fun sanitizeCurrency(amount: Double): String {
        return amount.toLong().toString()
    }
    
    /**
     * Generate a CSV report that can be opened in Excel
     * 
     * @param context Android context
     * @param title Report title (used in header row and filename)
     * @param headers Column headers
     * @param rows Data rows (values should already be sanitized for numeric columns)
     * @param summary Optional summary data to include at the top
     * @param fileName Output filename (should end with .csv)
     * @param numericColumns Optional set of column indices that contain numeric data (will be unquoted)
     */
    suspend fun generateReport(
        context: Context,
        title: String,
        headers: List<String>,
        rows: List<List<String>>,
        summary: Map<String, String> = emptyMap(),
        fileName: String,
        numericColumns: Set<Int> = emptySet()
    ) {
        val csvContent = buildString {
            // Title row
            appendLine("\"$title\"")
            appendLine()
            
            // Generation timestamp
            val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
            appendLine("\"Generated on: ${dateFormat.format(Date())}\"")
            appendLine()
            
            // Summary section
            if (summary.isNotEmpty()) {
                appendLine("\"SUMMARY\"")
                summary.forEach { (key, value) ->
                    // Clean numeric values in summary
                    val cleanValue = sanitizeNumeric(value)
                    appendLine("\"$key\",\"$cleanValue\"")
                }
                appendLine()
            }
            
            // Headers row
            appendLine(headers.joinToString(",") { "\"${escapeCSV(it)}\"" })
            
            // Data rows
            rows.forEach { row ->
                val csvRow = row.mapIndexed { index, value ->
                    if (numericColumns.contains(index)) {
                        // Numeric columns: no quotes, sanitize the value
                        sanitizeNumeric(value)
                    } else {
                        // Text columns: quoted
                        "\"${escapeCSV(value)}\""
                    }
                }
                appendLine(csvRow.joinToString(","))
            }
            
            // Footer
            appendLine()
            appendLine("\"Total Records: ${rows.size}\"")
            appendLine("\"Navodit Public Inter College - Fee Management System\"")
        }
        
        // Save and open file
        val file = saveToFile(context, csvContent, fileName)
        openFile(context, file)
    }
    
    /**
     * Generate a simple report without summary
     */
    suspend fun generateSimpleReport(
        context: Context,
        title: String,
        headers: List<String>,
        rows: List<List<String>>,
        fileName: String,
        numericColumns: Set<Int> = emptySet()
    ) {
        generateReport(
            context = context,
            title = title,
            headers = headers,
            rows = rows,
            summary = emptyMap(),
            fileName = fileName,
            numericColumns = numericColumns
        )
    }
    
    /**
     * Generate report with multiple sheets/sections
     * Each section is separated by blank lines
     */
    suspend fun generateMultiSectionReport(
        context: Context,
        title: String,
        sections: List<ReportSection>,
        fileName: String
    ) {
        val csvContent = buildString {
            // Title row
            appendLine("\"$title\"")
            appendLine()
            
            // Generation timestamp
            val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
            appendLine("\"Generated on: ${dateFormat.format(Date())}\"")
            appendLine()
            
            // Each section
            sections.forEach { section ->
                // Section title
                appendLine("\"${section.title}\"")
                
                // Section summary if present
                section.summary?.forEach { (key, value) ->
                    val cleanValue = sanitizeNumeric(value)
                    appendLine("\"$key\",\"$cleanValue\"")
                }
                if (section.summary != null) appendLine()
                
                // Headers
                appendLine(section.headers.joinToString(",") { "\"${escapeCSV(it)}\"" })
                
                // Data rows
                section.rows.forEach { row ->
                    val csvRow = row.mapIndexed { index, value ->
                        if (section.numericColumns.contains(index)) {
                            sanitizeNumeric(value)
                        } else {
                            "\"${escapeCSV(value)}\""
                        }
                    }
                    appendLine(csvRow.joinToString(","))
                }
                
                appendLine()
                appendLine("\"Section Total: ${section.rows.size} records\"")
                appendLine()
                appendLine() // Extra line between sections
            }
            
            // Footer
            appendLine("\"Navodit Public Inter College - Fee Management System\"")
        }
        
        val file = saveToFile(context, csvContent, fileName)
        openFile(context, file)
    }
    
    /**
     * Escape special characters for CSV format
     */
    private fun escapeCSV(value: String): String {
        return value
            .replace("\"", "\"\"") // Escape double quotes
            .replace("\n", " ")    // Replace newlines with space
            .replace("\r", "")     // Remove carriage returns
    }
    
    /**
     * Save CSV content to file
     */
    private fun saveToFile(context: Context, content: String, fileName: String): File {
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        
        val reportsDir = File(documentsDir, "FeeReports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        
        // Ensure filename ends with .csv
        val finalFileName = if (fileName.endsWith(".csv", ignoreCase = true)) {
            fileName
        } else {
            "$fileName.csv"
        }
        
        val file = File(reportsDir, finalFileName)
        FileWriter(file).use { writer ->
            writer.write(content)
        }
        
        return file
    }
    
    /**
     * Open the generated CSV file
     */
    private fun openFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            // Try to open with spreadsheet app first
            val chooserIntent = Intent.createChooser(intent, "Open with...")
            chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooserIntent)
            
        } catch (e: Exception) {
            // Fallback - try to share the file
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Excel file"))
                
            } catch (e2: Exception) {
                android.widget.Toast.makeText(
                    context,
                    "File saved to: ${file.absolutePath}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

/**
 * Data class representing a section in a multi-section report
 */
data class ReportSection(
    val title: String,
    val headers: List<String>,
    val rows: List<List<String>>,
    val summary: Map<String, String>? = null,
    val numericColumns: Set<Int> = emptySet()
)
