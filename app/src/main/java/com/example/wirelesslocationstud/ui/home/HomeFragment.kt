package com.example.wirelesslocationstud.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.wirelesslocationstud.R
import com.example.wirelesslocationstud.databinding.FragmentHomeBinding
import com.example.wirelesslocationstud.data.worker.MapSyncScheduler
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels() to share the ViewModel with MainActivity
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set up segmented button group
        binding.segmentedButtonGroup.check(R.id.button_canvas)

        binding.segmentedButtonGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_canvas -> {
                        homeViewModel.selectTab(HomeViewModel.TabType.CANVAS)
                        binding.scrollContainer.visibility = View.VISIBLE
                        binding.roomScrollContainer.visibility = View.GONE
                        binding.textContent.visibility = View.GONE
                    }
                    R.id.button_image -> {
                        homeViewModel.selectTab(HomeViewModel.TabType.IMAGE)
                        binding.scrollContainer.visibility = View.GONE
                        binding.roomScrollContainer.visibility = View.VISIBLE
                        binding.textContent.visibility = View.GONE
                    }
                }
            }
        }

        // Observe map data and update canvas
        homeViewModel.mapCells.observe(viewLifecycleOwner) { cells ->
            android.util.Log.d("HomeFragment", "Map cells updated: ${cells.size} cells")

            // Update both canvas views
            binding.canvasView.setMapData(cells)

            android.util.Log.d("HomeFragment", "Canvas views updated with ${cells.size} cells")

            // If no data, show a helpful message
            if (cells.isEmpty()) {
                Snackbar.make(binding.root, "No map data available. Tap Refresh to load data.", Snackbar.LENGTH_LONG)
                    .setAction("Refresh") {
                        android.util.Log.d("HomeFragment", "Manual refresh triggered")
                        MapSyncScheduler.forceRefresh(requireContext())
                        Snackbar.make(binding.root, "Refreshing map data from API...", Snackbar.LENGTH_SHORT).show()
                    }
                    .show()
            }
        }

        // Observe target point and update canvas
        homeViewModel.targetPoint.observe(viewLifecycleOwner) { targetPoint ->
            android.util.Log.d("HomeFragment", "Target point updated in fragment: $targetPoint")
            binding.canvasView.setTargetPoint(targetPoint)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}