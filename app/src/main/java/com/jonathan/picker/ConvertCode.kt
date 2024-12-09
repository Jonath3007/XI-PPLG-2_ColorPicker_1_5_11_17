package com.jonathan.picker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ConvertCode : AppCompatActivity() {
    private lateinit var rgbCodeEditText: EditText
    private lateinit var cmykTextView: TextView
    private lateinit var previewConvert: FrameLayout
    private lateinit var convertButton: Button
    private lateinit var resetButton: Button
    private lateinit var buttonFirst: Button
    private lateinit var buttonSecond: Button
    private lateinit var buttonThird: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_convert_code)

        buttonFirst = findViewById<Button>(R.id.buttonfirs)
        buttonSecond = findViewById<Button>(R.id.buttonseco)
        buttonThird = findViewById<Button>(R.id.buttonthir)

        buttonFirst.setOnClickListener {
            startActivity(Intent(this, ColorPicker::class.java))
        }
        buttonSecond.setOnClickListener {
            startActivity(Intent(this, FavColor::class.java))
        }
        buttonThird.setOnClickListener {
            startActivity(Intent(this, ColorLevel::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rgbCodeEditText = findViewById(R.id.rgbcode)
        cmykTextView = findViewById(R.id.textcmyk)
        previewConvert = findViewById(R.id.previewconvert)
        convertButton = findViewById(R.id.convert)
        resetButton = findViewById(R.id.reset)

        convertButton.setOnClickListener {
            convertRGBtoCMYK()
        }
        resetButton.setOnClickListener {
            resetFields()
        }
    }

    private fun convertRGBtoCMYK() {
        val rgbCode = rgbCodeEditText.text.toString().trim()
        if (rgbCode.isNotEmpty()) {
            val rgbValues = rgbCode.split(",").map { it.trim().toIntOrNull() }
            if (rgbValues.size == 3 && rgbValues.all { it != null }) {
                val r = rgbValues[0]!!
                val g = rgbValues[1]!!
                val b = rgbValues[2]!!

                val cmyk = rgbToCmyk(r, g, b)
                cmykTextView.text = "CMYK: ${cmyk[0]}, ${cmyk[1]}, ${cmyk[2]}, ${cmyk[3]}"
                previewConvert.setBackgroundColor(Color.rgb(r, g, b))
            } else {
                cmykTextView.text = "Invalid RGB Input"
            }
        }
    }

    private fun rgbToCmyk(r: Int, g: Int, b: Int): IntArray {
        val c = 1 - (r / 255.0f)
        val m = 1 - (g / 255.0f)
        val y = 1 - (b / 255.0f)

        val k = minOf(c, minOf(m, y))

        return if (k == 1f) {
            intArrayOf(0, 0, 0, (k * 100).toInt())
        } else {
            intArrayOf(
                ((c - k) / (1 - k) * 100).toInt(),
                ((m - k) / (1 - k) * 100).toInt(),
                ((y - k) / (1 - k) * 100).toInt(),
                (k * 100).toInt()
            )
        }
    }

    private fun resetFields() {
        rgbCodeEditText.text.clear()
        cmykTextView.text = " "
        previewConvert.setBackgroundColor(Color.TRANSPARENT)
    }
}