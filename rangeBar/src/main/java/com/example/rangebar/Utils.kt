package com.example.rangebar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

@SuppressLint("MissingPermission")
fun vibrate(duration: Long, context:Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    vibrator?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
}

fun getGradientDrawable(startColor:String , endColor: String):GradientDrawable{
    val gradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        val colors = intArrayOf(
            Color.parseColor(startColor),
            Color.parseColor(endColor)
        )
        this.colors = colors
        gradientType = GradientDrawable.LINEAR_GRADIENT
        orientation = GradientDrawable.Orientation.LEFT_RIGHT
    }

    return gradientDrawable
}