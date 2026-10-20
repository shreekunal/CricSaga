package com.cricsaga.app.ui.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.TeamEntity
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    team: TeamEntity,
    squad: List<UserEntity>,
    onAddPlayerClick: () -> Unit,
    onPlayerClick: (UserEntity) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = team.teamName,
                        style = PlayerNamePrimary,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPlayerClick,
                containerColor = CrimsonPrimaryDark,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Squad Member")
            }
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
            // Team Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(ElevatedSurfaceDark)
                                    .border(1.5.dp, BoundaryAmber, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = team.shortCode.ifEmpty { "TM" },
                                    style = ScoreSmall,
                                    color = BoundaryAmber
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = team.teamName,
                                    style = PlayerNamePrimary,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${team.city} • Home Turf",
                                    style = BodyCaption,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Team Record Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            TeamStatCol("PLAYED", "14")
                            TeamStatCol("WON", "10")
                            TeamStatCol("LOST", "4")
                            TeamStatCol("WIN %", "71.4%")
                        }
                    }
                }
            }

            // Squad Roster Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SQUAD ROSTER",
                        style = SectionLabel,
                        color = TextSecondary
                    )
                    Text(
                        text = "${squad.size} Players",
                        style = MicroTag,
                        color = TextTertiary
                    )
                }
            }

            // Squad Player List
            if (squad.isEmpty()) {
                item {
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
                                text = "No players added yet. Tap '+' to build your 15-player roster.",
                                style = BodyCaption,
                                color = TextTertiary
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(squad) { index, player ->
                    Card(
                        onClick = { onPlayerClick(player) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Player initials avatar
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElevatedSurfaceDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = player.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                    style = MicroTag,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = player.fullName,
                                        style = PlayerNameSecondary,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Captain / VC / WK Badges
                                    if (index == 0) {
                                        SquadRoleBadge("C", CrimsonPrimaryDark)
                                    } else if (index == 1) {
                                        SquadRoleBadge("VC", BoundaryAmber)
                                    } else if (player.playingRole == PlayingRole.WICKET_KEEPER) {
                                        SquadRoleBadge("WK", ExtraBlue)
                                    }
                                }
                                Text(
                                    text = player.playingRole.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() } + " • #${player.jerseyNumber}",
                                    style = BodyCaption,
                                    color = TextTertiary
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
private fun SquadRoleBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = label,
            style = MicroTag,
            color = color,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun TeamStatCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = ScoreSmall, color = TextPrimary)
        Text(text = label, style = MicroTag, color = TextTertiary)
    }
}
