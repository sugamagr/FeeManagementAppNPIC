package com.navoditpublic.fees.domain.model

import com.navoditpublic.fees.data.local.entity.LedgerEntryEntity
import com.navoditpublic.fees.data.local.entity.LedgerEntryType
import com.navoditpublic.fees.data.local.entity.LedgerReferenceType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LedgerEntry(
    val id: Long = 0,
    val studentId: Long,
    val sessionId: Long,
    val entryDate: Long,
    val particulars: String,
    val entryType: LedgerEntryType,
    val debitAmount: Double = 0.0,
    val creditAmount: Double = 0.0,
    val balance: Double,
    val referenceType: LedgerReferenceType,
    val referenceId: Long? = null,
    val folioNumber: String? = null,
    val isReversed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val entryDateFormatted: String
        get() = SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(Date(entryDate))
    
    val isDebit: Boolean get() = entryType == LedgerEntryType.DEBIT
    val isCredit: Boolean get() = entryType == LedgerEntryType.CREDIT
    
    val balanceStatus: String
        get() = when {
            balance > 0 -> "Due"
            balance < 0 -> "Advance"
            else -> "Cleared"
        }
    
    fun toEntity(): LedgerEntryEntity = LedgerEntryEntity(
        id = id,
        studentId = studentId,
        sessionId = sessionId,
        entryDate = entryDate,
        particulars = particulars,
        entryType = entryType,
        debitAmount = debitAmount,
        creditAmount = creditAmount,
        balance = balance,
        referenceType = referenceType,
        referenceId = referenceId,
        folioNumber = folioNumber,
        isReversed = isReversed,
        createdAt = createdAt
    )
    
    companion object {
        fun fromEntity(entity: LedgerEntryEntity): LedgerEntry = LedgerEntry(
            id = entity.id,
            studentId = entity.studentId,
            sessionId = entity.sessionId,
            entryDate = entity.entryDate,
            particulars = entity.particulars,
            entryType = entity.entryType,
            debitAmount = entity.debitAmount,
            creditAmount = entity.creditAmount,
            balance = entity.balance,
            referenceType = entity.referenceType,
            referenceId = entity.referenceId,
            folioNumber = entity.folioNumber,
            isReversed = entity.isReversed,
            createdAt = entity.createdAt
        )
    }
}


