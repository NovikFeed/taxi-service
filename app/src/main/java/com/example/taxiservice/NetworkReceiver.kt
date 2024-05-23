package com.example.taxiservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class NetworkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Втрата з'єднання з інтернетом", Toast.LENGTH_SHORT).show()
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo?.isConnected == false) {
            Toast.makeText(context, "Втрата з'єднання з інтернетом", Toast.LENGTH_SHORT).show()
            // Тут можна додати логіку для показу алерт-ділогу
        }
    }

    private fun checkConnection(context: Context?): Boolean {
        return try {
            val connectivityManager =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = connectivityManager?.activeNetwork
            val networkCapabilities = connectivityManager?.getNetworkCapabilities(network)
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            Log.d("NetworkReceiver", "Error checking internet connection", e)
            false
        }
    }
}