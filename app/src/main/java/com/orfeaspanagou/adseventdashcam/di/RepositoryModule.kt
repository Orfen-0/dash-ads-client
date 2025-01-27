package com.orfeaspanagou.adseventdashcam.di

import android.content.Context
import com.orfeaspanagou.adseventdashcam.data.managers.MqttClientManager
import com.orfeaspanagou.adseventdashcam.data.managers.stream.StreamManager
import com.orfeaspanagou.adseventdashcam.data.repository.DeviceRepositoryImpl
import com.orfeaspanagou.adseventdashcam.data.repository.StreamRepositoryImpl
import com.orfeaspanagou.adseventdashcam.domain.repository.IDeviceRepository
import com.orfeaspanagou.adseventdashcam.domain.repository.IStreamRepository
import com.orfeaspanagou.adseventdashcam.network.NetworkManager
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
        networkManager: NetworkManager,
        mqttClientManager: MqttClientManager
    ): IDeviceRepository {
        return DeviceRepositoryImpl(context, networkManager, mqttClientManager)
    }

    @Provides
    @Singleton
    fun provideStreamRepository(
        deviceRepository: IDeviceRepository,
        streamManager: StreamManager
    ): IStreamRepository {
        return StreamRepositoryImpl(deviceRepository, streamManager)
    }
}
