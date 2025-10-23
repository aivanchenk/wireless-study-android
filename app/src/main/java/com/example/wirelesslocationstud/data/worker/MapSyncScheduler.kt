package com.example.wirelesslocationstud.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object MapSyncScheduler {
    /**
     * Schedule map sync on first launch
     * This will only run once if not already completed
     */
    fun scheduleFirstTimeSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<MapSyncWorker>()
            .setConstraints(constraints)
            .build()

        // Use KEEP policy to avoid duplicate work
        WorkManager.getInstance(context).enqueueUniqueWork(
            MapSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    /**
     * Force refresh map data from API
     * This will cancel any existing work and start a new sync
     */
    fun forceRefresh(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<MapSyncWorker>()
            .setConstraints(constraints)
            .addTag("force_refresh")
            .build()

        // Use REPLACE policy to force a new sync even if one is already queued
        WorkManager.getInstance(context).enqueueUniqueWork(
            "${MapSyncWorker.WORK_NAME}_force",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }
}

