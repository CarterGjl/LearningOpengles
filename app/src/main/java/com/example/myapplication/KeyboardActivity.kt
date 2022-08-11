package com.example.myapplication

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.myapplication.databinding.ActivityKeyboardBinding

class KeyboardActivity : AppCompatActivity() {

    private lateinit var activityKeyboardBinding: ActivityKeyboardBinding
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityKeyboardBinding = ActivityKeyboardBinding.inflate(layoutInflater)
        setContentView(activityKeyboardBinding.root)
        setSupportActionBar(activityKeyboardBinding.toolbar)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(activityKeyboardBinding.root) { v, insets ->
            v.updatePadding(
                top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            )
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activityKeyboardBinding.fab.setWindowInsetsAnimationCallback(
                TranslateDeferringInsetsAnimationCallback(
                    activityKeyboardBinding.fab,
                    persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                    deferredInsetTypes = WindowInsetsCompat.Type.ime()
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed({
            val insetsController =
                WindowCompat.getInsetsController(window, activityKeyboardBinding.cc.et)
            insetsController.show(WindowInsetsCompat.Type.ime())
            insetsController.hide(WindowInsetsCompat.Type.statusBars())

        }, 2000)
    }
}