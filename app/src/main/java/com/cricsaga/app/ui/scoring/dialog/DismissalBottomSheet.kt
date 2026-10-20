package com.cricsaga.app.ui.scoring.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.DismissalDetails
import com.cricsaga.app.domain.model.DismissalType
import com.cricsaga.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissalBottomSheet(
    striker: UserEntity?,
    nonStriker: UserEntity?,
    fieldingSquad: List<UserEntity>,
    battingBench: List<UserEntity>,
    onDismissRequest: () -> Unit,
    onConfirmWicket: (DismissalDetails, incomingBatsmanId: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(DismissalType.CAUGHT) }
    var selectedDismissedPlayerId by remember { mutableStateOf(striker?.userId ?: "") }
    var selectedFielderId by remember { mutableStateOf(fieldingSquad.firstOrNull()?.userId ?: "") }
    var selectedIncomingId by remember { mutableStateOf(battingBench.firstOrNull()?.userId ?: "") }
    var runsCompletedBeforeOut by remember { mutableIntStateOf(0) }
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = SlateSurfaceDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "FALL OF WICKET",
                style = MaterialTheme.typography.titleMedium,
                color = DismissalRed,
                fontWeight = FontWeight.Bold
            )

            // 1. Dismissal Type Chips
            Text("Mode of Dismissal:", color = MutedGrey, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(
                    DismissalType.CAUGHT to "Caught",
                    DismissalType.BOWLED to "Bowled",
                    DismissalType.RUN_OUT to "Run Out",
                    DismissalType.STUMPED to "Stumped",
                    DismissalType.LBW to "LBW",
                    DismissalType.HIT_WICKET to "Hit Wicket"
                )) { (type, label) ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            hapticManager?.tapUiClick()
                            selectedType = type
                        },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (type == DismissalType.CAUGHT) BoundaryAmber else DismissalRed,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            // 2. Who is out? (Only relevant if Run Out)
            if (selectedType == DismissalType.RUN_OUT) {
                Text("Batsman Out:", color = MutedGrey, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = selectedDismissedPlayerId == striker?.userId,
                        onClick = {
                            hapticManager?.tapUiClick()
                            striker?.let { selectedDismissedPlayerId = it.userId }
                        },
                        label = { Text("Striker (${striker?.fullName ?: ""})") }
                    )
                    FilterChip(
                        selected = selectedDismissedPlayerId == nonStriker?.userId,
                        onClick = {
                            hapticManager?.tapUiClick()
                            nonStriker?.let { selectedDismissedPlayerId = it.userId }
                        },
                        label = { Text("Non-Striker (${nonStriker?.fullName ?: ""})") }
                    )
                }

                Text("Runs completed before Run Out:", color = MutedGrey, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0, 1, 2, 3).forEach { runs ->
                        FilterChip(
                            selected = runsCompletedBeforeOut == runs,
                            onClick = {
                                hapticManager?.tapUiClick()
                                runsCompletedBeforeOut = runs
                            },
                            label = { Text("$runs runs") }
                        )
                    }
                }
            }

            // 3. Fielder Selection (For Caught, Run Out, Stumped)
            if (selectedType in listOf(DismissalType.CAUGHT, DismissalType.RUN_OUT, DismissalType.STUMPED)) {
                val roleTitle = if (selectedType == DismissalType.STUMPED) "Wicketkeeper:" else "Fielder / Catcher:"
                Text(roleTitle, color = MutedGrey, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(fieldingSquad) { fielder ->
                        val isSelected = selectedFielderId == fielder.userId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                hapticManager?.tapUiClick()
                                selectedFielderId = fielder.userId
                            },
                            label = { Text(fielder.fullName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BoundaryAmber,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }

            // 4. Incoming Batsman Selection
            Text("Next Batsman In:", color = MutedGrey, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            if (battingBench.isEmpty()) {
                Text("All Out (No more batsmen available)", color = DismissalRed, fontSize = 12.sp)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(battingBench) { batter ->
                        val isSelected = selectedIncomingId == batter.userId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                hapticManager?.tapUiClick()
                                selectedIncomingId = batter.userId
                            },
                            label = { Text(batter.fullName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimaryDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Confirm Button
            Button(
                onClick = {
                    hapticManager?.tapWicket()
                    val dismissal = DismissalDetails(
                        dismissalType = selectedType,
                        dismissedPlayerId = if (selectedType == DismissalType.RUN_OUT) selectedDismissedPlayerId else (striker?.userId ?: ""),
                        primaryFielderId = if (selectedType in listOf(DismissalType.CAUGHT, DismissalType.RUN_OUT, DismissalType.STUMPED)) selectedFielderId else null,
                        runsCompletedBeforeDismissal = if (selectedType == DismissalType.RUN_OUT) runsCompletedBeforeOut else 0
                    )
                    onConfirmWicket(dismissal, selectedIncomingId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DismissalRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CONFIRM OUT", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
