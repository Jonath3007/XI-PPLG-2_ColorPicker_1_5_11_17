package com.jonathan.picker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
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
            startActivity(Intent (this, ColorPicker::class.java))
        }
        imageView7.setOnClickListener {
            startActivity(Intent(this, FavColor::class.java))
        }
        textView2.setOnClickListener {
            startActivity(Intent (this, FavColor::class.java))
        }
        imageView8.setOnClickListener {
            startActivity(Intent(this, ColorLevel::class.java))
        }
        textView10.setOnClickListener {
            startActivity(Intent (this, ColorLevel::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}