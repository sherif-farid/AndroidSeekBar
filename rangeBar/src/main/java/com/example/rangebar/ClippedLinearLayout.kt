package com.example.rangebar

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout

open class ClippedLinearLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    private  val rect = RectF()
    private val path = Path()
    private  var radius: FloatArray
    private val allRadius = 60
    init {
        radius = floatArrayOf(
            allRadius.toPx(),
            allRadius.toPx(),
            allRadius.toPx(),
            allRadius.toPx(),
            allRadius.toPx(),
            allRadius.toPx(),
            allRadius.toPx(),
            allRadius.toPx()
        )
    }
    private fun Int.toPx(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )
    }
    override fun onDraw(canvas: Canvas?) {
        rect.left = 0f
        rect.top = 0f
        rect.right = this.width.toFloat()
        rect.bottom = this.height.toFloat()
        path.addRoundRect(rect, radius, Path.Direction.CW)
        canvas?.clipPath(path)
        super.onDraw(canvas)
    }
}