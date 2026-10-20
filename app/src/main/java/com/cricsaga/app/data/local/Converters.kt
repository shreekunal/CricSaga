package com.cricsaga.app.data.local

import androidx.room.TypeConverter
import com.cricsaga.app.domain.model.*

class Converters {
    @TypeConverter
    fun fromPlayingRole(value: PlayingRole): String = value.name

    @TypeConverter
    fun toPlayingRole(value: String): PlayingRole = enumValueOf(value)

    @TypeConverter
    fun fromBattingStyle(value: BattingStyle): String = value.name

    @TypeConverter
    fun toBattingStyle(value: String): BattingStyle = enumValueOf(value)

    @TypeConverter
    fun fromBowlingStyle(value: BowlingStyle): String = value.name

    @TypeConverter
    fun toBowlingStyle(value: String): BowlingStyle = enumValueOf(value)

    @TypeConverter
    fun fromBallType(value: BallType): String = value.name

    @TypeConverter
    fun toBallType(value: String): BallType = enumValueOf(value)

    @TypeConverter
    fun fromTossDecision(value: TossDecision?): String? = value?.name

    @TypeConverter
    fun toTossDecision(value: String?): TossDecision? = value?.let { enumValueOf<TossDecision>(it) }

    @TypeConverter
    fun fromMatchStatus(value: MatchStatus): String = value.name

    @TypeConverter
    fun toMatchStatus(value: String): MatchStatus = enumValueOf(value)

    @TypeConverter
    fun fromExtraType(value: ExtraType): String = value.name

    @TypeConverter
    fun toExtraType(value: String): ExtraType = enumValueOf(value)

    @TypeConverter
    fun fromDismissalType(value: DismissalType?): String? = value?.name

    @TypeConverter
    fun toDismissalType(value: String?): DismissalType? = value?.let { enumValueOf<DismissalType>(it) }

    @TypeConverter
    fun fromProPlan(value: ProPlan?): String? = value?.name

    @TypeConverter
    fun toProPlan(value: String?): ProPlan? = value?.let { enumValueOf<ProPlan>(it) }
}
