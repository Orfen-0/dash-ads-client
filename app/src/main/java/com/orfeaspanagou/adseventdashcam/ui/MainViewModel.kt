package com.orfeaspanagou.adseventdashcam.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orfeaspanagou.adseventdashcam.data.config.StreamConfiguration
import com.orfeaspanagou.adseventdashcam.domain.model.Location
import com.orfeaspanagou.adseventdashcam.domain.repository.IDeviceRepository
import com.orfeaspanagou.adseventdashcam.domain.repository.IStreamRepository
import com.orfeaspanagou.adseventdashcam.data.datastore.SettingsRepository
import com.orfeaspanagou.adseventdashcam.data.managers.MqttClientManager
import com.orfeaspanagou.adseventdashcam.domain.repository.StreamState
import com.orfeaspanagou.adseventdashcam.network.NetworkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.thibaultbee.streampack.error.StreamPackError
import io.github.thibaultbee.streampack.listeners.OnConnectionListener
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import io.github.thibaultbee.streampack.views.PreviewView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.stream.Stream
import javax.inject.Inject


enum class AppScreen { MAIN, SETTINGS }

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context, // Injected application context
    private val deviceRepository: IDeviceRepository,
    private val streamRepository: IStreamRepository,
    private val settingsRepository: SettingsRepository,
    private val networkManager: NetworkManager,
    private val mqttClientManager: MqttClientManager
) : ViewModel() {


    private val _isStreamerReady = MutableStateFlow(false)
    val isStreamerReady = _isStreamerReady.asStateFlow()

    var currentPreviewView: PreviewView? = null

    val configFlow = settingsRepository.configFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StreamConfiguration()
    )

    private val _currentScreen = MutableStateFlow(AppScreen.MAIN)
    val currentScreen = _currentScreen.asStateFlow()

    fun goToSettings() {
        _currentScreen.value = AppScreen.SETTINGS
    }

    fun goToMain() {
        _currentScreen.value = AppScreen.MAIN
    }

    fun resetStreamerReady() {
        _isStreamerReady.value = false
    }

    fun saveConfig(newConfig: StreamConfiguration) {
        viewModelScope.launch {
            val currentConfig = configFlow.value  // Or retrieve it via first()
            settingsRepository.saveConfig(newConfig);
            if (currentConfig.httpEndpoint != newConfig.httpEndpoint) {
                networkManager.initRetrofit(newConfig)
            }
            if(currentConfig.mqttBrokerUrl != newConfig.mqttBrokerUrl){
                val port = newConfig.mqttBrokerUrl.split(":")[2].toInt()
                mqttClientManager.reinit(newConfig.mqttBrokerUrl,port,deviceRepository.getDeviceId());
            }
            // Optionally reinit streamer if streaming settings changed:
            if (currentConfig.rtmpEndpoint != newConfig.rtmpEndpoint ||
                currentConfig.bitrate != newConfig.bitrate ||
                currentConfig.resolutionWidth != newConfig.resolutionWidth ||
                currentConfig.resolutionHeight != newConfig.resolutionHeight ||
                currentConfig.fps != newConfig.fps ||
                currentConfig.audio != newConfig.audio) {
                _isStreamerReady.value = false;
                reinitStreamer(newConfig)
            }
            goToMain()
        }
    }
    val streamerError = MutableLiveData<String>()


    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)

    val streamState = streamRepository.streamState


    init {
        viewModelScope.launch {
            try {
                val initialConfig = configFlow.first()
            } catch (e: Exception) {
                _uiState.value = UiState.Initial
                println("Registration status check failed: ${e.message}")
            }
        }


        viewModelScope.launch {
            try {
                deviceRepository.observeLocation().collect { newLocation ->
                    _location.value = newLocation
                }
            } catch (e: Exception) {
                println("Failed to observe location: ${e.message}")
            }
        }

        viewModelScope.launch {
            mqttClientManager.commandFlow.collect { commandPayload ->
                try {
                    mqttClientManager.publishLog("info","mqtt_command", "Received command ${commandPayload}")
                    if(commandPayload.command == "startStream"){
                        mqttClientManager.publishLog("info","mqtt_command", "Received startStream with eventId: ${commandPayload.eventId}")


                        if(streamRepository.streamState.value == StreamState.Ready) {
                            streamRepository.startStream(commandPayload.eventId)
                            Log.d(
                                "MainViewModel",
                                "Started stream with eventId: ${commandPayload.eventId}"
                            )
                            mqttClientManager.publishLog(
                                "info",
                                "stream_start",
                                "Stream started for eventId: ${commandPayload.eventId}"
                            )
                        }
                    }
                    if(commandPayload.command == "stopStreaming") {
                        if (streamRepository.streamState.value == StreamState.Streaming){
                            mqttClientManager.publishLog("info","stream_stop", "Stream stopped manually or by command")
                            streamRepository.stopStream()
                        }

                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Error starting stream: ${e.message}")
                }
            }
        }
    }

    fun initApp() {
        viewModelScope.launch {
            // 1) Wait for configFlow.first() or read the current configFlow.value
            val config = settingsRepository.configFlow.first()
            // 2) re-init or create your network manager, streamer, etc.

            networkManager.initRetrofit(config)
            if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                createStreamer(configFlow.value)
            } else {
                Log.d("PERMISSION", "Audio permission not granted")
            }

            val registered = deviceRepository.checkRegistrationStatus()
            if(registered){
                _uiState.value = UiState.Success
                val urlWithoutProtocol = config.mqttBrokerUrl.removePrefix("mqtt://")
                val (brokerUrl, port) =urlWithoutProtocol
                    .split(":")
                    .let { it[0] to it[1].toInt() }
                mqttClientManager.connect(
                    brokerUrl = brokerUrl,
                    brokerPort = port,
                    deviceId = deviceRepository.getDeviceId()
                )
            }
            else
                _uiState.value = UiState.Error("Please register device first")

        }
    }

    fun registerDevice() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                deviceRepository.registerDevice()
                    .onSuccess {
                        _uiState.value = UiState.Success
                        val config = configFlow.value
                        val urlWithoutProtocol = config.mqttBrokerUrl.removePrefix("mqtt://")
                        val (brokerUrl, port) =urlWithoutProtocol
                            .split(":")
                            .let { it[0] to it[1].toInt() }
                        mqttClientManager.connect(
                            brokerUrl = brokerUrl,
                            brokerPort = port,
                            deviceId = deviceRepository.getDeviceId()
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Registration failed")
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Could not connect to server")
            }
        }
    }


    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun createStreamer(configuration: StreamConfiguration) {
        viewModelScope.launch {
            try {
                streamRepository.initializeStreamer(configuration,onErrorListener,onConnectionListener);
                Log.d("STREAMER", "Streamer is created")
            } catch (e: Throwable) {
                Log.d("STREAMER", "createStreamer failed", e)
                streamerError.postValue("createStreamer: ${e.message ?: "Unknown error"}")
            }
            _isStreamerReady.value = true;
        }
    }

    private val onErrorListener = object : OnErrorListener {
        override fun onError(error: StreamPackError) {
            Log.d("STREAMING", "onError", error)
            streamerError.postValue("${error.javaClass.simpleName}: ${error.message}")
        }
    }

    private val onConnectionListener = object : OnConnectionListener {
        override fun onLost(message: String) {
            Log.d("STREAMING", "Connection lost: $message")
            streamerError.postValue("Connection lost: $message")
        }

        override fun onFailed(message: String) {
            Log.d("STREAMING", message)
        }

        override fun onSuccess() {
            Log.d("STREAMING", "Connection succeeded")
        }
    }

    fun attachPreview(previewView: PreviewView) {
        currentPreviewView = previewView
        viewModelScope.launch {
            streamRepository.attachPreview(previewView)
            _isStreamerReady.value = true
        }
    }


    fun startStream() {
        viewModelScope.launch {
            try {
                if (!deviceRepository.isRegistered) {
                    _uiState.value = UiState.Error("Please register device first")
                    return@launch
                }
                val eventId = "user-triggered-event";
                streamRepository.startStream(eventId)
                    .onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Failed to start streaming")
                        Log.d("STREAMING", "Failed to start stream",error)
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Could not connect to streaming service")
                Log.d("STREAMING", "Failed to start stream",e)
            }
        }
    }

    fun stopStream() {
        viewModelScope.launch {
            try {
                streamRepository.stopStream()
                    .onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Failed to stop streaming")
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Could not connect to streaming service")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun reinitStreamer(newConfig: StreamConfiguration) {
        createStreamer(newConfig)
    }
}

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}