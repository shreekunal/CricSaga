package com.cricsaga.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.R
import com.cricsaga.app.data.local.entity.MatchEntity
import com.cricsaga.app.domain.model.MatchStatus
import com.cricsaga.app.ui.theme.*

data class LiveRadarItem(
    val matchId: String,
    val teamA: String,
    val teamB: String,
    val scoreText: String,
    val oversText: String,
    val crrText: String,
    val equationText: String
)

data class GrassrootsHeroItem(
    val playerName: String,
    val initials: String,
    val teamName: String,
    val performance: String,
    val context: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isPro: Boolean,
    matches: List<MatchEntity>,
    onStartMatchClick: () -> Unit,
    onManageTeamsClick: () -> Unit,
    onExploreTournamentsClick: () -> Unit,
    onProBadgeClick: () -> Unit,
    onMatchClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    val demoRadarMatches = listOf(
        LiveRadarItem(
            matchId = "live_1",
            teamA = "Warriors CC",
            teamB = "Royal Tigers",
            scoreText = "142/4",
            oversText = "16.2 ov",
            crrText = "CRR 8.7",
            equationText = "Tigers need 28 runs in 22 balls"
        ),
        LiveRadarItem(
            matchId = "live_2",
            teamA = "Shivaji Park XI",
            teamB = "Metro Strikers",
            scoreText = "86/2",
            oversText = "9.4 ov",
            crrText = "CRR 8.9",
            equationText = "1st Innings • 15 Overs Match"
        )
    )

    val topPerformers = listOf(
        GrassrootsHeroItem(
            playerName = "Rohit Verma",
            initials = "RV",
            teamName = "Warriors CC",
            performance = "104* (48) • 8x4, 7x6",
            context = "vs Royal Tigers • League Stage"
        ),
        GrassrootsHeroItem(
            playerName = "Jasprit Khan",
            initials = "JK",
            teamName = "Royal Tigers",
            performance = "5/14 (4.0) • Econ 3.50",
            context = "vs Metro Strikers • Semi-Final"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CricSaga",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                },
                actions = {
                    Surface(
                        onClick = onProBadgeClick,
                        shape = RoundedCornerShape(6.dp),
                        color = if (isPro) Color(0xFF231F14) else ElevatedSurfaceDark,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (isPro) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = BoundaryAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isPro) "PRO" else "UPGRADE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPro) BoundaryAmber else TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live Matches Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE MATCHES",
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${demoRadarMatches.size} Active",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(demoRadarMatches) { radar ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .clickable { onMatchClick(radar.matchId) },
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
                                        text = "${radar.teamA} vs ${radar.teamB}",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Surface(
                                        color = Color(0x22DC2626),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "LIVE",
                                            color = DismissalRed,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = radar.scoreText,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${radar.oversText})",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = radar.crrText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = radar.equationText,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. Action Strip
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Button(
                    onClick = onStartMatchClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Start New Match",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onManageTeamsClick,
                        modifier = Modifier.weight(1f),
                        color = ElevatedSurfaceDark,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Teams", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Surface(
                        onClick = onExploreTournamentsClick,
                        modifier = Modifier.weight(1f),
                        color = ElevatedSurfaceDark,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tournaments", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // 3. Top Performers (Clean Sports Card)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "TOP PERFORMERS",
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        topPerformers.forEachIndexed { index, hero ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(ElevatedSurfaceDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = hero.initials,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = hero.playerName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = hero.context,
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = hero.performance,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (index < topPerformers.size - 1) {
                                HorizontalDivider(color = DividerDark, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            // 4. Recent Matches
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "RECENT MATCHES",
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (matches.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No matches scored yet.",
                                color = TextTertiary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            matches.take(3).forEachIndexed { index, match ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onMatchClick(match.matchId) }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = match.matchTitle,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${match.totalOvers} Overs • ${match.ballType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }}",
                                            color = TextTertiary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Surface(
                                        color = if (match.matchStatus == MatchStatus.LIVE) Color(0x22DC2626) else ElevatedSurfaceDark,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = match.matchStatus.name,
                                            color = if (match.matchStatus == MatchStatus.LIVE) DismissalRed else TextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (index < matches.take(3).size - 1) {
                                    HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }

            // 5. Discreet Minimal Sponsor (Free tier only)
            if (!isPro) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AD",
                        color = TextTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Shivaji Turf Grounds • Night box cricket bookings available",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
