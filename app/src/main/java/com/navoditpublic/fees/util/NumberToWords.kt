package com.navoditpublic.fees.util

/**
 * Converts a number to Indian currency words format.
 * Example: 8300 -> "Eight Thousand Three Hundred Rupees Only"
 */
object NumberToWords {
    
    private val ones = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )
    
    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )
    
    fun convert(amount: Double): String {
        val rupees = amount.toLong()
        // Round to handle floating-point precision issues (e.g., 99.99 might be 99.9899999...)
        val paise = kotlin.math.round((amount - rupees) * 100).toInt()
        
        if (rupees == 0L && paise == 0) {
            return "Zero Rupees Only"
        }
        
        val result = StringBuilder()
        
        if (rupees > 0) {
            result.append(convertToWords(rupees))
            result.append(" Rupees")
        }
        
        if (paise > 0) {
            if (result.isNotEmpty()) {
                result.append(" and ")
            }
            result.append(convertToWords(paise.toLong()))
            result.append(" Paise")
        }
        
        result.append(" Only")
        
        return result.toString()
    }
    
    private fun convertToWords(number: Long): String {
        if (number == 0L) return ""
        
        var n = number
        val result = StringBuilder()
        
        // Crores (10,000,000)
        if (n >= 10000000) {
            result.append(convertToWords(n / 10000000))
            result.append(" Crore ")
            n %= 10000000
        }
        
        // Lakhs (100,000)
        if (n >= 100000) {
            result.append(convertToWords(n / 100000))
            result.append(" Lakh ")
            n %= 100000
        }
        
        // Thousands (1,000)
        if (n >= 1000) {
            result.append(convertToWords(n / 1000))
            result.append(" Thousand ")
            n %= 1000
        }
        
        // Hundreds (100)
        if (n >= 100) {
            result.append(ones[(n / 100).toInt()])
            result.append(" Hundred ")
            n %= 100
        }
        
        // Tens and Ones
        if (n > 0) {
            if (n < 20) {
                result.append(ones[n.toInt()])
            } else {
                result.append(tens[(n / 10).toInt()])
                if (n % 10 > 0) {
                    result.append(" ")
                    result.append(ones[(n % 10).toInt()])
                }
            }
        }
        
        return result.toString().trim()
    }
}


