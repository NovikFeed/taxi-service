package com.example.taxiservice

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelWithSharedPReferenceFactory(private val application: Application,
                                           private val sharedPreference : SharedPreferenceManager)
    : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(InZoneViewModel::class.java)){
            return InZoneViewModel(application, sharedPreference) as T
        }
        throw IllegalArgumentException("Unknown ViewMode Class ")
    }
}