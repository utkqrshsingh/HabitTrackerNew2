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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
                                .background(
                                    if (uiState.isApiAvailable) {
                                        Brush.linearGradient(GradientPurple)
                                    } else {
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFFF44336),
                                                Color(0xFFD32F2F)
                                            )
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.isApiAvailable) "🤖" else "⚠️",
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AI Coach",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.isLoading) "Thinking..." else uiState.connectionStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    uiState.isLoading -> MaterialTheme.colorScheme.primary
                                    uiState.isApiAvailable -> Color(0xFF4CAF50)
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.testConnection() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = "Test Connection")
                    }
                    IconButton(
                        onClick = { viewModel.clearChatHistory() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Chat")
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
            // Warning banner for connection issues
            if (!uiState.isApiAvailable) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "⚠️ AI Service Unavailable",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = uiState.error ?: "Check your internet connection or API key",
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.testConnection() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Test", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Chat messages area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (uiState.messages.isEmpty()) {
                    item {
                        WelcomeCard()
                    }
                } else {
                    items(uiState.messages) { message ->
                        ChatBubble(message = message)
                    }
                }

                if (uiState.isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // Quick action chips
            QuickActionsRow(
                onMotivation = { viewModel.askForMotivation() },
                onTips = { viewModel.askForTips() },
                onStruggle = { viewModel.askAboutStruggle() },
                onConnectionTest = { viewModel.testConnection() },
                isApiAvailable = uiState.isApiAvailable,
                enabled = !uiState.isLoading
            )

            // Chat input field
            ChatInputField(
                value = uiState.inputText,
                onValueChange = { viewModel.updateInputText(it) },
                onSend = { viewModel.sendMessage() },
                isLoading = uiState.isLoading,
                isApiAvailable = uiState.isApiAvailable,
                enabled = !uiState.isLoading
            )
        }
    }
}

@Composable
private fun WelcomeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(GradientPurple)),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to AI Coach!",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "I'm here to help you build better habits through personalized advice and motivation.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Try asking me:\n")
                    }
                    append("• How to build a morning routine\n")
                    append("• Tips for staying consistent\n")
                    append("• Motivation for today's goals\n")
                    append("• Science-backed habit strategies")
                },
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
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
        Row(
            modifier = if (!isUser) Modifier.padding(end = 40.dp) else Modifier,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(GradientPurple)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
            ) {
                Card(
                    modifier = Modifier.widthIn(max = 280.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUser) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (isUser) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                ) {
                    Text(
                        text = message.content,
                        modifier = Modifier.padding(12.dp),
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

            if (isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier
            .padding(start = 48.dp, top = 8.dp, bottom = 8.dp)
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
            text = "Coach is thinking...",
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
    onConnectionTest: () -> Unit,
    isApiAvailable: Boolean,
    enabled: Boolean
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!isApiAvailable) {
            item {
                AssistChip(
                    onClick = onConnectionTest,
                    enabled = enabled,
                    label = { Text("🔗 Test Connection") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        item {
            AssistChip(
                onClick = onMotivation,
                enabled = enabled && isApiAvailable,
                label = { Text("💪 Motivate me") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
        item {
            AssistChip(
                onClick = onTips,
                enabled = enabled && isApiAvailable,
                label = { Text("💡 Habit tips") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
        item {
            AssistChip(
                onClick = onStruggle,
                enabled = enabled && isApiAvailable,
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
    isLoading: Boolean,
    isApiAvailable: Boolean,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            if (isApiAvailable) "Ask your AI coach..."
                            else "AI service unavailable..."
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    enabled = enabled && !isLoading && isApiAvailable,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = onSend,
                    modifier = Modifier.size(48.dp),
                    enabled = enabled && !isLoading && isApiAvailable,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }

            if (!isApiAvailable) {
                Text(
                    text = "⚠️ Connect to internet and check API key to enable AI",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 24.dp, top = 4.dp)
                )
            }
        }
    }
}
