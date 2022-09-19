package com.example.finalyearproject.ui.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View  //check with the other import for view line14
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.example.finalyearproject.MainActivity
import com.example.finalyearproject.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

/**
 * Activity responsible for Registration Screen
 */
class RegistrationActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    /**
     * Initializing the registration activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        reg_button.setOnClickListener {
            registerUser()
        }
    }

    /**
     *  Method responsible for account creation with input sanitation.
     * It also informs the users about cases when input does not respect the min characters length, fields not being empty,etc
     */
    fun registerUser() {
        val email: String = findViewById<EditText>(R.id.email_edit_text).text.toString()
        val password: String = findViewById<EditText>(R.id.password_edit_text).text.toString()
        val repassword: String = findViewById<EditText>(R.id.re_password_edit_text).text.toString()
        Log.d("REGISTRATION", "$email $password")
        if(email == "") {
            Toast.makeText(this, "The email is empty!", Toast.LENGTH_SHORT).show()
        } else if(password != repassword) {
            Toast.makeText(this, "The passwords do not match!", Toast.LENGTH_SHORT).show()
        } else if(password.length < 6) {
            Toast.makeText(this, "The passwords must be at least 6 characters long!", Toast.LENGTH_SHORT).show()
        }
        else {
            auth.createUserWithEmailAndPassword(trim(email), password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        Toast.makeText(this, "The email address is not valid or is linked to another account!", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    /**
     *  Method removes whitespaces from the beginning and from the end of a string
     */
    fun trim(s: String): String {
        return s.replace("^\\s+|\\s+$".toRegex(), "")
    }


   
}
