package com.cricsaga.app.data.remote

import android.util.Log
import com.cricsaga.app.data.local.entity.DeliveryEntity
import com.cricsaga.app.data.local.entity.InningsEntity
import com.cricsaga.app.data.local.entity.MatchEntity
import com.cricsaga.app.data.local.entity.PostEntity
import com.cricsaga.app.data.local.entity.TeamEntity
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.BallType
import com.cricsaga.app.domain.model.BattingStyle
import com.cricsaga.app.domain.model.BowlingStyle
import com.cricsaga.app.domain.model.MatchStatus
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.domain.model.TossDecision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseClient {

    private const val TAG = "SupabaseClient"
    private const val BASE_URL = "https://wyycmghdkcgdmoaaleki.supabase.co/rest/v1"
    private const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind5eWNtZ2hka2NnZG1vYWFsZWtpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTI2NTY2NDIsImV4cCI6MjEwODIzMjY0Mn0.USMZes3f9weO34N3p5A7LRtt8FPvAtd7UunzjIPa7q0"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private fun newRequestBuilder(endpoint: String): Request.Builder {
        return Request.Builder()
            .url("$BASE_URL/$endpoint")
            .header("apikey", API_KEY)
            .header("Authorization", "Bearer $API_KEY")
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation")
    }

    // =========================================================================
    // User Authentication & Profile
    // =========================================================================

    suspend fun getUserByPhone(phoneNumber: String): Result<UserEntity?> = withContext(Dispatchers.IO) {
        try {
            val cleanPhone = phoneNumber.filter { it.isDigit() }.takeLast(10)
            val request = newRequestBuilder("users?phone_number=eq.$cleanPhone&select=*").get().build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} fetching user"))
            }

            val body = response.body?.string() ?: "[]"
            val array = JSONArray(body)
            if (array.length() == 0) {
                return@withContext Result.success(null)
            }

            val obj = array.getJSONObject(0)
            val user = parseUser(obj)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user by phone", e)
            Result.failure(e)
        }
    }

    suspend fun getUserByEmail(email: String): Result<UserEntity?> = withContext(Dispatchers.IO) {
        try {
            val cleanEmail = email.trim().lowercase()
            val request = newRequestBuilder("users?email=eq.$cleanEmail&select=*").get().build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} fetching user by email"))
            }

            val body = response.body?.string() ?: "[]"
            val array = JSONArray(body)
            if (array.length() == 0) {
                return@withContext Result.success(null)
            }

            val obj = array.getJSONObject(0)
            val user = parseUser(obj)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user by email", e)
            Result.failure(e)
        }
    }

    suspend fun getUserByGoogleId(googleId: String): Result<UserEntity?> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("users?google_id=eq.$googleId&select=*").get().build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} fetching user by googleId"))
            }

            val body = response.body?.string() ?: "[]"
            val array = JSONArray(body)
            if (array.length() == 0) {
                return@withContext Result.success(null)
            }

            val obj = array.getJSONObject(0)
            val user = parseUser(obj)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user by googleId", e)
            Result.failure(e)
        }
    }

    suspend fun signUpUser(user: UserEntity): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val cleanPhone = user.phoneNumber.filter { it.isDigit() }.takeLast(10)
            val phoneVal = if (cleanPhone.isNotEmpty()) cleanPhone else "pending_${user.userId.replace("-", "").take(10)}"

            val json = JSONObject().apply {
                put("user_id", user.userId)
                put("phone_number", phoneVal)
                put("full_name", user.fullName)
                put("password_hash", user.password)
                if (!user.email.isNullOrBlank()) put("email", user.email)
                if (!user.googleId.isNullOrBlank()) put("google_id", user.googleId)
                put("profile_photo_url", user.profilePhotoUrl ?: JSONObject.NULL)
                put("playing_role", user.playingRole.name)
                put("batting_style", user.battingStyle.name)
                put("bowling_style", user.bowlingStyle.name)
                put("city", user.city)
                put("jersey_number", user.jerseyNumber)
                put("is_pro", user.isPro)
                put("created_at", user.createdAt)
            }

            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = newRequestBuilder("users").post(body).build()
            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val resBody = response.body?.string() ?: "[]"
                val array = JSONArray(resBody)
                val returnedUser = if (array.length() > 0) parseUser(array.getJSONObject(0)) else user
                Result.success(returnedUser)
            } else {
                Result.failure(Exception("Failed to register user: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error signing up user", e)
            Result.failure(e)
        }
    }

    suspend fun authenticateUser(phone: String, password: String): Result<UserEntity?> = withContext(Dispatchers.IO) {
        try {
            val cleanPhone = phone.filter { it.isDigit() }.takeLast(10)
            val userResult = getUserByPhone(cleanPhone)
            if (userResult.isFailure) return@withContext userResult

            val user = userResult.getOrNull()
            if (user == null) {
                return@withContext Result.success(null) // User not found
            }

            if (user.password == password) {
                Result.success(user)
            } else {
                Result.success(null) // Password mismatch
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error authenticating user", e)
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): Result<List<UserEntity>> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("users?select=*&order=created_at.desc").get().build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} fetching all users"))
            }

            val body = response.body?.string() ?: "[]"
            val array = JSONArray(body)
            val list = mutableListOf<UserEntity>()
            for (i in 0 until array.length()) {
                list.add(parseUser(array.getJSONObject(i)))
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all users", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("users?user_id=eq.$userId").delete().build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful || response.code == 404) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete user: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user from Supabase", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserPassword(userId: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("password_hash", newPassword)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = newRequestBuilder("users?user_id=eq.$userId").patch(body).build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update password: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating password in Supabase", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserProStatus(userId: String, isPro: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("is_pro", isPro)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = newRequestBuilder("users?user_id=eq.$userId").patch(body).build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update pro status: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating pro status in Supabase", e)
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: UserEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                val cleanPhone = user.phoneNumber.filter { it.isDigit() }.takeLast(10)
                if (cleanPhone.isNotEmpty()) {
                    put("phone_number", cleanPhone)
                }
                put("full_name", user.fullName)
                if (!user.email.isNullOrBlank()) put("email", user.email)
                if (!user.googleId.isNullOrBlank()) put("google_id", user.googleId)
                if (!user.profilePhotoUrl.isNullOrBlank()) put("profile_photo_url", user.profilePhotoUrl)
                put("playing_role", user.playingRole.name)
                put("batting_style", user.battingStyle.name)
                put("bowling_style", user.bowlingStyle.name)
                put("city", user.city)
                put("jersey_number", user.jerseyNumber)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = newRequestBuilder("users?user_id=eq.${user.userId}").patch(body).build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update user: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user in Supabase", e)
            Result.failure(e)
        }
    }

    suspend fun checkCloudPing(): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            val request = newRequestBuilder("users?select=count").head().build()
            val response = httpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - start
            if (response.isSuccessful || response.code == 200 || response.code == 206) {
                Result.success(latency)
            } else {
                Result.success(latency)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // Matches & Innings Sync
    // =========================================================================

    suspend fun createMatch(match: MatchEntity, innings: InningsEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val matchJson = JSONObject().apply {
                put("match_id", match.matchId)
                put("match_title", match.matchTitle)
                put("team_a_id", match.teamAId)
                put("team_b_id", match.teamBId)
                put("total_overs", match.totalOvers)
                put("ball_type", match.ballType.name)
                put("toss_winner_team_id", match.tossWinnerTeamId ?: JSONObject.NULL)
                put("toss_decision", match.tossDecision?.name ?: JSONObject.NULL)
                put("max_overs_per_bowler", match.maxOversPerBowler)
                put("is_lbw_enabled", match.isLbwEnabled)
                put("is_free_hit_enabled", match.isFreeHitEnabled)
                put("match_status", match.matchStatus.name)
                put("created_at", match.createdAt)
            }

            val matchBody = matchJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val matchReq = newRequestBuilder("matches").post(matchBody).build()
            val matchRes = httpClient.newCall(matchReq).execute()

            if (!matchRes.isSuccessful && matchRes.code != 409) {
                Log.w(TAG, "Match insert returned HTTP ${matchRes.code}")
            }

            val inningsJson = JSONObject().apply {
                put("innings_id", innings.inningsId)
                put("match_id", innings.matchId)
                put("innings_number", innings.inningsNumber)
                put("batting_team_id", innings.battingTeamId)
                put("bowling_team_id", innings.bowlingTeamId)
                put("total_runs", innings.totalRuns)
                put("total_wickets", innings.totalWickets)
                put("total_legal_deliveries", innings.totalLegalDeliveries)
                put("is_completed", innings.isCompleted)
            }

            val inningsBody = inningsJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val inningsReq = newRequestBuilder("innings").post(inningsBody).build()
            val inningsRes = httpClient.newCall(inningsReq).execute()

            if (!inningsRes.isSuccessful && inningsRes.code != 409) {
                Log.w(TAG, "Innings insert returned HTTP ${inningsRes.code}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing match to Supabase", e)
            Result.failure(e)
        }
    }

    suspend fun recordDelivery(
        delivery: DeliveryEntity,
        totalRuns: Int,
        totalWickets: Int,
        legalDeliveries: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val deliveryJson = JSONObject().apply {
                put("delivery_id", delivery.deliveryId)
                put("innings_id", delivery.inningsId)
                put("delivery_index", delivery.deliveryIndex)
                put("over_number", delivery.overNumber)
                put("ball_in_over", delivery.ballInOver)
                put("striker_id", delivery.strikerId)
                put("non_striker_id", delivery.nonStrikerId)
                put("bowler_id", delivery.bowlerId)
                put("runs_off_bat", delivery.runsOffBat)
                put("extra_type", delivery.extraType.name)
                put("extra_runs", delivery.extraRuns)
                put("is_legal_delivery", delivery.isLegalDelivery)
                put("is_wicket", delivery.isWicket)
                put("dismissal_type", delivery.dismissalType?.name ?: JSONObject.NULL)
                put("dismissed_player_id", delivery.dismissedPlayerId ?: JSONObject.NULL)
                put("timestamp", delivery.timestamp)
            }

            val deliveryBody = deliveryJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val delReq = newRequestBuilder("deliveries").post(deliveryBody).build()
            httpClient.newCall(delReq).execute().close()

            // Update innings summary
            val inningsUpdateJson = JSONObject().apply {
                put("total_runs", totalRuns)
                put("total_wickets", totalWickets)
                put("total_legal_deliveries", legalDeliveries)
            }
            val innBody = inningsUpdateJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val innReq = newRequestBuilder("innings?innings_id=eq.${delivery.inningsId}")
                .patch(innBody)
                .build()
            httpClient.newCall(innReq).execute().close()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error recording delivery to Supabase", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // Teams & Community Posts
    // =========================================================================

    suspend fun createTeam(team: TeamEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("team_id", team.teamId)
                put("team_name", team.teamName)
                put("short_code", team.shortCode)
                put("city", team.city)
                put("captain_id", team.captainId ?: JSONObject.NULL)
                put("created_at", team.createdAt)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val req = newRequestBuilder("teams").post(body).build()
            httpClient.newCall(req).execute().close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTeamMember(teamId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("team_id", teamId)
                put("user_id", userId)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val req = newRequestBuilder("team_members").post(body).build()
            httpClient.newCall(req).execute().close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTeamCaptain(teamId: String, captainId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("captain_id", captainId)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val req = newRequestBuilder("teams?team_id=eq.$teamId").patch(body).build()
            httpClient.newCall(req).execute().close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(post: PostEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("post_id", post.postId)
                put("author_id", post.authorId)
                put("author_name", post.authorName)
                put("author_role", post.authorRole)
                put("author_city", post.authorCity)
                put("author_team_name", post.authorTeamName ?: JSONObject.NULL)
                put("author_avatar_url", post.authorAvatarUrl ?: JSONObject.NULL)
                put("content", post.content)
                put("image_uri", post.imageUri ?: JSONObject.NULL)
                put("linked_match_id", post.linkedMatchId ?: JSONObject.NULL)
                put("performance_tag", post.performanceTag ?: JSONObject.NULL)
                put("like_count", post.likeCount)
                put("comment_count", post.commentCount)
                put("created_at", post.createdAt)
            }
            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val req = newRequestBuilder("posts").post(body).build()
            httpClient.newCall(req).execute().close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllTeams(): Result<List<TeamEntity>> = withContext(Dispatchers.IO) {
        try {
            val req = newRequestBuilder("teams?select=*&order=created_at.desc").get().build()
            httpClient.newCall(req).execute().use { res ->
                if (!res.isSuccessful) return@withContext Result.success(emptyList())
                val body = res.body?.string() ?: "[]"
                val array = JSONArray(body)
                val list = mutableListOf<TeamEntity>()
                for (i in 0 until array.length()) {
                    list.add(parseTeam(array.getJSONObject(i)))
                }
                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllTeamMembers(): Result<List<Pair<String, String>>> = withContext(Dispatchers.IO) {
        try {
            val req = newRequestBuilder("team_members?select=team_id,user_id").get().build()
            httpClient.newCall(req).execute().use { res ->
                if (!res.isSuccessful) return@withContext Result.success(emptyList())
                val body = res.body?.string() ?: "[]"
                val array = JSONArray(body)
                val list = mutableListOf<Pair<String, String>>()
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    list.add(Pair(o.optString("team_id"), o.optString("user_id")))
                }
                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTeam(teamId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Delete associated matches first to satisfy foreign key constraint
            val delMatchesReq = newRequestBuilder("matches?or=(team_a_id.eq.$teamId,team_b_id.eq.$teamId)").delete().build()
            httpClient.newCall(delMatchesReq).execute().close()

            // Delete team members
            val delMembersReq = newRequestBuilder("team_members?team_id=eq.$teamId").delete().build()
            httpClient.newCall(delMembersReq).execute().close()

            // Delete the team itself
            val delTeamReq = newRequestBuilder("teams?team_id=eq.$teamId").delete().build()
            httpClient.newCall(delTeamReq).execute().close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeTeamMember(teamId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val req = newRequestBuilder("team_members?team_id=eq.$teamId&user_id=eq.$userId").delete().build()
            httpClient.newCall(req).execute().close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllMatches(): Result<List<MatchEntity>> = withContext(Dispatchers.IO) {
        try {
            val req = newRequestBuilder("matches?select=*&order=created_at.desc").get().build()
            httpClient.newCall(req).execute().use { res ->
                if (!res.isSuccessful) return@withContext Result.success(emptyList())
                val body = res.body?.string() ?: "[]"
                val array = JSONArray(body)
                val list = mutableListOf<MatchEntity>()
                for (i in 0 until array.length()) {
                    list.add(parseMatch(array.getJSONObject(i)))
                }
                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMatch(matchId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val req = newRequestBuilder("matches?match_id=eq.$matchId").delete().build()
            httpClient.newCall(req).execute().close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLiveMatches(): Result<List<MatchEntity>> = withContext(Dispatchers.IO) {
        try {
            val req = newRequestBuilder("matches?match_status=eq.LIVE&order=created_at.desc&limit=10")
                .get()
                .build()
            httpClient.newCall(req).execute().use { res ->
                if (!res.isSuccessful) return@withContext Result.success(emptyList())

                val body = res.body?.string() ?: "[]"
                val array = JSONArray(body)
                val list = mutableListOf<MatchEntity>()
                for (i in 0 until array.length()) {
                    list.add(parseMatch(array.getJSONObject(i)))
                }
                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseTeam(o: JSONObject): TeamEntity {
        return TeamEntity(
            teamId = o.optString("team_id"),
            teamName = o.optString("team_name"),
            shortCode = o.optString("short_code"),
            city = o.optString("city", ""),
            captainId = o.optString("captain_id").takeIf { it.isNotEmpty() && it != "null" },
            createdAt = o.optLong("created_at", System.currentTimeMillis())
        )
    }

    private fun parseMatch(o: JSONObject): MatchEntity {
        return MatchEntity(
            matchId = o.optString("match_id"),
            matchTitle = o.optString("match_title"),
            teamAId = o.optString("team_a_id"),
            teamBId = o.optString("team_b_id"),
            totalOvers = o.optInt("total_overs", 20),
            ballType = runCatching { BallType.valueOf(o.optString("ball_type")) }.getOrDefault(BallType.TENNIS_LIGHT),
            tossWinnerTeamId = o.optString("toss_winner_team_id").takeIf { it.isNotEmpty() && it != "null" },
            tossDecision = runCatching { TossDecision.valueOf(o.optString("toss_decision")) }.getOrNull(),
            maxOversPerBowler = o.optInt("max_overs_per_bowler", 4),
            isLbwEnabled = o.optBoolean("is_lbw_enabled", false),
            isFreeHitEnabled = o.optBoolean("is_free_hit_enabled", true),
            matchStatus = runCatching { MatchStatus.valueOf(o.optString("match_status")) }.getOrDefault(MatchStatus.SCHEDULED),
            winnerTeamId = o.optString("winner_team_id").takeIf { it.isNotEmpty() && it != "null" },
            resultSummary = o.optString("result_summary").takeIf { it.isNotEmpty() && it != "null" },
            playerOfTheMatchId = o.optString("player_of_the_match_id").takeIf { it.isNotEmpty() && it != "null" },
            createdAt = o.optLong("created_at", System.currentTimeMillis())
        )
    }

    private fun parseUser(obj: JSONObject): UserEntity {
        val rawPhone = obj.optString("phone_number")
        val cleanPhone = if (rawPhone.startsWith("pending_")) "" else rawPhone
        return UserEntity(
            userId = obj.optString("user_id"),
            phoneNumber = cleanPhone,
            fullName = obj.optString("full_name"),
            password = obj.optString("password_hash", "123456"),
            email = obj.optString("email").takeIf { it.isNotEmpty() && it != "null" },
            googleId = obj.optString("google_id").takeIf { it.isNotEmpty() && it != "null" },
            profilePhotoUrl = obj.optString("profile_photo_url").takeIf { it.isNotEmpty() && it != "null" },
            playingRole = runCatching { PlayingRole.valueOf(obj.optString("playing_role")) }.getOrDefault(PlayingRole.ALL_ROUNDER),
            battingStyle = runCatching { BattingStyle.valueOf(obj.optString("batting_style")) }.getOrDefault(BattingStyle.RIGHT_HAND),
            bowlingStyle = runCatching { BowlingStyle.valueOf(obj.optString("bowling_style")) }.getOrDefault(BowlingStyle.RIGHT_ARM_MEDIUM),
            city = obj.optString("city", "Mumbai"),
            jerseyNumber = obj.optString("jersey_number", "18"),
            isPro = obj.optBoolean("is_pro", false),
            createdAt = obj.optLong("created_at", System.currentTimeMillis())
        )
    }
}
