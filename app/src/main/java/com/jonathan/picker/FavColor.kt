package com.jonathan.picker

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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

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
        adjustForSystemBars()

        val menuImageView = findViewById<ImageView>(R.id.Menufav)
        menuImageView.setOnClickListener {
            val intent = Intent(this@FavColor, ConvertCode::class.java)
            startActivity(intent)
        }
    }
    private fun setupColorWheel(colorWheelView: ImageView) {
        colorWheelView.setOnTouchListener {_, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val bitmap = (colorWheelView.drawable as? BitmapDrawable)?.bitmap
                val color = bitmap?.let { getColorFromBitmap(it, event.x.toInt(), event.y.toInt()) }
                if (color != null) {
                    viewModel.selectColor(color)
                } else {
                    Log.d("FavColor", "Invalid color selection")
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
    private fun setupButtons(
        saveButton: Button,
        buttonFirst: Button,
        buttonThird: Button
    ) {
        saveButton.setOnClickListener{
            viewModel.saveColors()
        }
        buttonFirst.setOnClickListener {
            startActivity(Intent(this, ColorPicker::class.java))
        }
        buttonThird.setOnClickListener {
            startActivity(Intent(this, ColorLevel::class.java))
        }
    }
    private fun adjustForSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) {v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun getColorFromBitmap(bitmap: Bitmap, x: Int, y: Int): Int? {
        if (x < 0 || y < 0 || x >= bitmap.width || y>= bitmap.height) {
            return null
        }
        return bitmap.getPixel(x, y)
    }
    class ColorPickerViewModel : ViewModel() {
        private val _selectedColors = MutableLiveData<List<Int>>()
        val selectedColors: LiveData<List<Int>> = _selectedColors

        private var currentBoxIndex = 0

        fun selectColor(color: Int) {
            val colors = _selectedColors.value?.toMutableList()
                ?: mutableListOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT)
            colors[currentBoxIndex] = color
            _selectedColors.value = colors
            currentBoxIndex = (currentBoxIndex + 1) % 3
        }
        fun saveColors() {
            currentBoxIndex = 0
        }
    }
}