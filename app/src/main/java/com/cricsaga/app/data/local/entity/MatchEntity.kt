package com.cricsaga.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cricsaga.app.domain.model.BallType
import com.cricsaga.app.domain.model.MatchStatus
import com.cricsaga.app.domain.model.TossDecision

@Entity(
    tableName = "matches",
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["teamId"],
            childColumns = ["teamAId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["teamId"],
            childColumns = ["teamBId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("teamAId"), Index("teamBId")]
)
data class MatchEntity(
    @PrimaryKey
    val matchId: String,
    val matchTitle: String,
    val teamAId: String,
    val teamBId: String,
    val totalOvers: Int = 20,
    val ballType: BallType = BallType.TENNIS_LIGHT,
    val tossWinnerTeamId: String? = null,
    val tossDecision: TossDecision? = null,
    val maxOversPerBowler: Int = 4,
    val isLbwEnabled: Boolean = false,
    val isFreeHitEnabled: Boolean = true,
    val matchStatus: MatchStatus = MatchStatus.SCHEDULED,
    val winnerTeamId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "innings",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["matchId"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["teamId"],
            childColumns = ["battingTeamId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["teamId"],
            childColumns = ["bowlingTeamId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("matchId"), Index("battingTeamId"), Index("bowlingTeamId")]
)
data class InningsEntity(
    @PrimaryKey
    val inningsId: String,
    val matchId: String,
    val inningsNumber: Int, // 1 or 2
    val battingTeamId: String,
    val bowlingTeamId: String,
    val totalRuns: Int = 0,
    val totalWickets: Int = 0,
    val totalLegalDeliveries: Int = 0,
    val targetRuns: Int? = null,
    val isCompleted: Boolean = false
)
