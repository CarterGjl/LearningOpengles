//package com.example
//
//import com.example.myapplication.rx.Observable
//import com.example.myapplication.rx.ObservableOnSubscribe
//import com.example.myapplication.rx.Observer
//
//class Test {
//    fun process(){
//        Observable.create(source = object : ObservableOnSubscribe<Boolean> {
//            override fun subscribe(emitter: Observer<Boolean>) {
//                //1 数据库取数据
//                emitter.onNext(true)
//            }
//        }).subscribe(downstream = object : Observer<Boolean>{
//            override fun onSubscribe() {
//            }
//
//            override fun onNext(item: Boolean) {
//            }
//
//            override fun onError(e: Throwable) {
//            }
//
//            override fun onComplete() {
//                // 4 更新ui
//            }
//
//        })
//    }
//}