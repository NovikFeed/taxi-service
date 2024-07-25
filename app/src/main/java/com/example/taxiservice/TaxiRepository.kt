package com.example.taxiservice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TaxiRepository {
    private val db = Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("orders")
    fun getOrder(uid: String) : LiveData<Order>{
        val liveData = MutableLiveData<Order>()
        db.child(uid).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val dataOrder = snapshot.getValue<Order>()
                    dataOrder?.let {
                        liveData.value = dataOrder!!
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        return liveData
    }

}