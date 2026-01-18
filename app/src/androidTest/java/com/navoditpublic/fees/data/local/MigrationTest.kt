package com.navoditpublic.fees.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Tests for Room database migrations.
 * Ensures data integrity across schema changes.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FeesDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    // ===== Migration 1 to 2 Tests =====
    // Adds transport route closure fields and transport_fee_history table

    @Test
    @Throws(IOException::class)
    fun migrate1To2_addsTransportRouteColumns() {
        // Create database at version 1
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert a transport route at version 1
            execSQL("""
                INSERT INTO transport_routes (id, route_name, monthly_fee, is_active, created_at, updated_at)
                VALUES (1, 'Route A', 800.0, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """)
            close()
        }

        // Run migration and validate
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true)
        
        // Verify new columns exist with default values
        val cursor = db.query("SELECT is_closed, closed_date, close_reason FROM transport_routes WHERE id = 1")
        cursor.moveToFirst()
        
        assertThat(cursor.getInt(0)).isEqualTo(0) // is_closed default = 0
        assertThat(cursor.isNull(1)).isTrue() // closed_date default = null
        assertThat(cursor.isNull(2)).isTrue() // close_reason default = null
        
        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2_createsTransportFeeHistoryTable() {
        helper.createDatabase(TEST_DB, 1).apply { close() }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true)
        
        // Verify table exists by inserting data
        db.execSQL("""
            INSERT INTO transport_routes (id, route_name, monthly_fee, is_active, is_closed, created_at, updated_at)
            VALUES (1, 'Route A', 800.0, 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
        """)
        
        db.execSQL("""
            INSERT INTO transport_fee_history (route_id, monthly_fee, effective_from, notes, created_at)
            VALUES (1, 800.0, ${System.currentTimeMillis()}, 'Initial fee', ${System.currentTimeMillis()})
        """)
        
        val cursor = db.query("SELECT COUNT(*) FROM transport_fee_history")
        cursor.moveToFirst()
        assertThat(cursor.getInt(0)).isEqualTo(1)
        
        cursor.close()
        db.close()
    }

    // ===== Migration 2 to 3 Tests =====
    // Adds opening balance fields and transport_enrollments table

    @Test
    @Throws(IOException::class)
    fun migrate2To3_addsStudentOpeningBalanceColumns() {
        // Create database at version 2
        helper.createDatabase(TEST_DB, 2).apply {
            // First create required session
            execSQL("""
                INSERT INTO academic_sessions (id, session_name, start_date, end_date, is_current, is_active, created_at)
                VALUES (1, '2025-26', ${System.currentTimeMillis()}, ${System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L}, 1, 1, ${System.currentTimeMillis()})
            """)
            
            // Insert a student at version 2
            execSQL("""
                INSERT INTO students (id, sr_number, account_number, name, father_name, mother_name, 
                    phone_primary, phone_secondary, address_line1, address_line2, district, state, pincode,
                    photo_path, current_class, section, admission_date, admission_session_id, 
                    has_transport, transport_route_id, is_active, created_at, updated_at)
                VALUES (1, 'SR001', 'ACC001', 'Test Student', 'Test Father', '', 
                    '9876543210', '', '', '', '', 'UP', '',
                    NULL, '5th', 'A', ${System.currentTimeMillis()}, 1, 
                    0, NULL, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """)
            close()
        }

        // Run migration
        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true)
        
        // Verify new columns exist with default values
        val cursor = db.query("""
            SELECT opening_balance, opening_balance_remarks, opening_balance_date, admission_fee_paid 
            FROM students WHERE id = 1
        """)
        cursor.moveToFirst()
        
        assertThat(cursor.getDouble(0)).isEqualTo(0.0) // opening_balance default = 0.0
        assertThat(cursor.getString(1)).isEmpty() // opening_balance_remarks default = ''
        assertThat(cursor.isNull(2)).isTrue() // opening_balance_date default = null
        assertThat(cursor.getInt(3)).isEqualTo(0) // admission_fee_paid default = 0
        
        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate2To3_createsTransportEnrollmentsTable() {
        helper.createDatabase(TEST_DB, 2).apply { close() }

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true)
        
        // Setup required foreign key data
        db.execSQL("""
            INSERT INTO academic_sessions (id, session_name, start_date, end_date, is_current, is_active, created_at)
            VALUES (1, '2025-26', ${System.currentTimeMillis()}, ${System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L}, 1, 1, ${System.currentTimeMillis()})
        """)
        
        db.execSQL("""
            INSERT INTO students (id, sr_number, account_number, name, father_name, mother_name, 
                phone_primary, current_class, section, admission_date, admission_session_id, 
                has_transport, is_active, opening_balance, opening_balance_remarks, admission_fee_paid, created_at, updated_at)
            VALUES (1, 'SR001', 'ACC001', 'Test', 'Father', '', 
                '9876543210', '5th', 'A', ${System.currentTimeMillis()}, 1, 
                1, 1, 0.0, '', 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
        """)
        
        db.execSQL("""
            INSERT INTO transport_routes (id, route_name, monthly_fee, is_active, is_closed, created_at, updated_at)
            VALUES (1, 'Route A', 800.0, 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
        """)
        
        // Verify transport_enrollments table exists
        db.execSQL("""
            INSERT INTO transport_enrollments (student_id, route_id, start_date, end_date, 
                monthly_fee_at_enrollment, remarks, created_at, updated_at)
            VALUES (1, 1, ${System.currentTimeMillis()}, NULL, 800.0, '', 
                ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
        """)
        
        val cursor = db.query("SELECT COUNT(*) FROM transport_enrollments")
        cursor.moveToFirst()
        assertThat(cursor.getInt(0)).isEqualTo(1)
        
        cursor.close()
        db.close()
    }

    // ===== Migration 3 to 4 Tests =====
    // Adds saved_report_views table

    @Test
    @Throws(IOException::class)
    fun migrate3To4_createsSavedReportViewsTable() {
        helper.createDatabase(TEST_DB, 3).apply { close() }

        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true)
        
        // Verify table exists by inserting data
        db.execSQL("""
            INSERT INTO saved_report_views (view_name, report_type, selected_columns, 
                filter_config, sort_column, sort_ascending, is_default, created_at, updated_at)
            VALUES ('My View', 'DUES', 'name,class,balance', 
                '', 'name', 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
        """)
        
        val cursor = db.query("SELECT view_name, report_type FROM saved_report_views")
        cursor.moveToFirst()
        
        assertThat(cursor.getString(0)).isEqualTo("My View")
        assertThat(cursor.getString(1)).isEqualTo("DUES")
        
        cursor.close()
        db.close()
    }

    // ===== Migration 4 to 5 Tests =====
    // Adds class-wise transport fee columns

    @Test
    @Throws(IOException::class)
    fun migrate4To5_addsClassWiseTransportFeeColumns() {
        // Create database at version 4
        helper.createDatabase(TEST_DB, 4).apply {
            // Insert a transport route at version 4
            execSQL("""
                INSERT INTO transport_routes (id, route_name, monthly_fee, is_active, is_closed, created_at, updated_at)
                VALUES (1, 'Route A', 800.0, 1, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """)
            close()
        }

        // Run migration
        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true)
        
        // Verify new columns exist and are populated from monthly_fee
        val cursor = db.query("""
            SELECT fee_nc_to_5, fee_6_to_8, fee_9_to_12, monthly_fee 
            FROM transport_routes WHERE id = 1
        """)
        cursor.moveToFirst()
        
        // All class-wise fees should be migrated from monthly_fee
        assertThat(cursor.getDouble(0)).isEqualTo(800.0) // fee_nc_to_5
        assertThat(cursor.getDouble(1)).isEqualTo(800.0) // fee_6_to_8
        assertThat(cursor.getDouble(2)).isEqualTo(800.0) // fee_9_to_12
        assertThat(cursor.getDouble(3)).isEqualTo(800.0) // original monthly_fee
        
        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate4To5_preservesExistingRouteData() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL("""
                INSERT INTO transport_routes (id, route_name, monthly_fee, is_active, is_closed, 
                    closed_date, close_reason, created_at, updated_at)
                VALUES (1, 'Route A', 800.0, 1, 0, NULL, NULL, 
                    ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """)
            execSQL("""
                INSERT INTO transport_routes (id, route_name, monthly_fee, is_active, is_closed, 
                    closed_date, close_reason, created_at, updated_at)
                VALUES (2, 'Route B', 1000.0, 1, 1, ${System.currentTimeMillis()}, 'No longer needed', 
                    ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """)
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true)
        
        // Verify Route A data
        val cursor1 = db.query("SELECT route_name, monthly_fee, is_closed FROM transport_routes WHERE id = 1")
        cursor1.moveToFirst()
        assertThat(cursor1.getString(0)).isEqualTo("Route A")
        assertThat(cursor1.getDouble(1)).isEqualTo(800.0)
        assertThat(cursor1.getInt(2)).isEqualTo(0)
        cursor1.close()
        
        // Verify Route B data (closed route)
        val cursor2 = db.query("SELECT route_name, monthly_fee, is_closed, close_reason FROM transport_routes WHERE id = 2")
        cursor2.moveToFirst()
        assertThat(cursor2.getString(0)).isEqualTo("Route B")
        assertThat(cursor2.getDouble(1)).isEqualTo(1000.0)
        assertThat(cursor2.getInt(2)).isEqualTo(1)
        assertThat(cursor2.getString(3)).isEqualTo("No longer needed")
        cursor2.close()
        
        db.close()
    }

    // ===== Full Migration Path Tests =====

    @Test
    @Throws(IOException::class)
    fun migrateAll_fromVersion1ToLatest() {
        // Create database at version 1
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert initial data
            execSQL("""
                INSERT INTO academic_sessions (id, session_name, start_date, end_date, is_current, is_active, created_at)
                VALUES (1, '2025-26', ${System.currentTimeMillis()}, ${System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L}, 1, 1, ${System.currentTimeMillis()})
            """)
            
            execSQL("""
                INSERT INTO transport_routes (id, route_name, monthly_fee, is_active, created_at, updated_at)
                VALUES (1, 'Route A', 800.0, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """)
            close()
        }

        // Run all migrations to latest version (5)
        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true)
        
        // Verify data survived all migrations
        val cursor = db.query("SELECT route_name, fee_nc_to_5, fee_6_to_8, fee_9_to_12 FROM transport_routes WHERE id = 1")
        cursor.moveToFirst()
        
        assertThat(cursor.getString(0)).isEqualTo("Route A")
        assertThat(cursor.getDouble(1)).isEqualTo(800.0)
        assertThat(cursor.getDouble(2)).isEqualTo(800.0)
        assertThat(cursor.getDouble(3)).isEqualTo(800.0)
        
        cursor.close()
        db.close()
    }

    // ===== Data Integrity Tests =====

    @Test
    @Throws(IOException::class)
    fun migration_preservesStudentData() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL("""
                INSERT INTO academic_sessions (id, session_name, start_date, end_date, is_current, is_active, created_at)
                VALUES (1, '2025-26', ${System.currentTimeMillis()}, ${System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L}, 1, 1, ${System.currentTimeMillis()})
            """)
            
            execSQL("""
                INSERT INTO students (id, sr_number, account_number, name, father_name, mother_name, 
                    phone_primary, phone_secondary, address_line1, address_line2, district, state, pincode,
                    photo_path, current_class, section, admission_date, admission_session_id, 
                    has_transport, transport_route_id, is_active, created_at, updated_at)
                VALUES (1, 'SR001', 'ACC001', 'John Doe', 'Father Doe', 'Mother Doe', 
                    '9876543210', '9876543211', '123 Main St', 'Near Park', 'Lucknow', 'UP', '226001',
                    NULL, '5th', 'A', ${System.currentTimeMillis()}, 1, 
                    0, NULL, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """)
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true)
        
        val cursor = db.query("""
            SELECT sr_number, account_number, name, father_name, current_class, opening_balance 
            FROM students WHERE id = 1
        """)
        cursor.moveToFirst()
        
        assertThat(cursor.getString(0)).isEqualTo("SR001")
        assertThat(cursor.getString(1)).isEqualTo("ACC001")
        assertThat(cursor.getString(2)).isEqualTo("John Doe")
        assertThat(cursor.getString(3)).isEqualTo("Father Doe")
        assertThat(cursor.getString(4)).isEqualTo("5th")
        assertThat(cursor.getDouble(5)).isEqualTo(0.0) // New column with default
        
        cursor.close()
        db.close()
    }
}
