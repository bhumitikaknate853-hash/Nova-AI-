package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val preview: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val sender: String, // "USER" or "NOVA"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val isOfflineResponse: Boolean = false,
    val webSourcesJson: String? = null
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyName: String,
    val memoryValue: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_items")
data class StudyCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val question: String,
    val answer: String,
    val category: String = "General", // STEM, Coding, Language, History
    val difficulty: String = "Medium",
    val isMastered: Boolean = false
)
