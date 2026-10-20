package com.cricsaga.app.data.repository

import com.cricsaga.app.data.local.CricSagaDatabase
import com.cricsaga.app.data.local.entity.*
import com.cricsaga.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MatchRepository(private val database: CricSagaDatabase) {

    private val userDao = database.userDao()
    private val teamDao = database.teamDao()
    private val matchDao = database.matchDao()
    private val inningsDao = database.inningsDao()
    private val deliveryDao = database.deliveryDao()

    fun getDeliveries(inningsId: String): Flow<List<DeliveryEntity>> {
        return deliveryDao.getDeliveriesForInnings(inningsId)
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

    /**
     * Seeds initial sample teams and players for Stage 1 local development if database is empty.
     */
    suspend fun seedInitialDataIfEmpty(): SeededMatchData = withContext(Dispatchers.IO) {
        val teamAId = "team_warriors"
        val teamBId = "team_tigers"
        val matchId = "match_stage1"
        val inningsId = "innings_stage1_1"

        val p1 = UserEntity("user_rohit", "9876543210", "Rohit Sharma", playingRole = PlayingRole.BATSMAN, battingStyle = BattingStyle.RIGHT_HAND)
        val p2 = UserEntity("user_virat", "9876543211", "Virat Kohli", playingRole = PlayingRole.BATSMAN, battingStyle = BattingStyle.RIGHT_HAND)
        val p3 = UserEntity("user_bumrah", "9876543212", "Jasprit Bumrah", playingRole = PlayingRole.BOWLER, bowlingStyle = BowlingStyle.RIGHT_ARM_FAST)
        val p4 = UserEntity("user_surya", "9876543213", "Suryakumar Yadav", playingRole = PlayingRole.BATSMAN, battingStyle = BattingStyle.RIGHT_HAND)

        userDao.insertUsers(listOf(p1, p2, p3, p4))

        val teamA = TeamEntity(teamAId, "Warriors", "WAR", "Mumbai", captainId = p1.userId)
        val teamB = TeamEntity(teamBId, "Tigers", "TIG", "Delhi", captainId = p3.userId)

        teamDao.insertTeams(listOf(teamA, teamB))

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
