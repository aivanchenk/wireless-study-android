package com.example.wirelesslocationstud.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.example.wirelesslocationstud.data.local.dao.MapCellDao
import com.example.wirelesslocationstud.data.local.dao.MapMetadataDao
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity
import com.example.wirelesslocationstud.data.remote.api.RetrofitClient
import com.example.wirelesslocationstud.data.repository.WirelessMapRepository
import kotlin.math.sqrt
import kotlin.math.pow

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WirelessMapRepository
    private val mapCellDao: MapCellDao
    private val mapMetadataDao: MapMetadataDao

    init {
        val database = WirelessDatabase.getDatabase(application)
        mapCellDao = database.mapCellDao()
        mapMetadataDao = database.mapMetadataDao()
        repository = WirelessMapRepository(
            RetrofitClient.wirelessMapApi,
            mapCellDao,
            mapMetadataDao
        )
    }

    private val _selectedTab = MutableLiveData<TabType>().apply {
        value = TabType.CANVAS
    }
    val selectedTab: LiveData<TabType> = _selectedTab

    // Observe all map cells from the database
    val mapCells: LiveData<List<MapCellEntity>> = repository.observeAllMapCells().asLiveData()

    // Track the target point (point with smallest Euclidean distance)
    private val _targetPoint = MutableLiveData<Pair<Int, Int>?>()
    val targetPoint: LiveData<Pair<Int, Int>?> = _targetPoint

    fun selectTab(tabType: TabType) {
        _selectedTab.value = tabType
    }

    /**
     * Calculate Euclidean distance for all cells with strength and find the closest match
     * @param rssVector List of 3 RSS values entered by user [R1, R2, R3]
     */
    fun findClosestPoint(rssVector: List<Int>) {
        val cells = mapCells.value ?: return

        android.util.Log.d("HomeViewModel", "=== Starting Euclidean Distance Calculation ===")
        android.util.Log.d("HomeViewModel", "Input RSS Vector: $rssVector (R1=${rssVector[0]}, R2=${rssVector[1]}, R3=${rssVector[2]})")

        // Filter cells that have strength (green cells)
        val cellsWithStrength = cells.filter { cell ->
            (cell.strength1 ?: 0) > 0 || (cell.strength2 ?: 0) > 0 || (cell.strength3 ?: 0) > 0
        }

        android.util.Log.d("HomeViewModel", "Found ${cellsWithStrength.size} cells with strength (green cells)")

        if (cellsWithStrength.isEmpty()) {
            android.util.Log.w("HomeViewModel", "No cells with strength found - cannot calculate target point")
            _targetPoint.value = null
            return
        }

        // Calculate Euclidean distance for each cell
        // Formula: Dj = √((R1 − Rj1)² + (R2 − Rj2)² + (R3 − Rj3)²)
        var minDistance = Double.MAX_VALUE
        var closestCell: MapCellEntity? = null

        for (cell in cellsWithStrength) {
            val rj1 = cell.strength1 ?: 0
            val rj2 = cell.strength2 ?: 0
            val rj3 = cell.strength3 ?: 0

            val distance = sqrt(
                (rssVector[0] - rj1).toDouble().pow(2) +
                        (rssVector[1] - rj2).toDouble().pow(2) +
                        (rssVector[2] - rj3).toDouble().pow(2)
            )

            android.util.Log.d("HomeViewModel",
                "Cell (${cell.x}, ${cell.y}): RSS=($rj1, $rj2, $rj3) | " +
                "Calculation: √[(${rssVector[0]}-$rj1)² + (${rssVector[1]}-$rj2)² + (${rssVector[2]}-$rj3)²] = ${"%.2f".format(distance)}")

            if (distance < minDistance) {
                minDistance = distance
                closestCell = cell
            }
        }

        // Update the target point
        val targetCoord = closestCell?.let { it.x to it.y }
        _targetPoint.value = targetCoord

        android.util.Log.d("HomeViewModel", "=== Result ===")
        android.util.Log.d("HomeViewModel", "Closest Point: $targetCoord")
        android.util.Log.d("HomeViewModel", "Minimum Euclidean Distance: ${"%.2f".format(minDistance)}")
        android.util.Log.d("HomeViewModel", "Target Cell RSS: (${closestCell?.strength1}, ${closestCell?.strength2}, ${closestCell?.strength3})")
    }

    /**
     * Clear the target point highlight
     */
    fun clearTargetPoint() {
        _targetPoint.value = null
    }

    enum class TabType {
        CANVAS, IMAGE
    }
}