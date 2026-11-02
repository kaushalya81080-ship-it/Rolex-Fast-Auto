package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "autoplay_licenses")
data class AutoPlayLicenseEntity(
    @PrimaryKey val id: String,
    val licenseKey: String,
    val durationDays: Int,
    val createdAt: Long,
    val activatedAt: Long?,
    val expiresAt: Long?,
    val status: String,
    val customerId: String?,
    val customerName: String?,
    val deviceFingerprint: String?,
    val isRevoked: Boolean
)

@Entity(tableName = "admin_licenses")
data class AdminLicenseEntity(
    @PrimaryKey val id: String,
    val licenseKey: String,
    val setupCostInr: Int,
    val durationDays: Int,
    val createdAt: Long,
    val activatedAt: Long?,
    val expiresAt: Long?,
    val status: String,
    val adminEmail: String?,
    val isRevoked: Boolean
)

@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val planId: String,
    val planName: String,
    val amountInr: Int,
    val paymentMethod: String,
    val utrNumber: String,
    val status: String,
    val createdAt: Long,
    val verifiedAt: Long?,
    val issuedLicenseKey: String?
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val actor: String,
    val action: String,
    val details: String
)
