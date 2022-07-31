package com.example.finalyearproject.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View  //check with the other import for view line14
import android.widget.EditText
import android.widget.Toast
import com.example.finalyearproject.MainActivity
import com.example.finalyearproject.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class RegistrationActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        auth = FirebaseAuth.getInstance()
        reg_button.setOnClickListener {
            registerUser()
        }
    }

    fun registerUser() {

        val email: String = findViewById<EditText>(R.id.email_edit_text).text.toString()
        val password: String = findViewById<EditText>(R.id.password_edit_text).text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
