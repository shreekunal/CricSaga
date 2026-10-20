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
import com.cricsaga.app.domain.engine.ScoringEngine
import com.cricsaga.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class ScoringUiState(
    val isLoading: Boolean = true,
    val matchId: String = "",
    val inningsId: String = "",
    val matchTitle: String = "Loading...",
    val battingTeamName: String = "",
    val bowlingTeamName: String = "",
    val scoreState: InningsScoreState = InningsScoreState(),
    val striker: UserEntity? = null,
    val nonStriker: UserEntity? = null,
    val bowler: UserEntity? = null,
    val battingSquad: List<UserEntity> = emptyList(),
    val bowlingSquad: List<UserEntity> = emptyList(),
    val battingBench: List<UserEntity> = emptyList(),
    val showDismissalDialog: Boolean = false,
    val showBowlerDialog: Boolean = false,
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
                matchTitle = "Warriors vs Tigers",
                battingTeamName = data.teamAName,
                bowlingTeamName = data.teamBName,
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
                matchTitle = title,
                battingTeamName = battingTeamName,
                bowlingTeamName = bowlingTeamName,
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
