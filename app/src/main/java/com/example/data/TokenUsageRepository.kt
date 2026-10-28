package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class TokenUsageRepository(private val dao: TokenUsageDao) {

    val allUsage: Flow<List<TokenUsageEntity>> = dao.getAllUsage()

    fun getUsageForLast5Hours(): Flow<List<TokenUsageEntity>> {
        val fiveHoursAgo = System.currentTimeMillis() - (5 * 60 * 60 * 1000L)
        return dao.getUsageSince(fiveHoursAgo)
    }

    fun getTotalTokensLast5Hours(): Flow<Int> {
        val fiveHoursAgo = System.currentTimeMillis() - (5 * 60 * 60 * 1000L)
        return dao.getTotalTokensSince(fiveHoursAgo).map { it ?: 0 }
    }

    fun getRequestCountLast5Hours(): Flow<Int> {
        val fiveHoursAgo = System.currentTimeMillis() - (5 * 60 * 60 * 1000L)
        return dao.getRequestCountSince(fiveHoursAgo).map { it ?: 0 }
    }

    fun getTotalTokensThisWeek(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return dao.getTotalTokensSince(calendar.timeInMillis).map { it ?: 0 }
    }

    suspend fun insert(entry: TokenUsageEntity): Long {
        return dao.insertUsage(entry)
    }

    suspend fun delete(entry: TokenUsageEntity) {
        dao.deleteUsage(entry)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    suspend fun populateSampleDataIfEmpty() {
        // Generate realistic initial logs if empty so the user sees real visuals immediately
        val now = System.currentTimeMillis()
        val samples = listOf(
            TokenUsageEntity(
                timestamp = now - (15 * 60 * 1000L),
                model = "gpt-4o",
                promptTokens = 1250,
                completionTokens = 840,
                totalTokens = 2090,
                estimatedCostUsd = 0.012,
                promptSummary = "Análisis de código Kotlin y optimización de corrutinas",
                source = "ChatGPT Web"
            ),
            TokenUsageEntity(
                timestamp = now - (1 * 3600 * 1000L),
                model = "o3-mini",
                promptTokens = 3400,
                completionTokens = 1920,
                totalTokens = 5320,
                estimatedCostUsd = 0.021,
                promptSummary = "Razonamiento lógico para algoritmo de ordenamiento en Android",
                source = "ChatGPT App"
            ),
            TokenUsageEntity(
                timestamp = now - (3 * 3600 * 1000L),
                model = "gpt-4o-mini",
                promptTokens = 890,
                completionTokens = 420,
                totalTokens = 1310,
                estimatedCostUsd = 0.002,
                promptSummary = "Resumen de documentación KWGT Kustom",
                source = "Widget Auto-Sync"
            ),
            TokenUsageEntity(
                timestamp = now - (24 * 3600 * 1000L),
                model = "gpt-4o",
                promptTokens = 4500,
                completionTokens = 3100,
                totalTokens = 7600,
                estimatedCostUsd = 0.045,
                promptSummary = "Generación de layout Compose y componentes M3",
                source = "ChatGPT Web"
            ),
            TokenUsageEntity(
                timestamp = now - (3 * 86400 * 1000L),
                model = "gpt-4o",
                promptTokens = 8200,
                completionTokens = 4500,
                totalTokens = 12700,
                estimatedCostUsd = 0.078,
                promptSummary = "Refactorización de base de datos Room y DAOs",
                source = "API Key"
            )
        )
        samples.forEach { dao.insertUsage(it) }
    }
}
