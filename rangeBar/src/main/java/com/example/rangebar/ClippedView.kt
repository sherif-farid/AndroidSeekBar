package com.example.rangebar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class ClippedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {
        private val paint = Paint()
        private val transPaint = Paint()
        private val mPath = Path()

    init {
        paint.color = Color.parseColor("#D4D4D4")//D4D4D4
        transPaint.color = Color.TRANSPARENT
        mPath.fillType = Path.FillType.INVERSE_EVEN_ODD
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mPath.reset()
        mPath.moveTo((width /2).toFloat(), (height  /2).toFloat())
        val min = Math.min(width , height)
        mPath.addRoundRect(
            (width/-2).toFloat(), 0f ,(width/2 ).toFloat(), (height).toFloat() ,
            (min /2).toFloat(), (min /2).toFloat() ,Path.Direction.CW)
        canvas?.drawPath(mPath, paint)
    }
}