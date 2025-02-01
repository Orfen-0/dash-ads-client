package com.orfeaspanagou.adseventdashcam.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.orfeaspanagou.adseventdashcam.data.config.ConfigKeys
import com.orfeaspanagou.adseventdashcam.data.config.StreamConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val configFlow: Flow<StreamConfiguration> = dataStore.data.map { prefs ->
        StreamConfiguration(
            audio = prefs[ConfigKeys.AUDIO] ?: false,
            rtmpEndpoint = prefs[ConfigKeys.RTMP_ENDPOINT] ?: "rtmp://192.168.1.77:1935/live/stream",
            httpEndpoint = prefs[ConfigKeys.HTTP_ENDPOINT] ?: "http://192.168.1.77:8080/",
            mqttBrokerUrl = prefs[ConfigKeys.MQTT_BROKER_URL] ?: "mqtt://192.168.1.77:1883",
            fileEndpoint = "path",
            bitrate = prefs[ConfigKeys.BITRATE] ?: 2000,
            resolutionWidth = prefs[ConfigKeys.RES_WIDTH] ?: 1280,
            resolutionHeight = prefs[ConfigKeys.RES_HEIGHT] ?: 720,
            fps = prefs[ConfigKeys.FPS] ?: 30
        )

    }

    suspend fun saveConfig(newConfig: StreamConfiguration) {
        dataStore.edit { prefs ->
            prefs[ConfigKeys.AUDIO] = newConfig.audio
            prefs[ConfigKeys.RTMP_ENDPOINT] = newConfig.rtmpEndpoint
            prefs[ConfigKeys.HTTP_ENDPOINT] = newConfig.httpEndpoint
            prefs[ConfigKeys.FILE_ENDPOINT] = newConfig.fileEndpoint
            prefs[ConfigKeys.BITRATE] = newConfig.bitrate
            prefs[ConfigKeys.RES_WIDTH] = newConfig.resolutionWidth
            prefs[ConfigKeys.RES_HEIGHT] = newConfig.resolutionHeight
            prefs[ConfigKeys.FPS] = newConfig.fps
        }
    }
}
