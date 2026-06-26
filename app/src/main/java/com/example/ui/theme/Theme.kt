package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CrowmixColorScheme = lightColorScheme(
    primary = CrowmixBlue,
    onPrimary = CrowmixWhite,
    primaryContainer = CrowmixBlueLight,
    onPrimaryContainer = CrowmixBlueDark,
    secondary = CrowmixBlueAccent,
    onSecondary = CrowmixWhite,
    background = CrowmixBgLight,
    onBackground = CrowmixTextPrimary,
    surface = CrowmixWhite,
    onSurface = CrowmixTextPrimary,
    outline = CrowmixBorder,
    error = CrowmixRed,
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CrowmixColorScheme,
        typography = Typography,
        content = content
    )
}
