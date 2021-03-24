package com.example.decoder.decode

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.decoder.BaseDecoder
import com.example.decoder.VideoExtractor
import com.example.decoder.interfa.IExtractor
import java.nio.ByteBuffer

class VideoDecoder(path: String, sfv: SurfaceView?, surface: Surface?) : BaseDecoder(path) {

    companion object {
        private const val TAG = "VideoDecoder"
    }

    private val mSurfaceView = sfv
    private var mSurface = surface

    override fun render(outputBuffers: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {

    }

    override fun check(): Boolean {
        if (mSurfaceView == null && mSurface == null) {
            Log.w(TAG, "SurfaceView和Surface都为空，至少需要一个不为空")
            mStateListener?.decoderError(this, "显示器为空")
            return false
        }
        return true
    }

    override fun initExtractor(path: String): IExtractor {
        return VideoExtractor(path)
    }

    override fun initSpecParams(format: MediaFormat) {
    }

    override fun doneDecode() {
    }

    override fun initRender(): Boolean {
        return true
    }

    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        if (mSurface != null) {
            codec.configure(format, mSurface, null, 0)
            notifyDecode()
        } else {
            mSurfaceView?.holder?.addCallback(object : SurfaceHolder.Callback2 {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    mSurface = holder.surface
                    configCodec(codec, format)
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {

                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {

                }

                override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
                }

            })
            return false
        }
        return true
    }


}