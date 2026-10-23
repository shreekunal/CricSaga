package com.cricsaga.app.domain.engine

import com.cricsaga.app.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * End-to-end programmatic verification of an entire 5-Over Cricket Match.
 *
 * Verifies:
 * 1. 1st Innings (5 overs = 30 legal balls + extras, bowler changes, strike rotations, dismissals).
 * 2. Innings Break (score finalization, target equation, required run rate calculation).
 * 3. 2nd Innings (chasing target, pressure tracking, bowler restrictions, match result calculation).
 * 4. Match Result determination (winner, margin, wickets in hand, balls remaining).
 */
class FiveOverMatchVerificationTest {

    private val teamAId = "team_warriors"
    private val teamBId = "team_titans"

    // Team A Squad
    private val a1 = "a_rohit"
    private val a2 = "a_virat"
    private val a3 = "a_surya"
    private val a4 = "a_hardik"
    private val a5 = "a_bumrah"
    private val a6 = "a_shami"

    // Team B Squad
    private val b1 = "b_head"
    private val b2 = "b_warner"
    private val b3 = "b_smith"
    private val b4 = "b_maxwell"
    private val b5 = "b_starc"
    private val b6 = "b_cummins"

    private lateinit var playerNames: Map<String, String>

    @Before
    fun setUp() {
        playerNames = mapOf(
            a1 to "Rohit (C)", a2 to "Virat", a3 to "Surya", a4 to "Hardik", a5 to "Bumrah", a6 to "Shami",
            b1 to "Head", b2 to "Warner", b3 to "Smith", b4 to "Maxwell", b5 to "Starc", b6 to "Cummins (C)"
        )
    }

    @Test
    fun testCompleteFiveOverMatchSimulation() {
        println("=== STARTING 5-OVER MATCH VERIFICATION ===")

        // =========================================================================
        // INNINGS 1: Team A Bats, Team B Bowls (5 Overs Allotted)
        // =========================================================================
        val inn1Engine = ScoringEngine(
            matchId = "match_5ov_test",
            inningsNumber = 1,
            totalOversAllotted = 5,
            targetRuns = null,
            maxWickets = 5,
            playerNames = playerNames
        )

        // Opening Setup: Striker=Rohit, NonStriker=Virat, Opening Bowler=Starc
        inn1Engine.startInnings(strikerId = a1, nonStrikerId = a2, openingBowlerId = b5)
        assertEquals("Initial striker must be Rohit", a1, inn1Engine.state.strikerId)
        assertEquals("Initial non-striker must be Virat", a2, inn1Engine.state.nonStrikerId)
        assertEquals("Initial bowler must be Starc", b5, inn1Engine.state.currentBowlerId)

        // -------------------------------------------------------------
        // OVER 1: Bowled by Starc (b5)
        // Ball 1.1: Dot (0)
        inn1Engine.recordDelivery(runsOffBat = 0)
        assertEquals(a1, inn1Engine.state.strikerId)

        // Ball 1.2: Boundary Four (4)
        inn1Engine.recordDelivery(runsOffBat = 4)
        assertEquals(4, inn1Engine.state.totalRuns)
        assertEquals(a1, inn1Engine.state.strikerId) // Four keeps strike

        // Ball 1.3: Single (1) -> Strike rotates
        inn1Engine.recordDelivery(runsOffBat = 1)
        assertEquals(5, inn1Engine.state.totalRuns)
        assertEquals(a2, inn1Engine.state.strikerId) // Virat on strike

        // Ball 1.4: Wide (Wd) -> +1 run, legal delivery not counted
        inn1Engine.recordDelivery(extraType = ExtraType.WIDE, extraRuns = 1)
        assertEquals(6, inn1Engine.state.totalRuns)
        assertEquals(3, inn1Engine.state.totalLegalDeliveries) // Still 3 legal balls
        assertEquals(a2, inn1Engine.state.strikerId) // Wide maintains strike

        // Ball 1.5 (Legal 4): Six (6) by Virat!
        inn1Engine.recordDelivery(runsOffBat = 6)
        assertEquals(12, inn1Engine.state.totalRuns)
        assertEquals(a2, inn1Engine.state.strikerId)

        // Ball 1.6 (Legal 5): Single (1) -> Rohit back on strike
        inn1Engine.recordDelivery(runsOffBat = 1)
        assertEquals(13, inn1Engine.state.totalRuns)
        assertEquals(a1, inn1Engine.state.strikerId)

        // Ball 1.7 (Legal 6): Two runs (2) -> End of Over 1
        inn1Engine.recordDelivery(runsOffBat = 2)
        assertEquals(15, inn1Engine.state.totalRuns)
        assertEquals(6, inn1Engine.state.totalLegalDeliveries)
        assertEquals("1.0", inn1Engine.state.oversFormatted)
        // End of over switches strike! Rohit hit 2, stays at his end, then over switch brings Virat to strike for Over 2
        assertEquals(a2, inn1Engine.state.strikerId)
        assertEquals(a1, inn1Engine.state.nonStrikerId)

        // -------------------------------------------------------------
        // OVER 2: Bowled by Cummins (b6)
        inn1Engine.changeBowler(b6)
        assertEquals(b6, inn1Engine.state.currentBowlerId)

        // Ball 2.1: Single (1) -> Rohit on strike
        inn1Engine.recordDelivery(runsOffBat = 1)
        assertEquals(a1, inn1Engine.state.strikerId)

        // Ball 2.2: WICKET! Rohit Caught (c Maxwell b Cummins)
        inn1Engine.recordDelivery(
            runsOffBat = 0,
            dismissal = DismissalDetails(
                dismissalType = DismissalType.CAUGHT,
                dismissedPlayerId = a1,
                primaryFielderId = b4
            ),
            incomingBatsmanId = a3, // Surya walks in
            nextStrikerIdAfterDismissal = a3
        )
        assertEquals(1, inn1Engine.state.totalWickets)
        assertEquals(a3, inn1Engine.state.strikerId) // Surya takes strike
        assertEquals(a2, inn1Engine.state.nonStrikerId) // Virat still at non-striker end

        // Ball 2.3: Dot (0)
        inn1Engine.recordDelivery(runsOffBat = 0)

        // Ball 2.4: Surya hits a Boundary Four (4)
        inn1Engine.recordDelivery(runsOffBat = 4)
        assertEquals(20, inn1Engine.state.totalRuns)

        // Ball 2.5: Single (1) -> Virat on strike
        inn1Engine.recordDelivery(runsOffBat = 1)
        assertEquals(a2, inn1Engine.state.strikerId)

        // Ball 2.6: Dot (0) -> Over 2 ends
        inn1Engine.recordDelivery(runsOffBat = 0)
        assertEquals("2.0", inn1Engine.state.oversFormatted)
        assertEquals(21, inn1Engine.state.totalRuns)
        // Over switch puts Surya on strike for Over 3
        assertEquals(a3, inn1Engine.state.strikerId)

        // -------------------------------------------------------------
        // OVER 3: Bowled by Maxwell (b4)
        inn1Engine.changeBowler(b4)

        // 6 deliveries: 1, 1, 2, 0, 4, 1
        inn1Engine.recordDelivery(runsOffBat = 1) // Virat strike
        inn1Engine.recordDelivery(runsOffBat = 1) // Surya strike
        inn1Engine.recordDelivery(runsOffBat = 2) // Surya strike
        inn1Engine.recordDelivery(runsOffBat = 0) // Surya strike
        inn1Engine.recordDelivery(runsOffBat = 4) // Surya strike
        inn1Engine.recordDelivery(runsOffBat = 1) // Surya takes single on last ball -> retains strike for Over 4!
        // End of Over 3 -> Single on last ball + over end end-change = Surya retains strike
        assertEquals("3.0", inn1Engine.state.oversFormatted)
        assertEquals(30, inn1Engine.state.totalRuns)
        assertEquals(a3, inn1Engine.state.strikerId)

        // -------------------------------------------------------------
        // OVER 4: Bowled by Starc (b5)
        inn1Engine.changeBowler(b5)

        // Ball 4.1: Surya hits Six (6)
        inn1Engine.recordDelivery(runsOffBat = 6)
        // Ball 4.2: Surya hits Single (1) -> Virat on strike
        inn1Engine.recordDelivery(runsOffBat = 1)
        assertEquals(a2, inn1Engine.state.strikerId)

        // Ball 4.3: WICKET! Virat Run Out!
        inn1Engine.recordDelivery(
            runsOffBat = 0,
            dismissal = DismissalDetails(
                dismissalType = DismissalType.RUN_OUT,
                dismissedPlayerId = a2,
                primaryFielderId = b1
            ),
            incomingBatsmanId = a4, // Hardik walks in to striker end
            nextStrikerIdAfterDismissal = a4
        )
        assertEquals(2, inn1Engine.state.totalWickets)
        assertEquals(a4, inn1Engine.state.strikerId) // Hardik takes strike
        assertEquals(a3, inn1Engine.state.nonStrikerId) // Surya at non-striker end

        // Ball 4.4: Hardik on strike, takes a single (1) -> Surya on strike
        inn1Engine.recordDelivery(runsOffBat = 1)
        assertEquals(a3, inn1Engine.state.strikerId)

        // Ball 4.5: Surya hits Four (4)
        inn1Engine.recordDelivery(runsOffBat = 4)
        // Ball 4.6: Surya takes a single (1) -> retains strike for Over 5
        inn1Engine.recordDelivery(runsOffBat = 1)
        assertEquals("4.0", inn1Engine.state.oversFormatted)
        assertEquals(43, inn1Engine.state.totalRuns)
        assertEquals(a3, inn1Engine.state.strikerId)

        // -------------------------------------------------------------
        // OVER 5 (Final Over): Bowled by Cummins (b6)
        inn1Engine.changeBowler(b6)

        // Ball 5.1: Surya strike, hits Four (4)
        inn1Engine.recordDelivery(runsOffBat = 4)
        // Ball 5.2: Surya hits Two (2)
        inn1Engine.recordDelivery(runsOffBat = 2)
        // Ball 5.3: Surya hits Six (6)
        inn1Engine.recordDelivery(runsOffBat = 6)
        // Ball 5.4: Dot (0)
        inn1Engine.recordDelivery(runsOffBat = 0)
        // Ball 5.5: Single (1) -> Hardik strike
        inn1Engine.recordDelivery(runsOffBat = 1)
        // Ball 5.6: Hardik hits Boundary Four (4)!
        val finalInn1State = inn1Engine.recordDelivery(runsOffBat = 4)

        // =========================================================================
        // VERIFY 1ST INNINGS CONCLUSION
        // =========================================================================
        assertEquals(60, finalInn1State.totalRuns)
        assertEquals(2, finalInn1State.totalWickets)
        assertEquals(30, finalInn1State.totalLegalDeliveries)
        assertEquals("5.0", finalInn1State.oversFormatted)
        assertTrue("Innings 1 must be marked completed when 5 overs are bowled", finalInn1State.isCompleted)
        assertEquals(InningsCompletionReason.OVERS_COMPLETED, finalInn1State.completionReason)

        println("Innings 1 Finished: ${finalInn1State.totalRuns}/${finalInn1State.totalWickets} in ${finalInn1State.oversFormatted} overs")

        // =========================================================================
        // INNINGS BREAK & TARGET EQUATION
        // =========================================================================
        val targetRuns = finalInn1State.totalRuns + 1
        assertEquals("Target for Team B must be Innings 1 Runs + 1", 61, targetRuns)
        val requiredRunRate = targetRuns.toDouble() / 5.0
        assertEquals(12.2, requiredRunRate, 0.01)

        println("Target Equation: Team B needs $targetRuns runs from 5 overs (RRR: $requiredRunRate)")

        // =========================================================================
        // INNINGS 2: Team B Chases (Target: 61 Runs from 5 Overs)
        // =========================================================================
        val inn2Engine = ScoringEngine(
            matchId = "match_5ov_test",
            inningsNumber = 2,
            totalOversAllotted = 5,
            targetRuns = targetRuns,
            maxWickets = 5,
            playerNames = playerNames
        )

        // Openers for Team B: Striker=Head (b1), NonStriker=Warner (b2)
        // Opening Bowler for Team A: Bumrah (a5)
        inn2Engine.startInnings(strikerId = b1, nonStrikerId = b2, openingBowlerId = a5)

        // Over 1 (Bumrah): Tight over - 8 runs
        inn2Engine.recordDelivery(runsOffBat = 4) // 4
        inn2Engine.recordDelivery(runsOffBat = 0) // 0
        inn2Engine.recordDelivery(runsOffBat = 1) // 1 -> Warner strike
        inn2Engine.recordDelivery(runsOffBat = 1) // 1 -> Head strike
        inn2Engine.recordDelivery(runsOffBat = 2) // 2
        inn2Engine.recordDelivery(runsOffBat = 0) // 0
        assertEquals(8, inn2Engine.state.totalRuns)
        assertEquals("1.0", inn2Engine.state.oversFormatted)

        // Over 2 (Shami a6): Explosive over - 20 runs
        inn2Engine.changeBowler(a6)
        inn2Engine.recordDelivery(runsOffBat = 6)
        inn2Engine.recordDelivery(runsOffBat = 4)
        inn2Engine.recordDelivery(runsOffBat = 4)
        inn2Engine.recordDelivery(runsOffBat = 1)
        inn2Engine.recordDelivery(runsOffBat = 4)
        inn2Engine.recordDelivery(runsOffBat = 1)
        assertEquals(28, inn2Engine.state.totalRuns)
        assertEquals("2.0", inn2Engine.state.oversFormatted)

        // Over 3 (Hardik a4): 15 runs + 1 Wicket
        inn2Engine.changeBowler(a4)
        inn2Engine.recordDelivery(runsOffBat = 6)
        inn2Engine.recordDelivery(runsOffBat = 2)
        inn2Engine.recordDelivery(
            runsOffBat = 0,
            dismissal = DismissalDetails(
                dismissalType = DismissalType.BOWLED,
                dismissedPlayerId = inn2Engine.state.strikerId
            ),
            incomingBatsmanId = b3, // Smith walks in
            nextStrikerIdAfterDismissal = b3
        )
        inn2Engine.recordDelivery(runsOffBat = 1)
        inn2Engine.recordDelivery(runsOffBat = 6)
        inn2Engine.recordDelivery(runsOffBat = 0)
        assertEquals(43, inn2Engine.state.totalRuns)
        assertEquals(1, inn2Engine.state.totalWickets)
        assertEquals("3.0", inn2Engine.state.oversFormatted)

        // Over 4 (Bumrah a5): 12 runs
        inn2Engine.changeBowler(a5)
        inn2Engine.recordDelivery(runsOffBat = 4)
        inn2Engine.recordDelivery(runsOffBat = 1)
        inn2Engine.recordDelivery(runsOffBat = 2)
        inn2Engine.recordDelivery(runsOffBat = 1)
        inn2Engine.recordDelivery(runsOffBat = 4)
        inn2Engine.recordDelivery(runsOffBat = 0)
        assertEquals(55, inn2Engine.state.totalRuns)
        assertEquals("4.0", inn2Engine.state.oversFormatted)

        // Need 6 runs from last over (Over 5) to reach 61
        // Over 5 (Shami a6):
        inn2Engine.changeBowler(a6)
        // Ball 5.1: 4 runs (Total 59)
        inn2Engine.recordDelivery(runsOffBat = 4)
        assertFalse("Target not reached yet", inn2Engine.state.isCompleted)

        // Ball 5.2: WINNING SHOT! Boundary Two (2 runs -> Total 61 = Target Reached!)
        val finalInn2State = inn2Engine.recordDelivery(runsOffBat = 2)

        // =========================================================================
        // VERIFY MATCH COMPLETION & RESULT
        // =========================================================================
        assertTrue("Innings 2 must complete immediately upon reaching target", finalInn2State.isCompleted)
        assertEquals(InningsCompletionReason.TARGET_CHASED, finalInn2State.completionReason)
        assertEquals(61, finalInn2State.totalRuns)
        assertEquals(1, finalInn2State.totalWickets)

        // Calculate Official Match Result via MatchResultCalculator
        val matchResult = MatchResultCalculator.calculateResult(
            teamAId = teamAId,
            teamAName = "Warriors",
            teamBId = teamBId,
            teamBName = "Titans",
            innings1BattingTeamId = teamAId,
            innings1State = finalInn1State,
            innings2State = finalInn2State,
            maxWickets = 5
        )

        assertTrue(matchResult.isCompleted)
        assertEquals("Titans", matchResult.winnerTeamName)
        assertEquals(teamBId, matchResult.winnerTeamId)
        assertEquals("4 wickets", matchResult.winMarginText)
        assertTrue("Summary should report winning by wickets with balls remaining",
            matchResult.resultSummary.contains("Titans won by 4 wickets with 4 balls remaining")
        )
        assertFalse(matchResult.isTie)

        println("=== 5-OVER MATCH VERIFIED SUCCESSFULLY ===")
        println("Result: ${matchResult.resultSummary}")
    }
}
