package com.example.wirelesslocationstud.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.room.Room
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.remote.api.RetrofitClient
import com.example.wirelesslocationstud.data.repository.WirelessMapRepository

class MapSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "MapSyncWorker"
        const val WORK_NAME = "map_sync_work"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "MapSyncWorker started...")

        return try {
            // Initialize database and repository
            val database = Room.databaseBuilder(
                applicationContext,
                WirelessDatabase::class.java,
                WirelessDatabase.DATABASE_NAME
            ).build()

            val repository = WirelessMapRepository(
                api = RetrofitClient.wirelessMapApi,
                mapCellDao = database.mapCellDao(),
                mapMetadataDao = database.mapMetadataDao()
            )

            // Check if data is already cached
            if (repository.isMapCached()) {
                Log.d(TAG, "Map data already cached, skipping sync")
                return Result.success()
            }

            // Fetch and cache the map
            Log.d(TAG, "Fetching map data from API...")
            val result = repository.fetchAndCacheMap()

            if (result.isSuccess) {
                Log.d(TAG, "Map sync completed successfully!")
                Result.success()
            } else {
                Log.e(TAG, "Map sync failed: ${result.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MapSyncWorker error: ${e.message}", e)
            Result.retry()
        }
    }
}

