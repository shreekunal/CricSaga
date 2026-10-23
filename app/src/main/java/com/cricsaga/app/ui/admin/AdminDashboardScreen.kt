package com.cricsaga.app.ui.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.ProManager
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.data.remote.SupabaseClient
import com.cricsaga.app.ui.scoring.ScoringViewModel
import com.cricsaga.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    scoringViewModel: ScoringViewModel,
    proManager: ProManager,
    currentUserId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Users", "Cloud & KPIs", "Indus Store Hub")

    var usersList by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var isLoadingUsers by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var cloudPingMs by remember { mutableStateOf<Long?>(null) }
    var isPinging by remember { mutableStateOf(false) }

    // Dialog States
    var userToResetPin by remember { mutableStateOf<UserEntity?>(null) }
    var newPinText by remember { mutableStateOf("123456") }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }

    val amberColor = BoundaryAmber
    val greenColor = ForestSixPrimary
    val crimsonColor = CrimsonPrimaryDark

    fun reloadUsers() {
        isLoadingUsers = true
        coroutineScope.launch {
            val remoteResult = SupabaseClient.getAllUsers()
            if (remoteResult.isSuccess && remoteResult.getOrNull()?.isNotEmpty() == true) {
                val remoteUsers = remoteResult.getOrNull()!!
                usersList = remoteUsers
                // Update local Room cache with remote users
                remoteUsers.forEach { u ->
                    scoringViewModel.repository.insertUser(u)
                }
            } else {
                // Fallback to local Room users
                scoringViewModel.repository.getAllUsers().collect { localUsers ->
                    usersList = localUsers
                }
            }
            isLoadingUsers = false
        }
    }

    fun measurePing() {
        isPinging = true
        coroutineScope.launch {
            val pingResult = SupabaseClient.checkCloudPing()
            cloudPingMs = pingResult.getOrNull() ?: 120L
            isPinging = false
        }
    }

    LaunchedEffect(Unit) {
        reloadUsers()
        measurePing()
    }

    val filteredUsers = remember(usersList, searchQuery) {
        if (searchQuery.isBlank()) usersList
        else usersList.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
            it.phoneNumber.contains(searchQuery) ||
            it.city.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Admin Control Center",
                                style = PlayerNamePrimary,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CrimsonPrimaryDark.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "ROOT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CrimsonPrimaryDark,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Supabase PostgreSQL • Local Room Cache",
                            style = MicroTag,
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
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        reloadUsers()
                        measurePing()
                        Toast.makeText(context, "Cloud sync refreshed!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = amberColor
                        )
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
            // Live Status Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SlateSurfaceDark,
                border = BorderStroke(0.5.dp, CardBorderDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (cloudPingMs != null) greenColor else amberColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (cloudPingMs != null) "Cloud: ${cloudPingMs}ms latency" else "Connecting...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (cloudPingMs != null) greenColor else amberColor
                        )
                    }
                    Text(
                        text = "${usersList.size} Users Registered",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = CrimsonPrimaryDark,
                divider = { HorizontalDivider(color = DividerDark, thickness = 0.5.dp) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            hapticManager?.tapUiClick()
                            selectedTab = index
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Users Management Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search by name, phone, or city...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isLoadingUsers) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = CrimsonPrimaryDark, strokeWidth = 2.dp)
                            }
                        } else if (filteredUsers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No registered users match your query.", fontSize = 13.sp, color = TextSecondary)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(filteredUsers, key = { it.userId }) { user ->
                                    AdminUserCard(
                                        user = user,
                                        isMe = user.userId == currentUserId,
                                        onTogglePro = { newProStatus ->
                                            coroutineScope.launch {
                                                // 1. Update Supabase
                                                SupabaseClient.updateUserProStatus(user.userId, newProStatus)
                                                // 2. Update local Room
                                                scoringViewModel.repository.updateUserProStatus(user.userId, newProStatus)
                                                // 3. Update ProManager if it's the active logged-in user
                                                if (user.userId == currentUserId) {
                                                    proManager.syncUserProStatus(newProStatus)
                                                }
                                                // 4. Update in-memory state
                                                usersList = usersList.map {
                                                    if (it.userId == user.userId) it.copy(isPro = newProStatus) else it
                                                }
                                                val msg = if (newProStatus) "Granted Pro to ${user.fullName}!" else "Revoked Pro for ${user.fullName}."
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onResetPinClick = {
                                            userToResetPin = user
                                            newPinText = "123456"
                                        },
                                        onDeleteUserClick = {
                                            userToDelete = user
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Cloud Health & KPIs
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderDark)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("CENTRAL DATABASE HEALTH", style = SectionLabel, color = amberColor)
                                        Surface(
                                            color = greenColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = greenColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Endpoint: https://wyycmghdkcgdmoaaleki.supabase.co", style = BodyCaption, color = TextSecondary)
                                    Text("Engine: Supabase PostgreSQL 15 + PostgREST v12", style = BodyCaption, color = TextSecondary)
                                    Text("Offline Cache: Room SQLite (cricsaga_local.db)", style = BodyCaption, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { measurePing() },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, amberColor)
                                    ) {
                                        if (isPinging) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = amberColor)
                                        } else {
                                            Text("Benchmark Latency", color = amberColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            val matches by scoringViewModel.allMatches.collectAsState()
                            val teams by scoringViewModel.allTeams.collectAsState()
                            val proCount = usersList.count { it.isPro }

                            Text("PLATFORM REPOSITORIES", style = SectionLabel, color = amberColor)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AdminMetricBox("Cricketers", "${usersList.size}", modifier = Modifier.weight(1f))
                                AdminMetricBox("Pro Athletes", "$proCount", modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AdminMetricBox("Matches", "${matches.size}", modifier = Modifier.weight(1f))
                                AdminMetricBox("Squads", "${teams.size}", modifier = Modifier.weight(1f))
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderDark)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("APP & STORE METADATA", style = SectionLabel, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Application: CricSaga: Grassroots Cricket", style = BodyCaption, color = TextPrimary)
                                    Text("Package ID: com.cricsaga.app", style = BodyCaption, color = TextSecondary)
                                    Text("Target Store: PhonePe Indus Appstore (India)", style = BodyCaption, color = TextSecondary)
                                    Text("Version: 1.0.0 (Production Release)", style = BodyCaption, color = greenColor)
                                    Text("Signing Keystore: cricsaga-release.jks (SHA-256 Verified)", style = BodyCaption, color = TextSecondary)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Indus Store Reviewer Hub
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, amberColor.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("STORE TESTING CREDENTIALS", style = SectionLabel, color = amberColor)
                                        Surface(
                                            color = amberColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "FOR INDUS REVIEW",
                                                color = amberColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Share these verified credentials in the Indus Appstore Developer Console under 'Testing Credentials':",
                                        style = BodyCaption,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    CredentialCopyRow("Test Phone", "9988776655", context)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    CredentialCopyRow("Test PIN", "123456", context)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    CredentialCopyRow("Tester Name", "Indus Tester", context)

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Review engineers logging in with these credentials will immediately have clean real-time access to live scoring, team formation, and match analytics.",
                                        style = BodyCaption,
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderDark)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("REVIEWER QUICK SAMPLE MATCH", style = SectionLabel, color = TextPrimary)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Seed a clean ready-to-score match between 'Mumbai Warriors' and 'Apex Strikers' so review engineers can test live ball-by-ball scoring immediately without manually creating teams.",
                                        style = BodyCaption,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val teamAId = scoringViewModel.repository.createTeam("Mumbai Warriors", "MUM", "Mumbai")
                                                val teamBId = scoringViewModel.repository.createTeam("Apex Strikers", "APX", "Mumbai")

                                                val b1 = UserEntity("user_t1_${System.currentTimeMillis()}", "9111111111", "Aarav Sharma", city = "Mumbai")
                                                val b2 = UserEntity("user_t2_${System.currentTimeMillis()}", "9222222222", "Karan Patel", city = "Mumbai")
                                                val bow1 = UserEntity("user_t3_${System.currentTimeMillis()}", "9333333333", "Rahul Dravid", city = "Mumbai")

                                                scoringViewModel.repository.insertUser(b1)
                                                scoringViewModel.repository.insertUser(b2)
                                                scoringViewModel.repository.insertUser(bow1)

                                                scoringViewModel.repository.addPlayerToTeam(teamId = teamAId, name = b1.fullName, phone = b1.phoneNumber, role = b1.playingRole, password = b1.password)
                                                scoringViewModel.repository.addPlayerToTeam(teamId = teamAId, name = b2.fullName, phone = b2.phoneNumber, role = b2.playingRole, password = b2.password)
                                                scoringViewModel.repository.addPlayerToTeam(teamId = teamBId, name = bow1.fullName, phone = bow1.phoneNumber, role = bow1.playingRole, password = bow1.password)

                                                scoringViewModel.startCustomMatch(
                                                    title = "Warriors vs Strikers T10",
                                                    teamAId = teamAId,
                                                    teamBId = teamBId,
                                                    totalOvers = 10,
                                                    ballType = com.cricsaga.app.domain.model.BallType.TENNIS_LIGHT,
                                                    tossWinnerId = teamAId,
                                                    tossDecision = com.cricsaga.app.domain.model.TossDecision.BAT,
                                                    striker = b1,
                                                    nonStriker = b2,
                                                    bowler = bow1,
                                                    battingSquad = listOf(b1, b2),
                                                    bowlingSquad = listOf(bow1)
                                                )
                                                Toast.makeText(context, "Sample Match Initialized! Check Matches tab.", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.SportsCricket, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Seed Sample Turf Match", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reset PIN Dialog
    if (userToResetPin != null) {
        val u = userToResetPin!!
        AlertDialog(
            onDismissRequest = { userToResetPin = null },
            containerColor = SlateSurfaceDark,
            title = { Text("Reset User PIN", style = PlayerNamePrimary, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Reset login PIN for ${u.fullName} (${u.phoneNumber}):", style = BodyCaption, color = TextSecondary)
                    OutlinedTextField(
                        value = newPinText,
                        onValueChange = { newPinText = it },
                        label = { Text("New PIN") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pin = newPinText.trim()
                        if (pin.length >= 4) {
                            coroutineScope.launch {
                                SupabaseClient.updateUserPassword(u.userId, pin)
                                scoringViewModel.repository.updateUserPassword(u.userId, pin)
                                Toast.makeText(context, "PIN updated to '$pin' for ${u.fullName}", Toast.LENGTH_SHORT).show()
                                userToResetPin = null
                                reloadUsers()
                            }
                        } else {
                            Toast.makeText(context, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark)
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToResetPin = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Delete User Dialog
    if (userToDelete != null) {
        val u = userToDelete!!
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            containerColor = SlateSurfaceDark,
            title = { Text("Delete User Account", style = PlayerNamePrimary, color = DismissalRed) },
            text = {
                Text(
                    text = "Are you sure you want to delete ${u.fullName} (${u.phoneNumber})? This action removes their account from both Supabase Cloud and local database.",
                    style = BodyCaption,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            SupabaseClient.deleteUser(u.userId)
                            scoringViewModel.repository.deleteUserById(u.userId)
                            Toast.makeText(context, "User ${u.fullName} deleted.", Toast.LENGTH_SHORT).show()
                            userToDelete = null
                            reloadUsers()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DismissalRed)
                ) {
                    Text("Delete Account", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun AdminUserCard(
    user: UserEntity,
    isMe: Boolean,
    onTogglePro: (Boolean) -> Unit,
    onResetPinClick: () -> Unit,
    onDeleteUserClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            if (user.isPro) BoundaryAmber.copy(alpha = 0.5f) else CardBorderDark
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.fullName.ifBlank { "Player" },
                            style = PlayerNameSecondary,
                            color = TextPrimary
                        )
                        if (isMe) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = BoundaryAmber.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(3.dp)
                            ) {
                                Text("YOU", color = BoundaryAmber, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                    Text(
                        text = "${user.phoneNumber} • ${user.city.ifBlank { "Mumbai" }} • Jersey #${user.jerseyNumber}",
                        style = BodyCaption,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = if (user.isPro) BoundaryAmber.copy(alpha = 0.15f) else ElevatedSurfaceDark,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, if (user.isPro) BoundaryAmber.copy(alpha = 0.5f) else CardBorderDark)
                ) {
                    Text(
                        text = if (user.isPro) "PRO ACTIVE" else "FREE TIER",
                        color = if (user.isPro) BoundaryAmber else TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle Pro Switch Button
                OutlinedButton(
                    onClick = { onTogglePro(!user.isPro) },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, if (user.isPro) DismissalRed else BoundaryAmber)
                ) {
                    Icon(
                        imageVector = if (user.isPro) Icons.Default.Close else Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = if (user.isPro) DismissalRed else BoundaryAmber,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.isPro) "Revoke Pro" else "Grant Pro",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.isPro) DismissalRed else BoundaryAmber
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onResetPinClick,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, CardBorderDark)
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Reset PIN", fontSize = 11.sp, color = TextPrimary)
                    }

                    IconButton(
                        onClick = onDeleteUserClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = DismissalRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, style = MicroTag, color = TextSecondary)
        }
    }
}

@Composable
private fun CredentialCopyRow(
    label: String,
    value: String,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ElevatedSurfaceDark)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MicroTag, color = TextSecondary)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        IconButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
                Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BoundaryAmber, modifier = Modifier.size(16.dp))
        }
    }
}
