package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CalmGreenLight,
    onPrimary = DarkNavyBg,
    primaryContainer = DarkNavyCard,
    onPrimaryContainer = CalmGreenSubtle,
    secondary = CalmBlueLight,
    onSecondary = DarkNavyBg,
    secondaryContainer = DarkNavyCard,
    onSecondaryContainer = CalmBlueSubtle,
    tertiary = SoftLilac,
    onTertiary = DarkNavyBg,
    background = DarkNavyBg,
    onBackground = DarkTextPrimary,
    surface = DarkNavySurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkNavyCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkGlassBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CalmBlue,
    onPrimary = Color.White,
    primaryContainer = LightGlassCard,
    onPrimaryContainer = CalmBlue,
    secondary = CalmGreen,
    onSecondary = Color.White,
    secondaryContainer = LightGlassCard,
    onSecondaryContainer = CalmGreen,
    tertiary = CalmBlueLight,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorder
)

@Composable
fun RhythmFitTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use transparent system bars for seamless iPhone-like edge-to-edge
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
