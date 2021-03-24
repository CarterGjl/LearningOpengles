package com.example.camera.camera2

import android.opengl.*
import android.opengl.EGLExt.EGL_RECORDABLE_ANDROID
import android.view.Surface


class CodecInputSurface(private var surface: Surface?) {

    private var mEGLDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var mEGLContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var mEGLSurface: EGLSurface = EGL14.EGL_NO_SURFACE

    init {
        initEGL()
    }

    private fun initEGL() {
        // 获取eglDisplay
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        // 错误检查
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY){
            throw  RuntimeException("unable to get EGL14 display");
        }
        // 初始化
        val version = IntArray(2)
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            throw java.lang.RuntimeException("unable to initialize EGL14")
        }
        // Configure EGL for recording and OpenGL ES 2.0.
        // Configure EGL for recording and OpenGL ES 2.0.
        val attribList = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,  //
                EGL14.EGL_RENDERABLE_TYPE,
                EGL14.EGL_OPENGL_ES2_BIT,  // 录制android
                EGL_RECORDABLE_ANDROID,
                1,
                EGL14.EGL_NONE
            )
        } else {
            intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,  //
                EGL14.EGL_RENDERABLE_TYPE,
                EGL14.EGL_OPENGL_ES2_BIT,  // 录制android
                1,
                EGL14.EGL_NONE
            )
        }
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        // eglCreateContext RGB888+recordable ES2
        // eglCreateContext RGB888+recordable ES2
        EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.size, numConfigs, 0)
        // Configure context for OpenGL ES 2.0.
        // Configure context for OpenGL ES 2.0.
        val attrib_list = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        //--------------------mEGLContext-----------------------
        //  eglCreateContext
        //--------------------mEGLContext-----------------------
        //  eglCreateContext
        mEGLContext = EGL14.eglCreateContext(
            mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
            attrib_list, 0
        )
        checkEglError("eglCreateContext")

        //--------------------mEGLSurface-----------------------
        // 创建一个WindowSurface并与surface进行绑定,这里的surface来自mEncoder.createInputSurface();
        // Create a window surface, and attach it to the Surface we received.

        //--------------------mEGLSurface-----------------------
        // 创建一个WindowSurface并与surface进行绑定,这里的surface来自mEncoder.createInputSurface();
        // Create a window surface, and attach it to the Surface we received.
        val surfaceAttribs = intArrayOf(
            EGL14.EGL_NONE
        )
        // eglCreateWindowSurface
        // eglCreateWindowSurface
        mEGLSurface = EGL14.eglCreateWindowSurface(
            mEGLDisplay, configs[0], surface,
            surfaceAttribs, 0
        )
        checkEglError("eglCreateWindowSurface")
    }

    /**
     * Checks for EGL errors.  Throws an exception if one is found.
     * 检查错误,代码可以忽略
     */
    private fun checkEglError(msg: String) {
        var error: Int
        if (EGL14.eglGetError().also { error = it } != EGL14.EGL_SUCCESS) {
            throw java.lang.RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error))
        }
    }

    /**
     * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
     * 设置图像，发送给EGL的时间间隔
     */
    fun setPresentationTime(nsecs: Long) {
        // 设置发动给EGL的时间间隔
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs)
        checkEglError("eglPresentationTimeANDROID")
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     * 用该方法，发送当前Frame
     */
    fun swapBuffers(): Boolean {
        val result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)
        checkEglError("eglSwapBuffers")
        return result
    }

    /**
     * Makes our EGL context and surface current.
     * 设置 EGLDisplay dpy, EGLSurface draw, EGLSurface read, EGLContext ctx
     */
    fun makeCurrent() {
        EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)
        checkEglError("eglMakeCurrent")
    }

    /**
     * Discards all resources held by this class, notably the EGL context.  Also releases the
     * Surface that was passed to our constructor.
     * 释放资源
     */
    fun release() {
        if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(
                mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
            EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface)
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(mEGLDisplay)
        }
        surface?.release()
        mEGLDisplay = EGL14.EGL_NO_DISPLAY
        mEGLContext = EGL14.EGL_NO_CONTEXT
        mEGLSurface = EGL14.EGL_NO_SURFACE
        surface = null
    }
}