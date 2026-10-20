package com.cricsaga.app.ui.navigation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.cricsaga.app.data.local.AuthManager
import com.cricsaga.app.data.local.AuthState
import com.cricsaga.app.data.local.ProManager
import com.cricsaga.app.data.local.entity.LookingForEntity
import com.cricsaga.app.data.local.entity.TeamEntity
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.BallType
import com.cricsaga.app.domain.model.ExtraType
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.domain.model.TossDecision
import com.cricsaga.app.ui.auth.PhoneLoginScreen
import com.cricsaga.app.ui.auth.ProfileSetupScreen
import com.cricsaga.app.ui.discover.DiscoverScreen
import com.cricsaga.app.ui.discover.LookingForItem
import com.cricsaga.app.ui.home.HomeScreen
import com.cricsaga.app.ui.matches.MatchListScreen
import com.cricsaga.app.ui.profile.PlayerDetailScreen
import com.cricsaga.app.ui.profile.PlayerProfileScreen
import com.cricsaga.app.ui.redeem.RedeemCodeScreen
import com.cricsaga.app.ui.scorecard.FullScorecardScreen
import com.cricsaga.app.ui.scoring.ScoringScreen
import com.cricsaga.app.ui.scoring.ScoringViewModel
import com.cricsaga.app.ui.scoring.dialog.BowlerSelectionDialog
import com.cricsaga.app.ui.scoring.dialog.DismissalBottomSheet
import com.cricsaga.app.ui.scoring.dialog.InningsBreakDialog
import com.cricsaga.app.ui.scoring.dialog.MatchResultDialog
import com.cricsaga.app.ui.scoring.dialog.SecondInningsOpenersDialog
import com.cricsaga.app.ui.settings.SettingsScreen
import com.cricsaga.app.ui.utils.BroadcastNotifier
import com.cricsaga.app.ui.utils.HapticManager
import com.cricsaga.app.ui.utils.ShareUtils
import com.cricsaga.app.ui.setup.MatchSetupScreen
import com.cricsaga.app.ui.setup.OpenersSelectionScreen
import com.cricsaga.app.ui.teams.TeamDetailScreen
import com.cricsaga.app.ui.teams.TeamsListScreen
import com.cricsaga.app.ui.tournaments.TournamentListScreen
import com.cricsaga.app.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    scoringViewModel: ScoringViewModel,
    proManager: ProManager,
    authManager: AuthManager,
    themeManager: ThemeManager,
    hapticManager: HapticManager,
    isDark: Boolean = false
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val coroutineScope = rememberCoroutineScope()
    val subscriptionState by proManager.subscriptionState.collectAsState()
    val authState by authManager.authState.collectAsState()
    val themeMode by themeManager.themeMode.collectAsState()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val currentDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemDark
    }
    var tempAuthPhone by remember { mutableStateOf("") }
    var tempGoogleName by remember { mutableStateOf("") }
    var tempGoogleEmail by remember { mutableStateOf("") }
    var selectedTeamForDetail by remember { mutableStateOf<TeamEntity?>(null) }
    var selectedUserForDetail by remember { mutableStateOf<UserEntity?>(null) }

    // 5 Primary Bottom Navigation Bar Tabs
    val bottomBarScreens = listOf(
        Screen.Home,
        Screen.Matches,
        Screen.Discover,
        Screen.Tournaments,
        Screen.Profile
    )
    val showBottomBar = currentRoute in bottomBarScreens.map { it.route } && authState !is AuthState.Unauthenticated

    // Transient Match Setup State across screens
    var tempTitle by remember { mutableStateOf("") }
    var tempTeamAId by remember { mutableStateOf("") }
    var tempTeamBId by remember { mutableStateOf("") }
    var tempOvers by remember { mutableIntStateOf(20) }
    var tempBallType by remember { mutableStateOf(BallType.TENNIS_LIGHT) }
    var tempTossWinnerId by remember { mutableStateOf("") }
    var tempTossDecision by remember { mutableStateOf(TossDecision.BAT) }

    var tempBattingSquad by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var tempBowlingSquad by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var tempBattingTeamName by remember { mutableStateOf("") }
    var tempBowlingTeamName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = CharcoalBackgroundDark,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = CharcoalBackgroundDark,
                    tonalElevation = 0.dp
                ) {
                    bottomBarScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                hapticManager.tapUiClick()
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                screen.icon?.let {
                                    Icon(
                                        it,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) TextPrimary else TextTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    color = if (isSelected) TextPrimary else TextTertiary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = ElevatedSurfaceDark,
                                selectedIconColor = TextPrimary,
                                unselectedIconColor = TextTertiary,
                                selectedTextColor = TextPrimary,
                                unselectedTextColor = TextTertiary
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (authState is AuthState.Unauthenticated) Screen.PhoneLogin.route else Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Auth Flow: Google Sign-In & Pitch Guest Scorer
            composable(Screen.PhoneLogin.route) {
                PhoneLoginScreen(
                    onGoogleSignIn = { email, displayName ->
                        coroutineScope.launch {
                            val allUsers = scoringViewModel.repository.getAllUsers().first()
                            val existingUser = allUsers.find {
                                it.userId == "user_${email.replace("@", "_").replace(".", "_")}" ||
                                it.fullName.equals(displayName, ignoreCase = true)
                            }
                            if (existingUser != null) {
                                authManager.setLoggedIn(existingUser.userId, existingUser.phoneNumber)
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                tempGoogleName = displayName
                                tempGoogleEmail = email
                                tempAuthPhone = ""
                                navController.navigate(Screen.ProfileSetup.route)
                            }
                        }
                    },
                    onContinueAsGuest = {
                        authManager.setGuestMode()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onPhoneVerified = { phone ->
                        coroutineScope.launch {
                            val existingUser = scoringViewModel.repository.getUserByPhone(phone)
                            if (existingUser != null) {
                                authManager.setLoggedIn(existingUser.userId, phone)
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                tempAuthPhone = phone
                                navController.navigate(Screen.ProfileSetup.route)
                            }
                        }
                    }
                )
            }

            // Auth Flow: Profile Setup
            composable(Screen.ProfileSetup.route) {
                ProfileSetupScreen(
                    phoneNumber = tempAuthPhone,
                    initialFullName = tempGoogleName,
                    initialEmail = tempGoogleEmail,
                    onProfileCreated = { newUser ->
                        coroutineScope.launch {
                            scoringViewModel.repository.insertUser(newUser)
                            authManager.setLoggedIn(newUser.userId, newUser.phoneNumber)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Tab 1: Home Screen
            composable(Screen.Home.route) {
                val matches by scoringViewModel.allMatches.collectAsState()
                val topPerformers by scoringViewModel.topPerformers.collectAsState()
                HomeScreen(
                    isPro = subscriptionState.isActive,
                    matches = matches,
                    topPerformers = topPerformers,
                    isDarkTheme = currentDark,
                    onThemeToggleClick = {
                        hapticManager.tapUiClick()
                        themeManager.toggleTheme(currentDark)
                    },
                    onSettingsClick = {
                        hapticManager.tapUiClick()
                        navController.navigate(Screen.Settings.route)
                    },
                    onStartMatchClick = {
                        hapticManager.tapUiClick()
                        navController.navigate(Screen.MatchSetup.route)
                    },
                    onManageTeamsClick = {
                        hapticManager.tapUiClick()
                        navController.navigate(Screen.Teams.route)
                    },
                    onExploreTournamentsClick = {
                        hapticManager.tapUiClick()
                        navController.navigate(Screen.Tournaments.route)
                    },
                    onProBadgeClick = {
                        hapticManager.tapUiClick()
                        navController.navigate(Screen.Redeem.route)
                    },
                    onMatchClick = { matchId ->
                        hapticManager.tapUiClick()
                        scoringViewModel.loadMatchById(matchId) { isLive ->
                            if (isLive) {
                                navController.navigate(Screen.LiveScoring.route)
                            } else {
                                navController.navigate(Screen.FullScorecard.route)
                            }
                        }
                    }
                )
            }

            // Tab 2: Matches Hub
            composable(Screen.Matches.route) {
                val matches by scoringViewModel.allMatches.collectAsState()
                MatchListScreen(
                    matches = matches,
                    onStartNewMatchClick = {
                        hapticManager.tapUiClick()
                        navController.navigate(Screen.MatchSetup.route)
                    },
                    onMatchClick = { matchId ->
                        hapticManager.tapUiClick()
                        scoringViewModel.loadMatchById(matchId) { isLive ->
                            if (isLive) {
                                navController.navigate(Screen.LiveScoring.route)
                            } else {
                                navController.navigate(Screen.FullScorecard.route)
                            }
                        }
                    }
                )
            }

            // Tab 3: Discover Grassroots Cricket Hub
            composable(Screen.Discover.route) {
                val context = LocalContext.current
                val lookingForEntities by scoringViewModel.lookingForPosts.collectAsState()
                val lookingForItems = remember(lookingForEntities) {
                    lookingForEntities.map { entity ->
                        LookingForItem(
                            id = entity.id,
                            type = entity.type,
                            title = entity.title,
                            teamOrOrganizer = entity.teamOrOrganizer,
                            timing = entity.timing,
                            location = entity.location,
                            note = entity.note,
                            contactPhone = entity.contactPhone
                        )
                    }
                }
                DiscoverScreen(
                    isPro = subscriptionState.isActive,
                    lookingForList = lookingForItems,
                    onPostRequirement = { type, title, organizer, timing, location, note, phone ->
                        scoringViewModel.createLookingForPost(
                            type = type,
                            title = title,
                            teamOrOrganizer = organizer,
                            timing = timing,
                            location = location,
                            note = note,
                            contactPhone = phone
                        )
                    },
                    onContactWhatsApp = { phone, message ->
                        hapticManager.tapUiClick()
                        val digits = phone.filter { it.isDigit() }
                        val formattedPhone = if (digits.length == 10) "91$digits" else digits
                        val encodedMessage = Uri.encode(message)
                        val uri = Uri.parse("https://wa.me/$formattedPhone?text=$encodedMessage")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open WhatsApp. Dialing phone number...", Toast.LENGTH_SHORT).show()
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$formattedPhone")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(dialIntent)
                            } catch (e2: Exception) {
                                Toast.makeText(context, "No app available to handle this request", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            // Tab 4: Tournaments & Points Table Center
            composable(Screen.Tournaments.route) {
                val tournaments by scoringViewModel.repository.getAllTournaments().collectAsState(initial = emptyList())
                val matches by scoringViewModel.allMatches.collectAsState()
                TournamentListScreen(
                    isPro = subscriptionState.isActive,
                    tournaments = tournaments,
                    matches = matches,
                    onCreateTournament = { name, overs, teams ->
                        coroutineScope.launch {
                            scoringViewModel.repository.createTournament(name, overs, teams)
                        }
                    },
                    onFixtureClick = { _, _, _ ->
                        hapticManager.tapUiClick()
                        navController.navigate(Screen.MatchSetup.route)
                    }
                )
            }

            // Tab 5: My Career Player Profile
            composable(Screen.Profile.route) {
                val currentUserId = authManager.getCurrentUserId()
                val currentUser by scoringViewModel.repository.getUserFlowById(currentUserId).collectAsState(initial = null)
                val playerStats by scoringViewModel.repository.getPlayerStatsFlow(currentUserId).collectAsState(initial = null)
                PlayerProfileScreen(
                    proManager = proManager,
                    currentUser = currentUser,
                    playerStats = playerStats,
                    onRedeemCodeClick = {
                        hapticManager.tapUiClick()
                        navController.navigate(Screen.Redeem.route)
                    },
                    onUpdateProfile = { updated ->
                        coroutineScope.launch {
                            scoringViewModel.repository.updateUser(updated)
                        }
                    },
                    onLogout = {
                        hapticManager.tapUiClick()
                        authManager.logout()
                        navController.navigate(Screen.PhoneLogin.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Flow: Teams & Players Squad Manager
            composable(Screen.Teams.route) {
                val teams by scoringViewModel.allTeams.collectAsState()
                TeamsListScreen(
                    teams = teams,
                    onTeamClick = { team ->
                        hapticManager.tapUiClick()
                        selectedTeamForDetail = team
                        navController.navigate(Screen.TeamDetail.route)
                    },
                    onCreateTeam = { name, code, city ->
                        coroutineScope.launch {
                            scoringViewModel.repository.createTeam(name, code, city)
                        }
                    },
                    onAddPlayer = { teamId, name, role ->
                        coroutineScope.launch {
                            scoringViewModel.repository.addPlayerToTeam(teamId, name, role = role)
                        }
                    }
                )
            }

            // Flow: Team Squad Detail
            composable(Screen.TeamDetail.route) {
                selectedTeamForDetail?.let { team ->
                    val squad by scoringViewModel.repository.getTeamMembers(team.teamId).collectAsState(initial = emptyList())
                    TeamDetailScreen(
                        team = team,
                        squad = squad,
                        onAddPlayer = { name, jersey, role ->
                            coroutineScope.launch {
                                scoringViewModel.repository.addPlayerToTeam(
                                    teamId = team.teamId,
                                    name = name,
                                    role = role,
                                    jerseyNumber = jersey
                                )
                            }
                        },
                        onRemovePlayer = { player ->
                            coroutineScope.launch {
                                scoringViewModel.repository.removePlayerFromTeam(team.teamId, player.userId)
                            }
                        },
                        onSetCaptain = { player ->
                            coroutineScope.launch {
                                scoringViewModel.repository.setTeamCaptain(team.teamId, player.userId)
                            }
                        },
                        onPlayerClick = { player ->
                            hapticManager.tapUiClick()
                            selectedUserForDetail = player
                            navController.navigate(Screen.PlayerDetail.route)
                        },
                        onBackClick = {
                            hapticManager.tapUiClick()
                            navController.popBackStack()
                        }
                    )
                }
            }

            // Flow: Public Player Profile Detail
            composable(Screen.PlayerDetail.route) {
                selectedUserForDetail?.let { player ->
                    val stats by scoringViewModel.repository.getPlayerStatsFlow(player.userId).collectAsState(initial = null)
                    PlayerDetailScreen(
                        user = player,
                        stats = stats,
                        onBackClick = {
                            hapticManager.tapUiClick()
                            navController.popBackStack()
                        }
                    )
                }
            }

            // Flow: App Settings & Preferences
            composable(Screen.Settings.route) {
                SettingsScreen(
                    themeManager = themeManager,
                    hapticManager = hapticManager,
                    onBackClick = {
                        hapticManager.tapUiClick()
                        navController.popBackStack()
                    }
                )
            }

            // Flow: Pro Redeem Code & Pricing Screen
            composable(Screen.Redeem.route) {
                RedeemCodeScreen(proManager = proManager)
            }

            // Flow: Match Setup Form
            composable(Screen.MatchSetup.route) {
                val teams by scoringViewModel.allTeams.collectAsState()
                MatchSetupScreen(
                    teams = teams,
                    onBackClick = {
                        hapticManager.tapUiClick()
                        navController.popBackStack()
                    },
                    onCreateTeam = { name, code ->
                        coroutineScope.launch {
                            scoringViewModel.repository.createTeam(name, code, "Mumbai")
                        }
                    },
                    onProceedToOpeners = { title, teamA, teamB, overs, ballType, tossWinner, decision ->
                        tempTitle = title
                        tempTeamAId = teamA
                        tempTeamBId = teamB
                        tempOvers = overs
                        tempBallType = ballType
                        tempTossWinnerId = tossWinner
                        tempTossDecision = decision

                        // Resolve Batting vs Bowling Squads
                        val battingTeamId = if (decision == TossDecision.BAT) tossWinner else (if (tossWinner == teamA) teamB else teamA)
                        val bowlingTeamId = if (battingTeamId == teamA) teamB else teamA

                        val teamAObj = teams.find { it.teamId == teamA }
                        val teamBObj = teams.find { it.teamId == teamB }
                        tempBattingTeamName = if (battingTeamId == teamA) teamAObj?.teamName ?: "Team A" else teamBObj?.teamName ?: "Team B"
                        tempBowlingTeamName = if (bowlingTeamId == teamA) teamAObj?.teamName ?: "Team A" else teamBObj?.teamName ?: "Team B"

                        coroutineScope.launch {
                            scoringViewModel.repository.getTeamMembers(battingTeamId).collect { bSquad ->
                                tempBattingSquad = bSquad
                            }
                        }
                        coroutineScope.launch {
                            scoringViewModel.repository.getTeamMembers(bowlingTeamId).collect { bowSquad ->
                                tempBowlingSquad = bowSquad
                            }
                        }

                        navController.navigate(Screen.OpenersSelection.route)
                    }
                )
            }

            // Flow: Openers Selection
            composable(Screen.OpenersSelection.route) {
                val currentBattingSquad = if (tempBattingSquad.isNotEmpty()) tempBattingSquad else scoringViewModel.uiState.value.battingSquad
                val currentBowlingSquad = if (tempBowlingSquad.isNotEmpty()) tempBowlingSquad else scoringViewModel.uiState.value.bowlingSquad

                OpenersSelectionScreen(
                    battingTeamName = tempBattingTeamName.ifEmpty { "Batting Team" },
                    bowlingTeamName = tempBowlingTeamName.ifEmpty { "Bowling Team" },
                    battingSquad = currentBattingSquad,
                    bowlingSquad = currentBowlingSquad,
                    onBackClick = { navController.popBackStack() },
                    onStartMatch = { strikerId, nonStrikerId, bowlerId ->
                        val striker = currentBattingSquad.find { it.userId == strikerId } ?: currentBattingSquad.first()
                        val nonStriker = currentBattingSquad.find { it.userId == nonStrikerId } ?: currentBattingSquad[1]
                        val bowler = currentBowlingSquad.find { it.userId == bowlerId } ?: currentBowlingSquad.first()

                        scoringViewModel.startCustomMatch(
                            title = tempTitle.ifEmpty { "Match" },
                            teamAId = tempTeamAId,
                            teamBId = tempTeamBId,
                            totalOvers = tempOvers,
                            ballType = tempBallType,
                            tossWinnerId = tempTossWinnerId,
                            tossDecision = tempTossDecision,
                            striker = striker,
                            nonStriker = nonStriker,
                            bowler = bowler,
                            battingSquad = currentBattingSquad,
                            bowlingSquad = currentBowlingSquad
                        )
                        navController.navigate(Screen.LiveScoring.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            // Flow: Live Scoring Screen
            composable(Screen.LiveScoring.route) {
                val uiState by scoringViewModel.uiState.collectAsState()

                ScoringScreen(
                    state = uiState,
                    onBallTapped = { runs: Int ->
                        when (runs) {
                            4 -> hapticManager.tapFour()
                            6 -> hapticManager.tapSix()
                            else -> hapticManager.tapDotOrSingle()
                        }
                        scoringViewModel.recordDelivery(runs)
                    },
                    onExtraTapped = { extra: ExtraType ->
                        hapticManager.tapDotOrSingle()
                        scoringViewModel.recordExtra(extra)
                    },
                    onWicketTapped = {
                        hapticManager.tapWicket()
                        scoringViewModel.openDismissalDialog()
                    },
                    onUndoTapped = {
                        hapticManager.tapUndo()
                        scoringViewModel.undo()
                        BroadcastNotifier.postRevert("Delivery undone from scorecard")
                    },
                    onRotateStrikeTapped = {
                        hapticManager.tapUiClick()
                        scoringViewModel.rotateStrike()
                    },
                    onViewScorecardClick = {
                        hapticManager.tapUiClick()
                        navController.navigate(Screen.FullScorecard.route)
                    }
                )

                // Dismissal Modal Bottom Sheet
                if (uiState.showDismissalDialog) {
                    DismissalBottomSheet(
                        striker = uiState.striker,
                        nonStriker = uiState.nonStriker,
                        fieldingSquad = uiState.bowlingSquad,
                        battingBench = uiState.battingBench,
                        onDismissRequest = { scoringViewModel.closeDismissalDialog() },
                        onConfirmWicket = { dismissal, incomingId ->
                            hapticManager.tapWicket()
                            scoringViewModel.confirmDismissal(dismissal, incomingId)
                        }
                    )
                }

                // End of Over Bowler Selection Dialog
                if (uiState.showBowlerDialog) {
                    BowlerSelectionDialog(
                        lastBowlerId = uiState.lastCompletedBowlerId,
                        bowlingSquad = uiState.bowlingSquad,
                        onBowlerSelected = { nextBowler ->
                            hapticManager.tapUiClick()
                            scoringViewModel.onNextBowlerSelected(nextBowler)
                        }
                    )
                }

                // 1st Innings Break Dialog
                if (uiState.showInningsBreakDialog && uiState.innings1Score != null) {
                    InningsBreakDialog(
                        battingTeamName = uiState.innings1BattingTeamName,
                        bowlingTeamName = uiState.bowlingTeamName,
                        firstInningsState = uiState.innings1Score!!,
                        totalOvers = uiState.totalOvers,
                        onStartSecondInnings = {
                            hapticManager.tapUiClick()
                            scoringViewModel.onStartSecondInningsClicked()
                        }
                    )
                }

                // 2nd Innings Openers Picker Dialog
                if (uiState.showSecondInningsOpenersDialog) {
                    val targetRuns = (uiState.innings1Score?.totalRuns ?: 0) + 1
                    SecondInningsOpenersDialog(
                        battingTeamName = uiState.bowlingTeamName,
                        bowlingTeamName = uiState.battingTeamName,
                        targetRuns = targetRuns,
                        totalOvers = uiState.totalOvers,
                        battingSquad = uiState.bowlingSquad,
                        bowlingSquad = uiState.battingSquad,
                        onConfirm = { striker, nonStriker, bowler ->
                            hapticManager.tapUiClick()
                            scoringViewModel.confirmSecondInningsStart(striker, nonStriker, bowler)
                        }
                    )
                }

                // Match Result Celebration Dialog
                if (uiState.showMatchResultDialog && uiState.matchResult != null) {
                    val context = LocalContext.current
                    MatchResultDialog(
                        matchResult = uiState.matchResult!!,
                        innings1TeamName = uiState.innings1BattingTeamName.ifEmpty { "1st Innings" },
                        innings1Score = uiState.innings1Score,
                        innings2TeamName = uiState.battingTeamName,
                        innings2Score = uiState.scoreState,
                        candidates = uiState.playerOfTheMatchCandidates,
                        selectedPotm = uiState.selectedPlayerOfTheMatch,
                        onPotmSelected = { player ->
                            hapticManager.tapUiClick()
                            scoringViewModel.setPlayerOfTheMatch(player)
                        },
                        onViewScorecard = {
                            hapticManager.tapUiClick()
                            scoringViewModel.dismissMatchResultDialog()
                            navController.navigate(Screen.FullScorecard.route)
                        },
                        onShareToWhatsApp = {
                            hapticManager.tapUiClick()
                            ShareUtils.generateAndSharePoster(
                                context = context,
                                matchTitle = uiState.matchTitle,
                                battingTeamName = uiState.battingTeamName,
                                bowlingTeamName = uiState.bowlingTeamName,
                                scoreState = uiState.scoreState,
                                firstInningsScore = uiState.innings1Score,
                                firstInningsTeamName = uiState.innings1BattingTeamName,
                                matchResult = uiState.matchResult
                            )
                        },
                        onDismiss = {
                            scoringViewModel.dismissMatchResultDialog()
                        }
                    )
                }
            }

            // Flow: Full Match Scorecard Center
            composable(Screen.FullScorecard.route) {
                val uiState by scoringViewModel.uiState.collectAsState()
                val context = LocalContext.current

                FullScorecardScreen(
                    matchTitle = uiState.matchTitle,
                    battingTeamName = uiState.battingTeamName,
                    bowlingTeamName = uiState.bowlingTeamName,
                    scoreState = uiState.scoreState,
                    innings1Score = uiState.innings1Score,
                    innings1BattingTeamName = uiState.innings1BattingTeamName,
                    currentInningsDeliveries = uiState.currentInningsDeliveries,
                    innings1Deliveries = uiState.innings1Deliveries,
                    onBackClick = {
                        hapticManager.tapUiClick()
                        navController.popBackStack()
                    },
                    onSharePoster = {
                        hapticManager.tapUiClick()
                        ShareUtils.generateAndSharePoster(
                            context = context,
                            matchTitle = uiState.matchTitle,
                            battingTeamName = uiState.battingTeamName,
                            bowlingTeamName = uiState.bowlingTeamName,
                            scoreState = uiState.scoreState,
                            firstInningsScore = uiState.innings1Score,
                            firstInningsTeamName = uiState.innings1BattingTeamName,
                            matchResult = uiState.matchResult
                        )
                    }
                )
            }
        }
    }
}
