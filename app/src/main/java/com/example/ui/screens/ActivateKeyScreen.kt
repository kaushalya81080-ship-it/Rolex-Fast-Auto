package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.LicenseStatus
import com.example.ui.AppScreen
import com.example.ui.RolexViewModel
import com.example.ui.components.RolexCard
import com.example.ui.components.RolexHeader
import com.example.ui.components.RolexPrimaryButton
import com.example.ui.components.RolexSecondaryButton
import com.example.ui.components.StatusPill
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.OnyxBlack
import com.example.ui.theme.RolexGold
import com.example.ui.theme.RolexGreen
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusPending
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivateKeyScreen(
    viewModel: RolexViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    var keyInput by remember { mutableStateOf("") }
    val activationMessage by viewModel.activationMessage.collectAsState()
    val isError by viewModel.activationIsError.collectAsState()

    val activeAutoPlay by viewModel.activeAutoPlayLicense.collectAsState()
    val activeAdmin by viewModel.activeAdminLicense.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
            .verticalScroll(rememberScrollState())
    ) {
        RolexHeader(
            title = "ACTIVATE LICENSE",
            subtitle = "AutoPlay & Admin Panel Key Engine",
            onAdminClick = { onNavigate(AppScreen.ADMIN_PANEL) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Key Entry Card
            RolexCard(hasGoldBorder = true) {
                Text(
                    text = "ENTER ACTIVATION KEY",
                    color = RolexGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Paste your license key below. Keys must start with RFAX-PLAY (AutoPlay) or RFAX-ADM (Admin Panel).",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it.uppercase() },
                    placeholder = { Text("e.g. RFAX-PLAY-2026-GOLD") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activation_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RolexGold,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = RolexGold,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    RolexSecondaryButton(
                        text = "📋 PASTE CLIPBOARD",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipboard.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                keyInput = clip.getItemAt(0).text.toString().trim().uppercase()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "paste_key_btn"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    RolexPrimaryButton(
                        text = "ACTIVATE KEY",
                        onClick = {
                            if (keyInput.isNotBlank()) {
                                viewModel.activateLicenseKey(keyInput)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = keyInput.isNotBlank(),
                        testTag = "submit_activate_btn"
                    )
                }

                if (activationMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isError) StatusExpired.copy(alpha = 0.15f) else StatusActive.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isError) StatusExpired else StatusActive),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = activationMessage ?: "",
                            color = if (isError) StatusExpired else StatusActive,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Currently Active AutoPlay License Details
            Text(
                text = "ACTIVE AUTOPLAY STATUS",
                color = RolexGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (activeAutoPlay != null) {
                RolexCard(hasGoldBorder = true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "LICENSE TYPE", color = TextTertiary, fontSize = 10.sp)
                            Text(text = "Carrom Pool AutoPlay", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        StatusPill(status = activeAutoPlay?.status ?: LicenseStatus.ACTIVE)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "KEY STRING", color = TextTertiary, fontSize = 10.sp)
                    Text(text = activeAutoPlay?.licenseKey ?: "", color = TextGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "TIME REMAINING", color = TextTertiary, fontSize = 10.sp)
                            Text(text = activeAutoPlay?.remainingTimeFormatted ?: "—", color = RolexGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "EXPIRES AT", color = TextTertiary, fontSize = 10.sp)
                            val exp = activeAutoPlay?.expiresAt
                            Text(text = if (exp != null) dateFormat.format(Date(exp)) else "—", color = TextPrimary, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Device Lock: ${activeAutoPlay?.deviceFingerprint ?: "Bound to current device"}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            } else {
                RolexCard {
                    Text(
                        text = "No active AutoPlay license. You can purchase a 1D, 2D, 3D, 7D, or 30D pass from the Buy Key page.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RolexSecondaryButton(
                        text = "GO TO BUY KEY",
                        onClick = { onNavigate(AppScreen.BUY_KEY) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Currently Active Admin Panel License Details
            Text(
                text = "ACTIVE ADMIN PANEL STATUS",
                color = RolexGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (activeAdmin != null) {
                RolexCard(hasGoldBorder = true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "ADMIN ACCESS", color = TextTertiary, fontSize = 10.sp)
                            Text(text = "Rolex Admin License", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        StatusPill(status = activeAdmin?.status ?: LicenseStatus.ACTIVE)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "KEY: ${activeAdmin?.licenseKey}", color = TextGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    RolexPrimaryButton(
                        text = "OPEN ADMINISTRATOR DASHBOARD",
                        onClick = { onNavigate(AppScreen.ADMIN_PANEL) },
                        testTag = "open_admin_panel_btn"
                    )
                }
            } else {
                RolexCard {
                    Text(
                        text = "No active Admin Panel license. Initial Admin Panel setup cost is ₹500.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
