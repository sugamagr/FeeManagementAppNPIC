package com.navoditpublic.fees.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.studentDraftDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "student_draft"
)

/**
 * Data class representing a student draft.
 * Contains all form field values that can be restored.
 */
data class StudentDraft(
    // Migration mode affects default values
    val isMigrationMode: Boolean = false,
    
    val srNumber: String = "",
    val accountNumber: String = "",
    val name: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val phonePrimary: String = "",
    val phoneSecondary: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val district: String = "",
    val state: String = "Uttar Pradesh",
    val pincode: String = "",
    val currentClass: String = "",
    val section: String = "A",
    val admissionDate: Long = System.currentTimeMillis(),
    val hasTransport: Boolean = false,
    val transportRouteId: Long? = null,
    
    // Opening balance and migration-related
    val openingBalance: String = "",
    val openingBalanceRemarks: String = "",
    val currentYearPaidAmount: String = "", // Amount already paid this year (for migration)
    
    // Admission fee - new logic
    // isNewAdmission = true  → Apply admission fee (new student this year)
    // isNewAdmission = false → Skip admission fee (continuing student)
    val isNewAdmission: Boolean = true,
    
    val feesReceivedMode: String = "custom",
    val feesReceivedAmount: String = "",
    val monthsPaid: Set<Int> = emptySet(),
    val lastModified: Long = System.currentTimeMillis()
) {
    /**
     * Check if draft has meaningful content worth restoring
     */
    fun hasContent(): Boolean {
        return name.isNotBlank() || 
               srNumber.isNotBlank() || 
               accountNumber.isNotBlank() ||
               fatherName.isNotBlank() ||
               phonePrimary.isNotBlank()
    }
    
    /**
     * Get a display summary for the resume dialog
     */
    fun getDisplaySummary(): String {
        val parts = mutableListOf<String>()
        if (name.isNotBlank()) parts.add(name)
        if (currentClass.isNotBlank()) parts.add("Class $currentClass")
        if (isMigrationMode) parts.add("(Migration)")
        return parts.joinToString(" - ").ifBlank { "Incomplete entry" }
    }
}

/**
 * Manages student draft storage using DataStore.
 * Only stores ONE draft at a time.
 * Draft is completely isolated from Room database - no impact on actual data.
 */
@Singleton
class StudentDraftManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    
    companion object {
        private val DRAFT_KEY = stringPreferencesKey("student_draft_json")
    }
    
    /**
     * Save the current form state as a draft.
     * Overwrites any existing draft.
     */
    suspend fun saveDraft(draft: StudentDraft) {
        val draftWithTimestamp = draft.copy(lastModified = System.currentTimeMillis())
        val json = gson.toJson(draftWithTimestamp)
        context.studentDraftDataStore.edit { preferences ->
            preferences[DRAFT_KEY] = json
        }
    }
    
    /**
     * Get the existing draft, if any.
     * Returns null if no draft exists or draft has no meaningful content.
     */
    suspend fun getDraft(): StudentDraft? {
        return context.studentDraftDataStore.data.map { preferences ->
            val json = preferences[DRAFT_KEY]
            if (json.isNullOrBlank()) {
                null
            } else {
                try {
                    val draft = gson.fromJson(json, StudentDraft::class.java)
                    if (draft.hasContent()) draft else null
                } catch (e: Exception) {
                    null
                }
            }
        }.first()
    }
    
    /**
     * Check if a draft exists with meaningful content.
     */
    suspend fun hasDraft(): Boolean {
        return getDraft() != null
    }
    
    /**
     * Clear the stored draft.
     * Called after successful save or when user discards.
     */
    suspend fun clearDraft() {
        context.studentDraftDataStore.edit { preferences ->
            preferences.remove(DRAFT_KEY)
        }
    }
}
