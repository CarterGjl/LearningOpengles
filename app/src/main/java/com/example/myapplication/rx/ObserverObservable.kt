package com.example.myapplication.rx

class ObserverObservable<T>(private val source: ObservableOnSubscribe<T>, private val thread: Int) :
    ObservableOnSubscribe<T> {
    override fun subscribe(emitter: Observer<T>) {
        val observerObserver = ObserverObserver(emitter, thread)
        source.subscribe(observerObserver)
    }

    private class ObserverObserver<T>(private val downStream: Observer<T>, private val thread: Int) :
        Observer<T> {
        override fun onSubscribe() {
            Schedulers.INSTANCE.submitObserverWork(function = {
                downStream.onSubscribe()
            }, thread = thread)
        }

        override fun onNext(item: T) {
            Schedulers.INSTANCE.submitObserverWork(function = {
                downStream.onNext(item)
            }, thread = thread)
        }

        override fun onError(e: Throwable) {
            Schedulers.INSTANCE.submitObserverWork(function = {
                downStream.onError(e)
            }, thread = thread)
        }

        override fun onComplete() {
            Schedulers.INSTANCE.submitObserverWork(function = {
                downStream.onComplete()
            }, thread = thread)
        }
    }
}