package com.example.wirelesslocationstud.data.worker

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapSyncWorkerTest {

    companion object {
        private const val TAG = "MapSyncWorkerTest"
    }

    @Before
    fun setup() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(
            ApplicationProvider.getApplicationContext(),
            config
        )
    }

    @Test
    fun testMapSyncWorker() = runBlocking {
        Log.d(TAG, "=== TEST: MapSyncWorker ===")

        val worker = TestListenableWorkerBuilder<MapSyncWorker>(
            ApplicationProvider.getApplicationContext()
        ).build()

        Log.d(TAG, "Starting MapSyncWorker...")
        val result = worker.doWork()

        Log.d(TAG, "Worker result: $result")

        when {
            result is androidx.work.ListenableWorker.Result.Success -> {
                Log.d(TAG, "✓ Worker completed successfully!")
            }
            result is androidx.work.ListenableWorker.Result.Retry -> {
                Log.d(TAG, "⚠ Worker will retry (this is OK if API is not available)")
            }
            else -> {
                Log.e(TAG, "✗ Worker failed")
            }
        }

        Log.d(TAG, "=== TEST COMPLETED ===")
    }
}

