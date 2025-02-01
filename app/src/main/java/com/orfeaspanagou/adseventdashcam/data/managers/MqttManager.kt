// MqttClientManager.kt
package com.orfeaspanagou.adseventdashcam.data.managers


import android.util.Log
import com.google.gson.Gson
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Singleton

data class LocationPayload(
    val deviceId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)

/**
 * A simple manager for HiveMQ MQTT Client on Android.
 */
@Singleton
class MqttClientManager @Inject constructor(){

    private val tag = "HiveMqttClientManager"
    private val gson = Gson()

    // Our async MQTT5 client
    private var client: Mqtt5AsyncClient? = null

    /**
     * Connect to the MQTT broker with auto-reconnect enabled.
     * e.g. brokerUrl = "192.168.1.77", brokerPort = 1883, clientId = "myAndroidDevice"
     */
    fun connect(brokerUrl: String, brokerPort: Int, deviceId: String): CompletableFuture<Mqtt5ConnAck>? {
        if (client != null && client!!.state.isConnectedOrReconnect) {
            Log.d(tag, "Already connected or reconnecting.")
            return null
        }
        // build an async MQTT 5 client
        client = MqttClient.builder()
            .useMqttVersion5()
            .identifier(deviceId)
            .serverHost(brokerUrl)
            .serverPort(brokerPort)
            .automaticReconnectWithDefaultConfig() // auto reconnect
            .buildAsync()

        val connectFuture = client!!.connectWith()
            .cleanStart(true)
            .keepAlive(60) // e.g. 60 seconds keepalive
            .send()

        connectFuture.whenComplete { ack, throwable ->
            if (throwable != null) {
                // handle error
                if (throwable is ConnectionFailedException) {
                    Log.e(tag, "Failed to connect to broker: ${throwable.message}")
                } else {
                    Log.e(tag, "Unknown error connecting to broker: $throwable")
                }
            } else {
                Log.d(tag, "Connected! ack=$ack")
                subscribeToTopic("devices/${deviceId}/cmd")
            }
        }
        return connectFuture
    }

    /**
     * Publish location data as JSON to `devices/<deviceId>/location`
     */
    fun publishLocationUpdate(payload: LocationPayload) {
        val localClient = client
        if (localClient == null || !localClient.state.isConnected) {
            Log.e(tag, "MQTT client not connected; cannot publish location.")
            return
        }

        val topic = "devices/${payload.deviceId}/location"
        val jsonString = gson.toJson(payload)
        val publishFuture = localClient.publishWith()
            .topic(topic)
            .payload(jsonString.toByteArray(StandardCharsets.UTF_8))
            .qos(MqttQos.AT_MOST_ONCE)
            .send()

        publishFuture.whenComplete { publishResult, pubEx ->
            if (pubEx != null) {
                Log.e(tag, "Error publishing location to $topic: ${pubEx.message}")
            } else {
                Log.d(tag, "Location published to $topic: $publishResult")
            }
        }
    }

    /**
     * Optionally subscribe to commands or ack topics
     */
    fun subscribeToTopic(topicFilter: String) {
        val localClient = client ?: return
        localClient.subscribeWith()
            .topicFilter(topicFilter)
            .callback { publish ->
                val receivedTopic = publish.topic.toString()
                val payloadString = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
                Log.d(tag, "Received message on $receivedTopic: $payloadString")
                if(topicFilter.endsWith("cmd")){
                    handleCommand(payloadString)
                }
            }
            .send()
            .whenComplete { subAck, ex ->
                if (ex != null) {
                    Log.e(tag, "Subscribe error on $topicFilter: ${ex.message}")
                } else {
                    Log.d(tag, "Subscribed to $topicFilter successfully: $subAck")
                }
            }
    }

    fun handleCommand(payload: String) {
        Log.d(tag, "Received $payload")
    }

    fun disconnect() {
        val localClient = client
        if (localClient != null && localClient.state.isConnected) {
            localClient.disconnect()
                .whenComplete { _, ex ->
                    if (ex != null) {
                        Log.e(tag, "Error on disconnect: ${ex.message}")
                    } else {
                        Log.d(tag, "Disconnected successfully")
                    }
                }
        }
    }

    fun reinit(mqttBrokerUrl: String,brokerPort: Int, deviceId: String){
        disconnect();
        connect(mqttBrokerUrl,brokerPort,deviceId)
    }
}