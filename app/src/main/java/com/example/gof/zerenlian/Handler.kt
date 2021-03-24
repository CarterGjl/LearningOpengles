package com.example.gof.zerenlian

interface Handler {
    fun process(request: Request): Boolean?
}