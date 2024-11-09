package com.orfeaspanagou.adseventdashcam

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // This annotation tells Hilt to set up dependency injection
class ADSEventDashcamApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // This is where you can initialize other app-wide things if needed
        // For example:
        // - Crash reporting
        // - Analytics
        // - Logging
        // - Other libraries that need initialization
    }
}