package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OpenAiAuthService
import com.example.ui.MainViewModel
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.SecondaryMint
import com.example.ui.theme.SurfaceDark
import java.util.Locale

@Composable
fun WidgetStudioScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val clockText by viewModel.currentTimeText.collectAsState()
    val total5hReqs by viewModel.total5hReqs.collectAsState()
    val totalWeeklyTokens by viewModel.totalWeeklyTokens.collectAsState()
    val max5hReqs by viewModel.fiveHourLimit.collectAsState()
    val maxWeeklyTokens by viewModel.weeklyLimit.collectAsState()
    val selectedTheme by viewModel.widgetTheme.collectAsState()
    val authCode by viewModel.authCode.collectAsState()

    var slider5hVal by remember(max5hReqs) { mutableFloatStateOf(max5hReqs.toFloat()) }
    var sliderWeeklyVal by remember(maxWeeklyTokens) { mutableFloatStateOf(maxWeeklyTokens.toFloat() / 1000f) }

    val themes = listOf("KWGT Dark Minimal", "Cyberpunk Mint", "OLED Black", "Glassmorphism")

    val widgetBgColor = when (selectedTheme) {
        "OLED Black" -> Color.Black
        "Cyberpunk Mint" -> Color(0xFF064E3B)
        "Glassmorphism" -> Color(0xFF1E293B).copy(alpha = 0.85f)
        else -> ObsidianDark
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Widgets, contentDescription = null, tint = PrimaryEmerald)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "KWGT Studio & Personalización",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configura el Widget de Pantalla de Inicio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Live Widget Interactive Preview Box
        Text(
            text = "VISTA PREVIA DEL WIDGET",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryEmerald,
            letterSpacing = 1.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(widgetBgColor)
                .border(1.dp, PrimaryEmerald.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("widget_live_preview_box")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = clockText.take(5).ifEmpty { "14:30" },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Mié, 28 Oct",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryEmerald))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "5h: Óptimo", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 5h Gauge Progress
                val pct5h = if (slider5hVal > 0) ((total5hReqs / slider5hVal) * 100).toInt().coerceIn(0, 100) else 0
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Uso ChatGPT 5h", color = PrimaryEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$total5hReqs / ${slider5hVal.toInt()} reqs ($pct5h%)", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF334155))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pct5h / 100f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(PrimaryEmerald)
                        )
                    }
                }

                // Weekly Progress
                val weeklyK = String.format(Locale.US, "%.1fk", totalWeeklyTokens / 1000f)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tokens Semanales", color = SecondaryMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$weeklyK / ${sliderWeeklyVal.toInt()}k", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF334155))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(((totalWeeklyTokens / 1000f) / sliderWeeklyVal).coerceIn(0f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(SecondaryMint)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📋 Copiar Code", color = PrimaryEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                    }
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🌐 OpenAI", color = SecondaryMint, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }

        // Theme Customizer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = PrimaryEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Estilo Visual del Widget", fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    themes.forEach { themeName ->
                        FilterChip(
                            selected = selectedTheme == themeName,
                            onClick = { viewModel.updateWidgetTheme(themeName) },
                            label = { Text(themeName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryEmerald,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // Limit Adjuster Sliders Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = PrimaryEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajustes de Límites de Consumo", fontWeight = FontWeight.Bold)
                }

                // 5-Hour Slider
                Column {
                    Text("Límite de Peticiones en 5h: ${slider5hVal.toInt()} reqs", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = slider5hVal,
                        onValueChange = { slider5hVal = it },
                        valueRange = 20f..300f,
                        onValueChangeFinished = { viewModel.update5hLimit(slider5hVal.toInt()) },
                        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = PrimaryEmerald, activeTrackColor = PrimaryEmerald)
                    )
                }

                // Weekly Slider
                Column {
                    Text("Límite Semanal de Tokens: ${sliderWeeklyVal.toInt()}k tokens", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = sliderWeeklyVal,
                        onValueChange = { sliderWeeklyVal = it },
                        valueRange = 100f..2000f,
                        onValueChangeFinished = { viewModel.updateWeeklyLimit((sliderWeeklyVal * 1000).toInt()) },
                        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = SecondaryMint, activeTrackColor = SecondaryMint)
                    )
                }
            }
        }

        // KWGT Kustom Formulas Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = PrimaryEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fórmulas de Integración KWGT Kustom", fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Si usas la app KWGT Kustom Widget, puedes leer los datos transmitidos usando estas fórmulas:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val formulaList = listOf(
                    "Peticiones 5h" to "\$br(kwgt, chatgpt_5h_reqs)\$",
                    "Tokens Semanales" to "\$br(kwgt, chatgpt_weekly_tokens)\$",
                    "Auth Code" to "\$br(kwgt, chatgpt_auth_code)\$"
                )

                formulaList.forEach { (label, formula) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(formula, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = PrimaryEmerald)
                        }
                        IconButton(onClick = {
                            OpenAiAuthService.copyToClipboard(context, formula, label)
                            Toast.makeText(context, "Fórmula copiada", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
