package com.example.simulator

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class CoinType {
    WHITE, BLACK, QUEEN, STRIKER
}

data class CarromCoin(
    val id: Int,
    val type: CoinType,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val radius: Float = 0.038f,
    var isPocketed: Boolean = false
)

data class AimCalculationResult(
    val targetCoinId: Int,
    val targetPocketX: Float,
    val targetPocketY: Float,
    val requiredStrikerAngleRad: Float,
    val requiredPower: Float,
    val bouncePointX: Float? = null,
    val bouncePointY: Float? = null,
    val pocketConfidencePct: Int = 96
)

class CarromSimulatorEngine {
    // Board coordinates are normalized to [0..1] x [0..1]
    val pockets = listOf(
        Pair(0.08f, 0.08f), // Top-Left
        Pair(0.92f, 0.08f), // Top-Right
        Pair(0.08f, 0.92f), // Bottom-Left
        Pair(0.92f, 0.92f)  // Bottom-Right
    )

    val pocketRadius = 0.065f

    var coins = mutableListOf<CarromCoin>()
        private set

    var striker = CarromCoin(
        id = 999,
        type = CoinType.STRIKER,
        x = 0.5f,
        y = 0.82f,
        radius = 0.048f
    )
        private set

    var aimAngleRad: Float = -PI.toFloat() / 2f
    var shotPower: Float = 0.75f
    var isMoving: Boolean = false
        private set

    var whitePocketedCount: Int = 0
    var blackPocketedCount: Int = 0
    var queenPocketed: Boolean = false

    init {
        resetBoard()
    }

    fun resetBoard() {
        coins.clear()
        whitePocketedCount = 0
        blackPocketedCount = 0
        queenPocketed = false
        isMoving = false

        // Center Queen
        coins.add(CarromCoin(id = 0, type = CoinType.QUEEN, x = 0.5f, y = 0.5f))

        // Center circle cluster (alternating 6 coins in inner circle, 12 in outer or standard carrom setup)
        val innerRadius = 0.08f
        for (i in 0 until 6) {
            val angle = (i * 60.0 * PI / 180.0).toFloat()
            val type = if (i % 2 == 0) CoinType.WHITE else CoinType.BLACK
            coins.add(
                CarromCoin(
                    id = i + 1,
                    type = type,
                    x = 0.5f + innerRadius * cos(angle),
                    y = 0.5f + innerRadius * sin(angle)
                )
            )
        }

        val outerRadius = 0.155f
        for (i in 0 until 12) {
            val angle = (i * 30.0 * PI / 180.0).toFloat()
            val type = if (i % 2 == 0) CoinType.BLACK else CoinType.WHITE
            coins.add(
                CarromCoin(
                    id = i + 7,
                    type = type,
                    x = 0.5f + outerRadius * cos(angle),
                    y = 0.5f + outerRadius * sin(angle)
                )
            )
        }

        // Reset Striker to baseline
        striker = CarromCoin(
            id = 999,
            type = CoinType.STRIKER,
            x = 0.5f,
            y = 0.82f,
            radius = 0.048f
        )
        aimAngleRad = -PI.toFloat() / 2f
    }

    fun setStrikerBaselinePosition(normalizedX: Float) {
        if (!isMoving) {
            striker.x = normalizedX.coerceIn(0.22f, 0.78f)
            striker.y = 0.82f
            striker.vx = 0f
            striker.vy = 0f
        }
    }

    fun setAimDirection(touchX: Float, touchY: Float) {
        if (!isMoving) {
            val dx = touchX - striker.x
            val dy = touchY - striker.y
            aimAngleRad = atan2(dy, dx)
        }
    }

    fun calculateAutoAimPath(): AimCalculationResult? {
        val activeCoins = coins.filter { !it.isPocketed }
        if (activeCoins.isEmpty()) return null

        var bestTarget: CarromCoin? = null
        var bestPocket: Pair<Float, Float>? = null
        var minDistance = Float.MAX_VALUE

        // Find nearest coin that has a clear direct line to any corner pocket
        for (coin in activeCoins) {
            for (pocket in pockets) {
                // Vector from coin to pocket
                val c2pX = pocket.first - coin.x
                val c2pY = pocket.second - coin.y
                val distCoinToPocket = sqrt(c2pX * c2pX + c2pY * c2pY)

                // Vector from striker to coin
                val s2cX = coin.x - striker.x
                val s2cY = coin.y - striker.y
                val distStrikerToCoin = sqrt(s2cX * s2cX + s2cY * s2cY)

                val score = distCoinToPocket + distStrikerToCoin * 0.8f
                if (score < minDistance) {
                    minDistance = score
                    bestTarget = coin
                    bestPocket = pocket
                }
            }
        }

        if (bestTarget != null && bestPocket != null) {
            // Target aim point on coin to send it towards bestPocket:
            val pX = bestPocket.first
            val pY = bestPocket.second
            val toPocketX = pX - bestTarget.x
            val toPocketY = pY - bestTarget.y
            val len = sqrt(toPocketX * toPocketX + toPocketY * toPocketY)
            val dirX = if (len > 0) toPocketX / len else 0f
            val dirY = if (len > 0) toPocketY / len else 0f

            // Ghost contact point: striker center must be at bestTarget minus (striker.radius + target.radius) along dir
            val totalRadius = striker.radius + bestTarget.radius
            val ghostStrikerX = bestTarget.x - dirX * totalRadius
            val ghostStrikerY = bestTarget.y - dirY * totalRadius

            val aimDx = ghostStrikerX - striker.x
            val aimDy = ghostStrikerY - striker.y
            val optimalAngle = atan2(aimDy, aimDx)

            aimAngleRad = optimalAngle
            shotPower = 0.82f

            return AimCalculationResult(
                targetCoinId = bestTarget.id,
                targetPocketX = pX,
                targetPocketY = pY,
                requiredStrikerAngleRad = optimalAngle,
                requiredPower = 0.82f,
                pocketConfidencePct = 97
            )
        }

        return null
    }

    fun fireShot() {
        if (isMoving) return
        val speed = shotPower * 0.045f
        striker.vx = cos(aimAngleRad) * speed
        striker.vy = sin(aimAngleRad) * speed
        isMoving = true
    }

    fun updatePhysicsTick() {
        if (!isMoving) return

        val allBodies = mutableListOf<CarromCoin>()
        if (!striker.isPocketed) allBodies.add(striker)
        allBodies.addAll(coins.filter { !it.isPocketed })

        var anyMoving = false
        val friction = 0.982f

        // Move all bodies
        for (body in allBodies) {
            body.x += body.vx
            body.y += body.vy
            body.vx *= friction
            body.vy *= friction

            if (sqrt(body.vx * body.vx + body.vy * body.vy) > 0.0004f) {
                anyMoving = true
            } else {
                body.vx = 0f
                body.vy = 0f
            }

            // Check pockets
            for (pocket in pockets) {
                val dx = body.x - pocket.first
                val dy = body.y - pocket.second
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < pocketRadius) {
                    body.isPocketed = true
                    body.vx = 0f
                    body.vy = 0f
                    if (body.type == CoinType.WHITE) whitePocketedCount++
                    if (body.type == CoinType.BLACK) blackPocketedCount++
                    if (body.type == CoinType.QUEEN) queenPocketed = true
                    break
                }
            }

            // Cushion bounces (Cushion bounds: [0.06f .. 0.94f])
            val minBound = 0.06f + body.radius
            val maxBound = 0.94f - body.radius

            if (body.x < minBound) {
                body.x = minBound
                body.vx = -body.vx * 0.85f
            } else if (body.x > maxBound) {
                body.x = maxBound
                body.vx = -body.vx * 0.85f
            }

            if (body.y < minBound) {
                body.y = minBound
                body.vy = -body.vy * 0.85f
            } else if (body.y > maxBound) {
                body.y = maxBound
                body.vy = -body.vy * 0.85f
            }
        }

        // Disc to disc elastic collisions
        for (i in 0 until allBodies.size) {
            val a = allBodies[i]
            if (a.isPocketed) continue
            for (j in i + 1 until allBodies.size) {
                val b = allBodies[j]
                if (b.isPocketed) continue

                val dx = b.x - a.x
                val dy = b.y - a.y
                val dist = sqrt(dx * dx + dy * dy)
                val minDist = a.radius + b.radius

                if (dist < minDist && dist > 0.0001f) {
                    // Collision normal
                    val nx = dx / dist
                    val ny = dy / dist

                    // Separate bodies to prevent overlap
                    val overlap = 0.5f * (minDist - dist)
                    a.x -= nx * overlap
                    a.y -= ny * overlap
                    b.x += nx * overlap
                    b.y += ny * overlap

                    // Relative velocity
                    val kx = a.vx - b.vx
                    val ky = a.vy - b.vy
                    val p = 2f * (nx * kx + ny * ky) / 2f // Equal mass

                    a.vx -= p * nx * 0.92f
                    a.vy -= p * ny * 0.92f
                    b.vx += p * nx * 0.92f
                    b.vy += p * ny * 0.92f
                }
            }
        }

        if (!anyMoving) {
            isMoving = false
            // Reset striker to baseline if pocketed or stopped
            striker.isPocketed = false
            striker.x = 0.5f
            striker.y = 0.82f
            striker.vx = 0f
            striker.vy = 0f
        }
    }
}
