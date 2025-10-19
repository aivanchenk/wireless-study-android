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

    private var onRssSubmitted: ((String) -> Unit)? = null

    fun setOnRssSubmittedListener(listener: (String) -> Unit) {
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
            val rssVector = binding.rssInputEdittext.text.toString()
            if (rssVector.isNotEmpty()) {
                onRssSubmitted?.invoke(rssVector)
                dismiss()
            } else {
                binding.rssInputLayout.error = "Please enter an RSS vector"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RssInputDialogFragment"
    }
}
