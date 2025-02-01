package com.orfeaspanagou.adseventdashcam.data.config

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

data class StreamConfiguration(
    var audio: Boolean = true,
    var endpointType: EndpointType = EndpointType.RTMP,
    var mqttBrokerUrl: String = "mqtt://192.168.1.77:1883",
    var rtmpEndpoint: String = "rtmp://192.168.1.77:1935/live/stream",
    var httpEndpoint: String = "http://192.168.1.77:8080/",
    var fileEndpoint: String = "/path/to/default.mp4",
    var streamingEnabled: Boolean = false,
    var bitrate: Int = 2000000, // in kb/s or b/s depending on your usage
    var resolutionWidth: Int = 1280,
    var resolutionHeight: Int = 720,
    var fps: Int = 30,
)

object ConfigKeys {
    val AUDIO = booleanPreferencesKey("audio")
    val RTMP_ENDPOINT = stringPreferencesKey("rtmpEndpoint")
    val HTTP_ENDPOINT = stringPreferencesKey("httpEndpoint")
    val MQTT_BROKER_URL = stringPreferencesKey("mqttBrokerUrl")
    val FILE_ENDPOINT = stringPreferencesKey("fileEndpoint")
    val BITRATE = intPreferencesKey("bitrate")
    val RES_WIDTH = intPreferencesKey("resolutionWidth")
    val RES_HEIGHT = intPreferencesKey("resolutionHeight")
    val FPS = intPreferencesKey("fps")
}

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