package com.cricsaga.app.data.local.dao

import androidx.room.*
import com.cricsaga.app.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("""
        SELECT * FROM chat_messages 
        WHERE (senderId = :userA AND receiverId = :userB) 
           OR (senderId = :userB AND receiverId = :userA)
        ORDER BY timestamp ASC
    """)
    fun getMessagesBetween(userA: String, userB: String): Flow<List<ChatMessageEntity>>

    @Query("""
        SELECT * FROM chat_messages 
        WHERE senderId = :userId OR receiverId = :userId
        ORDER BY timestamp DESC
    """)
    fun getAllMessagesForUser(userId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET isRead = 1 WHERE senderId = :senderId AND receiverId = :receiverId AND isRead = 0")
    suspend fun markAsRead(senderId: String, receiverId: String)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE receiverId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>
}
