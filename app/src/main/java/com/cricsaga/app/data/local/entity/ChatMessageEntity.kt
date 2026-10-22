package com.cricsaga.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["senderId", "receiverId"]),
        Index(value = ["timestamp"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    val messageId: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val isChallenge: Boolean = false,
    val challengeDetails: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
