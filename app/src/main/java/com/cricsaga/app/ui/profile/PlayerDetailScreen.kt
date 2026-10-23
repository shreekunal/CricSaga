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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.cricsaga.app.data.local.entity.PlayerStatsEntity
import com.cricsaga.app.data.local.entity.PostEntity
import com.cricsaga.app.data.local.entity.TeamEntity
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.ui.home.CricketPostCard
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.ImageUtils
import com.cricsaga.app.ui.utils.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    user: UserEntity,
    stats: PlayerStatsEntity?,
    posts: List<PostEntity> = emptyList(),
    teams: List<TeamEntity> = emptyList(),
    isFollowing: Boolean = false,
    followersCount: Int = 0,
    followingCount: Int = 0,
    onToggleFollow: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onTeamClick: (String) -> Unit = {},
    onLikePost: (String) -> Unit = {},
    onRepostPost: (String) -> Unit = {},
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Stats", "Teams", "Highlights", "Posts")

    val playerName = user.fullName
    val initials = playerName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifEmpty { "CR" }

    val roleFormatted = user.playingRole.name.replace('_', ' ').lowercase()
        .replaceFirstChar { it.uppercase() }
    val bowlingFormatted = user.bowlingStyle.name.replace('_', ' ').lowercase()
        .replaceFirstChar { it.uppercase() }
    val roleText = "$roleFormatted • $bowlingFormatted"
    val jerseyNum = user.jerseyNumber.ifBlank { "18" }
    val cityText = "${user.city.ifBlank { "Grassroots Player" }} • Jersey #$jerseyNum"

    val totalMatches = stats?.matches ?: 0
    val totalInnings = stats?.innings ?: 0
    val totalRuns = stats?.runsScored ?: 0
    val totalBalls = stats?.ballsFaced ?: 0
    val totalFifties = stats?.fifties ?: 0
    val totalHundreds = stats?.hundreds ?: 0
    val totalFours = stats?.fours ?: 0
    val totalSixes = stats?.sixes ?: 0
    val highestScore = stats?.highestScore ?: 0
    val totalWickets = stats?.wicketsTaken ?: 0
    val totalBallsBowled = stats?.legalBallsBowled ?: 0
    val totalRunsConceded = stats?.runsConceded ?: 0
    val totalMaidens = stats?.maidens ?: 0

    val batAvg = if (totalInnings > 0) String.format(java.util.Locale.US, "%.1f", totalRuns.toDouble() / totalInnings.toDouble()) else "0.0"
    val batSr = if (totalBalls > 0) String.format(java.util.Locale.US, "%.1f", (totalRuns.toDouble() / totalBalls.toDouble()) * 100.0) else "0.0"
    val oversBowledFormatted = "${totalBallsBowled / 6}.${totalBallsBowled % 6}"
    val bowlEcon = if (totalBallsBowled > 0) String.format(java.util.Locale.US, "%.2f", totalRunsConceded.toDouble() / (totalBallsBowled.toDouble() / 6.0)) else "0.0"
    val bowlAvg = if (totalWickets > 0) String.format(java.util.Locale.US, "%.2f", totalRunsConceded.toDouble() / totalWickets.toDouble()) else "0.0"

    val archetype = when {
        (batSr.toDoubleOrNull() ?: 0.0) >= 140.0 -> "POWER HITTER"
        user.playingRole == PlayingRole.BOWLER -> "STRIKE BOWLER"
        user.playingRole == PlayingRole.WICKET_KEEPER -> "WK ANCHOR"
        else -> "ALL-ROUNDER"
    }

    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Player Profile",
                        style = PlayerNamePrimary,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        onBackClick()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        ShareUtils.generateAndShareAthleteCard(
                            context = context,
                            playerName = playerName,
                            jerseyNumber = jerseyNum,
                            archetype = archetype,
                            city = user.city.ifBlank { "Mumbai" },
                            matches = totalMatches,
                            runs = totalRuns,
                            wickets = totalWickets,
                            average = batAvg,
                            profilePhotoPath = user.profilePhotoUrl
                        )
                    }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Athlete Card",
                            tint = BoundaryAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = CharcoalBackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
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
                    // Oversized Jersey Watermark
                    Text(
                        text = "#$jerseyNum",
                        style = JerseyWatermark,
                        color = TextPrimary.copy(alpha = 0.05f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 16.dp, y = 24.dp)
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val avatarBitmap = remember(user.profilePhotoUrl) {
                                ImageUtils.loadBitmapFromPath(user.profilePhotoUrl)
                            }

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(ElevatedSurfaceDark)
                                    .border(1.5.dp, CrimsonPrimaryDark.copy(alpha = 0.6f), CircleShape),
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
                                        color = CrimsonPrimaryDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = playerName,
                                        style = PlayerNamePrimary,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
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
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons: Follow & Message
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    onToggleFollow()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) ElevatedSurfaceDark else CrimsonPrimaryDark
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isFollowing) TextPrimary else Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFollowing) "FOLLOWING" else "+ FOLLOW",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isFollowing) TextPrimary else Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    onMessageClick()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ElevatedSurfaceDark),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MESSAGE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
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
                    }
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
                                            isUnlocked = (batSr.toDoubleOrNull() ?: 0.0) >= 120.0 && totalRuns >= 50,
                                            modifier = Modifier.weight(1f)
                                        )
                                        HonourBadgeCard(
                                            icon = Icons.Default.Shield,
                                            title = "SAFE HANDS",
                                            subtitle = "0 Catches",
                                            isUnlocked = false,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // Career Breakdown Table
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    TableStatRow("Strike Rate", batSr)
                                    TableStatRow("Highest Score", if (highestScore > 0) "$highestScore*" else "-")
                                    TableStatRow("Bowling Economy", if (totalBallsBowled > 0) bowlEcon else "-")
                                    TableStatRow("Best Bowling", "-")
                                    TableStatRow("Fifties / Hundreds", "$totalFifties / $totalHundreds")
                                    TableStatRow("Catches / Run Outs", "0 / 0")
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Unified Stats Tab (Batting + Bowling + Fielding)
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
                                    TableStatRow("Not Outs (NO)", "0")
                                    TableStatRow("Total Runs", "$totalRuns")
                                    TableStatRow("Balls Faced", "$totalBalls")
                                    TableStatRow("Highest Score", if (highestScore > 0) "$highestScore*" else "-")
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
                                    TableStatRow("Best Bowling", "-")
                                    TableStatRow("Bowling Average", bowlAvg)
                                    TableStatRow("Economy Rate", if (totalBallsBowled > 0) bowlEcon else "-")
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
                                    TableStatRow("Catches", "0")
                                    TableStatRow("Direct Run Outs", "0")
                                    TableStatRow("Assisted Run Outs", "0")
                                    TableStatRow("Stumpings", "0")
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Teams Tab (Player's Clubs & Squads)
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
                                        text = "This cricketer has not listed any public team affiliations yet.",
                                        style = BodyCaption,
                                        color = TextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
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
                                        val roleInTeam = if (team.captainId == user.userId) "Team Captain" else "Squad Member"
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
                                            subtitle = if (totalHundreds > 0) "$totalHundreds Centuries" else "0 Centuries",
                                            isUnlocked = totalHundreds > 0,
                                            modifier = Modifier.weight(1f)
                                        )
                                        HonourBadgeCard(
                                            icon = Icons.Default.GpsFixed,
                                            title = "5-WICKET HAUL",
                                            subtitle = if (totalWickets >= 5) "5+ Wkts" else "$totalWickets/5 Wkts",
                                            isUnlocked = totalWickets >= 5,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        HonourBadgeCard(
                                            icon = Icons.Default.Bolt,
                                            title = "STRIKE ACCELERATOR",
                                            subtitle = "SR $batSr",
                                            isUnlocked = (batSr.toDoubleOrNull() ?: 0.0) >= 120.0 && totalRuns >= 50,
                                            modifier = Modifier.weight(1f)
                                        )
                                        HonourBadgeCard(
                                            icon = Icons.Default.Shield,
                                            title = "SAFE HANDS",
                                            subtitle = "0 Catches",
                                            isUnlocked = false,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // Match Knocks & Spells
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("STANDOUT INNINGS", style = SectionLabel, color = BoundaryAmber)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No standout knocks or spells recorded yet.\nMilestone performances from scored matches will appear here automatically.",
                                            style = BodyCaption,
                                            color = TextSecondary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // Posts Tab
                    if (posts.isEmpty()) {
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
                                        imageVector = Icons.Default.Article,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Posts Published Yet",
                                        style = PlayerNameSecondary,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Updates, milestone reports, and photos from this player will appear here.",
                                        style = BodyCaption,
                                        color = TextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(posts, key = { it.postId }) { post ->
                            CricketPostCard(
                                post = post,
                                isMyPost = false,
                                isFollowing = isFollowing,
                                onAuthorClick = {},
                                onToggleFollow = onToggleFollow,
                                onLikeClick = { onLikePost(post.postId) },
                                onMessageAuthor = onMessageClick,
                                onMatchClick = {},
                                onRepostClick = { onRepostPost(post.postId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInningsRow(
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
