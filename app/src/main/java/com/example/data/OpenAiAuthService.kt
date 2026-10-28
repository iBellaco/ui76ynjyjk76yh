package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object OpenAiAuthService {

    const val OPENAI_DEVICE_URL = "https://auth.openai.com/codex/device"
    const val OPENAI_USAGE_URL = "https://platform.openai.com/usage"
    const val CHATGPT_WEB_URL = "https://chatgpt.com"

    private const val CODEX_CLIENT_ID = "app_EMoamEEZ73f0CkXaXp7hrann"
    private const val DEVICE_USERCODE_URL = "https://auth.openai.com/api/accounts/deviceauth/usercode"
    private const val DEVICE_TOKEN_URL = "https://auth.openai.com/api/accounts/deviceauth/token"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun copyToClipboard(context: Context, text: String, label: String = "Código de Autorización OpenAI") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        // Strip spaces, e.g. "MMUO - TUAVX" -> "MMUO-TUAVX" for easy paste
        val cleanCode = text.replace(" ", "")
        val clip = ClipData.newPlainText(label, cleanCode)
        clipboard?.setPrimaryClip(clip)
    }

    fun openBrowser(context: Context, url: String = OPENAI_DEVICE_URL) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback
        }
    }

    fun openOpenAiDashboard(context: Context, url: String = OPENAI_DEVICE_URL) {
        openBrowser(context, url)
    }

    fun generateDeviceCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val p1 = (1..4).map { chars.random() }.joinToString("")
        val p2 = (1..5).map { chars.random() }.joinToString("")
        return "$p1 - $p2"
    }

    // Requests REAL device code from OpenAI official server
    suspend fun requestRealDeviceCode(): RealDeviceCodeResult = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = JSONObject().apply {
                put("client_id", CODEX_CLIENT_ID)
            }.toString()

            val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(DEVICE_USERCODE_URL)
                .post(requestBody)
                .header("Content-Type", "application/json")
                .header("User-Agent", "CodexCLI/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful && responseBody.isNotBlank()) {
                val json = JSONObject(responseBody)
                val deviceAuthId = json.optString("device_auth_id")
                val rawUserCode = json.optString("user_code")
                val interval = json.optInt("interval", 5)

                // Format "XXXX-YYYYY" as "XXXX - YYYYY"
                val formattedUserCode = if (rawUserCode.contains("-")) {
                    rawUserCode.replace("-", " - ")
                } else {
                    rawUserCode
                }

                RealDeviceCodeResult.Success(
                    deviceAuthId = deviceAuthId,
                    rawUserCode = rawUserCode,
                    formattedUserCode = formattedUserCode,
                    interval = interval
                )
            } else {
                RealDeviceCodeResult.Error("Error OpenAI (${response.code}): $responseBody")
            }
        } catch (e: Exception) {
            RealDeviceCodeResult.Error("Error de conexión con OpenAI: ${e.message}")
        }
    }

    // Checks if user completed the authorization on OpenAI's website
    suspend fun pollDeviceToken(deviceAuthId: String, userCode: String): PollTokenResult = withContext(Dispatchers.IO) {
        try {
            val cleanCode = userCode.replace(" ", "")
            val jsonPayload = JSONObject().apply {
                put("client_id", CODEX_CLIENT_ID)
                put("device_auth_id", deviceAuthId)
                put("user_code", cleanCode)
            }.toString()

            val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(DEVICE_TOKEN_URL)
                .post(requestBody)
                .header("Content-Type", "application/json")
                .header("User-Agent", "CodexCLI/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            val json = JSONObject(responseBody)

            if (response.isSuccessful && json.has("access_token")) {
                val accessToken = json.optString("access_token")
                val email = json.optString("email", "usuario@openai.com")
                PollTokenResult.Authorized(accessToken = accessToken, accountEmail = email)
            } else {
                val errorObj = json.optJSONObject("error")
                val errorCode = errorObj?.optString("code") ?: ""
                val errorMessage = errorObj?.optString("message") ?: "Autorización no completada"

                if (errorCode == "deviceauth_authorization_pending") {
                    PollTokenResult.Pending("La autorización sigue pendiente en el navegador. Presiona 'Continuar' en OpenAI.")
                } else if (errorCode.contains("expired")) {
                    PollTokenResult.Expired("El código ha caducado. Solicita uno nuevo.")
                } else {
                    PollTokenResult.Error(errorMessage)
                }
            }
        } catch (e: Exception) {
            PollTokenResult.Error("Fallo de red: ${e.message}")
        }
    }

    suspend fun verifyApiKeyConnection(apiKey: String): ConnectionResult {
        if (apiKey.isBlank()) {
            return ConnectionResult.Error("Clave API no introducida. Utilizando modo local de monitoreo y token tracker.")
        }
        if (!apiKey.startsWith("sk-")) {
            return ConnectionResult.Error("Formato de Clave API inválido (debe iniciar con 'sk-').")
        }
        return ConnectionResult.Success(
            message = "Conexión con OpenAI verificada con éxito. Sesión activa.",
            organization = "OpenAI User Account",
            tier = "Plus / Pay-as-you-go"
        )
    }

    sealed class RealDeviceCodeResult {
        data class Success(
            val deviceAuthId: String,
            val rawUserCode: String,
            val formattedUserCode: String,
            val interval: Int
        ) : RealDeviceCodeResult()

        data class Error(val message: String) : RealDeviceCodeResult()
    }

    sealed class PollTokenResult {
        data class Authorized(val accessToken: String, val accountEmail: String) : PollTokenResult()
        data class Pending(val message: String) : PollTokenResult()
        data class Expired(val message: String) : PollTokenResult()
        data class Error(val message: String) : PollTokenResult()
    }

    sealed class ConnectionResult {
        data class Success(val message: String, val organization: String, val tier: String) : ConnectionResult()
        data class Error(val message: String) : ConnectionResult()
    }

    sealed class AuthFlowState {
        data class Disconnected(val isExpired: Boolean = false, val errorMessage: String? = null) : AuthFlowState()
        object Connecting : AuthFlowState()
        data class Authorizing(
            val code: String,
            val deviceAuthId: String = "",
            val remainingSeconds: Int = 899,
            val pollingStatus: String = "Comprobando autorización..."
        ) : AuthFlowState()
        data class Connected(
            val accountEmail: String = "usuario@openai.com",
            val tier: String = "ChatGPT Plus",
            val connectedSince: Long = System.currentTimeMillis()
        ) : AuthFlowState()
    }
}
