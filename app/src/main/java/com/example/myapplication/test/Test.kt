package com.example.myapplication.test
//
//import com.example.myapplication.factory.Statuss
//import com.example.myapplication.rx.Observable
//import com.example.myapplication.rx.ObservableOnSubscribe
//import com.example.myapplication.rx.Observer
//import kotlinx.coroutines.*
//import java.net.URL
//
//
//fun main1() {
//    suspend fun test(): String {
//        return withContext(Dispatchers.IO) {
//            println(Thread.currentThread().name)
//            "test"
//        }
//    }
//
//    suspend fun getHtml(): String {
//        return withContext(Dispatchers.IO) { URL("https://www.baidu.com").readText() }
//    }
//    MainScope().launch(Dispatchers.Unconfined) {
//        println("thread name:::" + Thread.currentThread().name)
//        println(test())
//    }
//    runBlocking {
//        println("getHtml" + getHtml())
//    }
////    val async = GlobalScope.async {
////        println(Thread.currentThread().name)
////        return@async "hah"
////    }
////    GlobalScope.launch {
////        val await = async.await()
////        println("result :${Thread.currentThread().name} $await")
////    }
////    val newSingleThreadExecutor =
////        Executors.newSingleThreadExecutor()
////    Executors.newFixedThreadPool(1)
////    val stacks = Thread.getAllStackTraces()
////    val set: Set<Thread> = stacks.keys
////    for (key in set) {
////        val stackTraceElements = stacks[key]!!
////        println( "---- print thread: " + key.name + " start ----")
////        for (st in stackTraceElements) {
////            println("StackTraceElement: $st")
////        }
////        println( "---- print thread: " + key.name + " end ----")
////    }
////    val threadPoolExecutor = ThreadPoolExecutor(0, 2, 0L,TimeUnit.SECONDS, SynchronousQueue())
////    for (i in 1.. 10000){
////        newSingleThreadExecutor.submit {
////            Thread.sleep(10000)
////            println(Thread.currentThread().name + " running...$i")
////        }
////    }
//
////    val s3 = "我   是 一个         好  人啊"
////    val spString = s3.split("\\s+".toRegex()).toTypedArray()
////    for (ss in spString) {
////        println(ss)
////    }
////    newSingleThreadExecutor.shutdown()
////    println(newSingleThreadExecutor.isShutdown)
////    println(newSingleThreadExecutor.isTerminated)
//
//
////    newSingleThreadExecutor.execute {
////        for (i2 in 0..5) {
////            when (i2) {
////                3 -> {
////                    println("3")
////                    println(i2 / 0)
////
////                    throw Exception("can not ./ 0")
////                }
////                5 -> {
////                    println("over")
////                }
////                else -> {
////                    println(Thread.currentThread().name + " running...")
////                }
////            }
////        }
////    }
//
////    newSingleThreadExecutor.submit {
////
////        for (i2 in 0..5) {
////            when (i2) {
////                3 -> {
////                    println("3")
////                    println(i2 / 0)
////
////                    throw Exception("can not ./ 0")
////                }
////                5 -> {
////                    println("over")
////                }
////                else -> {
////                    println(Thread.currentThread().name + " running...")
////                }
////            }
////        }
////    }
////    for (i in 1.. 100){
//
//
////        for (i1 in 1.. 100){
////            println(Thread.currentThread().name + Thread.currentThread().state)
////            newSingleThreadExecutor.submit {
//////                Thread.sleep(3000)
////////                println(Thread.currentThread().name+Thread.currentThread().state)
////                for (i2 in 0..5) {
////                    when {
////                        i2 == 3 -> {
////                            println(i2 / 0)
////                        }
////                        i == 5 -> {
////                            println("over")
////                        }
////                        else -> {
////                            println(Thread.currentThread().name + " running...")
////                        }
////                    }
////                }
////            }
////        }
////        newSingleThreadExecutor.shutdown()
//
////    Observable.create(object :ObservableOnSubscribe<String>{
////        override fun subscribe(emitter: Observer<String>) {
////            emitter.onNext("fadfa ${Thread.currentThread().name} ")
////            emitter.onComplete()
////        }
////    }).map {
////        1
////    }.doOnNext {
////        2
////    }.observerOn(Schedulers.IO).subscribe(object : Observer<Int> {
////        override fun onSubscribe() {
//////            throw Exception("onSubscribe")
////        }
////
////        override fun onNext(item: Int) {
////
////            println("onnext$item")
//////            throw Exception("for test")
////        }
////
////        override fun onError(e: Throwable) {
////            println(e)
////        }
////
////        override fun onComplete() {
////            println("onComplete")
////        }
////
////    })
////        .setObserver(object : Observer<String> {
////            override fun onSubscribe() {
////                println("onSubscribe")
////            }
////
////            override fun onNext(item: String) {
////                println("下游接收到的数据$item")
////            }
////
////            override fun onError(e: Throwable) {
////            }
////
////            override fun onComplete() {
////            }
////
////        })
//
//    println(Statuss.FINISH)
//
//////        Stringhe
//    for (i in 0 until 1) {
//        println("fdasfa$i")
//    }
//    Observable.create(object : ObservableOnSubscribe<String> {
//        override fun subscribe(emitter: Observer<String>) {
//            println(Thread.currentThread().name)
//            emitter.onNext("abc")
//        }
//    }).doOnNext {
//        println(it)
//        "abcdfasf"
//    }
//        .subscribe(object : Observer<String> {
//            override fun onSubscribe() {
//            }
//
//            override fun onNext(item: String) {
//                println("${Thread.currentThread().name} $item")
//            }
//
//            override fun onError(e: Throwable) {
//            }
//
//            override fun onComplete() {
//            }
//
//        })
//
//}
//
//fun main() {
//    Observable.create(object : ObservableOnSubscribe<Int> {
//        override fun subscribe(emitter: Observer<Int>) {
//            val arrayListOf = arrayListOf<Int>(1, 2, 4, 3, 5)
//            for (i in arrayListOf) {
//                emitter.onNext(i)
//            }
//        }
//
//    }).filter {
//        println(it)
//        return@filter it > 3
//    }
//
//}
fun test():()-> Unit{
    var a = 3
    return fun(){
        a++
        println(a)
    }
}
fun main() {
    val t = test()
    t()
    t()
}