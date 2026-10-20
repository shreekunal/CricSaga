package com.cricsaga.app.ui.tournaments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.R
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
    val tabs = listOf("Points Table", "Leaderboards", "Fixtures")

    val samplePointsTable = listOf(
        PointsTableRow(1, "Warriors Cricket Club", "WAR", 4, 3, 1, 6, "+1.420"),
        PointsTableRow(2, "Royal Tigers Mumbai", "RTM", 4, 3, 1, 6, "+0.850"),
        PointsTableRow(3, "Metro Strikers", "MSK", 4, 2, 2, 4, "+0.120"),
        PointsTableRow(4, "Shivaji Park Champions", "SPC", 4, 0, 4, 0, "-2.390")
    )

    val orangeCapHolders = listOf(
        LeaderboardPlayer(1, "Rohit Verma", "Warriors CC", "324 Runs", "Avg: 64.8 • SR: 184.2"),
        LeaderboardPlayer(2, "Virat Sharma", "Royal Tigers", "289 Runs", "Avg: 57.8 • SR: 172.0"),
        LeaderboardPlayer(3, "Shubman Gill", "Metro Strikers", "245 Runs", "Avg: 49.0 • SR: 155.0")
    )

    val purpleCapHolders = listOf(
        LeaderboardPlayer(1, "Jasprit Khan", "Royal Tigers", "14 Wickets", "Econ: 5.20 • Best: 5/14"),
        LeaderboardPlayer(2, "Mohammed Shami", "Warriors CC", "12 Wickets", "Econ: 6.10 • Best: 4/18"),
        LeaderboardPlayer(3, "Rashid Rao", "Metro Strikers", "10 Wickets", "Econ: 5.80 • Best: 3/20")
    )

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(CharcoalBackgroundDark)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.cricsaga_logo),
                                contentDescription = "CricSaga Logo",
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Tournaments Center",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Mumbai Grassroots Champions Trophy 2026",
                                    fontSize = 10.sp,
                                    color = BoundaryAmber
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SlateSurfaceDark,
                    contentColor = CrimsonPrimaryDark,
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
                                    color = if (selectedTab == index) Color.White else MutedGrey
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTournamentClick,
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
            // Minimal Ad Banner for Free Tier
            if (!isPro) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = ElevatedSurfaceDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF333333),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "SPONSOR",
                                color = Color.Gray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Official League Ball: Kookaburra Turf Balls • Verified by CricSaga",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // Points Table with NRR
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
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Table Header Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(ElevatedSurfaceDark, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("#", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                                        Text("TEAM", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Text("P", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                                        Text("W", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                                        Text("L", color = MutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                                        Text("PTS", color = BoundaryAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                                        Text("NRR", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), textAlign = TextAlign.End)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    samplePointsTable.forEach { row ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${row.rank}", color = if (row.rank <= 2) BoundaryGreen else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(row.teamName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                Text(row.shortCode, color = MutedGrey, fontSize = 10.sp)
                                            }
                                            Text("${row.played}", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                                            Text("${row.won}", color = BoundaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                                            Text("${row.lost}", color = DismissalRed, fontSize = 12.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                                            Text("${row.points}", color = BoundaryAmber, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                                            Text(row.nrr, color = if (row.nrr.startsWith("+")) BoundaryGreen else DismissalRed, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(50.dp), textAlign = TextAlign.End)
                                        }
                                        HorizontalDivider(color = Color(0xFF2B2B2B), thickness = 0.5.dp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "• Top 2 teams qualify directly for Finals.\n• Net Run Rate (NRR) computed automatically per ICC Grassroots Standards.",
                                color = MutedGrey,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                1 -> {
                    // Leaderboards: Orange Cap & Purple Cap
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        // Orange Cap Section
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(12.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE65100)))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(0xFFE65100),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "ORANGE CAP",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Most Runs in Tournament", color = MutedGrey, fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    orangeCapHolders.forEach { player ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("#${player.rank}", color = if (player.rank == 1) Color(0xFFE65100) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(28.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("${player.team} • ${player.secondaryStat}", color = MutedGrey, fontSize = 11.sp)
                                            }
                                            Text(player.primaryStat, color = Color(0xFFE65100), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                        }
                                        HorizontalDivider(color = Color(0xFF262626), thickness = 0.5.dp)
                                    }
                                }
                            }
                        }

                        // Purple Cap Section
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(12.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF7B1FA2)))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(0xFF7B1FA2),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "PURPLE CAP",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Most Wickets in Tournament", color = MutedGrey, fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    purpleCapHolders.forEach { player ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("#${player.rank}", color = if (player.rank == 1) Color(0xFF7B1FA2) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(28.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("${player.team} • ${player.secondaryStat}", color = MutedGrey, fontSize = 11.sp)
                                            }
                                            Text(player.primaryStat, color = Color(0xFFBA68C8), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                        }
                                        HorizontalDivider(color = Color(0xFF262626), thickness = 0.5.dp)
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
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("LEAGUE MATCH #1 • COMPLETED", color = BoundaryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Warriors CC vs Royal Tigers", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Warriors CC: 172/5 (15.0 ov) • Tigers: 158/8 (15.0 ov)", color = Color.LightGray, fontSize = 12.sp)
                                    Text("Warriors CC won by 14 runs", color = BoundaryAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("LEAGUE MATCH #2 • COMPLETED", color = BoundaryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Metro Strikers vs Shivaji Park Champions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Metro Strikers: 142/3 (13.4 ov) chased 140", color = Color.LightGray, fontSize = 12.sp)
                                    Text("Metro Strikers won by 7 wickets", color = BoundaryAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("FINAL MATCH • UPCOMING", color = CrimsonPrimaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Warriors CC vs Royal Tigers Mumbai", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Sunday, 25th Nov • 4:00 PM • Shivaji Park Ground #1", color = MutedGrey, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
