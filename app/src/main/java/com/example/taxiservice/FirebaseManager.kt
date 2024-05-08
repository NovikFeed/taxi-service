package com.example.taxiservice

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class OrderManager {
    private val databaseRef = Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference
    private fun getOrderInClass(uid : String, callback : (Order) -> Unit){
        val dbOrder = databaseRef.child("order").child(uid)
        dbOrder.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val dataOrder = snapshot.getValue<Order>()
                    dataOrder?.let { callback(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}