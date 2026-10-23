package com.cricsaga.app.ui.teams

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.TeamEntity
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.ui.teams.dialog.AddPlayerToTeamDialog
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    team: TeamEntity,
    squad: List<UserEntity>,
    allUsers: List<UserEntity> = emptyList(),
    teamStats: com.cricsaga.app.data.repository.TeamStatsSummary? = null,
    onAddExistingPlayer: (userId: String) -> Unit = {},
    onCreateAndAddNewPlayer: (fullName: String, email: String, role: PlayingRole, jerseyNumber: String) -> Unit = { _, _, _, _ -> },
    onAddPlayer: (name: String, jersey: String, role: PlayingRole) -> Unit = { _, _, _ -> },
    onRemovePlayer: (UserEntity) -> Unit = {},
    onSetCaptain: (UserEntity) -> Unit = {},
    onDeleteTeam: () -> Unit = {},
    onPlayerClick: (UserEntity) -> Unit,
    onBackClick: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var playerForAction by remember { mutableStateOf<UserEntity?>(null) }
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

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
                        showDeleteConfirmDialog = true
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Team",
                            tint = CrimsonPrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    hapticManager?.tapUiClick()
                    showAddDialog = true
                },
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

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = team.teamName,
                                    style = PlayerNamePrimary,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${team.city.ifBlank { "Grassroots" }} • Squad Size: ${squad.size}/15",
                                    style = BodyCaption,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Team Stats Quad
                        val stats = teamStats ?: com.cricsaga.app.data.repository.TeamStatsSummary(0, 0, 0, "0.0%")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            TeamStatCol("PLAYED", "${stats.played}")
                            TeamStatCol("WON", "${stats.won}")
                            TeamStatCol("LOST", "${stats.lost}")
                            TeamStatCol("WIN %", stats.winPct)
                        }
                    }
                }
            }

            // Squad Roster Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SQUAD ROSTER (${squad.size})",
                        style = SectionLabel,
                        color = TextTertiary
                    )
                    Text(
                        text = "MAX 15 SQUAD",
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
                            val playerBitmap = remember(player.profilePhotoUrl) {
                                ImageUtils.loadBitmapFromPath(player.profilePhotoUrl)
                            }

                            // Player avatar / initials
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ElevatedSurfaceDark)
                                    .border(1.dp, CardBorderDark, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (playerBitmap != null) {
                                    Image(
                                        bitmap = playerBitmap,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = player.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                        style = MicroTag,
                                        color = TextPrimary
                                    )
                                }
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
                                    if (player.userId == team.captainId || (team.captainId == null && index == 0)) {
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

                            // Action Menu (Make Captain / Remove)
                            Box {
                                IconButton(onClick = {
                                    hapticManager?.tapUiClick()
                                    playerForAction = player
                                }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Player Options",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = playerForAction == player,
                                    onDismissRequest = { playerForAction = null },
                                    modifier = Modifier.background(SlateSurfaceDark)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Set as Captain (C)", color = TextPrimary, fontSize = 13.sp) },
                                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = CrimsonPrimaryDark, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            hapticManager?.tapUiClick()
                                            onSetCaptain(player)
                                            playerForAction = null
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Remove from Squad", color = DismissalRed, fontSize = 13.sp) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = DismissalRed, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            hapticManager?.tapUiClick()
                                            onRemovePlayer(player)
                                            playerForAction = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Player Dialog using unified AddPlayerToTeamDialog
        if (showAddDialog) {
            AddPlayerToTeamDialog(
                teamName = team.teamName,
                currentSquadUserIds = squad.map { it.userId }.toSet(),
                allUsers = allUsers,
                onDismiss = { showAddDialog = false },
                onAddExistingUser = { userId ->
                    onAddExistingPlayer(userId)
                    showAddDialog = false
                },
                onCreateAndAddNewPlayer = { name, email, role, jersey ->
                    onCreateAndAddNewPlayer(name, email, role, jersey)
                    showAddDialog = false
                }
            )
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                containerColor = SlateSurfaceDark,
                title = { Text("Delete Team", fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = { Text("Are you sure you want to delete ${team.teamName}? This action cannot be undone.", color = TextSecondary, fontSize = 13.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            hapticManager?.tapWicket()
                            showDeleteConfirmDialog = false
                            onDeleteTeam()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DismissalRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("DELETE", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("CANCEL", color = TextSecondary)
                    }
                }
            )
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
