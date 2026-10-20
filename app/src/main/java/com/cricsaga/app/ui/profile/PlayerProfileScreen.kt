package com.cricsaga.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.ProManager
import com.cricsaga.app.data.local.entity.PlayerStatsEntity
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.ui.profile.dialog.EditProfileDialog
import com.cricsaga.app.ui.theme.*

data class CareerMatchRecord(
    val matchTitle: String,
    val dateText: String,
    val battingPerformance: String,
    val bowlingPerformance: String,
    val resultWon: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    proManager: ProManager,
    currentUser: UserEntity? = null,
    playerStats: PlayerStatsEntity? = null,
    onRedeemCodeClick: () -> Unit,
    onUpdateProfile: (UserEntity) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Batting", "Bowling", "Fielding", "Matches")
    val subscriptionState by proManager.subscriptionState.collectAsState()

    val playerName = currentUser?.fullName?.ifBlank { "Rohit Sharma" } ?: "Rohit Sharma"
    val initials = playerName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifEmpty { "CR" }

    val roleFormatted = currentUser?.playingRole?.name?.replace('_', ' ')?.lowercase()
        ?.replaceFirstChar { it.uppercase() } ?: "All-Rounder"
    val bowlingFormatted = currentUser?.bowlingStyle?.name?.replace('_', ' ')?.lowercase()
        ?.replaceFirstChar { it.uppercase() } ?: "Right-Arm Medium"
    val roleText = "$roleFormatted • $bowlingFormatted"
    val cityText = "${currentUser?.city?.ifBlank { "Mumbai" } ?: "Mumbai"} • Jersey #${currentUser?.jerseyNumber?.ifBlank { "45" } ?: "45"}"

    val totalMatches = 24 + (playerStats?.matches ?: 0)
    val totalInnings = 22 + (playerStats?.innings ?: 0)
    val totalRuns = 782 + (playerStats?.runsScored ?: 0)
    val totalBalls = 464 + (playerStats?.ballsFaced ?: 0)
    val highestScore = maxOf(88, playerStats?.highestScore ?: 0)
    val totalWickets = 14 + (playerStats?.wicketsTaken ?: 0)
    val totalBallsBowled = (38 * 6 + 4) + (playerStats?.legalBallsBowled ?: 0)
    val oversBowledFormatted = "${totalBallsBowled / 6}.${totalBallsBowled % 6}"
    val totalRunsConceded = 262 + (playerStats?.runsConceded ?: 0)
    val totalMaidens = 2 + (playerStats?.maidens ?: 0)
    val totalFifties = 5 + (playerStats?.fifties ?: 0)
    val totalHundreds = 0 + (playerStats?.hundreds ?: 0)
    val totalFours = 68 + (playerStats?.fours ?: 0)
    val totalSixes = 26 + (playerStats?.sixes ?: 0)

    val batAvg = if (totalInnings > 0) String.format("%.1f", totalRuns.toDouble() / (totalInnings - 3).coerceAtLeast(1)) else "0.0"
    val batSr = if (totalBalls > 0) String.format("%.1f", (totalRuns.toDouble() / totalBalls.toDouble()) * 100.0) else "0.0"
    val bowlEcon = if (totalBallsBowled > 0) String.format("%.2f", (totalRunsConceded.toDouble() / (totalBallsBowled.toDouble() / 6.0))) else "0.0"
    val bowlAvg = if (totalWickets > 0) String.format("%.2f", totalRunsConceded.toDouble() / totalWickets.toDouble()) else "0.0"

    val matchHistory = listOf(
        CareerMatchRecord(
            matchTitle = "Warriors CC vs Royal Tigers",
            dateText = "18 Oct 2026 • 15 Overs",
            battingPerformance = "64* (32) • 6x4, 3x6",
            bowlingPerformance = "2/18 (3.0 ov)",
            resultWon = true
        ),
        CareerMatchRecord(
            matchTitle = "Shivaji Park Champions vs Metro Strikers",
            dateText = "12 Oct 2026 • 20 Overs",
            battingPerformance = "42 (24) • 4x4, 2x6",
            bowlingPerformance = "1/24 (4.0 ov)",
            resultWon = true
        ),
        CareerMatchRecord(
            matchTitle = "Corporate Trophy Semi-Final",
            dateText = "05 Oct 2026 • 12 Overs",
            battingPerformance = "18 (9) • 1x4, 2x6",
            bowlingPerformance = "0/15 (2.0 ov)",
            resultWon = false
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Career",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Switch Account / Logout",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Clean Player Hero Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(ElevatedSurfaceDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playerName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = roleText,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = cityText,
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
                            }
                            if (currentUser != null) {
                                IconButton(onClick = { showEditProfileDialog = true }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Membership Status Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (subscriptionState.isActive) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = BoundaryAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (subscriptionState.isActive) {
                                        "PRO • ${subscriptionState.plan?.title ?: "ACTIVE"}"
                                    } else {
                                        "Free Tier"
                                    },
                                    color = if (subscriptionState.isActive) BoundaryAmber else TextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = onRedeemCodeClick,
                                colors = ButtonDefaults.buttonColors(containerColor = ElevatedSurfaceDark),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (subscriptionState.isActive) "View Perks" else "Redeem Code",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Tabs Row for Career Records
            item {
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
                    },
                    modifier = Modifier.padding(top = 8.dp)
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

            // 3. Tab Content
            when (selectedTab) {
                0 -> {
                    // Overview: 4-Stat Hero Row + Career Highlights Table
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 4 Key Metrics Strip
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    CleanMetricCol("MATCHES", "$totalMatches")
                                    CleanMetricCol("RUNS", "$totalRuns")
                                    CleanMetricCol("WICKETS", "$totalWickets")
                                    CleanMetricCol("AVG", batAvg)
                                }
                            }

                            // Highlights Data Table
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    TableStatRow("Strike Rate", batSr)
                                    TableStatRow("Highest Score", "$highestScore*")
                                    TableStatRow("Bowling Economy", bowlEcon)
                                    TableStatRow("Best Bowling", "3/16 (4.0 ov)")
                                    TableStatRow("Fifties / Hundreds", "$totalFifties / $totalHundreds")
                                    TableStatRow("Catches / Run Outs", "12 / 3")
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Batting Table
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("BATTING CAREER", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                TableStatRow("Innings Batted", "$totalInnings")
                                TableStatRow("Not Outs (NO)", "3")
                                TableStatRow("Total Runs", "$totalRuns")
                                TableStatRow("Balls Faced", "$totalBalls")
                                TableStatRow("Highest Score", "$highestScore*")
                                TableStatRow("Batting Average", batAvg)
                                TableStatRow("Strike Rate", batSr)
                                TableStatRow("Fifties (50s)", "$totalFifties")
                                TableStatRow("Hundreds (100s)", "$totalHundreds")
                                TableStatRow("Fours / Sixes", "$totalFours / $totalSixes")
                            }
                        }
                    }
                }

                2 -> {
                    // Bowling Table
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("BOWLING CAREER", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                TableStatRow("Overs Bowled", "$oversBowledFormatted ov")
                                TableStatRow("Maiden Overs", "$totalMaidens")
                                TableStatRow("Runs Conceded", "$totalRunsConceded")
                                TableStatRow("Wickets Taken", "$totalWickets")
                                TableStatRow("Best Bowling", "3/16 (4.0 ov)")
                                TableStatRow("Bowling Average", bowlAvg)
                                TableStatRow("Economy Rate", bowlEcon)
                            }
                        }
                    }
                }

                3 -> {
                    // Fielding Table
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("FIELDING CAREER", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                TableStatRow("Catches", "12")
                                TableStatRow("Direct Run Outs", "3")
                                TableStatRow("Assisted Run Outs", "2")
                                TableStatRow("Stumpings", "0")
                            }
                        }
                    }
                }

                4 -> {
                    // Match History
                    items(matchHistory) { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(record.matchTitle, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(
                                        text = if (record.resultWon) "Won" else "Lost",
                                        color = if (record.resultWon) BoundaryGreen else TextTertiary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(record.dateText, color = TextTertiary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Bat: ${record.battingPerformance}", color = TextSecondary, fontSize = 11.sp)
                                    Text("Bowl: ${record.bowlingPerformance}", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showEditProfileDialog && currentUser != null) {
            EditProfileDialog(
                user = currentUser,
                onDismiss = { showEditProfileDialog = false },
                onSave = { updated ->
                    onUpdateProfile(updated)
                    showEditProfileDialog = false
                }
            )
        }
    }
}

@Composable
fun CleanMetricCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
    }
}

@Composable
fun TableStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
    HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
}
