package com.example.taxiservice

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseManager {
    private val databaseRef = Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference
    private val usersReference = databaseRef.child("users")
    private val ordersReference = databaseRef.child("orders")
     fun getOrder(uid : String, callback : (Order) -> Unit){
        ordersReference.child(uid).addValueEventListener(object : ValueEventListener{
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
     fun getUser(uidUser: String, callback: (User) -> Unit){
         usersReference.child(uidUser).addValueEventListener(object : ValueEventListener{
             override fun onDataChange(snapshot: DataSnapshot) {
                 if(snapshot.exists()){
                     val dataUser = snapshot.getValue<User>()
                     dataUser?.let{
                         callback(it)
                     }
                 }
             }

             override fun onCancelled(error: DatabaseError) {
             }

         })
     }
    fun setCurrentOrderUID(currentOrderUID: String, userUID: String){
        val dbOrder = usersReference.child(userUID).child("currentOrderUID")
        dbOrder.setValue(currentOrderUID)
    }
    fun setDriverCoord(orderUID : String, coord: LatLng){
        ordersReference.child(orderUID).child("driverCoordLat").setValue(coord.latitude)
        ordersReference.child(orderUID).child("driverCoordLng").setValue(coord.longitude)
    }
    fun getPhoneNumber(driverUID: String, callback: (String) -> Unit){
        usersReference.child(driverUID).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val data = snapshot.getValue<User>()
                    data?.let{
                        callback(it.getPhone())
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}