package com.cricsaga.app.data.repository

import com.cricsaga.app.data.local.CricSagaDatabase
import com.cricsaga.app.data.local.entity.*
import com.cricsaga.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

data class MatchCreationResult(
    val matchId: String,
    val inningsId: String,
    val battingTeamId: String,
    val bowlingTeamId: String
)

class MatchRepository(private val database: CricSagaDatabase) {

    private val userDao = database.userDao()
    private val teamDao = database.teamDao()
    private val matchDao = database.matchDao()
    private val inningsDao = database.inningsDao()
    private val deliveryDao = database.deliveryDao()

    fun getAllMatches(): Flow<List<MatchEntity>> = matchDao.getAllMatches()

    suspend fun getMatch(matchId: String): MatchEntity? = withContext(Dispatchers.IO) {
        matchDao.getMatchById(matchId)
    }

    fun getAllTeams(): Flow<List<TeamEntity>> = teamDao.getAllTeams()

    suspend fun getTeam(teamId: String): TeamEntity? = withContext(Dispatchers.IO) {
        teamDao.getTeamById(teamId)
    }

    fun getTeamMembers(teamId: String): Flow<List<UserEntity>> = teamDao.getTeamMembers(teamId)

    fun getDeliveries(inningsId: String): Flow<List<DeliveryEntity>> = deliveryDao.getDeliveriesForInnings(inningsId)

    suspend fun createTeam(name: String, shortCode: String, city: String): String = withContext(Dispatchers.IO) {
        val teamId = UUID.randomUUID().toString()
        val team = TeamEntity(teamId = teamId, teamName = name, shortCode = shortCode, city = city)
        teamDao.insertTeam(team)
        teamId
    }

    suspend fun addPlayerToTeam(
        teamId: String,
        name: String,
        phone: String = "0000000000",
        role: PlayingRole = PlayingRole.ALL_ROUNDER,
        battingStyle: BattingStyle = BattingStyle.RIGHT_HAND,
        bowlingStyle: BowlingStyle = BowlingStyle.RIGHT_ARM_MEDIUM
    ): String = withContext(Dispatchers.IO) {
        val userId = UUID.randomUUID().toString()
        val user = UserEntity(
            userId = userId,
            phoneNumber = phone,
            fullName = name,
            playingRole = role,
            battingStyle = battingStyle,
            bowlingStyle = bowlingStyle
        )
        userDao.insertUser(user)
        teamDao.insertTeamMember(TeamMemberEntity(teamId = teamId, userId = userId))
        userId
    }

    suspend fun createMatch(
        title: String,
        teamAId: String,
        teamBId: String,
        totalOvers: Int,
        ballType: BallType,
        tossWinnerTeamId: String,
        tossDecision: TossDecision
    ): MatchCreationResult = withContext(Dispatchers.IO) {
        val matchId = UUID.randomUUID().toString()
        val inningsId = UUID.randomUUID().toString()

        val battingTeamId = if (tossDecision == TossDecision.BAT) {
            tossWinnerTeamId
        } else {
            if (tossWinnerTeamId == teamAId) teamBId else teamAId
        }

        val bowlingTeamId = if (battingTeamId == teamAId) teamBId else teamAId

        val match = MatchEntity(
            matchId = matchId,
            matchTitle = title,
            teamAId = teamAId,
            teamBId = teamBId,
            totalOvers = totalOvers,
            ballType = ballType,
            tossWinnerTeamId = tossWinnerTeamId,
            tossDecision = tossDecision,
            matchStatus = MatchStatus.LIVE
        )
        matchDao.insertMatch(match)

        val innings = InningsEntity(
            inningsId = inningsId,
            matchId = matchId,
            inningsNumber = 1,
            battingTeamId = battingTeamId,
            bowlingTeamId = bowlingTeamId,
            totalRuns = 0,
            totalWickets = 0,
            totalLegalDeliveries = 0,
            targetRuns = null
        )
        inningsDao.insertInnings(innings)

        MatchCreationResult(
            matchId = matchId,
            inningsId = inningsId,
            battingTeamId = battingTeamId,
            bowlingTeamId = bowlingTeamId
        )
    }

    suspend fun recordDelivery(
        delivery: DeliveryEntity,
        newTotalRuns: Int,
        newTotalWickets: Int,
        newTotalBalls: Int
    ) = withContext(Dispatchers.IO) {
        deliveryDao.insertDelivery(delivery)
        val innings = inningsDao.getInningsById(delivery.inningsId)
        if (innings != null) {
            inningsDao.updateInnings(
                innings.copy(
                    totalRuns = newTotalRuns,
                    totalWickets = newTotalWickets,
                    totalLegalDeliveries = newTotalBalls
                )
            )
        }
    }

    suspend fun undoLastDelivery(
        inningsId: String,
        revertedRuns: Int,
        revertedWickets: Int,
        revertedBalls: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val deletedCount = deliveryDao.deleteLastDelivery(inningsId)
        if (deletedCount > 0) {
            val innings = inningsDao.getInningsById(inningsId)
            if (innings != null) {
                inningsDao.updateInnings(
                    innings.copy(
                        totalRuns = revertedRuns,
                        totalWickets = revertedWickets,
                        totalLegalDeliveries = revertedBalls
                    )
                )
            }
            true
        } else {
            false
        }
    }

    suspend fun seedInitialDataIfEmpty(): SeededMatchData = withContext(Dispatchers.IO) {
        val teamAId = "team_warriors"
        val teamBId = "team_tigers"
        val matchId = "match_stage1"
        val inningsId = "innings_stage1_1"

        val p1 = UserEntity("user_rohit", "9876543210", "Rohit Sharma", playingRole = PlayingRole.BATSMAN, battingStyle = BattingStyle.RIGHT_HAND)
        val p2 = UserEntity("user_virat", "9876543211", "Virat Kohli", playingRole = PlayingRole.BATSMAN, battingStyle = BattingStyle.RIGHT_HAND)
        val p3 = UserEntity("user_bumrah", "9876543212", "Jasprit Bumrah", playingRole = PlayingRole.BOWLER, bowlingStyle = BowlingStyle.RIGHT_ARM_FAST)
        val p4 = UserEntity("user_surya", "9876543213", "Suryakumar Yadav", playingRole = PlayingRole.BATSMAN, battingStyle = BattingStyle.RIGHT_HAND)
        val p5 = UserEntity("user_hardik", "9876543214", "Hardik Pandya", playingRole = PlayingRole.ALL_ROUNDER, battingStyle = BattingStyle.RIGHT_HAND)
        val p6 = UserEntity("user_shami", "9876543215", "Mohammed Shami", playingRole = PlayingRole.BOWLER, bowlingStyle = BowlingStyle.RIGHT_ARM_FAST)

        userDao.insertUsers(listOf(p1, p2, p3, p4, p5, p6))

        val teamA = TeamEntity(teamAId, "Warriors", "WAR", "Mumbai", captainId = p1.userId)
        val teamB = TeamEntity(teamBId, "Tigers", "TIG", "Delhi", captainId = p3.userId)

        teamDao.insertTeams(listOf(teamA, teamB))

        // Populate team rosters
        teamDao.insertTeamMembers(listOf(
            TeamMemberEntity(teamAId, p1.userId, 45),
            TeamMemberEntity(teamAId, p2.userId, 18),
            TeamMemberEntity(teamAId, p4.userId, 63),
            TeamMemberEntity(teamBId, p3.userId, 93),
            TeamMemberEntity(teamBId, p5.userId, 33),
            TeamMemberEntity(teamBId, p6.userId, 11)
        ))

        val existingMatch = matchDao.getMatchById(matchId)
        if (existingMatch == null) {
            val match = MatchEntity(
                matchId = matchId,
                matchTitle = "Warriors vs Tigers",
                teamAId = teamAId,
                teamBId = teamBId,
                totalOvers = 20,
                ballType = BallType.TENNIS_LIGHT,
                tossWinnerTeamId = teamAId,
                tossDecision = TossDecision.BAT,
                matchStatus = MatchStatus.LIVE
            )
            matchDao.insertMatch(match)

            val innings = InningsEntity(
                inningsId = inningsId,
                matchId = matchId,
                inningsNumber = 1,
                battingTeamId = teamAId,
                bowlingTeamId = teamBId,
                totalRuns = 0,
                totalWickets = 0,
                totalLegalDeliveries = 0,
                targetRuns = null
            )
            inningsDao.insertInnings(innings)
        }

        SeededMatchData(
            matchId = matchId,
            inningsId = inningsId,
            teamAName = teamA.teamName,
            teamBName = teamB.teamName,
            striker = p1,
            nonStriker = p2,
            bowler = p3,
            incomingBatsman = p4
        )
    }
}

data class SeededMatchData(
    val matchId: String,
    val inningsId: String,
    val teamAName: String,
    val teamBName: String,
    val striker: UserEntity,
    val nonStriker: UserEntity,
    val bowler: UserEntity,
    val incomingBatsman: UserEntity
)

