package com.example.appmini

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import android.net.Uri

class BluetoothReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

        val deviceName = device?.name ?: ""
        if (deviceName.contains("MINI", ignoreCase = true)) {
            when (action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    startCarSequence(context)
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    stopCarSequence(context)
                }
            }
        }
    }

    private fun startCarSequence(context: Context) {
        // 1. Activer la rotation et le Ne Pas Déranger
        setAutoRotation(context, true)
        setDoNotDisturb(context, true)

        // 2. Ouvrir Deezer pour initialiser la session audio native
        launchApp(context, "deezer.android.app")

        // 3. Lancer la musique après 2 secondes (temps de chargement de Deezer)
        Handler(Looper.getMainLooper()).postDelayed({
            toggleMediaPlay(context)

            // 4. Ouvrir Waze au premier plan 1,5 seconde après la lecture
            Handler(Looper.getMainLooper()).postDelayed({
                launchWaze(context)
            }, 1500)

        }, 2000)
    }

    // --- Méthode utilitaire pour ouvrir n'importe quelle application ---
    private fun launchApp(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            try {
                context.startActivity(launchIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun launchWaze(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.waze")
        if (launchIntent != null) {
            launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            try {
                context.startActivity(launchIntent)
            } catch (e: Exception) {
                showLaunchNotification(context, launchIntent)
            }
        }
    }

    private fun showLaunchNotification(context: Context, intent: Intent) {
        val channelId = "car_automation_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Car Automation",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Mode Voiture Actif")
            .setContentText("Ouverture de Waze...")
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    private fun stopCarSequence(context: Context) {
        // 1. Mettre la musique en pause
        toggleMediaPlay(context)

        // 2. Fermer Waze via son Intent de fermeture officiel
        closeWaze(context)

        // 3. Désactiver le mode Ne Pas Déranger
        setDoNotDisturb(context, false)

        // 4. Désactiver la rotation automatique
        setAutoRotation(context, false)
    }

    private fun setAutoRotation(context: Context, enabled: Boolean) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(context)) {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION,
                    if (enabled) 1 else 0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setDoNotDisturb(context: Context, enabled: Boolean) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager.isNotificationPolicyAccessGranted) {
                val filter = if (enabled) {
                    NotificationManager.INTERRUPTION_FILTER_PRIORITY
                } else {
                    NotificationManager.INTERRUPTION_FILTER_ALL
                }
                notificationManager.setInterruptionFilter(filter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleMediaPlay(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val eventTime = SystemClock.uptimeMillis()

            val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0)
            val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0)

            audioManager.dispatchMediaKeyEvent(downEvent)
            audioManager.dispatchMediaKeyEvent(upEvent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun closeWaze(context: Context) {
        try {
            // Waze répond à l'URL Scheme 'waze://?a=exit' pour se fermer proprement
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("waze://?a=exit")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Reduit le son de la musique (Deezer)
    private fun duckAudio(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val focusRequest = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .build()
                audioManager.requestAudioFocus(focusRequest)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Rétablit le volume normal de la musique
    private fun unduckAudio(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val focusRequest = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).build()
                audioManager.abandonAudioFocusRequest(focusRequest)
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}