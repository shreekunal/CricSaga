package com.cricsaga.app.ui.scoring

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cricsaga.app.data.local.CricSagaDatabase
import com.cricsaga.app.data.local.entity.DeliveryEntity
import com.cricsaga.app.data.repository.MatchRepository
import com.cricsaga.app.data.repository.SeededMatchData
import com.cricsaga.app.domain.engine.ScoringEngine
import com.cricsaga.app.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ScoringUiState(
    val isLoading: Boolean = true,
    val matchTitle: String = "Loading...",
    val battingTeamName: String = "",
    val bowlingTeamName: String = "",
    val scoreState: InningsScoreState = InningsScoreState(),
    val strikerName: String = "",
    val nonStrikerName: String = "",
    val bowlerName: String = "",
    val lastActionMessage: String = "Ready to score"
)

class ScoringViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MatchRepository
    private var engine: ScoringEngine? = null
    private var seededData: SeededMatchData? = null

    private val _uiState = MutableStateFlow(ScoringUiState())
    val uiState: StateFlow<ScoringUiState> = _uiState.asStateFlow()

    init {
        val database = CricSagaDatabase.getDatabase(application)
        repository = MatchRepository(database)
        initializeMatch()
    }

    private fun initializeMatch() {
        viewModelScope.launch {
            val data = repository.seedInitialDataIfEmpty()
            seededData = data

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

            updateUiState("Match initialized with Room DB: ${data.striker.fullName} on strike")
        }
    }

    fun recordDelivery(runsOffBat: Int) {
        val eng = engine ?: return
        val currentStriker = eng.state.strikerId
        val currentNonStriker = eng.state.nonStrikerId
        val currentBowler = eng.state.currentBowlerId
        val matchData = seededData ?: return

        val state = eng.recordDelivery(runsOffBat = runsOffBat)
        val lastRecord = eng.history.lastOrNull()

        if (lastRecord != null) {
            viewModelScope.launch {
                val entity = DeliveryEntity(
                    deliveryId = UUID.randomUUID().toString(),
                    inningsId = matchData.inningsId,
                    deliveryIndex = lastRecord.deliveryIndex,
                    overNumber = lastRecord.overNumber,
                    ballInOver = lastRecord.ballInOver,
                    strikerId = currentStriker,
                    nonStrikerId = currentNonStriker,
                    bowlerId = currentBowler,
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
        updateUiState("Recorded $runsOffBat runs (Persisted to Room SQLite)")
    }

    fun recordExtra(extraType: ExtraType) {
        val eng = engine ?: return
        val currentStriker = eng.state.strikerId
        val currentNonStriker = eng.state.nonStrikerId
        val currentBowler = eng.state.currentBowlerId
        val matchData = seededData ?: return

        val extraRuns = if (extraType == ExtraType.WIDE || extraType == ExtraType.NO_BALL) 1 else 1
        val state = eng.recordDelivery(extraType = extraType, extraRuns = extraRuns)
        val lastRecord = eng.history.lastOrNull()

        if (lastRecord != null) {
            viewModelScope.launch {
                val entity = DeliveryEntity(
                    deliveryId = UUID.randomUUID().toString(),
                    inningsId = matchData.inningsId,
                    deliveryIndex = lastRecord.deliveryIndex,
                    overNumber = lastRecord.overNumber,
                    ballInOver = lastRecord.ballInOver,
                    strikerId = currentStriker,
                    nonStrikerId = currentNonStriker,
                    bowlerId = currentBowler,
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
        updateUiState("Recorded ${extraType.name} (Persisted to Room SQLite)")
    }

    fun recordWicket() {
        val eng = engine ?: return
        val currentStriker = eng.state.strikerId
        val currentNonStriker = eng.state.nonStrikerId
        val currentBowler = eng.state.currentBowlerId
        val matchData = seededData ?: return
        val incomingId = matchData.incomingBatsman.userId

        val dismissal = DismissalDetails(
            dismissalType = DismissalType.CAUGHT,
            dismissedPlayerId = currentStriker,
            primaryFielderId = "user_surya"
        )

        val state = eng.recordDelivery(
            dismissal = dismissal,
            incomingBatsmanId = incomingId,
            nextStrikerIdAfterDismissal = incomingId
        )
        val lastRecord = eng.history.lastOrNull()

        if (lastRecord != null) {
            viewModelScope.launch {
                val entity = DeliveryEntity(
                    deliveryId = UUID.randomUUID().toString(),
                    inningsId = matchData.inningsId,
                    deliveryIndex = lastRecord.deliveryIndex,
                    overNumber = lastRecord.overNumber,
                    ballInOver = lastRecord.ballInOver,
                    strikerId = currentStriker,
                    nonStrikerId = currentNonStriker,
                    bowlerId = currentBowler,
                    runsOffBat = 0,
                    isWicket = true,
                    dismissalType = DismissalType.CAUGHT,
                    dismissedPlayerId = currentStriker,
                    primaryFielderId = "user_surya",
                    nextStrikerIdAfterDismissal = incomingId,
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
        updateUiState("WICKET! Caught by Surya. New batsman on strike.")
    }

    fun undo() {
        val eng = engine ?: return
        val matchData = seededData ?: return
        val state = eng.undo() ?: return

        viewModelScope.launch {
            repository.undoLastDelivery(
                inningsId = matchData.inningsId,
                revertedRuns = state.totalRuns,
                revertedWickets = state.totalWickets,
                revertedBalls = state.totalLegalDeliveries
            )
        }
        updateUiState("Delivery undone from memory and Room SQLite")
    }

    fun rotateStrike() {
        val eng = engine ?: return
        eng.rotateStrike()
        updateUiState("Strike rotated manually")
    }

    private fun updateUiState(message: String) {
        val eng = engine ?: return
        val data = seededData ?: return
        val state = eng.state

        val strikerName = state.batsmen[state.strikerId]?.playerName ?: "Striker"
        val nonStrikerName = state.batsmen[state.nonStrikerId]?.playerName ?: "Non-Striker"
        val bowlerName = state.bowlers[state.currentBowlerId]?.bowlerName ?: "Bowler"

        _uiState.value = ScoringUiState(
            isLoading = false,
            matchTitle = "Warriors vs Tigers",
            battingTeamName = data.teamAName,
            bowlingTeamName = data.teamBName,
            scoreState = state,
            strikerName = strikerName,
            nonStrikerName = nonStrikerName,
            bowlerName = bowlerName,
            lastActionMessage = message
        )
    }
}
