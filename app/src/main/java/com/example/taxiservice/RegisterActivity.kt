package com.example.taxiservice

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.logging.Handler

class RegisterActivity : AppCompatActivity() {
    //part view
    private lateinit var inputName : EditText
    private lateinit var inputEmail : EditText
    private lateinit var inputPhone:EditText
    private lateinit var inputPassword : EditText
    private lateinit var chooseWhoAreYou : Switch
    private lateinit var buttonRegister : Button
    private lateinit var textErrorLengthPassword : TextView
    //part data var
    private lateinit var userName :String
    private lateinit var userEmail:String
    private lateinit var  userPhoneNumber : String
    private lateinit var userPassword : String
    private lateinit var userChose : String
    //part system var
   private lateinit var auth:FirebaseAuth
   private lateinit var intent : Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setView()
    }
    fun setView(){
        inputName = findViewById(R.id.inputName)
        inputEmail = findViewById(R.id.inputEmail)
        inputPhone = findViewById(R.id.inputPhoneNumber)
        inputPassword = findViewById(R.id.inputPassword)
        chooseWhoAreYou = findViewById(R.id.choiceWho)
        buttonRegister = findViewById(R.id.regButton)
        textErrorLengthPassword = findViewById(R.id.errorLengthPass)
    }
    fun getViewValues(){
        userName = inputName.text.toString()
        userEmail = inputEmail.text.toString()
        userPhoneNumber = inputPhone.text.toString()
        userPassword = inputPassword.text.toString()
        userChose = if(chooseWhoAreYou.isChecked){
            "driver"
        } else{
            "passenger"
        }
    }
    fun checkNotEmptyInput():Boolean{
        if(inputName.text.toString() == ""){
            setRedBorderForInputText(inputName)
        }
        if(inputEmail.text.toString() == ""){
            setRedBorderForInputText(inputEmail)
        }
        if(inputPhone.text.toString() == ""){
            setRedBorderForInputText(inputPhone)
        }
        if(inputPassword.text.toString() == ""){
            setRedBorderForInputText(inputPassword)
        }
        if(inputPassword.text.toString().length<6){
            setRedBorderForInputText(inputPassword)
            setVisibleErrorLengthPassword(textErrorLengthPassword)
        }
        return (inputName.text.toString() != "")&&(inputEmail.text.toString() != "")&&(inputPhone.text.toString() != "")&&(inputPassword.text.toString() != "")&&(inputPassword.text.toString().length>=6)
    }
    fun setRedBorderForInputText(inputText:EditText){
        val style = inputText.background
        inputText.setBackgroundResource(R.drawable.red_border)
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            inputText.background = style
        },3000)
    }

    fun registerAction(view: View) {
        if(checkInternetConnect()){
            if(checkNotEmptyInput()){
                getViewValues()
                saveUser(userName,userEmail,userPhoneNumber,userPassword,userChose)


            }
            else{
                Toast.makeText(this,"There are empty fields", Toast.LENGTH_SHORT).show()
            }
        }
        else{
            Toast.makeText(this,"There is no internet connection", Toast.LENGTH_SHORT).show()
        }
    }
     fun checkInternetConnect() :Boolean{
       val context = this
        val serviceInternet = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val connectInfo = serviceInternet.activeNetworkInfo
        return (connectInfo != null)&&(connectInfo.isConnected)
    }
    fun saveUser(userName : String, userEmail: String, userPhone: String, userPassword:String, userChose:String){
        if(!checkInBase(userEmail)){
            auth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    val db = Firebase.database("https://taxiservice-ef804-default-rtdb.europe-west1.firebasedatabase.app/").reference
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.let{
                        val userUid = it.uid
                        val firebaseUser = User(userName,userEmail,userPhone,userChose)
                        db.child("users").child(userUid).setValue(firebaseUser).addOnSuccessListener {

                            Toast.makeText(baseContext,"Account data saved successfully", Toast.LENGTH_SHORT).show()
                            nextActivity()
                        }.addOnFailureListener { e ->
                            Log.e(TAG, "Error saving user data", e)}
                    resetInputText()
                    }
                }
                else{
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()

                }
            }
        }
        else{
            Toast.makeText(this,"A user with this email is in the system", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkInBase(email: String):Boolean{
        auth = FirebaseAuth.getInstance()
        var check :Boolean = false
            auth.fetchSignInMethodsForEmail(email).addOnCompleteListener{task ->
                check = task.isSuccessful
            }
        return check
    }
    private fun resetInputText(){
        inputName.setText("")
        inputEmail.setText("")
        inputPhone.setText("")
        inputPassword.setText("")
        chooseWhoAreYou.setOnCheckedChangeListener(null)

    }
    private fun setActivityForIntent(choose : String){
        if(choose == "driver"){
            intent = Intent(this, DriverActivity::class.java)
        }
        else if(choose == "passenger"){
            intent = Intent(this, PassagerActivity::class.java)
        }
    }

    private fun setVisibleErrorLengthPassword(textView: TextView){
        textView.visibility = View.VISIBLE
        android.os.Handler(Looper.getMainLooper()).postDelayed({textView.visibility = View.INVISIBLE},3000)
    }
    private fun nextActivity(){
        setActivityForIntent(userChose)
                startActivity(intent)
                finish()

    }


}