package com.example.taxiservice

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    //part input
    private lateinit var inputEmail : EditText
    private lateinit var inputPassword : EditText
    //part value
    private lateinit var userEmail :String
    private  lateinit var userPassword : String
    //part system var
    private lateinit var intent: Intent
    private lateinit var auth: FirebaseAuth
    lateinit var userChoose : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginActions(view: View) {
        setView()
        if(checkInternetConnect()){
            if(checkNotEmptyInput()){
                getInputValue()
                loginInServer(userEmail,userPassword)
            }
            else{
                Toast.makeText(this,"There are empty fields", Toast.LENGTH_SHORT).show()
            }
        }
        else{
            Toast.makeText(this,"There is no internet connection", Toast.LENGTH_SHORT).show()
        }

    }
    fun checkNotEmptyInput():Boolean{
        if(inputEmail.text.toString() == ""){
            setRedBorderForInputText(inputEmail)
        }
        if(inputPassword.text.toString() == ""){
            setRedBorderForInputText(inputPassword)
        }
        return (inputEmail.text.toString() != "")&&(inputPassword.text.toString() != "")
    }
    fun setRedBorderForInputText(inputText:EditText){
        val style = inputText.background
        inputText.setBackgroundResource(R.drawable.red_border)
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            inputText.background = style
        },1000)
    }
    private fun setView(){
        inputEmail = findViewById(R.id.inputEmailLogin)
        inputPassword = findViewById(R.id.inputPasswordLogin)
    }
    fun checkInternetConnect() :Boolean{
        val context = this
        val serviceInternet = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val connectInfo = serviceInternet.activeNetworkInfo
        return (connectInfo != null)&&(connectInfo.isConnected)
    }
    fun getInputValue(){
        userEmail = inputEmail.text.toString()
        userPassword = inputPassword.text.toString()
    }
    fun loginInServer(userEmail : String, userPassword : String){
        var userChooseInFun = ""
        auth = Firebase.auth
        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(this) {task ->
            if(task.isSuccessful){
               val user = auth.currentUser
                val db = Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference
                user?.let {
                    db.child("users").child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                val userData = snapshot.getValue<User>()
                                userData?.let{
                                    userChooseInFun = userData.getChoose()
                                    setActivityForIntent(userChooseInFun)
                                    startActivity(intent)
                                    finish()

                                }
                            }
                            else{
                                Toast.makeText(baseContext, "Read data failed1", Toast.LENGTH_SHORT).show()
                            }
                        }


                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(baseContext, "Read data failed", Toast.LENGTH_SHORT).show()
                        }

                    })
                }
            }
            else{Toast.makeText(baseContext, "Authentication failed", Toast.LENGTH_SHORT).show()}
        }

    }
    fun setActivityForIntent(choose : String){
        if(choose == "driver"){
            intent = Intent(this, DriverActivity::class.java)
        }
        else if(choose == "passenger"){
            intent = Intent(this, PassagerActivity::class.java)
        }
        else{
            Toast.makeText(baseContext, "Opertunity failed", Toast.LENGTH_SHORT).show()
            intent = Intent(this, this::class.java)
        }
    }
    }