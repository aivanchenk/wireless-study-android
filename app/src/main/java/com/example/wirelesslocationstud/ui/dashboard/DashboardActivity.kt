package com.example.wirelesslocationstud.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.wirelesslocationstud.R
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity
import com.example.wirelesslocationstud.databinding.ActivityDashboardBinding
import com.example.wirelesslocationstud.ui.form.CreateCoordinateActivity
import com.example.wirelesslocationstud.ui.form.EditCoordinateActivity
import com.example.wirelesslocationstud.ui.home.HomeActivity
import com.google.android.material.snackbar.Snackbar

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var adapter: CoordinateTableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dashboardViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(DashboardViewModel::class.java)

        setupBottomNavigation()
        setupRecyclerView()
        setupTableHeaders()
        setupFab()
        observeData()
    }

    private fun setupBottomNavigation() {
        binding.navView.selectedItemId = R.id.navigation_dashboard
        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_dashboard -> true
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

    private fun setupRecyclerView() {
        adapter = CoordinateTableAdapter(
            onEditClick = { cell ->
                launchEditCoordinateActivity(cell)
            },
            onDeleteClick = { cell ->
                showDeleteConfirmationDialog(cell)
            }
        )
        binding.recyclerViewCoordinates.adapter = adapter
    }

    private fun setupTableHeaders() {
        binding.headerX.setOnClickListener {
            dashboardViewModel.sortByColumn(DashboardViewModel.SortColumn.COORDINATE_X)
        }

        binding.headerY.setOnClickListener {
            dashboardViewModel.sortByColumn(DashboardViewModel.SortColumn.COORDINATE_Y)
        }

        binding.headerStrength1.setOnClickListener {
            dashboardViewModel.sortByColumn(DashboardViewModel.SortColumn.STRENGTH1)
        }

        binding.headerStrength2.setOnClickListener {
            dashboardViewModel.sortByColumn(DashboardViewModel.SortColumn.STRENGTH2)
        }

        binding.headerStrength3.setOnClickListener {
            dashboardViewModel.sortByColumn(DashboardViewModel.SortColumn.STRENGTH3)
        }
    }

    private fun setupFab() {
        binding.fabAddCoordinate.setOnClickListener {
            launchCreateCoordinateActivity()
        }
    }

    private fun launchCreateCoordinateActivity() {
        val intent = Intent(this, CreateCoordinateActivity::class.java)
        startActivity(intent)
    }

    private fun launchEditCoordinateActivity(cell: MapCellEntity) {
        val intent = Intent(this, EditCoordinateActivity::class.java).apply {
            putExtra(EditCoordinateActivity.EXTRA_COORDINATE_X, cell.x)
            putExtra(EditCoordinateActivity.EXTRA_COORDINATE_Y, cell.y)
            putExtra(EditCoordinateActivity.EXTRA_RSS1, cell.strength1 ?: 0)
            putExtra(EditCoordinateActivity.EXTRA_RSS2, cell.strength2 ?: 0)
            putExtra(EditCoordinateActivity.EXTRA_RSS3, cell.strength3 ?: 0)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(cell: MapCellEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete Coordinate")
            .setMessage("Are you sure you want to delete coordinate (${cell.x}, ${cell.y})?")
            .setPositiveButton("Delete") { dialog, _ ->
                dashboardViewModel.deleteCoordinate(cell)
                Snackbar.make(binding.root, "Coordinate deleted", Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeData() {
        dashboardViewModel.displayedCoordinates.observe(this) { coordinates ->
            adapter.submitList(coordinates)

            if (coordinates.isEmpty()) {
                binding.textEmptyState.visibility = View.VISIBLE
                binding.recyclerViewCoordinates.visibility = View.GONE

                binding.textEmptyState.setOnClickListener {
                    Snackbar.make(binding.root, "Refreshing map data...", Snackbar.LENGTH_SHORT).show()
                    dashboardViewModel.refreshMapData()
                }
            } else {
                binding.textEmptyState.visibility = View.GONE
                binding.recyclerViewCoordinates.visibility = View.VISIBLE
            }
        }

        dashboardViewModel.sortColumn.observe(this) { column ->
            updateSortIcons(column)
        }

        dashboardViewModel.sortAscending.observe(this) { ascending ->
            updateSortDirection(dashboardViewModel.sortColumn.value, ascending)
        }
    }

    private fun updateSortIcons(activeColumn: DashboardViewModel.SortColumn?) {
        binding.sortIconX.visibility = View.GONE
        binding.sortIconY.visibility = View.GONE
        binding.sortIconStrength1.visibility = View.GONE
        binding.sortIconStrength2.visibility = View.GONE
        binding.sortIconStrength3.visibility = View.GONE

        when (activeColumn) {
            DashboardViewModel.SortColumn.COORDINATE_X -> binding.sortIconX.visibility = View.VISIBLE
            DashboardViewModel.SortColumn.COORDINATE_Y -> binding.sortIconY.visibility = View.VISIBLE
            DashboardViewModel.SortColumn.STRENGTH1 -> binding.sortIconStrength1.visibility = View.VISIBLE
            DashboardViewModel.SortColumn.STRENGTH2 -> binding.sortIconStrength2.visibility = View.VISIBLE
            DashboardViewModel.SortColumn.STRENGTH3 -> binding.sortIconStrength3.visibility = View.VISIBLE
            else -> {}
        }
    }

    private fun updateSortDirection(activeColumn: DashboardViewModel.SortColumn?, ascending: Boolean) {
        val iconRes = if (ascending) {
            android.R.drawable.arrow_up_float
        } else {
            android.R.drawable.arrow_down_float
        }

        when (activeColumn) {
            DashboardViewModel.SortColumn.COORDINATE_X -> binding.sortIconX.setImageResource(iconRes)
            DashboardViewModel.SortColumn.COORDINATE_Y -> binding.sortIconY.setImageResource(iconRes)
            DashboardViewModel.SortColumn.STRENGTH1 -> binding.sortIconStrength1.setImageResource(iconRes)
            DashboardViewModel.SortColumn.STRENGTH2 -> binding.sortIconStrength2.setImageResource(iconRes)
            DashboardViewModel.SortColumn.STRENGTH3 -> binding.sortIconStrength3.setImageResource(iconRes)
            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.refreshMapData()
    }
}

