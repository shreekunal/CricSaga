package com.cricsaga.app.ui.scoring

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cricsaga.app.data.local.CricSagaDatabase
import com.cricsaga.app.data.local.entity.DeliveryEntity
import com.cricsaga.app.data.local.entity.MatchEntity
import com.cricsaga.app.data.local.entity.TeamEntity
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.data.repository.MatchRepository
import com.cricsaga.app.data.repository.SeededMatchData
import com.cricsaga.app.domain.engine.MatchResultCalculator
import com.cricsaga.app.domain.engine.ScoringEngine
import com.cricsaga.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class ScoringUiState(
    val isLoading: Boolean = true,
    val matchId: String = "",
    val inningsId: String = "",
    val inningsNumber: Int = 1,
    val matchTitle: String = "Loading...",
    val teamAId: String = "",
    val teamBId: String = "",
    val battingTeamId: String = "",
    val bowlingTeamId: String = "",
    val battingTeamName: String = "",
    val bowlingTeamName: String = "",
    val totalOvers: Int = 20,
    val scoreState: InningsScoreState = InningsScoreState(),
    val innings1Score: InningsScoreState? = null,
    val innings1BattingTeamName: String = "",
    val striker: UserEntity? = null,
    val nonStriker: UserEntity? = null,
    val bowler: UserEntity? = null,
    val battingSquad: List<UserEntity> = emptyList(),
    val bowlingSquad: List<UserEntity> = emptyList(),
    val battingBench: List<UserEntity> = emptyList(),
    val showDismissalDialog: Boolean = false,
    val showBowlerDialog: Boolean = false,
    val showInningsBreakDialog: Boolean = false,
    val showSecondInningsOpenersDialog: Boolean = false,
    val showMatchResultDialog: Boolean = false,
    val matchResult: MatchResult? = null,
    val playerOfTheMatchCandidates: List<UserEntity> = emptyList(),
    val selectedPlayerOfTheMatch: UserEntity? = null,
    val lastCompletedBowlerId: String = "",
    val lastActionMessage: String = "Ready to score"
)

class ScoringViewModel(application: Application) : AndroidViewModel(application) {

    val repository: MatchRepository
    private var engine: ScoringEngine? = null
    private var activeMatchData: SeededMatchData? = null

    val allMatches: StateFlow<List<MatchEntity>>
    val allTeams: StateFlow<List<TeamEntity>>

    private val _uiState = MutableStateFlow(ScoringUiState())
    val uiState: StateFlow<ScoringUiState> = _uiState.asStateFlow()

    init {
        val database = CricSagaDatabase.getDatabase(application)
        repository = MatchRepository(database)

        allMatches = repository.getAllMatches().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allTeams = repository.getAllTeams().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        loadActiveMatch()
    }

    private fun loadActiveMatch() {
        viewModelScope.launch {
            val data = repository.seedInitialDataIfEmpty()
            activeMatchData = data

            val playerNames = mapOf(
                data.striker.userId to data.striker.fullName,
                data.nonStriker.userId to data.nonStriker.fullName,
                data.bowler.userId to data.bowler.fullName,
                data.incomingBatsman.userId to data.incomingBatsman.fullName
            )

            val scoringEngine = ScoringEngine(
                matchId = data.matchId,
                inningsNumber = 1,
                totalOversAllotted = 20,
                playerNames = playerNames
            )
            scoringEngine.startInnings(
                strikerId = data.striker.userId,
                nonStrikerId = data.nonStriker.userId,
                openingBowlerId = data.bowler.userId
            )
            engine = scoringEngine

            val battingSquad = listOf(data.striker, data.nonStriker, data.incomingBatsman)
            val bowlingSquad = listOf(
                data.bowler,
                UserEntity("user_hardik", "9876543214", "Hardik Pandya", playingRole = PlayingRole.ALL_ROUNDER),
                UserEntity("user_shami", "9876543215", "Mohammed Shami", playingRole = PlayingRole.BOWLER)
            )

            val bench = battingSquad.filter { it.userId != data.striker.userId && it.userId != data.nonStriker.userId }

            _uiState.value = ScoringUiState(
                isLoading = false,
                matchId = data.matchId,
                inningsId = data.inningsId,
                inningsNumber = 1,
                matchTitle = "Warriors vs Tigers",
                teamAId = "team_warriors",
                teamBId = "team_tigers",
                battingTeamId = "team_warriors",
                bowlingTeamId = "team_tigers",
                battingTeamName = data.teamAName,
                bowlingTeamName = data.teamBName,
                totalOvers = 20,
                scoreState = scoringEngine.state,
                striker = data.striker,
                nonStriker = data.nonStriker,
                bowler = data.bowler,
                battingSquad = battingSquad,
                bowlingSquad = bowlingSquad,
                battingBench = bench,
                lastActionMessage = "Match loaded: ${data.striker.fullName} on strike"
            )
        }
    }

    fun startCustomMatch(
        title: String,
        teamAId: String,
        teamBId: String,
        totalOvers: Int,
        ballType: BallType,
        tossWinnerId: String,
        tossDecision: TossDecision,
        striker: UserEntity,
        nonStriker: UserEntity,
        bowler: UserEntity,
        battingSquad: List<UserEntity>,
        bowlingSquad: List<UserEntity>
    ) {
        viewModelScope.launch {
            val result = repository.createMatch(
                title = title,
                teamAId = teamAId,
                teamBId = teamBId,
                totalOvers = totalOvers,
                ballType = ballType,
                tossWinnerTeamId = tossWinnerId,
                tossDecision = tossDecision
            )

            val playerNames = (battingSquad + bowlingSquad).associate { it.userId to it.fullName }

            val scoringEngine = ScoringEngine(
                matchId = result.matchId,
                inningsNumber = 1,
                totalOversAllotted = totalOvers,
                playerNames = playerNames
            )
            scoringEngine.startInnings(
                strikerId = striker.userId,
                nonStrikerId = nonStriker.userId,
                openingBowlerId = bowler.userId
            )
            engine = scoringEngine

            val teamA = repository.getTeam(teamAId)
            val teamB = repository.getTeam(teamBId)
            val battingTeamName = if (result.battingTeamId == teamAId) teamA?.teamName ?: "Team A" else teamB?.teamName ?: "Team B"
            val bowlingTeamName = if (result.bowlingTeamId == teamAId) teamA?.teamName ?: "Team A" else teamB?.teamName ?: "Team B"

            val bench = battingSquad.filter { it.userId != striker.userId && it.userId != nonStriker.userId }

            _uiState.value = ScoringUiState(
                isLoading = false,
                matchId = result.matchId,
                inningsId = result.inningsId,
                inningsNumber = 1,
                matchTitle = title,
                teamAId = teamAId,
                teamBId = teamBId,
                battingTeamId = result.battingTeamId,
                bowlingTeamId = result.bowlingTeamId,
                battingTeamName = battingTeamName,
                bowlingTeamName = bowlingTeamName,
                totalOvers = totalOvers,
                scoreState = scoringEngine.state,
                striker = striker,
                nonStriker = nonStriker,
                bowler = bowler,
                battingSquad = battingSquad,
                bowlingSquad = bowlingSquad,
                battingBench = bench,
                lastActionMessage = "New match started: ${striker.fullName} on strike"
            )
        }
    }

    fun recordDelivery(runsOffBat: Int) {
        val eng = engine ?: return
        val priorBalls = eng.state.totalLegalDeliveries
        val priorBowlerId = eng.state.currentBowlerId

        val state = eng.recordDelivery(runsOffBat = runsOffBat)
        val lastRecord = eng.history.lastOrNull()
        val currentMatchId = _uiState.value.matchId
        val currentInningsId = _uiState.value.inningsId

        if (lastRecord != null && currentInningsId.isNotEmpty()) {
            viewModelScope.launch {
                val entity = DeliveryEntity(
                    deliveryId = UUID.randomUUID().toString(),
                    inningsId = currentInningsId,
                    deliveryIndex = lastRecord.deliveryIndex,
                    overNumber = lastRecord.overNumber,
                    ballInOver = lastRecord.ballInOver,
                    strikerId = lastRecord.strikerId,
                    nonStrikerId = lastRecord.nonStrikerId,
                    bowlerId = lastRecord.bowlerId,
                    runsOffBat = runsOffBat,
                    extraType = ExtraType.NONE,
                    extraRuns = 0,
                    isLegalDelivery = true,
                    timestamp = lastRecord.timestamp
                )
                repository.recordDelivery(
                    delivery = entity,
                    newTotalRuns = state.totalRuns,
                    newTotalWickets = state.totalWickets,
                    newTotalBalls = state.totalLegalDeliveries
                )
            }
        }

        // Check if over was just completed (6th legal ball bowled)
        val overJustCompleted = (state.totalLegalDeliveries > priorBalls && state.totalLegalDeliveries % 6 == 0)

        updateUiState(
            message = "Recorded $runsOffBat run${if (runsOffBat == 1) "" else "s"}",
            showBowlerDialog = overJustCompleted,
            lastCompletedBowlerId = if (overJustCompleted) priorBowlerId else _uiState.value.lastCompletedBowlerId
        )
    }

    fun recordExtra(extraType: ExtraType) {
        val eng = engine ?: return
        val currentInningsId = _uiState.value.inningsId
        val extraRuns = if (extraType == ExtraType.WIDE || extraType == ExtraType.NO_BALL) 1 else 1

        val state = eng.recordDelivery(extraType = extraType, extraRuns = extraRuns)
        val lastRecord = eng.history.lastOrNull()

        if (lastRecord != null && currentInningsId.isNotEmpty()) {
            viewModelScope.launch {
                val entity = DeliveryEntity(
                    deliveryId = UUID.randomUUID().toString(),
                    inningsId = currentInningsId,
                    deliveryIndex = lastRecord.deliveryIndex,
                    overNumber = lastRecord.overNumber,
                    ballInOver = lastRecord.ballInOver,
                    strikerId = lastRecord.strikerId,
                    nonStrikerId = lastRecord.nonStrikerId,
                    bowlerId = lastRecord.bowlerId,
                    runsOffBat = 0,
                    extraType = extraType,
                    extraRuns = extraRuns,
                    isLegalDelivery = lastRecord.isLegalDelivery,
                    timestamp = lastRecord.timestamp
                )
                repository.recordDelivery(
                    delivery = entity,
                    newTotalRuns = state.totalRuns,
                    newTotalWickets = state.totalWickets,
                    newTotalBalls = state.totalLegalDeliveries
                )
            }
        }
        updateUiState("Recorded ${extraType.name}")
    }

    fun openDismissalDialog() {
        _uiState.value = _uiState.value.copy(showDismissalDialog = true)
    }

    fun closeDismissalDialog() {
        _uiState.value = _uiState.value.copy(showDismissalDialog = false)
    }

    fun confirmDismissal(dismissal: DismissalDetails, incomingBatsmanId: String) {
        val eng = engine ?: return
        val currentInningsId = _uiState.value.inningsId

        val state = eng.recordDelivery(
            dismissal = dismissal,
            incomingBatsmanId = incomingBatsmanId,
            nextStrikerIdAfterDismissal = incomingBatsmanId
        )
        val lastRecord = eng.history.lastOrNull()

        if (lastRecord != null && currentInningsId.isNotEmpty()) {
            viewModelScope.launch {
                val entity = DeliveryEntity(
                    deliveryId = UUID.randomUUID().toString(),
                    inningsId = currentInningsId,
                    deliveryIndex = lastRecord.deliveryIndex,
                    overNumber = lastRecord.overNumber,
                    ballInOver = lastRecord.ballInOver,
                    strikerId = lastRecord.strikerId,
                    nonStrikerId = lastRecord.nonStrikerId,
                    bowlerId = lastRecord.bowlerId,
                    runsOffBat = 0,
                    isWicket = true,
                    dismissalType = dismissal.dismissalType,
                    dismissedPlayerId = dismissal.dismissedPlayerId,
                    primaryFielderId = dismissal.primaryFielderId,
                    runsCompletedBeforeDismissal = dismissal.runsCompletedBeforeDismissal,
                    nextStrikerIdAfterDismissal = incomingBatsmanId,
                    timestamp = lastRecord.timestamp
                )
                repository.recordDelivery(
                    delivery = entity,
                    newTotalRuns = state.totalRuns,
                    newTotalWickets = state.totalWickets,
                    newTotalBalls = state.totalLegalDeliveries
                )
            }
        }

        // Update bench
        val updatedBench = _uiState.value.battingBench.filter { it.userId != incomingBatsmanId }

        _uiState.value = _uiState.value.copy(
            showDismissalDialog = false,
            battingBench = updatedBench
        )
        updateUiState("OUT! ${dismissal.dismissalType.name}. Next batsman in.")
    }

    fun onNextBowlerSelected(newBowler: UserEntity) {
        val eng = engine ?: return
        eng.changeBowler(newBowler.userId)
        _uiState.value = _uiState.value.copy(
            showBowlerDialog = false,
            bowler = newBowler,
            scoreState = eng.state,
            lastActionMessage = "Over completed. ${newBowler.fullName} bowling."
        )
    }

    fun undo() {
        val eng = engine ?: return
        val currentInningsId = _uiState.value.inningsId
        val state = eng.undo() ?: return

        viewModelScope.launch {
            repository.undoLastDelivery(
                inningsId = currentInningsId,
                revertedRuns = state.totalRuns,
                revertedWickets = state.totalWickets,
                revertedBalls = state.totalLegalDeliveries
            )
        }
        updateUiState("Delivery undone from memory & SQLite")
    }

    fun rotateStrike() {
        val eng = engine ?: return
        eng.rotateStrike()
        updateUiState("Strike rotated manually")
    }

    fun onStartSecondInningsClicked() {
        _uiState.value = _uiState.value.copy(
            showInningsBreakDialog = false,
            showSecondInningsOpenersDialog = true
        )
    }

    fun confirmSecondInningsStart(
        striker: UserEntity,
        nonStriker: UserEntity,
        openingBowler: UserEntity
    ) {
        val current = _uiState.value
        val innings1Runs = current.innings1Score?.totalRuns ?: current.scoreState.totalRuns
        val targetRuns = innings1Runs + 1

        val newBattingTeamId = current.bowlingTeamId
        val newBowlingTeamId = current.battingTeamId
        val newBattingTeamName = current.bowlingTeamName
        val newBowlingTeamName = current.battingTeamName
        val newBattingSquad = current.bowlingSquad
        val newBowlingSquad = current.battingSquad

        viewModelScope.launch {
            val newInningsId = repository.startSecondInnings(
                matchId = current.matchId,
                battingTeamId = newBattingTeamId,
                bowlingTeamId = newBowlingTeamId,
                targetRuns = targetRuns
            )

            val playerNames = (newBattingSquad + newBowlingSquad).associate { it.userId to it.fullName }
            val newEngine = ScoringEngine(
                matchId = current.matchId,
                inningsNumber = 2,
                totalOversAllotted = current.totalOvers,
                targetRuns = targetRuns,
                maxWickets = (newBattingSquad.size - 1).coerceAtLeast(1),
                playerNames = playerNames
            )
            newEngine.startInnings(
                strikerId = striker.userId,
                nonStrikerId = nonStriker.userId,
                openingBowlerId = openingBowler.userId
            )
            engine = newEngine

            val bench = newBattingSquad.filter { it.userId != striker.userId && it.userId != nonStriker.userId }

            _uiState.value = current.copy(
                inningsId = newInningsId,
                inningsNumber = 2,
                battingTeamId = newBattingTeamId,
                bowlingTeamId = newBowlingTeamId,
                battingTeamName = newBattingTeamName,
                bowlingTeamName = newBowlingTeamName,
                battingSquad = newBattingSquad,
                bowlingSquad = newBowlingSquad,
                battingBench = bench,
                striker = striker,
                nonStriker = nonStriker,
                bowler = openingBowler,
                scoreState = newEngine.state,
                showSecondInningsOpenersDialog = false,
                lastCompletedBowlerId = "",
                lastActionMessage = "2nd Innings started! Target: $targetRuns runs from ${current.totalOvers} overs"
            )
        }
    }

    fun setPlayerOfTheMatch(player: UserEntity) {
        _uiState.value = _uiState.value.copy(
            selectedPlayerOfTheMatch = player,
            matchResult = _uiState.value.matchResult?.copy(
                playerOfTheMatchId = player.userId,
                playerOfTheMatchName = player.fullName
            )
        )
        viewModelScope.launch {
            repository.completeMatch(
                matchId = _uiState.value.matchId,
                winnerTeamId = _uiState.value.matchResult?.winnerTeamId,
                resultSummary = _uiState.value.matchResult?.resultSummary,
                playerOfTheMatchId = player.userId
            )
        }
    }

    fun dismissMatchResultDialog() {
        _uiState.value = _uiState.value.copy(showMatchResultDialog = false)
    }

    private fun determinePlayerOfTheMatch(
        allPlayers: List<UserEntity>,
        innings1: InningsScoreState?,
        innings2: InningsScoreState
    ): UserEntity? {
        val scores = mutableMapOf<String, Int>()

        listOfNotNull(innings1, innings2).forEach { inn ->
            inn.batsmen.forEach { (id, bat) ->
                val current = scores.getOrDefault(id, 0)
                scores[id] = current + bat.runs + (bat.fours * 1) + (bat.sixes * 2)
            }
            inn.bowlers.forEach { (id, bowl) ->
                val current = scores.getOrDefault(id, 0)
                scores[id] = current + (bowl.wickets * 25) + (bowl.maidens * 10) - (bowl.runsConceded / 2)
            }
        }

        val bestPlayerId = scores.maxByOrNull { it.value }?.key
        return allPlayers.find { it.userId == bestPlayerId } ?: allPlayers.firstOrNull()
    }

    private fun updateUiState(
        message: String,
        showBowlerDialog: Boolean = false,
        lastCompletedBowlerId: String = _uiState.value.lastCompletedBowlerId
    ) {
        val eng = engine ?: return
        val state = eng.state

        val striker = _uiState.value.battingSquad.find { it.userId == state.strikerId }
        val nonStriker = _uiState.value.battingSquad.find { it.userId == state.nonStrikerId }
        val bowler = _uiState.value.bowlingSquad.find { it.userId == state.currentBowlerId }

        // 1. Check if Innings 1 just completed
        if (_uiState.value.inningsNumber == 1 && state.isCompleted) {
            viewModelScope.launch {
                repository.completeInnings(_uiState.value.inningsId)
            }
            _uiState.value = _uiState.value.copy(
                scoreState = state,
                innings1Score = state,
                innings1BattingTeamName = _uiState.value.battingTeamName,
                showInningsBreakDialog = true,
                showBowlerDialog = false,
                lastActionMessage = "1st Innings completed: ${state.totalRuns}/${state.totalWickets} (${state.oversFormatted} ov)"
            )
            return
        }

        // 2. Check if Innings 2 is completed or in progress
        if (_uiState.value.inningsNumber == 2) {
            val result = MatchResultCalculator.calculateResult(
                teamAId = _uiState.value.teamAId,
                teamAName = if (_uiState.value.battingTeamId == _uiState.value.teamAId) _uiState.value.battingTeamName else _uiState.value.bowlingTeamName,
                teamBId = _uiState.value.teamBId,
                teamBName = if (_uiState.value.battingTeamId == _uiState.value.teamBId) _uiState.value.battingTeamName else _uiState.value.bowlingTeamName,
                innings1BattingTeamId = _uiState.value.bowlingTeamId,
                innings1State = _uiState.value.innings1Score ?: InningsScoreState(totalRuns = 0),
                innings2State = state,
                maxWickets = (_uiState.value.battingSquad.size - 1).coerceAtLeast(1)
            )

            if (result.isCompleted) {
                val allPlayers = _uiState.value.battingSquad + _uiState.value.bowlingSquad
                val candidatePOTM = determinePlayerOfTheMatch(
                    allPlayers = allPlayers,
                    innings1 = _uiState.value.innings1Score,
                    innings2 = state
                )

                viewModelScope.launch {
                    repository.completeInnings(_uiState.value.inningsId)
                    repository.completeMatch(
                        matchId = _uiState.value.matchId,
                        winnerTeamId = result.winnerTeamId,
                        resultSummary = result.resultSummary,
                        playerOfTheMatchId = candidatePOTM?.userId
                    )
                    repository.recordMatchPlayerStats(
                        innings1 = _uiState.value.innings1Score,
                        innings2 = state
                    )
                }

                _uiState.value = _uiState.value.copy(
                    scoreState = state,
                    showMatchResultDialog = true,
                    showBowlerDialog = false,
                    matchResult = result.copy(
                        playerOfTheMatchId = candidatePOTM?.userId,
                        playerOfTheMatchName = candidatePOTM?.fullName
                    ),
                    playerOfTheMatchCandidates = allPlayers,
                    selectedPlayerOfTheMatch = candidatePOTM,
                    lastActionMessage = result.resultSummary
                )
                return
            }
        }

        _uiState.value = _uiState.value.copy(
            scoreState = state,
            striker = striker ?: _uiState.value.striker,
            nonStriker = nonStriker ?: _uiState.value.nonStriker,
            bowler = bowler ?: _uiState.value.bowler,
            showBowlerDialog = showBowlerDialog,
            lastCompletedBowlerId = lastCompletedBowlerId,
            lastActionMessage = message
        )
    }
}
