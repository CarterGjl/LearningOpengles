package com.example.myapplication.video

import android.content.ContentValues
import android.content.res.Resources
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ContentView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySurfaceViewBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_surface_view.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SurfaceViewActivity : AppCompatActivity(), SurfaceHolder.Callback, GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "SurfaceViewActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_surface_view)
        setSupportActionBar(findViewById(R.id.toolbar))
        val view = ActivitySurfaceViewBinding.inflate(layoutInflater)
        setContentView(view.root)
        ContentValues().apply {
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/*")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            put(MediaStore.Audio.Media.DATA, Environment.DIRECTORY_DOWNLOADS)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            var action = Snackbar.make(cl, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
//            action.setAnchorView(window.decorView)
            action.show()
        }

        val surfaceView = GLSurfaceView(this)
//        root.addView(surfaceView)
        surfaceView.setRenderer(this)
        val registerForActivityResult =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {

                Log.d(TAG, "onCreate:$it ")
            }
//        registerForActivityResult.launch(null)
        cl.setOnApplyWindowInsetsListener { v, insets ->
            println("bottom padding ${insets.systemWindowInsetBottom}")
            v.updatePadding(top = insets.systemWindowInsetTop,bottom = insets.systemWindowInsetBottom)
            insets
        }
        val window = window
        val theme = theme


    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window?.decorView?.post {
                if (window.decorView.rootWindowInsets?.systemWindowInsetBottom ?: 0 >= Resources.getSystem().displayMetrics.density * 40) {
//                    window.navigationBarColor = theme.resolveColor(android.R.attr.navigationBarColor) and 0x00ffffff or -0x20000000
                    window.navigationBarColor = resources.getColor(R.color.colorAccent1)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        window.isNavigationBarContrastEnforced = false
                    }
                } else {
                    window.navigationBarColor = Color.TRANSPARENT
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        window.isNavigationBarContrastEnforced = true
                    }
                }
            }
        }
    }
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated: ")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged: ")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed: ")
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated: gl")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: gl")
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d(TAG, "onDrawFrame: gl")
    }
}

@ColorInt
fun Resources.Theme.resolveColor(@AttrRes attrId: Int): Int {
    val a = obtainStyledAttributes(intArrayOf(attrId))
    val res = a.getColor(0, 0)
    a.recycle()
    return res
}