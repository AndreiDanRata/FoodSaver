package com.example.finalyearproject.ui.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.finalyearproject.MainActivity
import com.example.finalyearproject.R
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val reg_btn: Button = findViewById(R.id.reg_button)
        reg_btn.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        val login_button = findViewById<Button>(R.id.login_button)
        login_button.setOnClickListener {
            loginUser()

        }

    }

    fun loginUser() {
        var check = true
        var email: String = findViewById<EditText>(R.id.login_email_edit_text).text.toString()
        var password: String = findViewById<EditText>(R.id.login_password_edit_text).text.toString()
        Log.d("EMAIL_LOGIN",email)
        Log.d("EMAIL_LOGIN",password)
        if(email == "") {        //TODO ADD HERE INPUT SANITATION: EMAIL FORMAT + PASSWORD MIN 6 CHARS
            Toast.makeText(this, "The email can not be empty", Toast.LENGTH_SHORT).show()
            check = false
        }
        if(password == "") {        //TODO ADD HERE INPUT SANITATION: EMAIL FORMAT + PASSWORD MIN 6 CHARS
            Toast.makeText(this, "The password can not be empty", Toast.LENGTH_SHORT).show()
            check = false
        }
        if(check) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        Toast.makeText(this, "Unable to login. Check your input or try again later", Toast.LENGTH_SHORT).show()
                    }
                }
        }


    }


}
