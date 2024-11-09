package com.orfeaspanagou.adseventdashcam.data.repository

import com.orfeaspanagou.adseventdashcam.domain.repository.IDeviceRepository
import com.orfeaspanagou.adseventdashcam.domain.repository.IStreamRepository
import com.orfeaspanagou.adseventdashcam.domain.repository.StreamState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamRepositoryImpl @Inject constructor(
    private val deviceRepository: IDeviceRepository
) : IStreamRepository {

    private val _streamState = MutableStateFlow(StreamState.IDLE)
    override val streamState: StateFlow<StreamState> = _streamState.asStateFlow()

    override suspend fun startStream(): Result<Unit> {
        return try {
            // Check if device is registered first
            if (!deviceRepository.isRegistered) {
                return Result.failure(Exception("Device not registered"))
            }

            _streamState.value = StreamState.STARTING

            // Get current location for stream metadata
            deviceRepository.getCurrentLocation()?.let { location ->
                // TODO: Initialize RTMP client with location metadata
                // This is where you'll add your RTMP streaming logic

                _streamState.value = StreamState.STREAMING
                Result.success(Unit)
            } ?: Result.failure(Exception("Could not get location"))

        } catch (e: Exception) {
            _streamState.value = StreamState.ERROR
            Result.failure(e)
        }
    }

    override suspend fun stopStream(): Result<Unit> {
        return try {
            if (_streamState.value != StreamState.STREAMING) {
                return Result.failure(Exception("Not streaming"))
            }

            _streamState.value = StreamState.STOPPING

            // TODO: Stop RTMP stream
            // This is where you'll add your RTMP stop logic

            _streamState.value = StreamState.IDLE
            Result.success(Unit)
        } catch (e: Exception) {
            _streamState.value = StreamState.ERROR
            Result.failure(e)
        }
    }
}