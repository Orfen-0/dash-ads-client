package com.orfeaspanagou.adseventdashcam.data.repository.stream


import android.Manifest
import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Size
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.thibaultbee.streampack.data.AudioConfig
import io.github.thibaultbee.streampack.data.VideoConfig
import io.github.thibaultbee.streampack.ext.rtmp.streamers.CameraRtmpLiveStreamer
import io.github.thibaultbee.streampack.streamers.interfaces.IStreamer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class  StreamerFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configuration: StreamConfiguration
) {
    private val enableAudio: Boolean
        get() = configuration.audio

    private val videoConfig = VideoConfig(
        mimeType = MediaFormat.MIMETYPE_VIDEO_AVC,
        startBitrate = 2 * 1000, // to b/s
        resolution = Size(1280, 720),
        fps = 30,
        profile = MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline,
        level = MediaCodecInfo.CodecProfileLevel.AVCLevel1
    )

    private val audioConfig = AudioConfig(
        mimeType = MediaFormat.MIMETYPE_AUDIO_AAC,
        startBitrate = 128000,
        sampleRate = 48000,
        channelConfig = AudioConfig.getChannelConfig(2),
        profile = MediaCodecInfo.CodecProfileLevel.AACObjectLC,
        byteFormat = 2,
        enableEchoCanceler = false,
        enableNoiseSuppressor = false
    )


    private fun createStreamer(context: Context): IStreamer {
        return when {
            configuration.endpointType == EndpointType.RTMP -> {
                CameraRtmpLiveStreamer(
                    context,
                    enableAudio = enableAudio
                )
            }
            else -> {
                throw IllegalStateException("StreamerFactory: You must enable at least one of audio or video")
            }
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun build(): IStreamer {
        val streamer = createStreamer(context)

        val videoConfig = videoConfig

        val audioConfig = audioConfig

       streamer.configure(videoConfig)


        if (enableAudio) {
            streamer.configure(audioConfig)
        }

        return streamer
    }
}