package com.example.decoder.interfa

import android.media.MediaFormat
import com.example.decoder.BaseDecoder
import com.example.decoder.Frame

interface IDecoder : Runnable {

    // 暂停编码
    fun pause()

    // 继续编码
    fun goOn()

    // 停止编码
    fun stop()

    // 是否正在编码
    fun isEncoding(): Boolean

    // 是否正在快进
    fun isSeeking(): Boolean

    // 是否停止解码
    fun isStop(): Boolean

    // 状态监听
    fun setStateListener(l: IDecoderStateListener?)

    // 视频宽
    fun getWidth(): Int

    // 视频高
    fun getHeight(): Int

    // 视频时长
    fun getDuration(): Long

    // 视频旋转角度
    fun getRotationAngle(): Int

    // 视频对应得格式参数
    fun getMediaFormat(): MediaFormat?

    // 获得音视频对应得媒体轨道
    fun getTack(): Int

    // 解码文件路径
    fun getFilePath(): String

    fun getCurTimeStamp(): Long
}

interface IDecoderStateListener {
    fun decoderPrepare(baseDecoder: BaseDecoder)
    fun decoderError(baseDecoder: BaseDecoder, msg: String)
    fun decoderRunning(baseDecoder: BaseDecoder)
    fun decoderPause(baseDecoder: BaseDecoder)
    fun decoderFinish(baseDecoder: BaseDecoder)
    fun decoderDestroy(baseDecoder: BaseDecoder)
    fun decodeOneFrame(baseDecoder: BaseDecoder, frame: Frame)

}
