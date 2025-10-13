package com.example.wirelesslocationstud.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "map_cells",
    primaryKeys = ["x", "y"]
)
data class MapCellEntity(
    val x: Int,
    val y: Int,
    val strength1: Int?,
    val strength2: Int?,
    val strength3: Int?,
    val lastUpdatedEpochMillis: Long = 0L
)