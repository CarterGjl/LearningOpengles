package com.example.myapplication.rx

interface ObservableOnSubscribe<T> {
    fun subscribe(emitter: Observer<T>)
}