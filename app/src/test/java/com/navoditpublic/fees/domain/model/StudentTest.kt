package com.navoditpublic.fees.domain.model

import com.google.common.truth.Truth.assertThat
import com.navoditpublic.fees.data.local.entity.StudentEntity
import org.junit.Test

/**
 * Unit tests for Student domain model.
 */
class StudentTest {

    private fun createStudent(
        id: Long = 1L,
        srNumber: String = "SR001",
        accountNumber: String = "ACC001",
        name: String = "Test Student",
        fatherName: String = "Test Father",
        motherName: String = "Test Mother",
        phonePrimary: String = "9876543210",
        phoneSecondary: String = "9876543211",
        addressLine1: String = "123 Main Street",
        addressLine2: String = "Near Park",
        district: String = "Test District",
        state: String = "Uttar Pradesh",
        pincode: String = "226001",
        currentClass: String = "5th",
        section: String = "A",
        admissionDate: Long = System.currentTimeMillis(),
        admissionSessionId: Long = 1L,
        hasTransport: Boolean = false,
        transportRouteId: Long? = null,
        openingBalance: Double = 0.0,
        openingBalanceRemarks: String = "",
        admissionFeePaid: Boolean = false,
        isActive: Boolean = true
    ) = Student(
        id = id,
        srNumber = srNumber,
        accountNumber = accountNumber,
        name = name,
        fatherName = fatherName,
        motherName = motherName,
        phonePrimary = phonePrimary,
        phoneSecondary = phoneSecondary,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        district = district,
        state = state,
        pincode = pincode,
        currentClass = currentClass,
        section = section,
        admissionDate = admissionDate,
        admissionSessionId = admissionSessionId,
        hasTransport = hasTransport,
        transportRouteId = transportRouteId,
        openingBalance = openingBalance,
        openingBalanceRemarks = openingBalanceRemarks,
        admissionFeePaid = admissionFeePaid,
        isActive = isActive
    )

    // ===== Full Address Tests =====

    @Test
    fun `fullAddress returns complete address with all fields`() {
        val student = createStudent(
            addressLine1 = "123 Main Street",
            addressLine2 = "Near Park",
            district = "Lucknow",
            state = "Uttar Pradesh",
            pincode = "226001"
        )
        
        assertThat(student.fullAddress).isEqualTo("123 Main Street, Near Park, Lucknow, Uttar Pradesh - 226001")
    }

    @Test
    fun `fullAddress handles missing addressLine2`() {
        val student = createStudent(
            addressLine1 = "123 Main Street",
            addressLine2 = "",
            district = "Lucknow",
            state = "Uttar Pradesh",
            pincode = "226001"
        )
        
        assertThat(student.fullAddress).isEqualTo("123 Main Street, Lucknow, Uttar Pradesh - 226001")
    }

    @Test
    fun `fullAddress handles missing district`() {
        val student = createStudent(
            addressLine1 = "123 Main Street",
            addressLine2 = "Near Park",
            district = "",
            state = "Uttar Pradesh",
            pincode = "226001"
        )
        
        assertThat(student.fullAddress).isEqualTo("123 Main Street, Near Park, Uttar Pradesh - 226001")
    }

    @Test
    fun `fullAddress handles missing pincode`() {
        val student = createStudent(
            addressLine1 = "123 Main Street",
            addressLine2 = "",
            district = "Lucknow",
            state = "Uttar Pradesh",
            pincode = ""
        )
        
        assertThat(student.fullAddress).isEqualTo("123 Main Street, Lucknow, Uttar Pradesh")
    }

    @Test
    fun `fullAddress returns empty when all address fields are empty`() {
        val student = createStudent(
            addressLine1 = "",
            addressLine2 = "",
            district = "",
            state = "",
            pincode = ""
        )
        
        assertThat(student.fullAddress).isEmpty()
    }

    // ===== Class Section Tests =====

    @Test
    fun `classSection returns correct format`() {
        val student = createStudent(currentClass = "5th", section = "A")
        assertThat(student.classSection).isEqualTo("5th - A")
    }

    @Test
    fun `classSection works with different classes`() {
        assertThat(createStudent(currentClass = "NC", section = "A").classSection).isEqualTo("NC - A")
        assertThat(createStudent(currentClass = "LKG", section = "B").classSection).isEqualTo("LKG - B")
        assertThat(createStudent(currentClass = "12th", section = "C").classSection).isEqualTo("12th - C")
    }

    // ===== Entity Mapping Tests =====

    @Test
    fun `toEntity converts Student to StudentEntity correctly`() {
        val student = createStudent(
            id = 1L,
            srNumber = "SR001",
            accountNumber = "ACC001",
            name = "Test Student",
            currentClass = "5th",
            hasTransport = true,
            transportRouteId = 2L
        )
        
        val entity = student.toEntity()
        
        assertThat(entity.id).isEqualTo(1L)
        assertThat(entity.srNumber).isEqualTo("SR001")
        assertThat(entity.accountNumber).isEqualTo("ACC001")
        assertThat(entity.name).isEqualTo("Test Student")
        assertThat(entity.currentClass).isEqualTo("5th")
        assertThat(entity.hasTransport).isTrue()
        assertThat(entity.transportRouteId).isEqualTo(2L)
    }

    @Test
    fun `fromEntity converts StudentEntity to Student correctly`() {
        val timestamp = System.currentTimeMillis()
        val entity = StudentEntity(
            id = 1L,
            srNumber = "SR001",
            accountNumber = "ACC001",
            name = "Test Student",
            fatherName = "Test Father",
            motherName = "Test Mother",
            phonePrimary = "9876543210",
            phoneSecondary = "9876543211",
            addressLine1 = "123 Main Street",
            addressLine2 = "Near Park",
            district = "Lucknow",
            state = "Uttar Pradesh",
            pincode = "226001",
            currentClass = "5th",
            section = "A",
            admissionDate = timestamp,
            admissionSessionId = 1L,
            hasTransport = true,
            transportRouteId = 2L,
            openingBalance = 1000.0,
            openingBalanceRemarks = "Previous dues",
            admissionFeePaid = true,
            isActive = true,
            createdAt = timestamp,
            updatedAt = timestamp
        )
        
        val student = Student.fromEntity(entity)
        
        assertThat(student.id).isEqualTo(1L)
        assertThat(student.srNumber).isEqualTo("SR001")
        assertThat(student.accountNumber).isEqualTo("ACC001")
        assertThat(student.name).isEqualTo("Test Student")
        assertThat(student.fatherName).isEqualTo("Test Father")
        assertThat(student.hasTransport).isTrue()
        assertThat(student.transportRouteId).isEqualTo(2L)
        assertThat(student.openingBalance).isEqualTo(1000.0)
        assertThat(student.admissionFeePaid).isTrue()
    }

    @Test
    fun `toEntity and fromEntity roundtrip preserves all data`() {
        val original = createStudent(
            id = 5L,
            srNumber = "SR999",
            accountNumber = "ACC999",
            name = "Round Trip Student",
            hasTransport = true,
            transportRouteId = 10L,
            openingBalance = 5000.0,
            openingBalanceRemarks = "Test remarks"
        )
        
        val entity = original.toEntity()
        val restored = Student.fromEntity(entity)
        
        assertThat(restored.id).isEqualTo(original.id)
        assertThat(restored.srNumber).isEqualTo(original.srNumber)
        assertThat(restored.accountNumber).isEqualTo(original.accountNumber)
        assertThat(restored.name).isEqualTo(original.name)
        assertThat(restored.hasTransport).isEqualTo(original.hasTransport)
        assertThat(restored.transportRouteId).isEqualTo(original.transportRouteId)
        assertThat(restored.openingBalance).isEqualTo(original.openingBalance)
        assertThat(restored.openingBalanceRemarks).isEqualTo(original.openingBalanceRemarks)
    }
}

/**
 * Unit tests for StudentWithBalance data class.
 */
class StudentWithBalanceTest {

    private fun createTestStudent() = Student(
        id = 1L,
        srNumber = "SR001",
        accountNumber = "ACC001",
        name = "Test Student",
        fatherName = "Test Father",
        phonePrimary = "9876543210",
        currentClass = "5th",
        admissionDate = System.currentTimeMillis(),
        admissionSessionId = 1L
    )

    @Test
    fun `hasDues returns true when balance is positive`() {
        val studentWithBalance = StudentWithBalance(
            student = createTestStudent(),
            currentBalance = 1000.0
        )
        
        assertThat(studentWithBalance.hasDues).isTrue()
        assertThat(studentWithBalance.hasAdvance).isFalse()
        assertThat(studentWithBalance.isCleared).isFalse()
    }

    @Test
    fun `hasAdvance returns true when balance is negative`() {
        val studentWithBalance = StudentWithBalance(
            student = createTestStudent(),
            currentBalance = -500.0
        )
        
        assertThat(studentWithBalance.hasDues).isFalse()
        assertThat(studentWithBalance.hasAdvance).isTrue()
        assertThat(studentWithBalance.isCleared).isFalse()
    }

    @Test
    fun `isCleared returns true when balance is zero`() {
        val studentWithBalance = StudentWithBalance(
            student = createTestStudent(),
            currentBalance = 0.0
        )
        
        assertThat(studentWithBalance.hasDues).isFalse()
        assertThat(studentWithBalance.hasAdvance).isFalse()
        assertThat(studentWithBalance.isCleared).isTrue()
    }

    @Test
    fun `computed properties handle edge cases`() {
        // Very small positive balance
        val smallDue = StudentWithBalance(createTestStudent(), 0.01)
        assertThat(smallDue.hasDues).isTrue()
        
        // Very small negative balance
        val smallAdvance = StudentWithBalance(createTestStudent(), -0.01)
        assertThat(smallAdvance.hasAdvance).isTrue()
    }
}
