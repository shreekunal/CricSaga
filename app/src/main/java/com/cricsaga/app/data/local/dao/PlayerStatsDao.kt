package com.cricsaga.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cricsaga.app.data.local.entity.PlayerStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: PlayerStatsEntity)

    @Query("SELECT * FROM player_stats WHERE userId = :userId")
    suspend fun getStatsForUser(userId: String): PlayerStatsEntity?

    @Query("SELECT * FROM player_stats WHERE userId = :userId")
    fun getStatsFlowForUser(userId: String): Flow<PlayerStatsEntity?>

    @Query("SELECT * FROM player_stats ORDER BY runsScored DESC")
    fun getAllPlayerStats(): Flow<List<PlayerStatsEntity>>
}
