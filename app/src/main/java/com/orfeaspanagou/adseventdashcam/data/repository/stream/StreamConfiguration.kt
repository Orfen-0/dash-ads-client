package com.orfeaspanagou.adseventdashcam.data.repository.stream

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


data class StreamConfiguration(
    var video: Boolean = true,
    var audio: Boolean = false,
    var endpointType: EndpointType = EndpointType.RTMP,
    var rtmpEndpoint: String = "rtmp://192.168.1.77:1935/live/stream",
    var fileEndpoint: String = "/path/to/default.mp4",
    var streamingEnabled: Boolean = false
)

enum class EndpointType(val id: Int) {
    RTMP(0),
    MP4_FILE(1);
}

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @Provides
    fun provideStreamConfiguration(): StreamConfiguration {
        return StreamConfiguration()
    }
}