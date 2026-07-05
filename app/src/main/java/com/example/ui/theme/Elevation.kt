package com.example.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object CrowmixElevation {
    val Flat = 0.dp
    val Low = 2.dp      // product boxes, list rows, small icon chips
    val Medium = 2.dp   // cards, buttons, search bar, Doxa AI button -> 2dp shadow
    val High = 4.dp     // bottom nav bar, top app bar, ads banners / templates -> 4dp shadow
    val Modal = 12.dp   // dialogs, bottom sheets
}

fun Modifier.crowmixShadow(
    elevation: Dp,
    shape: Shape,
    clip: Boolean = false,
    isTeal: Boolean = false
): Modifier {
    val shadowColor = if (isTeal) Color(0xFF059494).copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.15f)
    return this
        .graphicsLayer {
            // Forces a dedicated compositing layer to prevent shadow flattening in nested layouts
        }
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = clip,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
}
