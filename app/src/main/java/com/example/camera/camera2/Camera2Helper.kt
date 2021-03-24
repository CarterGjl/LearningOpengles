package com.example.camera.camera2

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import androidx.core.app.ActivityCompat
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


class Camera2Helper(private val activity: Activity, private val textureView: AutoFitTextureView) {

    companion object {
        private const val TAG = "Camera2Helper"
        private const val REQUEST_CAMERA_PERMISSION = 1
        private const val MAX_PREVIEW_WIDTH = 1920
        private const val MAX_PREVIEW_HEIGHT = 1080
        private const val mImageFormat = ImageFormat.YUV_420_888
        private const val STATE_PREVIEW = 0
        private const val STATE_WAITING_LOCK = 1
        private const val STATE_WAITING_PRECAPTURE = 2
        private const val STATE_WAITING_NON_PRECAPTURE = 3
        private const val STATE_PICTURE_TAKEN = 4
    }

    private var isOpen = false


    private var mState: Int = STATE_PREVIEW
    private lateinit var mPreviewRequest: CaptureRequest
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var mCameraDevice: CameraDevice? = null
    private var mSensorOrientation: Int? = 0
    private var mPreviewSize: Size? = null
    private var mCameraId: String = "0"
    private val mCameraOpenCloseLock: Semaphore = Semaphore(1)
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    private val mImageAvaiableListener //摄像头画面可达的时候
            : OnPreviewCallbackListener? = null
    private val mOpenErrorListener //打开错误，不设置才用默认策略
            : OnOpenErrorListener? = null
    private var mImageReader: ImageReader? = null
    private val mSurfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
    }

    /**
     * [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.
     */
    private val mStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            isOpen = true
            startBackgroundThread()
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release()
            mCameraDevice = cameraDevice
            startPreview()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            isOpen = false
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            isOpen = false
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
            if (mOpenErrorListener != null) {
                mOpenErrorListener.onOpenError()
            } else {
                activity.finish()
            }
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            activity, arrayOf(
                Manifest.permission.CAMERA
            ), REQUEST_CAMERA_PERMISSION
        )
    }

    /**
     * This a callback object for the [ImageReader]. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private val mOnImageAvailableListener =
        ImageReader.OnImageAvailableListener { reader ->
            val image: Image? = reader.acquireNextImage()
            if (image == null) {
                Log.d(TAG, "onImageAvailable,image is null")
                return@OnImageAvailableListener
            }
            mImageAvaiableListener?.onImageAvailable(image)
            image.close() //一定要关掉，否则回调和预览都会阻塞
        }

//    /**
//     * Creates a new [CameraCaptureSession] for camera preview.
//     */
//    private fun createCameraPreviewSession() {
//        try {
//            val texture: SurfaceTexture? = textureView.surfaceTexture
//
//            // We configure the size of default buffer to be the size of camera preview we want.
//            texture!!.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
//
//            // This is the output Surface we need to start preview.
//            val surface = Surface(texture)
//
//            // We set up a CaptureRequest.Builder with the output Surface.
//            mPreviewRequestBuilder =
//                mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//            mPreviewRequestBuilder.apply {
//                addTarget(surface)
//                addTarget(mImageReader!!.surface)
//            }
////            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
////                val outputConfiguration = OutputConfiguration(surface)
////                val outputConfiguration1 = OutputConfiguration(mImageReader!!.surface)
////                val sessionConfiguration = SessionConfiguration(
////                    SessionConfiguration.SESSION_REGULAR,
////                    listOf(outputConfiguration, outputConfiguration1),
////                    Executors.newSingleThreadExecutor(),
////                    object :
////                        CameraCaptureSession.StateCallback() {
////                        override fun onConfigured(session: CameraCaptureSession) {
////                            if (null == mCameraDevice) {
////                                return
////                            }
////
////                            // When the session is ready, we start displaying the preview.
////                            mCaptureSession = session
////                            try {
////                                // Auto focus should be continuous for camera preview.
////                                mPreviewRequestBuilder.set(
////                                    CaptureRequest.CONTROL_AF_MODE,
////                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
////                                )
////                                // Flash is automatically enabled when necessary.
////
////                                // Finally, we start displaying the camera preview.
////                                mPreviewRequest = mPreviewRequestBuilder.build()
////                                mCaptureSession?.setRepeatingRequest(
////                                    mPreviewRequest,
////                                    mCaptureCallback, mBackgroundHandler
////                                )
////                            } catch (e: CameraAccessException) {
////                                e.printStackTrace()
////                            }
////                        }
////
////                        override fun onConfigureFailed(session: CameraCaptureSession) {
////                        }
////
////                    })
////                mCameraDevice!!.createCaptureSession(sessionConfiguration)
////            }
//
//            // Here, we create a CameraCaptureSession for camera preview.
//            mCameraDevice!!.createCaptureSession(
//                listOf(surface, mImageReader!!.surface),
//                object : CameraCaptureSession.StateCallback() {
//                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
//                        // The camera is already closed
//                        if (null == mCameraDevice) {
//                            return
//                        }
//
//                        // When the session is ready, we start displaying the preview.
//                        mCaptureSession = cameraCaptureSession
//                        try {
//                            // Auto focus should be continuous for camera preview.
//                            mPreviewRequestBuilder.set(
//                                CaptureRequest.CONTROL_AF_MODE,
//                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
//                            )
//                            // Flash is automatically enabled when necessary.
//
//                            // Finally, we start displaying the camera preview.
//                            mPreviewRequest = mPreviewRequestBuilder.build()
//                            mCaptureSession?.setRepeatingRequest(
//                                mPreviewRequest,
//                                mCaptureCallback, mBackgroundHandler
//                            )
//                        } catch (e: CameraAccessException) {
//                            e.printStackTrace()
//                        }
//                    }
//
//                    override fun onConfigureFailed(
//                        cameraCaptureSession: CameraCaptureSession
//                    ) {
//                        Toast.makeText(activity, "Config Session Failed", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                }, null
//            )
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        }
//    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in [.mCaptureCallback] from {}.
     */
    private fun runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder?.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE
            mCaptureSession?.capture(
                mPreviewRequestBuilder!!.build(), mCaptureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * A [CameraCaptureSession.CaptureCallback] that handles events related to JPEG capture.
     */
    private val mCaptureCallback: CaptureCallback = object : CaptureCallback() {
        private fun process(result: CaptureResult) {
            when (mState) {
                STATE_PREVIEW -> {
                }
                STATE_WAITING_LOCK -> {
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState != null) {
                        if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                        ) {
                            // CONTROL_AE_STATE can be null on some devices
                            val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                            if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                            ) {
                                mState = STATE_PICTURE_TAKEN
                            } else {
                                runPrecaptureSequence()
                            }
                        }
                    }
                }
                STATE_WAITING_PRECAPTURE -> {

                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {

                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN
                    }
                }
            }
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            process(result)
        }
    }


    /**
     * Configures the necessary [android.graphics.Matrix] transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        if (null == mPreviewSize) {
            return
        }
        val rotation: Int = activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0F, 0F, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(
            0F, 0F, mPreviewSize!!.height.toFloat(),
            mPreviewSize!!.width.toFloat()
        )
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale: Float = (viewHeight.toFloat() / mPreviewSize!!.height).coerceAtLeast(
                viewWidth.toFloat() / mPreviewSize!!.width
            )
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180F, centerX, centerY)
        }
        textureView.setTransform(matrix)
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val manager: CameraManager =
            activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics: CameraCharacteristics =
                    manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    continue
                }
                val map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                ) ?: continue

                // For still image captures, we use the largest availablIe size.
                val arrayList = ArrayList<Size>()
                arrayList.addAll(map.getOutputSizes(mImageFormat))
                val largest = Collections.max(arrayList, CompareSizesByArea())

                if (mImageReader == null) {
                    mImageReader = ImageReader.newInstance(
                        largest.width, largest.height,
                        mImageFormat,  /*maxImages*/2
                    )
                    mImageReader?.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler
                    )
                }
                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                val displayRotation: Int = activity.windowManager.defaultDisplay.rotation
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                var swappedDimensions = false
                when (displayRotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 -> if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true
                    }
                    Surface.ROTATION_90, Surface.ROTATION_270 -> if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true
                    }
                    else -> Log.e(TAG, "Display rotation is invalid: $displayRotation")
                }
                val displaySize = Point()
                activity.windowManager.defaultDisplay.getSize(displaySize)
                var rotatedPreviewWidth = width
                var rotatedPreviewHeight = height
                var maxPreviewWidth: Int = displaySize.x
                var maxPreviewHeight: Int = displaySize.y
                if (swappedDimensions) {
                    rotatedPreviewWidth = height
                    rotatedPreviewHeight = width
                    maxPreviewWidth = displaySize.y
                    maxPreviewHeight = displaySize.x
                }
                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH
                }
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture::class.java),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, largest
                )

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                val orientation: Int = activity.resources.configuration.orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(
                        mPreviewSize!!.width, mPreviewSize!!.height
                    )
                } else {
                    textureView.setAspectRatio(
                        mPreviewSize!!.height, mPreviewSize!!.width
                    )
                }
                mCameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
        }
    }

    private fun openCamera(width: Int, height: Int) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
            return
        }

        val manager: CameraManager =
            activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (!isOpen) {
            setUpCameraOutputs(width, height)
            configureTransform(width, height)
            try {
                if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                    throw RuntimeException("Time out waiting to lock camera opening.")
                }
                manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                throw RuntimeException("Interrupted while trying to lock camera opening.", e)
            }
        } else {
            startPreview()
        }

    }

    private fun startPreview() {
        try {
            mCameraDevice?.apply {
                mPreviewRequestBuilder = createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                // 自动对焦
                mPreviewRequestBuilder?.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                val list = arrayListOf<Surface>()
                val surface = Surface(textureView.surfaceTexture)
                list.add(surface)
                list.add(mImageReader!!.surface)
                mPreviewRequestBuilder?.addTarget(surface)
                createCaptureSession2(list, {
                    mCaptureSession = it
                    updatePreview()
                }, handler = mBackgroundHandler)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "openCamera: ", e)
        }
    }

    private fun updatePreview() {
        try {
            Log.d(TAG, "update preview thread : ${Thread.currentThread().name}")
            mCaptureSession?.setRepeatingRequest(mPreviewRequestBuilder!!.build(), null, null)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun stopPreview() {
        mCaptureSession?.stopRepeating()
        mCaptureSession?.close()
        mCaptureSession = null
    }

    private fun stop() {
        mCameraDevice?.close()
    }

    fun open() {

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = mSurfaceTextureListener
        }
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        mBackgroundHandler?.removeCallbacksAndMessages(null)
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * Closes the current [CameraDevice].
     */
    fun closeCamera() {
        stopPreview()
//        stopBackgroundThread()
//        try {
//            mCameraOpenCloseLock.acquire()
//            if (null != mCaptureSession) {
//                mCaptureSession?.close()
//                mCaptureSession = null
//            }
//            if (null != mCameraDevice) {
//                mCameraDevice!!.close()
//                mCameraDevice = null
//            }
//            if (null != mImageReader) {
//                mImageReader?.close()
//                mImageReader = null
//            }
//        } catch (e: InterruptedException) {
//            throw java.lang.RuntimeException("Interrupted while trying to lock camera closing.", e)
//        } finally {
//            mCameraOpenCloseLock.release()
//        }
    }

    private fun chooseOptimalSize(
        choices: Array<Size>, textureViewWidth: Int,
        textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size
    ): Size {

        // Collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough: MutableList<Size> = ArrayList()
        // Collect the supported resolutions that are smaller than the preview Surface
        val notBigEnough: MutableList<Size> = ArrayList()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight && option.height == option.width * h / w) {
                if (option.width >= textureViewWidth &&
                    option.height >= textureViewHeight
                ) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        return when {
            bigEnough.size > 0 -> {
                Collections.min(bigEnough, CompareSizesByArea())
            }
            notBigEnough.size > 0 -> {
                Collections.max(notBigEnough, CompareSizesByArea())
            }
            else -> {
                Log.e(TAG, "Couldn't find any suitable preview size")
                choices[0]
            }
        }
    }


    private fun startBackgroundThread() {
        if (mBackgroundThread == null) {
            mBackgroundThread = HandlerThread("imageAvailableListener")
            mBackgroundThread?.start()
            mBackgroundHandler = Handler(mBackgroundThread!!.looper)
        }
    }

    /**
     * 当摄像头数据回调可达的时候
     */
    internal interface OnPreviewCallbackListener {
        fun onImageAvailable(image: Image)
    }

    /**
     * 打开摄像头错误
     */
    internal interface OnOpenErrorListener {
        fun onOpenError()
    }

    internal class CompareSizesByArea : Comparator<Size> {

        override fun compare(o1: Size, o2: Size): Int {
            return java.lang.Long.signum(
                o1.width.toLong() * o1.height -
                        o2.width.toLong() * o2.height
            )
        }
    }

}