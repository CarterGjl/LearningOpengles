package com.example.camera.camera

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GlSurfaceViewRender(private val surfaceTexture: SurfaceTexture,
                          private val textureID: Int) :
    GLSurfaceView.Renderer {


    private lateinit var mARDrawer: DirectDrawer


    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        mARDrawer = DirectDrawer(textureID)
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        surfaceTexture.updateTexImage()
        val mSTMatrix = FloatArray(16)

        // Draw the video frame.
        surfaceTexture.getTransformMatrix(mSTMatrix)
        mARDrawer.draw(mSTMatrix)
    }
}