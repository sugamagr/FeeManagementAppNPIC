package com.navoditpublic.fees.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.LedgerEntryEntity
import com.navoditpublic.fees.data.local.entity.LedgerEntryType
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LedgerEntryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<LedgerEntryEntity>)
    
    @Update
    suspend fun update(entry: LedgerEntryEntity)
    
    @Delete
    suspend fun delete(entry: LedgerEntryEntity)
    
    @Query("SELECT * FROM ledger_entries WHERE id = :id")
    suspend fun getById(id: Long): LedgerEntryEntity?
    
    @Query("""
        SELECT * FROM ledger_entries 
        WHERE student_id = :studentId 
        AND is_reversed = 0 
        ORDER BY entry_date ASC, id ASC
    """)
    fun getLedgerForStudent(studentId: Long): Flow<List<LedgerEntryEntity>>
    
    @Query("""
        SELECT * FROM ledger_entries 
        WHERE student_id = :studentId 
        AND session_id = :sessionId 
        AND is_reversed = 0 
        ORDER BY entry_date ASC, id ASC
    """)
    fun getLedgerForStudentBySession(studentId: Long, sessionId: Long): Flow<List<LedgerEntryEntity>>
    
    @Query("""
        SELECT * FROM ledger_entries 
        WHERE student_id = :studentId 
        ORDER BY entry_date DESC, id DESC 
        LIMIT 1
    """)
    suspend fun getLastEntryForStudent(studentId: Long): LedgerEntryEntity?
    
    @Query("""
        SELECT COALESCE(
            (SELECT balance FROM ledger_entries 
             WHERE student_id = :studentId 
             AND is_reversed = 0 
             ORDER BY entry_date DESC, id DESC 
             LIMIT 1
            ), 0.0
        )
    """)
    suspend fun getCurrentBalance(studentId: Long): Double
    
    @Query("""
        SELECT COALESCE(
            (SELECT balance FROM ledger_entries 
             WHERE student_id = :studentId 
             AND is_reversed = 0 
             ORDER BY entry_date DESC, id DESC 
             LIMIT 1
            ), 0.0
        )
    """)
    fun getCurrentBalanceFlow(studentId: Long): Flow<Double>
    
    @Query("""
        SELECT SUM(debit_amount) FROM ledger_entries 
        WHERE student_id = :studentId 
        AND is_reversed = 0
    """)
    suspend fun getTotalDebits(studentId: Long): Double?
    
    @Query("""
        SELECT SUM(credit_amount) FROM ledger_entries 
        WHERE student_id = :studentId 
        AND is_reversed = 0
    """)
    suspend fun getTotalCredits(studentId: Long): Double?
    
    @Query("""
        SELECT SUM(credit_amount) FROM ledger_entries 
        WHERE student_id = :studentId 
        AND session_id = :sessionId
        AND is_reversed = 0
    """)
    suspend fun getTotalCreditsForSession(studentId: Long, sessionId: Long): Double?
    
    @Query("""
        SELECT * FROM ledger_entries 
        WHERE reference_type = 'RECEIPT' 
        AND reference_id = :receiptId
    """)
    suspend fun getEntriesByReceipt(receiptId: Long): List<LedgerEntryEntity>
    
    @Query("""
        UPDATE ledger_entries 
        SET is_reversed = 1 
        WHERE reference_type = 'RECEIPT' 
        AND reference_id = :receiptId
    """)
    suspend fun reverseEntriesForReceipt(receiptId: Long)
    
    /**
     * Get total pending dues across all ACTIVE students.
     * Only counts students with positive balances (excludes overpayments).
     * Joins with students table to exclude inactive students from totals.
     * This uses a subquery to calculate each student's balance first,
     * then sums only the positive balances to match defaulters calculation.
     */
    @Query("""
        SELECT SUM(student_balance) FROM (
            SELECT le.student_id,
                   SUM(CASE WHEN le.entry_type = 'DEBIT' THEN le.debit_amount ELSE 0 END) -
                   SUM(CASE WHEN le.entry_type = 'CREDIT' THEN le.credit_amount ELSE 0 END) as student_balance
            FROM ledger_entries le
            INNER JOIN students s ON le.student_id = s.id AND s.is_active = 1
            WHERE le.is_reversed = 0
            GROUP BY le.student_id
            HAVING student_balance > 0
        )
    """)
    fun getTotalPendingDues(): Flow<Double?>
    
    /**
     * Get IDs of ACTIVE students with pending dues.
     */
    @Query("""
        SELECT le.student_id FROM ledger_entries le
        INNER JOIN students s ON le.student_id = s.id AND s.is_active = 1
        WHERE le.is_reversed = 0 
        GROUP BY le.student_id 
        HAVING SUM(le.debit_amount) - SUM(le.credit_amount) > 0
    """)
    fun getStudentIdsWithDues(): Flow<List<Long>>
    
    @Query("DELETE FROM ledger_entries WHERE student_id = :studentId")
    suspend fun deleteByStudentId(studentId: Long)
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM ledger_entries 
            WHERE student_id = :studentId 
            AND session_id = :sessionId 
            AND reference_type = 'FEE_CHARGE'
            AND is_reversed = 0
        )
    """)
    suspend fun hasFeeChargeEntries(studentId: Long, sessionId: Long): Boolean
    
    /**
     * Get all ledger entries for a student ordered chronologically for balance recalculation
     */
    @Query("""
        SELECT * FROM ledger_entries 
        WHERE student_id = :studentId 
        AND is_reversed = 0 
        ORDER BY entry_date ASC, id ASC
    """)
    suspend fun getAllEntriesForStudentChronological(studentId: Long): List<LedgerEntryEntity>
    
    /**
     * Update the balance for a specific ledger entry
     */
    @Query("UPDATE ledger_entries SET balance = :newBalance WHERE id = :entryId")
    suspend fun updateBalance(entryId: Long, newBalance: Double)
    
    /**
     * Get opening balance entry for a student in a session
     */
    @Query("""
        SELECT * FROM ledger_entries 
        WHERE student_id = :studentId 
        AND session_id = :sessionId 
        AND reference_type = 'OPENING_BALANCE'
        AND is_reversed = 0
        LIMIT 1
    """)
    suspend fun getOpeningBalanceEntry(studentId: Long, sessionId: Long): LedgerEntryEntity?
    
    /**
     * Update opening balance entry amount and remarks
     */
    @Query("""
        UPDATE ledger_entries 
        SET debit_amount = :amount, particulars = :particulars 
        WHERE student_id = :studentId 
        AND session_id = :sessionId 
        AND reference_type = 'OPENING_BALANCE'
        AND is_reversed = 0
    """)
    suspend fun updateOpeningBalanceEntry(studentId: Long, sessionId: Long, amount: Double, particulars: String)
    
    /**
     * Delete opening balance entry for a student
     */
    @Query("""
        DELETE FROM ledger_entries 
        WHERE student_id = :studentId 
        AND session_id = :sessionId 
        AND reference_type = 'OPENING_BALANCE'
    """)
    suspend fun deleteOpeningBalanceEntry(studentId: Long, sessionId: Long)
    
    // ========== Session Promotion Methods ==========
    
    /**
     * Get all students with positive balance (dues) for carry forward
     */
    @Query("""
        SELECT student_id, 
               SUM(CASE WHEN entry_type = 'DEBIT' THEN debit_amount ELSE 0 END) -
               SUM(CASE WHEN entry_type = 'CREDIT' THEN credit_amount ELSE 0 END) as balance
        FROM ledger_entries
        WHERE is_reversed = 0
        GROUP BY student_id
        HAVING balance > 0
    """)
    suspend fun getStudentsWithPositiveBalance(): List<StudentBalance>
    
    /**
     * Get count of ACTIVE students with positive balance.
     * Joins with students table to exclude inactive students.
     */
    @Query("""
        SELECT COUNT(*) FROM (
            SELECT le.student_id
            FROM ledger_entries le
            INNER JOIN students s ON le.student_id = s.id AND s.is_active = 1
            WHERE le.is_reversed = 0
            GROUP BY le.student_id
            HAVING SUM(CASE WHEN le.entry_type = 'DEBIT' THEN le.debit_amount ELSE 0 END) -
                   SUM(CASE WHEN le.entry_type = 'CREDIT' THEN le.credit_amount ELSE 0 END) > 0
        )
    """)
    suspend fun getStudentsWithDuesCount(): Int
    
    /**
     * Get total pending dues across all ACTIVE students (sync version).
     * Joins with students table to exclude inactive students from totals.
     */
    @Query("""
        SELECT COALESCE(SUM(balance), 0.0) FROM (
            SELECT le.student_id,
                   SUM(CASE WHEN le.entry_type = 'DEBIT' THEN le.debit_amount ELSE 0 END) -
                   SUM(CASE WHEN le.entry_type = 'CREDIT' THEN le.credit_amount ELSE 0 END) as balance
            FROM ledger_entries le
            INNER JOIN students s ON le.student_id = s.id AND s.is_active = 1
            WHERE le.is_reversed = 0
            GROUP BY le.student_id
            HAVING balance > 0
        )
    """)
    suspend fun getTotalPendingDuesSync(): Double
    
    /**
     * Delete all FEE_CHARGE entries for a session (for revert)
     */
    @Query("""
        DELETE FROM ledger_entries 
        WHERE session_id = :sessionId 
        AND reference_type = 'FEE_CHARGE'
    """)
    suspend fun deleteFeeChargeEntriesForSession(sessionId: Long): Int
    
    /**
     * Delete all OPENING_BALANCE entries for a session (for revert)
     */
    @Query("""
        DELETE FROM ledger_entries 
        WHERE session_id = :sessionId 
        AND reference_type = 'OPENING_BALANCE'
    """)
    suspend fun deleteOpeningBalanceEntriesForSession(sessionId: Long): Int
    
    /**
     * Delete all ledger entries for a session (for complete revert)
     */
    @Query("DELETE FROM ledger_entries WHERE session_id = :sessionId")
    suspend fun deleteAllEntriesForSession(sessionId: Long): Int
    
    /**
     * Get count of ledger entries for a session
     */
    @Query("SELECT COUNT(*) FROM ledger_entries WHERE session_id = :sessionId AND is_reversed = 0")
    suspend fun getEntryCountForSession(sessionId: Long): Int
    
    /**
     * Get all student IDs who have ledger entries in a specific session.
     * Used for viewing historical session data.
     */
    @Query("""
        SELECT DISTINCT student_id FROM ledger_entries 
        WHERE session_id = :sessionId 
        AND is_reversed = 0
    """)
    suspend fun getStudentIdsWithEntriesInSession(sessionId: Long): List<Long>
    
    /**
     * Get total pending dues for a specific session.
     * Only considers entries for that session, not cumulative balance.
     */
    @Query("""
        SELECT COALESCE(
            SUM(CASE WHEN entry_type = 'DEBIT' THEN debit_amount ELSE 0 END) -
            SUM(CASE WHEN entry_type = 'CREDIT' THEN credit_amount ELSE 0 END),
            0.0
        ) FROM ledger_entries le
        INNER JOIN students s ON le.student_id = s.id AND s.is_active = 1
        WHERE le.session_id = :sessionId
        AND le.is_reversed = 0
    """)
    suspend fun getTotalPendingDuesForSession(sessionId: Long): Double
    
    /**
     * Get total collection (credits) for a specific session.
     */
    @Query("""
        SELECT COALESCE(SUM(credit_amount), 0.0) FROM ledger_entries 
        WHERE session_id = :sessionId 
        AND entry_type = 'CREDIT'
        AND is_reversed = 0
    """)
    suspend fun getTotalCollectionForSession(sessionId: Long): Double
    
    /**
     * Get students with dues count for a specific session.
     */
    @Query("""
        SELECT COUNT(*) FROM (
            SELECT le.student_id
            FROM ledger_entries le
            INNER JOIN students s ON le.student_id = s.id AND s.is_active = 1
            WHERE le.session_id = :sessionId
            AND le.is_reversed = 0
            GROUP BY le.student_id
            HAVING SUM(CASE WHEN le.entry_type = 'DEBIT' THEN le.debit_amount ELSE 0 END) -
                   SUM(CASE WHEN le.entry_type = 'CREDIT' THEN le.credit_amount ELSE 0 END) > 0
        )
    """)
    suspend fun getStudentsWithDuesCountForSession(sessionId: Long): Int
    
    /**
     * Check if session has any RECEIPT entries (payments collected)
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM ledger_entries 
            WHERE session_id = :sessionId 
            AND reference_type = 'RECEIPT'
            AND is_reversed = 0
        )
    """)
    suspend fun hasReceiptEntriesForSession(sessionId: Long): Boolean
    
    /**
     * Delete all RECEIPT (credit) entries for a session (for revert when force deleting receipts)
     */
    @Query("""
        DELETE FROM ledger_entries
        WHERE session_id = :sessionId
        AND reference_type = 'RECEIPT'
    """)
    suspend fun deleteReceiptEntriesForSession(sessionId: Long): Int
    
    /**
     * Get the closing balance for a student in a specific session.
     * This is calculated as (total debits - total credits) for entries in that session.
     */
    @Query("""
        SELECT COALESCE(
            SUM(CASE WHEN entry_type = 'DEBIT' THEN debit_amount ELSE 0 END) -
            SUM(CASE WHEN entry_type = 'CREDIT' THEN credit_amount ELSE 0 END),
            0.0
        )
        FROM ledger_entries
        WHERE student_id = :studentId
        AND session_id = :sessionId
        AND is_reversed = 0
    """)
    suspend fun getClosingBalanceForSession(studentId: Long, sessionId: Long): Double
    
    @Query("""
        SELECT * FROM ledger_entries 
        WHERE reference_id = :receiptId
        AND reference_type = 'RECEIPT'
        AND is_reversed = 0
        LIMIT 1
    """)
    suspend fun getEntryForReceipt(receiptId: Long): LedgerEntryEntity?
    
    // ========== Student Deletion Checks ==========
    
    /**
     * Check if a student has any ledger entries.
     * Used to determine if a student can be permanently deleted.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM ledger_entries WHERE student_id = :studentId)")
    suspend fun hasEntriesForStudent(studentId: Long): Boolean
    
    // ========== Session-Based Viewing Methods ==========
    
    /**
     * Get all distinct student IDs that have ledger entries in a specific session.
     * Used to determine which students were "part of" a session for historical viewing.
     */
    @Query("""
        SELECT DISTINCT student_id 
        FROM ledger_entries 
        WHERE session_id = :sessionId 
        AND is_reversed = 0
    """)
    suspend fun getStudentIdsForSession(sessionId: Long): List<Long>
    
    /**
     * Get ledger entries for a student up to a certain date.
     * Used when viewing historical sessions to hide future entries.
     */
    @Query("""
        SELECT * FROM ledger_entries 
        WHERE student_id = :studentId 
        AND entry_date <= :maxDate
        AND is_reversed = 0 
        ORDER BY entry_date ASC, id ASC
    """)
    suspend fun getLedgerForStudentUpToDate(studentId: Long, maxDate: Long): List<LedgerEntryEntity>
    
    /**
     * Check if a student has entries in sessions after the given session.
     * Used to show "View Current Session" button in historical ledger views.
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM ledger_entries le
            INNER JOIN academic_sessions s ON le.session_id = s.id
            WHERE le.student_id = :studentId 
            AND le.is_reversed = 0
            AND s.start_date > (SELECT end_date FROM academic_sessions WHERE id = :sessionId)
        )
    """)
    suspend fun hasEntriesInLaterSessions(studentId: Long, sessionId: Long): Boolean
    
    /**
     * Get pending dues for ACTIVE students as of a specific date (session end date).
     * Used for historical session viewing.
     */
    @Query("""
        SELECT COALESCE(SUM(balance), 0.0) FROM (
            SELECT le.student_id,
                   SUM(CASE WHEN le.entry_type = 'DEBIT' THEN le.debit_amount ELSE 0 END) -
                   SUM(CASE WHEN le.entry_type = 'CREDIT' THEN le.credit_amount ELSE 0 END) as balance
            FROM ledger_entries le
            INNER JOIN students s ON le.student_id = s.id
            WHERE le.is_reversed = 0
            AND le.entry_date <= :asOfDate
            AND le.student_id IN (
                SELECT DISTINCT student_id FROM ledger_entries 
                WHERE session_id = :sessionId AND is_reversed = 0
            )
            GROUP BY le.student_id
            HAVING balance > 0
        )
    """)
    suspend fun getPendingDuesAsOfDate(sessionId: Long, asOfDate: Long): Double
    
}

/**
 * Data class for student balance query result
 */
data class StudentBalance(
    val student_id: Long,
    val balance: Double
)


