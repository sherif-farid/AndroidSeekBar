package com.example.seekbarlib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.rangebar.RangeBar
import com.example.seekbarlib.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), RangeBar.OnRangeChanged {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val tag = "MainActivityTag"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.bar.setDefaultValue(45)
        binding.bar.onRangeChanged = this
    }

    override fun onChange(rangeValue: Int , isMoving:Boolean) {
        Log.v(tag , "rangeValue $rangeValue isMoving $isMoving")
    }
}