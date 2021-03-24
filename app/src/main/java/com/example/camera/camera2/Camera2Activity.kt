package com.example.camera.camera2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import kotlinx.android.synthetic.main.activity_camera2.*

class Camera2Activity : AppCompatActivity() {

    companion object {
        private const val TAG = "Camera2Activity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)
        val camera2Helper = Camera2Helper(this, tv1)
        btnOpen.setOnClickListener {
            camera2Helper.open()
        }
        btnClose.setOnClickListener {
            camera2Helper.closeCamera()
        }
    }

}