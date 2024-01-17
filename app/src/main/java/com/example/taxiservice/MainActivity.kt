package com.example.taxiservice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent(this, ChoiceActionActivity::class.java)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            finish()
        },3000)
    }
}