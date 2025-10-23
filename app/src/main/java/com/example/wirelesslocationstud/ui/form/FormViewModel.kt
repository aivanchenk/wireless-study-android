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