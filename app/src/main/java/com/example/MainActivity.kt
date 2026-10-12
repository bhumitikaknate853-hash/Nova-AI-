package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.NovaTheme
import com.example.viewmodel.NovaScreen
import com.example.viewmodel.NovaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NovaViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            // Handle system back gesture
            BackHandler(enabled = currentScreen != NovaScreen.DASHBOARD) {
                viewModel.navigateTo(NovaScreen.DASHBOARD)
            }

            NovaTheme(themeMode = themeMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Show bottom navigation on primary screens
                        if (currentScreen != NovaScreen.VOICE) {
                            NavigationBar(
                                modifier = Modifier.testTag("nova_bottom_nav"),
                                tonalElevation = 6.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == NovaScreen.DASHBOARD,
                                    onClick = { viewModel.navigateTo(NovaScreen.DASHBOARD) },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                    label = { Text("Home") },
                                    modifier = Modifier.testTag("nav_item_dashboard")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == NovaScreen.CHAT,
                                    onClick = { viewModel.navigateTo(NovaScreen.CHAT) },
                                    icon = { Icon(Icons.Default.Chat, contentDescription = "Ask NOVA") },
                                    label = { Text("Chat") },
                                    modifier = Modifier.testTag("nav_item_chat")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == NovaScreen.VOICE,
                                    onClick = { viewModel.navigateTo(NovaScreen.VOICE) },
                                    icon = { Icon(Icons.Default.Mic, contentDescription = "Voice Assistant") },
                                    label = { Text("Voice") },
                                    modifier = Modifier.testTag("nav_item_voice")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == NovaScreen.PHONE_TOOLS,
                                    onClick = { viewModel.navigateTo(NovaScreen.PHONE_TOOLS) },
                                    icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = "Phone Tools") },
                                    label = { Text("Tools") },
                                    modifier = Modifier.testTag("nav_item_tools")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == NovaScreen.STUDY_STUDIO,
                                    onClick = { viewModel.navigateTo(NovaScreen.STUDY_STUDIO) },
                                    icon = { Icon(Icons.Default.School, contentDescription = "Study Studio") },
                                    label = { Text("Study") },
                                    modifier = Modifier.testTag("nav_item_study")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)
                    when (currentScreen) {
                        NovaScreen.DASHBOARD -> DashboardScreen(viewModel, screenModifier)
                        NovaScreen.CHAT -> ChatScreen(viewModel, screenModifier)
                        NovaScreen.VOICE -> VoiceScreen(viewModel, screenModifier)
                        NovaScreen.PHONE_TOOLS -> PhoneToolsScreen(viewModel, screenModifier)
                        NovaScreen.WEB_SEARCH -> WebSearchScreen(viewModel, screenModifier)
                        NovaScreen.STUDY_STUDIO -> StudyScreen(viewModel, screenModifier)
                        NovaScreen.VISION_STUDIO -> VisionScreen(viewModel, screenModifier)
                        NovaScreen.SETTINGS -> SettingsScreen(viewModel, screenModifier)
                    }
                }
            }
        }
    }
}
