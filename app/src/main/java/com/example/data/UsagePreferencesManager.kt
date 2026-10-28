package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class UsagePreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("kwgt_chatgpt_prefs", Context.MODE_PRIVATE)

    private val _apiKey = MutableStateFlow(prefs.getString(KEY_API_KEY, "") ?: "")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _authCode = MutableStateFlow(
        prefs.getString(KEY_AUTH_CODE, "") ?: generateDefaultAuthCode()
    )
    val authCode: StateFlow<String> = _authCode.asStateFlow()

    private val _fiveHourRequestLimit = MutableStateFlow(prefs.getInt(KEY_5H_LIMIT, 80))
    val fiveHourRequestLimit: StateFlow<Int> = _fiveHourRequestLimit.asStateFlow()

    private val _weeklyTokenLimit = MutableStateFlow(prefs.getInt(KEY_WEEKLY_LIMIT, 500000))
    val weeklyTokenLimit: StateFlow<Int> = _weeklyTokenLimit.asStateFlow()

    private val _widgetTheme = MutableStateFlow(prefs.getString(KEY_WIDGET_THEME, "KWGT Dark Minimal") ?: "KWGT Dark Minimal")
    val widgetTheme: StateFlow<String> = _widgetTheme.asStateFlow()

    private val _autoUpdateMinutes = MutableStateFlow(prefs.getInt(KEY_AUTO_UPDATE_MINS, 1))
    val autoUpdateMinutes: StateFlow<Int> = _autoUpdateMinutes.asStateFlow()

    init {
        // Ensure default auth code exists if first run
        if (prefs.getString(KEY_AUTH_CODE, null) == null) {
            val initialCode = generateDefaultAuthCode()
            saveAuthCode(initialCode)
        }
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key).apply()
        _apiKey.value = key
    }

    fun saveAuthCode(code: String) {
        prefs.edit().putString(KEY_AUTH_CODE, code).apply()
        _authCode.value = code
    }

    fun generateAndSaveNewAuthCode(): String {
        val newCode = "openai_auth_" + UUID.randomUUID().toString().take(12) + "_" + (1000..9999).random()
        saveAuthCode(newCode)
        return newCode
    }

    fun saveFiveHourLimit(limit: Int) {
        prefs.edit().putInt(KEY_5H_LIMIT, limit).apply()
        _fiveHourRequestLimit.value = limit
    }

    fun saveWeeklyLimit(limit: Int) {
        prefs.edit().putInt(KEY_WEEKLY_LIMIT, limit).apply()
        _weeklyTokenLimit.value = limit
    }

    fun saveWidgetTheme(themeName: String) {
        prefs.edit().putString(KEY_WIDGET_THEME, themeName).apply()
        _widgetTheme.value = themeName
    }

    fun saveAutoUpdateMinutes(mins: Int) {
        prefs.edit().putInt(KEY_AUTO_UPDATE_MINS, mins).apply()
        _autoUpdateMinutes.value = mins
    }

    private val _connectedEmail = MutableStateFlow(
        prefs.getString(KEY_CONNECTED_EMAIL, DEFAULT_EMAIL)?.let {
            if (it == "usuario@openai.com") DEFAULT_EMAIL else it
        } ?: DEFAULT_EMAIL
    )
    val connectedEmail: StateFlow<String> = _connectedEmail.asStateFlow()

    fun isSessionConnected(): Boolean = prefs.getBoolean(KEY_IS_CONNECTED, false)

    fun getConnectedEmail(): String {
        val email = prefs.getString(KEY_CONNECTED_EMAIL, DEFAULT_EMAIL) ?: DEFAULT_EMAIL
        return if (email == "usuario@openai.com") DEFAULT_EMAIL else email
    }

    fun saveConnectedEmail(email: String) {
        val validEmail = email.ifBlank { DEFAULT_EMAIL }
        prefs.edit().putString(KEY_CONNECTED_EMAIL, validEmail).apply()
        _connectedEmail.value = validEmail
    }

    fun getConnectedSince(): Long = prefs.getLong(KEY_CONNECTED_SINCE, 0L)

    fun get5hWindowResetTimestamp(): Long {
        val stored = prefs.getLong(KEY_5H_RESET_TS, 0L)
        val now = System.currentTimeMillis()
        return if (stored <= now) {
            val nextReset = now + (5 * 3600 * 1000L)
            prefs.edit().putLong(KEY_5H_RESET_TS, nextReset).apply()
            nextReset
        } else {
            stored
        }
    }

    fun reset5hWindow() {
        val nextReset = System.currentTimeMillis() + (5 * 3600 * 1000L)
        prefs.edit().putLong(KEY_5H_RESET_TS, nextReset).apply()
    }

    fun saveConnectedSession(email: String, connectedSince: Long = System.currentTimeMillis()) {
        val realEmail = if (email == "usuario@openai.com" || email.isBlank()) DEFAULT_EMAIL else email
        val nextReset = System.currentTimeMillis() + (5 * 3600 * 1000L)
        prefs.edit()
            .putBoolean(KEY_IS_CONNECTED, true)
            .putString(KEY_CONNECTED_EMAIL, realEmail)
            .putLong(KEY_CONNECTED_SINCE, connectedSince)
            .putLong(KEY_5H_RESET_TS, nextReset)
            .apply()
        _connectedEmail.value = realEmail
    }

    fun clearConnectedSession() {
        prefs.edit()
            .putBoolean(KEY_IS_CONNECTED, false)
            .remove(KEY_CONNECTED_EMAIL)
            .remove(KEY_CONNECTED_SINCE)
            .remove(KEY_5H_RESET_TS)
            .apply()
        _connectedEmail.value = DEFAULT_EMAIL
    }

    private fun generateDefaultAuthCode(): String {
        return "openai_auth_code_7f8a92b3c4d5"
    }

    companion object {
        const val DEFAULT_EMAIL = "barbadiego695@gmail.com"
        private const val KEY_API_KEY = "open_ai_api_key"
        private const val KEY_AUTH_CODE = "open_ai_auth_code"
        private const val KEY_5H_LIMIT = "five_hour_limit"
        private const val KEY_WEEKLY_LIMIT = "weekly_limit"
        private const val KEY_WIDGET_THEME = "widget_theme"
        private const val KEY_AUTO_UPDATE_MINS = "auto_update_mins"
        private const val KEY_IS_CONNECTED = "session_is_connected"
        private const val KEY_CONNECTED_EMAIL = "session_connected_email"
        private const val KEY_CONNECTED_SINCE = "session_connected_since"
        private const val KEY_5H_RESET_TS = "session_5h_reset_ts"
    }
}
