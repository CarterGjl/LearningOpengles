package com.example.myapplication.rx

class Observable<T> constructor() {

    private var source: ObservableOnSubscribe<T>? = null

    constructor(emitter: ObservableOnSubscribe<T>) : this() {
        this.source = emitter
    }

    companion object {
        @JvmStatic
        fun <T> create(source: ObservableOnSubscribe<T>): Observable<T> {
            return Observable(source)
        }
    }

    fun doOnNext(func: (T) -> T): Observable<T> {
        val doOnNextObservable = DoOnNextObservable(this.source!!, func)
        return Observable(doOnNextObservable)
    }

    fun subscribeOn(thread: Int): Observable<T> {
        val subscribeObservable = SubscribeObservable(this.source!!, thread)
        return Observable(subscribeObservable)
    }

    fun observerOn(thread: Int): Observable<T> {
        val observerObservable = ObserverObservable(this.source!!, thread)
        return Observable(observerObservable)
    }
    fun <R> map(func: (T) -> R): Observable<R> {
        val mapObservable = MapObservable(this.source!!, func)
        return Observable(mapObservable)
    }

    fun filter(func: (T) -> Boolean): Observable<T> {
        val filterObservable = FilterObservable(this.source!!,func = func)
        return Observable(filterObservable)
    }

    fun subscribe(downstream: Observer<T>) {
        try {
            downstream.onSubscribe()
            source?.subscribe(downstream)
        } catch (e: Throwable) {
            downstream.onError(e)
            println(e)
        }
    }
}