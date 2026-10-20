package com.cricsaga.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
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
    val equationText: String,
    val isLive: Boolean = true
)

data class GrassrootsHeroItem(
    val title: String,
    val playerName: String,
    val teamName: String,
    val statText: String,
    val badge: String
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
            crrText = "CRR: 8.7",
            equationText = "Tigers need 28 runs in 22 balls"
        ),
        LiveRadarItem(
            matchId = "live_2",
            teamA = "Shivaji Park XI",
            teamB = "Metro Strikers",
            scoreText = "86/2",
            oversText = "9.4 ov",
            crrText = "CRR: 8.9",
            equationText = "1st Innings • 15 Overs Match"
        )
    )

    val grassrootsHeroes = listOf(
        GrassrootsHeroItem(
            title = "CENTURY MAKER",
            playerName = "Rohit Verma",
            teamName = "Warriors CC",
            statText = "104* (48) • 8x4, 7x6 • SR: 216.6",
            badge = "100"
        ),
        GrassrootsHeroItem(
            title = "FIVE-WICKET HAUL",
            playerName = "Jasprit Khan",
            teamName = "Royal Tigers",
            statText = "5/14 (4.0) • Econ: 3.50",
            badge = "5W"
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
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CRIC",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 19.sp,
                                    color = CrimsonPrimaryDark
                                )
                                Text(
                                    text = "SAGA",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 19.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Har Run Ki Kahani",
                                fontSize = 10.sp,
                                color = MutedGrey,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        onClick = onProBadgeClick,
                        shape = RoundedCornerShape(20.dp),
                        color = if (isPro) Color(0xFF33290D) else ElevatedSurfaceDark,
                        border = if (isPro) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BoundaryAmber)) else null,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isPro) BoundaryAmber else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPro) "PRO GOLD" else "UPGRADE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPro) BoundaryAmber else Color.White
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
            // 1. Live Match Radar Section
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(DismissalRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE MATCH RADAR",
                            color = DismissalRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "${demoRadarMatches.size} Active",
                        color = MutedGrey,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(demoRadarMatches) { radar ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .clickable { onMatchClick(radar.matchId) },
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF333333)))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${radar.teamA} vs ${radar.teamB}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Surface(
                                        color = Color(0x33FF5252),
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

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = radar.scoreText,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${radar.oversText})",
                                        fontSize = 13.sp,
                                        color = MutedGrey,
                                        modifier = Modifier.padding(bottom = 3.dp)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = radar.crrText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BoundaryAmber,
                                        modifier = Modifier.padding(bottom = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = radar.equationText,
                                    color = BoundaryGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 2. Scorer Launchpad (Quick Actions)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SCORER LAUNCHPAD",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onStartMatchClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START NEW MATCH",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onManageTeamsClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("My Teams", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onExploreTournamentsClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tournaments", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. Minimal Sponsor Ad Banner (Shown ONLY for Free Tier, Hidden for Pro)
            if (!isPro) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ElevatedSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2C2C2C)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Shivaji Turf Ground • 15% off night box matches. Code: CRIC15",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Heroes of the Grassroots Highlights
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HEROES OF THE WEEK",
                        color = BoundaryAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                grassrootsHeroes.forEach { hero ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B1D1D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = hero.badge,
                                    color = CrimsonPrimaryDark,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = hero.playerName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${hero.teamName} • ${hero.title}",
                                    color = MutedGrey,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = hero.statText,
                                    color = BoundaryGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 5. Recent Matches Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "RECENT MATCHES",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (matches.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No matches recorded yet. Tap '+ START NEW MATCH' above!",
                                color = MutedGrey,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    matches.take(3).forEach { match ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable { onMatchClick(match.matchId) },
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = match.matchTitle,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${match.totalOvers} Overs • ${match.ballType.name.replace('_', ' ')}",
                                        color = MutedGrey,
                                        fontSize = 11.sp
                                    )
                                }
                                Surface(
                                    color = if (match.matchStatus == MatchStatus.LIVE) Color(0x33FF5252) else Color(0x334CAF50),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = match.matchStatus.name,
                                        color = if (match.matchStatus == MatchStatus.LIVE) DismissalRed else BoundaryGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
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
