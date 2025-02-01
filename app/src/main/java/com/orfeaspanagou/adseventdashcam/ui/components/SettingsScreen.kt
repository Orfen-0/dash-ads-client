package com.orfeaspanagou.adseventdashcam.ui.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orfeaspanagou.adseventdashcam.data.config.StreamConfiguration

@Composable
fun SettingsScreen(
    currentConfig: StreamConfiguration,
    onConfigChange: (StreamConfiguration) -> Unit
) {
    var tempConfig by remember { mutableStateOf(currentConfig) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))
        Text("RTMP URL")
        OutlinedTextField(
            value = tempConfig.rtmpEndpoint,
            onValueChange = { tempConfig = tempConfig.copy(rtmpEndpoint = it) }
        )

        Spacer(Modifier.height(16.dp))
        Text("HTTP URL")
        OutlinedTextField(
            value = tempConfig.httpEndpoint,
            onValueChange = { tempConfig = tempConfig.copy(httpEndpoint = it) }
        )

        Spacer(Modifier.height(16.dp))
        Text("MQTT BROKER URL")
        OutlinedTextField(
            value = tempConfig.mqttBrokerUrl,
            onValueChange = { tempConfig = tempConfig.copy(mqttBrokerUrl = it) }
        )

        Spacer(Modifier.height(16.dp))
        Text("Default Offline Storage Path")
        OutlinedTextField(
            value = tempConfig.fileEndpoint,
            onValueChange = { tempConfig = tempConfig.copy(fileEndpoint = it) }
        )

        Spacer(Modifier.height(16.dp))
        Text("Bitrate (kbps)")
        OutlinedTextField(
            value = tempConfig.bitrate.toString(),
            onValueChange = {
                val b = it.toIntOrNull() ?: tempConfig.bitrate
                tempConfig = tempConfig.copy(bitrate = b)
            }
        )

        Spacer(Modifier.height(16.dp))
        Text("Resolution (Width x Height)")
        Row {
            OutlinedTextField(
                value = tempConfig.resolutionWidth.toString(),
                onValueChange = {
                    val w = it.toIntOrNull() ?: tempConfig.resolutionWidth
                    tempConfig = tempConfig.copy(resolutionWidth = w)
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = tempConfig.resolutionHeight.toString(),
                onValueChange = {
                    val h = it.toIntOrNull() ?: tempConfig.resolutionHeight
                    tempConfig = tempConfig.copy(resolutionHeight = h)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))
        Text("FPS")
        OutlinedTextField(
            value = tempConfig.fps.toString(),
            onValueChange = {
                val f = it.toIntOrNull() ?: tempConfig.fps
                tempConfig = tempConfig.copy(fps = f)
            }
        )

        Spacer(Modifier.height(16.dp))
        Row {

            Checkbox(
                checked = tempConfig.audio,
                onCheckedChange = { newVal -> tempConfig = tempConfig.copy(audio = newVal) }
            )
            Text("Enable Audio")
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = { onConfigChange(tempConfig) }) {
            Text("Save & Back")
        }
    }
}