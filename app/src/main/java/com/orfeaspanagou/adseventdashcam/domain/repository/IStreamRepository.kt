package com.orfeaspanagou.adseventdashcam.domain.repository

import io.github.thibaultbee.streampack.listeners.OnConnectionListener
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import kotlinx.coroutines.flow.StateFlow

sealed class StreamState {
    object Idle : StreamState()
    object Starting : StreamState()
    object Streaming : StreamState()
    object Stopping : StreamState()
    data class Error(val message: String) : StreamState()
}

interface IStreamRepository {
    val streamState: StateFlow<StreamState>
    suspend fun startStream(): Result<Unit>
    suspend fun stopStream(): Result<Unit>
    suspend fun initializeStreamer(onErrorListener: OnErrorListener, onConnectionListener: OnConnectionListener):Result<Unit>
}