package com.example.taxiservice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class DriverIsFoundViewModelFactory(private val repository : TaxiRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(DriverIsFoundViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
                    return DriverIsFoundViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}