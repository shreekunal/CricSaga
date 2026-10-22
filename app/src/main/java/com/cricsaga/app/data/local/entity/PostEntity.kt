package com.cricsaga.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "posts",
    indices = [
        Index(value = ["authorId"]),
        Index(value = ["createdAt"])
    ]
)
data class PostEntity(
    @PrimaryKey
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorRole: String,
    val authorCity: String,
    val authorTeamName: String? = null,
    val authorAvatarUrl: String? = null,
    val content: String,
    val imageUri: String? = null,
    val linkedMatchId: String? = null,
    val performanceTag: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val repostCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
