package com.example.whatsdel.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.whatsdel.data.entity.ConversationEntity
import com.example.whatsdel.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("""
        SELECT 
            conversationId,
            CASE 
                WHEN INSTR(conversationName, ' (') > 0 THEN SUBSTR(conversationName, 1, INSTR(conversationName, ' (') - 1)
                ELSE conversationName 
            END AS conversationName,
            senderIdentifier,
            lastMessage,
            MAX(lastMessageTimestamp) AS lastMessageTimestamp,
            SUM(messageCount) AS messageCount,
            isGroup,
            SUM(unreadCount) AS unreadCount,
            MAX(hasDeletedMessages) AS hasDeletedMessages,
            MAX(hasEditedMessages) AS hasEditedMessages,
            MIN(createdAt) AS createdAt,
            MAX(updatedAt) AS updatedAt
        FROM conversations
        GROUP BY CASE 
            WHEN INSTR(conversationName, ' (') > 0 THEN SUBSTR(conversationName, 1, INSTR(conversationName, ' (') - 1)
            ELSE conversationName 
        END
        ORDER BY lastMessageTimestamp DESC
    """)
    fun observeAllConversations(): Flow<List<ConversationEntity>>

    @Query("""
        SELECT * FROM conversations
        WHERE conversationName LIKE '%' || :query || '%'
           OR senderIdentifier LIKE '%' || :query || '%'
           OR lastMessage LIKE '%' || :query || '%'
        ORDER BY lastMessageTimestamp DESC
    """)
    fun searchConversations(query: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE conversationId = :id LIMIT 1")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE conversationName = :name LIMIT 1")
    suspend fun findConversationByName(name: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE conversationId = :id OR conversationName = :name")
    suspend fun markConversationAsRead(id: Long, name: String)

    @Query("""
        SELECT * FROM messages
        WHERE conversationId = :conversationId
           OR (conversationId IS NULL AND (chatName = :chatName OR sender = :chatName))
        ORDER BY timestamp ASC
    """)
    fun observeMessagesForConversation(conversationId: Long, chatName: String): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(DISTINCT conversationName) FROM (SELECT conversationName FROM conversations UNION SELECT chatName AS conversationName FROM messages WHERE chatName IS NOT NULL AND chatName != '')")
    fun getConversationCount(): Flow<Int>

    @Query("DELETE FROM conversations WHERE conversationId = :conversationId")
    suspend fun deleteConversationById(conversationId: Long)
}
