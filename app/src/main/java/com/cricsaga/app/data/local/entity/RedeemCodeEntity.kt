package com.cricsaga.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cricsaga.app.data.local.ProPlan

@Entity(tableName = "redeem_codes")
data class RedeemCodeEntity(
    @PrimaryKey
    val code: String,
    val plan: ProPlan,
    val durationDays: Int,
    val isRedeemed: Boolean = false,
    val redeemedByUserId: String? = null,
    val redeemedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
