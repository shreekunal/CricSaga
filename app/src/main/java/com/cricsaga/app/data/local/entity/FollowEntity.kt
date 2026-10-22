package com.cricsaga.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "follows",
    primaryKeys = ["followerId", "followingId"],
    indices = [
        Index(value = ["followingId"])
    ]
)
data class FollowEntity(
    val followerId: String,
    val followingId: String,
    val createdAt: Long = System.currentTimeMillis()
)
