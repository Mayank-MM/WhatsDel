package com.example.whatsdel.di

import android.content.Context
import androidx.room.Room
import com.example.whatsdel.data.dao.ConversationDao
import com.example.whatsdel.data.dao.EditHistoryDao
import com.example.whatsdel.data.dao.MessageDao
import com.example.whatsdel.data.database.WhatsDelDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): WhatsDelDatabase {
        return Room.databaseBuilder(
            context,
            WhatsDelDatabase::class.java,
            "whatsdel_database"
        )
            .addMigrations(
                WhatsDelDatabase.MIGRATION_2_3, 
                WhatsDelDatabase.MIGRATION_3_4,
                WhatsDelDatabase.MIGRATION_4_5,
                WhatsDelDatabase.MIGRATION_5_6,
                WhatsDelDatabase.MIGRATION_6_7,
                WhatsDelDatabase.MIGRATION_7_8
            )
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: WhatsDelDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideEditHistoryDao(database: WhatsDelDatabase): EditHistoryDao {
        return database.editHistoryDao()
    }

    @Provides
    @Singleton
    fun provideConversationDao(database: WhatsDelDatabase): ConversationDao {
        return database.conversationDao()
    }
}
