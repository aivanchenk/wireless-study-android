package com.example.wirelesslocationstud.data.repository

import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.remote.api.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WirelessMapRepositoryTest {

    private lateinit var database: WirelessDatabase
    private lateinit var repository: WirelessMapRepository

    companion object {
        private const val TAG = "WirelessMapRepoTest"
    }

    @Before
    fun setup() {
        Log.d(TAG, "Setting up test database...")
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WirelessDatabase::class.java
        ).build()

        repository = WirelessMapRepository(
            api = RetrofitClient.wirelessMapApi,
            mapCellDao = database.mapCellDao(),
            mapMetadataDao = database.mapMetadataDao()
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testFetchAndCacheCompleteMap() = runBlocking {
        Log.d(TAG, "=== TEST: Fetch and Cache Complete Map ===")

        // Verify cache is empty initially
        val isCachedBefore = repository.isMapCached()
        Log.d(TAG, "Is map cached before fetch? $isCachedBefore")
        assert(!isCachedBefore) { "Map should not be cached initially" }

        // Fetch and cache map
        Log.d(TAG, "Fetching map from API...")
        val result = repository.fetchAndCacheMap()

        if (result.isSuccess) {
            Log.d(TAG, "✓ API request successful!")
        } else {
            Log.e(TAG, "✗ API request failed: ${result.exceptionOrNull()?.message}")
            throw result.exceptionOrNull() ?: Exception("Unknown error")
        }

        // Verify data is now cached
        val isCachedAfter = repository.isMapCached()
        Log.d(TAG, "Is map cached after fetch? $isCachedAfter")
        assert(isCachedAfter) { "Map should be cached after fetch" }

        // Verify metadata
        val metadata = repository.observeMapMetadata().first()
        Log.d(TAG, "Metadata: width=${metadata?.width}, height=${metadata?.height}, lastUpdated=${metadata?.lastUpdatedEpochMillis}")
        assert(metadata != null) { "Metadata should not be null" }

        // Verify cell data
        val allCells = repository.observeAllMapCells().first()
        Log.d(TAG, "Total cells cached: ${allCells.size}")
        assert(allCells.isNotEmpty()) { "Should have cached some cells" }

        // Log sample data
        Log.d(TAG, "Sample cells (first 10):")
        allCells.take(10).forEach { cell ->
            Log.d(TAG, "  Cell(x=${cell.x}, y=${cell.y}, s1=${cell.strength1}, s2=${cell.strength2}, s3=${cell.strength3})")
        }

        Log.d(TAG, "=== TEST PASSED ===")
    }

    @Test
    fun testFetchSpecificColumn() = runBlocking {
        Log.d(TAG, "=== TEST: Fetch Specific Column ===")

        val testX = 2
        Log.d(TAG, "Fetching column x=$testX...")

        val result = repository.refreshColumn(testX)

        if (result.isSuccess) {
            Log.d(TAG, "✓ Column fetch successful!")
            val cells = result.getOrNull()!!
            Log.d(TAG, "Column x=$testX has ${cells.size} cells")

            cells.forEach { cell ->
                Log.d(TAG, "  Cell(x=${cell.x}, y=${cell.y}, s1=${cell.strength1}, s2=${cell.strength2}, s3=${cell.strength3})")
                assert(cell.x == testX) { "Cell x should match requested column" }
            }
        } else {
            Log.e(TAG, "✗ Column fetch failed: ${result.exceptionOrNull()?.message}")
            throw result.exceptionOrNull() ?: Exception("Unknown error")
        }

        // Verify data is in database
        val cachedColumn = repository.observeMapColumn(testX).first()
        Log.d(TAG, "Cached cells for column x=$testX: ${cachedColumn.size}")
        assert(cachedColumn.isNotEmpty()) { "Should have cached cells for column" }

        Log.d(TAG, "=== TEST PASSED ===")
    }

    @Test
    fun testCacheVerification() = runBlocking {
        Log.d(TAG, "=== TEST: Cache Verification ===")

        // First, fetch and cache the map
        Log.d(TAG, "Step 1: Fetching map data...")
        val fetchResult = repository.fetchAndCacheMap()
        assert(fetchResult.isSuccess) { "Initial fetch should succeed" }

        // Verify all data is cached
        val metadata = repository.observeMapMetadata().first()
        val allCells = repository.observeAllMapCells().first()

        Log.d(TAG, "Cache verification:")
        Log.d(TAG, "  - Metadata exists: ${metadata != null}")
        Log.d(TAG, "  - Map dimensions: ${metadata?.width} x ${metadata?.height}")
        Log.d(TAG, "  - Total cells: ${allCells.size}")
        Log.d(TAG, "  - Last updated: ${metadata?.lastUpdatedEpochMillis}")

        // Verify specific column (x=2)
        val column2 = repository.observeMapColumn(2).first()
        Log.d(TAG, "  - Column x=2 cells: ${column2.size}")
        Log.d(TAG, "Column x=2 data:")
        column2.forEach { cell ->
            Log.d(TAG, "    y=${cell.y}: s1=${cell.strength1}, s2=${cell.strength2}, s3=${cell.strength3}")
        }

        assert(metadata != null) { "Metadata should be cached" }
        assert(allCells.isNotEmpty()) { "Cells should be cached" }
        assert(column2.isNotEmpty()) { "Column 2 should have data" }

        Log.d(TAG, "=== TEST PASSED ===")
    }

    @Test
    fun testClearCache() = runBlocking {
        Log.d(TAG, "=== TEST: Clear Cache ===")

        // First, cache some data
        Log.d(TAG, "Caching data...")
        repository.fetchAndCacheMap()

        val beforeClear = repository.isMapCached()
        Log.d(TAG, "Is cached before clear? $beforeClear")

        // Clear cache
        Log.d(TAG, "Clearing cache...")
        repository.clearCache()

        val afterClear = repository.isMapCached()
        Log.d(TAG, "Is cached after clear? $afterClear")

        assert(!afterClear) { "Cache should be empty after clearing" }

        Log.d(TAG, "=== TEST PASSED ===")
    }
}

