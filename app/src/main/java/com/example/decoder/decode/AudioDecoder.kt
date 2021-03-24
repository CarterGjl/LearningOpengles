package com.example.decoder.decode

import android.media.*
import android.util.Log
import com.example.decoder.AudioExtractor
import com.example.decoder.BaseDecoder
import com.example.decoder.interfa.IExtractor
import java.nio.ByteBuffer


class AudioDecoder(path: String) : BaseDecoder(path) {


    companion object {
        private const val TAG = "AudioDecoder"
    }

    /**采样率*/
    private var mSampleRate = -1

    /**声音通道数量*/
    private var mChannels = 1

    /**PCM采样位数*/
    private var mPCMEncodeBit = AudioFormat.ENCODING_PCM_16BIT

    /**音频播放器*/
    private var mAudioTrack: AudioTrack? = null

    /**音频数据缓存*/
    private var mAudioOutTempBuf: ShortArray? = null

    override fun render(outputBuffers: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        if (mAudioOutTempBuf!!.size < bufferInfo.size / 2) {
            mAudioOutTempBuf = ShortArray(bufferInfo.size / 2)
        }
        outputBuffers.position(0)
        outputBuffers.asShortBuffer().get(mAudioOutTempBuf, 0, bufferInfo.size / 2)
        mAudioTrack!!.write(mAudioOutTempBuf!!, 0, bufferInfo.size / 2)
    }

    override fun check(): Boolean {
        return true
    }

    override fun initExtractor(path: String): IExtractor {
        return AudioExtractor(path)
    }

    override fun initSpecParams(format: MediaFormat) {
        try {
            mChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)

            mPCMEncodeBit = if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    format.getInteger(MediaFormat.KEY_PCM_ENCODING)
                } else {
                    AudioFormat.ENCODING_PCM_16BIT
                }
            } else {
                //如果没有这个参数，默认为16位采样
                AudioFormat.ENCODING_PCM_16BIT
            }
        } catch (e: Exception) {
            Log.e(TAG, "initSpecParams: ", e)
        }

    }

    override fun doneDecode() {
        mAudioTrack?.stop()
        mAudioTrack?.release()
    }

    override fun initRender(): Boolean {
        val channel = if (mChannels == 1) {
            // 单声道
            AudioFormat.CHANNEL_OUT_MONO
        } else {
            // 双声道
            AudioFormat.CHANNEL_OUT_STEREO
        }
        //获取最小缓冲区
        val minBufferSize = AudioTrack.getMinBufferSize(mSampleRate, channel, mPCMEncodeBit)

        mAudioOutTempBuf = ShortArray(minBufferSize / 2)
        val build = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .build()
        val audioFormat = AudioFormat.Builder()
            .setChannelMask(channel)
            .setEncoding(mPCMEncodeBit)
            .setSampleRate(mSampleRate)
            .build()
        mAudioTrack = AudioTrack.Builder()
            .setAudioAttributes(build)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(minBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
//        mAudioTrack = AudioTrack(
//            AudioManager.STREAM_MUSIC,//播放类型：音乐
//            mSampleRate, //采样率
//            channel, //通道
//            mPCMEncodeBit, //采样位数
//            minBufferSize, //缓冲区大小
//            AudioTrack.MODE_STREAM
//        ) //播放模式：数据流动态写入，另一种是一次性写入

        mAudioTrack!!.play()


        return true
    }

    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        codec.configure(format, null, null, 0)
        return true
    }

}