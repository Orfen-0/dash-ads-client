package com.orfeaspanagou.adseventdashcam.ui

import PermissionUtils
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.core.app.ActivityCompat
import com.orfeaspanagou.adseventdashcam.R
import com.orfeaspanagou.adseventdashcam.domain.repository.StreamState
import com.orfeaspanagou.adseventdashcam.ui.theme.ADSEventDashcamTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(),PermissionUtils.PermissionListener {
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

    override fun onPermissionsGranted() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        viewModel.createStreamer();
        viewModel.registerDevice()
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
            enabled = (uiState is UiState.Success) && (
                    streamState == StreamState.Ready ||
                            streamState == StreamState.Streaming)
        )
        CameraPreview(
            onPreviewCreated = { previewView ->
                viewModel.attachPreview(previewView)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)  // partial bottom preview, for example
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
                else -> "Unknown state"
            },
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}