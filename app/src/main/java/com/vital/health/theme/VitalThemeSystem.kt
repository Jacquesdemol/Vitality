package com.vital.health.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode(val label: String) {
    SYSTEM("Seguir sistema"),
    DARK("Escuro"),
    LIGHT("Claro")
}

enum class ThemePalette(val label: String, val previewColor: Color) {
    MONOCHROME("Monocromático", Color(0xFFF2F2F4)),
    OCEAN_BLUE("Azul Oceano", Color(0xFF0288D1)),
    EMERALD_GREEN("Verde Esmeralda", Color(0xFF4CAF50)),
    AMETHYST_PURPLE("Roxo Ametista", Color(0xFFAB47BC)),
    SUNSET_ORANGE("Laranja Pôr do Sol", Color(0xFFFF9800)),
    DYNAMIC_MONET("Material You / Monet", Color(0xFF81D4FA))
}

data class VitalColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val cardBorder: Color,
    val primary: Color,
    val onPrimary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val pillHighlight: Color,
    val pillBackground: Color,
    val chartBarActive: Color,
    val chartBarInactive: Color,
    val streakFlame: Color,
    val isDark: Boolean
)

val LocalVitalColors = staticCompositionLocalOf {
    VitalColors(
        background = Color(0xFF090909),
        surface = Color(0xFF171719),
        surfaceVariant = Color(0xFF242428),
        cardBorder = Color(0x1AFFFFFF),
        primary = Color(0xFFF2F2F4),
        onPrimary = Color(0xFF090909),
        textPrimary = Color(0xFFF2F2F4),
        textSecondary = Color(0xFF85858C),
        pillHighlight = Color(0xFF2E2E36),
        pillBackground = Color(0xEB171719),
        chartBarActive = Color(0xFFF2F2F4),
        chartBarInactive = Color(0xFF242428),
        streakFlame = Color(0xFFFF9800),
        isDark = true
    )
}

object VitalTheme {
    val colors: VitalColors
        @Composable
        get() = LocalVitalColors.current
}

class ThemePreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("vital_theme_prefs", Context.MODE_PRIVATE)

    fun getThemeMode(): ThemeMode {
        val raw = prefs.getString("theme_mode", ThemeMode.DARK.name)
        return try {
            ThemeMode.valueOf(raw ?: ThemeMode.DARK.name)
        } catch (_: Exception) {
            ThemeMode.DARK
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun getThemePalette(): ThemePalette {
        val raw = prefs.getString("theme_palette", ThemePalette.MONOCHROME.name)
        return try {
            ThemePalette.valueOf(raw ?: ThemePalette.MONOCHROME.name)
        } catch (_: Exception) {
            ThemePalette.MONOCHROME
        }
    }

    fun setThemePalette(palette: ThemePalette) {
        prefs.edit().putString("theme_palette", palette.name).apply()
    }
}

fun buildVitalColors(palette: ThemePalette, isDark: Boolean, context: Context): VitalColors {
    // Primary accents based on palette
    val primaryAccent = when (palette) {
        ThemePalette.MONOCHROME -> if (isDark) Color(0xFFF2F2F4) else Color(0xFF111113)
        ThemePalette.OCEAN_BLUE -> if (isDark) Color(0xFF4FC3F7) else Color(0xFF0288D1)
        ThemePalette.EMERALD_GREEN -> if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
        ThemePalette.AMETHYST_PURPLE -> if (isDark) Color(0xFFBA68C8) else Color(0xFF7B1FA2)
        ThemePalette.SUNSET_ORANGE -> if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)
        ThemePalette.DYNAMIC_MONET -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Fallback to blue if dynamic colors extraction fails in custom theme
                Color(0xFF81D4FA)
            } else {
                Color(0xFF4FC3F7)
            }
        }
    }

    return if (isDark) {
        VitalColors(
            background = Color(0xFF090909),
            surface = Color(0xFF171719),
            surfaceVariant = Color(0xFF242428),
            cardBorder = Color(0x1AFFFFFF),
            primary = primaryAccent,
            onPrimary = Color(0xFF090909),
            textPrimary = Color(0xFFF2F2F4),
            textSecondary = Color(0xFF85858C),
            pillHighlight = Color(0xFF2E2E36),
            pillBackground = Color(0xEB171719),
            chartBarActive = primaryAccent,
            chartBarInactive = Color(0xFF242428),
            streakFlame = Color(0xFFFF9800),
            isDark = true
        )
    } else {
        VitalColors(
            background = Color(0xFFF7F7FA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEFEFF3),
            cardBorder = Color(0x14000000),
            primary = primaryAccent,
            onPrimary = Color(0xFFFFFFFF),
            textPrimary = Color(0xFF141416),
            textSecondary = Color(0xFF6B6B73),
            pillHighlight = Color(0xFFE0E0E6),
            pillBackground = Color(0xF2FFFFFF),
            chartBarActive = primaryAccent,
            chartBarInactive = Color(0xFFE0E0E6),
            streakFlame = Color(0xFFE65100),
            isDark = false
        )
    }
}
