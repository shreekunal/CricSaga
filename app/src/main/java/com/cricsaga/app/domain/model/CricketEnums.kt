package com.cricsaga.app.domain.model

enum class PlayingRole {
    BATSMAN,
    BOWLER,
    ALL_ROUNDER,
    WICKET_KEEPER
}

enum class BattingStyle {
    RIGHT_HAND,
    LEFT_HAND
}

enum class BowlingStyle {
    RIGHT_ARM_FAST,
    RIGHT_ARM_MEDIUM,
    RIGHT_ARM_SPIN,
    LEFT_ARM_FAST,
    LEFT_ARM_MEDIUM,
    LEFT_ARM_SPIN
}

enum class BallType {
    TENNIS_LIGHT,
    TENNIS_HEAVY,
    LEATHER
}

enum class TossDecision {
    BAT,
    BOWL
}

enum class MatchStatus {
    SCHEDULED,
    LIVE,
    COMPLETED,
    ABANDONED
}

enum class ExtraType {
    NONE,
    WIDE,
    NO_BALL,
    BYE,
    LEG_BYE,
    PENALTY
}

enum class DismissalType {
    BOWLED,
    CAUGHT,
    CAUGHT_BEHIND,
    CAUGHT_AND_BOWLED,
    RUN_OUT,
    STUMPED,
    LBW,
    HIT_WICKET,
    RETIRED_HURT,
    RETIRED_OUT
}
