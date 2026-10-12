package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.BuildConfig
import com.example.data.api.GeminiApiClient
import com.example.data.db.ConversationEntity
import com.example.data.db.MemoryEntity
import com.example.data.db.MessageEntity
import com.example.data.db.NovaDao
import com.example.data.db.StudyCardEntity
import com.example.data.model.AssistantLanguage
import com.example.data.model.GeminiContent
import com.example.data.model.GeminiInlineData
import com.example.data.model.GeminiPart
import com.example.data.model.GeminiRequest
import com.example.data.offline.OfflineKnowledgeEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class NovaRepository(
    private val context: Context,
    private val dao: NovaDao
) {
    fun getConversations(): Flow<List<ConversationEntity>> = dao.getAllConversations()

    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> =
        dao.getMessagesForConversation(conversationId)

    fun getMemories(): Flow<List<MemoryEntity>> = dao.getAllMemories()

    fun getStudyCards(): Flow<List<StudyCardEntity>> = dao.getAllStudyCards()

    suspend fun createNewConversation(title: String = "New Conversation"): Long {
        return dao.insertConversation(
            ConversationEntity(
                title = title,
                preview = "Started conversation"
            )
        )
    }

    suspend fun deleteConversation(id: Long) {
        dao.deleteMessagesForConversation(id)
        dao.deleteConversation(id)
    }

    suspend fun togglePinConversation(conversation: ConversationEntity) {
        dao.updateConversation(conversation.copy(isPinned = !conversation.isPinned))
    }

    suspend fun addMemory(key: String, value: String, category: String = "General") {
        dao.insertMemory(MemoryEntity(keyName = key, memoryValue = value, category = category))
    }

    suspend fun deleteMemory(memory: MemoryEntity) {
        dao.deleteMemory(memory)
    }

    suspend fun clearMemories() {
        dao.clearAllMemories()
    }

    suspend fun saveStudyCard(card: StudyCardEntity) {
        dao.insertStudyCards(listOf(card))
    }

    suspend fun toggleCardMastered(card: StudyCardEntity) {
        dao.updateStudyCard(card.copy(isMastered = !card.isMastered))
    }

    suspend fun deleteStudyCard(id: Long) {
        dao.deleteStudyCard(id)
    }

    fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun sendMessage(
        conversationId: Long,
        userText: String,
        imageBase64: String? = null,
        language: AssistantLanguage = AssistantLanguage.ENGLISH,
        forceOffline: Boolean = false
    ): MessageEntity = withContext(Dispatchers.IO) {
        // Save user message
        val userMsgId = dao.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                sender = "USER",
                text = userText,
                imageUri = if (imageBase64 != null) "attached_image" else null
            )
        )

        // Check if we should use offline mode
        val online = isOnline() && !forceOffline
        val apiKey = BuildConfig.GEMINI_API_KEY

        val responseText: String
        val isOffline: Boolean

        if (!online || apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Run offline engine
            val offlineRes = OfflineKnowledgeEngine.query(userText, language.code)
            responseText = offlineRes.text
            isOffline = true
        } else {
            // Run Gemini API with context and memories
            val memories = dao.getAllMemories().firstOrNull() ?: emptyList()
            val memoryPrompt = if (memories.isNotEmpty()) {
                "User Approved Memories:\n" + memories.joinToString("\n") { "• ${it.keyName}: ${it.memoryValue}" } + "\n"
            } else ""

            val langInstruction = when (language) {
                AssistantLanguage.HINDI -> "Respond in Hindi (हिन्दी) where appropriate."
                AssistantLanguage.MARATHI -> "Respond in Marathi (मराठी) where appropriate."
                else -> "Respond in clear, helpful English."
            }

            val systemPrompt = "You are NOVA, a friendly, intelligent, and futuristic Android AI assistant. " +
                    "Your personality is warm, brilliant, and concise. " +
                    "Format responses with clear markdown bullet points and bold headers when helpful. " +
                    langInstruction + "\n" + memoryPrompt

            val userParts = mutableListOf<GeminiPart>()
            userParts.add(GeminiPart(text = userText))
            if (imageBase64 != null) {
                userParts.add(
                    GeminiPart(
                        inlineData = GeminiInlineData(
                            mimeType = "image/jpeg",
                            data = imageBase64
                        )
                    )
                )
            }

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(role = "user", parts = userParts)
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                )
            )

            val reply = try {
                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                candidateText ?: "I received an empty response. Please try again."
            } catch (e: Exception) {
                // Graceful fallback to offline engine on network/API failure
                val fallback = OfflineKnowledgeEngine.query(userText, language.code)
                "[⚠️ Cloud network issue: ${e.localizedMessage ?: "Connection error"}]\n\nFalling back to On-Device Brain:\n\n${fallback.text}"
            }

            responseText = reply
            isOffline = false
        }

        // Save assistant message
        val assistantMessage = MessageEntity(
            conversationId = conversationId,
            sender = "NOVA",
            text = responseText,
            isOfflineResponse = isOffline
        )
        val assistantMsgId = dao.insertMessage(assistantMessage)

        // Update conversation preview and timestamp
        val conv = dao.getConversationById(conversationId)
        if (conv != null) {
            val previewText = if (userText.length > 40) userText.take(40) + "..." else userText
            dao.updateConversation(
                conv.copy(
                    preview = previewText,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        assistantMessage.copy(id = assistantMsgId)
    }
}
