package com.cricsaga.app.ui.scoring.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.ui.theme.*

@Composable
fun SecondInningsOpenersDialog(
    battingTeamName: String,
    bowlingTeamName: String,
    targetRuns: Int,
    totalOvers: Int,
    battingSquad: List<UserEntity>,
    bowlingSquad: List<UserEntity>,
    onConfirm: (striker: UserEntity, nonStriker: UserEntity, bowler: UserEntity) -> Unit
) {
    var selectedStriker by remember { mutableStateOf(battingSquad.getOrNull(0)) }
    var selectedNonStriker by remember { mutableStateOf(battingSquad.getOrNull(1)) }
    var selectedBowler by remember { mutableStateOf(bowlingSquad.firstOrNull()) }

    val scrollState = rememberScrollState()
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    AlertDialog(
        onDismissRequest = { /* Must select */ },
        containerColor = SlateSurfaceDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column {
                Text(
                    text = "2ND INNINGS SETUP",
                    color = CrimsonPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$battingTeamName vs $bowlingTeamName",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Target: $targetRuns runs from $totalOvers overs",
                    fontSize = 12.sp,
                    color = BoundaryAmber
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Opening Striker
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "1. Opening Striker ($battingTeamName)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    battingSquad.forEach { player ->
                        val isSelected = selectedStriker?.userId == player.userId
                        val isNonStriker = selectedNonStriker?.userId == player.userId
                        FilterChip(
                            selected = isSelected,
                            enabled = !isNonStriker,
                            onClick = {
                                hapticManager?.tapUiClick()
                                selectedStriker = player
                            },
                            label = { Text(player.fullName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimaryDark,
                                selectedLabelColor = Color.White,
                                containerColor = ElevatedSurfaceDark,
                                labelColor = MutedGrey
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider(color = DividerDark)

                // Section 2: Non-Striker
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "2. Opening Non-Striker ($battingTeamName)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    battingSquad.forEach { player ->
                        val isSelected = selectedNonStriker?.userId == player.userId
                        val isStriker = selectedStriker?.userId == player.userId
                        FilterChip(
                            selected = isSelected,
                            enabled = !isStriker,
                            onClick = {
                                hapticManager?.tapUiClick()
                                selectedNonStriker = player
                            },
                            label = { Text(player.fullName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimaryDark,
                                selectedLabelColor = Color.White,
                                containerColor = ElevatedSurfaceDark,
                                labelColor = MutedGrey
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider(color = DividerDark)

                // Section 3: Opening Bowler
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "3. Opening Bowler ($bowlingTeamName)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    bowlingSquad.forEach { player ->
                        val isSelected = selectedBowler?.userId == player.userId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                hapticManager?.tapUiClick()
                                selectedBowler = player
                            },
                            label = { Text(player.fullName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BoundaryAmber,
                                selectedLabelColor = Color.Black,
                                containerColor = ElevatedSurfaceDark,
                                labelColor = MutedGrey
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            val canStart = selectedStriker != null && selectedNonStriker != null && selectedBowler != null && selectedStriker?.userId != selectedNonStriker?.userId
            Button(
                onClick = {
                    if (canStart) {
                        hapticManager?.tapUiClick()
                        onConfirm(selectedStriker!!, selectedNonStriker!!, selectedBowler!!)
                    }
                },
                enabled = canStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "START 2ND INNINGS",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    )
}
