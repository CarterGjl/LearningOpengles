package com.example.myapplication.rx

class MapObservable<T, R>(
    private val source: ObservableOnSubscribe<T>,
    private val func: (T) -> R
) : ObservableOnSubscribe<R> {
    override fun subscribe(emitter: Observer<R>) {
        val mapObserver = MapObserver(emitter, func)
        source.subscribe(mapObserver)
    }


    private class MapObserver<T, R>(
        private val downStream: Observer<R>,
        private val func: ((T) -> R)
    ) :
        Observer<T> {
        override fun onSubscribe() {
            downStream.onSubscribe()
        }

        override fun onNext(item: T) {
            val invoke = func.invoke(item)
            downStream.onNext(invoke)
        }

        override fun onError(e: Throwable) {
            downStream.onError(e)
        }

        override fun onComplete() {
            downStream.onComplete()
        }

    }
}