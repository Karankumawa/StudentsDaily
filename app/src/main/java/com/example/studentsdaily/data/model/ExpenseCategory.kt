package com.example.studentsdaily.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.studentsdaily.ui.theme.CategoryAcademicColor
import com.example.studentsdaily.ui.theme.CategoryEntertainmentColor
import com.example.studentsdaily.ui.theme.CategoryFoodColor
import com.example.studentsdaily.ui.theme.CategoryHealthColor
import com.example.studentsdaily.ui.theme.CategoryTransportColor
import com.example.studentsdaily.ui.theme.CategoryUtilitiesColor

enum class ExpenseCategory(
    val categoryName: String,
    val icon: ImageVector,
    val color: Color
) {
    FOOD("Food", Icons.Rounded.Fastfood, CategoryFoodColor), // Red #E53935
    TRANSPORT("Transport", Icons.Rounded.DirectionsBus, CategoryTransportColor), // Blue #1E88E5
    ACADEMIC("Academic / Books", Icons.Rounded.Book, CategoryAcademicColor), // Purple #8E24AA
    ENTERTAINMENT("Entertainment", Icons.Rounded.Movie, CategoryEntertainmentColor), // Orange #FB8C00
    HEALTH("Health / Fitness", Icons.Rounded.Favorite, CategoryHealthColor), // Teal #00897B
    UTILITIES("Utilities / General", Icons.Rounded.Receipt, CategoryUtilitiesColor) // Green #43A047
}
