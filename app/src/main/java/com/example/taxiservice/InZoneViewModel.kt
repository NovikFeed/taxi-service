package com.example.taxiservice

import android.util.Log
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

class InZoneViewModel : ViewModel() {
    private var _inZone : MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var inZone : LiveData<Boolean> = _inZone
    private lateinit var geoQuery : GeoQuery

    fun setupGeoQuery(uidDriver : String, positionUser: DoubleArray){
        val geoFireRef = Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("driversLocation")
        val geoFire = GeoFire(geoFireRef)
        val currentPosition = GeoLocation(positionUser[0],positionUser[1])
        geoQuery= geoFire.queryAtLocation(currentPosition, 1.0)
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
                Log.d("COORD", "REady")
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                Log.d("COORD", error.toString())
            }

        })
    }
}