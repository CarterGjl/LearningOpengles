package com.example.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.myapplication.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.sin


@SuppressLint("CustomViewStyleable")
class VoiceLineView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val LINE = 0
        private const val RECT = 1
    }

    private var middleLineColor: Int = Color.BLACK
    private var voiceLineColor: Int = Color.BLACK
    private var middleLineHeight = 4f
    private var paint: Paint? = null
    private var paintVoicLine: Paint? = null
    private var mode = 0

    /**
     * 灵敏度
     */
    private var sensibility = 4

    private var maxVolume = 100f


    private var translateX = 0f
    private var isSet = false

    /**
     * 振幅
     */
    private var amplitude = 1f

    /**
     * 音量
     */
    private var volume = 10f
    private var fineness = 1
    private var targetVolume = 1f


    private var speedY: Long = 50
    private var rectWidth = 25f
    private var rectSpace = 5f
    private var rectInitHeight = 4f
    private var rectList: LinkedList<Rect>? = null

    private var lastTime: Long = 0
    private var lineSpeed = 90

    private var paths: ArrayList<Path>? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.voiceView)
        mode = typedArray.getInt(R.styleable.voiceView_viewMode, LINE)
        voiceLineColor = typedArray.getColor(R.styleable.voiceView_voiceLine, Color.BLACK)
        maxVolume = typedArray.getFloat(R.styleable.voiceView_maxVolume, 100f)
        sensibility = typedArray.getInt(R.styleable.voiceView_sensibility, 4)
        if (RECT == mode) {
            rectWidth = typedArray.getDimension(R.styleable.voiceView_rectWidth, 25f)
            rectSpace = typedArray.getDimension(R.styleable.voiceView_rectSpace, 5f)
            rectInitHeight = typedArray.getDimension(R.styleable.voiceView_rectInitHeight, 4f)
        } else {
            middleLineColor = typedArray.getColor(R.styleable.voiceView_middleLine, Color.BLACK)
            middleLineHeight = typedArray.getDimension(R.styleable.voiceView_middleLineHeight, 4f)
            lineSpeed = typedArray.getInt(R.styleable.voiceView_lineSpeed, 90)
            fineness = typedArray.getInt(R.styleable.voiceView_fineness, 1)
            paths = ArrayList(20)
            for (i in 0..19) {
                paths!!.add(Path())
            }
        }
        typedArray.recycle()

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mode == RECT) {
            drawVoiceRect(canvas = canvas)
        } else {
            drawMiddleLine(canvas = canvas)
            drawVoiceLine(canvas = canvas)
        }
        run()
    }

    private fun drawVoiceRect(canvas: Canvas) {
        if (paintVoicLine == null) {
            paintVoicLine = Paint()
            paintVoicLine!!.color = voiceLineColor
            paintVoicLine!!.isAntiAlias = true
            paintVoicLine!!.style = Paint.Style.FILL
            paintVoicLine!!.strokeWidth = 2f
        }
        if (rectList == null) {
            rectList = LinkedList()
        }
        val totalWidth = (rectSpace + rectWidth).toInt()
        if (speedY % totalWidth < 6) {
            val rect = Rect(
                (-rectWidth - 10 - speedY + speedY % totalWidth).toInt(),
                ((height / 2 - rectInitHeight / 2).toInt() - (if (volume == 10f) 0 else volume / 2).toInt()),
                (-10 - speedY + speedY % totalWidth).toInt(),
                ((height / 2 + rectInitHeight / 2).toInt() + (if (volume == 10f) 0 else volume / 2).toInt())
            )
            if (rectList!!.size > width / (rectSpace + rectWidth) + 2) {
                rectList!!.removeAt(0)
            }
            rectList!!.add(rect)
        }
        canvas.translate(speedY.toFloat(), 0f)
        for (i in rectList!!.indices.reversed()) {
            canvas.drawRect(rectList!![i], paintVoicLine!!)
        }
        rectChange()
    }


    private fun drawMiddleLine(canvas: Canvas) {
        if (paint == null) {
            paint = Paint()
            paint!!.color = middleLineColor
            paint!!.isAntiAlias = true
        }
        canvas.save()
        canvas.drawRect(
            0f, height / 2 - middleLineHeight / 2,
            width.toFloat(), height / 2 + middleLineHeight / 2, paint!!
        )
        canvas.restore()
    }

    private fun drawVoiceLine(canvas: Canvas) {
        lineChange()
        if (paintVoicLine == null) {
            paintVoicLine = Paint()
            paintVoicLine!!.color = voiceLineColor
            paintVoicLine!!.isAntiAlias = true
            paintVoicLine!!.style = Paint.Style.STROKE
            paintVoicLine!!.strokeWidth = 2f
        }
        canvas.save()
        val moveY = height / 2
        for (i in paths!!.indices) {
            paths!![i].reset()
            paths!![i].moveTo(width.toFloat(), (height / 2).toFloat())
        }
        var i = (width - 1).toFloat()
        while (i >= 0) {
            amplitude = 4 * volume * i / width - 4 * volume * i * i / width / width
            for (n in 1..paths!!.size) {
                val sin = amplitude * sin(
                    (i - 1.22.pow(n.toDouble())) * Math.PI / 180 - translateX
                )
                    .toFloat()
                paths!![n - 1].lineTo(
                    i,
                    2 * n * sin / paths!!.size - 15 * sin / paths!!.size + moveY
                )
            }
            i -= fineness.toFloat()
        }
        for (n in paths!!.indices) {
            if (n == paths!!.size - 1) {
                paintVoicLine!!.alpha = 255
            } else {
                paintVoicLine!!.alpha = n * 130 / paths!!.size
            }
            if (paintVoicLine!!.alpha > 0) {
                canvas.drawPath(paths!![n], paintVoicLine!!)
            }
        }
        canvas.restore()
    }

    private fun rectChange() {
        speedY += 6
        if (volume < targetVolume && isSet) {
            volume += (height / 30).toFloat()
        } else {
            isSet = false
            if (volume <= 10) {
                volume = 10f
            } else {
                volume -= if (volume < height / 30) {
                    (height / 60).toFloat()
                } else {
                    (height / 30).toFloat()
                }
            }
        }
    }

    fun setVolume(volume: Int) {
        if (volume > maxVolume * sensibility / 25) {
            isSet = true
            targetVolume = height * volume / 2 / maxVolume
        }
    }


    private fun lineChange() {
        if (lastTime == 0L) {
            lastTime = System.currentTimeMillis()
            translateX += 1.5f
        } else {
            if (System.currentTimeMillis() - lastTime > lineSpeed) {
                lastTime = System.currentTimeMillis()
                translateX += 1.5f
            } else {
                return
            }
        }
        if (volume < targetVolume && isSet) {
            volume += (height / 30).toFloat()
        } else {
            isSet = false
            if (volume <= 10) {
                volume = 10f
            } else {
                volume -= if (volume < height / 30) {
                    (height / 60).toFloat()
                } else {
                    (height / 30).toFloat()
                }
            }
        }
    }

    fun run() {
        if (mode == RECT) {
            postInvalidateDelayed(30)
        } else {
            invalidate()
        }
    }


}