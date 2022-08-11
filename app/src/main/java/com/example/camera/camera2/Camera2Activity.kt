package com.example.camera.camera2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityCamera2Binding

class Camera2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityCamera2Binding: ActivityCamera2Binding =
            ActivityCamera2Binding.inflate(layoutInflater)
        setContentView(activityCamera2Binding.root)
        val camera2Helper = Camera2Helper(this, activityCamera2Binding.tv1)
        activityCamera2Binding.btnOpen.setOnClickListener {
            camera2Helper.open()
        }
        activityCamera2Binding.btnClose.setOnClickListener {
            camera2Helper.closeCamera()
        }
    }

}