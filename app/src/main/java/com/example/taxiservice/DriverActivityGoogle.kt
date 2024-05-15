package com.example.taxiservice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract.Data
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.database.snapshots
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.protobuf.Value
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import `in`.blogspot.kmvignesh.googlemapexample.GoogleMapDTO
import `in`.blogspot.kmvignesh.googlemapexample.PolyLine
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

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
    private lateinit var firebaseManager : FirebaseManager
    private lateinit var dataBase : DatabaseReference
    private lateinit var dataBaseOrders : DatabaseReference
    private lateinit var poliLine : Polyline
    private lateinit var travelTime : String
    private lateinit var currentOrderUID : String
    private lateinit var sharedPreference : SharedPreferenceManager
    private var driverHaveOrder = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDriverGoogleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        setView()
        checkOrder()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }



    override fun onRestart() {
        super.onRestart()
        isSharedLocation = false
        geoFire.setLocation(currentUserUID, GeoLocation(0.0,0.0))
        setStyleButtonToWork()
    }

    private fun toWork() {
        setStyleButtonToWork()
        if (!isSharedLocation) {
            startLocationUpdate()
            currentUserInDB.child("positionShared").setValue(true)
            driverHaveOrder = false
            processedData()
        } else {
            fusedLocation.removeLocationUpdates(locationCallback)
            currentUserInDB.child("positionShared").setValue(false)
            geoFire.setLocation(currentUserUID, GeoLocation(0.0,0.0))

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
                val coord = GeoLocation(location.latitude, location.longitude)
                geoFire.setLocation(currentUserUID, coord){key, error ->
                    if(error != null){
                        Log.i("HER","Помилка при оновленні локації для користувача $key: ${error.message}")
                    }
                }
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
        sharedPreference = SharedPreferenceManager(this)
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
        firebaseManager = FirebaseManager()
        currentUserUID = callingIntent.getStringExtra("currentUserUID")!!
        currentUserInDB = Firebase.database.reference.child("users").child(currentUserUID)
        currentOrderUID = sharedPreference.getStringData("currentOrderUID")!!
        buttonToWork.setOnClickListener { toWork() }
        dataBase = Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("driversLocation")
        dataBaseOrders = Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("orders")
        geoFire = GeoFire(dataBase)
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(location: LocationResult) {
                super.onLocationResult(location)
                val coord = GeoLocation(location.lastLocation!!.latitude, location.lastLocation!!.longitude)
                geoFire.setLocation(currentUserUID, coord){key, error ->
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
    private fun processedData(){
        dataBaseOrders.addValueEventListener(object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
            for(data in snapshot.children){
                val dataOrder = data.getValue<Order>()
                dataOrder?.let{
                    if(!driverHaveOrder) {
                        if (it.driver == currentUserUID && it.status == "open") {
                            driverHaveOrder = true
                            currentOrderUID = data.key!!
                            val passagerAddress = getAddressFromCoordinates(
                                LatLng(
                                    it.passagerCoordLat,
                                    it.passagerCoordLng
                                )
                            )

                            val distLocation = getAddressFromCoordinates(
                                LatLng(
                                    it.destinationCoordLat,
                                    it.destinationCoordLng
                                )
                            )
                            setMyName()
                            val alertDialog = AlertDialog.Builder(this@DriverActivityGoogle)
                            alertDialog.setTitle("Order")
                            alertDialog.setMessage("$passagerAddress -> $distLocation  ${it.price}")
                            alertDialog.setCancelable(false)
                            alertDialog.setPositiveButton("Okey") { dialog, which ->
                                sharedPreference.saveData("currentOrderUID", currentOrderUID)
                                firebaseManager.setCurrentOrderUID(currentOrderUID, currentUserUID)
                                setRouteToPassanger(it)
                                setListenerForRoute()
                                nextFragment(RouteToUserFragment(), R.id.sheetDriver,currentUserUID,currentOrderUID)
                                buttonToWork.visibility = View.INVISIBLE
                                openMapApp(it.passagerCoordLat, it.passagerCoordLng  )
                            }
                            alertDialog.show()
                        }
                    }
                }
            }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("COORD", error.message)
            }

        })
    }
    private fun setRouteToPassanger(order : Order){
        mMap.clear()
        val passangerPosition = LatLng(order.passagerCoordLat, order.passagerCoordLng)
        getCurrentPosition {
            dataBaseOrders.child(currentOrderUID).child("driverCoordLat").setValue(it.latitude)
            dataBaseOrders.child(currentOrderUID).child("driverCoordLng").setValue(it.longitude)
            val driverPosition = it
            val url = getDirectionURL(driverPosition, passangerPosition)
            GetDirection(url).execute()
        }
        val marker = mMap.addMarker(MarkerOptions().position(passangerPosition))
        marker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.passenger))
    }
    private fun setRouteWithPassanger(order : Order){
        mMap.clear()
        val destPosition = LatLng(order.destinationCoordLat, order.destinationCoordLng)
        getCurrentPosition {
            firebaseManager.setDriverCoord(currentOrderUID, it)
            val driverPosition = it
            val url = getDirectionURL(driverPosition, destPosition)
            GetDirection(url).execute()
        }
        val marker = mMap.addMarker(MarkerOptions().position(destPosition))
        marker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.user_icon))
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val REQUEST_CHECK_SETTINGS = 123
    }
    private fun getAddressFromCoordinates(coord : LatLng): String{
        val geocoder = Geocoder(this@DriverActivityGoogle)
        var addressResult = ""
        try{
            val addresses = geocoder.getFromLocation(coord.latitude, coord.longitude, 1)
            if(addresses!!.isNotEmpty()){
                val address = addresses[0]
                val addressParts= mutableListOf<String>()
                for(i in 0..address.maxAddressLineIndex){
                    addressParts.add(address.getAddressLine(i))
                }
                addressResult = addressParts.joinToString(separator = ", ")
            }
        }
        catch (e : IOException){
            e.printStackTrace()
        }
        return addressResult
    }
    @SuppressLint("MissingPermission")
    private fun getCurrentPosition(callback : (LatLng) -> Unit){

        fusedLocation.lastLocation.addOnSuccessListener(this) {location ->
            if(location != null) {

                val userPosition = LatLng(location.latitude, location.longitude)
                callback(userPosition)
            }

        }
    }
    private fun getDirectionURL(origin: LatLng, dest: LatLng): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=${
            getString(
                R.string.google_map_api_key_for_request
            )
        }"
    }

    private inner class GetDirection(val url: String) :
        AsyncTask<Void, Void, List<List<LatLng>>>() {
        override fun doInBackground(vararg p0: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)
                val path = ArrayList<LatLng>()
                var distance = 0.0
                for (i in 0..(respObj.routes[0].legs[0].steps.size - 1)) {
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                    distance += respObj.routes[0].legs[0].distance.value
                }
                travelTime = ("Driver will arrive via " + (distance/1000*1.5).roundToInt().toString() + " min")
                dataBaseOrders.child(currentOrderUID).child("timeToUser").setValue(travelTime)

                result.add(path)


            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineOption = PolylineOptions()
            for (i in result.indices) {
                lineOption.addAll(result[i])
                lineOption.width(13f)
                lineOption.color(Color.BLACK)
                lineOption.geodesic(true)
            }
            if (::poliLine.isInitialized) {
                poliLine.remove()
            }
            poliLine = mMap.addPolyline(lineOption)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.cameraPosition.target, 13f))
        }

    }

    fun decodePolyline(encoded: String): List<LatLng> {

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

            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }
    private fun openMapApp(destLat: Double, destLng : Double){
         val uri = "geo:$destLat,$destLng"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.waze")
        if(intent.resolveActivity(packageManager) != null){
            startActivity(intent)
        }
        else{
            Toast.makeText(this@DriverActivityGoogle, "Waze application is missing", Toast.LENGTH_SHORT).show()
        }

    }
    private fun setMyName(){
        currentUserInDB.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val data = snapshot.getValue<User>()
                    data?.let {
                        dataBaseOrders.child(currentOrderUID).child("driverName").setValue(it.userName)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun nextFragment(nextFragment: Fragment, thisFragment : Int){
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(thisFragment, nextFragment).commit()
    }
    private fun nextFragmentWithInfo(nextFragment: Fragment, thisFragment: Int){
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("info", "i")
        nextFragment.arguments = bundle
        transaction.replace(thisFragment, nextFragment).commit()
    }
    private fun getPassengerCoord(orderUID: String, callback: (DoubleArray) -> Unit){
        dataBaseOrders.child(orderUID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val data = snapshot.getValue<Order>()
                    data?.let {
                        val arr = DoubleArray(2)
                        arr.set(0, it.passagerCoordLat)
                        arr.set(1, it.passagerCoordLng)
                        callback(arr)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun nextFragment(nextFragment: Fragment, thisFragment : Int, driverUid : String, orderUID : String){
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        getPassengerCoord(orderUID) {
            sharedPreference.saveData("driverUID", driverUid)
            sharedPreference.saveData("passengerLat", it[0])
            sharedPreference.saveData("passengerLng", it[1])
            transaction.replace(thisFragment, nextFragment).commit()
        }
    }
    private fun checkOrder(){
        val thisFragment = R.id.sheetDriver
        val nextFragment = RouteToUserFragment()
        val viewModel = ViewModelProvider(this, ViewModelWithSharedPReferenceFactory(this.application, sharedPreference))[InZoneViewModel::class.java]
        var check = false
        val firebaseManager = FirebaseManager()
        val userUID = sharedPreference.getStringData("currentUserUID")
            if (userUID != null) {
                firebaseManager.getUser(userUID!!) { user ->
                    val orderUID = user.currentOrderUID
                    firebaseManager.getOrder(orderUID) { order ->
                        saveCoordUser(order)
                        if(!check){
                        if (order.status == "open") {
                            startLocationUpdate()
                            setRouteToPassanger(order)
                            buttonToWork.visibility = View.INVISIBLE
                            nextFragment(nextFragment, thisFragment)
                            setListenerForRoute()
                            check = true
                        } else if (order.status == "Active") {
                            startLocationUpdate()
                            setRouteWithPassanger(order)
                            buttonToWork.visibility = View.INVISIBLE
                            nextFragmentWithInfo(nextFragment, thisFragment)
                            check = true
                        }
                        }
                    }
                }
        }
    }
    private fun saveCoordUser(order : Order){
        sharedPreference.saveData("passengerLat", order.passagerCoordLat)
        sharedPreference.saveData("passengerLng", order.passagerCoordLng)

    }

    private fun setListenerForRoute(){
        val order = dataBaseOrders.child(currentOrderUID)
        order.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val order = snapshot.getValue<Order>()
                    order?.let {
                        if(it.status == "Active"){
                            setRouteWithPassanger(it)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }


}
