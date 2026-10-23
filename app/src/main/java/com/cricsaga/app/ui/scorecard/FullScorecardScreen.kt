package com.cricsaga.app.ui.scorecard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.R
import com.cricsaga.app.domain.model.BatsmanScore
import com.cricsaga.app.domain.model.BowlerFigures
import com.cricsaga.app.domain.model.DeliveryRecord
import com.cricsaga.app.domain.model.InningsScoreState
import com.cricsaga.app.ui.analytics.ManhattanChart
import com.cricsaga.app.ui.analytics.WormGraph
import com.cricsaga.app.ui.utils.LocalHapticManager
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.PdfExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScorecardScreen(
    matchTitle: String,
    battingTeamName: String,
    bowlingTeamName: String,
    scoreState: InningsScoreState,
    innings1Score: InningsScoreState? = null,
    innings1BattingTeamName: String = "",
    currentInningsDeliveries: List<DeliveryRecord> = emptyList(),
    innings1Deliveries: List<DeliveryRecord> = emptyList(),
    onBackClick: () -> Unit,
    onSharePoster: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Scorecard", "Manhattan", "Worm Graph", "Fall of Wickets", "Match Poster")
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val hapticManager = LocalHapticManager.current

    Scaffold(
        containerColor = CharcoalBackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(matchTitle, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text("Complete Match Center", style = MaterialTheme.typography.bodyMedium, color = MutedGrey)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        onBackClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        PdfExportUtils.generateAndShareMatchPdf(
                            context = context,
                            matchTitle = matchTitle,
                            battingTeamName = battingTeamName,
                            bowlingTeamName = bowlingTeamName,
                            scoreState = scoreState,
                            firstInningsScore = innings1Score,
                            firstInningsTeamName = innings1BattingTeamName
                        )
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export A4 PDF", tint = TextPrimary)
                    }
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        onSharePoster()
                    }) {
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
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SlateSurfaceDark,
                contentColor = CrimsonPrimaryDark,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            hapticManager?.tapUiClick()
                            selectedTab = index
                        },
                        text = {
                            Text(
                                title,
                                color = if (selectedTab == index) TextPrimary else MutedGrey,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
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
                    0 -> ScorecardTab(battingTeamName, bowlingTeamName, scoreState, innings1Score)
                    1 -> ManhattanChart(
                        battingTeamName = battingTeamName,
                        deliveries = currentInningsDeliveries,
                        totalOvers = scoreState.totalOversAllotted
                    )
                    2 -> WormGraph(
                        teamAName = if (innings1BattingTeamName.isNotBlank()) innings1BattingTeamName else bowlingTeamName,
                        teamBName = battingTeamName,
                        innings1Deliveries = innings1Deliveries,
                        innings2Deliveries = currentInningsDeliveries,
                        targetRuns = scoreState.targetRuns,
                        totalOvers = scoreState.totalOversAllotted
                    )
                    3 -> FallOfWicketsTab(scoreState)
                    4 -> WhatsAppMatchPosterTab(matchTitle, battingTeamName, bowlingTeamName, scoreState, onSharePoster)
                }
            }
        }
    }
}

data class MatchBadgeAward(
    val title: String,
    val playerDetail: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
fun ScorecardTab(
    battingTeamName: String,
    bowlingTeamName: String,
    scoreState: InningsScoreState,
    firstInningsState: InningsScoreState? = null
) {
    val amberColor = BoundaryAmber
    val greenColor = ForestSixPrimary
    val crimsonColor = CrimsonPrimaryDark
    val blueColor = Color(0xFF38BDF8)
    val goldColor = AmberFourPrimary

    val matchBadges = remember(scoreState, firstInningsState, amberColor, greenColor, crimsonColor, blueColor, goldColor) {
        val badges = mutableListOf<MatchBadgeAward>()
        val allBatsmen = (firstInningsState?.batsmen?.values?.toList().orEmpty()) + scoreState.batsmen.values.toList()
        val allBowlers = (firstInningsState?.bowlers?.values?.toList().orEmpty()) + scoreState.bowlers.values.toList()

        // 1. Century Club (100+ runs)
        allBatsmen.filter { it.runs >= 100 }.forEach {
            badges.add(MatchBadgeAward("CENTURY CLUB", "${it.playerName} • ${it.runs} (${it.ballsFaced}b)", Icons.Default.SportsCricket, goldColor))
        }
        // 2. Half Century (50-99 runs)
        allBatsmen.filter { it.runs in 50..99 }.forEach {
            badges.add(MatchBadgeAward("HALF CENTURY", "${it.playerName} • ${it.runs} (${it.ballsFaced}b)", Icons.Default.Star, greenColor))
        }
        // 3. Super Sixer (3+ sixes)
        allBatsmen.filter { it.sixes >= 3 }.forEach {
            badges.add(MatchBadgeAward("SUPER SIXER", "${it.playerName} • ${it.sixes} Sixes", Icons.Default.RocketLaunch, greenColor))
        }
        // 4. Boundary King (5+ fours)
        allBatsmen.filter { it.fours >= 5 }.forEach {
            badges.add(MatchBadgeAward("BOUNDARY KING", "${it.playerName} • ${it.fours} Fours", Icons.Default.LocalFireDepartment, amberColor))
        }
        // 5. Strike Accelerator (SR >= 150.0, runs >= 20)
        allBatsmen.filter { it.runs >= 20 && it.strikeRate >= 150.0 }.forEach {
            badges.add(MatchBadgeAward("STRIKE ACCELERATOR", "${it.playerName} • SR ${String.format(java.util.Locale.US, "%.1f", it.strikeRate)}", Icons.Default.Bolt, amberColor))
        }
        // 6. 5-Wicket Haul (5+ wkts)
        allBowlers.filter { it.wickets >= 5 }.forEach {
            badges.add(MatchBadgeAward("5-WICKET HAUL", "${it.bowlerName} • ${it.wickets}/${it.runsConceded}", Icons.Default.GpsFixed, crimsonColor))
        }
        // 7. 3-Wicket Spell (3-4 wkts)
        allBowlers.filter { it.wickets in 3..4 }.forEach {
            badges.add(MatchBadgeAward("3-WICKET SPELL", "${it.bowlerName} • ${it.wickets}/${it.runsConceded}", Icons.Default.SportsBaseball, blueColor))
        }
        // 8. Economy Maestro (legalBalls >= 12, econ <= 5.0)
        allBowlers.filter { it.legalBalls >= 12 && it.economyRate <= 5.0 }.forEach {
            badges.add(MatchBadgeAward("ECONOMY MAESTRO", "${it.bowlerName} • Econ ${String.format(java.util.Locale.US, "%.1f", it.economyRate)}", Icons.Default.Shield, greenColor))
        }
        badges
    }

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
                color = TextPrimary
            )
            Text(
                text = "Overs: ${scoreState.oversFormatted} / ${scoreState.totalOversAllotted}.0  •  CRR: ${String.format("%.2f", scoreState.currentRunRate)}",
                color = MutedGrey,
                fontSize = 13.sp
            )
        }
    }

    // Match Badges & Honours Shelf
    if (matchBadges.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BoundaryAmber.copy(alpha = 0.4f)))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MATCH BADGES & HONOURS", color = BoundaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Surface(
                        color = BoundaryAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${matchBadges.size} Badges",
                            color = BoundaryAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                matchBadges.forEach { badge ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(badge.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(badge.icon, contentDescription = null, tint = badge.color, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(badge.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(badge.playerDetail, color = TextSecondary, fontSize = 10.sp)
                        }
                        Surface(
                            color = badge.color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, badge.color.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "AWARDED",
                                color = badge.color,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerDark)

            scoreState.batsmen.values.forEach { b ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(b.playerName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                text = if (b.isOut) "c & b (${b.dismissal?.dismissalType?.name ?: "Out"})" else "not out *",
                                color = if (b.isOut) DismissalRed else BoundaryGreen,
                                fontSize = 10.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${b.runs}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerDark)

            scoreState.bowlers.values.forEach { bow ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(bow.bowlerName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(bow.oversFormatted, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
                        Text("${bow.maidens}", color = MutedGrey, fontSize = 13.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
                        Text("${bow.runsConceded}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
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
                            color = TextPrimary,
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
                        HorizontalDivider(color = DividerDark)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MATCH SUMMARY",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = matchTitle,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Score Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ElevatedSurfaceDark, RoundedCornerShape(8.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$battingTeamName: ${scoreState.totalRuns}/${scoreState.totalWickets}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${scoreState.oversFormatted} ov • CRR ${String.format("%.2f", scoreState.currentRunRate)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top Batter & Bowler summary
            val topBatter = scoreState.batsmen.values.maxByOrNull { it.runs }
            val topBowler = scoreState.bowlers.values.maxByOrNull { it.wickets }

            if (topBatter != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Top Batter:", color = TextTertiary, fontSize = 12.sp)
                    Text("${topBatter.playerName} ${topBatter.runs}* (${topBatter.ballsFaced})", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
            if (topBowler != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Top Bowler:", color = TextTertiary, fontSize = 12.sp)
                    Text("${topBowler.bowlerName} ${topBowler.wickets}/${topBowler.runsConceded}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = ElevatedSurfaceDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Summary", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            }
        }
    }
}
