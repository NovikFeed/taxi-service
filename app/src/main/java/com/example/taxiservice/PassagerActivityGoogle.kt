package com.example.taxiservice

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView

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
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.taxiservice.databinding.ActivityPassagerGoogleBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
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
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.core.Context
import com.google.gson.Gson
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import `in`.blogspot.kmvignesh.googlemapexample.GoogleMapDTO
import `in`.blogspot.kmvignesh.googlemapexample.PolyLine
import java.io.IOException
import java.util.Locale

class PassagerActivityGoogle : AppCompatActivity(), OnMapReadyCallback,LocationListener, OnCameraMoveListener, OnCameraIdleListener, OnCameraMoveStartedListener {

    private lateinit var mMap: GoogleMap
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var binding: ActivityPassagerGoogleBinding
    private lateinit var fusedLocation : FusedLocationProviderClient
    private var markerFindAddressWithSearchView : Marker? = null
    private lateinit var autoChooseAddress : TextView
    private lateinit var buttonEnableCameraMoveListener: ImageButton
    private lateinit var pinChooseAddress : ImageView
    private lateinit var bottomSheet :  BottomSheetBehavior<FrameLayout>
    private lateinit var buttonSetRoute : Button
    private lateinit var poliLine: Polyline
    private var isCameraTrakingEnable : Boolean = false
    private var chooseAddressWithPin : LatLng = LatLng(0.0,0.0)
    private var chooseAddressWithSearch : LatLng =  LatLng(0.0,0.0)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityPassagerGoogleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setView()
        autoComplete()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val sheet = findViewById<FrameLayout>(R.id.sheet)
        try {
            bottomSheet = BottomSheetBehavior.from(sheet)
                bottomSheet.apply {
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
        buttonSetRoute.setOnClickListener{setRoute()}

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (checkEnableLocation()){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i("Device", "Permission Off")
                requestPermission()
            }
            setStartPosition()}
        try {
            mMap.setOnCameraIdleListener(this)
            mMap.setOnCameraMoveStartedListener(this)
            mMap.setOnCameraMoveListener(this)
        }
         catch(e : Exception){
         }

    }

    private fun setView(){
        buttonSetRoute = findViewById(R.id.buttonSetRout)
        autoChooseAddress = findViewById(R.id.autoChooseAddress)
        buttonEnableCameraMoveListener = findViewById(R.id.enableListner)
        pinChooseAddress = findViewById(R.id.pin)

    }
    fun setStartPosition(){
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
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 18.0f))

            }
        }
    }
    private fun getUserPosition(callback:(LatLng) -> Unit){
         var userPosition : LatLng = LatLng(0.0,0.0)
        if(checkEnableLocation()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission()
            }

            fusedLocation.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {

                    userPosition = LatLng(location.latitude, location.longitude)
                    callback(userPosition)
                }
            }
        }
    }

    private fun requestPermission(){
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
            autoChooseAddress.visibility = View.VISIBLE
            if(address.getAddressLine(0) != null){
                autoChooseAddress.setText(address.getAddressLine(0))
            }
//            if(address.getAddressLine(1) != null){
//                autoChooseAddress.text.toString() + address.getAddressLine(1)
//                 }
        }
    }

    override fun onCameraMove() {
    }

    override fun onCameraIdle() {
        if(isCameraTrakingEnable) {
            if(bottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED){
                bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            }
            var addresses: List<Address>? = null
            val geocoder = Geocoder(this, Locale.getDefault())
            try {
                addresses = geocoder.getFromLocation(
                    mMap.cameraPosition.target.latitude,
                    mMap.cameraPosition.target.longitude,
                    1
                )
                chooseAddressWithPin = mMap.cameraPosition.target
                setAddres(addresses!![0])
            } catch (e: IOException) {
                Log.e("Eror", e.toString())
            } catch (e: IndexOutOfBoundsException) {
                Log.e("Eror", e.toString())
            }
        }

    }

    override fun onCameraMoveStarted(p0: Int) {
        if(bottomSheet.state == BottomSheetBehavior.STATE_EXPANDED && isCameraTrakingEnable){
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun chooseAddressWithPin(){
        if(autoChooseAddress.visibility == View.VISIBLE){
            isCameraTrakingEnable = false
            pinChooseAddress.visibility = View.INVISIBLE
            autoChooseAddress.visibility = View.INVISIBLE
        }
        else{
            if(markerFindAddressWithSearchView != null){
                markerFindAddressWithSearchView?.remove()
            }
            isCameraTrakingEnable = true
            autoChooseAddress.visibility = View.VISIBLE
            pinChooseAddress.visibility = View.VISIBLE
        }
    }
    private fun hideBottomSheet(){
        bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
    }
    private fun autoComplete(){
        if(Places.isInitialized()){
            Places.deinitialize()
        }
        Places.initialize(this, getString(R.string.google_map_api_key))
        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.fragmentAutocomplete)
                as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleteFragment.setCountries("UA")
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener{
            override fun onError(p0: Status) {
                Toast.makeText(this@PassagerActivityGoogle, "Error search place", Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng
                setMarkerAndCamera(latLng)
                chooseAddressWithSearch = latLng
            }

        })
    }
        private fun setMarkerAndCamera(latLng: LatLng){
            if(markerFindAddressWithSearchView != null){
                markerFindAddressWithSearchView?.remove()
            }
            markerFindAddressWithSearchView = mMap.addMarker(MarkerOptions().position(latLng))!!
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        }
// this block for draw route between location user and chooses location
    private fun setRoute(){
            lateinit var userPosition : LatLng
            var destPosition: LatLng = LatLng(0.0,0.0)
            getUserPosition {
                userPosition = it
                Log.i("Coord", userPosition.toString())
                if (isCameraTrakingEnable && chooseAddressWithPin != LatLng(0.0,0.0)) {
                    setMarkerAndCamera(chooseAddressWithPin)
                    chooseAddressWithPin()
                    destPosition = chooseAddressWithPin

                } else if(!isCameraTrakingEnable && chooseAddressWithSearch != LatLng(0.0,0.0)){
                    destPosition = chooseAddressWithSearch
                }
                else {
                    Toast.makeText(this@PassagerActivityGoogle, "You have not selected a destination", Toast.LENGTH_SHORT).show()
                }
                if(destPosition != LatLng(0.0,0.0) && userPosition != LatLng(0.0,0.0)){
                    hideBottomSheet()
                    val URL = getDirectionURL(userPosition, destPosition)
                    GetDirection(URL).execute()
                }
                else{
                    Toast.makeText(this@PassagerActivityGoogle, "You have not selected a destination", Toast.LENGTH_SHORT).show()

                }
            }
            }
    private fun getDirectionURL(origin: LatLng, dest: LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=${getString(R.string.google_map_api_key_for_request)}"
    }
    private inner class GetDirection(val url : String) : AsyncTask<Void,Void,List<List<LatLng>>>(){
        override fun doInBackground(vararg p0: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            Log.d("GoogleMap" , " data : $data")
            val result = ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)
                val path = ArrayList<LatLng>()
                for(i in 0..(respObj.routes[0].legs[0].steps.size-1)) {
//                    val startLatLng = LatLng(
//                        respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble(),
//                        respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble()
//                    )
//                    path.add(startLatLng)
//                    val destLatLng = LatLng(
//                        respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble(),
//                        respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble()
//                    )
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }
            catch(e : Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineOption = PolylineOptions()
            for(i in result.indices){
                lineOption.addAll(result[i])
                lineOption.width(13f)
                lineOption.color(Color.BLACK)
                lineOption.geodesic(true)
            }
            if(::poliLine.isInitialized){
                poliLine.remove()
            }
            poliLine = mMap.addPolyline(lineOption)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.cameraPosition.target, 13f))
        }

    }
    public fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }

    }

