package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.R

class FloatingOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isExpanded: Boolean = true
    private var isAutoPlayRunning: Boolean = false

    companion object {
        const val CHANNEL_ID = "rolex_overlay_channel"
        const val NOTIFICATION_ID = 1001
        var isServiceRunning = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        initFloatingOverlay()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rolex Fast Auto Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Rolex Fast Auto Floating Control Window"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rolex Fast Auto Active")
            .setContentText("Target: Miniclip Carrom Pool: Disc Game")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFloatingOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        // Programmatic luxury dark & gold UI for the overlay
        val container = FrameLayout(this)
        val density = resources.displayMetrics.density

        // Card background (Dark Onyx with Gold Border)
        val bgDrawable = GradientDrawable().apply {
            setColor(Color.parseColor("#151820"))
            setStroke((2 * density).toInt(), Color.parseColor("#FFD700"))
            cornerRadius = 16 * density
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = bgDrawable
            setPadding((14 * density).toInt(), (10 * density).toInt(), (14 * density).toInt(), (10 * density).toInt())
        }

        // Header Title
        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "ROLEX FAST AUTO"
            setTextColor(Color.parseColor("#FFD700"))
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val minBtn = TextView(this).apply {
            text = " — "
            setTextColor(Color.parseColor("#FFFFFF"))
            textSize = 16f
            setPadding((8 * density).toInt(), 0, (8 * density).toInt(), 0)
        }

        val closeBtn = TextView(this).apply {
            text = " ✕ "
            setTextColor(Color.parseColor("#FF5252"))
            textSize = 14f
            setPadding((8 * density).toInt(), 0, 0, 0)
        }

        titleRow.addView(title, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        titleRow.addView(minBtn)
        titleRow.addView(closeBtn)

        // Subtitle / Target Game indicator
        val targetText = TextView(this).apply {
            text = "Target: Carrom Pool: Disc Game"
            setTextColor(Color.parseColor("#A6ABB8"))
            textSize = 11f
            setPadding(0, (2 * density).toInt(), 0, (6 * density).toInt())
        }

        // Status badge
        val statusText = TextView(this).apply {
            text = "● Status: IDLE"
            setTextColor(Color.parseColor("#FFB300"))
            textSize = 12f
            setPadding(0, 0, 0, (8 * density).toInt())
        }

        // Button row
        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val toggleBtn = Button(this).apply {
            text = "START AUTO"
            setBackgroundColor(Color.parseColor("#006039"))
            setTextColor(Color.WHITE)
            textSize = 12f
        }

        btnRow.addView(toggleBtn, LinearLayout.LayoutParams(0, (38 * density).toInt(), 1f))

        contentLayout.addView(titleRow)
        contentLayout.addView(targetText)
        contentLayout.addView(statusText)
        contentLayout.addView(btnRow)

        // Minimized bubble view
        val bubbleLayout = FrameLayout(this).apply {
            val bubbleBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#151820"))
                setStroke((2 * density).toInt(), Color.parseColor("#FFD700"))
            }
            background = bubbleBg
            layoutParams = FrameLayout.LayoutParams((52 * density).toInt(), (52 * density).toInt())
            visibility = View.GONE

            val crownText = TextView(context).apply {
                text = "👑"
                textSize = 22f
                gravity = Gravity.CENTER
            }
            addView(crownText, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }

        container.addView(contentLayout)
        container.addView(bubbleLayout)

        // Interaction logic
        toggleBtn.setOnClickListener {
            isAutoPlayRunning = !isAutoPlayRunning
            if (isAutoPlayRunning) {
                toggleBtn.text = "STOP AUTO"
                toggleBtn.setBackgroundColor(Color.parseColor("#B71C1C"))
                statusText.text = "● Status: AUTO AIM ACTIVE"
                statusText.setTextColor(Color.parseColor("#00E676"))
            } else {
                toggleBtn.text = "START AUTO"
                toggleBtn.setBackgroundColor(Color.parseColor("#006039"))
                statusText.text = "● Status: IDLE"
                statusText.setTextColor(Color.parseColor("#FFB300"))
            }
        }

        minBtn.setOnClickListener {
            isExpanded = false
            contentLayout.visibility = View.GONE
            bubbleLayout.visibility = View.VISIBLE
        }

        bubbleLayout.setOnClickListener {
            isExpanded = true
            bubbleLayout.visibility = View.GONE
            contentLayout.visibility = View.VISIBLE
        }

        closeBtn.setOnClickListener {
            stopSelf()
        }

        // Draggable touch listener
        container.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        try {
                            windowManager?.updateViewLayout(container, params)
                        } catch (e: Exception) {
                            // Ignored if detached
                        }
                        return false
                    }
                }
                return false
            }
        })

        overlayView = container
        try {
            windowManager?.addView(container, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
