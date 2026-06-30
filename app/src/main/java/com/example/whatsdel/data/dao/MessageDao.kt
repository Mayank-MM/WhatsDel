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

    @Query("SELECT * FROM messages WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun observeActiveMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isDeleted = 1 ORDER BY deletedTimestamp DESC")
    fun observeDeletedMessages(): Flow<List<MessageEntity>>

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

    // Phase 4: Media queries
    @Query("SELECT * FROM messages WHERE hasMedia = 1 ORDER BY timestamp DESC")
    fun observeAllMedia(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE hasMedia = 1 AND mediaType = :type ORDER BY timestamp DESC")
    fun observeMediaByType(type: String): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE hasMedia = 1 AND mediaType = 'image'")
    fun getImageCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE hasMedia = 1 AND mediaType = 'video'")
    fun getVideoCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE hasMedia = 1 AND mediaType IN ('audio', 'voice_note')")
    fun getAudioCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE hasMedia = 1 AND mediaType = 'sticker'")
    fun getStickerCount(): Flow<Int>

    @Query("""
        SELECT * FROM messages WHERE hasMedia = 1
        AND (sender LIKE '%' || :query || '%'
            OR chatName LIKE '%' || :query || '%'
            OR message LIKE '%' || :query || '%'
            OR mediaFileName LIKE '%' || :query || '%')
        ORDER BY timestamp DESC
    """)
    fun searchMedia(query: String): Flow<List<MessageEntity>>

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

    @Query("""
        SELECT * FROM messages 
        WHERE isDeleted = 0 
        AND (sender LIKE '%' || :query || '%' 
        OR chatName LIKE '%' || :query || '%' 
        OR message LIKE '%' || :query || '%')
        ORDER BY timestamp DESC
    """)
    fun searchActiveMessages(query: String): Flow<List<MessageEntity>>

    @Query("""
        SELECT * FROM messages 
        WHERE isDeleted = 1 
        AND (sender LIKE '%' || :query || '%' 
        OR chatName LIKE '%' || :query || '%' 
        OR message LIKE '%' || :query || '%')
        ORDER BY deletedTimestamp DESC
    """)
    fun searchDeletedMessages(query: String): Flow<List<MessageEntity>>

    @Query("""
        SELECT * FROM messages 
        WHERE isEdited = 1 
        AND (sender LIKE '%' || :query || '%' 
        OR chatName LIKE '%' || :query || '%' 
        OR message LIKE '%' || :query || '%'
        OR originalMessage LIKE '%' || :query || '%')
        ORDER BY editedAt DESC
    """)
    fun searchEditedMessages(query: String): Flow<List<MessageEntity>>

    @Query("""
        SELECT * FROM messages 
        WHERE (chatName LIKE '%' || :chatName || '%' OR sender LIKE '%' || :chatName || '%')
        AND isDeleted = 0 
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
    suspend fun findMatchingMessage(chatName: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE isDeleted = 0 ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentActiveMessages(): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE notificationId = :notificationId AND isDeleted = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun findMessageByNotificationId(notificationId: Int): MessageEntity?

    @Query("SELECT * FROM messages WHERE sender = :sender AND notificationId = :notificationId AND isDeleted = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun findMessageBySenderAndNotificationId(sender: String, notificationId: Int): MessageEntity?

    @Query("UPDATE messages SET isDeleted = :isDeleted, deletedTimestamp = :deletedTimestamp WHERE id = :id")
    suspend fun markAsDeleted(id: Long, deletedTimestamp: Long, isDeleted: Boolean = true)

    @Query("""
        UPDATE messages SET 
            isEdited = 1, 
            editedAt = :editedAt, 
            message = :newText,
            originalMessage = CASE WHEN originalMessage IS NULL THEN :originalText ELSE originalMessage END
        WHERE id = :id
    """)
    suspend fun markMessageEdited(id: Long, editedAt: Long, newText: String, originalText: String)

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Long): MessageEntity?
}
