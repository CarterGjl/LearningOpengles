package com.example.gof.zerenlian

import java.lang.RuntimeException

class HandlerChain {

    private val handlers = ArrayList<Handler>()

    fun addHandler(handler: Handler) {
        handlers.add(handler)
    }

    fun process(request: Request): Boolean {
        for (handler in handlers) {
            val process = handler.process(request = request)
            if (process != null) {
                return process
            }
        }
        throw RuntimeException("Could not handle request $request")
    }
}