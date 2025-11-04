package com.pyinsights.reminderapp.screens.rdp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pyinsights.reminderapp.repository.MainRepository

class RDPViewModelFactory(private val repository: MainRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RDPViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RDPViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
