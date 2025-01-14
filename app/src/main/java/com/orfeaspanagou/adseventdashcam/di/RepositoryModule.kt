package com.orfeaspanagou.adseventdashcam.di

import android.content.Context
import com.orfeaspanagou.adseventdashcam.data.api.DeviceApi
import com.orfeaspanagou.adseventdashcam.data.managers.stream.StreamManager
import com.orfeaspanagou.adseventdashcam.data.repository.DeviceRepositoryImpl
import com.orfeaspanagou.adseventdashcam.data.repository.StreamRepositoryImpl
import com.orfeaspanagou.adseventdashcam.domain.repository.IDeviceRepository
import com.orfeaspanagou.adseventdashcam.domain.repository.IStreamRepository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDeviceRepository(
        @ApplicationContext context: Context,
        api: DeviceApi
    ): IDeviceRepository {
        return DeviceRepositoryImpl(context, api)
    }

    @Provides
    @Singleton
    fun provideStreamRepository(
        deviceRepository: IDeviceRepository,
        streamManager: StreamManager
    ): IStreamRepository {
        return StreamRepositoryImpl(deviceRepository,streamManager)
    }
}