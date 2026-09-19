package com.zoya.assistant.utils

import com.zoya.assistant.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GeminiApiClient(private val settings: SettingsRepository) {
    private val client = OkHttpClient()

    suspend fun generateResponse(prompt: String, modelName: String, language: String): Result<String> =
        withContext(Dispatchers.IO) {
            val apiKey = settings.getApiKey()
            if (apiKey.isBlank()) return@withContext Result.failure(IllegalStateException("Gemini API key is not configured. Open Settings and add it."))

            try {
                val system = """You are JARVIS, a practical Android personal assistant.
Be intelligent, friendly, calm, respectful, natural and concise for simple requests.
Support English, Nepali and Hindi and reply in the user's language.
Never claim an Android action was completed unless the application actually completed it.
Do not invent memories, tools, permissions, sources or results.
Do not repeatedly ask how you can help.
${JARVIS_SYSTEM_PROMPT}"""
                val bodyJson = JSONObject().apply {
                    put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
                    put("contents", JSONArray().put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                    }))
                }
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")
                    .post(bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(IllegalStateException("Gemini request failed (${response.code}). Check API configuration and network connection."))
                    }
                    val text = JSONObject(raw)
                        .optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")
                        .orEmpty()
                    if (text.isBlank()) Result.failure(IllegalStateException("Gemini returned no text response."))
                    else Result.success(text)
                }
            } catch (e: Exception) {
                Result.failure(IllegalStateException("Gemini connection failed: ${e.message ?: "unknown error"}"))
            }
        }

    companion object {
        const val JARVIS_SYSTEM_PROMPT = """
Use natural voice-friendly answers. Support English, Nepali and Hindi. Maintain conversation context when supplied.
Be proactive only when explicitly configured. Respect privacy and Android permissions.
Potentially sensitive actions require confirmation. Never claim an action happened unless verified.
"""
    }
}
