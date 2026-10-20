package com.cricsaga.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "looking_for_posts")
data class LookingForEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val title: String,
    val teamOrOrganizer: String,
    val timing: String,
    val location: String,
    val note: String,
    val contactPhone: String,
    val createdAt: Long = System.currentTimeMillis()
)
