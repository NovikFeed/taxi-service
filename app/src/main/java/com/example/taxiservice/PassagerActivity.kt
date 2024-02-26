package com.example.taxiservice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationRequest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.internal.ApiKey
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PassagerActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var mMap: GoogleMap
    lateinit var fusedLocationClient : FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passager)
        var mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this,)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
       // checkEnablePosition()
//            val userCoordinat = getCoordinateUser()
//            mMap.addMarker(MarkerOptions().position(userCoordinat).title("You"))
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userCoordinat, 14f))

    }
    companion object {
        const val REQUEST_CHECK_SETTINGS = 1001
    }
    private fun checkEnablePosition(){
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val client :SettingsClient = LocationServices.getSettingsClient(this)
        val task:Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {

        }
        task.addOnFailureListener{exception ->
        if(exception is ResolvableApiException){
            try{
                exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
            }
            catch(sendEx: IntentSender.SendIntentException){

            }
        }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CHECK_SETTINGS -> {
                if(resultCode != RESULT_OK){
                    checkEnablePosition()
                }
            }
        }
    }

    fun exit(view: View) {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, ChoiceActionActivity::class.java)
        startActivity(intent)
        finish()
    }
     private fun getCoordinateUser() : LatLng {
         var cord : LatLng = LatLng(49.235743, 28.485847)
         if (ActivityCompat.checkSelfPermission(
             this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
             this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//             fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
//                 location?.let {
//                     val latitude = it.latitude
//                     val longitude = it.longitude
//                     val coordinate = LatLng(latitude, longitude)
//                     cord = coordinate
//                 }
//             }
     }

         return cord
     }

}