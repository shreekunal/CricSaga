package com.cricsaga.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cricsaga.app.domain.model.DismissalType
import com.cricsaga.app.domain.model.ExtraType

@Entity(
    tableName = "deliveries",
    foreignKeys = [
        ForeignKey(
            entity = InningsEntity::class,
            parentColumns = ["inningsId"],
            childColumns = ["inningsId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["strikerId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["nonStrikerId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["bowlerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("inningsId"),
        Index("strikerId"),
        Index("nonStrikerId"),
        Index("bowlerId"),
        Index(value = ["inningsId", "deliveryIndex"], unique = true)
    ]
)
data class DeliveryEntity(
    @PrimaryKey
    val deliveryId: String,
    val inningsId: String,
    val deliveryIndex: Int,
    val overNumber: Int,
    val ballInOver: Int,
    val strikerId: String,
    val nonStrikerId: String,
    val bowlerId: String,
    val runsOffBat: Int = 0,
    val extraType: ExtraType = ExtraType.NONE,
    val extraRuns: Int = 0,
    val isLegalDelivery: Boolean = true,
    // Dismissal columns
    val isWicket: Boolean = false,
    val dismissalType: DismissalType? = null,
    val dismissedPlayerId: String? = null,
    val primaryFielderId: String? = null,
    val secondaryFielderId: String? = null,
    val runsCompletedBeforeDismissal: Int = 0,
    val nextStrikerIdAfterDismissal: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
