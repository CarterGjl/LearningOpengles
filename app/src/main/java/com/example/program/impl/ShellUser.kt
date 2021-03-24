package com.example.program.impl

import com.example.program.IUseShell

class ShellUser {

    private val iUseShell:IUseShell

    init {
        iUseShell = ShellImpl()
    }

    fun startGB() {
        println("开始上分")
        iUseShell.enableShell()
    }

    fun stopGB() {
        println("停止上分")
        iUseShell.disableShell()
    }
}

fun main() {
    val shellUser = ShellUser()
    shellUser.startGB()
}