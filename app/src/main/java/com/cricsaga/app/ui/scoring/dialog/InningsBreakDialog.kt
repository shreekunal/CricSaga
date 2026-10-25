package com.cricsaga.app.ui.scoring.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.domain.model.InningsScoreState
import com.cricsaga.app.ui.theme.*
import java.util.Locale

@Composable
fun InningsBreakDialog(
    battingTeamName: String,
    bowlingTeamName: String,
    firstInningsState: InningsScoreState,
    totalOvers: Int,
    onStartSecondInnings: () -> Unit
) {
    val targetRuns = firstInningsState.totalRuns + 1
    val rrr = if (totalOvers > 0) targetRuns.toDouble() / totalOvers.toDouble() else 0.0

    AlertDialog(
        onDismissRequest = { /* Non-dismissible: must proceed */ },
        containerColor = SlateSurfaceDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "INNINGS BREAK",
                    color = BoundaryAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1st Innings Concluded",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1st Innings Summary Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElevatedSurfaceDark, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = battingTeamName.uppercase(),
                            color = MutedGrey,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${firstInningsState.totalRuns} / ${firstInningsState.totalWickets}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${firstInningsState.oversFormatted} ov • CRR: ${String.format(Locale.US, "%.2f", firstInningsState.currentRunRate)}",
                            fontSize = 12.sp,
                            color = MutedGrey
                        )
                    }
                }

                // Target Equation Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElevatedSurfaceDark, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "TARGET EQUATION",
                            color = CrimsonPrimaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$targetRuns Runs",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$bowlingTeamName needs $targetRuns runs from $totalOvers overs",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Required Run Rate: ${String.format(Locale.US, "%.2f", rrr)} per over",
                            fontSize = 11.sp,
                            color = BoundaryAmber,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onStartSecondInnings,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "SET UP 2ND INNINGS",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    )
}
