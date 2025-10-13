package com.example.wirelesslocationstud.data.remote

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.remote.api.RetrofitClient
import com.example.wirelesslocationstud.data.repository.WirelessMapRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WirelessApiIntegrationTest(private val context: Context) {
    private val TAG = "WirelessApiTest"

    private lateinit var database: WirelessDatabase
    private lateinit var repository: WirelessMapRepository

    suspend fun runTest() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "Starting API Integration Test")
                Log.d(TAG, "========================================")

                // Initialize database and repository
                initRepository()

                // Test 1: Check if API is accessible
                testApiConnection()

                // Test 2: Fetch and cache complete map
                testFetchAndCacheMap()

                // Test 3: Verify cached data
                testVerifyCachedData()

                // Test 4: Fetch specific column (x=2)
                testFetchSpecificColumn(2)

                // Test 5: Verify complete rectangular map
                testVerifyRectangularMap()

                // Test 6: Verify cache status
                testCacheStatus()

                Log.d(TAG, "========================================")
                Log.d(TAG, "All API integration tests completed!")
                Log.d(TAG, "========================================")

            } catch (e: Exception) {
                Log.e(TAG, "Test failed with error: ${e.message}", e)
                Log.e(TAG, "Stack trace:", e)
            } finally {
                closeDatabase()
            }
        }
    }

    private fun initRepository() {
        Log.d(TAG, "\n--- Initializing Database and Repository ---")
        database = Room.databaseBuilder(
            context,
            WirelessDatabase::class.java,
            "test_api_wireless_map.db"
        ).build()

        repository = WirelessMapRepository(
            api = RetrofitClient.wirelessMapApi,
            mapCellDao = database.mapCellDao(),
            mapMetadataDao = database.mapMetadataDao()
        )
        Log.d(TAG, "Repository initialized successfully")
    }

    private suspend fun testApiConnection() {
        Log.d(TAG, "\n--- Test 1: Testing API Connection ---")
        try {
            Log.d(TAG, "Attempting to fetch map size from API...")
            val mapSize = RetrofitClient.wirelessMapApi.getMapSize()
            Log.d(TAG, "✓ API connection successful!")
            Log.d(TAG, "Map size: minX=${mapSize.minX}, maxX=${mapSize.maxX}, minY=${mapSize.minY}, maxY=${mapSize.maxY}")
            Log.d(TAG, "Map dimensions: ${mapSize.maxX - mapSize.minX + 1} x ${mapSize.maxY - mapSize.minY + 1}")
        } catch (e: Exception) {
            Log.e(TAG, "✗ API connection failed: ${e.message}")
            Log.e(TAG, "Make sure your service is running on http://localhost:9000")
            throw e
        }
    }

    private suspend fun testFetchAndCacheMap() {
        Log.d(TAG, "\n--- Test 2: Fetching and Caching Complete Map ---")

        // Clear existing cache first
        Log.d(TAG, "Clearing existing cache...")
        repository.clearCache()

        // Check cache is empty
        val isCachedBefore = repository.isMapCached()
        Log.d(TAG, "Is map cached before fetch? $isCachedBefore")

        // Fetch and cache
        Log.d(TAG, "Starting to fetch map from API...")
        val result = repository.fetchAndCacheMap()

        if (result.isSuccess) {
            Log.d(TAG, "✓ Map fetched and cached successfully!")
        } else {
            Log.e(TAG, "✗ Failed to fetch map: ${result.exceptionOrNull()?.message}")
            throw result.exceptionOrNull() ?: Exception("Unknown error")
        }

        // Verify cache is populated
        val isCachedAfter = repository.isMapCached()
        Log.d(TAG, "Is map cached after fetch? $isCachedAfter")

        if (isCachedAfter) {
            Log.d(TAG, "✓ Cache status verified!")
        } else {
            Log.e(TAG, "✗ Cache is still empty after fetch!")
        }
    }

    private suspend fun testVerifyCachedData() {
        Log.d(TAG, "\n--- Test 3: Verifying Cached Data ---")

        // Check metadata
        val metadata = repository.observeMapMetadata().first()
        if (metadata != null) {
            Log.d(TAG, "✓ Metadata found:")
            Log.d(TAG, "  Width: ${metadata.width}")
            Log.d(TAG, "  Height: ${metadata.height}")
            Log.d(TAG, "  Last updated: ${metadata.lastUpdatedEpochMillis}")
        } else {
            Log.e(TAG, "✗ No metadata found!")
        }

        // Check all cells
        val allCells = repository.observeAllMapCells().first()
        Log.d(TAG, "Total cells in cache: ${allCells.size}")

        if (allCells.isNotEmpty()) {
            Log.d(TAG, "✓ Cache contains data!")
            Log.d(TAG, "\nSample cells (first 10):")
            allCells.take(10).forEach { cell ->
                Log.d(TAG, "  Cell(x=${cell.x}, y=${cell.y}, s1=${cell.strength1}, s2=${cell.strength2}, s3=${cell.strength3})")
            }
        } else {
            Log.e(TAG, "✗ No cells found in cache!")
        }
    }

    private suspend fun testFetchSpecificColumn(x: Int) {
        Log.d(TAG, "\n--- Test 4: Fetching Specific Column (x=$x) ---")

        Log.d(TAG, "Fetching column x=$x from API...")
        val result = repository.refreshColumn(x)

        if (result.isSuccess) {
            val cells = result.getOrNull()!!
            Log.d(TAG, "✓ Column fetched successfully!")
            Log.d(TAG, "Column x=$x contains ${cells.size} cells:")

            // Show only cells with non-zero signal strengths first
            val nonZeroCells = cells.filter { it.strength1 != 0 || it.strength2 != 0 || it.strength3 != 0 }
            val zeroCells = cells.filter { it.strength1 == 0 && it.strength2 == 0 && it.strength3 == 0 }

            Log.d(TAG, "Cells with signal data (${nonZeroCells.size}):")
            nonZeroCells.forEach { cell ->
                Log.d(TAG, "  Cell(x=${cell.x}, y=${cell.y}, s1=${cell.strength1}, s2=${cell.strength2}, s3=${cell.strength3})")
            }

            if (zeroCells.isNotEmpty()) {
                Log.d(TAG, "Cells filled with zeros (${zeroCells.size}):")
                zeroCells.take(3).forEach { cell ->
                    Log.d(TAG, "  Cell(x=${cell.x}, y=${cell.y}, s1=0, s2=0, s3=0)")
                }
                if (zeroCells.size > 3) {
                    Log.d(TAG, "  ... and ${zeroCells.size - 3} more cells with zeros")
                }
            }

            // Verify it's in the cache
            val cachedColumn = repository.observeMapColumn(x).first()
            Log.d(TAG, "Cached cells for column x=$x: ${cachedColumn.size}")

            if (cachedColumn.isNotEmpty()) {
                Log.d(TAG, "✓ Column data cached successfully!")
            } else {
                Log.e(TAG, "✗ Column data not found in cache!")
            }
        } else {
            Log.e(TAG, "✗ Failed to fetch column: ${result.exceptionOrNull()?.message}")
        }
    }

    private suspend fun testVerifyRectangularMap() {
        Log.d(TAG, "\n--- Test 5: Verifying Complete Rectangular Map ---")

        val metadata = repository.observeMapMetadata().first()
        if (metadata == null) {
            Log.e(TAG, "✗ No metadata available!")
            return
        }

        val allCells = repository.observeAllMapCells().first()
        val cellMap = allCells.associateBy { it.x to it.y }

        // Calculate expected dimensions from API
        val mapSize = RetrofitClient.wirelessMapApi.getMapSize()
        val expectedCells = (mapSize.maxX - mapSize.minX + 1) * (mapSize.maxY - mapSize.minY + 1)

        Log.d(TAG, "Map should be complete rectangle:")
        Log.d(TAG, "  X range: ${mapSize.minX} to ${mapSize.maxX}")
        Log.d(TAG, "  Y range: ${mapSize.minY} to ${mapSize.maxY}")
        Log.d(TAG, "  Expected cells: $expectedCells")
        Log.d(TAG, "  Actual cells: ${allCells.size}")

        // Count cells with signal vs zeros
        val cellsWithSignal = allCells.count { it.strength1 != 0 || it.strength2 != 0 || it.strength3 != 0 }
        val cellsWithZeros = allCells.count { it.strength1 == 0 && it.strength2 == 0 && it.strength3 == 0 }

        Log.d(TAG, "Cell statistics:")
        Log.d(TAG, "  Cells with signal data: $cellsWithSignal")
        Log.d(TAG, "  Cells filled with zeros: $cellsWithZeros")

        // Check for any missing cells
        var missingCells = 0
        for (x in mapSize.minX..mapSize.maxX) {
            for (y in mapSize.minY..mapSize.maxY) {
                if (cellMap[x to y] == null) {
                    missingCells++
                    if (missingCells <= 3) {
                        Log.e(TAG, "  ✗ Missing cell at (x=$x, y=$y)")
                    }
                }
            }
        }

        if (missingCells > 0) {
            Log.e(TAG, "✗ Found $missingCells missing cells in the map!")
        } else if (allCells.size == expectedCells) {
            Log.d(TAG, "✓ Map is complete rectangle with all $expectedCells cells present!")
            Log.d(TAG, "✓ Missing API data properly filled with zero strengths!")
        } else {
            Log.w(TAG, "⚠ Unexpected cell count: ${allCells.size}/$expectedCells")
        }
    }

    private suspend fun testCacheStatus() {
        Log.d(TAG, "\n--- Test 6: Final Cache Status ---")

        val isCached = repository.isMapCached()
        val metadata = repository.observeMapMetadata().first()
        val allCells = repository.observeAllMapCells().first()

        Log.d(TAG, "Cache summary:")
        Log.d(TAG, "  Is cached: $isCached")
        Log.d(TAG, "  Metadata exists: ${metadata != null}")
        Log.d(TAG, "  Total cells: ${allCells.size}")

        // Group cells by column for summary
        val cellsByColumn = allCells.groupBy { it.x }
        Log.d(TAG, "  Columns cached: ${cellsByColumn.keys.sorted()}")
        Log.d(TAG, "\nCells per column:")
        cellsByColumn.keys.sorted().forEach { x ->
            val columnCells = cellsByColumn[x]!!
            val nonZeroCells = columnCells.count { it.strength1 != 0 || it.strength2 != 0 || it.strength3 != 0 }
            Log.d(TAG, "    x=$x: ${columnCells.size} cells ($nonZeroCells with signal, ${columnCells.size - nonZeroCells} with zeros)")
        }

        if (isCached && metadata != null && allCells.isNotEmpty()) {
            Log.d(TAG, "\n✓ Cache is fully populated and ready for use!")
            Log.d(TAG, "✓ Rectangular map structure maintained with zero-filled cells!")
        } else {
            Log.e(TAG, "\n✗ Cache has issues!")
        }
    }

    private fun closeDatabase() {
        if (::database.isInitialized) {
            Log.d(TAG, "\n--- Closing Database ---")
            database.close()
            Log.d(TAG, "Database closed")
        }
    }

    companion object {
        /**
         * Convenience function to run the test from an Activity or Fragment
         * Usage: WirelessApiIntegrationTest.runTest(context)
         */
        fun runTest(context: Context) {
            val test = WirelessApiIntegrationTest(context)
            CoroutineScope(Dispatchers.Main).launch {
                test.runTest()
            }
        }
    }
}
