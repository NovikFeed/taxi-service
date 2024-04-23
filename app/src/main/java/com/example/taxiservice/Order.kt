package com.example.taxiservice

import com.google.android.gms.maps.model.LatLng

data class Order(
    var status: String = "", var passagerCoordLat: Double = 0.0, var passagerCoordLng: Double = 0.0,
    var driverCoordLat : Double = 0.0, var driverCoordLng : Double = 0.0, var destinationCoordLat : Double = 0.0, var destinationCoordLng : Double = 0.0, var price : String = " ", var driver : String = " " , var driverName : String= "", var timeToUser : String = "",
    var inZone : Boolean = false) {
}