package com.example.myapplication.utils

import android.os.Bundle
import android.os.Handler
import android.os.Message
import java.util.*

class UIEvent private constructor(){
    companion object{
        private const val TAG = "UIEvent"
        val uiEvent:UIEvent by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            UIEvent()
        }
    }
    private  val needToRefreshList:ArrayList<Handler> by lazy { ArrayList() }

    @Synchronized
    fun register(item: Handler?) {
        if (item == null) {
            return
        }
        if (!needToRefreshList.contains(item)) {
            needToRefreshList.add(item)
        }
    }

    @Synchronized
    fun remove(item: Handler) {
        needToRefreshList.remove(item)
    }

    @Synchronized
    fun removeMessage(flag: Int) {
        if (needToRefreshList.isEmpty()) {
            return
        }
        for (item in needToRefreshList) {
            item.removeMessages(flag)
        }
    }

    @Synchronized
    fun notifications(msg: Message) {
        if (needToRefreshList.isEmpty()) {
            return
        }
        for (item in needToRefreshList) {
            item.sendMessage(msg)
        }
    }

    @Synchronized
    fun notifications(flag: Int, arg1: Int, arg2: Int, obj: Any?, data: Bundle?) {
        // LogUtil.v(TAG, needToReflashList.toString());
        if (needToRefreshList.isEmpty()) {
            return
        }
        for (item in needToRefreshList) {
            val msg = Message.obtain()
            msg.what = flag
            msg.arg1 = arg1
            msg.arg2 = arg2
            msg.obj = obj
            msg.data = data
            item.sendMessage(msg)
        }
    }
}