package com.cricsaga.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_stats")
data class PlayerStatsEntity(
    @PrimaryKey
    val userId: String,
    val matches: Int = 0,
    val innings: Int = 0,
    val notOuts: Int = 0,
    val runsScored: Int = 0,
    val ballsFaced: Int = 0,
    val highestScore: Int = 0,
    val fifties: Int = 0,
    val hundreds: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val wicketsTaken: Int = 0,
    val legalBallsBowled: Int = 0,
    val runsConceded: Int = 0,
    val maidens: Int = 0,
    val catches: Int = 0,
    val runOuts: Int = 0,
    val stumpings: Int = 0,
    val threeWicketHauls: Int = 0,
    val fiveWicketHauls: Int = 0
) {
    val dismissals: Int
        get() = (innings - notOuts).coerceAtLeast(0)

    val battingAverage: Double
        get() = when {
            dismissals > 0 -> runsScored.toDouble() / dismissals.toDouble()
            innings > 0 -> runsScored.toDouble()
            else -> 0.0
        }

    val strikeRate: Double
        get() = if (ballsFaced > 0) (runsScored.toDouble() / ballsFaced.toDouble()) * 100.0 else 0.0

    val bowlingEconomy: Double
        get() = if (legalBallsBowled > 0) (runsConceded.toDouble() / (legalBallsBowled.toDouble() / 6.0)) else 0.0
}
