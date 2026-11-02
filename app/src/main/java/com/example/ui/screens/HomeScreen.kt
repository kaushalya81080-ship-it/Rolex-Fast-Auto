package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.LicenseStatus
import com.example.service.FloatingOverlayService
import com.example.ui.AppScreen
import com.example.ui.RolexViewModel
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
import com.example.ui.theme.RolexGoldDark
import com.example.ui.theme.RolexGreen
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun HomeScreen(
    viewModel: RolexViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    val activeAutoPlay by viewModel.activeAutoPlayLicense.collectAsState()
    val activeAdmin by viewModel.activeAdminLicense.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showPermissionRationale by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
            .verticalScroll(rememberScrollState())
    ) {
        RolexHeader(
            title = "ROLEX FAST AUTO",
            subtitle = "Target: Miniclip Carrom Pool: Disc Game ONLY",
            activeKey = activeAutoPlay?.licenseKey,
            onAdminClick = { onNavigate(AppScreen.ADMIN_PANEL) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Target Game Assurance Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = RolexGreen.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, RolexGreen)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎯", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Miniclip Carrom Pool: Disc Game",
                            color = RolexGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Exclusively engineered for Carrom Pool discs & boards. No 8 Ball Pool or third-party game support.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AutoPlay License Status Card
            RolexCard(hasGoldBorder = activeAutoPlay != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AUTOPLAY LICENSE STATUS",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (activeAutoPlay != null) "Active & Authorized" else "No Active License",
                            color = if (activeAutoPlay != null) StatusActive else StatusExpired,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    StatusPill(status = activeAutoPlay?.status ?: LicenseStatus.UNACTIVATED)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activeAutoPlay != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "TIME REMAINING", color = TextTertiary, fontSize = 10.sp)
                            Text(
                                text = activeAutoPlay?.remainingTimeFormatted ?: "—",
                                color = RolexGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "PLAN DURATION", color = TextTertiary, fontSize = 10.sp)
                            Text(
                                text = "${activeAutoPlay?.durationDays} Days Plan",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Activate an AutoPlay key or purchase a plan to unlock floating controls, trajectory calculations, and the Carrom assistant.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        RolexPrimaryButton(
                            text = "BUY KEY NOW",
                            onClick = { onNavigate(AppScreen.BUY_KEY) },
                            modifier = Modifier.weight(1f),
                            testTag = "home_buy_key_btn"
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        RolexSecondaryButton(
                            text = "ACTIVATE",
                            onClick = { onNavigate(AppScreen.ACTIVATE_KEY) },
                            modifier = Modifier.weight(1f),
                            testTag = "home_activate_btn"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fast Launch Controls
            Text(
                text = "FAST TOOLS & ASSISTANT",
                color = RolexGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Action 1: Launch Floating Overlay
            RolexCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(RolexGreen.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🪟", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Floating Controls Overlay",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Display companion controls over Miniclip Carrom Pool.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                RolexPrimaryButton(
                    text = if (FloatingOverlayService.isServiceRunning) "STOP FLOATING OVERLAY" else "LAUNCH FLOATING OVERLAY",
                    onClick = {
                        if (FloatingOverlayService.isServiceRunning) {
                            val intent = Intent(context, FloatingOverlayService::class.java)
                            context.stopService(intent)
                        } else {
                            if (viewModel.isOverlayPermissionGranted(context)) {
                                val intent = Intent(context, FloatingOverlayService::class.java)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent)
                                } else {
                                    context.startService(intent)
                                }
                            } else {
                                showPermissionRationale = true
                            }
                        }
                    },
                    testTag = "launch_overlay_btn"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action 2: Carrom Simulator
            RolexCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(RolexGoldDark.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎱", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Carrom Physics & Aim Simulator",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Interactive board with automated aim angles and pocket reflections.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                RolexSecondaryButton(
                    text = "OPEN CARROM SIMULATOR",
                    onClick = { onNavigate(AppScreen.CARROM_SIMULATOR) },
                    testTag = "open_simulator_btn"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action 3: Account Compatibility Guide
            RolexCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔐", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Carrom Pool Account Safety",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Official Google, Facebook, & Guest account binding instructions.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                RolexSecondaryButton(
                    text = "VIEW ACCOUNT COMPATIBILITY",
                    onClick = { onNavigate(AppScreen.ACCOUNT_COMPATIBILITY) },
                    testTag = "open_account_sync_btn"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Admin Panel Key Separation Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚖️ LICENSING SEPARATION ARCHITECTURE",
                        color = RolexGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Admin Panel Key (₹500 setup) grants administrative controls only and NEVER activates AutoPlay.\n• AutoPlay Keys grant Carrom companion access only and NEVER grant administrative privileges.\n• Server-calculated timestamps protect against device clock manipulation.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Permission Dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = {
                Text(
                    text = "Display Over Other Apps Permission",
                    color = RolexGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Rolex Fast Auto requires Android's 'Display over other apps' permission to show the floating control panel and aim guideline overlay while you play Miniclip Carrom Pool.\n\nNote: The floating overlay does NOT alter or inject into Carrom Pool binaries. It runs as an Android system overlay window.",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationale = false
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RolexGold, contentColor = OnyxBlack)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
