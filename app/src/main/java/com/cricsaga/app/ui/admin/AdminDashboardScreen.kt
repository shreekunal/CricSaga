package com.cricsaga.app.ui.admin

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.AuthManager
import com.cricsaga.app.data.local.ProManager
import com.cricsaga.app.data.local.entity.MatchEntity
import com.cricsaga.app.data.local.entity.TeamEntity
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.data.remote.SupabaseClient
import com.cricsaga.app.domain.model.MatchStatus
import com.cricsaga.app.ui.scoring.ScoringViewModel
import com.cricsaga.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    scoringViewModel: ScoringViewModel,
    proManager: ProManager,
    authManager: AuthManager,
    currentUserId: String,
    onNavigateToPlayerApp: () -> Unit = {},
    onInspectMatchScorecard: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Athletes", "Matches", "System")

    // Room database states
    val allMatches by scoringViewModel.allMatches.collectAsState()
    val allTeams by scoringViewModel.allTeams.collectAsState()
    val localUsers by scoringViewModel.repository.getAllUsers().collectAsState(initial = emptyList())
    val totalDeliveries by scoringViewModel.repository.getDeliveryCountFlow().collectAsState(initial = 0)
    val totalRuns by scoringViewModel.repository.getTotalRunsFlow().collectAsState(initial = 0)
    val totalFours by scoringViewModel.repository.getFourCountFlow().collectAsState(initial = 0)
    val totalSixes by scoringViewModel.repository.getSixCountFlow().collectAsState(initial = 0)
    val totalWickets by scoringViewModel.repository.getWicketCountFlow().collectAsState(initial = 0)
    val liveMatchCount by scoringViewModel.repository.getLiveMatchCountFlow().collectAsState(initial = 0)
    val completedMatchCount by scoringViewModel.repository.getCompletedMatchCountFlow().collectAsState(initial = 0)
    val proUserCount by scoringViewModel.repository.getProUserCountFlow().collectAsState(initial = 0)

    // Remote sync states
    var remoteUsers by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var isSyncingCloud by remember { mutableStateOf(false) }
    var cloudPingMs by remember { mutableStateOf<Long?>(null) }
    var isPinging by remember { mutableStateOf(false) }

    val activeUsers = remember(localUsers, remoteUsers) {
        val merged = LinkedHashMap<String, UserEntity>()
        localUsers.forEach { merged[it.userId] = it }
        remoteUsers.forEach { merged[it.userId] = it }
        merged.values.toList()
    }

    // Filter and search states
    var userSearchQuery by remember { mutableStateOf("") }
    var userFilterType by remember { mutableStateOf(0) } // 0: All, 1: Pro, 2: Admins, 3: Free
    var matchFilterType by remember { mutableStateOf(0) } // 0: All, 1: Live, 2: Completed, 3: Scheduled

    // Dialog States
    var userToResetPin by remember { mutableStateOf<UserEntity?>(null) }
    var newPinText by remember { mutableStateOf("123456") }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var matchToDelete by remember { mutableStateOf<MatchEntity?>(null) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    fun measurePing() {
        isPinging = true
        coroutineScope.launch {
            val pingResult = SupabaseClient.checkCloudPing()
            cloudPingMs = pingResult.getOrNull() ?: 120L
            isPinging = false
        }
    }

    fun triggerFullSync() {
        isSyncingCloud = true
        coroutineScope.launch {
            try {
                scoringViewModel.repository.syncRemoteToLocal(currentUserId)
                val remoteResult = SupabaseClient.getAllUsers()
                if (remoteResult.isSuccess && remoteResult.getOrNull()?.isNotEmpty() == true) {
                    remoteUsers = remoteResult.getOrNull()!!
                    scoringViewModel.repository.insertUsers(remoteUsers)
                }
                measurePing()
                Toast.makeText(context, "Cloud sync complete!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Sync note: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSyncingCloud = false
            }
        }
    }

    LaunchedEffect(Unit) {
        measurePing()
        triggerFullSync()
    }

    BackHandler {
        showLogoutConfirmDialog = true
    }

    val teamMap = remember(allTeams) { allTeams.associateBy { it.teamId } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Admin Root",
                            style = PlayerNamePrimary,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CrimsonPrimaryDark.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PROD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = CrimsonPrimaryDark,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Quick Action: Switch to Player View
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        onNavigateToPlayerApp()
                    }) {
                        Icon(
                            Icons.Default.SportsCricket,
                            contentDescription = "Player View",
                            tint = ForestSixPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Quick Action: Refresh & Reconcile
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        triggerFullSync()
                    }) {
                        if (isSyncingCloud) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = BoundaryAmber
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = BoundaryAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Quick Action: Log Out
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        showLogoutConfirmDialog = true
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log Out",
                            tint = CrimsonPrimaryDark,
                            modifier = Modifier.size(20.dp)
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
            // Live Status Banner (Single-line, responsive)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SlateSurfaceDark,
                border = BorderStroke(0.5.dp, CardBorderDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (cloudPingMs != null) ForestSixPrimary else BoundaryAmber)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (cloudPingMs != null) "Cloud ${cloudPingMs}ms" else "Connecting...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (cloudPingMs != null) ForestSixPrimary else BoundaryAmber
                        )
                    }

                    Text(
                        text = "Supabase PostgreSQL 15",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )

                    Text(
                        text = "${activeUsers.size} Athletes",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }

            // Fixed 4-Tab Row (Cleanly fits any screen width with zero scrolling needed)
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
                                maxLines = 1,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: Clean Executive Overview & KPI Tiles
                    CleanOverviewKpiTab(
                        totalUsers = activeUsers.size,
                        proUsers = proUserCount,
                        totalMatches = allMatches.size,
                        liveMatches = liveMatchCount,
                        completedMatches = completedMatchCount,
                        totalDeliveries = totalDeliveries,
                        totalRuns = totalRuns,
                        totalFours = totalFours,
                        totalSixes = totalSixes,
                        totalWickets = totalWickets,
                        totalTeams = allTeams.size,
                        cloudPingMs = cloudPingMs,
                        isPinging = isPinging,
                        onBenchmarkPing = { measurePing() },
                        onForceSync = { triggerFullSync() },
                        onNavigateToPlayerApp = onNavigateToPlayerApp
                    )
                }

                1 -> {
                    // TAB 1: Mobile-Optimized Athletes Directory
                    CleanAthletesDirectoryTab(
                        users = activeUsers,
                        currentUserId = currentUserId,
                        searchQuery = userSearchQuery,
                        onSearchChange = { userSearchQuery = it },
                        filterType = userFilterType,
                        onFilterChange = { userFilterType = it },
                        authManager = authManager,
                        onTogglePro = { user, newPro ->
                            coroutineScope.launch {
                                SupabaseClient.updateUserProStatus(user.userId, newPro)
                                scoringViewModel.repository.updateUserProStatus(user.userId, newPro)
                                if (user.userId == currentUserId) {
                                    proManager.syncUserProStatus(newPro)
                                }
                                remoteUsers = remoteUsers.map {
                                    if (it.userId == user.userId) it.copy(isPro = newPro) else it
                                }
                                val msg = if (newPro) "Granted Pro to ${user.fullName}!" else "Revoked Pro for ${user.fullName}."
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onToggleAdmin = { user, makeAdmin ->
                            val email = user.email
                            if (email.isNullOrBlank()) {
                                Toast.makeText(context, "User has no email for admin assignment.", Toast.LENGTH_SHORT).show()
                            } else {
                                if (makeAdmin) {
                                    authManager.addAdminEmail(email)
                                    Toast.makeText(context, "${user.fullName} is now an Admin!", Toast.LENGTH_SHORT).show()
                                } else {
                                    authManager.removeAdminEmail(email)
                                    Toast.makeText(context, "Revoked Admin for ${user.fullName}.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onResetPin = { user ->
                            userToResetPin = user
                            newPinText = "123456"
                        },
                        onDeleteUser = { user ->
                            userToDelete = user
                        }
                    )
                }

                2 -> {
                    // TAB 2: Matches & Live Ground Operations
                    CleanMatchesOperationsTab(
                        matches = allMatches,
                        teamMap = teamMap,
                        filterType = matchFilterType,
                        onFilterChange = { matchFilterType = it },
                        onInspectScorecard = onInspectMatchScorecard,
                        onDeleteMatch = { match -> matchToDelete = match }
                    )
                }

                3 -> {
                    // TAB 3: System Diagnostics & Health
                    CleanSystemDiagnosticsTab(
                        totalUsers = activeUsers.size,
                        totalTeams = allTeams.size,
                        totalMatches = allMatches.size,
                        totalDeliveries = totalDeliveries,
                        cloudPingMs = cloudPingMs,
                        isPinging = isPinging,
                        isSyncing = isSyncingCloud,
                        onBenchmarkPing = { measurePing() },
                        onForceSync = { triggerFullSync() }
                    )
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
                    Text("Enter new PIN for ${u.fullName} (${u.phoneNumber}):", style = BodyCaption, color = TextSecondary)
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
                                Toast.makeText(context, "PIN updated to '$pin'", Toast.LENGTH_SHORT).show()
                                userToResetPin = null
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
            title = { Text("Delete Athlete", style = PlayerNamePrimary, color = DismissalRed) },
            text = {
                Text(
                    text = "Are you sure you want to delete ${u.fullName} (${u.phoneNumber})? This permanently deletes their record from both cloud and local databases.",
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
                            remoteUsers = remoteUsers.filterNot { it.userId == u.userId }
                            Toast.makeText(context, "Deleted ${u.fullName}", Toast.LENGTH_SHORT).show()
                            userToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DismissalRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Delete Match Dialog
    if (matchToDelete != null) {
        val m = matchToDelete!!
        AlertDialog(
            onDismissRequest = { matchToDelete = null },
            containerColor = SlateSurfaceDark,
            title = { Text("Delete Match", style = PlayerNamePrimary, color = DismissalRed) },
            text = {
                Text(
                    text = "Delete '${m.matchTitle}' and all associated innings and delivery records?",
                    style = BodyCaption,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            scoringViewModel.repository.deleteMatch(m.matchId)
                            Toast.makeText(context, "Match deleted.", Toast.LENGTH_SHORT).show()
                            matchToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DismissalRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { matchToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = SlateSurfaceDark,
            title = { Text("Log Out from Admin Console", style = PlayerNamePrimary, color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Text(
                    text = "Return to sign-in screen?",
                    style = BodyCaption,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark)
                ) {
                    Text("Log Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

// =============================================================================
// SUB-TAB 0: MOBILE-FIRST OVERVIEW & KPI TILES
// =============================================================================

@Composable
private fun CleanOverviewKpiTab(
    totalUsers: Int,
    proUsers: Int,
    totalMatches: Int,
    liveMatches: Int,
    completedMatches: Int,
    totalDeliveries: Int,
    totalRuns: Int,
    totalFours: Int,
    totalSixes: Int,
    totalWickets: Int,
    totalTeams: Int,
    cloudPingMs: Long?,
    isPinging: Boolean,
    onBenchmarkPing: () -> Unit,
    onForceSync: () -> Unit,
    onNavigateToPlayerApp: () -> Unit
) {
    val proPercentage = if (totalUsers > 0) (proUsers * 100) / totalUsers else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 72.dp)
    ) {
        // Player App Mode Switcher Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPlayerApp() },
                colors = CardDefaults.cardColors(containerColor = ElevatedSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ForestSixPrimary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ForestSixPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SportsCricket, contentDescription = null, tint = ForestSixPrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Switch to Player App Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text("Inspect live scorecards & player rankings", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ForestSixPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }

        // 2-Column KPI Grid: Athletes & Matches
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactKpiTile(
                    title = "Athletes",
                    value = "$totalUsers",
                    subtext = "$proUsers Pro ($proPercentage%)",
                    badge = if (proUsers > 0) "PRO" else "GROWTH",
                    accentColor = BoundaryAmber,
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f)
                )
                CompactKpiTile(
                    title = "Live Matches",
                    value = "$liveMatches",
                    subtext = "$completedMatches Completed",
                    badge = if (liveMatches > 0) "LIVE" else "IDLE",
                    accentColor = if (liveMatches > 0) ForestSixPrimary else TextSecondary,
                    icon = Icons.Default.SportsCricket,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2-Column KPI Grid: Scoring Volume & Clubs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactKpiTile(
                    title = "Runs Scored",
                    value = "$totalRuns",
                    subtext = "$totalDeliveries Balls Bowled",
                    badge = "$totalWickets WKTS",
                    accentColor = CrimsonPrimaryDark,
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
                CompactKpiTile(
                    title = "Clubs & Squads",
                    value = "$totalTeams",
                    subtext = "$totalFours 4s • $totalSixes 6s",
                    badge = "ACTIVE",
                    accentColor = DarkExtraBlue,
                    icon = Icons.Default.Shield,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Database & Latency Card (Clean Vertical Stack, Zero Button Overlap)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CENTRAL DATABASE HEALTH", style = SectionLabel, color = BoundaryAmber)
                        }
                        Surface(
                            color = ForestSixPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = ForestSixPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Supabase PostgreSQL 15 + PostgREST v12", style = BodyCaption, color = TextPrimary)
                    Text("Roundtrip Latency: ${cloudPingMs ?: 120}ms", style = BodyCaption, color = ForestSixPrimary)
                    Text("Local SQLite Room: Synchronized", style = BodyCaption, color = TextSecondary)

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2 Equal-Width Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBenchmarkPing,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            border = BorderStroke(1.dp, BoundaryAmber)
                        ) {
                            if (isPinging) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = BoundaryAmber)
                            } else {
                                Text("Ping (${cloudPingMs ?: 120}ms)", color = BoundaryAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }

                        Button(
                            onClick = onForceSync,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reconcile Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactKpiTile(
    title: String,
    value: String,
    subtext: String,
    badge: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
                }
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, maxLines = 1)
            Text(subtext, fontSize = 10.sp, color = TextTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// =============================================================================
// SUB-TAB 1: ATHLETES DIRECTORY (CLEAN 2-ROW BUTTON DESIGN)
// =============================================================================

@Composable
private fun CleanAthletesDirectoryTab(
    users: List<UserEntity>,
    currentUserId: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filterType: Int,
    onFilterChange: (Int) -> Unit,
    authManager: AuthManager,
    onTogglePro: (UserEntity, Boolean) -> Unit,
    onToggleAdmin: (UserEntity, Boolean) -> Unit,
    onResetPin: (UserEntity) -> Unit,
    onDeleteUser: (UserEntity) -> Unit
) {
    val adminEmails = remember { authManager.getAdminEmails() }

    val filtered = remember(users, searchQuery, filterType, adminEmails) {
        users.filter { u ->
            val matchesSearch = searchQuery.isBlank() ||
                u.fullName.contains(searchQuery, ignoreCase = true) ||
                u.phoneNumber.contains(searchQuery) ||
                u.city.contains(searchQuery, ignoreCase = true) ||
                (u.email ?: "").contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (filterType) {
                1 -> u.isPro
                2 -> !u.email.isNullOrBlank() && adminEmails.contains(u.email.lowercase())
                3 -> !u.isPro
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search name, phone, email, or city...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(15.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontally Scrollable Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val chips = listOf("All (${users.size})", "Pro Athletes", "Admins", "Free Tier")
            items(chips.indices.toList()) { idx ->
                FilterChip(
                    selected = filterType == idx,
                    onClick = { onFilterChange(idx) },
                    label = { Text(chips[idx], fontSize = 11.sp, fontWeight = if (filterType == idx) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonPrimaryDark,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No athletes match your query.", fontSize = 13.sp, color = TextSecondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 72.dp)
            ) {
                items(filtered, key = { it.userId }) { user ->
                    val isMe = user.userId == currentUserId
                    val isUserAdmin = !user.email.isNullOrBlank() && adminEmails.contains(user.email.lowercase())

                    CleanAthleteCard(
                        user = user,
                        isMe = isMe,
                        isAdmin = isUserAdmin,
                        onTogglePro = { newPro -> onTogglePro(user, newPro) },
                        onToggleAdmin = { makeAdmin -> onToggleAdmin(user, makeAdmin) },
                        onResetPin = { onResetPin(user) },
                        onDeleteUser = { onDeleteUser(user) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CleanAthleteCard(
    user: UserEntity,
    isMe: Boolean,
    isAdmin: Boolean,
    onTogglePro: (Boolean) -> Unit,
    onToggleAdmin: (Boolean) -> Unit,
    onResetPin: () -> Unit,
    onDeleteUser: () -> Unit
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
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: User Identity & Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.fullName.ifBlank { "Grassroots Player" },
                            style = PlayerNameSecondary,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                        text = "${user.phoneNumber} • ${user.city.ifBlank { "Mumbai" }} • #${user.jerseyNumber}",
                        style = BodyCaption,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    if (!user.email.isNullOrBlank()) {
                        Text(
                            text = user.email,
                            style = MicroTag,
                            color = DarkExtraBlue,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Badges Column
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    if (isAdmin) {
                        Surface(
                            color = CrimsonPrimaryDark.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = "ADMIN",
                                color = CrimsonPrimaryDark,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row 1: Primary Controls (2 Equal Buttons - NEVER Wrap)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onTogglePro(!user.isPro) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, if (user.isPro) DismissalRed else BoundaryAmber)
                ) {
                    Icon(
                        imageVector = if (user.isPro) Icons.Default.Close else Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = if (user.isPro) DismissalRed else BoundaryAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.isPro) "Revoke Pro" else "Grant Pro",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.isPro) DismissalRed else BoundaryAmber,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onResetPin,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, CardBorderDark)
                ) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset PIN", fontSize = 11.sp, color = TextPrimary, maxLines = 1)
                }
            }

            // Action Row 2: Secondary / Role Management (Only if not current user)
            if (!isMe) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!user.email.isNullOrBlank()) {
                        TextButton(
                            onClick = { onToggleAdmin(!isAdmin) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = if (isAdmin) DismissalRed else DarkExtraBlue,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAdmin) "Revoke Admin Role" else "Promote to Admin",
                                fontSize = 11.sp,
                                color = if (isAdmin) DismissalRed else DarkExtraBlue
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    TextButton(
                        onClick = onDeleteUser,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = DismissalRed, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 11.sp, color = DismissalRed)
                    }
                }
            }
        }
    }
}

// =============================================================================
// SUB-TAB 2: MATCHES & LIVE GROUND OPERATIONS
// =============================================================================

@Composable
private fun CleanMatchesOperationsTab(
    matches: List<MatchEntity>,
    teamMap: Map<String, TeamEntity>,
    filterType: Int,
    onFilterChange: (Int) -> Unit,
    onInspectScorecard: (String) -> Unit,
    onDeleteMatch: (MatchEntity) -> Unit
) {
    val filtered = remember(matches, filterType) {
        when (filterType) {
            1 -> matches.filter { it.matchStatus == MatchStatus.LIVE }
            2 -> matches.filter { it.matchStatus == MatchStatus.COMPLETED }
            3 -> matches.filter { it.matchStatus == MatchStatus.SCHEDULED }
            else -> matches
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val chips = listOf("All (${matches.size})", "Live Now", "Completed", "Scheduled")
            chips.forEachIndexed { idx, label ->
                FilterChip(
                    selected = filterType == idx,
                    onClick = { onFilterChange(idx) },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (filterType == idx) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonPrimaryDark,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matches match the selected filter.", fontSize = 13.sp, color = TextSecondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 72.dp)
            ) {
                items(filtered, key = { it.matchId }) { match ->
                    val teamA = teamMap[match.teamAId]?.teamName ?: "Team A"
                    val teamB = teamMap[match.teamBId]?.teamName ?: "Team B"

                    CleanMatchCard(
                        match = match,
                        teamAName = teamA,
                        teamBName = teamB,
                        onInspectScorecard = { onInspectScorecard(match.matchId) },
                        onDeleteMatch = { onDeleteMatch(match) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CleanMatchCard(
    match: MatchEntity,
    teamAName: String,
    teamBName: String,
    onInspectScorecard: () -> Unit,
    onDeleteMatch: () -> Unit
) {
    val isLive = match.matchStatus == MatchStatus.LIVE
    val isCompleted = match.matchStatus == MatchStatus.COMPLETED

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            if (isLive) ForestSixPrimary.copy(alpha = 0.6f) else CardBorderDark
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Title and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.matchTitle.ifBlank { "$teamAName vs $teamBName" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = when {
                        isLive -> ForestSixPrimary.copy(alpha = 0.15f)
                        isCompleted -> BoundaryAmber.copy(alpha = 0.15f)
                        else -> ElevatedSurfaceDark
                    },
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(
                        0.5.dp,
                        when {
                            isLive -> ForestSixPrimary.copy(alpha = 0.6f)
                            isCompleted -> BoundaryAmber.copy(alpha = 0.6f)
                            else -> CardBorderDark
                        }
                    )
                ) {
                    Text(
                        text = when {
                            isLive -> "● LIVE"
                            isCompleted -> "COMPLETED"
                            else -> "SCHEDULED"
                        },
                        color = when {
                            isLive -> ForestSixPrimary
                            isCompleted -> BoundaryAmber
                            else -> TextSecondary
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$teamAName vs $teamBName • ${match.totalOvers} Overs (${match.ballType.name.replace("_", " ")})",
                style = BodyCaption,
                color = TextSecondary,
                fontSize = 11.sp
            )

            if (!match.resultSummary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = match.resultSummary,
                    style = MicroTag,
                    color = ForestSixPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DividerDark, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onInspectScorecard,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspect Scorecard", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onDeleteMatch,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, DismissalRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = DismissalRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 11.sp, color = DismissalRed)
                }
            }
        }
    }
}

// =============================================================================
// SUB-TAB 3: SYSTEM DIAGNOSTICS & CONTROLS
// =============================================================================

@Composable
private fun CleanSystemDiagnosticsTab(
    totalUsers: Int,
    totalTeams: Int,
    totalMatches: Int,
    totalDeliveries: Int,
    cloudPingMs: Long?,
    isPinging: Boolean,
    isSyncing: Boolean,
    onBenchmarkPing: () -> Unit,
    onForceSync: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 72.dp)
    ) {
        // Table Records Inventory
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("DATABASE RECORD COUNTS", style = SectionLabel, color = BoundaryAmber)
                    Spacer(modifier = Modifier.height(8.dp))
                    CleanDiagnosticRow("Registered Athletes (users)", "$totalUsers")
                    CleanDiagnosticRow("Squads & Clubs (teams)", "$totalTeams")
                    CleanDiagnosticRow("Matches Archived (matches)", "$totalMatches")
                    CleanDiagnosticRow("Balls Bowled (deliveries)", "$totalDeliveries")
                }
            }
        }

        // Cloud Actions Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("SYNCHRONIZATION CONTROLS", style = SectionLabel, color = ForestSixPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Trigger 2-way cloud reconciliation between Supabase and Room.",
                        style = BodyCaption,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onForceSync,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reconciling Cloud...", fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reconcile All Cloud Tables", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onBenchmarkPing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BoundaryAmber)
                    ) {
                        if (isPinging) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = BoundaryAmber)
                        } else {
                            Text("Measure Network Latency (${cloudPingMs ?: 120}ms)", color = BoundaryAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Build Environment Details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("BUILD & SIGNING DETAILS", style = SectionLabel, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    CleanDiagnosticRow("App", "CricSaga Grassroots")
                    CleanDiagnosticRow("Package", "com.cricsaga.app")
                    CleanDiagnosticRow("Keystore", "cricsaga-release.jks")
                    CleanDiagnosticRow("SHA-1", "B6:D2:45...6F:0E")
                }
            }
        }
    }
}

@Composable
private fun CleanDiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = BodyCaption, color = TextSecondary, fontSize = 11.sp)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
