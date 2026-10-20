package com.cricsaga.app.ui.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.TeamEntity
import com.cricsaga.app.domain.model.BallType
import com.cricsaga.app.domain.model.TossDecision
import com.cricsaga.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    teams: List<TeamEntity>,
    onBackClick: () -> Unit,
    onProceedToOpeners: (
        title: String,
        teamAId: String,
        teamBId: String,
        overs: Int,
        ballType: BallType,
        tossWinnerId: String,
        tossDecision: TossDecision
    ) -> Unit
) {
    var teamA by remember { mutableStateOf(teams.getOrNull(0)?.teamId ?: "") }
    var teamB by remember { mutableStateOf(teams.getOrNull(1)?.teamId ?: "") }
    var totalOvers by remember { mutableIntStateOf(20) }
    var selectedBallType by remember { mutableStateOf(BallType.TENNIS_LIGHT) }
    var tossWinnerId by remember { mutableStateOf(teams.getOrNull(0)?.teamId ?: "") }
    var tossDecision by remember { mutableStateOf(TossDecision.BAT) }
    var venue by remember { mutableStateOf("Shivaji Park Ground #2, Dadar") }
    var maxOversPerBowler by remember { mutableIntStateOf(4) }
    var isBoxCricket by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    Scaffold(
        containerColor = CharcoalBackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Match Setup", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        onBackClick()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Team A & Team B Selectors
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SELECT COMPETING TEAMS", color = BoundaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Text("Team A (Home):", color = MutedGrey, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(teams) { t ->
                            FilterChip(
                                selected = teamA == t.teamId,
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    teamA = t.teamId
                                    if (tossWinnerId.isEmpty()) tossWinnerId = t.teamId
                                },
                                label = { Text(t.teamName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonPrimaryDark,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Team B (Away):", color = MutedGrey, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(teams.filter { it.teamId != teamA }) { t ->
                            FilterChip(
                                selected = teamB == t.teamId,
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    teamB = t.teamId
                                },
                                label = { Text(t.teamName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BoundaryGreen,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }

            // Overs Configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("MATCH OVERS", color = BoundaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(5, 8, 10, 12, 15, 20)) { ov ->
                            FilterChip(
                                selected = totalOvers == ov,
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    totalOvers = ov
                                },
                                label = { Text("$ov Overs") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("BALL TYPE", color = BoundaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedBallType == BallType.TENNIS_LIGHT,
                            onClick = {
                                hapticManager?.tapUiClick()
                                selectedBallType = BallType.TENNIS_LIGHT
                            },
                            label = { Text("Tennis Ball") }
                        )
                        FilterChip(
                            selected = selectedBallType == BallType.LEATHER,
                            onClick = {
                                hapticManager?.tapUiClick()
                                selectedBallType = BallType.LEATHER
                            },
                            label = { Text("Leather Ball") }
                        )
                    }
                }
            }

            // Venue & Custom Grassroots Rules
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("VENUE & CUSTOM RULES", color = BoundaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    OutlinedTextField(
                        value = venue,
                        onValueChange = { venue = it },
                        label = { Text("Ground / Turf Name") },
                        placeholder = { Text("e.g. Shivaji Park Ground #2, Dadar") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CrimsonPrimaryDark,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Max Overs per Bowler:", color = MutedGrey, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 3, 4, 5).forEach { quota ->
                            FilterChip(
                                selected = maxOversPerBowler == quota,
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    maxOversPerBowler = quota
                                },
                                label = { Text("$quota ov") }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Box Cricket Mode (2-run Wide)", color = TextPrimary, fontSize = 12.sp)
                        Switch(
                            checked = isBoxCricket,
                            onCheckedChange = {
                                hapticManager?.tapUiClick()
                                isBoxCricket = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonPrimaryDark)
                        )
                    }
                }
            }

            // Toss Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("TOSS", color = BoundaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    val teamAName = teams.find { it.teamId == teamA }?.teamName ?: "Team A"
                    val teamBName = teams.find { it.teamId == teamB }?.teamName ?: "Team B"

                    Text("Who won the toss?", color = MutedGrey, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = tossWinnerId == teamA,
                            onClick = {
                                hapticManager?.tapUiClick()
                                tossWinnerId = teamA
                            },
                            label = { Text(teamAName) }
                        )
                        FilterChip(
                            selected = tossWinnerId == teamB,
                            onClick = {
                                hapticManager?.tapUiClick()
                                tossWinnerId = teamB
                            },
                            label = { Text(teamBName) }
                        )
                    }

                    Text("Elected to:", color = MutedGrey, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = tossDecision == TossDecision.BAT,
                            onClick = {
                                hapticManager?.tapUiClick()
                                tossDecision = TossDecision.BAT
                            },
                            label = { Text("BAT FIRST") }
                        )
                        FilterChip(
                            selected = tossDecision == TossDecision.BOWL,
                            onClick = {
                                hapticManager?.tapUiClick()
                                tossDecision = TossDecision.BOWL
                            },
                            label = { Text("BOWL FIRST") }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    hapticManager?.tapUiClick()
                    val teamAName = teams.find { it.teamId == teamA }?.teamName ?: "Team A"
                    val teamBName = teams.find { it.teamId == teamB }?.teamName ?: "Team B"
                    val title = "$teamAName vs $teamBName"
                    onProceedToOpeners(title, teamA, teamB, totalOvers, selectedBallType, tossWinnerId, tossDecision)
                },
                enabled = teamA.isNotEmpty() && teamB.isNotEmpty() && teamA != teamB,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("SELECT OPENERS & START", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
