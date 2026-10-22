package com.cricsaga.app.data.local.dao

import androidx.room.*
import com.cricsaga.app.data.local.entity.FollowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun followUser(follow: FollowEntity)

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun unfollowUser(followerId: String, followingId: String)

    @Query("SELECT COUNT(*) > 0 FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    fun isFollowing(followerId: String, followingId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM follows WHERE followingId = :userId")
    fun getFollowersCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :userId")
    fun getFollowingCount(userId: String): Flow<Int>

    @Query("SELECT followingId FROM follows WHERE followerId = :followerId")
    fun getFollowingIds(followerId: String): Flow<List<String>>
}
