package com.example.wirelesslocationstud

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.wirelesslocationstud.databinding.ActivityMainBinding
import com.example.wirelesslocationstud.data.remote.WirelessApiIntegrationTest

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        // Run API Integration Test - Check Logcat with tag "WirelessApiTest" to see results
        // Make sure your service is running on http://localhost:9000 before launching the app!
        WirelessApiIntegrationTest.runTest(this)

        // Uncomment below to also run the basic database test:
        // WirelessDatabaseTest.runTest(this)
    }
}