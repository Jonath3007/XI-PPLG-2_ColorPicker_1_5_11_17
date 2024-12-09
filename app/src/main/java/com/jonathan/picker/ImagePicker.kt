package com.jonathan.picker

import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageView
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.TextView
import android.view.MotionEvent
import android.view.View
import android.graphics.Color
import android.widget.FrameLayout
import android.widget.ImageView.ScaleType
import androidx.activity.result.contract.ActivityResultContracts
import org.w3c.dom.Text

class ImagePicker : AppCompatActivity() {
    private lateinit var bitmap: Bitmap
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_picker)

        val imageView: ImageView = findViewById(R.id.imageView3)
        val rgbTextView: TextView = findViewById(R.id.textViewrgb)
        val hexTextView: TextView = findViewById(R.id.textViewhex)
        val previewFrame: FrameLayout = findViewById(R.id.preview)

        imageView.scaleType = ScaleType.FIT_CENTER

        imageView.post {
            imageUri?.let { updateImageUri(it) }
        }
        imageView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if (::bitmap.isInitialized) {
                        try {
                            val viewWidth = view.width.toFloat()
                            val viewHeight = view.height.toFloat()

                            val relativeX = event.x / viewWidth
                            val relativeY = event.y / viewHeight

                            val x = (relativeX * bitmap.width).toInt()
                            val y = (relativeY * bitmap.height).toInt()

                            if (x in 0 until bitmap.width && y in 0 until bitmap.height) {
                                val pixelColor = bitmap.getPixel(x, y)

                                val red = Color.red(pixelColor)
                                val green = Color.green(pixelColor)
                                val blue = Color.blue(pixelColor)

                                val hexColor = String.format("#%02X%02X%02X", red, green, blue)

                                rgbTextView.text = " RGB $red $green $blue"
                                hexTextView.text = "HEX $hexColor"
                                previewFrame.setBackgroundColor(pixelColor)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    true
                }
                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun updateImageUri(uri: Uri) {
        imageUri = uri
        val imageView: ImageView = findViewById(R.id.imageView3)
        imageView.setImageURI(uri)
        imageView.post {
            val drawable = imageView.drawable
            if (drawable is BitmapDrawable) {
                bitmap = drawable.bitmap
            }
        }
    }
    private val selectImageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let { updateImageUri(it) }
        }
    }
}