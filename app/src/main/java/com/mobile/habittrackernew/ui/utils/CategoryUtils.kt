package com.mobile.habittrackernew.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mobile.habittrackernew.data.models.Category


fun Category.getComposeColor(): Color {
    return when (this) {
        Category.YOGA -> Color(0xFF7E57C2)
        Category.STUDY -> Color(0xFF42A5F5)
        Category.DIET -> Color(0xFF66BB6A)
        Category.EXERCISE -> Color(0xFFEF5350)
        Category.MEDITATION -> Color(0xFF26A69A)
        Category.HYDRATION -> Color(0xFF29B6F6)
        Category.SLEEP -> Color(0xFF5C6BC0)
        Category.SELF_CARE -> Color(0xFFAB47BC)
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
        Category.SLEEP -> Icons.Default.Bed
        Category.SELF_CARE -> Icons.Default.Face
    }
}