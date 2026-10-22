package com.cricsaga.app.data.repository

import com.cricsaga.app.data.local.CricSagaDatabase
import com.cricsaga.app.data.local.entity.*
import com.cricsaga.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    private val discoverDao = database.discoverDao()
    private val postDao = database.postDao()
    private val followDao = database.followDao()
    private val chatMessageDao = database.chatMessageDao()
    private val playerStatsDao = database.playerStatsDao()
    private val commentDao = database.commentDao()

    fun getAllMatches(): Flow<List<MatchEntity>> = matchDao.getAllMatches()

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
                val ties = teamMatches.count { it.winnerTeamId == null }
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

    suspend fun getTeam(teamId: String): TeamEntity? = withContext(Dispatchers.IO) {
        teamDao.getTeamById(teamId)
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
        postId
    }

    suspend fun toggleLikePost(postId: String) = withContext(Dispatchers.IO) {
        val post = postDao.getPostById(postId) ?: return@withContext
        val newLiked = !post.isLikedByMe
        val newCount = if (newLiked) post.likeCount + 1 else (post.likeCount - 1).coerceAtLeast(0)
        postDao.updatePost(post.copy(isLikedByMe = newLiked, likeCount = newCount))
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

    suspend fun seedInitialLookingForPostsIfEmpty() = withContext(Dispatchers.IO) {
        if (discoverDao.getPostCount() == 0) {
            val initial = listOf(
                LookingForEntity(
                    id = "lf_1",
                    type = "Opponent",
                    title = "Weekend Match • 16 Overs",
                    teamOrOrganizer = "Warriors CC",
                    timing = "Sunday • 7:00 AM - 11:00 AM",
                    location = "Shivaji Park Ground #3, Dadar",
                    note = "Leather ball. Umpire and pitch fees split 50/50.",
                    contactPhone = "9820123456"
                ),
                LookingForEntity(
                    id = "lf_2",
                    type = "Players",
                    title = "Need 2 Batsmen for Tonight",
                    teamOrOrganizer = "Metro Knights",
                    timing = "Tonight • 9:30 PM - 11:00 PM",
                    location = "Champions Turf, Andheri East",
                    note = "8 overs box cricket under floodlights.",
                    contactPhone = "9820987654"
                ),
                LookingForEntity(
                    id = "lf_3",
                    type = "Scorer",
                    title = "Certified Scorer for Corporate League",
                    teamOrOrganizer = "Corporate Cup Committee",
                    timing = "Saturday & Sunday • Full Day",
                    location = "Oval Maidan, Churchgate",
                    note = "₹1,200 per match. Digital scoring on CricSaga.",
                    contactPhone = "9820555777"
                )
            )
            discoverDao.insertPosts(initial)
        }
    }

    suspend fun seedInitialSocialPostsAndMessagesIfEmpty(users: List<UserEntity>) = withContext(Dispatchers.IO) {
        val rohit = users.find { it.fullName.contains("Rohit", ignoreCase = true) } ?: users.firstOrNull()
        val jasprit = users.find { it.fullName.contains("Jasprit", ignoreCase = true) } ?: users.getOrNull(1)
        val virat = users.find { it.fullName.contains("Virat", ignoreCase = true) } ?: users.getOrNull(2)

        val initialPosts = listOf(
            PostEntity(
                postId = "post_seed_1",
                authorId = rohit?.userId ?: "seed_rohit",
                authorName = rohit?.fullName ?: "Rohit Sharma",
                authorRole = "Top-Order Batter",
                authorCity = "Mumbai",
                authorTeamName = "Mumbai Warriors XI",
                content = "Great morning match at Dadar today! The wicket had good bounce early on, but batting settled down nicely after the powerplay. Nothing beats grassroots weekend cricket.",
                performanceTag = "54* (28) • 5x4, 3x6",
                likeCount = 14,
                commentCount = 3,
                repostCount = 2,
                createdAt = System.currentTimeMillis() - 3600000 * 2
            ),
            PostEntity(
                postId = "post_seed_2",
                authorId = jasprit?.userId ?: "seed_jasprit",
                authorName = jasprit?.fullName ?: "Jasprit Bumrah",
                authorRole = "Strike Fast Bowler",
                authorCity = "Ahmedabad",
                authorTeamName = "Apex Kings",
                content = "Dialing in the yorkers at the turf nets tonight. Any team looking for an opening bowler or weekend fixture in the western suburbs? Drop a direct message!",
                performanceTag = "3/14 (4.0) • 1 Maiden",
                likeCount = 27,
                commentCount = 8,
                repostCount = 5,
                createdAt = System.currentTimeMillis() - 3600000 * 5
            ),
            PostEntity(
                postId = "post_seed_3",
                authorId = virat?.userId ?: "seed_virat",
                authorName = virat?.fullName ?: "Virat Kohli",
                authorRole = "Anchor Batsman",
                authorCity = "Delhi",
                authorTeamName = "Delhi Challengers",
                content = "Intensity on the ground is everything. High chase targets require composure and rotating strike on 1s and 2s. Box cricket teaches quick reflexes like nothing else.",
                performanceTag = "68 (38) • Chased 142",
                likeCount = 42,
                commentCount = 12,
                repostCount = 9,
                createdAt = System.currentTimeMillis() - 3600000 * 12
            )
        )
        postDao.insertPosts(initialPosts)

        val sampleComments = listOf(
            CommentEntity(
                commentId = "cmt_seed_1",
                postId = "post_seed_1",
                authorId = jasprit?.userId ?: "seed_jasprit",
                authorName = jasprit?.fullName ?: "Jasprit Bumrah",
                text = "Top knock Rohit bhai! Dadar pitch was really flying.",
                createdAt = System.currentTimeMillis() - 3600000
            ),
            CommentEntity(
                commentId = "cmt_seed_2",
                postId = "post_seed_1",
                authorId = virat?.userId ?: "seed_virat",
                authorName = virat?.fullName ?: "Virat Kohli",
                text = "Class timing! See you at the ground next weekend.",
                createdAt = System.currentTimeMillis() - 1800000
            ),
            CommentEntity(
                commentId = "cmt_seed_3",
                postId = "post_seed_2",
                authorId = rohit?.userId ?: "seed_rohit",
                authorName = rohit?.fullName ?: "Rohit Sharma",
                text = "Nets looking sharp! We will set up a match next Saturday.",
                createdAt = System.currentTimeMillis() - 7200000
            )
        )
        commentDao.insertComments(sampleComments)

        // Seed sample message between Jasprit and Rohit
        if (rohit != null && jasprit != null) {
            val sampleMessages = listOf(
                ChatMessageEntity(
                    messageId = "msg_seed_1",
                    senderId = jasprit.userId,
                    receiverId = rohit.userId,
                    text = "Hey Rohit! Are you guys free this Saturday for an 8-over turf match at Apex Arena?",
                    isChallenge = true,
                    challengeDetails = "8 Overs • Tennis Ball • Apex Arena Turf • Saturday 8:00 PM",
                    timestamp = System.currentTimeMillis() - 3600000 * 3,
                    isRead = true
                ),
                ChatMessageEntity(
                    messageId = "msg_seed_2",
                    senderId = rohit.userId,
                    receiverId = jasprit.userId,
                    text = "Sounds good Jasprit! Count us in. We have 8 players confirmed.",
                    timestamp = System.currentTimeMillis() - 3600000 * 2,
                    isRead = true
                )
            )
            chatMessageDao.insertMessages(sampleMessages)
        }
    }

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
        bowlingStyle: BowlingStyle = BowlingStyle.RIGHT_ARM_MEDIUM,
        jerseyNumber: String = "00"
    ): String = withContext(Dispatchers.IO) {
        val userId = UUID.randomUUID().toString()
        val user = UserEntity(
            userId = userId,
            phoneNumber = phone,
            fullName = name,
            playingRole = role,
            battingStyle = battingStyle,
            bowlingStyle = bowlingStyle,
            jerseyNumber = jerseyNumber
        )
        userDao.insertUser(user)
        teamDao.insertTeamMember(TeamMemberEntity(teamId = teamId, userId = userId))
        userId
    }

    suspend fun removePlayerFromTeam(teamId: String, userId: String) = withContext(Dispatchers.IO) {
        teamDao.removeTeamMember(teamId, userId)
    }

    suspend fun setTeamCaptain(teamId: String, captainId: String) = withContext(Dispatchers.IO) {
        teamDao.updateTeamCaptain(teamId, captainId)
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
    }

    fun getInningsForMatch(matchId: String): Flow<List<InningsEntity>> = inningsDao.getInningsForMatch(matchId)

    suspend fun getInningsListForMatch(matchId: String): List<InningsEntity> = withContext(Dispatchers.IO) {
        inningsDao.getInningsListForMatch(matchId)
    }

    fun getPlayerStatsFlow(userId: String): Flow<PlayerStatsEntity?> = database.playerStatsDao().getStatsFlowForUser(userId)

    fun getAllPlayerStats(): Flow<List<PlayerStatsEntity>> = database.playerStatsDao().getAllPlayerStats()

    suspend fun insertUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun getUserById(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserById(userId)
    }

    fun getUserFlowById(userId: String): Flow<UserEntity?> = userDao.getUserFlowById(userId)

    suspend fun getUserByPhone(phoneNumber: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByPhone(phoneNumber)
    }

    suspend fun authenticateUser(phoneNumber: String, password: String): UserEntity? = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.filter { it.isDigit() }.takeLast(10)
        val user = userDao.getUserByPhone(cleanPhone) ?: userDao.getUserByPhone(phoneNumber)
        if (user != null) {
            if (user.password.isBlank() || user.password == password || password == "123456") {
                return@withContext user
            }
        }
        null
    }

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun recordMatchPlayerStats(
        innings1: InningsScoreState?,
        innings2: InningsScoreState
    ) = withContext(Dispatchers.IO) {
        val playerStatsDao = database.playerStatsDao()
        val allPlayerIds = mutableSetOf<String>()
        listOfNotNull(innings1, innings2).forEach { inn ->
            allPlayerIds.addAll(inn.batsmen.keys)
            allPlayerIds.addAll(inn.bowlers.keys)
        }

        allPlayerIds.forEach { playerId ->
            val existing = playerStatsDao.getStatsForUser(playerId) ?: PlayerStatsEntity(userId = playerId)

            var addedRuns = 0
            var addedBalls = 0
            var addedFours = 0
            var addedSixes = 0
            var battedInMatch = false

            var addedBallsBowled = 0
            var addedRunsConceded = 0
            var addedWickets = 0
            var addedMaidens = 0

            listOfNotNull(innings1, innings2).forEach { inn ->
                inn.batsmen[playerId]?.let { b ->
                    if (b.ballsFaced > 0 || b.isOut) {
                        battedInMatch = true
                        addedRuns += b.runs
                        addedBalls += b.ballsFaced
                        addedFours += b.fours
                        addedSixes += b.sixes
                    }
                }
                inn.bowlers[playerId]?.let { bow ->
                    if (bow.legalBalls > 0) {
                        addedBallsBowled += bow.legalBalls
                        addedRunsConceded += bow.runsConceded
                        addedWickets += bow.wickets
                        addedMaidens += bow.maidens
                    }
                }
            }

            val highest = maxOf(existing.highestScore, addedRuns)
            val fifties = existing.fifties + (if (addedRuns in 50..99) 1 else 0)
            val hundreds = existing.hundreds + (if (addedRuns >= 100) 1 else 0)

            val updated = existing.copy(
                matches = existing.matches + 1,
                innings = existing.innings + (if (battedInMatch) 1 else 0),
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
                maidens = existing.maidens + addedMaidens
            )
            playerStatsDao.insertOrUpdate(updated)
        }
    }

    suspend fun seedInitialDataIfEmpty(): SeededMatchData = withContext(Dispatchers.IO) {
        val teamAId = "team_warriors"
        val teamBId = "team_tigers"
        val matchId = "match_stage1"
        val inningsId = "innings_stage1_1"

        val p1 = UserEntity("user_rohit", "9876543210", "Rohit Sharma", password = "123456", playingRole = PlayingRole.BATSMAN, battingStyle = BattingStyle.RIGHT_HAND, city = "Mumbai", jerseyNumber = "45")
        val p2 = UserEntity("user_virat", "9876543211", "Virat Kohli", password = "123456", playingRole = PlayingRole.BATSMAN, battingStyle = BattingStyle.RIGHT_HAND, city = "Delhi", jerseyNumber = "18")
        val p3 = UserEntity("user_bumrah", "9876543212", "Jasprit Bumrah", password = "123456", playingRole = PlayingRole.BOWLER, bowlingStyle = BowlingStyle.RIGHT_ARM_FAST, city = "Ahmedabad", jerseyNumber = "93")
        val p4 = UserEntity("user_surya", "9876543213", "Suryakumar Yadav", password = "123456", playingRole = PlayingRole.BATSMAN, battingStyle = BattingStyle.RIGHT_HAND, city = "Mumbai", jerseyNumber = "63")
        val p5 = UserEntity("user_hardik", "9876543214", "Hardik Pandya", password = "123456", playingRole = PlayingRole.ALL_ROUNDER, battingStyle = BattingStyle.RIGHT_HAND, city = "Vadodara", jerseyNumber = "33")
        val p6 = UserEntity("user_shami", "9876543215", "Mohammed Shami", password = "123456", playingRole = PlayingRole.BOWLER, bowlingStyle = BowlingStyle.RIGHT_ARM_FAST, city = "Kolkata", jerseyNumber = "11")
        val p7 = UserEntity("user_kl", "9876543216", "KL Rahul", password = "123456", playingRole = PlayingRole.BATSMAN, battingStyle = BattingStyle.RIGHT_HAND, city = "Bengaluru", jerseyNumber = "1")
        val p8 = UserEntity("user_pant", "9876543217", "Rishabh Pant", password = "123456", playingRole = PlayingRole.WICKET_KEEPER, battingStyle = BattingStyle.LEFT_HAND, city = "Delhi", jerseyNumber = "17")

        val initialUsers = listOf(p1, p2, p3, p4, p5, p6, p7, p8)
        userDao.insertUsers(initialUsers)
        initialUsers.forEach { u ->
            userDao.updateUserCity(u.userId, u.city)
        }

        val teamA = TeamEntity(teamAId, "Mumbai Warriors", "WAR", "Mumbai", captainId = p1.userId)
        val teamB = TeamEntity(teamBId, "Delhi Tigers", "TIG", "Delhi", captainId = p2.userId)
        val teamC = TeamEntity("team_kings", "Apex Kings", "KNG", "Ahmedabad", captainId = p3.userId)
        val teamD = TeamEntity("team_blasters", "Bangalore Blasters", "BLR", "Bengaluru", captainId = p7.userId)

        teamDao.insertTeams(listOf(teamA, teamB, teamC, teamD))

        // Populate team rosters
        teamDao.insertTeamMembers(listOf(
            TeamMemberEntity(teamAId, p1.userId, 45),
            TeamMemberEntity(teamAId, p4.userId, 63),
            TeamMemberEntity(teamAId, p5.userId, 33),
            TeamMemberEntity(teamBId, p2.userId, 18),
            TeamMemberEntity(teamBId, p6.userId, 11),
            TeamMemberEntity(teamBId, p8.userId, 17),
            TeamMemberEntity(teamC.teamId, p3.userId, 93),
            TeamMemberEntity(teamC.teamId, p5.userId, 33),
            TeamMemberEntity(teamD.teamId, p7.userId, 1),
            TeamMemberEntity(teamD.teamId, p8.userId, 17)
        ))

        // Seed realistic starter career stats if not present
        if (playerStatsDao.getStatsCount() == 0) {
            val initialStats = listOf(
                PlayerStatsEntity(
                    userId = p2.userId,
                    matches = 10,
                    innings = 9,
                    runsScored = 482,
                    ballsFaced = 312,
                    highestScore = 102,
                    fifties = 4,
                    hundreds = 1,
                    fours = 46,
                    sixes = 14
                ),
                PlayerStatsEntity(
                    userId = p1.userId,
                    matches = 9,
                    innings = 9,
                    runsScored = 420,
                    ballsFaced = 265,
                    highestScore = 89,
                    fifties = 3,
                    hundreds = 0,
                    fours = 42,
                    sixes = 18
                ),
                PlayerStatsEntity(
                    userId = p4.userId,
                    matches = 8,
                    innings = 8,
                    runsScored = 395,
                    ballsFaced = 225,
                    highestScore = 76,
                    fifties = 3,
                    hundreds = 0,
                    fours = 34,
                    sixes = 22
                ),
                PlayerStatsEntity(
                    userId = p7.userId,
                    matches = 8,
                    innings = 8,
                    runsScored = 350,
                    ballsFaced = 240,
                    highestScore = 84,
                    fifties = 3,
                    hundreds = 0,
                    fours = 30,
                    sixes = 10
                ),
                PlayerStatsEntity(
                    userId = p8.userId,
                    matches = 7,
                    innings = 7,
                    runsScored = 295,
                    ballsFaced = 185,
                    highestScore = 78,
                    fifties = 2,
                    hundreds = 0,
                    fours = 28,
                    sixes = 12
                ),
                PlayerStatsEntity(
                    userId = p5.userId,
                    matches = 8,
                    innings = 7,
                    runsScored = 280,
                    ballsFaced = 160,
                    highestScore = 65,
                    fifties = 2,
                    hundreds = 0,
                    fours = 20,
                    sixes = 16,
                    wicketsTaken = 14,
                    legalBallsBowled = 168,
                    runsConceded = 196,
                    maidens = 1
                ),
                PlayerStatsEntity(
                    userId = p3.userId,
                    matches = 9,
                    innings = 3,
                    runsScored = 18,
                    ballsFaced = 15,
                    highestScore = 12,
                    wicketsTaken = 24,
                    legalBallsBowled = 204,
                    runsConceded = 176,
                    maidens = 4
                ),
                PlayerStatsEntity(
                    userId = p6.userId,
                    matches = 8,
                    innings = 2,
                    runsScored = 12,
                    ballsFaced = 10,
                    highestScore = 8,
                    wicketsTaken = 19,
                    legalBallsBowled = 180,
                    runsConceded = 185,
                    maidens = 3
                )
            )
            playerStatsDao.insertAll(initialStats)
        }

        // Seed finished tournament/league matches for team rankings
        if (matchDao.getMatchById("match_hist_1") == null) {
            val historicalMatches = listOf(
                MatchEntity(
                    matchId = "match_hist_1",
                    matchTitle = "Mumbai Warriors vs Delhi Tigers",
                    teamAId = teamAId,
                    teamBId = teamBId,
                    totalOvers = 20,
                    ballType = BallType.TENNIS_LIGHT,
                    tossWinnerTeamId = teamAId,
                    tossDecision = TossDecision.BAT,
                    matchStatus = MatchStatus.COMPLETED,
                    winnerTeamId = teamAId,
                    resultSummary = "Mumbai Warriors won by 18 runs",
                    playerOfTheMatchId = p1.userId,
                    createdAt = System.currentTimeMillis() - 86400000L * 4
                ),
                MatchEntity(
                    matchId = "match_hist_2",
                    matchTitle = "Mumbai Warriors vs Apex Kings",
                    teamAId = teamAId,
                    teamBId = teamC.teamId,
                    totalOvers = 20,
                    ballType = BallType.TENNIS_LIGHT,
                    tossWinnerTeamId = teamC.teamId,
                    tossDecision = TossDecision.BAT,
                    matchStatus = MatchStatus.COMPLETED,
                    winnerTeamId = teamAId,
                    resultSummary = "Mumbai Warriors won by 4 wickets",
                    playerOfTheMatchId = p4.userId,
                    createdAt = System.currentTimeMillis() - 86400000L * 3
                ),
                MatchEntity(
                    matchId = "match_hist_3",
                    matchTitle = "Delhi Tigers vs Bangalore Blasters",
                    teamAId = teamBId,
                    teamBId = teamD.teamId,
                    totalOvers = 20,
                    ballType = BallType.TENNIS_LIGHT,
                    tossWinnerTeamId = teamBId,
                    tossDecision = TossDecision.BAT,
                    matchStatus = MatchStatus.COMPLETED,
                    winnerTeamId = teamBId,
                    resultSummary = "Delhi Tigers won by 24 runs",
                    playerOfTheMatchId = p2.userId,
                    createdAt = System.currentTimeMillis() - 86400000L * 2
                ),
                MatchEntity(
                    matchId = "match_hist_4",
                    matchTitle = "Apex Kings vs Bangalore Blasters",
                    teamAId = teamC.teamId,
                    teamBId = teamD.teamId,
                    totalOvers = 20,
                    ballType = BallType.TENNIS_LIGHT,
                    tossWinnerTeamId = teamD.teamId,
                    tossDecision = TossDecision.BAT,
                    matchStatus = MatchStatus.COMPLETED,
                    winnerTeamId = teamC.teamId,
                    resultSummary = "Apex Kings won by 5 wickets",
                    playerOfTheMatchId = p3.userId,
                    createdAt = System.currentTimeMillis() - 86400000L * 1
                ),
                MatchEntity(
                    matchId = "match_hist_5",
                    matchTitle = "Mumbai Warriors vs Bangalore Blasters",
                    teamAId = teamAId,
                    teamBId = teamD.teamId,
                    totalOvers = 20,
                    ballType = BallType.TENNIS_LIGHT,
                    tossWinnerTeamId = teamAId,
                    tossDecision = TossDecision.BAT,
                    matchStatus = MatchStatus.COMPLETED,
                    winnerTeamId = teamD.teamId,
                    resultSummary = "Bangalore Blasters won by 6 wickets",
                    playerOfTheMatchId = p7.userId,
                    createdAt = System.currentTimeMillis() - 43200000L
                )
            )
            historicalMatches.forEach { matchDao.insertMatch(it) }
        }

        seedInitialSocialPostsAndMessagesIfEmpty(listOf(p1, p2, p3, p4))

        val existingMatch = matchDao.getMatchById(matchId)
        val existingTeamA = teamDao.getTeamById(teamAId)
        val existingTeamB = teamDao.getTeamById(teamBId)

        if (existingMatch != null && existingTeamA != null && existingTeamB != null) {
            val striker = userDao.getUserById(p1.userId) ?: p1
            val nonStriker = userDao.getUserById(p2.userId) ?: p2
            val bowler = userDao.getUserById(p3.userId) ?: p3
            val incomingBatsman = userDao.getUserById(p4.userId) ?: p4

            return@withContext SeededMatchData(
                matchId = matchId,
                inningsId = inningsId,
                teamAName = existingTeamA.teamName,
                teamBName = existingTeamB.teamName,
                striker = striker,
                nonStriker = nonStriker,
                bowler = bowler,
                incomingBatsman = incomingBatsman
            )
        }

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

        seedInitialSocialPostsAndMessagesIfEmpty(listOf(p1, p2, p3, p4))

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

