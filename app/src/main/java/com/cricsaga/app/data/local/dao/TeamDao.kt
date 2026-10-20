package com.cricsaga.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cricsaga.app.data.local.entity.TeamEntity
import com.cricsaga.app.data.local.entity.TeamMemberEntity
import com.cricsaga.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMember(member: TeamMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMembers(members: List<TeamMemberEntity>)

    @Query("SELECT * FROM teams WHERE teamId = :teamId")
    suspend fun getTeamById(teamId: String): TeamEntity?

    @Query("SELECT * FROM teams ORDER BY teamName ASC")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Query("""
        SELECT u.* FROM users u
        INNER JOIN team_members tm ON u.userId = tm.userId
        WHERE tm.teamId = :teamId
        ORDER BY u.fullName ASC
    """)
    fun getTeamMembers(teamId: String): Flow<List<UserEntity>>
}
