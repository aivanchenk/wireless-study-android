package com.example.wirelesslocationstud.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_metadata")
data class MapMetadataEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val width: Int,
    val height: Int,
    val minX: Int = 0,
    val maxX: Int = width - 1,
    val minY: Int = 0,
    val maxY: Int = height - 1,
    val lastUpdatedEpochMillis: Long
) {
    companion object {
        const val SINGLETON_ID: Int = 0
    }
}