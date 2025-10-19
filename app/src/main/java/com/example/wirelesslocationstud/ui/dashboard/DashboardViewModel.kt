package com.example.wirelesslocationstud.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.wirelesslocationstud.data.local.dao.MapCellDao
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val mapCellDao: MapCellDao = WirelessDatabase.getDatabase(application).mapCellDao()

    // Sorting state
    private val _sortColumn = MutableLiveData<SortColumn>(SortColumn.NONE)
    val sortColumn: LiveData<SortColumn> = _sortColumn

    private val _sortAscending = MutableLiveData<Boolean>(true)
    val sortAscending: LiveData<Boolean> = _sortAscending

    // Observe all map cells with strength (green cells only)
    private val allCellsFlow = mapCellDao.observeAllCells()

    val coordinates: LiveData<List<MapCellEntity>> = allCellsFlow.asLiveData()

    // Filtered and sorted coordinates
    private val _displayedCoordinates = MutableLiveData<List<MapCellEntity>>()
    val displayedCoordinates: LiveData<List<MapCellEntity>> = _displayedCoordinates

    init {
        // Observe changes and apply filtering/sorting
        coordinates.observeForever { cells ->
            updateDisplayedCoordinates(cells)
        }
    }

    private fun updateDisplayedCoordinates(cells: List<MapCellEntity>?) {
        if (cells == null) {
            _displayedCoordinates.value = emptyList()
            return
        }

        // Filter: Only show cells with at least one non-zero strength (green cells)
        val greenCells = cells.filter { cell ->
            (cell.strength1 ?: 0) > 0 || (cell.strength2 ?: 0) > 0 || (cell.strength3 ?: 0) > 0
        }

        // Sort based on current sort column
        val sorted = when (_sortColumn.value) {
            SortColumn.COORDINATE_X -> {
                if (_sortAscending.value == true) {
                    greenCells.sortedBy { it.x }
                } else {
                    greenCells.sortedByDescending { it.x }
                }
            }
            SortColumn.COORDINATE_Y -> {
                if (_sortAscending.value == true) {
                    greenCells.sortedBy { it.y }
                } else {
                    greenCells.sortedByDescending { it.y }
                }
            }
            SortColumn.STRENGTH1 -> {
                if (_sortAscending.value == true) {
                    greenCells.sortedBy { it.strength1 ?: 0 }
                } else {
                    greenCells.sortedByDescending { it.strength1 ?: 0 }
                }
            }
            SortColumn.STRENGTH2 -> {
                if (_sortAscending.value == true) {
                    greenCells.sortedBy { it.strength2 ?: 0 }
                } else {
                    greenCells.sortedByDescending { it.strength2 ?: 0 }
                }
            }
            SortColumn.STRENGTH3 -> {
                if (_sortAscending.value == true) {
                    greenCells.sortedBy { it.strength3 ?: 0 }
                } else {
                    greenCells.sortedByDescending { it.strength3 ?: 0 }
                }
            }
            else -> greenCells // No sorting
        }

        _displayedCoordinates.value = sorted
    }

    fun sortByColumn(column: SortColumn) {
        if (_sortColumn.value == column) {
            // Toggle sort direction if clicking the same column
            _sortAscending.value = !(_sortAscending.value ?: true)
        } else {
            // New column, start with ascending
            _sortColumn.value = column
            _sortAscending.value = true
        }
        updateDisplayedCoordinates(coordinates.value)
    }

    fun deleteCoordinate(cell: MapCellEntity) {
        viewModelScope.launch {
            // For now, just remove from display (placeholder)
            // In a real implementation, you'd delete from database
            android.util.Log.d("DashboardViewModel", "Delete clicked for: x${cell.x} y${cell.y}")
        }
    }

    fun editCoordinate(cell: MapCellEntity) {
        // Placeholder for edit functionality
        android.util.Log.d("DashboardViewModel", "Edit clicked for: x${cell.x} y${cell.y}")
    }

    enum class SortColumn {
        NONE,
        COORDINATE_X,
        COORDINATE_Y,
        STRENGTH1,
        STRENGTH2,
        STRENGTH3
    }
}