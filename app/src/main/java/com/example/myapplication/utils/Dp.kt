package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
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
fun Context.getScreenWidth():Int{
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    windowManager.defaultDisplay.getSize(point)
    return point.y
}
fun Context.getScreenHeight():Int{
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        val bottom = windowManager.currentWindowMetrics.bounds.bottom
        println("R $bottom")
    }
    val point = Point()
    windowManager.defaultDisplay.getSize(point)
    println("old ${point.y}")
    return point.y
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