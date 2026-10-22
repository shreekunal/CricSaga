package com.cricsaga.app.ui.rankings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.domain.model.*
import com.cricsaga.app.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingsScreen(
    topBatters: List<PlayerRankingItem>,
    topBowlers: List<PlayerRankingItem>,
    topAllRounders: List<PlayerRankingItem>,
    teamRankings: List<TeamRankingItem>,
    availableCities: List<String>,
    selectedCity: String,
    onSelectCity: (String) -> Unit,
    onPlayerClick: (String) -> Unit,
    onTeamClick: (String) -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = listOf("Top Batters", "Top Bowlers", "MVP All-Rounders", "Team Rankings")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = AmberFourPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rankings & Leaderboards",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Grassroots Cricket Standings & Regional Talent",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Regional City Filter Chip Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableCities) { city ->
                    val isSelected = selectedCity.equals(city, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCity(city) },
                        label = {
                            Text(
                                text = city,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = CrimsonPrimaryDark
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonPrimaryDark.copy(alpha = 0.15f),
                            selectedLabelColor = CrimsonPrimaryDark,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) CrimsonPrimaryDark else Color.Transparent,
                            borderWidth = 1.dp,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Category Tabs
            TabRow(
                selectedTabIndex = selectedCategoryIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = CrimsonPrimaryDark,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)) }
            ) {
                categories.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedCategoryIndex == index,
                        onClick = { selectedCategoryIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategoryIndex == index) CrimsonPrimaryDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            // Main Content Area based on Selected Category
            when (selectedCategoryIndex) {
                0 -> BattersLeaderboard(topBatters, onPlayerClick)
                1 -> BowlersLeaderboard(topBowlers, onPlayerClick)
                2 -> AllRoundersLeaderboard(topAllRounders, onPlayerClick)
                3 -> TeamStandingsLeaderboard(teamRankings, onTeamClick)
            }
        }
    }
}

@Composable
private fun BattersLeaderboard(
    players: List<PlayerRankingItem>,
    onPlayerClick: (String) -> Unit
) {
    if (players.isEmpty()) {
        EmptyLeaderboardPlaceholder("No ranked batters found in this region.")
        return
    }

    val top3 = players.take(3)
    val rest = players.drop(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ChampionshipPodium(
                top3 = top3,
                primaryStatLabel = "RUNS",
                onPlayerClick = onPlayerClick
            )
        }

        if (rest.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "STANDINGS (#4 - #${players.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
            items(rest) { player ->
                PlayerRankingCard(
                    player = player,
                    primaryStatLabel = "RUNS",
                    primaryStatValue = player.runs.toString(),
                    statLine1 = "Avg: ${String.format(Locale.US, "%.1f", player.battingAverage)} • SR: ${String.format(Locale.US, "%.1f", player.strikeRate)}",
                    statLine2 = "HS: ${player.highestScore} • 50s: ${player.fifties} • 100s: ${player.hundreds}",
                    accentColor = MaterialTheme.colorScheme.outlineVariant,
                    onPlayerClick = { onPlayerClick(player.userId) }
                )
            }
        }
    }
}

@Composable
private fun BowlersLeaderboard(
    players: List<PlayerRankingItem>,
    onPlayerClick: (String) -> Unit
) {
    if (players.isEmpty()) {
        EmptyLeaderboardPlaceholder("No ranked bowlers found in this region.")
        return
    }

    val top3 = players.take(3)
    val rest = players.drop(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ChampionshipPodium(
                top3 = top3,
                primaryStatLabel = "WICKETS",
                onPlayerClick = onPlayerClick
            )
        }

        if (rest.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "STANDINGS (#4 - #${players.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
            items(rest) { player ->
                PlayerRankingCard(
                    player = player,
                    primaryStatLabel = "WICKETS",
                    primaryStatValue = player.wickets.toString(),
                    statLine1 = "Econ: ${String.format(Locale.US, "%.2f", player.bowlingEconomy)} • Overs: ${String.format(Locale.US, "%.1f", player.oversBowled)}",
                    statLine2 = "Maidens: ${player.maidens} • Runs Conceded: ${player.runsConceded}",
                    accentColor = MaterialTheme.colorScheme.outlineVariant,
                    onPlayerClick = { onPlayerClick(player.userId) }
                )
            }
        }
    }
}

@Composable
private fun AllRoundersLeaderboard(
    players: List<PlayerRankingItem>,
    onPlayerClick: (String) -> Unit
) {
    if (players.isEmpty()) {
        EmptyLeaderboardPlaceholder("No ranked all-rounders found in this region.")
        return
    }

    val top3 = players.take(3)
    val rest = players.drop(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ChampionshipPodium(
                top3 = top3,
                primaryStatLabel = "MVP PTS",
                onPlayerClick = onPlayerClick
            )
        }

        if (rest.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "STANDINGS (#4 - #${players.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
            items(rest) { player ->
                PlayerRankingCard(
                    player = player,
                    primaryStatLabel = "MVP PTS",
                    primaryStatValue = player.mvpPoints.toInt().toString(),
                    statLine1 = "${player.runs} Runs • ${player.wickets} Wickets",
                    statLine2 = "SR: ${String.format(Locale.US, "%.1f", player.strikeRate)} • Econ: ${String.format(Locale.US, "%.2f", player.bowlingEconomy)}",
                    accentColor = MaterialTheme.colorScheme.outlineVariant,
                    onPlayerClick = { onPlayerClick(player.userId) }
                )
            }
        }
    }
}

@Composable
private fun TeamStandingsLeaderboard(
    teams: List<TeamRankingItem>,
    onTeamClick: (String) -> Unit
) {
    if (teams.isEmpty()) {
        EmptyLeaderboardPlaceholder("No ranked teams found in this region.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(teams) { team ->
            TeamRankingCard(team = team, onTeamClick = { onTeamClick(team.teamId) })
        }
    }
}

@Composable
private fun PlayerRankingCard(
    player: PlayerRankingItem,
    primaryStatLabel: String,
    primaryStatValue: String,
    statLine1: String,
    statLine2: String,
    accentColor: Color,
    onPlayerClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayerClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (player.rank <= 3) 1.5.dp else 0.5.dp,
            color = if (player.rank <= 3) accentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number Badge
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        color = when (player.rank) {
                            1 -> Color(0xFFD97706).copy(alpha = 0.2f)
                            2 -> Color(0xFF94A3B8).copy(alpha = 0.2f)
                            3 -> Color(0xFFB45309).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    )
                    .border(
                        width = 0.75.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${player.rank}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = when (player.rank) {
                        1 -> Color(0xFFD97706)
                        2 -> Color(0xFF94A3B8)
                        3 -> Color(0xFFB45309)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Player Avatar & Information with high-contrast tinted container
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = CrimsonPrimaryDark.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = CrimsonPrimaryDark.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.initials,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = CrimsonPrimaryDark
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.playerName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (player.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Player",
                            tint = AmberFourPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Text(
                    text = "${player.role} • ${player.city}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Text(
                    text = statLine1,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = statLine2,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Primary Key Metric Pill
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Surface(
                    color = when (player.rank) {
                        1 -> Color(0xFFD97706).copy(alpha = 0.15f)
                        2 -> Color(0xFF94A3B8).copy(alpha = 0.15f)
                        3 -> Color(0xFFB45309).copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = primaryStatValue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = when (player.rank) {
                                1 -> Color(0xFFD97706)
                                2 -> Color(0xFF94A3B8)
                                3 -> Color(0xFFB45309)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Text(
                            text = primaryStatLabel,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${player.city} • ${player.matches}m",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TeamRankingCard(
    team: TeamRankingItem,
    onTeamClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTeamClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (team.rank <= 3) 1.5.dp else 0.5.dp,
            color = when (team.rank) {
                1 -> Color(0xFFD97706).copy(alpha = 0.8f)
                2 -> Color(0xFF94A3B8).copy(alpha = 0.8f)
                3 -> Color(0xFFB45309).copy(alpha = 0.8f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank Badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            color = when (team.rank) {
                                1 -> Color(0xFFD97706).copy(alpha = 0.2f)
                                2 -> Color(0xFF94A3B8).copy(alpha = 0.2f)
                                3 -> Color(0xFFB45309).copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${team.rank}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = when (team.rank) {
                            1 -> Color(0xFFD97706)
                            2 -> Color(0xFF94A3B8)
                            3 -> Color(0xFFB45309)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Team Shield Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(CrimsonPrimaryDark, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = team.shortCode.take(3),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = team.teamName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${team.city} • Grassroots Club",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Points & Win Rate Box
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${team.points} PTS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = CrimsonPrimaryDark
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.0f", team.winPercentage)}% Win Rate",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Matches Played / Won / Lost and Form Guide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "P: ${team.matchesPlayed}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "W: ${team.wins}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestSixPrimary)
                    Text(text = "L: ${team.losses}", fontSize = 11.sp, color = DismissalRed)
                    Text(text = "T: ${team.ties}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Form Guide Pills
                if (team.recentForm.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "FORM:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        team.recentForm.forEach { outcome ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = when (outcome) {
                                            MatchOutcome.WIN -> ForestSixPrimary
                                            MatchOutcome.LOSS -> DismissalRed
                                            MatchOutcome.TIE -> AmberFourPrimary
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (outcome) {
                                        MatchOutcome.WIN -> "W"
                                        MatchOutcome.LOSS -> "L"
                                        MatchOutcome.TIE -> "T"
                                    },
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLeaderboardPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChampionshipPodium(
    top3: List<PlayerRankingItem>,
    primaryStatLabel: String,
    onPlayerClick: (String) -> Unit
) {
    if (top3.isEmpty()) return

    val first = top3.getOrNull(0)
    val second = top3.getOrNull(1)
    val third = top3.getOrNull(2)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            // #2 Silver (Left)
            if (second != null) {
                PodiumPlayerCard(
                    player = second,
                    rank = 2,
                    accentColor = Color(0xFF94A3B8),
                    primaryStatLabel = primaryStatLabel,
                    modifier = Modifier.weight(1f),
                    onPlayerClick = { onPlayerClick(second.userId) }
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // #1 Gold Champion (Center)
            if (first != null) {
                PodiumPlayerCard(
                    player = first,
                    rank = 1,
                    accentColor = Color(0xFFFFB800),
                    primaryStatLabel = primaryStatLabel,
                    isChampion = true,
                    modifier = Modifier.weight(1.15f),
                    onPlayerClick = { onPlayerClick(first.userId) }
                )
            }

            // #3 Bronze (Right)
            if (third != null) {
                PodiumPlayerCard(
                    player = third,
                    rank = 3,
                    accentColor = Color(0xFFD97706),
                    primaryStatLabel = primaryStatLabel,
                    modifier = Modifier.weight(1f),
                    onPlayerClick = { onPlayerClick(third.userId) }
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PodiumPlayerCard(
    player: PlayerRankingItem,
    rank: Int,
    accentColor: Color,
    primaryStatLabel: String,
    modifier: Modifier = Modifier,
    isChampion: Boolean = false,
    onPlayerClick: () -> Unit
) {
    val statValue = when (primaryStatLabel) {
        "RUNS" -> "${player.runs}"
        "WICKETS" -> "${player.wickets}"
        "MVP PTS" -> "${player.mvpPoints.toInt()}"
        else -> "${player.runs}"
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onPlayerClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Champion Crown Indicator
        Box(
            modifier = Modifier.height(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isChampion) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = "Champion",
                    tint = Color(0xFFFFB800),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Avatar with Rank Badge overlay
        Box(contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier
                    .size(if (isChampion) 54.dp else 46.dp)
                    .background(accentColor.copy(alpha = 0.12f), CircleShape)
                    .border(
                        width = if (isChampion) 2.dp else 1.5.dp,
                        color = accentColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.initials,
                    fontSize = if (isChampion) 16.sp else 13.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            }

            // Rank Number Pill
            Surface(
                color = accentColor,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.offset(y = 6.dp)
            ) {
                Text(
                    text = "#$rank",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Player Name
        Text(
            text = player.playerName,
            fontSize = if (isChampion) 12.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        // Player City
        Text(
            text = player.city.ifBlank { "Mumbai" },
            fontSize = 9.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Primary Stat Capsule Pill
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = accentColor.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(0.75.dp, accentColor.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statValue,
                    fontSize = if (isChampion) 12.sp else 11.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = primaryStatLabel,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
