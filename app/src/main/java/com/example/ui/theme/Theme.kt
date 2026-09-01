package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    themeMode: String = "System default",
    colorIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }

    val primaryColor = AppColorThemes.getOrElse(colorIndex) { AppColorThemes[0] }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.25f),
            onPrimaryContainer = Color.White,
            surface = Color(0xFF1E1E1E),
            onSurface = Color(0xFFE0E0E0),
            background = Color(0xFF121212),
            onBackground = Color(0xFFE0E0E0)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = primaryColor,
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF212121),
            background = Color(0xFFF7F8FA),
            onBackground = Color(0xFF212121)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
