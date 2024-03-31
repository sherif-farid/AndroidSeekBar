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
        val stepsList = arrayListOf<Int>()
        for (i in 30..79) {
            stepsList.add(i *10)
        }
        binding.bar.setStepsList(stepsList , 31)
        //uncomment to set a specific step
        //binding.bar.setCurrentStep(3)
        binding.bar.onRangeChanged = this
        binding.btn1.setOnClickListener {
            binding.bar.setStepsList(stepsList , 31)
            binding.bar.setCurrentStep(0)
        }
        binding.btn2.setOnClickListener {
            binding.bar.setStepsList(stepsList , 31)
            binding.bar.setCurrentStep(stepsList.size -1)
        }
        binding.btn3.setOnClickListener {
            binding.bar.setStepsList(stepsList , 31)
            binding.bar.setCurrentStep(15)
        }
        binding.btn4.setOnClickListener {
            binding.bar.setStepsList(stepsList , 31)
            binding.bar.setCurrentStep(20)
        }
    }

    override fun onChange(price: Int, isMoving: Boolean, info: Int) {
        Log.v(tag , "rangeValue $price isMoving $isMoving")
        binding.priceTxt.text = "$price"
    }
}