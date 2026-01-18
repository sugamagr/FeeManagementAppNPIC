package com.navoditpublic.fees.data.local

import com.navoditpublic.fees.data.local.dao.AcademicSessionDao
import com.navoditpublic.fees.data.local.dao.ClassSectionDao
import com.navoditpublic.fees.data.local.dao.SchoolSettingsDao
import com.navoditpublic.fees.data.local.entity.AcademicSessionEntity
import com.navoditpublic.fees.data.local.entity.ClassSectionEntity
import com.navoditpublic.fees.data.local.entity.SchoolSettingsEntity
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds initial data into the database when the app is first installed.
 */
@Singleton
class DataSeeder @Inject constructor(
    private val classSectionDao: ClassSectionDao,
    private val academicSessionDao: AcademicSessionDao,
    private val schoolSettingsDao: SchoolSettingsDao
) {
    
    suspend fun seedInitialData() {
        seedClassesAndSections()
        seedCurrentSession()
        seedSchoolSettings()
    }
    
    private suspend fun seedClassesAndSections() {
        // Check if already seeded
        if (classSectionDao.getCount() > 0) return
        
        val classes = listOf(
            "NC" to 1,
            "LKG" to 2,
            "UKG" to 3,
            "1st" to 4,
            "2nd" to 5,
            "3rd" to 6,
            "4th" to 7,
            "5th" to 8,
            "6th" to 9,
            "7th" to 10,
            "8th" to 11,
            "9th" to 12,
            "10th" to 13,
            "11th" to 14,
            "12th" to 15
        )
        
        // Default to single section "A" per class
        val classSections = classes.map { (className, order) ->
            ClassSectionEntity(
                className = className,
                sectionName = "A",
                displayOrder = order
            )
        }
        
        classSectionDao.insertAll(classSections)
    }
    
    private suspend fun seedCurrentSession() {
        // Check if already seeded
        val existingSession = academicSessionDao.getCurrentSession()
        if (existingSession != null) return
        
        // Create current academic session (April to March)
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        
        // If before April, session is previous year to current year
        // If April or later, session is current year to next year
        val (startYear, endYear) = if (currentMonth < Calendar.APRIL) {
            currentYear - 1 to currentYear
        } else {
            currentYear to currentYear + 1
        }
        
        // Start date: April 1
        val startCal = Calendar.getInstance().apply {
            set(startYear, Calendar.APRIL, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // End date: March 31
        val endCal = Calendar.getInstance().apply {
            set(endYear, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        
        val sessionName = "${startYear}-${endYear.toString().takeLast(2)}"
        
        val session = AcademicSessionEntity(
            sessionName = sessionName,
            startDate = startCal.timeInMillis,
            endDate = endCal.timeInMillis,
            isCurrent = true,
            isActive = true
        )
        
        academicSessionDao.insert(session)
    }
    
    private suspend fun seedSchoolSettings() {
        // Check if already seeded
        val existingSettings = schoolSettingsDao.getSettings()
        if (existingSettings != null) return
        
        val settings = SchoolSettingsEntity(
            id = 1,
            schoolName = "Navodit Public Inter College",
            tagline = "Approved By the Government",
            addressLine1 = "Myuna Khudaganj",
            addressLine2 = "Shahjahanpur",
            district = "Shahjahanpur",
            state = "Uttar Pradesh"
        )
        
        schoolSettingsDao.insert(settings)
    }
}


