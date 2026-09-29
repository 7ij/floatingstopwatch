package com.floatwatch.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.floatwatch.app.FloatWatchApp
import com.floatwatch.app.R
import com.floatwatch.app.data.OverlayTheme
import com.floatwatch.app.data.PreferencesManager
import com.floatwatch.app.engine.StopwatchEngine
import com.floatwatch.app.ui.MainActivity
import com.floatwatch.app.ui.overlay.FloatingStopwatchOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StopwatchOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null

    private lateinit var prefsManager: PreferencesManager

    companion object {
        const val ACTION_START = "ACTION_START_OVERLAY"
        const val ACTION_STOP = "ACTION_STOP_OVERLAY"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, StopwatchOverlayService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, StopwatchOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        prefsManager = PreferencesManager(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        setupOverlayWindow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification())
            }
        }
        return START_STICKY
    }

    private fun setupOverlayWindow() {
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        windowLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@StopwatchOverlayService)
            setViewTreeSavedStateRegistryOwner(this@StopwatchOverlayService)
            setContent {
                val themeState by prefsManager.selectedThemeFlow.collectAsState(initial = OverlayTheme.MINIMAL_DARK)
                val opacityState by prefsManager.opacityFlow.collectAsState(initial = 0.95f)
                val isProState by prefsManager.isProUserFlow.collectAsState(initial = false)
                val hapticState by prefsManager.hapticEnabledFlow.collectAsState(initial = true)

                FloatingStopwatchOverlay(
                    theme = themeState,
                    opacity = opacityState,
                    isProUser = isProState,
                    hapticsEnabled = hapticState,
                    onDragDelta = { dx, dy ->
                        windowLayoutParams?.let { params ->
                            params.x += dx.toInt()
                            params.y += dy.toInt()
                            windowManager?.updateViewLayout(this@apply, params)
                        }
                    },
                    onCloseClicked = {
                        stopSelf()
                    }
                )
            }
        }

        try {
            windowManager?.addView(composeView, windowLayoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, StopwatchOverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, FloatWatchApp.CHANNEL_ID)
            .setContentTitle("FloatWatch Running")
            .setContentText("Tap to open settings, or tap dismiss to close overlay")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(R.mipmap.ic_launcher, "Close Overlay", stopPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel()

        if (composeView != null && windowManager != null) {
            try {
                windowManager?.removeView(composeView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            composeView = null
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
