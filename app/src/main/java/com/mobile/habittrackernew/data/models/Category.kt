package com.mobile.habittrackernew.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
) {
    YOGA(
        displayName = "Yoga",
        icon = Icons.Default.SelfImprovement,
        color = Color(0xFF9C27B0),
        description = "Daily yoga practice"
    ),
    STUDY(
        displayName = "Study",
        icon = Icons.Default.MenuBook,
        color = Color(0xFF2196F3),
        description = "Learning activities"
    ),
    DIET(
        displayName = "Diet",
        icon = Icons.Default.Restaurant,
        color = Color(0xFF4CAF50),
        description = "Healthy eating"
    ),
    EXERCISE(
        displayName = "Exercise",
        icon = Icons.Default.FitnessCenter,
        color = Color(0xFFFF5722),
        description = "Physical workout"
    ),
    MEDITATION(
        displayName = "Meditation",
        icon = Icons.Default.Spa,
        color = Color(0xFF00BCD4),
        description = "Mindfulness"
    ),
    HYDRATION(
        displayName = "Hydration",
        icon = Icons.Default.WaterDrop,
        color = Color(0xFF03A9F4),
        description = "Stay hydrated"
    ),
    SLEEP(
        displayName = "Sleep",
        icon = Icons.Default.Bedtime,
        color = Color(0xFF673AB7),
        description = "Quality rest"
    ),
    SELF_CARE(
        displayName = "Self Care",
        icon = Icons.Default.Favorite,
        color = Color(0xFFE91E63),
        description = "Personal wellness"
    )
}