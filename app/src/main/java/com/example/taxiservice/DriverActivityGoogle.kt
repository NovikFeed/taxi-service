package com.example.taxiservice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.ContactsContract.Data
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.taxiservice.databinding.ActivityDriverGoogleBinding
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class DriverActivityGoogle : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDriverGoogleBinding
    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var bottomSheet: BottomSheetBehavior<FrameLayout>
    private lateinit var buttonToWork: Button
    private lateinit var callingIntent: Intent
    private var isSharedLocation: Boolean = false
    private lateinit var currentUserInDB: DatabaseReference
    private lateinit var currentUserUID: String
    private lateinit var geoFire: GeoFire
    private lateinit var dataBase : DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDriverGoogleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        setView()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onStop() {
        super.onStop()
        fusedLocation.removeLocationUpdates(locationCallback)
        setSharedPositionAfterStop()
    }

    override fun onRestart() {
        super.onRestart()
        isSharedLocation = false
        setStyleButtonToWork()
    }

    private fun toWork() {
        setStyleButtonToWork()
        if (!isSharedLocation) {
            startLocationUpdate()
            currentUserInDB.child("positionShared").setValue(true)
        } else {
            fusedLocation.removeLocationUpdates(locationCallback)
            currentUserInDB.child("positionShared").setValue(false)

        }
    }
    private fun setStartLocationAfterStartListener(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            requestPermission()
        }
        fusedLocation.lastLocation.addOnSuccessListener { location ->
            if(location != null){
                currentUserInDB.child("latitude").setValue(location.latitude)
                currentUserInDB.child("longitude").setValue(location.longitude)
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        setStartLocationAfterStartListener()
        fusedLocation.requestLocationUpdates(locationRequest,locationCallback,
            Looper.getMainLooper())
    }

    private fun setSharedPositionAfterStop() {
        currentUserInDB.child("positionShared").setValue(false)
    }

    private fun setStyleButtonToWork() {
        if (!isSharedLocation) {
            isSharedLocation = true
            buttonToWork.text = "to work"
            buttonToWork.background = getDrawable(R.color.purple)
        } else {
            isSharedLocation = false
            buttonToWork.text = "not work"
            buttonToWork.background = getDrawable(R.color.grey)
        }

    }

    private fun setView() {
        buttonToWork = findViewById(R.id.buttonWork)
        callingIntent = intent
        val sheet = findViewById<FrameLayout>(R.id.sheetDriver)
        bottomSheet = BottomSheetBehavior.from(sheet)
        bottomSheet.apply {
            bottomSheet.peekHeight = 400
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100).apply {
            setMinUpdateIntervalMillis(10)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

        currentUserUID = callingIntent.getStringExtra("currentUserUID")!!
        currentUserInDB = Firebase.database.reference.child("users").child(currentUserUID)
        buttonToWork.setOnClickListener { toWork() }
        dataBase = Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("users")
        geoFire = GeoFire(dataBase)
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(location: LocationResult) {
                super.onLocationResult(location)
                val coord = GeoLocation(location.lastLocation!!.latitude, location.lastLocation!!.longitude)
                geoFire.setLocation("location", coord){key, error ->
                    if (error != null) {
                        Log.i("HER","Помилка при оновленні локації для користувача $key: ${error.message}")
                    } else {
                        Log.i("HER","Локація для користувача $key була успішно оновлена.")
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (checkEnableLocation()) {

        }
        setDriverPosition()
    }

    private fun checkEnableLocation(): Boolean {
        var check = true
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnFailureListener { exception ->
            check = false
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        this,
                        DriverActivityGoogle.REQUEST_CHECK_SETTINGS
                    )

                } catch (sendEx: IntentSender.SendIntentException) {

                }
            }
        }
        return check
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != RESULT_OK) {
            checkEnableLocation()
        }
    }

    private fun setDriverPosition() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        } else {
            mMap.isMyLocationEnabled = true
            fusedLocation.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f))
                }
            }

        }


    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setDriverPosition()
            } else {
                requestPermission()
            }
        }

    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val REQUEST_CHECK_SETTINGS = 123
    }


}
