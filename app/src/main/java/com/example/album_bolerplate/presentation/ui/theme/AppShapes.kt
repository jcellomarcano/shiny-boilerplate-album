package com.example.album_bolerplate.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Defines the shapes used throughout the application,
 * based on Material Design guidelines.
 */
val AppShapes = Shapes(

    // Shape used by small components like Buttons, Chips, TextFields
    small = RoundedCornerShape(4.dp),

    // Shape used by medium components like Cards
    medium = RoundedCornerShape(8.dp),

    // Shape used by large components like Modal Drawers, Bottom Sheets
    large = RoundedCornerShape(12.dp),

)
