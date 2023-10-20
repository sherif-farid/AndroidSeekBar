package com.example.rangebar

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
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
    private var minVisualRange = 0
    private var availableVisualRange = maxVisualRange - minVisualRange
    private var availableTrack = 0
    private var step = 1
    private var isLogEnabled = false
    private var isRoundToMin = false
    private var defaultValue = maxVisualRange
    private var textLayoutWidth = 0

    fun interface OnRangeChanged {
        fun onChange(rangeValue: Int , isMoving:Boolean)
    }

    var onRangeChanged: OnRangeChanged? = null
    private fun logs(log: String) {
        if (!isLogEnabled) return
        Log.v(tag, log)
    }
    fun setDefaultValue(value:Int){
        this.defaultValue = value
        logs("setDefaultValue $defaultValue")
        setText("$defaultValue")
        inputsFromEditText()
    }
    private fun setText(txt:String){
        binding.et.setText(txt)
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
            defaultValue  = typedArray.getInt(R.styleable.RangeBar_default_value, maxVisualRange)
            step = typedArray.getInt(R.styleable.RangeBar_step, step)
            isLogEnabled = typedArray.getBoolean(R.styleable.RangeBar_log_enabled, isLogEnabled)
            isRoundToMin = typedArray.getBoolean(R.styleable.RangeBar_round_to_min, isRoundToMin)
            val trackShape = typedArray.getDrawable(R.styleable.RangeBar_main_track_shape)
            val selectedTrackShape = typedArray.getDrawable(R.styleable.RangeBar_selected_track_shape)
            val thumbShape = typedArray.getDrawable(R.styleable.RangeBar_thumb_shape)
            val textShape = typedArray.getDrawable(R.styleable.RangeBar_text_shape)
            val trackHeight = typedArray.getDimensionPixelSize(R.styleable.RangeBar_track_height , 0)
            val thumbSize = typedArray.getDimensionPixelSize(R.styleable.RangeBar_thumb_size , 0)
            binding.mainTrack.background = trackShape
            binding.rangeView.background = selectedTrackShape
            binding.thumb.background = thumbShape
            binding.textLayout.background = textShape
            if (trackHeight > 0){
                binding.mainTrack.updateLayoutParams {
                    this.height = trackHeight
                }
                binding.rangeView.updateLayoutParams {
                    this.height = trackHeight
                }
            }
            if (thumbSize > 0){
                binding.thumb.updateLayoutParams {
                    this.height = thumbSize
                    this.width = thumbSize
                }
            }
            typedArray.recycle()
            availableVisualRange = maxVisualRange - minVisualRange
            logs("initValues thumbSize $thumbSize trackHeight $trackHeight minVisualRange $minVisualRange maxVisualRange $maxVisualRange" +
                    " step $step isRoundToMin $isRoundToMin availableVisualRange $availableVisualRange")
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
        initValues(attrs)
        binding.root.post {
            mWidth = binding.mainTrack.width
            thumbWidth = binding.thumb.width
            availableTrack = mWidth - thumbWidth
            textLayoutWidth = binding.textLayout.width
            binding.rangeView.updateLayoutParams {
                this.width = availableTrack
            }
            logs("mWidth $mWidth thumbWidth $thumbWidth availableTrack $availableTrack")
            setDefaultValue(defaultValue)
        }
        binding.et.filters = arrayOf(InputFilter.LengthFilter("$maxVisualRange".length))
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
        binding.textLayout.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            textLayoutWidth = binding.textLayout.width
            updateTextPositionToThumb()
        }
        binding.et.addTextChangedListener {
            binding.et.setSelection(it?.length?:0)
        }
    }

    private fun inputsFromEditText(): Boolean {
        return try {
            var currentTxt = binding.et.text.toString().ifEmpty {"0"}.toInt()
            if (currentTxt < minVisualRange) currentTxt = minVisualRange
            if (currentTxt > maxVisualRange) currentTxt = maxVisualRange
            currentTxt = roundToStep(currentTxt)
            setText("$currentTxt")
            val percent = (currentTxt.toFloat() - minVisualRange) / availableVisualRange.toFloat()
            val x = percent * availableTrack.toFloat()
            logs("setOnEditorActionListener currentTxt $currentTxt x $x percent $percent")
            moveThumb(availableTrack - x, true)
            triggerCallBack(false)
            false
        } catch (e: Exception) {
            e.printStackTrace()
            logs("setOnEditorActionListener e $e")
            false
        }
    }
    private fun updateTextPositionToThumb(){
        val transX = binding.thumb.translationX
        val centerTextLayoutWidth = textLayoutWidth.toFloat() / 2
        when {
            transX > centerTextLayoutWidth  && transX < mWidth - centerTextLayoutWidth -> {
                binding.textLayout.translationX = transX - centerTextLayoutWidth
            }
            transX <= centerTextLayoutWidth -> {
                binding.textLayout.translationX = 0f
            }
            transX >=  mWidth - textLayoutWidth -> {
                val v = (mWidth - textLayoutWidth).toFloat()
                binding.textLayout.translationX = v
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun moveThumb(x: Float, isFromText: Boolean = false) {
        triggerCallBack(true)
        logs("moveThumb x $x current x ${binding.thumb.translationX}")
        var transX = x
        if (transX < 0) {
            logs("moveThumb x $x transX $transX return under minimum")
            transX = 0f
        }
        if (transX > availableTrack) {
            logs("moveThumb x $x transX $transX return more than maximum")
            transX = availableTrack.toFloat()
        }
        binding.thumb.translationX = transX
        updateTextPositionToThumb()
        logs("after moveThumb current x ${binding.thumb.translationX}")
        binding.rangeView.updateLayoutParams {
            this.width = availableTrack - transX.toInt() + (thumbWidth.toFloat() / 2).toInt()
        }
        if (!isFromText) {
            val percent = (availableTrack - transX) / availableTrack.toFloat()
            val txt = (percent * availableVisualRange) + minVisualRange
            val txtRounded = roundToStep(txt.toInt())
            logs("moveThumb percent $percent txt $txt")
            setText("$txtRounded")
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

    private fun triggerCallBack(isMoving: Boolean) {
        try {
            onRangeChanged?.onChange(binding.et.text.toString().toInt() , isMoving)
        } catch (e: Exception) {
            e.printStackTrace()
            logs("triggerCallBack e :$e")
        }
    }
}