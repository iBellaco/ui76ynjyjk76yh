package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OpenAiAuthScreen
import com.example.ui.screens.TokenHistoryScreen
import com.example.ui.screens.WidgetStudioScreen
import com.example.ui.theme.KwgtChatGPTTheme
import com.example.ui.theme.PrimaryEmerald

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KwgtChatGPTTheme {
                val context = LocalContext.current
                // Default tab is 0 (OpenAI) as requested
                var selectedTabIndex by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (selectedTabIndex) {
                                        0 -> "Codex · OpenAI"
                                        1 -> "Historial de Tokens"
                                        else -> "Paquete KWGT Studio"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            },
                            actions = {
                                androidx.compose.material3.Surface(
                                    color = PrimaryEmerald.copy(alpha = 0.15f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = "v1.0.1 Patch",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryEmerald,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.copyAuthCode(context)
                                        Toast.makeText(context, "Código copiado. Pégalo en OpenAI.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("btn_topbar_copy_code")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiar Código",
                                        tint = PrimaryEmerald
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("bottom_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                icon = { Icon(Icons.Default.Security, contentDescription = "OpenAI") },
                                label = { Text("OpenAI") },
                                modifier = Modifier.testTag("nav_item_auth"),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryEmerald,
                                    indicatorColor = PrimaryEmerald.copy(alpha = 0.2f)
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                                label = { Text("Historial") },
                                modifier = Modifier.testTag("nav_item_history"),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryEmerald,
                                    indicatorColor = PrimaryEmerald.copy(alpha = 0.2f)
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTabIndex == 2,
                                onClick = { selectedTabIndex = 2 },
                                icon = { Icon(Icons.Default.Palette, contentDescription = "Paquete KWGT") },
                                label = { Text("Paquete KWGT") },
                                modifier = Modifier.testTag("nav_item_studio"),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryEmerald,
                                    indicatorColor = PrimaryEmerald.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTabIndex) {
                            0 -> OpenAiAuthScreen(viewModel)
                            1 -> TokenHistoryScreen(viewModel)
                            2 -> WidgetStudioScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
