package com.example.taxiservice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DriverIsFoundViewModel(private val repository : TaxiRepository) : ViewModel() {
    fun getOrder(uid:String): LiveData<Order>
    {
       return repository.getOrder(uid)
    }
}