package com.example.utils

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GeminiApiClient {
    private val client = OkHttpClient()

    private fun getApiKey(): String {
        return try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun generateResponse(prompt: String, modelName: String = "gemini-2.5-flash", language: String = "en"): String {
        return withContext(Dispatchers.IO) {
            val apiKey = getApiKey()

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext getOfflineResponse(prompt, language)
            }

            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                
                val systemInstruction = "You are JARVIS, an intelligent personal AI assistant. Your personality is intelligent, friendly, calm, respectful, helpful, slightly humorous, and concise for simple requests. You support English, Nepali, and Hindi. Respond naturally in the language of the user."
                val fullPrompt = "$systemInstruction\n\nUser: $prompt"

                val jsonBody = JSONObject().apply {
                    put("contents", org.json.JSONArray().put(
                        JSONObject().put("parts", org.json.JSONArray().put(
                            JSONObject().put("text", fullPrompt)
                        ))
                    ))
                }

                val body = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        return@withContext "API connection failed (${response.code}): $errorBody. Using offline assistant."
                    }

                    val responseString = response.body?.string() ?: return@withContext "Empty response from AI."
                    val jsonResponse = JSONObject(responseString)
                    val candidates = jsonResponse.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No response text.")
                        }
                    }
                    "Unable to parse AI response."
                }
            } catch (e: Exception) {
                val offlineReply = getOfflineResponse(prompt, language)
                "$offlineReply\n\n(Note: Online AI request failed: ${e.localizedMessage}. Using offline assistant mode.)"
            }
        }
    }

    private fun getOfflineResponse(prompt: String, language: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> {
                when (language) {
                    "ne" -> "नमस्ते! म JARVIS हुँ। आज म तपाईलाई कसरी सहयोग गर्न सक्छु?"
                    "hi" -> "नमस्ते! मैं JARVIS हूँ। आज मैं आपकी कैसे मदद कर सकता हूँ?"
                    else -> "Hello. I am JARVIS, your personal assistant. How can I assist you today?"
                }
            }
            lower.contains("battery") -> {
                when (language) {
                    "ne" -> "तपाईंको ब्याट्री स्थिति जाँच गर्न Quick Actions प्रयोग गर्नुहोस्।"
                    "hi" -> "अपनी बैटरी स्थिति जांचने के लिए Quick Actions का उपयोग करें।"
                    else -> "You can check your current battery level in the Quick Actions screen."
                }
            }
            lower.contains("name") -> "I am JARVIS, an intelligent personal AI assistant designed for your Android device."
            lower.contains("help") -> "I can assist you with general questions, task planning, memory, device controls, and multi-language conversation."
            else -> {
                when (language) {
                    "ne" -> "तपाईंको कुरा बुझें। म अफलाइन मोडमा छु, कृपया आफ्नो API कुञ्जी सेटिङ्समा थप्नुहोस्।"
                    "hi" -> "मैंने आपका संदेश समझ लिया। मैं अभी ऑफ़लाइन मोड में हूँ, कृपया सेटिंग्स में अपनी API कुंजी जोड़ें।"
                    else -> "I understand your request. I am currently operating in offline mode. To use online AI capabilities, please configure your API key in Settings."
                }
            }
        }
    }
}
