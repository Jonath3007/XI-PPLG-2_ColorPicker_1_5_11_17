package com.jonathan.picker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var buttonmove: Button
    private lateinit var buttoncont: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        emailInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) emailInput.setText("")
        }

        passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) passwordInput.setText("")
        }

        buttoncont.setOnClickListener {
            validateAndRegister()
        }

        buttonmove.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }

    private fun validateAndRegister() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Silakan email dan password")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email tidak valid")
            return
        }

        if (password.length < 6) {
            showToast("Password perlu setidaknya 6 karakter")
            return
        }

        registerUser(email, password)
    }

    private fun registerUser(email: String, password: String) {
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