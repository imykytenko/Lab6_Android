package com.example.lab6

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.media.MediaPlayer
import android.view.MotionEvent
import android.view.SurfaceView
import kotlin.math.abs
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), Runnable {

    @Volatile
    private var playing = true
    private var thread: Thread? = null
    private var canvas: Canvas? = null
    private val paint = Paint()

    private val originalBasket = BitmapFactory.decodeResource(resources, R.drawable.basket)
    private val basket = Bitmap.createScaledBitmap(originalBasket, 200, 200, true)
    private var basketX = 500f
    private var basketY = 1700f
    private var basketSpeed = 0f

    private val fruits = mutableListOf<Fruit>()
    private val fruitBitmaps = listOf(
        R.drawable.fruit1,
        R.drawable.fruit2,
        R.drawable.fruit3,
        R.drawable.fruit4,
        R.drawable.fruit5
    ).map {
        val original = BitmapFactory.decodeResource(resources, it)
        Bitmap.createScaledBitmap(original, 100, 100, true)
    }

    private val bombBitmap = Bitmap.createScaledBitmap(
        BitmapFactory.decodeResource(resources, R.drawable.bomb),
        100, 100, true
    )

    private val screenWidth = resources.displayMetrics.widthPixels
    private val screenHeight = resources.displayMetrics.heightPixels

    private val gameDuration = 30000L
    private var remainingTime = gameDuration
    private var gameStartTime = 0L
    private var pausedAt = 0L

    private var score = 0
    private var highScore = 0

    private val prefs: SharedPreferences =
        context.getSharedPreferences("game", Context.MODE_PRIVATE)

    private val fruitSound = MediaPlayer.create(context, R.raw.fruit_catch)
    private val bombSound = MediaPlayer.create(context, R.raw.bomb_hit)

    init {
        highScore = prefs.getInt("high_score", 0)
        spawnFruit()
        gameStartTime = System.currentTimeMillis()
        thread = Thread(this)
        thread?.start()
    }

    override fun run() {
        while (playing) {
            update()
            draw()
            control()
        }
    }

    private fun spawnFruit() {
        fruits.add(
            Fruit(
                fruitBitmaps.random(),
                Random.nextInt(0, screenWidth - 200).toFloat(),
                -200f,
                false
            )
        )
        if (Random.nextInt(10) < 2) {
            fruits.add(
                Fruit(
                    bombBitmap,
                    Random.nextInt(0, screenWidth - 200).toFloat(),
                    -200f,
                    true
                )
            )
        }
    }

    private fun update() {
        basketX += basketSpeed
        if (basketX < 0) basketX = 0f
        if (basketX > screenWidth - basket.width) basketX = (screenWidth - basket.width).toFloat()

        val iterator = fruits.iterator()
        while (iterator.hasNext()) {
            val fruit = iterator.next()
            fruit.y += 10f

            if (fruit.y > screenHeight) {
                iterator.remove()
                continue
            }

            if (abs(fruit.x - basketX) < 100 && abs(fruit.y - basketY) < 100) {
                if (fruit.isBomb) {
                    score--
                    bombSound.start()
                } else {
                    score++
                    fruitSound.start()
                }
                iterator.remove()
            }
        }

        if (Random.nextInt(100) < 4) {
            spawnFruit()
        }

        if (score > highScore) {
            highScore = score
            prefs.edit().putInt("high_score", highScore).apply()
        }

        val elapsed = System.currentTimeMillis() - gameStartTime
        if (elapsed >= remainingTime) {
            playing = false
            (context as? Activity)?.runOnUiThread {
                val intent = Intent(context, GameOverActivity::class.java)
                intent.putExtra("score", score)
                context.startActivity(intent)
                (context as Activity).finish()
            }
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            canvas = holder.lockCanvas()
            canvas?.drawColor(Color.rgb(255, 242, 228))

            paint.textSize = 60f
            paint.color = Color.BLACK
            canvas?.drawText("Score: $score", 50f, 100f, paint)
            canvas?.drawText("Best: $highScore", 50f, 170f, paint)

            val elapsed = System.currentTimeMillis() - gameStartTime
            val timeLeft = (remainingTime - elapsed).coerceAtLeast(0L)
            val progress = timeLeft.toFloat() / gameDuration

            val barLeft = 50f
            val barTop = 200f
            val barRight = screenWidth - 50f
            val barBottom = 240f

            paint.color = Color.LTGRAY
            canvas?.drawRect(barLeft, barTop, barRight, barBottom, paint)

            paint.color = Color.parseColor("#FF5722")
            canvas?.drawRect(
                barLeft,
                barTop,
                barLeft + (barRight - barLeft) * progress,
                barBottom,
                paint
            )

            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            canvas?.drawRect(barLeft, barTop, barRight, barBottom, paint)
            paint.style = Paint.Style.FILL

            canvas?.drawBitmap(basket, basketX, basketY, paint)
            val fruitsCopy: List<Fruit>
            synchronized(this) {
                fruitsCopy = fruits.toList()
            }
            for (fruit in fruitsCopy) {
                canvas?.drawBitmap(fruit.bitmap, fruit.x, fruit.y, paint)
            }
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun control() {
        Thread.sleep(17)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (it.action == MotionEvent.ACTION_MOVE) {
                basketX = it.x - basket.width / 2
            }
        }
        return true
    }

    fun pause() {
        playing = false
        pausedAt = System.currentTimeMillis()
        thread?.join()
        val elapsed = pausedAt - gameStartTime
        remainingTime -= elapsed
    }

    fun resume() {
        playing = true
        gameStartTime = System.currentTimeMillis()
        thread = Thread(this)
        thread?.start()
    }
}