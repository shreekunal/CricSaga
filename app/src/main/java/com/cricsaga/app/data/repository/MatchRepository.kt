package com.cricsaga.app.data.repository

import androidx.room.withTransaction
import com.cricsaga.app.data.local.CricSagaDatabase
import com.cricsaga.app.data.local.entity.*
import com.cricsaga.app.data.remote.SupabaseClient
import com.cricsaga.app.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class MatchCreationResult(
    val matchId: String,
    val inningsId: String,
    val battingTeamId: String,
    val bowlingTeamId: String
)

class MatchRepository(private val database: CricSagaDatabase) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val cloudSyncScope = CoroutineScope(
        Dispatchers.IO.limitedParallelism(1) + SupervisorJob()
    )

    private val userDao = database.userDao()
    private val teamDao = database.teamDao()
    private val matchDao = database.matchDao()
    private val inningsDao = database.inningsDao()
    private val deliveryDao = database.deliveryDao()
    private val discoverDao = database.discoverDao()
    private val postDao = database.postDao()
    private val followDao = database.followDao()
    private val chatMessageDao = database.chatMessageDao()
    private val playerStatsDao = database.playerStatsDao()
    private val commentDao = database.commentDao()
    private val redeemCodeDao = database.redeemCodeDao()

    fun getAllMatches(): Flow<List<MatchEntity>> = matchDao.getAllMatches()
    fun getLiveMatchCountFlow(): Flow<Int> = matchDao.getLiveMatchCountFlow()
    fun getCompletedMatchCountFlow(): Flow<Int> = matchDao.getCompletedMatchCountFlow()
    fun getUserCountFlow(): Flow<Int> = userDao.getUserCountFlow()
    fun getProUserCountFlow(): Flow<Int> = userDao.getProUserCountFlow()
    fun getTeamCountFlow(): Flow<Int> = teamDao.getTeamCountFlow()
    fun getDeliveryCountFlow(): Flow<Int> = deliveryDao.getDeliveryCountFlow()
    fun getTotalRunsFlow(): Flow<Int> = deliveryDao.getTotalRunsFlow()
    fun getFourCountFlow(): Flow<Int> = deliveryDao.getFourCountFlow()
    fun getSixCountFlow(): Flow<Int> = deliveryDao.getSixCountFlow()
    fun getWicketCountFlow(): Flow<Int> = deliveryDao.getWicketCountFlow()

    suspend fun deleteMatch(matchId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.deleteMatch(matchId)
            matchDao.deleteMatch(matchId)
            Result.success(Unit)
        } catch (e: Exception) {
            matchDao.deleteMatch(matchId)
            Result.failure(e)
        }
    }

    fun getMatchesForUser(userId: String): Flow<List<MatchEntity>> = matchDao.getMatchesForUser(userId)

    fun getTopBatters(cityFilter: String? = null): Flow<List<PlayerRankingItem>> =
        combine(
            playerStatsDao.getAllPlayerStats(),
            userDao.getAllUsers(),
            teamDao.getAllTeams(),
            teamDao.getAllTeamMembers()
        ) { statsList, users, teams, members ->
            val userMap = users.associateBy { it.userId }
            val teamMap = teams.associateBy { it.teamId }
            val memberTeamMap = members.associate { it.userId to (teamMap[it.teamId]?.teamName ?: "Grassroots XI") }

            statsList.filter { stat ->
                val user = userMap[stat.userId]
                val matchesCity = if (cityFilter.isNullOrBlank() || cityFilter.equals("All", ignoreCase = true) || cityFilter.equals("All Regions", ignoreCase = true)) {
                    true
                } else {
                    user?.city.equals(cityFilter, ignoreCase = true)
                }
                matchesCity && (stat.runsScored > 0 || stat.matches > 0)
            }.sortedWith(
                compareByDescending<PlayerStatsEntity> { it.runsScored }
                    .thenByDescending { it.battingAverage }
                    .thenByDescending { it.strikeRate }
            ).mapIndexed { index, stat ->
                val user = userMap[stat.userId]
                val name = user?.fullName ?: "Player"
                val initials = name.split(" ")
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                    .uppercase()
                    .ifEmpty { "CR" }
                val role = user?.playingRole?.name?.replace('_', ' ')?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Batter"
                val teamName = memberTeamMap[stat.userId] ?: ((user?.city?.ifBlank { "Grassroots" } ?: "Grassroots") + " XI")

                PlayerRankingItem(
                    rank = index + 1,
                    userId = stat.userId,
                    playerName = name,
                    initials = initials,
                    role = role,
                    teamName = teamName,
                    city = user?.city?.ifBlank { "Mumbai" } ?: "Mumbai",
                    matches = stat.matches,
                    runs = stat.runsScored,
                    innings = stat.innings,
                    battingAverage = stat.battingAverage,
                    strikeRate = stat.strikeRate,
                    highestScore = stat.highestScore,
                    fifties = stat.fifties,
                    hundreds = stat.hundreds,
                    fours = stat.fours,
                    sixes = stat.sixes,
                    isVerified = true
                )
            }
        }

    fun getTopBowlers(cityFilter: String? = null): Flow<List<PlayerRankingItem>> =
        combine(
            playerStatsDao.getAllPlayerStats(),
            userDao.getAllUsers(),
            teamDao.getAllTeams(),
            teamDao.getAllTeamMembers()
        ) { statsList, users, teams, members ->
            val userMap = users.associateBy { it.userId }
            val teamMap = teams.associateBy { it.teamId }
            val memberTeamMap = members.associate { it.userId to (teamMap[it.teamId]?.teamName ?: "Grassroots XI") }

            statsList.filter { stat ->
                val user = userMap[stat.userId]
                val matchesCity = if (cityFilter.isNullOrBlank() || cityFilter.equals("All", ignoreCase = true) || cityFilter.equals("All Regions", ignoreCase = true)) {
                    true
                } else {
                    user?.city.equals(cityFilter, ignoreCase = true)
                }
                matchesCity && (stat.wicketsTaken > 0 || stat.legalBallsBowled > 0)
            }.sortedWith(
                compareByDescending<PlayerStatsEntity> { it.wicketsTaken }
                    .thenBy { if (it.bowlingEconomy > 0) it.bowlingEconomy else 999.0 }
                    .thenByDescending { it.maidens }
            ).mapIndexed { index, stat ->
                val user = userMap[stat.userId]
                val name = user?.fullName ?: "Player"
                val initials = name.split(" ")
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                    .uppercase()
                    .ifEmpty { "CR" }
                val role = user?.playingRole?.name?.replace('_', ' ')?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Bowler"
                val teamName = memberTeamMap[stat.userId] ?: ((user?.city?.ifBlank { "Grassroots" } ?: "Grassroots") + " XI")
                val overs = (stat.legalBallsBowled / 6).toDouble() + ((stat.legalBallsBowled % 6).toDouble() * 0.1)

                PlayerRankingItem(
                    rank = index + 1,
                    userId = stat.userId,
                    playerName = name,
                    initials = initials,
                    role = role,
                    teamName = teamName,
                    city = user?.city?.ifBlank { "Mumbai" } ?: "Mumbai",
                    matches = stat.matches,
                    wickets = stat.wicketsTaken,
                    oversBowled = overs,
                    bowlingEconomy = stat.bowlingEconomy,
                    maidens = stat.maidens,
                    runsConceded = stat.runsConceded,
                    isVerified = true
                )
            }
        }

    fun getTopAllRounders(cityFilter: String? = null): Flow<List<PlayerRankingItem>> =
        combine(
            playerStatsDao.getAllPlayerStats(),
            userDao.getAllUsers(),
            teamDao.getAllTeams(),
            teamDao.getAllTeamMembers()
        ) { statsList, users, teams, members ->
            val userMap = users.associateBy { it.userId }
            val teamMap = teams.associateBy { it.teamId }
            val memberTeamMap = members.associate { it.userId to (teamMap[it.teamId]?.teamName ?: "Grassroots XI") }

            statsList.filter { stat ->
                val user = userMap[stat.userId]
                val matchesCity = if (cityFilter.isNullOrBlank() || cityFilter.equals("All", ignoreCase = true) || cityFilter.equals("All Regions", ignoreCase = true)) {
                    true
                } else {
                    user?.city.equals(cityFilter, ignoreCase = true)
                }
                matchesCity && (stat.runsScored > 0 || stat.wicketsTaken > 0)
            }.map { stat ->
                val mvp = (stat.runsScored * 1.0) + (stat.sixes * 2.0) + (stat.fours * 1.0) + (stat.wicketsTaken * 25.0) + (stat.maidens * 10.0) + (stat.catches * 10.0)
                stat to mvp
            }.sortedByDescending { it.second }
            .mapIndexed { index, (stat, mvp) ->
                val user = userMap[stat.userId]
                val name = user?.fullName ?: "Player"
                val initials = name.split(" ")
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                    .uppercase()
                    .ifEmpty { "CR" }
                val role = user?.playingRole?.name?.replace('_', ' ')?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "All-Rounder"
                val teamName = memberTeamMap[stat.userId] ?: ((user?.city?.ifBlank { "Grassroots" } ?: "Grassroots") + " XI")
                val overs = (stat.legalBallsBowled / 6).toDouble() + ((stat.legalBallsBowled % 6).toDouble() * 0.1)

                PlayerRankingItem(
                    rank = index + 1,
                    userId = stat.userId,
                    playerName = name,
                    initials = initials,
                    role = role,
                    teamName = teamName,
                    city = user?.city?.ifBlank { "Mumbai" } ?: "Mumbai",
                    matches = stat.matches,
                    runs = stat.runsScored,
                    wickets = stat.wicketsTaken,
                    oversBowled = overs,
                    bowlingEconomy = stat.bowlingEconomy,
                    strikeRate = stat.strikeRate,
                    mvpPoints = mvp,
                    isVerified = true
                )
            }
        }

    fun getTeamRankings(cityFilter: String? = null): Flow<List<TeamRankingItem>> =
        combine(
            teamDao.getAllTeams(),
            matchDao.getAllMatches()
        ) { teams, matches ->
            val completedMatches = matches.filter { it.matchStatus == MatchStatus.COMPLETED }

            teams.filter { team ->
                if (cityFilter.isNullOrBlank() || cityFilter.equals("All", ignoreCase = true) || cityFilter.equals("All Regions", ignoreCase = true)) {
                    true
                } else {
                    team.city.equals(cityFilter, ignoreCase = true)
                }
            }.map { team ->
                val teamMatches = completedMatches.filter { it.teamAId == team.teamId || it.teamBId == team.teamId }
                val played = teamMatches.size
                val wins = teamMatches.count { it.winnerTeamId == team.teamId }
                val losses = teamMatches.count { it.winnerTeamId != null && it.winnerTeamId != team.teamId }
                val ties = teamMatches.count { it.winnerTeamId == null && it.resultSummary?.contains("tied", ignoreCase = true) == true }
                val winPct = if (played > 0) (wins.toDouble() / played.toDouble()) * 100.0 else 0.0
                val points = (wins * 2) + (ties * 1)
                val form = teamMatches.take(5).map { m ->
                    if (m.winnerTeamId == team.teamId) MatchOutcome.WIN
                    else if (m.winnerTeamId != null) MatchOutcome.LOSS
                    else MatchOutcome.TIE
                }

                TeamRankingItem(
                    rank = 0,
                    teamId = team.teamId,
                    teamName = team.teamName,
                    shortCode = team.shortCode,
                    city = team.city.ifBlank { "India" },
                    matchesPlayed = played,
                    wins = wins,
                    losses = losses,
                    ties = ties,
                    winPercentage = winPct,
                    points = points,
                    recentForm = form
                )
            }.sortedWith(
                compareByDescending<TeamRankingItem> { it.points }
                    .thenByDescending { it.winPercentage }
                    .thenByDescending { it.wins }
            ).mapIndexed { index, item ->
                item.copy(rank = index + 1)
            }
        }

    fun getAvailableCities(): Flow<List<String>> =
        combine(
            userDao.getAllUsers(),
            teamDao.getAllTeams()
        ) { users, teams ->
            val fromUsers = users.map { it.city.trim() }
            val fromTeams = teams.map { it.city.trim() }
            val allCities = (fromUsers + fromTeams)
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
            listOf("All Regions") + allCities
        }

    suspend fun getMatch(matchId: String): MatchEntity? = withContext(Dispatchers.IO) {
        matchDao.getMatchById(matchId)
    }

    fun getAllTeams(): Flow<List<TeamEntity>> = teamDao.getAllTeams()
    fun getTeamsForUser(userId: String): Flow<List<TeamEntity>> = teamDao.getTeamsForUser(userId)

    suspend fun getTeam(teamId: String): TeamEntity? = withContext(Dispatchers.IO) {
        teamDao.getTeamById(teamId)
    }

    suspend fun deleteTeam(teamId: String) = withContext(Dispatchers.IO) {
        teamDao.deleteTeam(teamId)
        try {
            SupabaseClient.deleteTeam(teamId)
        } catch (_: Exception) {}
    }

    suspend fun getTeamStats(teamId: String): TeamStatsSummary = withContext(Dispatchers.IO) {
        val matches = matchDao.getAllMatchesList()
        val completed = matches.filter { (it.teamAId == teamId || it.teamBId == teamId) && it.matchStatus == MatchStatus.COMPLETED }
        val played = completed.size
        val won = completed.count { it.winnerTeamId == teamId }
        val lost = completed.count { it.winnerTeamId != null && it.winnerTeamId != teamId }
        val winPct = if (played > 0) String.format(java.util.Locale.US, "%.1f%%", (won.toDouble() / played) * 100.0) else "0.0%"
        TeamStatsSummary(played = played, won = won, lost = lost, winPct = winPct)
    }

    fun getTeamMembers(teamId: String): Flow<List<UserEntity>> = teamDao.getTeamMembers(teamId)

    suspend fun getTeamMembersList(teamId: String): List<UserEntity> = withContext(Dispatchers.IO) {
        teamDao.getTeamMembersList(teamId)
    }

    fun getDeliveries(inningsId: String): Flow<List<DeliveryEntity>> = deliveryDao.getDeliveriesForInnings(inningsId)

    suspend fun getDeliveriesList(inningsId: String): List<DeliveryEntity> = withContext(Dispatchers.IO) {
        deliveryDao.getDeliveriesListForInnings(inningsId)
    }

    fun getAllLookingForPosts(): Flow<List<LookingForEntity>> = discoverDao.getAllPosts()

    fun getAllPosts(): Flow<List<PostEntity>> = postDao.getAllPosts()

    fun getFollowingFeed(currentUserId: String): Flow<List<PostEntity>> = postDao.getFollowingFeed(currentUserId)

    fun getPostsByAuthor(authorId: String): Flow<List<PostEntity>> = postDao.getPostsByAuthor(authorId)

    suspend fun createPost(
        authorId: String,
        authorName: String,
        authorRole: String,
        authorCity: String,
        authorTeamName: String? = null,
        authorAvatarUrl: String? = null,
        content: String,
        imageUri: String? = null,
        linkedMatchId: String? = null,
        performanceTag: String? = null
    ): String = withContext(Dispatchers.IO) {
        val postId = "post_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val post = PostEntity(
            postId = postId,
            authorId = authorId,
            authorName = authorName,
            authorRole = authorRole,
            authorCity = authorCity,
            authorTeamName = authorTeamName,
            authorAvatarUrl = authorAvatarUrl,
            content = content,
            imageUri = imageUri,
            linkedMatchId = linkedMatchId,
            performanceTag = performanceTag,
            createdAt = System.currentTimeMillis()
        )
        postDao.insertPost(post)
        try {
            SupabaseClient.createPost(post)
        } catch (_: Exception) {}
        postId
    }

    suspend fun toggleLikePost(postId: String) = withContext(Dispatchers.IO) {
        val post = postDao.getPostById(postId) ?: return@withContext
        val newLiked = !post.isLikedByMe
        val newCount = if (newLiked) post.likeCount + 1 else (post.likeCount - 1).coerceAtLeast(0)
        postDao.updatePost(post.copy(isLikedByMe = newLiked, likeCount = newCount))
    }

    suspend fun deletePost(postId: String) = withContext(Dispatchers.IO) {
        postDao.deletePost(postId)
    }

    suspend fun incrementRepost(postId: String) = withContext(Dispatchers.IO) {
        postDao.incrementRepostCount(postId)
    }

    fun getCommentsFlow(postId: String): Flow<List<CommentEntity>> =
        commentDao.getCommentsForPost(postId)

    suspend fun addComment(
        postId: String,
        authorId: String,
        authorName: String,
        text: String,
        authorAvatarUrl: String? = null
    ): CommentEntity = withContext(Dispatchers.IO) {
        val comment = CommentEntity(
            commentId = "cmt_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
            postId = postId,
            authorId = authorId,
            authorName = authorName,
            authorAvatarUrl = authorAvatarUrl,
            text = text,
            createdAt = System.currentTimeMillis()
        )
        commentDao.insertComment(comment)
        postDao.incrementCommentCount(postId)
        comment
    }

    suspend fun followUser(followerId: String, followingId: String) = withContext(Dispatchers.IO) {
        followDao.followUser(FollowEntity(followerId, followingId))
    }

    suspend fun unfollowUser(followerId: String, followingId: String) = withContext(Dispatchers.IO) {
        followDao.unfollowUser(followerId, followingId)
    }

    fun isFollowing(followerId: String, followingId: String): Flow<Boolean> =
        followDao.isFollowing(followerId, followingId)

    fun getFollowersCount(userId: String): Flow<Int> = followDao.getFollowersCount(userId)

    fun getFollowingCount(userId: String): Flow<Int> = followDao.getFollowingCount(userId)

    fun getFollowingIds(userId: String): Flow<List<String>> = followDao.getFollowingIds(userId)

    fun getMessagesBetween(userA: String, userB: String): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getMessagesBetween(userA, userB)

    fun getAllMessagesForUser(userId: String): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getAllMessagesForUser(userId)

    suspend fun sendChatMessage(
        senderId: String,
        receiverId: String,
        text: String,
        isChallenge: Boolean = false,
        challengeDetails: String? = null
    ): String = withContext(Dispatchers.IO) {
        val messageId = "msg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val message = ChatMessageEntity(
            messageId = messageId,
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            isChallenge = isChallenge,
            challengeDetails = challengeDetails,
            timestamp = System.currentTimeMillis()
        )
        chatMessageDao.insertMessage(message)
        cloudSyncScope.launch {
            try {
                SupabaseClient.sendChatMessage(message)
            } catch (_: Exception) {}
        }
        messageId
    }

    suspend fun markConversationAsRead(senderId: String, receiverId: String) = withContext(Dispatchers.IO) {
        chatMessageDao.markAsRead(senderId, receiverId)
    }

    fun getUnreadChatCount(userId: String): Flow<Int> = chatMessageDao.getUnreadCount(userId)

    suspend fun createLookingForPost(
        type: String,
        title: String,
        teamOrOrganizer: String,
        timing: String,
        location: String,
        note: String,
        contactPhone: String
    ) = withContext(Dispatchers.IO) {
        val post = LookingForEntity(
            id = "lf_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
            type = type,
            title = title,
            teamOrOrganizer = teamOrOrganizer,
            timing = timing,
            location = location,
            note = note,
            contactPhone = contactPhone
        )
        discoverDao.insertPost(post)
    }

    suspend fun createTeam(name: String, shortCode: String, city: String, captainId: String? = null): String = withContext(Dispatchers.IO) {
        val teamId = UUID.randomUUID().toString()
        val team = TeamEntity(teamId = teamId, teamName = name, shortCode = shortCode, city = city, captainId = captainId)
        teamDao.insertTeam(team)
        try {
            SupabaseClient.createTeam(team)
        } catch (_: Exception) {}

        if (!captainId.isNullOrBlank()) {
            teamDao.insertTeamMember(TeamMemberEntity(teamId = teamId, userId = captainId))
            try {
                SupabaseClient.addTeamMember(teamId, captainId)
            } catch (_: Exception) {}
        }

        teamId
    }

    suspend fun addExistingUserToTeam(teamId: String, userId: String) = withContext(Dispatchers.IO) {
        teamDao.insertTeamMember(TeamMemberEntity(teamId = teamId, userId = userId))
        try {
            SupabaseClient.addTeamMember(teamId, userId)
        } catch (_: Exception) {}

        val team = teamDao.getTeamById(teamId)
        if (team != null && team.captainId.isNullOrBlank()) {
            teamDao.updateTeamCaptain(teamId, userId)
            try {
                SupabaseClient.updateTeamCaptain(teamId, userId)
            } catch (_: Exception) {}
        }
    }

    suspend fun createAndAddPlayerToTeam(
        teamId: String,
        fullName: String,
        email: String,
        role: PlayingRole = PlayingRole.ALL_ROUNDER,
        jerseyNumber: String = "18",
        battingStyle: BattingStyle = BattingStyle.RIGHT_HAND,
        bowlingStyle: BowlingStyle = BowlingStyle.RIGHT_ARM_MEDIUM
    ): String = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        var existingUser: UserEntity? = if (cleanEmail.isNotEmpty()) {
            userDao.getUserByEmail(cleanEmail) ?: SupabaseClient.getUserByEmail(cleanEmail).getOrNull()
        } else null

        val finalUserId = if (existingUser != null) {
            userDao.insertUser(existingUser)
            existingUser.userId
        } else {
            val newUserId = "user_${UUID.randomUUID().toString().replace("-", "").take(12)}"
            val newUser = UserEntity(
                userId = newUserId,
                phoneNumber = "", // Phone number collected upon their first Google login
                fullName = fullName.trim(),
                email = cleanEmail.ifEmpty { null },
                playingRole = role,
                battingStyle = battingStyle,
                bowlingStyle = bowlingStyle,
                jerseyNumber = jerseyNumber.ifBlank { "18" }
            )
            userDao.insertUser(newUser)
            try {
                SupabaseClient.signUpUser(newUser)
            } catch (_: Exception) {}
            newUserId
        }

        teamDao.insertTeamMember(TeamMemberEntity(teamId = teamId, userId = finalUserId))
        try {
            SupabaseClient.addTeamMember(teamId, finalUserId)
        } catch (_: Exception) {}

        val team = teamDao.getTeamById(teamId)
        if (team != null && team.captainId.isNullOrBlank()) {
            teamDao.updateTeamCaptain(teamId, finalUserId)
            try {
                SupabaseClient.updateTeamCaptain(teamId, finalUserId)
            } catch (_: Exception) {}
        }

        finalUserId
    }

    suspend fun addPlayerToTeam(
        teamId: String,
        name: String,
        phone: String = "0000000000",
        role: PlayingRole = PlayingRole.ALL_ROUNDER,
        battingStyle: BattingStyle = BattingStyle.RIGHT_HAND,
        bowlingStyle: BowlingStyle = BowlingStyle.RIGHT_ARM_MEDIUM,
        jerseyNumber: String = "00",
        password: String = "123456",
        city: String = "Mumbai"
    ): String = withContext(Dispatchers.IO) {
        val cleanPhone = phone.filter { it.isDigit() }.takeLast(10).ifEmpty {
            val randomSuffix = (10000000..99999999).random().toString()
            "98$randomSuffix"
        }
        val existing = userDao.getUserByPhone(cleanPhone)
        val userId = if (existing != null) {
            existing.userId
        } else {
            val newUserId = "user_$cleanPhone"
            val user = UserEntity(
                userId = newUserId,
                phoneNumber = cleanPhone,
                fullName = name.trim().ifBlank { "Player $cleanPhone" },
                password = com.cricsaga.app.ui.utils.SecurityUtils.hashPassword(password.ifBlank { "123456" }),
                playingRole = role,
                battingStyle = battingStyle,
                bowlingStyle = bowlingStyle,
                jerseyNumber = jerseyNumber.ifBlank { (1..99).random().toString() },
                city = city.ifBlank { "Mumbai" }
            )
            userDao.insertUser(user)
            try {
                SupabaseClient.signUpUser(user)
            } catch (_: Exception) {}
            newUserId
        }
        teamDao.insertTeamMember(TeamMemberEntity(teamId = teamId, userId = userId))
        try {
            SupabaseClient.addTeamMember(teamId, userId)
        } catch (_: Exception) {}
        userId
    }

    suspend fun removePlayerFromTeam(teamId: String, userId: String) = withContext(Dispatchers.IO) {
        teamDao.removeTeamMember(teamId, userId)
        try {
            SupabaseClient.removeTeamMember(teamId, userId)
        } catch (_: Exception) {}
    }

    suspend fun setTeamCaptain(teamId: String, captainId: String) = withContext(Dispatchers.IO) {
        teamDao.updateTeamCaptain(teamId, captainId)
        try {
            SupabaseClient.updateTeamCaptain(teamId, captainId)
        } catch (_: Exception) {}
    }

    suspend fun createMatch(
        title: String,
        teamAId: String,
        teamBId: String,
        totalOvers: Int,
        ballType: BallType,
        tossWinnerTeamId: String,
        tossDecision: TossDecision,
        createdByUserId: String? = null
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
        try {
            SupabaseClient.createMatch(match, innings, createdByUserId)
        } catch (_: Exception) {}

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
        database.withTransaction {
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
        cloudSyncScope.launch {
            try {
                SupabaseClient.recordDelivery(delivery, newTotalRuns, newTotalWickets, newTotalBalls)
            } catch (_: Exception) {}
        }
    }

    suspend fun undoLastDelivery(
        inningsId: String,
        revertedRuns: Int,
        revertedWickets: Int,
        revertedBalls: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val (deletedCount, lastDeliv) = database.withTransaction {
            val lastDeliv = deliveryDao.getLastDelivery(inningsId)
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
            }
            Pair(deletedCount, lastDeliv)
        }
        if (deletedCount > 0) {
            cloudSyncScope.launch {
                try {
                    SupabaseClient.undoDelivery(
                        deliveryId = lastDeliv?.deliveryId,
                        inningsId = inningsId,
                        totalRuns = revertedRuns,
                        totalWickets = revertedWickets,
                        legalDeliveries = revertedBalls
                    )
                } catch (_: Exception) {}
            }
            true
        } else {
            false
        }
    }

    suspend fun startSecondInnings(
        matchId: String,
        battingTeamId: String,
        bowlingTeamId: String,
        targetRuns: Int
    ): String = withContext(Dispatchers.IO) {
        val inningsId = UUID.randomUUID().toString()
        val innings = InningsEntity(
            inningsId = inningsId,
            matchId = matchId,
            inningsNumber = 2,
            battingTeamId = battingTeamId,
            bowlingTeamId = bowlingTeamId,
            totalRuns = 0,
            totalWickets = 0,
            totalLegalDeliveries = 0,
            targetRuns = targetRuns,
            isCompleted = false
        )
        inningsDao.insertInnings(innings)
        cloudSyncScope.launch {
            try {
                SupabaseClient.createInnings(innings)
            } catch (_: Exception) {}
        }
        inningsId
    }

    suspend fun completeInnings(inningsId: String) = withContext(Dispatchers.IO) {
        inningsDao.completeInnings(inningsId)
    }

    suspend fun completeMatch(
        matchId: String,
        winnerTeamId: String?,
        resultSummary: String?,
        playerOfTheMatchId: String?
    ) = withContext(Dispatchers.IO) {
        matchDao.completeMatch(
            matchId = matchId,
            winnerTeamId = winnerTeamId,
            resultSummary = resultSummary,
            playerOfTheMatchId = playerOfTheMatchId
        )
        cloudSyncScope.launch {
            try {
                SupabaseClient.updateMatchCompletion(
                    matchId = matchId,
                    matchStatus = MatchStatus.COMPLETED.name,
                    winnerTeamId = winnerTeamId,
                    resultSummary = resultSummary,
                    playerOfTheMatchId = playerOfTheMatchId
                )
            } catch (_: Exception) {}
        }
    }

    fun getInningsForMatch(matchId: String): Flow<List<InningsEntity>> = inningsDao.getInningsForMatch(matchId)

    suspend fun getInningsListForMatch(matchId: String): List<InningsEntity> = withContext(Dispatchers.IO) {
        inningsDao.getInningsListForMatch(matchId)
    }

    fun getPlayerStatsFlow(userId: String): Flow<PlayerStatsEntity?> = database.playerStatsDao().getStatsFlowForUser(userId)

    fun getAllPlayerStats(): Flow<List<PlayerStatsEntity>> = database.playerStatsDao().getAllPlayerStats()

    suspend fun insertUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
        try {
            SupabaseClient.signUpUser(user)
        } catch (_: Exception) {}
    }

    suspend fun insertUsers(users: List<UserEntity>) = withContext(Dispatchers.IO) {
        userDao.insertUsers(users)
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
        try {
            SupabaseClient.updateUser(user)
        } catch (_: Exception) {}
    }

    suspend fun updateUserProStatus(userId: String, isPro: Boolean) = withContext(Dispatchers.IO) {
        userDao.updateUserProStatus(userId, isPro)
        try {
            SupabaseClient.updateUserProStatus(userId, isPro)
        } catch (_: Exception) {}
    }

    suspend fun updateUserPassword(userId: String, newPassword: String) = withContext(Dispatchers.IO) {
        val hashedPassword = com.cricsaga.app.ui.utils.SecurityUtils.hashPassword(newPassword)
        userDao.updateUserPassword(userId, hashedPassword)
        try {
            SupabaseClient.updateUserPassword(userId, newPassword)
        } catch (_: Exception) {}
    }

    suspend fun deleteUserById(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteResult = SupabaseClient.deleteUser(userId)
            userDao.deleteUserById(userId)
            remoteResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearLocalCache() = withContext(Dispatchers.IO) {
        try {
            database.clearAllTables()
        } catch (_: Exception) {}
    }

    suspend fun syncRemoteToLocal(currentUserId: String? = null) = withContext(Dispatchers.IO) {
        try {
            // 1. Sync Users from cloud (Additive Upsert)
            val usersResult = SupabaseClient.getAllUsers()
            if (usersResult.isSuccess) {
                val remoteUsers = usersResult.getOrNull() ?: emptyList()
                if (remoteUsers.isNotEmpty()) {
                    userDao.insertUsers(remoteUsers)
                }
                // Upload any unsynced local users to cloud
                val remoteUserIds = remoteUsers.map { it.userId }.toSet()
                val localUsers = userDao.getAllUsersList()
                for (localUser in localUsers) {
                    if (!remoteUserIds.contains(localUser.userId) && localUser.userId != "user_guest_scorer") {
                        try {
                            SupabaseClient.signUpUser(localUser)
                        } catch (_: Exception) {}
                    }
                }
            }

            // 2. Sync Teams from cloud (Additive Upsert - Preserve local offline teams)
            val teamsResult = SupabaseClient.getAllTeams()
            if (teamsResult.isSuccess) {
                val remoteTeams = teamsResult.getOrNull() ?: emptyList()
                if (remoteTeams.isNotEmpty()) {
                    teamDao.insertTeams(remoteTeams)
                }
            }

            // 3. Sync Team Members from cloud (Additive Upsert - Preserve local offline squads)
            val membersResult = SupabaseClient.getAllTeamMembers()
            if (membersResult.isSuccess) {
                val remoteMembers = membersResult.getOrNull() ?: emptyList()
                if (remoteMembers.isNotEmpty()) {
                    val memberEntities = remoteMembers.map { (teamId, userId) ->
                        TeamMemberEntity(teamId = teamId, userId = userId)
                    }
                    teamDao.insertTeamMembers(memberEntities)
                }
            }

            // 4. Sync Matches from cloud (Additive Upsert - Preserve local offline matches)
            val matchesResult = SupabaseClient.getAllMatches()
            if (matchesResult.isSuccess) {
                val remoteMatches = matchesResult.getOrNull() ?: emptyList()
                if (remoteMatches.isNotEmpty()) {
                    matchDao.insertMatches(remoteMatches)
                }
            }

            // 5. Sync Innings from cloud (Additive Upsert - Preserve local offline innings)
            val inningsResult = SupabaseClient.getAllInnings()
            if (inningsResult.isSuccess) {
                val remoteInnings = inningsResult.getOrNull() ?: emptyList()
                if (remoteInnings.isNotEmpty()) {
                    inningsDao.insertInningsList(remoteInnings)
                }
            }

            // 6. Sync Chat Messages for user if signed in
            if (!currentUserId.isNullOrBlank()) {
                val chatResult = SupabaseClient.fetchChatMessagesForUser(currentUserId)
                if (chatResult.isSuccess) {
                    val remoteMessages = chatResult.getOrNull() ?: emptyList()
                    remoteMessages.forEach { remoteMsg ->
                        chatMessageDao.insertMessage(remoteMsg)
                    }
                }
            }

            // 7. Sync Promo Codes from cloud
            val codesResult = SupabaseClient.getAllRemoteRedeemCodes()
            if (codesResult.isSuccess) {
                val remoteCodes = codesResult.getOrNull() ?: emptyList()
                if (remoteCodes.isNotEmpty()) {
                    redeemCodeDao.insertCodes(remoteCodes)
                }
            }
        } catch (_: Exception) {
            // Offline-resilient: retain local SQLite cache if network is unreachable
        }
    }

    suspend fun syncScorecardForMatch(matchId: String) = withContext(Dispatchers.IO) {
        try {
            val inningsResult = SupabaseClient.getInningsByMatchId(matchId)
            if (inningsResult.isSuccess) {
                val remoteInnings = inningsResult.getOrNull() ?: emptyList()
                if (remoteInnings.isNotEmpty()) {
                    inningsDao.insertInningsList(remoteInnings)
                    for (inn in remoteInnings) {
                        val delivResult = SupabaseClient.getDeliveriesByInningsId(inn.inningsId)
                        if (delivResult.isSuccess) {
                            val remoteDeliveries = delivResult.getOrNull() ?: emptyList()
                            if (remoteDeliveries.isNotEmpty()) {
                                deliveryDao.insertDeliveries(remoteDeliveries)
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Offline-resilient: retain local SQLite cache if network is unreachable
        }
    }

    suspend fun getUserCount(): Int = withContext(Dispatchers.IO) {
        userDao.getUserCount()
    }

    suspend fun getUserById(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserById(userId)
    }

    fun getUserFlowById(userId: String): Flow<UserEntity?> = userDao.getUserFlowById(userId)

    suspend fun getUserByPhone(phoneNumber: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByPhone(phoneNumber)
    }

    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    suspend fun getUserByGoogleId(googleId: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByGoogleId(googleId)
    }

    suspend fun authenticateUser(phoneNumber: String, password: String): UserEntity? = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.filter { it.isDigit() }.takeLast(10)
        val user = userDao.getUserByPhone(cleanPhone) ?: userDao.getUserByPhone(phoneNumber)
        if (user != null) {
            if (user.password.isNotBlank() && com.cricsaga.app.ui.utils.SecurityUtils.verifyPassword(password, user.password)) {
                return@withContext user
            }
        }
        null
    }

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun recordMatchPlayerStats(
        matchId: String? = null,
        innings1: InningsScoreState?,
        innings2: InningsScoreState,
        squadPlayerIds: Collection<String> = emptySet()
    ) = withContext(Dispatchers.IO) {
        if (matchId != null) {
            val match = matchDao.getMatchById(matchId)
            if (match?.isStatsRecorded == true) {
                // Idempotent guard: career stats have already been calculated and committed for this match
                return@withContext
            }
        }

        val playerStatsDao = database.playerStatsDao()
        val allPlayerIds = mutableSetOf<String>()
        allPlayerIds.addAll(squadPlayerIds)
        listOfNotNull(innings1, innings2).forEach { inn ->
            allPlayerIds.addAll(inn.batsmen.keys)
            allPlayerIds.addAll(inn.bowlers.keys)
            inn.batsmen.values.forEach { b ->
                b.dismissal?.primaryFielderId?.let { if (it.isNotBlank()) allPlayerIds.add(it) }
                b.dismissal?.secondaryFielderId?.let { if (it.isNotBlank()) allPlayerIds.add(it) }
            }
        }

        allPlayerIds.forEach { playerId ->
            val existing = playerStatsDao.getStatsForUser(playerId) ?: PlayerStatsEntity(userId = playerId)

            var addedRuns = 0
            var addedBalls = 0
            var addedFours = 0
            var addedSixes = 0
            var battedInMatch = false
            var wasDismissed = false

            var addedBallsBowled = 0
            var addedRunsConceded = 0
            var addedWickets = 0
            var addedMaidens = 0
            var addedThreeWickets = 0
            var addedFiveWickets = 0

            var addedCatches = 0
            var addedRunOuts = 0
            var addedStumpings = 0

            listOfNotNull(innings1, innings2).forEach { inn ->
                inn.batsmen[playerId]?.let { b ->
                    if (b.ballsFaced > 0 || b.isOut) {
                        battedInMatch = true
                        addedRuns += b.runs
                        addedBalls += b.ballsFaced
                        addedFours += b.fours
                        addedSixes += b.sixes
                        if (b.isOut) {
                            wasDismissed = true
                        }
                    }
                }
                inn.bowlers[playerId]?.let { bow ->
                    if (bow.legalBalls > 0) {
                        addedBallsBowled += bow.legalBalls
                        addedRunsConceded += bow.runsConceded
                        addedWickets += bow.wickets
                        addedMaidens += bow.maidens
                        if (bow.wickets >= 5) {
                            addedFiveWickets += 1
                        } else if (bow.wickets >= 3) {
                            addedThreeWickets += 1
                        }
                    }
                }

                // Check fielding contributions across all dismissals in this innings
                inn.batsmen.values.forEach { b ->
                    val d = b.dismissal
                    if (d != null) {
                        when (d.dismissalType) {
                            DismissalType.CAUGHT, DismissalType.CAUGHT_BEHIND -> {
                                if (d.primaryFielderId == playerId || d.secondaryFielderId == playerId) {
                                    addedCatches += 1
                                }
                            }
                            DismissalType.CAUGHT_AND_BOWLED -> {
                                if (d.primaryFielderId == playerId) {
                                    addedCatches += 1
                                }
                            }
                            DismissalType.STUMPED -> {
                                if (d.primaryFielderId == playerId) {
                                    addedStumpings += 1
                                }
                            }
                            DismissalType.RUN_OUT -> {
                                if (d.primaryFielderId == playerId || d.secondaryFielderId == playerId) {
                                    addedRunOuts += 1
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            val highest = maxOf(existing.highestScore, addedRuns)
            val fifties = existing.fifties + (if (addedRuns in 50..99) 1 else 0)
            val hundreds = existing.hundreds + (if (addedRuns >= 100) 1 else 0)
            val addedNotOut = if (battedInMatch && !wasDismissed) 1 else 0

            val updated = existing.copy(
                matches = existing.matches + 1,
                innings = existing.innings + (if (battedInMatch) 1 else 0),
                notOuts = existing.notOuts + addedNotOut,
                runsScored = existing.runsScored + addedRuns,
                ballsFaced = existing.ballsFaced + addedBalls,
                highestScore = highest,
                fifties = fifties,
                hundreds = hundreds,
                fours = existing.fours + addedFours,
                sixes = existing.sixes + addedSixes,
                legalBallsBowled = existing.legalBallsBowled + addedBallsBowled,
                runsConceded = existing.runsConceded + addedRunsConceded,
                wicketsTaken = existing.wicketsTaken + addedWickets,
                maidens = existing.maidens + addedMaidens,
                catches = existing.catches + addedCatches,
                runOuts = existing.runOuts + addedRunOuts,
                stumpings = existing.stumpings + addedStumpings,
                threeWicketHauls = existing.threeWicketHauls + addedThreeWickets,
                fiveWicketHauls = existing.fiveWicketHauls + addedFiveWickets
            )
            playerStatsDao.insertOrUpdate(updated)
        }

        if (matchId != null) {
            matchDao.markStatsRecorded(matchId)
        }
    }
}

data class TeamStatsSummary(
    val played: Int,
    val won: Int,
    val lost: Int,
    val winPct: String
)

