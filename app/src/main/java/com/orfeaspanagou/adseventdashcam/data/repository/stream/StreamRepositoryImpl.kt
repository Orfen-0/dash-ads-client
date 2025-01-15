package com.orfeaspanagou.adseventdashcam.data.repository


import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.orfeaspanagou.adseventdashcam.data.config.StreamConfiguration
import com.orfeaspanagou.adseventdashcam.data.managers.stream.StreamManager
import com.orfeaspanagou.adseventdashcam.domain.repository.IDeviceRepository
import com.orfeaspanagou.adseventdashcam.domain.repository.IStreamRepository
import io.github.thibaultbee.streampack.listeners.OnConnectionListener
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import io.github.thibaultbee.streampack.views.PreviewView
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

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override suspend fun initializeStreamer(
        configuration: StreamConfiguration,
        onErrorListener: OnErrorListener,
        onConnectionListener: OnConnectionListener
    ): Result<Unit> {
        return try {
            streamManager.rebuildStreamer(configuration)
            streamManager.onErrorListener = onErrorListener
            streamManager.onConnectionListener = onConnectionListener

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "stream initialization failed", e)
            Result.failure(e)
        }
    }

    override suspend fun attachPreview(previewView: PreviewView) {
        streamManager.inflateStreamerView(previewView)
    }


    companion object {
        private const val TAG = "StreamRepositoryImpl"
    }
}