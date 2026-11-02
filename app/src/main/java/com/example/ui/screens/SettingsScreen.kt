package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.AuthProvider
import com.example.ui.AppScreen
import com.example.ui.RolexViewModel
import com.example.ui.components.RolexCard
import com.example.ui.components.RolexHeader
import com.example.ui.components.RolexPrimaryButton
import com.example.ui.components.RolexSecondaryButton
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
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
fun SettingsScreen(
    viewModel: RolexViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    var showAuthDialog by remember { mutableStateOf(false) }
    var showSchemaDialog by remember { mutableStateOf(false) }

    val isOverlayAllowed = viewModel.isOverlayPermissionGranted(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
            .verticalScroll(rememberScrollState())
    ) {
        RolexHeader(
            title = "SETTINGS & SUPPORT",
            subtitle = "Rolex Fast Auto Configuration & Profile",
            onAdminClick = { onNavigate(AppScreen.ADMIN_PANEL) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // User Session Card
            Text(
                text = "USER AUTHENTICATION",
                color = RolexGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            RolexCard(hasGoldBorder = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(RolexGoldDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👤", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.displayName,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser.email ?: "Guest ID: ${currentUser.id}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Surface(
                        color = RolexGreen.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, RolexGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = currentUser.authProvider.displayName,
                            color = RolexGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                RolexSecondaryButton(
                    text = "SWITCH / CHANGE AUTHENTICATION METHOD",
                    onClick = { showAuthDialog = true },
                    modifier = Modifier.height(42.dp),
                    testTag = "switch_auth_btn"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Android Permissions
            Text(
                text = "SYSTEM PERMISSIONS",
                color = RolexGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            RolexCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Display Over Other Apps",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Required for the floating control interface over Carrom Pool",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Surface(
                        color = if (isOverlayAllowed) StatusActive.copy(alpha = 0.2f) else StatusExpired.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isOverlayAllowed) "GRANTED" else "DENIED",
                            color = if (isOverlayAllowed) StatusActive else StatusExpired,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                RolexSecondaryButton(
                    text = "MANAGE OVERLAY PERMISSION IN SYSTEM SETTINGS",
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.height(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Developer & Database Schema
            Text(
                text = "BACKEND & DATABASE (SUPABASE)",
                color = RolexGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            RolexCard {
                Text(
                    text = "PostgreSQL & Supabase Architecture",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Complete database schema with 8 tables (profiles, admin_roles, admin_licenses, autoplay_licenses, payment_transactions, license_activations, application_settings, audit_logs) and Row Level Security (RLS) policies.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                RolexSecondaryButton(
                    text = "VIEW BACKEND SCHEMA / DDL",
                    onClick = { showSchemaDialog = true },
                    modifier = Modifier.height(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Info Card
            RolexCard {
                Text(
                    text = "ROLEX FAST AUTO v1.0.0",
                    color = RolexGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Engineered exclusively for Miniclip's Carrom Pool: Disc Game.\nNo support for 8 Ball Pool or third-party games.\nCompliant with Google Play & Fair-Play developer guidelines.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Switch Auth Dialog
    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Text(text = "Select Authentication Method", color = RolexGold, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(text = "Choose your supported login method for Rolex Fast Auto:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    AuthProvider.values().forEach { provider ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setAuthProvider(provider)
                                    showAuthDialog = false
                                }
                                .padding(vertical = 4.dp),
                            color = if (currentUser.authProvider == provider) RolexGreen.copy(alpha = 0.3f) else DarkSurface,
                            border = BorderStroke(1.dp, if (currentUser.authProvider == provider) RolexGold else DarkBorder)
                        ) {
                            Text(
                                text = "• ${provider.displayName}",
                                color = if (currentUser.authProvider == provider) RolexGold else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text("Close", color = RolexGold)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Schema Dialog
    if (showSchemaDialog) {
        AlertDialog(
            onDismissRequest = { showSchemaDialog = false },
            title = {
                Text(text = "Supabase Database Schema", color = RolexGold, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "The complete schema file has been written to supabase_schema.sql in the project root.\n\nIncluded Tables:\n1. profiles (UUID, email, is_admin)\n2. admin_roles (Role Based Access Control)\n3. admin_licenses (Setup cost ₹500, status, expiry)\n4. autoplay_licenses (1D, 2D, 3D, 7D, 30D, custom duration)\n5. payment_transactions (UTR, amount, status, issued key)\n6. license_activations (Device binding & session tokens)\n7. application_settings (Pricing & support contacts)\n8. audit_logs (Security & privileged action logging)\n\nRow-Level Security (RLS) is enabled on all 8 tables.",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Schema Info", "See supabase_schema.sql in project root."))
                        Toast.makeText(context, "Schema path copied", Toast.LENGTH_SHORT).show()
                        showSchemaDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RolexGold, contentColor = OnyxBlack)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
