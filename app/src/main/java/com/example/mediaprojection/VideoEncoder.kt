package com.example.mediaprojection

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*


class VideoEncoder(private val context: Context) {
    private var mediaCodec: MediaCodec? = null
    var surface: Surface? = null
    var mediaMuxer: MediaMuxer? = null
    private var videoTrack = -1
    var handler: Handler? = null
    private var handlerThread: HandlerThread? = null
    private var os: FileOutputStream? = null
    companion object {
        private const val TAG = "VideoEncoder"
    }
    private val sampleDir = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DCIM),
        "/TestRecordingData"
    )

    private val mCallback: MediaCodec.Callback = object : MediaCodec.Callback() {
        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            // 当输入缓冲区可用时此方法会被调用 使用mediacodec inputsurface 编码时不需要使用该方法
        }

        override fun onOutputBufferAvailable(
            codec: MediaCodec,
            index: Int,
            info: MediaCodec.BufferInfo
        ) {
            // 获得输出缓冲区
            val outputBuffer = codec.getOutputBuffer(index)
//            if (videoTrack >= 0 && outputBuffer != null) {
//                mediaMuxer?.writeSampleData(videoTrack, outputBuffer, info)
//            }
            outputBuffer?.let { writeH264DataToFile(it,info) }
            Log.d(TAG, "onOutputBufferAvailable: $index  ---${info.size}")
            // 记得释放
            codec.releaseOutputBuffer(index, false)
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
//            videoTrack = mediaMuxer!!.addTrack(format)
//            mediaMuxer!!.start()
        }

    }

    fun setRate(rate:Int){
        val bundle = Bundle()
        bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, rate)
        mediaCodec?.setParameters(bundle)
    }
    fun setKeyFrame(keyFrame:Int){
        val bundle = Bundle()
        bundle.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME,keyFrame)
    }
    private fun configMediaCodec() {
        val createVideoFormat =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 1080, 1920)
        createVideoFormat.apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
            // 编码比特率
            setInteger(MediaFormat.KEY_BIT_RATE, 400_000)
            // 编码帧数
            setInteger(MediaFormat.KEY_FRAME_RATE, 15)
            // 关键帧
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        }
        handler = startBackground()
        try {
            // 创建编码器
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            mediaCodec?.setCallback(mCallback, handler)
            // 配置mediacode属性
            mediaCodec?.configure(createVideoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            // 获取medicodec interface  传递给 mediaprojection 用于接收数据
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
//            mediaMuxer = MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
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
        if (!sampleDir.exists()) {
            val mkdirs = sampleDir.mkdirs()
        }
        val format = SimpleDateFormat.getDateTimeInstance().format(Date())
        val fileName = "Record-$format.h264"
        val filePath = sampleDir.absolutePath + "/" + fileName

        try {
            os = FileOutputStream(filePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun writeH264DataToFile(byteBuffer: ByteBuffer,info: MediaCodec.BufferInfo) {
        // Write the output audio in byte

        try {
            val outData = ByteArray(info.size)
            byteBuffer.get(outData)
            // // writes the data to file from buffer
            // // stores the voice buffer
            os?.write(outData, 0, outData.size)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun stop() {


        try {
            os?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.d("mmm", "停止写入")

        mediaCodec?.stop()
        mediaCodec?.release()
        mediaCodec = null
//        mediaMuxer?.stop()
//        mediaMuxer?.release()
//        mediaMuxer = null
        stopBackground()
    }
}