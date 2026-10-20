package com.cricsaga.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cricsaga.app.domain.model.BattingStyle
import com.cricsaga.app.domain.model.BowlingStyle
import com.cricsaga.app.domain.model.PlayingRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val phoneNumber: String,
    val fullName: String,
    val profilePhotoUrl: String? = null,
    val playingRole: PlayingRole = PlayingRole.ALL_ROUNDER,
    val battingStyle: BattingStyle = BattingStyle.RIGHT_HAND,
    val bowlingStyle: BowlingStyle = BowlingStyle.RIGHT_ARM_MEDIUM,
    val city: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
