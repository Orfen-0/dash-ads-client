package com.orfeaspanagou.adseventdashcam.ui

import PermissionUtils
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.orfeaspanagou.adseventdashcam.R
import com.orfeaspanagou.adseventdashcam.data.config.StreamConfiguration
import com.orfeaspanagou.adseventdashcam.data.managers.MqttClientManager
import com.orfeaspanagou.adseventdashcam.domain.repository.StreamState
import com.orfeaspanagou.adseventdashcam.ui.components.CameraPreview
import com.orfeaspanagou.adseventdashcam.ui.components.SettingsScreen
import com.orfeaspanagou.adseventdashcam.ui.theme.ADSEventDashcamTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(),PermissionUtils.PermissionListener {
    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADSEventDashcamTheme {
                AppRootContent(viewModel)
            }
        }
        PermissionUtils.checkAndRequestPermissions(this)




    }

    override fun onPermissionsGranted() {
        viewModel.initApp();
    }

    override fun onPermissionsDenied() {
        Toast.makeText(this, "Permissions are required to continue", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.handlePermissionResult(requestCode, permissions, grantResults, this)
    }
    override fun onResume() {
        super.onResume()
        viewModel.resetStreamerReady()
        viewModel.currentPreviewView?.let { preview ->
            // Reattach the preview to update the surface in the streamer.
            viewModel.attachPreview(preview)
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRootContent(viewModel: MainViewModel) {
    val screenState by viewModel.currentScreen.collectAsState()
    val configState by viewModel.configFlow.collectAsState()
    LaunchedEffect(configState) {
        // Rebuild streamer whenever config changes
        viewModel.reinitStreamer(configState)
    }
    when (screenState) {
        AppScreen.MAIN -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("ADS Client App") },
                        actions = {
                            // The gear icon in the top-right
                            IconButton(onClick = { viewModel.goToSettings() }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                MainComposable(
                    modifier = Modifier.padding(innerPadding),
                    viewModel = viewModel,
                )
            }
        }

        AppScreen.SETTINGS -> {
            SettingsRoute(
                streamConfiguration = configState,
                onSave = { newConfig ->
                    viewModel.saveConfig(newConfig)
                }
            )
        }
    }
}

    @Composable
    fun MainComposable(
        modifier: Modifier = Modifier,
        viewModel: MainViewModel,
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val streamState by viewModel.streamState.collectAsState()
        val isStreamerReady by viewModel.isStreamerReady.collectAsState()


        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegisterDevice(
                uiState = uiState,
                onRegisterClick = { viewModel.registerDevice() }
            )
            StreamCamera(
                streamState = streamState,
                onStreamClick = {
                    if (streamState == StreamState.Streaming) {
                        viewModel.stopStream()
                    } else {
                        viewModel.startStream()
                    }
                },
                enabled = (uiState is UiState.Success) && (
                        streamState == StreamState.Ready ||
                                streamState == StreamState.Streaming) && isStreamerReady
            )
            if (isStreamerReady) {
                CameraPreview(
                    onPreviewCreated = { previewView ->
                        viewModel.attachPreview(previewView)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                // Optionally, show a loading placeholder
                Text("Initializing streamer...", style = MaterialTheme.typography.bodyMedium)
            }


            // Show error if any
            if (uiState is UiState.Error) {
                Text(
                    text = (uiState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }

@Composable
fun RegisterDevice(
    uiState: UiState,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onRegisterClick,
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(4.dp),
        enabled = uiState == UiState.Initial || uiState is UiState.Error
    ) {
        when (uiState) {
            is UiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            is UiState.Success -> {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Registered",
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Device Registered",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Registration Icon",
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = stringResource(R.string.register_button_text),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun StreamCamera(
    streamState: StreamState,
    onStreamClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onStreamClick,
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(4.dp),
        enabled = enabled
    ) {
        Icon(
            painter = painterResource(id = R.drawable.videocam),
            contentDescription = "Streaming icon",
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = when (streamState) {
                StreamState.Idle -> stringResource(R.string.stream_button_text)
                StreamState.Ready -> "Click to start Stream"
                StreamState.Offline -> "Click to start recording (offline)"
                StreamState.Starting -> "Starting Stream..."
                StreamState.Streaming -> "Stop Streaming"
                StreamState.Stopping -> "Stopping Stream..."
                is StreamState.Error -> "Error: ${streamState.message}"
            },
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun SettingsRoute(
    streamConfiguration: StreamConfiguration,
    onSave: (StreamConfiguration) -> Unit
) {
    SettingsScreen(
        currentConfig = streamConfiguration,
        onConfigChange = { newConfig ->
            // The user tapped Save. We pass back the updated config.
            onSave(newConfig)
        }
    )
}