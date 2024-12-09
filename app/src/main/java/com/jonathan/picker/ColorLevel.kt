package com.jonathan.picker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.TextView

class ColorLevel : AppCompatActivity() {
    private lateinit var previewLayout: LinearLayout
    private lateinit var hexCodeTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_colorlevel)

        previewLayout = findViewById(R.id.previewLayout)
        hexCodeTextView = findViewById(R.id.ViewHexCode)

        val button7 = findViewById<Button>(R.id.buttonfirstly)
        val button8 = findViewById<Button>(R.id.buttonsecondly)
        val button9 = findViewById<Button>(R.id.buttonthirds)

        button7.setOnClickListener {
            val Intent = (Intent(this@ColorLevel, ColorPicker::class.java ))
            startActivity(Intent)
        }

        button8.setOnClickListener {
            val Intent = (Intent(this@ColorLevel, FavColor::class.java ))
            startActivity(Intent)
        }

        val colorLevelContainer1 = findViewById<LinearLayout>(R.id.colorLevelContainer1)
        val colorLevelContainer2 = findViewById<LinearLayout>(R.id.colorLevelContainer2)
        val colorLevelContainer3 = findViewById<LinearLayout>(R.id.colorLevelContainer3)

        val colorSteps = 20

        val baseColor1 = Color.rgb(255, 0, 0)
        val baseColor2 = Color.rgb(0, 0, 255)
        val baseColor3 = Color.rgb(255, 255, 0)

        createGradient(colorLevelContainer1, baseColor1, colorSteps)
        createGradient(colorLevelContainer2, baseColor2, colorSteps)
        createGradient(colorLevelContainer3, baseColor3, colorSteps)
    }

    private fun createGradient(container: LinearLayout, baseColor: Int, colorSteps: Int) {
        val halfSteps = colorSteps / 2

        for (i in 0 until colorSteps) {
            val colorView = View(this)
            colorView.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
            val color = if (i < halfSteps) {
                getGradientColor(Color.WHITE, baseColor, i, halfSteps)
            } else {
                getGradientColor(baseColor, Color.BLACK, i - halfSteps, halfSteps)
            }
            colorView.setBackgroundColor(color)

            colorView.setOnClickListener{
                showColorPreview(color)
            }

            container.addView(colorView)
        }
    }
    private fun getGradientColor(startColor: Int, endColor: Int, step: Int, totalSteps: Int): Int {
        val factor = step.toFloat() / (totalSteps - 1)

        val red = (Color.red(startColor) * (1 - factor) + Color.red(endColor) * factor).toInt()
        val green = (Color.green(startColor) * (1 - factor) + Color.green(endColor) * factor).toInt()
        val blue = (Color.blue(startColor) * (1 - factor) + Color.blue(endColor) * factor).toInt()

        return Color.rgb(red, green, blue)
    }

    private fun showColorPreview(color: Int) {
        previewLayout.setBackgroundColor(color)
        val hexColor = String.format("#%06X", (0xFFFFFF and color))
        hexCodeTextView.text = "Hex Code: $hexColor"
    }
}