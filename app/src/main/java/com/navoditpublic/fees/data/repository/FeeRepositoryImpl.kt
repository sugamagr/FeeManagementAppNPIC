package com.navoditpublic.fees.data.repository

import androidx.room.withTransaction
import com.navoditpublic.fees.data.local.FeesDatabase
import com.navoditpublic.fees.data.local.dao.AcademicSessionDao
import com.navoditpublic.fees.data.local.dao.FeeStructureDao
import com.navoditpublic.fees.data.local.dao.LedgerDao
import com.navoditpublic.fees.data.local.dao.ReceiptDao
import com.navoditpublic.fees.data.local.dao.SchoolSettingsDao
import com.navoditpublic.fees.data.local.dao.StudentDao
import com.navoditpublic.fees.data.local.dao.TransportEnrollmentDao
import com.navoditpublic.fees.data.local.dao.TransportRouteDao
import com.navoditpublic.fees.data.local.entity.FeeType
import com.navoditpublic.fees.data.local.entity.LedgerEntryEntity
import com.navoditpublic.fees.data.local.entity.LedgerEntryType
import com.navoditpublic.fees.data.local.entity.LedgerReferenceType
import com.navoditpublic.fees.domain.model.FeeStructure
import com.navoditpublic.fees.domain.model.LedgerEntry
import com.navoditpublic.fees.domain.model.Receipt
import com.navoditpublic.fees.domain.model.ReceiptItem
import com.navoditpublic.fees.domain.model.ReceiptWithStudent
import com.navoditpublic.fees.domain.repository.FeeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeeRepositoryImpl @Inject constructor(
    private val database: FeesDatabase,
    private val feeStructureDao: FeeStructureDao,
    private val receiptDao: ReceiptDao,
    private val ledgerDao: LedgerDao,
    private val studentDao: StudentDao,
    private val schoolSettingsDao: SchoolSettingsDao,
    private val transportRouteDao: TransportRouteDao,
    private val transportEnrollmentDao: TransportEnrollmentDao,
    private val academicSessionDao: AcademicSessionDao
) : FeeRepository {
    
    // Fee Structure
    override suspend fun insertFeeStructure(feeStructure: FeeStructure): Result<Long> = runCatching {
        feeStructureDao.insert(feeStructure.toEntity())
    }
    
    override suspend fun updateFeeStructure(feeStructure: FeeStructure): Result<Unit> = runCatching {
        feeStructureDao.update(feeStructure.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }
    
    override suspend fun deleteFeeStructure(feeStructure: FeeStructure): Result<Unit> = runCatching {
        feeStructureDao.delete(feeStructure.toEntity())
    }
    
    override suspend fun softDeleteFeeStructure(
        sessionId: Long,
        className: String,
        feeType: FeeType,
        remarks: String
    ): Result<Unit> = runCatching {
        val existingFee = feeStructureDao.getFeeForClass(sessionId, className, feeType)
        if (existingFee != null) {
            feeStructureDao.update(
                existingFee.copy(
                    isActive = false,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
    
    override suspend fun getFeeStructureById(id: Long): FeeStructure? {
        return feeStructureDao.getById(id)?.let { FeeStructure.fromEntity(it) }
    }
    
    override fun getFeeStructureBySession(sessionId: Long): Flow<List<FeeStructure>> {
        return feeStructureDao.getFeeStructureBySession(sessionId).map { entities ->
            entities.map { FeeStructure.fromEntity(it) }
        }
    }
    
    override suspend fun getFeeForClass(sessionId: Long, className: String, feeType: FeeType): FeeStructure? {
        return feeStructureDao.getFeeForClass(sessionId, className, feeType)?.let { FeeStructure.fromEntity(it) }
    }
    
    override fun getFeesForClass(sessionId: Long, className: String): Flow<List<FeeStructure>> {
        return feeStructureDao.getFeesForClass(sessionId, className).map { entities ->
            entities.map { FeeStructure.fromEntity(it) }
        }
    }
    
    override fun getFeesByType(sessionId: Long, feeType: FeeType): Flow<List<FeeStructure>> {
        return feeStructureDao.getFeesByType(sessionId, feeType).map { entities ->
            entities.map { FeeStructure.fromEntity(it) }
        }
    }
    
    override suspend fun getAdmissionFee(sessionId: Long, className: String): FeeStructure? {
        return feeStructureDao.getAdmissionFee(sessionId, className)?.let { FeeStructure.fromEntity(it) }
    }
    
    override suspend fun getRegistrationFee(sessionId: Long, className: String): FeeStructure? {
        return feeStructureDao.getRegistrationFee(sessionId, className)?.let { FeeStructure.fromEntity(it) }
    }
    
    // Receipts
    override suspend fun insertReceipt(receipt: Receipt, items: List<ReceiptItem>): Result<Long> = runCatching {
        // Use transaction to ensure all operations succeed or fail together
        database.withTransaction {
            // Insert receipt
            val receiptId = receiptDao.insertReceipt(receipt.toEntity())
            
            // Insert receipt items
            val itemEntities = items.map { it.toEntity().copy(receiptId = receiptId) }
            receiptDao.insertReceiptItems(itemEntities)
            
            // Include remarks in particulars if available
            val particulars = buildString {
                append("Receipt #${receipt.receiptNumber}")
                if (receipt.remarks?.isNotBlank() == true) {
                    append(" - ${receipt.remarks}")
                }
            }
            
            // Create ledger entry for payment (CREDIT) with temporary balance (will be recalculated)
            val ledgerEntry = LedgerEntryEntity(
                studentId = receipt.studentId,
                sessionId = receipt.sessionId,
                entryDate = receipt.receiptDate,
                particulars = particulars,
                entryType = LedgerEntryType.CREDIT,
                debitAmount = 0.0,
                creditAmount = receipt.netAmount,
                balance = 0.0, // Temporary - will be recalculated
                referenceType = LedgerReferenceType.RECEIPT,
                referenceId = receiptId
            )
            ledgerDao.insert(ledgerEntry)
            
            // If there's a discount (full year payment), create additional ledger entry
            if (receipt.discountAmount > 0) {
                val discountEntry = LedgerEntryEntity(
                    studentId = receipt.studentId,
                    sessionId = receipt.sessionId,
                    entryDate = receipt.receiptDate,
                    particulars = "Full Year Discount (1 month tuition free) - Receipt #${receipt.receiptNumber}",
                    entryType = LedgerEntryType.CREDIT,
                    debitAmount = 0.0,
                    creditAmount = receipt.discountAmount,
                    balance = 0.0, // Temporary - will be recalculated
                    referenceType = LedgerReferenceType.DISCOUNT,
                    referenceId = receiptId
                )
                ledgerDao.insert(discountEntry)
            }
            
            // Recalculate all balances for this student to ensure correctness
            // This handles backdated receipts properly
            recalculateStudentBalances(receipt.studentId)
            
            // Update last receipt number in school settings
            schoolSettingsDao.updateLastReceiptNumber(receipt.receiptNumber)
            
            receiptId
        }
    }
    
    /**
     * Recalculate all ledger balances for a student in chronological order.
     * This ensures correct running balances even after backdated entries.
     */
    override suspend fun recalculateStudentBalances(studentId: Long) {
        val entries = ledgerDao.getAllEntriesForStudentChronological(studentId)
        var runningBalance = 0.0
        
        entries.forEach { entry ->
            runningBalance += entry.debitAmount - entry.creditAmount
            if (entry.balance != runningBalance) {
                ledgerDao.updateBalance(entry.id, runningBalance)
            }
        }
    }
    
    override suspend fun updateReceipt(receipt: Receipt): Result<Unit> = runCatching {
        receiptDao.updateReceipt(receipt.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }
    
    override suspend fun cancelReceipt(receiptId: Long, reason: String): Result<Unit> = runCatching {
        // Use transaction to ensure all operations succeed or fail together
        database.withTransaction {
            val receipt = receiptDao.getById(receiptId) ?: throw IllegalArgumentException("Receipt not found")
            
            // Mark receipt as cancelled
            receiptDao.updateReceipt(
                receipt.copy(
                    isCancelled = true,
                    cancelledAt = System.currentTimeMillis(),
                    cancellationReason = reason,
                    updatedAt = System.currentTimeMillis()
                )
            )
            
            // Reverse ledger entries (marks original entries as reversed)
            ledgerDao.reverseEntriesForReceipt(receiptId)
            
            // Create reversal DEBIT entry for payment amount
            // Balance is temporary (0.0) - will be recalculated at the end
            val reversalEntry = LedgerEntryEntity(
                studentId = receipt.studentId,
                sessionId = receipt.sessionId,
                entryDate = System.currentTimeMillis(),
                particulars = "Cancelled Receipt #${receipt.receiptNumber} - $reason",
                entryType = LedgerEntryType.DEBIT,
                debitAmount = receipt.netAmount,
                creditAmount = 0.0,
                balance = 0.0, // Temporary - will be recalculated
                referenceType = LedgerReferenceType.REVERSAL,
                referenceId = receiptId
            )
            ledgerDao.insert(reversalEntry)
            
            // Also reverse discount if there was one
            if (receipt.discountAmount > 0) {
                val discountReversalEntry = LedgerEntryEntity(
                    studentId = receipt.studentId,
                    sessionId = receipt.sessionId,
                    entryDate = System.currentTimeMillis(),
                    particulars = "Cancelled Full Year Discount - Receipt #${receipt.receiptNumber}",
                    entryType = LedgerEntryType.DEBIT,
                    debitAmount = receipt.discountAmount,
                    creditAmount = 0.0,
                    balance = 0.0, // Temporary - will be recalculated
                    referenceType = LedgerReferenceType.REVERSAL,
                    referenceId = receiptId
                )
                ledgerDao.insert(discountReversalEntry)
            }
            
            // Recalculate all balances for this student to ensure correctness
            recalculateStudentBalances(receipt.studentId)
        }
    }
    
    override suspend fun getReceiptById(id: Long): Receipt? {
        val receiptEntity = receiptDao.getById(id) ?: return null
        val items = receiptDao.getReceiptItems(id).map { ReceiptItem.fromEntity(it) }
        return Receipt.fromEntity(receiptEntity, items)
    }
    
    override suspend fun getReceiptByNumber(receiptNumber: Int): Receipt? {
        val receiptEntity = receiptDao.getByReceiptNumber(receiptNumber) ?: return null
        val items = receiptDao.getReceiptItems(receiptEntity.id).map { ReceiptItem.fromEntity(it) }
        return Receipt.fromEntity(receiptEntity, items)
    }
    
    override fun getReceiptByIdFlow(id: Long): Flow<Receipt?> {
        return receiptDao.getByIdFlow(id).map { entity ->
            entity?.let {
                val items = receiptDao.getReceiptItems(it.id).map { item -> ReceiptItem.fromEntity(item) }
                Receipt.fromEntity(it, items)
            }
        }
    }
    
    override fun getReceiptsForStudent(studentId: Long): Flow<List<Receipt>> {
        return receiptDao.getReceiptsForStudent(studentId).map { entities ->
            entities.map { Receipt.fromEntity(it) }
        }
    }
    
    override fun getReceiptsBySession(sessionId: Long): Flow<List<Receipt>> {
        return receiptDao.getReceiptsBySession(sessionId).map { entities ->
            entities.map { Receipt.fromEntity(it) }
        }
    }
    
    override fun getReceiptsByDateRange(startDate: Long, endDate: Long): Flow<List<Receipt>> {
        return receiptDao.getReceiptsByDateRange(startDate, endDate).map { entities ->
            entities.map { Receipt.fromEntity(it) }
        }
    }
    
    override fun getReceiptsWithStudentsByDateRange(startDate: Long, endDate: Long): Flow<List<ReceiptWithStudent>> {
        return receiptDao.getReceiptsWithStudentByDateRange(startDate, endDate).map { results ->
            results.map { data ->
                ReceiptWithStudent(
                    receipt = Receipt.fromEntity(data.receipt),
                    studentName = data.studentName ?: "Unknown",
                    studentClass = data.studentClass ?: "",
                    studentSection = data.studentSection ?: "",
                    studentSrNumber = data.studentSrNumber ?: "",
                    studentAccountNumber = data.studentAccountNumber ?: "",
                    fatherName = data.fatherName ?: ""
                )
            }
        }
    }
    
    override fun getDailyReceipts(date: Long): Flow<List<Receipt>> {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis
        
        return receiptDao.getDailyReceipts(startOfDay, endOfDay).map { entities ->
            entities.map { Receipt.fromEntity(it) }
        }
    }
    
    override fun getDailyCollectionTotal(date: Long): Flow<Double?> {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis
        
        return receiptDao.getDailyCollectionTotal(startOfDay, endOfDay)
    }
    
    override fun getMonthlyCollectionTotal(startOfMonth: Long, endOfMonth: Long): Flow<Double?> {
        return receiptDao.getDailyCollectionTotal(startOfMonth, endOfMonth)
    }
    
    override fun getCollectionForPeriod(sessionId: Long, startDate: Long, endDate: Long): Flow<Double?> {
        return receiptDao.getCollectionForPeriod(sessionId, startDate, endDate)
    }
    
    override suspend fun receiptNumberExists(receiptNumber: Int): Boolean {
        return receiptDao.receiptNumberExists(receiptNumber)
    }
    
    override suspend fun getMaxReceiptNumber(): Int? {
        return receiptDao.getMaxReceiptNumber()
    }
    
    override fun getRecentReceipts(limit: Int): Flow<List<Receipt>> {
        return receiptDao.getRecentReceipts(limit).map { entities ->
            entities.map { Receipt.fromEntity(it) }
        }
    }
    
    override fun getRecentReceiptsWithStudents(limit: Int): Flow<List<ReceiptWithStudent>> {
        return receiptDao.getRecentReceipts(limit).map { entities ->
            entities.mapNotNull { receiptEntity ->
                val student = studentDao.getById(receiptEntity.studentId)
                student?.let {
                    ReceiptWithStudent(
                        receipt = Receipt.fromEntity(receiptEntity),
                        studentName = it.name,
                        studentClass = it.currentClass,
                        studentSection = it.section,
                        studentSrNumber = it.srNumber,
                        studentAccountNumber = it.accountNumber,
                        fatherName = it.fatherName
                    )
                }
            }
        }
    }
    
    // Ledger
    override suspend fun insertLedgerEntry(entry: LedgerEntry): Result<Long> = runCatching {
        ledgerDao.insert(entry.toEntity())
    }
    
    override suspend fun updateLedgerEntry(entry: LedgerEntry): Result<Unit> = runCatching {
        ledgerDao.update(entry.toEntity())
    }
    
    override fun getLedgerForStudent(studentId: Long): Flow<List<LedgerEntry>> {
        return ledgerDao.getLedgerForStudent(studentId).map { entities ->
            entities.map { LedgerEntry.fromEntity(it) }
        }
    }
    
    override fun getLedgerForStudentBySession(studentId: Long, sessionId: Long): Flow<List<LedgerEntry>> {
        return ledgerDao.getLedgerForStudentBySession(studentId, sessionId).map { entities ->
            entities.map { LedgerEntry.fromEntity(it) }
        }
    }
    
    override suspend fun getCurrentBalance(studentId: Long): Double {
        return ledgerDao.getCurrentBalance(studentId)
    }
    
    override fun getCurrentBalanceFlow(studentId: Long): Flow<Double> {
        return ledgerDao.getCurrentBalanceFlow(studentId)
    }
    
    override suspend fun getTotalDebits(studentId: Long): Double {
        return ledgerDao.getTotalDebits(studentId) ?: 0.0
    }
    
    override suspend fun getTotalCredits(studentId: Long): Double {
        return ledgerDao.getTotalCredits(studentId) ?: 0.0
    }
    
    override fun getTotalPendingDues(): Flow<Double?> {
        return ledgerDao.getTotalPendingDues()
    }
    
    override fun getStudentIdsWithDues(): Flow<List<Long>> {
        return ledgerDao.getStudentIdsWithDues()
    }
    
    override suspend fun calculateExpectedSessionDues(studentId: Long, sessionId: Long): Double {
        val student = studentDao.getById(studentId) ?: return 0.0
        val className = student.currentClass
        
        var expectedFees = 0.0
        
        // Check if monthly or annual class
        val isMonthlyClass = className in FeeStructure.MONTHLY_FEE_CLASSES
        
        if (isMonthlyClass) {
            // Monthly fee for full 12 months (discount is applied when full year is paid at once)
            val monthlyFee = feeStructureDao.getFeeForClass(sessionId, className, FeeType.MONTHLY)
            if (monthlyFee != null) {
                // Calculate for 12 months - discount is handled during collection
                expectedFees += monthlyFee.amount * 12
            }
        } else {
            // Annual fee
            val annualFee = feeStructureDao.getFeeForClass(sessionId, className, FeeType.ANNUAL)
            if (annualFee != null) {
                expectedFees += annualFee.amount
            }
            
            // Registration fee for 9th-12th
            if (className in FeeStructure.REGISTRATION_FEE_CLASSES) {
                val regFee = feeStructureDao.getRegistrationFee(sessionId, className)
                if (regFee != null) {
                    expectedFees += regFee.amount
                }
            }
        }
        
        // Add transport fees if applicable (11 months max - June excluded)
        if (student.hasTransport && student.transportRouteId != null) {
            val route = transportRouteDao.getById(student.transportRouteId)
            if (route != null) {
                // Get session dates for proper transport fee calculation
                val session = academicSessionDao.getById(sessionId)
                if (session != null) {
                    // startDate and endDate are non-nullable in AcademicSessionEntity
                    val transportMonths = calculateTransportMonthsForSession(
                        studentId, 
                        session.startDate, 
                        session.endDate, 
                        route.getFeeForClass(className)
                    )
                    expectedFees += transportMonths.second  // Add total transport fee
                } else {
                    // Fallback to 11 months if session not found - use class-based fee
                    expectedFees += route.getFeeForClass(className) * 11
                }
            }
        }
        
        // Subtract total payments made in this session (including discounts)
        val totalPayments = getTotalPaymentsForSession(studentId, sessionId)
        
        return (expectedFees - totalPayments).coerceAtLeast(0.0)
    }
    
    override suspend fun getTotalPaymentsForSession(studentId: Long, sessionId: Long): Double {
        return ledgerDao.getTotalCreditsForSession(studentId, sessionId) ?: 0.0
    }
    
    override suspend fun getFeeStructuresForClass(className: String, sessionId: Long): List<FeeStructure> {
        return feeStructureDao.getFeesForClassSync(sessionId, className).map { FeeStructure.fromEntity(it) }
    }
    
    override suspend fun createInitialPaymentEntry(
        studentId: Long,
        sessionId: Long,
        amount: Double,
        date: Long,
        description: String
    ): Result<Long> = runCatching {
        // Insert with temporary balance (will be recalculated)
        val ledgerEntry = LedgerEntryEntity(
            studentId = studentId,
            sessionId = sessionId,
            entryDate = date,
            particulars = description,
            entryType = LedgerEntryType.CREDIT,
            debitAmount = 0.0,
            creditAmount = amount,
            balance = 0.0, // Temporary - will be recalculated
            referenceType = LedgerReferenceType.ADJUSTMENT,
            referenceId = null
        )
        val entryId = ledgerDao.insert(ledgerEntry)
        
        // Recalculate all balances to handle backdated entries correctly
        recalculateStudentBalances(studentId)
        
        entryId
    }
    
    override suspend fun createOpeningBalanceEntry(
        studentId: Long,
        sessionId: Long,
        amount: Double,
        date: Long,
        remarks: String
    ): Result<Long> = runCatching {
        val particulars = if (remarks.isNotBlank()) {
            "Opening Balance - $remarks"
        } else {
            "Opening Balance (Previous Year Dues)"
        }
        
        // Insert with temporary balance (will be recalculated)
        val ledgerEntry = LedgerEntryEntity(
            studentId = studentId,
            sessionId = sessionId,
            entryDate = date,
            particulars = particulars,
            entryType = LedgerEntryType.DEBIT,
            debitAmount = amount,
            creditAmount = 0.0,
            balance = 0.0, // Temporary - will be recalculated
            referenceType = LedgerReferenceType.OPENING_BALANCE,
            referenceId = 0
        )
        val entryId = ledgerDao.insert(ledgerEntry)
        
        // Recalculate all balances to handle backdated entries correctly
        recalculateStudentBalances(studentId)
        
        entryId
    }
    
    override suspend fun syncOpeningBalanceEntry(
        studentId: Long,
        sessionId: Long,
        newAmount: Double,
        date: Long,
        remarks: String
    ): Result<Unit> = runCatching {
        val existingEntry = ledgerDao.getOpeningBalanceEntry(studentId, sessionId)
        
        when {
            // Case 1: No entry exists and new amount is 0 -> do nothing
            existingEntry == null && newAmount <= 0 -> {
                // Nothing to do
            }
            
            // Case 2: No entry exists but new amount > 0 -> create entry
            existingEntry == null && newAmount > 0 -> {
                createOpeningBalanceEntry(studentId, sessionId, newAmount, date, remarks).getOrThrow()
            }
            
            // Case 3: Entry exists but new amount is 0 -> delete entry
            existingEntry != null && newAmount <= 0 -> {
                ledgerDao.deleteOpeningBalanceEntry(studentId, sessionId)
                recalculateStudentBalances(studentId)
            }
            
            // Case 4: Entry exists and new amount > 0 -> update entry
            existingEntry != null && newAmount > 0 -> {
                val particulars = if (remarks.isNotBlank()) {
                    "Opening Balance - $remarks"
                } else {
                    "Opening Balance (Previous Year Dues)"
                }
                ledgerDao.updateOpeningBalanceEntry(studentId, sessionId, newAmount, particulars)
                recalculateStudentBalances(studentId)
            }
        }
    }
    
    override suspend fun addSessionFeesForStudent(
        studentId: Long,
        sessionId: Long,
        addTuition: Boolean,
        addTransport: Boolean
    ): Result<Double> = runCatching {
        // Use transaction to ensure all fee entries are added atomically
        // The duplicate check MUST be inside the transaction to prevent race conditions
        database.withTransaction {
            // Check for duplicate fee entries - prevent adding fees twice
            // This check is inside the transaction to ensure atomicity
            if (hasSessionFeeEntries(studentId, sessionId)) {
                return@withTransaction 0.0 // Already has fee entries, skip
            }
            
            val student = studentDao.getById(studentId) ?: throw IllegalArgumentException("Student not found")
            val session = academicSessionDao.getById(sessionId) ?: throw IllegalArgumentException("Session not found")
            val className = student.currentClass
            val sessionStartDate = session.startDate
            val sessionName = session.sessionName
            
            var totalFeesAdded = 0.0
            
            // Add admission fee if not already paid
            if (!student.admissionFeePaid) {
                val admissionFee = feeStructureDao.getAdmissionFee(sessionId, className)
                if (admissionFee != null && admissionFee.amount > 0) {
                    val ledgerEntry = LedgerEntryEntity(
                        studentId = studentId,
                        sessionId = sessionId,
                        entryDate = sessionStartDate,
                        particulars = "Admission Fee (New Admission)",
                        entryType = LedgerEntryType.DEBIT,
                        debitAmount = admissionFee.amount,
                        creditAmount = 0.0,
                        balance = 0.0, // Temporary - will be recalculated
                        referenceType = LedgerReferenceType.FEE_CHARGE,
                        referenceId = 0
                    )
                    ledgerDao.insert(ledgerEntry)
                    totalFeesAdded += admissionFee.amount
                    
                    // Mark admission fee as paid to prevent duplicate charges
                    studentDao.updateAdmissionFeePaid(studentId, true)
                }
            }
            
            // Add tuition fees (with temporary balance - will be recalculated at end)
            if (addTuition) {
                val isMonthlyClass = className in FeeStructure.MONTHLY_FEE_CLASSES
                
                if (isMonthlyClass) {
                    // Monthly fee for 12 months
                    val monthlyFee = feeStructureDao.getFeeForClass(sessionId, className, FeeType.MONTHLY)
                    if (monthlyFee != null && monthlyFee.amount > 0) {
                        val totalTuition = monthlyFee.amount * 12
                        
                        val ledgerEntry = LedgerEntryEntity(
                            studentId = studentId,
                            sessionId = sessionId,
                            entryDate = sessionStartDate,
                            particulars = "Tuition Fee - Session $sessionName (12 months @ ₹${monthlyFee.amount.toInt()}/month)",
                            entryType = LedgerEntryType.DEBIT,
                            debitAmount = totalTuition,
                            creditAmount = 0.0,
                            balance = 0.0, // Temporary - will be recalculated
                            referenceType = LedgerReferenceType.FEE_CHARGE,
                            referenceId = 0
                        )
                        ledgerDao.insert(ledgerEntry)
                        totalFeesAdded += totalTuition
                    }
                } else {
                    // Annual fee for higher classes
                    val annualFee = feeStructureDao.getFeeForClass(sessionId, className, FeeType.ANNUAL)
                    if (annualFee != null && annualFee.amount > 0) {
                        val ledgerEntry = LedgerEntryEntity(
                            studentId = studentId,
                            sessionId = sessionId,
                            entryDate = sessionStartDate,
                            particulars = "Annual Fee - Session $sessionName",
                            entryType = LedgerEntryType.DEBIT,
                            debitAmount = annualFee.amount,
                            creditAmount = 0.0,
                            balance = 0.0, // Temporary - will be recalculated
                            referenceType = LedgerReferenceType.FEE_CHARGE,
                            referenceId = 0
                        )
                        ledgerDao.insert(ledgerEntry)
                        totalFeesAdded += annualFee.amount
                    }
                    
                    // Registration fee for 9th-12th
                    if (className in FeeStructure.REGISTRATION_FEE_CLASSES) {
                        val regFee = feeStructureDao.getRegistrationFee(sessionId, className)
                        if (regFee != null && regFee.amount > 0) {
                            val ledgerEntry = LedgerEntryEntity(
                                studentId = studentId,
                                sessionId = sessionId,
                                entryDate = sessionStartDate,
                                particulars = "Registration Fee - Session $sessionName",
                                entryType = LedgerEntryType.DEBIT,
                                debitAmount = regFee.amount,
                                creditAmount = 0.0,
                                balance = 0.0, // Temporary - will be recalculated
                                referenceType = LedgerReferenceType.FEE_CHARGE,
                                referenceId = 0
                            )
                            ledgerDao.insert(ledgerEntry)
                            totalFeesAdded += regFee.amount
                        }
                    }
                }
            }
            
            // Add transport fees (11 months - June excluded)
            if (addTransport && student.hasTransport && student.transportRouteId != null) {
                val route = transportRouteDao.getById(student.transportRouteId)
                if (route != null) {
                    val monthlyTransportFee = route.getFeeForClass(className)
                    val transportMonths = 11 // June excluded
                    val totalTransportFee = monthlyTransportFee * transportMonths
                    
                    if (totalTransportFee > 0) {
                        val ledgerEntry = LedgerEntryEntity(
                            studentId = studentId,
                            sessionId = sessionId,
                            entryDate = sessionStartDate,
                            particulars = "Transport Fee - Session $sessionName (11 months excl. June, ${route.routeName})",
                            entryType = LedgerEntryType.DEBIT,
                            debitAmount = totalTransportFee,
                            creditAmount = 0.0,
                            balance = 0.0, // Temporary - will be recalculated
                            referenceType = LedgerReferenceType.FEE_CHARGE,
                            referenceId = 0
                        )
                        ledgerDao.insert(ledgerEntry)
                        totalFeesAdded += totalTransportFee
                    }
                }
            }
            
            // Recalculate all balances to ensure correct running totals
            if (totalFeesAdded > 0) {
                recalculateStudentBalances(studentId)
            }
            
            totalFeesAdded
        }
    }
    
    override suspend fun addSessionFeesForAllStudents(
        sessionId: Long,
        addTuition: Boolean,
        addTransport: Boolean
    ): Result<Pair<Int, Double>> = runCatching {
        val students = studentDao.getAllActiveStudentsList()
        var totalStudentsProcessed = 0
        var totalFeesAdded = 0.0
        
        for (student in students) {
            // Check if student already has fee entries for this session
            if (!hasSessionFeeEntries(student.id, sessionId)) {
                val result = addSessionFeesForStudent(student.id, sessionId, addTuition, addTransport)
                result.onSuccess { fees ->
                    if (fees > 0) {
                        totalStudentsProcessed++
                        totalFeesAdded += fees
                    }
                }
            }
        }
        
        Pair(totalStudentsProcessed, totalFeesAdded)
    }
    
    override suspend fun hasSessionFeeEntries(studentId: Long, sessionId: Long): Boolean {
        return ledgerDao.hasFeeChargeEntries(studentId, sessionId)
    }
    
    /**
     * Calculate transport months and total fee for a session based on actual enrollment dates.
     * Excludes June (month index 5) from transport fee calculation.
     * 
     * @return Pair(totalMonths, totalFee)
     */
    private suspend fun calculateTransportMonthsForSession(
        studentId: Long,
        sessionStart: Long,
        sessionEnd: Long,
        monthlyFee: Double
    ): Pair<Int, Double> {
        // Get all transport enrollments within this session
        val enrollments = transportEnrollmentDao.getEnrollmentsInDateRange(studentId, sessionStart, sessionEnd)
        
        if (enrollments.isEmpty()) {
            // No enrollments found - use default 11 months (June excluded)
            return Pair(11, monthlyFee * 11)
        }
        
        // Calculate months based on actual enrollment periods
        val allMonths = getMonthsInRange(sessionStart, sessionEnd)
        var totalMonths = 0
        var totalFee = 0.0
        
        for ((year, month) in allMonths) {
            // Skip June (month index 5) - no transport fee in June
            if (month == Calendar.JUNE) continue
            
            val monthStart = getMonthStart(year, month)
            val monthEnd = getMonthEnd(year, month)
            
            // Check if there's an active enrollment during this month
            val activeEnrollment = enrollments.find { enrollment ->
                val enrollmentStart = enrollment.startDate
                val enrollmentEnd = enrollment.endDate ?: Long.MAX_VALUE
                enrollmentStart <= monthEnd && enrollmentEnd >= monthStart
            }
            
            if (activeEnrollment != null) {
                totalMonths++
                totalFee += activeEnrollment.monthlyFeeAtEnrollment
            }
        }
        
        // If no months calculated but student has transport, use default
        if (totalMonths == 0 && enrollments.isNotEmpty()) {
            return Pair(11, monthlyFee * 11)
        }
        
        return Pair(totalMonths, totalFee)
    }
    
    private fun getMonthsInRange(startDate: Long, endDate: Long): List<Pair<Int, Int>> {
        val months = mutableListOf<Pair<Int, Int>>()
        val calendar = Calendar.getInstance().apply { timeInMillis = startDate }
        val endCalendar = Calendar.getInstance().apply { timeInMillis = endDate }
        
        while (calendar.before(endCalendar) || 
               (calendar.get(Calendar.YEAR) == endCalendar.get(Calendar.YEAR) && 
                calendar.get(Calendar.MONTH) == endCalendar.get(Calendar.MONTH))) {
            months.add(Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)))
            calendar.add(Calendar.MONTH, 1)
        }
        
        return months
    }
    
    private fun getMonthStart(year: Int, month: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    private fun getMonthEnd(year: Int, month: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    
    override suspend fun getLastPaymentDate(studentId: Long, sessionId: Long): Long? {
        return receiptDao.getLastPaymentDate(studentId, sessionId)
    }
    
    override suspend fun getPaymentsCount(studentId: Long, sessionId: Long): Int {
        return receiptDao.getPaymentsCount(studentId, sessionId)
    }
    
    // ========== Session Promotion Methods ==========
    
    override suspend fun carryForwardDuesForAllStudents(
        newSessionId: Long,
        sessionStartDate: Long
    ): Result<Pair<Int, Double>> = runCatching {
        database.withTransaction {
            val studentsWithBalance = ledgerDao.getStudentsWithPositiveBalance()
            var totalAmount = 0.0
            var studentCount = 0
            
            for (studentBalance in studentsWithBalance) {
                // Skip if already has opening balance in new session (idempotent)
                val existingEntry = ledgerDao.getOpeningBalanceEntry(studentBalance.student_id, newSessionId)
                if (existingEntry != null) {
                    continue
                }
                
                val balance = studentBalance.balance
                if (balance > 0) {
                    val ledgerEntry = LedgerEntryEntity(
                        studentId = studentBalance.student_id,
                        sessionId = newSessionId,
                        entryDate = sessionStartDate,
                        particulars = "Previous Session Dues (Carried Forward)",
                        entryType = LedgerEntryType.DEBIT,
                        debitAmount = balance,
                        creditAmount = 0.0,
                        balance = balance, // Starting balance for new session
                        referenceType = LedgerReferenceType.OPENING_BALANCE,
                        referenceId = 0
                    )
                    ledgerDao.insert(ledgerEntry)
                    totalAmount += balance
                    studentCount++
                }
            }
            
            Pair(studentCount, totalAmount)
        }
    }
    
    override suspend fun copyFeeStructures(
        sourceSessionId: Long, 
        targetSessionId: Long
    ): Result<Int> = runCatching {
        // Check if target session already has fee structures (idempotent)
        val existingCount = feeStructureDao.getCountForSession(targetSessionId)
        if (existingCount > 0) {
            // Already has fee structures, skip to avoid duplicates
            return@runCatching 0
        }
        
        val sourceFees = feeStructureDao.getAllFeeStructuresForSession(sourceSessionId)
        val newFees = sourceFees.map { fee ->
            fee.copy(
                id = 0,
                sessionId = targetSessionId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }
        feeStructureDao.insertAll(newFees)
        newFees.size
    }
    
    override suspend fun deleteFeeChargeEntriesForSession(sessionId: Long): Result<Int> = runCatching {
        ledgerDao.deleteFeeChargeEntriesForSession(sessionId)
    }
    
    override suspend fun deleteOpeningBalanceEntriesForSession(sessionId: Long): Result<Int> = runCatching {
        ledgerDao.deleteOpeningBalanceEntriesForSession(sessionId)
    }
    
    override suspend fun deleteFeeStructuresForSession(sessionId: Long): Result<Int> = runCatching {
        feeStructureDao.deleteFeeStructuresForSession(sessionId)
    }
    
    override suspend fun getStudentsWithDuesCount(): Int {
        return ledgerDao.getStudentsWithDuesCount()
    }
    
    override suspend fun getTotalPendingDuesSync(): Double {
        return ledgerDao.getTotalPendingDuesSync()
    }
    
    // ========== Session-Based Viewing Methods ==========
    
    override suspend fun getStudentIdsWithEntriesInSession(sessionId: Long): List<Long> {
        return ledgerDao.getStudentIdsWithEntriesInSession(sessionId)
    }
    
    override suspend fun getTotalPendingDuesForSession(sessionId: Long): Double {
        return ledgerDao.getTotalPendingDuesForSession(sessionId)
    }
    
    override suspend fun getStudentsWithDuesCountForSession(sessionId: Long): Int {
        return ledgerDao.getStudentsWithDuesCountForSession(sessionId)
    }
    
    override suspend fun getReceiptCountForSession(sessionId: Long): Int {
        return receiptDao.getReceiptCountForSession(sessionId)
    }
    
    override suspend fun getTotalCollectionForSession(sessionId: Long): Double {
        return receiptDao.getTotalCollectionForSession(sessionId)
    }
    
    override suspend fun deleteReceiptsForSession(sessionId: Long): Result<Int> = runCatching {
        database.withTransaction {
            // Delete receipt items first (due to foreign key)
            receiptDao.deleteReceiptItemsForSession(sessionId)
            // Then delete receipts
            receiptDao.deleteReceiptsForSession(sessionId)
        }
    }
    
    override suspend fun deleteReceiptLedgerEntriesForSession(sessionId: Long): Result<Int> = runCatching {
        ledgerDao.deleteReceiptEntriesForSession(sessionId)
    }
    
    override suspend fun getFeeStructureCountForSession(sessionId: Long): Int {
        return feeStructureDao.getCountForSession(sessionId)
    }
    
    // ========== Session Balance Adjustment Methods ==========
    
    override suspend fun getClosingBalanceForSession(studentId: Long, sessionId: Long): Double {
        return ledgerDao.getClosingBalanceForSession(studentId, sessionId)
    }
    
    override suspend fun updateOpeningBalanceFromClosingBalance(
        studentId: Long,
        sourceSessionId: Long,
        targetSessionId: Long
    ): Result<Double> = runCatching {
        database.withTransaction {
            // Get the closing balance from the source (previous) session
            val closingBalance = ledgerDao.getClosingBalanceForSession(studentId, sourceSessionId)
            
            // Get the session start date for the entry date
            val targetSession = academicSessionDao.getById(targetSessionId)
                ?: throw IllegalArgumentException("Target session not found")
            
            // Sync the opening balance entry (create/update/delete as needed)
            syncOpeningBalanceEntry(
                studentId = studentId,
                sessionId = targetSessionId,
                newAmount = closingBalance,
                date = targetSession.startDate,
                remarks = "Auto-adjusted from previous session"
            ).getOrThrow()
            
            closingBalance
        }
    }
    
    override suspend fun editReceiptWithLedger(
        receipt: Receipt,
        items: List<ReceiptItem>
    ): Result<Double> = runCatching {
        database.withTransaction {
            // Get the old receipt to compare amounts
            val oldReceipt = receiptDao.getById(receipt.id)
                ?: throw IllegalArgumentException("Receipt not found")
            
            // Cannot edit cancelled receipts
            if (oldReceipt.isCancelled) {
                throw IllegalStateException("Cannot edit a cancelled receipt")
            }
            
            val oldAmount = oldReceipt.netAmount
            val newAmount = receipt.netAmount
            
            // Update receipt
            receiptDao.updateReceipt(receipt.toEntity().copy(updatedAt = System.currentTimeMillis()))
            
            // Update receipt items - delete old ones and insert new ones
            receiptDao.deleteReceiptItems(receipt.id)
            items.forEach { item ->
                // Reset id to 0 for new insert, set correct receiptId
                receiptDao.insertReceiptItem(item.copy(id = 0, receiptId = receipt.id).toEntity())
            }
            
            // Find the original CREDIT ledger entry for this receipt
            val existingEntry = ledgerDao.getEntryForReceipt(receipt.id)
            
            if (existingEntry != null && !existingEntry.isReversed) {
                // Update the ledger entry (amount and/or particulars may have changed)
                val amountChanged = oldAmount != newAmount
                ledgerDao.update(existingEntry.copy(
                    creditAmount = newAmount,
                    particulars = "Receipt #${receipt.receiptNumber} - ${receipt.remarks ?: "Fee Payment"}${if (amountChanged) " (Edited)" else ""}"
                ))
                
                // Only recalculate balances if amount changed
                if (amountChanged) {
                    recalculateStudentBalances(receipt.studentId)
                }
            }
            
            oldAmount // Return old amount for reference
        }
    }
    
    override suspend fun getLedgerEntryForReceipt(receiptId: Long): LedgerEntry? {
        return ledgerDao.getEntryForReceipt(receiptId)?.let { LedgerEntry.fromEntity(it) }
    }
    
    // ========== Student Deletion Checks ==========
    
    override suspend fun hasLedgerEntries(studentId: Long): Boolean {
        return ledgerDao.hasEntriesForStudent(studentId)
    }
    
    override suspend fun hasReceipts(studentId: Long): Boolean {
        return receiptDao.hasReceiptsForStudent(studentId)
    }
    
    override suspend fun canDeleteStudent(studentId: Long): Boolean {
        return !hasLedgerEntries(studentId) && !hasReceipts(studentId)
    }
}
