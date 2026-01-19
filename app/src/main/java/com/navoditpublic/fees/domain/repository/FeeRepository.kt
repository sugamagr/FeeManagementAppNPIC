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
}


