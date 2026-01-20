package com.navoditpublic.fees.di

import android.content.Context
import com.navoditpublic.fees.data.local.DataSeeder
import com.navoditpublic.fees.data.local.FeesDatabase
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
import com.navoditpublic.fees.data.local.dao.SessionPromotionDao
import com.navoditpublic.fees.data.repository.AuditRepositoryImpl
import com.navoditpublic.fees.data.repository.FeeRepositoryImpl
import com.navoditpublic.fees.data.repository.SettingsRepositoryImpl
import com.navoditpublic.fees.data.repository.StudentRepositoryImpl
import com.navoditpublic.fees.data.repository.TransportEnrollmentRepositoryImpl
import com.navoditpublic.fees.domain.repository.AuditRepository
import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import com.navoditpublic.fees.domain.repository.StudentRepository
import com.navoditpublic.fees.domain.repository.TransportEnrollmentRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FeesDatabase {
        return FeesDatabase.create(context)
    }
    
    @Provides
    fun provideStudentDao(database: FeesDatabase): StudentDao = database.studentDao()
    
    @Provides
    fun provideAcademicSessionDao(database: FeesDatabase): AcademicSessionDao = database.academicSessionDao()
    
    @Provides
    fun provideClassSectionDao(database: FeesDatabase): ClassSectionDao = database.classSectionDao()
    
    @Provides
    fun provideFeeStructureDao(database: FeesDatabase): FeeStructureDao = database.feeStructureDao()
    
    @Provides
    fun provideTransportRouteDao(database: FeesDatabase): TransportRouteDao = database.transportRouteDao()
    
    @Provides
    fun provideTransportFeeHistoryDao(database: FeesDatabase): TransportFeeHistoryDao = database.transportFeeHistoryDao()
    
    @Provides
    fun provideTransportEnrollmentDao(database: FeesDatabase): TransportEnrollmentDao = database.transportEnrollmentDao()
    
    @Provides
    fun provideSavedReportViewDao(database: FeesDatabase): SavedReportViewDao = database.savedReportViewDao()
    
    @Provides
    fun provideSessionPromotionDao(database: FeesDatabase): SessionPromotionDao = database.sessionPromotionDao()
    
    @Provides
    fun provideReceiptDao(database: FeesDatabase): ReceiptDao = database.receiptDao()
    
    @Provides
    fun provideLedgerDao(database: FeesDatabase): LedgerDao = database.ledgerDao()
    
    @Provides
    fun provideSchoolSettingsDao(database: FeesDatabase): SchoolSettingsDao = database.schoolSettingsDao()
    
    @Provides
    fun provideAuditLogDao(database: FeesDatabase): AuditLogDao = database.auditLogDao()
    
    @Provides
    @Singleton
    fun provideDataSeeder(
        classSectionDao: ClassSectionDao,
        academicSessionDao: AcademicSessionDao,
        schoolSettingsDao: SchoolSettingsDao
    ): DataSeeder {
        return DataSeeder(classSectionDao, academicSessionDao, schoolSettingsDao)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindStudentRepository(impl: StudentRepositoryImpl): StudentRepository
    
    @Binds
    @Singleton
    abstract fun bindFeeRepository(impl: FeeRepositoryImpl): FeeRepository
    
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
    
    @Binds
    @Singleton
    abstract fun bindAuditRepository(impl: AuditRepositoryImpl): AuditRepository
    
    @Binds
    @Singleton
    abstract fun bindTransportEnrollmentRepository(impl: TransportEnrollmentRepositoryImpl): TransportEnrollmentRepository
}

