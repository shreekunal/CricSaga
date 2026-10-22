package com.cricsaga.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cricsaga.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsers(users: List<UserEntity>)

    @androidx.room.Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUserFlowById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getUserByPhone(phoneNumber: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("UPDATE users SET city = :city WHERE userId = :userId")
    suspend fun updateUserCity(userId: String, city: String)
}
