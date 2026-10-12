package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MemoryEntity
import com.example.data.model.AssistantLanguage
import com.example.data.model.RobotAccent
import com.example.data.model.RobotExpression
import com.example.ui.components.NovaRobotAvatar
import com.example.ui.theme.NovaThemeMode
import com.example.viewmodel.NovaScreen
import com.example.viewmodel.NovaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val robotExpression by viewModel.robotExpression.collectAsState()
    val robotAccent by viewModel.robotAccent.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val lowPerf by viewModel.lowPerformanceMode.collectAsState()
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val forceOffline by viewModel.forceOffline.collectAsState()
    val memories by viewModel.memories.collectAsState()

    var showAddMemoryDialog by remember { mutableStateOf(false) }

    if (showAddMemoryDialog) {
        var keyInput by remember { mutableStateOf("") }
        var valInput by remember { mutableStateOf("") }
        var catInput by remember { mutableStateOf("Preferences") }

        AlertDialog(
            onDismissRequest = { showAddMemoryDialog = false },
            title = { Text("Add Approved Memory", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Key (e.g. Preferred Name, Occupation)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("memory_key_input")
                    )
                    OutlinedTextField(
                        value = valInput,
                        onValueChange = { valInput = it },
                        label = { Text("Memory Value (e.g. Alex, Software Engineer)") },
                        modifier = Modifier.fillMaxWidth().testTag("memory_val_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (keyInput.isNotBlank() && valInput.isNotBlank()) {
                            viewModel.addMemory(keyInput, valInput, catInput)
                            showAddMemoryDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_memory_button")
                ) {
                    Text("Save Memory")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Settings & Customization", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(NovaScreen.DASHBOARD) },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Robot Appearance & Live Preview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Robot Avatar Customizer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NovaRobotAvatar(
                            size = 110.dp,
                            expression = robotExpression,
                            accent = robotAccent,
                            lowPerformanceMode = lowPerf
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Accent Color Palette
                        Text(
                            text = "Core Accent Color",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RobotAccent.values().forEach { accent ->
                                val isSelected = robotAccent == accent
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(accent.color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.setRobotAccent(accent) }
                                        .testTag("accent_color_${accent.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Expressions
                        Text(
                            text = "Robot Expression",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(RobotExpression.values()) { expr ->
                                FilterChip(
                                    selected = robotExpression == expr,
                                    onClick = { viewModel.setRobotExpression(expr) },
                                    label = { Text(expr.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("expr_chip_${expr.name}")
                                )
                            }
                        }
                    }
                }
            }

            // 2. UI Theme & Performance Mode
            item {
                Text(
                    text = "Display & Performance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Theme Style", fontWeight = FontWeight.SemiBold)
                                Text("Select app color scheme", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                NovaThemeMode.values().forEach { mode ->
                                    FilterChip(
                                        selected = themeMode == mode,
                                        onClick = { viewModel.setThemeMode(mode) },
                                        label = {
                                            Text(
                                                when (mode) {
                                                    NovaThemeMode.FUTURISTIC -> "Neon"
                                                    NovaThemeMode.DEEP_SPACE -> "Space"
                                                    NovaThemeMode.CLEAN_LIGHT -> "Light"
                                                },
                                                fontSize = 11.sp
                                            )
                                        },
                                        modifier = Modifier.testTag("theme_chip_${mode.name}")
                                    )
                                }
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Low-Performance Animation Mode", fontWeight = FontWeight.SemiBold)
                                Text("Reduces glowing radial particles for smoother operation on older devices", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = lowPerf,
                                onCheckedChange = { viewModel.toggleLowPerformanceMode() },
                                modifier = Modifier.testTag("low_perf_switch")
                            )
                        }
                    }
                }
            }

            // 3. Language & Speech Voice
            item {
                Text(
                    text = "Language & Voice Engine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Active Language", fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistantLanguage.values().forEach { lang ->
                                FilterChip(
                                    selected = selectedLang == lang,
                                    onClick = { viewModel.setLanguage(lang) },
                                    label = { Text(lang.displayName, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f).testTag("settings_lang_${lang.code}")
                                )
                            }
                        }
                    }
                }
            }

            // 4. User-Approved Memory Bank
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User Memory Bank (AI Context)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { showAddMemoryDialog = true },
                        modifier = Modifier.testTag("add_memory_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Memory")
                    }
                }
            }

            if (memories.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "No saved memories. Add preferences like your preferred name or interests to give NOVA personal context with your consent.",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(memories) { mem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mem.keyName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(mem.memoryValue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(
                                onClick = { viewModel.deleteMemory(mem) },
                                modifier = Modifier.size(32.dp).testTag("delete_memory_${mem.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                item {
                    TextButton(
                        onClick = { viewModel.clearAllMemories() },
                        modifier = Modifier.fillMaxWidth().testTag("clear_all_memories_button")
                    ) {
                        Text("Clear All Approved Memories", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // 5. Privacy Policy & Google Play Disclosures
            item {
                Text(
                    text = "Privacy & Play Store Compliance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("• Data Storage: Conversations, flashcards, and memories reside securely on your local device via Room Database.", style = MaterialTheme.typography.bodySmall)
                        Text("• Zero-Permission Media: Media access strictly uses the zero-permission Android Photo Picker without broad storage permissions.", style = MaterialTheme.typography.bodySmall)
                        Text("• Speech Privacy: Microphone input is activated strictly on-demand by the user and never listens in the background.", style = MaterialTheme.typography.bodySmall)
                        Text("• System Sandboxing: Phone tools use standard Android intents with user review and never execute silent background operations.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NOVA AI Assistant • v1.0 • Built for Android",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
