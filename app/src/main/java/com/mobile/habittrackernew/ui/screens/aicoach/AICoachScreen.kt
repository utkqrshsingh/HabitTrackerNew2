package com.mobile.habittrackernew.ui.screens.aicoach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.habittrackernew.ui.theme.GradientPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICoachScreen(
    onBack: () -> Unit,
    viewModel: AICoachViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(GradientPurple)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤖", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AI Coach",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.isLoading) "Thinking..." else "Powered by Gemini",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.isLoading)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color(0xFF4CAF50)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearChatHistory() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Clear Chat")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(uiState.messages) { message ->
                    ChatBubble(message = message)
                }

                if (uiState.isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            QuickActionsRow(
                onMotivation = { viewModel.askForMotivation() },
                onTips = { viewModel.askForTips() },
                onStruggle = { viewModel.askAboutStruggle() },
                enabled = !uiState.isLoading
            )

            ChatInputField(
                value = uiState.inputText,
                onValueChange = { viewModel.updateInputText(it) },
                onSend = { viewModel.sendMessage() },
                isLoading = uiState.isLoading
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) {
                        Brush.linearGradient(GradientPurple)
                    } else {
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = timeString,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha = when (index) {
                0 -> 0.4f
                1 -> 0.7f
                else -> 1f
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "AI is thinking...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickActionsRow(
    onMotivation: () -> Unit,
    onTips: () -> Unit,
    onStruggle: () -> Unit,
    enabled: Boolean
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AssistChip(
                onClick = onMotivation,
                enabled = enabled,
                label = { Text("💪 Motivate me") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
        item {
            AssistChip(
                onClick = onTips,
                enabled = enabled,
                label = { Text("💡 Habit tips") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
        item {
            AssistChip(
                onClick = onStruggle,
                enabled = enabled,
                label = { Text("😓 I'm struggling") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }
}

@Composable
private fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask your AI coach...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = onSend,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}