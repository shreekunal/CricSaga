package com.cricsaga.app.data.local.dao

import androidx.room.*
import com.cricsaga.app.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun getPostsByAuthor(authorId: String): Flow<List<PostEntity>>

    @Query("""
        SELECT * FROM posts 
        WHERE authorId IN (SELECT followingId FROM follows WHERE followerId = :currentUserId)
        ORDER BY createdAt DESC
    """)
    fun getFollowingFeed(currentUserId: String): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE postId = :postId")
    suspend fun deletePost(postId: String)

    @Query("SELECT * FROM posts WHERE postId = :postId LIMIT 1")
    suspend fun getPostById(postId: String): PostEntity?

    @Query("UPDATE posts SET commentCount = commentCount + 1 WHERE postId = :postId")
    suspend fun incrementCommentCount(postId: String)
}
