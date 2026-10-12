package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.StudyCardEntity
import com.example.viewmodel.NovaScreen
import com.example.viewmodel.NovaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val studyCards by viewModel.studyCards.collectAsState()
    val quizQuestions by viewModel.currentQuiz.collectAsState()

    var showAddCardDialog by remember { mutableStateOf(false) }

    if (showAddCardDialog) {
        var newTopic by remember { mutableStateOf("") }
        var newQ by remember { mutableStateOf("") }
        var newA by remember { mutableStateOf("") }
        var newCat by remember { mutableStateOf("STEM") }

        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("Add Flashcard", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTopic,
                        onValueChange = { newTopic = it },
                        label = { Text("Topic (e.g. Physics)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_card_topic")
                    )
                    OutlinedTextField(
                        value = newQ,
                        onValueChange = { newQ = it },
                        label = { Text("Question / Concept") },
                        modifier = Modifier.fillMaxWidth().testTag("new_card_question")
                    )
                    OutlinedTextField(
                        value = newA,
                        onValueChange = { newA = it },
                        label = { Text("Answer / Explanation") },
                        modifier = Modifier.fillMaxWidth().testTag("new_card_answer")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newQ.isNotBlank() && newA.isNotBlank()) {
                            viewModel.addStudyCard(
                                topic = newTopic.ifBlank { "General" },
                                question = newQ,
                                answer = newA,
                                category = newCat
                            )
                            showAddCardDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_new_card_button")
                ) {
                    Text("Save Card")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.testTag("study_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Study Assistant Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(NovaScreen.DASHBOARD) },
                        modifier = Modifier.testTag("study_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedTab == 1) {
                        IconButton(
                            onClick = { showAddCardDialog = true },
                            modifier = Modifier.testTag("add_study_card_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Card")
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
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📝 Interactive Quiz", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("🗂️ Flashcards", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("💻 STEM & Code", fontSize = 12.sp) }
                )
            }

            when (selectedTab) {
                0 -> QuizSection(quizQuestions = quizQuestions)
                1 -> FlashcardSection(
                    cards = studyCards,
                    onToggleMastered = { viewModel.toggleCardMastered(it) },
                    onDelete = { viewModel.deleteStudyCard(it.id) }
                )
                2 -> StemHelperSection(
                    onAskPrompt = { prompt ->
                        viewModel.sendMessage(prompt)
                        viewModel.navigateTo(NovaScreen.CHAT)
                    }
                )
            }
        }
    }
}

@Composable
fun QuizSection(quizQuestions: List<com.example.viewmodel.QuizQuestion>) {
    var userAnswers by remember { mutableStateOf(mapOf<Int, Int>()) }
    var submitted by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STEM & Computer Science Quiz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Test your knowledge. Submit to verify answers and see detailed explanations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        items(quizQuestions.size) { index ->
            val q = quizQuestions[index]
            val selectedOption = userAnswers[index]

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Q${index + 1}: ${q.question}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    q.options.forEachIndexed { optIndex, optText ->
                        val isSelected = selectedOption == optIndex
                        val isCorrect = optIndex == q.correctIndex

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (!submitted) {
                                        userAnswers = userAnswers + (index to optIndex)
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                submitted && isCorrect -> Color(0xFF00E676).copy(alpha = 0.2f)
                                submitted && isSelected && !isCorrect -> Color(0xFFFF5252).copy(alpha = 0.2f)
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        if (!submitted) {
                                            userAnswers = userAnswers + (index to optIndex)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = optText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (submitted && isCorrect) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (submitted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Explanation: ${q.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = { submitted = !submitted },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_quiz_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (submitted) "Reset Quiz" else "Check Answers", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FlashcardSection(
    cards: List<StudyCardEntity>,
    onToggleMastered: (StudyCardEntity) -> Unit,
    onDelete: (StudyCardEntity) -> Unit
) {
    if (cards.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No flashcards found. Tap '+' to create your first card!")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cards) { card ->
                FlashcardItem(
                    card = card,
                    onToggleMastered = { onToggleMastered(card) },
                    onDelete = { onDelete(card) }
                )
            }
        }
    }
}

@Composable
fun FlashcardItem(
    card: StudyCardEntity,
    onToggleMastered: () -> Unit,
    onDelete: () -> Unit
) {
    var isFlipped by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isFlipped = !isFlipped }
            .testTag("flashcard_${card.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isMastered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = card.topic,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Row {
                    IconButton(onClick = onToggleMastered, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (card.isMastered) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                            contentDescription = "Toggle Mastered",
                            tint = if (card.isMastered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (!isFlipped) "❓ Question:" else "💡 Answer:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (!isFlipped) card.question else card.answer,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (!isFlipped) FontWeight.SemiBold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (!isFlipped) "Tap to flip and reveal answer" else "Tap to flip back",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StemHelperSection(onAskPrompt: (String) -> Unit) {
    val quickTopics = listOf(
        "Explain QuickSort vs MergeSort with time & space complexity" to "Algorithm Analysis",
        "Step-by-step solution to solve quadratic equations: ax² + bx + c = 0" to "Math Solver",
        "Explain Quantum Superposition and Entanglement in simple terms" to "Physics Concepts",
        "How do Coroutines and Flow work in Kotlin Android?" to "Android Development",
        "Explain Neural Network Backpropagation and Gradient Descent" to "AI & Machine Learning"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Fast STEM & Coding Assistance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap any topic to formulate an in-depth lesson with NOVA.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(quickTopics.size) { i ->
            val (prompt, label) = quickTopics[i]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAskPrompt(prompt) }
                    .testTag("stem_topic_$i"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(text = prompt, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
