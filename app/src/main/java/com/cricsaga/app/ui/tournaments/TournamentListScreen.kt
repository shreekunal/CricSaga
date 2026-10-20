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
import com.cricsaga.app.data.local.entity.TournamentEntity
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
    onCreateTournament: (name: String, overs: Int, teams: List<String>) -> Unit = { _, _, _ -> },
    onFixtureClick: (teamA: String, teamB: String, overs: Int) -> Unit = { _, _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Points Table", "Leaders", "Fixtures")

    var showCreateDialog by remember { mutableStateOf(false) }

    val activeTournament = tournaments.firstOrNull()
    val activeTournamentTitle = activeTournament?.name ?: "Mumbai Champions Trophy 2026"
    val activeOvers = activeTournament?.overs ?: 20

    // Build dynamic points table from tournament team names if available
    val pointsTableData = remember(activeTournament) {
        if (activeTournament != null && activeTournament.teamNamesCsv.isNotBlank()) {
            val teamNames = activeTournament.teamNamesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            teamNames.mapIndexed { index, name ->
                val code = name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(3).uppercase().ifEmpty { "TM${index + 1}" }
                PointsTableRow(
                    rank = index + 1,
                    teamName = name,
                    shortCode = code,
                    played = if (index < 2) 2 else 1,
                    won = if (index == 0) 2 else if (index == 1) 1 else 0,
                    lost = if (index == 0) 0 else 1,
                    points = if (index == 0) 4 else if (index == 1) 2 else 0,
                    nrr = if (index == 0) "+1.420" else if (index == 1) "+0.250" else "-0.850"
                )
            }
        } else {
            listOf(
                PointsTableRow(1, "Warriors Cricket Club", "WAR", 4, 3, 1, 6, "+1.420"),
                PointsTableRow(2, "Royal Tigers Mumbai", "RTM", 4, 3, 1, 6, "+0.850"),
                PointsTableRow(3, "Metro Strikers", "MSK", 4, 2, 2, 4, "+0.120"),
                PointsTableRow(4, "Shivaji Park Champions", "SPC", 4, 0, 4, 0, "-2.390")
            )
        }
    }

    val dynamicFixtures = remember(activeTournament) {
        if (activeTournament != null && activeTournament.teamNamesCsv.isNotBlank()) {
            val teams = activeTournament.teamNamesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val list = mutableListOf<FixtureDisplay>()
            for (i in teams.indices) {
                for (j in i + 1 until teams.size) {
                    list.add(
                        FixtureDisplay(
                            stage = "League Match ${list.size + 1}",
                            teamA = teams[i],
                            teamB = teams[j],
                            summary = "Scheduled • Tap to score live",
                            isCompleted = false
                        )
                    )
                }
            }
            if (list.isNotEmpty()) list else listOf(
                FixtureDisplay("Match 1 • Final", "Warriors CC", "Royal Tigers", "Warriors CC won by 14 runs (172/5 vs 158/8)", true),
                FixtureDisplay("Match 2 • Semi-Final", "Metro Strikers", "Shivaji Park", "Metro Strikers won by 7 wickets", true)
            )
        } else {
            listOf(
                FixtureDisplay("Match 1 • Final", "Warriors CC", "Royal Tigers", "Warriors CC won by 14 runs (172/5 vs 158/8)", true),
                FixtureDisplay("Match 2 • Semi-Final", "Metro Strikers", "Shivaji Park Champions", "Metro Strikers won by 7 wickets", true),
                FixtureDisplay("Match 3 • Qualifier", "Warriors CC", "Metro Strikers", "Scheduled • Tap to score live", false)
            )
        }
    }

    val mostRuns = listOf(
        LeaderboardPlayer(1, "Rohit Verma", "Warriors CC", "324", "Avg 64.8 • SR 184.2"),
        LeaderboardPlayer(2, "Virat Sharma", "Royal Tigers", "289", "Avg 57.8 • SR 172.0"),
        LeaderboardPlayer(3, "Shubman Gill", "Metro Strikers", "245", "Avg 49.0 • SR 155.0")
    )

    val mostWickets = listOf(
        LeaderboardPlayer(1, "Jasprit Khan", "Royal Tigers", "14", "Econ 5.20 • Best 5/14"),
        LeaderboardPlayer(2, "Mohammed Shami", "Warriors CC", "12", "Econ 6.10 • Best 4/18"),
        LeaderboardPlayer(3, "Rashid Rao", "Metro Strikers", "10", "Econ 5.80 • Best 3/20")
    )

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

                1 -> {
                    // Leaders Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
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

                2 -> {
                    // Fixtures Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
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
