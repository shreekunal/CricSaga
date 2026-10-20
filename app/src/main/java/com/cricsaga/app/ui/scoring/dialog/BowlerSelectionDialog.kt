package com.cricsaga.app.ui.scoring.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun BowlerSelectionDialog(
    lastBowlerId: String,
    bowlingSquad: List<UserEntity>,
    onBowlerSelected: (UserEntity) -> Unit
) {
    val eligibleBowlers = bowlingSquad.filter { it.userId != lastBowlerId }
    var selectedBowler by remember { mutableStateOf(eligibleBowlers.firstOrNull()) }
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    AlertDialog(
        onDismissRequest = { /* Modal must be chosen */ },
        containerColor = SlateSurfaceDark,
        title = {
            Column {
                Text(
                    text = "END OF OVER",
                    color = BoundaryAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Select Next Bowler",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Note: A bowler cannot bowl two consecutive overs.",
                    color = MutedGrey,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(bowlingSquad) { bowler ->
                        val isPreviousBowler = bowler.userId == lastBowlerId
                        val isSelected = selectedBowler?.userId == bowler.userId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FilterChip(
                                selected = isSelected,
                                enabled = !isPreviousBowler,
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    selectedBowler = bowler
                                },
                                label = {
                                    Text(
                                        text = bowler.fullName + if (isPreviousBowler) " (Just Bowled)" else "",
                                        color = if (isPreviousBowler) Color.Gray else TextPrimary
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BoundaryAmber,
                                    selectedLabelColor = Color.Black
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedBowler?.let {
                        hapticManager?.tapUiClick()
                        onBowlerSelected(it)
                    }
                },
                enabled = selectedBowler != null,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("START NEXT OVER", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    )
}
