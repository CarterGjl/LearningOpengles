package com.example

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class RequestHttp {

    fun request(): Unit {
        val request = Request.Builder()
            .url("")
            .addHeader("", "")
            .get().build()
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val newCall = okHttpClient.newCall(request = request)
        val response = newCall.execute()
    }
}