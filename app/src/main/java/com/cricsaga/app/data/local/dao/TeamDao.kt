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

    @Query("DELETE FROM teams WHERE teamId NOT IN (:keepTeamIds)")
    suspend fun deleteTeamsNotIn(keepTeamIds: List<String>)

    @Query("DELETE FROM team_members")
    suspend fun clearAllTeamMembers()

    @Query("SELECT * FROM teams WHERE teamId = :teamId")
    suspend fun getTeamById(teamId: String): TeamEntity?

    @Query("SELECT * FROM team_members")
    fun getAllTeamMembers(): Flow<List<TeamMemberEntity>>

    @Query("SELECT * FROM teams ORDER BY teamName ASC")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT COUNT(*) FROM teams")
    fun getTeamCountFlow(): Flow<Int>

    @Query("""
        SELECT DISTINCT t.* FROM teams t
        LEFT JOIN team_members tm ON t.teamId = tm.teamId
        WHERE tm.userId = :userId OR t.captainId = :userId
        ORDER BY t.teamName ASC
    """)
    fun getTeamsForUser(userId: String): Flow<List<TeamEntity>>

    @Query("""
        SELECT u.* FROM users u
        INNER JOIN team_members tm ON u.userId = tm.userId
        WHERE tm.teamId = :teamId
        ORDER BY u.fullName ASC
    """)
    fun getTeamMembers(teamId: String): Flow<List<UserEntity>>

    @Query("""
        SELECT u.* FROM users u
        INNER JOIN team_members tm ON u.userId = tm.userId
        WHERE tm.teamId = :teamId
        ORDER BY u.fullName ASC
    """)
    suspend fun getTeamMembersList(teamId: String): List<UserEntity>

    @Query("DELETE FROM team_members WHERE teamId = :teamId AND userId = :userId")
    suspend fun removeTeamMember(teamId: String, userId: String)

    @Query("UPDATE teams SET captainId = :captainId WHERE teamId = :teamId")
    suspend fun updateTeamCaptain(teamId: String, captainId: String)

    @Query("DELETE FROM teams WHERE teamId = :teamId")
    suspend fun deleteTeam(teamId: String)
}
