package com.orfeaspanagou.adseventdashcam.domain.model


data class Stream(
    val id: String,
    val deviceId: String,
    val startTime: Long,
    val status: StreamStatus,
    val metadata: StreamMetadata
)

data class StreamMetadata(
    val location: Location,
    val resolution: String,
    val bitrate: Int,
    val fps: Int
)

enum class StreamStatus {
    PREPARING,
    STREAMING,
    PAUSED,
    STOPPED,
    ERROR
}