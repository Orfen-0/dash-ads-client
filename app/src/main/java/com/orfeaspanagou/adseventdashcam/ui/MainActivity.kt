package com.orfeaspanagou.adseventdashcam.ui

import PermissionUtils
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orfeaspanagou.adseventdashcam.R
import com.orfeaspanagou.adseventdashcam.domain.repository.StreamState
import com.orfeaspanagou.adseventdashcam.ui.theme.ADSEventDashcamTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADSEventDashcamTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainComposable(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
        PermissionUtils.checkAndRequestPermissions(this)
    }
}

@Composable
fun MainComposable(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val streamState by viewModel.streamState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        RegisterDevice(
            uiState = uiState,
            onRegisterClick = { viewModel.registerDevice() }
        )
        Spacer(modifier = Modifier.height(16.dp))
        StreamCamera(
            streamState = streamState,
            onStreamClick = {
                if (streamState == StreamState.Streaming) {
                    viewModel.stopStream()
                } else {
                    viewModel.startStream()
                }
            },
            enabled = uiState is UiState.Success // Only enable if device is registered
        )

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
        enabled = enabled && (streamState == StreamState.Idle || streamState == StreamState.Streaming)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.videocam),
            contentDescription = "Streaming icon",
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = when (streamState) {
                StreamState.Idle -> stringResource(R.string.stream_button_text)
                StreamState.Starting -> "Starting Stream..."
                StreamState.Streaming -> "Stop Streaming"
                StreamState.Stopping -> "Stopping Stream..."
                StreamState.Error(message = "") -> "Error"
                is StreamState.Error -> TODO()
            },
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}