package com.example.ui

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.RolexApplication
import com.example.data.repository.RolexRepository
import com.example.models.AdminLicense
import com.example.models.AppSetting
import com.example.models.AuditLog
import com.example.models.AuthProvider
import com.example.models.AutoPlayLicense
import com.example.models.LicensePlan
import com.example.models.LicenseStatus
import com.example.models.LicenseType
import com.example.models.PaymentStatus
import com.example.models.PaymentTransaction
import com.example.models.UserProfile
import com.example.simulator.AimCalculationResult
import com.example.simulator.CarromSimulatorEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen(val title: String) {
    HOME("Home"),
    BUY_KEY("Buy Key"),
    ACTIVATE_KEY("Activate Key"),
    CARROM_SIMULATOR("Carrom Simulator"),
    ADMIN_PANEL("Admin Panel"),
    ACCOUNT_COMPATIBILITY("Account Sync"),
    SETTINGS("Settings & Help")
}

class RolexViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RolexRepository = (application as RolexApplication).repository

    // Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // User Profile
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user_vip_001",
            email = "vip.carrom.pro@gmail.com",
            displayName = "Rolex Elite User",
            authProvider = AuthProvider.GOOGLE,
            isAdmin = false
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // License flows
    val autoPlayLicenses: StateFlow<List<AutoPlayLicense>> = repository.autoPlayLicenses
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val adminLicenses: StateFlow<List<AdminLicense>> = repository.adminLicenses
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allPayments: StateFlow<List<PaymentTransaction>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val appSettings: StateFlow<Map<String, String>> = repository.appSettings
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    // Active key caches
    private val _activeAutoPlayLicense = MutableStateFlow<AutoPlayLicense?>(null)
    val activeAutoPlayLicense: StateFlow<AutoPlayLicense?> = _activeAutoPlayLicense.asStateFlow()

    private val _activeAdminLicense = MutableStateFlow<AdminLicense?>(null)
    val activeAdminLicense: StateFlow<AdminLicense?> = _activeAdminLicense.asStateFlow()

    // Activation feedback
    private val _activationMessage = MutableStateFlow<String?>(null)
    val activationMessage: StateFlow<String?> = _activationMessage.asStateFlow()

    private val _activationIsError = MutableStateFlow(false)
    val activationIsError: StateFlow<Boolean> = _activationIsError.asStateFlow()

    // Buy key & Payment states
    private val _selectedPlanId = MutableStateFlow("plan_7d")
    val selectedPlanId: StateFlow<String> = _selectedPlanId.asStateFlow()

    private val _paymentUtrInput = MutableStateFlow("")
    val paymentUtrInput: StateFlow<String> = _paymentUtrInput.asStateFlow()

    private val _paymentStatusMessage = MutableStateFlow<String?>(null)
    val paymentStatusMessage: StateFlow<String?> = _paymentStatusMessage.asStateFlow()

    // Admin Panel State
    private val _adminUnlocked = MutableStateFlow(false)
    val adminUnlocked: StateFlow<Boolean> = _adminUnlocked.asStateFlow()

    private val _generatedKeyResult = MutableStateFlow<String?>(null)
    val generatedKeyResult: StateFlow<String?> = _generatedKeyResult.asStateFlow()

    // Simulator
    val simulatorEngine = CarromSimulatorEngine()
    private val _simulatorTick = MutableStateFlow(0L)
    val simulatorTick: StateFlow<Long> = _simulatorTick.asStateFlow()

    private val _calculatedAim = MutableStateFlow<AimCalculationResult?>(null)
    val calculatedAim: StateFlow<AimCalculationResult?> = _calculatedAim.asStateFlow()

    init {
        // Observe licenses to find currently active ones
        viewModelScope.launch {
            repository.autoPlayLicenses.collect { list ->
                val active = list.firstOrNull { it.status == LicenseStatus.ACTIVE && !it.isRevoked && it.isValid }
                _activeAutoPlayLicense.value = active
            }
        }
        viewModelScope.launch {
            repository.adminLicenses.collect { list ->
                val active = list.firstOrNull { it.status == LicenseStatus.ACTIVE && !it.isRevoked && it.isValid }
                _activeAdminLicense.value = active
                if (active != null) {
                    _adminUnlocked.value = true
                }
            }
        }

        // Simulator physics loop
        viewModelScope.launch {
            while (true) {
                if (simulatorEngine.isMoving) {
                    simulatorEngine.updatePhysicsTick()
                    _simulatorTick.value = System.currentTimeMillis()
                }
                delay(16) // ~60 FPS
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setAuthProvider(provider: AuthProvider) {
        _currentUser.value = _currentUser.value.copy(
            authProvider = provider,
            displayName = when (provider) {
                AuthProvider.GOOGLE -> "Rolex Google Member"
                AuthProvider.FACEBOOK -> "Rolex FB VIP Player"
                AuthProvider.EMAIL -> "carrom.pro@rolexfastauto.vip"
                AuthProvider.GUEST -> "Guest Player #${(1000..9999).random()}"
            }
        )
    }

    fun activateLicenseKey(keyText: String) {
        viewModelScope.launch {
            _activationMessage.value = "Validating with secure licensing engine..."
            _activationIsError.value = false
            delay(400)

            val result = repository.activateKey(
                keyInput = keyText,
                customerId = _currentUser.value.id,
                customerName = _currentUser.value.displayName,
                deviceId = "DEVICE-ARM64-${android.os.Build.MODEL.replace(" ", "-")}"
            )

            result.onSuccess { (type, message) ->
                _activationMessage.value = message
                _activationIsError.value = false
                if (type == LicenseType.ADMIN_PANEL) {
                    _adminUnlocked.value = true
                }
            }.onFailure { err ->
                _activationMessage.value = err.message ?: "Activation failed."
                _activationIsError.value = true
            }
        }
    }

    fun selectPlan(planId: String) {
        _selectedPlanId.value = planId
    }

    fun setPaymentUtr(utr: String) {
        _paymentUtrInput.value = utr
    }

    fun submitCustomerPayment(plan: LicensePlan) {
        if (_paymentUtrInput.value.isBlank()) {
            _paymentStatusMessage.value = "Please enter the Bank UTR / UPI Transaction Reference Number."
            return
        }

        viewModelScope.launch {
            _paymentStatusMessage.value = "Submitting payment for administrator verification..."
            delay(400)
            repository.submitPayment(
                customerId = _currentUser.value.id,
                customerName = _currentUser.value.displayName,
                planId = plan.id,
                planName = plan.name,
                amountInr = plan.priceInr,
                paymentMethod = "UPI Transfer",
                utrNumber = _paymentUtrInput.value
            )
            _paymentStatusMessage.value = "Payment Submitted! Status: VERIFICATION PENDING. Administrator will approve shortly."
            _paymentUtrInput.value = ""
        }
    }

    fun adminGenerateAutoPlayKey(days: Int, note: String?) {
        viewModelScope.launch {
            val key = repository.generateAutoPlayKey(days, note, actor = _currentUser.value.displayName)
            _generatedKeyResult.value = key
        }
    }

    fun adminGenerateAdminKey(setupCost: Int) {
        viewModelScope.launch {
            val key = repository.generateAdminLicenseKey(setupCost = setupCost, actor = _currentUser.value.displayName)
            _generatedKeyResult.value = key
        }
    }

    fun adminVerifyPayment(paymentId: String, approve: Boolean) {
        viewModelScope.launch {
            repository.verifyPayment(paymentId, approve, adminActor = _currentUser.value.displayName)
        }
    }

    fun adminToggleAutoPlayRevocation(id: String, revoke: Boolean) {
        viewModelScope.launch {
            repository.toggleAutoPlayRevocation(id, revoke, actor = _currentUser.value.displayName)
        }
    }

    fun adminUpdatePrice(key: String, newPrice: String) {
        viewModelScope.launch {
            repository.updateSetting(key, newPrice, actor = _currentUser.value.displayName)
        }
    }

    fun adminUpdateSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.updateSetting(key, value, actor = _currentUser.value.displayName)
        }
    }

    fun clearGeneratedKey() {
        _generatedKeyResult.value = null
    }

    // Simulator helpers
    fun simulatorAutoAim() {
        val result = simulatorEngine.calculateAutoAimPath()
        _calculatedAim.value = result
        _simulatorTick.value = System.currentTimeMillis()
    }

    fun simulatorFire() {
        simulatorEngine.fireShot()
        _simulatorTick.value = System.currentTimeMillis()
    }

    fun simulatorReset() {
        simulatorEngine.resetBoard()
        _calculatedAim.value = null
        _simulatorTick.value = System.currentTimeMillis()
    }

    fun isOverlayPermissionGranted(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }
}
