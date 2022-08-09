package com.example.camera.camera

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityCameraBinding
import com.example.opengl.GLSurface
import com.example.opengl.VideoGlRender
import javax.microedition.khronos.opengles.GL10

class CameraActivity : AppCompatActivity() {

    private lateinit var mSurface: SurfaceTexture
    private var cameraHandlerThread: CameraHandlerThread? = null
    private var createTextureID: Int = -1

    // 默认打开前置摄像头
    private var isCameraFront = true

    // 相机预览宽
    private val mPreviewWidth = 1280

    // 相机预览高
    private val mPreviewHeight = 720
    private lateinit var cameraGlSurfaceView: CameraGlSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = ActivityCameraBinding.inflate(layoutInflater)

        setContentView(inflate.root)
        createTextureID = createTextureID()
        mSurface = SurfaceTexture(createTextureID)
        openCamera(cameraTex = mSurface, false)
        initGlSurfaceView(inflate)
        inflate.btnRemoveOrAdd.setOnClickListener {
            if (inflate.root.indexOfChild(cameraGlSurfaceView) == -1) {
                inflate.root.addView(cameraGlSurfaceView, 0)
            } else {
                inflate.root.removeView(cameraGlSurfaceView)
            }
        }
    }


    private fun createTextureID(): Int {
        val tex = IntArray(1)
        //生成一个纹理
        GLES20.glGenTextures(1, tex, 0)
        //将此纹理绑定到外部纹理上
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])
        //设置纹理过滤参数
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        //解除纹理绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        return tex[0]
    }

    private fun initGlSurfaceView(inflate: ActivityCameraBinding) {
        val videoGlRender = VideoGlRender()
        videoGlRender.setSurfaceTexture(mSurface)
        videoGlRender.mTextureId = createTextureID
        videoGlRender.startRender()
        inflate.sf1.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, width: Int, height: Int) {
                videoGlRender.addSurface(GLSurface(p0.surface, width, height))
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
            }
        })
        inflate.sf12.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, width: Int, height: Int) {
                videoGlRender.addSurface(GLSurface(p0.surface, width, height))
//                mSurface.setOnFrameAvailableListener {
//                    videoGlRender.requestRender()
//                }
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
            }
        })
        inflate.sf13.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, width: Int, height: Int) {
                videoGlRender.addSurface(GLSurface(p0.surface, width, height))
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
            }
        })
        mSurface.setOnFrameAvailableListener {
            videoGlRender.requestRender()
        }

//        val cameraGlSurfaceView1 = CameraGlSurfaceView(this, object : DuMixRenderCallback {
//            override fun onSurfaceCreated(cameraTex: SurfaceTexture, arTex: SurfaceTexture?) {
//
////                openCamera(cameraTex = cameraTex, false)
//            }
//
//            override fun onSurfaceChanged(width: Int, height: Int) {
//            }
//
//        },"cameraGlSurfaceView1")
//        cameraGlSurfaceView1.setTextureID(createTextureID)
//        cameraGlSurfaceView1.setSurface(mSurface)
//        cameraGlSurfaceView = CameraGlSurfaceView(this, object : DuMixRenderCallback {
//            override fun onSurfaceCreated(cameraTex: SurfaceTexture, arTex: SurfaceTexture?) {
//
////                cameraTex.setOnFrameAvailableListener {
////                    cameraGlSurfaceView.requestRender()
////                    cameraGlSurfaceView1.requestRender()
////                }
////
//            }
//
//            override fun onSurfaceChanged(width: Int, height: Int) {
//            }

//        },"cameraGlSurfaceView")
//        cameraGlSurfaceView.setTextureID(createTextureID)
////        cameraGlSurfaceView.setSurface(mSurface)
//        mSurface.setOnFrameAvailableListener {
//            cameraGlSurfaceView.requestRender()
//            cameraGlSurfaceView1.requestRender()
//        }
//        val layoutParams = ViewGroup.LayoutParams(300, 300)
//        inflate.root.addView(cameraGlSurfaceView, 0)
//        inflate.root.addView(cameraGlSurfaceView1,layoutParams)
    }

    private fun openCamera(cameraTex: SurfaceTexture, switchCam: Boolean) {
        if (cameraHandlerThread == null) {
            cameraHandlerThread = CameraHandlerThread()
        }
        cameraHandlerThread!!.stopPreview()
        val windowRotation = windowManager.defaultDisplay.rotation
        if (switchCam) {
            cameraHandlerThread!!.switchCamera(
                if (isCameraFront) 1 else 0,
                mPreviewWidth,
                mPreviewHeight
            )
        } else {
            cameraHandlerThread!!.openCamera(
                if (isCameraFront) 1 else 0,
                mPreviewWidth,
                mPreviewHeight,
                Surface.ROTATION_270
            )
        }
        cameraHandlerThread!!.setPreviewSurface(cameraTex)
        cameraHandlerThread!!.startPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cameraHandlerThread != null) {
            cameraHandlerThread!!.stopPreview()
            cameraHandlerThread!!.releaseCamera()
            cameraHandlerThread!!.destoryThread()
            cameraHandlerThread = null
        }
    }
}