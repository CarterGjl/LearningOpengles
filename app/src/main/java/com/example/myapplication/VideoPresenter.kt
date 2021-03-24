package com.example.myapplication

import android.opengl.GLSurfaceView
import android.view.View
import com.example.camera.camera.CameraActivity
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VideoPresenter private constructor() {
    private var views: Array<View?>? = arrayOfNulls(10)
    private var activity: CameraActivity? = null

    companion object {
        val instance: VideoPresenter by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            VideoPresenter()
        }
    }

    fun prepareViews() {
        for (i in 0..9) {
            val glSurfaceView = GLSurfaceView(ChatApplication.context)
            glSurfaceView.setRenderer(object : GLSurfaceView.Renderer {
                override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                }

                override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                }

                override fun onDrawFrame(gl: GL10?) {
                }

            })
            views?.set(i, glSurfaceView)
            Timer("test").schedule(object :TimerTask(){
                override fun run() {
//                    activity?.test()
                }

            },1000,1000)
        }

    }

    fun setActivity(x: CameraActivity?) {
        activity = x
    }

    fun release() {
        println("view size ${views?.size}")
        views?.forEach { view: View? ->
            println(view)
            val glSurfaceView = view as GLSurfaceView
//            glSurfaceView.onPause()
        }
        views = null
    }
}