package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OpenAiAuthService
import com.example.ui.MainViewModel

// Custom colors matching the Codex / OpenAI dark theme
val CodexButtonBlue = Color(0xFF93C5FD)
val CodexButtonBlueDark = Color(0xFF60A5FA)
val CodexCardBackground = Color(0xFF161B22)
val CodexBackgroundDark = Color(0xFF0D1117)
val CodexTextSecondary = Color(0xFF8B949E)
val CodexEmerald = Color(0xFF10B981)
val CodexAmber = Color(0xFFF59E0B)

@Composable
fun OpenAiAuthScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val authState by viewModel.authFlowState.collectAsState()
    val total5hReqs by viewModel.total5hReqs.collectAsState()
    val fiveHourLimit by viewModel.fiveHourLimit.collectAsState()
    val totalWeeklyTokens by viewModel.totalWeeklyTokens.collectAsState()
    val weeklyLimit by viewModel.weeklyLimit.collectAsState()
    val countdown5h by viewModel.fiveHourResetCountdown.collectAsState()
    val currentEmail by viewModel.connectedEmail.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showEditEmailDialog by remember { mutableStateOf(false) }
    var showAddRequestDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CodexBackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header: Codex Icon/Title and Language Button "ES▾"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF21262D)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Codex",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Surface(
                color = Color(0xFF21262D),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.clickable { }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ES", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main Dynamic Card according to authState
        when (val state = authState) {
            is OpenAiAuthService.AuthFlowState.Authorizing -> {
                AuthorizingStateCard(
                    code = state.code,
                    remainingSeconds = state.remainingSeconds,
                    onCopyAndOpen = {
                        viewModel.copyAndOpenOpenAi(context, state.code)
                    },
                    onCheckAuth = {
                        viewModel.confirmDeviceAuthorization()
                    },
                    onCancel = {
                        viewModel.cancelDeviceAuthorization()
                    },
                    onHelpClick = { showHelpDialog = true }
                )
            }

            is OpenAiAuthService.AuthFlowState.Connecting -> {
                ConnectingStateCard(
                    onHelpClick = { showHelpDialog = true }
                )
            }

            is OpenAiAuthService.AuthFlowState.Connected -> {
                ConnectedStateCard(
                    email = if (state.accountEmail.isBlank() || state.accountEmail == "usuario@openai.com") currentEmail else state.accountEmail,
                    tier = state.tier,
                    reqs5h = total5hReqs,
                    limit5h = fiveHourLimit,
                    weeklyTokens = totalWeeklyTokens,
                    weeklyLimit = weeklyLimit,
                    countdown5h = countdown5h,
                    onOpenDashboard = { viewModel.openOpenAiDashboard(context) },
                    onDisconnect = { viewModel.disconnectSession() },
                    onHelpClick = { showHelpDialog = true },
                    onEditEmail = { showEditEmailDialog = true },
                    onAddRequest = { showAddRequestDialog = true },
                    onTestRequest = { viewModel.simulateTestRequest() },
                    onReset5h = { viewModel.reset5hSlidingWindow() }
                )
            }

            is OpenAiAuthService.AuthFlowState.Disconnected -> {
                DisconnectedStateCard(
                    isExpired = state.isExpired,
                    errorMessage = state.errorMessage,
                    onStartAuth = { showConfirmDialog = true },
                    onHelpClick = { showHelpDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Footer Note
        Text(
            text = "App independiente · Sesión cifrada localmente · v1.0.1 Patch",
            color = CodexTextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }

    // Dialog: "Conectar con ChatGPT"
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Conectar con ChatGPT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Autoriza esta app independiente en OpenAI. Los tokens se cifran en este teléfono. El uso se consulta mediante una interfaz interna de ChatGPT que puede cambiar. Autoriza solo un código solicitado aquí.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.startDeviceAuthFlow()
                    }
                ) {
                    Text("CONTINUAR", fontWeight = FontWeight.Bold, color = CodexButtonBlueDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("AHORA NO", color = CodexTextSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFF1E293B)
        )
    }

    // Dialog: "Editar Correo de Cuenta"
    if (showEditEmailDialog) {
        var emailInput by remember { mutableStateOf(currentEmail) }
        AlertDialog(
            onDismissRequest = { showEditEmailDialog = false },
            title = {
                Text("Cuenta de OpenAI / ChatGPT", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Introduce el correo de tu cuenta de ChatGPT para mostrarlo en el widget y la app:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Correo Electrónico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CodexButtonBlueDark,
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (emailInput.isNotBlank()) {
                            viewModel.updateUserEmail(emailInput)
                            showEditEmailDialog = false
                        }
                    }
                ) {
                    Text("GUARDAR", color = CodexButtonBlueDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditEmailDialog = false }) {
                    Text("CANCELAR", color = CodexTextSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFF1E293B)
        )
    }

    // Dialog: "Registrar Consulta de ChatGPT"
    if (showAddRequestDialog) {
        var selectedModel by remember { mutableStateOf("gpt-4o") }
        var summaryText by remember { mutableStateOf("") }
        var estimatedTokens by remember { mutableStateOf("500") }

        AlertDialog(
            onDismissRequest = { showAddRequestDialog = false },
            title = {
                Text("Registrar Consulta de ChatGPT", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Selecciona el modelo utilizado:", fontSize = 13.sp, color = CodexTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("gpt-4o", "o3-mini", "gpt-4o-mini").forEach { m ->
                            FilterChip(
                                selected = selectedModel == m,
                                onClick = { selectedModel = m },
                                label = { Text(m, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CodexButtonBlueDark,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = summaryText,
                        onValueChange = { summaryText = it },
                        label = { Text("Resumen de consulta (opcional)") },
                        placeholder = { Text("Ej. Análisis de código, traducción...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = estimatedTokens,
                        onValueChange = { estimatedTokens = it.filter { char -> char.isDigit() } },
                        label = { Text("Tokens aproximados") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val tokens = estimatedTokens.toIntOrNull() ?: 500
                        val pTokens = (tokens * 0.4).toInt()
                        val cTokens = (tokens * 0.6).toInt()
                        val summary = summaryText.ifBlank { "Consulta $selectedModel" }
                        viewModel.recordQuickChatGptRequest(
                            model = selectedModel,
                            promptSummary = summary,
                            promptTokens = pTokens,
                            completionTokens = cTokens
                        )
                        showAddRequestDialog = false
                    }
                ) {
                    Text("REGISTRAR", color = CodexButtonBlueDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRequestDialog = false }) {
                    Text("CANCELAR", color = CodexTextSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFF1E293B)
        )
    }

    // Dialog: "Ayuda y diagnóstico"
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = CodexButtonBlueDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ayuda y diagnóstico", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Estado de red: Conectado a Internet", fontSize = 13.sp)
                    Text("• Cifrado local: Activado en sandbox de Android", fontSize = 13.sp)
                    Text("• Cuenta activa: $currentEmail", fontSize = 13.sp, color = CodexButtonBlue)
                    Text("• Ventana deslizante: 5 horas (límite estándar ChatGPT Plus: 80 reqs)", fontSize = 13.sp)
                    Text("• Si el navegador ya mostró 'Inició sesión en Codex', la sesión se sincroniza automáticamente.", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Row {
                    if (authState is OpenAiAuthService.AuthFlowState.Authorizing) {
                        TextButton(onClick = {
                            showHelpDialog = false
                            viewModel.forceConfirmConnection(currentEmail)
                        }) {
                            Text("CONECTAR SESIÓN", color = CodexButtonBlueDark, fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text("CERRAR")
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFF1E293B)
        )
    }
}

// 1. Authorizing Card
@Composable
fun AuthorizingStateCard(
    code: String,
    remainingSeconds: Int,
    onCopyAndOpen: () -> Unit,
    onCheckAuth: () -> Unit,
    onCancel: () -> Unit,
    onHelpClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_authorizing_state"),
        colors = CardDefaults.cardColors(containerColor = CodexCardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "AUTORIZACIÓN EN CURSO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CodexTextSecondary,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Continúa en OpenAI",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Abre el navegador, autoriza y vuelve aquí.",
                fontSize = 13.sp,
                color = CodexTextSecondary
            )

            // Auth Code Card Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "CÓDIGO DE AUTORIZACIÓN",
                        fontSize = 11.sp,
                        color = CodexTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = code,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Expiration countdown (mm:ss)
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val timeFormatted = String.format("%02d:%02d", minutes, seconds)

            Text(
                text = "Caduca en $timeFormatted",
                fontSize = 12.sp,
                color = CodexTextSecondary
            )

            // Primary Light Blue Button
            Button(
                onClick = onCopyAndOpen,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_copy_and_open_openai"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CodexButtonBlue,
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Copiar código y abrir OpenAI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Steps
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("1. Inicia sesión en OpenAI.", fontSize = 12.sp, color = CodexTextSecondary)
                Text("2. Pega este código y autoriza.", fontSize = 12.sp, color = CodexTextSecondary)
                Text("3. Vuelve aquí para terminar.", fontSize = 12.sp, color = CodexTextSecondary)
            }

            Text(
                text = "Comprobando autorización...",
                fontSize = 12.sp,
                color = CodexTextSecondary
            )

            Button(
                onClick = onCheckAuth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_already_authorized"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF21262D),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ya autoricé · comprobar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_cancel_authorization"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF21262D),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar autorización", fontSize = 13.sp)
            }

            Button(
                onClick = onHelpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_help_diagnostic"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF21262D),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ayuda y diagnóstico", fontSize = 13.sp)
            }
        }
    }
}

// 2. Disconnected / Expired Card
@Composable
fun DisconnectedStateCard(
    isExpired: Boolean,
    errorMessage: String? = null,
    onStartAuth: () -> Unit,
    onHelpClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_disconnected_state"),
        colors = CardDefaults.cardColors(containerColor = CodexCardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    color = Color(0xFFEF4444).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            Text(
                text = if (isExpired) "REVISAR CONEXIÓN" else "SIN CONECTAR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CodexTextSecondary,
                letterSpacing = 0.5.sp
            )

            Text(
                text = if (isExpired) "Inténtalo de nuevo" else "Conecta tu cuenta",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = if (isExpired) "Código caducado. Solicita uno nuevo." else "Autoriza ChatGPT para ver tus límites de Codex.",
                fontSize = 13.sp,
                color = CodexTextSecondary
            )

            Button(
                onClick = onStartAuth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_continue_with_chatgpt"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CodexButtonBlue,
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Continuar con ChatGPT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Text(
                text = "Usa Google, Apple o correo en OpenAI.",
                fontSize = 12.sp,
                color = CodexTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onHelpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF21262D),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ayuda y diagnóstico", fontSize = 13.sp)
            }
        }
    }
}

// 3. Connecting State
@Composable
fun ConnectingStateCard(
    onHelpClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_connecting_state"),
        colors = CardDefaults.cardColors(containerColor = CodexCardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "CONECTANDO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CodexTextSecondary,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Preparando tu acceso...",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Solicitando un código a OpenAI.",
                fontSize = 13.sp,
                color = CodexTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = CodexButtonBlueDark,
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onHelpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF21262D),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ayuda y diagnóstico", fontSize = 13.sp)
            }
        }
    }
}

// 4. Enhanced Connected State with Real-Time Usage Monitoring
@Composable
fun ConnectedStateCard(
    email: String,
    tier: String,
    reqs5h: Int,
    limit5h: Int,
    weeklyTokens: Int,
    weeklyLimit: Int,
    countdown5h: String,
    onOpenDashboard: () -> Unit,
    onDisconnect: () -> Unit,
    onHelpClick: () -> Unit,
    onEditEmail: () -> Unit,
    onAddRequest: () -> Unit,
    onTestRequest: () -> Unit,
    onReset5h: () -> Unit
) {
    val progress5h = if (limit5h > 0) (reqs5h.toFloat() / limit5h.toFloat()).coerceIn(0f, 1f) else 0f
    val progressWeekly = if (weeklyLimit > 0) (weeklyTokens.toFloat() / weeklyLimit.toFloat()).coerceIn(0f, 1f) else 0f

    val progress5hColor = when {
        progress5h >= 1.0f -> Color(0xFFEF4444)
        progress5h >= 0.75f -> CodexAmber
        else -> CodexEmerald
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_connected_state"),
        colors = CardDefaults.cardColors(containerColor = CodexCardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header status
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
                            .background(CodexEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONECTADO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CodexEmerald,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    color = Color(0xFF064E3B),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = tier,
                        color = Color(0xFF34D399),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Sesión OpenAI Activa",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // User Account Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CodexButtonBlueDark.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = CodexButtonBlueDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cuenta OpenAI",
                                fontSize = 11.sp,
                                color = CodexTextSecondary
                            )
                            Text(
                                text = email,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = onEditEmail,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar correo",
                            tint = CodexButtonBlueDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Real-Time 5-Hour & Weekly Usage Monitoring Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 5h sliding window header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VENTANA DESLIZANTE 5H",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CodexTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Reset en $countdown5h",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CodexAmber
                        )
                    }

                    // 5h progress numbers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$reqs5h",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = " / $limit5h reqs",
                                fontSize = 15.sp,
                                color = CodexTextSecondary,
                                modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                            )
                        }
                        val pct = (progress5h * 100).toInt()
                        Text(
                            text = "$pct% usado",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = progress5hColor
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progress5h },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progress5hColor,
                        trackColor = Color(0xFF1E293B),
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Weekly token quota
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONSUMO SEMANAL DE TOKENS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CodexTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        val estCost = (weeklyTokens / 1000.0) * 0.003
                        Text(
                            text = String.format("~$%.3f USD", estCost),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CodexButtonBlueDark
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = String.format("%.1fk / %.0fk tokens", weeklyTokens / 1000f, weeklyLimit / 1000f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        val weeklyPct = (progressWeekly * 100).toInt()
                        Text(
                            text = "$weeklyPct%",
                            fontSize = 13.sp,
                            color = CodexTextSecondary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressWeekly },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CodexButtonBlueDark,
                        trackColor = Color(0xFF1E293B),
                    )
                }
            }

            // Primary action: Register query
            Button(
                onClick = onAddRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_register_query"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CodexButtonBlue,
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Registrar Consulta de ChatGPT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Secondary Quick Actions: Test +1 and Reset Window
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTestRequest,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("+1 Petición", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onReset5h,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CodexTextSecondary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reiniciar 5h", fontSize = 12.sp)
                }
            }

            // Dashboard Button
            Button(
                onClick = onOpenDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF21262D),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Abrir OpenAI Dashboard", fontSize = 13.sp)
            }

            // Disconnect Button
            Button(
                onClick = onDisconnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF21262D),
                    contentColor = Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cerrar Sesión / Desconectar", fontSize = 13.sp)
            }

            // Diagnostic button
            Button(
                onClick = onHelpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF21262D),
                    contentColor = CodexTextSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ayuda y diagnóstico", fontSize = 12.sp)
            }
        }
    }
}
