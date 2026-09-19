package com.zoya.assistant.data

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_settings", Context.MODE_PRIVATE)

    fun getApiKey(): String = prefs.getString("api_key", "") ?: ""
    fun setApiKey(value: String) = prefs.edit().putString("api_key", value.trim()).apply()
    fun clearApiKey() = prefs.edit().remove("api_key").apply()

    fun getBoolean(key: String, default: Boolean) = prefs.getBoolean(key, default)
    fun setBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun getString(key: String, default: String) = prefs.getString(key, default) ?: default
    fun setString(key: String, value: String) = prefs.edit().putString(key, value).apply()
}
