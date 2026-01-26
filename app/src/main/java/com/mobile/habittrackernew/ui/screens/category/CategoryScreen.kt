package com.mobile.habittrackernew.ui.screens.category

import com.mobile.habittrackernew.ui.utils.getComposeColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.habittrackernew.data.models.Category
import com.mobile.habittrackernew.data.models.HabitLog
import com.mobile.habittrackernew.ui.components.AnimatedCircularProgress
import com.mobile.habittrackernew.ui.components.GradientCard
import com.mobile.habittrackernew.ui.components.WeekDayIndicator
import com.mobile.habittrackernew.ui.screens.dashboard.getCategoryIcon
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryName: String,
    onBackClick: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val category = try {
        Category.valueOf(categoryName)
    } catch (e: Exception) {
        null
    }

    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(categoryName) {
        viewModel.loadCategory(categoryName)
    }

    if (category == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Category not found")
        }
        return
    }

    val categoryColor = category.getComposeColor()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            categoryColor,
                                            categoryColor.copy(alpha = 0.7f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(category),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = category.displayName,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Streak Hero Card
                item {
                    StreakHeroCard(
                        currentStreak = uiState.currentStreak,
                        longestStreak = uiState.longestStreak,
                        isCompletedToday = uiState.isCompletedToday,
                        color = categoryColor,
                        onMarkComplete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleTodayCompletion()
                        }
                    )
                }

                // Week Progress
                item {
                    WeekProgressSection(
                        weekLogs = uiState.weekLogs,
                        color = categoryColor
                    )
                }

                // Monthly Calendar
                item {
                    MonthlyCalendarCard(
                        logs = uiState.monthLogs,
                        color = categoryColor
                    )
                }

                // Recent Activity Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.recentLogs.count { it.isCompleted }} completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Recent Activity Items
                items(uiState.recentLogs.take(7)) { log ->
                    ActivityLogItem(log = log, color = categoryColor)
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun StreakHeroCard(
    currentStreak: Int,
    longestStreak: Int,
    isCompletedToday: Boolean,
    color: Color,
    onMarkComplete: () -> Unit
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isCompletedToday) {
        if (isCompletedToday) {
            scale.animateTo(1.02f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    GradientCard(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value),
        gradientColors = listOf(color, color.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Streak Fire Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Streak Count
            Text(
                text = currentStreak.toString(),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Day Streak ${getStreakEmoji(currentStreak)}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Motivation Message
            Text(
                text = getStreakMessage(currentStreak),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$longestStreak days",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Longest Streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Complete Button
            Button(
                onClick = onMarkComplete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompletedToday)
                        Color.White.copy(alpha = 0.3f)
                    else
                        Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = if (isCompletedToday) Icons.Default.Check else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isCompletedToday) Color.White else color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCompletedToday) "Completed! ✓" else "Mark as Complete",
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompletedToday) Color.White else color,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun WeekProgressSection(
    weekLogs: Map<String, Boolean>,
    color: Color
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dayFormatter = DateTimeFormatter.ofPattern("EEE")
    val today = LocalDate.now()
    val weekDays = (6 downTo 0).map { today.minusDays(it.toLong()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "This Week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { date ->
                    val dateStr = date.format(dateFormatter)
                    val isCompleted = weekLogs[dateStr] == true
                    val isToday = date == today

                    WeekDayIndicator(
                        dayLabel = date.format(dayFormatter),
                        isCompleted = isCompleted,
                        isToday = isToday,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Week Summary
            val completedDays = weekLogs.values.count { it }
            val progress = completedDays / 7f

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(color, color.copy(alpha = 0.7f))
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$completedDays/7",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun MonthlyCalendarCard(
    logs: List<HabitLog>,
    color: Color
) {
    val today = LocalDate.now()
    val firstDayOfMonth = today.withDayOfMonth(1)
    val daysInMonth = today.lengthOfMonth()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val completedDates = logs.filter { it.isCompleted }.map { it.date }.toSet()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${completedDates.size} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            val startOffset = firstDayOfMonth.dayOfWeek.value % 7
            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - startOffset + 1

                        if (dayNumber in 1..daysInMonth) {
                            val date = firstDayOfMonth.withDayOfMonth(dayNumber)
                            val dateStr = date.format(dateFormatter)
                            val isCompleted = completedDates.contains(dateStr)
                            val isToday = date == today
                            val isFuture = date.isAfter(today)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCompleted -> color
                                            isToday -> color.copy(alpha = 0.2f)
                                            else -> Color.Transparent
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isCompleted -> Color.White
                                        isFuture -> MaterialTheme.colorScheme.onSurfaceVariant
                                            .copy(alpha = 0.4f)
                                        isToday -> color
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityLogItem(
    log: HabitLog,
    color: Color
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val displayFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

    val date = try {
        LocalDate.parse(log.date, dateFormatter)
    } catch (e: Exception) {
        null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (log.isCompleted)
                color.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (log.isCompleted) color
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (log.isCompleted) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (log.isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = date?.format(displayFormatter) ?: log.date,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (log.isCompleted) "Completed ✓" else "Missed",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (log.isCompleted) color else MaterialTheme.colorScheme.error
                    )
                }
            }

            if (log.isCompleted && log.duration > 0) {
                Text(
                    text = "${log.duration} min",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun getStreakEmoji(streak: Int): String {
    return when {
        streak >= 100 -> "💎"
        streak >= 60 -> "👑"
        streak >= 30 -> "🏆"
        streak >= 14 -> "⭐"
        streak >= 7 -> "🔥"
        streak >= 3 -> "✨"
        streak >= 1 -> "💪"
        else -> "🚀"
    }
}

fun getStreakMessage(streak: Int): String {
    return when {
        streak == 0 -> "Start your journey today!"
        streak == 1 -> "Great start! Keep it going!"
        streak in 2..4 -> "You're building momentum!"
        streak in 5..6 -> "Almost a week! Amazing!"
        streak == 7 -> "One week complete! 🎉"
        streak in 8..13 -> "Incredible consistency!"
        streak == 14 -> "Two weeks strong! 💪"
        streak in 15..29 -> "You're a habit master!"
        streak == 30 -> "ONE MONTH! Legendary! 🏆"
        streak in 31..59 -> "Over a month! Unstoppable!"
        streak >= 60 -> "You're an inspiration! 👑"
        else -> "Keep going!"
    }
}