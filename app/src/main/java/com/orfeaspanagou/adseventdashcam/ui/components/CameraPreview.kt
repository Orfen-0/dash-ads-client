package com.orfeaspanagou.adseventdashcam.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.github.thibaultbee.streampack.views.PreviewView

/**
 * A composable that hosts the StreamPack PreviewView within a Compose UI.
 * When created, it calls [onPreviewCreated] so you can bind your camera streamer.
 */
@Composable
fun CameraPreview(
    onPreviewCreated: (PreviewView) -> Unit,
    modifier: Modifier = Modifier
) {
    // Use a simple integer key that increments on resume.
    var recreateKey by remember { mutableStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        // Create a simple lifecycle observer that increments the key on resume.
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                recreateKey++ // trigger a new key to force a recomposition
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Wrap AndroidView in a key block so that it recreates whenever recreateKey changes.
    key(recreateKey) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                PreviewView(context).also { previewView ->
                    onPreviewCreated(previewView)
                }
            }
        )
    }
}



