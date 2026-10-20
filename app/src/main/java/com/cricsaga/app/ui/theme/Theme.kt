package com.cricsaga.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
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
    primary = CrimsonPrimaryDark,
    onPrimary = Color.White,
    background = CharcoalBackgroundDark,
    onBackground = Color.White,
    surface = SlateSurfaceDark,
    onSurface = Color.White,
    surfaceVariant = ElevatedSurfaceDark,
    onSurfaceVariant = Color.White,
    secondary = BoundaryAmber,
    onSecondary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonPrimaryLight,
    onPrimary = Color.White,
    background = CleanBackgroundLight,
    onBackground = Color(0xFF1C1B1F),
    surface = PureSurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = Color(0xFF1C1B1F),
    secondary = BoundaryAmber,
    onSecondary = Color.Black
)

@Composable
fun CricSagaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
