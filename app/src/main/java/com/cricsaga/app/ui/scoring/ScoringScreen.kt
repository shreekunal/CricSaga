package com.cricsaga.app.ui.scoring

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.domain.model.ExtraType
import com.cricsaga.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringScreen(
    state: ScoringUiState,
    onBallTapped: (Int) -> Unit,
    onExtraTapped: (ExtraType) -> Unit,
    onWicketTapped: () -> Unit,
    onUndoTapped: () -> Unit,
    onRotateStrikeTapped: () -> Unit,
    onViewScorecardClick: () -> Unit = {},
    isDark: Boolean = true,
    onThemeToggleClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.matchTitle,
                            style = PlayerNamePrimary,
                            color = TextPrimary
                        )
                        Text(
                            text = if (state.inningsNumber == 1) "1st Innings • ${state.totalOvers} Overs" else "2nd Innings • Target: ${state.scoreState.targetRuns ?: 0}",
                            style = BodyCaption,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onThemeToggleClick) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDark) BoundaryAmber else TextPrimary
                        )
                    }
                    TextButton(onClick = onViewScorecardClick) {
                        Text(
                            text = "SCORECARD",
                            style = MicroTag,
                            color = BoundaryAmber
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBackgroundDark
                )
            )
        },
        containerColor = CharcoalBackgroundDark
    ) { paddingValues ->
        // Spatial Cockpit Layout: Scrollable Match Telemetry (Top) + Fixed Pitch Keypad (Bottom)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Upper Section: Live Match Telemetry (Scrollable)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Chase Equation & 5-State Pressure Bar (2nd Innings Only)
                if (state.inningsNumber == 2) {
                    val targetRuns = state.scoreState.targetRuns ?: 0
                    val currentRuns = state.scoreState.totalRuns
                    val runsNeeded = state.scoreState.runsNeeded ?: 0
                    val ballsRemaining = state.scoreState.ballsRemaining ?: 0
                    val rrr = state.scoreState.requiredRunRate ?: 0.0

                    ChasePressureBarCard(
                        firstInningsTeam = state.innings1BattingTeamName,
                        firstInningsScoreText = "${state.innings1Score?.totalRuns ?: 0}/${state.innings1Score?.totalWickets ?: 0} (${state.innings1Score?.oversFormatted ?: "0.0"} ov)",
                        targetRuns = targetRuns,
                        currentRuns = currentRuns,
                        runsNeeded = runsNeeded,
                        ballsRemaining = ballsRemaining,
                        rrr = rrr,
                        isTargetAchieved = runsNeeded <= 0,
                        isAllOut = state.scoreState.totalWickets >= 10 || (ballsRemaining <= 0 && runsNeeded > 0)
                    )
                }

                // 2. Broadcast Scoreboard Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.battingTeamName.uppercase() + " INNINGS",
                            style = SectionLabel,
                            color = CrimsonPrimaryDark
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Large athletic numerals via Barlow Condensed
                        Text(
                            text = "${state.scoreState.totalRuns} / ${state.scoreState.totalWickets}",
                            style = ScoreBig,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Overs: ${state.scoreState.oversFormatted} / ${state.totalOvers}.0",
                                style = BodyCaption,
                                color = TextSecondary
                            )
                            Text(
                                text = "  •  ",
                                color = TextTertiary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "CRR: ${String.format("%.2f", state.scoreState.currentRunRate)}",
                                style = BodyCaption,
                                color = BoundaryAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // 3. Batsman & Bowler Live Tables
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("BATSMAN", style = MicroTag, color = TextTertiary)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("R", style = MicroTag, color = TextTertiary, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                                Text("B", style = MicroTag, color = TextTertiary, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                                Text("4s", style = MicroTag, color = TextTertiary, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                                Text("6s", style = MicroTag, color = TextTertiary, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                                Text("SR", style = MicroTag, color = TextTertiary, modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerDark)

                        // Striker Row
                        val striker = state.scoreState.batsmen[state.scoreState.strikerId]
                        BatsmanRow(
                            name = (striker?.playerName ?: state.striker?.fullName ?: "Striker"),
                            runs = striker?.runs ?: 0,
                            balls = striker?.ballsFaced ?: 0,
                            fours = striker?.fours ?: 0,
                            sixes = striker?.sixes ?: 0,
                            sr = striker?.strikeRate ?: 0.0,
                            isStriker = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Non-Striker Row
                        val nonStriker = state.scoreState.batsmen[state.scoreState.nonStrikerId]
                        BatsmanRow(
                            name = (nonStriker?.playerName ?: state.nonStriker?.fullName ?: "Non-Striker"),
                            runs = nonStriker?.runs ?: 0,
                            balls = nonStriker?.ballsFaced ?: 0,
                            fours = nonStriker?.fours ?: 0,
                            sixes = nonStriker?.sixes ?: 0,
                            sr = nonStriker?.strikeRate ?: 0.0,
                            isStriker = false
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerDark)

                        // Bowler Row
                        val bowler = state.scoreState.bowlers[state.scoreState.currentBowlerId]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BOWL: " + (bowler?.bowlerName ?: state.bowler?.fullName ?: "Bowler"),
                                style = PlayerNameSecondary,
                                color = TextPrimary
                            )
                            Text(
                                text = "${bowler?.oversFormatted ?: "0.0"}-${bowler?.maidens ?: 0}-${bowler?.runsConceded ?: 0}-${bowler?.wickets ?: 0}  (Econ: ${String.format("%.2f", bowler?.economyRate ?: 0.0)})",
                                style = BodyCaption,
                                color = BoundaryAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // 4. Current Over Badges Timeline
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "THIS OVER:",
                            style = MicroTag,
                            color = TextTertiary
                        )
                        if (state.scoreState.currentOverBalls.isEmpty()) {
                            Text(
                                text = "Awaiting first ball of the over...",
                                style = BodyCaption,
                                color = TextTertiary
                            )
                        } else {
                            state.scoreState.currentOverBalls.forEach { ball ->
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(
                                            when {
                                                ball.isWicket -> DismissalRed
                                                ball.runsOffBat == 4 -> BoundaryAmber
                                                ball.runsOffBat == 6 -> BoundaryGreen
                                                ball.extraType != ExtraType.NONE -> ExtraBlue
                                                else -> ElevatedSurfaceDark
                                            },
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ball.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ball.runsOffBat == 4 || ball.runsOffBat == 6 || ball.extraType != ExtraType.NONE) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Lower Section: Fixed Pitch Scoring Keypad (Never Scrolls Off Screen)
            Surface(
                color = SlateSurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp,
                        color = CardBorderDark,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    // Row 1: 0, 1, 2, 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadButton("0", ElevatedSurfaceDark, TextPrimary, Modifier.weight(1f)) { onBallTapped(0) }
                        KeypadButton("1", ElevatedSurfaceDark, TextPrimary, Modifier.weight(1f)) { onBallTapped(1) }
                        KeypadButton("2", ElevatedSurfaceDark, TextPrimary, Modifier.weight(1f)) { onBallTapped(2) }
                        KeypadButton("3", ElevatedSurfaceDark, TextPrimary, Modifier.weight(1f)) { onBallTapped(3) }
                    }

                    // Row 2: 4, 6, Wide, No Ball
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadButton("4", BoundaryAmber, Color.White, Modifier.weight(1f)) { onBallTapped(4) }
                        KeypadButton("6", BoundaryGreen, Color.White, Modifier.weight(1f)) { onBallTapped(6) }
                        KeypadButton("WD", ExtraBlue, Color.White, Modifier.weight(1f)) { onExtraTapped(ExtraType.WIDE) }
                        KeypadButton("NB", ExtraBlue, Color.White, Modifier.weight(1f)) { onExtraTapped(ExtraType.NO_BALL) }
                    }

                    // Row 3: Bye, Leg Bye, OUT, UNDO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadButton("BYE", ElevatedSurfaceDark, TextPrimary, Modifier.weight(1f)) { onExtraTapped(ExtraType.BYE) }
                        KeypadButton("LB", ElevatedSurfaceDark, TextPrimary, Modifier.weight(1f)) { onExtraTapped(ExtraType.LEG_BYE) }
                        KeypadButton("OUT / W", DismissalRed, Color.White, Modifier.weight(1f)) { onWicketTapped() }
                        KeypadButton("UNDO", ElevatedSurfaceDark, CrimsonPrimaryDark, Modifier.weight(1f)) { onUndoTapped() }
                    }

                    // Row 4: Action & Telemetry Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rotate Strike Action Pill
                        Surface(
                            onClick = onRotateStrikeTapped,
                            shape = RoundedCornerShape(8.dp),
                            color = ElevatedSurfaceDark,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ROTATE STRIKE",
                                    style = MicroTag,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Status Telemetry Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CharcoalBackgroundDark,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(BoundaryGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = state.lastActionMessage.take(24),
                                    style = BodyCaption,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5-State Animated Chase Pressure Bar
 *
 * State 1: BALANCED (RRR <= 7.0) -> Cool Muted Slate
 * State 2: PRESSURE (RRR 7.1..10.0) -> Cricket Amber
 * State 3: CRITICAL (RRR > 10.0) -> Dismissal Crimson
 * State 4: TARGET ACHIEVED -> Boundary Green
 * State 5: ALL OUT / LOSS -> Dismissal Red
 */
@Composable
fun ChasePressureBarCard(
    firstInningsTeam: String,
    firstInningsScoreText: String,
    targetRuns: Int,
    currentRuns: Int,
    runsNeeded: Int,
    ballsRemaining: Int,
    rrr: Double,
    isTargetAchieved: Boolean,
    isAllOut: Boolean
) {
    val progressTarget = if (targetRuns > 0) {
        (currentRuns.toFloat() / targetRuns.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = if (isTargetAchieved) 1f else progressTarget,
        animationSpec = tween(400),
        label = "ChaseBarProgress"
    )

    val (pressureTag, targetColor) = when {
        isTargetAchieved -> "TARGET ACHIEVED" to BoundaryGreen
        isAllOut -> "ALL OUT" to DismissalRed
        rrr > 10.0 -> "CRITICAL" to DismissalRed
        rrr > 7.0 -> "HIGH PRESSURE" to BoundaryAmber
        else -> "BALANCED" to TextSecondary
    }

    val animatedBarColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(400),
        label = "ChaseBarColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1st Inn: $firstInningsTeam $firstInningsScoreText",
                    style = BodyCaption,
                    color = TextTertiary
                )
                Surface(
                    color = animatedBarColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = pressureTag,
                        style = MicroTag,
                        color = animatedBarColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Animated progress line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ElevatedSurfaceDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(animatedBarColor)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTargetAchieved) "Target of $targetRuns runs achieved!" else "Need $runsNeeded runs from $ballsRemaining balls",
                    style = PlayerNameSecondary,
                    color = TextPrimary
                )
                Text(
                    text = "RRR: ${String.format("%.2f", rrr)}",
                    style = BodyCaption,
                    color = animatedBarColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BatsmanRow(
    name: String,
    runs: Int,
    balls: Int,
    fours: Int,
    sixes: Int,
    sr: Double,
    isStriker: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (isStriker) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(BoundaryAmber, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = name,
                style = if (isStriker) PlayerNameSecondary else BodyCaption,
                color = if (isStriker) TextPrimary else TextSecondary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "$runs",
                style = ScoreSmall,
                color = TextPrimary,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.End
            )
            Text(
                text = "$balls",
                style = BodyCaption,
                color = TextSecondary,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.End
            )
            Text(
                text = "$fours",
                style = BodyCaption,
                color = TextSecondary,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.End
            )
            Text(
                text = "$sixes",
                style = BodyCaption,
                color = TextSecondary,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.End
            )
            Text(
                text = String.format("%.1f", sr),
                style = BodyCaption,
                color = BoundaryAmber,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(42.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun KeypadButton(
    label: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color = CardBorderDark,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(0.75.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
