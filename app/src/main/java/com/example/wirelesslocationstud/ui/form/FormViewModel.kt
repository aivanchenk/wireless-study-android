package com.example.wirelesslocationstud.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity
import com.example.wirelesslocationstud.data.local.entity.MapMetadataEntity
import com.example.wirelesslocationstud.data.repository.WirelessMapRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FormViewModel(
    private val repository: WirelessMapRepository
) : ViewModel() {

    val mapMetadata: StateFlow<MapMetadataEntity?> = repository.observeMapMetadata()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val mapCells: StateFlow<List<MapCellEntity>> = repository.observeAllMapCells()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Combined state that provides coordinate ranges from either metadata or actual cells
    val coordinateRanges: StateFlow<CoordinateRanges?> = combine(
        mapMetadata,
        mapCells
    ) { metadata, cells ->
        if (metadata != null) {
            // Use metadata if available
            CoordinateRanges(metadata.minX, metadata.maxX, metadata.minY, metadata.maxY)
        } else if (cells.isNotEmpty()) {
            // Calculate from actual cells if metadata not available
            CoordinateRanges(
                minX = cells.minOf { it.x },
                maxX = cells.maxOf { it.x },
                minY = cells.minOf { it.y },
                maxY = cells.maxOf { it.y }
            )
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun validateCoordinates(x: Int, y: Int): ValidationResult {
        val ranges = coordinateRanges.value

        if (ranges == null) {
            return ValidationResult.Error("Map data not loaded. Please sync map first.")
        }

        if (x < ranges.minX || x > ranges.maxX) {
            return ValidationResult.Error("X coordinate must be between ${ranges.minX} and ${ranges.maxX}")
        }

        if (y < ranges.minY || y > ranges.maxY) {
            return ValidationResult.Error("Y coordinate must be between ${ranges.minY} and ${ranges.maxY}")
        }

        return ValidationResult.Success
    }
    /**
     * Submit a new measurement to the database
     * This will save the coordinate with isCustom = true to indicate it was added from the form
     */
    fun submitMeasurement(x: Int, y: Int, rss1: Int, rss2: Int, rss3: Int) {
        viewModelScope.launch {
            val newCell = MapCellEntity(
                x = x,
                y = y,
                strength1 = rss1,
                strength2 = rss2,
                strength3 = rss3,
                lastUpdatedEpochMillis = System.currentTimeMillis(),
                isCustom = true // Mark as custom since it's from the form
            )
            repository.saveMapCell(newCell)
            android.util.Log.d("FormViewModel", "Saved measurement: ($x, $y) with RSS values ($rss1, $rss2, $rss3)")
        }
    }

    /**
     * Trigger a refresh of map data from the API
     */
    fun refreshData() {
        viewModelScope.launch {
            repository.fetchAndCacheMap()
            android.util.Log.d("FormViewModel", "Manual refresh triggered")
        }
    }

    data class CoordinateRanges(
        val minX: Int,
        val maxX: Int,
        val minY: Int,
        val maxY: Int
    )

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}