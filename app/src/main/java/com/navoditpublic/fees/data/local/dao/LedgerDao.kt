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
    
    @Query("""
        SELECT SUM(
            CASE WHEN entry_type = 'DEBIT' THEN debit_amount ELSE 0 END
        ) - SUM(
            CASE WHEN entry_type = 'CREDIT' THEN credit_amount ELSE 0 END
        ) 
        FROM ledger_entries 
        WHERE is_reversed = 0
    """)
    fun getTotalPendingDues(): Flow<Double?>
    
    @Query("""
        SELECT student_id FROM ledger_entries 
        WHERE is_reversed = 0 
        GROUP BY student_id 
        HAVING SUM(debit_amount) - SUM(credit_amount) > 0
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
}


