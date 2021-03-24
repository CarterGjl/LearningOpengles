package com.example.myapplication.rx

class FilterObservable<T>(
    private val source: ObservableOnSubscribe<T>,
    private val func: (T) -> Boolean
) : ObservableOnSubscribe<T> {
    override fun subscribe(emitter: Observer<T>) {
        source.subscribe(FilterObserver(emitter, func))
    }

    private class FilterObserver<T>(
        private val downStream: Observer<T>,
        private val func: (T) -> Boolean
    ) : Observer<T> {
        override fun onSubscribe() {
        }

        override fun onNext(item: T) {
            val invoke = func.invoke(item)
            if (invoke) {
                downStream.onNext(item = item)
            }
        }

        override fun onError(e: Throwable) {
            downStream.onError(e)
        }

        override fun onComplete() {
            downStream.onComplete()
        }

    }
}