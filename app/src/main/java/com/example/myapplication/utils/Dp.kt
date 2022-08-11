package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.ChatApplication
import com.example.myapplication.R

val Float.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )
val Float.dp2px: Int
    get() {
        val scale = ChatApplication.context.resources.displayMetrics.density
        val fl = scale * this + 0.5f
        return fl.toInt()
    }

fun Context.getScreenWidth(): Int {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return windowManager.currentWindowMetricsPointCompat().x
}


fun WindowManager.currentWindowMetricsPointCompat(): Point {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowInsets = currentWindowMetrics.windowInsets
        var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
        windowInsets.displayCutout?.run {
            insets = Insets.max(
                insets,
                Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom)
            )
        }
        val insetsWidth = insets.right + insets.left
        val insetsHeight = insets.top + insets.bottom
        Point(
            currentWindowMetrics.bounds.width() - insetsWidth,
            currentWindowMetrics.bounds.height() - insetsHeight
        )
    } else {
        Point().apply {
            defaultDisplay.getSize(this)
        }
    }
}
fun Context.getScreenHeight():Int{
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return windowManager.currentWindowMetricsPointCompat().y
}
fun Context.makeTopToast(text: String, during: Int = Toast.LENGTH_SHORT) {
    val result = Toast(this)
    val inflate = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    @SuppressLint("InflateParams")
    val v: View = inflate.inflate(R.layout.custom_toast, null)
    val tv = v.findViewById<TextView>(R.id.text)

    tv.text = text
    result.setGravity(Gravity.TOP, 0, 100f.dp2px)
    result.duration = during
    result.view = v
    result.show()
}