package com.cricsaga.app.domain.engine

import com.cricsaga.app.domain.model.InningsScoreState
import com.cricsaga.app.domain.model.MatchResult

object MatchResultCalculator {

    fun calculateResult(
        teamAId: String,
        teamAName: String,
        teamBId: String,
        teamBName: String,
        innings1BattingTeamId: String,
        innings1State: InningsScoreState,
        innings2State: InningsScoreState,
        maxWickets: Int = 10
    ): MatchResult {
        val targetRuns = innings2State.targetRuns ?: (innings1State.totalRuns + 1)
        val team1Name = if (innings1BattingTeamId == teamAId) teamAName else teamBName
        val team1Id = if (innings1BattingTeamId == teamAId) teamAId else teamBId

        val team2Name = if (innings1BattingTeamId == teamAId) teamBName else teamAName
        val team2Id = if (innings1BattingTeamId == teamAId) teamBId else teamAId

        val innings2Runs = innings2State.totalRuns
        val innings2Wickets = innings2State.totalWickets
        val totalOvers = innings2State.totalOversAllotted
        val totalBalls = totalOvers * 6
        val ballsBowled = innings2State.totalLegalDeliveries
        val ballsRemaining = (totalBalls - ballsBowled).coerceAtLeast(0)
        val wicketsInHand = (maxWickets - innings2Wickets).coerceAtLeast(0)

        // Case 1: Chasing team achieved target!
        if (innings2Runs >= targetRuns) {
            val margin = "$wicketsInHand wicket${if (wicketsInHand == 1) "" else "s"}"
            val ballsText = if (ballsRemaining > 0) " with $ballsRemaining ball${if (ballsRemaining == 1) "" else "s"} remaining" else ""
            val summary = "$team2Name won by $margin$ballsText"
            return MatchResult(
                isCompleted = true,
                winnerTeamId = team2Id,
                winnerTeamName = team2Name,
                winMarginText = margin,
                resultSummary = summary,
                isTie = false
            )
        }

        // Case 2: Innings 2 is completed without chasing target (all out or overs completed)
        val isAllOut = (innings2Wickets >= maxWickets)
        val isOversDone = (ballsBowled >= totalBalls)

        if (isAllOut || isOversDone) {
            val deficit = (targetRuns - 1) - innings2Runs
            return if (deficit > 0) {
                val margin = "$deficit run${if (deficit == 1) "" else "s"}"
                val summary = "$team1Name won by $margin"
                MatchResult(
                    isCompleted = true,
                    winnerTeamId = team1Id,
                    winnerTeamName = team1Name,
                    winMarginText = margin,
                    resultSummary = summary,
                    isTie = false
                )
            } else if (deficit == 0) {
                MatchResult(
                    isCompleted = true,
                    winnerTeamId = null,
                    winnerTeamName = null,
                    winMarginText = "Match Tied",
                    resultSummary = "Scores level! Match Tied.",
                    isTie = true
                )
            } else {
                MatchResult(
                    isCompleted = true,
                    winnerTeamId = team2Id,
                    winnerTeamName = team2Name,
                    winMarginText = "$wicketsInHand wickets",
                    resultSummary = "$team2Name won by $wicketsInHand wickets"
                )
            }
        }

        // Match still in progress
        return MatchResult(isCompleted = false)
    }
}
