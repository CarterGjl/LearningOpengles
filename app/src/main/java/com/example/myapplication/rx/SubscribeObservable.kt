package com.example.myapplication.rx

class SubscribeObservable<T>(
    private val source: ObservableOnSubscribe<T>,
    private val thread: Int
) : ObservableOnSubscribe<T> {
    override fun subscribe(emitter: Observer<T>) {
        val subscribeObserver = SubscribeObserver(emitter)
        Schedulers.INSTANCE.submitSubscribeWork(source, subscribeObserver, thread)
    }

    private class SubscribeObserver<T>(private val downStream: Observer<T>) : Observer<T> {
        override fun onSubscribe() {
            downStream.onSubscribe()
        }

        override fun onNext(item: T) {
            downStream.onNext(item)
        }

        override fun onError(e: Throwable) {
            downStream.onError(e)
        }

        override fun onComplete() {
            downStream.onComplete()
        }

    }
}