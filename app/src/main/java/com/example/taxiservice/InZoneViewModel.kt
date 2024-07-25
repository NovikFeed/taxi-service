package com.example.taxiservice

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.system.exitProcess

class InZoneViewModel(application : Application, private val sharedPreference : SharedPreferenceManager, private var repository: TaxiRepository): AndroidViewModel(application) {
    private var _inZone : MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var inZone : LiveData<Boolean> = _inZone
    private lateinit var geoQuery : GeoQuery

    private var _fragmentVariant : MutableLiveData<String> = MutableLiveData<String>()
    var fragmentVariant : LiveData<String> = _fragmentVariant

    private var _costTrip : MutableLiveData<String> = MutableLiveData<String>()
    var costTrip : LiveData<String> = _costTrip

    fun getOrder(uid:String): LiveData<Order>
    {
        return repository.getOrder(uid)
    }
    fun handleOrderStatus(order:Order){
        setCost(order)
        when(order.status){
            "open" -> {
                setupGeoQuery(order.driver, LatLng(order.passagerCoordLat, order.passagerCoordLng))
            }
            "Active" -> {
                setupGeoQuery(order.driver, LatLng(order.destinationCoordLat, order.destinationCoordLng))
            }
        }
    }

    private fun setupGeoQuery(uidDriver : String, destCoordinate : LatLng){

        val geoFireRef = Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("driversLocation")
        val geoFire = GeoFire(geoFireRef)
        val currentPosition = GeoLocation(destCoordinate.latitude, destCoordinate.longitude)
        geoQuery= geoFire.queryAtLocation(currentPosition, 0.2)
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener{
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                key?.let{
                    if(it == uidDriver){
                        _inZone.postValue(true)

                    }
                }
            }

            override fun onKeyExited(key: String?) {
                key?.let{
                    if(it == uidDriver){
                        _inZone.postValue(false)

                    }
                }
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                key?.let{
                    if(it == uidDriver){
                        _inZone.postValue(true)

                    }
                }
            }

            override fun onGeoQueryReady() {
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                Log.d("GeoQueryError", error.toString())
            }

        })
    }
    fun changeOrderStatus(status : String) {
        val currentOrder = sharedPreference.getStringData("currentOrderUID")
        currentOrder?.let {
            val dbRef =
                Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference.child(
                    "orders"
                ).child(currentOrder)?.child("status")
            dbRef?.setValue(status)
        }
    }

    fun unistalGeoQuerry(){
        geoQuery.removeAllListeners()
    }
    private fun setCost(order: Order){
        _costTrip.value = order.price
    }
    fun restartApplication(context: Context) {
        val packageName = context.packageName
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    fun setOrder(){
        val user = sharedPreference.getStringData("currentUserUID")
        Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference
            .child("users").child(user!!).child("currentOrderUID").setValue("")
    }

}