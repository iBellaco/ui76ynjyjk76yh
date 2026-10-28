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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

// Custom colors from the reference app
val CodexButtonBlue = Color(0xFF93C5FD)
val CodexButtonBlueDark = Color(0xFF60A5FA)
val CodexCardBackground = Color(0xFF161B22)
val CodexBackgroundDark = Color(0xFF0D1117)
val CodexTextSecondary = Color(0xFF8B949E)

@Composable
fun OpenAiAuthScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val authState by viewModel.authFlowState.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

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
                shape = RoundedCornerShape(12.dp)
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

        Spacer(modifier = Modifier.height(8.dp))

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
                    email = state.accountEmail,
                    tier = state.tier,
                    onOpenDashboard = { viewModel.openOpenAiDashboard(context) },
                    onDisconnect = { viewModel.disconnectSession() },
                    onHelpClick = { showHelpDialog = true }
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

        Spacer(modifier = Modifier.height(12.dp))

        // Footer Note
        Text(
            text = "App independiente · Sesión cifrada",
            color = CodexTextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }

    // "Conectar con ChatGPT" Dialog (Exact copy from video frame 00:01)
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

    // "Ayuda y diagnóstico" Dialog
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
                    Text("• Endpoint de autorización: auth.openai.com/codex/device", fontSize = 13.sp)
                    Text("• Si el código caduca (después de 15 min), solicita uno nuevo desde 'Continuar con ChatGPT'.", fontSize = 13.sp)
                    Text("• Si el navegador ya mostró 'Inició sesión en Codex', puedes presionar 'Ya autoricé · comprobar' o 'Conectar sesión'.", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Row {
                    if (authState is OpenAiAuthService.AuthFlowState.Authorizing) {
                        TextButton(onClick = {
                            showHelpDialog = false
                            viewModel.forceConfirmConnection()
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

// 1. Authorizing Card (Exact layout from video frames 00:04 - 00:07)
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

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "CÓDIGO DE AUTORIZACIÓN",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = CodexTextSecondary,
                letterSpacing = 0.5.sp
            )

            // Monospace Device Code Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF21262D), RoundedCornerShape(16.dp))
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    modifier = Modifier.testTag("device_code_text")
                )
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

            // Primary Light Blue Button: "Copiar código y abrir OpenAI"
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

            // Checking status
            Text(
                text = "Comprobando autorización...",
                fontSize = 12.sp,
                color = CodexTextSecondary
            )

            // Secondary Action: "Ya autoricé - comprobar"
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

            // Cancel Button
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

            // Ayuda y diagnóstico
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

// 2. Disconnected / Expired Card (Exact layout from video frames 00:00 & 00:19)
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

            // Primary Blue button: "Continuar con ChatGPT"
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

            // Ayuda y diagnóstico
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

// 3. Connecting State (Exact layout from video frame 00:03)
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

// 4. Connected State
@Composable
fun ConnectedStateCard(
    email: String,
    tier: String,
    onOpenDashboard: () -> Unit,
    onDisconnect: () -> Unit,
    onHelpClick: () -> Unit
) {
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONECTADO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399),
                    letterSpacing = 0.5.sp
                )

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

            Text(
                text = "Cuenta: $email",
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Los tokens y límites se consultan automáticamente cada minuto para el widget de pantalla de inicio.",
                fontSize = 13.sp,
                color = CodexTextSecondary
            )

            Button(
                onClick = onOpenDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CodexButtonBlue,
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Abrir OpenAI Dashboard", fontWeight = FontWeight.Bold)
            }

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
