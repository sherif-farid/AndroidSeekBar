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
        for (i in 1..5) {
            stepsList.add(i *100)
        }
        binding.bar.setStepsList(stepsList , 2)
        //uncomment to set a specific step
        //binding.bar.setCurrentStep(3)
        binding.bar.onRangeChanged = this
    }

    override fun onChange(price: Int , isMoving:Boolean) {
        Log.v(tag , "rangeValue $price isMoving $isMoving")
        binding.priceTxt.text = "$price"
    }
}