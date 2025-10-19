package com.example.wirelesslocationstud

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.wirelesslocationstud.databinding.ActivityMainBinding
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.worker.MapSyncScheduler
import com.example.wirelesslocationstud.ui.home.RssInputDialogFragment
import com.example.wirelesslocationstud.ui.home.HomeViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database early
        WirelessDatabase.getDatabase(applicationContext)

        // Initialize HomeViewModel
        homeViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(HomeViewModel::class.java)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        // Schedule background sync to fetch map data from API on first launch
        // This will populate the Room database cache with data from http://localhost:9000
        MapSyncScheduler.scheduleFirstTimeSync(this)

        // Set up FAB click listener to open RSS input dialog
        binding.fabAdd.setOnClickListener {
            val dialog = RssInputDialogFragment()
            dialog.setOnRssSubmittedListener { rssVector ->
                android.util.Log.d("MainActivity", "RSS Vector submitted: $rssVector")
                // Calculate Euclidean distance and find the closest point
                homeViewModel.findClosestPoint(rssVector)
            }
            dialog.show(supportFragmentManager, RssInputDialogFragment.TAG)
        }
    }
}