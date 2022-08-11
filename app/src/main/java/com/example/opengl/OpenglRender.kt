@file:Suppress("unused")

package com.example.opengl

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.util.Log

private const val TAG = "OpenglRender"

abstract class OpenglRender : HandlerThread("GLRender") {

    private lateinit var handler:Handler

    private var eglConfig: EGLConfig? = null
    private var eglDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext = EGL14.EGL_NO_CONTEXT

    var surfaceTexture: SurfaceTexture? = null
    private val outputSurfaces: ArrayList<GLSurface> by lazy {
        ArrayList()
    }

    override fun start() {
        super.start()
        handler = Handler(looper)
        handler.post {
            createGL()
            onCreated()
        }
    }

    /**
     * 创建OpenGL环境
     */
    private fun createGL() {
        // 获取显示设备(默认的显示设备)
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        // 初始化
        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw RuntimeException("EGL error " + EGL14.eglGetError())
        }
        // 获取FrameBuffer格式和能力
        val configAttribs = intArrayOf(
            EGL14.EGL_BUFFER_SIZE, 32,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_NONE
        )
        val numConfigs = IntArray(1)
        val configs = arrayOfNulls<EGLConfig>(1)
        if (!EGL14.eglChooseConfig(
                eglDisplay,
                configAttribs,
                0,
                configs,
                0,
                configs.size,
                numConfigs,
                0
            )
        ) {
            throw RuntimeException("EGL error " + EGL14.eglGetError())
        }
        eglConfig = configs[0]
        // 创建OpenGL上下文(可以先不设置EGLSurface，但EGLContext必须创建，
        // 因为后面调用GLES方法基本都要依赖于EGLContext)
        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
            EGL14.EGL_NONE
        )
        eglContext =
            EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
        if (eglContext === EGL14.EGL_NO_CONTEXT) {
            throw RuntimeException("EGL error " + EGL14.eglGetError())
        }
        // 设置默认的上下文环境和输出缓冲区(小米4上如果不设置有效的eglSurface后面创建着色器会失败，可以先创建一个默认的eglSurface)
        //EGL14.eglMakeCurrent(eglDisplay, surface.eglSurface, surface.eglSurface, eglContext);
        if (!EGL14.eglMakeCurrent(
                eglDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                eglContext
            )
        ) {
            Log.e(TAG, "eglMakeCurrent: error")
        }
    }

    fun addSurface(surface: GLSurface) {
        handler.post {
            makeOutputSurface(surface)
            outputSurfaces.add(surface)
        }
    }

    fun removeSurface(surface: GLSurface) {
        handler.post {
            EGL14.eglDestroySurface(eglDisplay, surface.eglSurface)
            outputSurfaces.remove(surface)
        }
    }

    /**
     * 当创建完基本的OpenGL环境后调用此方法，可以在这里初始化纹理之类的东西
     */
    abstract fun onCreated()

    fun requestRender() {
        handler.post {
            if (outputSurfaces.isEmpty()) {
                return@post
            }
            // 将数据更新到纹理的
            surfaceTexture?.updateTexImage()
            onUpdate()
            render()
        }
    }

    /**
     * 渲染到各个eglSurface
     */
    private fun render() {
        // 渲染(绘制)
        for (output in outputSurfaces) {
            if (output.eglSurface === EGL14.EGL_NO_SURFACE) {
                if (!makeOutputSurface(output)) continue
            }
            // 设置当前的上下文环境和输出缓冲区
            if (!EGL14.eglMakeCurrent(
                    eglDisplay,
                    output.eglSurface,
                    output.eglSurface,
                    eglContext
                )
            ) {
                Log.e(TAG, "eglMakeCurrent: error")
            }

            // 设置视窗大小及位置
            GLES20.glViewport(
                output.viewport.x,
                output.viewport.y,
                output.viewport.width,
                output.viewport.height
            )
            // 绘制
            onDrawFrame(output)
            // 交换显存(将surface显存和显示器的显存交换)
            EGL14.eglSwapBuffers(eglDisplay, output.eglSurface)
        }
    }

    abstract fun onDrawFrame(output: GLSurface)

    /**
     * 在渲染之前调用，用于更新纹理数据。渲染一帧调用一次
     */
    abstract fun onUpdate()

    private fun makeOutputSurface(surface: GLSurface): Boolean {
        // 创建Surface缓存
        try {
            when (surface.type) {
                GLSurface.TYPE_WINDOW_SURFACE -> {
                    val attributes = intArrayOf(EGL14.EGL_NONE)
                    // 创建失败时返回EGL14.EGL_NO_SURFACE
                    surface.eglSurface = EGL14.eglCreateWindowSurface(
                        eglDisplay,
                        eglConfig,
                        surface.surface,
                        attributes,
                        0
                    )
                }
                GLSurface.TYPE_PBUFFER_SURFACE -> {
                    val attributes = intArrayOf(
                        EGL14.EGL_WIDTH, surface.viewport.width,
                        EGL14.EGL_HEIGHT, surface.viewport.height,
                        EGL14.EGL_NONE
                    )
                    // 创建失败时返回EGL14.EGL_NO_SURFACE
                    surface.eglSurface =
                        EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, attributes, 0)
                }
                GLSurface.TYPE_PIXMAP_SURFACE -> {
                    Log.w(TAG, "nonsupport pixmap surface")
                    return false
                }
                else -> {
                    Log.w(TAG, "surface type error " + surface.type)
                    return false
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "can't create eglSurface")
            surface.eglSurface = EGL14.EGL_NO_SURFACE
            return false
        }
        return true
    }

    fun release() {
        handler.post {
            // 回调
            onDestroy()
            // 销毁eglSurface
            for (outputSurface in outputSurfaces) {
                EGL14.eglDestroySurface(eglDisplay, outputSurface.eglSurface)
                outputSurface.eglSurface = EGL14.EGL_NO_SURFACE
            }
            quit()
        }
    }

    abstract fun onDestroy()

    fun postDelay(runnable: Runnable, delayTime: Long) {
        handler.postDelayed(runnable, delayTime)
    }


}