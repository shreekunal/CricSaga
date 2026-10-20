package com.cricsaga.app.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.ImageUtils
import com.cricsaga.app.ui.utils.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    user: UserEntity,
    stats: PlayerStatsEntity?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
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
                        color = TextPrimary
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
                            tint = TextPrimary
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
                            tint = BoundaryAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
            )
        },
        containerColor = CharcoalBackgroundDark
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
                        .background(SlateSurfaceDark)
                        .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                ) {
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
                                        color = TextPrimary
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
                            }
                        }
                    }
                }
            }

            // 4 Key Stats Quad-Strip
            item {
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
            }

            // Batting Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("BATTING RECORD", style = SectionLabel, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        TableStatRow("Innings", "$totalInnings")
                        TableStatRow("Total Runs", "$totalRuns")
                        TableStatRow("Balls Faced", "$totalBalls")
                        TableStatRow("Batting Average", batAvg)
                        TableStatRow("Strike Rate", batSr)
                    }
                }
            }

            // Bowling Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("BOWLING RECORD", style = SectionLabel, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        TableStatRow("Overs Bowled", "${totalBallsBowled / 6}.${totalBallsBowled % 6} ov")
                        TableStatRow("Wickets", "$totalWickets")
                        TableStatRow("Economy", bowlEcon)
                        TableStatRow("Bowling Average", bowlAvg)
                    }
                }
            }
        }
    }
}
