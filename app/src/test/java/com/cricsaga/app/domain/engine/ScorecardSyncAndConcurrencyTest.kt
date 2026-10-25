package com.cricsaga.app.domain.engine

import com.cricsaga.app.domain.model.DismissalDetails
import com.cricsaga.app.domain.model.DismissalType
import com.cricsaga.app.domain.model.ExtraType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScorecardSyncAndConcurrencyTest {

    @Test
    fun testRapidTapMutexSerializationPreventsRaceConditions() = runBlocking {
        val mutex = Mutex()
        val engine = ScoringEngine(
            matchId = "test_match_concurrency",
            inningsNumber = 1,
            totalOversAllotted = 20,
            playerNames = mapOf("bat1" to "Batter 1", "bat2" to "Batter 2", "bowl1" to "Bowler 1")
        )
        engine.startInnings(strikerId = "bat1", nonStrikerId = "bat2", openingBowlerId = "bowl1")

        // Simulate 30 rapid tap coroutines executing concurrently
        val runsToRecord = listOf(1, 0, 4, 6, 2, 1, 0, 0, 4, 1, 2, 6, 1, 0, 0, 1, 4, 6, 2, 0, 1, 1, 4, 0, 6, 1, 2, 4, 1, 0)
        val expectedTotalRuns = runsToRecord.sum()

        val jobs = runsToRecord.map { runs ->
            async(Dispatchers.Default) {
                mutex.withLock {
                    engine.recordDelivery(runsOffBat = runs)
                }
            }
        }
        jobs.awaitAll()

        // Verify total runs and legal deliveries match expected sum without data loss
        assertEquals(expectedTotalRuns, engine.state.totalRuns)
        assertEquals(30, engine.state.totalLegalDeliveries)
        assertEquals(30, engine.history.size)

        // Verify delivery indices are strictly monotonic and sequential (1-indexed)
        engine.history.forEachIndexed { index, record ->
            assertEquals(index + 1, record.deliveryIndex)
        }
    }

    @Test
    fun testReplayFromDeliveriesReconstructsExactScorecard() {
        val playerNames = mapOf(
            "p1" to "Player One",
            "p2" to "Player Two",
            "p3" to "Player Three",
            "b1" to "Bowler One"
        )
        val originalEngine = ScoringEngine(
            matchId = "replay_test",
            inningsNumber = 1,
            totalOversAllotted = 5,
            playerNames = playerNames
        )
        originalEngine.startInnings(strikerId = "p1", nonStrikerId = "p2", openingBowlerId = "b1")
        originalEngine.recordDelivery(runsOffBat = 4)
        originalEngine.recordDelivery(runsOffBat = 1) // strike rotates to p2
        originalEngine.recordDelivery(runsOffBat = 6)
        originalEngine.recordDelivery(extraType = ExtraType.WIDE, extraRuns = 1)
        originalEngine.recordDelivery(
            runsOffBat = 0,
            dismissal = DismissalDetails(
                dismissalType = DismissalType.BOWLED,
                dismissedPlayerId = "p2"
            ),
            incomingBatsmanId = "p3",
            nextStrikerIdAfterDismissal = "p3"
        )

        // Save delivery records as would be done in SQLite and Supabase
        val savedRecords = originalEngine.history

        // Replay in a fresh engine (simulating spectator device downloading deliveries from Supabase)
        val replayedEngine = ScoringEngine(
            matchId = "replay_test",
            inningsNumber = 1,
            totalOversAllotted = 5,
            playerNames = playerNames
        )
        val firstDel = savedRecords.first()
        replayedEngine.startInnings(firstDel.strikerId, firstDel.nonStrikerId, firstDel.bowlerId)

        savedRecords.forEach { r ->
            if (r.bowlerId.isNotBlank() && r.bowlerId != replayedEngine.state.currentBowlerId) {
                replayedEngine.changeBowler(r.bowlerId)
            }
            if (r.strikerId.isNotBlank() && r.strikerId == replayedEngine.state.nonStrikerId) {
                replayedEngine.rotateStrike()
            }
            replayedEngine.recordDelivery(
                runsOffBat = r.runsOffBat,
                extraType = r.extraType,
                extraRuns = r.extraRuns,
                dismissal = r.dismissal,
                incomingBatsmanId = r.nextStrikerIdAfterDismissal,
                nextStrikerIdAfterDismissal = r.nextStrikerIdAfterDismissal
            )
        }

        // Verify state is 100% identical
        assertEquals(originalEngine.state.totalRuns, replayedEngine.state.totalRuns)
        assertEquals(originalEngine.state.totalWickets, replayedEngine.state.totalWickets)
        assertEquals(originalEngine.state.totalLegalDeliveries, replayedEngine.state.totalLegalDeliveries)
        assertEquals(originalEngine.state.completedOvers, replayedEngine.state.completedOvers)
        assertEquals(originalEngine.state.ballsInCurrentOver, replayedEngine.state.ballsInCurrentOver)
        assertEquals(originalEngine.state.batsmen["p1"]?.runs, replayedEngine.state.batsmen["p1"]?.runs)
        assertEquals(originalEngine.state.batsmen["p2"]?.runs, replayedEngine.state.batsmen["p2"]?.runs)
        assertEquals(originalEngine.state.bowlers["b1"]?.wickets, replayedEngine.state.bowlers["b1"]?.wickets)
        assertEquals(originalEngine.state.bowlers["b1"]?.runsConceded, replayedEngine.state.bowlers["b1"]?.runsConceded)
    }
}
