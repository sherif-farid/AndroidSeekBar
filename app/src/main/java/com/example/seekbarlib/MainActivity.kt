package com.example.seekbarlib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.rangebar.RangeBar
import com.example.rangebar.RangeBarGradiant
import com.example.seekbarlib.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), RangeBarGradiant.OnRangeChanged {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val tag = "MainActivityTag"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val stepsList1 = arrayListOf<Int>()
        for (i in 1..50) {
            stepsList1.add(i *10)
        }
        val stepsList2 = arrayListOf<Int>()
        for (i in 1..5) {
            stepsList2.add(i *10)
        }
        binding.bar.setStepsList(stepsList1 , 24)
        //uncomment to set a specific step
        //binding.bar.setCurrentStep(3)
        binding.bar.onRangeChanged = this
        binding.btn1.setOnClickListener {
            binding.bar.setStepsList(stepsList1 , 24)
            binding.bar.setCurrentStep(0)
            binding.min.text = stepsList1.first().toString()
            binding.recommendedTxt.text = "${stepsList1[24]}"
            binding.max.text = stepsList1.last().toString()
        }
        binding.btn2.setOnClickListener {
            binding.bar.setStepsList(stepsList1 , 24)
            binding.bar.setCurrentStep(stepsList1.size -1)
            binding.min.text = stepsList1.first().toString()
            binding.recommendedTxt.text = "${stepsList1[24]}"
            binding.max.text = stepsList1.last().toString()
        }
        binding.btn3.setOnClickListener {
            binding.bar.setStepsList(stepsList2 , 3)
            binding.bar.setCurrentStep(2)
            binding.min.text = stepsList2.first().toString()
            binding.recommendedTxt.text = "${stepsList2[3]}"
            binding.max.text = stepsList2.last().toString()
        }
        binding.btn4.setOnClickListener {
            binding.bar.setStepsList(stepsList2 , 2)
            binding.bar.setCurrentStep(4)
            binding.min.text = stepsList2.first().toString()
            binding.recommendedTxt.text = "${stepsList2[2]}"
            binding.max.text = stepsList2.last().toString()
        }
    }

    override fun onChange(price: Int, isMoving: Boolean, info: Int) {
        Log.v(tag , "rangeValue $price isMoving $isMoving")
        binding.priceTxt.text = "$price"
    }
}