package com.zoya.assistant.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onState: (String) -> Unit,
    private val onText: (String) -> Unit,
    private val onError: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var initialized = false

    init {
        if (TextToSpeech(context, this).also { tts = it } == null) {
            onError("Text-to-speech is unavailable on this device.")
        }
    }

    override fun onInit(status: Int) {
        initialized = status == TextToSpeech.SUCCESS
        if (initialized) {
            tts?.language = Locale.getDefault()
        }
    }

    fun startListening(languageTag: String = "en-US") {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition is not available on this device.")
            return
        }
        stopListening()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { onState("Listening") }
                override fun onBeginningOfSpeech() { onState("Listening") }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { onState("Thinking") }
                override fun onError(error: Int) {
                    onState("Error")
                    onError("Speech recognition error ($error).")
                }
                override fun onResults(results: Bundle?) {
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!text.isNullOrBlank()) onText(text)
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
    }

    fun speak(text: String, speed: Float = 1f, volume: Float = 1f) {
        if (!initialized || text.isBlank()) return
        stopListening()
        onState("Speaking")
        tts?.setSpeechRate(speed.coerceIn(0.5f, 2f))
        tts?.setVolume(volume.coerceIn(0f, 1f))
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_response")
        onState("Completed")
    }

    fun stopSpeaking() {
        tts?.stop()
        onState("Completed")
    }

    fun shutdown() {
        stopListening()
        tts?.stop()
        tts?.shutdown()
    }
}
