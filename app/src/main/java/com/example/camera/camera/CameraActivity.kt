package com.example.camera.camera

import android.graphics.Bitmap
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewOutlineProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.myapplication.databinding.ActivityCameraBinding
import com.example.opengl.GLSurface
import com.example.opengl.ShaderUtil
import com.example.opengl.VideoOpenGlRender
import kotlinx.android.synthetic.main.fragment_first_test_motion_image.view.*
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL10


private const val TAG = "CameraActivity"

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
        WindowCompat.setDecorFitsSystemWindows(window,false)
        setContentView(inflate.root)

        val loadFromAssetsFile = ShaderUtil.loadFromAssetsFile("test_shader.glsl", resources)

        Log.d(TAG, "onCreate: $loadFromAssetsFile")
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

    override fun onResume() {
        super.onResume()
        cameraHandlerThread!!.startPreview()
    }

    override fun onStop() {
        super.onStop()
        cameraHandlerThread?.stopPreview()
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
//        val videoGlRender = VideoGlRender()
        val videoGlRender = VideoOpenGlRender()

        videoGlRender.surfaceTexture = mSurface
        videoGlRender.mTextureId = createTextureID
        videoGlRender.start()
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
                inflate.sf12.outlineProvider = object :ViewOutlineProvider(){
                    override fun getOutline(view: View, outline: Outline) {
                        val rect = Rect()
                        view.getGlobalVisibleRect(rect)
                        val leftMargin = 0
                        val topMargin = 0
                        val newRect = Rect(leftMargin,topMargin, view.width,
                            view.height)
                        outline.setRoundRect(newRect,39f)

                    }
                }
                inflate.sf12.clipToOutline = true
//                mSurface.setOnFrameAvailableListener {
//                    videoGlRender.requestRender()
//                }
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
            }
        })
        val glPbufferSurface = GLSurface(512, 512)
        videoGlRender.addSurface(glPbufferSurface)
        videoGlRender.postDelay({
            val ib: IntBuffer = IntBuffer.allocate(512 * 512)
            GLES20.glReadPixels(0, 0, 512, 512, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib)

            val bitmap: Bitmap = frameToBitmap(512, 512, ib)
            Handler(Looper.getMainLooper()).post { inflate.imageView.setImageBitmap(bitmap) }
        }, 1000)
//        inflate.sf13.holder.addCallback(object : SurfaceHolder.Callback {
//            override fun surfaceCreated(p0: SurfaceHolder) {
//            }
//
//            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, width: Int, height: Int) {
//                videoGlRender.addSurface(GLSurface(p0.surface, width, height))
//            }
//
//            override fun surfaceDestroyed(p0: SurfaceHolder) {
//            }
//        })
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
        Handler(Looper.getMainLooper()).post {


        }
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

    /**
     * 将数据转换成bitmap(OpenGL和Android的Bitmap色彩空间不一致，这里需要做转换)
     *
     * @param width 图像宽度
     * @param height 图像高度
     * @param ib 图像数据
     * @return bitmap
     */
    private fun frameToBitmap(width: Int, height: Int, ib: IntBuffer): Bitmap {
        val pixs = ib.array()
        // 扫描转置(OpenGl:左上->右下 Bitmap:左下->右上)
        for (y in 0 until height / 2) {
            for (x in 0 until width) {
                val pos1 = y * width + x
                val pos2 = (height - 1 - y) * width + x
                val tmp = pixs[pos1]
                pixs[pos1] =
                    pixs[pos2] and -0xff0100 or (pixs[pos2] shr 16 and 0xff) or (pixs[pos2] shl 16 and 0x00ff0000) // ABGR->ARGB
                pixs[pos2] =
                    tmp and -0xff0100 or (tmp shr 16 and 0xff) or (tmp shl 16 and 0x00ff0000)
            }
        }
        if (height % 2 == 1) { // 中间一行
            for (x in 0 until width) {
                val pos = (height / 2 + 1) * width + x
                pixs[pos] =
                    pixs[pos] and -0xff0100 or (pixs[pos] shr 16 and 0xff) or (pixs[pos] shl 16 and 0x00ff0000)
            }
        }
        return Bitmap.createBitmap(pixs, width, height, Bitmap.Config.ARGB_8888)
    }
}