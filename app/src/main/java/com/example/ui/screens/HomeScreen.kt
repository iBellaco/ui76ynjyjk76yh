package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.SecondaryMint
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusGood
import com.example.ui.theme.StatusWarning
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val clockText by viewModel.currentTimeText.collectAsState()
    val dateText by viewModel.currentDateText.collectAsState()

    val total5hReqs by viewModel.total5hReqs.collectAsState()
    val total5hTokens by viewModel.total5hTokens.collectAsState()
    val totalWeeklyTokens by viewModel.totalWeeklyTokens.collectAsState()

    val max5hReqs by viewModel.fiveHourLimit.collectAsState()
    val maxWeeklyTokens by viewModel.weeklyLimit.collectAsState()
    val authCode by viewModel.authCode.collectAsState()

    val pct5h = if (max5hReqs > 0) ((total5hReqs.toFloat() / max5hReqs) * 100).toInt().coerceIn(0, 100) else 0
    val pctWeekly = if (maxWeeklyTokens > 0) ((totalWeeklyTokens.toFloat() / maxWeeklyTokens) * 100).toInt().coerceIn(0, 100) else 0

    var showCalculator by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card: Live Clock & Quick Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_clock_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(PrimaryEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ESTADO REAL DE CHATGPT",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryEmerald,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Auto-Sync 1 min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = clockText.ifEmpty { "00:00:00" },
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("clock_text_display")
                )

                Text(
                    text = dateText.ifEmpty { "Cargando fecha..." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.copyAuthCode(context)
                            Toast.makeText(context, "📋 Código de Autorización Copiado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_copy_auth_home"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.openOpenAiDashboard(context) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_open_openai_home"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("OpenAI", fontSize = 12.sp)
                    }
                }
            }
        }

        // 5-Hour Usage Window Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_5h_usage"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = SecondaryMint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ventana de 5 Horas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val statusColor = when {
                        pct5h >= 90 -> StatusError
                        pct5h >= 75 -> StatusWarning
                        else -> StatusGood
                    }

                    val statusText = when {
                        pct5h >= 90 -> "Crítico"
                        pct5h >= 75 -> "Alto"
                        else -> "Óptimo"
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusText,
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "$total5hReqs / $max5hReqs peticiones",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tokens consumidos: ${String.format(Locale.US, "%,d", total5hTokens)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "$pct5h%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryMint
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { (pct5h / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (pct5h >= 90) StatusError else PrimaryEmerald,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Weekly Usage Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_weekly_usage"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Consumo Semanal de Tokens",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "$pctWeekly%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val weeklyK = String.format(Locale.US, "%.1fk", totalWeeklyTokens / 1000f)
                val maxWeeklyK = String.format(Locale.US, "%.0fk", maxWeeklyTokens / 1000f)

                Text(
                    text = "$weeklyK de $maxWeeklyK tokens",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { (pctWeekly / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Calculator & Quick Log Toggle Button
        OutlinedButton(
            onClick = { showCalculator = !showCalculator },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_toggle_calculator"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (showCalculator) "Ocultar Calculador de Tokens" else "🧮 Simular / Registrar Consumo Manual")
        }

        // Token Calculator Section
        AnimatedVisibility(visible = showCalculator) {
            TokenCalculatorCard(onLogUsage = { model, pText, cText, summary ->
                viewModel.addManualTokenUsage(model, pText, cText, summary)
                Toast.makeText(context, "✅ Consumo registrado con éxito", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenCalculatorCard(
    onLogUsage: (model: String, promptText: String, completionText: String, summary: String) -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    var completionInput by remember { mutableStateOf("") }
    var summaryInput by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("gpt-4o") }
    var expandedModelMenu by remember { mutableStateOf(false) }

    val models = listOf("gpt-4o", "o3-mini", "gpt-4o-mini", "gpt-4")

    val estPromptTokens = (promptInput.length / 3.8).toInt().coerceAtLeast(0)
    val estCompletionTokens = (completionInput.length / 3.8).toInt().coerceAtLeast(0)
    val estTotalTokens = estPromptTokens + estCompletionTokens

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calculator_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Calculador Rápido de Tokens",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Model Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedModelMenu,
                onExpandedChange = { expandedModelMenu = !expandedModelMenu }
            ) {
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Modelo ChatGPT") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModelMenu) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedModelMenu,
                    onDismissRequest = { expandedModelMenu = false }
                ) {
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                selectedModel = model
                                expandedModelMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                label = { Text("Texto / Prompt de entrada") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            OutlinedTextField(
                value = completionInput,
                onValueChange = { completionInput = it },
                label = { Text("Respuesta / Salida del modelo") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            OutlinedTextField(
                value = summaryInput,
                onValueChange = { summaryInput = it },
                label = { Text("Descripción / Nota (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Tokens estimados:", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "$estTotalTokens tokens",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryEmerald
                )
            }

            Button(
                onClick = {
                    onLogUsage(selectedModel, promptInput, completionInput, summaryInput)
                    promptInput = ""
                    completionInput = ""
                    summaryInput = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_log_manual_usage"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                enabled = promptInput.isNotBlank() || completionInput.isNotBlank()
            ) {
                Text("Registrar Consumo en Historial")
            }
        }
    }
}
