package com.orfeaspanagou.adseventdashcam.domain.repository

import com.orfeaspanagou.adseventdashcam.data.config.StreamConfiguration
import io.github.thibaultbee.streampack.listeners.OnConnectionListener
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import io.github.thibaultbee.streampack.views.PreviewView
import kotlinx.coroutines.flow.StateFlow

sealed class StreamState {
    object Idle : StreamState()
    object Ready: StreamState()
    object Offline: StreamState()
    object Starting : StreamState()
    object Streaming : StreamState()
    object Stopping : StreamState()
    data class Error(val message: String) : StreamState()
}

interface IStreamRepository {
    val streamState: StateFlow<StreamState>
    suspend fun startStream(eventId:String): Result<Unit>
    suspend fun attachPreview(previewView: PreviewView)
    suspend fun stopStream(): Result<Unit>
    suspend fun initializeStreamer(configuration: StreamConfiguration,onErrorListener: OnErrorListener, onConnectionListener: OnConnectionListener):Result<Unit>
}