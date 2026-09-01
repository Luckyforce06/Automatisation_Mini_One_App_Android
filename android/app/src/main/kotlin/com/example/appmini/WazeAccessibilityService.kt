package com.example.appmini

import android.accessibilityservice.AccessibilityService
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class WazeAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var isDucking = false
    private var resetRunnable: Runnable? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // On vérifie si l'événement provient de Waze
        if (event.packageName == "com.waze") {
            // L'événement TYPE_WINDOW_CONTENT_CHANGED ou TYPE_VIEW_TEXT_CHANGED
            // indique que Waze met à jour sa bannière d'instruction
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                
                triggerAudioDucking()
            }
        }
    }

    private fun triggerAudioDucking() {
        if (!isDucking) {
            duckAudio()
            isDucking = true
        }

        // Annuler le minuteur précédent si une nouvelle instruction arrive rapidement
        resetRunnable?.let { handler.removeCallbacks(it) }

        // Réaugmenter le volume 6 secondes après la dernière mise à jour d'instruction
        resetRunnable = Runnable {
            unduckAudio()
            isDucking = false
        }
        handler.postDelayed(resetRunnable!!, 6000)
    }

    private fun duckAudio() {
        try {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .build()
                audioManager.requestAudioFocus(focusRequest)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unduckAudio() {
        try {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).build()
                audioManager.abandonAudioFocusRequest(focusRequest)
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {
        unduckAudio()
    }
}