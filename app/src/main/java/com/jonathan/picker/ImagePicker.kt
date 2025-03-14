package com.jonathan.picker

import android.content.Intent
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
import android.graphics.Color
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView.ScaleType
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

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

        // Set scale type untuk memastikan gambar tampil dengan benar
        imageView.scaleType = ScaleType.FIT_CENTER

        // Cek apakah URI gambar diterima dari intent
        val receivedUri = intent.data
        val extraUri = intent.getParcelableExtra<Uri>("imageUri")

        // Log untuk debugging
        Log.d("ImagePicker", "Intent data URI: $receivedUri")
        Log.d("ImagePicker", "Intent extra URI: $extraUri")

        // Perbarui gambar berdasarkan URI yang diterima
        when {
            receivedUri != null -> updateImageUri(receivedUri)
            extraUri != null -> updateImageUri(extraUri)
            else -> Log.d("ImagePicker", "Tidak ada URI gambar yang diterima")
        }

        // Handle touch event untuk mendeteksi warna
        imageView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if (::bitmap.isInitialized && !bitmap.isRecycled) {
                        try {
                            // Dapatkan ukuran tampilan
                            val viewWidth = view.width
                            val viewHeight = view.height

                            // Hitung rasio aspek
                            val bitmapRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                            val viewRatio = viewWidth.toFloat() / viewHeight.toFloat()

                            // Hitung ukuran gambar yang sebenarnya ditampilkan
                            val displayedWidth: Float
                            val displayedHeight: Float
                            val offsetX: Float
                            val offsetY: Float

                            if (bitmapRatio > viewRatio) {
                                // Gambar dibatasi oleh lebar
                                displayedWidth = viewWidth.toFloat()
                                displayedHeight = displayedWidth / bitmapRatio
                                offsetX = 0f
                                offsetY = (viewHeight - displayedHeight) / 2
                            } else {
                                // Gambar dibatasi oleh tinggi
                                displayedHeight = viewHeight.toFloat()
                                displayedWidth = displayedHeight * bitmapRatio
                                offsetX = (viewWidth - displayedWidth) / 2
                                offsetY = 0f
                            }

                            // Cek apakah sentuhan berada dalam area gambar
                            if (event.x < offsetX || event.x > offsetX + displayedWidth ||
                                event.y < offsetY || event.y > offsetY + displayedHeight) {
                                return@setOnTouchListener true
                            }

                            // Konversi ke koordinat bitmap
                            val bitmapX = ((event.x - offsetX) / displayedWidth * bitmap.width).toInt()
                                .coerceIn(0, bitmap.width - 1)
                            val bitmapY = ((event.y - offsetY) / displayedHeight * bitmap.height).toInt()
                                .coerceIn(0, bitmap.height - 1)

                            // Ambil warna pixel
                            val pixelColor = bitmap.getPixel(bitmapX, bitmapY)
                            val red = Color.red(pixelColor)
                            val green = Color.green(pixelColor)
                            val blue = Color.blue(pixelColor)
                            val hexColor = String.format("#%02X%02X%02X", red, green, blue)

                            // Update UI
                            rgbTextView.text = " RGB $red $green $blue"
                            hexTextView.text = "HEX $hexColor"
                            previewFrame.setBackgroundColor(pixelColor)

                            // Debug log
                            Log.d("ImagePicker", "Touch at: ($bitmapX, $bitmapY), Color: RGB($red,$green,$blue) HEX: $hexColor")
                        } catch (e: Exception) {
                            Log.e("ImagePicker", "Error mendapatkan warna: ${e.message}")
                            e.printStackTrace()
                        }
                    } else {
                        Log.e("ImagePicker", "Bitmap belum diinisialisasi atau telah di-recycle")
                    }
                    true
                }
                else -> false
            }
        }

        // Setup listeners untuk navigasi
        setupNavigationButtons()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupNavigationButtons() {
        // Menu button
        val menuImageView = findViewById<ImageView>(R.id.Menupicker)
        menuImageView.setOnClickListener {
            val intent = Intent(this@ImagePicker, ConvertCode::class.java)
            startActivity(intent)
        }

        // Navigation buttons
        val button6 = findViewById<android.widget.Button>(R.id.button6)
        button6.setOnClickListener {
            val intent = Intent(this@ImagePicker, ColorPicker::class.java)
            startActivity(intent)
        }

        val button7 = findViewById<android.widget.Button>(R.id.button7)
        button7.setOnClickListener {
            val intent = Intent(this@ImagePicker, FavColor::class.java)
            startActivity(intent)
        }

        val button8 = findViewById<android.widget.Button>(R.id.button8)
        button8.setOnClickListener {
            val intent = Intent(this@ImagePicker, ColorLevel::class.java)
            startActivity(intent)
        }
    }

    fun updateImageUri(uri: Uri) {
        try {
            Log.d("ImagePicker", "Memperbarui URI gambar: $uri")
            imageUri = uri
            val imageView: ImageView = findViewById(R.id.imageView3)

            // PERBAIKAN UTAMA: Gunakan contentResolver untuk mendapatkan bitmap langsung
            val inputStream = contentResolver.openInputStream(uri)
            bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null && !bitmap.isRecycled) {
                // Perbarui ImageView dengan bitmap yang baru dibuat
                imageView.setImageBitmap(bitmap)
                Log.d("ImagePicker", "Bitmap berhasil diperbarui: ${bitmap.width}x${bitmap.height}")
            } else {
                Log.e("ImagePicker", "Gagal memuat bitmap dari URI")
                // Fallback: Coba gunakan setImageURI
                imageView.setImageURI(uri)

                // Perbarui bitmap dari drawable
                imageView.post {
                    val drawable = imageView.drawable
                    if (drawable is BitmapDrawable) {
                        bitmap = drawable.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                        Log.d("ImagePicker", "Bitmap dari drawable: ${bitmap.width}x${bitmap.height}")
                    } else {
                        Log.e("ImagePicker", "Drawable bukan BitmapDrawable")
                        Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error dalam updateImageUri: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh bitmap jika kembali ke activity ini
        imageUri?.let {
            Log.d("ImagePicker", "OnResume: menyegarkan gambar")
            updateImageUri(it)
        }
    }
}