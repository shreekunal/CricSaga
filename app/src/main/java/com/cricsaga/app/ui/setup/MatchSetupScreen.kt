package com.cricsaga.app.ui.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.TeamEntity
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.BallType
import com.cricsaga.app.domain.model.BattingStyle
import com.cricsaga.app.domain.model.BowlingStyle
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.domain.model.TossDecision
import com.cricsaga.app.ui.theme.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    teams: List<TeamEntity>,
    onBackClick: () -> Unit,
    onCreateTeam: (name: String, code: String, city: String, onCreated: (teamId: String) -> Unit) -> Unit = { _, _, _, _ -> },
    onAddPlayerToTeam: (teamId: String, name: String, phone: String, role: PlayingRole, password: String, onAdded: () -> Unit) -> Unit = { _, _, _, _, _, _ -> },
    getTeamMembersFlow: (teamId: String) -> Flow<List<UserEntity>> = { flowOf(emptyList()) },
    onProceedToOpeners: (
        title: String,
        teamAId: String,
        teamBId: String,
        overs: Int,
        ballType: BallType,
        tossWinnerId: String,
        tossDecision: TossDecision,
        maxOversPerBowler: Int
    ) -> Unit
) {
    var teamA by remember { mutableStateOf(teams.getOrNull(0)?.teamId ?: "") }
    var teamB by remember { mutableStateOf(teams.getOrNull(1)?.teamId ?: "") }
    var totalOvers by remember { mutableIntStateOf(20) }
    var selectedBallType by remember { mutableStateOf(BallType.TENNIS_LIGHT) }
    var tossWinnerId by remember { mutableStateOf(teams.getOrNull(0)?.teamId ?: "") }
    var tossDecision by remember { mutableStateOf(TossDecision.BAT) }
    var venue by remember { mutableStateOf("Shivaji Park Ground #2, Dadar") }
    var maxOversPerBowler by remember { mutableIntStateOf(4) }
    var isBoxCricket by remember { mutableStateOf(false) }

    var showAddTeamDialog by remember { mutableStateOf(false) }
    var teamIdForAddPlayer by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    // Observe squads for selected teams
    val teamASquad by remember(teamA) {
        if (teamA.isNotEmpty()) getTeamMembersFlow(teamA) else flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val teamBSquad by remember(teamB) {
        if (teamB.isNotEmpty()) getTeamMembersFlow(teamB) else flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    LaunchedEffect(teams) {
        if (teamA.isEmpty() && teams.isNotEmpty()) {
            teamA = teams[0].teamId
        }
        if (teamB.isEmpty() && teams.size > 1) {
            teamB = teams[1].teamId
        }
        if (tossWinnerId.isEmpty() && teamA.isNotEmpty()) {
            tossWinnerId = teamA
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Match Setup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Configure Teams & Playing Conditions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Team Selection & Squad Previews
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = CrimsonPrimaryDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "COMPETING TEAMS",
                                color = CrimsonPrimaryDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                hapticManager?.tapUiClick()
                                showAddTeamDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CrimsonPrimaryDark.copy(alpha = 0.12f),
                                contentColor = CrimsonPrimaryDark
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New Team",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (teams.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "No teams available yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = {
                                        hapticManager?.tapUiClick()
                                        showAddTeamDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create First Team")
                                }
                            }
                        }
                    } else {
                        // Team A Selector
                        Text(
                            text = "Team A (Home):",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(teams) { t ->
                                val isSelected = teamA == t.teamId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        hapticManager?.tapUiClick()
                                        teamA = t.teamId
                                        if (tossWinnerId.isEmpty() || tossWinnerId != teamA && tossWinnerId != teamB) {
                                            tossWinnerId = t.teamId
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = t.teamName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CrimsonPrimaryDark,
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        // Team A Squad Preview Card
                        val selectedTeamA = teams.find { it.teamId == teamA }
                        if (selectedTeamA != null) {
                            TeamSquadOverviewCard(
                                team = selectedTeamA,
                                squad = teamASquad,
                                isHome = true,
                                onAddPlayerClick = {
                                    hapticManager?.tapUiClick()
                                    teamIdForAddPlayer = selectedTeamA.teamId
                                }
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        // Team B Selector
                        Text(
                            text = "Team B (Away):",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(teams.filter { it.teamId != teamA }) { t ->
                                val isSelected = teamB == t.teamId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        hapticManager?.tapUiClick()
                                        teamB = t.teamId
                                    },
                                    label = {
                                        Text(
                                            text = t.teamName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF059669),
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        // Team B Squad Preview Card
                        val selectedTeamB = teams.find { it.teamId == teamB }
                        if (selectedTeamB != null) {
                            TeamSquadOverviewCard(
                                team = selectedTeamB,
                                squad = teamBSquad,
                                isHome = false,
                                onAddPlayerClick = {
                                    hapticManager?.tapUiClick()
                                    teamIdForAddPlayer = selectedTeamB.teamId
                                }
                            )
                        }
                    }
                }
            }

            // Section 2: Overs & Ball Configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsCricket,
                            contentDescription = null,
                            tint = CrimsonPrimaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MATCH FORMAT & BALL",
                            color = CrimsonPrimaryDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = "Total Overs per Innings:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(5, 8, 10, 12, 15, 20)) { ov ->
                            val isSelected = totalOvers == ov
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    totalOvers = ov
                                    maxOversPerBowler = (ov / 5).coerceIn(1, 4)
                                },
                                label = {
                                    Text(
                                        text = "$ov Overs",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonPrimaryDark,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Text(
                        text = "Ball Type:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedBallType == BallType.TENNIS_LIGHT,
                            onClick = {
                                hapticManager?.tapUiClick()
                                selectedBallType = BallType.TENNIS_LIGHT
                            },
                            label = { Text("Tennis Ball (Turf/Box)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimaryDark,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        FilterChip(
                            selected = selectedBallType == BallType.LEATHER,
                            onClick = {
                                hapticManager?.tapUiClick()
                                selectedBallType = BallType.LEATHER
                            },
                            label = { Text("Leather Ball") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimaryDark,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // Section 3: Venue & Grassroots Rules
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "VENUE & PLAYING RULES",
                        color = CrimsonPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )

                    OutlinedTextField(
                        value = venue,
                        onValueChange = { venue = it },
                        label = { Text("Ground / Turf Venue") },
                        placeholder = { Text("e.g. Shivaji Park Ground #2, Dadar") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = CrimsonPrimaryDark,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = CrimsonPrimaryDark,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text(
                        text = "Max Overs per Bowler:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3, 4, 5).forEach { quota ->
                            val isSelected = maxOversPerBowler == quota
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    maxOversPerBowler = quota
                                },
                                label = { Text("$quota ov") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonPrimaryDark,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Box Cricket Rules",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "2-run wide / no-ball & enclosed turf boundaries",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isBoxCricket,
                            onCheckedChange = {
                                hapticManager?.tapUiClick()
                                isBoxCricket = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CrimsonPrimaryDark,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            // Section 4: Toss Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TOSS DECISION",
                        color = CrimsonPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )

                    val teamAName = teams.find { it.teamId == teamA }?.teamName ?: "Team A"
                    val teamBName = teams.find { it.teamId == teamB }?.teamName ?: "Team B"

                    Text(
                        text = "Who won the toss?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = tossWinnerId == teamA,
                            onClick = {
                                hapticManager?.tapUiClick()
                                tossWinnerId = teamA
                            },
                            label = { Text(teamAName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimaryDark,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        FilterChip(
                            selected = tossWinnerId == teamB,
                            onClick = {
                                hapticManager?.tapUiClick()
                                tossWinnerId = teamB
                            },
                            label = { Text(teamBName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF059669),
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Text(
                        text = "Elected to:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = tossDecision == TossDecision.BAT,
                            onClick = {
                                hapticManager?.tapUiClick()
                                tossDecision = TossDecision.BAT
                            },
                            label = { Text("BAT FIRST") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimaryDark,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        FilterChip(
                            selected = tossDecision == TossDecision.BOWL,
                            onClick = {
                                hapticManager?.tapUiClick()
                                tossDecision = TossDecision.BOWL
                            },
                            label = { Text("BOWL FIRST") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimaryDark,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // Squad Minimum Notice
            val canStart = teamA.isNotEmpty() &&
                    teamB.isNotEmpty() &&
                    teamA != teamB &&
                    teamASquad.size >= 2 &&
                    teamBSquad.size >= 2

            if (teamA.isNotEmpty() && teamB.isNotEmpty() && (teamASquad.size < 2 || teamBSquad.size < 2)) {
                Surface(
                    color = BoundaryAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BoundaryAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Each team must have at least 2 players in its squad to begin a match. Use '+ Add Player' above to add squad members.",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Start Match Button
            Button(
                onClick = {
                    hapticManager?.tapUiClick()
                    val teamAName = teams.find { it.teamId == teamA }?.teamName ?: "Team A"
                    val teamBName = teams.find { it.teamId == teamB }?.teamName ?: "Team B"
                    val title = "$teamAName vs $teamBName"
                    onProceedToOpeners(
                        title,
                        teamA,
                        teamB,
                        totalOvers,
                        selectedBallType,
                        tossWinnerId,
                        tossDecision,
                        maxOversPerBowler
                    )
                },
                enabled = canStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimaryDark,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "SELECT OPENERS & START",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (canStart) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialog: Create New Team on the go
    if (showAddTeamDialog) {
        AddTeamOnTheGoDialog(
            onDismiss = { showAddTeamDialog = false },
            onCreate = { name, code, city ->
                onCreateTeam(name, code, city) { newId ->
                    if (teamA.isEmpty()) {
                        teamA = newId
                        tossWinnerId = newId
                    } else if (teamB.isEmpty()) {
                        teamB = newId
                    }
                }
                showAddTeamDialog = false
            }
        )
    }

    // Dialog: Add Player on the go to a Team
    teamIdForAddPlayer?.let { targetTeamId ->
        val targetTeam = teams.find { it.teamId == targetTeamId }
        AddPlayerOnTheGoDialog(
            teamName = targetTeam?.teamName ?: "Team",
            onDismiss = { teamIdForAddPlayer = null },
            onAddPlayer = { name, phone, role, password ->
                onAddPlayerToTeam(targetTeamId, name, phone, role, password) {
                    teamIdForAddPlayer = null
                }
            }
        )
    }
}

/**
 * Team Squad Overview Card with clean vertical hierarchy and generous breathing room.
 */
@Composable
private fun TeamSquadOverviewCard(
    team: TeamEntity,
    squad: List<UserEntity>,
    isHome: Boolean,
    onAddPlayerClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Team Home/Away Badge + Team Name (Left) and "+ Player" Action (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        color = if (isHome) CrimsonPrimaryDark.copy(alpha = 0.15f) else Color(0xFF059669).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isHome) "HOME" else "AWAY",
                            color = if (isHome) CrimsonPrimaryDark else Color(0xFF059669),
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = "${team.teamName} (${team.shortCode})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    modifier = Modifier.clickable { onAddPlayerClick() },
                    shape = RoundedCornerShape(8.dp),
                    color = CrimsonPrimaryDark.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CrimsonPrimaryDark.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = CrimsonPrimaryDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ Player",
                            color = CrimsonPrimaryDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Row 2: Dedicated Squad Count Sub-strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Squad Roster • ${squad.size} Players registered",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (squad.size < 2) {
                    Text(
                        text = "Min 2 needed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BoundaryAmber
                    )
                }
            }

            // Row 3: Squad Member Chips or Helpful Empty Prompt
            if (squad.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No players in squad yet. Tap '+ Player' above to create and add squad members.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(squad) { player ->
                        PlayerMiniChip(player = player)
                    }
                }
            }
        }
    }
}

/**
 * Compact Player chip showing avatar initials, name, and role badge.
 */
@Composable
private fun PlayerMiniChip(player: UserEntity) {
    val initials = player.fullName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifEmpty { "P" }

    val roleBadge = when (player.playingRole) {
        PlayingRole.BATSMAN -> "BAT"
        PlayingRole.BOWLER -> "BOWL"
        PlayingRole.ALL_ROUNDER -> "AR"
        PlayingRole.WICKET_KEEPER -> "WK"
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(CrimsonPrimaryDark.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = CrimsonPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = player.fullName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = roleBadge,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * Dialog to add a new player on the go.
 * Creates both a player roster entry and a full user login account.
 */
@Composable
private fun AddPlayerOnTheGoDialog(
    teamName: String,
    onDismiss: () -> Unit,
    onAddPlayer: (name: String, phone: String, role: PlayingRole, password: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(PlayingRole.ALL_ROUNDER) }
    var password by remember { mutableStateOf("123456") }

    fun generateRandomPhone() {
        val randomDigits = (10000000..99999999).random().toString()
        phone = "98$randomDigits"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column {
                Text(
                    text = "Add Player to $teamName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Creates player profile and login account",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Player Full Name") },
                    placeholder = { Text("e.g. Aarav Patel") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = CrimsonPrimaryDark,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = CrimsonPrimaryDark,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) phone = it.filter { ch -> ch.isDigit() } },
                    label = { Text("10-Digit Mobile Number") },
                    placeholder = { Text("e.g. 9876543210") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = CrimsonPrimaryDark,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = CrimsonPrimaryDark,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { generateRandomPhone() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = CrimsonPrimaryDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto-Generate Number",
                            color = CrimsonPrimaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "Playing Role:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        PlayingRole.BATSMAN to "Batter",
                        PlayingRole.BOWLER to "Bowler",
                        PlayingRole.ALL_ROUNDER to "All-Round",
                        PlayingRole.WICKET_KEEPER to "Keeper"
                    ).forEach { (r, label) ->
                        val isSelected = role == r
                        FilterChip(
                            selected = isSelected,
                            onClick = { role = r },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimaryDark,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Account Password:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Default password is set to 123456 so the player can log in immediately from the login screen.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAddPlayer(name.trim(), phone.trim(), role, password)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ADD PLAYER", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

/**
 * Dialog to create a brand new team on the go.
 */
@Composable
private fun AddTeamOnTheGoDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, code: String, city: String) -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    var teamCode by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Mumbai") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column {
                Text(
                    text = "Create New Team",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Creates team and auto-generates 8 starter accounts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = {
                        teamName = it
                        if (teamCode.isEmpty() && it.isNotEmpty()) {
                            teamCode = it.split(" ")
                                .mapNotNull { part -> part.firstOrNull() }
                                .joinToString("")
                                .take(3)
                                .uppercase()
                        }
                    },
                    label = { Text("Team Name") },
                    placeholder = { Text("e.g. Shivaji Park XI") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = CrimsonPrimaryDark,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = CrimsonPrimaryDark,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = teamCode,
                    onValueChange = { if (it.length <= 4) teamCode = it.uppercase() },
                    label = { Text("Short Code (e.g. SPX)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = CrimsonPrimaryDark,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = CrimsonPrimaryDark,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City / Region") },
                    placeholder = { Text("e.g. Mumbai") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = CrimsonPrimaryDark,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = CrimsonPrimaryDark,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (teamName.isNotBlank()) {
                        val code = teamCode.ifBlank { teamName.take(3).uppercase() }
                        onCreate(teamName.trim(), code, city.trim())
                    }
                },
                enabled = teamName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CREATE TEAM", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
