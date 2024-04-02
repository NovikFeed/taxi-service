package com.example.taxiservice

import com.google.android.gms.maps.model.LatLng

data class Order(
    var status: String = "", var passagerCoord: LatLng = LatLng(0.0, 0.0),
    var driverCoord : LatLng = LatLng(0.0,0.0), var distinationCoord : LatLng = LatLng(0.0,0.0), var price : String = " " ) {
}