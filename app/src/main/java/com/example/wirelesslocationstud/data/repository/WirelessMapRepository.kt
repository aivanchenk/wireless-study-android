package com.example.wirelesslocationstud.data.repository

import android.util.Log
import com.example.wirelesslocationstud.data.local.dao.MapCellDao
import com.example.wirelesslocationstud.data.local.dao.MapMetadataDao
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity
import com.example.wirelesslocationstud.data.local.entity.MapMetadataEntity
import com.example.wirelesslocationstud.data.remote.api.WirelessMapApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WirelessMapRepository(
    private val api: WirelessMapApi,
    private val mapCellDao: MapCellDao,
    private val mapMetadataDao: MapMetadataDao
) {
    companion object {
        private const val TAG = "WirelessMapRepository"
    }

    /**
     * Observe map metadata from local cache
     */
    fun observeMapMetadata(): Flow<MapMetadataEntity?> {
        return mapMetadataDao.observeMetadata()
    }

    /**
     * Observe all map cells from local cache
     */
    fun observeAllMapCells(): Flow<List<MapCellEntity>> {
        return mapCellDao.observeAllCells()
    }

    /**
     * Observe a specific column from local cache
     */
    fun observeMapColumn(x: Int): Flow<List<MapCellEntity>> {
        return mapCellDao.observeColumn(x)
    }

    /**
     * Check if map data is cached
     */
    suspend fun isMapCached(): Boolean {
        val metadata = mapMetadataDao.observeMetadata().first()
        return metadata != null
    }

    /**
     * Fetch and cache the entire map from the API
     * Ensures a complete rectangular map with zero strengths for missing cells
     */
    suspend fun fetchAndCacheMap(): Result<Unit> {
        return try {
            Log.d(TAG, "Starting to fetch map data from API...")

            // Step 1: Get map size
            Log.d(TAG, "Fetching map size...")
            val mapSize = api.getMapSize()
            Log.d(TAG, "Map size received: minX=${mapSize.minX}, minY=${mapSize.minY}, maxX=${mapSize.maxX}, maxY=${mapSize.maxY}")

            // Step 2: Fetch all columns and build a map of existing cells
            val apiCellsMap = mutableMapOf<Pair<Int, Int>, MapCellEntity>()
            val currentTime = System.currentTimeMillis()

            for (x in mapSize.minX..mapSize.maxX) {
                Log.d(TAG, "Fetching column x=$x...")
                try {
                    val columnData = api.getMapColumn(x)
                    Log.d(TAG, "Column x=$x received ${columnData.size} cells from API")

                    columnData.forEach { cell ->
                        val cellEntity = MapCellEntity(
                            x = cell.x,
                            y = cell.y,
                            strength1 = cell.strength1 ?: 0,
                            strength2 = cell.strength2 ?: 0,
                            strength3 = cell.strength3 ?: 0,
                            lastUpdatedEpochMillis = currentTime
                        )
                        apiCellsMap[cell.x to cell.y] = cellEntity
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to fetch column x=$x: ${e.message}")
                    // Continue with other columns even if one fails
                }
            }

            // Step 3: Create complete rectangular map with zero strengths for missing cells
            val allCells = mutableListOf<MapCellEntity>()
            var filledCells = 0

            for (x in mapSize.minX..mapSize.maxX) {
                for (y in mapSize.minY..mapSize.maxY) {
                    val cell = apiCellsMap[x to y]
                    if (cell != null) {
                        allCells.add(cell)
                    } else {
                        // Create cell with zero strengths for missing data
                        allCells.add(
                            MapCellEntity(
                                x = x,
                                y = y,
                                strength1 = 0,
                                strength2 = 0,
                                strength3 = 0,
                                lastUpdatedEpochMillis = currentTime
                            )
                        )
                        filledCells++
                    }
                }
            }

            Log.d(TAG, "Complete rectangular map: ${allCells.size} total cells (${apiCellsMap.size} from API, $filledCells filled with zeros)")

            // Step 4: Save to database
            Log.d(TAG, "Saving ${allCells.size} cells to database...")
            mapCellDao.clear()
            mapCellDao.upsertCells(allCells)

            // Step 5: Save metadata
            val metadata = MapMetadataEntity(
                width = mapSize.maxX - mapSize.minX + 1,
                height = mapSize.maxY - mapSize.minY + 1,
                lastUpdatedEpochMillis = currentTime
            )
            mapMetadataDao.upsert(metadata)

            Log.d(TAG, "Map data successfully cached! Total cells: ${allCells.size}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching and caching map: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Refresh a specific column from the API
     * Ensures complete column with zero strengths for missing cells
     */
    suspend fun refreshColumn(x: Int): Result<List<MapCellEntity>> {
        return try {
            Log.d(TAG, "Refreshing column x=$x from API...")

            // Get map size to know the Y range
            val mapSize = api.getMapSize()
            val columnData = api.getMapColumn(x)

            // Build map of existing cells from API
            val apiCellsMap = columnData.associateBy { it.y }

            val currentTime = System.currentTimeMillis()
            val cellEntities = mutableListOf<MapCellEntity>()

            // Create complete column with zero strengths for missing cells
            for (y in mapSize.minY..mapSize.maxY) {
                val apiCell = apiCellsMap[y]
                if (apiCell != null) {
                    cellEntities.add(
                        MapCellEntity(
                            x = apiCell.x,
                            y = apiCell.y,
                            strength1 = apiCell.strength1 ?: 0,
                            strength2 = apiCell.strength2 ?: 0,
                            strength3 = apiCell.strength3 ?: 0,
                            lastUpdatedEpochMillis = currentTime
                        )
                    )
                } else {
                    // Fill missing cell with zero strengths
                    cellEntities.add(
                        MapCellEntity(
                            x = x,
                            y = y,
                            strength1 = 0,
                            strength2 = 0,
                            strength3 = 0,
                            lastUpdatedEpochMillis = currentTime
                        )
                    )
                }
            }

            mapCellDao.deleteColumn(x)
            mapCellDao.upsertCells(cellEntities)

            Log.d(TAG, "Column x=$x refreshed with ${cellEntities.size} cells (${columnData.size} from API)")
            Result.success(cellEntities)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing column x=$x: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Clear all cached data
     */
    suspend fun clearCache() {
        Log.d(TAG, "Clearing all cached data...")
        mapCellDao.clear()
        mapMetadataDao.clear()
    }
}
