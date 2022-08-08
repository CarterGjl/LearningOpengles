package com.example.mediaprojection

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.hardware.*
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expanded.ExpandedItem
import com.example.expanded.MyExpandedAdapter
import com.example.myapplication.R
import kotlinx.android.synthetic.main.activity_media_projection.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.random.Random


class MediaProjectionActivity : AppCompatActivity() {


    private var videoEncoder: VideoEncoder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val PERIODIC_TASK = 3

    @SuppressLint("WrongConstant")
    private val mImageReader = ImageReader.newInstance(
        1280, 720,
        PixelFormat.RGBA_8888,  /*maxImages*/3
    )
    private var backThread: HandlerThread? = null
    private var screenShareHandler: Handler? = null
    private val mMediaProjectionManager: MediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }


    private var mMediaProjection: MediaProjection? = null

    companion object {
        private const val TAG = "MediaProjectionActivity"
        private const val ALL_PERMISSIONS_PERMISSION_CODE = 1000
    }


    private var handler:Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_projection)
        handler = Handler(Looper.getMainLooper()) { message ->
            when (message.arg1) {
                PERIODIC_TASK -> {
                    if (message.arg1 > 0) {
                        val mMessage: Message = handler!!.obtainMessage(message.what)
                        mMessage.arg1 = message.arg1
                        mMessage.arg2 = message.arg2
                        (message.obj as Runnable).run()
                        mMessage.obj = message.obj
                        handler!!.sendMessageDelayed(mMessage, mMessage.arg2.toLong())
                    }
                }
            }


            true
        }
        val message: Message = handler!!.obtainMessage(0)
        message.obj = Runnable {
            Log.d(TAG, "onCreate: ")
            val nextInt = Random.nextInt(100) +10
//            voicLine.setVolume(nextInt)
        }
        message.arg1 = PERIODIC_TASK
        message.arg2 = 200
        handler!!.sendMessageDelayed(message, 10)

        lifecycle.addObserver(object : LifecycleObserver {

        })
        val locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
//        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
//        val newWakeLock = powerManager.newWakeLock(PROXIMITY_SCREEN_OFF_WAKE_LOCK, "TEST:CALL")
//        newWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val s: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        lifecycleScope.launch {
        }
        MainScope().launch(Dispatchers.Main) {

        }
        sensorManager.registerListener(object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                Log.d(TAG, "onSensorChanged:$--》 ${event.values[0]}")
//                var size: Int = event.values.size
//                Log.d(TAG, "onSensorChanged: maximumRange${event.sensor.maximumRange}")
//                for (index in 0 until size) {
//                    Log.d(TAG, "onSensorChanged:$index--》 ${event.values[index]}")
//                }

            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                Log.d(TAG, "onSensorChanged:$--》 ")
            }

        }, s, SensorManager.SENSOR_DELAY_UI)
        checkAndRequestPermissions()
        start.setOnClickListener {
//            startRecording()
//            var mCamera = Camera.open(1)
//            for (supportedPreviewSize in mCamera.parameters.supportedPreviewSizes) {
//                Log.d(TAG, "onCreate: size width ${supportedPreviewSize.width} height ${supportedPreviewSize.height}")
//            }
        }
        stop.setOnClickListener {
//            stopRecording()
            videoEncoder?.stop()
        }
        capture.setOnClickListener {
            initAudioCapture()
        }
        rv_ex.layoutManager = LinearLayoutManager(this)
        val myExpandedAdapter = MyExpandedAdapter(this)
        val arrayList = ArrayList<String>()
        for (i in 0 until 100){
            arrayList.add("$i")
        }
        val expandedItem1 = ExpandedItem<String, String>("1", arrayList)
        val expandedItem2 = ExpandedItem<String, String>("2", arrayList)
        val expandedItem3 = ExpandedItem<String, String>("3", arrayList)

        myExpandedAdapter.addExpandedItem(expandedItem1)
        myExpandedAdapter.addExpandedItem(expandedItem2)
        myExpandedAdapter.addExpandedItem(expandedItem3)
        myExpandedAdapter.refreshDatas()
        rv_ex.adapter = myExpandedAdapter

    }

    private fun startBackground() {
        if (backThread == null) {
            backThread = HandlerThread("ScreenShare", Process.THREAD_PRIORITY_URGENT_DISPLAY)
            backThread!!.start()
            screenShareHandler = Handler(backThread!!.looper)
        }
    }

    private val value = object : MediaProjection.Callback() {

        override fun onStop() {
            super.onStop()
            Log.d(TAG, "onStop: ${Thread.currentThread().name}")
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        val appPermissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            appPermissions[appPermissions.size - 1] = Manifest.permission.FOREGROUND_SERVICE
        }
        for (permission in appPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionsNeeded.add(permission)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                ALL_PERMISSIONS_PERMISSION_CODE
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ALL_PERMISSIONS_PERMISSION_CODE) {
            val permissionResults: HashMap<String?, Int> = HashMap()
            var deniedCount = 0
            for (permissionIndx in permissions.indices) {
                if (grantResults[permissionIndx] != PackageManager.PERMISSION_GRANTED) {
                    permissionResults[permissions[permissionIndx]] = grantResults[permissionIndx]
                    deniedCount++
                }
            }
            if (deniedCount != 0) {
                Log.e(TAG, "Permission Denied!  Now you must allow  permission from settings.")
            }
        }
    }

    private val screenRecordLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                startBackground()
                val data = it.data
                data?.apply {
                    mMediaProjection =
                        mMediaProjectionManager.getMediaProjection(it.resultCode, this)
                    mMediaProjection!!.registerCallback(value, screenShareHandler)
                    this@MediaProjectionActivity.virtualDisplay = createVirtualDisplay()
                }
                val i = Intent(this, MediaCaptureService::class.java)
                i.action = MediaCaptureService.ACTION_START
                i.putExtra(MediaCaptureService.EXTRA_RESULT_CODE, it.resultCode)
                i.putExtras(data!!)
                startService(i)
            } else if (it.resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(
                    this,
                    "User denied screen sharing permission", Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun initAudioCapture() {
        val intent: Intent = mMediaProjectionManager.createScreenCaptureIntent()
        screenRecordLauncher.launch(intent)
    }

    private fun stopRecording() {
        val broadCastIntent = Intent()
        broadCastIntent.action = MediaCaptureService.ACTION_ALL
        broadCastIntent.putExtra(
            MediaCaptureService.EXTRA_ACTION_NAME,
            MediaCaptureService.ACTION_STOP
        )
        this.sendBroadcast(broadCastIntent)
    }

    private fun startRecording() {
        val broadCastIntent = Intent()
        broadCastIntent.action = MediaCaptureService.ACTION_ALL
        broadCastIntent.putExtra(
            MediaCaptureService.EXTRA_ACTION_NAME,
            MediaCaptureService.ACTION_START
        )
        this.sendBroadcast(broadCastIntent)
    }

    @SuppressLint("WrongConstant")
    private fun createVirtualDisplay(): VirtualDisplay? {

//        mImageReader.setOnImageAvailableListener({
////            Log.d(TAG, "createVirtualDisplay: ${Thread.currentThread().name}")
//            val image = it.acquireLatestImage()
//            if (image != null) {
//                val planes = image.planes
//
//                val buffer = planes[0].buffer
//                val pixelStride = planes[0].pixelStride
//                val rowStride = planes[0].rowStride
//                val rowPadding = rowStride - pixelStride * 1280;
//                val bitmap = Bitmap.createBitmap(
//                    1280 + rowPadding / pixelStride,
//                    720,
//                    Bitmap.Config.ARGB_8888
//                )
//                bitmap.copyPixelsFromBuffer(buffer)
//                runOnUiThread {
//                    imageView.setImageBitmap(bitmap)
//                }
//                image.close()
////                virtualDisplay?.release()
////                mMediaProjection?.stop()
//            }
//        }, screenShareHandler)
        videoEncoder = VideoEncoder(this)
        return mMediaProjection?.createVirtualDisplay(
            "ScreenSharingDemo",
            1920, 1080, 1,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            videoEncoder?.surface, object : VirtualDisplay.Callback() {
                override fun onStopped() {
                    super.onStopped()
                    Log.d(TAG, "onStopped: ${Thread.currentThread().name}")
                }

                override fun onResumed() {
                    super.onResumed()
//                        virtualDisplay?.surface = mImageReader.surface
                    Log.d(TAG, "onResumed: ${Thread.currentThread().name}")
                }

                override fun onPaused() {
                    super.onPaused()
                    Log.d(TAG, "onPaused: ${Thread.currentThread().name}")
                }

            }, screenShareHandler
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaProjection?.unregisterCallback(value)
        virtualDisplay?.release()
        mMediaProjection?.stop()
    }
}