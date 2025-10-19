package com.example.wirelesslocationstud.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.wirelesslocationstud.databinding.FragmentDashboardBinding

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
        observeData()

        return root
    }

    private fun setupRecyclerView() {
        adapter = CoordinateTableAdapter(
            onEditClick = { cell ->
                dashboardViewModel.editCoordinate(cell)
                // TODO: Open edit dialog
            },
            onDeleteClick = { cell ->
                dashboardViewModel.deleteCoordinate(cell)
                // TODO: Show confirmation dialog
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

    private fun observeData() {
        // Observe displayed coordinates
        dashboardViewModel.displayedCoordinates.observe(viewLifecycleOwner) { coordinates ->
            adapter.submitList(coordinates)

            // Show/hide empty state
            if (coordinates.isEmpty()) {
                binding.textEmptyState.visibility = View.VISIBLE
                binding.recyclerViewCoordinates.visibility = View.GONE
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