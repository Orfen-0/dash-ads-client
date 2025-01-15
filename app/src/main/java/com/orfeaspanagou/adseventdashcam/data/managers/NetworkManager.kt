package com.orfeaspanagou.adseventdashcam.network

import com.orfeaspanagou.adseventdashcam.data.api.DeviceApi
import com.orfeaspanagou.adseventdashcam.data.config.StreamConfiguration
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManager @Inject constructor() {

    // Use lateinit so that once we call initRetrofit, these are guaranteed to be non-null.
    private lateinit var retrofit: Retrofit
    private lateinit var deviceApi: DeviceApi

    fun initRetrofit(configuration: StreamConfiguration) {
        // Build a new Retrofit instance using the latest httpEndpoint from the configuration.
        retrofit = Retrofit.Builder()
            .baseUrl(configuration.httpEndpoint)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create a new DeviceApi using the freshly built Retrofit instance.
        deviceApi = retrofit.create(DeviceApi::class.java)
    }

    fun getDeviceApi(): DeviceApi {
        // Caller is responsible to call initRetrofit() first
        return deviceApi
    }
}
