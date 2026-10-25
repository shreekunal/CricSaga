package com.cricsaga.app.data.local

import androidx.room.TypeConverter
import com.cricsaga.app.domain.model.*

class Converters {
    @TypeConverter
    fun fromPlayingRole(value: PlayingRole?): String = (value ?: PlayingRole.ALL_ROUNDER).name

    @TypeConverter
    fun toPlayingRole(value: String?): PlayingRole =
        runCatching { enumValueOf<PlayingRole>(value?.trim()?.uppercase() ?: "") }
            .getOrDefault(PlayingRole.ALL_ROUNDER)

    @TypeConverter
    fun fromBattingStyle(value: BattingStyle?): String = (value ?: BattingStyle.RIGHT_HAND).name

    @TypeConverter
    fun toBattingStyle(value: String?): BattingStyle =
        runCatching { enumValueOf<BattingStyle>(value?.trim()?.uppercase() ?: "") }
            .getOrDefault(BattingStyle.RIGHT_HAND)

    @TypeConverter
    fun fromBowlingStyle(value: BowlingStyle?): String = (value ?: BowlingStyle.RIGHT_ARM_MEDIUM).name

    @TypeConverter
    fun toBowlingStyle(value: String?): BowlingStyle =
        runCatching { enumValueOf<BowlingStyle>(value?.trim()?.uppercase() ?: "") }
            .getOrDefault(BowlingStyle.RIGHT_ARM_MEDIUM)

    @TypeConverter
    fun fromBallType(value: BallType?): String = (value ?: BallType.TENNIS_LIGHT).name

    @TypeConverter
    fun toBallType(value: String?): BallType =
        runCatching { enumValueOf<BallType>(value?.trim()?.uppercase() ?: "") }
            .getOrDefault(BallType.TENNIS_LIGHT)

    @TypeConverter
    fun fromTossDecision(value: TossDecision?): String? = value?.name

    @TypeConverter
    fun toTossDecision(value: String?): TossDecision? =
        value?.let { v -> runCatching { enumValueOf<TossDecision>(v.trim().uppercase()) }.getOrNull() }

    @TypeConverter
    fun fromMatchStatus(value: MatchStatus?): String = (value ?: MatchStatus.SCHEDULED).name

    @TypeConverter
    fun toMatchStatus(value: String?): MatchStatus =
        runCatching { enumValueOf<MatchStatus>(value?.trim()?.uppercase() ?: "") }
            .getOrDefault(MatchStatus.SCHEDULED)

    @TypeConverter
    fun fromExtraType(value: ExtraType?): String = (value ?: ExtraType.NONE).name

    @TypeConverter
    fun toExtraType(value: String?): ExtraType =
        runCatching { enumValueOf<ExtraType>(value?.trim()?.uppercase() ?: "") }
            .getOrDefault(ExtraType.NONE)

    @TypeConverter
    fun fromDismissalType(value: DismissalType?): String? = value?.name

    @TypeConverter
    fun toDismissalType(value: String?): DismissalType? =
        value?.let { v -> runCatching { enumValueOf<DismissalType>(v.trim().uppercase()) }.getOrNull() }

    @TypeConverter
    fun fromProPlan(value: ProPlan?): String? = value?.name

    @TypeConverter
    fun toProPlan(value: String?): ProPlan? =
        value?.let { v -> runCatching { enumValueOf<ProPlan>(v.trim().uppercase()) }.getOrNull() }
}
