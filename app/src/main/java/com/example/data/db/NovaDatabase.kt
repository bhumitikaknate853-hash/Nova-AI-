package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        StudyCardEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun novaDao(): NovaDao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getDatabase(context: Context): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "nova_ai_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).novaDao()
                            val initialConvId = dao.insertConversation(
                                ConversationEntity(
                                    title = "Welcome to NOVA AI 🤖",
                                    preview = "Hello! I am NOVA, your intelligent personal AI assistant."
                                )
                            )
                            dao.insertMessage(
                                MessageEntity(
                                    conversationId = initialConvId,
                                    sender = "NOVA",
                                    text = "Greetings! I'm NOVA — your futuristic, intelligent Android AI assistant.\n\nI can assist you with:\n• 🧠 STEM, coding, math & worldwide knowledge\n• 🎙️ Natural voice conversations with live speech\n• 🌐 Real web searches & research summaries\n• 📱 Native Android phone tools (dialer, SMS, camera, apps)\n• ⚡ Genuine Offline AI mode when you're disconnected\n• 📚 Study assistant with interactive flashcards & quizzes\n\nHow can I help you today?"
                                )
                            )
                            // Initial study deck
                            dao.insertStudyCards(
                                listOf(
                                    StudyCardEntity(
                                        topic = "Computer Science",
                                        question = "What is the time complexity of searching in a Balanced Binary Search Tree (AVL/Red-Black)?",
                                        answer = "O(log n) because the tree height is guaranteed to be logarithmic with respect to the number of nodes.",
                                        category = "Coding",
                                        difficulty = "Medium"
                                    ),
                                    StudyCardEntity(
                                        topic = "Physics",
                                        question = "What does Einstein's mass-energy equivalence equation E = mc² signify?",
                                        answer = "It demonstrates that mass and energy are interchangeable; a small amount of mass can be converted into an immense amount of energy.",
                                        category = "STEM",
                                        difficulty = "Easy"
                                    ),
                                    StudyCardEntity(
                                        topic = "Machine Learning",
                                        question = "What is the primary role of the Self-Attention mechanism in Transformer architectures?",
                                        answer = "It allows the model to dynamically weight the relevance of every token relative to all other tokens in a sequence simultaneously.",
                                        category = "Coding",
                                        difficulty = "Hard"
                                    )
                                )
                            )
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
