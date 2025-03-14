package com.jonathan.picker

import android.annotation.SuppressLint
import android.net.Uri
import android.app.Activity
import android.provider.MediaStore
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts

class ColorPicker : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_colorpicker)

        val imageView1 = findViewById<ImageView>(R.id.imageView1)
        val textView10 = findViewById<TextView>(R.id.textView10_)
        val textView11 = findViewById<TextView>(R.id.textView11)
        val bitmap = (imageView1.drawable as BitmapDrawable).bitmap
        val frameLayout = findViewById<FrameLayout>(R.id.framePreview)
        val uploadImage = findViewById<Button>(R.id.button3)
        val button7 = findViewById<Button>(R.id.buttonfavcolor)
        val button8 = findViewById<Button>(R.id.buttoncolorlevel)

        val pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    selectedImageUri = data?.data

                    selectedImageUri?.let { uri ->
                        try {
                            Log.d("ColorPicker", "Image selected: $uri")
                            // Set di ColorPicker dulu
                            imageView1.setImageURI(uri)

                            // Kirim ke ImagePicker dengan Extra
                            val intent = Intent(this, ImagePicker::class.java)
                            intent.putExtra("imageUri", uri)
                            startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("ColorPicker", "Error loading image: ${e.message}")
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        imageView1.post{
            val drawable = imageView1.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap

                imageView1.setOnTouchListener { v, event ->
                    val viewWidth = v.width.toFloat()
                    val viewHeight = v.height.toFloat()

                    val relativeX = event.x / viewWidth
                    val relativeY = event.y / viewHeight

                    val x = (relativeX * bitmap.width).toInt()
                    val y = (relativeY * bitmap.height).toInt()

                    if (x >= 0 && x < bitmap.width && y >= 0 && y < bitmap.height) {
                        val pixelColor = bitmap.getPixel(x, y)
                        val red = Color.red(pixelColor)
                        val green = Color.green(pixelColor)
                        val blue = Color.blue(pixelColor)
                        val hexCode = String.format("#%02X%02X%02X", red, green, blue)

                        textView10.text = "RGB: $red, $green, $blue"
                        textView11.text = "HEX: $hexCode"

                        frameLayout.setBackgroundColor(Color.rgb(red, green, blue))
                    }
                    true
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textView10.setOnClickListener{
            copyToClipboard("RGB", textView10.text.toString())
        }

        textView11.setOnClickListener{
            copyToClipboard("HEX", textView11.text.toString())
        }

        uploadImage.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        button7.setOnClickListener{
            startActivity(Intent(this, FavColor::class.java))
        }

        button8.setOnClickListener{
            startActivity(Intent(this, ColorLevel::class.java))
        }

        val menuImageView = findViewById<ImageView>(R.id.Menucolpick)
        menuImageView.setOnClickListener {
            val intent = Intent(this@ColorPicker, ConvertCode::class.java)
            startActivity(intent)
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}