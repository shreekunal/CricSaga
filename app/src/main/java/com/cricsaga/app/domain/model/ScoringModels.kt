package com.cricsaga.app.domain.model

data class DismissalDetails(
    val dismissalType: DismissalType,
    val dismissedPlayerId: String,
    val primaryFielderId: String? = null,
    val secondaryFielderId: String? = null,
    val runsCompletedBeforeDismissal: Int = 0
)

data class DeliveryRecord(
    val deliveryIndex: Int,
    val overNumber: Int,
    val ballInOver: Int, // 1..6 for legal balls, or indicator for extras
    val strikerId: String,
    val nonStrikerId: String,
    val bowlerId: String,
    val runsOffBat: Int,
    val extraType: ExtraType,
    val extraRuns: Int,
    val isLegalDelivery: Boolean,
    val dismissal: DismissalDetails? = null,
    val nextStrikerIdAfterDismissal: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalRunsThisBall: Int
        get() = runsOffBat + extraRuns
}

data class BatsmanScore(
    val playerId: String,
    val playerName: String,
    val runs: Int = 0,
    val ballsFaced: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val isOut: Boolean = false,
    val dismissal: DismissalDetails? = null
) {
    val strikeRate: Double
        get() = if (ballsFaced > 0) (runs.toDouble() / ballsFaced) * 100.0 else 0.0
}

data class BowlerFigures(
    val bowlerId: String,
    val bowlerName: String,
    val legalBalls: Int = 0,
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val currentOverRuns: Int = 0
) {
    val completedOvers: Int get() = legalBalls / 6
    val ballsInCurrentOver: Int get() = legalBalls % 6

    val oversFormatted: String
        get() = "$completedOvers.$ballsInCurrentOver"

    val economyRate: Double
        get() = if (legalBalls > 0) (runsConceded.toDouble() / (legalBalls.toDouble() / 6.0)) else 0.0
}

data class OverTimelineBall(
    val label: String,
    val extraType: ExtraType,
    val runsOffBat: Int,
    val isWicket: Boolean
)

data class InningsScoreState(
    val totalRuns: Int = 0,
    val totalWickets: Int = 0,
    val totalLegalDeliveries: Int = 0,
    val totalOversAllotted: Int = 20,
    val strikerId: String = "",
    val nonStrikerId: String = "",
    val currentBowlerId: String = "",
    val batsmen: Map<String, BatsmanScore> = emptyMap(),
    val bowlers: Map<String, BowlerFigures> = emptyMap(),
    val currentOverBalls: List<OverTimelineBall> = emptyList(),
    val targetRuns: Int? = null,
    val isCompleted: Boolean = false,
    val completionReason: InningsCompletionReason? = null,
    val statusMessage: String = ""
) {
    val completedOvers: Int get() = totalLegalDeliveries / 6
    val ballsInCurrentOver: Int get() = totalLegalDeliveries % 6
    val oversFormatted: String get() = "$completedOvers.$ballsInCurrentOver"

    val currentRunRate: Double
        get() = if (totalLegalDeliveries > 0) {
            (totalRuns.toDouble() / (totalLegalDeliveries.toDouble() / 6.0))
        } else 0.0

    val runsNeeded: Int?
        get() = targetRuns?.let { (it - totalRuns).coerceAtLeast(0) }

    val ballsRemaining: Int?
        get() = if (targetRuns != null) {
            ((totalOversAllotted * 6) - totalLegalDeliveries).coerceAtLeast(0)
        } else null

    val requiredRunRate: Double?
        get() {
            if (targetRuns == null) return null
            val runs = runsNeeded ?: 0
            val balls = ballsRemaining ?: 0
            return if (balls > 0 && runs > 0) {
                (runs.toDouble() / (balls.toDouble() / 6.0))
            } else 0.0
        }
}

enum class InningsCompletionReason {
    OVERS_COMPLETED,
    ALL_OUT,
    TARGET_CHASED
}

data class MatchResult(
    val isCompleted: Boolean = false,
    val winnerTeamId: String? = null,
    val winnerTeamName: String? = null,
    val winMarginText: String = "",
    val resultSummary: String = "",
    val isTie: Boolean = false,
    val playerOfTheMatchId: String? = null,
    val playerOfTheMatchName: String? = null
)
