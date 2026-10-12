package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AssistantLanguage
import com.example.data.model.RobotExpression
import com.example.ui.components.NovaRobotAvatar
import com.example.ui.components.VoiceWaveVisualizer
import com.example.viewmodel.NovaScreen
import com.example.viewmodel.NovaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceManager = viewModel.voiceManager
    val isListening by voiceManager.isListening.collectAsState()
    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val spokenText by voiceManager.spokenText.collectAsState()
    val audioLevel by voiceManager.audioLevel.collectAsState()
    val statusMsg by voiceManager.voiceStatusMessage.collectAsState()
    val robotAccent by viewModel.robotAccent.collectAsState()
    val lowPerf by viewModel.lowPerformanceMode.collectAsState()
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            viewModel.startVoiceInput()
        }
    }

    // Glowing pulse for mic button
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        modifier = modifier.testTag("voice_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Voice Assistant", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            voiceManager.stopListening()
                            voiceManager.stopSpeaking()
                            viewModel.navigateTo(NovaScreen.DASHBOARD)
                        },
                        modifier = Modifier.testTag("voice_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSpeaking) {
                        IconButton(
                            onClick = { voiceManager.stopSpeaking() },
                            modifier = Modifier.testTag("voice_stop_speaking_button")
                        ) {
                            Icon(
                                Icons.Default.VolumeOff,
                                contentDescription = "Mute Speaking",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Language selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AssistantLanguage.values().forEach { lang ->
                    FilterChip(
                        selected = selectedLang == lang,
                        onClick = { viewModel.setLanguage(lang) },
                        label = { Text(lang.displayName, fontSize = 12.sp) },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .testTag("voice_lang_${lang.code}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Large Central Animated Robot Avatar
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                NovaRobotAvatar(
                    size = 190.dp,
                    expression = when {
                        isSpeaking -> RobotExpression.SPEAKING
                        isListening -> RobotExpression.SURPRISED
                        isGenerating -> RobotExpression.THINKING
                        else -> RobotExpression.HAPPY
                    },
                    accent = robotAccent,
                    isSpeaking = isSpeaking,
                    audioLevel = audioLevel,
                    lowPerformanceMode = lowPerf
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Status Badge & Description
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when {
                    isListening -> MaterialTheme.colorScheme.primaryContainer
                    isSpeaking -> MaterialTheme.colorScheme.secondaryContainer
                    isGenerating -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = when {
                        isListening -> "🎙️ Listening to your voice..."
                        isSpeaking -> "🔊 NOVA is speaking aloud..."
                        isGenerating -> "⚡ Processing AI response..."
                        else -> "Tap the microphone to speak"
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Interactive Sine Wave Visualizer
            VoiceWaveVisualizer(
                isActive = isListening || isSpeaking,
                audioLevel = audioLevel,
                primaryColor = robotAccent.color,
                secondaryColor = MaterialTheme.colorScheme.primary,
                height = 54.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Transcription Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_transcription_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (spokenText.isNotBlank()) "\"$spokenText\"" else (statusMsg ?: "Speak naturally in English, Hindi, or Marathi"),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Big Glowing Microphone Control Button
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .testTag("voice_mic_container"),
                contentAlignment = Alignment.Center
            ) {
                if (isListening && !lowPerf) {
                    Box(
                        modifier = Modifier
                            .size((96 * pulseScale).dp)
                            .clip(CircleShape)
                            .background(robotAccent.color.copy(alpha = 0.25f))
                    )
                }

                FilledIconButton(
                    onClick = {
                        if (!hasAudioPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (isListening) {
                                viewModel.stopVoiceInput()
                            } else {
                                viewModel.startVoiceInput()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(76.dp)
                        .testTag("voice_toggle_mic_button"),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isListening) MaterialTheme.colorScheme.error else robotAccent.color
                    )
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Toggle Microphone",
                        modifier = Modifier.size(36.dp),
                        tint = if (isListening) MaterialTheme.colorScheme.onError else Color(0xFF001B24)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
