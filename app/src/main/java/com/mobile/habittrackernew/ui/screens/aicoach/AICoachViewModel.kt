package com.mobile.habittrackernew.ui.screens.aicoach

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.database.AIMessageDao
import com.mobile.habittrackernew.data.models.AIMessage
import com.mobile.habittrackernew.data.repository.HabitRepository
import com.mobile.habittrackernew.services.AIService
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val inputText: String = ""
)

@HiltViewModel
class AICoachViewModel @Inject constructor(
    private val aiService: AIService,
    private val habitRepository: HabitRepository,
    private val aiMessageDao: AIMessageDao
) : ViewModel() {

    companion object {
        private const val TAG = "AICoachViewModel"
    }

    private val _uiState = MutableStateFlow(AICoachUiState())
    val uiState: StateFlow<AICoachUiState> = _uiState.asStateFlow()

    init {
        loadChatHistory()
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
        val welcomeMessage = ChatMessage(
            content = """
                Hello! 👋 I'm your AI Habit Coach powered by Google Gemini!
                
                I'm here to help you build better habits and reach your goals. You can ask me:
                
                • Tips for building new habits
                • Motivation when you're struggling  
                • Advice on staying consistent
                • Science-backed habit strategies
                
                How can I help you today? 🚀
            """.trimIndent(),
            isFromUser = false
        )

        viewModelScope.launch {
            addMessageToChat(welcomeMessage)
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val messageText = _uiState.value.inputText.trim()
        if (messageText.isBlank()) return

        viewModelScope.launch {
            val userMessage = ChatMessage(
                content = messageText,
                isFromUser = true
            )
            addMessageToChat(userMessage)

            _uiState.update {
                it.copy(
                    inputText = "",
                    isLoading = true,
                    error = null
                )
            }

            val habits = try {
                habitRepository.getAllHabits().first()
            } catch (e: Exception) {
                emptyList()
            }

            val result = aiService.sendMessage(messageText, habits)

            result.fold(
                onSuccess = { response ->
                    val aiMessage = ChatMessage(
                        content = response,
                        isFromUser = false
                    )
                    addMessageToChat(aiMessage)
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    Log.e(TAG, "AI Error", error)
                    val errorMessage = ChatMessage(
                        content = "I'm having trouble connecting right now. Please check your internet connection and try again. 🔄",
                        isFromUser = false
                    )
                    addMessageToChat(errorMessage)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    private suspend fun addMessageToChat(message: ChatMessage) {
        _uiState.update { state ->
            state.copy(messages = state.messages + message)
        }

        try {
            val aiMessage = AIMessage(
                content = message.content,
                isFromUser = message.isFromUser,
                timestamp = message.timestamp
            )
            aiMessageDao.insertMessage(aiMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving message", e)
        }
    }

    fun askForMotivation() {
        viewModelScope.launch {
            val userMessage = ChatMessage(
                content = "I need some motivation! 💪",
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
                                content = "Keep going! Every small step counts towards your bigger goals! 🌟",
                                isFromUser = false
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting motivation data", e)
                addMessageToChat(
                    ChatMessage(
                        content = "Keep going! Every small step counts towards your bigger goals! 🌟",
                        isFromUser = false
                    )
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun askForTips() {
        _uiState.update { it.copy(inputText = "What are some science-backed tips for building habits that stick?") }
        sendMessage()
    }

    fun askAboutStruggle() {
        _uiState.update { it.copy(inputText = "I'm struggling to stay consistent with my habits. Can you help me?") }
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
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing chat", e)
            }
        }
    }
}
