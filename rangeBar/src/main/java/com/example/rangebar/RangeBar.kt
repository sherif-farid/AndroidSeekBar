package com.example.rangebar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.example.rangebar.databinding.RangeBarBinding


/*
 * Created by Sherif farid
 * Date: 10/19/2023.
 * email: sherffareed39@gmail.com.
 * phone: 00201007538470
 */


@SuppressLint("ClickableViewAccessibility,SetTextI18n")
class RangeBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), View.OnTouchListener {
    private var binding: RangeBarBinding
    private val tag = "RangeBarTag"
    private var mWidth = 0
    private var thumbWidth = 0
    private var maxVisualRange = 100
    private var minVisualRange = 15
    private var availableVisualRange = maxVisualRange - minVisualRange
    private var availableTrack = 0
    private var step = 5
    private var isLogEnabled = true
    private var isRoundToMin = false
    private val percentFlag = " %"

    fun interface OnRangeChanged {
        fun onChange(rangeValue: Int)
    }

    var onRangeChanged: OnRangeChanged? = null
    private fun logs(log: String) {
        if (!isLogEnabled) return
        Log.v(tag, log)
    }

    private fun roundToStep(value: Int): Int {
        val r = value % step
        if (r == 0) return value
        return if (isRoundToMin) {
            value - r
        } else {
            value + step - r
        }
    }

    private fun initValues(attrs: AttributeSet?) {
        try {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RangeBar)
            minVisualRange = typedArray.getInt(R.styleable.RangeBar_min, minVisualRange)
            maxVisualRange = typedArray.getInt(R.styleable.RangeBar_max, maxVisualRange)
            step = typedArray.getInt(R.styleable.RangeBar_step, step)
            isLogEnabled = typedArray.getBoolean(R.styleable.RangeBar_log_enabled, isLogEnabled)
            isRoundToMin = typedArray.getBoolean(R.styleable.RangeBar_round_to_min, isRoundToMin)
            val trackShape = typedArray.getDrawable(R.styleable.RangeBar_main_track_shape)
            val selectedTrackShape =
                typedArray.getDrawable(R.styleable.RangeBar_selected_track_shape)
            val thumbShape = typedArray.getDrawable(R.styleable.RangeBar_thumb_shape)
            val textShape = typedArray.getDrawable(R.styleable.RangeBar_text_shape)
            binding.mainTrack.background = trackShape
            binding.rangeView.background = selectedTrackShape
            binding.thumb.background = thumbShape
            binding.et.background = textShape
            typedArray.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
            logs("initValues e $e")
        }
    }

    init {
        binding = RangeBarBinding.inflate(
            LayoutInflater.from(context),
            this,
            false
        )
        this.addView(binding.root)
        binding.root.post {
            mWidth = binding.mainTrack.width
            thumbWidth = binding.thumb.width
            availableTrack = mWidth - thumbWidth
            binding.rangeView.updateLayoutParams {
                this.width = availableTrack
            }
            logs("mWidth $mWidth thumbWidth $thumbWidth availableTrack $availableTrack")

        }
        initValues(attrs)
        binding.root.setOnTouchListener(this)
        binding.et.isCursorVisible = false
        binding.et.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                return@setOnEditorActionListener inputsFromEditText()
            } else {
                logs("setOnEditorActionListener false")
                return@setOnEditorActionListener false
            }

        }
    }

    private fun inputsFromEditText(): Boolean {
        return try {
            var currentTxt = binding.et.text.toString().replace(percentFlag , "").toInt()
            if (currentTxt < minVisualRange) currentTxt = minVisualRange
            if (currentTxt > maxVisualRange) currentTxt = maxVisualRange
            currentTxt = roundToStep(currentTxt)
            binding.et.setText("$currentTxt$percentFlag")
            val percent = (currentTxt.toFloat() - minVisualRange) / availableVisualRange.toFloat()
            val x = percent * availableTrack.toFloat()
            logs("setOnEditorActionListener currentTxt $currentTxt x $x percent $percent")
            moveThumb(availableTrack - x, true)
            triggerCallBack()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            logs("setOnEditorActionListener e $e")
            false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun moveThumb(x: Float, isFromText: Boolean = false) {
        logs("moveThumb x $x current x ${binding.thumb.translationX}")
        var offsetX = x
        if (offsetX < 0) {
            logs("moveThumb x $x offsetX $offsetX return under minimum")
            offsetX = 0f
        }
        if (x > availableTrack) {
            logs("moveThumb x $x offsetX $offsetX return more than maximum")
            offsetX = availableTrack.toFloat()
        }
        binding.thumb.translationX = offsetX
        binding.et.translationX = offsetX
        logs("after moveThumb current x ${binding.thumb.translationX}")
        binding.rangeView.updateLayoutParams {
            this.width = availableTrack - offsetX.toInt()
        }
        if (!isFromText) {
            val percent = (availableTrack - offsetX) / availableTrack.toFloat()
            val txt = (percent * availableVisualRange) + minVisualRange
            val txtRounded = roundToStep(txt.toInt())
            logs("moveThumb percent $percent txt $txt")
            binding.et.setText("$txtRounded$percentFlag")
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                true
            }
            MotionEvent.ACTION_MOVE -> {
                moveThumb(event.x)
                true
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                logs("ACTION_UP event.x ${event.x}")
                inputsFromEditText()
            }
            else -> false
        }
    }

    private fun triggerCallBack() {
        try {
            onRangeChanged?.onChange(binding.et.text.toString().replace(percentFlag , "").toInt())
        } catch (e: Exception) {
            e.printStackTrace()
            logs("triggerCallBack e :$e")
        }
    }
}