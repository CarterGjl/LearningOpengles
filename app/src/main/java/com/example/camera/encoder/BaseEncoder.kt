package com.example.camera.encoder

abstract class BaseEncoder( width: Int = -1, height: Int = -1):Runnable {

    /**
     * 是否手动编码
     * 视频：false 音频：true
     *
     * 注：视频编码通过Surface，MediaCodec自动完成编码；音频数据需要用户自己压入编码缓冲区，完成编码
     */
    open fun encodeManually() = true

    override fun run() {

    }
}