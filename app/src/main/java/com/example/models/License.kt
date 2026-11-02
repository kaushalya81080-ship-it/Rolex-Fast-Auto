package com.example.models

enum class LicenseType(val displayName: String, val prefix: String) {
    AUTOPLAY("AutoPlay License", "RFAX-PLAY"),
    ADMIN_PANEL("Admin Panel License", "RFAX-ADM")
}

enum class LicenseStatus(val displayName: String) {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    REVOKED("Revoked"),
    UNACTIVATED("Unactivated")
}

data class AutoPlayLicense(
    val id: String,
    val licenseKey: String,
    val durationDays: Int,
    val createdAt: Long,
    val activatedAt: Long? = null,
    val expiresAt: Long? = null,
    val status: LicenseStatus = LicenseStatus.UNACTIVATED,
    val customerId: String? = null,
    val customerName: String? = null,
    val deviceFingerprint: String? = null,
    val isRevoked: Boolean = false
) {
    val isValid: Boolean
        get() {
            if (isRevoked || status == LicenseStatus.REVOKED) return false
            if (expiresAt == null) return false
            return System.currentTimeMillis() < expiresAt
        }

    val remainingTimeFormatted: String
        get() {
            if (expiresAt == null) return "Not Activated"
            val diff = expiresAt - System.currentTimeMillis()
            if (diff <= 0) return "Expired"
            val days = diff / (1000 * 60 * 60 * 24)
            val hours = (diff / (1000 * 60 * 60)) % 24
            val minutes = (diff / (1000 * 60)) % 60
            return if (days > 0) "${days}d ${hours}h ${minutes}m" else "${hours}h ${minutes}m"
        }
}

data class AdminLicense(
    val id: String,
    val licenseKey: String,
    val setupCostInr: Int = 500,
    val durationDays: Int = 365,
    val createdAt: Long,
    val activatedAt: Long? = null,
    val expiresAt: Long? = null,
    val status: LicenseStatus = LicenseStatus.UNACTIVATED,
    val adminEmail: String? = null,
    val isRevoked: Boolean = false
) {
    val isValid: Boolean
        get() {
            if (isRevoked || status == LicenseStatus.REVOKED) return false
            if (expiresAt == null) return false
            return System.currentTimeMillis() < expiresAt
        }
}

data class LicensePlan(
    val id: String,
    val name: String,
    val durationDays: Int,
    val priceInr: Int,
    val badge: String? = null,
    val description: String,
    val features: List<String>
)

enum class PaymentStatus(val displayName: String) {
    PENDING("Verification Pending"),
    VERIFIED("Payment Verified"),
    REJECTED("Payment Rejected")
}

data class PaymentTransaction(
    val id: String,
    val customerId: String,
    val customerName: String,
    val planId: String,
    val planName: String,
    val amountInr: Int,
    val paymentMethod: String,
    val utrNumber: String,
    val status: PaymentStatus,
    val createdAt: Long,
    val verifiedAt: Long? = null,
    val issuedLicenseKey: String? = null
)

data class AppSetting(
    val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

data class AuditLog(
    val id: String,
    val timestamp: Long,
    val actor: String,
    val action: String,
    val details: String
)

enum class AuthProvider(val displayName: String) {
    GOOGLE("Google Sign-In"),
    FACEBOOK("Facebook Login"),
    EMAIL("Email / Password"),
    GUEST("Guest Account")
}

data class UserProfile(
    val id: String,
    val email: String?,
    val displayName: String,
    val authProvider: AuthProvider,
    val isAdmin: Boolean = false,
    val activeAutoPlayKey: String? = null,
    val activeAdminKey: String? = null
)
