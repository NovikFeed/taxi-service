package com.example.taxiservice

data class User(var userName: String = "", var userEmail:String= "", var userPhone: String= "", var userChoose : String= ""){
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
}
