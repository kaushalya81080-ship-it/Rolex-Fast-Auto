package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simulator.CoinType
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
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CarromSimulatorScreen(
    viewModel: RolexViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val simTick by viewModel.simulatorTick.collectAsState()
    val calculatedAim by viewModel.calculatedAim.collectAsState()
    val engine = viewModel.simulatorEngine

    val whitePocketed = engine.whitePocketedCount
    val blackPocketed = engine.blackPocketedCount
    val queenPocketed = engine.queenPocketed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
            .verticalScroll(rememberScrollState())
    ) {
        RolexHeader(
            title = "CARROM SIMULATOR",
            subtitle = "Miniclip Carrom Pool Auto-Aim Trajectory Engine",
            onAdminClick = { onNavigate(AppScreen.ADMIN_PANEL) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkCard)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚪ White: $whitePocketed", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "⚫ Black: $blackPocketed", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "🔴 Queen: ${if (queenPocketed) "Pocketed" else "Active"}", color = if (queenPocketed) RolexGold else Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = RolexGreen.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (engine.isMoving) "ROLLING" else "READY",
                        color = if (engine.isMoving) RolexGold else StatusActive,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The Interactive Carrom Board Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2A1C0E)) // Mahogany wooden frame
                    .padding(10.dp)
                    .testTag("carrom_canvas_board")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEADBBE)) // Traditional Carrom board plywood surface
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val normX = offset.x / size.width
                                val normY = offset.y / size.height
                                engine.setAimDirection(normX, normY)
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val normX = change.position.x / size.width
                                val normY = change.position.y / size.height
                                if (normY > 0.75f) {
                                    engine.setStrikerBaselinePosition(normX)
                                } else {
                                    engine.setAimDirection(normX, normY)
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw 4 Corner Pockets
                    for (pocket in engine.pockets) {
                        val px = pocket.first * w
                        val py = pocket.second * h
                        val r = engine.pocketRadius * w

                        // Pocket outer border
                        drawCircle(Color(0xFF5A5046), radius = r, center = Offset(px, py))
                        // Pocket hole
                        drawCircle(Color(0xFF141414), radius = r * 0.85f, center = Offset(px, py))
                    }

                    // 2. Draw Baseline and Arrows
                    val baselineY = 0.82f * h
                    drawLine(
                        color = Color(0xFF6B5848),
                        start = Offset(0.22f * w, baselineY),
                        end = Offset(0.78f * w, baselineY),
                        strokeWidth = 2.dp.toPx()
                    )
                    // Baseline inner circle indicators
                    drawCircle(Color(0xFF6B5848), radius = 0.03f * w, center = Offset(0.22f * w, baselineY), style = Stroke(1.5f))
                    drawCircle(Color(0xFF6B5848), radius = 0.03f * w, center = Offset(0.78f * w, baselineY), style = Stroke(1.5f))

                    // 3. Draw Center Circles
                    val centerX = 0.5f * w
                    val centerY = 0.5f * h
                    drawCircle(Color(0xFF8C3224), radius = 0.08f * w, center = Offset(centerX, centerY), style = Stroke(1.5.dp.toPx()))
                    drawCircle(Color(0xFF6B5848), radius = 0.16f * w, center = Offset(centerX, centerY), style = Stroke(1.5.dp.toPx()))

                    // 4. Draw Trajectory Guideline if not moving
                    if (!engine.isMoving) {
                        val sx = engine.striker.x * w
                        val sy = engine.striker.y * h
                        val lineLen = 0.55f * w
                        val endX = sx + cos(engine.aimAngleRad) * lineLen
                        val endY = sy + sin(engine.aimAngleRad) * lineLen

                        // Rolex Gold Laser Trajectory
                        drawLine(
                            color = RolexGold,
                            start = Offset(sx, sy),
                            end = Offset(endX, endY),
                            strokeWidth = 2.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )

                        // If auto-aim result available, draw connection to target pocket
                        calculatedAim?.let { aim ->
                            val tpX = aim.targetPocketX * w
                            val tpY = aim.targetPocketY * h
                            val targetCoin = engine.coins.firstOrNull { it.id == aim.targetCoinId }
                            if (targetCoin != null) {
                                drawLine(
                                    color = Color(0xFF00E676),
                                    start = Offset(targetCoin.x * w, targetCoin.y * h),
                                    end = Offset(tpX, tpY),
                                    strokeWidth = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                                )
                                // Target lock ring
                                drawCircle(
                                    color = Color(0xFF00E676),
                                    radius = (targetCoin.radius + 0.012f) * w,
                                    center = Offset(targetCoin.x * w, targetCoin.y * h),
                                    style = Stroke(2.dp.toPx())
                                )
                            }
                        }
                    }

                    // 5. Draw Carrom Coins
                    for (coin in engine.coins) {
                        if (coin.isPocketed) continue
                        val cx = coin.x * w
                        val cy = coin.y * h
                        val cr = coin.radius * w

                        when (coin.type) {
                            CoinType.WHITE -> {
                                drawCircle(Color(0xFFFAF7F0), radius = cr, center = Offset(cx, cy))
                                drawCircle(Color(0xFFC7BBAA), radius = cr * 0.7f, center = Offset(cx, cy), style = Stroke(1.5f))
                            }
                            CoinType.BLACK -> {
                                drawCircle(Color(0xFF222428), radius = cr, center = Offset(cx, cy))
                                drawCircle(Color(0xFF424855), radius = cr * 0.7f, center = Offset(cx, cy), style = Stroke(1.5f))
                            }
                            CoinType.QUEEN -> {
                                drawCircle(Color(0xFFD32F2F), radius = cr, center = Offset(cx, cy))
                                drawCircle(Color(0xFFFFD700), radius = cr * 0.65f, center = Offset(cx, cy), style = Stroke(2f))
                            }
                            else -> {}
                        }
                    }

                    // 6. Draw Striker
                    if (!engine.striker.isPocketed) {
                        val sx = engine.striker.x * w
                        val sy = engine.striker.y * h
                        val sr = engine.striker.radius * w

                        // Gold metallic striker with inner ring
                        drawCircle(Color(0xFFFFD700), radius = sr, center = Offset(sx, sy))
                        drawCircle(Color(0xFFB8860B), radius = sr * 0.82f, center = Offset(sx, sy), style = Stroke(2.dp.toPx()))
                        drawCircle(Color(0xFF13151B), radius = sr * 0.35f, center = Offset(sx, sy))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Auto-Aim Calculation & Controls
            RolexCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUTOPLAY TRAJECTORY ENGINE",
                        color = RolexGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (calculatedAim != null) {
                        Text(
                            text = "Aim Accuracy: ${calculatedAim?.pocketConfidencePct}%",
                            color = StatusActive,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Striker Position Slider
                Text(
                    text = "Striker Position (Baseline)",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Slider(
                    value = engine.striker.x,
                    onValueChange = { engine.setStrikerBaselinePosition(it) },
                    valueRange = 0.22f..0.78f,
                    enabled = !engine.isMoving,
                    colors = SliderDefaults.colors(
                        thumbColor = RolexGold,
                        activeTrackColor = RolexGold,
                        inactiveTrackColor = DarkBorder
                    ),
                    modifier = Modifier.testTag("striker_position_slider")
                )

                // Power Slider
                Text(
                    text = "Shot Power: ${(engine.shotPower * 100).toInt()}%",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Slider(
                    value = engine.shotPower,
                    onValueChange = { engine.shotPower = it },
                    valueRange = 0.3f..1.0f,
                    enabled = !engine.isMoving,
                    colors = SliderDefaults.colors(
                        thumbColor = RolexGreen,
                        activeTrackColor = RolexGreen,
                        inactiveTrackColor = DarkBorder
                    ),
                    modifier = Modifier.testTag("shot_power_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    RolexSecondaryButton(
                        text = "⚡ AUTO-CALCULATE AIM",
                        onClick = { viewModel.simulatorAutoAim() },
                        modifier = Modifier.weight(1f),
                        enabled = !engine.isMoving,
                        testTag = "auto_aim_btn"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    RolexPrimaryButton(
                        text = "🎯 STRIKE DISC",
                        onClick = { viewModel.simulatorFire() },
                        modifier = Modifier.weight(1f),
                        enabled = !engine.isMoving,
                        testTag = "strike_disc_btn"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                RolexSecondaryButton(
                    text = "🔄 RESET BOARD",
                    onClick = { viewModel.simulatorReset() },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "reset_board_btn"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Safety & Terms Disclaimer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "⚖️ COMPLIANCE & SIMULATOR NOTICE",
                        color = RolexGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This Carrom Simulator runs on the official Rolex trajectory physics engine. It provides legal, mathematically accurate angle calculations and reflection modeling without modifying third-party game code or memory.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
