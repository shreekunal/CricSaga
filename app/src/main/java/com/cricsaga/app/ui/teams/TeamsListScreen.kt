package com.cricsaga.app.ui.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun TeamsListScreen(
    teams: List<TeamEntity>,
    onCreateTeam: (name: String, code: String, city: String) -> Unit,
    onAddPlayer: (teamId: String, name: String, role: PlayingRole) -> Unit
) {
    var showCreateTeamDialog by remember { mutableStateOf(false) }
    var selectedTeamForAddPlayer by remember { mutableStateOf<TeamEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teams & Squads", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateTeamDialog = true },
                containerColor = CrimsonPrimaryDark,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Team")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (teams.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No teams created yet. Tap '+' to create your first team!", color = MutedGrey)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(teams) { team ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = team.teamName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Code: ${team.shortCode} • City: ${team.city}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedGrey
                                    )
                                }
                                IconButton(onClick = { selectedTeamForAddPlayer = team }) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Player", tint = BoundaryAmber)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create Team Dialog
        if (showCreateTeamDialog) {
            var teamName by remember { mutableStateOf("") }
            var teamCode by remember { mutableStateOf("") }
            var city by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCreateTeamDialog = false },
                containerColor = SlateSurfaceDark,
                title = { Text("Create New Team", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = teamName,
                            onValueChange = { teamName = it },
                            label = { Text("Team Name (e.g. Royal Kings)") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = teamCode,
                            onValueChange = { teamCode = it.uppercase() },
                            label = { Text("Short Code (e.g. RK)") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (teamName.isNotBlank()) {
                                onCreateTeam(teamName, teamCode, city)
                                showCreateTeamDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark)
                    ) {
                        Text("CREATE", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateTeamDialog = false }) {
                        Text("CANCEL", color = Color.Gray)
                    }
                }
            )
        }

        // Add Player Dialog
        selectedTeamForAddPlayer?.let { team ->
            var playerName by remember { mutableStateOf("") }
            var selectedRole by remember { mutableStateOf(PlayingRole.BATSMAN) }

            AlertDialog(
                onDismissRequest = { selectedTeamForAddPlayer = null },
                containerColor = SlateSurfaceDark,
                title = { Text("Add Player to ${team.teamName}", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = playerName,
                            onValueChange = { playerName = it },
                            label = { Text("Player Full Name") },
                            singleLine = true
                        )
                        Text("Role:", color = MutedGrey, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(PlayingRole.BATSMAN, PlayingRole.BOWLER, PlayingRole.ALL_ROUNDER).forEach { role ->
                                FilterChip(
                                    selected = selectedRole == role,
                                    onClick = { selectedRole = role },
                                    label = { Text(role.name.replace("_", " ")) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (playerName.isNotBlank()) {
                                onAddPlayer(team.teamId, playerName, selectedRole)
                                selectedTeamForAddPlayer = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BoundaryGreen)
                    ) {
                        Text("ADD PLAYER", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedTeamForAddPlayer = null }) {
                        Text("CANCEL", color = Color.Gray)
                    }
                }
            )
        }
    }
}
