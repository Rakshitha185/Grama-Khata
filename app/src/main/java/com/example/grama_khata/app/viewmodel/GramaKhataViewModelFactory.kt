package com.example.grama_khata.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GramaKhataViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GramaKhataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GramaKhataViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}