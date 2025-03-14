package com.jonathan.picker

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        syncOnlineWhenAvailable()

        val imageView6 = findViewById<ImageView>(R.id.imageView6)
        val textView9 = findViewById<TextView>(R.id.textView9)
        val imageView7 = findViewById<ImageView>(R.id.imageView7)
        val textView2 = findViewById<TextView>(R.id.textView2)
        val imageView8 = findViewById<ImageView>(R.id.imageView8)
        val textView10 = findViewById<TextView>(R.id.textView10)

        imageView6.setOnClickListener {
            startActivity(Intent(this, ColorPicker::class.java))
        }
        textView9.setOnClickListener {
            startActivity(Intent(this, ColorPicker::class.java))
        }
        imageView7.setOnClickListener {
            startActivity(Intent(this, FavColor::class.java))
        }
        textView2.setOnClickListener {
            startActivity(Intent(this, FavColor::class.java))
        }
        imageView8.setOnClickListener {
            startActivity(Intent(this, ColorLevel::class.java))
        }
        textView10.setOnClickListener {
            startActivity(Intent(this, ColorLevel::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val menuImageView = findViewById<ImageView>(R.id.Menucolpick)
        menuImageView.setOnClickListener {
            val intent = Intent(this@MainActivity, ConvertCode::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        syncOnlineWhenAvailable()
    }

    fun syncOnlineWhenAvailable() {
        if (!isNetworkAvailable()) return

        val pendingSync = sharedPreferences.getBoolean("pendingSync", false)
        if (pendingSync) {
            val email = sharedPreferences.getString("email", null) ?: return
            val password = sharedPreferences.getString("password", null) ?: return

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        syncFavColor()
                        sharedPreferences.edit().putBoolean("pendingSync", false).apply()
                    } else {
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { registerTask ->
                                if (registerTask.isSuccessful) {
                                    val favColor = sharedPreferences.getString("favcolor", "#FFFFFF") ?: "#FFFFFF"
                                    val user = firebaseAuth.currentUser ?: return@addOnCompleteListener
                                    val userRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid)
                                    userRef.child("favcolor").setValue(favColor)
                                    sharedPreferences.edit().putBoolean("pendingSync", false).apply()
                                }
                            }
                    }
                }
        }
    }

    private fun syncFavColor() {
        val user = firebaseAuth.currentUser ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid)
        userRef.child("favcolor").get().addOnSuccessListener {
            val favColor = it.value?.toString() ?: "#FFFFFF"
            sharedPreferences.edit().putString("favcolor", favColor).apply()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}