package com.example.camera.opengllearning.drawer

import android.graphics.SurfaceTexture

interface IDrawer {
    fun setVideoSize(videoW: Int, videoH: Int)
    fun setWorldSize(videoW: Int, videoH: Int)
    fun draw()
    fun setTextureID(id: Int)
    fun getSurfaceTexture(cb: (st: SurfaceTexture) -> Unit) {

    }
    fun release()
    fun setAlpha(alpha: Float)
}