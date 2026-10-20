package com.cricsaga.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkBrandPrimary,
    onPrimary = Color.White,
    background = DarkBackgroundCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkSurfaceCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextPrimary,
    secondary = DarkAccentFour,
    onSecondary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = LightBrandPrimary,
    onPrimary = Color.White,
    background = LightBackgroundCanvas,
    onBackground = LightTextPrimary,
    surface = LightSurfaceCard,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextPrimary,
    secondary = LightAccentFour,
    onSecondary = Color.White
)

@Composable
fun CricSagaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val cricsagaColors = if (darkTheme) DarkCricSagaColors else LightCricSagaColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = cricsagaColors.backgroundCanvas.toArgb()
                it.navigationBarColor = cricsagaColors.backgroundCanvas.toArgb()
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalCricSagaColors provides cricsagaColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CricSagaTypography,
            content = content
        )
    }
}
