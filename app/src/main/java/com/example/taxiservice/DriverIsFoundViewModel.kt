package com.example.taxiservice

import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.api.Context

class DriverIsFoundViewModel(private val repository : TaxiRepository) : ViewModel() {

    private var _fragmentVariant : MutableLiveData<String> = MutableLiveData<String>()
    var fragmentVariant : LiveData<String> = _fragmentVariant

    private var _phoneNumberDriver : MutableLiveData<String> = MutableLiveData<String>()
    var phoneNumberDriver : LiveData<String> = _phoneNumberDriver
    fun getOrder(uid:String): LiveData<Order>
    {
       return repository.getOrder(uid)
    }


    fun setPhoneNumber(driverUID : String){
        val manager = FirebaseManager()
        manager.getPhoneNumber(driverUID){
            _phoneNumberDriver.value = it
        }
    }
    fun callDriver(context: android.content.Context, phone: String){
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        context.startActivity(intent)
    }
}