package com.navoditpublic.fees.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.navoditpublic.fees.data.local.entity.ReceiptEntity
import com.navoditpublic.fees.data.local.entity.ReceiptItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data class for receipt with student info from JOIN query
 */
data class ReceiptWithStudentInfo(
    @Embedded val receipt: ReceiptEntity,
    @ColumnInfo(name = "student_name") val studentName: String?,
    @ColumnInfo(name = "student_class") val studentClass: String?,
    @ColumnInfo(name = "student_section") val studentSection: String?,
    @ColumnInfo(name = "student_sr_number") val studentSrNumber: String?,
    @ColumnInfo(name = "student_account_number") val studentAccountNumber: String?,
    @ColumnInfo(name = "father_name") val fatherName: String?
)

@Dao
interface ReceiptDao {
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceiptItems(items: List<ReceiptItemEntity>)
    
    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)
    
    @Delete
    suspend fun deleteReceipt(receipt: ReceiptEntity)
    
    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceipt(id: Long)
    
    @Query("DELETE FROM receipt_items WHERE receipt_id = :receiptId")
    suspend fun deleteReceiptItems(receiptId: Long)
    
    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getById(id: Long): ReceiptEntity?
    
    @Query("SELECT * FROM receipts WHERE receipt_number = :receiptNumber")
    suspend fun getByReceiptNumber(receiptNumber: Int): ReceiptEntity?
    
    @Query("SELECT * FROM receipts WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<ReceiptEntity?>
    
    @Query("SELECT * FROM receipt_items WHERE receipt_id = :receiptId")
    suspend fun getReceiptItems(receiptId: Long): List<ReceiptItemEntity>
    
    @Query("SELECT * FROM receipt_items WHERE receipt_id = :receiptId")
    fun getReceiptItemsFlow(receiptId: Long): Flow<List<ReceiptItemEntity>>
    
    @Query("""
        SELECT * FROM receipts 
        WHERE student_id = :studentId 
        AND is_cancelled = 0 
        ORDER BY receipt_date DESC
    """)
    fun getReceiptsForStudent(studentId: Long): Flow<List<ReceiptEntity>>
    
    @Query("""
        SELECT * FROM receipts 
        WHERE session_id = :sessionId 
        AND is_cancelled = 0 
        ORDER BY receipt_date DESC, receipt_number DESC
    """)
    fun getReceiptsBySession(sessionId: Long): Flow<List<ReceiptEntity>>
    
    @Query("""
        SELECT * FROM receipts 
        WHERE receipt_date >= :startDate 
        AND receipt_date <= :endDate 
        AND is_cancelled = 0 
        ORDER BY receipt_date DESC, receipt_number DESC
    """)
    fun getReceiptsByDateRange(startDate: Long, endDate: Long): Flow<List<ReceiptEntity>>
    
    @Query("""
        SELECT r.*, s.name as student_name, s.current_class as student_class, s.section as student_section,
               s.sr_number as student_sr_number, s.account_number as student_account_number, s.father_name as father_name
        FROM receipts r 
        LEFT JOIN students s ON r.student_id = s.id
        WHERE r.receipt_date >= :startDate 
        AND r.receipt_date <= :endDate 
        AND r.is_cancelled = 0 
        ORDER BY r.receipt_date DESC, r.receipt_number DESC
    """)
    fun getReceiptsWithStudentByDateRange(startDate: Long, endDate: Long): Flow<List<ReceiptWithStudentInfo>>
    
    @Query("""
        SELECT * FROM receipts 
        WHERE receipt_date >= :startDate 
        AND receipt_date < :endDate 
        AND is_cancelled = 0 
        ORDER BY receipt_number ASC
    """)
    fun getDailyReceipts(startDate: Long, endDate: Long): Flow<List<ReceiptEntity>>
    
    @Query("""
        SELECT SUM(net_amount) FROM receipts 
        WHERE receipt_date >= :startDate 
        AND receipt_date < :endDate 
        AND is_cancelled = 0
    """)
    fun getDailyCollectionTotal(startDate: Long, endDate: Long): Flow<Double?>
    
    @Query("""
        SELECT SUM(net_amount) FROM receipts 
        WHERE session_id = :sessionId 
        AND receipt_date >= :startDate 
        AND receipt_date <= :endDate 
        AND is_cancelled = 0
    """)
    fun getCollectionForPeriod(sessionId: Long, startDate: Long, endDate: Long): Flow<Double?>
    
    @Query("SELECT EXISTS(SELECT 1 FROM receipts WHERE receipt_number = :receiptNumber)")
    suspend fun receiptNumberExists(receiptNumber: Int): Boolean
    
    @Query("SELECT MAX(receipt_number) FROM receipts")
    suspend fun getMaxReceiptNumber(): Int?
    
    @Query("""
        SELECT * FROM receipts 
        ORDER BY receipt_date DESC, receipt_number DESC 
        LIMIT :limit
    """)
    fun getRecentReceipts(limit: Int): Flow<List<ReceiptEntity>>
    
    @Query("SELECT COUNT(*) FROM receipts WHERE is_cancelled = 0")
    fun getTotalReceiptCount(): Flow<Int>
    
    @Query("SELECT * FROM receipts WHERE student_id = :studentId")
    suspend fun getReceiptsForStudentList(studentId: Long): List<ReceiptEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceiptItem(item: ReceiptItemEntity): Long
    
    @Query("""
        SELECT MAX(receipt_date) FROM receipts 
        WHERE student_id = :studentId 
        AND session_id = :sessionId 
        AND is_cancelled = 0
    """)
    suspend fun getLastPaymentDate(studentId: Long, sessionId: Long): Long?
    
    @Query("""
        SELECT COUNT(*) FROM receipts 
        WHERE student_id = :studentId 
        AND session_id = :sessionId 
        AND is_cancelled = 0
    """)
    suspend fun getPaymentsCount(studentId: Long, sessionId: Long): Int
}


