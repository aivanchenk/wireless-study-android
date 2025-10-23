package com.example.wirelesslocationstud.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.remote.api.RetrofitClient
import com.example.wirelesslocationstud.data.repository.WirelessMapRepository
import com.example.wirelesslocationstud.databinding.FragmentFormBinding
import kotlinx.coroutines.launch

class FormFragment : Fragment() {

    private var _binding: FragmentFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FormViewModel by viewModels {
        val database = WirelessDatabase.getDatabase(requireContext())
        val repository = WirelessMapRepository(
            RetrofitClient.wirelessMapApi,
            database.mapCellDao(),
            database.mapMetadataDao()
        )
        FormViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSubmitButton()
        observeMapMetadata()
    }

    private fun setupSubmitButton() {
        binding.buttonSubmit.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {
        // Clear previous errors
        binding.inputX.error = null
        binding.inputY.error = null

        val xText = binding.inputX.text.toString()
        val yText = binding.inputY.text.toString()
        val rss1Text = binding.inputRss1.text.toString()
        val rss2Text = binding.inputRss2.text.toString()
        val rss3Text = binding.inputRss3.text.toString()

        // Check if all fields are filled
        var hasError = false

        if (xText.isEmpty()) {
            binding.inputX.error = "X coordinate is required"
            hasError = true
        }

        if (yText.isEmpty()) {
            binding.inputY.error = "Y coordinate is required"
            hasError = true
        }

        if (rss1Text.isEmpty()) {
            binding.inputRss1.error = "RSS Value 1 is required"
            hasError = true
        }

        if (rss2Text.isEmpty()) {
            binding.inputRss2.error = "RSS Value 2 is required"
            hasError = true
        }

        if (rss3Text.isEmpty()) {
            binding.inputRss3.error = "RSS Value 3 is required"
            hasError = true
        }

        if (hasError) return

        // Validate coordinate values
        try {
            val x = xText.toInt()
            val y = yText.toInt()

            when (val result = viewModel.validateCoordinates(x, y)) {
                is FormViewModel.ValidationResult.Success -> {
                    // Validation successful - proceed with submission
                    Toast.makeText(
                        requireContext(),
                        "Form submitted successfully! X=$x, Y=$y",
                        Toast.LENGTH_SHORT
                    ).show()

                    // TODO: Add actual submission logic here
                    // For now, just clear the form
                    clearForm()
                }
                is FormViewModel.ValidationResult.Error -> {
                    // Show error message
                    if (result.message.contains("X coordinate")) {
                        binding.inputX.error = result.message
                    } else if (result.message.contains("Y coordinate")) {
                        binding.inputY.error = result.message
                    } else {
                        // General error (e.g., map not loaded)
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (_: NumberFormatException) {
            // Invalid number format
            Toast.makeText(
                requireContext(),
                "Please enter valid integer values for coordinates",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun clearForm() {
        binding.inputX.text?.clear()
        binding.inputY.text?.clear()
        binding.inputRss1.text?.clear()
        binding.inputRss2.text?.clear()
        binding.inputRss3.text?.clear()
    }

    private fun observeMapMetadata() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.coordinateRanges.collect { ranges ->
                    // Update hint text with actual coordinate ranges if available
                    ranges?.let {
                        binding.inputX.hint = "X Coordinate (${it.minX} to ${it.maxX})"
                        binding.inputY.hint = "Y Coordinate (${it.minY} to ${it.maxY})"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}