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
                var selectedTabIndex by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (selectedTabIndex) {
                                        0 -> "KWGT ChatGPT Monitor"
                                        1 -> "Historial de Tokens"
                                        2 -> "Sesión OpenAI & Auth"
                                        else -> "KWGT Studio"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        viewModel.copyAuthCode(context)
                                        Toast.makeText(context, "📋 Código de Autorización Copiado", Toast.LENGTH_SHORT).show()
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
                                icon = { Icon(Icons.Default.Widgets, contentDescription = "Monitor") },
                                label = { Text("Monitor") },
                                modifier = Modifier.testTag("nav_item_home"),
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
                                icon = { Icon(Icons.Default.Security, contentDescription = "OpenAI Auth") },
                                label = { Text("OpenAI") },
                                modifier = Modifier.testTag("nav_item_auth"),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryEmerald,
                                    indicatorColor = PrimaryEmerald.copy(alpha = 0.2f)
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTabIndex == 3,
                                onClick = { selectedTabIndex = 3 },
                                icon = { Icon(Icons.Default.Palette, contentDescription = "Studio") },
                                label = { Text("KWGT Studio") },
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
                            0 -> HomeScreen(viewModel)
                            1 -> TokenHistoryScreen(viewModel)
                            2 -> OpenAiAuthScreen(viewModel)
                            3 -> WidgetStudioScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
