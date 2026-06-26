package com.example.whatsdel.di

import com.example.whatsdel.data.repository.MessageRepositoryImpl
import com.example.whatsdel.domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: MessageRepositoryImpl
    ): MessageRepository
}
