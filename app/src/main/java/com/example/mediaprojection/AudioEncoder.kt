package com.example.mediaprojection

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer

class AudioEncoder(private val mediaMuxer: MediaMuxer) {
    private var codec: MediaCodec? = null
    private var audiotrack = -1
    init {
        configEncode()
    }

    private fun configEncode() {
        codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        val createAudioFormat =
            MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2)
        createAudioFormat.apply {
            setInteger(MediaFormat.KEY_BIT_RATE, 96000)
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096)
        }
        codec!!.configure(createAudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec!!.setCallback(object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {

            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                audiotrack = mediaMuxer.addTrack(format)
            }

        })
        codec!!.start()
    }

    fun stop() {
        codec?.stop()
        codec?.release()
        codec = null
    }
}