package com.navoditpublic.fees.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Unit tests for DateUtils utility class.
 * Tests date formatting, parsing, and date range calculations.
 */
class DateUtilsTest {

    // Helper to create a timestamp for a specific date
    private fun createTimestamp(year: Int, month: Int, day: Int, hour: Int = 12, minute: Int = 0): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // ===== Format Date Tests =====

    @Test
    fun `formatDate returns correct format dd-MM-yyyy`() {
        val timestamp = createTimestamp(2025, Calendar.JANUARY, 15)
        val result = DateUtils.formatDate(timestamp)
        assertThat(result).isEqualTo("15-01-2025")
    }

    @Test
    fun `formatDate handles different months correctly`() {
        val aprilDate = createTimestamp(2025, Calendar.APRIL, 1)
        assertThat(DateUtils.formatDate(aprilDate)).isEqualTo("01-04-2025")

        val decemberDate = createTimestamp(2025, Calendar.DECEMBER, 31)
        assertThat(DateUtils.formatDate(decemberDate)).isEqualTo("31-12-2025")
    }

    // ===== Format DateTime Tests =====

    @Test
    fun `formatDateTime returns correct format with time`() {
        val timestamp = createTimestamp(2025, Calendar.JANUARY, 15, 14, 30)
        val result = DateUtils.formatDateTime(timestamp)
        assertThat(result).isEqualTo("15-01-2025 14:30")
    }

    // ===== Format Month Year Tests =====

    @Test
    fun `formatMonthYear returns full month name and year`() {
        val timestamp = createTimestamp(2025, Calendar.APRIL, 15)
        val result = DateUtils.formatMonthYear(timestamp)
        assertThat(result).isEqualTo("April 2025")
    }

    @Test
    fun `formatShortMonthYear returns abbreviated month`() {
        val timestamp = createTimestamp(2025, Calendar.SEPTEMBER, 15)
        val result = DateUtils.formatShortMonthYear(timestamp)
        assertThat(result).isEqualTo("Sep 2025")
    }

    @Test
    fun `formatMonthKey returns MM-yyyy format`() {
        val timestamp = createTimestamp(2025, Calendar.APRIL, 15)
        val result = DateUtils.formatMonthKey(timestamp)
        assertThat(result).isEqualTo("04-2025")
    }

    // ===== Parse Date Tests =====

    @Test
    fun `parseDate parses valid date string`() {
        val result = DateUtils.parseDate("15-01-2025")
        assertThat(result).isNotNull()
        
        // Verify by formatting back
        val formatted = DateUtils.formatDate(result!!)
        assertThat(formatted).isEqualTo("15-01-2025")
    }

    @Test
    fun `parseDate returns null for invalid date string`() {
        assertThat(DateUtils.parseDate("invalid")).isNull()
        assertThat(DateUtils.parseDate("2025-01-15")).isNull() // Wrong format
        assertThat(DateUtils.parseDate("")).isNull()
    }

    // ===== Start of Day Tests =====

    @Test
    fun `getStartOfDay returns midnight of given day`() {
        val midday = createTimestamp(2025, Calendar.JANUARY, 15, 14, 30)
        val startOfDay = DateUtils.getStartOfDay(midday)
        
        val calendar = Calendar.getInstance().apply { timeInMillis = startOfDay }
        assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(0)
        assertThat(calendar.get(Calendar.MINUTE)).isEqualTo(0)
        assertThat(calendar.get(Calendar.SECOND)).isEqualTo(0)
        assertThat(calendar.get(Calendar.MILLISECOND)).isEqualTo(0)
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(15)
    }

    @Test
    fun `getStartOfDay with no parameter uses current time`() {
        val startOfDay = DateUtils.getStartOfDay()
        val now = Calendar.getInstance()
        val result = Calendar.getInstance().apply { timeInMillis = startOfDay }
        
        assertThat(result.get(Calendar.DAY_OF_MONTH)).isEqualTo(now.get(Calendar.DAY_OF_MONTH))
        assertThat(result.get(Calendar.HOUR_OF_DAY)).isEqualTo(0)
    }

    // ===== End of Day Tests =====

    @Test
    fun `getEndOfDay returns 23-59-59-999 of given day`() {
        val midday = createTimestamp(2025, Calendar.JANUARY, 15, 14, 30)
        val endOfDay = DateUtils.getEndOfDay(midday)
        
        val calendar = Calendar.getInstance().apply { timeInMillis = endOfDay }
        assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(23)
        assertThat(calendar.get(Calendar.MINUTE)).isEqualTo(59)
        assertThat(calendar.get(Calendar.SECOND)).isEqualTo(59)
        assertThat(calendar.get(Calendar.MILLISECOND)).isEqualTo(999)
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(15)
    }

    // ===== Start of Month Tests =====

    @Test
    fun `getStartOfMonth returns first day of month at midnight`() {
        val midMonth = createTimestamp(2025, Calendar.JANUARY, 15, 14, 30)
        val startOfMonth = DateUtils.getStartOfMonth(midMonth)
        
        val calendar = Calendar.getInstance().apply { timeInMillis = startOfMonth }
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(1)
        assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(0)
        assertThat(calendar.get(Calendar.MINUTE)).isEqualTo(0)
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY)
    }

    // ===== End of Month Tests =====

    @Test
    fun `getEndOfMonth returns last day of month at end of day`() {
        val midJanuary = createTimestamp(2025, Calendar.JANUARY, 15)
        val endOfMonth = DateUtils.getEndOfMonth(midJanuary)
        
        val calendar = Calendar.getInstance().apply { timeInMillis = endOfMonth }
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(31) // January has 31 days
        assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(23)
        assertThat(calendar.get(Calendar.MINUTE)).isEqualTo(59)
    }

    @Test
    fun `getEndOfMonth handles February correctly`() {
        val midFebruary = createTimestamp(2025, Calendar.FEBRUARY, 15)
        val endOfMonth = DateUtils.getEndOfMonth(midFebruary)
        
        val calendar = Calendar.getInstance().apply { timeInMillis = endOfMonth }
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(28) // 2025 is not a leap year
    }

    @Test
    fun `getEndOfMonth handles leap year February`() {
        val midFebruary = createTimestamp(2024, Calendar.FEBRUARY, 15) // 2024 is a leap year
        val endOfMonth = DateUtils.getEndOfMonth(midFebruary)
        
        val calendar = Calendar.getInstance().apply { timeInMillis = endOfMonth }
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(29)
    }

    // ===== Session Months Tests =====

    @Test
    fun `getSessionMonths returns 12 months from April to March`() {
        val sessionMonths = DateUtils.getSessionMonths(2025)
        
        assertThat(sessionMonths).hasSize(12)
        
        // First month should be April 2025
        assertThat(sessionMonths.first().displayName).isEqualTo("April 2025")
        assertThat(sessionMonths.first().monthKey).isEqualTo("04-2025")
        
        // Last month should be March 2026
        assertThat(sessionMonths.last().displayName).isEqualTo("March 2026")
        assertThat(sessionMonths.last().monthKey).isEqualTo("03-2026")
    }

    @Test
    fun `getSessionMonths returns months in correct order`() {
        val sessionMonths = DateUtils.getSessionMonths(2025)
        
        val expectedOrder = listOf(
            "April 2025", "May 2025", "June 2025", "July 2025",
            "August 2025", "September 2025", "October 2025", "November 2025",
            "December 2025", "January 2026", "February 2026", "March 2026"
        )
        
        assertThat(sessionMonths.map { it.displayName }).isEqualTo(expectedOrder)
    }

    @Test
    fun `getSessionMonths provides correct short names`() {
        val sessionMonths = DateUtils.getSessionMonths(2025)
        
        assertThat(sessionMonths[0].shortName).isEqualTo("Apr 2025")
        assertThat(sessionMonths[5].shortName).isEqualTo("Sep 2025")
        assertThat(sessionMonths[11].shortName).isEqualTo("Mar 2026")
    }

    // ===== Current Session Start Year Tests =====

    @Test
    fun `getCurrentSessionStartYear returns current year when in April or later`() {
        // This test may vary based on current date
        // We're testing the logic: if month >= April, return current year
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)
        
        val expected = if (currentMonth < Calendar.APRIL) currentYear - 1 else currentYear
        assertThat(DateUtils.getCurrentSessionStartYear()).isEqualTo(expected)
    }

    // ===== Relative Time String Tests =====

    @Test
    fun `getRelativeTimeString returns Just now for recent timestamps`() {
        val now = System.currentTimeMillis()
        val thirtySecondsAgo = now - 30_000
        
        assertThat(DateUtils.getRelativeTimeString(thirtySecondsAgo)).isEqualTo("Just now")
    }

    @Test
    fun `getRelativeTimeString returns minutes ago`() {
        val now = System.currentTimeMillis()
        val fiveMinutesAgo = now - 5 * 60_000
        
        assertThat(DateUtils.getRelativeTimeString(fiveMinutesAgo)).isEqualTo("5 min ago")
    }

    @Test
    fun `getRelativeTimeString returns hours ago`() {
        val now = System.currentTimeMillis()
        val threeHoursAgo = now - 3 * 3600_000
        
        assertThat(DateUtils.getRelativeTimeString(threeHoursAgo)).isEqualTo("3 hours ago")
    }

    @Test
    fun `getRelativeTimeString returns Yesterday for previous day`() {
        val now = System.currentTimeMillis()
        val yesterday = now - 25 * 3600_000 // 25 hours ago
        
        assertThat(DateUtils.getRelativeTimeString(yesterday)).isEqualTo("Yesterday")
    }

    @Test
    fun `getRelativeTimeString returns days ago for recent days`() {
        val now = System.currentTimeMillis()
        val threeDaysAgo = now - 3 * 86400_000L
        
        assertThat(DateUtils.getRelativeTimeString(threeDaysAgo)).isEqualTo("3 days ago")
    }

    @Test
    fun `getRelativeTimeString returns formatted date for older timestamps`() {
        val now = System.currentTimeMillis()
        val twoWeeksAgo = now - 14 * 86400_000L
        
        // Should return formatted date instead of relative time
        val result = DateUtils.getRelativeTimeString(twoWeeksAgo)
        assertThat(result).matches("\\d{2}-\\d{2}-\\d{4}")
    }
}
