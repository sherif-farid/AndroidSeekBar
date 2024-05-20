package com.example.rangebar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat

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

fun getGradientDrawable(endColor: String, isApplyGradient: Boolean , ctx:Context):GradientDrawable{
    val gradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        val mainColor = ContextCompat.getColor(ctx, R.color.main_color)
        val parsedEndColor =  Color.parseColor(endColor)
        val end = if (isApplyGradient) parsedEndColor else mainColor
        val colors = intArrayOf(
            mainColor,
            end
        )
        this.colors = colors
        gradientType = GradientDrawable.LINEAR_GRADIENT
        orientation = GradientDrawable.Orientation.LEFT_RIGHT
    }

    return gradientDrawable
}

fun <T> List<T>.safeIndex(index: Int): T? {
    return if (this.isNotEmpty() && index >= 0 && this.size > index) this[index] else null
}
