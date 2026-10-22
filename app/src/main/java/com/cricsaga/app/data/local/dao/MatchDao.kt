package com.cricsaga.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cricsaga.app.data.local.entity.InningsEntity
import com.cricsaga.app.data.local.entity.MatchEntity
import com.cricsaga.app.domain.model.MatchStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("UPDATE matches SET matchStatus = :status WHERE matchId = :matchId")
    suspend fun updateMatchStatus(matchId: String, status: MatchStatus)

    @Query("UPDATE matches SET matchStatus = :status, winnerTeamId = :winnerTeamId, resultSummary = :resultSummary, playerOfTheMatchId = :playerOfTheMatchId WHERE matchId = :matchId")
    suspend fun completeMatch(
        matchId: String,
        status: MatchStatus = MatchStatus.COMPLETED,
        winnerTeamId: String?,
        resultSummary: String?,
        playerOfTheMatchId: String?
    )

    @Query("SELECT * FROM matches WHERE matchId = :matchId")
    suspend fun getMatchById(matchId: String): MatchEntity?

    @Query("SELECT * FROM matches ORDER BY createdAt DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches ORDER BY createdAt DESC")
    suspend fun getAllMatchesList(): List<MatchEntity>
}

@Dao
interface InningsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInnings(innings: InningsEntity)

    @Update
    suspend fun updateInnings(innings: InningsEntity)

    @Query("SELECT * FROM innings WHERE inningsId = :inningsId")
    suspend fun getInningsById(inningsId: String): InningsEntity?

    @Query("UPDATE innings SET isCompleted = 1 WHERE inningsId = :inningsId")
    suspend fun completeInnings(inningsId: String)

    @Query("SELECT * FROM innings WHERE matchId = :matchId ORDER BY inningsNumber ASC")
    fun getInningsForMatch(matchId: String): Flow<List<InningsEntity>>

    @Query("SELECT * FROM innings WHERE matchId = :matchId ORDER BY inningsNumber ASC")
    suspend fun getInningsListForMatch(matchId: String): List<InningsEntity>
}
