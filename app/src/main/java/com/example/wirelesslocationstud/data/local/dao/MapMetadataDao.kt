package com.example.wirelesslocationstud.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wirelesslocationstud.data.local.entity.MapMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapMetadataDao {
    @Query("SELECT * FROM map_metadata WHERE id = :id")
    fun observeMetadata(id: Int = MapMetadataEntity.SINGLETON_ID): Flow<MapMetadataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: MapMetadataEntity)

    @Query("DELETE FROM map_metadata")
    suspend fun clear()
}