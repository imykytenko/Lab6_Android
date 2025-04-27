package com.example.lab6
import android.graphics.Bitmap

data class Fruit(
    val bitmap: Bitmap,
    var x: Float,
    var y: Float,
    val isBomb: Boolean
)