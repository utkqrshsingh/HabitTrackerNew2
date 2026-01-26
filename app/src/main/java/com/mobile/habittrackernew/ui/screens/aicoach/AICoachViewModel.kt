package com.mobile.habittrackernew.ui.screens.aicoach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.models.AIMessage
import com.mobile.habittrackernew.data.repository.HabitRepository
import com.mobile.habittrackernew.services.AIService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AICoachUiState(
    val messages: List<AIMessage> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AICoachViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val aiService: AIService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AICoachUiState())
    val uiState: StateFlow<AICoachUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
        sendWelcomeMessage()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            repository.getAIMessages().collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    private fun sendWelcomeMessage() {
        viewModelScope.launch {
            if (repository.getRecentMessages(1).isEmpty()) {
                val welcomeMessage = """
                    👋 Hello! I'm your personal AI wellness coach!
                    
                    I'm here to help you with:
                    • Creating personalized schedules
                    • Diet and nutrition plans
                    • Exercise routines
                    • Sleep optimization
                    • Meditation guidance
                    • And much more!
                    
                    How can I help you today?
                """.trimIndent()

                repository.saveAIMessage(
                    AIMessage(
                        content = welcomeMessage,
                        isFromUser = false
                    )
                )
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            // Save user message
            repository.saveAIMessage(
                AIMessage(
                    content = text,
                    isFromUser = true
                )
            )

            _uiState.update { it.copy(isLoading = true) }

            // Get user progress for context
            val streaks = repository.getAllStreaks()
            val userProfile = repository.getUserProfileSync()

            // Generate AI response
            val response = aiService.generateResponse(
                userMessage = text,
                streaks = streaks,
                userProfile = userProfile
            )

            // Save AI response
            repository.saveAIMessage(
                AIMessage(
                    content = response,
                    isFromUser = false
                )
            )

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearAIMessages()
            sendWelcomeMessage()
        }
    }
}