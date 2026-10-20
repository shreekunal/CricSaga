package com.cricsaga.app.ui.tournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentListScreen(
    isPro: Boolean,
    onCreateTournamentClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Points Table", "Leaders", "Fixtures")

    val samplePointsTable = listOf(
        PointsTableRow(1, "Warriors Cricket Club", "WAR", 4, 3, 1, 6, "+1.420"),
        PointsTableRow(2, "Royal Tigers Mumbai", "RTM", 4, 3, 1, 6, "+0.850"),
        PointsTableRow(3, "Metro Strikers", "MSK", 4, 2, 2, 4, "+0.120"),
        PointsTableRow(4, "Shivaji Park Champions", "SPC", 4, 0, 4, 0, "-2.390")
    )

    var showCreateDialog by remember { mutableStateOf(false) }
    var activeTournamentTitle by remember { mutableStateOf("Mumbai Champions Trophy 2026") }
    var pointsTableData by remember { mutableStateOf(samplePointsTable) }

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
                            color = CrimsonPrimaryDark
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) TextPrimary else TextSecondary
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CrimsonPrimaryDark,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Tournament")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    // Points Table (Clean Sports League Grid)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Table Header Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("#", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(18.dp))
                                        Text("TEAM", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Text("P", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                                        Text("W", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                                        Text("L", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                                        Text("PTS", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                                        Text("NRR", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(46.dp), textAlign = TextAlign.End)
                                    }

                                    HorizontalDivider(color = DividerDark, thickness = 0.5.dp)

                                    pointsTableData.forEach { row ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 6.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "${row.rank}",
                                                color = if (row.rank <= 2) BoundaryGreen else TextTertiary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.width(18.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(row.teamName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                Text(row.shortCode, color = TextTertiary, fontSize = 10.sp)
                                            }
                                            Text("${row.played}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                                            Text("${row.won}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                                            Text("${row.lost}", color = TextTertiary, fontSize = 12.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                                            Text("${row.points}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                                            Text(row.nrr, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(46.dp), textAlign = TextAlign.End)
                                        }
                                        HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Top 2 teams qualify for finals • Net Run Rate computed per official rules.",
                                color = TextTertiary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                1 -> {
                    // Leaderboards: Most Runs & Most Wickets (Clean Minimal List)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        item {
                            Text("MOST RUNS", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    mostRuns.forEachIndexed { index, player ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${player.rank}", color = TextTertiary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.width(22.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(player.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                Text("${player.team} • ${player.secondaryStat}", color = TextTertiary, fontSize = 11.sp)
                                            }
                                            Text(player.primaryStat, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        if (index < mostRuns.size - 1) {
                                            HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("MOST WICKETS", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    mostWickets.forEachIndexed { index, player ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${player.rank}", color = TextTertiary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.width(22.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(player.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                Text("${player.team} • ${player.secondaryStat}", color = TextTertiary, fontSize = 11.sp)
                                            }
                                            Text(player.primaryStat, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        item {
                            Card(
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
                                        Text("Match 1 • Final", color = TextTertiary, fontSize = 10.sp)
                                        Text("Completed", color = TextSecondary, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Warriors CC vs Royal Tigers", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Warriors CC won by 14 runs (172/5 vs 158/8)", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }

                        item {
                            Card(
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
                                        Text("Match 2 • Semi-Final", color = TextTertiary, fontSize = 10.sp)
                                        Text("Completed", color = TextSecondary, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Metro Strikers vs Shivaji Park Champions", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Metro Strikers won by 7 wickets", color = TextSecondary, fontSize = 11.sp)
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
                    color = Color.White,
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
                        label = { Text("Tournament Name", color = MutedGrey) },
                        placeholder = { Text("e.g. Gully Premier League 2026", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CrimsonPrimaryDark,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = oversText,
                        onValueChange = { oversText = it },
                        label = { Text("Overs per side", color = MutedGrey) },
                        placeholder = { Text("10, 15, or 20", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CrimsonPrimaryDark,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = teamsInput,
                        onValueChange = { teamsInput = it },
                        label = { Text("Participating Teams (comma-separated)", color = MutedGrey) },
                        placeholder = { Text("Team 1, Team 2, Team 3", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CrimsonPrimaryDark,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = tourneyName.trim().ifEmpty { "Gully Championship 2026" }
                        val teams = teamsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        if (teams.isNotEmpty()) {
                            val newRows = teams.mapIndexed { index, name ->
                                val code = name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(3).uppercase().ifEmpty { "TM${index + 1}" }
                                PointsTableRow(
                                    rank = index + 1,
                                    teamName = name,
                                    shortCode = code,
                                    played = 0,
                                    won = 0,
                                    lost = 0,
                                    points = 0,
                                    nrr = "0.000"
                                )
                            }
                            pointsTableData = newRows
                        }
                        activeTournamentTitle = trimmedName
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CREATE & INITIALIZE", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = MutedGrey)
                }
            }
        )
    }
}
