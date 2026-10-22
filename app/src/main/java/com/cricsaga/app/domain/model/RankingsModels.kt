package com.cricsaga.app.domain.model

data class PlayerRankingItem(
    val rank: Int,
    val userId: String,
    val playerName: String,
    val initials: String,
    val role: String,
    val teamName: String,
    val city: String,
    val matches: Int,
    // Batting metrics
    val runs: Int = 0,
    val innings: Int = 0,
    val battingAverage: Double = 0.0,
    val strikeRate: Double = 0.0,
    val highestScore: Int = 0,
    val fifties: Int = 0,
    val hundreds: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    // Bowling metrics
    val wickets: Int = 0,
    val oversBowled: Double = 0.0,
    val bowlingEconomy: Double = 0.0,
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    // All-Rounder MVP metric
    val mvpPoints: Double = 0.0,
    val isVerified: Boolean = false
)

data class TeamRankingItem(
    val rank: Int,
    val teamId: String,
    val teamName: String,
    val shortCode: String,
    val city: String,
    val matchesPlayed: Int,
    val wins: Int,
    val losses: Int,
    val ties: Int,
    val winPercentage: Double,
    val points: Int,
    val recentForm: List<MatchOutcome> = emptyList()
)

enum class MatchOutcome {
    WIN, LOSS, TIE
}

enum class RankingCategory(val title: String) {
    TOP_BATTERS("Top Batters"),
    TOP_BOWLERS("Top Bowlers"),
    MVP_ALL_ROUNDERS("MVP All-Rounders"),
    TEAM_STANDINGS("Team Rankings")
}
