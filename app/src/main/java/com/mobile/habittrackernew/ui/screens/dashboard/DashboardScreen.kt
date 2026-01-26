package com.mobile.habittrackernew.ui.screens.dashboard

import com.mobile.habittrackernew.ui.utils.getComposeColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.habittrackernew.data.models.Category
import com.mobile.habittrackernew.data.models.StreakInfo
import com.mobile.habittrackernew.ui.components.AnimatedCheckbox
import com.mobile.habittrackernew.ui.components.AnimatedCircularProgress
import com.mobile.habittrackernew.ui.components.GradientCard
import com.mobile.habittrackernew.ui.components.MotivationalQuoteCard
import com.mobile.habittrackernew.ui.components.StatItem
import com.mobile.habittrackernew.ui.components.StreakBadge
import com.mobile.habittrackernew.ui.theme.GradientPurple
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onCategoryClick: (Category) -> Unit,
    onNotificationClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    val quotes = listOf(
        Pair("The secret of getting ahead is getting started.", "Mark Twain"),
        Pair("Small daily improvements are the key to staggering long-term results.", "Robin Sharma"),
        Pair("Success is the sum of small efforts repeated day in and day out.", "Robert Collier"),
        Pair("You don't have to be great to start, but you have to start to be great.", "Zig Ziglar"),
        Pair("The only bad workout is the one that didn't happen.", "Unknown")
    )
    val randomQuote = remember { quotes.random() }
    val incompleteCount = Category.values().size - uiState.todayCompletedCount

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
            // Using LazyColumn for proper scrolling - FIXED!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress Overview Card
                item {
                    ProgressOverviewCard(
                        completedCount = uiState.todayCompletedCount,
                        totalCount = Category.values().size,
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
                    Text(
                        text = "Today's Habits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Categories - Using items() for each category
                items(Category.values().toList().chunked(2)) { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowCategories.forEach { category ->
                            EnhancedCategoryCard(
                                category = category,
                                streakInfo = uiState.streaks[category.name],
                                isCompletedToday = uiState.todayCompletions[category.name] ?: false,
                                onClick = { onCategoryClick(category) },
                                onToggleComplete = { viewModel.toggleHabitCompletion(category.name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // If odd number of items, add spacer
                        if (rowCategories.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ProgressOverviewCard(
    completedCount: Int,
    totalCount: Int,
    streaks: Map<String, StreakInfo>
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
fun EnhancedCategoryCard(
    category: Category,
    streakInfo: StreakInfo?,
    isCompletedToday: Boolean,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val categoryColor = category.getComposeColor()
    val icon = getCategoryIcon(category)
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
                spotColor = categoryColor.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompletedToday)
                categoryColor.copy(alpha = 0.15f)
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
                                    categoryColor.copy(alpha = 0.8f),
                                    categoryColor.copy(alpha = 0.5f)
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

                AnimatedCheckbox(
                    checked = isCompletedToday,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleComplete()
                    },
                    checkedColor = categoryColor,
                    size = 32.dp
                )
            }

            Column {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (streakInfo != null && streakInfo.currentStreak > 0) {
                    StreakBadge(streak = streakInfo.currentStreak)
                } else {
                    Text(
                        text = if (isCompletedToday) "Great job! ✨" else "Tap to complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompletedToday)
                            categoryColor
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
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

fun getCategoryIcon(category: Category): ImageVector {
    return when (category) {
        Category.YOGA -> Icons.Default.SelfImprovement
        Category.STUDY -> Icons.Default.MenuBook
        Category.DIET -> Icons.Default.Restaurant
        Category.EXERCISE -> Icons.Default.FitnessCenter
        Category.MEDITATION -> Icons.Default.Spa
        Category.HYDRATION -> Icons.Default.WaterDrop
        Category.SLEEP -> Icons.Default.Bedtime
        Category.SELF_CARE -> Icons.Default.Favorite
    }
}