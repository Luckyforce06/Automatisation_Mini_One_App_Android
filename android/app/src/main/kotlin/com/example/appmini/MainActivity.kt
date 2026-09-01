package com.example.appmini

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.car_automation/system"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "setAutoRotation" -> {
                    val enabled = call.argument<Boolean>("enabled") ?: false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                            data = Uri.parse("package:$packageName")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        result.error("PERMISSION_DENIED", "Permission écriture requise", null)
                    } else {
                        try {
                            Settings.System.putInt(
                                contentResolver,
                                Settings.System.ACCELEROMETER_ROTATION,
                                if (enabled) 1 else 0
                            )
                            result.success(true)
                        } catch (e: Exception) {
                            result.error("UNAVAILABLE", e.message, null)
                        }
                    }
                }
                "setDoNotDisturb" -> {
                    val enabled = call.argument<Boolean>("enabled") ?: false
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        result.error("PERMISSION_DENIED", "Permission DND requise", null)
                    } else {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val filter = if (enabled) {
                                    NotificationManager.INTERRUPTION_FILTER_PRIORITY
                                } else {
                                    NotificationManager.INTERRUPTION_FILTER_ALL
                                }
                                notificationManager.setInterruptionFilter(filter)
                            }
                            result.success(true)
                        } catch (e: Exception) {
                            result.error("UNAVAILABLE", e.message, null)
                        }
                    }
                }
                "playAudio" -> {
                    try {
                        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        val eventTime = SystemClock.uptimeMillis()

                        // Simuler l'appui sur le bouton Play (Down puis Up)
                        val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0)
                        val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0)

                        audioManager.dispatchMediaKeyEvent(downEvent)
                        audioManager.dispatchMediaKeyEvent(upEvent)

                        result.success(true)
                    } catch (e: Exception) {
                        result.error("UNAVAILABLE", e.message, null)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }
}