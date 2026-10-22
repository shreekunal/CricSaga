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
    maxOversPerBowler: Int = 4,
    bowlerOversMap: Map<String, Int> = emptyMap(),
    onBowlerSelected: (UserEntity) -> Unit
) {
    val eligibleBowlers = bowlingSquad.filter {
        it.userId != lastBowlerId && (bowlerOversMap[it.userId] ?: 0) < maxOversPerBowler
    }
    var selectedBowler by remember { mutableStateOf(eligibleBowlers.firstOrNull() ?: bowlingSquad.firstOrNull { it.userId != lastBowlerId }) }
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
                    text = "A bowler cannot bowl consecutive overs. Max quota: $maxOversPerBowler overs per bowler.",
                    color = MutedGrey,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(bowlingSquad) { bowler ->
                        val oversBowled = bowlerOversMap[bowler.userId] ?: 0
                        val isQuotaFull = oversBowled >= maxOversPerBowler
                        val isPreviousBowler = bowler.userId == lastBowlerId
                        val isEnabled = !isPreviousBowler && !isQuotaFull
                        val isSelected = selectedBowler?.userId == bowler.userId

                        val statusSuffix = when {
                            isPreviousBowler -> " (Just Bowled)"
                            isQuotaFull -> " (Quota Full: $oversBowled/$maxOversPerBowler ov)"
                            oversBowled > 0 -> " ($oversBowled/$maxOversPerBowler ov)"
                            else -> " (0/$maxOversPerBowler ov)"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FilterChip(
                                selected = isSelected,
                                enabled = isEnabled,
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    selectedBowler = bowler
                                },
                                label = {
                                    Text(
                                        text = bowler.fullName + statusSuffix,
                                        color = if (!isEnabled) Color.Gray else TextPrimary
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
