package com.example.wirelesslocationstud.data.local.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.wirelesslocationstud.data.local.dao.AccessPointReadingDao
import com.example.wirelesslocationstud.data.local.dao.MapCellDao
import com.example.wirelesslocationstud.data.local.dao.MapMetadataDao
import com.example.wirelesslocationstud.data.local.dao.UserMeasurementDao
import com.example.wirelesslocationstud.data.local.entity.AccessPointReadingEntity
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity
import com.example.wirelesslocationstud.data.local.entity.MapMetadataEntity
import com.example.wirelesslocationstud.data.local.entity.UserMeasurementEntity
import com.example.wirelesslocationstud.data.worker.MapSyncScheduler

@Database(
    entities = [
        AccessPointReadingEntity::class,
        MapCellEntity::class,
        MapMetadataEntity::class,
        UserMeasurementEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class WirelessDatabase : RoomDatabase() {
    abstract fun accessPointReadingDao(): AccessPointReadingDao
    abstract fun mapCellDao(): MapCellDao
    abstract fun mapMetadataDao(): MapMetadataDao
    abstract fun userMeasurementDao(): UserMeasurementDao

    companion object {
        const val DATABASE_NAME: String = "wireless_map.db"
        private const val TAG = "WirelessDatabase"

        @Volatile
        private var INSTANCE: WirelessDatabase? = null

        fun getDatabase(context: Context): WirelessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WirelessDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            Log.w(TAG, "Database was destructively migrated - all data lost! Scheduling re-sync...")
                            // Schedule background sync to refetch data from API
                            MapSyncScheduler.scheduleFirstTimeSync(context)
                        }

                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d(TAG, "Database created for the first time")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}