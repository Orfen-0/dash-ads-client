package com.orfeaspanagou.adseventdashcam.data.repository.stream

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Range
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.createVideoMediaOutputStream(name: String): OutputStream? {
    val fileName = generateTimestampedFileName(name);
    val videoDetails = ContentValues().apply {
        put(MediaStore.Video.Media.TITLE, name)
        put(
            MediaStore.Video.Media.DISPLAY_NAME,
            name
        )
    }

    val resolver = this.contentResolver
    val collection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

    val video = resolver.insert(collection, videoDetails)
        ?: throw Exception("Unable to create video file")

    return resolver.openOutputStream(video)
}

val Range<*>.isEmpty: Boolean
    get() = upper == lower


fun generateTimestampedFileName(
    prefix: String = "recording",
    extension: String = ".mp4"
): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return "${prefix}_$timestamp$extension"
}