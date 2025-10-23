package com.example.wirelesslocationstud.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity
import com.example.wirelesslocationstud.databinding.FragmentDashboardBinding
import com.example.wirelesslocationstud.databinding.DialogCoordinateEditBinding
import com.google.android.material.snackbar.Snackbar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var adapter: CoordinateTableAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupTableHeaders()
        setupFab()
        observeData()

        return root
    }

    private fun setupRecyclerView() {
        adapter = CoordinateTableAdapter(
            onEditClick = { cell ->
                showCoordinateDialog(cell)
            },
            onDeleteClick = { cell ->
                showDeleteConfirmationDialog(cell)
            }
        )
        binding.recyclerViewCoordinates.adapter = adapter
    }

    private fun setupTableHeaders() {
        // X header - sort by X coordinate
        binding.headerX.setOnClickListener {
            dashboardViewModel.sortByColumn(DashboardViewModel.SortColumn.COORDINATE_X)
        }

        // Y header - sort by Y coordinate
        binding.headerY.setOnClickListener {
            dashboardViewModel.sortByColumn(DashboardViewModel.SortColumn.COORDINATE_Y)
        }

        // Strength 1 header
        binding.headerStrength1.setOnClickListener {
            dashboardViewModel.sortByColumn(DashboardViewModel.SortColumn.STRENGTH1)
        }

        // Strength 2 header
        binding.headerStrength2.setOnClickListener {
            dashboardViewModel.sortByColumn(DashboardViewModel.SortColumn.STRENGTH2)
        }

        // Strength 3 header
        binding.headerStrength3.setOnClickListener {
            dashboardViewModel.sortByColumn(DashboardViewModel.SortColumn.STRENGTH3)
        }
    }

    private fun setupFab() {
        binding.fabAddCoordinate.setOnClickListener {
            showCoordinateDialog(null)
        }
    }

    private fun showCoordinateDialog(existingCell: MapCellEntity?) {
        val dialogBinding = DialogCoordinateEditBinding.inflate(layoutInflater)

        // Pre-fill values if editing existing cell
        existingCell?.let { cell ->
            dialogBinding.dialogTitle.text = "Edit Coordinate"
            dialogBinding.xInputEdittext.setText(cell.x.toString())
            dialogBinding.yInputEdittext.setText(cell.y.toString())
            dialogBinding.strength1InputEdittext.setText(cell.strength1?.toString() ?: "")
            dialogBinding.strength2InputEdittext.setText(cell.strength2?.toString() ?: "")
            dialogBinding.strength3InputEdittext.setText(cell.strength3?.toString() ?: "")

            // Disable coordinate editing when editing existing cell
            dialogBinding.xInputEdittext.isEnabled = false
            dialogBinding.yInputEdittext.isEnabled = false
        } ?: run {
            dialogBinding.dialogTitle.text = "Add New Coordinate"
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.buttonSave.setOnClickListener {
            val xText = dialogBinding.xInputEdittext.text.toString()
            val yText = dialogBinding.yInputEdittext.text.toString()
            val strength1Text = dialogBinding.strength1InputEdittext.text.toString()
            val strength2Text = dialogBinding.strength2InputEdittext.text.toString()
            val strength3Text = dialogBinding.strength3InputEdittext.text.toString()

            // Validate inputs
            if (xText.isEmpty() || yText.isEmpty()) {
                Snackbar.make(binding.root, "X and Y coordinates are required", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val x = xText.toIntOrNull()
            val y = yText.toIntOrNull()

            if (x == null || y == null) {
                Snackbar.make(binding.root, "Invalid coordinate values", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val strength1 = strength1Text.toIntOrNull()
            val strength2 = strength2Text.toIntOrNull()
            val strength3 = strength3Text.toIntOrNull()

            // Save the coordinate (will override if exists)
            dashboardViewModel.saveCoordinate(x, y, strength1, strength2, strength3)

            dialog.dismiss()

            val message = if (existingCell != null) {
                "Coordinate updated successfully"
            } else {
                "Coordinate added successfully"
            }
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(cell: MapCellEntity) {
        AlertDialog.Builder(requireContext())
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
        // Observe displayed coordinates
        dashboardViewModel.displayedCoordinates.observe(viewLifecycleOwner) { coordinates ->
            adapter.submitList(coordinates)

            // Show/hide empty state
            if (coordinates.isEmpty()) {
                binding.textEmptyState.visibility = View.VISIBLE
                binding.recyclerViewCoordinates.visibility = View.GONE

                // Set up click listener on empty state to refresh
                binding.textEmptyState.setOnClickListener {
                    Snackbar.make(binding.root, "Refreshing map data...", Snackbar.LENGTH_SHORT).show()
                    dashboardViewModel.refreshMapData()
                }
            } else {
                binding.textEmptyState.visibility = View.GONE
                binding.recyclerViewCoordinates.visibility = View.VISIBLE
            }
        }

        // Observe sort state and update icons
        dashboardViewModel.sortColumn.observe(viewLifecycleOwner) { column ->
            updateSortIcons(column)
        }

        dashboardViewModel.sortAscending.observe(viewLifecycleOwner) { ascending ->
            updateSortDirection(dashboardViewModel.sortColumn.value, ascending)
        }
    }

    private fun updateSortIcons(activeColumn: DashboardViewModel.SortColumn?) {
        // Hide all sort icons first
        binding.sortIconX.visibility = View.GONE
        binding.sortIconY.visibility = View.GONE
        binding.sortIconStrength1.visibility = View.GONE
        binding.sortIconStrength2.visibility = View.GONE
        binding.sortIconStrength3.visibility = View.GONE

        // Show icon for active column
        when (activeColumn) {
            DashboardViewModel.SortColumn.COORDINATE_X -> {
                binding.sortIconX.visibility = View.VISIBLE
            }
            DashboardViewModel.SortColumn.COORDINATE_Y -> {
                binding.sortIconY.visibility = View.VISIBLE
            }
            DashboardViewModel.SortColumn.STRENGTH1 -> {
                binding.sortIconStrength1.visibility = View.VISIBLE
            }
            DashboardViewModel.SortColumn.STRENGTH2 -> {
                binding.sortIconStrength2.visibility = View.VISIBLE
            }
            DashboardViewModel.SortColumn.STRENGTH3 -> {
                binding.sortIconStrength3.visibility = View.VISIBLE
            }
            else -> {} // No active sort
        }
    }

    private fun updateSortDirection(activeColumn: DashboardViewModel.SortColumn?, ascending: Boolean) {
        val iconRes = if (ascending) {
            android.R.drawable.arrow_up_float
        } else {
            android.R.drawable.arrow_down_float
        }

        when (activeColumn) {
            DashboardViewModel.SortColumn.COORDINATE_X -> {
                binding.sortIconX.setImageResource(iconRes)
            }
            DashboardViewModel.SortColumn.COORDINATE_Y -> {
                binding.sortIconY.setImageResource(iconRes)
            }
            DashboardViewModel.SortColumn.STRENGTH1 -> {
                binding.sortIconStrength1.setImageResource(iconRes)
            }
            DashboardViewModel.SortColumn.STRENGTH2 -> {
                binding.sortIconStrength2.setImageResource(iconRes)
            }
            DashboardViewModel.SortColumn.STRENGTH3 -> {
                binding.sortIconStrength3.setImageResource(iconRes)
            }
            else -> {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}