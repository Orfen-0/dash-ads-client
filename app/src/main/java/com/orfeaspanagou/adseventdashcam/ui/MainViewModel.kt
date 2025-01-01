package com.orfeaspanagou.adseventdashcam.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orfeaspanagou.adseventdashcam.domain.model.Location
import com.orfeaspanagou.adseventdashcam.domain.repository.IDeviceRepository
import com.orfeaspanagou.adseventdashcam.domain.repository.IStreamRepository
import com.orfeaspanagou.adseventdashcam.domain.repository.StreamState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.thibaultbee.streampack.error.StreamPackError
import io.github.thibaultbee.streampack.listeners.OnConnectionListener
import io.github.thibaultbee.streampack.listeners.OnErrorListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val deviceRepository: IDeviceRepository,
    private val streamRepository: IStreamRepository
) : ViewModel() {

    val streamerError = MutableLiveData<String>()


    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location = _location.asStateFlow()

    val streamState = streamRepository.streamState

    init {
        viewModelScope.launch {
            try {
                // Call an API method to check registration status
                val registrationResponse = deviceRepository.checkRegistrationStatus()
                _uiState.value = if (registrationResponse) UiState.Success else UiState.Initial
            } catch (e: Exception) {
                // If API call fails, default to Initial state
                _uiState.value = UiState.Initial
                println("Registration status check failed: ${e.message}")
            }
        }


        // Your existing location observation
        viewModelScope.launch {
            try {
                deviceRepository.observeLocation().collect { newLocation ->
                    _location.value = newLocation
                }
            } catch (e: Exception) {
                println("Failed to observe location: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun registerDevice() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                deviceRepository.registerDevice()
                    .onSuccess {
                        _uiState.value = UiState.Success
                    }
                    .onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Registration failed")
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Could not connect to server")
            }
            createStreamer();
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun createStreamer() {
        viewModelScope.launch {
            try {
                streamRepository.initializeStreamer(onErrorListener,onConnectionListener);
                Log.d(TAG, "Streamer is created")
            } catch (e: Throwable) {
                Log.e(TAG, "createStreamer failed", e)
                streamerError.postValue("createStreamer: ${e.message ?: "Unknown error"}")
            }
        }
    }

    private val onErrorListener = object : OnErrorListener {
        override fun onError(error: StreamPackError) {
            Log.e(TAG, "onError", error)
            streamerError.postValue("${error.javaClass.simpleName}: ${error.message}")
        }
    }

    private val onConnectionListener = object : OnConnectionListener {
        override fun onLost(message: String) {
            streamerError.postValue("Connection lost: $message")
        }

        override fun onFailed(message: String) {
            // Not needed as we catch startStream
        }

        override fun onSuccess() {
            Log.i(TAG, "Connection succeeded")
        }
    }


    fun startStream() {
        viewModelScope.launch {
            try {
                if (!deviceRepository.isRegistered) {
                    _uiState.value = UiState.Error("Please register device first")
                    return@launch
                }

                streamRepository.startStream()
                    .onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Failed to start streaming")
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Could not connect to streaming service")
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
}

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}