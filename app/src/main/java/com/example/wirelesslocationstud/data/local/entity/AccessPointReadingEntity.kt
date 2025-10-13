package com.example.wirelesslocationstud.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "access_point_readings",
    primaryKeys = ["x", "y", "sensor"]
)
data class AccessPointReadingEntity(
    val x: Int,
    val y: Int,
    val sensor: String,
    val strength: Int,
    val lastUpdatedEpochMillis: Long = 0L
)