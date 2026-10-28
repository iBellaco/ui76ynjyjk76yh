package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "token_usage")
data class TokenUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String = "gpt-4o",
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = promptTokens + completionTokens,
    val estimatedCostUsd: Double = 0.0,
    val promptSummary: String = "",
    val source: String = "Chat/API" // "Chat/API", "Calculador", "Simulación", "Sesión 5h"
)
