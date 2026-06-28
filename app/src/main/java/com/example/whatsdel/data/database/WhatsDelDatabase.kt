package com.example.whatsdel.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.whatsdel.data.dao.MessageDao
import com.example.whatsdel.data.entity.MessageEntity

@Database(
    entities = [MessageEntity::class],
    version = 3,
    exportSchema = true
)
abstract class WhatsDelDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
