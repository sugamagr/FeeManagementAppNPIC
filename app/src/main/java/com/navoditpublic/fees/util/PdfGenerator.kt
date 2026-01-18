package com.navoditpublic.fees.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for generating PDF reports with school branding
 */
object PdfGenerator {
    
    private const val PAGE_WIDTH = 595  // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 40f
    private const val HEADER_HEIGHT = 120f
    
    // Colors
    private val SAFFRON_COLOR = Color.rgb(255, 136, 62) // Saffron/Bhagwa
    private val HEADER_BG = Color.rgb(255, 248, 240)
    private val TABLE_HEADER_BG = Color.rgb(255, 136, 62)
    private val TABLE_ROW_ALT = Color.rgb(255, 250, 245)
    private val TEXT_PRIMARY = Color.rgb(33, 33, 33)
    private val TEXT_SECONDARY = Color.rgb(100, 100, 100)
    
    /**
     * Generate a PDF report with school header and table
     */
    suspend fun generateReport(
        context: Context,
        title: String,
        headers: List<String>,
        rows: List<List<String>>,
        summary: Map<String, String> = emptyMap(),
        fileName: String
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var currentY = MARGIN
        var pageNumber = 1
        var rowsOnCurrentPage = 0
        val maxRowsPerPage = 25
        
        // Draw header on first page
        currentY = drawHeader(canvas, context, title)
        
        // Draw summary if present
        if (summary.isNotEmpty()) {
            currentY = drawSummary(canvas, summary, currentY)
        }
        
        // Draw table headers
        val tableStartY = currentY + 20f
        currentY = drawTableHeader(canvas, headers, tableStartY)
        
        // Calculate column widths
        val contentWidth = PAGE_WIDTH - (2 * MARGIN)
        val columnWidth = contentWidth / headers.size
        
        // Draw table rows
        val rowHeight = 28f
        var isAlternate = false
        
        for (row in rows) {
            // Check if we need a new page
            if (currentY + rowHeight > PAGE_HEIGHT - MARGIN - 50) {
                // Add page number
                drawPageNumber(canvas, pageNumber)
                document.finishPage(page)
                
                // Start new page
                pageNumber++
                val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(newPageInfo)
                canvas = page.canvas
                
                // Draw header on new page (smaller version)
                currentY = drawSmallHeader(canvas, title, pageNumber)
                
                // Redraw table headers
                currentY = drawTableHeader(canvas, headers, currentY)
                isAlternate = false
            }
            
            // Draw row background
            if (isAlternate) {
                val bgPaint = Paint().apply {
                    color = TABLE_ROW_ALT
                    style = Paint.Style.FILL
                }
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, bgPaint)
            }
            
            // Draw row data
            val textPaint = Paint().apply {
                color = TEXT_PRIMARY
                textSize = 10f
                isAntiAlias = true
            }
            
            row.forEachIndexed { index, cellValue ->
                val x = MARGIN + (index * columnWidth) + 8
                canvas.drawText(
                    cellValue.take(25), // Truncate if too long
                    x,
                    currentY + 18f,
                    textPaint
                )
            }
            
            // Draw row border
            val borderPaint = Paint().apply {
                color = Color.LTGRAY
                style = Paint.Style.STROKE
                strokeWidth = 0.5f
            }
            canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, borderPaint)
            
            currentY += rowHeight
            isAlternate = !isAlternate
            rowsOnCurrentPage++
        }
        
        // Draw final page number
        drawPageNumber(canvas, pageNumber)
        
        // Draw footer
        drawFooter(canvas)
        
        document.finishPage(page)
        
        // Save document
        val file = saveDocument(context, document, fileName)
        document.close()
        
        // Open the PDF
        openPdf(context, file)
    }
    
    private fun drawHeader(canvas: Canvas, context: Context, title: String): Float {
        var y = MARGIN
        
        // Header background
        val bgPaint = Paint().apply {
            color = HEADER_BG
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), HEADER_HEIGHT + MARGIN, bgPaint)
        
        // Saffron accent line
        val accentPaint = Paint().apply {
            color = SAFFRON_COLOR
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 6f, accentPaint)
        
        // School name
        val schoolNamePaint = Paint().apply {
            color = SAFFRON_COLOR
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        y += 35f
        canvas.drawText("NAVODIT PUBLIC INTER COLLEGE", PAGE_WIDTH / 2f, y, schoolNamePaint)
        
        // Tagline
        val taglinePaint = Paint().apply {
            color = TEXT_SECONDARY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        y += 18f
        canvas.drawText("Approved By the Government", PAGE_WIDTH / 2f, y, taglinePaint)
        
        // Address
        val addressPaint = Paint().apply {
            color = TEXT_PRIMARY
            textSize = 10f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        y += 16f
        canvas.drawText("Myuna Khudaganj, Shahjahanpur, Uttar Pradesh", PAGE_WIDTH / 2f, y, addressPaint)
        
        // Report title
        val titlePaint = Paint().apply {
            color = TEXT_PRIMARY
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        y += 30f
        canvas.drawText(title.uppercase(), PAGE_WIDTH / 2f, y, titlePaint)
        
        // Date
        val datePaint = Paint().apply {
            color = TEXT_SECONDARY
            textSize = 9f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        y += 16f
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
        canvas.drawText("Generated on: ${dateFormat.format(Date())}", PAGE_WIDTH / 2f, y, datePaint)
        
        // Bottom border of header
        canvas.drawRect(MARGIN, y + 10f, PAGE_WIDTH - MARGIN, y + 12f, accentPaint)
        
        return y + 25f
    }
    
    private fun drawSmallHeader(canvas: Canvas, title: String, pageNumber: Int): Float {
        // Simple header for continuation pages
        val paint = Paint().apply {
            color = SAFFRON_COLOR
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(title, MARGIN, MARGIN + 15f, paint)
        
        val pagePaint = Paint().apply {
            color = TEXT_SECONDARY
            textSize = 10f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("(Continued)", PAGE_WIDTH - MARGIN, MARGIN + 15f, pagePaint)
        
        // Line separator
        val linePaint = Paint().apply {
            color = SAFFRON_COLOR
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, MARGIN + 25f, PAGE_WIDTH - MARGIN, MARGIN + 25f, linePaint)
        
        return MARGIN + 40f
    }
    
    private fun drawSummary(canvas: Canvas, summary: Map<String, String>, startY: Float): Float {
        var y = startY + 15f
        
        val labelPaint = Paint().apply {
            color = TEXT_SECONDARY
            textSize = 10f
            isAntiAlias = true
        }
        
        val valuePaint = Paint().apply {
            color = TEXT_PRIMARY
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        // Draw summary in a box
        val boxPaint = Paint().apply {
            color = Color.rgb(245, 245, 245)
            style = Paint.Style.FILL
        }
        
        val summaryHeight = (summary.size * 20f) + 20f
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + summaryHeight, boxPaint)
        
        y += 15f
        summary.forEach { (key, value) ->
            canvas.drawText(key + ":", MARGIN + 10f, y, labelPaint)
            canvas.drawText(value, MARGIN + 150f, y, valuePaint)
            y += 18f
        }
        
        return y + 10f
    }
    
    private fun drawTableHeader(canvas: Canvas, headers: List<String>, startY: Float): Float {
        val headerHeight = 30f
        val contentWidth = PAGE_WIDTH - (2 * MARGIN)
        val columnWidth = contentWidth / headers.size
        
        // Header background
        val bgPaint = Paint().apply {
            color = TABLE_HEADER_BG
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN, startY, PAGE_WIDTH - MARGIN, startY + headerHeight, bgPaint)
        
        // Header text
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        headers.forEachIndexed { index, header ->
            val x = MARGIN + (index * columnWidth) + 8
            canvas.drawText(header, x, startY + 20f, textPaint)
        }
        
        return startY + headerHeight
    }
    
    private fun drawPageNumber(canvas: Canvas, pageNumber: Int) {
        val paint = Paint().apply {
            color = TEXT_SECONDARY
            textSize = 9f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Page $pageNumber", PAGE_WIDTH / 2f, PAGE_HEIGHT - 25f, paint)
    }
    
    private fun drawFooter(canvas: Canvas) {
        val footerY = PAGE_HEIGHT - 40f
        
        // Footer line
        val linePaint = Paint().apply {
            color = SAFFRON_COLOR
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, footerY, PAGE_WIDTH - MARGIN, footerY, linePaint)
        
        // Footer text
        val paint = Paint().apply {
            color = TEXT_SECONDARY
            textSize = 8f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "This is a computer-generated report. | Navodit Public Inter College Fee Management System",
            PAGE_WIDTH / 2f,
            footerY + 12f,
            paint
        )
    }
    
    private fun saveDocument(context: Context, document: PdfDocument, fileName: String): File {
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        
        val reportsDir = File(documentsDir, "FeeReports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        
        val file = File(reportsDir, fileName)
        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        
        return file
    }
    
    private fun openPdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            // No PDF viewer installed - notify user
            android.util.Log.e("PdfGenerator", "No PDF viewer found", e)
            android.widget.Toast.makeText(
                context,
                "No PDF viewer found. File saved to: ${file.absolutePath}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    /**
     * Generate a student list report with configurable columns
     */
    suspend fun generateStudentReport(
        context: Context,
        title: String,
        headers: List<String>,
        studentData: List<List<String>>,
        fileName: String
    ) {
        generateReport(
            context = context,
            title = title,
            headers = headers,
            rows = studentData,
            fileName = fileName
        )
    }
}

