package com.orfeaspanagou.adseventdashcam.data.manager.stream


import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import com.orfeaspanagou.adseventdashcam.data.repository.stream.StreamConfiguration
import com.orfeaspanagou.adseventdashcam.data.repository.stream.StreamerFactory
import com.orfeaspanagou.adseventdashcam.data.repository.stream.createVideoMediaOutputStream
import com.orfeaspanagou.adseventdashcam.domain.model.Location
import com.orfeaspanagou.adseventdashcam.domain.repository.StreamState
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.thibaultbee.streampack.listeners.OnConnectionListener
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import io.github.thibaultbee.streampack.streamers.StreamerLifeCycleObserver
import io.github.thibaultbee.streampack.streamers.interfaces.IStreamer
import io.github.thibaultbee.streampack.utils.getCameraStreamer
import io.github.thibaultbee.streampack.utils.getFileStreamer
import io.github.thibaultbee.streampack.utils.getLiveStreamer
import io.github.thibaultbee.streampack.views.PreviewView
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


    var onErrorListener: OnErrorListener?
        get() = streamer?.onErrorListener
        set(value) {
            streamer?.onErrorListener = value
        }

    var onConnectionListener: OnConnectionListener?
        get() = streamer?.getLiveStreamer()?.onConnectionListener
        set(value) {
            streamer?.getLiveStreamer()?.onConnectionListener = value
        }

    val cameraId: String?
        get() = streamer?.getCameraStreamer()?.camera

    val streamerLifeCycleObserver: StreamerLifeCycleObserver by lazy {
        StreamerLifeCycleObserver(streamer!!)
    }


    val requiredPermissions: List<String>
        get() {
            val permissions = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
            streamer?.getFileStreamer()?.let {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            return permissions
        }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun rebuildStreamer() {
        streamer = StreamerFactory(context, configuration).build()
    }

    fun inflateStreamerView(view: PreviewView) {
        view.streamer = streamer?.getCameraStreamer()
        _streamState.value = StreamState.Idle
    }


    suspend fun startStream(deviceId: String, currentLocation: Location) {
        if (_streamState.value != StreamState.Idle) {
            _streamState.value = StreamState.Error("Must open preview before starting stream")
            return
        }
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

