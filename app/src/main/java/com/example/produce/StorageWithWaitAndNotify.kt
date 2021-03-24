package com.example.produce

import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.locks.ReentrantLock

const val MAX_SIZE = 10
val lock = Object()
class StorageWithWaitAndNotify {
    private val list = LinkedList<Any>()
    fun produce() {
        synchronized(lock) {
            while (list.size == MAX_SIZE) {
                println("仓库已满")
                lock.wait()
            }
            list.add("a")
            println("生产了一个新产品，现库存为：" + list.size)
            lock.notifyAll()
        }
    }
    fun consume() {
        synchronized(lock) {
            while (list.isEmpty()){
                println("库存为0：消费暂停");
                lock.wait() // 暂停消费
            }

            val pop = list.pop()
            println("pop 消费 $pop")
            lock.notifyAll()
        }
    }

}

fun main() {
    var storageWithWaitAndNotify = StorageWithWaitAndNotify()

    Thread{
        storageWithWaitAndNotify.consume()
    }.start()
    Thread{
        storageWithWaitAndNotify.consume()
    }.start()
    Thread{
        storageWithWaitAndNotify.produce()
    }.start()
    Thread{
        storageWithWaitAndNotify.produce()
    }.start()

}
class StorageWithBlocQueue{
    private val list = LinkedList<Any>()
    private val reentrantLock = ReentrantLock()
    private val mEmpty = reentrantLock.newCondition()
    private val mFull = reentrantLock.newCondition()
    fun produce() {
        reentrantLock.lock()
        while (list.size == MAX_SIZE){
            println("缓冲区已经满了")
            mFull.await()
        }
        list.add("a")
        println("生产新产品 容量 ${list.size}")
        mEmpty.signalAll()
        reentrantLock.unlock()
    }
    fun consume(): Unit {
        reentrantLock.lock()
        while (list.isEmpty()){
            println("库存为0：消费暂停");
            mEmpty.await()
        }
        val pop = list.pop()
        println("pop 消费 $pop")
        mFull.signalAll()
        reentrantLock.unlock()
    }
}
class StorageWithBlockQueue{
    private val list = LinkedBlockingDeque<Any>()
    fun produce(): Unit {
        if (list.size == MAX_SIZE){
            println("缓冲区已经满了")
        }
        list.put("a")
        println("生产新产品 容量 ${list.size}")
    }
    fun consume(): Unit {
        if (list.isEmpty()){
            println("库存为0：消费暂停")
            return
        }
        val pop = list.pop()
        println("pop 消费 $pop")
    }
}