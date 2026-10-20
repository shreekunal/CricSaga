package com.cricsaga.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cricsaga.app.domain.model.BallType

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey
    val tournamentId: String,
    val name: String,
    val overs: Int = 20,
    val ballType: BallType = BallType.TENNIS_LIGHT,
    val city: String = "Mumbai",
    val status: String = "ACTIVE",
    val teamNamesCsv: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tournament_fixtures")
data class TournamentFixtureEntity(
    @PrimaryKey
    val fixtureId: String,
    val tournamentId: String,
    val stage: String,
    val teamAName: String,
    val teamBName: String,
    val scheduledTime: String = "Today",
    val matchId: String? = null,
    val resultSummary: String? = null,
    val isCompleted: Boolean = false
)
