package com.example.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object CrowmixElevation {
    val Flat = 0.dp
    val Low = 2.dp      // list rows, small icon chips
    val Medium = 4.dp   // cards, buttons, search bar
    val High = 8.dp     // bottom nav bar, top app bar, FAB
    val Modal = 12.dp   // dialogs, bottom sheets
}

fun Modifier.crowmixShadow(
    elevation: Dp,
    shape: Shape,
    clip: Boolean = false,
    isTeal: Boolean = false
): Modifier {
    val shadowColor = if (isTeal) Color(0xFF059494).copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.10f)
    return this.shadow(
        elevation = elevation,
        shape = shape,
        clip = clip,
        ambientColor = shadowColor,
        spotColor = shadowColor
    )
}
