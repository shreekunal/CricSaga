package com.cricsaga.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    onRedeemCodeClick: () -> Unit,
    onEditProfileClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Batting", "Bowling", "Fielding", "Matches")
    val subscriptionState by proManager.subscriptionState.collectAsState()

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
                                    text = "RS",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rohit Sharma",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Top-Order Batsman • Right-Arm Medium",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Mumbai • Jersey #45",
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
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
                                    CleanMetricCol("MATCHES", "24")
                                    CleanMetricCol("RUNS", "782")
                                    CleanMetricCol("WICKETS", "14")
                                    CleanMetricCol("AVG", "41.1")
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
                                    TableStatRow("Strike Rate", "168.4")
                                    TableStatRow("Highest Score", "88*")
                                    TableStatRow("Bowling Economy", "6.80")
                                    TableStatRow("Best Bowling", "3/16 (4.0 ov)")
                                    TableStatRow("Fifties / Hundreds", "5 / 0")
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
                                TableStatRow("Innings Batted", "22")
                                TableStatRow("Not Outs (NO)", "3")
                                TableStatRow("Total Runs", "782")
                                TableStatRow("Balls Faced", "464")
                                TableStatRow("Highest Score", "88*")
                                TableStatRow("Batting Average", "41.15")
                                TableStatRow("Strike Rate", "168.45")
                                TableStatRow("Fifties (50s)", "5")
                                TableStatRow("Hundreds (100s)", "0")
                                TableStatRow("Fours / Sixes", "68 / 26")
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
                                TableStatRow("Overs Bowled", "38.4 ov")
                                TableStatRow("Maiden Overs", "2")
                                TableStatRow("Runs Conceded", "262")
                                TableStatRow("Wickets Taken", "14")
                                TableStatRow("Best Bowling", "3/16 (4.0 ov)")
                                TableStatRow("Bowling Average", "18.71")
                                TableStatRow("Economy Rate", "6.80")
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
