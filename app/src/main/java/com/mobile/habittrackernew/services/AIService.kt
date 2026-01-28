package com.mobile.habittrackernew.services

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.mobile.habittrackernew.BuildConfig
import com.mobile.habittrackernew.data.models.Habit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIService @Inject constructor() {

    companion object {
        private const val TAG = "AIService"
    }

    private val generativeModel: GenerativeModel? by lazy {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank()) {
                GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )
            } else {
                Log.e(TAG, "Gemini API key is empty")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Gemini", e)
            null
        }
    }

    private val systemPrompt = """
        You are an encouraging and supportive AI habit coach named "Coach". 
        Your role is to help users build and maintain healthy habits.
        
        Guidelines:
        - Be positive, motivating, and supportive
        - Give practical, actionable advice
        - Keep responses concise (2-3 paragraphs max)
        - Use emojis occasionally to be friendly 😊
        - If user shares struggles, be empathetic first, then offer solutions
        - Celebrate wins, no matter how small
        - Reference habit science when relevant (habit stacking, 2-minute rule, etc.)
        - Personalize responses based on user's habits when mentioned
        
        Remember: You're helping someone become their best self through better habits!
    """.trimIndent()

    suspend fun sendMessage(userMessage: String, userHabits: List<Habit> = emptyList()): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val model = generativeModel
                if (model == null) {
                    Log.e(TAG, "Generative model not initialized")
                    return@withContext Result.failure(Exception("AI not available. Check API key."))
                }

                Log.d(TAG, "Sending message to Gemini: $userMessage")

                val habitContext = if (userHabits.isNotEmpty()) {
                    val habitList = userHabits.joinToString(", ") { it.name }
                    "\n\nUser's current habits: $habitList"
                } else {
                    ""
                }

                val fullPrompt = """
                    $systemPrompt
                    $habitContext
                    
                    User: $userMessage
                    
                    Coach:
                """.trimIndent()

                val response = model.generateContent(fullPrompt)
                val responseText = response.text?.trim()
                    ?: "I'm having trouble responding right now. Please try again!"

                Log.d(TAG, "Gemini response received")
                Result.success(responseText)

            } catch (e: Exception) {
                Log.e(TAG, "Error sending message to Gemini", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getMotivation(completedToday: Int, totalHabits: Int, streak: Int): Result<String> {
        val prompt = """
            Today I completed $completedToday out of $totalHabits habits.
            My longest current streak is $streak days.
            
            Give me a short motivational message based on my progress today!
        """.trimIndent()

        return sendMessage(prompt)
    }

    suspend fun getHabitAdvice(habit: Habit): Result<String> {
        val prompt = """
            I'm working on this habit: "${habit.name}"
            Description: ${habit.description}
            
            Can you give me specific tips to help me maintain and improve this habit?
        """.trimIndent()

        return sendMessage(prompt)
    }

    fun clearChatHistory() {
        // For future use if needed
    }
}
