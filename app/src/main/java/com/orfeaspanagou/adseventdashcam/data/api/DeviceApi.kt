package com.orfeaspanagou.adseventdashcam.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DeviceApi {
    @POST("devices/register")
    suspend fun registerDevice(@Body registration: DeviceRegistrationDto): Response<Unit>

    @PUT("devices/location")
    suspend fun updateLocation(@Body location: LocationUpdateDto): Response<Unit>

    @GET("devices/{deviceId}/status")
    suspend fun checkRegistrationStatus(@Path("deviceId") deviceId: String): Response<DeviceStatusResponse>
}

data class DeviceRegistrationDto(
    val deviceId: String,
    val model: String,
    val manufacturer: String,
    val osVersion: String,
    val location: LocationUpdateDto
)

data class LocationUpdateDto(
    val deviceId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)

data class DeviceStatusResponse(
    val isRegistered: Boolean
)