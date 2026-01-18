package com.navoditpublic.fees.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.navoditpublic.fees.data.local.dao.AcademicSessionDao
import com.navoditpublic.fees.data.local.dao.AuditLogDao
import com.navoditpublic.fees.data.local.dao.ClassSectionDao
import com.navoditpublic.fees.data.local.dao.FeeStructureDao
import com.navoditpublic.fees.data.local.dao.LedgerDao
import com.navoditpublic.fees.data.local.dao.ReceiptDao
import com.navoditpublic.fees.data.local.dao.SchoolSettingsDao
import com.navoditpublic.fees.data.local.dao.StudentDao
import com.navoditpublic.fees.data.local.dao.TransportRouteDao
import com.navoditpublic.fees.data.local.dao.TransportFeeHistoryDao
import com.navoditpublic.fees.data.local.dao.TransportEnrollmentDao
import com.navoditpublic.fees.data.local.dao.SavedReportViewDao
import com.navoditpublic.fees.data.local.entity.AcademicSessionEntity
import com.navoditpublic.fees.data.local.entity.AuditLogEntity
import com.navoditpublic.fees.data.local.entity.ClassSectionEntity
import com.navoditpublic.fees.data.local.entity.FeeStructureEntity
import com.navoditpublic.fees.data.local.entity.LedgerEntryEntity
import com.navoditpublic.fees.data.local.entity.ReceiptEntity
import com.navoditpublic.fees.data.local.entity.ReceiptItemEntity
import com.navoditpublic.fees.data.local.entity.SchoolSettingsEntity
import com.navoditpublic.fees.data.local.entity.StudentEntity
import com.navoditpublic.fees.data.local.entity.TransportRouteEntity
import com.navoditpublic.fees.data.local.entity.TransportFeeHistoryEntity
import com.navoditpublic.fees.data.local.entity.TransportEnrollmentEntity
import com.navoditpublic.fees.data.local.entity.SavedReportViewEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentEntity::class,
        AcademicSessionEntity::class,
        ClassSectionEntity::class,
        FeeStructureEntity::class,
        TransportRouteEntity::class,
        TransportFeeHistoryEntity::class,
        TransportEnrollmentEntity::class,
        ReceiptEntity::class,
        ReceiptItemEntity::class,
        LedgerEntryEntity::class,
        SchoolSettingsEntity::class,
        AuditLogEntity::class,
        SavedReportViewEntity::class
    ],
    version = 7,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FeesDatabase : RoomDatabase() {
    
    abstract fun studentDao(): StudentDao
    abstract fun academicSessionDao(): AcademicSessionDao
    abstract fun classSectionDao(): ClassSectionDao
    abstract fun feeStructureDao(): FeeStructureDao
    abstract fun transportRouteDao(): TransportRouteDao
    abstract fun transportFeeHistoryDao(): TransportFeeHistoryDao
    abstract fun transportEnrollmentDao(): TransportEnrollmentDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun schoolSettingsDao(): SchoolSettingsDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun savedReportViewDao(): SavedReportViewDao
    
    companion object {
        const val DATABASE_NAME = "fees_database"
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to transport_routes table
                db.execSQL("ALTER TABLE transport_routes ADD COLUMN is_closed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transport_routes ADD COLUMN closed_date INTEGER")
                db.execSQL("ALTER TABLE transport_routes ADD COLUMN close_reason TEXT")
                
                // Create transport_fee_history table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS transport_fee_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        route_id INTEGER NOT NULL,
                        monthly_fee REAL NOT NULL,
                        effective_from INTEGER NOT NULL,
                        notes TEXT NOT NULL DEFAULT '',
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY(route_id) REFERENCES transport_routes(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transport_fee_history_route_id ON transport_fee_history(route_id)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_transport_fee_history_route_id_effective_from ON transport_fee_history(route_id, effective_from)")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add opening balance fields to students table
                db.execSQL("ALTER TABLE students ADD COLUMN opening_balance REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE students ADD COLUMN opening_balance_remarks TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN opening_balance_date INTEGER")
                db.execSQL("ALTER TABLE students ADD COLUMN admission_fee_paid INTEGER NOT NULL DEFAULT 0")
                
                // Create transport_enrollments table for tracking student transport history
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS transport_enrollments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        student_id INTEGER NOT NULL,
                        route_id INTEGER NOT NULL,
                        start_date INTEGER NOT NULL,
                        end_date INTEGER,
                        monthly_fee_at_enrollment REAL NOT NULL,
                        remarks TEXT NOT NULL DEFAULT '',
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE,
                        FOREIGN KEY(route_id) REFERENCES transport_routes(id) ON DELETE RESTRICT
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transport_enrollments_student_id ON transport_enrollments(student_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transport_enrollments_route_id ON transport_enrollments(route_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transport_enrollments_student_id_start_date ON transport_enrollments(student_id, start_date)")
            }
        }
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create saved_report_views table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS saved_report_views (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        view_name TEXT NOT NULL,
                        report_type TEXT NOT NULL,
                        selected_columns TEXT NOT NULL,
                        filter_config TEXT NOT NULL DEFAULT '',
                        sort_column TEXT NOT NULL DEFAULT '',
                        sort_ascending INTEGER NOT NULL DEFAULT 1,
                        is_default INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                """)
            }
        }
        
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add class-wise transport fee columns
                db.execSQL("ALTER TABLE transport_routes ADD COLUMN fee_nc_to_5 REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE transport_routes ADD COLUMN fee_6_to_8 REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE transport_routes ADD COLUMN fee_9_to_12 REAL NOT NULL DEFAULT 0.0")
                
                // Migrate existing monthly_fee to all three columns for backward compatibility
                db.execSQL("UPDATE transport_routes SET fee_nc_to_5 = monthly_fee, fee_6_to_8 = monthly_fee, fee_9_to_12 = monthly_fee")
            }
        }
        
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Remove CHEQUE and UPI payment mode columns from receipts table
                // SQLite doesn't support DROP COLUMN directly, so we recreate the table
                
                // 1. Create new table without the old columns
                db.execSQL("""
                    CREATE TABLE receipts_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        receipt_number INTEGER NOT NULL,
                        student_id INTEGER NOT NULL,
                        session_id INTEGER NOT NULL,
                        receipt_date INTEGER NOT NULL,
                        total_amount REAL NOT NULL,
                        discount_amount REAL NOT NULL DEFAULT 0.0,
                        net_amount REAL NOT NULL,
                        payment_mode TEXT NOT NULL,
                        online_reference TEXT,
                        remarks TEXT NOT NULL DEFAULT '',
                        is_cancelled INTEGER NOT NULL DEFAULT 0,
                        cancelled_at INTEGER,
                        cancellation_reason TEXT,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE RESTRICT,
                        FOREIGN KEY(session_id) REFERENCES academic_sessions(id) ON DELETE RESTRICT
                    )
                """)
                
                // 2. Copy data from old table (map UPI/CHEQUE to ONLINE)
                db.execSQL("""
                    INSERT INTO receipts_new (
                        id, receipt_number, student_id, session_id, receipt_date,
                        total_amount, discount_amount, net_amount, payment_mode,
                        online_reference, remarks, is_cancelled, cancelled_at,
                        cancellation_reason, created_at, updated_at
                    )
                    SELECT 
                        id, receipt_number, student_id, session_id, receipt_date,
                        total_amount, discount_amount, net_amount,
                        CASE 
                            WHEN payment_mode = 'UPI' THEN 'ONLINE'
                            WHEN payment_mode = 'CHEQUE' THEN 'ONLINE'
                            ELSE payment_mode
                        END,
                        COALESCE(online_reference, upi_reference, cheque_number),
                        remarks, is_cancelled, cancelled_at,
                        cancellation_reason, created_at, updated_at
                    FROM receipts
                """)
                
                // 3. Drop old table
                db.execSQL("DROP TABLE receipts")
                
                // 4. Rename new table
                db.execSQL("ALTER TABLE receipts_new RENAME TO receipts")
                
                // 5. Recreate indexes
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_receipts_receipt_number ON receipts(receipt_number)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_receipts_student_id ON receipts(student_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_receipts_session_id ON receipts(session_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_receipts_receipt_date ON receipts(receipt_date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_receipts_is_cancelled ON receipts(is_cancelled)")
            }
        }
        
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add class-wise fee columns to transport_fee_history table
                db.execSQL("ALTER TABLE transport_fee_history ADD COLUMN fee_nc_to_5 REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE transport_fee_history ADD COLUMN fee_6_to_8 REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE transport_fee_history ADD COLUMN fee_9_to_12 REAL NOT NULL DEFAULT 0.0")
                
                // Migrate existing data: copy monthly_fee to all three columns
                db.execSQL("UPDATE transport_fee_history SET fee_nc_to_5 = monthly_fee, fee_6_to_8 = monthly_fee, fee_9_to_12 = monthly_fee")
            }
        }
        
        fun create(context: Context): FeesDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FeesDatabase::class.java,
                DATABASE_NAME
            )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .addCallback(DatabaseCallback())
            // NOTE: Removed fallbackToDestructiveMigration() to protect user data
            // All schema changes must have explicit migrations
            .build()
        }
    }
    
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Seed initial data
            CoroutineScope(Dispatchers.IO).launch {
                // Initial data will be seeded through DataSeeder
            }
        }
    }
}

