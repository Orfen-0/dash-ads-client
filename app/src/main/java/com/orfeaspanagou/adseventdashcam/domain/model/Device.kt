package com.orfeaspanagou.adseventdashcam.domain.model

data class Device(
    val deviceId: String,
    val model: String,
    val manufacturer: String,
    val osVersion: String
)