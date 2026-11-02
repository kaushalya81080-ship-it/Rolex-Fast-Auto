package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun AccountCompatibilityScreen(
    viewModel: RolexViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
            .verticalScroll(rememberScrollState())
    ) {
        RolexHeader(
            title = "ACCOUNT COMPATIBILITY",
            subtitle = "Miniclip Carrom Pool Official Sync & Safety Guide",
            onAdminClick = { onNavigate(AppScreen.ADMIN_PANEL) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Strict Security & Zero-Interception Disclosure
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = RolexGreen.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, RolexGreen)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "🛡️", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "OFFICIAL SAFETY PLEDGE",
                            color = RolexGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Rolex Fast Auto NEVER collects, intercepts, or asks for your Carrom Pool passwords, session cookies, or tokens.\n• Signing into Rolex Fast Auto authenticates your license with our server, NOT with Miniclip.\n• Always log in to Carrom Pool through Miniclip's official game app.",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CARROM POOL ACCOUNT TYPES",
                color = RolexGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Google / Gmail Accounts
            RolexCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌐", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Google / Gmail Account", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Most secure synchronization for Android players", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "1. Open Miniclip Carrom Pool.\n2. Tap the Gear/Settings icon on the top right.\n3. Tap 'Sign in with Google Play Games'.\n4. Your progress, coins, and strikers are backed up automatically to Google Cloud.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Facebook Accounts
            RolexCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📘", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Facebook Connected Profile", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Used for friend challenges, leaderboards, and gifts", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "1. In Carrom Pool, select 'Log in with Facebook' on the title screen.\n2. You will be redirected to the official Facebook app or browser OAuth screen.\n3. Grant read permissions. Do not share credentials with any third-party tool.\n4. If disconnected, use Facebook's 'Apps and Websites' settings to re-authorize.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Guest Mode Safeguard
            RolexCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚠️", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Guest Mode (High Risk Warning)", color = TextGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Guest accounts are tied only to local device app cache", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "CRITICAL WARNING: If you play Carrom Pool as a Guest, clearing app data or switching phones will permanently erase your progress.\n\nRecommended Action: Immediately bind your Guest account to a Google or Facebook account inside the official Carrom Pool app before using any companion tools.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Official Game Launch Shortcut
            RolexCard(hasGoldBorder = true) {
                Text(
                    text = "LAUNCH CARROM POOL GAME",
                    color = RolexGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Launch the official installed Miniclip Carrom Pool app on your device:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                RolexPrimaryButton(
                    text = "OPEN MINICLIP CARROM POOL",
                    onClick = {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.miniclip.carrom")
                        if (launchIntent != null) {
                            context.startActivity(launchIntent)
                        } else {
                            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.miniclip.carrom"))
                            context.startActivity(marketIntent)
                        }
                    },
                    testTag = "launch_carrom_pool_app_btn"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
