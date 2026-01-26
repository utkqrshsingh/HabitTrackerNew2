package com.mobile.habittrackernew.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.habittrackernew.ui.theme.GradientPurple
import com.mobile.habittrackernew.ui.theme.StreakBronze
import com.mobile.habittrackernew.ui.theme.StreakDiamond
import com.mobile.habittrackernew.ui.theme.StreakGold
import com.mobile.habittrackernew.ui.theme.StreakPlatinum
import com.mobile.habittrackernew.ui.theme.StreakSilver

// Animated Circular Progress
@Composable
fun AnimatedCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    gradientColors: List<Color> = GradientPurple,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val sweepAngle = animatedProgress * 360f
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - strokeWidthPx, size.toPx() - strokeWidthPx)
            val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)

            // Background arc
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Progress arc with gradient
            drawArc(
                brush = Brush.sweepGradient(gradientColors),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
        content()
    }
}

// Animated Checkbox with Haptic Feedback
@Composable
fun AnimatedCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkedColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColor: Color = MaterialTheme.colorScheme.outline,
    size: Dp = 28.dp
) {
    val haptic = LocalHapticFeedback.current
    val scale = remember { Animatable(1f) }

    val backgroundColor by animateColorAsState(
        targetValue = if (checked) checkedColor else Color.Transparent,
        animationSpec = tween(200),
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) checkedColor else uncheckedColor,
        animationSpec = tween(200),
        label = "borderColor"
    )

    LaunchedEffect(checked) {
        if (checked) {
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scale.value)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

// Streak Badge with Animation
@Composable
fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier,
    showAnimation: Boolean = true
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(streak) {
        if (showAnimation && streak > 0) {
            scale.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    val (backgroundColor, textColor, icon) = when {
        streak >= 100 -> Triple(StreakDiamond, Color.Black, "💎")
        streak >= 60 -> Triple(StreakPlatinum, Color.Black, "👑")
        streak >= 30 -> Triple(StreakGold, Color.Black, "🏆")
        streak >= 14 -> Triple(StreakSilver, Color.Black, "⭐")
        streak >= 7 -> Triple(StreakBronze, Color.White, "🔥")
        else -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "🔥")
    }

    Row(
        modifier = modifier
            .scale(scale.value)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$streak day${if (streak != 1) "s" else ""}",
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

// Gradient Card
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = GradientPurple,
    cornerRadius: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = gradientColors.first().copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(gradientColors)
                )
        ) {
            content()
        }
    }
}

// Stats Card Item
@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Motivational Quote Card
@Composable
fun MotivationalQuoteCard(
    quote: String,
    author: String,
    modifier: Modifier = Modifier
) {
    GradientCard(
        modifier = modifier.fillMaxWidth(),
        gradientColors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "💭",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"$quote\"",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "— $author",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// Week Day Indicator
@Composable
fun WeekDayIndicator(
    dayLabel: String,
    isCompleted: Boolean,
    isToday: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> color
                        isToday -> color.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .then(
                    if (isToday && !isCompleted) {
                        Modifier.border(2.dp, color, CircleShape)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Empty State
@Composable
fun EmptyState(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}