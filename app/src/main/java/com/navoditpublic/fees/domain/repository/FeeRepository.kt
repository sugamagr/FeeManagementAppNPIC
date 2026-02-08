package com.navoditpublic.fees.domain.repository

import com.navoditpublic.fees.data.local.entity.FeeType
import com.navoditpublic.fees.domain.model.FeeStructure
import com.navoditpublic.fees.domain.model.LedgerEntry
import com.navoditpublic.fees.domain.model.Receipt
import com.navoditpublic.fees.domain.model.ReceiptItem
import com.navoditpublic.fees.domain.model.ReceiptWithStudent
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Fee-related operations.
 */
interface FeeRepository {
    
    // Fee Structure
    suspend fun insertFeeStructure(feeStructure: FeeStructure): Result<Long>
    
    suspend fun updateFeeStructure(feeStructure: FeeStructure): Result<Unit>
    
    suspend fun deleteFeeStructure(feeStructure: FeeStructure): Result<Unit>
    
    /**
     * Soft delete a fee structure by setting isActive = false with optional remarks
     */
    suspend fun softDeleteFeeStructure(
        sessionId: Long,
        className: String,
        feeType: FeeType,
        remarks: String = "Cleared by user"
    ): Result<Unit>
    
    suspend fun getFeeStructureById(id: Long): FeeStructure?
    
    fun getFeeStructureBySession(sessionId: Long): Flow<List<FeeStructure>>
    
    suspend fun getFeeForClass(sessionId: Long, className: String, feeType: FeeType): FeeStructure?
    
    fun getFeesForClass(sessionId: Long, className: String): Flow<List<FeeStructure>>
    
    fun getFeesByType(sessionId: Long, feeType: FeeType): Flow<List<FeeStructure>>
    
    suspend fun getAdmissionFee(sessionId: Long, className: String): FeeStructure?
    
    suspend fun getRegistrationFee(sessionId: Long, className: String): FeeStructure?
    
    // Receipts
    suspend fun insertReceipt(receipt: Receipt, items: List<ReceiptItem>): Result<Long>
    
    suspend fun updateReceipt(receipt: Receipt): Result<Unit>
    
    suspend fun cancelReceipt(receiptId: Long, reason: String): Result<Unit>
    
    suspend fun getReceiptById(id: Long): Receipt?
    
    suspend fun getReceiptByNumber(receiptNumber: Int): Receipt?
    
    fun getReceiptByIdFlow(id: Long): Flow<Receipt?>
    
    fun getReceiptsForStudent(studentId: Long): Flow<List<Receipt>>
    
    fun getReceiptsBySession(sessionId: Long): Flow<List<Receipt>>
    
    fun getReceiptsByDateRange(startDate: Long, endDate: Long): Flow<List<Receipt>>
    
    fun getReceiptsWithStudentsByDateRange(startDate: Long, endDate: Long): Flow<List<ReceiptWithStudent>>
    
    fun getDailyReceipts(date: Long): Flow<List<Receipt>>
    
    fun getDailyCollectionTotal(date: Long): Flow<Double?>
    
    fun getMonthlyCollectionTotal(startOfMonth: Long, endOfMonth: Long): Flow<Double?>
    
    fun getCollectionForPeriod(sessionId: Long, startDate: Long, endDate: Long): Flow<Double?>
    
    suspend fun receiptNumberExists(receiptNumber: Int): Boolean
    
    suspend fun getMaxReceiptNumber(): Int?
    
    fun getRecentReceipts(limit: Int): Flow<List<Receipt>>
    
    fun getRecentReceiptsWithStudents(limit: Int): Flow<List<ReceiptWithStudent>>
    
    // Ledger
    suspend fun insertLedgerEntry(entry: LedgerEntry): Result<Long>
    
    suspend fun updateLedgerEntry(entry: LedgerEntry): Result<Unit>
    
    fun getLedgerForStudent(studentId: Long): Flow<List<LedgerEntry>>
    
    fun getLedgerForStudentBySession(studentId: Long, sessionId: Long): Flow<List<LedgerEntry>>
    
    suspend fun getCurrentBalance(studentId: Long): Double
    
    fun getCurrentBalanceFlow(studentId: Long): Flow<Double>
    
    suspend fun getTotalDebits(studentId: Long): Double
    
    suspend fun getTotalCredits(studentId: Long): Double
    
    fun getTotalPendingDues(): Flow<Double?>
    
    fun getStudentIdsWithDues(): Flow<List<Long>>
    
    /**
     * @deprecated Use getCurrentBalance() instead - it includes all fees from ledger (opening balance, 
     * tuition, transport, admission) minus payments. The ledger is the single source of truth.
     * 
     * For total fees charged, use getTotalDebits(studentId).
     * For total payments, use getTotalCredits(studentId).
     * For current balance owed, use getCurrentBalance(studentId).
     */
    @Deprecated(
        message = "Use getCurrentBalance() for dues, getTotalDebits() for total fees charged. Ledger is single source of truth.",
        replaceWith = ReplaceWith("getCurrentBalance(studentId)")
    )
    suspend fun calculateExpectedSessionDues(studentId: Long, sessionId: Long): Double
    
    /**
     * Get total payments made by student in the session
     */
    suspend fun getTotalPaymentsForSession(studentId: Long, sessionId: Long): Double
    
    /**
     * Get all fee structures for a class in a session
     */
    suspend fun getFeeStructuresForClass(className: String, sessionId: Long): List<FeeStructure>
    
    /**
     * Create initial payment entry for data migration (fees already received)
     */
    suspend fun createInitialPaymentEntry(
        studentId: Long,
        sessionId: Long,
        amount: Double,
        date: Long,
        description: String
    ): Result<Long>
    
    /**
     * Create opening balance DEBIT entry in ledger for a student
     * This records previous year dues carried forward
     */
    suspend fun createOpeningBalanceEntry(
        studentId: Long,
        sessionId: Long,
        amount: Double,
        date: Long,
        remarks: String
    ): Result<Long>
    
    /**
     * Sync opening balance entry with student entity.
     * Creates, updates, or deletes the ledger entry based on the new amount.
     * This should be called when editing a student's opening balance.
     */
    suspend fun syncOpeningBalanceEntry(
        studentId: Long,
        sessionId: Long,
        newAmount: Double,
        date: Long,
        remarks: String
    ): Result<Unit>
    
    /**
     * Add session fees (tuition + transport) for a single student as DEBIT entries in ledger
     * Creates entries dated at session start (April 1)
     * 
     * @param studentId The student to add fees for
     * @param sessionId The session to add fees for
     * @param addTuition Whether to add tuition fees
     * @param addTransport Whether to add transport fees
     * @return Result with total fees added
     */
    suspend fun addSessionFeesForStudent(
        studentId: Long,
        sessionId: Long,
        addTuition: Boolean = true,
        addTransport: Boolean = true
    ): Result<Double>
    
    /**
     * Add session fees for all active students
     * Creates DEBIT entries for tuition (12 months) and transport (11 months) dated April 1
     * 
     * @param sessionId The session to add fees for
     * @param addTuition Whether to add tuition fees
     * @param addTransport Whether to add transport fees
     * @return Result with count of students processed and total fees added
     */
    suspend fun addSessionFeesForAllStudents(
        sessionId: Long,
        addTuition: Boolean = true,
        addTransport: Boolean = true
    ): Result<Pair<Int, Double>>
    
    /**
     * Check if a student already has session fee entries for the given session
     */
    suspend fun hasSessionFeeEntries(studentId: Long, sessionId: Long): Boolean
    
    /**
     * Recalculate all ledger balances for a student in chronological order.
     * This ensures correct running balances even after backdated entries.
     * Should be called after inserting backdated entries.
     */
    suspend fun recalculateStudentBalances(studentId: Long)
    
    /**
     * Get the date of the last payment made by a student in a session
     */
    suspend fun getLastPaymentDate(studentId: Long, sessionId: Long): Long?
    
    /**
     * Get the count of payments/receipts for a student in a session
     */
    suspend fun getPaymentsCount(studentId: Long, sessionId: Long): Int
    
    // ========== Session Promotion Methods ==========
    
    /**
     * Carry forward dues for all students with positive balance.
     * Creates OPENING_BALANCE entries in the new session.
     * 
     * @param newSessionId The target session
     * @param sessionStartDate The start date for entries (April 1)
     * @return Pair of (students processed, total amount carried forward)
     */
    suspend fun carryForwardDuesForAllStudents(
        newSessionId: Long,
        sessionStartDate: Long
    ): Result<Pair<Int, Double>>
    
    /**
     * Copy fee structures from one session to another
     * @return Number of fee structures copied
     */
    suspend fun copyFeeStructures(sourceSessionId: Long, targetSessionId: Long): Result<Int>
    
    /**
     * Delete all fee charge entries for a session (for revert)
     */
    suspend fun deleteFeeChargeEntriesForSession(sessionId: Long): Result<Int>
    
    /**
     * Delete all opening balance entries for a session (for revert)
     */
    suspend fun deleteOpeningBalanceEntriesForSession(sessionId: Long): Result<Int>
    
    /**
     * Delete fee structures for a session (for revert)
     */
    suspend fun deleteFeeStructuresForSession(sessionId: Long): Result<Int>
    
    /**
     * Get students with positive balance (dues) count
     */
    suspend fun getStudentsWithDuesCount(): Int
    
    /**
     * Get total pending dues across all students (non-flow)
     */
    suspend fun getTotalPendingDuesSync(): Double
    
    // ========== Session-Based Viewing Methods ==========
    
    /**
     * Get all student IDs who have ledger entries in a specific session.
     * Used for viewing historical session data.
     */
    suspend fun getStudentIdsWithEntriesInSession(sessionId: Long): List<Long>
    
    /**
     * Get total pending dues for a specific session only.
     * Only considers active students and entries for that session.
     */
    suspend fun getTotalPendingDuesForSession(sessionId: Long): Double
    
    /**
     * Get count of students with dues in a specific session.
     */
    suspend fun getStudentsWithDuesCountForSession(sessionId: Long): Int
    
    /**
     * Get count of receipts in a session
     */
    suspend fun getReceiptCountForSession(sessionId: Long): Int
    
    /**
     * Get total amount collected in a session
     */
    suspend fun getTotalCollectionForSession(sessionId: Long): Double
    
    /**
     * Delete all receipts and their items in a session (for forced revert)
     */
    suspend fun deleteReceiptsForSession(sessionId: Long): Result<Int>
    
    /**
     * Delete all RECEIPT ledger entries for a session (for forced revert)
     */
    suspend fun deleteReceiptLedgerEntriesForSession(sessionId: Long): Result<Int>
    
    /**
     * Get fee structure count for a session
     */
    suspend fun getFeeStructureCountForSession(sessionId: Long): Int
    
    // ========== Session Balance Adjustment Methods ==========
    
    /**
     * Get closing balance for a student in a specific session.
     * This is the balance at the end of the session (all debits - all credits).
     */
    suspend fun getClosingBalanceForSession(studentId: Long, sessionId: Long): Double
    
    /**
     * Update the opening balance in the next session based on the closing balance from previous session.
     * This is called when making changes to a previous session to keep balances consistent.
     * 
     * @param studentId The student whose balance to update
     * @param sourceSessionId The session that was edited (source/previous session)
     * @param targetSessionId The session whose opening balance should be updated (target/current session)
     * @return Result with the new opening balance amount
     */
    suspend fun updateOpeningBalanceFromClosingBalance(
        studentId: Long,
        sourceSessionId: Long,
        targetSessionId: Long
    ): Result<Double>
    
    /**
     * Edit a receipt and update its ledger entry.
     * This handles the case where receipt amount, date, payment mode, or remarks are changed.
     * 
     * @param receipt The updated receipt with new values
     * @param items The receipt items (may be updated)
     * @return Result with the old amount for reference (to calculate balance change)
     */
    suspend fun editReceiptWithLedger(
        receipt: Receipt,
        items: List<ReceiptItem>
    ): Result<Double>
    
    /**
     * Get the ledger entry associated with a receipt
     */
    suspend fun getLedgerEntryForReceipt(receiptId: Long): LedgerEntry?
    
    // ========== Student Deletion Checks ==========
    
    /**
     * Check if a student has any ledger entries (fees charged or payments).
     * Used to determine if a student can be permanently deleted.
     * 
     * @param studentId The student to check
     * @return true if the student has any ledger entries
     */
    suspend fun hasLedgerEntries(studentId: Long): Boolean
    
    /**
     * Check if a student has any receipts (payments received).
     * Used to determine if a student can be permanently deleted.
     * 
     * @param studentId The student to check
     * @return true if the student has any receipts
     */
    suspend fun hasReceipts(studentId: Long): Boolean
    
    /**
     * Check if a student can be permanently deleted.
     * A student can only be deleted if they have no financial records
     * (no ledger entries and no receipts).
     * 
     * @param studentId The student to check
     * @return true if the student can be safely deleted
     */
    suspend fun canDeleteStudent(studentId: Long): Boolean
}
