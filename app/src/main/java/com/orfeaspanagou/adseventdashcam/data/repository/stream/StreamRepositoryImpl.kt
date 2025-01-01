package com.orfeaspanagou.adseventdashcam.data.repository


import android.util.Log
import com.orfeaspanagou.adseventdashcam.data.manager.stream.StreamManager
import com.orfeaspanagou.adseventdashcam.domain.repository.IDeviceRepository
import com.orfeaspanagou.adseventdashcam.domain.repository.IStreamRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamRepositoryImpl @Inject constructor(
    private val deviceRepository: IDeviceRepository,
    private val streamManager: StreamManager,
) : IStreamRepository {

    override val streamState = streamManager.streamState

    override suspend fun startStream(): Result<Unit> {
        return try {
            if (!deviceRepository.isRegistered) {
                return Result.failure(Exception("Device not registered"))
            }

            deviceRepository.getCurrentLocation()?.let { location ->
                val deviceId = deviceRepository.getDeviceId()

                streamManager.startStream(deviceId,location)
                Result.success(Unit)
            } ?: Result.failure(Exception("Could not get location"))

        } catch (e: Exception) {
            Log.e(TAG, "startStream failed", e)
            Result.failure(e)
        }
    }

    override suspend fun stopStream(): Result<Unit> {
        return try {
            streamManager.stopStream()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "stopStream failed", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "StreamRepositoryImpl"
    }
}