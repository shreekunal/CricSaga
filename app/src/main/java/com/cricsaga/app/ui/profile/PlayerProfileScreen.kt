package com.cricsaga.app.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.ProManager
import com.cricsaga.app.data.local.entity.PlayerStatsEntity
import com.cricsaga.app.data.local.entity.TeamEntity
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.ui.profile.dialog.EditProfileDialog
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.ImageUtils
import com.cricsaga.app.ui.utils.ShareUtils

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
    teams: List<TeamEntity> = emptyList(),
    posts: List<com.cricsaga.app.data.local.entity.PostEntity> = emptyList(),
    followersCount: Int = 0,
    followingCount: Int = 0,
    onRedeemCodeClick: () -> Unit,
    onUpdateProfile: (UserEntity) -> Unit = {},
    onTeamClick: (String) -> Unit = {},
    onCreateOrJoinTeamClick: () -> Unit = {},
    onLikePost: (String) -> Unit = {},
    onDeletePost: (String) -> Unit = {},
    onRepostPost: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Stats", "Teams", "Highlights", "Posts")
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
    val jerseyNum = currentUser?.jerseyNumber?.ifBlank { "18" } ?: "18"
    val cityText = "${currentUser?.city?.ifBlank { "Mumbai" } ?: "Mumbai"} • Jersey #$jerseyNum"

    val totalMatches = playerStats?.matches ?: 0
    val totalInnings = playerStats?.innings ?: 0
    val totalRuns = playerStats?.runsScored ?: 0
    val totalBalls = playerStats?.ballsFaced ?: 0
    val highestScore = playerStats?.highestScore ?: 0
    val totalWickets = playerStats?.wicketsTaken ?: 0
    val totalBallsBowled = playerStats?.legalBallsBowled ?: 0
    val oversBowledFormatted = "${totalBallsBowled / 6}.${totalBallsBowled % 6}"
    val totalRunsConceded = playerStats?.runsConceded ?: 0
    val totalMaidens = playerStats?.maidens ?: 0
    val totalFifties = playerStats?.fifties ?: 0
    val totalHundreds = playerStats?.hundreds ?: 0
    val totalFours = playerStats?.fours ?: 0
    val totalSixes = playerStats?.sixes ?: 0

    val batAvg = if (totalInnings > 0) String.format(java.util.Locale.US, "%.1f", totalRuns.toDouble() / totalInnings.toDouble()) else "0.0"
    val batSr = if (totalBalls > 0) String.format(java.util.Locale.US, "%.1f", (totalRuns.toDouble() / totalBalls.toDouble()) * 100.0) else "0.0"
    val bowlEcon = if (totalBallsBowled > 0) String.format(java.util.Locale.US, "%.2f", (totalRunsConceded.toDouble() / (totalBallsBowled.toDouble() / 6.0))) else "0.0"
    val bowlAvg = if (totalWickets > 0) String.format(java.util.Locale.US, "%.2f", totalRunsConceded.toDouble() / totalWickets.toDouble()) else "0.0"

    // Archetype resolution
    val archetype = when {
        (batSr.toDoubleOrNull() ?: 0.0) >= 140.0 && totalRuns >= 50 -> "POWER HITTER"
        currentUser?.playingRole == PlayingRole.BOWLER -> "STRIKE BOWLER"
        currentUser?.playingRole == PlayingRole.WICKET_KEEPER -> "WK ANCHOR"
        (batAvg.toDoubleOrNull() ?: 0.0) >= 35.0 && totalRuns >= 50 -> "SOLID ANCHOR"
        else -> "ALL-ROUNDER"
    }

    val matchHistory = emptyList<CareerMatchRecord>()

    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header Row: My Career Title + Share & Logout Actions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Career",
                        style = PlayerNamePrimary,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Share Collectible Athlete Card Action
                        IconButton(onClick = {
                            hapticManager?.tapUiClick()
                            ShareUtils.generateAndShareAthleteCard(
                                context = context,
                                playerName = playerName,
                                jerseyNumber = jerseyNum,
                                archetype = archetype,
                                city = currentUser?.city?.ifBlank { "Mumbai" } ?: "Mumbai",
                                matches = totalMatches,
                                runs = totalRuns,
                                wickets = totalWickets,
                                average = batAvg,
                                profilePhotoPath = currentUser?.profilePhotoUrl
                            )
                        }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share Athlete Card",
                                tint = AmberFourPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Logout / Switch Account Action
                        IconButton(onClick = {
                            hapticManager?.tapUiClick()
                            onLogout()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Switch Account / Logout",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 1. Collectible Athlete Card Hero
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateSurfaceDark)
                        .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                ) {
                    // Oversized Jersey Watermark (Barlow Condensed ExtraBold)
                    Text(
                        text = "#$jerseyNum",
                        style = JerseyWatermark,
                        color = TextPrimary.copy(alpha = 0.05f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 16.dp, y = 24.dp)
                    )

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val avatarBitmap = remember(currentUser?.profilePhotoUrl) {
                                ImageUtils.loadBitmapFromPath(currentUser?.profilePhotoUrl)
                            }

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(ElevatedSurfaceDark)
                                    .border(1.5.dp, CrimsonPrimaryDark.copy(alpha = 0.6f), CircleShape)
                                    .clickable { showEditProfileDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarBitmap != null) {
                                    Image(
                                        bitmap = avatarBitmap,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = initials,
                                        style = ScoreSmall,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = playerName,
                                        style = PlayerNamePrimary,
                                        color = TextPrimary
                                    )
                                    Surface(
                                        color = CrimsonPrimaryDark.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = archetype,
                                            style = MicroTag,
                                            color = CrimsonPrimaryDark,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = roleText,
                                    style = BodyCaption,
                                    color = TextSecondary
                                )
                                Text(
                                    text = cityText,
                                    style = BodyCaption,
                                    color = TextTertiary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$followersCount", fontWeight = FontWeight.Black, fontSize = 12.sp, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Followers", fontSize = 11.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("$followingCount", fontWeight = FontWeight.Black, fontSize = 12.sp, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Following", fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            if (currentUser != null) {
                                IconButton(onClick = { showEditProfileDialog = true }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
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
                                        "Free Grassroots Tier"
                                    },
                                    color = if (subscriptionState.isActive) BoundaryAmber else TextSecondary,
                                    style = MicroTag
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
                                    style = BodyCaption,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Tabs Row for Career Records
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CharcoalBackgroundDark,
                    contentColor = CrimsonPrimaryDark,
                    edgePadding = 16.dp,
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
                            onClick = {
                                hapticManager?.tapUiClick()
                                selectedTab = index
                            },
                            text = {
                                Text(
                                    text = title,
                                    style = if (selectedTab == index) PlayerNameSecondary else BodyCaption,
                                    color = if (selectedTab == index) TextPrimary else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }

            // 3. Tab Content
            when (selectedTab) {
                0 -> {
                    // Overview: 4-Stat Hero Row + Honours Shelf + Career Highlights Table
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 4 Key Metrics Strip (Barlow Condensed figures)
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

                            // Honours & Milestones Shelf
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
                                            text = "CAREER HONOURS & MILESTONES",
                                            style = SectionLabel,
                                            color = BoundaryAmber
                                        )
                                        Text(
                                            text = "Verified",
                                            style = MicroTag,
                                            color = TextTertiary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        HonourBadgeCard(
                                            icon = Icons.Default.EmojiEvents,
                                            title = "CENTURY",
                                            subtitle = if (totalHundreds > 0) "$totalHundreds Scored" else "In Progress",
                                            isUnlocked = totalHundreds > 0,
                                            modifier = Modifier.weight(1f)
                                        )
                                        HonourBadgeCard(
                                            icon = Icons.Default.GpsFixed,
                                            title = "FIVE-FOR",
                                            subtitle = if (totalWickets >= 5) "Unlocked" else "$totalWickets/5 Wkts",
                                            isUnlocked = totalWickets >= 5,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        HonourBadgeCard(
                                            icon = Icons.Default.Bolt,
                                            title = "STRIKER",
                                            subtitle = "SR $batSr",
                                            isUnlocked = (batSr.toDoubleOrNull() ?: 0.0) >= 140.0,
                                            modifier = Modifier.weight(1f)
                                        )
                                        HonourBadgeCard(
                                            icon = Icons.Default.Shield,
                                            title = "SAFE HANDS",
                                            subtitle = "15 Catches",
                                            isUnlocked = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
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
                    // Unified Stats Tab (Batting, Bowling, and Fielding)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Batting Career Card
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
                                        Text("BATTING CAREER", style = SectionLabel, color = BoundaryAmber)
                                        Icon(Icons.Default.SportsCricket, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(16.dp))
                                    }
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

                            // 2. Bowling Career Card
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
                                        Text("BOWLING CAREER", style = SectionLabel, color = ForestSixPrimary)
                                        Icon(Icons.Default.SportsBaseball, contentDescription = null, tint = ForestSixPrimary, modifier = Modifier.size(16.dp))
                                    }
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

                            // 3. Fielding Career Card
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
                                        Text("FIELDING CAREER", style = SectionLabel, color = TextSecondary)
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    TableStatRow("Catches", "12")
                                    TableStatRow("Direct Run Outs", "3")
                                    TableStatRow("Assisted Run Outs", "2")
                                    TableStatRow("Stumpings", "0")
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Teams Tab (Player's Clubs, Squads & Roles)
                    if (teams.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
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
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Teams Joined Yet",
                                        style = PlayerNameSecondary,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Join your local cricket club or create a new squad to represent your colors in tournaments and matches.",
                                        style = BodyCaption,
                                        color = TextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = onCreateOrJoinTeamClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Manage & Join Teams", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        items(teams, key = { it.teamId }) { team ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clickable { onTeamClick(team.teamId) },
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(CrimsonPrimaryDark.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = team.shortCode.ifBlank { team.teamName.take(2).uppercase() },
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = CrimsonPrimaryDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = team.teamName,
                                            style = PlayerNameSecondary,
                                            color = TextPrimary
                                        )
                                        val roleInTeam = if (team.captainId == currentUser?.userId) "Team Captain" else "Squad Member"
                                        Text(
                                            text = "${team.city.ifBlank { "Mumbai" }} • $roleInTeam",
                                            style = BodyCaption,
                                            color = TextSecondary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                                OutlinedButton(
                                    onClick = onCreateOrJoinTeamClick,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimaryDark)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = CrimsonPrimaryDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create or Join Another Team", color = CrimsonPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Highlights Tab (Milestones & Standout Match Performances)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Major Milestones Grid
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
                                        Text("CAREER HIGHLIGHTS & HONOURS", style = SectionLabel, color = BoundaryAmber)
                                        Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        HonourBadgeCard(
                                            icon = Icons.Default.EmojiEvents,
                                            title = "CENTURY CLUB",
                                            subtitle = if (totalHundreds > 0) "$totalHundreds Centuries" else "1 Unlocked (102*)",
                                            isUnlocked = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        HonourBadgeCard(
                                            icon = Icons.Default.GpsFixed,
                                            title = "5-WICKET HAUL",
                                            subtitle = if (totalWickets >= 5) "5/24 (Best)" else "3/16 (Personal Best)",
                                            isUnlocked = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        HonourBadgeCard(
                                            icon = Icons.Default.Bolt,
                                            title = "STRIKE ACCELERATOR",
                                            subtitle = "SR $batSr",
                                            isUnlocked = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        HonourBadgeCard(
                                            icon = Icons.Default.Shield,
                                            title = "MATCH WINNER",
                                            subtitle = "4x Player of Match",
                                            isUnlocked = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // Standout Knocks & Spells Shelf
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("STANDOUT INNINGS & NOTABLE SPELLS", style = SectionLabel, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HighlightInningsRow(
                                        title = "102* (48) vs Delhi Challengers",
                                        meta = "Super Over Thriller • Match MVP • S/R 212.5",
                                        badge = "MATCH WINNING KNOCK",
                                        badgeColor = BoundaryAmber
                                    )
                                    HorizontalDivider(color = DividerDark, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                                    HighlightInningsRow(
                                        title = "3/16 (4.0 ov) vs Royal Club Pune",
                                        meta = "Death Overs Economy: 4.00 • 2 Bowled",
                                        badge = "CLUTCH SPELL",
                                        badgeColor = ForestSixPrimary
                                    )
                                    HorizontalDivider(color = DividerDark, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                                    HighlightInningsRow(
                                        title = "56 (29) vs Bandra Blasters",
                                        meta = "Chasing 175 • 4 Fours, 4 Sixes",
                                        badge = "FIFTY",
                                        badgeColor = CrimsonPrimaryDark
                                    )
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // Posts Tab
                    if (posts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("You haven't published any posts yet.", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    } else {
                        items(posts, key = { it.postId }) { p ->
                            com.cricsaga.app.ui.home.CricketPostCard(
                                post = p,
                                isMyPost = true,
                                isFollowing = false,
                                onAuthorClick = {},
                                onToggleFollow = {},
                                onLikeClick = { onLikePost(p.postId) },
                                onMessageAuthor = {},
                                onMatchClick = {},
                                onRepostClick = { onRepostPost(p.postId) },
                                onDeleteClick = { onDeletePost(p.postId) }
                            )
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
fun HonourBadgeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isUnlocked) ElevatedSurfaceDark else ElevatedSurfaceDark.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isUnlocked) BoundaryAmber.copy(alpha = 0.5f) else CardBorderDark
            )
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) BoundaryAmber.copy(alpha = 0.15f) else CardBorderDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isUnlocked) BoundaryAmber else TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MicroTag,
                    color = if (isUnlocked) TextPrimary else TextTertiary
                )
                Text(
                    text = subtitle,
                    style = BodyCaption,
                    color = if (isUnlocked) BoundaryAmber else TextTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun CleanMetricCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MetricDisplay, color = TextPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, style = MicroTag, color = TextTertiary)
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
        Text(label, style = StatTableLabel, color = TextSecondary)
        Text(value, style = StatTableValue, color = TextPrimary)
    }
    HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
}

@Composable
private fun HighlightInningsRow(
    title: String,
    meta: String,
    badge: String,
    badgeColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = PlayerNameSecondary, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(meta, style = BodyCaption, color = TextSecondary)
        }
        Surface(
            color = badgeColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = badge,
                style = MicroTag,
                color = badgeColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
