package com.example.whatsdel.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.whatsdel.data.dao.MessageDao
import com.example.whatsdel.data.entity.MessageEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MessageEntity::class],
    version = 3,
    exportSchema = true
)
abstract class WhatsDelDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN deletedTimestamp INTEGER")
            }
        }
    }
}
