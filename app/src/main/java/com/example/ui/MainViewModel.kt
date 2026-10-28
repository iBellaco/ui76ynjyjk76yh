package com.example.ui

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.OpenAiAuthService
import com.example.data.TokenUsageEntity
import com.example.data.TokenUsageRepository
import com.example.data.UsagePreferencesManager
import com.example.widget.ChatGPTKwgtWidgetProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = TokenUsageRepository(db.tokenUsageDao())
    val preferences = UsagePreferencesManager(application)

    // Clock state
    private val _currentTimeText = MutableStateFlow("")
    val currentTimeText: StateFlow<String> = _currentTimeText.asStateFlow()

    private val _currentDateText = MutableStateFlow("")
    val currentDateText: StateFlow<String> = _currentDateText.asStateFlow()

    // Usage Flows
    val tokenLogs: StateFlow<List<TokenUsageEntity>> = repository.allUsage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val total5hTokens: StateFlow<Int> = repository.getTotalTokensLast5Hours()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val total5hReqs: StateFlow<Int> = repository.getRequestCountLast5Hours()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalWeeklyTokens: StateFlow<Int> = repository.getTotalTokensThisWeek()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Preference States
    val fiveHourLimit: StateFlow<Int> = preferences.fiveHourRequestLimit
    val weeklyLimit: StateFlow<Int> = preferences.weeklyTokenLimit
    val apiKey: StateFlow<String> = preferences.apiKey
    val authCode: StateFlow<String> = preferences.authCode
    val widgetTheme: StateFlow<String> = preferences.widgetTheme

    // API Connection Verification State
    private val _connectionResult = MutableStateFlow<OpenAiAuthService.ConnectionResult?>(null)
    val connectionResult: StateFlow<OpenAiAuthService.ConnectionResult?> = _connectionResult.asStateFlow()

    val connectedEmail: StateFlow<String> = preferences.connectedEmail

    private val _fiveHourResetCountdown = MutableStateFlow("4h 59m")
    val fiveHourResetCountdown: StateFlow<String> = _fiveHourResetCountdown.asStateFlow()

    // Device Auth Flow State (Loads persisted session if already connected)
    private val _authFlowState = MutableStateFlow<OpenAiAuthService.AuthFlowState>(
        if (preferences.isSessionConnected()) {
            OpenAiAuthService.AuthFlowState.Connected(
                accountEmail = preferences.getConnectedEmail(),
                tier = "ChatGPT Plus",
                connectedSince = preferences.getConnectedSince()
            )
        } else {
            OpenAiAuthService.AuthFlowState.Disconnected(isExpired = false)
        }
    )
    val authFlowState: StateFlow<OpenAiAuthService.AuthFlowState> = _authFlowState.asStateFlow()

    private var countdownJob: Job? = null
    private var pollingJob: Job? = null

    init {
        // Start live clock ticking
        startClockTimer()
    }

    private fun startClockTimer() {
        viewModelScope.launch {
            val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateFormatter = SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Locale("es", "ES"))
            while (true) {
                val now = Date()
                _currentTimeText.value = timeFormatter.format(now)
                _currentDateText.value = dateFormatter.format(now).replaceFirstChar { it.uppercase() }

                val resetTs = preferences.get5hWindowResetTimestamp()
                val diff = (resetTs - now.time).coerceAtLeast(0L)
                val hours = (diff / (3600 * 1000L))
                val minutes = (diff % (3600 * 1000L)) / (60 * 1000L)
                _fiveHourResetCountdown.value = "${hours}h ${minutes}m"

                delay(1000L)
            }
        }
    }

    // Requests REAL device code from OpenAI official server
    fun startDeviceAuthFlow() {
        countdownJob?.cancel()
        pollingJob?.cancel()
        _authFlowState.value = OpenAiAuthService.AuthFlowState.Connecting

        viewModelScope.launch {
            when (val result = OpenAiAuthService.requestRealDeviceCode()) {
                is OpenAiAuthService.RealDeviceCodeResult.Success -> {
                    preferences.saveAuthCode(result.formattedUserCode)
                    _authFlowState.value = OpenAiAuthService.AuthFlowState.Authorizing(
                        code = result.formattedUserCode,
                        deviceAuthId = result.deviceAuthId,
                        remainingSeconds = 899,
                        pollingStatus = "Comprobando autorización..."
                    )
                    startCountdownTimer(899, result.formattedUserCode, result.deviceAuthId)
                    startPolling(result.deviceAuthId, result.rawUserCode, result.interval)
                    notifyWidgetUpdate(getApplication())
                }
                is OpenAiAuthService.RealDeviceCodeResult.Error -> {
                    _authFlowState.value = OpenAiAuthService.AuthFlowState.Disconnected(
                        isExpired = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun startCountdownTimer(initialSeconds: Int, code: String, deviceAuthId: String) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var secs = initialSeconds
            while (secs > 0) {
                delay(1000L)
                secs -= 1
                val current = _authFlowState.value
                if (current is OpenAiAuthService.AuthFlowState.Authorizing) {
                    _authFlowState.value = current.copy(remainingSeconds = secs)
                }
            }
            if (_authFlowState.value is OpenAiAuthService.AuthFlowState.Authorizing) {
                pollingJob?.cancel()
                _authFlowState.value = OpenAiAuthService.AuthFlowState.Disconnected(isExpired = true)
            }
        }
    }

    private fun startPolling(deviceAuthId: String, userCode: String, intervalSeconds: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            val waitMillis = (intervalSeconds.coerceAtLeast(3)) * 1000L
            while (true) {
                delay(waitMillis)
                val current = _authFlowState.value
                if (current !is OpenAiAuthService.AuthFlowState.Authorizing) break

                val pollResult = OpenAiAuthService.pollDeviceToken(deviceAuthId, userCode)
                when (pollResult) {
                    is OpenAiAuthService.PollTokenResult.Authorized -> {
                        completeAuthorization(pollResult.accountEmail)
                        break
                    }
                    is OpenAiAuthService.PollTokenResult.Expired -> {
                        countdownJob?.cancel()
                        _authFlowState.value = OpenAiAuthService.AuthFlowState.Disconnected(isExpired = true)
                        break
                    }
                    is OpenAiAuthService.PollTokenResult.Pending -> {
                        // Keep polling
                    }
                    is OpenAiAuthService.PollTokenResult.Error -> {
                        // Transient error, keep waiting
                    }
                }
            }
        }
    }

    fun confirmDeviceAuthorization() {
        val current = _authFlowState.value as? OpenAiAuthService.AuthFlowState.Authorizing ?: return
        viewModelScope.launch {
            val pollResult = OpenAiAuthService.pollDeviceToken(current.deviceAuthId, current.code)
            when (pollResult) {
                is OpenAiAuthService.PollTokenResult.Authorized -> {
                    completeAuthorization(pollResult.accountEmail)
                }
                is OpenAiAuthService.PollTokenResult.Pending -> {
                    // Quick recheck after 1.5s in case OpenAI web hook was processing
                    delay(1500)
                    val retryResult = OpenAiAuthService.pollDeviceToken(current.deviceAuthId, current.code)
                    if (retryResult is OpenAiAuthService.PollTokenResult.Authorized) {
                        completeAuthorization(retryResult.accountEmail)
                    } else {
                        Toast.makeText(
                            getApplication(),
                            "Autorización pendiente en OpenAI. Si ya viste 'Inició sesión en Codex' en el navegador, se activará en breve.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                is OpenAiAuthService.PollTokenResult.Expired -> {
                    countdownJob?.cancel()
                    pollingJob?.cancel()
                    _authFlowState.value = OpenAiAuthService.AuthFlowState.Disconnected(isExpired = true)
                    Toast.makeText(getApplication(), "El código ha caducado", Toast.LENGTH_SHORT).show()
                }
                is OpenAiAuthService.PollTokenResult.Error -> {
                    // Try re-checking once more
                    delay(1000)
                    val retryResult = OpenAiAuthService.pollDeviceToken(current.deviceAuthId, current.code)
                    if (retryResult is OpenAiAuthService.PollTokenResult.Authorized) {
                        completeAuthorization(retryResult.accountEmail)
                    } else {
                        Toast.makeText(getApplication(), pollResult.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun forceConfirmConnection(email: String = "usuario@openai.com") {
        completeAuthorization(email)
    }

    private fun completeAuthorization(accountEmail: String) {
        val now = System.currentTimeMillis()
        countdownJob?.cancel()
        pollingJob?.cancel()
        preferences.saveConnectedSession(accountEmail, now)
        _authFlowState.value = OpenAiAuthService.AuthFlowState.Connected(
            accountEmail = accountEmail,
            tier = "ChatGPT Plus",
            connectedSince = now
        )
        Toast.makeText(getApplication(), "¡Dispositivo Autorizado con Éxito!", Toast.LENGTH_SHORT).show()
        notifyWidgetUpdate(getApplication())
    }

    fun cancelDeviceAuthorization() {
        countdownJob?.cancel()
        pollingJob?.cancel()
        _authFlowState.value = OpenAiAuthService.AuthFlowState.Disconnected(isExpired = false)
    }

    fun disconnectSession() {
        countdownJob?.cancel()
        pollingJob?.cancel()
        preferences.clearConnectedSession()
        _authFlowState.value = OpenAiAuthService.AuthFlowState.Disconnected(isExpired = false)
        notifyWidgetUpdate(getApplication())
    }

    fun updateUserEmail(newEmail: String) {
        val trimmed = newEmail.trim()
        if (trimmed.isNotBlank()) {
            preferences.saveConnectedEmail(trimmed)
            val current = _authFlowState.value
            if (current is OpenAiAuthService.AuthFlowState.Connected) {
                _authFlowState.value = current.copy(accountEmail = trimmed)
            }
            Toast.makeText(getApplication(), "Cuenta actualizada: $trimmed", Toast.LENGTH_SHORT).show()
            notifyWidgetUpdate(getApplication())
        }
    }

    fun recordQuickChatGptRequest(
        model: String = "gpt-4o",
        promptSummary: String = "Consulta ChatGPT",
        promptTokens: Int = 180,
        completionTokens: Int = 320
    ) {
        viewModelScope.launch {
            val total = promptTokens + completionTokens
            val costPer1k = when (model) {
                "gpt-4o" -> 0.005
                "o3-mini" -> 0.003
                "gpt-4o-mini" -> 0.0003
                else -> 0.002
            }
            val estCost = (total / 1000.0) * costPer1k

            val entry = TokenUsageEntity(
                timestamp = System.currentTimeMillis(),
                model = model,
                promptTokens = promptTokens,
                completionTokens = completionTokens,
                totalTokens = total,
                estimatedCostUsd = estCost,
                promptSummary = promptSummary,
                source = "Sesión OpenAI"
            )
            repository.insert(entry)
            notifyWidgetUpdate(getApplication())
            Toast.makeText(getApplication(), "Petición registrada: $model ($total tokens)", Toast.LENGTH_SHORT).show()
        }
    }

    fun simulateTestRequest() {
        val models = listOf("gpt-4o", "o3-mini", "gpt-4o-mini")
        val randomModel = models.random()
        val pTok = (80..350).random()
        val cTok = (150..600).random()
        recordQuickChatGptRequest(
            model = randomModel,
            promptSummary = "Petición $randomModel (${pTok + cTok} tokens)",
            promptTokens = pTok,
            completionTokens = cTok
        )
    }

    fun increment5hRequests(delta: Int) {
        if (delta > 0) {
            for (i in 1..delta) {
                recordQuickChatGptRequest("gpt-4o", "Consulta rápida (+1)", 150, 250)
            }
        } else if (delta < 0) {
            viewModelScope.launch {
                val currentLogs = repository.allUsage.firstOrNull() ?: emptyList()
                val fiveHoursAgo = System.currentTimeMillis() - (5 * 60 * 60 * 1000L)
                val recentLogs = currentLogs.filter { it.timestamp >= fiveHoursAgo }
                val toDelete = recentLogs.take((-delta).coerceAtMost(recentLogs.size))
                for (entry in toDelete) {
                    repository.delete(entry)
                }
                notifyWidgetUpdate(getApplication())
            }
        }
    }

    fun set5hRequestsExact(targetCount: Int) {
        viewModelScope.launch {
            val current5hCount = repository.getRequestCountLast5Hours().firstOrNull() ?: 0
            val diff = targetCount - current5hCount
            val fiveHoursAgo = System.currentTimeMillis() - (5 * 60 * 60 * 1000L)
            if (diff > 0) {
                for (i in 1..diff) {
                    recordQuickChatGptRequest("gpt-4o", "Ajuste manual de uso real", 150, 250)
                }
            } else if (diff < 0) {
                val currentLogs = repository.allUsage.firstOrNull() ?: emptyList()
                val recentLogs = currentLogs.filter { it.timestamp >= fiveHoursAgo }
                val toDelete = recentLogs.take((-diff).coerceAtMost(recentLogs.size))
                for (entry in toDelete) {
                    repository.delete(entry)
                }
                notifyWidgetUpdate(getApplication())
            }
        }
    }

    fun reset5hSlidingWindow() {
        preferences.reset5hWindow()
        Toast.makeText(getApplication(), "Ventana de 5 horas reiniciada", Toast.LENGTH_SHORT).show()
        notifyWidgetUpdate(getApplication())
    }

    fun copyAndOpenOpenAi(context: Context, code: String) {
        OpenAiAuthService.copyToClipboard(context, code, "Código de Autorización OpenAI")
        Toast.makeText(context, "Código copiado. Pégalo en OpenAI.", Toast.LENGTH_SHORT).show()
        OpenAiAuthService.openBrowser(context, OpenAiAuthService.OPENAI_DEVICE_URL)
    }

    fun addManualTokenUsage(
        model: String,
        promptText: String,
        completionText: String,
        promptSummary: String
    ) {
        viewModelScope.launch {
            val pTokens = estimateTokenCount(promptText).coerceAtLeast(10)
            val cTokens = estimateTokenCount(completionText).coerceAtLeast(10)
            val total = pTokens + cTokens

            // Cost estimation based on model
            val costPer1k = when (model) {
                "gpt-4o" -> 0.005
                "o3-mini" -> 0.003
                "gpt-4o-mini" -> 0.0003
                else -> 0.002
            }
            val estCost = (total / 1000.0) * costPer1k

            val entry = TokenUsageEntity(
                timestamp = System.currentTimeMillis(),
                model = model,
                promptTokens = pTokens,
                completionTokens = cTokens,
                totalTokens = total,
                estimatedCostUsd = estCost,
                promptSummary = promptSummary.ifBlank { "Consumo manual de $model ($total tokens)" },
                source = "Calculador Manual"
            )
            repository.insert(entry)
            notifyWidgetUpdate(getApplication())
        }
    }

    fun deleteTokenLog(entry: TokenUsageEntity) {
        viewModelScope.launch {
            repository.delete(entry)
            notifyWidgetUpdate(getApplication())
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAll()
            notifyWidgetUpdate(getApplication())
        }
    }

    fun saveApiKey(key: String) {
        preferences.saveApiKey(key)
        verifyConnection(key)
    }

    fun verifyConnection(key: String = apiKey.value) {
        viewModelScope.launch {
            val result = OpenAiAuthService.verifyApiKeyConnection(key)
            _connectionResult.value = result
        }
    }

    fun update5hLimit(limit: Int) {
        preferences.saveFiveHourLimit(limit)
        notifyWidgetUpdate(getApplication())
    }

    fun updateWeeklyLimit(limit: Int) {
        preferences.saveWeeklyLimit(limit)
        notifyWidgetUpdate(getApplication())
    }

    fun updateWidgetTheme(themeName: String) {
        preferences.saveWidgetTheme(themeName)
        notifyWidgetUpdate(getApplication())
    }

    fun estimateTokenCount(text: String): Int {
        if (text.isBlank()) return 0
        return (text.length / 3.8).toInt().coerceAtLeast(1)
    }

    fun copyAuthCode(context: Context) {
        OpenAiAuthService.copyToClipboard(context, authCode.value, "Código de Autorización OpenAI")
    }

    fun openOpenAiDashboard(context: Context) {
        OpenAiAuthService.openOpenAiDashboard(context)
    }

    private fun notifyWidgetUpdate(context: Context) {
        ChatGPTKwgtWidgetProvider.updateWidget(
            context,
            android.appwidget.AppWidgetManager.getInstance(context),
            0
        )
    }
}
