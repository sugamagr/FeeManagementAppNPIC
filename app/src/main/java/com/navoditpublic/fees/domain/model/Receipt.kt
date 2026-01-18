package com.navoditpublic.fees.domain.model

import com.navoditpublic.fees.data.local.entity.PaymentMode
import com.navoditpublic.fees.data.local.entity.ReceiptEntity
import com.navoditpublic.fees.data.local.entity.ReceiptItemEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Receipt(
    val id: Long = 0,
    val receiptNumber: Int,
    val studentId: Long,
    val sessionId: Long,
    val receiptDate: Long,
    val totalAmount: Double,
    val discountAmount: Double = 0.0,
    val netAmount: Double,
    val paymentMode: PaymentMode,
    val onlineReference: String? = null,
    val remarks: String? = "",
    val isCancelled: Boolean = false,
    val cancelledAt: Long? = null,
    val cancellationReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val items: List<ReceiptItem> = emptyList(),
    // Student details (populated when fetching with join)
    val studentName: String? = null,
    val studentClass: String? = null,
    val studentSection: String? = null
) {
    val receiptDateFormatted: String
        get() = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(receiptDate))
    
    val paymentReference: String?
        get() = when (paymentMode) {
            PaymentMode.CASH -> null
            PaymentMode.ONLINE -> onlineReference
        }
    
    val referenceNumber: String?
        get() = paymentReference
    
    fun toEntity(): ReceiptEntity = ReceiptEntity(
        id = id,
        receiptNumber = receiptNumber,
        studentId = studentId,
        sessionId = sessionId,
        receiptDate = receiptDate,
        totalAmount = totalAmount,
        discountAmount = discountAmount,
        netAmount = netAmount,
        paymentMode = paymentMode,
        onlineReference = onlineReference,
        remarks = remarks ?: "",
        isCancelled = isCancelled,
        cancelledAt = cancelledAt,
        cancellationReason = cancellationReason,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    companion object {
        fun fromEntity(
            entity: ReceiptEntity, 
            items: List<ReceiptItem> = emptyList(),
            studentName: String? = null,
            studentClass: String? = null,
            studentSection: String? = null
        ): Receipt = Receipt(
            id = entity.id,
            receiptNumber = entity.receiptNumber,
            studentId = entity.studentId,
            sessionId = entity.sessionId,
            receiptDate = entity.receiptDate,
            totalAmount = entity.totalAmount,
            discountAmount = entity.discountAmount,
            netAmount = entity.netAmount,
            paymentMode = entity.paymentMode,
            onlineReference = entity.onlineReference,
            remarks = entity.remarks,
            isCancelled = entity.isCancelled,
            cancelledAt = entity.cancelledAt,
            cancellationReason = entity.cancellationReason,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            items = items,
            studentName = studentName,
            studentClass = studentClass,
            studentSection = studentSection
        )
    }
}

data class ReceiptItem(
    val id: Long = 0,
    val receiptId: Long = 0,
    val feeType: String,
    val description: String,
    val monthYear: String? = null,
    val amount: Double,
    val createdAt: Long = System.currentTimeMillis()
) {
    val months: String
        get() = monthYear ?: ""
    
    fun toEntity(): ReceiptItemEntity = ReceiptItemEntity(
        id = id,
        receiptId = receiptId,
        feeType = feeType,
        description = description,
        monthYear = monthYear,
        amount = amount,
        createdAt = createdAt
    )
    
    companion object {
        fun fromEntity(entity: ReceiptItemEntity): ReceiptItem = ReceiptItem(
            id = entity.id,
            receiptId = entity.receiptId,
            feeType = entity.feeType,
            description = entity.description,
            monthYear = entity.monthYear,
            amount = entity.amount,
            createdAt = entity.createdAt
        )
    }
}

/**
 * Receipt with student details for display
 */
data class ReceiptWithStudent(
    val receipt: Receipt,
    val studentName: String,
    val studentClass: String,
    val studentSection: String = "",
    val studentSrNumber: String = "",
    val studentAccountNumber: String = "",
    val fatherName: String = ""
)

/**
 * Extended receipt item with display properties
 */
data class ReceiptItemDisplay(
    val description: String,
    val months: String,
    val amount: Double
) {
    companion object {
        fun fromReceiptItem(item: ReceiptItem): ReceiptItemDisplay = ReceiptItemDisplay(
            description = item.description,
            months = item.monthYear ?: "",
            amount = item.amount
        )
    }
}


