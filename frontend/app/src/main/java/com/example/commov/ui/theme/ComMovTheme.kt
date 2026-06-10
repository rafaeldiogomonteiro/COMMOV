package com.example.commov.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ComMovLightColors = lightColorScheme(
    primary = Color(0xFF2F67E8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDEBFF),
    onPrimaryContainer = Color(0xFF0457D8),
    secondary = Color(0xFF4C5567),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F6F8),
    onBackground = Color(0xFF20232B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF20232B),
    surfaceVariant = Color(0xFFEEF3FF),
    onSurfaceVariant = Color(0xFF4C5567),
    outline = Color(0xFFCED5E4),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

@Composable
fun ComMovTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ComMovLightColors,
        content = content,
    )
}
