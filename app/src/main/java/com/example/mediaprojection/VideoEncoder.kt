package com.example.mediaprojection

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class VideoEncoder(private val context: Context) {
    private var mediaCodec: MediaCodec? = null
    var surface: Surface? = null
    var mediaMuxer: MediaMuxer? = null
    private var videoTrack = -1
    var handler: Handler? = null
    private var handlerThread: HandlerThread? = null

    companion object {
        private const val TAG = "VideoEncoder"
    }

    private val mCallback: MediaCodec.Callback = object : MediaCodec.Callback() {
        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        }

        override fun onOutputBufferAvailable(
            codec: MediaCodec,
            index: Int,
            info: MediaCodec.BufferInfo
        ) {
            val outputBuffer = codec.getOutputBuffer(index)
            if (videoTrack >= 0 && outputBuffer != null) {
                mediaMuxer?.writeSampleData(videoTrack, outputBuffer, info)
            }
            Log.d(TAG, "onOutputBufferAvailable: $index  ---${info.size}")
            codec.releaseOutputBuffer(index, false)
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            videoTrack = mediaMuxer!!.addTrack(format)
            mediaMuxer!!.start()
        }

    }

    private fun configMediaCodec() {
        val createVideoFormat =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 1080, 1920)
        createVideoFormat.apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BIT_RATE, 400_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, 15)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        }
        handler = startBackground()
        try {
            // 创建编码器
            mediaCodec = MediaCodec.createEncoderByType("video/avc")
            mediaCodec?.setCallback(mCallback, handler)
            mediaCodec?.configure(createVideoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            surface = mediaCodec?.createInputSurface()
            mediaCodec?.start()
            val sampleDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DCIM),
                "/TestRecording"
            )
            if (!sampleDir.exists()) {
                sampleDir.mkdirs()
            }
            val dateTime = SimpleDateFormat.getDateTimeInstance().format(Date())
            val fileName = "Record-$dateTime.mp4"
            val filePath = sampleDir.absolutePath + "/" + fileName
            mediaMuxer = MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (e: Exception) {
            Log.e(TAG, "configMediaCodec: ", e)
        }
    }

    private fun startBackground(): Handler {
        handlerThread = HandlerThread("video_encoder")
        handlerThread!!.start()
        return Handler(handlerThread!!.looper)
    }

    private fun stopBackground() {
        handlerThread?.quit()
        handlerThread = null
    }

    init {
        configMediaCodec()
    }

    fun stop() {
        Log.d("mmm", "停止写入")

        mediaCodec?.stop()
        mediaCodec?.release()
        mediaCodec = null
        mediaMuxer?.stop()
        mediaMuxer?.release()
        mediaMuxer = null
        stopBackground()
    }
}