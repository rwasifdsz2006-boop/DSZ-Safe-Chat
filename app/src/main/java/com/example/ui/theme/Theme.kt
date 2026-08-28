package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkGoldPrimary,
    onPrimary = Color(0xFF121212),
    primaryContainer = DarkGoldContainer,
    onPrimaryContainer = DarkOnGoldContainer,
    secondary = DarkGoldSecondary,
    onSecondary = Color(0xFF121212),
    secondaryContainer = Color(0xFF26210A),
    onSecondaryContainer = DarkOnGoldContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF2E2E36)
)

private val LightColorScheme = lightColorScheme(
    primary = LightGoldPrimary,
    onPrimary = Color(0xFF121212),
    primaryContainer = LightGoldContainer,
    onPrimaryContainer = LightOnGoldContainer,
    secondary = Color(0xFFB89426),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF7DB),
    onSecondaryContainer = Color(0xFF4A3E0D),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun DSZSaveChatTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme: ColorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // When isDark is false (Light Theme), isAppearanceLightStatusBars = true ensures dark icons (battery, wifi, time, signals) on light background.
                // When isDark is true (Dark Theme), isAppearanceLightStatusBars = false ensures crisp white icons on dark background.
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

