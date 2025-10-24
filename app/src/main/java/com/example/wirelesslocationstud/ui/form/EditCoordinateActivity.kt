package com.example.wirelesslocationstud.ui.form

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wirelesslocationstud.data.local.database.WirelessDatabase
import com.example.wirelesslocationstud.data.remote.api.RetrofitClient
import com.example.wirelesslocationstud.data.repository.WirelessMapRepository
import com.example.wirelesslocationstud.databinding.ActivityEditCoordinateBinding
import kotlinx.coroutines.launch

class EditCoordinateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCoordinateBinding

    private val viewModel: FormViewModel by viewModels {
        val database = WirelessDatabase.getDatabase(applicationContext)
        val repository = WirelessMapRepository(
            RetrofitClient.wirelessMapApi,
            database.mapCellDao(),
            database.mapMetadataDao()
        )
        FormViewModelFactory(repository)
    }

    private var coordinateX: Int = 0
    private var coordinateY: Int = 0

    companion object {
        const val EXTRA_COORDINATE_X = "extra_coordinate_x"
        const val EXTRA_COORDINATE_Y = "extra_coordinate_y"
        const val EXTRA_RSS1 = "extra_rss1"
        const val EXTRA_RSS2 = "extra_rss2"
        const val EXTRA_RSS3 = "extra_rss3"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCoordinateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadIntentData()
        setupBackButton()
        setupSubmitButton()
        observeMapMetadata()
    }

    private fun setupBackButton() {
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun loadIntentData() {
        coordinateX = intent.getIntExtra(EXTRA_COORDINATE_X, 0)
        coordinateY = intent.getIntExtra(EXTRA_COORDINATE_Y, 0)
        val rss1 = intent.getIntExtra(EXTRA_RSS1, 0)
        val rss2 = intent.getIntExtra(EXTRA_RSS2, 0)
        val rss3 = intent.getIntExtra(EXTRA_RSS3, 0)

        // Pre-fill the form
        binding.inputX.setText(coordinateX.toString())
        binding.inputY.setText(coordinateY.toString())
        binding.inputRss1.setText(if (rss1 != 0) rss1.toString() else "")
        binding.inputRss2.setText(if (rss2 != 0) rss2.toString() else "")
        binding.inputRss3.setText(if (rss3 != 0) rss3.toString() else "")

        // Disable coordinate editing
        binding.inputX.isEnabled = false
        binding.inputY.isEnabled = false
    }

    private fun setupSubmitButton() {
        binding.buttonSubmit.setOnClickListener {
            validateAndSubmit()
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateAndSubmit() {
        // Clear previous errors
        binding.inputRss1.error = null
        binding.inputRss2.error = null
        binding.inputRss3.error = null

        val rss1Text = binding.inputRss1.text.toString()
        val rss2Text = binding.inputRss2.text.toString()
        val rss3Text = binding.inputRss3.text.toString()

        // Check if all RSS fields are filled
        var hasError = false

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

        // Validate RSS values
        try {
            val rss1 = rss1Text.toInt()
            val rss2 = rss2Text.toInt()
            val rss3 = rss3Text.toInt()

            // Update the coordinate (coordinates are locked, only RSS values change)
            viewModel.submitMeasurement(coordinateX, coordinateY, rss1, rss2, rss3)

            Toast.makeText(
                this,
                "Coordinate updated successfully!",
                Toast.LENGTH_SHORT
            ).show()

            // Return to previous screen
            finish()
        } catch (_: NumberFormatException) {
            // Invalid number format
            Toast.makeText(
                this,
                "Please enter valid integer values for RSS values",
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

