package com.orfeaspanagou.adseventdashcam.network

import com.orfeaspanagou.adseventdashcam.data.api.DeviceApi
import com.orfeaspanagou.adseventdashcam.data.config.StreamConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkManager(): NetworkManager {
        return NetworkManager()
    }
}

@Singleton // The manager as a whole is a singleton, but it can rebuild retrofit inside.
class NetworkManager @Inject constructor() {

    private var retrofit: Retrofit? = null
    private var deviceApi: DeviceApi? = null

    fun initRetrofit(configuration: StreamConfiguration) {
        val newRetrofit = Retrofit.Builder()
            .baseUrl(configuration.httpEndpoint)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit = newRetrofit
        deviceApi = newRetrofit.create(DeviceApi::class.java)
    }

    fun getDeviceApi(): DeviceApi? {
        return deviceApi
    }
}
