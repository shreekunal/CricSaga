package com.cricsaga.app.data.local

import com.cricsaga.app.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun testPlayingRoleSafeConversion() {
        // Valid exact uppercase
        assertEquals(PlayingRole.BATSMAN, converters.toPlayingRole("BATSMAN"))
        assertEquals(PlayingRole.BOWLER, converters.toPlayingRole("BOWLER"))

        // Case insensitive & whitespace trimmed
        assertEquals(PlayingRole.ALL_ROUNDER, converters.toPlayingRole("all_rounder"))
        assertEquals(PlayingRole.WICKET_KEEPER, converters.toPlayingRole("  wicket_keeper  "))

        // Corrupted, empty, or null strings fallback safely without throwing
        assertEquals(PlayingRole.ALL_ROUNDER, converters.toPlayingRole("UNKNOWN_ROLE"))
        assertEquals(PlayingRole.ALL_ROUNDER, converters.toPlayingRole(""))
        assertEquals(PlayingRole.ALL_ROUNDER, converters.toPlayingRole(null))
    }

    @Test
    fun testMatchStatusSafeConversion() {
        assertEquals(MatchStatus.LIVE, converters.toMatchStatus("LIVE"))
        assertEquals(MatchStatus.COMPLETED, converters.toMatchStatus("completed"))
        assertEquals(MatchStatus.SCHEDULED, converters.toMatchStatus("corrupted_value"))
        assertEquals(MatchStatus.SCHEDULED, converters.toMatchStatus(null))
    }

    @Test
    fun testBallTypeSafeConversion() {
        assertEquals(BallType.LEATHER, converters.toBallType("LEATHER"))
        assertEquals(BallType.TENNIS_HEAVY, converters.toBallType("tennis_heavy"))
        assertEquals(BallType.TENNIS_LIGHT, converters.toBallType("INVALID_BALL"))
        assertEquals(BallType.TENNIS_LIGHT, converters.toBallType(null))
    }

    @Test
    fun testNullableEnumsSafeConversion() {
        assertEquals(TossDecision.BAT, converters.toTossDecision("BAT"))
        assertEquals(TossDecision.BOWL, converters.toTossDecision("bowl"))
        assertNull(converters.toTossDecision("INVALID"))
        assertNull(converters.toTossDecision(null))

        assertEquals(DismissalType.BOWLED, converters.toDismissalType("BOWLED"))
        assertEquals(DismissalType.CAUGHT, converters.toDismissalType("caught"))
        assertNull(converters.toDismissalType("INVALID_DISMISSAL"))
        assertNull(converters.toDismissalType(null))

        assertEquals(ProPlan.LIFETIME, converters.toProPlan("LIFETIME"))
        assertEquals(ProPlan.ONE_YEAR, converters.toProPlan("one_year"))
        assertNull(converters.toProPlan("INVALID_PLAN"))
        assertNull(converters.toProPlan(null))
    }
}
