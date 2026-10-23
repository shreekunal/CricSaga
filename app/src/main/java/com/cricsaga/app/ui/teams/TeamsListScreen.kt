package com.cricsaga.app.ui.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.cricsaga.app.ui.teams.dialog.AddPlayerToTeamDialog
import com.cricsaga.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsListScreen(
    teams: List<TeamEntity>,
    allUsers: List<UserEntity> = emptyList(),
    onTeamClick: (TeamEntity) -> Unit = {},
    onCreateTeam: (name: String, code: String, city: String) -> Unit,
    onAddExistingPlayer: (teamId: String, userId: String) -> Unit = { _, _ -> },
    onCreateAndAddNewPlayer: (teamId: String, fullName: String, email: String, role: PlayingRole, jerseyNumber: String) -> Unit = { _, _, _, _, _ -> },
    onAddPlayer: (teamId: String, name: String, role: PlayingRole) -> Unit = { _, _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var showCreateTeamDialog by remember { mutableStateOf(false) }
    var selectedTeamForAddPlayer by remember { mutableStateOf<TeamEntity?>(null) }
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    Scaffold(
        containerColor = CharcoalBackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Teams & Squads", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        onBackClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    hapticManager?.tapUiClick()
                    showCreateTeamDialog = true
                },
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
                    Text("No teams created yet. Tap '+' to create your first team.", color = TextTertiary, fontSize = 13.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(teams) { team ->
                        Card(
                            onClick = {
                                hapticManager?.tapUiClick()
                                onTeamClick(team)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = team.teamName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${team.shortCode} • ${team.city}",
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )
                                }
                                IconButton(onClick = { selectedTeamForAddPlayer = team }) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Player", tint = TextSecondary, modifier = Modifier.size(20.dp))
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
                title = { Text("Create New Team", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = teamName,
                            onValueChange = { teamName = it },
                            label = { Text("Team Name") },
                            placeholder = { Text("e.g. Royal Kings") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = teamCode,
                            onValueChange = { if (it.length <= 4) teamCode = it.uppercase() },
                            label = { Text("Short Code (e.g. RK)") },
                            placeholder = { Text("e.g. RK") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City / Region (Optional)") },
                            placeholder = { Text("e.g. Mumbai") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    val canCreate = teamName.trim().isNotBlank() && teamCode.trim().isNotBlank()
                    Button(
                        onClick = {
                            if (canCreate) {
                                onCreateTeam(teamName.trim(), teamCode.trim(), city.trim())
                                showCreateTeamDialog = false
                            }
                        },
                        enabled = canCreate,
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

        // Add Player Dialog using unified AddPlayerToTeamDialog
        selectedTeamForAddPlayer?.let { team ->
            AddPlayerToTeamDialog(
                teamName = team.teamName,
                currentSquadUserIds = emptySet(),
                allUsers = allUsers,
                onDismiss = { selectedTeamForAddPlayer = null },
                onAddExistingUser = { userId ->
                    onAddExistingPlayer(team.teamId, userId)
                    selectedTeamForAddPlayer = null
                },
                onCreateAndAddNewPlayer = { name, email, role, jersey ->
                    onCreateAndAddNewPlayer(team.teamId, name, email, role, jersey)
                    selectedTeamForAddPlayer = null
                }
            )
        }
    }
}
