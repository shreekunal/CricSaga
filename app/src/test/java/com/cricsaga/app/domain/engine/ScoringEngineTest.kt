package com.cricsaga.app.domain.engine

import com.cricsaga.app.domain.model.DeliveryRecord
import com.cricsaga.app.domain.model.DismissalDetails
import com.cricsaga.app.domain.model.DismissalType
import com.cricsaga.app.domain.model.ExtraType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ScoringEngineTest {

    private lateinit var engine: ScoringEngine
    private val strikerId = "p1"
    private val nonStrikerId = "p2"
    private val bowlerId = "b1"
    private val incomingBatsmanId = "p3"

    @Before
    fun setUp() {
        val playerNames = mapOf(
            strikerId to "Rohit Sharma",
            nonStrikerId to "Virat Kohli",
            bowlerId to "Jasprit Bumrah",
            incomingBatsmanId to "Suryakumar Yadav"
        )
        engine = ScoringEngine(
            matchId = "match_101",
            inningsNumber = 1,
            totalOversAllotted = 20,
            playerNames = playerNames
        )
        engine.startInnings(strikerId, nonStrikerId, bowlerId)
    }

    @Test
    fun singleRotatesStrike() {
        val state = engine.recordDelivery(runsOffBat = 1)
        assertEquals(1, state.totalRuns)
        assertEquals(1, state.totalLegalDeliveries)
        assertEquals("0.1", state.oversFormatted)
        assertEquals(nonStrikerId, state.strikerId) // Strike rotated to non-striker
        assertEquals(strikerId, state.nonStrikerId)
        assertEquals(1, state.batsmen[strikerId]?.runs)
        assertEquals(1, state.batsmen[strikerId]?.ballsFaced)
    }

    @Test
    fun fourMaintainsStrike() {
        val state = engine.recordDelivery(runsOffBat = 4)
        assertEquals(4, state.totalRuns)
        assertEquals(1, state.totalLegalDeliveries)
        assertEquals(strikerId, state.strikerId) // Strike unchanged on boundary 4
        assertEquals(4, state.batsmen[strikerId]?.runs)
        assertEquals(1, state.batsmen[strikerId]?.fours)
    }

    @Test
    fun overCompletionSwitchesEnds() {
        // Bowl 6 dot balls (0 runs each)
        repeat(6) {
            engine.recordDelivery(runsOffBat = 0)
        }
        val state = engine.state
        assertEquals(0, state.totalRuns)
        assertEquals(6, state.totalLegalDeliveries)
        assertEquals("1.0", state.oversFormatted)
        // End of over switches strike to the other batsman
        assertEquals(nonStrikerId, state.strikerId)
        assertEquals(strikerId, state.nonStrikerId)
    }

    @Test
    fun oddRunOnLastBallOfOverRetainsStrike() {
        // Bowl 5 dot balls
        repeat(5) {
            engine.recordDelivery(runsOffBat = 0)
        }
        // 6th ball: batsman takes a single (1 run)
        val state = engine.recordDelivery(runsOffBat = 1)
        assertEquals(1, state.totalRuns)
        assertEquals(6, state.totalLegalDeliveries)
        assertEquals("1.0", state.oversFormatted)
        // Batsmen crossed on the single, and then switched ends on the over completion
        // Result: The original striker is back on strike for over 2!
        assertEquals(strikerId, state.strikerId)
    }

    @Test
    fun wideDoesNotIncrementLegalBallCount() {
        val state = engine.recordDelivery(
            extraType = ExtraType.WIDE,
            extraRuns = 1
        )
        assertEquals(1, state.totalRuns)
        assertEquals(0, state.totalLegalDeliveries) // Legal deliveries unchanged
        assertEquals("0.0", state.oversFormatted)
        assertEquals(strikerId, state.strikerId) // Strike unchanged
        assertEquals(0, state.batsmen[strikerId]?.ballsFaced) // Wide doesn't count against balls faced
        assertEquals(1, state.bowlers[bowlerId]?.runsConceded) // Charged to bowler
    }

    @Test
    fun noBallDoesNotIncrementLegalBallCount() {
        val state = engine.recordDelivery(
            runsOffBat = 2,
            extraType = ExtraType.NO_BALL,
            extraRuns = 1
        )
        assertEquals(3, state.totalRuns) // 2 off bat + 1 extra
        assertEquals(0, state.totalLegalDeliveries)
        assertEquals(1, state.batsmen[strikerId]?.ballsFaced) // No-ball counts as ball faced
        assertEquals(2, state.batsmen[strikerId]?.runs)
        assertEquals(3, state.bowlers[bowlerId]?.runsConceded)
        assertEquals(strikerId, state.strikerId) // Even run maintains strike
    }

    @Test
    fun caughtDismissalCreditsBowlerAndFielder() {
        val dismissal = DismissalDetails(
            dismissalType = DismissalType.CAUGHT,
            dismissedPlayerId = strikerId,
            primaryFielderId = "fielder_1"
        )
        val state = engine.recordDelivery(
            dismissal = dismissal,
            incomingBatsmanId = incomingBatsmanId,
            nextStrikerIdAfterDismissal = incomingBatsmanId
        )
        assertEquals(1, state.totalWickets)
        assertEquals(1, state.totalLegalDeliveries)
        assertEquals(1, state.bowlers[bowlerId]?.wickets) // Credited to bowler
        assertTrue(state.batsmen[strikerId]?.isOut == true)
        assertEquals(DismissalType.CAUGHT, state.batsmen[strikerId]?.dismissal?.dismissalType)
        assertEquals(incomingBatsmanId, state.strikerId) // New batsman on strike
    }

    @Test
    fun runOutDoesNotCreditBowler() {
        val dismissal = DismissalDetails(
            dismissalType = DismissalType.RUN_OUT,
            dismissedPlayerId = strikerId,
            primaryFielderId = "fielder_1",
            runsCompletedBeforeDismissal = 1
        )
        val state = engine.recordDelivery(
            runsOffBat = 1,
            dismissal = dismissal,
            incomingBatsmanId = incomingBatsmanId,
            nextStrikerIdAfterDismissal = nonStrikerId
        )
        assertEquals(1, state.totalWickets)
        assertEquals(1, state.totalRuns)
        assertEquals(0, state.bowlers[bowlerId]?.wickets) // Run-out NOT credited to bowler
        assertTrue(state.batsmen[strikerId]?.isOut == true)
    }

    @Test
    fun fullUndoRestoresPreviousScoreAndStrike() {
        // Record 1 run
        engine.recordDelivery(runsOffBat = 1)
        assertEquals(1, engine.state.totalRuns)
        assertEquals(nonStrikerId, engine.state.strikerId)

        // Undo
        val revertedState = engine.undo()
        assertNotNull(revertedState)
        assertEquals(0, revertedState?.totalRuns)
        assertEquals(0, revertedState?.totalLegalDeliveries)
        assertEquals(strikerId, revertedState?.strikerId) // Original striker restored
        assertEquals(0, revertedState?.batsmen?.get(strikerId)?.runs)
        assertEquals(0, revertedState?.batsmen?.get(strikerId)?.ballsFaced)
    }

    @Test
    fun multipleSequentialUndosBackToMatchStart() {
        // Ball 1: 4
        engine.recordDelivery(runsOffBat = 4)
        // Ball 2: 1 (rotates strike)
        engine.recordDelivery(runsOffBat = 1)
        // Ball 3: 6
        engine.recordDelivery(runsOffBat = 6)

        assertEquals(11, engine.state.totalRuns)
        assertEquals(3, engine.state.totalLegalDeliveries)

        // Undo ball 3
        engine.undo()
        assertEquals(5, engine.state.totalRuns)
        assertEquals(2, engine.state.totalLegalDeliveries)

        // Undo ball 2
        engine.undo()
        assertEquals(4, engine.state.totalRuns)
        assertEquals(1, engine.state.totalLegalDeliveries)
        assertEquals(strikerId, engine.state.strikerId)

        // Undo ball 1
        engine.undo()
        assertEquals(0, engine.state.totalRuns)
        assertEquals(0, engine.state.totalLegalDeliveries)
        assertEquals(emptyList<DeliveryRecord>(), engine.history)
    }
}
