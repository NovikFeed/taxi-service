package com.example.taxiservice

import android.content.Context
import android.content.SharedPreferences


class SharedPreferenceManager(context: Context) {
    private val sharedPreference : SharedPreferences = context.getSharedPreferences("OrderData", Context.MODE_PRIVATE)
    fun saveData(key : String, data: String){
        sharedPreference.edit().putString(key, data).apply()
    }
    fun saveData(key: String, data : Double){
        val changeType = data.toFloat()
        sharedPreference.edit().putFloat(key, changeType).apply()
    }
    fun removeData(key: String){
        val data = sharedPreference.getString(key, null)
        if(data != null){
            sharedPreference.edit().remove(key).apply()
        }
    }
    fun getStringData(key: String) : String?{
        val data = sharedPreference.getString(key, null)
        return data
    }
    fun getDoubleData(key: String): Double{
        return sharedPreference.getFloat(key, 0.0f).toDouble()
    }
    fun checkData(key: String): Boolean{
        val data = sharedPreference.getString(key, null)
        return data != null // якщо true  то данні в кеші вже є
    }

}