package com.example.myapplication

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsetsAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import kotlinx.android.synthetic.main.activity_keyboard.*
import kotlinx.android.synthetic.main.content_keyboard.*

class KeyboardActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyboard)
        setSupportActionBar(toolbar)
        WindowCompat.setDecorFitsSystemWindows(window,false)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            v.updatePadding(top =insets.getInsets(WindowInsetsCompat.Type.statusBars()).top, bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            fab.setWindowInsetsAnimationCallback(TranslateDeferringInsetsAnimationCallback(
                fab,persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = WindowInsetsCompat.Type.ime()
            ))
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed({
            val insetsController = WindowCompat.getInsetsController(window, et)
            insetsController?.show(WindowInsetsCompat.Type.ime())
            insetsController?.hide(WindowInsetsCompat.Type.statusBars())

        },2000)
    }
}