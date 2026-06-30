package com.example.whatsdel.di

import android.content.Context
import androidx.room.Room
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
                WhatsDelDatabase.MIGRATION_4_5
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: WhatsDelDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideEditHistoryDao(database: WhatsDelDatabase): com.example.whatsdel.data.dao.EditHistoryDao {
        return database.editHistoryDao()
    }
}
