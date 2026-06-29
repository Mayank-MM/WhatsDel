package com.example.whatsdel.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.whatsdel.data.dao.MessageDao
import com.example.whatsdel.data.entity.MessageEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MessageEntity::class],
    version = 4,
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN hasMedia INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN mediaType TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN mediaUri TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN mediaMimeType TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN mediaFileName TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN mediaSize INTEGER")
                db.execSQL("ALTER TABLE messages ADD COLUMN thumbnailPath TEXT")
            }
        }
    }
}
