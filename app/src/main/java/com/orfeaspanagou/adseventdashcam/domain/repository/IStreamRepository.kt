package com.orfeaspanagou.adseventdashcam.domain.repository

import kotlinx.coroutines.flow.StateFlow

enum class StreamState {
    IDLE,
    STARTING,
    STREAMING,
    STOPPING,
    ERROR
}

interface IStreamRepository {
    val streamState: StateFlow<StreamState>
    suspend fun startStream(): Result<Unit>
    suspend fun stopStream(): Result<Unit>
}