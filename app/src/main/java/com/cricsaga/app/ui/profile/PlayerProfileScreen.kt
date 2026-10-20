package com.cricsaga.app.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.cricsaga.app.data.local.ProManager
import com.cricsaga.app.data.local.ProSubscriptionState
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.cricsaga_logo),
                            contentDescription = "CricSaga Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("My Cricket Career", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
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
            // 1. Player Profile Hero Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    border = if (subscriptionState.isActive) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BoundaryAmber)) else null
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B1D1D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "RS",
                                    color = CrimsonPrimaryDark,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Rohit Sharma",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (subscriptionState.isActive) {
                                        Icon(Icons.Default.Verified, contentDescription = "Pro", tint = BoundaryAmber, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(
                                    text = "Top-Order Batsman • Right-Arm Medium",
                                    color = MutedGrey,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Mumbai, Maharashtra • Jersey #45",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFF2C2C2C), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Pro Status Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (subscriptionState.isActive) BoundaryAmber else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (subscriptionState.isActive) {
                                        "PRO ACTIVE (${subscriptionState.plan?.title ?: "MEMBER"})"
                                    } else {
                                        "FREE TIER"
                                    },
                                    color = if (subscriptionState.isActive) BoundaryAmber else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = onRedeemCodeClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (subscriptionState.isActive) ElevatedSurfaceDark else CrimsonPrimaryDark
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (subscriptionState.isActive) "VIEW PRO PERKS" else "REDEEM PRO CODE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 2. Pro Pricing Plans Overview (₹99 / 3mo, ₹199 / 1yr, ₹2000 / Lifetime)
            if (!subscriptionState.isActive) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onRedeemCodeClick() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF231B1B)),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CrimsonPrimaryDark))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("UPGRADE TO CRICSAGA PRO", color = BoundaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Redeem Code Available", color = Color.LightGray, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• ₹99 for 3 Months (Seasonal Pass)\n• ₹199 for 1 Year (Annual Pass - Most Popular)\n• ₹2,000 for Lifetime VIP (Forever Ad-Free)",
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 3. Minimal Ad Banner for Free Users (Hidden for Pro)
            if (!subscriptionState.isActive) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
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
                                text = "SG Cricket Gear: Buy 1 Get 1 on batting grips & accessories",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 4. Tabs Row for Career Records
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SlateSurfaceDark,
                    contentColor = CrimsonPrimaryDark,
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
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Color.White else MutedGrey
                                )
                            }
                        )
                    }
                }
            }

            // 5. Tab Content
            when (selectedTab) {
                0 -> {
                    // Overview Stats Grid
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatBox(label = "MATCHES", value = "24", modifier = Modifier.weight(1f))
                                StatBox(label = "INNINGS", value = "22", modifier = Modifier.weight(1f))
                                StatBox(label = "TOTAL RUNS", value = "782", valueColor = CrimsonPrimaryDark, modifier = Modifier.weight(1f))
                                StatBox(label = "WICKETS", value = "14", valueColor = BoundaryAmber, modifier = Modifier.weight(1f))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatBox(label = "BATTING AVG", value = "41.15", modifier = Modifier.weight(1f))
                                StatBox(label = "STRIKE RATE", value = "168.4", modifier = Modifier.weight(1f))
                                StatBox(label = "HIGH SCORE", value = "88*", modifier = Modifier.weight(1f))
                                StatBox(label = "BOWLING ECON", value = "6.80", modifier = Modifier.weight(1f))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatBox(label = "50s", value = "5", modifier = Modifier.weight(1f))
                                StatBox(label = "100s", value = "0", modifier = Modifier.weight(1f))
                                StatBox(label = "4s / 6s", value = "68 / 26", modifier = Modifier.weight(1f))
                                StatBox(label = "CATCHES", value = "12", modifier = Modifier.weight(1f))
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
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("CAREER BATTING STATISTICS", color = BoundaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                TableStatRow("Matches Played", "24")
                                TableStatRow("Innings Batted", "22")
                                TableStatRow("Not Outs (NO)", "3")
                                TableStatRow("Total Runs Scored", "782")
                                TableStatRow("Balls Faced", "464")
                                TableStatRow("Highest Score", "88*")
                                TableStatRow("Batting Average", "41.15")
                                TableStatRow("Strike Rate", "168.45")
                                TableStatRow("Fifties (50s)", "5")
                                TableStatRow("Hundreds (100s)", "0")
                                TableStatRow("Fours (4s)", "68")
                                TableStatRow("Sixes (6s)", "26")
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
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("CAREER BOWLING STATISTICS", color = BoundaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                TableStatRow("Overs Bowled", "38.4 ov")
                                TableStatRow("Maiden Overs", "2")
                                TableStatRow("Runs Conceded", "262")
                                TableStatRow("Wickets Taken", "14")
                                TableStatRow("Best Bowling Figures", "3/16 (4.0 ov)")
                                TableStatRow("Bowling Average", "18.71")
                                TableStatRow("Economy Rate", "6.80")
                                TableStatRow("4-Wicket Hauls", "0")
                                TableStatRow("5-Wicket Hauls", "0")
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
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("CAREER FIELDING STATISTICS", color = BoundaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                TableStatRow("Catches Taken", "12")
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
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(record.matchTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Surface(
                                        color = if (record.resultWon) Color(0x334CAF50) else Color(0x33FF5252),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (record.resultWon) "WON" else "LOST",
                                            color = if (record.resultWon) BoundaryGreen else DismissalRed,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(record.dateText, color = MutedGrey, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = ElevatedSurfaceDark,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Bat: ${record.battingPerformance}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        color = ElevatedSurfaceDark,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Bowl: ${record.bowlingPerformance}",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
}

@Composable
fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = MutedGrey, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun TableStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.LightGray, fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
    HorizontalDivider(color = Color(0xFF262626), thickness = 0.5.dp)
}
