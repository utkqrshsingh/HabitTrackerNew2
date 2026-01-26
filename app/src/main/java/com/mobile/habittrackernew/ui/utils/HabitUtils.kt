package com.mobile.habittrackernew.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mobile.habittrackernew.data.models.Category

// Icon mapping
fun getIconByName(iconName: String): ImageVector {
    return when (iconName) {
        "SelfImprovement" -> Icons.Default.SelfImprovement
        "MenuBook" -> Icons.Default.MenuBook
        "Restaurant" -> Icons.Default.Restaurant
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "Spa" -> Icons.Default.Spa
        "WaterDrop" -> Icons.Default.WaterDrop
        "Bedtime" -> Icons.Default.Bedtime
        "Favorite" -> Icons.Default.Favorite
        "DirectionsRun" -> Icons.Default.DirectionsRun
        "MusicNote" -> Icons.Default.MusicNote
        "Brush" -> Icons.Default.Brush
        "Code" -> Icons.Default.Code
        "AttachMoney" -> Icons.Default.AttachMoney
        "People" -> Icons.Default.People
        "Nature" -> Icons.Default.Nature
        "LocalCafe" -> Icons.Default.LocalCafe
        "Create" -> Icons.Default.Create
        "Pets" -> Icons.Default.Pets
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "Work" -> Icons.Default.Work
        else -> Icons.Default.CheckCircle
    }
}

// Available icons for habit creation
val availableIcons = listOf(
    "SelfImprovement" to Icons.Default.SelfImprovement,
    "MenuBook" to Icons.Default.MenuBook,
    "Restaurant" to Icons.Default.Restaurant,
    "FitnessCenter" to Icons.Default.FitnessCenter,
    "Spa" to Icons.Default.Spa,
    "WaterDrop" to Icons.Default.WaterDrop,
    "Bedtime" to Icons.Default.Bedtime,
    "Favorite" to Icons.Default.Favorite,
    "DirectionsRun" to Icons.Default.DirectionsRun,
    "MusicNote" to Icons.Default.MusicNote,
    "Brush" to Icons.Default.Brush,
    "Code" to Icons.Default.Code,
    "AttachMoney" to Icons.Default.AttachMoney,
    "People" to Icons.Default.People,
    "Nature" to Icons.Default.Nature,
    "LocalCafe" to Icons.Default.LocalCafe,
    "Create" to Icons.Default.Create,
    "Pets" to Icons.Default.Pets,
    "ShoppingCart" to Icons.Default.ShoppingCart,
    "Work" to Icons.Default.Work
)

// Available colors for habit creation
val availableColors = listOf(
    "#9C27B0", // Purple
    "#2196F3", // Blue
    "#4CAF50", // Green
    "#FF5722", // Deep Orange
    "#00BCD4", // Cyan
    "#03A9F4", // Light Blue
    "#673AB7", // Deep Purple
    "#E91E63", // Pink
    "#FF9800", // Orange
    "#009688", // Teal
    "#795548", // Brown
    "#607D8B", // Blue Grey
    "#F44336", // Red
    "#FFEB3B", // Yellow
    "#8BC34A", // Light Green
    "#3F51B5"  // Indigo
)

// Parse hex color to Compose Color
fun parseColor(hexColor: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (e: Exception) {
        Color(0xFF4CAF50) // Default green
    }
}
