package com.cricsaga.app.ui.scoring.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.InningsScoreState
import com.cricsaga.app.domain.model.MatchResult
import com.cricsaga.app.ui.theme.*

@Composable
fun MatchResultDialog(
    matchResult: MatchResult,
    innings1TeamName: String,
    innings1Score: InningsScoreState?,
    innings2TeamName: String,
    innings2Score: InningsScoreState,
    candidates: List<UserEntity>,
    selectedPotm: UserEntity?,
    onPotmSelected: (UserEntity) -> Unit,
    onViewScorecard: () -> Unit,
    onShareToFeed: () -> Unit = {},
    onShareToWhatsApp: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SlateSurfaceDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = BoundaryAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MATCH CONCLUDED",
                        style = SectionLabel,
                        color = BoundaryAmber
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = matchResult.resultSummary,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Scores Comparison Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElevatedSurfaceDark, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // 1st Innings
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = innings1TeamName,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${innings1Score?.totalRuns ?: 0}/${innings1Score?.totalWickets ?: 0} (${innings1Score?.oversFormatted ?: "0.0"} ov)",
                                color = MutedGrey,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        HorizontalDivider(color = DividerDark)

                        // 2nd Innings
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = innings2TeamName,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${innings2Score.totalRuns}/${innings2Score.totalWickets} (${innings2Score.oversFormatted} ov)",
                                color = MutedGrey,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Player of the Match Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = BoundaryAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PLAYER OF THE MATCH",
                            style = MicroTag,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Selected: ${selectedPotm?.fullName ?: "Choose performer"}",
                        color = BoundaryAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current
                    // Candidate chips
                    candidates.take(6).forEach { player ->
                        val isSelected = selectedPotm?.userId == player.userId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                hapticManager?.tapUiClick()
                                onPotmSelected(player)
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onViewScorecard,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "VIEW FULL SCORECARD",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = onShareToFeed,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberFourPrimary)
                ) {
                    Text(
                        text = "POST HIGHLIGHT TO FEED",
                        fontWeight = FontWeight.Bold,
                        color = AmberFourPrimary,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = onShareToWhatsApp,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = "SHARE TO WHATSAPP",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DONE",
                        color = MutedGrey,
                        fontSize = 12.sp
                    )
                }
            }
        }
    )
}
