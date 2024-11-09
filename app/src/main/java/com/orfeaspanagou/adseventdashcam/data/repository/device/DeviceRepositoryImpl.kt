package com.orfeaspanagou.adseventdashcam.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.orfeaspanagou.adseventdashcam.data.api.DeviceApi
import com.orfeaspanagou.adseventdashcam.data.api.DeviceRegistrationDto
import com.orfeaspanagou.adseventdashcam.data.api.LocationUpdateDto
import com.orfeaspanagou.adseventdashcam.domain.model.Location
import com.orfeaspanagou.adseventdashcam.domain.repository.IDeviceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: DeviceApi
) : IDeviceRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private var _isRegistered = false
    override val isRegistered: Boolean get() = _isRegistered

    init {
        // Start location updates when repository is created
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        locationUpdateJob
    }

    private val locationUpdateJob = scope.launch {
        observeLocation()
            .collect { location ->
                try {
                    updateLocation()
                } catch (e: Exception) {
                    // Log error but continue collecting
                    println("Failed to update location: ${e.message}")
                }
            }
    }

    override suspend fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    override suspend fun registerDevice(): Result<Unit> {
        return try {
            val location = getCurrentLocation() ?: return Result.failure(
                Exception("Could not get location")
            )

            val registration = DeviceRegistrationDto(
                deviceId = getDeviceId(),
                model = Build.MODEL,
                manufacturer = Build.MANUFACTURER,
                osVersion = Build.VERSION.RELEASE,
                location = LocationUpdateDto(
                    deviceId = getDeviceId(),
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    timestamp = System.currentTimeMillis()
                )
            )

            val response = api.registerDevice(registration)
            if (response.isSuccessful) {
                _isRegistered = true
                Result.success(Unit)
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLocation(): Result<Unit> {
        return try {
            val location = getCurrentLocation() ?: return Result.failure(
                Exception("Could not get location")
            )

            val locationUpdate = LocationUpdateDto(
                deviceId = getDeviceId(),
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                timestamp = System.currentTimeMillis()
            )

            val response = api.updateLocation(locationUpdate)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Location update failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeLocation(): Flow<Location> = flow {
        while (true) {
            try {
                getCurrentLocation()?.let { location ->
                    emit(location)
                }
                delay(LOCATION_UPDATE_INTERVAL)
            } catch (e: Exception) {
                println("Error getting location: ${e.message}")
                delay(RETRY_INTERVAL)
            }
        }
    }.filterNotNull()

    override suspend fun getCurrentLocation(): Location? {
        return suspendCancellableCoroutine { continuation ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                continuation.resumeWithException(
                    SecurityException("Location permission not granted")
                )
                return@suspendCancellableCoroutine
            }

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(
                            Location(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracy = location.accuracy,
                                timestamp = location.time
                            )
                        )
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    companion object {
        private const val LOCATION_UPDATE_INTERVAL = 5 * 60 * 1000L // 5 minutes
        private const val RETRY_INTERVAL = 30 * 1000L // 30 seconds
    }
}