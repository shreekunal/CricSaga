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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.PlayerStatsEntity
import com.cricsaga.app.data.local.entity.PostEntity
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
    isFollowing: Boolean = false,
    followersCount: Int = 0,
    followingCount: Int = 0,
    onToggleFollow: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Batting", "Bowling", "Posts")

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
    val totalWickets = stats?.wicketsTaken ?: 0
    val totalBallsBowled = stats?.legalBallsBowled ?: 0
    val totalRunsConceded = stats?.runsConceded ?: 0

    val batAvg = if (totalInnings > 0) String.format(java.util.Locale.US, "%.1f", totalRuns.toDouble() / totalInnings.toDouble()) else "0.0"
    val batSr = if (totalBalls > 0) String.format(java.util.Locale.US, "%.1f", (totalRuns.toDouble() / totalBalls.toDouble()) * 100.0) else "0.0"
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
                            contentDescription = "Share",
                            tint = AmberFourPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Collectible Athlete Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "#$jerseyNum",
                        style = JerseyWatermark,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
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
                                    .background(CrimsonPrimaryDark.copy(alpha = 0.2f))
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
                                        color = MaterialTheme.colorScheme.onSurface
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = cityText,
                                    style = BodyCaption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Social Followers & Following Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$followersCount",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Followers",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "$followingCount",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Following",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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
                                    containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else CrimsonPrimaryDark
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isFollowing) MaterialTheme.colorScheme.onSurface else Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFollowing) "FOLLOWING" else "+ FOLLOW",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isFollowing) MaterialTheme.colorScheme.onSurface else Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    onMessageClick()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MESSAGE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Tabs Row
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = CrimsonPrimaryDark,
                    divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp) }
                ) {
                    tabs.forEachIndexed { index, tabTitle ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                hapticManager?.tapUiClick()
                                selectedTab = index
                            },
                            text = {
                                Text(
                                    text = tabTitle,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Overview
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp)
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
                    }
                }
                1 -> {
                    // Batting Breakdown
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("BATTING RECORD", style = SectionLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                TableStatRow("Innings", "$totalInnings")
                                TableStatRow("Total Runs", "$totalRuns")
                                TableStatRow("Balls Faced", "$totalBalls")
                                TableStatRow("Batting Average", batAvg)
                                TableStatRow("Strike Rate", batSr)
                            }
                        }
                    }
                }
                2 -> {
                    // Bowling Breakdown
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("BOWLING RECORD", style = SectionLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                TableStatRow("Overs Bowled", "${totalBallsBowled / 6}.${totalBallsBowled % 6} ov")
                                TableStatRow("Wickets", "$totalWickets")
                                TableStatRow("Economy", bowlEcon)
                                TableStatRow("Bowling Average", bowlAvg)
                            }
                        }
                    }
                }
                3 -> {
                    // Posts Tab
                    if (posts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No posts published yet by ${user.fullName}.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(posts, key = { it.postId }) { p ->
                            CricketPostCard(
                                post = p,
                                isMyPost = false,
                                isFollowing = isFollowing,
                                onAuthorClick = {},
                                onToggleFollow = onToggleFollow,
                                onLikeClick = {},
                                onMessageAuthor = onMessageClick,
                                onMatchClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
