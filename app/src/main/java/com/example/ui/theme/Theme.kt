package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class NovaThemeMode {
    FUTURISTIC,
    DEEP_SPACE,
    CLEAN_LIGHT
}

private val FuturisticColorScheme = darkColorScheme(
    primary = NovaCyan,
    onPrimary = Color(0xFF001B24),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFB5EBFF),
    secondary = NovaPurpleLight,
    onSecondary = Color(0xFF260058),
    secondaryContainer = Color(0xFF3F1A73),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = NovaEmerald,
    background = CyberpunkBackground,
    onBackground = CyberpunkOnSurface,
    surface = CyberpunkSurface,
    onSurface = CyberpunkOnSurface,
    surfaceVariant = CyberpunkSurfaceVariant,
    onSurfaceVariant = CyberpunkOnSurfaceVariant,
    outline = CyberpunkOutline
)

private val DeepSpaceColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF002235),
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFFA855F7),
    onSecondary = Color(0xFF38006B),
    secondaryContainer = Color(0xFF581C87),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = NovaRose,
    background = DeepSpaceBackground,
    onBackground = Color(0xFFF9FAFB),
    surface = DeepSpaceSurface,
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = DeepSpaceSurfaceVariant,
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF374151)
)

private val CleanLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF003554),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDD6FE),
    onSecondaryContainer = Color(0xFF2E1065),
    tertiary = Color(0xFF059669),
    background = CleanLightBackground,
    onBackground = CleanLightOnSurface,
    surface = CleanLightSurface,
    onSurface = CleanLightOnSurface,
    surfaceVariant = CleanLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun NovaTheme(
    themeMode: NovaThemeMode = NovaThemeMode.FUTURISTIC,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        NovaThemeMode.FUTURISTIC -> FuturisticColorScheme
        NovaThemeMode.DEEP_SPACE -> DeepSpaceColorScheme
        NovaThemeMode.CLEAN_LIGHT -> CleanLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
