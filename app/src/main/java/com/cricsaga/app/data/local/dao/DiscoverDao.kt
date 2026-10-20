package com.cricsaga.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cricsaga.app.data.local.entity.LookingForEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscoverDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: LookingForEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<LookingForEntity>)

    @Query("SELECT * FROM looking_for_posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<LookingForEntity>>

    @Query("DELETE FROM looking_for_posts WHERE id = :id")
    suspend fun deletePost(id: String)

    @Query("SELECT COUNT(*) FROM looking_for_posts")
    suspend fun getPostCount(): Int
}
