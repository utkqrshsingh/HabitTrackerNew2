package com.mobile.habittrackernew.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.habittrackernew.data.models.Category
import com.mobile.habittrackernew.data.models.HabitLog
import com.mobile.habittrackernew.ui.utils.getCategoryIcon
import com.mobile.habittrackernew.ui.utils.getComposeColor

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Progress",
                        fontWeight = FontWeight.Bold
                    )
                }
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overall Stats Card
                item {
                    OverallStatsCard(
                        totalCompleted = uiState.totalCompleted,
                        currentStreaks = uiState.streaks
                    )
                }

                // Category Progress Table
                item {
                    CategoryProgressTable(
                        streaks = uiState.streaks,
                        completionRates = uiState.completionRates
                    )
                }

                // Calendar Navigation
                item {
                    CalendarNavigation(
                        selectedMonth = selectedMonth,
                        onPreviousMonth = {
                            selectedMonth = selectedMonth.minusMonths(1)
                            viewModel.loadMonthData(selectedMonth)
                        },
                        onNextMonth = {
                            if (selectedMonth.isBefore(YearMonth.now())) {
                                selectedMonth = selectedMonth.plusMonths(1)
                                viewModel.loadMonthData(selectedMonth)
                            }
                        }
                    )
                }

                // Full Calendar View
                item {
                    FullCalendarCard(
                        yearMonth = selectedMonth,
                        logs = uiState.monthLogs
                    )
                }

                // Daily Breakdown Header
                item {
                    Text(
                        text = "Daily Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Daily Breakdown Items
                items(
                    uiState.dailyBreakdown.entries.toList()
                        .sortedByDescending { it.key }
                        .take(14)
                ) { (date, logs) ->
                    DailyBreakdownCard(date = date, logs = logs)
                }
            }
        }
    }
}

@Composable
fun OverallStatsCard(
    totalCompleted: Int,
    currentStreaks: Map<String, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Your Journey",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = totalCompleted.toString(),
                    label = "Total\nCompleted",
                    icon = Icons.Default.CheckCircle
                )
                StatItem(
                    value = (currentStreaks.values.maxOrNull() ?: 0).toString(),
                    label = "Best\nStreak",
                    icon = Icons.Default.LocalFireDepartment
                )
                StatItem(
                    value = "${currentStreaks.values.count { it > 0 }}/${Category.values().size}",
                    label = "Active\nHabits",
                    icon = Icons.Default.TrendingUp
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CategoryProgressTable(
    streaks: Map<String, Int>,
    completionRates: Map<String, Float>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Category Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Table Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    text = "Streak",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Rate",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Category.values().forEach { category ->
                val categoryColor = category.getComposeColor()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1.5f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(categoryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(category),
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = "${streaks[category.name] ?: 0} days",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "${((completionRates[category.name] ?: 0f) * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarNavigation(
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }

        Text(
            text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = onNextMonth,
            enabled = selectedMonth.isBefore(YearMonth.now())
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Next month",
                tint = if (selectedMonth.isBefore(YearMonth.now()))
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun FullCalendarCard(
    yearMonth: YearMonth,
    logs: List<HabitLog>
) {
    val today = LocalDate.now()
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val logsByDate = logs.groupBy { it.date }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            val startOffset = (firstDayOfMonth.dayOfWeek.value % 7)
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
                            val date = yearMonth.atDay(dayNumber)
                            val dateStr = date.format(dateFormatter)
                            val dayLogs = logsByDate[dateStr] ?: emptyList()
                            val completedCount = dayLogs.count { it.isCompleted }
                            val isToday = date == today
                            val isFuture = date.isAfter(today)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isToday -> MaterialTheme.colorScheme.primary
                                                    completedCount > 0 -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> Color.Transparent
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when {
                                                isToday -> MaterialTheme.colorScheme.onPrimary
                                                isFuture -> MaterialTheme.colorScheme.onSurfaceVariant
                                                    .copy(alpha = 0.5f)
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                    if (completedCount > 0) {
                                        Text(
                                            text = "$completedCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
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
fun DailyBreakdownCard(
    date: String,
    logs: List<HabitLog>
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val displayFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

    val localDate = try {
        LocalDate.parse(date, dateFormatter)
    } catch (e: Exception) {
        null
    }

    val completedLogs = logs.filter { it.isCompleted }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localDate?.format(displayFormatter) ?: date,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${completedLogs.size}/${Category.values().size} completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (completedLogs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    completedLogs.forEach { log ->
                        val category = try {
                            Category.valueOf(log.category)
                        } catch (e: Exception) {
                            null
                        }

                        category?.let {
                            val categoryColor = it.getComposeColor()

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(categoryColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(it),
                                    contentDescription = it.displayName,
                                    tint = categoryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}