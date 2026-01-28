package com.mobile.habittrackernew.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.mobile.habittrackernew.BuildConfig
import com.mobile.habittrackernew.data.models.Habit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIService @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val TAG = "AIService"
        // Use gemini-2.5-flash which you have access to
        private const val MODEL_NAME = "gemini-2.5-flash"
    }

    // Lazy initialization of the model
    private val generativeModel: GenerativeModel? by lazy {
        initializeModel()
    }

    private fun initializeModel(): GenerativeModel? {
        return try {
            val rawApiKey = BuildConfig.GEMINI_API_KEY

            Log.d(TAG, "========== GEMINI API INIT ==========")
            Log.d(TAG, "Raw API Key exists: ${rawApiKey.isNotBlank()}")
            Log.d(TAG, "Raw API Key length: ${rawApiKey.length}")

            // Clean the API key
            val cleanApiKey = rawApiKey.trim()
                .removeSurrounding("\"")
                .trim()

            Log.d(TAG, "Clean API Key length: ${cleanApiKey.length}")
            Log.d(TAG, "Starts with AIza: ${cleanApiKey.startsWith("AIza")}")
            Log.d(TAG, "First 10 chars: ${cleanApiKey.take(10)}")

            if (cleanApiKey.isEmpty()) {
                Log.e(TAG, "❌ API Key is EMPTY!")
                return null
            }

            if (!cleanApiKey.startsWith("AIza")) {
                Log.e(TAG, "❌ API Key doesn't start with 'AIza' - INVALID FORMAT!")
                return null
            }

            Log.d(TAG, "✅ Creating Gemini model: $MODEL_NAME")

            val model = GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = cleanApiKey
            )

            Log.d(TAG, "✅ Gemini model '$MODEL_NAME' created successfully!")
            Log.d(TAG, "=====================================")
            model

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error initializing GenerativeModel: ${e.message}", e)
            Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Make sure you're using a model available in your account")
            e.printStackTrace()
            null
        }
    }

    private fun isNetworkConnected(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val hasCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

            Log.d(TAG, "📡 Network - WiFi: $hasWifi, Cellular: $hasCellular")

            hasWifi || hasCellular
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network: ${e.message}")
            false
        }
    }

    fun isInitialized(): Boolean {
        val initialized = generativeModel != null
        Log.d(TAG, "Model initialized: $initialized")
        return initialized
    }

    suspend fun sendMessage(
        userMessage: String,
        userHabits: List<Habit> = emptyList()
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Check network
                if (!isNetworkConnected()) {
                    Log.w(TAG, "No internet connection")
                    return@withContext Result.failure(
                        Exception("No internet connection. Please check WiFi or mobile data.")
                    )
                }

                // Get model
                val model = generativeModel
                if (model == null) {
                    Log.e(TAG, "Model is NULL")
                    return@withContext Result.failure(
                        Exception("AI model not initialized. Please check your API key.")
                    )
                }

                // Build prompt
                val habitContext = if (userHabits.isNotEmpty()) {
                    "User's current habits:\n" + userHabits.joinToString("\n") { "- ${it.name}" } + "\n\n"
                } else {
                    ""
                }

                val systemPrompt = """
                    You are Coach, a friendly AI habit coach. Your job is to:
                    - Help users build better habits
                    - Provide motivation and support
                    - Give practical, actionable advice
                    - Keep responses short and friendly (1-2 paragraphs max)
                    - Use occasional emojis to be friendly
                """.trimIndent()

                val prompt = """
                    $systemPrompt
                    
                    $habitContext
                    User message: $userMessage
                    
                    Coach response:
                """.trimIndent()

                Log.d(TAG, "📤 Sending message to $MODEL_NAME...")
                Log.d(TAG, "User message: ${userMessage.take(50)}...")

                // Call the API
                val response = model.generateContent(prompt)
                val responseText = response.text?.trim()

                if (responseText == null || responseText.isEmpty()) {
                    Log.w(TAG, "Got empty response from AI")
                    return@withContext Result.failure(
                        Exception("AI returned empty response. Please try again.")
                    )
                }

                Log.d(TAG, "✅ Response received (${responseText.length} chars)")
                Log.d(TAG, "Response preview: ${responseText.take(50)}...")

                Result.success(responseText)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in sendMessage: ${e.message}", e)

                val errorMsg = when {
                    e.message?.contains("INVALID_ARGUMENT", ignoreCase = true) == true ->
                        "Invalid request format - please try a simpler message"
                    e.message?.contains("NOT_FOUND", ignoreCase = true) == true ->
                        "Model not found - your API key may not have access to this model"
                    e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                        "Permission denied - check your API key has proper access"
                    e.message?.contains("RESOURCE_EXHAUSTED", ignoreCase = true) == true ->
                        "API quota exceeded - please wait and try again later"
                    e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                        "Service temporarily unavailable - please try again"
                    e.message?.contains("quota", ignoreCase = true) == true ->
                        "API quota limit reached - check your usage"
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error - check your internet connection"
                    e is java.net.SocketTimeoutException ->
                        "Request timed out - please try again"
                    e is java.net.UnknownHostException ->
                        "Cannot reach API servers - check internet connection"
                    else -> e.message ?: "Unknown error occurred"
                }

                Log.e(TAG, "Error response: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        }
    }

    suspend fun getMotivation(
        completedToday: Int,
        totalHabits: Int,
        streak: Int
    ): Result<String> {
        return sendMessage(
            "I completed $completedToday out of $totalHabits habits today! " +
                    "My current streak is $streak days. " +
                    "Give me a short, motivational message to keep me going! 💪"
        )
    }

    suspend fun getHabitAdvice(habit: Habit): Result<String> {
        return sendMessage(
            "I'm working on building this habit: '${habit.name}'. " +
                    (habit.description?.let { "Description: $it. " } ?: "") +
                    "Can you give me one practical tip to help me succeed? 📝"
        )
    }
}