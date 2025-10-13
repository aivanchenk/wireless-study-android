package com.example.wirelesslocationstud.data.local

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WirelessDatabaseTest(private val context: Context) {
    private val TAG = "WirelessDatabaseTest"

    private lateinit var database: WirelessDatabase

    // Sample data from user
    private val sampleData = listOf(
        MapCellEntity(
            x = 2,
            y = 35,
            strength1 = 16,
            strength2 = 0,
            strength3 = 30,
            lastUpdatedEpochMillis = System.currentTimeMillis()
        ),
        MapCellEntity(
            x = 2,
            y = 34,
            strength1 = 18,
            strength2 = 0,
            strength3 = 30,
            lastUpdatedEpochMillis = System.currentTimeMillis()
        ),
        MapCellEntity(
            x = 2,
            y = 33,
            strength1 = 18,
            strength2 = 0,
            strength3 = 37,
            lastUpdatedEpochMillis = System.currentTimeMillis()
        ),
        MapCellEntity(
            x = 2,
            y = 32,
            strength1 = 23,
            strength2 = 3,
            strength3 = 28,
            lastUpdatedEpochMillis = System.currentTimeMillis()
        )
    )

    suspend fun runTest() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "Starting Room Database Test")
                Log.d(TAG, "========================================")

                // Initialize database
                initDatabase()

                // Test 1: Clear existing data
                testClearDatabase()

                // Test 2: Insert sample data
                testInsertData()

                // Test 3: Query all cells
                testQueryAllCells()

                // Test 4: Query specific column
                testQueryColumn(2)

                // Test 5: Update existing data (upsert)
                testUpdateData()

                // Test 6: Delete column
                testDeleteColumn(2)

                Log.d(TAG, "========================================")
                Log.d(TAG, "All tests completed successfully!")
                Log.d(TAG, "========================================")

            } catch (e: Exception) {
                Log.e(TAG, "Test failed with error: ${e.message}", e)
            } finally {
                closeDatabase()
            }
        }
    }

    private fun initDatabase() {
        Log.d(TAG, "\n--- Initializing Database ---")
        database = Room.databaseBuilder(
            context,
            WirelessDatabase::class.java,
            "test_wireless_map.db"
        ).build()
        Log.d(TAG, "Database initialized successfully")
    }

    private suspend fun testClearDatabase() {
        Log.d(TAG, "\n--- Test 1: Clearing Database ---")
        database.mapCellDao().clear()
        Log.d(TAG, "Database cleared successfully")
    }

    private suspend fun testInsertData() {
        Log.d(TAG, "\n--- Test 2: Inserting Sample Data ---")
        Log.d(TAG, "Inserting ${sampleData.size} map cells:")
        sampleData.forEach { cell ->
            Log.d(TAG, "  Cell(x=${cell.x}, y=${cell.y}, s1=${cell.strength1}, s2=${cell.strength2}, s3=${cell.strength3})")
        }

        database.mapCellDao().upsertCells(sampleData)
        Log.d(TAG, "Data inserted successfully")
    }

    private suspend fun testQueryAllCells() {
        Log.d(TAG, "\n--- Test 3: Querying All Cells ---")
        val cells = database.mapCellDao().observeAllCells().first()
        Log.d(TAG, "Retrieved ${cells.size} cells from database:")
        cells.forEach { cell ->
            Log.d(TAG, "  Cell(x=${cell.x}, y=${cell.y}, s1=${cell.strength1}, s2=${cell.strength2}, s3=${cell.strength3}, lastUpdated=${cell.lastUpdatedEpochMillis})")
        }

        if (cells.size == sampleData.size) {
            Log.d(TAG, "✓ Cell count matches expected count")
        } else {
            Log.e(TAG, "✗ Cell count mismatch! Expected ${sampleData.size}, got ${cells.size}")
        }
    }

    private suspend fun testQueryColumn(x: Int) {
        Log.d(TAG, "\n--- Test 4: Querying Column x=$x ---")
        val cells = database.mapCellDao().observeColumn(x).first()
        Log.d(TAG, "Retrieved ${cells.size} cells from column x=$x:")
        cells.forEach { cell ->
            Log.d(TAG, "  Cell(x=${cell.x}, y=${cell.y}, s1=${cell.strength1}, s2=${cell.strength2}, s3=${cell.strength3})")
        }
    }

    private suspend fun testUpdateData() {
        Log.d(TAG, "\n--- Test 5: Updating Data (Upsert) ---")
        val updatedCell = MapCellEntity(
            x = 2,
            y = 35,
            strength1 = 99,  // Updated value
            strength2 = 88,  // Updated value
            strength3 = 77,  // Updated value
            lastUpdatedEpochMillis = System.currentTimeMillis()
        )
        Log.d(TAG, "Updating cell at (2, 35) with new strength values: s1=99, s2=88, s3=77")

        database.mapCellDao().upsertCells(listOf(updatedCell))

        val cells = database.mapCellDao().observeColumn(2).first()
        val updated = cells.find { it.x == 2 && it.y == 35 }
        if (updated != null) {
            Log.d(TAG, "Updated cell: Cell(x=${updated.x}, y=${updated.y}, s1=${updated.strength1}, s2=${updated.strength2}, s3=${updated.strength3})")
            if (updated.strength1 == 99 && updated.strength2 == 88 && updated.strength3 == 77) {
                Log.d(TAG, "✓ Update successful!")
            } else {
                Log.e(TAG, "✗ Update failed! Values don't match")
            }
        } else {
            Log.e(TAG, "✗ Could not find updated cell")
        }
    }

    private suspend fun testDeleteColumn(x: Int) {
        Log.d(TAG, "\n--- Test 6: Deleting Column x=$x ---")
        database.mapCellDao().deleteColumn(x)
        Log.d(TAG, "Deleted column x=$x")

        val cells = database.mapCellDao().observeAllCells().first()
        Log.d(TAG, "Remaining cells in database: ${cells.size}")
        if (cells.isEmpty()) {
            Log.d(TAG, "✓ All cells deleted successfully")
        } else {
            Log.e(TAG, "✗ Some cells remain in database")
            cells.forEach { cell ->
                Log.d(TAG, "  Remaining: Cell(x=${cell.x}, y=${cell.y})")
            }
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
         * Usage: WirelessDatabaseTest.runTest(context)
         */
        fun runTest(context: Context) {
            val test = WirelessDatabaseTest(context)
            CoroutineScope(Dispatchers.Main).launch {
                test.runTest()
            }
        }
    }
}

