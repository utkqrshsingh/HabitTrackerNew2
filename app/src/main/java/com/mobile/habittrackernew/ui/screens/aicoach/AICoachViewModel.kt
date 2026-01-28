package com.mobile.habittrackernew.ui.screens.aicoach

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.database.AIMessageDao
import com.mobile.habittrackernew.data.models.AIMessage
import com.mobile.habittrackernew.data.repository.HabitRepository
import com.mobile.habittrackernew.services.AIService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class AICoachUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val inputText: String = "",
    val isApiAvailable: Boolean = true,
    val connectionStatus: String = "Checking..."
)

@HiltViewModel
class AICoachViewModel @Inject constructor(
    private val aiService: AIService,
    private val habitRepository: HabitRepository,
    private val aiMessageDao: AIMessageDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "AICoachViewModel"
        private const val MAX_INPUT_LENGTH = 1000
    }

    private val _uiState = MutableStateFlow(AICoachUiState())
    val uiState: StateFlow<AICoachUiState> = _uiState.asStateFlow()

    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    init {
        checkConnection()
        loadChatHistory()
    }

    private fun checkConnection() {
        viewModelScope.launch {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            val isConnected = capabilities?.let {
                it.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                        it.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)
            } ?: false

            _uiState.update { it.copy(connectionStatus = if (isConnected) "Connected ✓" else "No internet") }

            if (!isConnected) {
                addSystemMessage("⚠️ You're offline. Some features may not work until you reconnect to the internet.")
            }
        }
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            try {
                val savedMessages = aiMessageDao.getAllMessages().first()

                if (savedMessages.isEmpty()) {
                    sendWelcomeMessage()
                } else {
                    val chatMessages = savedMessages.map {
                        ChatMessage(
                            id = it.id,
                            content = it.content,
                            isFromUser = it.isFromUser,
                            timestamp = it.timestamp
                        )
                    }
                    _uiState.update { it.copy(messages = chatMessages) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading chat history", e)
                sendWelcomeMessage()
            }
        }
    }

    private fun sendWelcomeMessage() {
        viewModelScope.launch {
            val welcomeMessage = ChatMessage(
                content = """
                    👋 **Hello! I'm Coach, your AI Habit Companion!**
                    
                    I'm here to help you build better habits through:
                    • **Practical tips** backed by science
                    • **Personalized advice** for your goals
                    • **Daily motivation** to keep you going
                    • **Habit troubleshooting** when you're stuck
                    
                    Try asking me:
                    - "How can I build a morning routine?"
                    - "I'm struggling with consistency, any tips?"
                    - "Give me motivation for today!"
                    - "What are good habits for productivity?"
                    
                    What would you like to work on today? 🚀
                """.trimIndent(),
                isFromUser = false
            )
            addMessageToChat(welcomeMessage)
        }
    }

    private fun addSystemMessage(content: String) {
        viewModelScope.launch {
            val systemMessage = ChatMessage(
                content = content,
                isFromUser = false
            )
            addMessageToChat(systemMessage, saveToDb = false)
        }
    }

    fun updateInputText(text: String) {
        if (text.length <= MAX_INPUT_LENGTH) {
            _uiState.update { it.copy(inputText = text) }
        }
    }

    fun sendMessage() {
        val messageText = _uiState.value.inputText.trim()
        if (messageText.isBlank()) return

        // Check if AI is initialized
        if (!aiService.isInitialized()) {
            addSystemMessage("⚠️ AI service is still initializing... Please wait a moment.")
            return
        }

        viewModelScope.launch {
            // Add user message
            val userMessage = ChatMessage(
                content = messageText,
                isFromUser = true
            )
            addMessageToChat(userMessage)

            // Clear input and set loading
            _uiState.update {
                it.copy(
                    inputText = "",
                    isLoading = true,
                    error = null
                )
            }

            // Check connection again
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            val isConnected = capabilities?.let {
                it.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                        it.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)
            } ?: false

            if (!isConnected) {
                addSystemMessage("🌐 **No internet connection detected.**\n\nPlease check your WiFi or mobile data and try again.")
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            // Get user habits for context
            val habits = try {
                habitRepository.getAllHabits().first()
            } catch (e: Exception) {
                emptyList()
            }

            Log.d(TAG, "Sending message with ${habits.size} habits as context")

            // Get AI response
            val result = aiService.sendMessage(messageText, habits)

            result.fold(
                onSuccess = { response ->
                    val aiMessage = ChatMessage(
                        content = response,
                        isFromUser = false
                    )
                    addMessageToChat(aiMessage)
                    _uiState.update { it.copy(isLoading = false, isApiAvailable = true) }
                },
                onFailure = { error ->
                    Log.e(TAG, "AI Service Error Details:", error)

                    val errorMessage = when {
                        error.message?.contains("API key", ignoreCase = true) == true -> {
                            """
                            🔑 **API Key Issue Detected**
                            
                            Please check your configuration:
                            1. Open `local.properties` file
                            2. Add: `GEMINI_API_KEY="your-actual-api-key"`
                            3. Rebuild the app
                            
                            Get API key from: https://makersuite.google.com/app/apikey
                            """.trimIndent()
                        }
                        error.message?.contains("quota", ignoreCase = true) == true -> {
                            """
                            ⚠️ **API Limit Reached**
                            
                            Your Gemini API quota has been exceeded.
                            • Wait 24 hours for reset
                            • Or upgrade your plan
                            • Use "Test Connection" below to check
                            """.trimIndent()
                        }
                        error.message?.contains("No internet", ignoreCase = true) == true -> {
                            "🌐 **Connection Issue**\n\nPlease check your internet connection and try again."
                        }
                        error.message?.contains("timeout", ignoreCase = true) == true -> {
                            "⏰ **Request Timeout**\n\nThe AI is taking too long to respond. Please try again."
                        }
                        else -> {
                            "🤖 **Technical Difficulty**\n\n${error.message ?: "Please try again in a moment."}"
                        }
                    }

                    val errorChatMessage = ChatMessage(
                        content = errorMessage,
                        isFromUser = false
                    )
                    addMessageToChat(errorChatMessage)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message,
                            isApiAvailable = false
                        )
                    }
                }
            )
        }
    }

    private suspend fun addMessageToChat(message: ChatMessage, saveToDb: Boolean = true) {
        _uiState.update { state ->
            state.copy(messages = state.messages + message)
        }

        if (saveToDb) {
            try {
                val aiMessage = AIMessage(
                    content = message.content,
                    isFromUser = message.isFromUser,
                    timestamp = message.timestamp
                )
                aiMessageDao.insertMessage(aiMessage)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving message to database", e)
            }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            addSystemMessage("🔍 Testing connection to Gemini AI...")

            val testResult = aiService.sendMessage("Hello")

            testResult.fold(
                onSuccess = { response ->
                    addSystemMessage("✅ **Connection successful!**\n\nAI is responding correctly.")
                    _uiState.update { it.copy(isApiAvailable = true) }
                },
                onFailure = { error ->
                    addSystemMessage("❌ **Connection failed:** ${error.message}")
                }
            )

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun askForMotivation() {
        viewModelScope.launch {
            val userMessage = ChatMessage(
                content = "Give me some motivation for today! 💪",
                isFromUser = true
            )
            addMessageToChat(userMessage)
            _uiState.update { it.copy(isLoading = true) }

            try {
                val habits = habitRepository.getAllHabits().first()
                val completedToday = habitRepository.getTotalCompletedToday()
                val streaks = habitRepository.getAllStreaksForHabits()
                val longestStreak = streaks.values.maxOfOrNull { it.currentStreak } ?: 0

                val result = aiService.getMotivation(completedToday, habits.size, longestStreak)

                result.fold(
                    onSuccess = { response ->
                        addMessageToChat(ChatMessage(content = response, isFromUser = false))
                    },
                    onFailure = {
                        addMessageToChat(
                            ChatMessage(
                                content = "🌟 **Keep going!** Every small step you take is progress toward your goals. You've got this! 💫",
                                      isFromUser = false
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting motivation data", e)
                addMessageToChat(
                    ChatMessage(
                        content = "🌟 Remember: Progress, not perfection! Celebrate your small wins today! 🎉",
                        isFromUser = false
                    )
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun askForTips() {
        _uiState.update { it.copy(inputText = "What are 3 science-backed tips for building habits that last?") }
        sendMessage()
    }

    fun askAboutStruggle() {
        _uiState.update { it.copy(inputText = "I'm struggling to stay consistent with my habits. Can you give me practical advice?") }
        sendMessage()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            try {
                aiMessageDao.deleteAllMessages()
                _uiState.update { it.copy(messages = emptyList()) }
                sendWelcomeMessage()
                addSystemMessage("🗑️ Chat history cleared!")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing chat", e)
                addSystemMessage("⚠️ Could not clear chat history")
            }
        }
    }
}