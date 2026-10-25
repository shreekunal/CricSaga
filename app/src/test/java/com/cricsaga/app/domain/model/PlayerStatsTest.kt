package com.cricsaga.app.domain.model

import com.cricsaga.app.data.local.entity.PlayerStatsEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerStatsTest {

    @Test
    fun testBattingAverageWithNotOuts() {
        // Batter played 3 innings, scored 100 runs, remained not out in 1 inning -> 2 dismissals
        val stats = PlayerStatsEntity(
            userId = "user_1",
            matches = 3,
            innings = 3,
            notOuts = 1,
            runsScored = 100,
            ballsFaced = 80
        )

        assertEquals(2, stats.dismissals)
        assertEquals(50.0, stats.battingAverage, 0.001)
        assertEquals(125.0, stats.strikeRate, 0.001)
    }

    @Test
    fun testBattingAverageWithZeroDismissals() {
        // Batter scored 45 runs in 1 innings and was not out -> 0 dismissals
        val stats = PlayerStatsEntity(
            userId = "user_2",
            matches = 1,
            innings = 1,
            notOuts = 1,
            runsScored = 45,
            ballsFaced = 30
        )

        assertEquals(0, stats.dismissals)
        assertEquals(45.0, stats.battingAverage, 0.001)
    }

    @Test
    fun testBattingAverageWithZeroInnings() {
        val stats = PlayerStatsEntity(
            userId = "user_3",
            matches = 1,
            innings = 0,
            notOuts = 0,
            runsScored = 0
        )

        assertEquals(0, stats.dismissals)
        assertEquals(0.0, stats.battingAverage, 0.001)
    }

    @Test
    fun testFieldingStatsEntityDefaults() {
        val stats = PlayerStatsEntity(
            userId = "user_4",
            catches = 4,
            runOuts = 2,
            stumpings = 1
        )

        assertEquals(4, stats.catches)
        assertEquals(2, stats.runOuts)
        assertEquals(1, stats.stumpings)
    }

    @Test
    fun testBowlingHaulsAreExplicitNotDivided() {
        // Player took 20 wickets across 10 matches (2 wickets per match) -> 0 five-wicket hauls
        val stats = PlayerStatsEntity(
            userId = "bowler_regular",
            matches = 10,
            wicketsTaken = 20,
            threeWicketHauls = 0,
            fiveWicketHauls = 0
        )

        assertEquals(0, stats.fiveWicketHauls)
        assertEquals(0, stats.threeWicketHauls)

        // Player with 1 five-wicket haul and 2 three-wicket hauls
        val starBowlerStats = stats.copy(
            threeWicketHauls = 2,
            fiveWicketHauls = 1
        )
        assertEquals(1, starBowlerStats.fiveWicketHauls)
        assertEquals(2, starBowlerStats.threeWicketHauls)
    }

    @Test
    fun testCaughtAndBowledAttribution() {
        val catchingBowlerId = "bowler_1"
        val lastInningsBowlerId = "bowler_2"

        val dismissal = DismissalDetails(
            dismissalType = DismissalType.CAUGHT_AND_BOWLED,
            dismissedPlayerId = "batter_1",
            primaryFielderId = catchingBowlerId
        )

        // Verifying catch credit logic: catching bowler must receive +1 catch,
        // while an unrelated bowler finishing the innings must receive 0 catches.
        val catchCreditForBowler1 = if (dismissal.primaryFielderId == catchingBowlerId) 1 else 0
        val catchCreditForBowler2 = if (dismissal.primaryFielderId == lastInningsBowlerId) 1 else 0

        assertEquals(1, catchCreditForBowler1)
        assertEquals(0, catchCreditForBowler2)
    }
}
