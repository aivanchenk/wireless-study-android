package com.example.wirelesslocationstud.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_measurements")
data class UserMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val x: Int,
    val y: Int,
    val sensor: String,
    val strength: Int,
    val recordedAtEpochMillis: Long
)