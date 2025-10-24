package com.example.wirelesslocationstud.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.wirelesslocationstud.R
import com.example.wirelesslocationstud.databinding.ActivityHomeBinding
import com.example.wirelesslocationstud.data.worker.MapSyncScheduler
import com.example.wirelesslocationstud.ui.dashboard.DashboardActivity
import com.example.wirelesslocationstud.ui.form.CreateCoordinateActivity
import com.google.android.material.snackbar.Snackbar

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        homeViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(HomeViewModel::class.java)

        setupBottomNavigation()
        setupSegmentedButtons()
        setupFab()
        observeViewModel()
    }

    private fun setupBottomNavigation() {
        binding.navView.selectedItemId = R.id.navigation_home
        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_form -> {
                    startActivity(Intent(this, CreateCoordinateActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSegmentedButtons() {
        binding.segmentedButtonGroup.check(R.id.button_canvas)

        binding.segmentedButtonGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_canvas -> {
                        binding.canvasView.visibility = android.view.View.VISIBLE
                        binding.roomScrollContainer.visibility = android.view.View.GONE
                    }
                    R.id.button_map -> {
                        binding.canvasView.visibility = android.view.View.GONE
                        binding.roomScrollContainer.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabRefresh.setOnClickListener {
            MapSyncScheduler.forceRefresh(this)
            Snackbar.make(binding.root, "Refreshing map data...", Snackbar.LENGTH_SHORT).show()
        }

        binding.fabAdd.setOnClickListener {
            val dialog = RssInputDialogFragment()
            dialog.setOnRssSubmittedListener { rssVector ->
                android.util.Log.d("HomeActivity", "RSS Vector submitted: $rssVector")
                homeViewModel.findClosestPoint(rssVector)
            }
            dialog.show(supportFragmentManager, RssInputDialogFragment.TAG)
        }
    }

    private fun observeViewModel() {
        homeViewModel.mapCells.observe(this) { cells ->
            android.util.Log.d("HomeActivity", "Map cells updated: ${cells.size} cells")
            binding.canvasView.setMapData(cells)
        }

        homeViewModel.targetPoint.observe(this) { targetPoint ->
            android.util.Log.d("HomeActivity", "Target point updated: $targetPoint")
            binding.canvasView.setTargetPoint(targetPoint)
        }
    }
}


