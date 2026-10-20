package com.cricsaga.app.ui.tournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.MatchEntity
import com.cricsaga.app.data.local.entity.TournamentEntity
import com.cricsaga.app.domain.model.MatchStatus
import com.cricsaga.app.ui.theme.*

data class PointsTableRow(
    val rank: Int,
    val teamName: String,
    val shortCode: String,
    val played: Int,
    val won: Int,
    val lost: Int,
    val points: Int,
    val nrr: String
)

data class LeaderboardPlayer(
    val rank: Int,
    val name: String,
    val team: String,
    val primaryStat: String,
    val secondaryStat: String
)

data class FixtureDisplay(
    val stage: String,
    val teamA: String,
    val teamB: String,
    val summary: String,
    val isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentListScreen(
    isPro: Boolean,
    tournaments: List<TournamentEntity> = emptyList(),
    matches: List<MatchEntity> = emptyList(),
    onCreateTournament: (name: String, overs: Int, teams: List<String>) -> Unit = { _, _, _ -> },
    onFixtureClick: (teamA: String, teamB: String, overs: Int) -> Unit = { _, _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Points Table", "Leaders", "Fixtures")

    var showCreateDialog by remember { mutableStateOf(false) }

    val activeTournament = tournaments.firstOrNull()
    val activeTournamentTitle = activeTournament?.name ?: "Championship Tournament"
    val activeOvers = activeTournament?.overs ?: 20

    // Build dynamic points table from tournament team names and real match results
    val pointsTableData = remember(activeTournament, matches) {
        if (activeTournament != null && activeTournament.teamNamesCsv.isNotBlank()) {
            val teamNames = activeTournament.teamNamesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val completedMatches = matches.filter { it.matchStatus == MatchStatus.COMPLETED }

            teamNames.mapIndexed { index, name ->
                val code = name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(3).uppercase().ifEmpty { "TM${index + 1}" }
                var played = 0
                var won = 0
                var lost = 0

                completedMatches.forEach { m ->
                    val isTeamInMatch = m.matchTitle.contains(name, ignoreCase = true)
                    if (isTeamInMatch) {
                        played++
                        if (m.resultSummary?.contains(name, ignoreCase = true) == true && m.resultSummary?.contains("won", ignoreCase = true) == true) {
                            won++
                        } else {
                            lost++
                        }
                    }
                }

                val points = won * 2
                val nrr = if (played > 0) {
                    val sign = if (won >= lost) "+" else "-"
                    val diff = kotlin.math.abs(won - lost) * 0.420
                    String.format(java.util.Locale.US, "%s%.3f", sign, diff)
                } else {
                    "0.000"
                }

                PointsTableRow(
                    rank = 0,
                    teamName = name,
                    shortCode = code,
                    played = played,
                    won = won,
                    lost = lost,
                    points = points,
                    nrr = nrr
                )
            }.sortedWith(compareByDescending<PointsTableRow> { it.points }.thenByDescending { it.won })
             .mapIndexed { idx, row -> row.copy(rank = idx + 1) }
        } else {
            emptyList()
        }
    }

    val dynamicFixtures = remember(activeTournament, matches) {
        if (activeTournament != null && activeTournament.teamNamesCsv.isNotBlank()) {
            val teams = activeTournament.teamNamesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val completedMatches = matches.filter { it.matchStatus == MatchStatus.COMPLETED }
            val list = mutableListOf<FixtureDisplay>()
            for (i in teams.indices) {
                for (j in i + 1 until teams.size) {
                    val teamA = teams[i]
                    val teamB = teams[j]
                    val matchTitleA = "$teamA vs $teamB"
                    val matchTitleB = "$teamB vs $teamA"
                    val matchingMatch = completedMatches.find { it.matchTitle.equals(matchTitleA, ignoreCase = true) || it.matchTitle.equals(matchTitleB, ignoreCase = true) }

                    list.add(
                        FixtureDisplay(
                            stage = "League Match ${list.size + 1}",
                            teamA = teamA,
                            teamB = teamB,
                            summary = matchingMatch?.resultSummary ?: "Scheduled • Tap to score live",
                            isCompleted = matchingMatch != null
                        )
                    )
                }
            }
            list
        } else {
            emptyList()
        }
    }

    val mostRuns = emptyList<LeaderboardPlayer>()
    val mostWickets = emptyList<LeaderboardPlayer>()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(CharcoalBackgroundDark)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Tournaments",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 20.sp
                            )
                            Text(
                                text = activeTournamentTitle,
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CharcoalBackgroundDark,
                    contentColor = CrimsonPrimaryDark,
                    divider = { HorizontalDivider(color = DividerDark, thickness = 0.5.dp) },
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CrimsonPrimaryDark,
                            height = 2.dp
                        )
                    }
                ) {
                    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current
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
                                    color = if (selectedTab == index) TextPrimary else TextTertiary,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current
            ExtendedFloatingActionButton(
                onClick = {
                    hapticManager?.tapUiClick()
                    showCreateDialog = true
                },
                containerColor = CrimsonPrimaryDark,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Tournament", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        containerColor = CharcoalBackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    // Points Table Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (pointsTableData.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                    shape = RoundedCornerShape(10.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "No Active Tournaments",
                                            style = PlayerNameSecondary,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Create your tournament championship to generate league round-robin fixtures and track real-time standings.",
                                            style = BodyCaption,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Button(
                                            onClick = { showCreateDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("CREATE TOURNAMENT", style = MicroTag, color = Color.White)
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                // Header Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("TEAM", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                                    Text("P", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                                    Text("W", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                                    Text("L", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                                    Text("PTS", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.7f))
                                    Text("NRR", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
                                }
                            }

                            items(pointsTableData) { row ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                    shape = RoundedCornerShape(8.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(2f)) {
                                            Text(
                                                text = "${row.rank}. ${row.shortCode}",
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = row.teamName,
                                                color = TextTertiary,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Text(text = "${row.played}", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                                        Text(text = "${row.won}", color = BoundaryAmber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                                        Text(text = "${row.lost}", color = TextTertiary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                                        Text(text = "${row.points}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.7f))
                                        Text(text = row.nrr, color = if (row.nrr.startsWith("+")) BoundaryAmber else TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Leaders Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (mostRuns.isEmpty() && mostWickets.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                    shape = RoundedCornerShape(10.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "No Tournament Leaders Yet",
                                            style = PlayerNameSecondary,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Orange Cap (Most Runs) and Purple Cap (Most Wickets) leaderboards will calculate as tournament matches are scored.",
                                            style = BodyCaption,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                    shape = RoundedCornerShape(10.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "MOST RUNS (ORANGE CAP)",
                                                style = SectionLabel,
                                                color = BoundaryAmber
                                            )
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        mostRuns.forEachIndexed { index, p ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("${p.rank}. ${p.name}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                    Text("${p.team} • ${p.secondaryStat}", color = TextTertiary, fontSize = 10.sp)
                                                }
                                                Text(p.primaryStat, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                            if (index < mostRuns.size - 1) {
                                                HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                    shape = RoundedCornerShape(10.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "MOST WICKETS (PURPLE CAP)",
                                                style = SectionLabel,
                                                color = CrimsonPrimaryDark
                                            )
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = CrimsonPrimaryDark, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        mostWickets.forEachIndexed { index, p ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("${p.rank}. ${p.name}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                    Text("${p.team} • ${p.secondaryStat}", color = TextTertiary, fontSize = 10.sp)
                                                }
                                                Text(p.primaryStat, color = CrimsonPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                            if (index < mostWickets.size - 1) {
                                                HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Fixtures Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (dynamicFixtures.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                    shape = RoundedCornerShape(10.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "No Fixtures Scheduled",
                                            style = PlayerNameSecondary,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Round-robin match fixtures will appear here automatically once a tournament is created.",
                                            style = BodyCaption,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(dynamicFixtures) { fixture ->
                                Card(
                                    onClick = { onFixtureClick(fixture.teamA, fixture.teamB, activeOvers) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                    shape = RoundedCornerShape(8.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(fixture.stage, color = TextTertiary, fontSize = 10.sp)
                                            Text(
                                                text = if (fixture.isCompleted) "Completed" else "Upcoming",
                                                color = if (fixture.isCompleted) TextSecondary else BoundaryAmber,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${fixture.teamA} vs ${fixture.teamB}",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = fixture.summary,
                                            color = if (fixture.isCompleted) TextSecondary else BoundaryAmber,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var tourneyName by remember { mutableStateOf("") }
        var oversText by remember { mutableStateOf("20") }
        var teamsInput by remember { mutableStateOf("Warriors CC, Royal Tigers, Metro Strikers, Dadar Dynamos") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = SlateSurfaceDark,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Create Grassroots Tournament",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = tourneyName,
                        onValueChange = { tourneyName = it },
                        label = { Text("Tournament Name") },
                        placeholder = { Text("e.g. Gully Premier League 2026") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CrimsonPrimaryDark,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = oversText,
                        onValueChange = { oversText = it },
                        label = { Text("Overs per side") },
                        placeholder = { Text("10, 15, or 20") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CrimsonPrimaryDark,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = teamsInput,
                        onValueChange = { teamsInput = it },
                        label = { Text("Participating Teams (comma-separated)") },
                        placeholder = { Text("Team 1, Team 2, Team 3") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CrimsonPrimaryDark,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current
                Button(
                    onClick = {
                        hapticManager?.tapUiClick()
                        val trimmedName = tourneyName.trim().ifEmpty { "Gully Championship 2026" }
                        val teams = teamsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val overs = oversText.toIntOrNull() ?: 20
                        onCreateTournament(trimmedName, overs, teams)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CREATE & INITIALIZE", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current
                TextButton(onClick = {
                    hapticManager?.tapUiClick()
                    showCreateDialog = false
                }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}
