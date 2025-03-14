package com.jonathan.picker

import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var continueButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var resendVerificationText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.e("Login", "Gagal mengaktifkan persistensi Firebase", e)
        }

        firebaseAuth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        emailInput = findViewById(R.id.username)
        passwordInput = findViewById(R.id.password)
        continueButton = findViewById(R.id.buttoncontinue)

        val savedEmail = sharedPreferences.getString("email", "")
        val savedPassword = sharedPreferences.getString("password", "")

        if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            emailInput.setText(savedEmail)
            passwordInput.setText(savedPassword)

        }

        continueButton.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Silakan isi kedua bidang dengan benar")
            return
        }

        tryFirebaseLogin(email, password)
    }

    private fun tryFirebaseLogin(email: String, password: String) {
        if (isNetworkAvailable()) {
            showToast("Mencoba login...")
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        if (user != null) {
                            if (user.isEmailVerified) {
                                saveLoginData(email, password)

                                syncFavColors(user.uid)

                                showToast("Login Berhasil")
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                showToast("Silakan verifikasi email Anda terlebih dahulu")
                                sendVerificationEmail()
                                firebaseAuth.signOut()
                            }
                        } else {
                            showToast("Gagal mendapatkan data pengguna")
                        }
                    } else {
                        val exception = task.exception
                        if (exception is FirebaseAuthInvalidUserException) {
                            showToast("Akun tidak ditemukan! Silakan daftar terlebih dahulu.")
                        } else {
                            showToast("Login gagal: ${exception?.localizedMessage}")
                        }
                    }
                }
        } else {
            showToast("Tidak ada koneksi internet! Silakan coba lagi nanti.")
        }
    }

    private fun sendVerificationEmail() {
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Email verifikasi telah dikirim ke alamat email Anda")
                } else {
                    showToast("Gagal mengirim email verifikasi: ${task.exception?.message}")
                }
            }
    }

    private fun sendVerificationEmail(email: String) {
        firebaseAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (!signInMethods.isNullOrEmpty()) {
                        showToast("Silakan login terlebih dahulu untuk mengirim email verifikasi")
                    } else {
                        showToast("Email belum terdaftar")
                    }
                } else {
                    showToast("Gagal memeriksa email: ${task.exception?.message}")
                }
            }
    }

    private fun saveLoginData(email: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
        Log.d("Login", "Data login berhasil disimpan ke SharedPreferences")
    }

    private fun syncFavColors(uid: String) {
        Log.d("Login", "Mulai sinkronisasi warna favorit untuk UID: $uid")

        val userColorsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("userColors")

        userColorsRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                try {
                    val color1 = snapshot.child("color1").getValue(String::class.java) ?: "#000000"
                    val color2 = snapshot.child("color2").getValue(String::class.java) ?: "#000000"
                    val color3 = snapshot.child("color3").getValue(String::class.java) ?: "#000000"

                    val prefName = "ColorPrefs_$uid"
                    getSharedPreferences(prefName, MODE_PRIVATE).edit().apply {
                        putString("color1", color1)
                        putString("color2", color2)
                        putString("color3", color3)
                        putLong("timestamp", System.currentTimeMillis())
                        apply()
                    }

                    sharedPreferences.edit().putString("favcolor", color1).apply()

                    Log.d("Login", "Warna favorit berhasil disinkronkan: $color1, $color2, $color3")
                } catch (e: Exception) {
                    Log.e("Login", "Error memproses warna dari Firebase", e)
                }
            } else {
                val defaultColors = listOf("#000000", "#FFFFFF", "#FF0000")

                val colorData = hashMapOf(
                    "color1" to defaultColors[0],
                    "color2" to defaultColors[1],
                    "color3" to defaultColors[2],
                    "timestamp" to System.currentTimeMillis()
                )

                userColorsRef.setValue(colorData)
                    .addOnSuccessListener {
                        Log.d("Login", "Warna default berhasil disimpan ke Firebase")

                        FirebaseDatabase.getInstance().getReference("users").child(uid)
                            .child("favcolor").setValue(defaultColors[0])
                    }

                val prefName = "ColorPrefs_$uid"
                getSharedPreferences(prefName, MODE_PRIVATE).edit().apply {
                    putString("color1", defaultColors[0])
                    putString("color2", defaultColors[1])
                    putString("color3", defaultColors[2])
                    putLong("timestamp", System.currentTimeMillis())
                    apply()
                }

                sharedPreferences.edit().putString("favcolor", defaultColors[0]).apply()

                Log.d("Login", "Tidak ada warna favorit ditemukan, menggunakan default")
            }
        }.addOnFailureListener { e ->
            Log.e("Login", "Gagal mendapatkan warna favorit dari Firebase", e)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}