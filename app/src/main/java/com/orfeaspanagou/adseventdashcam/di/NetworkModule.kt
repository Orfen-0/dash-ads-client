// di/NetworkModule.kt
package com.orfeaspanagou.adseventdashcam.di

import com.orfeaspanagou.adseventdashcam.data.api.DeviceApi
import com.orfeaspanagou.adseventdashcam.data.config.StreamConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(configuration: StreamConfiguration
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(configuration.httpEndpoint) // Change this to your server URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDeviceApi(retrofit: Retrofit): DeviceApi {
        return retrofit.create(DeviceApi::class.java)
    }
}