package com.example.wirelesslocationstud.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wirelesslocationstud.data.repository.WirelessMapRepository

class FormViewModelFactory(
    private val repository: WirelessMapRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FormViewModel::class.java)) {
            return FormViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

