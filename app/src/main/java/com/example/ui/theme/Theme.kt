package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = ForestGreenOnPrimary,
    primaryContainer = ForestGreenPrimaryContainer,
    onPrimaryContainer = ForestGreenOnPrimaryContainer,
    secondary = ForestGreenSecondary,
    onSecondary = ForestGreenOnSecondary,
    secondaryContainer = ForestGreenSecondaryContainer,
    onSecondaryContainer = ForestGreenOnSecondaryContainer,
    tertiary = AgriTertiary,
    onTertiary = ForestGreenOnPrimary,
    tertiaryContainer = AgriTertiaryContainer,
    onTertiaryContainer = AgriOnTertiaryContainer,
    background = AgriSurface,
    onBackground = AgriOnSurface,
    surface = AgriSurface,
    onSurface = AgriOnSurface,
    surfaceVariant = AgriSurfaceContainerHighest,
    onSurfaceVariant = AgriOnSurfaceVariant,
    surfaceContainer = AgriSurfaceContainer,
    surfaceContainerLow = AgriSurfaceContainerLow,
    surfaceContainerLowest = AgriSurfaceContainerLowest,
    surfaceContainerHigh = AgriSurfaceContainerHigh,
    surfaceContainerHighest = AgriSurfaceContainerHighest,
    outline = AgriOutline,
    outlineVariant = AgriOutlineVariant,
    error = AgriError,
    onError = AgriOnError,
    errorContainer = AgriErrorContainer,
    onErrorContainer = AgriOnErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = ForestGreenOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnPrimary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = ForestGreenSecondaryContainer,
    tertiary = AgriTertiaryContainer,
    onTertiary = AgriOnTertiaryContainer,
    tertiaryContainer = AgriTertiary,
    onTertiaryContainer = AgriTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceContainerHighest,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline = AgriOutline,
    outlineVariant = AgriOutlineVariant,
    error = AgriError,
    onError = AgriOnError,
    errorContainer = AgriErrorContainer,
    onErrorContainer = AgriOnErrorContainer
)

@Composable
fun LiveRoutineTheme(
    themeMode: String = "system", // "system", "light", "dark"
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
