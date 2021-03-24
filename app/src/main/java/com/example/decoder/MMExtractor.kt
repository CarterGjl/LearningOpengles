package com.example.decoder

import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

class MMExtractor(path: String) {

    /**音视频分离器*/
    private var mediaExtractor: MediaExtractor? = null
    /**音频通道索引*/
    private var mAudioTrack = -1
    /**视频通道索引*/
    private var mVideoTrack = -1
    /**当前帧时间戳*/
    private var mCurSampleTime: Long = 0
    /**开始解码时间点*/
    private var mStartPos: Long = 0
    init {
        // 初始化
        mediaExtractor = MediaExtractor()
        mediaExtractor?.setDataSource(path)
    }

    /**
     * 获取视频格式参数
     */
    fun getVideoFormat(): MediaFormat? {
        //【2.1，获取视频多媒体格式】
        for (index in 0 until mediaExtractor!!.trackCount){
            val trackFormat = mediaExtractor!!.getTrackFormat(index)
            val mime = trackFormat.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("video/") == true) {
                mVideoTrack = index
                break
            }
        }
        return if (mVideoTrack >= 0)
            mediaExtractor!!.getTrackFormat(mVideoTrack)
        else null
    }
    /**
     * 获取音频格式参数
     */
    fun getAudioFormat(): MediaFormat? {
        //【2.2，获取音频频多媒体格式】
        for (i in 0 until mediaExtractor!!.trackCount) {
            val mediaFormat = mediaExtractor!!.getTrackFormat(i)
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                mAudioTrack = i
                break
            }
        }
        return if (mAudioTrack >= 0) {
            mediaExtractor!!.getTrackFormat(mAudioTrack)
        } else null
    }

    /**
     * 选择通道
     */
    private fun selectSourceTrack() {
        if (mVideoTrack >= 0) {
            mediaExtractor!!.selectTrack(mVideoTrack)
        } else if (mAudioTrack >= 0) {
            mediaExtractor!!.selectTrack(mAudioTrack)
        }
    }

    /**
     * 读取视频数据
     * 注意：如果SampleSize返回-1，说明没有更多的数据了。
     * 这个时候，queueInputBuffer的最后一个参数要传入结束标记MediaCodec.BUFFER_FLAG_END_OF_STREAM。
     */
    fun readBuffer(byteBuffer: ByteBuffer): Int {
        //【3，提取数据】
        byteBuffer.clear()
        selectSourceTrack()
        val readSampleData = mediaExtractor!!.readSampleData(byteBuffer, 0)
        if (readSampleData < 0){
            return -1
        }
        mCurSampleTime = mediaExtractor!!.sampleTime
        mediaExtractor!!.advance()
        return readSampleData;
    }

    fun getVideoTrack(): Int {
        return mVideoTrack
    }

    fun getAudioTrack(): Int {
        return mAudioTrack
    }

    fun getCurrentTimestamp(): Long {
        return mCurSampleTime
    }

    /**
     * 说明：seek(pos: Long)方法，主要用于跳播，快速将数据定位到指定的播放位置，
     * 但是，由于视频中，除了I帧以外，PB帧都需要依赖其他的帧进行解码，所以，通常只
     * 能seek到I帧，但是I帧通常和指定的播放位置有一定误差，因此需要指定seek靠近哪
     * 个关键帧，有以下三种类型：
        SEEK_TO_PREVIOUS_SYNC：跳播位置的上一个关键帧
        SEEK_TO_NEXT_SYNC：跳播位置的下一个关键帧
        EEK_TO_CLOSEST_SYNC：距离跳播位置的最近的关键帧
        到这里你就可以明白，为什么我们平时在看视频时，拖动进度条释放以后，视频通常会
        在你释放的位置往前一点
   */

    /**
     * Seek到指定位置，并返回实际帧的时间戳
     */
    fun seek(pos: Long): Long {
        mediaExtractor!!.seekTo(pos,MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        return mediaExtractor!!.sampleTime
    }

    fun setStartPos(pos: Long) {
        mStartPos = pos
    }

    // 停止取数据
    fun stop() {
        //【4，释放提取器】
        mediaExtractor?.release()
        mediaExtractor = null
    }

}