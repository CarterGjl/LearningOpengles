package com.example.mediaprojection

class SingletonDemo private constructor() {
    companion object {
        @JvmStatic
        val instance: SingletonDemo by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SingletonDemo()
        }
    }
}

fun main() {
    SingletonDemo.instance
}