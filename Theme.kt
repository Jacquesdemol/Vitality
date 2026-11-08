package com.vital.health.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CrispWhite,
    onPrimary = DeepBlack,
    surface = Graphite,
    onSurface = CrispWhite,
    background = DeepBlack,
    onBackground = CrispWhite,
    outline = DarkGray,
    surfaceVariant = DarkGray,
    onSurfaceVariant = MidGray
)

@Composable
fun VitalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
