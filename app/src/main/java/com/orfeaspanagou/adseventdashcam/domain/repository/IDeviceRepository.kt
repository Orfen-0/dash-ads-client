package com.orfeaspanagou.adseventdashcam.domain.repository

import com.orfeaspanagou.adseventdashcam.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface IDeviceRepository {
    suspend fun registerDevice(): Result<Unit>
    suspend fun updateLocation(): Result<Unit>
    fun observeLocation(): Flow<Location>
    suspend fun getDeviceId(): String
    suspend fun getCurrentLocation(): Location? // Make sure it's public in interface
    suspend fun checkRegistrationStatus(): Boolean
    val isRegistered: Boolean
}