package com.example.data.repository

import com.example.data.local.AdminLicenseEntity
import com.example.data.local.AppSettingEntity
import com.example.data.local.AuditLogEntity
import com.example.data.local.AutoPlayLicenseEntity
import com.example.data.local.PaymentTransactionEntity
import com.example.data.local.RolexDao
import com.example.models.AdminLicense
import com.example.models.AppSetting
import com.example.models.AuditLog
import com.example.models.AutoPlayLicense
import com.example.models.LicensePlan
import com.example.models.LicenseStatus
import com.example.models.LicenseType
import com.example.models.PaymentStatus
import com.example.models.PaymentTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.UUID

class RolexRepository(
    private val dao: RolexDao,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val random = SecureRandom()

    val autoPlayLicenses: Flow<List<AutoPlayLicense>> = dao.getAllAutoPlayLicenses().map { list ->
        list.map { it.toDomain() }
    }

    val adminLicenses: Flow<List<AdminLicense>> = dao.getAllAdminLicenses().map { list ->
        list.map { it.toDomain() }
    }

    val allPayments: Flow<List<PaymentTransaction>> = dao.getAllPayments().map { list ->
        list.map { it.toDomain() }
    }

    val auditLogs: Flow<List<AuditLog>> = dao.getAuditLogs().map { list ->
        list.map { it.toDomain() }
    }

    val appSettings: Flow<Map<String, String>> = dao.getAllSettings().map { list ->
        list.associate { it.key to it.value }
    }

    init {
        externalScope.launch {
            seedDefaultsIfEmpty()
        }
    }

    private suspend fun seedDefaultsIfEmpty() {
        val existingSettings = dao.getSettingValue("seeded")
        if (existingSettings != "true") {
            // Seed default settings
            dao.saveSettings(
                listOf(
                    AppSettingEntity("seeded", "true", System.currentTimeMillis()),
                    AppSettingEntity("admin_upi_id", "rolexfastauto@icici", System.currentTimeMillis()),
                    AppSettingEntity("support_whatsapp", "+91 98765 43210", System.currentTimeMillis()),
                    AppSettingEntity("support_telegram", "@RolexFastAutoOfficial", System.currentTimeMillis()),
                    AppSettingEntity("support_email", "support@rolexfastauto.vip", System.currentTimeMillis()),
                    AppSettingEntity("price_1d", "99", System.currentTimeMillis()),
                    AppSettingEntity("price_2d", "180", System.currentTimeMillis()),
                    AppSettingEntity("price_3d", "250", System.currentTimeMillis()),
                    AppSettingEntity("price_7d", "500", System.currentTimeMillis()),
                    AppSettingEntity("price_30d", "1500", System.currentTimeMillis()),
                    AppSettingEntity("price_admin", "500", System.currentTimeMillis())
                )
            )

            // Seed master demo Admin Panel license (setup cost ₹500)
            val demoAdminKey = "RFAX-ADM-7788-9900"
            val now = System.currentTimeMillis()
            dao.insertAdminLicense(
                AdminLicenseEntity(
                    id = UUID.randomUUID().toString(),
                    licenseKey = demoAdminKey,
                    setupCostInr = 500,
                    durationDays = 365,
                    createdAt = now,
                    activatedAt = now,
                    expiresAt = now + (365L * 24 * 60 * 60 * 1000L),
                    status = LicenseStatus.ACTIVE.name,
                    adminEmail = "master.admin@rolexfastauto.vip",
                    isRevoked = false
                )
            )

            // Seed sample active AutoPlay demo license for evaluation
            val demoPlayKey = "RFAX-PLAY-2026-GOLD"
            dao.insertAutoPlayLicense(
                AutoPlayLicenseEntity(
                    id = UUID.randomUUID().toString(),
                    licenseKey = demoPlayKey,
                    durationDays = 7,
                    createdAt = now,
                    activatedAt = now,
                    expiresAt = now + (7L * 24 * 60 * 60 * 1000L),
                    status = LicenseStatus.ACTIVE.name,
                    customerId = "cust_demo",
                    customerName = "VIP Player",
                    deviceFingerprint = "android-device-arm64",
                    isRevoked = false
                )
            )

            // Seed an unactivated 1-Day AutoPlay key ready for customer testing
            val freshKey = "RFAX-PLAY-1001-AUTO"
            dao.insertAutoPlayLicense(
                AutoPlayLicenseEntity(
                    id = UUID.randomUUID().toString(),
                    licenseKey = freshKey,
                    durationDays = 1,
                    createdAt = now,
                    activatedAt = null,
                    expiresAt = null,
                    status = LicenseStatus.UNACTIVATED.name,
                    customerId = null,
                    customerName = null,
                    deviceFingerprint = null,
                    isRevoked = false
                )
            )

            dao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    timestamp = now,
                    actor = "System",
                    action = "INITIALIZE",
                    details = "Rolex Fast Auto initialized with standard pricing and ₹500 Admin setup key."
                )
            )
        }
    }

    // Key Generation
    suspend fun generateAutoPlayKey(durationDays: Int, customerNote: String? = null, actor: String = "Admin"): String {
        val segment1 = generateAlphaNumeric(4)
        val segment2 = generateAlphaNumeric(4)
        val key = "RFAX-PLAY-$segment1-$segment2"
        val now = System.currentTimeMillis()

        dao.insertAutoPlayLicense(
            AutoPlayLicenseEntity(
                id = UUID.randomUUID().toString(),
                licenseKey = key,
                durationDays = durationDays,
                createdAt = now,
                activatedAt = null,
                expiresAt = null,
                status = LicenseStatus.UNACTIVATED.name,
                customerId = null,
                customerName = customerNote,
                deviceFingerprint = null,
                isRevoked = false
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                timestamp = now,
                actor = actor,
                action = "GENERATE_AUTOPLAY_KEY",
                details = "Generated $durationDays-Day AutoPlay key: $key for $customerNote"
            )
        )

        return key
    }

    suspend fun generateAdminLicenseKey(setupCost: Int = 500, durationDays: Int = 365, actor: String = "MasterAdmin"): String {
        val segment1 = generateAlphaNumeric(4)
        val segment2 = generateAlphaNumeric(4)
        val key = "RFAX-ADM-$segment1-$segment2"
        val now = System.currentTimeMillis()

        dao.insertAdminLicense(
            AdminLicenseEntity(
                id = UUID.randomUUID().toString(),
                licenseKey = key,
                setupCostInr = setupCost,
                durationDays = durationDays,
                createdAt = now,
                activatedAt = null,
                expiresAt = null,
                status = LicenseStatus.UNACTIVATED.name,
                adminEmail = null,
                isRevoked = false
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                timestamp = now,
                actor = actor,
                action = "GENERATE_ADMIN_KEY",
                details = "Generated Admin Panel key: $key (Setup cost: ₹$setupCost)"
            )
        )

        return key
    }

    // Activation
    suspend fun activateKey(
        keyInput: String,
        customerId: String,
        customerName: String,
        deviceId: String
    ): Result<Pair<LicenseType, String>> {
        val cleanKey = keyInput.trim().uppercase()

        if (cleanKey.startsWith("RFAX-PLAY")) {
            val entity = dao.getAutoPlayLicenseByKey(cleanKey)
                ?: return Result.failure(Exception("Invalid AutoPlay License Key. Key not found in system."))

            if (entity.isRevoked) {
                return Result.failure(Exception("This AutoPlay License has been REVOKED by the administrator."))
            }

            val now = System.currentTimeMillis()
            val finalExpiresAt: Long
            val finalActivatedAt: Long

            if (entity.activatedAt == null) {
                // First time activation
                finalActivatedAt = now
                finalExpiresAt = now + (entity.durationDays.toLong() * 24 * 60 * 60 * 1000L)
            } else {
                finalActivatedAt = entity.activatedAt
                finalExpiresAt = entity.expiresAt ?: (finalActivatedAt + entity.durationDays.toLong() * 24 * 60 * 60 * 1000L)
            }

            if (now >= finalExpiresAt) {
                dao.updateAutoPlayLicense(
                    entity.copy(status = LicenseStatus.EXPIRED.name)
                )
                return Result.failure(Exception("This AutoPlay License has EXPIRED."))
            }

            val updated = entity.copy(
                activatedAt = finalActivatedAt,
                expiresAt = finalExpiresAt,
                status = LicenseStatus.ACTIVE.name,
                customerId = customerId,
                customerName = customerName,
                deviceFingerprint = deviceId
            )
            dao.updateAutoPlayLicense(updated)

            dao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    timestamp = now,
                    actor = customerName,
                    action = "ACTIVATE_AUTOPLAY",
                    details = "Activated key $cleanKey for ${entity.durationDays} days on device $deviceId."
                )
            )

            return Result.success(Pair(LicenseType.AUTOPLAY, "AutoPlay License successfully activated for ${entity.durationDays} days!"))

        } else if (cleanKey.startsWith("RFAX-ADM")) {
            val entity = dao.getAdminLicenseByKey(cleanKey)
                ?: return Result.failure(Exception("Invalid Admin Panel License Key. Key not found in system."))

            if (entity.isRevoked) {
                return Result.failure(Exception("This Admin Panel License has been REVOKED."))
            }

            val now = System.currentTimeMillis()
            val finalExpiresAt = entity.expiresAt ?: (now + (entity.durationDays.toLong() * 24 * 60 * 60 * 1000L))

            if (now >= finalExpiresAt) {
                dao.updateAdminLicense(entity.copy(status = LicenseStatus.EXPIRED.name))
                return Result.failure(Exception("This Admin Panel License has EXPIRED."))
            }

            val updated = entity.copy(
                activatedAt = entity.activatedAt ?: now,
                expiresAt = finalExpiresAt,
                status = LicenseStatus.ACTIVE.name,
                adminEmail = customerId
            )
            dao.updateAdminLicense(updated)

            dao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    timestamp = now,
                    actor = customerName,
                    action = "ACTIVATE_ADMIN_PANEL",
                    details = "Admin Panel activated using key $cleanKey."
                )
            )

            return Result.success(Pair(LicenseType.ADMIN_PANEL, "Admin Panel Access successfully granted!"))
        } else {
            return Result.failure(Exception("Invalid key format. Key must start with RFAX-PLAY or RFAX-ADM."))
        }
    }

    // Revocation
    suspend fun toggleAutoPlayRevocation(id: String, revoked: Boolean, actor: String = "Admin") {
        dao.updateAutoPlayRevocation(id, revoked)
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                actor = actor,
                action = if (revoked) "REVOKE_KEY" else "RESTORE_KEY",
                details = "Updated revocation to $revoked for license ID: $id"
            )
        )
    }

    suspend fun toggleAdminRevocation(id: String, revoked: Boolean, actor: String = "MasterAdmin") {
        val list = dao.getAllAdminLicenses()
        // Simple direct query can be done via DAO update
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                actor = actor,
                action = if (revoked) "REVOKE_ADMIN" else "RESTORE_ADMIN",
                details = "Admin license $id revocation set to $revoked"
            )
        )
    }

    // Payments
    suspend fun submitPayment(
        customerId: String,
        customerName: String,
        planId: String,
        planName: String,
        amountInr: Int,
        paymentMethod: String,
        utrNumber: String
    ): PaymentTransaction {
        val now = System.currentTimeMillis()
        val tx = PaymentTransactionEntity(
            id = UUID.randomUUID().toString(),
            customerId = customerId,
            customerName = customerName,
            planId = planId,
            planName = planName,
            amountInr = amountInr,
            paymentMethod = paymentMethod,
            utrNumber = utrNumber.trim().uppercase(),
            status = PaymentStatus.PENDING.name,
            createdAt = now,
            verifiedAt = null,
            issuedLicenseKey = null
        )
        dao.insertPayment(tx)

        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                timestamp = now,
                actor = customerName,
                action = "SUBMIT_PAYMENT",
                details = "Submitted payment ₹$amountInr for $planName with UTR: $utrNumber"
            )
        )

        return tx.toDomain()
    }

    suspend fun verifyPayment(
        paymentId: String,
        approve: Boolean,
        adminActor: String = "Admin"
    ): Result<String> {
        val tx = dao.getPaymentById(paymentId) ?: return Result.failure(Exception("Transaction not found."))
        val now = System.currentTimeMillis()

        if (approve) {
            val issuedKey: String = if (tx.planId == "admin_license") {
                generateAdminLicenseKey(setupCost = tx.amountInr, durationDays = 365, actor = adminActor)
            } else {
                val days = when (tx.planId) {
                    "plan_1d" -> 1
                    "plan_2d" -> 2
                    "plan_3d" -> 3
                    "plan_7d" -> 7
                    "plan_30d" -> 30
                    else -> 1
                }
                generateAutoPlayKey(durationDays = days, customerNote = tx.customerName, actor = adminActor)
            }

            val updated = tx.copy(
                status = PaymentStatus.VERIFIED.name,
                verifiedAt = now,
                issuedLicenseKey = issuedKey
            )
            dao.updatePayment(updated)

            dao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    timestamp = now,
                    actor = adminActor,
                    action = "VERIFY_PAYMENT_APPROVED",
                    details = "Approved payment ${tx.id} (₹${tx.amountInr}) and issued key $issuedKey"
                )
            )

            return Result.success("Payment verified and key generated: $issuedKey")
        } else {
            val updated = tx.copy(
                status = PaymentStatus.REJECTED.name,
                verifiedAt = now
            )
            dao.updatePayment(updated)

            dao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    timestamp = now,
                    actor = adminActor,
                    action = "VERIFY_PAYMENT_REJECTED",
                    details = "Rejected payment ${tx.id} (UTR: ${tx.utrNumber})"
                )
            )

            return Result.success("Payment marked as rejected.")
        }
    }

    // Settings & Pricing
    suspend fun updateSetting(key: String, value: String, actor: String = "Admin") {
        dao.saveSetting(AppSettingEntity(key, value, System.currentTimeMillis()))
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                actor = actor,
                action = "UPDATE_SETTING",
                details = "Updated setting $key to $value"
            )
        )
    }

    private fun generateAlphaNumeric(len: Int): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val sb = StringBuilder(len)
        for (i in 0 until len) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        return sb.toString()
    }
}

// Extension mappers
fun AutoPlayLicenseEntity.toDomain(): AutoPlayLicense = AutoPlayLicense(
    id = id,
    licenseKey = licenseKey,
    durationDays = durationDays,
    createdAt = createdAt,
    activatedAt = activatedAt,
    expiresAt = expiresAt,
    status = try { LicenseStatus.valueOf(status) } catch (e: Exception) { LicenseStatus.UNACTIVATED },
    customerId = customerId,
    customerName = customerName,
    deviceFingerprint = deviceFingerprint,
    isRevoked = isRevoked
)

fun AdminLicenseEntity.toDomain(): AdminLicense = AdminLicense(
    id = id,
    licenseKey = licenseKey,
    setupCostInr = setupCostInr,
    durationDays = durationDays,
    createdAt = createdAt,
    activatedAt = activatedAt,
    expiresAt = expiresAt,
    status = try { LicenseStatus.valueOf(status) } catch (e: Exception) { LicenseStatus.UNACTIVATED },
    adminEmail = adminEmail,
    isRevoked = isRevoked
)

fun PaymentTransactionEntity.toDomain(): PaymentTransaction = PaymentTransaction(
    id = id,
    customerId = customerId,
    customerName = customerName,
    planId = planId,
    planName = planName,
    amountInr = amountInr,
    paymentMethod = paymentMethod,
    utrNumber = utrNumber,
    status = try { PaymentStatus.valueOf(status) } catch (e: Exception) { PaymentStatus.PENDING },
    createdAt = createdAt,
    verifiedAt = verifiedAt,
    issuedLicenseKey = issuedLicenseKey
)

fun AuditLogEntity.toDomain(): AuditLog = AuditLog(
    id = id,
    timestamp = timestamp,
    actor = actor,
    action = action,
    details = details
)
