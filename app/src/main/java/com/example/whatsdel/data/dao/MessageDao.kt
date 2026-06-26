package com.example.whatsdel.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.whatsdel.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isDeleted = 1 ORDER BY timestamp DESC")
    fun getDeletedMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isEdited = 1 ORDER BY timestamp DESC")
    fun getEditedMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE mediaPath IS NOT NULL ORDER BY timestamp DESC")
    fun getMediaMessages(): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages")
    fun getMessageCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE isDeleted = 1")
    fun getDeletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE isEdited = 1")
    fun getEditedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE mediaPath IS NOT NULL")
    fun getMediaCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}
