package com.orfeaspanagou.adseventdashcam.data.manager.stream


import android.content.Context
import com.orfeaspanagou.adseventdashcam.data.repository.stream.StreamConfiguration
import com.orfeaspanagou.adseventdashcam.data.repository.stream.createVideoMediaOutputStream
import com.orfeaspanagou.adseventdashcam.domain.model.Location
import com.orfeaspanagou.adseventdashcam.domain.repository.StreamState
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.thibaultbee.streampack.streamers.interfaces.IStreamer
import io.github.thibaultbee.streampack.utils.getFileStreamer
import io.github.thibaultbee.streampack.utils.getLiveStreamer
import isConnected
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class StreamManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configuration: StreamConfiguration

) {
    private var streamer: IStreamer? = null


    private val _streamState = MutableStateFlow<StreamState>(StreamState.Idle)
    val streamState = _streamState.asStateFlow()



    suspend fun startStream(deviceId: String, currentLocation: Location) {
        _streamState.value = StreamState.Starting
        val isConnected = isConnected(context)
        try {
            // Decide whether to stream or record based on connectivity
            if (isConnected) {
                val rtmpUrlWithParams = buildString {
                    append(configuration.rtmpEndpoint)
                    append("?deviceId="); append(deviceId)
                    append("&lat=");      append(currentLocation.latitude)
                    append("&lng=");      append(currentLocation.longitude)
                    append("&acc=");      append(currentLocation.accuracy)
                    append("&ts=");       append(currentLocation.timestamp)
                }

                streamer?.getLiveStreamer()?.connect(rtmpUrlWithParams)
            } else {
                streamer?.getFileStreamer()?.let {
                    it.outputStream = context.createVideoMediaOutputStream(configuration.fileEndpoint)
                        ?: throw Exception("Unable to create video output stream")
                }
            }
            streamer?.startStream()
            _streamState.value = StreamState.Streaming
        } catch (e: Exception) {
            _streamState.value = StreamState.Error(e.message ?: "Unknown error")
        }
    }

    fun stopStream() {
        _streamState.value = StreamState.Stopping
        runBlocking {
            streamer?.stopStream()
        }
        streamer?.getLiveStreamer()?.disconnect()
        _streamState.value = StreamState.Idle
    }

}

