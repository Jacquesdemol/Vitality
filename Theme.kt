package com.vital.health.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

@Composable
fun VitalTheme(
    mode: ThemeMode = ThemeMode.DARK,
    palette: ThemePalette = ThemePalette.MONOCHROME,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val isDark = when (mode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val customColors = buildVitalColors(palette, isDark, context)

    val materialColorScheme = if (isDark) {
        darkColorScheme(
            primary = customColors.primary,
            onPrimary = customColors.onPrimary,
            surface = customColors.surface,
            onSurface = customColors.textPrimary,
            background = customColors.background,
            onBackground = customColors.textPrimary,
            outline = customColors.surfaceVariant,
            surfaceVariant = customColors.surfaceVariant,
            onSurfaceVariant = customColors.textSecondary
        )
    } else {
        lightColorScheme(
            primary = customColors.primary,
            onPrimary = customColors.onPrimary,
            surface = customColors.surface,
            onSurface = customColors.textPrimary,
            background = customColors.background,
            onBackground = customColors.textPrimary,
            outline = customColors.surfaceVariant,
            surfaceVariant = customColors.surfaceVariant,
            onSurfaceVariant = customColors.textSecondary
        )
    }

    CompositionLocalProvider(LocalVitalColors provides customColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}
