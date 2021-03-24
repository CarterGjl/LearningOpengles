package com.example.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import com.example.decoder.interfa.IDecoder
import com.example.decoder.interfa.IDecoderStateListener
import com.example.decoder.interfa.IExtractor
import java.io.File
import java.nio.ByteBuffer

abstract class BaseDecoder(private val mFilePath: String) : IDecoder {


    companion object {
        private const val TAG = "BaseDecoder"
    }

    private var mStartPos: Long = 0
    private var mEndPos = 0L
    private var mDuration: Long = 0L

    //-------------线程相关------------------------
    private var mIsRunning = true

    // 线程等待锁
    private val mLock = Object()

    // 是否可以进入解码
    private var mReadyForDecode = false

    //---------------解码相关-----------------------
    protected var mCodec: MediaCodec? = null

    /**
     * 音视频数据读取器
     */
    protected var mExtractor: IExtractor? = null

//    /**
//     * 解码输入缓存区
//     */
//    protected var mInputBuffers: Array<ByteBuffer>? = null

//    /**
//     * 解码输出缓存区
//     */
//    protected var mOutputBuffers: Array<ByteBuffer>? = null

    /**
     * 解码数据信息
     */
    private var mBufferInfo = MediaCodec.BufferInfo()

    private var mState = DecodeState.STOP

    protected var mStateListener: IDecoderStateListener? = null

    /**
     * 流数据是否结束
     */
    private var mIsEOS = false

    protected var mVideoWidth = 0

    protected var mVideoHeight = 0

    /**
     * 开始解码时间，用于音视频同步
     */
    private var mStartTimeForSync = -1L

    // 是否需要音视频渲染同步
    private var mSyncRender = true

    private fun init(): Boolean {
        //1.检查参数是否完整
        if (mFilePath.isEmpty() || !File(mFilePath).exists()) {
            Log.w(TAG, "文件路径为空")
            mStateListener?.decoderError(this, "文件路径为空")
            return false
        }
        //调用虚函数，检查子类参数是否完整
        if (!check()) return false

        //2.初始化数据提取器
        mExtractor = initExtractor(mFilePath)
        if (mExtractor == null ||
            mExtractor!!.getFormat() == null
        ) return false

        //3.初始化参数
        if (!initParams()) return false

        //4.初始化渲染器
        if (!initRender()) return false

        //5.初始化解码器
        if (!initCodec()) return false
        return true
    }

    private fun initParams(): Boolean {
        try {
            val format = mExtractor!!.getFormat()!!
            mDuration = format.getLong(MediaFormat.KEY_DURATION) / 1000
            if (mEndPos == 0L) mEndPos = mDuration

            initSpecParams(mExtractor!!.getFormat()!!)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun initCodec(): Boolean {
        try {
            //1.根据音视频编码格式初始化解码器
            val type = mExtractor!!.getFormat()!!.getString(MediaFormat.KEY_MIME)
            mCodec = MediaCodec.createDecoderByType(type!!)
            //2.配置解码器
            if (!configCodec(mCodec!!, mExtractor!!.getFormat()!!)) {
                Log.d(TAG, "initCodec: waitDecode")
                waitDecode()
            }
            //3.启动解码器
            mCodec!!.start()

//            mCodec!!.setCallback(object : MediaCodec.Callback(){
//                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onOutputBufferAvailable(
//                    codec: MediaCodec,
//                    index: Int,
//                    info: MediaCodec.BufferInfo
//                ) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
//                    TODO("Not yet implemented")
//                }
//
//            })
//            //4.获取解码器缓冲区 使用 getInputBuffer 替代
//            mInputBuffers = mCodec?.inputBuffers
//            mOutputBuffers = mCodec?.outputBuffers
        } catch (e: Exception) {
            return false
        }
        return true
    }


    final override fun run() {
        if (mState == DecodeState.STOP) {
            mState = DecodeState.START
        }
        mStateListener?.decoderPrepare(this)

        //【解码步骤：1. 初始化，并启动解码器】
        if (!init()) return
        Log.i(TAG, "开始解码")
        try {
            while (mIsRunning) {
                if (mState != DecodeState.START &&
                    mState != DecodeState.DECODING &&
                    mState != DecodeState.SEEKING
                ) {
                    Log.i(TAG, "进入等待：$mState$this")
                    waitDecode()
                    // ---------【同步时间矫正】-------------
                    //恢复同步的起始时间，即去除等待流失的时间
                    mStartTimeForSync = System.currentTimeMillis() - getCurTimeStamp()
                }

                if (!mIsRunning ||
                    mState == DecodeState.STOP
                ) {
                    mIsRunning = false
                    break
                }

                //如果数据没有解码完毕，将数据推入解码器解码
                if (!mIsEOS) {
                    //【解码步骤：2. 将数据压入解码器输入缓冲】
                    mIsEOS = pushBufferToDecoder()
                }

                //【解码步骤：3. 将解码好的数据从缓冲区拉取出来】
                val index = pullBufferFromDecoder()
                if (index >= 0) {
                    // ---------【音视频同步】-------------
                    if (mSyncRender && (mState == DecodeState.DECODING)) {
                        sleepRender()
                    }
                    //【解码步骤：4. 渲染】
                    if (mSyncRender) {
                        mCodec!!.getOutputBuffer(index)?.let {
//                        render(mOutputBuffers!![index], mBufferInfo)
                            render(it, mBufferInfo)
                            //将解码数据传递出去
                            val frame = Frame()
                            frame.buffer = it
                            frame.setBufferInfo(mBufferInfo)
                            mStateListener?.decodeOneFrame(this, frame)
                        }
                    }

                    //【解码步骤：5. 释放输出缓冲】
                    mCodec!!.releaseOutputBuffer(index, true)
                    if (mState == DecodeState.START) {
                        mState = DecodeState.PAUSE
                    }
                }
                //【解码步骤：6. 判断解码是否完成】
                if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    mState = DecodeState.FINISH
                    mStateListener?.decoderFinish(this)
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "run: ", e)
        } finally {
            doneDecode()
            //【解码步骤：7. 释放解码器】
            release()
        }

    }

    private fun release() {
        try {
            mState = DecodeState.STOP
            mIsEOS = false
            mExtractor?.stop()
            mCodec?.stop()
            mCodec?.release()
            mStateListener?.decoderDestroy(this)
        } catch (e: Exception) {
        }
    }

    private fun pushBufferToDecoder(): Boolean {
        val inputBufferIndex = mCodec!!.dequeueInputBuffer(2000)
        var isEndOfStream = false

        if (inputBufferIndex >= 0) {
//            val inputBuffer = mInputBuffers!![inputBufferIndex]
            val inputBuffer = mCodec!!.getInputBuffer(inputBufferIndex)
            inputBuffer?.let {
                val sampleSize = mExtractor!!.readBuffer(inputBuffer)
                if (sampleSize < 0) {
                    //如果数据已经取完，压入数据结束标志：BUFFER_FLAG_END_OF_STREAM
                    mCodec!!.queueInputBuffer(
                        inputBufferIndex, 0, 0,
                        0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                    isEndOfStream = true
                } else {
                    mCodec!!.queueInputBuffer(
                        inputBufferIndex, 0,
                        sampleSize, mExtractor!!.getCurrentTimestamp(), 0
                    )
                }
            }

        }
        return isEndOfStream
    }

    /**
     *  判断index类型：
     *  MediaCodec.INFO_OUTPUT_FORMAT_CHANGED：输出格式改变了
     *  MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED：输入缓冲改变了
     *  MediaCodec.INFO_TRY_AGAIN_LATER：没有可用数据，等会再来
     *  大于等于0：有可用数据，index就是输出缓冲索引
     */
    private fun pullBufferFromDecoder(): Int {
        // 查询是否有解码完成的数据，index >=0 时，表示数据有效，并且index为缓冲区索引
        when (val index = mCodec!!.dequeueOutputBuffer(mBufferInfo, 1000)) {
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
            }
            MediaCodec.INFO_TRY_AGAIN_LATER -> {
            }
//            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
////                mOutputBuffers = mCodec!!.outputBuffers
//            }
            else -> {
                return index
            }
        }
        return -1
    }


    /**
     * 解码线程进入等待
     */
    private fun waitDecode() {
        try {
            if (mState == DecodeState.PAUSE) {
                mStateListener?.decoderPause(this)
            }
            synchronized(mLock) {
                mLock.wait()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 通知解码线程继续运行
     */
    protected fun notifyDecode() {
        synchronized(mLock) {
            mLock.notifyAll()
        }
        if (mState == DecodeState.DECODING) {
            mStateListener?.decoderRunning(this)
        }
    }

    override fun pause() {
        mState = DecodeState.DECODING
    }

    override fun goOn() {
        mState = DecodeState.DECODING
        notifyDecode()
    }

    override fun stop() {
        mState = DecodeState.STOP
        mIsRunning = false
        notifyDecode()
    }

    override fun isEncoding(): Boolean {
        return mState == DecodeState.DECODING
    }

    override fun isSeeking(): Boolean {
        return mState == DecodeState.SEEKING
    }

    override fun isStop(): Boolean {
        return mState == DecodeState.STOP
    }

    override fun setStateListener(l: IDecoderStateListener?) {
        mStateListener = l
    }

    override fun getWidth(): Int {
        return mVideoWidth
    }

    override fun getCurTimeStamp(): Long {
        return mBufferInfo.presentationTimeUs / 1000
    }

    override fun getHeight(): Int {
        return mVideoHeight
    }

    override fun getDuration(): Long {
        return mDuration
    }

    override fun getRotationAngle(): Int {
        return 0
    }

    override fun getMediaFormat(): MediaFormat? {
        return mExtractor?.getFormat()
    }

    override fun getTack(): Int {
        return 0
    }

    override fun getFilePath(): String {
        return mFilePath
    }

    private fun sleepRender() {
        val passTime = System.currentTimeMillis() - mStartTimeForSync
        val curTime = getCurTimeStamp()
        if (curTime > passTime) {
            Thread.sleep(curTime - passTime)
        }
    }

    /**
     * 渲染
     */
    abstract fun render(
        outputBuffers: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    )

    /**
     * 检查子类参数
     */
    abstract fun check(): Boolean

    /**
     * 初始化数据提取器
     */
    abstract fun initExtractor(path: String): IExtractor

    /**
     * 初始化子类自己特有的参数
     */
    abstract fun initSpecParams(format: MediaFormat)

    /**
     * 结束解码
     */
    abstract fun doneDecode()

    /**
     * 初始化渲染器
     */
    abstract fun initRender(): Boolean

    /**
     * 配置解码器
     */
    abstract fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean
}

class Frame {
    fun setBufferInfo(mBufferInfo: MediaCodec.BufferInfo) {
    }

    lateinit var buffer: ByteBuffer
}

enum class DecodeState {
    /**开始状态*/
    START,

    /**解码中*/
    DECODING,

    /**解码暂停*/
    PAUSE,

    /**正在快进*/
    SEEKING,

    /**解码完成*/
    FINISH,

    /**解码器释放*/
    STOP
}