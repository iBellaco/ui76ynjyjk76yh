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
        // Starts completely empty by default as requested
    }
}
