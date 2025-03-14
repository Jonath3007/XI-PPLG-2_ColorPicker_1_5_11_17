package com.jonathan.picker

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavColor : AppCompatActivity() {
    private val viewModel: ColorPickerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav_color)

        val colorWheelView = findViewById<ImageView>(R.id.rgbcircle)
        val colorBoxes = listOf(
            findViewById<View>(R.id.frameLayout1),
            findViewById<View>(R.id.frameLayout2),
            findViewById<View>(R.id.frameLayout3)
        )
        val hexCodeViews = listOf(
            findViewById<TextView>(R.id.hexCode1),
            findViewById<TextView>(R.id.hexCode2),
            findViewById<TextView>(R.id.hexCode3)
        )
        val saveButton = findViewById<Button>(R.id.savebutton)
        val buttonFirst = findViewById<Button>(R.id.buttonfirst)
        val buttonThird = findViewById<Button>(R.id.buttonthirdly)

        setupViewModel(colorBoxes, hexCodeViews)
        setupColorWheel(colorWheelView)
        setupButtons(saveButton, buttonFirst, buttonThird)
        setupCopyHexCodeFunctionality(hexCodeViews)
        adjustForSystemBars()

        viewModel.initialize(this)
        viewModel.loadSavedColors()

        findViewById<ImageView>(R.id.Menufav).setOnClickListener {
            startActivity(Intent(this@FavColor, ConvertCode::class.java))
        }
    }

    private fun setupCopyHexCodeFunctionality(hexCodeViews: List<TextView>) {
        hexCodeViews.forEach { textView ->
            textView.setOnClickListener {
                val hexCode = textView.text.toString()
                if (hexCode.isNotEmpty() && hexCode != "#") {
                    copyToClipboard(hexCode)
                    Toast.makeText(this, "Hex code $hexCode disalin", Toast.LENGTH_SHORT).show()
                }
            }

            // Make it visually clear these are clickable
            textView.isClickable = true
            textView.isFocusable = true
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Hex Color Code", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    private fun setupColorWheel(colorWheelView: ImageView) {
        colorWheelView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                (colorWheelView.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                    val color = getColorFromBitmap(bitmap, event.x.toInt(), event.y.toInt())
                    color?.let { viewModel.selectColor(it) }
                }
            }
            true
        }
    }

    private fun setupViewModel(colorBoxes: List<View>, hexCodeViews: List<TextView>) {
        viewModel.selectedColors.observe(this, Observer { colors ->
            colors.forEachIndexed { index, color ->
                if (index < colorBoxes.size) {
                    colorBoxes[index].setBackgroundColor(color)
                    hexCodeViews[index].text = String.format("#%06X", 0xFFFFFF and color)
                }
            }
        })
    }

    private fun setupButtons(saveButton: Button, buttonFirst: Button, buttonThird: Button) {
        saveButton.setOnClickListener {
            viewModel.saveColors()
            Toast.makeText(this, "Warna berhasil disimpan", Toast.LENGTH_SHORT).show()
        }
        buttonFirst.setOnClickListener { startActivity(Intent(this, ColorPicker::class.java)) }
        buttonThird.setOnClickListener { startActivity(Intent(this, ColorLevel::class.java)) }
    }

    private fun adjustForSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getColorFromBitmap(bitmap: Bitmap, x: Int, y: Int): Int? {
        return if (x in 0 until bitmap.width && y in 0 until bitmap.height) {
            bitmap.getPixel(x, y)
        } else null
    }

    class ColorPickerViewModel : ViewModel() {
        private val _selectedColors = MutableLiveData<List<Int>>().apply {
            value = listOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT)
        }
        val selectedColors: LiveData<List<Int>> = _selectedColors

        private var currentBoxIndex = 0
        private lateinit var database: FirebaseDatabase
        private lateinit var auth: FirebaseAuth
        private var userId: String? = null
        private lateinit var appContext: Context
        private var isInitialized = false

        fun initialize(context: AppCompatActivity) {
            if (isInitialized) return

            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            } catch (e: Exception) {
                Log.e("ColorPickerViewModel", "Gagal mengaktifkan persistensi Firebase", e)
            }

            database = FirebaseDatabase.getInstance()
            auth = FirebaseAuth.getInstance()
            appContext = context.applicationContext

            // Dapatkan UID pengguna saat ini, jika tidak ada, gunakan "guest"
            userId = auth.currentUser?.uid ?: "guest"

            Log.d("ColorPickerViewModel", "Inisialisasi dengan UID: $userId")
            isInitialized = true
        }

        fun selectColor(color: Int) {
            _selectedColors.value = _selectedColors.value?.toMutableList()?.apply {
                this[currentBoxIndex] = color
            }
            currentBoxIndex = (currentBoxIndex + 1) % 3
        }

        fun saveColors() {
            val colors = _selectedColors.value ?: return
            val colorStrings = colors.map { String.format("#%06X", 0xFFFFFF and it) }

            val user = auth.currentUser
            if (user != null) {
                val uid = user.uid

                val colorData = hashMapOf(
                    "color1" to colorStrings[0],
                    "color2" to colorStrings[1],
                    "color3" to colorStrings[2],
                    "timestamp" to System.currentTimeMillis()
                )

                database.reference.child("users").child(uid).child("userColors")
                    .setValue(colorData)
                    .addOnSuccessListener {
                        Log.d("ColorPickerViewModel", "Warna berhasil disimpan ke Firebase untuk UID: $uid")

                        database.reference.child("users").child(uid).child("favcolor")
                            .setValue(colorStrings[0])
                            .addOnSuccessListener {
                                Log.d("ColorPickerViewModel", "FavColor berhasil disimpan ke user data: ${colorStrings[0]}")
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("ColorPickerViewModel", "Error menyimpan warna ke Firebase", e)
                    }

                saveToSharedPreferences(colorStrings, uid)
            } else {
                saveToSharedPreferences(colorStrings, "guest")
            }
        }

        private fun saveToSharedPreferences(colorStrings: List<String>, uid: String) {
            val prefName = "ColorPrefs_$uid"
            appContext.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().apply {
                putString("color1", colorStrings[0])
                putString("color2", colorStrings[1])
                putString("color3", colorStrings[2])
                putLong("timestamp", System.currentTimeMillis())
                apply()
            }
            Log.d("ColorPickerViewModel", "Warna disimpan ke SharedPreferences dengan ID: $prefName")
        }

        fun loadFromSharedPreferences(): Boolean {
            val uid = auth.currentUser?.uid ?: "guest"
            val prefName = "ColorPrefs_$uid"

            val prefs = appContext.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            if (!prefs.contains("color1")) return false

            try {
                val color1 = prefs.getString("color1", "#000000") ?: "#000000"
                val color2 = prefs.getString("color2", "#000000") ?: "#000000"
                val color3 = prefs.getString("color3", "#000000") ?: "#000000"

                _selectedColors.postValue(
                    listOf(
                        Color.parseColor(color1),
                        Color.parseColor(color2),
                        Color.parseColor(color3)
                    )
                )
                Log.d("ColorPickerViewModel", "Warna dimuat dari SharedPreferences ($prefName): $color1, $color2, $color3")
                return true
            } catch (e: Exception) {
                Log.e("ColorPickerViewModel", "Error memuat warna dari SharedPreferences", e)
                return false
            }
        }

        fun loadSavedColors() {
            val loadedFromPrefs = loadFromSharedPreferences()

            val user = auth.currentUser
            val databaseRef = if (user != null) {
                database.reference.child("users").child(user.uid).child("userColors")
            } else {
                database.reference.child("guestColors")
            }

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        try {
                            val color1 = snapshot.child("color1").getValue(String::class.java) ?: "#000000"
                            val color2 = snapshot.child("color2").getValue(String::class.java) ?: "#000000"
                            val color3 = snapshot.child("color3").getValue(String::class.java) ?: "#000000"

                            _selectedColors.postValue(
                                listOf(
                                    Color.parseColor(color1),
                                    Color.parseColor(color2),
                                    Color.parseColor(color3)
                                )
                            )

                            // Update SharedPreferences dengan data dari Firebase
                            val uid = user?.uid ?: "guest"
                            saveToSharedPreferences(listOf(color1, color2, color3), uid)

                            Log.d("ColorPickerViewModel", "Warna dimuat dari Firebase: $color1, $color2, $color3")
                        } catch (e: Exception) {
                            Log.e("ColorPickerViewModel", "Error memproses warna dari Firebase", e)
                        }
                    } else if (!loadedFromPrefs) {
                        Log.d("ColorPickerViewModel", "Tidak ada warna tersimpan, menggunakan default")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ColorPickerViewModel", "Firebase error: ${error.message}")
                }
            })
        }
    }
}