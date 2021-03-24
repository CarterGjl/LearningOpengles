package com.example.myapplication.rx

class DoOnNextObservable<T>(
    private val source: ObservableOnSubscribe<T>,
    private val func: ((T) -> T)
) :ObservableOnSubscribe<T>{
    override fun subscribe(emitter: Observer<T>) {
        source.subscribe(DoOnNextObserver(emitter,func))
    }

    private class DoOnNextObserver<T>(private val emitter: Observer<T>, private val func: ((T)->T)):Observer<T> {
        override fun onSubscribe() {
            emitter.onSubscribe()
        }

        override fun onNext(item: T) {
            val invoke = func.invoke(item)
            emitter.onNext(invoke)
        }

        override fun onError(e: Throwable) {
            emitter.onError(e)
        }

        override fun onComplete() {
            emitter.onComplete()
        }
    }
}