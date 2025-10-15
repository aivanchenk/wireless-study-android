package com.example.wirelesslocationstud.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.wirelesslocationstud.R
import com.example.wirelesslocationstud.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set up segmented button group
        binding.segmentedButtonGroup.check(R.id.button_canvas)

        binding.segmentedButtonGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_canvas -> homeViewModel.selectTab(HomeViewModel.TabType.CANVAS)
                    R.id.button_image -> homeViewModel.selectTab(HomeViewModel.TabType.IMAGE)
                }
            }
        }

        // Observe content text changes
        homeViewModel.contentText.observe(viewLifecycleOwner) {
            binding.textContent.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}