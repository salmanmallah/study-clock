package com.ghost.fullscreenclock

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ghost.fullscreenclock.model.ClockMode
import com.ghost.fullscreenclock.preferences.ClockPreferences
import com.ghost.fullscreenclock.theme.FullscreenClockTheme
import com.ghost.fullscreenclock.ui.ClockApp

class MainActivity : ComponentActivity() {

    private lateinit var prefs: ClockPreferences
    private var insetsController: WindowInsetsControllerCompat? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission granted or denied; handled gracefully
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = ClockPreferences(this)

        // Set initial orientation based on saved mode before first layout pass to prevent jarring jump
        val initialMode = try {
            val saved = ClockMode.valueOf(prefs.lastMode)
            if (saved == ClockMode.SETTINGS) ClockMode.DIGITAL_24 else saved
        } catch (_: Exception) {
            ClockMode.DIGITAL_24
        }
        requestedOrientation = if (initialMode.isLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // Enable modern Edge-to-Edge
        enableEdgeToEdge()

        // Configure Display Cutout to draw into notches/cutouts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // Apply Screen-On Flag if enabled in preferences
        updateKeepScreenOn(prefs.keepScreenOn)

        // Setup Immersive Sticky Mode using WindowInsetsControllerCompat
        WindowCompat.setDecorFitsSystemWindows(window, false)
        insetsController = WindowCompat.getInsetsController(window, window.decorView)?.apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        // Request Notification permission for Timer/Pomodoro background alerts on Android 13+
        requestNotificationPermission()

        setContent {
            FullscreenClockTheme {
                ClockApp(
                    prefs = prefs,
                    onKeepScreenOnChanged = { enabled ->
                        updateKeepScreenOn(enabled)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-apply immersive fullscreen when activity resumes
        insetsController?.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun updateKeepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
