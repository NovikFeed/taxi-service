package com.example.taxiservice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    lateinit var auth : FirebaseAuth
    lateinit var intentForAutoAuth : Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()
        val manager = SharedPreferenceManager(this)
        val currentUser = auth.currentUser
        if(currentUser != null){
            getChoose(currentUser)
            if(!manager.checkData("currentUserUID")){
                manager.saveData("currentUserUID", currentUser.uid)
            }
        }
        else{
            val intent = Intent(this, ChoiceActionActivity::class.java)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            finish()
        },3000)
        }
    }

    fun getChoose(currentUser : FirebaseUser){
        val db = Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference
        db.child("users").child(currentUser.uid).addListenerForSingleValueEvent(object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue<User>()
                    user?.let {
                        val choose = it.getChoose()
                        setActivityForIntent(choose)
                        intentForAutoAuth.putExtra("currentUserUID", currentUser.uid)
                        startActivity(intentForAutoAuth)
                        finish()
                    }
                }
                else{
                    setActivityForIntent("false")
                    startActivity(intentForAutoAuth)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                setActivityForIntent("false")
                startActivity(intentForAutoAuth)
                finish()
            }

        })
    }

    fun setActivityForIntent(choose : String){
        if(choose == "driver"){
            intentForAutoAuth = Intent(this, DriverActivityGoogle::class.java)
        }
        else if(choose == "passenger"){
            intentForAutoAuth = Intent(this, PassagerActivityGoogle::class.java)
        }
        else{
//            Toast.makeText(baseContext, "Opertunity failed", Toast.LENGTH_SHORT).show()
            intentForAutoAuth = Intent(this, ChoiceActionActivity::class.java)
        }
    }

}