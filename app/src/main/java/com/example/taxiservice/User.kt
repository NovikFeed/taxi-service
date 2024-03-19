package com.example.taxiservice

import com.google.android.gms.maps.model.LatLng

data class User(var userName: String = "", var userEmail:String= "", var userPhone: String= "", var userChoose : String= "", var positionShared : Boolean= false, var latitude : Double = 0.0, var longitude : Double = 0.0){
    fun getChoose() : String{
        return userChoose
    }
    fun getName() :String{
        return userName
    }
    fun getEmail() :String{
        return userEmail
    }
    fun getPhone() : String{
        return userPhone
    }
    fun setChoose(choose : String){
        userChoose = choose
    }
    fun setName(name : String){
        userName = name
    }
    fun setEmail(email: String){
        userEmail = email
    }
    fun setPhone(phone : String){
        userPhone = phone
    }
    fun getCord() : LatLng{
        return LatLng(this.latitude, this.longitude)
    }
    fun getSharedPosition() : Boolean{
        return positionShared
    }
}
