package com.example.wirelesslocationstud.ui.form

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wirelesslocationstud.R
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.remote.api.RetrofitClient
import com.example.wirelesslocationstud.data.repository.WirelessMapRepository
import com.example.wirelesslocationstud.databinding.ActivityCreateCoordinateBinding
import com.example.wirelesslocationstud.ui.dashboard.DashboardActivity
import com.example.wirelesslocationstud.ui.home.HomeActivity
import kotlinx.coroutines.launch

class CreateCoordinateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateCoordinateBinding

    private val viewModel: FormViewModel by viewModels {
        val database = WirelessDatabase.getDatabase(applicationContext)
        val repository = WirelessMapRepository(
            RetrofitClient.wirelessMapApi,
            database.mapCellDao(),
            database.mapMetadataDao()
        )
        FormViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCoordinateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        setupSubmitButton()
        setupRefreshButton()
        observeMapMetadata()
    }

    private fun setupRefreshButton() {
        binding.fabRefresh.setOnClickListener {
            viewModel.refreshData()
            Toast.makeText(this, "Refreshing map data...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.navView.selectedItemId = R.id.navigation_form
        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_form -> true
                else -> false
            }
        }
    }

    private fun setupSubmitButton() {
        binding.buttonSubmit.setOnClickListener {
            validateAndSubmit()
        }

        binding.buttonCancel.setOnClickListener {
            clearForm()
        }
    }

    private fun clearForm() {
        binding.inputX.text?.clear()
        binding.inputY.text?.clear()
        binding.inputRss1.text?.clear()
        binding.inputRss2.text?.clear()
        binding.inputRss3.text?.clear()
        binding.inputX.requestFocus()
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
            val rss1 = rss1Text.toInt()
            val rss2 = rss2Text.toInt()
            val rss3 = rss3Text.toInt()

            when (val result = viewModel.validateCoordinates(x, y)) {
                is FormViewModel.ValidationResult.Success -> {
                    // Validation successful - save to database
                    viewModel.submitMeasurement(x, y, rss1, rss2, rss3)

                    Toast.makeText(
                        this,
                        "Coordinate created successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Clear the form for next entry
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
                        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (_: NumberFormatException) {
            // Invalid number format
            Toast.makeText(
                this,
                "Please enter valid integer values for coordinates and RSS values",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeMapMetadata() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
}

