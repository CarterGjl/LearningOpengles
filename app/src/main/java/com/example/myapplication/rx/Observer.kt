package com.example.myapplication.rx

interface Observer<T> {
    fun onSubscribe()
    fun onNext(item:T)
    fun onError(e:Throwable)
    fun onComplete()
}