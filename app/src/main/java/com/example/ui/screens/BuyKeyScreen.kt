package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.LicensePlan
import com.example.models.PaymentStatus
import com.example.ui.AppScreen
import com.example.ui.RolexViewModel
import com.example.ui.components.PaymentStatusPill
import com.example.ui.components.PlanSelectionCard
import com.example.ui.components.RolexCard
import com.example.ui.components.RolexHeader
import com.example.ui.components.RolexPrimaryButton
import com.example.ui.components.RolexSecondaryButton
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
import com.example.ui.theme.StatusPending
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun BuyKeyScreen(
    viewModel: RolexViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsState()
    val selectedPlanId by viewModel.selectedPlanId.collectAsState()
    val utrInput by viewModel.paymentUtrInput.collectAsState()
    val paymentStatusMsg by viewModel.paymentStatusMessage.collectAsState()
    val payments by viewModel.allPayments.collectAsState()

    val p1d = settings["price_1d"]?.toIntOrNull() ?: 99
    val p2d = settings["price_2d"]?.toIntOrNull() ?: 180
    val p3d = settings["price_3d"]?.toIntOrNull() ?: 250
    val p7d = settings["price_7d"]?.toIntOrNull() ?: 500
    val p30d = settings["price_30d"]?.toIntOrNull() ?: 1500
    val pAdmin = settings["price_admin"]?.toIntOrNull() ?: 500

    val plans = listOf(
        LicensePlan(
            id = "plan_1d",
            name = "1 Day Trial Key",
            durationDays = 1,
            priceInr = p1d,
            description = "Quick 24-hour access to Carrom Pool AutoPlay assistant.",
            features = listOf("24-Hour validity", "Auto Aim Guide", "Floating Window")
        ),
        LicensePlan(
            id = "plan_2d",
            name = "2 Days Weekend Pass",
            durationDays = 2,
            priceInr = p2d,
            description = "48 hours of continuous aiming assistance.",
            features = listOf("48-Hour validity", "Cushion angle prediction", "Fast auto shots")
        ),
        LicensePlan(
            id = "plan_3d",
            name = "3 Days Standard Pass",
            durationDays = 3,
            priceInr = p3d,
            description = "Perfect for weekend tournaments & league climbing.",
            features = listOf("72-Hour validity", "Full physics calculation", "Priority support")
        ),
        LicensePlan(
            id = "plan_7d",
            name = "7 Days Weekly VIP",
            durationDays = 7,
            priceInr = p7d,
            badge = "POPULAR",
            description = "Full 1-week elite access for Carrom Pool disc masters.",
            features = listOf("7-Day continuous session", "Rebound path reflection", "VIP status badge")
        ),
        LicensePlan(
            id = "plan_30d",
            name = "30 Days Monthly Pro",
            durationDays = 30,
            priceInr = p30d,
            badge = "BEST VALUE",
            description = "30 days unrestricted AutoPlay companion & simulator access.",
            features = listOf("30-Day validity", "All future v1 updates", "Direct WhatsApp hotline")
        ),
        LicensePlan(
            id = "admin_license",
            name = "Admin Panel Setup License",
            durationDays = 365,
            priceInr = pAdmin,
            badge = "ADMIN ACCESS",
            description = "Initial Admin Panel setup key to manage customer keys & pricing.",
            features = listOf("Administrator Dashboard", "Key generation system", "Payment verification center")
        )
    )

    val currentPlan = plans.firstOrNull { it.id == selectedPlanId } ?: plans[3]
    val upiId = settings["admin_upi_id"] ?: "rolexfastauto@icici"
    val whatsappContact = settings["support_whatsapp"] ?: "+91 98765 43210"
    val telegramContact = settings["support_telegram"] ?: "@RolexFastAutoOfficial"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
            .verticalScroll(rememberScrollState())
    ) {
        RolexHeader(
            title = "BUY LICENSE KEY",
            subtitle = "Official Plans & Direct Administrator Payments",
            onAdminClick = { onNavigate(AppScreen.ADMIN_PANEL) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "1. SELECT LICENSE PLAN",
                color = RolexGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            plans.forEach { plan ->
                PlanSelectionCard(
                    plan = plan,
                    isSelected = plan.id == currentPlan.id,
                    onSelect = { viewModel.selectPlan(plan.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Instructions Card
            Text(
                text = "2. PAYMENT INSTRUCTIONS",
                color = RolexGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            RolexCard(hasGoldBorder = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "AMOUNT PAYABLE", color = TextTertiary, fontSize = 11.sp)
                        Text(
                            text = "₹${currentPlan.priceInr}",
                            color = RolexGold,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Surface(
                        color = RolexGreen.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, RolexGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = currentPlan.name,
                            color = RolexGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // UPI VPA Copy Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Admin UPI ID", upiId))
                            Toast.makeText(context, "UPI ID copied: $upiId", Toast.LENGTH_SHORT).show()
                        }
                        .testTag("copy_upi_btn"),
                    color = DarkSurface,
                    border = BorderStroke(1.dp, GoldBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "ADMIN UPI ID (Tap to Copy)", color = TextTertiary, fontSize = 10.sp)
                            Text(text = upiId, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "📋 COPY", color = RolexGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Luxury Stylized QR Code
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            val blockSize = size.width / 21f
                            drawRect(Color.White, Offset.Zero, size)

                            // Corner finders
                            fun drawFinder(x: Float, y: Float) {
                                drawRect(Color.Black, Offset(x * blockSize, y * blockSize), Size(7 * blockSize, 7 * blockSize))
                                drawRect(Color.White, Offset((x + 1) * blockSize, (y + 1) * blockSize), Size(5 * blockSize, 5 * blockSize))
                                drawRect(Color.Black, Offset((x + 2) * blockSize, (y + 2) * blockSize), Size(3 * blockSize, 3 * blockSize))
                            }
                            drawFinder(1f, 1f)
                            drawFinder(13f, 1f)
                            drawFinder(1f, 13f)

                            // Stylized decorative pattern
                            for (r in 0 until 21) {
                                for (c in 0 until 21) {
                                    val isInsideFinder = (r < 8 && c < 8) || (r < 8 && c > 12) || (r > 12 && c < 8)
                                    if (!isInsideFinder && ((r * 7 + c * 13 + currentPlan.priceInr) % 3 == 0)) {
                                        drawRect(
                                            Color.Black,
                                            Offset(c * blockSize, r * blockSize),
                                            Size(blockSize * 0.9f, blockSize * 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scan with GPay / PhonePe / Paytm / BHIM",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Transfer exactly ₹${currentPlan.priceInr} to the Administrator UPI ID above. Note down the 12-digit UTR / Reference ID from your UPI app receipt and submit below.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // UTR Submission Form
            Text(
                text = "3. SUBMIT PAYMENT REFERENCE",
                color = RolexGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            RolexCard {
                OutlinedTextField(
                    value = utrInput,
                    onValueChange = { viewModel.setPaymentUtr(it) },
                    label = { Text("12-Digit UTR / Transaction Ref Number") },
                    placeholder = { Text("e.g. 429182749102") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("utr_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = RolexGold,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = RolexGold,
                        unfocusedLabelColor = TextTertiary
                    )
                )

                if (paymentStatusMsg != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = paymentStatusMsg ?: "",
                        color = StatusActive,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                RolexPrimaryButton(
                    text = "SUBMIT FOR VERIFICATION",
                    onClick = { viewModel.submitCustomerPayment(currentPlan) },
                    testTag = "submit_payment_btn"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment History & Live Status
            Text(
                text = "YOUR SUBMITTED TRANSACTIONS",
                color = RolexGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (payments.isEmpty()) {
                RolexCard {
                    Text(
                        text = "No payments submitted yet. Select a plan and submit your transaction reference above.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            } else {
                payments.forEach { tx ->
                    RolexCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = tx.planName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "UTR: ${tx.utrNumber}", color = TextSecondary, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "₹${tx.amountInr}", color = RolexGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                PaymentStatusPill(status = tx.status)
                            }
                        }

                        if (tx.status == PaymentStatus.VERIFIED && tx.issuedLicenseKey != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, RolexGold, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                color = DarkSurface
                            ) {
                                Column {
                                    Text(text = "ISSUED LICENSE KEY:", color = RolexGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = tx.issuedLicenseKey, color = TextGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    RolexSecondaryButton(
                                        text = "ACTIVATE THIS KEY NOW",
                                        onClick = {
                                            viewModel.activateLicenseKey(tx.issuedLicenseKey)
                                            onNavigate(AppScreen.ACTIVATE_KEY)
                                        },
                                        modifier = Modifier.height(38.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Administrator Support Card
            RolexCard {
                Text(
                    text = "DIRECT ADMINISTRATOR SUPPORT",
                    color = RolexGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "For urgent activation or manual payment verification, contact the administrator directly:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "WhatsApp: $whatsappContact", color = TextPrimary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Telegram: $telegramContact", color = TextPrimary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
