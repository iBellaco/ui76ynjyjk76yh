package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenUsageDao {

    @Query("SELECT * FROM token_usage ORDER BY timestamp DESC")
    fun getAllUsage(): Flow<List<TokenUsageEntity>>

    @Query("SELECT * FROM token_usage WHERE timestamp >= :startTimeMillis ORDER BY timestamp DESC")
    fun getUsageSince(startTimeMillis: Long): Flow<List<TokenUsageEntity>>

    @Query("SELECT SUM(totalTokens) FROM token_usage WHERE timestamp >= :startTimeMillis")
    fun getTotalTokensSince(startTimeMillis: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM token_usage WHERE timestamp >= :startTimeMillis")
    fun getRequestCountSince(startTimeMillis: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(entry: TokenUsageEntity): Long

    @Delete
    suspend fun deleteUsage(entry: TokenUsageEntity)

    @Query("DELETE FROM token_usage WHERE id = :id")
    suspend fun deleteUsageById(id: Long)

    @Query("DELETE FROM token_usage")
    suspend fun clearAll()
}
