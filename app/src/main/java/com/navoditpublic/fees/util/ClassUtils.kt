package com.navoditpublic.fees.util

/**
 * Utility object for class-related operations.
 * Contains class progression mapping for session promotion.
 */
object ClassUtils {
    
    /**
     * All classes in order from lowest to highest
     */
    val ALL_CLASSES = listOf(
        "NC", "LKG", "UKG",
        "1st", "2nd", "3rd", "4th", "5th",
        "6th", "7th", "8th",
        "9th", "10th", "11th", "12th"
    )
    
    /**
     * Class progression map: current class -> next class
     * null means student has passed out (completed 12th)
     */
    val CLASS_PROGRESSION: Map<String, String?> = mapOf(
        "NC" to "LKG",
        "LKG" to "UKG",
        "UKG" to "1st",
        "1st" to "2nd",
        "2nd" to "3rd",
        "3rd" to "4th",
        "4th" to "5th",
        "5th" to "6th",
        "6th" to "7th",
        "7th" to "8th",
        "8th" to "9th",
        "9th" to "10th",
        "10th" to "11th",
        "11th" to "12th",
        "12th" to null  // Passed out
    )
    
    /**
     * Reverse progression map for reverting promotions: current class -> previous class
     */
    val CLASS_DEMOTION: Map<String, String?> = mapOf(
        "LKG" to "NC",
        "UKG" to "LKG",
        "1st" to "UKG",
        "2nd" to "1st",
        "3rd" to "2nd",
        "4th" to "3rd",
        "5th" to "4th",
        "6th" to "5th",
        "7th" to "6th",
        "8th" to "7th",
        "9th" to "8th",
        "10th" to "9th",
        "11th" to "10th",
        "12th" to "11th",
        "NC" to null  // Cannot demote below NC
    )
    
    /**
     * Get the next class for promotion
     * @return next class name, or null if student has passed out (was in 12th)
     */
    fun getNextClass(currentClass: String): String? {
        return CLASS_PROGRESSION[currentClass]
    }
    
    /**
     * Get the previous class for demotion (revert)
     * @return previous class name, or null if cannot demote (was in NC)
     */
    fun getPreviousClass(currentClass: String): String? {
        return CLASS_DEMOTION[currentClass]
    }
    
    /**
     * Check if a class is valid
     */
    fun isValidClass(className: String): Boolean {
        return className in ALL_CLASSES
    }
    
    /**
     * Get class index for sorting (0 for NC, 14 for 12th)
     */
    fun getClassIndex(className: String): Int {
        return ALL_CLASSES.indexOf(className)
    }
    
    /**
     * Get class order for sorting - handles various class name formats.
     * Returns 0 for NC/Nursery, 1 for LKG, 2 for UKG, 3 for 1st, etc.
     * Returns 100 for unknown classes (sorts to end).
     * 
     * Handles formats like: "NC", "NURSERY", "LKG", "L.K.G", "1st", "1ST", "1", etc.
     */
    fun getClassOrder(className: String): Int {
        val normalized = className.uppercase().trim()
            .replace(".", "")  // Remove dots (L.K.G -> LKG)
            .replace(" ", "")  // Remove spaces
        
        return when {
            // Nursery variants
            normalized == "NC" || normalized == "NURSERY" || normalized == "NUR" -> 0
            // LKG variants
            normalized == "LKG" || normalized == "LOWERKINDERGARTEN" -> 1
            // UKG variants
            normalized == "UKG" || normalized == "UPPERKINDERGARTEN" -> 2
            // Numeric classes (1st through 12th)
            else -> {
                // Extract the numeric part
                val numericPart = normalized
                    .replace("ST", "")
                    .replace("ND", "")
                    .replace("RD", "")
                    .replace("TH", "")
                    .filter { it.isDigit() }
                    .toIntOrNull()
                
                if (numericPart != null && numericPart in 1..12) {
                    numericPart + 2  // 1st = 3, 2nd = 4, ..., 12th = 14
                } else {
                    100  // Unknown class, sort to end
                }
            }
        }
    }
    
    /**
     * Classes that can be promoted (all except 12th)
     */
    val PROMOTABLE_CLASSES = ALL_CLASSES.filter { it != "12th" }
    
    /**
     * Classes that can be demoted (all except NC)
     */
    val DEMOTABLE_CLASSES = ALL_CLASSES.filter { it != "NC" }
    
    // ==================== Account Number Prefix Utilities ====================
    
    /**
     * Prefix for passed-out students' account numbers
     */
    const val PASS_PREFIX = "PASS"
    
    /**
     * Generate session code for account number prefix.
     * Converts "2024-25" to "2425", "2025-26" to "2526", etc.
     * 
     * @param sessionName Session name in format "YYYY-YY" (e.g., "2024-25")
     * @return Short session code (e.g., "2425")
     */
    fun getSessionCode(sessionName: String): String {
        // Try to parse "2024-25" format
        val parts = sessionName.split("-")
        return if (parts.size == 2) {
            val startYear = parts[0].takeLast(2) // "24" from "2024"
            val endYear = parts[1].takeLast(2)   // "25" from "25" or "2025"
            "$startYear$endYear"
        } else {
            // Fallback: use first 4 digits
            sessionName.filter { it.isDigit() }.take(4)
        }
    }
    
    /**
     * Generate passed-out account number prefix for a session.
     * 
     * @param sessionName Session name (e.g., "2024-25")
     * @return Prefix string (e.g., "PASS2425-")
     */
    fun getPassedOutPrefix(sessionName: String): String {
        return "$PASS_PREFIX${getSessionCode(sessionName)}-"
    }
    
    /**
     * Add passed-out prefix to an account number.
     * 
     * @param accountNumber Original account number (e.g., "5")
     * @param sessionName Session name (e.g., "2024-25")
     * @return Prefixed account number (e.g., "PASS2425-5")
     */
    fun addPassedOutPrefix(accountNumber: String, sessionName: String): String {
        // Don't double-prefix
        if (accountNumber.startsWith(PASS_PREFIX)) {
            return accountNumber
        }
        return "${getPassedOutPrefix(sessionName)}$accountNumber"
    }
    
    /**
     * Remove passed-out prefix from an account number.
     * 
     * @param accountNumber Prefixed account number (e.g., "PASS2425-5")
     * @return Original account number (e.g., "5"), or unchanged if no prefix
     */
    fun removePassedOutPrefix(accountNumber: String): String {
        // Match pattern: PASS followed by 4 digits and a hyphen
        val prefixPattern = Regex("^$PASS_PREFIX\\d{4}-")
        return accountNumber.replace(prefixPattern, "")
    }
    
    /**
     * Check if an account number has the passed-out prefix.
     */
    fun hasPassedOutPrefix(accountNumber: String): Boolean {
        return accountNumber.startsWith(PASS_PREFIX) && 
               accountNumber.matches(Regex("^$PASS_PREFIX\\d{4}-.+"))
    }
    
    /**
     * Extract session code from a prefixed account number.
     * 
     * @param accountNumber Prefixed account number (e.g., "PASS2425-5")
     * @return Session code (e.g., "2425"), or null if not prefixed
     */
    fun extractSessionCode(accountNumber: String): String? {
        if (!hasPassedOutPrefix(accountNumber)) return null
        return accountNumber.removePrefix(PASS_PREFIX).take(4)
    }
}
