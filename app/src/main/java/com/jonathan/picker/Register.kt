package com.jonathan.picker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var buttonmove: Button
    private lateinit var buttoncont: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        emailInput = findViewById(R.id.usernamereg)
        passwordInput = findViewById(R.id.passwordreg)
        buttonmove = findViewById(R.id.buttonmove)
        buttoncont = findViewById(R.id.buttoncont)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        buttoncont.setOnClickListener {
            registerUser()
        }

        buttonmove.setOnClickListener {
        val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
    private fun registerUser() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please fill in both of them")
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showToast("Registration Successful")
                    startActivity(Intent(this, Login::class.java))
                    finish()
                } else {
                    showToast("Registration Failed: ${task.exception?.message}")
                }
            }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}