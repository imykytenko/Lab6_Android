package com.example.lab6

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameOverActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val score = intent.getIntExtra("score", 0)
        val prefs = getSharedPreferences("game", Context.MODE_PRIVATE)
        prefs.edit().putInt("last_score", score).apply()

        val resultText = findViewById<TextView>(R.id.result_text)
        resultText.text = "Ваш результат: $score 🎯"

        val restartBtn = findViewById<Button>(R.id.restart_button)
        restartBtn.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }

        val backBtn = findViewById<Button>(R.id.back_button)
        backBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
