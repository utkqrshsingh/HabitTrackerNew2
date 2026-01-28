// ui/screens/splash/SplashScreen.kt
package com.mobile.habittrackernew.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.habittrackernew.ui.theme.GradientPurple
import com.mobile.habittrackernew.ui.theme.PrimaryLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: (isLoggedIn: Boolean) -> Unit,  // Updated signature
    viewModel: SplashViewModel = hiltViewModel()       // Added ViewModel
) {
    var startAnimation by remember { mutableStateOf(false) }

    // Auth state from ViewModel
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isCheckComplete by viewModel.isCheckComplete.collectAsState()

    // Logo scale animation
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    // Logo alpha animation
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "logoAlpha"
    )

    // Text alpha animation (delayed)
    var showText by remember { mutableStateOf(false) }
    val textAlpha by animateFloatAsState(
        targetValue = if (showText) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "textAlpha"
    )

    // Infinite transition for animations
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

    // Pulsing animation for glow
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Floating particles
    val particle1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle1Y"
    )

    val particle2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle2Y"
    )

    // Start animations
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(500)
        showText = true
    }

    // Navigate when auth check is complete (after minimum splash time)
    LaunchedEffect(isCheckComplete) {
        if (isCheckComplete) {
            delay(2000) // Keep original 2 second splash duration
            onSplashComplete(isLoggedIn)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background floating particles
        FloatingParticles(
            particle1Y = particle1Y,
            particle2Y = particle2Y,
            alpha = logoAlpha
        )

        // Pulsing glow behind logo
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(pulseScale)
                .alpha(0.3f * logoAlpha)
                .blur(40.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryLight,
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Main logo container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(logoScale)
                .alpha(logoAlpha)
        ) {
            // Logo icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.linearGradient(GradientPurple)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Habit checkmark design
                Canvas(
                    modifier = Modifier.size(60.dp)
                ) {
                    val strokeWidth = 6.dp.toPx()

                    // Draw checkmark
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.2f, size.height * 0.5f),
                        end = Offset(size.width * 0.4f, size.height * 0.7f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.4f, size.height * 0.7f),
                        end = Offset(size.width * 0.8f, size.height * 0.3f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )

                    // Draw circular progress
                    drawArc(
                        color = Color.White.copy(alpha = 0.3f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App name
            Text(
                text = "HabitTracker",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Build Better Habits",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}

@Composable
private fun FloatingParticles(
    particle1Y: Float,
    particle2Y: Float,
    alpha: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Particle 1 - Top left
        Box(
            modifier = Modifier
                .offset(x = 60.dp, y = 200.dp + particle1Y.dp)
                .size(8.dp)
                .alpha(alpha * 0.6f)
                .background(
                    color = PrimaryLight.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        )

        // Particle 2 - Top right
        Box(
            modifier = Modifier
                .offset(x = 300.dp, y = 150.dp + particle2Y.dp)
                .size(6.dp)
                .alpha(alpha * 0.4f)
                .background(
                    color = Color(0xFF10B981).copy(alpha = 0.5f),
                    shape = CircleShape
                )
        )

        // Particle 3 - Bottom left
        Box(
            modifier = Modifier
                .offset(x = 80.dp, y = 600.dp + particle1Y.dp * 0.5f)
                .size(10.dp)
                .alpha(alpha * 0.3f)
                .background(
                    color = Color(0xFFF59E0B).copy(alpha = 0.5f),
                    shape = CircleShape
                )
        )

        // Particle 4 - Bottom right
        Box(
            modifier = Modifier
                .offset(x = 280.dp, y = 500.dp + particle2Y.dp * 0.7f)
                .size(5.dp)
                .alpha(alpha * 0.5f)
                .background(
                    color = Color(0xFFEC4899).copy(alpha = 0.5f),
                    shape = CircleShape
                )
        )
    }
}