package com.zoya.assistant.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import com.zoya.assistant.R
import java.util.Locale

/**
 * Optional, user-started foreground voice loop.
 *
 * Android's SpeechRecognizer is not a true low-power hardware hotword engine.
 * This service emulates "Hey JARVIS" by repeatedly opening short recognition
 * sessions. OEMs may stop or throttle it, so the notification stays visible.
 */
class WakeWordService : Service() {
    private var recognizer: SpeechRecognizer? = null
    private var listening = false
    private var lastTriggerAt = 0L

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!hasMicPermission()) {
            stopSelf()
            return START_NOT_STICKY
        }
        startWakeLoop()
        return START_STICKY
    }

    private fun hasMicPermission(): Boolean =
        checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    private fun startWakeLoop() {
        if (listening || !SpeechRecognizer.isRecognitionAvailable(this)) return
        listening = true
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit

                override fun onResults(results: Bundle?) {
                    listening = false
                    val result = results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )?.firstOrNull().orEmpty()
                    handleRecognition(result)
                    restartSoon()
                }

                override fun onError(error: Int) {
                    listening = false
                    restartSoon()
                }
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        recognizer?.startListening(intent)
    }

    private fun handleRecognition(raw: String) {
        val normalized = raw.trim()
        if (normalized.isBlank()) return

        val configured = getSharedPreferences("jarvis_settings", MODE_PRIVATE)
            .getString("wake_phrase", "Hey JARVIS").orEmpty().trim()
        val phrase = Regex.escape(if (configured.isBlank()) "Hey JARVIS" else configured)
        val wakePattern = Regex("""^$phrase\b[\s,:-]*(.*)$""", RegexOption.IGNORE_CASE)
        val match = wakePattern.find(normalized) ?: return
        val command = match.groupValues.getOrNull(2)?.trim().orEmpty()
        val now = System.currentTimeMillis()
        if (now - lastTriggerAt < 2500) return
        lastTriggerAt = now

        getSharedPreferences("jarvis_runtime", MODE_PRIVATE)
            .edit()
            .putString("pending_wake_command", command)
            .apply()

        sendBroadcast(Intent(ACTION_WAKE).setPackage(packageName).apply {
            putExtra(EXTRA_COMMAND, command)
        })
    }

    private fun restartSoon() {
        android.os.Handler(mainLooper).postDelayed({
            if (!isDestroyed) startWakeLoop()
        }, 350L)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_jarvis)
            .setContentTitle("JARVIS wake word is active")
            .setContentText("Say “Hey JARVIS”. Tap to open the assistant.")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(
                android.app.PendingIntent.getActivity(
                    this, 10,
                    Intent(this, com.zoya.assistant.MainActivity::class.java),
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                        android.app.PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "JARVIS voice assistant",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    override fun onDestroy() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        listening = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_WAKE = "com.zoya.assistant.ACTION_WAKE"
        const val EXTRA_COMMAND = "command"
        private const val CHANNEL_ID = "jarvis_voice"
        private const val NOTIFICATION_ID = 4201

        fun start(context: android.content.Context) {
            val intent = Intent(context, WakeWordService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: android.content.Context) {
            context.stopService(Intent(context, WakeWordService::class.java))
        }
    }
}
