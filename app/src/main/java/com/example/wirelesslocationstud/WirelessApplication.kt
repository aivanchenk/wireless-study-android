package com.example.wirelesslocationstud

import android.app.Application
import android.util.Log
import com.example.wirelesslocationstud.data.worker.MapSyncScheduler

class WirelessApplication : Application() {

    companion object {
        private const val TAG = "WirelessApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application starting...")

        // Schedule first-time map sync
        MapSyncScheduler.scheduleFirstTimeSync(this)
        Log.d(TAG, "Map sync scheduled for first launch")
    }
}

