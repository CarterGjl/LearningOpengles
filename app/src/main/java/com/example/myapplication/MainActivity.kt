package com.example.myapplication

import android.annotation.SuppressLint
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.Icon
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.example.camera.camera.CameraActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.local.User
import com.example.myapplication.utils.UIEvent


class MainActivity : AppCompatActivity() {
    private var remoteService: IRemoteService? = null
    private val mCallBack = object : IRemoteCallBack.Stub() {
        override fun notifyEvent(event: String?) {
            println(event)
        }

        override fun showUser(user: User?) {
            println(user)
            activityMainBinding.coreProcess.text = user.toString()
        }
    }

    private val ns = NOTIFICATION_SERVICE
    private val mNotificationManager: NotificationManager by lazy {
        getSystemService(ns) as NotificationManager
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var xSpringAnimation: SpringAnimation
    private lateinit var ySpringAnimation: SpringAnimation
    private lateinit var activityMainBinding: ActivityMainBinding
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            remoteService = IRemoteService.Stub.asInterface(service)
            remoteService?.registerCallback(mCallBack)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            remoteService?.unregisterCallback(mCallBack)
            remoteService = null
        }

    }
    var xDiffLeft: Float? = null
    private var yDiffTop: Float? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        try {
            val result = bindService(
                Intent(this, RemoteService::class.java),
                mConnection,
                Context.BIND_AUTO_CREATE
            )
            println(result)

        } catch (e: Exception) {
            e.printStackTrace()
        }

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            if (Settings.canDrawOverlays(this)) {
//                FloatView.show()
//            } else {
//                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
//                intent.data = Uri.parse("package:$packageName")
//                startActivity(intent)
//            }
//        }

        activityMainBinding.imageView.postDelayed({
            val intArray = IntArray(2)
            activityMainBinding.imageView.getLocationInWindow(intArray)

            for (i in intArray) {
                println("getLocationInWindow$i")
            }

        }, 1000)
        activityMainBinding.coreProcess.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val activity = PendingIntent.getActivity(this, 0, intent, 0)

            val build = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Notification.BubbleMetadata.Builder()
                    .setDesiredHeight(900)
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))

                    .setIntent(activity)
                    .build()
            } else {
                null
            }
            val build1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Person.Builder()
                    .setBot(true)
                    .setName("BubbleBot")
                    .setImportant(true)
                    .build()
            } else {
                null
            }
            val build2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Notification.Builder(this, "test")
                        .setContentIntent(activity)
                        .setBubbleMetadata(build)
                        .addPerson(build1)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .build()
                } else {
                    TODO("VERSION.SDK_INT < Q")
                }
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            val notificationChannel =
                NotificationChannel("test", "test", NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
            notificationManager.notify(1, build2)
            remoteService?.basicTypes(1, 2, true, 0F, 0.0, "")
        }
        SpringAnimation(activityMainBinding.imageView, DynamicAnimation.TRANSLATION_Y, 100f).apply {
            spring.dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            start()
        }
        val springForce = SpringForce(0f).apply {
            dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            stiffness = SpringForce.STIFFNESS_MEDIUM
        }
        xSpringAnimation =
            SpringAnimation(
                activityMainBinding.imageView,
                DynamicAnimation.TRANSLATION_X
            ).setSpring(springForce)
        ySpringAnimation =
            SpringAnimation(
                activityMainBinding.imageView,
                DynamicAnimation.TRANSLATION_Y
            ).setSpring(springForce)
        activityMainBinding.imageView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    xDiffLeft = event.rawX - activityMainBinding.imageView.x
                    yDiffTop = event.rawY - activityMainBinding.imageView.y
                    xSpringAnimation.cancel()
                    ySpringAnimation.cancel()
                }
                MotionEvent.ACTION_MOVE -> {
//                    imageView.x = event.rawX - xDiffLeft!!
                    val fl = event.rawY - yDiffTop!!
                    if (fl < 0) {
                        return@setOnTouchListener false
                    }
                    activityMainBinding.imageView.y = fl
                }
                MotionEvent.ACTION_UP -> {
                    xSpringAnimation.start()
                    ySpringAnimation.start()
                }
            }
            if (event.rawY - yDiffTop!! > 100f) {
                return@setOnTouchListener true
            }
            false
        }
        activityMainBinding.imageView.setOnClickListener {
            Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
        }

        UIEvent.uiEvent.notifications(Message.obtain())
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")

    }

    private fun createNotify() {
        val settingsFragment = SettingsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.root, settingsFragment).commit()
//        PermissionDialog().show(supportFragmentManager, "permisson")
//        val intent = Intent(Intent.ACTION_MAIN, null)
//        intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
//                or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
//        intent.setClass(this, MainActivity2::class.java)
//        startActivity(intent)
        Log.d(TAG, "startActivity: ")
//        val fullScreenIntent = Intent(this, MainActivity2::class.java)
//        val fullScreenPendingIntent = PendingIntent.getActivity(
//            this, 0,
//            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel =
//                NotificationChannel("CHANNEL_ID", "test", NotificationManager.IMPORTANCE_HIGH)
//            mNotificationManager.createNotificationChannel(channel)
//
//        }
//
//
//
//        val notificationBuilder =
//            NotificationCompat.Builder(this, "CHANNEL_ID")
//                .setSmallIcon(R.drawable.ic_launcher_background)
//                .setContentTitle("Incoming call")
//                .setContentText("(919) 555-1234")
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setCategory(NotificationCompat.CATEGORY_CALL)
//                // Use a full-screen intent only for the highest-priority alerts where you
//                // have an associated activity that you would like to launch after the user
//                // interacts with the notification. Also, if your app targets Android 10
//                // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
//                // order for the platform to invoke this notification.
//                .setFullScreenIntent(fullScreenPendingIntent, true)
//
//        val incomingCallNotification = notificationBuilder.build()
//        mNotificationManager.notify(1,incomingCallNotification)
    }

    private fun initListener() {
        activityMainBinding.btnDataStore.setOnClickListener {
        }
        activityMainBinding.btn.setOnClickListener {
            Handler().postDelayed({
                createNotify()
            }, 20000)
//            val intent = Intent(this, MainActivity2::class.java)
//            startActivity(intent)
        }
        activityMainBinding.glBtn.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }


    }
}