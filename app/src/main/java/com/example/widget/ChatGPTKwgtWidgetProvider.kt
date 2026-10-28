package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.OpenAiAuthService
import com.example.data.UsagePreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatGPTKwgtWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_COPY_AUTH -> {
                val prefs = UsagePreferencesManager(context)
                val authCode = prefs.authCode.value
                OpenAiAuthService.copyToClipboard(context, authCode, "Código Autorización OpenAI")
                Toast.makeText(context, "📋 Código de Autorización Copiado", Toast.LENGTH_SHORT).show()
            }
            ACTION_OPEN_OPENAI -> {
                OpenAiAuthService.openOpenAiDashboard(context)
            }
            ACTION_REFRESH_WIDGET, AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, ChatGPTKwgtWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                for (id in allWidgetIds) {
                    updateWidget(context, appWidgetManager, id)
                }
            }
        }
    }

    companion object {
        const val ACTION_COPY_AUTH = "com.example.widget.ACTION_COPY_AUTH"
        const val ACTION_OPEN_OPENAI = "com.example.widget.ACTION_OPEN_OPENAI"
        const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_kwgt_chatgpt)

            // Current Time & Date
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
            val now = Date()

            views.setTextViewText(R.id.widget_text_clock, timeFormat.format(now))
            views.setTextViewText(R.id.widget_text_date, dateFormat.format(now))

            // Query database in coroutine for accurate usage stats
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val prefs = UsagePreferencesManager(context)

                val fiveHoursAgo = System.currentTimeMillis() - (5 * 60 * 60 * 1000L)
                val total5hTokens = db.tokenUsageDao().getTotalTokensSince(fiveHoursAgo).firstOrNull() ?: 0
                val total5hReqs = db.tokenUsageDao().getRequestCountSince(fiveHoursAgo).firstOrNull() ?: 0

                val calendar = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                }
                val weeklyTokens = db.tokenUsageDao().getTotalTokensSince(calendar.timeInMillis).firstOrNull() ?: 0

                val max5hReqs = prefs.fiveHourRequestLimit.value
                val maxWeeklyTokens = prefs.weeklyTokenLimit.value

                val pct5h = if (max5hReqs > 0) ((total5hReqs.toFloat() / max5hReqs) * 100).toInt().coerceIn(0, 100) else 0
                val pctWeekly = if (maxWeeklyTokens > 0) ((weeklyTokens.toFloat() / maxWeeklyTokens) * 100).toInt().coerceIn(0, 100) else 0

                views.setTextViewText(R.id.widget_text_5h_usage, "$total5hReqs / $max5hReqs reqs ($pct5h%)")
                views.setProgressBar(R.id.widget_progress_5h, 100, pct5h, false)

                val weeklyK = String.format(Locale.US, "%.1fk", weeklyTokens / 1000f)
                val maxWeeklyK = String.format(Locale.US, "%.0fk", maxWeeklyTokens / 1000f)
                views.setTextViewText(R.id.widget_text_weekly_usage, "$weeklyK / $maxWeeklyK")
                views.setProgressBar(R.id.widget_progress_weekly, 100, pctWeekly, false)

                val statusLabel = when {
                    pct5h >= 90 -> "5h: Excedido"
                    pct5h >= 75 -> "5h: Alto"
                    else -> "5h: Óptimo"
                }
                views.setTextViewText(R.id.widget_status_label, statusLabel)

                // Setup PendingIntents
                val openAppIntent = Intent(context, MainActivity::class.java)
                val openAppPendingIntent = PendingIntent.getActivity(
                    context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

                val copyIntent = Intent(context, ChatGPTKwgtWidgetProvider::class.java).apply {
                    action = ACTION_COPY_AUTH
                }
                val copyPendingIntent = PendingIntent.getBroadcast(
                    context, 101, copyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_copy_auth, copyPendingIntent)

                val openAiIntent = Intent(context, ChatGPTKwgtWidgetProvider::class.java).apply {
                    action = ACTION_OPEN_OPENAI
                }
                val openAiPendingIntent = PendingIntent.getBroadcast(
                    context, 102, openAiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_open_openai, openAiPendingIntent)

                val refreshIntent = Intent(context, ChatGPTKwgtWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH_WIDGET
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context, 103, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)

                // Broadcast KWGT Intent for external KWGT Users
                broadcastToKwgt(context, total5hReqs, pct5h, weeklyTokens, prefs.authCode.value)
            }
        }

        private fun broadcastToKwgt(
            context: Context,
            reqs5h: Int,
            pct5h: Int,
            weeklyTokens: Int,
            authCode: String
        ) {
            val kwgtIntent = Intent("org.kustom.widget.UPDATE").apply {
                putExtra("chatgpt_5h_reqs", reqs5h)
                putExtra("chatgpt_5h_pct", pct5h)
                putExtra("chatgpt_weekly_tokens", weeklyTokens)
                putExtra("chatgpt_auth_code", authCode)
            }
            context.sendBroadcast(kwgtIntent)
        }
    }
}
