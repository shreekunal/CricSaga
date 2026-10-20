package com.cricsaga.app.ui.scorecard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.R
import com.cricsaga.app.domain.model.BatsmanScore
import com.cricsaga.app.domain.model.BowlerFigures
import com.cricsaga.app.domain.model.InningsScoreState
import com.cricsaga.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScorecardScreen(
    matchTitle: String,
    battingTeamName: String,
    bowlingTeamName: String,
    scoreState: InningsScoreState,
    onBackClick: () -> Unit,
    onSharePoster: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Scorecard", "Fall of Wickets", "Match Poster")
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(matchTitle, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text("Complete Match Center", style = MaterialTheme.typography.bodyMedium, color = MutedGrey)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onSharePoster) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = BoundaryAmber)
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
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SlateSurfaceDark,
                contentColor = CrimsonPrimaryDark
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedTab == index) Color.White else MutedGrey,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> ScorecardTab(battingTeamName, bowlingTeamName, scoreState)
                    1 -> FallOfWicketsTab(scoreState)
                    2 -> WhatsAppMatchPosterTab(matchTitle, battingTeamName, bowlingTeamName, scoreState, onSharePoster)
                }
            }
        }
    }
}

@Composable
fun ScorecardTab(
    battingTeamName: String,
    bowlingTeamName: String,
    scoreState: InningsScoreState
) {
    // Total Banner
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = battingTeamName.uppercase() + " INNINGS",
                color = CrimsonPrimaryDark,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${scoreState.totalRuns} / ${scoreState.totalWickets}",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Overs: ${scoreState.oversFormatted} / ${scoreState.totalOversAllotted}.0  •  CRR: ${String.format("%.2f", scoreState.currentRunRate)}",
                color = MutedGrey,
                fontSize = 13.sp
            )
        }
    }

    // Batting Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("BATTING", color = BoundaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("BATSMAN", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("R", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                    Text("B", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                    Text("4s", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                    Text("6s", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                    Text("SR", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF2E2E2E))

            scoreState.batsmen.values.forEach { b ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(b.playerName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                text = if (b.isOut) "c & b (${b.dismissal?.dismissalType?.name ?: "Out"})" else "not out *",
                                color = if (b.isOut) DismissalRed else BoundaryGreen,
                                fontSize = 10.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${b.runs}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                            Text("${b.ballsFaced}", color = MutedGrey, fontSize = 13.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                            Text("${b.fours}", color = MutedGrey, fontSize = 13.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                            Text("${b.sixes}", color = MutedGrey, fontSize = 13.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                            Text(String.format("%.1f", b.strikeRate), color = BoundaryAmber, fontSize = 12.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
                        }
                    }
                }
            }
        }
    }

    // Bowling Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("BOWLING", color = BoundaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("BOWLER", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("O", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                    Text("M", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                    Text("R", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                    Text("W", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                    Text("ECON", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF2E2E2E))

            scoreState.bowlers.values.forEach { bow ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(bow.bowlerName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(bow.oversFormatted, color = Color.White, fontSize = 13.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                        Text("${bow.maidens}", color = MutedGrey, fontSize = 13.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                        Text("${bow.runsConceded}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                        Text("${bow.wickets}", color = DismissalRed, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                        Text(String.format("%.1f", bow.economyRate), color = BoundaryAmber, fontSize = 12.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}

@Composable
fun FallOfWicketsTab(scoreState: InningsScoreState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("FALL OF WICKETS", color = DismissalRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))

            val dismissedBatsmen = scoreState.batsmen.values.filter { it.isOut }
            if (dismissedBatsmen.isEmpty()) {
                Text("No wickets have fallen yet.", color = MutedGrey, fontSize = 13.sp)
            } else {
                dismissedBatsmen.forEachIndexed { index, b ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index + 1}-${b.runs} (${b.playerName})",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${b.dismissal?.dismissalType?.name ?: "OUT"}",
                            color = BoundaryAmber,
                            fontSize = 12.sp
                        )
                    }
                    if (index < dismissedBatsmen.size - 1) {
                        HorizontalDivider(color = Color(0xFF282828))
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppMatchPosterTab(
    matchTitle: String,
    battingTeamName: String,
    bowlingTeamName: String,
    scoreState: InningsScoreState,
    onShare: () -> Unit
) {
    // Beautiful WhatsApp Status Card (High Contrast)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CrimsonPrimaryDark))
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.cricsaga_logo),
                contentDescription = "CricSaga Logo",
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "CRICSAGA",
                color = CrimsonPrimaryDark,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = 2.sp
            )
            Text(
                text = "HAR RUN KI KAHANI",
                color = MutedGrey,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = matchTitle.uppercase(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Score Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF252525), RoundedCornerShape(10.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$battingTeamName: ${scoreState.totalRuns} / ${scoreState.totalWickets}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "(${scoreState.oversFormatted} ov • CRR: ${String.format("%.2f", scoreState.currentRunRate)})",
                        fontSize = 13.sp,
                        color = BoundaryAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top Batter & Bowler summary
            val topBatter = scoreState.batsmen.values.maxByOrNull { it.runs }
            val topBowler = scoreState.bowlers.values.maxByOrNull { it.wickets }

            if (topBatter != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Top Batter:", color = MutedGrey, fontSize = 12.sp)
                    Text("${topBatter.playerName} ${topBatter.runs}* (${topBatter.ballsFaced})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            if (topBowler != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Top Bowler:", color = MutedGrey, fontSize = 12.sp)
                    Text("${topBowler.bowlerName} ${topBowler.wickets}/${topBowler.runsConceded}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = BoundaryGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SHARE TO WHATSAPP STATUS", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
