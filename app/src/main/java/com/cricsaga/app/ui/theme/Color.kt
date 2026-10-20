package com.cricsaga.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * CricSaga Design Tokens — Phase 5.2 Dynamic Theme Engine
 *
 * Supports two distinct environments:
 *   - Dark Stadium Mode: Default deep OLED slate canvas for evening turf & floodlit matches.
 *   - Crisp Sunlight Light Mode: High-contrast pure white/slate palette for glaring outdoor noon matches.
 */
data class CricSagaColors(
    val backgroundCanvas: Color,
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val cardBorder: Color,
    val divider: Color,
    val brandPrimary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentFour: Color,
    val accentSix: Color,
    val alertWicket: Color,
    val extraBlue: Color,
    val isDark: Boolean
)

// === RAW COLOR CONSTANTS: DARK STADIUM ===
val DarkBackgroundCanvas = Color(0xFF0A0B0E)   // Deep OLED Slate
val DarkSurfaceCard = Color(0xFF14171F)        // Charcoal Slate
val DarkSurfaceElevated = Color(0xFF1C202B)    // Elevated Slate
val DarkCardBorder = Color(0xFF222733)         // Hairline Slate
val DarkDivider = Color(0xFF1E2129)            // Divider Line
val DarkBrandPrimary = Color(0xFFE53935)       // Broadcast Crimson
val DarkTextPrimary = Color(0xFFF9FAFB)        // High-Contrast White
val DarkTextSecondary = Color(0xFF9CA3AF)      // Muted Cool Gray
val DarkTextTertiary = Color(0xFF64748B)       // Subtle Slate
val DarkAccentFour = Color(0xFFD97706)         // Cricket Amber (4s / Pro)
val DarkAccentSix = Color(0xFF16A34A)          // Boundary Green (6s / Win)
val DarkAlertWicket = Color(0xFFDC2626)        // Dismissal Red (Wickets / Alert)
val DarkExtraBlue = Color(0xFF3B82F6)          // Extra Blue

// === RAW COLOR CONSTANTS: CRISP SUNLIGHT ===
val LightBackgroundCanvas = Color(0xFFF4F5F8)  // Clean Pitch Off-White
val LightSurfaceCard = Color(0xFFFFFFFF)       // Pure White Card
val LightSurfaceElevated = Color(0xFFEDF0F5)   // High-Contrast Neutral
val LightCardBorder = Color(0xFFD1D5DB)        // Sunlight Contrast Border
val LightDivider = Color(0xFFE5E7EB)           // Crisp Light Divider
val LightBrandPrimary = Color(0xFFC62828)      // Deep Cricket Crimson
val LightTextPrimary = Color(0xFF0F172A)       // Ink Black
val LightTextSecondary = Color(0xFF475569)     // Slate Gray
val LightTextTertiary = Color(0xFF94A3B8)      // Muted Neutral
val LightAccentFour = Color(0xFFB45309)        // Deep Amber
val LightAccentSix = Color(0xFF15803D)         // Forest Green
val LightAlertWicket = Color(0xFFB91C1C)       // Dark Alert Red
val LightExtraBlue = Color(0xFF2563EB)         // Sunlight Extra Blue

// Token Preset Bundles
val DarkCricSagaColors = CricSagaColors(
    backgroundCanvas = DarkBackgroundCanvas,
    surfaceCard = DarkSurfaceCard,
    surfaceElevated = DarkSurfaceElevated,
    cardBorder = DarkCardBorder,
    divider = DarkDivider,
    brandPrimary = DarkBrandPrimary,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    accentFour = DarkAccentFour,
    accentSix = DarkAccentSix,
    alertWicket = DarkAlertWicket,
    extraBlue = DarkExtraBlue,
    isDark = true
)

val LightCricSagaColors = CricSagaColors(
    backgroundCanvas = LightBackgroundCanvas,
    surfaceCard = LightSurfaceCard,
    surfaceElevated = LightSurfaceElevated,
    cardBorder = LightCardBorder,
    divider = LightDivider,
    brandPrimary = LightBrandPrimary,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    accentFour = LightAccentFour,
    accentSix = LightAccentSix,
    alertWicket = LightAlertWicket,
    extraBlue = LightExtraBlue,
    isDark = false
)

val LocalCricSagaColors = staticCompositionLocalOf { DarkCricSagaColors }

object CricSagaTheme {
    val colors: CricSagaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCricSagaColors.current
}

// ---------------------------------------------------------------------------
// Backward-Compatible Semantic Accessors
//
// These composable getters resolve to LocalCricSagaColors.current so that
// all 450+ existing color call sites automatically adapt to Dark/Light theme
// without requiring manual code refactoring.
// ---------------------------------------------------------------------------

val CharcoalBackgroundDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.backgroundCanvas

val SlateSurfaceDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.surfaceCard

val ElevatedSurfaceDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.surfaceElevated

val CardBorderDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.cardBorder

val DividerDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.divider

val CrimsonPrimaryDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.brandPrimary

val CrimsonPrimaryLight: Color
    get() = LightBrandPrimary

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.textSecondary

val TextTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.textTertiary

val MutedGrey: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.textSecondary

val BoundaryAmber: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.accentFour

val BoundaryGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.accentSix

val DismissalRed: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.alertWicket

val ExtraBlue: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalCricSagaColors.current.extraBlue

val CleanBackgroundLight: Color
    get() = LightBackgroundCanvas

val PureSurfaceLight: Color
    get() = LightSurfaceCard
