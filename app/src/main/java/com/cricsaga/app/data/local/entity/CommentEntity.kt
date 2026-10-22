package com.cricsaga.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comments",
    indices = [
        Index(value = ["postId"]),
        Index(value = ["createdAt"])
    ]
)
data class CommentEntity(
    @PrimaryKey
    val commentId: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)
