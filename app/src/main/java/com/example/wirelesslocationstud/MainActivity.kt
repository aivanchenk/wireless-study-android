package com.example.wirelesslocationstud

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.wirelesslocationstud.databinding.ActivityMainBinding
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.worker.MapSyncScheduler

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database early
        WirelessDatabase.getDatabase(applicationContext)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        // Schedule background sync to fetch map data from API on first launch
        // This will populate the Room database cache with data from http://localhost:9000
        MapSyncScheduler.scheduleFirstTimeSync(this)
    }
}