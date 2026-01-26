package com.mobile.habittrackernew.ui.screens.aicoach
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ---------- MODEL ----------

data class AIMessage(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    val isFromUser: Boolean
)

// ---------- MAIN SCREEN ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICoachScreen(
    onBack: () -> Unit
) {
    val messages = remember {
        mutableStateListOf(
            AIMessage(
                content = "👋 Hi, I'm your AI coach. Ask me for a diet plan, workout routine, sleep schedule, or motivation!",
                isFromUser = false
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Automatically scroll to the latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        text = "AI Coach",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSendClick = {
                    val trimmed = inputText.trim()
                    if (trimmed.isEmpty()) return@ChatInputBar

                    messages.add(
                        AIMessage(
                            content = trimmed,
                            isFromUser = true
                        )
                    )
                    inputText = ""

                    scope.launch {
                        delay(600)
                        val reply = generateCoachReply(trimmed)
                        messages.add(
                            AIMessage(
                                content = reply,
                                isFromUser = false
                            )
                        )
                    }
                }
            )
        }

    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (messages.isEmpty()) {
                Text(
                    text = "Start a conversation with your coach!",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    state = listState
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatBubble(message = msg)
                    }
                }
            }
        }
    }
}

// ---------- COMPOSABLES ----------

@Composable
private fun ChatBubble(message: AIMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isFromUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Surface(
            color = if (message.isFromUser)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomEnd = if (message.isFromUser) 0.dp else 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 0.dp
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(10.dp),
                color = if (message.isFromUser)
                    Color.White
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask your coach...") },
                maxLines = 4
            )
            IconButton(
                onClick = onSendClick,
                enabled = text.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}

// ---------- SIMPLE REPLY LOGIC ----------

private fun generateCoachReply(userMessage: String): String {
    val msg = userMessage.lowercase()

    return when {
        listOf("diet", "food", "eat", "meal").any { it in msg } -> """
            🥗 Here's a simple balanced diet plan:

            • Breakfast: Oats or eggs + fruit  
            • Lunch: Lean protein + veggies + whole grains  
            • Snack: Nuts / yogurt / fruit  
            • Dinner: Light, more veggies + some protein  

            Drink water through the day and avoid heavy, sugary foods at night.
        """.trimIndent()

        listOf("exercise", "workout", "gym", "fitness").any { it in msg } -> """
            💪 Sample weekly workout:

            • Mon: Upper body (push-ups, rows, presses)  
            • Tue: Cardio (walk/jog 20–30 min)  
            • Wed: Lower body (squats, lunges)  
            • Thu: Light cardio + stretching  
            • Fri: Full body circuit  
            • Sat/Sun: Rest or light walk  

            Start easy and increase intensity gradually.
        """.trimIndent()

        listOf("sleep", "insomnia", "tired", "rest").any { it in msg } -> """
            😴 Better sleep tips:

            • Fix a regular sleep & wake time  
            • Avoid screens 1 hour before bed  
            • Keep your room cool and dark  
            • No heavy meals or caffeine late at night  
            • Try 5–10 minutes of deep breathing before bed  

            Aim for 7–8 hours of sleep.
        """.trimIndent()

        listOf("schedule", "timetable", "routine", "plan my day").any { it in msg } -> """
            📅 Basic daily routine:

            • Morning: Wake, hydrate, light movement, healthy breakfast  
            • Day: Focused work/study blocks + short breaks  
            • Evening: Exercise / walk, light dinner  
            • Night: Relax, no screens, prepare for tomorrow  

            You can tell me your usual wake/sleep times and I can adjust this.
        """.trimIndent()

        listOf("motivate", "motivation", "hard", "stuck", "lazy").any { it in msg } -> """
            🌟 You’re doing better than you think.

            Progress is built from tiny daily actions, not perfection.  
            Even 5 minutes of effort keeps your momentum alive.  

            Pick ONE small task you can do in the next 10 minutes.  
            Do it, and tell me when you’re done — we’ll build from there 💪
        """.trimIndent()

        else -> """
            Thanks for sharing! I can help with:

            • Diet ideas  
            • Exercise plans  
            • Sleep improvements  
            • Daily schedules  
            • Motivation and habit tips  

            Ask me about any of these, or describe your goal in your own words.
        """.trimIndent()
    }
}