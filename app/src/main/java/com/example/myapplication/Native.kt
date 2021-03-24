package com.example.myapplication

class Native {
    private external fun nativeInit()
    init {
        nativeInit()
    }
}