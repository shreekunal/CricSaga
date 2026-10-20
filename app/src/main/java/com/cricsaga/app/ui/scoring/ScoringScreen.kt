package com.cricsaga.app.ui.scoring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onViewScorecardClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.matchTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "1st Innings • 20 Overs • Local-First Room DB",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedGrey
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onViewScorecardClick) {
                        Text(
                            text = "SCORECARD",
                            color = BoundaryAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBackgroundDark
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Live Match Scoreboard Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.battingTeamName.uppercase() + " INNINGS",
                        color = CrimsonPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.scoreState.totalRuns} / ${state.scoreState.totalWickets}",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Overs: ${state.scoreState.oversFormatted} / 20.0  •  CRR: ${String.format("%.2f", state.scoreState.currentRunRate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedGrey
                    )
                }
            }

            // 2. Batsman & Bowler Live Tables
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("BATSMAN", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("R", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                            Text("B", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                            Text("4s", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                            Text("6s", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                            Text("SR", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF2E2E2E))

                    // Striker Row
                    val striker = state.scoreState.batsmen[state.scoreState.strikerId]
                    BatsmanRow(
                        name = "* " + (striker?.playerName ?: state.striker?.fullName ?: "Striker"),
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
                        name = "  " + (nonStriker?.playerName ?: state.nonStriker?.fullName ?: "Non-Striker"),
                        runs = nonStriker?.runs ?: 0,
                        balls = nonStriker?.ballsFaced ?: 0,
                        fours = nonStriker?.fours ?: 0,
                        sixes = nonStriker?.sixes ?: 0,
                        sr = nonStriker?.strikeRate ?: 0.0,
                        isStriker = false
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF2E2E2E))

                    // Bowler Row
                    val bowler = state.scoreState.bowlers[state.scoreState.currentBowlerId]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bowler: " + (bowler?.bowlerName ?: state.bowler?.fullName ?: "Bowler"),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${bowler?.oversFormatted ?: "0.0"} - ${bowler?.maidens ?: 0} - ${bowler?.runsConceded ?: 0} - ${bowler?.wickets ?: 0} (Econ: ${String.format("%.1f", bowler?.economyRate ?: 0.0)})",
                            color = BoundaryAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 3. Current Over Badges Timeline
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "THIS OVER:",
                        color = MutedGrey,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.scoreState.currentOverBalls.isEmpty()) {
                        Text(
                            text = "Awaiting first ball...",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else {
                        state.scoreState.currentOverBalls.forEach { ball ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
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

            // 4. Interactive Keypad (Exact CricHeroes Muscle Memory)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: 0, 1, 2, 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadButton("0", ElevatedSurfaceDark, Color.White, Modifier.weight(1f)) { onBallTapped(0) }
                        KeypadButton("1", ElevatedSurfaceDark, Color.White, Modifier.weight(1f)) { onBallTapped(1) }
                        KeypadButton("2", ElevatedSurfaceDark, Color.White, Modifier.weight(1f)) { onBallTapped(2) }
                        KeypadButton("3", ElevatedSurfaceDark, Color.White, Modifier.weight(1f)) { onBallTapped(3) }
                    }

                    // Row 2: 4, 6, Wide, No Ball
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadButton("4", BoundaryAmber, Color.Black, Modifier.weight(1f)) { onBallTapped(4) }
                        KeypadButton("6", BoundaryGreen, Color.Black, Modifier.weight(1f)) { onBallTapped(6) }
                        KeypadButton("WIDE", ExtraBlue, Color.Black, Modifier.weight(1f)) { onExtraTapped(ExtraType.WIDE) }
                        KeypadButton("NO BALL", ExtraBlue, Color.Black, Modifier.weight(1f)) { onExtraTapped(ExtraType.NO_BALL) }
                    }

                    // Row 3: Bye, Leg Bye, OUT, UNDO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadButton("BYE", ElevatedSurfaceDark, Color.White, Modifier.weight(1f)) { onExtraTapped(ExtraType.BYE) }
                        KeypadButton("LEG BYE", ElevatedSurfaceDark, Color.White, Modifier.weight(1f)) { onExtraTapped(ExtraType.LEG_BYE) }
                        KeypadButton("OUT / W", DismissalRed, Color.White, Modifier.weight(1f)) { onWicketTapped() }
                        KeypadButton("UNDO", ElevatedSurfaceDark, BoundaryAmber, Modifier.weight(1f)) { onUndoTapped() }
                    }

                    // Row 4: Quick Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadButton("🔄 Rotate Strike", Color(0xFF2A2A2A), Color.White, Modifier.weight(1f)) {
                            onRotateStrikeTapped()
                        }
                    }
                }
            }

            // 5. Database Status & Log Indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF191919)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.lastActionMessage,
                        color = MutedGrey,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
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
        Text(
            text = name,
            color = if (isStriker) Color.White else MutedGrey,
            fontWeight = if (isStriker) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("$runs", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
            Text("$balls", color = MutedGrey, fontSize = 13.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
            Text("$fours", color = MutedGrey, fontSize = 13.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
            Text("$sixes", color = MutedGrey, fontSize = 13.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
            Text(String.format("%.1f", sr), color = BoundaryAmber, fontSize = 12.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
        }
    }
}

@Composable
fun KeypadButton(
    label: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
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
