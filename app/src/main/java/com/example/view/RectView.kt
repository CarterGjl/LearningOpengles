package com.example.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R

class RectView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val color = Color.RED
    private val color1 = Color.BLUE
    lateinit var drawable:BitmapDrawable
    init {
        initDrawer()
    }

    private fun initDrawer() {
        paint.color = color
        paint.strokeWidth = 1.5F
        drawable =  ResourcesCompat.getDrawable(resources,R.drawable.test,null) as BitmapDrawable

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(),paint)
        paint.color = color1
        canvas.drawRect(40F, 40F, width.toFloat()/2, height.toFloat()/2,paint)
        paint.isFilterBitmap = true
        paint.isDither = true
        canvas.drawBitmap(drawable.bitmap,0F, 0F,paint)
    }
}