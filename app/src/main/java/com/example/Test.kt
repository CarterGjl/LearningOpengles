//package com.example
//
//import com.example.myapplication.rx.Observable
//import com.example.myapplication.rx.ObservableOnSubscribe
//import com.example.myapplication.rx.Observer
//import com.example.myapplication.rx.Schedulers
//import kotlinx.coroutines.sync.Semaphore
//
//class Test {
//    fun process(){
//        Observable.create(source = object : ObservableOnSubscribe<Boolean> {
//            override fun subscribe(emitter: Observer<Boolean>) {
//                //1 数据库取数据
//                emitter.onNext(true)
//            }
//        }).subscribeOn(Schedulers.IO)
//            .subscribe(downstream = object : Observer<Boolean> {
//                override fun onSubscribe() {
//                    println(Thread.currentThread().name)
//                }
//
//                override fun onNext(item: Boolean) {
//                    println(Thread.currentThread().name)
//                }
//
//                override fun onError(e: Throwable) {
//                    println(Thread.currentThread().name)
//                }
//
//                override fun onComplete() {
//                    // 4 更新ui
//                    println(Thread.currentThread().name)
//                }
//
//            })
//    }
//}
//class H20(){
//    val h = Semaphore(2)
//    val o = Semaphore(0)
//
//    @Throws(InterruptedException::class)
//    suspend fun hydrogen(releaseHydrogen: Runnable) {
//        h.acquire(1)
//        // releaseHydrogen.run() outputs "H". Do not change or remove this line.
//        releaseHydrogen.run()
//    }
//
//    @Throws(InterruptedException::class)
//    fun oxygen(releaseOxygen: Runnable) {
//
//        // releaseOxygen.run() outputs "O". Do not change or remove this line.
//        releaseOxygen.run()
//    }
//}
//fun main() {
//    Test().process()
//}