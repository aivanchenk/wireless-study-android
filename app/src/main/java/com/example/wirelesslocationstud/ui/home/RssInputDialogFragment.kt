package com.example.wirelesslocationstud.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.wirelesslocationstud.databinding.DialogRssInputBinding

class RssInputDialogFragment : DialogFragment() {

    private var _binding: DialogRssInputBinding? = null
    private val binding get() = _binding!!

    private var onRssSubmitted: ((List<Int>) -> Unit)? = null

    fun setOnRssSubmittedListener(listener: (List<Int>) -> Unit) {
        onRssSubmitted = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRssInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSubmit.setOnClickListener {
            if (validateInputs()) {
                val rssValue1 = binding.rssInputEdittext1.text.toString().toInt()
                val rssValue2 = binding.rssInputEdittext2.text.toString().toInt()
                val rssValue3 = binding.rssInputEdittext3.text.toString().toInt()

                val rssVector = listOf(rssValue1, rssValue2, rssValue3)
                onRssSubmitted?.invoke(rssVector)
                dismiss()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Clear previous errors
        binding.rssInputLayout1.error = null
        binding.rssInputLayout2.error = null
        binding.rssInputLayout3.error = null

        // Validate RSS Value 1
        val rssValue1 = binding.rssInputEdittext1.text.toString()
        if (rssValue1.isEmpty()) {
            binding.rssInputLayout1.error = "Please enter RSS Value 1"
            isValid = false
        } else {
            val value = rssValue1.toIntOrNull()
            if (value == null) {
                binding.rssInputLayout1.error = "Please enter a valid positive integer"
                isValid = false
            } else if (value < 0) {
                binding.rssInputLayout1.error = "Value must be positive"
                isValid = false
            }
        }

        // Validate RSS Value 2
        val rssValue2 = binding.rssInputEdittext2.text.toString()
        if (rssValue2.isEmpty()) {
            binding.rssInputLayout2.error = "Please enter RSS Value 2"
            isValid = false
        } else {
            val value = rssValue2.toIntOrNull()
            if (value == null) {
                binding.rssInputLayout2.error = "Please enter a valid positive integer"
                isValid = false
            } else if (value < 0) {
                binding.rssInputLayout2.error = "Value must be positive"
                isValid = false
            }
        }

        // Validate RSS Value 3
        val rssValue3 = binding.rssInputEdittext3.text.toString()
        if (rssValue3.isEmpty()) {
            binding.rssInputLayout3.error = "Please enter RSS Value 3"
            isValid = false
        } else {
            val value = rssValue3.toIntOrNull()
            if (value == null) {
                binding.rssInputLayout3.error = "Please enter a valid positive integer"
                isValid = false
            } else if (value < 0) {
                binding.rssInputLayout3.error = "Value must be positive"
                isValid = false
            }
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RssInputDialogFragment"
    }
}
