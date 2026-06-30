package com.example.whatsdel.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.whatsdel.data.entity.MessageEditHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EditHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditHistory(entry: MessageEditHistoryEntity): Long

    @Query("SELECT * FROM message_edit_history WHERE messageId = :messageId ORDER BY editedTimestamp ASC")
    fun observeEditHistory(messageId: Long): Flow<List<MessageEditHistoryEntity>>

    @Query("SELECT * FROM message_edit_history WHERE messageId = :messageId ORDER BY editedTimestamp ASC")
    suspend fun getEditHistory(messageId: Long): List<MessageEditHistoryEntity>

    @Query("SELECT COUNT(*) FROM message_edit_history WHERE messageId = :messageId AND newText = :newText")
    suspend fun countDuplicateEdits(messageId: Long, newText: String): Int
}
