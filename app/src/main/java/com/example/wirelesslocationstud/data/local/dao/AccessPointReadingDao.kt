package com.example.wirelesslocationstud.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wirelesslocationstud.data.local.entity.AccessPointReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessPointReadingDao {
    @Query(
        "SELECT * FROM access_point_readings WHERE x = :x ORDER BY y DESC, sensor ASC"
    )
    fun observeColumn(x: Int): Flow<List<AccessPointReadingEntity>>

    @Query("SELECT * FROM access_point_readings ORDER BY x ASC, y DESC, sensor ASC")
    fun observeAll(): Flow<List<AccessPointReadingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReadings(readings: List<AccessPointReadingEntity>)

    @Query("DELETE FROM access_point_readings")
    suspend fun clear()

    @Query("DELETE FROM access_point_readings WHERE x = :x")
    suspend fun deleteColumn(x: Int)
}