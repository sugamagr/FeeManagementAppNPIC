package com.navoditpublic.fees.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Thread-safe date utilities.
 * Each format call creates a new formatter instance to avoid thread-safety issues
 * with SimpleDateFormat which is not thread-safe.
 */
object DateUtils {
    
    // Pattern constants
    private const val PATTERN_DATE = "dd-MM-yyyy"
    private const val PATTERN_DATE_TIME = "dd-MM-yyyy HH:mm"
    private const val PATTERN_MONTH_YEAR = "MMMM yyyy"
    private const val PATTERN_SHORT_MONTH = "MMM yyyy"
    private const val PATTERN_MONTH_KEY = "MM-yyyy"
    private const val PATTERN_DAY_MONTH = "dd MMM"
    private const val PATTERN_DAY_MONTH_YEAR = "dd MMM yy"
    
    // Thread-safe formatters - create new instance per call
    private fun createDateFormat() = SimpleDateFormat(PATTERN_DATE, Locale.getDefault())
    private fun createDateTimeFormat() = SimpleDateFormat(PATTERN_DATE_TIME, Locale.getDefault())
    private fun createMonthYearFormat() = SimpleDateFormat(PATTERN_MONTH_YEAR, Locale.getDefault())
    private fun createShortMonthFormat() = SimpleDateFormat(PATTERN_SHORT_MONTH, Locale.getDefault())
    private fun createMonthKeyFormat() = SimpleDateFormat(PATTERN_MONTH_KEY, Locale.getDefault())
    private fun createDayMonthFormat() = SimpleDateFormat(PATTERN_DAY_MONTH, Locale.getDefault())
    private fun createDayMonthYearFormat() = SimpleDateFormat(PATTERN_DAY_MONTH_YEAR, Locale.getDefault())
    
    fun formatDate(timestamp: Long): String = createDateFormat().format(Date(timestamp))
    
    fun formatDateTime(timestamp: Long): String = createDateTimeFormat().format(Date(timestamp))
    
    fun formatMonthYear(timestamp: Long): String = createMonthYearFormat().format(Date(timestamp))
    
    fun formatShortMonthYear(timestamp: Long): String = createShortMonthFormat().format(Date(timestamp))
    
    fun formatMonthKey(timestamp: Long): String = createMonthKeyFormat().format(Date(timestamp))
    
    fun formatDayMonth(timestamp: Long): String = createDayMonthFormat().format(Date(timestamp))
    
    fun formatDayMonthYear(timestamp: Long): String = createDayMonthYearFormat().format(Date(timestamp))
    
    /**
     * Alias for formatDayMonthYear - formats as "01 Jan 25"
     */
    fun formatShortDate(timestamp: Long): String = formatDayMonthYear(timestamp)
    
    fun parseDate(dateString: String): Long? {
        if (dateString.isBlank()) return null
        return try {
            val formatter = createDateFormat().apply { isLenient = false }
            formatter.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }
    
    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
    
    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }
    
    fun getStartOfMonth(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
    
    fun getEndOfMonth(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }
    
    /**
     * Get list of months for the academic session (April to March)
     */
    fun getSessionMonths(startYear: Int): List<SessionMonth> {
        val months = mutableListOf<SessionMonth>()
        val calendar = Calendar.getInstance()
        
        // Create formatters locally for thread safety
        val monthKeyFormatter = createMonthKeyFormat()
        val monthYearFormatter = createMonthYearFormat()
        val shortMonthFormatter = createShortMonthFormat()
        
        // April to December of start year
        for (month in Calendar.APRIL..Calendar.DECEMBER) {
            calendar.set(startYear, month, 1)
            months.add(
                SessionMonth(
                    monthKey = monthKeyFormatter.format(calendar.time),
                    displayName = monthYearFormatter.format(calendar.time),
                    shortName = shortMonthFormatter.format(calendar.time),
                    timestamp = calendar.timeInMillis
                )
            )
        }
        
        // January to March of next year
        for (month in Calendar.JANUARY..Calendar.MARCH) {
            calendar.set(startYear + 1, month, 1)
            months.add(
                SessionMonth(
                    monthKey = monthKeyFormatter.format(calendar.time),
                    displayName = monthYearFormatter.format(calendar.time),
                    shortName = shortMonthFormatter.format(calendar.time),
                    timestamp = calendar.timeInMillis
                )
            )
        }
        
        return months
    }
    
    fun getCurrentSessionStartYear(): Int {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        
        return if (month < Calendar.APRIL) year - 1 else year
    }
    
    fun getRelativeTimeString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} min ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            diff < 172800_000 -> "Yesterday"
            diff < 604800_000 -> "${diff / 86400_000} days ago"
            else -> formatDate(timestamp)
        }
    }
}

data class SessionMonth(
    val monthKey: String,    // "04-2025"
    val displayName: String, // "April 2025"
    val shortName: String,   // "Apr 2025"
    val timestamp: Long
)


