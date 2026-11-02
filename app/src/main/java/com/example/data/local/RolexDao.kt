package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RolexDao {
    // AutoPlay Licenses
    @Query("SELECT * FROM autoplay_licenses ORDER BY createdAt DESC")
    fun getAllAutoPlayLicenses(): Flow<List<AutoPlayLicenseEntity>>

    @Query("SELECT * FROM autoplay_licenses WHERE licenseKey = :key LIMIT 1")
    suspend fun getAutoPlayLicenseByKey(key: String): AutoPlayLicenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutoPlayLicense(license: AutoPlayLicenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutoPlayLicenses(licenses: List<AutoPlayLicenseEntity>)

    @Update
    suspend fun updateAutoPlayLicense(license: AutoPlayLicenseEntity)

    @Query("UPDATE autoplay_licenses SET isRevoked = :revoked, status = CASE WHEN :revoked = 1 THEN 'REVOKED' ELSE 'ACTIVE' END WHERE id = :id")
    suspend fun updateAutoPlayRevocation(id: String, revoked: Boolean)

    // Admin Licenses
    @Query("SELECT * FROM admin_licenses ORDER BY createdAt DESC")
    fun getAllAdminLicenses(): Flow<List<AdminLicenseEntity>>

    @Query("SELECT * FROM admin_licenses WHERE licenseKey = :key LIMIT 1")
    suspend fun getAdminLicenseByKey(key: String): AdminLicenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminLicense(license: AdminLicenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminLicenses(licenses: List<AdminLicenseEntity>)

    @Update
    suspend fun updateAdminLicense(license: AdminLicenseEntity)

    // Payment Transactions
    @Query("SELECT * FROM payment_transactions ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<PaymentTransactionEntity>>

    @Query("SELECT * FROM payment_transactions WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getCustomerPayments(customerId: String): Flow<List<PaymentTransactionEntity>>

    @Query("SELECT * FROM payment_transactions WHERE id = :id LIMIT 1")
    suspend fun getPaymentById(id: String): PaymentTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentTransactionEntity)

    @Update
    suspend fun updatePayment(payment: PaymentTransactionEntity)

    // Application Settings
    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSettingEntity>>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: List<AppSettingEntity>)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}
