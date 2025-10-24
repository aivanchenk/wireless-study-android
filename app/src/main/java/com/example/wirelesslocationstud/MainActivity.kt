package com.example.wirelesslocationstud

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.worker.MapSyncScheduler
import com.example.wirelesslocationstud.ui.home.HomeActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database early
        WirelessDatabase.getDatabase(applicationContext)

        // Schedule background sync to fetch map data from API on first launch
        MapSyncScheduler.scheduleFirstTimeSync(this)

        // Launch HomeActivity and finish MainActivity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}