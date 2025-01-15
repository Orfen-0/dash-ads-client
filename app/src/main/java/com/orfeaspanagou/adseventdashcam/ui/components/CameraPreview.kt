package com.orfeaspanagou.adseventdashcam.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.thibaultbee.streampack.views.PreviewView

/**
 * A composable that hosts the StreamPack PreviewView within a Compose UI.
 * When created, it calls [onPreviewCreated] so you can bind your camera streamer.
 */
@Composable
fun CameraPreview(
    onPreviewCreated: (previewView: PreviewView) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context)
            Log.d("CameraPreview", "PreviewView created; calling onPreviewCreated")
            onPreviewCreated(previewView)
            previewView
        }
    )
}



