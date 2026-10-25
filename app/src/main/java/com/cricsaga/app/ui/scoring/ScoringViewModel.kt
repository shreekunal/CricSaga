package com.cricsaga.app.ui.scoring

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cricsaga.app.data.local.CricSagaDatabase
import com.cricsaga.app.data.local.entity.CommentEntity
import com.cricsaga.app.data.local.entity.DeliveryEntity
import com.cricsaga.app.data.local.entity.LookingForEntity
import com.cricsaga.app.data.local.entity.MatchEntity
import com.cricsaga.app.data.local.entity.PostEntity
import com.cricsaga.app.data.local.entity.TeamEntity
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.data.repository.MatchRepository
import com.cricsaga.app.domain.engine.MatchResultCalculator
import com.cricsaga.app.domain.engine.ScoringEngine
import com.cricsaga.app.domain.model.*
import com.cricsaga.app.ui.home.GrassrootsHeroItem
import com.cricsaga.app.ui.utils.BroadcastNotifier
import com.cricsaga.app.ui.utils.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    val maxOversPerBowler: Int = 4,
    val lastActionMessage: String = "Ready to score",
    val currentInningsDeliveries: List<DeliveryRecord> = emptyList(),
    val innings1Deliveries: List<DeliveryRecord> = emptyList()
)

class ScoringViewModel(application: Application) : AndroidViewModel(application) {

    val repository: MatchRepository
    private var engine: ScoringEngine? = null
    private val scoringMutex = Mutex()

    val allMatches: StateFlow<List<MatchEntity>>

    fun getMatchesForUser(userId: String): Flow<List<MatchEntity>> = repository.getMatchesForUser(userId)
    val allTeams: StateFlow<List<TeamEntity>>
    val topPerformers: StateFlow<List<GrassrootsHeroItem>>
    val lookingForPosts: StateFlow<List<LookingForEntity>>
    val allPosts: StateFlow<List<PostEntity>>
    val selectedRankingCity = MutableStateFlow("All Regions")
    val topBatters: StateFlow<List<PlayerRankingItem>>
    val topBowlers: StateFlow<List<PlayerRankingItem>>
    val topAllRounders: StateFlow<List<PlayerRankingItem>>
    val teamRankings: StateFlow<List<TeamRankingItem>>
    val availableCities: StateFlow<List<String>>

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

        topPerformers = repository.getAllPlayerStats()
            .combine(repository.getAllUsers()) { statsList, users ->
                val userMap = users.associateBy { it.userId }
                statsList.filter { it.runsScored > 0 || it.wicketsTaken > 0 }
                    .sortedByDescending { it.runsScored + (it.wicketsTaken * 25) }
                    .take(3)
                    .map { stat ->
                        val user = userMap[stat.userId]
                        val name = user?.fullName ?: "Player"
                        val initials = name.split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .take(2)
                            .joinToString("")
                            .uppercase()
                            .ifEmpty { "CR" }
                        val performance = if (stat.runsScored >= stat.wicketsTaken * 20) {
                            "${stat.runsScored} runs • HS ${stat.highestScore} (${stat.fifties}x50, ${stat.hundreds}x100)"
                        } else {
                            "${stat.wicketsTaken} wkts • ${stat.maidens} maidens"
                        }
                        val role = user?.playingRole?.name?.replace('_', ' ')?.lowercase()
                            ?.replaceFirstChar { it.uppercase() } ?: "All-Rounder"
                        GrassrootsHeroItem(
                            playerName = name,
                            initials = initials,
                            teamName = user?.city?.ifEmpty { "Grassroots XI" } ?: "Grassroots XI",
                            performance = performance,
                            context = "$role • ${stat.matches} matches"
                        )
                    }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

        lookingForPosts = repository.getAllLookingForPosts().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allPosts = repository.getAllPosts().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        @OptIn(ExperimentalCoroutinesApi::class)
        topBatters = selectedRankingCity.flatMapLatest { city ->
            repository.getTopBatters(city)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        @OptIn(ExperimentalCoroutinesApi::class)
        topBowlers = selectedRankingCity.flatMapLatest { city ->
            repository.getTopBowlers(city)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        @OptIn(ExperimentalCoroutinesApi::class)
        topAllRounders = selectedRankingCity.flatMapLatest { city ->
            repository.getTopAllRounders(city)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        @OptIn(ExperimentalCoroutinesApi::class)
        teamRankings = selectedRankingCity.flatMapLatest { city ->
            repository.getTeamRankings(city)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        availableCities = repository.getAvailableCities().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            listOf("All Regions")
        )
    }

    fun setSelectedRankingCity(city: String) {
        selectedRankingCity.value = city
    }

    fun createPost(
        authorId: String,
        authorName: String,
        authorRole: String,
        authorCity: String,
        authorTeamName: String?,
        authorAvatarUrl: String?,
        content: String,
        imageUri: String? = null,
        linkedMatchId: String? = null,
        performanceTag: String? = null
    ) {
        viewModelScope.launch {
            repository.createPost(
                authorId = authorId,
                authorName = authorName,
                authorRole = authorRole,
                authorCity = authorCity,
                authorTeamName = authorTeamName,
                authorAvatarUrl = authorAvatarUrl,
                content = content,
                imageUri = imageUri,
                linkedMatchId = linkedMatchId,
                performanceTag = performanceTag
            )
            NotificationHelper.sendCommunityNotification(
                context = getApplication(),
                title = "Post Shared",
                message = "Your update was shared with the grassroots community."
            )
        }
    }

    fun toggleLikePost(postId: String) {
        viewModelScope.launch {
            repository.toggleLikePost(postId)
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
        }
    }

    fun incrementRepost(postId: String) {
        viewModelScope.launch {
            repository.incrementRepost(postId)
        }
    }

    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>> =
        repository.getCommentsFlow(postId)

    fun addComment(
        postId: String,
        authorId: String,
        authorName: String,
        text: String,
        authorAvatarUrl: String? = null
    ) {
        viewModelScope.launch {
            repository.addComment(postId, authorId, authorName, text, authorAvatarUrl)
        }
    }

    fun followPlayer(followerId: String, followingId: String) {
        viewModelScope.launch {
            repository.followUser(followerId, followingId)
        }
    }

    fun unfollowPlayer(followerId: String, followingId: String) {
        viewModelScope.launch {
            repository.unfollowUser(followerId, followingId)
        }
    }

    fun sendChatMessage(
        senderId: String,
        receiverId: String,
        text: String,
        isChallenge: Boolean = false,
        challengeDetails: String? = null
    ) {
        viewModelScope.launch {
            repository.sendChatMessage(senderId, receiverId, text, isChallenge, challengeDetails)
        }
    }

    fun createLookingForPost(
        type: String,
        title: String,
        teamOrOrganizer: String,
        timing: String,
        location: String,
        note: String,
        contactPhone: String
    ) {
        viewModelScope.launch {
            repository.createLookingForPost(type, title, teamOrOrganizer, timing, location, note, contactPhone)
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
        bowlingSquad: List<UserEntity>,
        maxOversPerBowler: Int = 4
    ) {
        viewModelScope.launch {
            val currentUserId = (com.cricsaga.app.data.local.AuthManager(getApplication()).authState.value as? com.cricsaga.app.data.local.AuthState.Authenticated)?.userId
            val result = repository.createMatch(
                title = title,
                teamAId = teamAId,
                teamBId = teamBId,
                totalOvers = totalOvers,
                ballType = ballType,
                tossWinnerTeamId = tossWinnerId,
                tossDecision = tossDecision,
                createdByUserId = currentUserId
            )

            val playerNames = (battingSquad + bowlingSquad).associate { it.userId to it.fullName }
            val calculatedMaxWickets = (battingSquad.size - 1).coerceAtLeast(1)

            val scoringEngine = ScoringEngine(
                matchId = result.matchId,
                inningsNumber = 1,
                totalOversAllotted = totalOvers,
                maxWickets = calculatedMaxWickets,
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
                maxOversPerBowler = maxOversPerBowler,
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
        val currentInningsId = _uiState.value.inningsId
        viewModelScope.launch {
            scoringMutex.withLock {
                val priorBalls = eng.state.totalLegalDeliveries
                val priorBowlerId = eng.state.currentBowlerId
                val strikerId = eng.state.strikerId
                val prevRuns = eng.state.batsmen[strikerId]?.runs ?: 0

                val state = eng.recordDelivery(runsOffBat = runsOffBat)
                val lastRecord = eng.history.lastOrNull()

                if (lastRecord != null && currentInningsId.isNotEmpty()) {
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

                // Milestone notification & banner check
                val newScore = state.batsmen[strikerId]
                val newRuns = newScore?.runs ?: 0
                val newBalls = newScore?.ballsFaced ?: 0
                val strikerName = _uiState.value.battingSquad.find { it.userId == strikerId }?.fullName ?: "Batter"

                if (prevRuns < 50 && newRuns >= 50) {
                    BroadcastNotifier.postMilestone(strikerName, "Fifty (50*)", "$newRuns runs off $newBalls balls")
                    NotificationHelper.sendMatchEventNotification(
                        getApplication(),
                        "Milestone: Fifty!",
                        "$strikerName reached 50 ($newRuns off $newBalls balls)"
                    )
                } else if (prevRuns < 100 && newRuns >= 100) {
                    BroadcastNotifier.postMilestone(strikerName, "Century (100*)", "$newRuns runs off $newBalls balls")
                    NotificationHelper.sendMatchEventNotification(
                        getApplication(),
                        "Milestone: Century!",
                        "$strikerName reached 100 ($newRuns off $newBalls balls)"
                    )
                }

                // Check if over was just completed (6th legal ball bowled)
                val overJustCompleted = (state.totalLegalDeliveries > priorBalls && state.totalLegalDeliveries % 6 == 0)

                updateUiState(
                    message = "Recorded $runsOffBat run${if (runsOffBat == 1) "" else "s"}",
                    showBowlerDialog = overJustCompleted,
                    lastCompletedBowlerId = if (overJustCompleted) priorBowlerId else _uiState.value.lastCompletedBowlerId
                )
            }
        }
    }

    fun recordExtra(extraType: ExtraType, runs: Int = 1) {
        val eng = engine ?: return
        val currentInningsId = _uiState.value.inningsId
        viewModelScope.launch {
            scoringMutex.withLock {
                val priorBalls = eng.state.totalLegalDeliveries
                val priorBowlerId = eng.state.currentBowlerId
                val extraRuns = if (runs > 0) runs else 1

                val state = eng.recordDelivery(extraType = extraType, extraRuns = extraRuns)
                val lastRecord = eng.history.lastOrNull()

                if (lastRecord != null && currentInningsId.isNotEmpty()) {
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
                val overJustCompleted = (state.totalLegalDeliveries > priorBalls && state.totalLegalDeliveries % 6 == 0)
                updateUiState(
                    message = "Recorded ${extraType.name}",
                    showBowlerDialog = overJustCompleted,
                    lastCompletedBowlerId = if (overJustCompleted) priorBowlerId else _uiState.value.lastCompletedBowlerId
                )
            }
        }
    }

    fun openDismissalDialog() {
        _uiState.value = _uiState.value.copy(showDismissalDialog = true)
    }

    fun closeDismissalDialog() {
        _uiState.value = _uiState.value.copy(showDismissalDialog = false)
    }

    fun confirmDismissal(
        dismissal: DismissalDetails,
        incomingBatsmanId: String,
        extraType: ExtraType = ExtraType.NONE
    ) {
        val eng = engine ?: return
        val currentInningsId = _uiState.value.inningsId
        viewModelScope.launch {
            scoringMutex.withLock {
                val priorBalls = eng.state.totalLegalDeliveries
                val priorBowlerId = eng.state.currentBowlerId
                val effectiveFielderId = if (dismissal.dismissalType == DismissalType.CAUGHT_AND_BOWLED && dismissal.primaryFielderId.isNullOrBlank()) {
                    priorBowlerId
                } else {
                    dismissal.primaryFielderId
                }
                val effectiveDismissal = dismissal.copy(primaryFielderId = effectiveFielderId)
                val completedRuns = effectiveDismissal.runsCompletedBeforeDismissal
                val (runsOffBatValue, calculatedExtraRuns) = when (extraType) {
                    ExtraType.WIDE -> 0 to (1 + completedRuns)
                    ExtraType.BYE, ExtraType.LEG_BYE -> 0 to completedRuns
                    ExtraType.NO_BALL -> completedRuns to 1
                    ExtraType.PENALTY -> 0 to completedRuns
                    ExtraType.NONE -> completedRuns to 0
                }
                val state = eng.recordDelivery(
                    runsOffBat = runsOffBatValue,
                    extraType = extraType,
                    extraRuns = calculatedExtraRuns,
                    dismissal = effectiveDismissal,
                    incomingBatsmanId = incomingBatsmanId,
                    nextStrikerIdAfterDismissal = incomingBatsmanId
                )
                val lastRecord = eng.history.lastOrNull()

                if (lastRecord != null && currentInningsId.isNotEmpty()) {
                    val entity = DeliveryEntity(
                        deliveryId = UUID.randomUUID().toString(),
                        inningsId = currentInningsId,
                        deliveryIndex = lastRecord.deliveryIndex,
                        overNumber = lastRecord.overNumber,
                        ballInOver = lastRecord.ballInOver,
                        strikerId = lastRecord.strikerId,
                        nonStrikerId = lastRecord.nonStrikerId,
                        bowlerId = lastRecord.bowlerId,
                        runsOffBat = runsOffBatValue,
                        extraType = extraType,
                        extraRuns = calculatedExtraRuns,
                        isLegalDelivery = lastRecord.isLegalDelivery,
                        isWicket = true,
                        dismissalType = effectiveDismissal.dismissalType,
                        dismissedPlayerId = effectiveDismissal.dismissedPlayerId,
                        primaryFielderId = effectiveDismissal.primaryFielderId,
                        runsCompletedBeforeDismissal = effectiveDismissal.runsCompletedBeforeDismissal,
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

                // Wicket Notification
                val dismissedName = (_uiState.value.battingSquad.find { it.userId == dismissal.dismissedPlayerId }?.fullName) ?: "Batter"
                val dismissedScore = state.batsmen[dismissal.dismissedPlayerId]
                val batRuns = dismissedScore?.runs ?: 0
                val batBalls = dismissedScore?.ballsFaced ?: 0
                NotificationHelper.sendMatchEventNotification(
                    getApplication(),
                    "WICKET! ${dismissal.dismissalType.name}",
                    "$dismissedName dismissed for $batRuns ($batBalls b)"
                )

                // Update bench
                val updatedBench = _uiState.value.battingBench.filter { it.userId != incomingBatsmanId }

                _uiState.value = _uiState.value.copy(
                    showDismissalDialog = false,
                    battingBench = updatedBench
                )
                val overJustCompleted = (state.totalLegalDeliveries > priorBalls && state.totalLegalDeliveries % 6 == 0)
                updateUiState(
                    message = "OUT! ${dismissal.dismissalType.name}. Next batsman in.",
                    showBowlerDialog = overJustCompleted,
                    lastCompletedBowlerId = if (overJustCompleted) priorBowlerId else _uiState.value.lastCompletedBowlerId
                )
            }
        }
    }

    fun onNextBowlerSelected(newBowler: UserEntity) {
        val eng = engine ?: return
        viewModelScope.launch {
            scoringMutex.withLock {
                eng.changeBowler(newBowler.userId)
                _uiState.value = _uiState.value.copy(
                    showBowlerDialog = false,
                    bowler = newBowler,
                    scoreState = eng.state,
                    lastActionMessage = "Over completed. ${newBowler.fullName} bowling."
                )
            }
        }
    }

    fun undo() {
        val eng = engine ?: return
        val currentInningsId = _uiState.value.inningsId
        viewModelScope.launch {
            scoringMutex.withLock {
                val state = eng.undo() ?: return@withLock
                repository.undoLastDelivery(
                    inningsId = currentInningsId,
                    revertedRuns = state.totalRuns,
                    revertedWickets = state.totalWickets,
                    revertedBalls = state.totalLegalDeliveries
                )
                updateUiState("Delivery undone from memory & SQLite")
            }
        }
    }

    fun rotateStrike() {
        val eng = engine ?: return
        viewModelScope.launch {
            scoringMutex.withLock {
                eng.rotateStrike()
                updateUiState("Strike rotated manually")
            }
        }
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
                lastActionMessage = "2nd Innings started! Target: $targetRuns runs from ${current.totalOvers} overs",
                innings1Deliveries = current.currentInningsDeliveries,
                currentInningsDeliveries = newEngine.history
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
            val target = state.totalRuns + 1
            val rrr = target.toDouble() / _uiState.value.totalOvers.coerceAtLeast(1)
            BroadcastNotifier.postInningsBreak(target, rrr)
            NotificationHelper.sendMatchEventNotification(
                getApplication(),
                "Innings Break: Target $target",
                "${_uiState.value.battingTeamName} finished on ${state.totalRuns}/${state.totalWickets} (${state.oversFormatted} ov). Target is $target."
            )
            _uiState.value = _uiState.value.copy(
                scoreState = state,
                innings1Score = state,
                innings1BattingTeamName = _uiState.value.battingTeamName,
                innings1Deliveries = eng.history,
                currentInningsDeliveries = eng.history,
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

                BroadcastNotifier.postResult(result.resultSummary, candidatePOTM?.fullName)
                NotificationHelper.sendMatchEventNotification(
                    getApplication(),
                    "Match Result: ${_uiState.value.matchTitle.ifBlank { "Match Completed" }}",
                    result.resultSummary
                )

                val allSquadIds = allPlayers.map { it.userId }.toSet()
                viewModelScope.launch {
                    repository.completeInnings(_uiState.value.inningsId)
                    repository.completeMatch(
                        matchId = _uiState.value.matchId,
                        winnerTeamId = result.winnerTeamId,
                        resultSummary = result.resultSummary,
                        playerOfTheMatchId = candidatePOTM?.userId
                    )
                    repository.recordMatchPlayerStats(
                        matchId = _uiState.value.matchId,
                        innings1 = _uiState.value.innings1Score,
                        innings2 = state,
                        squadPlayerIds = allSquadIds
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
            lastActionMessage = message,
            currentInningsDeliveries = eng.history
        )
    }

    fun loadMatchById(matchId: String, onLoaded: (isLive: Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.syncScorecardForMatch(matchId)
            } catch (_: Exception) {}

            val match = repository.getMatch(matchId) ?: return@launch
            val inningsList = repository.getInningsListForMatch(matchId)
            val teamA = repository.getTeam(match.teamAId)
            val teamB = repository.getTeam(match.teamBId)
            val teamAName = teamA?.teamName ?: "Team A"
            val teamBName = teamB?.teamName ?: "Team B"

            val teamASquad = repository.getTeamMembersList(match.teamAId)
            val teamBSquad = repository.getTeamMembersList(match.teamBId)
            val allPlayers = teamASquad + teamBSquad
            val playerNames = allPlayers.associate { it.userId to it.fullName }

            val inn1 = inningsList.getOrNull(0)
            val inn2 = inningsList.getOrNull(1)

            val inn1Entities = inn1?.let { repository.getDeliveriesList(it.inningsId) } ?: emptyList()
            val inn2Entities = inn2?.let { repository.getDeliveriesList(it.inningsId) } ?: emptyList()

            val inn1Records = inn1Entities.map { it.toRecord() }
            val inn2Records = inn2Entities.map { it.toRecord() }

            val inn1BattingName = if (inn1?.battingTeamId == match.teamAId) teamAName else teamBName
            val inn1BowlingName = if (inn1?.battingTeamId == match.teamAId) teamBName else teamAName
            val inn2BattingName = if (inn2?.battingTeamId == match.teamAId) teamAName else teamBName
            val inn2BowlingName = if (inn2?.battingTeamId == match.teamAId) teamBName else teamAName

            // Replay Innings 1
            val eng1 = inn1?.let {
                val inn1BattingSquad = if (inn1.battingTeamId == match.teamAId) teamASquad else teamBSquad
                val inn1BowlingSquad = if (inn1.battingTeamId == match.teamAId) teamBSquad else teamASquad
                val maxWkts = (inn1BattingSquad.size - 1).coerceAtLeast(1)
                val e = ScoringEngine(
                    matchId = matchId,
                    inningsNumber = 1,
                    totalOversAllotted = match.totalOvers,
                    maxWickets = maxWkts,
                    playerNames = playerNames
                )
                val firstDel = inn1Records.firstOrNull()
                val striker = firstDel?.strikerId ?: inn1BattingSquad.firstOrNull()?.userId ?: ""
                val nonStriker = firstDel?.nonStrikerId ?: inn1BattingSquad.getOrNull(1)?.userId ?: ""
                val bowler = firstDel?.bowlerId ?: inn1BowlingSquad.firstOrNull()?.userId ?: ""
                if (striker.isNotEmpty() && nonStriker.isNotEmpty() && bowler.isNotEmpty()) {
                    e.startInnings(striker, nonStriker, bowler)
                    inn1Records.forEach { r ->
                        if (r.bowlerId.isNotBlank() && r.bowlerId != e.state.currentBowlerId) {
                            e.changeBowler(r.bowlerId)
                        }
                        if (r.strikerId.isNotBlank() && r.strikerId == e.state.nonStrikerId) {
                            e.rotateStrike()
                        }
                        e.recordDelivery(
                            runsOffBat = r.runsOffBat,
                            extraType = r.extraType,
                            extraRuns = r.extraRuns,
                            dismissal = r.dismissal,
                            incomingBatsmanId = r.nextStrikerIdAfterDismissal,
                            nextStrikerIdAfterDismissal = r.nextStrikerIdAfterDismissal
                        )
                    }
                }
                e
            }

            // Replay Innings 2 if present
            val eng2 = inn2?.let {
                val target = (inn1?.totalRuns ?: eng1?.state?.totalRuns ?: 0) + 1
                val inn2BattingSquad = if (inn2.battingTeamId == match.teamAId) teamASquad else teamBSquad
                val inn2BowlingSquad = if (inn2.battingTeamId == match.teamAId) teamBSquad else teamASquad
                val maxWkts = (inn2BattingSquad.size - 1).coerceAtLeast(1)
                val e = ScoringEngine(
                    matchId = matchId,
                    inningsNumber = 2,
                    totalOversAllotted = match.totalOvers,
                    targetRuns = target,
                    maxWickets = maxWkts,
                    playerNames = playerNames
                )
                val firstDel = inn2Records.firstOrNull()
                val striker = firstDel?.strikerId ?: inn2BattingSquad.firstOrNull()?.userId ?: ""
                val nonStriker = firstDel?.nonStrikerId ?: inn2BattingSquad.getOrNull(1)?.userId ?: ""
                val bowler = firstDel?.bowlerId ?: inn2BowlingSquad.firstOrNull()?.userId ?: ""
                if (striker.isNotEmpty() && nonStriker.isNotEmpty() && bowler.isNotEmpty()) {
                    e.startInnings(striker, nonStriker, bowler)
                    inn2Records.forEach { r ->
                        if (r.bowlerId.isNotBlank() && r.bowlerId != e.state.currentBowlerId) {
                            e.changeBowler(r.bowlerId)
                        }
                        if (r.strikerId.isNotBlank() && r.strikerId == e.state.nonStrikerId) {
                            e.rotateStrike()
                        }
                        e.recordDelivery(
                            runsOffBat = r.runsOffBat,
                            extraType = r.extraType,
                            extraRuns = r.extraRuns,
                            dismissal = r.dismissal,
                            incomingBatsmanId = r.nextStrikerIdAfterDismissal,
                            nextStrikerIdAfterDismissal = r.nextStrikerIdAfterDismissal
                        )
                    }
                }
                e
            }

            val isMatchLive = match.matchStatus == MatchStatus.LIVE
            val activeEng = if (inn2 != null) eng2 else eng1
            engine = activeEng

            val activeBattingTeamName = if (inn2 != null) inn2BattingName else inn1BattingName
            val activeBowlingTeamName = if (inn2 != null) inn2BowlingName else inn1BowlingName

            val currentStriker = allPlayers.find { it.userId == activeEng?.state?.strikerId }
            val currentNonStriker = allPlayers.find { it.userId == activeEng?.state?.nonStrikerId }
            val currentBowler = allPlayers.find { it.userId == activeEng?.state?.currentBowlerId }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                matchId = match.matchId,
                inningsId = inn2?.inningsId ?: inn1?.inningsId ?: "",
                inningsNumber = if (inn2 != null) 2 else 1,
                matchTitle = match.matchTitle,
                teamAId = match.teamAId,
                teamBId = match.teamBId,
                battingTeamName = activeBattingTeamName,
                bowlingTeamName = activeBowlingTeamName,
                totalOvers = match.totalOvers,
                scoreState = activeEng?.state ?: InningsScoreState(),
                innings1Score = eng1?.state,
                innings1BattingTeamName = inn1BattingName,
                striker = currentStriker,
                nonStriker = currentNonStriker,
                bowler = currentBowler,
                battingSquad = if (activeBattingTeamName == teamAName) teamASquad else teamBSquad,
                bowlingSquad = if (activeBattingTeamName == teamAName) teamBSquad else teamASquad,
                currentInningsDeliveries = activeEng?.history ?: emptyList(),
                innings1Deliveries = eng1?.history ?: emptyList(),
                matchResult = if (match.matchStatus == MatchStatus.COMPLETED) {
                    MatchResult(
                        isCompleted = true,
                        winnerTeamId = match.winnerTeamId,
                        winnerTeamName = if (match.winnerTeamId == match.teamAId) teamAName else if (match.winnerTeamId == match.teamBId) teamBName else "Match Drawn",
                        resultSummary = match.resultSummary ?: "Completed",
                        playerOfTheMatchId = match.playerOfTheMatchId,
                        playerOfTheMatchName = playerNames[match.playerOfTheMatchId]
                    )
                } else null,
                lastActionMessage = if (isMatchLive) "Resumed match: ${match.matchTitle}" else (match.resultSummary ?: "Match Final")
            )

            onLoaded(isMatchLive)
        }
    }

    private fun DeliveryEntity.toRecord(): DeliveryRecord {
        val dismissalDetails = if (isWicket && dismissalType != null && dismissedPlayerId != null) {
            DismissalDetails(
                dismissalType = dismissalType,
                dismissedPlayerId = dismissedPlayerId,
                primaryFielderId = primaryFielderId,
                secondaryFielderId = secondaryFielderId,
                runsCompletedBeforeDismissal = runsCompletedBeforeDismissal
            )
        } else null

        return DeliveryRecord(
            deliveryIndex = deliveryIndex,
            overNumber = overNumber,
            ballInOver = ballInOver,
            strikerId = strikerId,
            nonStrikerId = nonStrikerId,
            bowlerId = bowlerId,
            runsOffBat = runsOffBat,
            extraType = extraType,
            extraRuns = extraRuns,
            isLegalDelivery = isLegalDelivery,
            dismissal = dismissalDetails,
            nextStrikerIdAfterDismissal = nextStrikerIdAfterDismissal,
            timestamp = timestamp
        )
    }
}
