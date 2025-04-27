package com.example.lab6

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("game", Context.MODE_PRIVATE)
        val highScore = prefs.getInt("high_score", 0)
        val lastScore = prefs.getInt("last_score", 0)

        val highScoreText = findViewById<TextView>(R.id.high_score_text)
        val lastScoreText = findViewById<TextView>(R.id.last_score_text)

        highScoreText.text = "🌟 Рекорд: $highScore 🌟"
        lastScoreText.text = "🎯 Попередній результат: $lastScore"
        highScoreText.setTextColor(Color.MAGENTA)
        highScoreText.textSize = 28f

        val startButton = findViewById<Button>(R.id.start_button)
        startButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

    }
}
