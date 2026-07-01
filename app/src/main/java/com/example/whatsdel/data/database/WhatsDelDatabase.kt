package com.example.whatsdel.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.whatsdel.data.dao.MessageDao
import com.example.whatsdel.data.entity.MessageEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.example.whatsdel.data.entity.MessageEditHistoryEntity

@Database(
    entities = [MessageEntity::class, MessageEditHistoryEntity::class],
    version = 6,
    exportSchema = true
)
abstract class WhatsDelDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun editHistoryDao(): com.example.whatsdel.data.dao.EditHistoryDao

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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN originalMessage TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN editedAt INTEGER")
                db.execSQL("CREATE TABLE IF NOT EXISTS `message_edit_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `messageId` INTEGER NOT NULL, `previousText` TEXT NOT NULL, `newText` TEXT NOT NULL, `editedTimestamp` INTEGER NOT NULL, FOREIGN KEY(`messageId`) REFERENCES `messages`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_edit_history_messageId` ON `message_edit_history` (`messageId`)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN updatedAt INTEGER")
            }
        }
    }
}
