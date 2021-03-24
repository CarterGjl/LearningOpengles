package com.example.camera.camera

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.Surface
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.example.camera.camera.CameraGlSurfaceView
import com.example.camera.camera.CameraHandlerThread
import com.example.camera.camera.DuMixRenderCallback
import com.example.myapplication.databinding.ActivityCameraBinding
import org.webrtc.VideoCapturer

class CameraActivity : AppCompatActivity() {

    private var cameraHandlerThread: CameraHandlerThread? = null

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

        initGlSurfaceView(inflate)
        inflate.btnRemoveOrAdd.setOnClickListener {
            if (inflate.root.indexOfChild(cameraGlSurfaceView) == -1) {
                inflate.root.addView(cameraGlSurfaceView, 0)
            } else {
                inflate.root.removeView(cameraGlSurfaceView)
            }
        }
    }

    private fun initGlSurfaceView(inflate: ActivityCameraBinding) {
        cameraGlSurfaceView = CameraGlSurfaceView(this, object : DuMixRenderCallback {
            override fun onSurfaceCreated(cameraTex: SurfaceTexture, arTex: SurfaceTexture?) {

                openCamera(cameraTex = cameraTex, false)
            }

            override fun onSurfaceChanged(width: Int, height: Int) {
            }

        })
        inflate.root.addView(cameraGlSurfaceView, 0)
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