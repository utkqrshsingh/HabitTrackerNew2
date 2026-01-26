package com.mobile.habittrackernew.ui.screens.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.habittrackernew.data.models.Habit
import com.mobile.habittrackernew.data.models.StreakInfo
import com.mobile.habittrackernew.ui.components.*
import com.mobile.habittrackernew.ui.theme.GradientPurple
import com.mobile.habittrackernew.ui.utils.getIconByName
import com.mobile.habittrackernew.ui.utils.parseColor
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onHabitClick: (Long) -> Unit,
    onNotificationClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    var showAddDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }

    val quotes = listOf(
        Pair("The secret of getting ahead is getting started.", "Mark Twain"),
        Pair("Small daily improvements are the key to staggering long-term results.", "Robin Sharma"),
        Pair("Success is the sum of small efforts repeated day in and day out.", "Robert Collier"),
        Pair("You don't have to be great to start, but you have to start to be great.", "Zig Ziglar"),
        Pair("The only bad workout is the one that didn't happen.", "Unknown")
    )
    val randomQuote = remember { quotes.random() }
    val incompleteCount = uiState.habits.size - uiState.todayCompletedCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = getGreeting(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = today,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Add Habit Button
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Habit"
                        )
                    }
                    // Notification Button with Badge
                    IconButton(onClick = onNotificationClick) {
                        BadgedBox(
                            badge = {
                                if (incompleteCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Text(
                                            text = incompleteCount.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Habit"
                )
            }
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
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Show content based on whether habits exist
                if (uiState.habits.isEmpty()) {
                    // Empty State - No habits
                    item {
                        EmptyHabitsState(
                            onAddHabit = { showAddDialog = true }
                        )
                    }
                } else {
                    // Progress Overview Card
                    item {
                        ProgressOverviewCard(
                            completedCount = uiState.todayCompletedCount,
                            totalCount = uiState.habits.size,
                            streaks = uiState.streaks
                        )
                    }

                    // Quick Stats Row
                    item {
                        QuickStatsRow(
                            totalCompleted = uiState.todayCompletedCount,
                            bestStreak = uiState.streaks.values.maxOfOrNull { it.currentStreak } ?: 0,
                            activeHabits = uiState.streaks.values.count { it.currentStreak > 0 }
                        )
                    }

                    // Motivational Quote
                    item {
                        MotivationalQuoteCard(
                            quote = randomQuote.first,
                            author = randomQuote.second
                        )
                    }

                    // Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Habits",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.habits.size} habits",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Habits Grid - 2 columns
                    items(uiState.habits.chunked(2)) { rowHabits ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowHabits.forEach { habit ->
                                EnhancedHabitCard(
                                    habit = habit,
                                    streakInfo = uiState.streaks[habit.id],
                                    isCompletedToday = uiState.todayCompletions[habit.id] ?: false,
                                    onClick = { onHabitClick(habit.id) },
                                    onToggleComplete = { viewModel.toggleHabitCompletion(habit) },
                                    onEdit = { editingHabit = habit },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // If odd number of items, add spacer
                            if (rowHabits.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add Habit Dialog
    if (showAddDialog) {
        AddEditHabitDialog(
            habit = null,
            onDismiss = { showAddDialog = false },
            onSave = { newHabit ->
                viewModel.addHabit(newHabit)
                showAddDialog = false
            }
        )
    }

    // Edit Habit Dialog
    editingHabit?.let { habit ->
        AddEditHabitDialog(
            habit = habit,
            onDismiss = { editingHabit = null },
            onSave = { updatedHabit ->
                viewModel.updateHabit(updatedHabit)
                editingHabit = null
            },
            onDelete = { habitToDelete ->
                viewModel.deleteHabit(habitToDelete)
                editingHabit = null
            }
        )
    }
}

@Composable
fun EmptyHabitsState(
    onAddHabit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Animated icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(GradientPurple)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎯",
                fontSize = 56.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No habits yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Start your journey by adding your first habit.\nSmall steps lead to big changes!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddHabit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Your First Habit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "💡 Quick Tips",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Start with 1-3 habits\n• Set specific times for each habit\n• Track your progress daily\n• Celebrate small wins!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProgressOverviewCard(
    completedCount: Int,
    totalCount: Int,
    streaks: Map<Long, StreakInfo>
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val bestStreak = streaks.values.maxOfOrNull { it.currentStreak } ?: 0

    GradientCard(
        modifier = Modifier.fillMaxWidth(),
        gradientColors = GradientPurple
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Progress
            AnimatedCircularProgress(
                progress = progress,
                size = 100.dp,
                strokeWidth = 10.dp,
                gradientColors = listOf(Color.White, Color.White.copy(alpha = 0.7f)),
                backgroundColor = Color.White.copy(alpha = 0.2f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$completedCount of $totalCount habits completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (bestStreak > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Best streak: $bestStreak days",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatsRow(
    totalCompleted: Int,
    bestStreak: Int,
    activeHabits: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            icon = Icons.Outlined.CheckCircle,
            value = totalCompleted.toString(),
            label = "Completed\nToday",
            color = MaterialTheme.colorScheme.primary
        )
        StatItem(
            icon = Icons.Default.LocalFireDepartment,
            value = bestStreak.toString(),
            label = "Best\nStreak",
            color = Color(0xFFFF6B35)
        )
        StatItem(
            icon = Icons.Outlined.TrendingUp,
            value = activeHabits.toString(),
            label = "Active\nStreaks",
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedHabitCard(
    habit: Habit,
    streakInfo: StreakInfo?,
    isCompletedToday: Boolean,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val habitColor = parseColor(habit.colorHex)
    val icon = getIconByName(habit.iconName)
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isCompletedToday) {
        if (isCompletedToday) {
            scale.animateTo(
                targetValue = 1.05f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }
    }

    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .scale(scale.value)
            .shadow(
                elevation = if (isCompletedToday) 12.dp else 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = habitColor.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompletedToday)
                habitColor.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row - Icon, Edit, Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon with gradient background
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    habitColor.copy(alpha = 0.8f),
                                    habitColor.copy(alpha = 0.5f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit button
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEdit()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedCheckbox(
                        checked = isCompletedToday,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleComplete()
                        },
                        checkedColor = habitColor,
                        size = 32.dp
                    )
                }
            }

            // Bottom Section - Name, Streak/Status, Reminder
            Column {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Streak or Status
                if (streakInfo != null && streakInfo.currentStreak > 0) {
                    StreakBadge(streak = streakInfo.currentStreak)
                } else {
                    Text(
                        text = if (isCompletedToday) "Great job! ✨" else "Tap to complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompletedToday)
                            habitColor
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Reminder Indicator
                if (habit.reminderTime != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    ReminderIndicator(
                        time = habit.reminderTime,
                        color = habitColor
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderIndicator(
    time: String,
    color: Color
) {
    val formattedTime = remember(time) {
        try {
            val parts = time.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val amPm = if (hour >= 12) "PM" else "AM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            String.format("%d:%02d %s", displayHour, minute, amPm)
        } catch (e: Exception) {
            time
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Reminder",
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

fun getGreeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> "Good Morning! ☀️"
        hour < 17 -> "Good Afternoon! 🌤️"
        hour < 21 -> "Good Evening! 🌅"
        else -> "Good Night! 🌙"
    }
}