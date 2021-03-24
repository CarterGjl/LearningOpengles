package com.example.myapplication.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import androidx.core.view.ViewCompat
import com.example.myapplication.ChatApplication
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.utils.dp2px
import com.example.myapplication.utils.getScreenHeight
import com.example.myapplication.utils.getScreenWidth
import kotlin.math.abs

object FloatView {
    val MIN_MOVE_SLOP = ViewConfiguration.get(ChatApplication.context).scaledTouchSlop
    private val PADDING = 8f.dp2px
    private var X_MIN: Int
    private var Y_MIN: Int
    private var X_MAX: Int
    private var Y_MAX: Int
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var type = WindowManager.LayoutParams.TYPE_TOAST

    init {
        X_MIN = PADDING
        X_MAX = ChatApplication.context.getScreenWidth() - PADDING
        Y_MIN = PADDING
        Y_MAX = ChatApplication.context.getScreenHeight() - PADDING
        initView()
    }

    @SuppressLint("InflateParams")
    private fun initView() {
        windowManager =
            ChatApplication.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            type = WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams = WindowManager.LayoutParams(
            69f.dp2px, 69f.dp2px, type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.START or Gravity.TOP
        layoutParams.x = (ChatApplication.context.getScreenWidth()
                - layoutParams.width - PADDING)
        layoutParams.y = 51f.dp2px
        val root = LayoutInflater.from(ChatApplication.context).inflate(R.layout.test, null, false)
        root.setOnTouchListener(Touch())
    }

    @SuppressLint("InflateParams")
    fun show() {
        val root = LayoutInflater.from(ChatApplication.context).inflate(R.layout.test, null, false)
        root.setOnTouchListener(Touch())
        layoutParams.width = 69f.dp2px
        layoutParams.height = 69f.dp2px
        layoutParams.x = (ChatApplication.context.getScreenWidth()
                - layoutParams.width - PADDING)
        layoutParams.y = 51f.dp2px
        windowManager.addView(root, layoutParams)
    }
    class Touch : View.OnTouchListener {
        private var x: Int = 0
        private var y = 0
        private var downX = 0
        private var downY = 0

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX.toInt()
                    y = event.rawY.toInt()
                    downX = x
                    downY = y
                }
                MotionEvent.ACTION_MOVE -> {
                    val nowX = event.rawX.toInt()
                    val nowY = event.rawY.toInt()
                    val movedX = nowX - x
                    val movedY = nowY - y
                    x = nowX
                    y = nowY
                    layoutParams.x = layoutParams.x + movedX
                    layoutParams.y = layoutParams.y + movedY
                    if (layoutParams.x + layoutParams.width > X_MAX) {
                        layoutParams.x = X_MAX - layoutParams.width
                    }
                    if (layoutParams.x < X_MIN) {
                        layoutParams.x = X_MIN
                    }
                    if (layoutParams.y < Y_MIN) {
                        layoutParams.y = Y_MIN
                    }
                    if (layoutParams.y + layoutParams.height > Y_MAX) {
                        layoutParams.y = Y_MAX - layoutParams.height
                    }
                    updateViewLayout(view)
                }
                MotionEvent.ACTION_UP -> {
                    val upX = event.rawX.toInt()
                    val upY = event.rawY.toInt()
                    if (abs(upX - downX) < MIN_MOVE_SLOP
                        && abs(upY - downY) < MIN_MOVE_SLOP
                    ) {
                        clickFloatWindow()
                        return false
                    }
                    if (upX > ChatApplication.context.getScreenWidth() / 2) {
                        animate(
                            view,
                            ChatApplication.context.getScreenWidth() - PADDING -
                                    layoutParams.x - layoutParams.width
                        )
                    } else {
                        animate(view, -layoutParams.x + PADDING)
                    }
                }
                else -> {
                }
            }
            return false
        }

        private fun clickFloatWindow() {
            val intent = Intent(
                ChatApplication.context,
                MainActivity::class.java
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ChatApplication.context.startActivity(
                intent
            )
        }

        private fun animate(view: View, distance: Int) {
            val valueAnimator = ValueAnimator.ofInt(distance)
            val originX = layoutParams.x
            valueAnimator.addUpdateListener { animation ->
                layoutParams.x = originX + animation.animatedValue as Int
                updateViewLayout(view)
            }
            valueAnimator.duration = 300
            valueAnimator.start()
        }

        private fun updateViewLayout(view: View) {
            if (ViewCompat.isAttachedToWindow(view)) {
                windowManager.updateViewLayout(view, layoutParams)
            }
        }
    }
}