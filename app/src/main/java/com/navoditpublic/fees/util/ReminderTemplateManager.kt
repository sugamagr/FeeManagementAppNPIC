package com.navoditpublic.fees.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ReminderTemplate(
    val id: Int,
    val name: String,
    val hindiMessage: String,
    val englishMessage: String,
    val isDefault: Boolean = false
)

@Singleton
class ReminderTemplateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "reminder_templates"
        private const val KEY_TEMPLATES = "templates"
        private const val MAX_CUSTOM_TEMPLATES = 2
        
        // Default template content
        private const val DEFAULT_HINDI = """नमस्कार,

यह संदेश {student_name} (कक्षा: {class}) के अभिभावक को भेजा जा रहा है।

हमारे रिकॉर्ड के अनुसार, आपके बच्चे की बकाया फीस {amount} है।

कृपया जल्द से जल्द भुगतान करें।

यदि आपने पहले ही भुगतान कर दिया है, तो कृपया इस संदेश को अनदेखा करें।

धन्यवाद।"""
        
        private const val DEFAULT_ENGLISH = """Hello,

This message is for the parent/guardian of {student_name} (Class: {class}).

As per our records, the pending fee amount is {amount}.

Please clear the dues at your earliest convenience.

If you have already paid, please ignore this message.

Thank you."""
    }
    
    init {
        // Ensure default template exists
        ensureDefaultTemplate()
    }
    
    private fun ensureDefaultTemplate() {
        val templates = getTemplates()
        if (templates.none { it.isDefault }) {
            val defaultTemplate = ReminderTemplate(
                id = 0,
                name = "Default Template",
                hindiMessage = DEFAULT_HINDI,
                englishMessage = DEFAULT_ENGLISH,
                isDefault = true
            )
            saveTemplates(listOf(defaultTemplate))
        }
    }
    
    fun getTemplates(): List<ReminderTemplate> {
        val json = prefs.getString(KEY_TEMPLATES, null) ?: return listOf(
            ReminderTemplate(
                id = 0,
                name = "Default Template",
                hindiMessage = DEFAULT_HINDI,
                englishMessage = DEFAULT_ENGLISH,
                isDefault = true
            )
        )
        
        val type = object : TypeToken<List<ReminderTemplate>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            listOf(
                ReminderTemplate(
                    id = 0,
                    name = "Default Template",
                    hindiMessage = DEFAULT_HINDI,
                    englishMessage = DEFAULT_ENGLISH,
                    isDefault = true
                )
            )
        }
    }
    
    fun getDefaultTemplate(): ReminderTemplate {
        return getTemplates().first { it.isDefault }
    }
    
    fun canAddMoreTemplates(): Boolean {
        val customTemplates = getTemplates().count { !it.isDefault }
        return customTemplates < MAX_CUSTOM_TEMPLATES
    }
    
    fun addTemplate(name: String, hindiMessage: String, englishMessage: String): Boolean {
        if (!canAddMoreTemplates()) return false
        
        val templates = getTemplates().toMutableList()
        val newId = (templates.maxOfOrNull { it.id } ?: 0) + 1
        
        templates.add(
            ReminderTemplate(
                id = newId,
                name = name,
                hindiMessage = hindiMessage,
                englishMessage = englishMessage,
                isDefault = false
            )
        )
        
        saveTemplates(templates)
        return true
    }
    
    fun updateTemplate(template: ReminderTemplate): Boolean {
        val templates = getTemplates().toMutableList()
        val index = templates.indexOfFirst { it.id == template.id }
        
        if (index == -1) return false
        
        // Don't allow changing default flag
        val updatedTemplate = if (templates[index].isDefault) {
            template.copy(isDefault = true)
        } else {
            template.copy(isDefault = false)
        }
        
        templates[index] = updatedTemplate
        saveTemplates(templates)
        return true
    }
    
    fun deleteTemplate(templateId: Int): Boolean {
        val templates = getTemplates().toMutableList()
        val template = templates.find { it.id == templateId }
        
        // Can't delete default template
        if (template?.isDefault == true) return false
        
        templates.removeIf { it.id == templateId }
        saveTemplates(templates)
        return true
    }
    
    private fun saveTemplates(templates: List<ReminderTemplate>) {
        val json = gson.toJson(templates)
        prefs.edit().putString(KEY_TEMPLATES, json).apply()
    }
    
    /**
     * Build the final message with placeholders replaced
     */
    fun buildMessage(
        template: ReminderTemplate,
        studentName: String,
        className: String,
        amount: String
    ): String {
        val hindi = template.hindiMessage
            .replace("{student_name}", studentName)
            .replace("{class}", className)
            .replace("{amount}", amount)
        
        val english = template.englishMessage
            .replace("{student_name}", studentName)
            .replace("{class}", className)
            .replace("{amount}", amount)
        
        return "$hindi\n\n---\n\n$english"
    }
}
