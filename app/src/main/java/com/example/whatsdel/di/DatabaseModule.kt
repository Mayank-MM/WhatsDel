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
        ).build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: WhatsDelDatabase): MessageDao {
        return database.messageDao()
    }
}
