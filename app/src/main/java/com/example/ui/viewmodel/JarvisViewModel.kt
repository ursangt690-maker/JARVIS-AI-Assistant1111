package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisMemoryEntity
import com.example.utils.AndroidActionsHelper
import com.example.utils.GeminiApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JarvisSettings(
    val aiProvider: String = "Gemini AI",
    val aiModel: String = "gemini-2.5-flash",
    val apiKeyConfigured: Boolean = false,
    val creativity: Float = 0.7f,
    val responseLength: String = "Standard",
    val streamingResponse: Boolean = true,
    
    // Voice Settings
    val voiceAssistantEnabled: Boolean = true,
    val voiceSelection: String = "JARVIS Natural",
    val speechSpeed: Float = 1.0f,
    val speechVolume: Float = 1.0f,
    val language: String = "Auto-detect (EN/NE/HI)",
    val autoDetectLanguage: Boolean = true,
    val interruptWhileSpeaking: Boolean = true,

    // Wake Word Settings
    val wakeWordEnabled: Boolean = false,
    val wakePhrase: String = "Hey JARVIS",
    val wakeWordSensitivity: Float = 0.75f,

    // Proactive Settings
    val proactiveEnabled: Boolean = true,
    val proactiveFrequency: String = "Normal",
    val quietMode: Boolean = false,

    // Memory Settings
    val memoryEnabled: Boolean = true,

    // Personality Settings
    val friendlyLevel: String = "High",
    val humorLevel: String = "Moderate",
    val formality: String = "Balanced",

    // Privacy & Security
    val appLock: Boolean = false,
    val biometricAuth: Boolean = false,
    val privacyMode: Boolean = false,

    // Appearance
    val themeMode: String = "System", // Light, Dark, System
    val futuristicUi: Boolean = true
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = JarvisDatabase.getDatabase(application).jarvisDao()
    private val geminiClient = GeminiApiClient()

    val messages = dao.getAllMessages().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val memories = dao.getAllMemories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _assistantStatus = MutableStateFlow("Completed") // Listening, Thinking, Processing, Speaking, Completed, Error, Offline
    val assistantStatus: StateFlow<String> = _assistantStatus.asStateFlow()

    private val _settings = MutableStateFlow(JarvisSettings())
    val settings: StateFlow<JarvisSettings> = _settings.asStateFlow()

    fun updateSettings(newSettings: JarvisSettings) {
        _settings.value = newSettings
    }

    fun sendMessage(text: String, context: Context) {
        if (text.isBlank()) return
        val currentLang = detectLanguage(text)

        viewModelScope.launch {
            // Save user message
            dao.insertMessage(
                ChatMessageEntity(
                    role = "user",
                    message = text,
                    language = currentLang,
                    status = "completed"
                )
            )

            _assistantStatus.value = "Thinking"

            // Check for direct command actions (e.g. Battery, Flashlight, etc.)
            val lower = text.lowercase()
            val reply = when {
                lower.contains("battery") || lower.contains("ब्याट्री") -> {
                    AndroidActionsHelper.getBatteryLevel(context)
                }
                lower.contains("device info") || lower.contains("phone info") -> {
                    AndroidActionsHelper.getDeviceInfo()
                }
                lower.contains("turn on flashlight") ||lower.contains("flashlight on") -> {
                    AndroidActionsHelper.toggleFlashlight(context, true)
                }
                lower.contains("turn off flashlight") || lower.contains("flashlight off") -> {
                    AndroidActionsHelper.toggleFlashlight(context, false)
                }
                lower.contains("search for") || lower.contains("google") -> {
                    val query = text.replace("search for", "", true).replace("google", "", true).trim()
                    val success = AndroidActionsHelper.performWebSearch(context, query)
                    if (success) "I opened a web search for: $query" else "Unable to launch web search."
                }
                else -> {
                    _assistantStatus.value = "Processing"
                    val model = _settings.value.aiModel
                    geminiClient.generateResponse(text, model, currentLang)
                }
            }

            _assistantStatus.value = "Speaking"
            dao.insertMessage(
                ChatMessageEntity(
                    role = "jarvis",
                    message = reply,
                    language = currentLang,
                    status = "completed"
                )
            )
            _assistantStatus.value = "Completed"
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            dao.clearMessages()
        }
    }

    fun saveMemory(key: String, value: String, category: String = "general") {
        if (!_settings.value.memoryEnabled) return
        viewModelScope.launch {
            dao.insertMemory(JarvisMemoryEntity(memoryKey = key, memoryValue = value, category = category))
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            dao.deleteMemory(id)
        }
    }

    fun clearMemories() {
        viewModelScope.launch {
            dao.clearMemories()
        }
    }

    private fun detectLanguage(text: String): String {
        // Simple heuristic detection for Nepali / Hindi / English
        val nepaliChars = text.any { it in '\u0900'..'\u097F' }
        return if (nepaliChars) {
            // Check Devanagari script for Nepali specific words if needed, default to ne
            "ne"
        } else {
            "en"
        }
    }
}
