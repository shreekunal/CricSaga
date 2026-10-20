package com.cricsaga.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// CricSaga Named Text Styles — Phase 5.0
//
// Use these named styles at call sites instead of hardcoding fontFamily +
// fontSize + fontWeight inline. This ensures that any future font change
// only needs to be made once here.
//
// USAGE PATTERN:
//   Text(
//       text = "142",
//       style = ScoreBig,
//       color = TextPrimary
//   )
// ---------------------------------------------------------------------------

// === SCORE / NUMERIC DISPLAY (BarlowCondensed) ===

/**
 * ScoreBig — Primary scoring cockpit runs/wickets display.
 * "142 / 4"
 * BarlowCondensed ExtraBold 56sp
 */
val ScoreBig = TextStyle(
    fontFamily = BarlowCondensed,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 56.sp,
    lineHeight = 60.sp,
    letterSpacing = (-0.5).sp
)

/**
 * ScoreMedium — Home radar card score, full scorecard inning totals.
 * "86 / 2"
 * BarlowCondensed ExtraBold 36sp
 */
val ScoreMedium = TextStyle(
    fontFamily = BarlowCondensed,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 36.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.25).sp
)

/**
 * ScoreSmall — Scorecard table individual batsman / bowler numerals.
 * "64*", "3/16"
 * BarlowCondensed Bold 20sp
 */
val ScoreSmall = TextStyle(
    fontFamily = BarlowCondensed,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 24.sp
)

/**
 * MetricDisplay — Profile metric strip values (Matches, Runs, Wickets, Avg).
 * "782", "24", "14", "35.5"
 * BarlowCondensed Bold 26sp
 */
val MetricDisplay = TextStyle(
    fontFamily = BarlowCondensed,
    fontWeight = FontWeight.Bold,
    fontSize = 26.sp,
    lineHeight = 30.sp
)

/**
 * JerseyWatermark — Oversized jersey number for athlete card background.
 * "#18"
 * BarlowCondensed ExtraBold 120sp, alpha applied at call site (0.07f)
 */
val JerseyWatermark = TextStyle(
    fontFamily = BarlowCondensed,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 120.sp,
    lineHeight = 120.sp,
    letterSpacing = (-2).sp
)

// === INTERFACE / LABEL STYLES (DmSans) ===

/**
 * SectionLabel — Section headers displayed in uppercase with tracking.
 * "LIVE MATCHES", "TOP PERFORMERS", "RECENT MATCHES"
 * DmSans Bold 11sp, letterSpacing 1.5sp
 */
val SectionLabel = TextStyle(
    fontFamily = DmSans,
    fontWeight = FontWeight.Bold,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 1.5.sp
)

/**
 * MicroTag — Floating typographic status tags. Always rendered UPPERCASE.
 * "LIVE", "POTM", "CRR", "RRR", "TARGET", "INNINGS BREAK", "RESULT", "REVERTED"
 * DmSans SemiBold 9sp, letterSpacing 1.5sp
 */
val MicroTag = TextStyle(
    fontFamily = DmSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 9.sp,
    lineHeight = 13.sp,
    letterSpacing = 1.5.sp
)

/**
 * ArchetypeTag — Player archetype pill on athlete card.
 * "POWER HITTER", "DEATH BOWLER", "ALL-ROUNDER"
 * DmSans Bold 10sp, letterSpacing 1.5sp
 */
val ArchetypeTag = TextStyle(
    fontFamily = DmSans,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    lineHeight = 14.sp,
    letterSpacing = 1.5.sp
)

/**
 * PlayerNamePrimary — Player name on athlete card and prominent rows.
 * "Rohit Verma", "Jasprit Khan"
 * DmSans SemiBold 15sp
 */
val PlayerNamePrimary = TextStyle(
    fontFamily = DmSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    lineHeight = 21.sp
)

/**
 * PlayerNameSecondary — Player name in compact list rows (scorecards, leaderboards).
 * DmSans Medium 13sp
 */
val PlayerNameSecondary = TextStyle(
    fontFamily = DmSans,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 18.sp
)

/**
 * StatTableLabel — Left-column label in stat tables.
 * "Strike Rate", "Bowling Economy", "Highest Score"
 * DmSans Normal 12sp
 */
val StatTableLabel = TextStyle(
    fontFamily = DmSans,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 18.sp
)

/**
 * StatTableValue — Right-column value in stat tables.
 * "168.3", "3.50", "88*"
 * DmSans SemiBold 12sp
 */
val StatTableValue = TextStyle(
    fontFamily = DmSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 18.sp
)

/**
 * BodyCaption — Secondary metadata: dates, venues, overs format, context strings.
 * "18 Oct 2026 • 15 Overs", "vs Royal Tigers • League Stage"
 * DmSans Normal 11sp
 */
val BodyCaption = TextStyle(
    fontFamily = DmSans,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp
)

/**
 * CtaLabel — Primary call-to-action button text.
 * "Start New Match", "Sign In with Google", "Continue as Guest Scorer"
 * DmSans Bold 14sp
 */
val CtaLabel = TextStyle(
    fontFamily = DmSans,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 20.sp
)
