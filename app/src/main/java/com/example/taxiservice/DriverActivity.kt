package com.example.taxiservice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth

class DriverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)
    }
    fun exit(view: View) {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, ChoiceActionActivity::class.java)
        startActivity(intent)
        finish()
    }
}