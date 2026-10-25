package com.cricsaga.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cricsaga.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @androidx.room.Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUserFlowById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getUserByPhone(phoneNumber: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE googleId = :googleId LIMIT 1")
    suspend fun getUserByGoogleId(googleId: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    suspend fun getAllUsersList(): List<UserEntity>

    @Query("UPDATE users SET city = :city WHERE userId = :userId")
    suspend fun updateUserCity(userId: String, city: String)

    @Query("UPDATE users SET isPro = :isPro WHERE userId = :userId")
    suspend fun updateUserProStatus(userId: String, isPro: Boolean)

    @Query("UPDATE users SET isPro = :isPro, proPlan = :proPlan, proExpiresAt = :proExpiresAt WHERE userId = :userId")
    suspend fun updateUserProPlan(userId: String, isPro: Boolean, proPlan: String?, proExpiresAt: Long?)

    @Query("UPDATE users SET password = :password WHERE userId = :userId")
    suspend fun updateUserPassword(userId: String, password: String)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE isPro = 1")
    fun getProUserCountFlow(): Flow<Int>
}
