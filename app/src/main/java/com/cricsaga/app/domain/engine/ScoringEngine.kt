package com.cricsaga.app.domain.engine

import com.cricsaga.app.domain.model.*

/**
 * Pure Kotlin cricket scoring state machine.
 *
 * Designed with ZERO Android dependencies to ensure:
 * 1. 100% determinism and rapid unit-test execution on JVM.
 * 2. Unbreakable single-ball undo via event-sourcing replay.
 * 3. Future portability to Web / KMP without rewriting cricket rules.
 */
class ScoringEngine(
    val matchId: String,
    val inningsNumber: Int,
    val totalOversAllotted: Int = 20,
    val targetRuns: Int? = null,
    val maxWickets: Int = 10,
    val playerNames: Map<String, String> = emptyMap()
) {
    private val deliveryHistory = mutableListOf<DeliveryRecord>()

    private var initialStrikerId: String = ""
    private var initialNonStrikerId: String = ""
    private var initialBowlerId: String = ""

    // Current in-memory state
    private var currentState: InningsScoreState = InningsScoreState(
        totalOversAllotted = totalOversAllotted,
        targetRuns = targetRuns
    )

    val state: InningsScoreState get() = currentState
    val history: List<DeliveryRecord> get() = deliveryHistory.toList()

    /**
     * Initializes the innings with the opening striker, non-striker, and opening bowler.
     */
    fun startInnings(strikerId: String, nonStrikerId: String, openingBowlerId: String): InningsScoreState {
        initialStrikerId = strikerId
        initialNonStrikerId = nonStrikerId
        initialBowlerId = openingBowlerId

        val initialBatsmen = mutableMapOf<String, BatsmanScore>()
        initialBatsmen[strikerId] = BatsmanScore(
            playerId = strikerId,
            playerName = playerNames[strikerId] ?: "Striker"
        )
        initialBatsmen[nonStrikerId] = BatsmanScore(
            playerId = nonStrikerId,
            playerName = playerNames[nonStrikerId] ?: "Non-Striker"
        )

        val initialBowlers = mutableMapOf<String, BowlerFigures>()
        initialBowlers[openingBowlerId] = BowlerFigures(
            bowlerId = openingBowlerId,
            bowlerName = playerNames[openingBowlerId] ?: "Bowler"
        )

        currentState = currentState.copy(
            strikerId = strikerId,
            nonStrikerId = nonStrikerId,
            currentBowlerId = openingBowlerId,
            batsmen = initialBatsmen,
            bowlers = initialBowlers
        )
        return currentState
    }

    /**
     * Records a new delivery and advances the cricket state machine.
     */
    fun recordDelivery(
        runsOffBat: Int = 0,
        extraType: ExtraType = ExtraType.NONE,
        extraRuns: Int = if (extraType == ExtraType.WIDE || extraType == ExtraType.NO_BALL) 1 else 0,
        dismissal: DismissalDetails? = null,
        incomingBatsmanId: String? = null,
        nextStrikerIdAfterDismissal: String? = null
    ): InningsScoreState {
        if (currentState.isCompleted) {
            return currentState
        }
        val isLegal = (extraType != ExtraType.WIDE && extraType != ExtraType.NO_BALL)
        val overNum = currentState.totalLegalDeliveries / 6
        val ballInOver = (currentState.totalLegalDeliveries % 6) + if (isLegal) 1 else 0

        val record = DeliveryRecord(
            deliveryIndex = deliveryHistory.size + 1,
            overNumber = overNum,
            ballInOver = ballInOver,
            strikerId = currentState.strikerId,
            nonStrikerId = currentState.nonStrikerId,
            bowlerId = currentState.currentBowlerId,
            runsOffBat = runsOffBat,
            extraType = extraType,
            extraRuns = extraRuns,
            isLegalDelivery = isLegal,
            dismissal = dismissal,
            nextStrikerIdAfterDismissal = nextStrikerIdAfterDismissal
        )

        deliveryHistory.add(record)
        recalculateState()
        return currentState
    }

    /**
     * Pops the last delivery from the history stack and recalculates match state.
     */
    fun undo(): InningsScoreState? {
        if (deliveryHistory.isEmpty()) return null
        deliveryHistory.removeAt(deliveryHistory.size - 1)
        recalculateState()
        return currentState
    }

    /**
     * Changes the current active bowler (e.g. at the start of a new over).
     */
    fun changeBowler(newBowlerId: String): InningsScoreState {
        val updatedBowlers = currentState.bowlers.toMutableMap()
        if (!updatedBowlers.containsKey(newBowlerId)) {
            updatedBowlers[newBowlerId] = BowlerFigures(
                bowlerId = newBowlerId,
                bowlerName = playerNames[newBowlerId] ?: "Bowler"
            )
        }
        currentState = currentState.copy(
            currentBowlerId = newBowlerId,
            bowlers = updatedBowlers
        )
        return currentState
    }

    /**
     * Manually rotates strike between striker and non-striker.
     */
    fun rotateStrike(): InningsScoreState {
        currentState = currentState.copy(
            strikerId = currentState.nonStrikerId,
            nonStrikerId = currentState.strikerId
        )
        return currentState
    }

    /**
     * Deterministically recalculates the entire score state by replaying delivery history.
     */
    private fun recalculateState() {
        var totalRuns = 0
        var totalWickets = 0
        var totalLegalDeliveries = 0

        // Reset in-memory scores to clean state
        val batsmenMap = currentState.batsmen.mapValues { (id, score) ->
            score.copy(runs = 0, ballsFaced = 0, fours = 0, sixes = 0, isOut = false, dismissal = null)
        }.toMutableMap()

        val bowlersMap = currentState.bowlers.mapValues { (id, figures) ->
            figures.copy(legalBalls = 0, maidens = 0, runsConceded = 0, wickets = 0, currentOverRuns = 0)
        }.toMutableMap()

        val currentOverTimeline = mutableListOf<OverTimelineBall>()

        var currentStriker = initialStrikerId
        var currentNonStriker = initialNonStrikerId
        var currentBowler = initialBowlerId

        for (deliv in deliveryHistory) {
            val strikerId = deliv.strikerId
            val nonStrikerId = deliv.nonStrikerId
            val bowlerId = deliv.bowlerId

            // Ensure player entries exist
            val striker = batsmenMap[strikerId] ?: BatsmanScore(strikerId, playerNames[strikerId] ?: "Striker")
            val bowler = bowlersMap[bowlerId] ?: BowlerFigures(bowlerId, playerNames[bowlerId] ?: "Bowler")

            // 1. Calculate runs
            val runsThisBall = deliv.totalRunsThisBall
            totalRuns += runsThisBall

            // 2. Update Batsman
            val isWide = deliv.extraType == ExtraType.WIDE
            val updatedBallsFaced = striker.ballsFaced + if (!isWide) 1 else 0
            val updatedRuns = striker.runs + deliv.runsOffBat
            val updatedFours = striker.fours + if (deliv.runsOffBat == 4) 1 else 0
            val updatedSixes = striker.sixes + if (deliv.runsOffBat == 6) 1 else 0

            var updatedStriker = striker.copy(
                runs = updatedRuns,
                ballsFaced = updatedBallsFaced,
                fours = updatedFours,
                sixes = updatedSixes
            )

            // 3. Update Bowler
            val runsChargedToBowler = deliv.runsOffBat + when (deliv.extraType) {
                ExtraType.WIDE, ExtraType.NO_BALL -> deliv.extraRuns
                else -> 0 // Byes and Leg Byes not charged to bowler
            }
            val legalBallIncrement = if (deliv.isLegalDelivery) 1 else 0
            val updatedLegalBalls = bowler.legalBalls + legalBallIncrement
            val updatedRunsConceded = bowler.runsConceded + runsChargedToBowler

            var isBowlerWicket = false

            // 4. Dismissal Resolution
            if (deliv.dismissal != null) {
                totalWickets += 1
                val dismissedId = deliv.dismissal.dismissedPlayerId

                // Check if wicket is credited to bowler
                isBowlerWicket = when (deliv.dismissal.dismissalType) {
                    DismissalType.BOWLED,
                    DismissalType.CAUGHT,
                    DismissalType.CAUGHT_BEHIND,
                    DismissalType.CAUGHT_AND_BOWLED,
                    DismissalType.STUMPED,
                    DismissalType.LBW,
                    DismissalType.HIT_WICKET -> true
                    DismissalType.RUN_OUT,
                    DismissalType.RETIRED_HURT,
                    DismissalType.RETIRED_OUT -> false
                }

                if (dismissedId == strikerId) {
                    updatedStriker = updatedStriker.copy(
                        isOut = true,
                        dismissal = deliv.dismissal
                    )
                } else if (dismissedId == nonStrikerId) {
                    val nonStriker = batsmenMap[nonStrikerId] ?: BatsmanScore(nonStrikerId, playerNames[nonStrikerId] ?: "Non-Striker")
                    batsmenMap[nonStrikerId] = nonStriker.copy(
                        isOut = true,
                        dismissal = deliv.dismissal
                    )
                }
            }

            val updatedWickets = bowler.wickets + if (isBowlerWicket) 1 else 0
            val updatedBowler = bowler.copy(
                legalBalls = updatedLegalBalls,
                runsConceded = updatedRunsConceded,
                wickets = updatedWickets
            )

            batsmenMap[strikerId] = updatedStriker
            bowlersMap[bowlerId] = updatedBowler

            if (deliv.isLegalDelivery) {
                totalLegalDeliveries += 1
            }

            // 5. Timeline badge
            val badgeLabel = when {
                deliv.dismissal != null -> "W" + if (deliv.runsOffBat > 0) "+${deliv.runsOffBat}" else ""
                deliv.extraType == ExtraType.WIDE -> if (deliv.extraRuns > 1) "${deliv.extraRuns}Wd" else "Wd"
                deliv.extraType == ExtraType.NO_BALL -> if (deliv.runsOffBat > 0) "Nb+${deliv.runsOffBat}" else "Nb"
                deliv.extraType == ExtraType.BYE -> "B${deliv.extraRuns}"
                deliv.extraType == ExtraType.LEG_BYE -> "Lb${deliv.extraRuns}"
                else -> "${deliv.runsOffBat}"
            }

            // If an over was completed on the prior legal ball, reset current over timeline
            if (deliv.isLegalDelivery && (totalLegalDeliveries - 1) % 6 == 0 && (totalLegalDeliveries - 1) > 0 && currentOverTimeline.size >= 6) {
                currentOverTimeline.clear()
            }
            currentOverTimeline.add(
                OverTimelineBall(
                    label = badgeLabel,
                    extraType = deliv.extraType,
                    runsOffBat = deliv.runsOffBat,
                    isWicket = deliv.dismissal != null
                )
            )

            // 6. Strike Rotation
            // Physical runs that switch ends:
            val physicalRuns = when (deliv.extraType) {
                ExtraType.WIDE -> deliv.extraRuns - 1 // Wides don't switch ends unless extra runs run
                ExtraType.BYE, ExtraType.LEG_BYE -> deliv.extraRuns
                else -> deliv.runsOffBat
            }

            val runsSwitchStrike = (physicalRuns % 2 != 0)
            if (runsSwitchStrike) {
                val temp = currentStriker
                currentStriker = currentNonStriker
                currentNonStriker = temp
            }

            // If wicket fell and next striker is specified
            if (deliv.dismissal != null && deliv.nextStrikerIdAfterDismissal != null) {
                currentStriker = deliv.nextStrikerIdAfterDismissal
            }

            // End of Over strike switch (occurs on the 6th legal delivery of the over)
            if (deliv.isLegalDelivery && totalLegalDeliveries % 6 == 0) {
                val temp = currentStriker
                currentStriker = currentNonStriker
                currentNonStriker = temp
                currentOverTimeline.clear() // Over completed
            }

            currentBowler = bowlerId
        }

        // Check Innings Completion
        val isTargetReached = (targetRuns != null && totalRuns >= targetRuns)
        val isAllOut = (totalWickets >= maxWickets)
        val isAllOversCompleted = (totalLegalDeliveries >= totalOversAllotted * 6)
        val isCompleted = isTargetReached || isAllOut || isAllOversCompleted

        val completionReason = when {
            isTargetReached -> InningsCompletionReason.TARGET_CHASED
            isAllOut -> InningsCompletionReason.ALL_OUT
            isAllOversCompleted -> InningsCompletionReason.OVERS_COMPLETED
            else -> null
        }

        val statusMessage = when (completionReason) {
            InningsCompletionReason.TARGET_CHASED -> "Target reached! Innings complete."
            InningsCompletionReason.ALL_OUT -> "All out! Innings complete."
            InningsCompletionReason.OVERS_COMPLETED -> "Overs completed."
            null -> "Live"
        }

        currentState = currentState.copy(
            totalRuns = totalRuns,
            totalWickets = totalWickets,
            totalLegalDeliveries = totalLegalDeliveries,
            strikerId = currentStriker,
            nonStrikerId = currentNonStriker,
            currentBowlerId = currentBowler,
            batsmen = batsmenMap,
            bowlers = bowlersMap,
            currentOverBalls = currentOverTimeline,
            isCompleted = isCompleted,
            completionReason = completionReason,
            statusMessage = statusMessage
        )
    }
}
