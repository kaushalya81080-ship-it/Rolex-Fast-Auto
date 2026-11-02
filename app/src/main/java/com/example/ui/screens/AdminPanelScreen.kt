package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.LicenseStatus
import com.example.models.PaymentStatus
import com.example.ui.AppScreen
import com.example.ui.RolexViewModel
import com.example.ui.components.PaymentStatusPill
import com.example.ui.components.RolexCard
import com.example.ui.components.RolexHeader
import com.example.ui.components.RolexPrimaryButton
import com.example.ui.components.RolexSecondaryButton
import com.example.ui.components.StatusPill
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.OnyxBlack
import com.example.ui.theme.RolexGold
import com.example.ui.theme.RolexGreen
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusRevoked
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    viewModel: RolexViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    val adminUnlocked by viewModel.adminUnlocked.collectAsState()
    val activeAdmin by viewModel.activeAdminLicense.collectAsState()
    val autoPlayLicenses by viewModel.autoPlayLicenses.collectAsState()
    val adminLicenses by viewModel.adminLicenses.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    val generatedKey by viewModel.generatedKeyResult.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Generate Keys", "Manage Licenses", "Payments", "Pricing & Support", "Audit Logs")

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
    ) {
        RolexHeader(
            title = "ADMINISTRATOR DASHBOARD",
            subtitle = "Rolex Fast Auto Secure Control Center"
        )

        if (!adminUnlocked) {
            // Admin Access Gate
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(RolexGreen.copy(alpha = 0.2f))
                        .padding(20.dp)
                ) {
                    Text(text = "🛡️", fontSize = 48.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Administrator Authorization Required",
                    color = RolexGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "The Admin Panel requires a dedicated Admin Panel License Key (RFAX-ADM-...).\n\nInitial Admin Panel Setup Cost: ₹500 (Project Configured Price).\n\nNote: AutoPlay Keys cannot be used to unlock the Administrator Dashboard.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                var adminKeyInput by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = adminKeyInput,
                    onValueChange = { adminKeyInput = it.uppercase() },
                    placeholder = { Text("Enter Admin Key (e.g. RFAX-ADM-7788-9900)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_login_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RolexGold,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = RolexGold,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                RolexPrimaryButton(
                    text = "UNLOCK ADMIN PANEL",
                    onClick = {
                        if (adminKeyInput.isNotBlank()) {
                            viewModel.activateLicenseKey(adminKeyInput)
                        }
                    },
                    testTag = "unlock_admin_btn"
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "💡 Quick Testing Note: The pre-configured demo key is RFAX-ADM-7788-9900",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
        } else {
            // Admin Panel Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = RolexGold,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RolexGold,
                        height = 2.5.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = tabTitle,
                                color = if (selectedTab == index) RolexGold else TextSecondary,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> TabKeyGenerator(viewModel)
                    1 -> TabLicenseManager(viewModel, autoPlayLicenses, adminLicenses, dateFormat)
                    2 -> TabPaymentVerification(viewModel, payments, dateFormat)
                    3 -> TabPricingAndSupport(viewModel, settings)
                    4 -> TabAuditLogs(auditLogs, dateFormat)
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Generated Key Dialog
    if (generatedKey != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearGeneratedKey() },
            title = {
                Text(text = "Key Successfully Generated", color = RolexGold, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(text = "Share this activation key with the customer:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .padding(12.dp),
                        color = DarkSurface
                    ) {
                        Text(
                            text = generatedKey ?: "",
                            color = RolexGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("License Key", generatedKey))
                        Toast.makeText(context, "Key copied to clipboard!", Toast.LENGTH_SHORT).show()
                        viewModel.clearGeneratedKey()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RolexGold, contentColor = OnyxBlack)
                ) {
                    Text("Copy & Close", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun TabKeyGenerator(viewModel: RolexViewModel) {
    var selectedDurationDays by remember { mutableIntStateOf(7) }
    var customDaysInput by remember { mutableStateOf("") }
    var customerNote by remember { mutableStateOf("") }

    RolexCard(hasGoldBorder = true) {
        Text(
            text = "GENERATE AUTOPLAY LICENSE KEY",
            color = RolexGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Generate a single or batch AutoPlay key for Miniclip Carrom Pool. Duration begins when customer activates it.",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "SELECT DURATION", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        val options = listOf(1, 2, 3, 7, 30)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { days ->
                val isSelected = selectedDurationDays == days && customDaysInput.isBlank()
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            selectedDurationDays = days
                            customDaysInput = ""
                        },
                    color = if (isSelected) RolexGold else DarkSurface,
                    border = BorderStroke(1.dp, if (isSelected) RolexGold else DarkBorder)
                ) {
                    Text(
                        text = "${days}D",
                        color = if (isSelected) OnyxBlack else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = customDaysInput,
            onValueChange = {
                customDaysInput = it
                it.toIntOrNull()?.let { d -> selectedDurationDays = d }
            },
            label = { Text("Or Enter Custom Days") },
            placeholder = { Text("e.g. 60 or 90") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = RolexGold,
                unfocusedBorderColor = DarkBorder
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = customerNote,
            onValueChange = { customerNote = it },
            label = { Text("Customer Name / Reference Note") },
            placeholder = { Text("e.g. VIP League Player #401") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = RolexGold,
                unfocusedBorderColor = DarkBorder
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        RolexPrimaryButton(
            text = "GENERATE AUTOPLAY KEY ($selectedDurationDays DAYS)",
            onClick = {
                viewModel.adminGenerateAutoPlayKey(selectedDurationDays, customerNote.ifBlank { null })
                customerNote = ""
            },
            testTag = "admin_gen_play_key_btn"
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Generate Admin Key Card
    RolexCard {
        Text(
            text = "GENERATE ADMIN PANEL KEY (₹500 SETUP)",
            color = RolexGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Generates a dedicated administrative license (RFAX-ADM-...) granting dashboard control.",
            color = TextSecondary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        RolexSecondaryButton(
            text = "GENERATE ADMIN PANEL KEY",
            onClick = { viewModel.adminGenerateAdminKey(500) },
            testTag = "admin_gen_adm_key_btn"
        )
    }
}

@Composable
private fun TabLicenseManager(
    viewModel: RolexViewModel,
    autoPlayLicenses: List<com.example.models.AutoPlayLicense>,
    adminLicenses: List<com.example.models.AdminLicense>,
    dateFormat: SimpleDateFormat
) {
    Text(
        text = "AUTOPLAY LICENSES (${autoPlayLicenses.size})",
        color = RolexGold,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))

    autoPlayLicenses.forEach { lic ->
        RolexCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = lic.licenseKey, color = TextGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${lic.durationDays} Days • ${lic.customerName ?: "Unassigned"}", color = TextSecondary, fontSize = 11.sp)
                }
                StatusPill(status = lic.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (lic.expiresAt != null) "Expires: ${dateFormat.format(Date(lic.expiresAt))}" else "Unactivated",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
                Text(
                    text = if (lic.isRevoked) "REVOKED" else "ACTIVE",
                    color = if (lic.isRevoked) StatusRevoked else StatusActive,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            RolexSecondaryButton(
                text = if (lic.isRevoked) "RESTORE LICENSE" else "REVOKE LICENSE",
                onClick = { viewModel.adminToggleAutoPlayRevocation(lic.id, !lic.isRevoked) },
                modifier = Modifier.height(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "ADMIN LICENSES (${adminLicenses.size})",
        color = RolexGold,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))

    adminLicenses.forEach { adm ->
        RolexCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = adm.licenseKey, color = RolexGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Setup: ₹${adm.setupCostInr} • ${adm.adminEmail ?: "Admin"}", color = TextSecondary, fontSize = 11.sp)
                }
                StatusPill(status = adm.status)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun TabPaymentVerification(
    viewModel: RolexViewModel,
    payments: List<com.example.models.PaymentTransaction>,
    dateFormat: SimpleDateFormat
) {
    Text(
        text = "CUSTOMER PAYMENT SUBMISSIONS (${payments.size})",
        color = RolexGold,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (payments.isEmpty()) {
        RolexCard {
            Text(text = "No payments recorded yet.", color = TextSecondary, fontSize = 12.sp)
        }
    } else {
        payments.forEach { tx ->
            RolexCard(hasGoldBorder = tx.status == PaymentStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = tx.planName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Customer: ${tx.customerName}", color = TextSecondary, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "₹${tx.amountInr}", color = RolexGold, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        PaymentStatusPill(status = tx.status)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "UTR / Ref: ${tx.utrNumber}", color = TextGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Submitted: ${dateFormat.format(Date(tx.createdAt))}", color = TextTertiary, fontSize = 10.sp)

                if (tx.status == PaymentStatus.PENDING) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.adminVerifyPayment(tx.id, approve = true) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RolexGreen, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("APPROVE & ISSUE KEY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.adminVerifyPayment(tx.id, approve = false) },
                            modifier = Modifier.weight(0.6f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusExpired, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("REJECT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (tx.issuedLicenseKey != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Issued Key: ${tx.issuedLicenseKey}", color = RolexGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TabPricingAndSupport(
    viewModel: RolexViewModel,
    settings: Map<String, String>
) {
    var p1d by remember(settings) { mutableStateOf(settings["price_1d"] ?: "99") }
    var p2d by remember(settings) { mutableStateOf(settings["price_2d"] ?: "180") }
    var p3d by remember(settings) { mutableStateOf(settings["price_3d"] ?: "250") }
    var p7d by remember(settings) { mutableStateOf(settings["price_7d"] ?: "500") }
    var p30d by remember(settings) { mutableStateOf(settings["price_30d"] ?: "1500") }
    var pAdmin by remember(settings) { mutableStateOf(settings["price_admin"] ?: "500") }

    var upi by remember(settings) { mutableStateOf(settings["admin_upi_id"] ?: "rolexfastauto@icici") }
    var whatsapp by remember(settings) { mutableStateOf(settings["support_whatsapp"] ?: "+91 98765 43210") }
    var telegram by remember(settings) { mutableStateOf(settings["support_telegram"] ?: "@RolexFastAutoOfficial") }

    RolexCard(hasGoldBorder = true) {
        Text(text = "CONFIGURE CUSTOMER PRICING (INR ₹)", color = RolexGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = p1d,
                onValueChange = { p1d = it },
                label = { Text("1-Day (₹)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = p2d,
                onValueChange = { p2d = it },
                label = { Text("2-Day (₹)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = p3d,
                onValueChange = { p3d = it },
                label = { Text("3-Day (₹)") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = p7d,
                onValueChange = { p7d = it },
                label = { Text("7-Day (₹)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = p30d,
                onValueChange = { p30d = it },
                label = { Text("30-Day (₹)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = pAdmin,
                onValueChange = { pAdmin = it },
                label = { Text("Admin Setup (₹)") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        RolexPrimaryButton(
            text = "SAVE PRICING CONFIGURATION",
            onClick = {
                viewModel.adminUpdatePrice("price_1d", p1d)
                viewModel.adminUpdatePrice("price_2d", p2d)
                viewModel.adminUpdatePrice("price_3d", p3d)
                viewModel.adminUpdatePrice("price_7d", p7d)
                viewModel.adminUpdatePrice("price_30d", p30d)
                viewModel.adminUpdatePrice("price_admin", pAdmin)
            }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    RolexCard {
        Text(text = "CONFIGURE SUPPORT & PAYMENT CONTACTS", color = RolexGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = upi,
            onValueChange = { upi = it },
            label = { Text("Administrator UPI VPA") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = whatsapp,
            onValueChange = { whatsapp = it },
            label = { Text("Support WhatsApp Number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = telegram,
            onValueChange = { telegram = it },
            label = { Text("Support Telegram Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        RolexSecondaryButton(
            text = "UPDATE CONTACT DETAILS",
            onClick = {
                viewModel.adminUpdateSetting("admin_upi_id", upi)
                viewModel.adminUpdateSetting("support_whatsapp", whatsapp)
                viewModel.adminUpdateSetting("support_telegram", telegram)
            }
        )
    }
}

@Composable
private fun TabAuditLogs(
    auditLogs: List<com.example.models.AuditLog>,
    dateFormat: SimpleDateFormat
) {
    Text(
        text = "ADMINISTRATIVE AUDIT LOGS (${auditLogs.size})",
        color = RolexGold,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))

    auditLogs.forEach { log ->
        RolexCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = log.action, color = RolexGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = dateFormat.format(Date(log.timestamp)), color = TextTertiary, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = log.details, color = TextPrimary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Actor: ${log.actor}", color = TextSecondary, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}
