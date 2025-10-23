package com.example.wirelesslocationstud.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapCellDao {
    @Query("SELECT * FROM map_cells ORDER BY x ASC, y DESC")
    fun observeAllCells(): Flow<List<MapCellEntity>>

    @Query("SELECT * FROM map_cells WHERE x = :x ORDER BY y DESC")
    fun observeColumn(x: Int): Flow<List<MapCellEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCells(cells: List<MapCellEntity>)

    @Query("DELETE FROM map_cells")
    suspend fun clear()

    @Query("DELETE FROM map_cells WHERE x = :x")
    suspend fun deleteColumn(x: Int)

    @Query("DELETE FROM map_cells WHERE x = :x AND y = :y")
    suspend fun deleteCell(x: Int, y: Int)
}