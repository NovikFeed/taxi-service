package com.example.taxiservice

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.taxiservice.databinding.ActivityPassagerGoogleBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.core.Context
import java.io.IOException
import java.util.Locale

class PassagerActivityGoogle : AppCompatActivity(), OnMapReadyCallback,LocationListener, OnCameraMoveListener, OnCameraIdleListener, OnCameraMoveStartedListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPassagerGoogleBinding
    private lateinit var fusedLocation : FusedLocationProviderClient
    lateinit var inputAddress : EditText
    lateinit var autoChooseAddress : TextView
    lateinit var buttonEnableCameraMoveListener: ImageButton
    lateinit var pinChooseAddress : ImageView
    var isCameraTrakingEnable : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityPassagerGoogleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setView()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val sheet = findViewById<FrameLayout>(R.id.sheet)
        try {
            BottomSheetBehavior.from(sheet).apply {
                peekHeight = 200
                this.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        catch (e: Exception){
            Log.e("EROR", e.toString())
        }
        buttonEnableCameraMoveListener.setOnClickListener{
            chooseAddressWithPin()
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (checkEnableLocation()){
        setUpMap()}
        try {
            mMap.setOnCameraIdleListener(this)
            mMap.setOnCameraMoveStartedListener(this)
            mMap.setOnCameraMoveListener(this)
        }
         catch(e : Exception){
         }

    }

    private fun setView(){
        inputAddress = findViewById(R.id.inputAddress)
        autoChooseAddress = findViewById(R.id.autoChooseAddress)
        buttonEnableCameraMoveListener = findViewById(R.id.enableListner)
        pinChooseAddress = findViewById(R.id.pin)

    }
    fun setUpMap(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("Device", "Permission Off")
            requestPermission()
        }
            mMap.isMyLocationEnabled = true
            var userPosition : LatLng
            fusedLocation.lastLocation.addOnSuccessListener(this){location ->
                if(location != null){
                    userPosition = LatLng(location.latitude, location.longitude)
                    //placeMarkerOnMap(userPosition)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 18.0f))

                }
            }
        }

    fun placeMarkerOnMap(currentPosition : LatLng){
        val markerOptions = MarkerOptions().position(currentPosition)
        val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.user_icon)
        val marker = mMap.addMarker(markerOptions)
        marker?.setIcon(markerIcon)
    }

    fun requestPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            }
            else{
                requestPermission()
            }
        }
        else{
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CHECK_SETTINGS -> {
                if(resultCode != RESULT_OK){
                    checkEnableLocation()
                }
                else{

                }
            }
        }
    }
    private fun checkEnableLocation():Boolean{
        var check : Boolean = true
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,5000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task : Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            check = true
        }
        task.addOnFailureListener{exception ->
            check = false
            if(exception is ResolvableApiException){
                try{
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)

                }
                catch (sendEx: IntentSender.SendIntentException){}
            }
        }
            return check
    }
    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val REQUEST_CHECK_SETTINGS = 123
    }

    override fun onLocationChanged(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val adres : List<Address>? = null
        try{
            geocoder.getFromLocation(location.latitude, location.longitude, 1)
        }
        catch (e : IOException){
            Log.e("Eror", e.toString())
        }
        setAddres(adres!![0])
    }

    private fun setAddres(address: Address) {
        if(address != null){
            if(address.getAddressLine(0) != null){
                autoChooseAddress.setText(address.getAddressLine(0))
            }
//            if(address.getAddressLine(1) != null){
//                inputAddress.text.toString() + address.getAddressLine(1)
//                 }
        }
    }

    override fun onCameraMove() {
    }

    override fun onCameraIdle() {
        if(isCameraTrakingEnable) {
            var addresses: List<Address>? = null
            val geocoder = Geocoder(this, Locale.getDefault())
            try {
                addresses = geocoder.getFromLocation(
                    mMap.cameraPosition.target.latitude,
                    mMap.cameraPosition.target.longitude,
                    1
                )
                setAddres(addresses!![0])
            } catch (e: IOException) {
                Log.e("Eror", e.toString())
            } catch (e: IndexOutOfBoundsException) {
                Log.e("Eror", e.toString())
            }
        }

    }

    override fun onCameraMoveStarted(p0: Int) {
    }

    private fun chooseAddressWithPin(){
        if(autoChooseAddress.visibility == View.VISIBLE){
            isCameraTrakingEnable = false
            autoChooseAddress.visibility = View.INVISIBLE
            pinChooseAddress.visibility = View.INVISIBLE
        }
        else{
            isCameraTrakingEnable = true
            autoChooseAddress.visibility = View.VISIBLE
            pinChooseAddress.visibility = View.VISIBLE
        }
    }

}