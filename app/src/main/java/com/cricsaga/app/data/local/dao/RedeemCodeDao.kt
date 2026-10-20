package com.cricsaga.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cricsaga.app.data.local.entity.RedeemCodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RedeemCodeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCode(code: RedeemCodeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCodes(codes: List<RedeemCodeEntity>)

    @Query("SELECT * FROM redeem_codes WHERE UPPER(code) = UPPER(:code) LIMIT 1")
    suspend fun getCode(code: String): RedeemCodeEntity?

    @Query("SELECT * FROM redeem_codes ORDER BY createdAt DESC")
    fun getAllCodes(): Flow<List<RedeemCodeEntity>>

    @Query("UPDATE redeem_codes SET isRedeemed = 1, redeemedByUserId = :userId, redeemedAt = :timestamp WHERE UPPER(code) = UPPER(:code) AND isRedeemed = 0")
    suspend fun markCodeRedeemed(code: String, userId: String, timestamp: Long): Int

    @Query("SELECT COUNT(*) FROM redeem_codes")
    suspend fun getCount(): Int
}
