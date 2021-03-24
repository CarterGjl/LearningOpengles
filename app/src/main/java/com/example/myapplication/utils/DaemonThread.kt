package com.example.myapplication.utils

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message

class DaemonThread private constructor() {
    private  var mHandlerWorker: Handler? =null
    private  var mHandlerUi:Handler? = null
    private  var mWorkThread:HandlerThread? = null
    companion object{
        private const val IO_TASK = 1
        private const val NETWORK_TASK = 2
        private const val PERIODIC_TASK = 3
        val daemonThread by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DaemonThread()
        }
    }
    init {
        initUIHandler()
        initWorkHandler()
    }

    private fun initWorkHandler() {
        mWorkThread = HandlerThread("CustomWorkThread")
        mWorkThread!!.start()
        mHandlerWorker = Handler(mWorkThread!!.looper) { message->
            val runnable = message.obj as Runnable
            when (message.arg1) {
                PERIODIC_TASK -> if (message.arg2 > 0) {
//                    ThreadPoolManager.getInstance().addIOTask(runnable)
                    if (mHandlerWorker != null) {
                        val mMessage = mHandlerWorker!!.obtainMessage(message.what)
                        mMessage.arg1 = message.arg1
                        mMessage.arg2 = message.arg2
                        mMessage.obj = runnable
                        mHandlerWorker!!.sendMessageDelayed(mMessage, mMessage.arg2.toLong())
                    }

                }
                NETWORK_TASK -> {
//                    ThreadPoolManager.getInstance()
//                        .addSendCommandTask(runnable)
                }
                IO_TASK -> {
//                    ThreadPoolManager.getInstance()
//                        .addIOTask(runnable)
                }
                else -> {
//                    ThreadPoolManager.getInstance().addIOTask(runnable)
                }
            }
            true
        }

    }

    private fun initUIHandler() {
        mHandlerUi = Handler(Looper.getMainLooper()) { message: Message ->
            val runnable = message.obj as Runnable
            runnable.run()
            true
        }
    }
    fun quit(){
        mWorkThread?.quit()
    }

    fun runIOThread(runnable: Runnable) {
        if (mHandlerWorker != null) {
            val obtainMessage = mHandlerWorker!!.obtainMessage()
            obtainMessage.obj = runnable
            obtainMessage.arg1 = IO_TASK
            mHandlerWorker!!.sendMessage(obtainMessage)
        }

    }
    fun runIOThread(runnable: Runnable, delayMs: Long) {
        if (mHandlerWorker != null) {
            val obtainMessage = mHandlerWorker!!.obtainMessage()
            obtainMessage.obj =runnable
            obtainMessage.arg1 = IO_TASK
            mHandlerWorker!!.sendMessageDelayed(obtainMessage, delayMs)
        }
    }
    fun runPeriodicTask(runnable: Runnable?, what: Int, delayMs: Long, periodMs: Int) {
        if (mHandlerWorker != null) {
            val message: Message = mHandlerWorker!!.obtainMessage(what)
            message.obj = runnable
            message.arg1 =PERIODIC_TASK
            message.arg2 = periodMs
            mHandlerWorker!!.sendMessageDelayed(message, delayMs)
        }
    }
    fun runWorkThread(runnable: Runnable?) {
        if (mHandlerWorker != null) {
            val message: Message = mHandlerWorker!!.obtainMessage()
            message.obj = runnable
            message.arg1 = NETWORK_TASK
            mHandlerWorker!!.sendMessage(message)
        }
    }
}