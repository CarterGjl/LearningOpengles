package com.example.myapplication.rx

import java.util.concurrent.Executors

class Schedulers {
    private val service = Executors.newCachedThreadPool()

    //    private var handler = Handler(Looper.getMainLooper()){
//        it.callback.run()
//        return@Handler true
//    }
    companion object {
        val INSTANCE: Schedulers by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Schedulers()
        }
        const val IO = 0
        const val MAIN = 1
    }
    fun  submitObserverWork(function: () -> Unit,thread:Int) {
        when(thread){
            IO->{
                service.submit {
                    function.invoke() //调用高阶函数
                }
            }
            MAIN->{
//                handler?.let {
//                    val m=Message.obtain(it){
//                        function.invoke()//调用高阶函数
//                    }
//                    it.sendMessage(m)
//                }
            }
        }
    }

    fun <T> submitSubscribeWork(
        source: ObservableOnSubscribe<T>,
        downStream: Observer<T>,
        thread: Int
    ) {

        when (thread) {
            IO -> {
                service.submit {
                    source.subscribe(downStream)
                }
//                service.shutdown()
            }
            MAIN -> {
//                val message = Message.obtain(handler) {
//                    source.setObserver(downStream)
//                }
//                handler.sendMessage(message)
            }
        }
    }
}