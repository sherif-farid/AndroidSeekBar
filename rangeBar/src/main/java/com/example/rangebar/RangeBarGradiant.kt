package com.example.rangebar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import com.example.rangebar.databinding.RangeBarGradiantBinding
import java.lang.RuntimeException


/*
 * Created by Sherif farid
 * Date: 03/22/2024.
 * email: sherffareed39@gmail.com.
 * phone: 00201007538470
 */
private const val THUMB_EDGE = 5


@SuppressLint("ClickableViewAccessibility,SetTextI18n")
class RangeBarGradiant @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), View.OnTouchListener {
    private var info: Int = -1
    private var binding: RangeBarGradiantBinding
    private val tag = "RangeBarTag"
    private var mWidth = 0
    private var thumbWidth = 0
    private var maxVisualRange = 100
    private var minVisualRange = 0
    private var availableVisualRange = maxVisualRange - minVisualRange
    private var isLogEnabled = false
    private var isRoundToMin = false
    private var defaultValue = maxVisualRange
    private var textLayoutWidth = 0
    private var stepsList = ArrayList<Int>()
    private val viewsStepsList = ArrayList<View>()
    private var RECOMMENDED_STEP_INDEX = 0
    private var listOfStepsXAxis = ArrayList<Float>()
    private var isStepsDrawn = false
    private fun validateStepAxisList(){
        if (listOfStepsXAxis.size != stepsList.size){
            listOfStepsXAxis = listOfStepsXAxis.distinct() as ArrayList<Float>
        }
    }

    fun setStepsList(list: ArrayList<Int>, recommendedStepIndex: Int) {
        updateDimensions()
        logs("setStepsList list ${list.toList()} recommendedStepIndex $recommendedStepIndex")
        if (recommendedStepIndex < 0 ||
            recommendedStepIndex > list.size - 1 ||
            list.isEmpty()
        ) {
            throw RuntimeException("invalid list size ${list.size} with recommendedStepIndex $recommendedStepIndex")
        }
        this.RECOMMENDED_STEP_INDEX = recommendedStepIndex
        this.stepsList = list
        invalidate()
    }

    fun interface OnRangeChanged {
        fun onChange(price: Int, isMoving: Boolean , info: Int)
    }

    var onRangeChanged: OnRangeChanged? = null
    private fun logs(log: String) {
        if (!isLogEnabled) return
        Log.v(tag, log)
    }

    private fun initValues(attrs: AttributeSet?) {
        try {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RangeBar)
            minVisualRange = typedArray.getInt(R.styleable.RangeBar_min, minVisualRange)
            maxVisualRange = typedArray.getInt(R.styleable.RangeBar_max, maxVisualRange)
            defaultValue = typedArray.getInt(R.styleable.RangeBar_default_value, maxVisualRange)
            isLogEnabled = typedArray.getBoolean(R.styleable.RangeBar_log_enabled, isLogEnabled)
            isRoundToMin = typedArray.getBoolean(R.styleable.RangeBar_round_to_min, isRoundToMin)
            val thumbShape = typedArray.getDrawable(R.styleable.RangeBar_thumb_shape)
            val textShape = typedArray.getDrawable(R.styleable.RangeBar_text_shape)
            val trackHeight = typedArray.getDimensionPixelSize(R.styleable.RangeBar_track_height, 0)
            val thumbSize = typedArray.getDimensionPixelSize(R.styleable.RangeBar_thumb_size, 0)
            binding.gradientView.background = getGradientDrawable("#FF5A409B", "#FFFC2727")
            binding.thumb.background = thumbShape
            binding.textLayout.background = textShape
            if (thumbSize > 0) {
                binding.thumb.updateLayoutParams {
                    this.height = thumbSize
                    this.width = thumbSize
                }
            }
            typedArray.recycle()
            availableVisualRange = maxVisualRange - minVisualRange

            logs(
                "initValues thumbSize $thumbSize trackHeight $trackHeight minVisualRange $minVisualRange maxVisualRange $maxVisualRange" +
                        " isRoundToMin $isRoundToMin availableVisualRange $availableVisualRange"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            logs("initValues e $e")
        }
    }

    private fun stepSpace(): View {
        val v = View(this.context)
        v.apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT).apply {
                weight = 1f
            }
        }
        return v
    }

    private fun stepStartSpace(): View {
        val v = View(this.context)
        v.apply {
            this.id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                thumbWidth / 2,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return v
    }

    private fun stepView(): View {
        val v = View(this.context)
        v.apply {
            this.id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.step_width),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            background = ContextCompat.getDrawable(context, R.drawable.step_shape)
            alpha = 0.5f
        }
        return v
    }

    private fun drawSteps() {
        if (isStepsDrawn)return
        isStepsDrawn = true
        logs("drawSteps stepsList ${stepsList.toList()}")
        listOfStepsXAxis.clear()
        binding.stepsLayout.removeAllViews()
        binding.stepsLayout.addView(stepStartSpace())
        for (i in stepsList.indices) {
            if (i != 0) {
                binding.stepsLayout.addView(stepSpace())
            }
            val stepView = stepView()
            binding.stepsLayout.addView(stepView)
            viewsStepsList.add(stepView)
            stepView.post {
                val stepX = stepView.x
                if (stepX > 0 ) {
                    listOfStepsXAxis.add(stepX)
                }
                logs("listOfStepsXAxis ${listOfStepsXAxis.toList()}")
                if (i == RECOMMENDED_STEP_INDEX) {
                    updateTextPositionToThumb(stepView.x, stepView.width)
                }
            }

        }
        binding.stepsLayout.addView(stepStartSpace())
    }

    init {
        binding = RangeBarGradiantBinding.inflate(
            LayoutInflater.from(context),
            this,
            false
        )
        this.addView(binding.root)
        initValues(attrs)
        binding.cardView.setOnTouchListener(this)
        binding.textLayout.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            textLayoutWidth = binding.textLayout.width
        }
    }
    private fun updateDimensions(){
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.root.post {
                    binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    getParentScroll()
                    mWidth = binding.cardView.width
                    thumbWidth = binding.thumb.width
                    textLayoutWidth = binding.textLayout.width
                    logs("mWidth $mWidth thumbWidth $thumbWidth ")
                    drawSteps()
                }

            }
        })
    }
    var mParent : ViewParent? = null
    private fun getParentScroll(){
        mParent = binding.root.parent
        while (mParent != null ){
            if (mParent is ScrollView || mParent is NestedScrollView) {
                break
            }
            mParent = mParent?.parent
        }
    }

    private fun updateTextPositionToThumb(x: Float, width: Int) {
        logs("updateTextPositionToThumb x $x ")

        val xPosition = x + (width / 2) - (binding.textLayout.width / 2) + binding.cardView.x
        binding.textLayout.translationX = xPosition
        val lp = binding.solidView.layoutParams
        lp.apply {
            this.width = x.toInt()
        }
        binding.solidView.layoutParams = lp
        setCurrentStep(RECOMMENDED_STEP_INDEX )
    }

    @SuppressLint("SetTextI18n")
    private fun moveThumb(x: Float , index: Int = -1) {
        triggerCallBack(true, x = x , index = index)
        logs(
            "moveThumb x $x current x ${binding.thumb.translationX} " +
                    "availableTrack $mWidth thumbWidth $thumbWidth"
        )
        var transX = x
        val max = mWidth - THUMB_EDGE - thumbWidth
        if (transX < THUMB_EDGE) {
            logs("moveThumb x $x transX $transX return under minimum")
            transX = THUMB_EDGE.toFloat()
        }
        if (transX > max) {
            logs("moveThumb x $x transX $transX return more than maximum")
            transX = max.toFloat()
        }
        binding.thumb.translationX = transX
        binding.mainTrack.translationX = transX + THUMB_EDGE
    }



    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mParent?.requestDisallowInterceptTouchEvent(true)
                true
            }

            MotionEvent.ACTION_MOVE -> {
                moveThumb(event.x)
                true
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                mParent?.requestDisallowInterceptTouchEvent(false)
                logs("ACTION_UP event.x ${event.x}")
                val lastMinStepIndex = lastMinStepIndex(event.x)
                val lastStepX = listOfStepsXAxis.safeIndex(lastMinStepIndex)?:0f
                if (lastMinStepIndex > -1) {
                    moveThumb(lastStepX - (thumbWidth / 2))
                    triggerCallBack(isMoving = false, index = lastMinStepIndex)
                }
                true
            }

            else -> false
        }
    }


    fun setCurrentStep(stepIndex: Int , info:Int = -1) {
        this.info = info
        binding.root.post{
            try {
                val stepX = listOfStepsXAxis.safeIndex(stepIndex)?:0f
                moveThumb(stepX - (thumbWidth / 2) , index = stepIndex)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun lastMinStepIndex(x: Float): Int {
        validateStepAxisList()
        logs("lastMinStepIndex listOfStepsXAxis size ${listOfStepsXAxis.size} list ${listOfStepsXAxis.toList()}")
        for (i in listOfStepsXAxis.size - 1 downTo 0) {
            val stepX = listOfStepsXAxis.safeIndex(i)?:0f
            logs("lastMinStepIndex i $i stepX $stepX x $x")
            if (x > stepX) {
                return i
            }
        }
        return 0
    }

    private fun triggerCallBack(isMoving: Boolean, index: Int = 0, x: Float = 0f) {
        val currentPrice = try {
            val lastMinStepIndex = lastMinStepIndex(x)
            val nextStepIndex = lastMinStepIndex + 1
            val prevX = listOfStepsXAxis.safeIndex(lastMinStepIndex)?:0f
            val nextX = listOfStepsXAxis.safeIndex(nextStepIndex)?:0f
            val prevPrice = stepsList[lastMinStepIndex]
            val nextPrice = stepsList[nextStepIndex]
            logs("callback x $x prevX $prevX nextX $nextX prevPrice $prevPrice nextPrice $nextPrice")
            var price = (((x - prevX) / (nextX - prevX)) * (nextPrice - prevPrice)) + prevPrice
            logs("callback price $price")
            if (price < stepsList.first()) price = stepsList.first().toFloat()
            if (price > stepsList.last())price = stepsList.last().toFloat()
            price
        } catch (e: Exception) {
            e.printStackTrace()
            logs("callback error $e")
            stepsList.lastOrNull() ?: 0
        }

        try {
            val isInProgress = isMoving && index == -1
            val price = if (isInProgress) {
                currentPrice.toInt()
            } else {
                stepsList[index]
            }
            onRangeChanged?.onChange(price, isInProgress , info)
        } catch (e: Exception) {
            e.printStackTrace()
            logs("callback e :$e")
        }
    }
}