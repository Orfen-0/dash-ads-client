package com.orfeaspanagou.adseventdashcam

import PermissionUtils
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.orfeaspanagou.adseventdashcam.ui.theme.ADSEventDashcamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADSEventDashcamTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RegisterDevice(id = "test",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        PermissionUtils.checkAndRequestPermissions(this)
    }
}


@Composable
fun MainComposeable(modifier: Modifier = Modifier){
    Column (
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RegisterDevice(id = "test");
        StreamCamera();
    }

}

@Composable
fun RegisterDevice(id:String, modifier: Modifier = Modifier){
        Button(onClick = { /*TODO*/ }) {
            Text(stringResource(R.string.register_button_text))
        }
}

@Composable
fun StreamCamera(){

}
