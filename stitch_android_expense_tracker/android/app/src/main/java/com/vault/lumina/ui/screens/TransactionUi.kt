package com.vault.lumina.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

internal fun transactionCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingBag
        "gaming" -> Icons.Default.SportsEsports
        "gym" -> Icons.Default.FitnessCenter
        "entertainment" -> Icons.Default.Movie
        "technology" -> Icons.Default.Cloud
        "health" -> Icons.Default.LocalHospital
        "travel" -> Icons.Default.Flight
        "subscription" -> Icons.Default.Subscriptions
        "housing" -> Icons.Default.Home
        else -> Icons.Default.Receipt
    }
}

internal fun prettifyCategory(category: String): String {
    return category.lowercase().replaceFirstChar { it.titlecase(Locale.US) }
}
