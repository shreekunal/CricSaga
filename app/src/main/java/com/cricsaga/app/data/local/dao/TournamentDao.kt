package com.cricsaga.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cricsaga.app.data.local.entity.TournamentEntity
import com.cricsaga.app.data.local.entity.TournamentFixtureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixtures(fixtures: List<TournamentFixtureEntity>)

    @Query("SELECT * FROM tournaments ORDER BY createdAt DESC")
    fun getAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE tournamentId = :id")
    suspend fun getTournamentById(id: String): TournamentEntity?

    @Query("SELECT * FROM tournament_fixtures WHERE tournamentId = :tournamentId")
    fun getFixturesForTournament(tournamentId: String): Flow<List<TournamentFixtureEntity>>

    @Query("UPDATE tournament_fixtures SET matchId = :matchId, resultSummary = :result, isCompleted = 1 WHERE fixtureId = :fixtureId")
    suspend fun updateFixtureResult(fixtureId: String, matchId: String, result: String)
}
