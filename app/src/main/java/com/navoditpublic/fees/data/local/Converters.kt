package com.navoditpublic.fees.data.local

import androidx.room.TypeConverter
import com.navoditpublic.fees.data.local.entity.AuditAction
import com.navoditpublic.fees.data.local.entity.FeeType
import com.navoditpublic.fees.data.local.entity.LedgerEntryType
import com.navoditpublic.fees.data.local.entity.LedgerReferenceType
import com.navoditpublic.fees.data.local.entity.PaymentMode

class Converters {
    
    // FeeType converters
    @TypeConverter
    fun fromFeeType(value: FeeType): String = value.name
    
    @TypeConverter
    fun toFeeType(value: String): FeeType = try {
        FeeType.valueOf(value)
    } catch (e: IllegalArgumentException) {
        FeeType.MONTHLY // Default fallback
    }
    
    // PaymentMode converters
    @TypeConverter
    fun fromPaymentMode(value: PaymentMode): String = value.name
    
    @TypeConverter
    fun toPaymentMode(value: String): PaymentMode = try {
        PaymentMode.valueOf(value)
    } catch (e: IllegalArgumentException) {
        PaymentMode.CASH // Default fallback
    }
    
    // LedgerEntryType converters
    @TypeConverter
    fun fromLedgerEntryType(value: LedgerEntryType): String = value.name
    
    @TypeConverter
    fun toLedgerEntryType(value: String): LedgerEntryType = try {
        LedgerEntryType.valueOf(value)
    } catch (e: IllegalArgumentException) {
        LedgerEntryType.DEBIT // Default fallback
    }
    
    // LedgerReferenceType converters
    @TypeConverter
    fun fromLedgerReferenceType(value: LedgerReferenceType): String = value.name
    
    @TypeConverter
    fun toLedgerReferenceType(value: String): LedgerReferenceType = try {
        LedgerReferenceType.valueOf(value)
    } catch (e: IllegalArgumentException) {
        LedgerReferenceType.ADJUSTMENT // Default fallback
    }
    
    // AuditAction converters
    @TypeConverter
    fun fromAuditAction(value: AuditAction): String = value.name
    
    @TypeConverter
    fun toAuditAction(value: String): AuditAction = try {
        AuditAction.valueOf(value)
    } catch (e: IllegalArgumentException) {
        AuditAction.UPDATE // Default fallback
    }
}


