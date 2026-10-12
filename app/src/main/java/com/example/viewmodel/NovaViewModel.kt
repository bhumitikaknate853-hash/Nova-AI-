package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ConversationEntity
import com.example.data.db.MemoryEntity
import com.example.data.db.MessageEntity
import com.example.data.db.NovaDatabase
import com.example.data.db.StudyCardEntity
import com.example.data.model.*
import com.example.data.repository.NovaRepository
import com.example.ui.theme.NovaThemeMode
import com.example.voice.NovaVoiceManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class NovaScreen {
    DASHBOARD,
    CHAT,
    VOICE,
    PHONE_TOOLS,
    WEB_SEARCH,
    STUDY_STUDIO,
    VISION_STUDIO,
    SETTINGS
}

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = NovaDatabase.getDatabase(application)
    private val repository = NovaRepository(application, db.novaDao())
    val voiceManager = NovaVoiceManager(application)

    // Navigation & Screen
    private val _currentScreen = MutableStateFlow(NovaScreen.DASHBOARD)
    val currentScreen: StateFlow<NovaScreen> = _currentScreen.asStateFlow()

    // Robot & Appearance customization
    private val _robotExpression = MutableStateFlow(RobotExpression.HAPPY)
    val robotExpression: StateFlow<RobotExpression> = _robotExpression.asStateFlow()

    private val _robotAccent = MutableStateFlow(RobotAccent.CYAN)
    val robotAccent: StateFlow<RobotAccent> = _robotAccent.asStateFlow()

    private val _themeMode = MutableStateFlow(NovaThemeMode.FUTURISTIC)
    val themeMode: StateFlow<NovaThemeMode> = _themeMode.asStateFlow()

    private val _lowPerformanceMode = MutableStateFlow(false)
    val lowPerformanceMode: StateFlow<Boolean> = _lowPerformanceMode.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(AssistantLanguage.ENGLISH)
    val selectedLanguage: StateFlow<AssistantLanguage> = _selectedLanguage.asStateFlow()

    private val _forceOffline = MutableStateFlow(false)
    val forceOffline: StateFlow<Boolean> = _forceOffline.asStateFlow()

    private val _isGuestMode = MutableStateFlow(true)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    // Conversations & Messages
    val conversations: StateFlow<List<ConversationEntity>> = repository.getConversations()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    val activeMessages: StateFlow<List<MessageEntity>> = _activeConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getMessages(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Memories & Study
    val memories: StateFlow<List<MemoryEntity>> = repository.getMemories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val studyCards: StateFlow<List<StudyCardEntity>> = repository.getStudyCards()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Quiz State
    private val _currentQuiz = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val currentQuiz: StateFlow<List<QuizQuestion>> = _currentQuiz.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    // Web Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<WebSearchResult>>(emptyList())
    val searchResults: StateFlow<List<WebSearchResult>> = _searchResults.asStateFlow()

    // Pending phone action confirmation dialog
    private val _pendingPhoneAction = MutableStateFlow<PhoneAction?>(null)
    val pendingPhoneAction: StateFlow<PhoneAction?> = _pendingPhoneAction.asStateFlow()

    init {
        // Automatically select first conversation if available
        viewModelScope.launch {
            conversations.collect { convList ->
                if (_activeConversationId.value == null && convList.isNotEmpty()) {
                    _activeConversationId.value = convList.first().id
                }
            }
        }
        loadDefaultQuiz()
    }

    fun navigateTo(screen: NovaScreen) {
        _currentScreen.value = screen
        if (screen == NovaScreen.VOICE) {
            _robotExpression.value = RobotExpression.SPEAKING
        } else {
            _robotExpression.value = RobotExpression.HAPPY
        }
    }

    fun selectConversation(id: Long) {
        _activeConversationId.value = id
        _currentScreen.value = NovaScreen.CHAT
    }

    fun createNewChat(title: String = "New Conversation") {
        viewModelScope.launch {
            val newId = repository.createNewConversation(title)
            _activeConversationId.value = newId
            _currentScreen.value = NovaScreen.CHAT
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_activeConversationId.value == id) {
                _activeConversationId.value = conversations.value.firstOrNull { it.id != id }?.id
            }
        }
    }

    fun togglePinConversation(conversation: ConversationEntity) {
        viewModelScope.launch {
            repository.togglePinConversation(conversation)
        }
    }

    fun setRobotExpression(expression: RobotExpression) {
        _robotExpression.value = expression
    }

    fun setRobotAccent(accent: RobotAccent) {
        _robotAccent.value = accent
    }

    fun setThemeMode(mode: NovaThemeMode) {
        _themeMode.value = mode
    }

    fun toggleLowPerformanceMode() {
        _lowPerformanceMode.value = !_lowPerformanceMode.value
    }

    fun setLanguage(language: AssistantLanguage) {
        _selectedLanguage.value = language
        voiceManager.setLanguage(language)
    }

    fun toggleForceOffline() {
        _forceOffline.value = !_forceOffline.value
    }

    fun isDeviceOnline(): Boolean = repository.isOnline() && !_forceOffline.value

    fun dismissOnboarding() {
        _showOnboarding.value = false
    }

    // Chat Message Send
    fun sendMessage(text: String, imageBase64: String? = null, autoSpeak: Boolean = false) {
        if (text.isBlank() && imageBase64 == null) return
        val convId = _activeConversationId.value
        if (convId == null) {
            viewModelScope.launch {
                val newId = repository.createNewConversation("Chat with NOVA")
                _activeConversationId.value = newId
                executeMessageSend(newId, text, imageBase64, autoSpeak)
            }
        } else {
            executeMessageSend(convId, text, imageBase64, autoSpeak)
        }
    }

    private fun executeMessageSend(
        convId: Long,
        text: String,
        imageBase64: String?,
        autoSpeak: Boolean
    ) {
        _isGenerating.value = true
        _robotExpression.value = RobotExpression.THINKING
        viewModelScope.launch {
            try {
                val response = repository.sendMessage(
                    conversationId = convId,
                    userText = text,
                    imageBase64 = imageBase64,
                    language = _selectedLanguage.value,
                    forceOffline = _forceOffline.value
                )
                _robotExpression.value = RobotExpression.HAPPY
                if (autoSpeak) {
                    voiceManager.speak(response.text)
                }
            } catch (e: Exception) {
                _robotExpression.value = RobotExpression.SURPRISED
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // Voice interaction
    fun startVoiceInput() {
        _robotExpression.value = RobotExpression.SPEAKING
        voiceManager.startListening { spoken ->
            sendMessage(spoken, autoSpeak = true)
        }
    }

    fun stopVoiceInput() {
        voiceManager.stopListening()
        _robotExpression.value = RobotExpression.HAPPY
    }

    // Web Search
    fun performSearch(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        val encoded = Uri.encode(query)
        _searchResults.value = listOf(
            WebSearchResult(
                title = "Google Search: \"$query\"",
                snippet = "Search the live web for verified articles, academic papers, and encyclopedic sources regarding $query.",
                url = "https://www.google.com/search?q=$encoded",
                source = "Google"
            ),
            WebSearchResult(
                title = "YouTube Video Lessons: \"$query\"",
                snippet = "Watch explainer videos, educational walkthroughs, and visual demonstrations on YouTube.",
                url = "https://www.youtube.com/results?search_query=$encoded",
                source = "YouTube"
            ),
            WebSearchResult(
                title = "Wikipedia Encyclopedia: \"$query\"",
                snippet = "Read in-depth community-curated background, history, and scientific details on Wikipedia.",
                url = "https://en.wikipedia.org/wiki/Special:Search?search=$encoded",
                source = "Wikipedia"
            ),
            WebSearchResult(
                title = "DuckDuckGo Privacy Search: \"$query\"",
                snippet = "Private web search results without trackers for $query.",
                url = "https://duckduckgo.com/?q=$encoded",
                source = "DuckDuckGo"
            )
        )
    }

    // Study & Quiz Tools
    private fun loadDefaultQuiz() {
        _currentQuiz.value = listOf(
            QuizQuestion(
                question = "What is the primary function of an operating system's kernel?",
                options = listOf(
                    "Rendering web pages",
                    "Managing system hardware resources and processes",
                    "Compressing video files",
                    "Compiling source code"
                ),
                correctIndex = 1,
                explanation = "The kernel is the core component that manages CPU, memory, peripherals, and system calls."
            ),
            QuizQuestion(
                question = "Which asymptotic notation describes the strict upper bound of an algorithm's execution time?",
                options = listOf("Big Omega (Ω)", "Big Theta (Θ)", "Big O (O)", "Small o (o)"),
                correctIndex = 2,
                explanation = "Big O notation characterizes worst-case upper bound computational complexity."
            ),
            QuizQuestion(
                question = "What is the primary role of the mitochondria in eukaryotic cells?",
                options = listOf("Protein folding", "Lipid storage", "ATP energy synthesis", "Cell division"),
                correctIndex = 2,
                explanation = "Mitochondria produce ATP through oxidative phosphorylation."
            )
        )
    }

    fun toggleCardMastered(card: StudyCardEntity) {
        viewModelScope.launch {
            repository.toggleCardMastered(card)
        }
    }

    fun addStudyCard(topic: String, question: String, answer: String, category: String) {
        viewModelScope.launch {
            repository.saveStudyCard(
                StudyCardEntity(
                    topic = topic,
                    question = question,
                    answer = answer,
                    category = category
                )
            )
        }
    }

    fun deleteStudyCard(id: Long) {
        viewModelScope.launch {
            repository.deleteStudyCard(id)
        }
    }

    // Memories management
    fun addMemory(key: String, value: String, category: String) {
        viewModelScope.launch {
            repository.addMemory(key, value, category)
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearMemories()
        }
    }

    // Phone Action Confirmation & Execution
    fun requestPhoneAction(action: PhoneAction) {
        _pendingPhoneAction.value = action
    }

    fun dismissPhoneAction() {
        _pendingPhoneAction.value = null
    }

    fun executePhoneAction(action: PhoneAction, context: Context, extraInput: String = "") {
        _pendingPhoneAction.value = null
        try {
            when (action.actionType) {
                PhoneActionType.OPEN_BROWSER -> {
                    val url = if (extraInput.startsWith("http")) extraInput else "https://www.google.com"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                PhoneActionType.SEARCH_GOOGLE -> {
                    val query = Uri.encode(extraInput.ifBlank { "NOVA AI Assistant" })
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                PhoneActionType.SEARCH_YOUTUBE -> {
                    val query = Uri.encode(extraInput.ifBlank { "AI Technology" })
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                PhoneActionType.OPEN_MAPS -> {
                    val location = Uri.encode(extraInput.ifBlank { "nearby places" })
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$location")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                PhoneActionType.OPEN_CAMERA -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                PhoneActionType.CREATE_REMINDER -> {
                    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_MESSAGE, extraInput.ifBlank { "NOVA Reminder" })
                        putExtra(AlarmClock.EXTRA_HOUR, 9)
                        putExtra(AlarmClock.EXTRA_MINUTES, 0)
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                PhoneActionType.SEND_SMS -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:${extraInput.ifBlank { "" }}")
                        putExtra("sms_body", "Hello from NOVA AI Assistant!")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                PhoneActionType.DIAL_PHONE -> {
                    val number = extraInput.ifBlank { "" }
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                PhoneActionType.SYSTEM_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                PhoneActionType.SHARE_APP -> {
                    val sendIntent = Intent().apply {
                        this.action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Check out NOVA AI — The futuristic intelligent Android AI Assistant with Voice, Offline Mode, 3D Robot, and Study Tools!"
                        )
                        type = "text/plain"
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share NOVA AI").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(shareIntent)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open action: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(text: String, context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("NOVA Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
