package com.example.wirelesslocationstud.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.wirelesslocationstud.data.local.dao.AccessPointReadingDao
import com.example.wirelesslocationstud.data.local.dao.MapCellDao
import com.example.wirelesslocationstud.data.local.dao.MapMetadataDao
import com.example.wirelesslocationstud.data.local.dao.UserMeasurementDao
import com.example.wirelesslocationstud.data.local.entity.AccessPointReadingEntity
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity
import com.example.wirelesslocationstud.data.local.entity.MapMetadataEntity
import com.example.wirelesslocationstud.data.local.entity.UserMeasurementEntity

@Database(
    entities = [
        AccessPointReadingEntity::class,
        MapCellEntity::class,
        MapMetadataEntity::class,
        UserMeasurementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WirelessDatabase : RoomDatabase() {
    abstract fun accessPointReadingDao(): AccessPointReadingDao
    abstract fun mapCellDao(): MapCellDao
    abstract fun mapMetadataDao(): MapMetadataDao
    abstract fun userMeasurementDao(): UserMeasurementDao

    companion object {
        const val DATABASE_NAME: String = "wireless_map.db"

        @Volatile
        private var INSTANCE: WirelessDatabase? = null

        fun getDatabase(context: Context): WirelessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WirelessDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}