package com.example.wirelesslocationstud.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wirelesslocationstud.data.local.entity.UserMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserMeasurementDao {
    @Query("SELECT * FROM user_measurements ORDER BY recordedAtEpochMillis DESC")
    fun observeAll(): Flow<List<UserMeasurementEntity>>

    @Query(
        "SELECT * FROM user_measurements WHERE x = :x AND y = :y ORDER BY recordedAtEpochMillis DESC"
    )
    fun observeForCell(x: Int, y: Int): Flow<List<UserMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(measurement: UserMeasurementEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(measurements: List<UserMeasurementEntity>): List<Long>

    @Query("DELETE FROM user_measurements")
    suspend fun clear()
}