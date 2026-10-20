package com.cricsaga.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey
    val teamId: String,
    val teamName: String,
    val shortCode: String,
    val city: String = "",
    val captainId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "team_members",
    primaryKeys = ["teamId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["teamId"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class TeamMemberEntity(
    val teamId: String,
    val userId: String,
    val jerseyNumber: Int? = null,
    val joinedAt: Long = System.currentTimeMillis()
)
