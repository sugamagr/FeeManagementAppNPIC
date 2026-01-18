package com.navoditpublic.fees.data.local

import com.navoditpublic.fees.data.local.dao.AcademicSessionDao
import com.navoditpublic.fees.data.local.dao.FeeStructureDao
import com.navoditpublic.fees.data.local.dao.LedgerDao
import com.navoditpublic.fees.data.local.dao.ReceiptDao
import com.navoditpublic.fees.data.local.dao.StudentDao
import com.navoditpublic.fees.data.local.dao.TransportRouteDao
import com.navoditpublic.fees.data.local.dao.TransportEnrollmentDao
import com.navoditpublic.fees.data.local.entity.FeeStructureEntity
import com.navoditpublic.fees.data.local.entity.FeeType
import com.navoditpublic.fees.data.local.entity.LedgerEntryEntity
import com.navoditpublic.fees.data.local.entity.LedgerEntryType
import com.navoditpublic.fees.data.local.entity.LedgerReferenceType
import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.data.local.entity.ReceiptEntity
import com.navoditpublic.fees.data.local.entity.ReceiptItemEntity
import com.navoditpublic.fees.data.local.entity.StudentEntity
import com.navoditpublic.fees.data.local.entity.TransportRouteEntity
import com.navoditpublic.fees.data.local.entity.TransportEnrollmentEntity
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Seeds comprehensive demo data for testing all reports and features.
 * Call clearDemoData() to remove all demo data.
 */
@Singleton
class DemoDataSeeder @Inject constructor(
    private val studentDao: StudentDao,
    private val academicSessionDao: AcademicSessionDao,
    private val feeStructureDao: FeeStructureDao,
    private val transportRouteDao: TransportRouteDao,
    private val transportEnrollmentDao: TransportEnrollmentDao,
    private val receiptDao: ReceiptDao,
    private val ledgerDao: LedgerDao
) {
    
    companion object {
        const val DEMO_PREFIX = "DEMO"
        
        private val FIRST_NAMES = listOf(
            "Aarav", "Vivaan", "Aditya", "Vihaan", "Arjun", "Sai", "Reyansh", "Ayaan", "Krishna", "Ishaan",
            "Shaurya", "Atharva", "Advait", "Pranav", "Kabir", "Aadhya", "Ananya", "Diya", "Myra", "Sara",
            "Prisha", "Aanya", "Aarohi", "Anvi", "Pari", "Pihu", "Kavya", "Riya", "Ira", "Avni",
            "Rahul", "Amit", "Priya", "Neha", "Raj", "Pooja", "Vikram", "Sneha", "Rohan", "Anjali",
            "Karan", "Shruti", "Dev", "Kritika", "Aryan", "Tanya", "Mohit", "Deepika", "Nikhil", "Sanya",
            "Ravi", "Meera", "Akash", "Nidhi", "Varun", "Swati", "Gaurav", "Rashmi", "Yash", "Pallavi"
        )
        
        private val LAST_NAMES = listOf(
            "Sharma", "Verma", "Singh", "Kumar", "Gupta", "Patel", "Yadav", "Chauhan", "Joshi", "Mishra",
            "Pandey", "Agarwal", "Dubey", "Tiwari", "Srivastava", "Rastogi", "Saxena", "Kapoor", "Malhotra", "Khanna"
        )
        
        private val VILLAGES = listOf(
            "Chandpur", "Rampur", "Sultanpur", "Mirzapur", "Fatehpur", "Jalalpur", "Islampur", "Nizampur", 
            "Shahpur", "Akbarpur", "Mohanpur", "Govindpur", "Krishnapur", "Shivpur", "Raghunathpur",
            "Haripur", "Devpur", "Lakshmipur", "Saraswatipur", "Durganagar", "Kurgaon", "Madila", "Deeppur"
        )
        
        private val LOCALITIES = listOf(
            "Gandhi Nagar", "Nehru Colony", "Shastri Nagar", "Rajendra Nagar", "Patel Chowk",
            "Station Road", "Civil Lines", "Sadar Bazaar", "Kotwali", "Naya Mohalla"
        )
        
        private val DISTRICTS = listOf("Shahjahanpur", "Bareilly", "Rampur", "Budaun", "Pilibhit")
        
        private val CLASSES = listOf("NC", "LKG", "UKG", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "11th", "12th")
        
        private val MONTHLY_FEES = mapOf(
            "NC" to 400.0, "LKG" to 400.0, "UKG" to 400.0,
            "1st" to 450.0, "2nd" to 450.0, 
            "3rd" to 500.0, "4th" to 500.0, "5th" to 500.0,
            "6th" to 600.0, "7th" to 600.0, "8th" to 600.0
        )
        
        private val ANNUAL_FEES = mapOf(
            "9th" to 9000.0, "10th" to 10000.0, "11th" to 11000.0, "12th" to 12000.0
        )
        
        private val TRANSPORT_ROUTES = listOf(
            Triple("Khudaganj Nagar", Triple(450.0, 675.0, 900.0), ""),
            Triple("Kandharapur Navdadiya", Triple(550.0, 825.0, 1100.0), ""),
            Triple("Kurgaon", Triple(600.0, 900.0, 1200.0), ""),
            Triple("Jaitipur", Triple(700.0, 1050.0, 1400.0), ""),
            Triple("Madila", Triple(500.0, 750.0, 1000.0), ""),
            Triple("Khakharaiya", Triple(600.0, 900.0, 1200.0), ""),
            Triple("Deeppur", Triple(450.0, 675.0, 900.0), ""),
            Triple("Jalalpur", Triple(450.0, 675.0, 900.0), ""),
            Triple("Dirdira", Triple(500.0, 750.0, 1000.0), ""),
            Triple("Khanpura", Triple(650.0, 975.0, 1300.0), ""),
            Triple("Taharpur", Triple(500.0, 750.0, 1000.0), ""),
            Triple("Devaras", Triple(450.0, 675.0, 900.0), ""),
            Triple("Pachhad", Triple(600.0, 900.0, 1200.0), ""),
            Triple("Hatsa", Triple(600.0, 900.0, 1200.0), ""),
            Triple("Nagla Ibrahim", Triple(650.0, 975.0, 1300.0), ""),
            Triple("Rahimpur", Triple(700.0, 1050.0, 1400.0), ""),
            Triple("Khandepur", Triple(800.0, 1200.0, 1600.0), ""),
            Triple("Salempur", Triple(650.0, 975.0, 1300.0), ""),
            Triple("Datoniya", Triple(700.0, 1050.0, 1400.0), ""),
            Triple("Shahpura", Triple(650.0, 975.0, 1300.0), "")
        )
    }
    
    suspend fun seedDemoData() {
        val existingDemoStudents = studentDao.countBySrNumberPrefix(DEMO_PREFIX)
        if (existingDemoStudents > 0) return
        
        val sessionId = academicSessionDao.getCurrentSession()?.id ?: return
        val session = academicSessionDao.getById(sessionId) ?: return
        
        seedTransportRoutes()
        seedFeeStructure(sessionId)
        val studentIds = seedStudents(sessionId)
        seedTransportEnrollments(studentIds, sessionId)
        seedFeeDebits(studentIds, sessionId, session.startDate)
        seedComprehensiveReceipts(studentIds, sessionId)
    }
    
    private suspend fun seedTransportRoutes() {
        if (transportRouteDao.getCount() > 0) return
        
        TRANSPORT_ROUTES.forEach { (name, fees, description) ->
            val route = TransportRouteEntity(
                routeName = name,
                monthlyFee = fees.first,
                feeNcTo5 = fees.first,
                fee6To8 = fees.second,
                fee9To12 = fees.third,
                description = description
            )
            transportRouteDao.insert(route)
        }
    }
    
    private suspend fun seedFeeStructure(sessionId: Long) {
        val existingFees = feeStructureDao.getCountForSession(sessionId)
        if (existingFees > 0) return
        
        val feeStructures = mutableListOf<FeeStructureEntity>()
        
        MONTHLY_FEES.forEach { (className, amount) ->
            feeStructures.add(FeeStructureEntity(sessionId = sessionId, className = className, feeType = FeeType.MONTHLY, amount = amount))
        }
        
        ANNUAL_FEES.forEach { (className, amount) ->
            feeStructures.add(FeeStructureEntity(sessionId = sessionId, className = className, feeType = FeeType.ANNUAL, amount = amount))
        }
        
        CLASSES.forEach { className ->
            feeStructures.add(FeeStructureEntity(sessionId = sessionId, className = className, feeType = FeeType.ADMISSION, amount = 1200.0))
        }
        
        listOf("9th", "10th", "11th", "12th").forEach { className ->
            feeStructures.add(FeeStructureEntity(sessionId = sessionId, className = className, feeType = FeeType.REGISTRATION, amount = if (className in listOf("11th", "12th")) 500.0 else 400.0))
        }
        
        feeStructures.forEach { feeStructureDao.insert(it) }
    }
    
    private suspend fun seedStudents(sessionId: Long): List<Long> {
        val calendar = Calendar.getInstance()
        val session = academicSessionDao.getById(sessionId)
        val studentIds = mutableListOf<Long>()
        val transportRoutes = transportRouteDao.getAllRoutesList()
        
        var srCounter = 1
        var acCounter = 1001
        
        // Create 150 students with varied scenarios for comprehensive testing
        repeat(150) { index ->
            val firstName = FIRST_NAMES.random()
            val lastName = LAST_NAMES.random()
            val fatherFirstName = FIRST_NAMES.filter { it != firstName }.random()
            val motherFirstName = FIRST_NAMES.filter { it != firstName && it != fatherFirstName }.random()
            
            val className = CLASSES[index % CLASSES.size]
            
            // Transport probability based on class
            val transportChance = when {
                className in listOf("NC", "LKG", "UKG") -> 0.20f
                className in listOf("1st", "2nd", "3rd", "4th", "5th") -> 0.30f
                className in listOf("6th", "7th", "8th") -> 0.40f
                else -> 0.50f
            }
            val hasTransport = Random.nextFloat() < transportChance
            val transportRoute = if (hasTransport && transportRoutes.isNotEmpty()) transportRoutes.random() else null
            
            // Admission dates - varied for aging analysis
            calendar.timeInMillis = System.currentTimeMillis()
            val yearsAgo = Random.nextInt(0, 5)
            calendar.add(Calendar.YEAR, -yearsAgo)
            calendar.add(Calendar.MONTH, -Random.nextInt(0, 12))
            val admissionDate = calendar.timeInMillis
            
            // Opening balance for older students (creates 90+ days aging)
            val hasOpeningBalance = yearsAgo > 0 && Random.nextFloat() < 0.40
            val openingBalance = if (hasOpeningBalance) (Random.nextInt(1, 10) * 1000).toDouble() else 0.0
            val openingRemarks = if (hasOpeningBalance) listOf(
                "Dues from previous session", "Pending fees 2024-25", "Previous year balance"
            ).random() else ""
            
            val student = StudentEntity(
                srNumber = "$DEMO_PREFIX${String.format("%04d", srCounter++)}",
                accountNumber = "AC${String.format("%05d", acCounter++)}",
                name = "$firstName $lastName",
                fatherName = "$fatherFirstName $lastName",
                motherName = "$motherFirstName Devi",
                phonePrimary = "9${Random.nextInt(100000000, 999999999)}",
                phoneSecondary = if (Random.nextFloat() < 0.3) "9${Random.nextInt(100000000, 999999999)}" else "",
                addressLine1 = "H.No. ${Random.nextInt(1, 500)}, ${LOCALITIES.random()}",
                addressLine2 = VILLAGES.random(),
                district = DISTRICTS.random(),
                state = "Uttar Pradesh",
                pincode = "242${Random.nextInt(1, 999).toString().padStart(3, '0')}",
                currentClass = className,
                section = if (index % 5 == 0) "B" else "A",
                admissionDate = admissionDate,
                admissionSessionId = sessionId,
                hasTransport = hasTransport,
                transportRouteId = transportRoute?.id,
                openingBalance = openingBalance,
                openingBalanceRemarks = openingRemarks,
                openingBalanceDate = session?.startDate ?: System.currentTimeMillis(),
                admissionFeePaid = yearsAgo > 0,
                isActive = true
            )
            
            studentIds.add(studentDao.insert(student))
        }
        
        return studentIds
    }
    
    private suspend fun seedTransportEnrollments(studentIds: List<Long>, sessionId: Long) {
        val calendar = Calendar.getInstance()
        val session = academicSessionDao.getById(sessionId) ?: return
        
        studentIds.forEach { studentId ->
            val student = studentDao.getById(studentId) ?: return@forEach
            
            if (student.hasTransport && student.transportRouteId != null) {
                val route = transportRouteDao.getById(student.transportRouteId) ?: return@forEach
                
                calendar.timeInMillis = session.startDate
                calendar.add(Calendar.MONTH, Random.nextInt(0, 2))
                val startDate = calendar.timeInMillis
                
                val hasEnded = Random.nextFloat() < 0.15
                val endDate = if (hasEnded) {
                    calendar.add(Calendar.MONTH, Random.nextInt(3, 6))
                    calendar.timeInMillis
                } else null
                
                transportEnrollmentDao.insert(TransportEnrollmentEntity(
                    studentId = studentId,
                    routeId = student.transportRouteId,
                    startDate = startDate,
                    endDate = endDate,
                    monthlyFeeAtEnrollment = route.getFeeForClass(student.currentClass)
                ))
            }
        }
    }
    
    private suspend fun seedFeeDebits(studentIds: List<Long>, sessionId: Long, sessionStartDate: Long) {
        val session = academicSessionDao.getById(sessionId)
        val sessionName = session?.sessionName ?: "2025-26"
        
        studentIds.forEach { studentId ->
            val student = studentDao.getById(studentId) ?: return@forEach
            var runningBalance = 0.0
            
            // Opening balance creates old dues for aging report
            if (student.openingBalance > 0) {
                runningBalance += student.openingBalance
                ledgerDao.insert(LedgerEntryEntity(
                    studentId = studentId,
                    sessionId = sessionId,
                    entryDate = sessionStartDate,
                    particulars = "Opening Balance - ${student.openingBalanceRemarks}",
                    entryType = LedgerEntryType.DEBIT,
                    debitAmount = student.openingBalance,
                    creditAmount = 0.0,
                    balance = runningBalance,
                    referenceType = LedgerReferenceType.OPENING_BALANCE,
                    referenceId = 0
                ))
            }
            
            // Monthly fee classes - Single session entry for 12 months
            if (student.currentClass in MONTHLY_FEES.keys) {
                val monthlyFee = MONTHLY_FEES[student.currentClass] ?: 500.0
                val totalSessionFee = monthlyFee * 12  // Full 12 months
                
                runningBalance += totalSessionFee
                ledgerDao.insert(LedgerEntryEntity(
                    studentId = studentId,
                    sessionId = sessionId,
                    entryDate = sessionStartDate,
                    particulars = "Tuition Fee - Session $sessionName (12 months @ ₹${monthlyFee.toInt()}/month)",
                    entryType = LedgerEntryType.DEBIT,
                    debitAmount = totalSessionFee,
                    creditAmount = 0.0,
                    balance = runningBalance,
                    referenceType = LedgerReferenceType.FEE_CHARGE,
                    referenceId = 0
                ))
            }
            
            // Annual fee classes - Single session entry
            if (student.currentClass in ANNUAL_FEES.keys) {
                val annualFee = ANNUAL_FEES[student.currentClass] ?: 9000.0
                
                runningBalance += annualFee
                ledgerDao.insert(LedgerEntryEntity(
                    studentId = studentId,
                    sessionId = sessionId,
                    entryDate = sessionStartDate,
                    particulars = "Annual Fee - Session $sessionName",
                    entryType = LedgerEntryType.DEBIT,
                    debitAmount = annualFee,
                    creditAmount = 0.0,
                    balance = runningBalance,
                    referenceType = LedgerReferenceType.FEE_CHARGE,
                    referenceId = 0
                ))
            }
            
            // Transport fees - Single session entry for 11 months (June excluded)
            if (student.hasTransport && student.transportRouteId != null) {
                val route = transportRouteDao.getById(student.transportRouteId) ?: return@forEach
                val monthlyTransportFee = route.getFeeForClass(student.currentClass)
                
                // Check if student left transport mid-session
                val enrollment = transportEnrollmentDao.getActiveEnrollment(studentId)
                val transportMonths = if (enrollment?.endDate != null) {
                    // Student left transport - calculate months from start to end (excluding June)
                    calculateTransportMonths(sessionStartDate, enrollment.endDate)
                } else {
                    11  // Full session - 11 months (June excluded)
                }
                
                val totalTransportFee = monthlyTransportFee * transportMonths
                runningBalance += totalTransportFee
                
                val transportDescription = if (enrollment?.endDate != null) {
                    "Transport Fee - Session $sessionName ($transportMonths months, ${route.routeName})"
                } else {
                    "Transport Fee - Session $sessionName (11 months excl. June, ${route.routeName})"
                }
                
                ledgerDao.insert(LedgerEntryEntity(
                    studentId = studentId,
                    sessionId = sessionId,
                    entryDate = sessionStartDate,
                    particulars = transportDescription,
                    entryType = LedgerEntryType.DEBIT,
                    debitAmount = totalTransportFee,
                    creditAmount = 0.0,
                    balance = runningBalance,
                    referenceType = LedgerReferenceType.FEE_CHARGE,
                    referenceId = 0
                ))
            }
        }
    }
    
    /**
     * Calculate transport months between two dates, excluding June
     */
    private fun calculateTransportMonths(startDate: Long, endDate: Long): Int {
        val startCal = Calendar.getInstance().apply { timeInMillis = startDate }
        val endCal = Calendar.getInstance().apply { timeInMillis = endDate }
        
        var months = 0
        val tempCal = startCal.clone() as Calendar
        
        while (tempCal.before(endCal) || 
               (tempCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) && 
                tempCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH))) {
            // Exclude June (month index 5)
            if (tempCal.get(Calendar.MONTH) != Calendar.JUNE) {
                months++
            }
            tempCal.add(Calendar.MONTH, 1)
        }
        
        return months
    }
    
    /**
     * Creates comprehensive receipts with:
     * - TODAY's receipts for Daily Collection report
     * - Recent week receipts 
     * - Current month receipts for Monthly Collection
     * - Varied payment modes for Custom Reports
     * - Different amounts for variety
     * - Some fully paid students, some partial, some defaulters
     * 
     * IMPORTANT: Receipts are created in CHRONOLOGICAL ORDER (oldest first)
     * to ensure proper running balance calculation in ledger.
     */
    private suspend fun seedComprehensiveReceipts(studentIds: List<Long>, sessionId: Long) {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        
        // Start from next available receipt number to avoid unique constraint violation
        var receiptNumber = (receiptDao.getMaxReceiptNumber() ?: 0) + 1
        
        // Shuffle and categorize students
        val shuffled = studentIds.shuffled()
        
        // 20% fully paid (multiple receipts)
        val fullyPaid = shuffled.take((shuffled.size * 0.20).toInt())
        
        // 40% partially paid (2-4 receipts)
        val partiallyPaid = shuffled.drop((shuffled.size * 0.20).toInt()).take((shuffled.size * 0.40).toInt())
        
        // 25% minimal payment (1 receipt)
        val minimalPaid = shuffled.drop((shuffled.size * 0.60).toInt()).take((shuffled.size * 0.25).toInt())
        
        // 15% defaulters (no payment) - these won't get receipts
        
        // Collect all planned receipts with dates, then sort and create in chronological order
        data class PlannedReceipt(val studentId: Long, val date: Long, val isRecent: Boolean)
        val plannedReceipts = mutableListOf<PlannedReceipt>()
        
        // === PLAN HISTORICAL RECEIPTS (6-3 months ago) ===
        fullyPaid.forEach { studentId ->
            repeat(Random.nextInt(3, 6)) { i ->
                calendar.timeInMillis = today.timeInMillis
                calendar.add(Calendar.MONTH, -(6 - i))
                calendar.add(Calendar.DAY_OF_MONTH, -Random.nextInt(0, 25))
                plannedReceipts.add(PlannedReceipt(studentId, calendar.timeInMillis, false))
            }
        }
        
        partiallyPaid.forEach { studentId ->
            repeat(Random.nextInt(1, 4)) { i ->
                calendar.timeInMillis = today.timeInMillis
                calendar.add(Calendar.MONTH, -(4 - i))
                calendar.add(Calendar.DAY_OF_MONTH, -Random.nextInt(0, 25))
                plannedReceipts.add(PlannedReceipt(studentId, calendar.timeInMillis, false))
            }
        }
        
        minimalPaid.forEach { studentId ->
            calendar.timeInMillis = today.timeInMillis
            calendar.add(Calendar.MONTH, -Random.nextInt(2, 5))
            calendar.add(Calendar.DAY_OF_MONTH, -Random.nextInt(0, 25))
            plannedReceipts.add(PlannedReceipt(studentId, calendar.timeInMillis, false))
        }
        
        // === PLAN CURRENT MONTH RECEIPTS (28-7 days ago) ===
        for (daysAgo in 28 downTo 7) {
            if (Random.nextFloat() < 0.6) {
                calendar.timeInMillis = today.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
                val dayDate = calendar.timeInMillis
                
                (fullyPaid + partiallyPaid + minimalPaid).shuffled().take(Random.nextInt(2, 6)).forEach { studentId ->
                    plannedReceipts.add(PlannedReceipt(studentId, dayDate, false))
                }
            }
        }
        
        // === PLAN THIS WEEK'S RECEIPTS (6-2 days ago) ===
        for (daysAgo in 6 downTo 2) {
            calendar.timeInMillis = today.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dayDate = calendar.timeInMillis
            
            (fullyPaid + partiallyPaid).shuffled().take(Random.nextInt(4, 8)).forEach { studentId ->
                plannedReceipts.add(PlannedReceipt(studentId, dayDate, true))
            }
        }
        
        // === PLAN YESTERDAY'S RECEIPTS ===
        calendar.timeInMillis = today.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.timeInMillis
        (fullyPaid + partiallyPaid).shuffled().take(Random.nextInt(6, 10)).forEach { studentId ->
            plannedReceipts.add(PlannedReceipt(studentId, yesterday, true))
        }
        
        // === PLAN TODAY'S RECEIPTS ===
        (fullyPaid + partiallyPaid).shuffled().take(Random.nextInt(8, 13)).forEach { studentId ->
            plannedReceipts.add(PlannedReceipt(studentId, today.timeInMillis, true))
        }
        
        // === SORT BY DATE (oldest first) AND CREATE RECEIPTS ===
        val sortedReceipts = plannedReceipts.sortedBy { it.date }
        
        sortedReceipts.forEach { planned ->
            receiptNumber = createReceipt(
                studentId = planned.studentId,
                sessionId = sessionId,
                receiptNumber = receiptNumber,
                receiptDate = planned.date,
                isRecent = planned.isRecent
            )
        }
    }
    
    private suspend fun createReceipt(
        studentId: Long,
        sessionId: Long,
        receiptNumber: Int,
        receiptDate: Long,
        isRecent: Boolean
    ): Int {
        return try {
            val student = studentDao.getById(studentId) ?: return receiptNumber + 1
            
            // Determine amount based on class
            val (amount, description) = when {
                student.currentClass in MONTHLY_FEES.keys -> {
                    val monthlyFee = MONTHLY_FEES[student.currentClass] ?: 500.0
                    val months = if (isRecent) Random.nextInt(1, 4) else Random.nextInt(1, 6)
                    val monthlyTotal = monthlyFee * months
                    
                    val transportFee = if (student.hasTransport && student.transportRouteId != null) {
                        val route = transportRouteDao.getById(student.transportRouteId)
                        (route?.getFeeForClass(student.currentClass) ?: 0.0) * months
                    } else 0.0
                    
                    val total = monthlyTotal + transportFee
                    val desc = if (transportFee > 0) "Fee + Transport ($months months)" else "Monthly Fee ($months months)"
                    Pair(total, desc)
                }
                student.currentClass in ANNUAL_FEES.keys -> {
                    val fullAmount = ANNUAL_FEES[student.currentClass] ?: 9000.0
                    when {
                        Random.nextFloat() < 0.4 -> Pair(fullAmount, "Annual Fee (Full)")
                        Random.nextFloat() < 0.6 -> Pair(fullAmount / 2, "Annual Fee (Half)")
                        else -> Pair(fullAmount / 3, "Annual Fee (Partial)")
                    }
                }
                else -> Pair(2000.0, "Fee Payment")
            }
            
            // Round amounts sometimes
            val finalAmount = if (Random.nextFloat() < 0.25) {
                ((amount / 500).toInt() * 500).toDouble().coerceAtLeast(500.0)
            } else amount
            
            // Payment modes: Cash or Online
            val paymentMode = when (Random.nextInt(100)) {
                in 0..64 -> PaymentMode.CASH        // 65% cash
                else -> PaymentMode.ONLINE          // 35% online
            }
            
            val receipt = ReceiptEntity(
                receiptNumber = receiptNumber,
                studentId = studentId,
                sessionId = sessionId,
                receiptDate = receiptDate,
                totalAmount = finalAmount,
                discountAmount = 0.0,
                netAmount = finalAmount,
                paymentMode = paymentMode,
                onlineReference = if (paymentMode == PaymentMode.ONLINE) "TXN${Random.nextInt(100000000, 999999999)}" else null
            )
            
            val receiptId = receiptDao.insertReceipt(receipt)
            
            receiptDao.insertReceiptItem(ReceiptItemEntity(
                receiptId = receiptId,
                feeType = "FEE_PAYMENT",
                description = description,
                amount = finalAmount
            ))
            
            // Calculate proper running balance after credit
            val currentBalance = ledgerDao.getCurrentBalance(studentId)
            val newBalance = currentBalance - finalAmount
            
            ledgerDao.insert(LedgerEntryEntity(
                studentId = studentId,
                sessionId = sessionId,
                entryDate = receiptDate,
                particulars = "Receipt #$receiptNumber - $description",
                entryType = LedgerEntryType.CREDIT,
                debitAmount = 0.0,
                creditAmount = finalAmount,
                balance = newBalance,
                referenceType = LedgerReferenceType.RECEIPT,
                referenceId = receiptId
            ))
            
            receiptNumber + 1
        } catch (e: Exception) {
            // If receipt creation fails (e.g., duplicate number), skip and continue
            receiptNumber + 1
        }
    }
    
    suspend fun clearDemoData() {
        val demoStudentIds = studentDao.getStudentIdsBySrNumberPrefix(DEMO_PREFIX)
        
        demoStudentIds.forEach { studentId ->
            transportEnrollmentDao.deleteAllForStudent(studentId)
            ledgerDao.deleteByStudentId(studentId)
            val receipts = receiptDao.getReceiptsForStudentList(studentId)
            receipts.forEach { receipt ->
                receiptDao.deleteReceiptItems(receipt.id)
                receiptDao.deleteReceipt(receipt.id)
            }
        }
        
        studentDao.deleteBySrNumberPrefix(DEMO_PREFIX)
    }
    
    suspend fun hasDemoData(): Boolean {
        return studentDao.countBySrNumberPrefix(DEMO_PREFIX) > 0
    }
}
