package com.cricsaga.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.cricsaga.app.data.local.ProManager
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.BallType
import com.cricsaga.app.domain.model.ExtraType
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.domain.model.TossDecision
import com.cricsaga.app.ui.matches.MatchListScreen
import com.cricsaga.app.ui.redeem.RedeemCodeScreen
import com.cricsaga.app.ui.scorecard.FullScorecardScreen
import com.cricsaga.app.ui.scoring.ScoringScreen
import com.cricsaga.app.ui.scoring.ScoringViewModel
import com.cricsaga.app.ui.scoring.dialog.BowlerSelectionDialog
import com.cricsaga.app.ui.scoring.dialog.DismissalBottomSheet
import com.cricsaga.app.ui.setup.MatchSetupScreen
import com.cricsaga.app.ui.setup.OpenersSelectionScreen
import com.cricsaga.app.ui.teams.TeamsListScreen
import com.cricsaga.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    scoringViewModel: ScoringViewModel,
    proManager: ProManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val coroutineScope = rememberCoroutineScope()

    val bottomBarScreens = listOf(Screen.Matches, Screen.Teams, Screen.Redeem)
    val showBottomBar = currentRoute in bottomBarScreens.map { it.route }

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
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = SlateSurfaceDark,
                    contentColor = Color.White
                ) {
                    bottomBarScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
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
                                    Icon(it, contentDescription = screen.title, tint = if (isSelected) CrimsonPrimaryDark else MutedGrey)
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    color = if (isSelected) Color.White else MutedGrey,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = ElevatedSurfaceDark
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Matches.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Tab 1: Matches List
            composable(Screen.Matches.route) {
                val matches by scoringViewModel.allMatches.collectAsState()
                MatchListScreen(
                    matches = matches,
                    onStartNewMatchClick = { navController.navigate(Screen.MatchSetup.route) },
                    onMatchClick = { matchId ->
                        navController.navigate(Screen.LiveScoring.route)
                    }
                )
            }

            // Tab 2: Teams & Players List
            composable(Screen.Teams.route) {
                val teams by scoringViewModel.allTeams.collectAsState()
                TeamsListScreen(
                    teams = teams,
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

            // Tab 3: Pro Redeem Code
            composable(Screen.Redeem.route) {
                RedeemCodeScreen(proManager = proManager)
            }

            // Flow: Match Setup Form
            composable(Screen.MatchSetup.route) {
                val teams by scoringViewModel.allTeams.collectAsState()
                MatchSetupScreen(
                    teams = teams,
                    onBackClick = { navController.popBackStack() },
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
                            popUpTo(Screen.Matches.route)
                        }
                    }
                )
            }

            // Flow: Live Scoring Screen
            composable(Screen.LiveScoring.route) {
                val uiState by scoringViewModel.uiState.collectAsState()

                ScoringScreen(
                    state = uiState,
                    onBallTapped = { runs: Int -> scoringViewModel.recordDelivery(runs) },
                    onExtraTapped = { extra: ExtraType -> scoringViewModel.recordExtra(extra) },
                    onWicketTapped = { scoringViewModel.openDismissalDialog() },
                    onUndoTapped = { scoringViewModel.undo() },
                    onRotateStrikeTapped = { scoringViewModel.rotateStrike() },
                    onViewScorecardClick = { navController.navigate(Screen.FullScorecard.route) }
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
                            scoringViewModel.onNextBowlerSelected(nextBowler)
                        }
                    )
                }
            }

            // Flow: Full Match Scorecard Center
            composable(Screen.FullScorecard.route) {
                val uiState by scoringViewModel.uiState.collectAsState()

                FullScorecardScreen(
                    matchTitle = uiState.matchTitle,
                    battingTeamName = uiState.battingTeamName,
                    bowlingTeamName = uiState.bowlingTeamName,
                    scoreState = uiState.scoreState,
                    onBackClick = { navController.popBackStack() },
                    onSharePoster = {
                        // Triggers share action
                    }
                )
            }
        }
    }
}
