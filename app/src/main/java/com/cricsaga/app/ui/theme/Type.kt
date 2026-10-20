package com.cricsaga.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cricsaga.app.R

// ---------------------------------------------------------------------------
// CricSaga Typography System — Phase 5.0
//
// TWO typeface families:
//
//   BarlowCondensed — Athletic grotesque for all numeric/score displays.
//                     Condensed numerals allow larger point sizes in the same
//                     card width.  Used ONLY for runs, wickets, overs, averages.
//
//   DmSans          — Clean, warm UI face for names, headers, body, tags.
//                     Used for everything that is not a sports numeric figure.
//
// Both families are declared as downloadable fonts (res/font/*.xml) and are
// preloaded via AndroidManifest.xml — zero APK size cost.
// ---------------------------------------------------------------------------

val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed, FontWeight.Normal),
    Font(R.font.barlow_condensed, FontWeight.Bold),
    Font(R.font.barlow_condensed, FontWeight.ExtraBold)
)

val DmSans = FontFamily(
    Font(R.font.dm_sans, FontWeight.Normal),
    Font(R.font.dm_sans, FontWeight.Medium),
    Font(R.font.dm_sans, FontWeight.SemiBold),
    Font(R.font.dm_sans, FontWeight.Bold)
)

// ---------------------------------------------------------------------------
// Material3 Typography Binding
// ---------------------------------------------------------------------------

val CricSagaTypography = Typography(
    // displayLarge — Primary scoreboard: runs/wickets in scoring cockpit
    // Rendered as BarlowCondensed ExtraBold 56sp
    displayLarge = TextStyle(
        fontFamily = BarlowCondensed,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 56.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.5).sp
    ),
    // displayMedium — Home screen live radar score, full scorecard totals
    displayMedium = TextStyle(
        fontFamily = BarlowCondensed,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.25).sp
    ),
    // displaySmall — Profile metric strip (matches, runs, wickets, avg)
    displaySmall = TextStyle(
        fontFamily = BarlowCondensed,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // headlineLarge — Screen title bars ("My Career", "Match Setup")
    headlineLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    // headlineMedium — Section headers ("LIVE MATCHES", "TOP PERFORMERS")
    headlineMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    // headlineSmall — Card sub-headers, dialog titles
    headlineSmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    // titleLarge — Top app bar title ("CricSaga")
    titleLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    // titleMedium — Player names (prominent), match titles
    titleMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    // titleSmall — Team names, secondary card titles
    titleSmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    // bodyLarge — Description text, settings items, setup form labels
    bodyLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    // bodyMedium — Stat table rows, match info text, overs/extras
    bodyMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    // bodySmall — Dates, venues, secondary metadata
    bodySmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
    // labelLarge — Primary CTA button text ("Start New Match", "Sign In")
    labelLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    // labelMedium — Micro-tags (LIVE, POTM, CRR, RRR, TARGET, INNINGS BREAK)
    // Uppercase tracking applied at call site via letterSpacing = 1.5.sp
    labelMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp
    ),
    // labelSmall — Tertiary info: ball type, pitch conditions, venue notes
    labelSmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.5.sp
    )
)
