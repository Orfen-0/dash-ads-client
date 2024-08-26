package com.orfeaspanagou.adseventdashcam

import PermissionUtils
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.orfeaspanagou.adseventdashcam.ui.theme.ADSEventDashcamTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADSEventDashcamTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainComposable(modifier = Modifier.padding(innerPadding))
                }
            }
        }
        PermissionUtils.checkAndRequestPermissions(this)
    }
}

@Preview
@Composable
fun MainComposable(modifier: Modifier = Modifier){
    Column(
        modifier = modifier
            .fillMaxSize() // Fill the available space
            .padding(16.dp),
        verticalArrangement = Arrangement.Top, // Center content vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        RegisterDevice(id = "test")
        Spacer(modifier = Modifier.height(16.dp))
        StreamCamera()
    }

}


@Composable
fun RegisterDevice(id: String, modifier: Modifier = Modifier) {
    Button(
        onClick = { /*TODO: Execute your code */ },
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
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

@Composable
fun StreamCamera(modifier: Modifier = Modifier) {
    Button(
        onClick = { /*TODO: Execute your code */ },
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.videocam),
            contentDescription = "Streaming icon",
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = stringResource(R.string.stream_button_text),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
