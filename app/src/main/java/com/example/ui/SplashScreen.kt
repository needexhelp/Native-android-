package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1800AD)),
        contentAlignment = Alignment.Center
    ) {
        // Glow circle behind logo
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Logo Text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Crow", color = Color(0xFFFFFFFF), fontSize = 56.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                Text("m", color = Color(0xFF0BDD14), fontSize = 56.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                Text("i", color = Color(0xFFFFC107), fontSize = 56.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                Text("x", color = Color(0xFFFF3B30), fontSize = 56.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Everything. Delivered Fast.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Animated dots loader
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 200, 400).forEach { delay ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = delay),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$delay"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = dotAlpha))
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "IMPROVE LOCAL BUSINESS",
                color = Color.White.copy(alpha = alphaAnim),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
